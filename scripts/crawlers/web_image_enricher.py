#!/usr/bin/env python3
"""High-precision, binary-verified web image discovery for missing hardware catalog entries."""

from __future__ import annotations

import argparse
import collections
import hashlib
import html as html_lib
import io
import json
import os
import re
import ssl
import sys
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Any

from PIL import Image, ImageStat

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)

from scripts.crawlers.image_curator import (
    _GPU_SUB_SERIES_GROUPS,
    _extract_gpu_sig,
    is_image_compatible_with_item,
)
from scripts.crawlers.state_manager import (
    CrawlStateManager,
    normalize_canonical_key,
)

FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

# MD5 hashes of known bad images (Itopya placeholder logo, ASUS 5-year warranty QR)
KNOWN_BAD_MD5 = frozenset({
    "117282a3fd403318316c63b652e4dcab",
    "a031b5efe882531a1de3cfa3d9bb27d3",
})

BLOCKED_WEB_DOMAINS = (
    "versus.com", "images.versus.io", "kleinanzeigen", "sahibinden", "letgo.",
    "olx.", "ebay.", "ebayimg.com", "aliexpress.", "alibaba.", "dhgate.",
    "temu.", "wish.", "mercari.", "carousell.", "tokopedia.", "bukalapak.",
    "poshmark.", "depop.", "vinted.", "wallapop.", "leboncoin.", "subito.",
    "willhaben.", "trademe.", "gumtree.", "craigslist.", "avito.", "youla.",
    "marktplaats.", "2dehands.", "tweakers.net/aanbod", "shutterstock.",
    "dreamstime.", "istockphoto.", "alamy.", "gettyimages.", "123rf.",
    "depositphotos.", "vecteezy.", "freepik.", "pinterest.", "youtube.",
    "youtu.be", "ytimg.", "tiktok.", "facebook.", "instagram.", "twitter.",
    "twimg.", "reddit.", "redd.it", "quora.", "prom.ua", "rozetka.",
    "userbenchmark.", "cpubenchmark.", "videocardbenchmark.", "passmark.",
    "nanoreview.", "kimovil.", "ricardostatic.", "ricardo.ch", "mercari.",
    "microless.com", "usermanual.wiki", "manualzz.", "manualslib.",
    "stockinthechannel.", "teknobiyotik.com", "profesionalreview.com",
    "pc-builds.com", "getsview.com", "routerpasswords.", "portforward.",
    "modemly.com", "192-168-1-1-ip.co", "login-page",
)

DISALLOWED_TITLE_PHRASES = (
    " vs ", " versus ", " comparison", " compare ", "karsilastirma", "karşılaştırma",
    "tier list", "benchmark", "unboxing", "teardown", "broken", "damaged",
    "repair", "for parts", "empty box", "box only", "sticker", "decal",
    "wallpaper", "schematic", "pinout", "diagram", "die shot", "delidded",
    "user manual", "datasheet", "review chart", "fps test", "price history",
    " badge", " logo", " icon", "mockup", "concept", "rumor", "leaked",
    "llegara", "llegará", "default password", "login screen", "admin login",
    "username:",
)

CAT_QUERY_SUFFIX = {
    "CPU": "processor box",
    "GPU": "graphics card",
    "MOTHERBOARD": "motherboard",
    "RAM": "ram memory",
    "STORAGE": "ssd",
    "PSU": "power supply",
    "COOLER": "cpu cooler",
    "CASE": "pc case",
    "LAPTOP": "laptop",
    "SMARTPHONE": "smartphone official",
    "TABLET": "tablet official",
    "ROUTER": "router",
    "UPS": "ups uninterruptible power supply",
    "KEYBOARD": "keyboard",
    "MOUSE": "mouse",
    "MOUSEPAD": "mousepad",
}

CATALOG_FILES: list[tuple[str, str | None]] = [
    ("cpu.json", "CPU"),
    ("video-card.json", "GPU"),
    ("motherboard.json", "MOTHERBOARD"),
    ("memory.json", "RAM"),
    ("internal-hard-drive.json", "STORAGE"),
    ("power-supply.json", "PSU"),
    ("cpu-cooler.json", "COOLER"),
    ("case.json", "CASE"),
    ("laptop.json", "LAPTOP"),
    ("smartphone.json", "SMARTPHONE"),
    ("tablet.json", "TABLET"),
    ("peripherals.json", None),
]

_STOP_WORDS = frozenset({
    "the", "and", "for", "with", "edition", "series", "gaming", "black",
    "pcie", "nvme", "ddr4", "ddr5", "atx", "matx", "micro", "mini",
    "itx", "tray", "mpk", "kutulu", "kutusuz", "fansiz", "siyah",
    "ekran", "karti", "anakart", "islemci", "sogutucu", "kasa",
    "freedos", "official", "processor", "graphics", "card", "motherboard",
    "power", "supply", "cooler", "case", "laptop", "smartphone", "tablet",
    "memory", "ram", "ssd", "hdd", "internal", "hard", "drive",
    "tam", "cnc", "aluminyum", "alüminyum", "elektrostatik", "kapasitif",
    "klavye", "fare", "kulaklik", "harici", "oasis", "sivi", "sıvı",
    "sogutma", "soğutma", "dunyanin", "dünyanın", "ilk", "ekrani", "ekranı",
    "cift", "çift", "modlu", "hediyeli", "canta", "çanta", "tasinabilir",
    "taşınabilir", "bilgisayar", "oyuncu", "magnezyum", "alasim", "alaşım",
    "mekanik", "klavyeli", "stealth", "modu", "full", "inc", "inç",
    "nesil", "katlanabilir", "periskop", "magcharge", "kamera", "kavisli",
    "ince", "telefonu", "matte", "xmp", "expo", "intel", "amd",
})


def _compute_bad_hashes() -> set[str]:
    hashes = set(KNOWN_BAD_MD5)
    for rel in ("data/images",):
        base = os.path.join(ROOT_DIR, rel)
        if not os.path.isdir(base):
            continue
        # Scan sample known repo assets if needed; already cleaned on disk
    return hashes


def _fold_turkish_ascii(text: str) -> str:
    """Folds Turkish dotted/dotless characters cleanly before lowercasing."""
    return (text or "").replace("İ", "I").replace("ı", "i").replace("Ş", "S").replace("ş", "s").replace("Ğ", "G").replace("ğ", "g").replace("Ü", "U").replace("ü", "u").replace("Ö", "O").replace("ö", "o").replace("Ç", "C").replace("ç", "c")


def _normalize_model_text(category: str, name: str, simplified: bool = False) -> str:
    """Normalizes catalog item names for both search queries and anchor extraction."""
    raw = _fold_turkish_ascii(name or "")
    raw = re.sub(r"\bexcercia\b", "Exceria", raw, flags=re.I)
    raw = re.sub(r"10\.?\s*nesil", "10th Gen", raw, flags=re.I)
    raw = re.sub(r"9\.?\s*nesil", "9th Gen", raw, flags=re.I)
    raw = re.sub(r"\(\s*(10th Gen|9th Gen|White)\s*\)", r" \1 ", raw, flags=re.I)
    raw = re.sub(r"\(\s*Katlanabilir\s*\)", " Fold ", raw, flags=re.I)
    raw = raw.replace("Katlanabilir", "Fold")
    if category in ("SMARTPHONE", "TABLET"):
        raw = raw.replace("+", " Plus ")

    clean = re.sub(r"\([^)]*\)", " ", raw)
    clean = re.sub(
        r"\b(?:Tray|MPK|Kutulu|Kutusuz|Fansız|Fansiz|OEM|Bulk|Wraith|FreeDOS|W11|Win11|Windows\s*\d+)\b",
        " ",
        clean,
        flags=re.I,
    )
    if category == "RAM":
        clean = re.sub(r"\bddr([45])[\-\s]*\d{4}\b", r"DDR\1", clean, flags=re.I)
        clean = re.sub(r"\b\d+\s*x\s*\d+\s*gb\b", " ", clean, flags=re.I)
        clean = re.sub(r"\b\d{1,4}\s*gb\b", " ", clean, flags=re.I)
        clean = re.sub(r"\bcl\d{2}\b", " ", clean, flags=re.I)
        clean = re.sub(r"\b\d{4}\s*(?:mhz|mt/s)\b", " ", clean, flags=re.I)
        clean = re.sub(r"\b(?:intel|amd|xmp|expo)\b", " ", clean, flags=re.I)
    elif category == "STORAGE":
        clean = re.sub(
            r"\b(?:1000|2000|4000|500|250|480|960|120|240|128|256|512)\s*gb\b|\b[1248]\s*tb\b",
            " ",
            clean,
            flags=re.I,
        )
    elif category == "PSU":
        clean = re.sub(r"\b(?:Orange|Grey|Gray|Green)\s+Label\b", " ", clean, flags=re.I)
        clean = re.sub(r"\bATX\s*3\.[x0-9]+\b", " ", clean, flags=re.I)
        clean = re.sub(r"\bV[12]\b", " ", clean, flags=re.I)
        if simplified:
            clean = re.sub(r"\b(VS|CX)\s+(\d{3,4})W?\b", r"\1\2", clean, flags=re.I)
    elif category == "LAPTOP":
        clean = re.sub(r"\b\d+(?:\.\d+)?\s*(?:inç|inc|\").*$", " ", clean, flags=re.I)
        clean = re.sub(r"\b(g\d{3})\.[a-z0-9\-]+", r"\1", clean, flags=re.I)
        clean = re.sub(r"\b\d+\s*(?:gb|tb|w|hz)\b", " ", clean, flags=re.I)
        clean = re.sub(
            r"\b(?:harici|oasis|sıvı|sivi|soğutma|sogutma|kutulu|dünyanın|dunyanin|ilk|ekranı|ekrani|"
            r"çift|cift|modlu|hediyeli|çanta|canta|taşınabilir|tasinabilir|bilgisayar|oyuncu|"
            r"magnezyum|alaşım|alasim|mekanik|klavyeli|stealth|modu|full|power|qhd\+?|fhd|uhd|"
            r"wqxga|wuxga|oled|mini-?led|ips|srgb|ram|ssd|cnc|alüminyum)\b",
            " ",
            clean,
            flags=re.I,
        )
        if simplified:
            # Strip regional sub-SKU tokens & CPU/GPU specs to match chassis family
            clean = re.sub(r"\b[a-z]{1,3}\d{3,4}[a-z]{1,3}\b", " ", clean, flags=re.I)
            clean = re.sub(r"\b\d{2}[a-z]{2}\d{2}[a-z0-9]{2,}\b", " ", clean, flags=re.I)
            clean = re.sub(r"-\s*[a-z]{2}\d{4}[a-z0-9]*", " ", clean, flags=re.I)
            clean = re.sub(r"\b(?:rtx|gtx|rx|core|ryzen|ultra|i[3579])[-\s]*\d+\w*\b.*$", " ", clean, flags=re.I)
        clean = " ".join(clean.split()[:6])
    elif category in ("KEYBOARD", "MOUSE", "MOUSEPAD"):
        clean = re.sub(
            r"\b(?:tam|cnc|alüminyum|aluminyum|elektrostatik|kapasitif|klavye|mekanik|fare|kablosuz|oyuncu)\b",
            " ",
            clean,
            flags=re.I,
        )

    return re.sub(r"\s+", " ", clean).strip()


def _build_search_query(category: str, brand: str, name: str, simplified: bool = False) -> str:
    clean = _normalize_model_text(category, name, simplified=simplified)
    if brand and not clean.lower().startswith(brand.lower()):
        clean = f"{brand} {clean}"
    if simplified:
        if category == "CPU":
            return f"{clean} processor".strip()
        if category == "UPS":
            return f"{clean} ups".strip()
        if category == "ROUTER":
            return f"{clean} router".strip()
        if category == "RAM":
            if "rgb" not in clean.lower():
                return f"{clean} non RGB ram memory".strip()
        if category == "TABLET":
            q_t = re.sub(r"\b(\d{1,2})th\s+gen\b", r"\1 2022", clean, flags=re.I)
            return f"{q_t} tablet".strip()
    suffix = CAT_QUERY_SUFFIX.get(category, "")
    if suffix and suffix.split()[0] not in clean.lower():
        return f"{clean} {suffix}".strip()
    return clean


def _search_bing_candidates(query: str, ssl_ctx: ssl.SSLContext, count: int = 12) -> list[dict[str, str]]:
    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
        ),
        "Accept": "text/html,application/xhtml+xml,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9",
    }
    url = (
        "https://www.bing.com/images/async?q="
        + urllib.parse.quote_plus(query)
        + f"&first=0&count={count}&scenario=ImageBasicHover&datsrc=N_RS&layout=RowBased&mmasync=1"
    )
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=8, context=ssl_ctx) as resp:
            body = resp.read().decode("utf-8", errors="ignore")
    except Exception:
        return []

    out: list[dict[str, str]] = []
    seen: set[str] = set()
    for raw_m in re.findall(r'm="([^"]+)"', body):
        try:
            d = json.loads(html_lib.unescape(raw_m))
        except Exception:
            continue
        murl = str(d.get("murl") or "").strip()
        if not murl.startswith("https://") or murl in seen:
            continue
        seen.add(murl)
        out.append({
            "murl": murl,
            "t": str(d.get("t") or ""),
            "purl": str(d.get("purl") or ""),
        })
    return out


def _search_wikimedia_candidates(query: str, ssl_ctx: ssl.SSLContext) -> list[dict[str, str]]:
    url = (
        "https://commons.wikimedia.org/w/api.php"
        f"?action=query&generator=search&gsrsearch={urllib.parse.quote_plus(query)}"
        "&gsrnamespace=6&prop=imageinfo&iiprop=url|mime&format=json&gsrlimit=6"
    )
    req = urllib.request.Request(
        url,
        headers={"User-Agent": "HardwareWizard/1.0 (https://github.com/omeryavuzyigit61/HardwareWizard)"},
    )
    try:
        with urllib.request.urlopen(req, timeout=8, context=ssl_ctx) as resp:
            data = json.loads(resp.read().decode("utf-8", errors="ignore"))
    except Exception:
        return []
    pages = (data or {}).get("query", {}).get("pages", {})
    if not isinstance(pages, dict):
        return []
    out: list[dict[str, str]] = []
    for p in sorted(pages.values(), key=lambda x: x.get("index", 999)):
        infos = p.get("imageinfo") or []
        if not infos:
            continue
        info = infos[0]
        u = str(info.get("url") or "")
        mime = str(info.get("mime") or "").lower()
        if u.startswith("https://") and mime in ("image/jpeg", "image/png", "image/webp"):
            out.append({"murl": u, "t": str(p.get("title") or ""), "purl": u})
    return out


def _extract_model_anchors(category: str, brand: str, name: str, simplified: bool = False) -> tuple[list[str], list[str]]:
    """Returns (required_numeric_or_sku_anchors, required_word_anchors) for strict title verification."""
    clean = _normalize_model_text(category, name, simplified=simplified).lower()
    if category == "PSU":
        clean = re.sub(r"\b(?:750|850|650|550|1000|1200)w\b", lambda m: m.group(0), clean)

    toks = [t for t in re.findall(r"[a-z0-9]+", clean) if t not in _STOP_WORDS]
    br_toks = set(re.findall(r"[a-z0-9]+", _fold_turkish_ascii(brand or "").lower()))
    keep_short_series = category in ("PSU", "COOLER", "MOTHERBOARD", "ROUTER", "UPS")
    short_ignore = {"atx", "oc", "ac", "dc", "v1", "v2", "v3", "pc", "in", "by", "to", "of", "on", "w"}
    anchors_num = [t for t in toks if len(t) >= 2 and any(ch.isdigit() for ch in t) and t not in br_toks]
    anchors_word = [
        t for t in toks
        if not any(ch.isdigit() for ch in t)
        and t not in br_toks
        and (len(t) >= 3 or (keep_short_series and t not in short_ignore and t.isalpha()))
    ]
    return anchors_num, anchors_word


def _candidate_matches_item(
    category: str,
    brand: str,
    name: str,
    chipset: str,
    cand: dict[str, str],
    simplified: bool = False,
) -> bool:
    murl = cand["murl"]
    low_u = murl.lower()
    low_p = re.sub(r"\+", " plus ", cand["purl"].lower())
    low_t = re.sub(r"\+", " plus ", cand["t"].lower())
    meta_all = f" {low_t} {low_p} {low_u} "
    meta_norm = re.sub(r"[^a-z0-9]+", " ", meta_all)
    # Split concatenated model words like reno12 -> reno 12 for cleaner numeric token matching
    meta_norm_split = re.sub(r"\b([a-z]{2,})(\d{1,3}[a-z]?)\b", r"\1 \2", meta_norm)
    meta_words = set(meta_norm.split()) | set(meta_norm_split.split())

    if any(b in low_u or b in low_p for b in BLOCKED_WEB_DOMAINS):
        return False
    if any(ph in f" {low_t} " for ph in DISALLOWED_TITLE_PHRASES):
        return False
    if "case" not in category.lower() and any(ph in low_t for ph in ("phone case", "protective case", "silicone case", "leather case", "screen protector", "tempered glass")):
        return False

    # Run core curator compatibility on both actual URL and cleaned synthetic metadata-path URL
    if not is_image_compatible_with_item(category, brand, name, chipset, murl):
        return False
    pseudo_t = re.sub(r"\b\d+\s*x\s*\d+\s*gb\b", " ", low_t)
    pseudo_t = re.sub(r"\b\d+(?:\.\d+)?\s*(?:gb|tb|mb|mhz|ghz|mt/s|mah|hz|w|cores?|threads?|c/\d+t)\b", " ", pseudo_t)
    pseudo_t = re.sub(r"\b(?:xmp|expo|cas\s*\d+|cl\d{2})\b", " ", pseudo_t)
    meta_slug = re.sub(r"[^a-z0-9]+", "-", f"{pseudo_t}-{urllib.parse.urlparse(low_p).path}".lower()).strip("-")
    pseudo_url = f"https://verified-meta.example/{meta_slug}.jpg"
    if not is_image_compatible_with_item(category, brand, name, chipset, pseudo_url):
        return False

    t_low = re.sub(r"\+", " plus ", _fold_turkish_ascii(f"{brand} {name} {chipset}").lower())
    t_low = re.sub(r"\bexcercia\b", "exceria", t_low)
    t_norm = re.sub(r"[^a-z0-9]+", " ", t_low)
    t_words = set(t_norm.split())

    # Symmetric color parity for components where White vs Black editions look visually distinct
    item_is_white = any(w in t_words for w in ("white", "beyaz", "snow", "ice", "silver"))
    cand_is_white = any(w in meta_words for w in ("white", "beyaz", "snow", "silver"))
    if category in ("RAM", "COOLER", "CASE", "GPU", "MOTHERBOARD", "PSU") and item_is_white != cand_is_white:
        return False
    if category == "PSU" and "non modular" in t_norm and re.search(r"\b(?:cx|rm|hx)\d{3,4}m\b", meta_norm):
        return False

    # Brand presence in metadata (or silicon maker for AMD/Intel/NVIDIA reference)
    br_low = (brand or (name.split()[0] if name.split() else "")).lower()
    br_clean = re.sub(r"[^a-z0-9]+", "", br_low)
    if br_clean and len(br_clean) >= 2:
        br_aliases = {br_clean}
        if br_clean == "gskill":
            br_aliases.add("g")
        if br_clean == "western":
            br_aliases.update({"wd", "westerndigital"})
        if br_clean == "lian":
            br_aliases.add("lianli")
        if br_clean == "be":
            br_aliases.add("bequiet")
        if br_clean == "cooler":
            br_aliases.add("coolermaster")
        if not any(a in meta_words or a in meta_norm.replace(" ", "") for a in br_aliases):
            return False

    # Category-specific strict anchor verification
    if category == "GPU":
        g1 = _extract_gpu_sig(t_low)
        g2 = _extract_gpu_sig(meta_norm)
        if not g1 or not g2 or g1 != g2:
            return False
        for sub_grp in _GPU_SUB_SERIES_GROUPS:
            ts = {s.replace("_", " ") for s in sub_grp if s.replace("_", " ") in t_norm}
            ms = {s.replace("_", " ") for s in sub_grp if s.replace("_", " ") in meta_norm}
            if ts and not (ts & ms):
                return False
            if ms and not ts and any(x in ms for x in ("infinity", "matrix", "astral", "suprim", "aorus", "strix")):
                return False
        return True

    if category == "CPU":
        if ("threadripper" in t_words) != ("threadripper" in meta_words):
            return False
        if ("pro" in t_words and "threadripper" in t_words) != ("pro" in meta_words and "threadripper" in meta_words):
            return False
        if ("x3d" in t_low) != ("x3d" in meta_norm):
            return False
        t_skus = re.findall(r"\b(\d{4,5}[a-z0-9]{0,4}|2[2345678][05]k?f?)\b", t_norm)
        m_skus = set(re.findall(r"\b(\d{4,5}[a-z0-9]{0,4}|2[2345678][05]k?f?)\b", meta_norm))
        if not t_skus:
            return False
        return all(sk in m_skus for sk in t_skus if len(sk) >= 4 or sk.endswith(("k", "f", "x")) or sk.isdigit())

    if category in ("SMARTPHONE", "TABLET"):
        t_clean = _normalize_model_text(category, f"{brand} {name}", simplified=False).lower()
        t_clean = re.sub(r"\b\d{4,5}\s*mah\b", " ", t_clean)
        gen_match = re.search(r"\b(\d{1,2})th\s+gen\b", t_clean)
        if gen_match:
            gen_n = gen_match.group(1)
            if not re.search(rf"\b(?:{gen_n}th|{gen_n}\b.*gen|ipad\s*{gen_n})\b", meta_norm_split):
                return False
            t_clean = re.sub(r"\b\d{1,2}th\s+gen\b", " ", t_clean)
        nums_t = set(re.findall(r"\b([a-z]?\d{1,3}[a-z]?)\b", t_clean)) - {"5g", "4g", "3g"}
        nums_m = set(re.findall(r"\b([a-z]?\d{1,3}[a-z]?)\b", meta_norm_split)) - {"5g", "4g", "3g"}
        if nums_t and not (nums_t <= nums_m):
            return False
        t_clean_words = set(re.sub(r"[^a-z0-9]+", " ", t_clean).split())
        for tier in ("ultra", "pro", "plus", "mini", "air", "fe", "fold", "flip", "max", "lite"):
            if (tier in t_clean_words) != (tier in meta_words):
                return False
        return True

    anchors_num, anchors_word = _extract_model_anchors(category, brand, name, simplified=simplified)
    meta_flat = meta_norm.replace(" ", "")
    for num_tok in anchors_num:
        if num_tok not in meta_words and num_tok not in meta_flat:
            return False
    if anchors_word:
        short_toks = [w for w in anchors_word if len(w) <= 2]
        long_toks = [w for w in anchors_word if len(w) >= 3]
        # Short 1-2 char model series codes (e.g. M/K/G/RS/VS/CX/VIC) must match exact word boundaries
        if any(w not in meta_words for w in short_toks):
            return False
        matched_w = sum(1 for w in long_toks if w in meta_words or w in meta_flat)
        req_w = min(len(long_toks), 2 if anchors_num else 3)
        if matched_w < req_w:
            return False
    return bool(anchors_num or anchors_word)


def _probe_image_with_hash(
    url: str,
    ssl_ctx: ssl.SSLContext,
    bad_hashes: set[str],
) -> tuple[bool, str | None]:
    """Downloads image with Android UA and verifies format, dimensions, aspect ratio, variance, returning (ok, md5)."""
    headers = {
        "User-Agent": (
            "HardwareWizard/1.0 (https://github.com/omeryavuzyigit61/HardwareWizard)"
            if "wikimedia.org" in url.lower()
            else "Dalvik/2.1.0 (Linux; U; Android 14)"
        ),
        "Accept": "image/webp,image/png,image/jpeg,image/*,*/*;q=0.8",
    }
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=10, context=ssl_ctx) as resp:
            if resp.status not in (200, 206):
                return False, None
            ct = str(resp.headers.get("Content-Type") or "").lower()
            if "svg" in ct or ("html" in ct):
                return False, None
            data = resp.read(3_500_000)
    except Exception:
        return False, None

    if len(data) < 3500:
        return False, None
    md5 = hashlib.md5(data).hexdigest()
    if md5 in bad_hashes:
        return False, None

    try:
        with Image.open(io.BytesIO(data)) as im:
            if im.format not in ("JPEG", "PNG", "WEBP"):
                return False, None
            w, h = im.size
            if min(w, h) < 160 or max(w, h) < 220:
                return False, None
            ratio = w / float(h)
            if ratio < 0.35 or ratio > 2.6:
                return False, None
            rgb = im.convert("RGB")
            stat = ImageStat.Stat(rgb)
            if max(stat.stddev) < 8.0:
                return False, None
    except Exception:
        return False, None
    return True, md5


def _probe_and_validate_image(
    url: str,
    ssl_ctx: ssl.SSLContext,
    bad_hashes: set[str],
) -> bool:
    """Downloads image with Android UA and verifies format, dimensions, aspect ratio, and visual variance."""
    ok, _ = _probe_image_with_hash(url, ssl_ctx, bad_hashes)
    return ok


def enrich_missing_items(
    categories_filter: set[str] | None = None,
    limit_per_cat: int = 0,
    workers: int = 10,
    target_per_item: int = 3,
    top_up_single_limit: int = 0,
) -> None:
    state = CrawlStateManager()
    bad_hashes = _compute_bad_hashes()
    ssl_ctx = ssl.create_default_context()

    # Load missing items (or single-image items when top_up_single_limit > 0) across all catalogs
    jobs: list[tuple[str, str, str, str, str, list[str], float]] = []
    seen_job_keys: set[tuple[str, str]] = set()
    for fn, fixed_cat in CATALOG_FILES:
        path = os.path.join(FILES_DIR, fn)
        if not os.path.exists(path):
            continue
        with open(path, "r", encoding="utf-8") as f:
            arr = json.load(f)
        for it in arr:
            u0 = it.get("imageUrl") or it.get("image_url")
            imgs0 = [x for x in (it.get("images") or []) if isinstance(x, str) and x.startswith("https://")]
            if not imgs0 and u0:
                imgs0 = [u0]
            if top_up_single_limit > 0:
                if len(imgs0) != 1:
                    continue
            else:
                if imgs0:
                    continue
            cat = fixed_cat or str(it.get("category") or "").upper()
            if not cat:
                continue
            if categories_filter and cat not in categories_filter:
                continue
            name = str(it.get("name") or it.get("model") or "").strip()
            brand = str(it.get("brand") or (name.split()[0] if name.split() else "")).strip()
            chipset = str(it.get("chipset") or "").strip()
            if not name:
                continue
            ckey = normalize_canonical_key(name)
            if (cat, ckey) in seen_job_keys:
                continue
            seen_job_keys.add((cat, ckey))
            price_val = float(it.get("price") or it.get("currentPriceTry") or 0.0)
            jobs.append((cat, ckey, brand, name, chipset, imgs0, price_val))

    if top_up_single_limit > 0:
        jobs.sort(key=lambda j: (1 if j[6] > 0 else 0, j[6]), reverse=True)
        jobs = jobs[:top_up_single_limit]
    elif limit_per_cat > 0:
        per_cat_cnt: collections.Counter[str] = collections.Counter()
        filtered_jobs = []
        for job in jobs:
            if per_cat_cnt[job[0]] < limit_per_cat:
                filtered_jobs.append(job)
                per_cat_cnt[job[0]] += 1
        jobs = filtered_jobs

    task_prefix = "img:web2_multi" if top_up_single_limit > 0 else "img:web2"
    pending_jobs: list[tuple[str, str, str, str, str, list[str], float]] = []
    for job in jobs:
        cat, ckey, brand, name, chipset, imgs0, _ = job
        task = f"{task_prefix}:{cat}:{ckey}"
        prog = state.get_progress(task)
        if prog and prog["status"] == "DONE":
            continue
        pending_jobs.append(job)

    mode_label = "single-image top-up" if top_up_single_limit > 0 else "missing-item"
    print(f"Total {mode_label} unique items: {len(jobs)} | Uncached web queries to run: {len(pending_jobs)}")
    if not pending_jobs:
        state.close()
        return

    bing_lock = threading.Lock()
    last_bing = [0.0]

    def _search_and_pick(
        cat: str,
        brand: str,
        name: str,
        chipset: str,
        q: str,
        simplified: bool,
        picked: list[str],
        seen_md5: set[str],
        limit_n: int,
    ) -> None:
        if len(picked) >= limit_n:
            return
        with bing_lock:
            now = time.monotonic()
            wait = 0.08 - (now - last_bing[0])
            if wait > 0:
                time.sleep(wait)
            last_bing[0] = time.monotonic()
        cands = _search_bing_candidates(q, ssl_ctx, count=12)
        if not simplified and cat in ("CPU", "SMARTPHONE", "TABLET") and len(cands) < 4:
            cands.extend(_search_wikimedia_candidates(f"{brand} {name}".strip(), ssl_ctx))
        for cand in cands:
            if len(picked) >= limit_n:
                break
            murl = cand["murl"]
            if murl in picked:
                continue
            if not _candidate_matches_item(cat, brand, name, chipset, cand, simplified=simplified):
                continue
            ok, md5 = _probe_image_with_hash(murl, ssl_ctx, bad_hashes)
            if ok and md5 and md5 not in seen_md5:
                seen_md5.add(md5)
                picked.append(murl)

    def _process_one(job: tuple[str, str, str, str, str, list[str], float]) -> tuple[tuple[str, str, str, str, str, list[str], float], list[str]]:
        cat, ckey, brand, name, chipset, existing_urls, _ = job
        picked: list[str] = list(existing_urls)
        seen_md5: set[str] = set()
        for eu in existing_urls:
            _, emd5 = _probe_image_with_hash(eu, ssl_ctx, bad_hashes)
            if emd5:
                seen_md5.add(emd5)

        query = _build_search_query(cat, brand, name, simplified=False)
        _search_and_pick(cat, brand, name, chipset, query, simplified=False, picked=picked, seen_md5=seen_md5, limit_n=target_per_item)
        if len(picked) < target_per_item and cat in ("CPU", "LAPTOP", "PSU", "UPS", "ROUTER", "TABLET", "RAM", "GPU", "MOTHERBOARD"):
            fb_query = _build_search_query(cat, brand, name, simplified=True)
            if fb_query and fb_query != query:
                _search_and_pick(cat, brand, name, chipset, fb_query, simplified=True, picked=picked, seen_md5=seen_md5, limit_n=target_per_item)
        return job, picked

    found = 0
    done = 0
    batch_raw: list[dict[str, Any]] = []
    batch_health: list[tuple[str, bool, int, str]] = []
    cat_found: collections.Counter[str] = collections.Counter()

    with ThreadPoolExecutor(max_workers=workers) as pool:
        futs = [pool.submit(_process_one, j) for j in pending_jobs]
        for fut in as_completed(futs):
            job, found_urls = fut.result()
            cat, ckey, brand, name, chipset, existing_urls, _ = job
            done += 1
            task = f"{task_prefix}:{cat}:{ckey}"
            new_only = [u for u in found_urls if u not in existing_urls]
            state.save_progress(task, "DONE", cursor=found_urls[0] if found_urls else "")
            if new_only:
                found += 1
                cat_found[cat] += 1
                batch_raw.append({
                    "category": cat,
                    "source_name": "Web Curated",
                    "canonical_key": ckey,
                    "display_name": name,
                    "brand": brand,
                    "payload": {"query": _build_search_query(cat, brand, name)},
                    "extra_specs": {},
                    "images": found_urls,
                    "source_url": found_urls[0],
                })
                for u in new_only:
                    batch_health.append((u, True, 200, "image/verified"))
            if len(batch_raw) >= 40:
                state.upsert_raw_batch(batch_raw)
                state.set_image_health_batch(batch_health)
                batch_raw.clear()
                batch_health.clear()
            if done % 100 == 0 or done == len(pending_jobs):
                print(f"  Progress: {done}/{len(pending_jobs)} | Enriched items: {found} {dict(cat_found)}", flush=True)

    if batch_raw:
        state.upsert_raw_batch(batch_raw)
        state.set_image_health_batch(batch_health)
    state.close()
    print(f"Web image enrichment complete: {found}/{len(pending_jobs)} items enriched (up to {target_per_item} photos/item).")


def main() -> int:
    parser = argparse.ArgumentParser(description="Enrich missing catalog images via verified web search")
    parser.add_argument("--cats", default="", help="Comma-separated category filter (e.g. CPU,GPU)")
    parser.add_argument("--limit-per-cat", type=int, default=0, help="Max items per category (0 = unlimited)")
    parser.add_argument("--top-up-single", type=int, default=0, help="Top up up to N single-image items with additional verified angles")
    parser.add_argument("--target-per-item", type=int, default=3, help="Max verified images to collect per item")
    parser.add_argument("--workers", type=int, default=10, help="Worker thread count")
    args = parser.parse_args()
    cats = {c.strip().upper() for c in args.cats.split(",") if c.strip()} or None
    enrich_missing_items(
        categories_filter=cats,
        limit_per_cat=args.limit_per_cat,
        workers=args.workers,
        target_per_item=max(1, args.target_per_item),
        top_up_single_limit=max(0, args.top_up_single),
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
