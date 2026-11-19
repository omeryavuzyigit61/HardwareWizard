"""Strict cross-category image authenticity validator, clone deduplicator, and multi-source gallery matcher."""

from __future__ import annotations

import collections
import json
import os
import re
import subprocess
from typing import Any
from urllib.parse import unquote, urlparse

from .sources_images import search_wikimedia_image, verify_image_url_pool
from .state_manager import (
    ROOT_DIR,
    CrawlStateManager,
    normalize_canonical_key,
    normalize_for_brand_match,
)

KNOWN_BRAND_TOKENS = (
    "asus", "msi", "gigabyte", "asrock", "sapphire", "xfx", "powercolor",
    "zotac", "palit", "pny", "gainward", "inno3d", "galax", "kfa2", "evga",
    "nvidia", "amd", "intel", "corsair", "gskill", "kingston", "crucial",
    "teamgroup", "patriot", "adata", "xpg", "samsung", "seagate", "kioxia",
    "lexar", "sabrent", "lenovo", "acer", "hp", "dell", "alienware", "apple",
    "xiaomi", "poco", "redmi", "oneplus", "google", "huawei", "honor", "oppo",
    "vivo", "realme", "motorola", "razer", "logitech", "steelseries", "keychron",
    "pulsar", "lamzu", "wooting", "hyperx", "noctua", "arctic", "lianli", "nzxt",
    "deepcool", "seasonic", "fsp", "bequiet", "thermaltake", "thermalright",
    "casper", "excalibur", "gamegaraj", "monsternotebook", "biostar", "maxsun",
    "colorful", "sparkle", "yeston",
)

BAD_HOST_SUBSTRS = (
    "static0.gamerantimages.com",
    "images.unsplash.com",
    "fdn.gsmarena.com/imgroot/static/stores/",
    "/static/stores/",
    "/stores/amazon-",
    "iconic-features",
    "icon_features",
    "feature-banner",
    "proart-rog-tuf",
    "5-year-qr",
    "qr-code",
    "qrcode",
    "garanti-uzatma",
    "itopya-logo",
    "itopya.png",
    "/logos/",
    "-logo.",
    "_logo.",
    "placeholder",
    "no-image",
    "no_image",
    ".svg",
    ".webm",
    ".mp4",
    ".ogv",
    "unboxing",
    "vergleich",
    "geekerwan",
    "_ihs_studio",
    "_reference.",
    "teknobiyotik.com",
    "profesionalreview.com",
    "pc-builds.com/logo",
    "getsview.com",
    "_logo_",
    "modemly.com",
    "192-168-1-1-ip.co",
    "login-page",
    "genericrouter",
    "_bottom.",
    "generic_cpu",
    "shutterstock",
    "dreamstime",
    "istockphoto",
    "alamy.",
    "gettyimages",
    "123rf.",
    "depositphotos",
    "vecteezy",
    "freepik",
    "versus.com",
    "images.versus.io",
    "kleinanzeigen",
    "sahibinden",
    "letgo.",
    "pinterest.",
    "youtube.com",
    "youtu.be",
    "ytimg.com",
    "tiktok.com",
    "facebook.com",
    "instagram.com",
    "ebayimg.com",
)

CPU_SOCKET_NUMBERS = frozenset({
    "1150", "1151", "1155", "1156", "1200", "1366",
    "1700", "1851", "1954", "2011", "2066", "3647", "4677",
})

GALLERY_SOURCE_PRIORITY = {
    "GamingGen Store API": 4,
    "Itopya Catalog": 4,
    "Repo Assets": 4,
    "GSMArena Gallery": 4,
    "Keychron Official": 4,
    "Pulsar Official": 4,
    "Secretlab Official": 4,
    "LethalGamingGear Official": 4,
    "TechPowerUp Ajax": 3,
    "IRONScale Labs": 3,
    "PCPartPicker Dataset": 3,
    "DXOMARK": 2,
    "Web Curated": 2,
    "GSMArena": 1,
}

_REPO_IMAGE_SUBDIRS = {
    "CPU": "cpu",
    "GPU": "gpu",
    "MOTHERBOARD": "motherboard",
    "RAM": "ram",
    "STORAGE": "storage",
    "PSU": "psu",
    "COOLER": "cooler",
    "CASE": "case",
}

_REPO_RAW_BASE = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main"

GPU_CHIP_PATTERNS = (
    re.compile(r"\b(rtx|gtx|rx|arc)[_\-\s]*(\d{3,4})\s*(ti\s*super|super|xtx|xt|ti|gre)?\b", re.I),
    re.compile(r"\b(b580|b570|b390|a770|a750|a580|a380)\b", re.I),
)

AIB_SERIES_TOKENS = (
    "astral", "matrix", "strix", "tuf", "prime", "proart", "dual", "phoenix",
    "suprim", "vanguard", "gaming_trio", "gaming_x", "expert", "inspire", "shadow", "ventus", "mech", "armor",
    "aero", "aorus", "eagle", "windforce", "gaming_oc",
    "nitro", "pulse", "pure", "hellhound", "red_devil", "fighter", "reaper",
    "taichi", "steel_legend", "phantom", "challenger",
    "mercury", "magnetic_air", "merc", "qick", "swft",
    "trinity", "amp_extreme", "amp", "solid", "twin_edge",
    "ichill", "twin_x2", "gamerock", "gamingpro", "jetstream", "ghost", "panther", "python",
    "ex_gamer", "1_click", "hof", "vulcan", "neptune", "ultra_w", "battle_ax",
    "verto", "xlr8", "epic_x", "liquid",
)

_GPU_SUB_SERIES_GROUPS = (
    ("master", "infinity", "xtreme", "waterforce", "elite"),
    ("astral", "matrix", "strix", "tuf", "proart", "prime", "dual"),
    ("suprim", "vanguard", "trio", "expert", "inspire", "shadow", "ventus", "mech"),
    ("gamerock", "gamingpro", "jetstream", "infinity"),
    ("nitro", "pulse", "pure"),
    ("red_devil", "hellhound", "fighter", "reaper"),
    ("vulcan", "neptune", "ultra", "battle_ax"),
    ("2x", "3x"),
)


def clean_doubled_gpu_name(name: str) -> str:
    """Strips repeated chipset phrases such as 'GeForce RTX 4060 Ti 16GB RTX 4060 Ti 16GB Dual'."""
    if not name:
        return name
    out = re.sub(
        r"\b((?:RTX|GTX|RX|Arc)\s+\d{3,4}(?:\s+(?:Ti\s+SUPER|Ti|SUPER|XTX|XT|GRE))?)\s*(\d+\s*GB)?\s+\1\b(?:\s+\2)?",
        lambda m: f"{m.group(1)} {m.group(2)}".strip() if m.group(2) else m.group(1),
        name,
        flags=re.I,
    )
    out = re.sub(r"\b(RX\s+\d{4})\s+\1\s+(XT|XTX|GRE)\b", r"\1 \2", out, flags=re.I)
    out = re.sub(r"\b(RTX\s+\d{4})\s+\1\s+(Ti\s+SUPER|Ti|SUPER)\b", r"\1 \2", out, flags=re.I)
    out = re.sub(r"\bGeForce\s+(RTX\s+\d{4}(?:\s+(?:Ti\s+SUPER|Ti|SUPER))?)\s+GeForce\s+RTX\s+\d{4}\s*(?:Ti\s*SUPER|Ti|SUPER)?\b", r"GeForce \1", out, flags=re.I)
    return re.sub(r"\s+", " ", out).strip()


def is_valid_gpu_product(name: str, chipset: str = "") -> bool:
    """Filters impossible brand×chipset hallucinations, phantom AIB series, leaked dates, and mobile iGPUs."""
    u = clean_doubled_gpu_name(f"{name} {chipset}").upper()
    if re.search(r"\b(?:JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)\s+\d{1,2}(?:ST|ND|RD|TH)?,?\s+20\d\d\b", u):
        return False
    if any(
        bad in u
        for bad in (
            "MOBILE", "MAX-Q", "LAPTOP", "EMBEDDED", "GRAPHICS ",
            "RADEON 610M", "RADEON 660M", "RADEON 680M", "RADEON 740M",
            "RADEON 760M", "RADEON 780M", "RADEON 840M", "RADEON 860M",
            "RADEON 880M", "RADEON 890M", "QUADRO", "RTX A", "RTX PRO", "CMP ",
        )
    ):
        return False

    brand = u.split()[0] if u.split() else ""
    is_nv = any(k in u for k in ("GEFORCE", "RTX ", "GTX "))
    is_amd = any(k in u for k in ("RADEON", "RX "))
    is_arc = "ARC " in u or "B580" in u or "B570" in u or "A770" in u or "A750" in u

    if brand in ("SAPPHIRE", "POWERCOLOR", "XFX") and (is_nv or is_arc):
        return False
    if brand == "ASROCK" and is_nv:
        return False
    if brand in ("ZOTAC", "PNY", "PALIT", "GAINWARD", "INNO3D", "GALAX", "KFA2", "EVGA") and (is_amd or is_arc):
        return False
    if brand == "EVGA" and any(k in u for k in ("RTX 40", "RTX 50")):
        return False
    if brand in ("ASUS", "MSI", "GIGABYTE") and is_arc:
        return False

    # Reject impossible AIB series × GPU generation hallucinations
    is_rtx20_30 = any(k in u for k in ("RTX 20", "RTX 30"))
    is_rtx50 = "RTX 50" in u
    if "ASTRAL" in u and not is_rtx50:
        return False
    if "PROART" in u and is_rtx20_30:
        return False
    if "PRIME" in u and is_rtx20_30:
        return False
    if brand == "GIGABYTE" and "AERO" in u and is_rtx20_30:
        return False
    if brand == "ZOTAC" and "AMP EXTREME AIRO" in u and is_rtx20_30:
        return False
    if "GAMING X SLIM" in u and (is_rtx20_30 or is_rtx50):
        return False
    if brand == "MSI" and "SUPRIM" in u and any(k in u for k in ("RTX 3050", "RTX 3060")):
        return False
    if brand == "MSI" and any(k in u for k in ("RX 7900 GRE", "RX 7800 XT", "RX 7700 XT", "RX 7600 XT")):
        return False
    if any(k in u for k in ("RTX 5090", "RTX 4090", "RTX 3090")) and any(s in u for s in ("DUAL", "TWIN EDGE", "EAGLE", "WIND FORCE", "VENTUS 2X")):
        return False
    if "RTX 5080" in u and "VENTUS 2X" in u:
        return False
    return True


def _extract_gpu_sig(text: str) -> str | None:
    low = re.sub(r"[_\-/]+", " ", (text or "").lower())
    for pat in GPU_CHIP_PATTERNS:
        m = pat.search(low)
        if m:
            parts = [g for g in m.groups() if g]
            return " ".join(" ".join(parts).split())
    # Fallback standalone 4-digit GPU chip number
    m2 = re.search(r"\b(5090|5080|5070|5060|5050|4090|4080|4070|4060|3090|3080|3070|3060|3050|9070|9060|7900|7800|7700|7600|6950|6900|6800|6750|6700|6650|6600)\b", low)
    if m2:
        return m2.group(1)
    return None


def is_image_compatible_with_item(
    category: str,
    brand: str,
    model: str,
    chipset: str,
    url: str,
) -> bool:
    """Strictly validates that a candidate image URL does not portray a different brand, chipset, or device generation."""
    if not url or not url.startswith("https://"):
        return False
    low_u = url.lower()
    if any(b in low_u for b in BAD_HOST_SUBSTRS):
        return False
    repo_prefix = f"{_REPO_RAW_BASE.lower()}/data/images/"
    if low_u.startswith(repo_prefix):
        rel_repo = urlparse(url).path.split("/data/images/", 1)[-1]
        if not os.path.isfile(os.path.join(ROOT_DIR, "data", "images", rel_repo.replace("/", os.sep))):
            return False

    parsed = urlparse(low_u)
    path = parsed.path
    # Opaque CDN hashes (cdna.pcpartpicker.com/<hex>, amazon /I/<asin>, shopify numeric files without names)
    stem = re.sub(r"\.[a-z0-9]+$", "", path.rsplit("/", 1)[-1])
    is_opaque_hash = bool(
        "cdna.pcpartpicker.com" in low_u
        or "m.media-amazon.com" in low_u
        or "images-na.ssl-images-amazon.com" in low_u
        or "pisces.bbystatic.com" in low_u
        or "media.ldlc.com" in low_u
        or "c1.neweggimages.com" in low_u
        or re.match(r"^[0-9a-f\-_]{16,}(?:[_\-]hi)?$", stem)
        or re.match(r"^\d{1,3}\-[0-9a-f]{5,12}$", stem)
        or re.match(r"^(?:kv[_\-]img|main|hero|gallery|product)[0-9_\-]*$", stem)
    )

    title_str = f"{brand} {model} {chipset}".strip()
    t_low = title_str.lower()
    t_flat = re.sub(r"[^a-z0-9]", "", t_low)
    b_flat = re.sub(r"[^a-z0-9]", "", (brand or "").lower())

    # 1. Explicit conflicting brand name in URL path
    if not is_opaque_hash:
        path_flat = re.sub(r"[^a-z0-9]", " ", path)
        path_words = set(path_flat.split())
        url_brands = [b for b in KNOWN_BRAND_TOKENS if b in path_words]
        # Exclude silicon makers on board-partner hardware, and exclude 'sapphire' watch-crystal term on wearables/phones/tablets
        skip_brands = {"nvidia", "amd", "intel"}
        if category in ("SMARTWATCH", "SMARTPHONE", "TABLET"):
            skip_brands.add("sapphire")
        non_silicon = [b for b in url_brands if b not in skip_brands]
        if non_silicon and b_flat and not any(b in t_flat or b_flat in b for b in non_silicon):
            # Allow Keychron CDN paths on Keychron/Lemokey sub-brand items, and Xiaomi on POCO/Redmi
            is_keychron_sub = "keychron" in non_silicon and ("lemokey" in t_flat or "lseries" in b_flat or "isokeyboards" in b_flat or "trackball" in b_flat or "orca" in t_flat)
            is_xiaomi_sub = "xiaomi" in non_silicon and any(sb in t_flat or sb in b_flat for sb in ("poco", "redmi"))
            if not (is_keychron_sub or is_xiaomi_sub):
                return False

    # 2. Category-specific strict semantic checks
    if category == "GPU":
        if "gpu-monkey.com" in low_u:
            # Must explicitly match card brand and exact GPU chip signature on gpu-monkey.com
            if b_flat and b_flat not in re.sub(r"[^a-z0-9]", "", path):
                return False
            t_sig_gm = _extract_gpu_sig(title_str)
            u_sig_gm = _extract_gpu_sig(path)
            if not t_sig_gm or not u_sig_gm or t_sig_gm != u_sig_gm:
                return False
        if not is_opaque_hash:
            t_sig = _extract_gpu_sig(title_str)
            u_sig = _extract_gpu_sig(path)
            if t_sig and u_sig:
                # Core number must match cleanly (4070 vs 4060 -> reject; 5070 ti vs 5070 -> reject)
                t_num = re.search(r"\d{3,4}", t_sig)
                u_num = re.search(r"\d{3,4}", u_sig)
                if t_num and u_num and t_num.group(0) != u_num.group(0):
                    return False
                if ("ti" in u_sig and "ti" not in t_sig) or ("ti" in t_sig and "ti" not in u_sig):
                    return False
                if ("xtx" in u_sig and "xtx" not in t_sig) or ("xtx" in t_sig and "xtx" not in u_sig):
                    return False
                if ("xt" in u_sig and "xt" not in t_sig and "xtx" not in t_sig) or ("xt" in t_sig and "xt" not in u_sig):
                    return False
                if ("super" in u_sig and "super" not in t_sig) or ("super" in t_sig and "super" not in u_sig):
                    return False
            # Also block AIB series, special bundle edition, color & liquid-cooling conflict in descriptive URLs
            t_norm_u = re.sub(r"[^a-z0-9]", " ", t_low)
            p_norm_u = re.sub(r"[^a-z0-9]", " ", path)
            for se in ("mandalorian", "onimusha", "black ops", "black_ops", "world of warcraft", "world-of-warcraft"):
                if se.replace("_", " ").replace("-", " ") in p_norm_u and se.replace("_", " ").replace("-", " ") not in t_norm_u:
                    return False
            t_aib = [s for s in AIB_SERIES_TOKENS if s.replace("_", " ") in t_norm_u or s in t_flat]
            u_aib = [s for s in AIB_SERIES_TOKENS if s.replace("_", " ") in p_norm_u or s in path]
            if t_aib and u_aib and not set(t_aib) & set(u_aib):
                return False
            if t_aib and not u_aib and "tpucdn.com" not in low_u:
                return False
            p_words_gpu = set(p_norm_u.split())
            t_words_gpu = set(t_norm_u.split())
            for sub_grp in _GPU_SUB_SERIES_GROUPS:
                t_sub = {s.replace("_", " ") for s in sub_grp if s.replace("_", " ") in t_norm_u or s in t_flat}
                u_sub = {s.replace("_", " ") for s in sub_grp if s.replace("_", " ") in p_norm_u or s in path}
                if t_sub and u_sub and not (t_sub & u_sub):
                    return False
                if u_sub and not t_sub and any(tok in p_words_gpu for tok in ("infinity", "matrix", "miku", "hatsune", "eva", "dhahab", "btf")):
                    return False
            for ed in ("miku", "hatsune", "eva", "evangelion", "gundam", "dhahab", "btf", "noctua", "sakura"):
                if (ed in p_words_gpu) != (ed in t_words_gpu):
                    return False
            if ("liquid" in t_low) != ("liquid" in path):
                return False
            u_white_g = any(w in p_words_gpu for w in ("white", "beyaz", "ice", "aero", "snow"))
            t_white_g = any(w in t_words_gpu for w in ("white", "beyaz", "ice", "aero", "snow"))
            if u_white_g != t_white_g:
                return False

    elif category == "CPU":
        if any(bad_s in low_u for bad_s in ("_ihs_studio", "_bottom", "generic_", "_reference", "unterseite", "backside")):
            return False
        if not is_opaque_hash:
            p_norm_cpu = re.sub(r"[^a-z0-9]", " ", path)
            p_words_cpu = set(p_norm_cpu.split())
            t_words_cpu = set(re.split(r"[^a-z0-9]+", t_low))
            if ("threadripper" in p_words_cpu) != ("threadripper" in t_words_cpu):
                return False
            if ("epyc" in p_words_cpu) != ("epyc" in t_words_cpu):
                return False
            if ("ultra" in p_words_cpu) != ("ultra" in t_words_cpu) and ("intel" in t_low):
                return False
            if ("x3d" in p_words_cpu) != ("x3d" in t_words_cpu) and ("x3d" in p_words_cpu or any(w.endswith("x3d") for w in t_words_cpu) or any(w.endswith("x3d") for w in p_words_cpu)):
                if any("x3d" in w for w in p_words_cpu) != any("x3d" in w for w in t_words_cpu):
                    return False
            t_nums = set(re.findall(r"(?:^|[^a-z0-9])(\d{4,5}[a-z0-9]{0,4}|2[2345678][05]k?f?)(?:$|[^a-z0-9])", t_low))
            path_clean_cpu = re.sub(r"\b\d{3,4}(?:gb|mb|tb|mhz|hz|p|w)\b", " ", re.sub(r"[-_/]", " ", path))
            path_clean_cpu = re.sub(r"(?:^|\s)0\d{3}(?=\s|[a-z])", " ", path_clean_cpu)
            u_nums = set(re.findall(r"(?:^|[^a-z0-9])(\d{4,5}[a-z0-9]{0,4}|2[2345678][05]k?f?)(?:$|[^a-z0-9])", path_clean_cpu))
            bad = [
                n for n in u_nums
                if n not in CPU_SOCKET_NUMBERS
                and n not in t_nums
                and not re.match(r"^20[12]\d$", n)
                and n not in {"1080", "1440", "2160", "1600", "1200", "1000"}
                and len(n) >= 4
            ]
            if bad and t_nums:
                return False

    elif category == "LAPTOP":
        if "hp-250-255-g10" in low_u and "250" not in t_low and "255" not in t_low:
            return False
        if not is_opaque_hash:
            # Conflicting RTX/RX GPU tier or chassis series in URL
            t_gpu = _extract_gpu_sig(t_low)
            u_gpu = _extract_gpu_sig(path)
            if t_gpu and u_gpu and re.search(r"\d{4}", t_gpu) and re.search(r"\d{4}", u_gpu):
                if re.search(r"\d{4}", t_gpu).group(0) != re.search(r"\d{4}", u_gpu).group(0):
                    return False
            # Strict laptop series exclusivity across brands
            lap_series = (
                "loq", "legion", "thinkbook", "thinkpad", "ideapad", "yoga",
                "victus", "omen", "envy", "pavilion", "spectre",
                "strix", "scar", "tuf", "zephyrus", "flow", "vivobook", "zenbook", "proart",
                "katana", "raider", "vector", "thin", "cyborg", "prestige", "stealth", "sword", "pulse", "modern", "summit", "creator", "titan",
                "nitro", "predator", "helios", "triton", "swift", "aspire",
                "alienware", "xps", "inspiron", "latitude", "excalibur", "abra", "tulpar", "semruk",
            )
            p_lap_words = set(re.split(r"[^a-z0-9]+", path))
            t_lap_words = set(re.split(r"[^a-z0-9]+", t_low))
            for ch in lap_series:
                if ch in p_lap_words and ch not in t_lap_words:
                    return False
            m_g_u = re.search(r"\bg(\d{3})\b", path)
            m_g_t = re.search(r"\bg(\d{3})\b", t_low)
            if m_g_u and m_g_t and m_g_u.group(1) != m_g_t.group(1):
                return False
            m_casp_u = re.search(r"g(\d{3})[._-](\d{4})", path)
            m_casp_t = re.search(r"g(\d{3})[.\s\-]?(\d{4})", t_low)
            if m_casp_u and m_casp_t and m_casp_u.groups() != m_casp_t.groups():
                return False

    elif category in ("SMARTPHONE", "TABLET"):
        if not is_opaque_hash:
            slug_raw = unquote(stem).replace("+", " plus ")
            # Strip date stamps like 2025-01-26, 2024年2月20日, or trailing _001/_002 photo indices in Wikimedia filenames
            slug_raw = re.sub(r"20[12]\d(?:[^a-z0-9]+\d{1,2}){1,2}[^a-z]*", " ", slug_raw)
            slug_raw = re.sub(r"[_\-]\d{3}$", "", slug_raw)
            slug = re.sub(r"(\-\d+)?\.(jpg|jpeg|png|webp)$", "", slug_raw)
            slug = re.sub(r"\b(fold|flip|xcover|tab|pad)(\d)", r"\1-\2", slug)
            t_norm = re.sub(r"\b(fold|flip|xcover|tab|pad)(\d)", r"\1 \2", t_low)
            t_norm = re.sub(r"\b\d{4,5}\s*mah\b", " ", t_norm)
            t_norm = t_norm.replace("+", " plus ").replace("10. nesil", "10th 2022").replace("9. nesil", "9th 2021 102")
            t_norm_flat = re.sub(r"[^a-z0-9]", "", t_norm)
            t_norm_words = set(re.findall(r"[a-z0-9]+", t_norm))
            # Tablet must NEVER display a smartphone photo (iphone / galaxy-s / xiaomi-14 / pura)
            if category == "TABLET":
                if any(k in slug for k in ("iphone", "galaxy-s2", "galaxy-a5", "galaxy-z", "xiaomi-14", "xiaomi-15", "pura", "poco-")):
                    return False
                if "ipad" in t_norm and "ipad" not in slug:
                    return False
                if "pad" in t_norm and "pad" not in slug and "tab" not in slug:
                    return False
            # Generation number & suffix tokens must match closely
            # Strip storage/RAM capacity & resolution tags so "Galaxy S23 128GB" matches "Galaxy S23"
            slug_no_cap = re.sub(r"\b\d{2,4}(?:gb|tb|mah|mp|hz|p)\b", " ", slug)
            slug_toks = [t for t in re.split(r"[^a-z0-9]+", slug_no_cap) if t and t not in {"5g", "4g", "lte", "sm", "2024", "2025", "2026", "1", "2"}]
            # Require at least one distinguishing alphanumeric model number from URL slug to appear in title!
            model_num_toks = [t for t in slug_toks if re.search(r"\d", t) and not re.match(r"^[a-z]\d{3}$", t)]
            for nt in model_num_toks:
                if nt not in t_norm_flat and nt not in t_norm_words:
                    if nt == "102" and ("9. nesil" in t_low or "9th" in t_low or "10.2" in t_low):
                        continue
                    if nt == "2022" and ("m2" in t_norm_words or "air 5" in t_norm or "10. nesil" in t_low):
                        continue
                    if nt == "2021" and ("mini 6" in t_norm or "9. nesil" in t_low):
                        continue
                    if "upload.wikimedia.org" in low_u and nt in ("01", "02", "03"):
                        continue
                    return False
            for tier in ("ultra", "pro", "plus", "mini", "air", "fe", "fold", "flip", "max", "lite"):
                # e.g. don't show 'ultra' image on base model, or 'pro' on base
                if tier in slug_toks and tier not in t_norm_words:
                    return False
                # e.g. don't show base 'Redmi Note 14' packshot on 'Redmi Note 14 Pro+'
                if tier in ("ultra", "pro", "fold", "flip", "mini") and tier in t_norm_words and tier not in slug_toks:
                    return False
            # Sub-brand line parity (prevents 'xiaomi-14-pro' matching 'Xiaomi Redmi Note 14 Pro+')
            for line_tok in ("redmi", "poco", "note", "nord", "pixel", "moto", "xperia"):
                if (line_tok in t_norm_words) != (line_tok in slug_toks) and line_tok in t_norm_words:
                    if line_tok in ("redmi", "poco", "note"):
                        return False
            if "plus" in t_norm_words and "pro" in slug_toks and "plus" not in slug_toks and "plus" not in t_low.replace("+", "") and "gsmarena.com" in low_u:
                return False
            # Roman numeral generation check (e.g. Xperia 1 V vs Xperia 1 VI / base Xperia 1)
            for rom in ("ii", "iii", "iv", "v", "vi"):
                if rom in t_norm_words or rom in slug_toks:
                    if ("xperia" in t_norm_words or "zenfone" in t_norm_words or "rog" in t_norm_words) and (rom in t_norm_words) != (rom in slug_toks):
                        return False
        if "nothing_phone.jpg" in low_u and "(1)" not in t_low:
            return False

    elif category == "RAM":
        if not is_opaque_hash:
            if ("notebook-ram" in path or "sodimm" in path) and "sodimm" not in t_low and "notebook" not in t_low:
                return False
            if ("server-premier" in path or "ecc-registered" in path or "sunucu-ram" in path) and "ecc" not in t_low:
                return False
            ram_series = (
                "vengeance", "dominator", "trident", "royal", "ripjaws", "flare", "lancer",
                "spectrix", "beast", "renegade", "impact", "venom", "viper",
                "thor", "ares", "signature", "delta", "vulcan", "cras",
            )
            t_rs = {s for s in ram_series if s in t_low}
            u_rs = {s for s in ram_series if s in path}
            if u_rs and t_rs and not (t_rs & u_rs):
                return False
            path_words_ram = set(re.split(r"[^a-z0-9]+", path))
            title_words_ram = set(re.split(r"[^a-z0-9]+", t_low))
            if ("blade" in path_words_ram) != ("blade" in title_words_ram):
                return False
            if ("pro" in path_words_ram) != ("pro" in title_words_ram):
                return False
            if ("titanium" in path_words_ram) != ("titanium" in title_words_ram):
                return False
            if ("platinum" in path_words_ram) != ("platinum" in title_words_ram):
                return False
            if ("ddr4" in path_words_ram and "ddr5" in title_words_ram) or ("ddr5" in path_words_ram and "ddr4" in title_words_ram):
                return False
            if ("rgb" in path_words_ram) and "rgb" not in title_words_ram:
                return False
            u_white = any(w in path_words_ram for w in ("white", "beyaz", "silver"))
            t_white = any(w in title_words_ram for w in ("white", "beyaz", "silver"))
            if u_white != t_white:
                return False

    elif category == "STORAGE":
        if not is_opaque_hash:
            st_models = (
                "990-pro", "990-evo-plus", "990-evo", "980-pro", "970-evo-plus", "870-evo",
                "mp700-pro", "mp600-pro", "mp600-elite", "mp600-core", "kc3000", "fury-renegade",
                "nv3", "nv2", "sn850x", "sn770", "sn580", "t705", "t700", "p3-plus", "p5-plus", "e100",
                "nm790", "nm710", "nm610", "exceria-pro", "exceria-plus-g3", "exceria-plus", "exceria-g2",
                "m570", "m480", "m460", "m461", "m371", "s70-blade",
            )
            p_norm_st = re.sub(r"[^a-z0-9]+", "-", path)
            t_norm_st = re.sub(r"[^a-z0-9]+", "-", t_low)
            u_st = {m for m in st_models if m in p_norm_st}
            t_st = {m for m in st_models if m in t_norm_st}
            if u_st and t_st and not (u_st & t_st):
                return False
            p_st_words = set(re.split(r"[^a-z0-9]+", path))
            t_st_words = set(re.split(r"[^a-z0-9]+", t_low))
            for mod in ("xt", "se", "plus", "evo", "pro", "heatsink", "g2", "g3"):
                if mod in p_st_words and mod not in t_st_words:
                    return False

    elif category == "PSU":
        if not is_opaque_hash:
            path_no_ext = re.sub(r"\.[a-z0-9]+$", "", path)
            p_norm_psu = re.sub(r"[^a-z0-9]+", " ", path_no_ext)
            t_norm_psu = re.sub(r"[^a-z0-9]+", " ", t_low)
            psu_series = (
                "rmx shift", "rmx", "rme", "hxi", "sf l", "thor", "loki", "edge",
                "core reactor", "cybercore", "vita", "hydro g", "hydro ptm", "toughpower", "vertex", "focus",
            )
            u_ps = {s for s in psu_series if s in p_norm_psu}
            t_ps = {s for s in psu_series if s in t_norm_psu}
            if u_ps and t_ps and not (u_ps & t_ps):
                return False
            if ("shift" in p_norm_psu.split()) != ("shift" in t_norm_psu.split()):
                return False
            u_wh_p = any(w in p_norm_psu.split() for w in ("white", "beyaz", "snow"))
            t_wh_p = any(w in t_norm_psu.split() for w in ("white", "beyaz", "snow"))
            if u_wh_p != t_wh_p:
                return False
            for rom in ("ii", "iii", "iv"):
                if (rom in p_norm_psu.split()) != (rom in t_norm_psu.split()):
                    return False
            w_t = re.search(r"\b(\d{3,4})\s*w\b", t_norm_psu)
            w_u = re.findall(r"\b(\d{3,4})w\b", p_norm_psu) or re.findall(r"\b(\d{3,4})\s+w\b", p_norm_psu)
            if w_t and w_u and w_t.group(1) not in w_u:
                return False

    elif category == "CASE":
        if not is_opaque_hash:
            p_words_case = set(re.split(r"[^a-z0-9]+", path))
            t_words_case = set(re.split(r"[^a-z0-9]+", t_low))
            u_white_c = any(w in p_words_case for w in ("white", "beyaz", "snow"))
            t_white_c = any(w in t_words_case for w in ("white", "beyaz", "snow"))
            if u_white_c != t_white_c:
                return False

    elif category == "COOLER":
        if not is_opaque_hash:
            # Don't show 240mm / AE24 photo on AD36 / 360mm
            for sz in ("120", "140", "240", "280", "360", "420"):
                if re.search(rf"(?:^|[^0-9]){sz}(?:$|[^0-9])", path):
                    if sz not in t_low and not (sz == "360" and "36" in t_low) and not (sz == "240" and "24" in t_low):
                        return False
            m_cool = re.search(r"\b([a-z]{2}\d{2})\b", path)
            if m_cool and m_cool.group(1) not in t_flat:
                return False
            p_cool_words = set(re.split(r"[^a-z0-9]+", path))
            t_cool_words = set(re.split(r"[^a-z0-9]+", t_low))
            if any(w in p_cool_words for w in ("white", "beyaz", "snow")) != any(w in t_cool_words for w in ("white", "beyaz", "snow")):
                return False
            for rom in ("ii", "iii", "iv"):
                if (rom in p_cool_words) != (rom in t_cool_words) and ("atmos" in t_cool_words or "coreliquid" in t_cool_words or "galahad" in t_cool_words or "ryujin" in t_cool_words):
                    return False
            for c_ser in ("skeleton", "vortex", "kraken", "titan", "nautilus", "coreliquid", "atmos", "galahad", "hydroshift", "ryujin", "proart"):
                if c_ser in p_cool_words and c_ser not in t_cool_words:
                    return False

    elif category in ("MOUSE", "KEYBOARD", "HEADSET", "ROUTER", "UPS", "MONITOR"):
        if not is_opaque_hash:
            p_per_words = set(re.split(r"[^a-z0-9]+", path))
            t_per_words = set(re.split(r"[^a-z0-9]+", t_low))
            if category == "MOUSE" and "arctis" in path and "arctis" not in t_low:
                return False
            if any(w in p_per_words for w in ("white", "beyaz", "snow")) != any(w in t_per_words for w in ("white", "beyaz", "snow")):
                return False
            if "superlight" in t_per_words and "2" in t_per_words and "2" not in p_per_words:
                return False

    elif category == "MOTHERBOARD":
        if not is_opaque_hash:
            m_mb = re.search(r"(?:^|[^a-z0-9])([abxz]\d{3}[a-z]{0,2}(?:[-_][a-z0-9]{1,2})?|trx\d0|wrx\d0)(?:$|[^a-z0-9])", path)
            if m_mb:
                mb_full = re.sub(r"[^a-z0-9]", "", m_mb.group(1))
                mb_chip = re.match(r"([abxz]\d{3}[a-z]{0,2}|trx\d0|wrx\d0)", m_mb.group(1)).group(1)
                is_nzxt_b650e = ("nzxt" in t_flat and "n7" in t_flat and "b650" in t_flat and mb_chip == "b650e")
                if not is_nzxt_b650e and (mb_chip not in t_flat or (mb_full != mb_chip and mb_full not in t_flat)):
                    return False
            t_mb_norm = re.sub(r"[-_/]+", " ", t_low)
            p_mb_norm = re.sub(r"[-_/]+", " ", path)
            for ms in (
                "tomahawk", "mortar", "carbon", "godlike", "ace", "edge", "gaming plus",
                "pro rs", "steel legend", "taichi", "pg lightning", "riptide",
                "phantom gaming", "aorus", "tuf", "prime", "proart", "strix",
                "crosshair", "hero", "apex", "btf", "max gaming", "nitro", "pure", "pulse",
            ):
                if ms in p_mb_norm and ms not in t_mb_norm:
                    return False
            if ("ice" in p_mb_norm.split()) != ("ice" in t_mb_norm.split()):
                return False

    return True


_OPAQUE_URL_TITLES_CACHE: dict[str, list[str]] | None = None


def _get_opaque_url_titles() -> dict[str, list[str]]:
    global _OPAQUE_URL_TITLES_CACHE
    if _OPAQUE_URL_TITLES_CACHE is not None:
        return _OPAQUE_URL_TITLES_CACHE
    mapping: dict[str, list[str]] = collections.defaultdict(list)
    try:
        st = CrawlStateManager()
        rows = st.conn.execute(
            "SELECT display_name, images_json FROM raw_records WHERE source_name IN ('Itopya Catalog', 'GamingGen Store API', 'Web Curated') AND images_json != '[]'"
        ).fetchall()
        st.close()
        for disp, imgs_j in rows:
            for u in json.loads(imgs_j or "[]"):
                if isinstance(u, str) and u.startswith("https://"):
                    mapping[u].append(str(disp or ""))
    except Exception:
        pass
    pcp_jsonl = os.path.join(ROOT_DIR, "data", "pcpartpicker_parts.jsonl")
    if os.path.isfile(pcp_jsonl):
        try:
            with open(pcp_jsonl, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if not line:
                        continue
                    d = json.loads(line)
                    nm = str(d.get("name") or "").strip()
                    if not nm:
                        continue
                    for u in (list(d.get("images") or []) + [d.get("image_url"), d.get("imageUrl")]):
                        if isinstance(u, str) and "cdna.pcpartpicker.com" in u:
                            mapping[u].append(nm)
                            if ".256p." in u:
                                mapping[u.replace(".256p.", ".1600.")].append(nm)
                            elif ".1600." in u:
                                mapping[u.replace(".1600.", ".256p.")].append(nm)
        except Exception:
            pass
    _OPAQUE_URL_TITLES_CACHE = dict(mapping)
    return _OPAQUE_URL_TITLES_CACHE


def filter_and_prune_item_images(
    item: dict[str, Any],
    category: str,
    health_map: dict[str, bool],
    image_key: str = "imageUrl",
) -> list[str]:
    """Keeps only live HTTP 200 images that strictly pass brand/model compatibility."""
    brand = str(item.get("brand") or "")
    name = str(item.get("name") or item.get("model") or "")
    if not brand and name:
        brand = name.split()[0]
    chipset = str(item.get("chipset") or "")

    candidates: list[str] = []
    for k in (image_key, "imageUrl", "image_url"):
        u = item.get(k)
        if isinstance(u, str) and u.startswith("https://") and u not in candidates:
            candidates.append(u)
    for u in (item.get("images") or []):
        if isinstance(u, str) and u.startswith("https://") and u not in candidates:
            candidates.append(u)

    url_titles = _get_opaque_url_titles()
    valid: list[str] = []
    for u in candidates:
        if not health_map.get(u, False):
            continue
        if not is_image_compatible_with_item(category, brand, name, chipset, u):
            continue
        known_titles = url_titles.get(u)
        if known_titles:
            any_title_ok = False
            for kt in known_titles:
                kt_slug = re.sub(r"[^a-z0-9]+", "-", kt.lower()).strip("-")
                if is_image_compatible_with_item(category, brand, name, chipset, f"https://source-title.example/{kt_slug}.png"):
                    any_title_ok = True
                    break
            if not any_title_ok:
                continue
        valid.append(u)
    valid_set = set(valid)
    deduped = [
        u for u in valid
        if not (u.endswith(".256p.jpg") and u.replace(".256p.jpg", ".1600.jpg") in valid_set)
    ]
    return deduped[:6]


def dedupe_synthetic_peripheral_clones(items: list[dict[str, Any]]) -> list[dict[str, Any]]:
    """Collapses synthetic copy-paste suffix variants sharing identical prefix + image URL."""
    target_cats = {
        "ROUTER", "MODEM", "ACCESS_POINT", "RANGE_EXTENDER", "UPS",
        "GAMING_LIGHTING", "GAMING_DESK", "CASE_FAN", "SMARTWATCH",
    }
    groups: dict[tuple[str, str], list[dict[str, Any]]] = collections.defaultdict(list)
    passthrough: list[dict[str, Any]] = []
    for it in items:
        cat = str(it.get("category") or "")
        nm = str(it.get("name") or "")
        if cat in target_cats and " - " in nm:
            base = nm.split(" - ", 1)[0].strip()
            groups[(cat, base.lower())].append(it)
        else:
            passthrough.append(it)

    kept: list[dict[str, Any]] = list(passthrough)
    for (cat, _), clist in groups.items():
        if len(clist) < 3:
            kept.extend(clist)
            continue
        # Collapse multi-clone spam (keep clean base model name cleanly!)
        # For CASE_FAN, allow at most genuine Single + 3-Pack ONLY if NOT Classic Brown Fake White
        best = dict(clist[0])
        base_clean = str(best.get("name") or "").split(" - ", 1)[0].strip()
        best["name"] = base_clean
        best["model"] = base_clean
        kept.append(best)
    return kept


def _iter_tracked_repo_images() -> list[tuple[str, str]]:
    """Lists (subdir, filename) pairs for verified studio images currently present in local data/images/."""
    pairs: list[tuple[str, str]] = []
    base = os.path.join(ROOT_DIR, "data", "images")
    if os.path.isdir(base):
        for sub in sorted(os.listdir(base)):
            sub_dir = os.path.join(base, sub)
            if os.path.isdir(sub_dir):
                for fn in sorted(os.listdir(sub_dir)):
                    pairs.append((sub, fn))
    return pairs


def collect_repo_asset_urls() -> list[str]:
    """Returns all valid non-generic repo image URLs for batch health verification."""
    urls: list[str] = []
    valid_subs = set(_REPO_IMAGE_SUBDIRS.values())
    for sub, fn in _iter_tracked_repo_images():
        if sub not in valid_subs or not fn.lower().endswith((".webp", ".png", ".jpg", ".jpeg")):
            continue
        low = fn.lower()
        if any(bad in low for bad in ("_reference.", "_bottom.", "generic_")):
            continue
        urls.append(f"{_REPO_RAW_BASE}/data/images/{sub}/{fn}")
    return urls


def build_repo_asset_gallery(category: str) -> list[dict[str, Any]]:
    """Builds exact-slug studio gallery entries from tracked data/images/<category>/."""
    target_sub = _REPO_IMAGE_SUBDIRS.get(category)
    if not target_sub:
        return []
    records: list[dict[str, Any]] = []
    for sub, fn in _iter_tracked_repo_images():
        if sub != target_sub or not fn.lower().endswith((".webp", ".png", ".jpg", ".jpeg")):
            continue
        low = fn.lower()
        if any(bad in low for bad in ("_reference.", "_bottom.", "_ihs_studio.", "generic_")):
            continue
        stem = os.path.splitext(fn)[0]
        display = re.sub(r"\s+", " ", stem.replace("_", " ")).strip()
        if not display:
            continue
        brand = display.split()[0]
        url = f"{_REPO_RAW_BASE}/data/images/{sub}/{fn}"
        records.append(
            {
                "source": "Repo Assets",
                "priority": GALLERY_SOURCE_PRIORITY.get("Repo Assets", 4),
                "display_name": display,
                "brand": brand,
                "ckey": normalize_canonical_key(display),
                "bkey": normalize_for_brand_match(display),
                "tokens": set(re.findall(r"[a-z0-9]{2,}", _clean_match_string(display))),
                "images": [url],
            }
        )
    return records


def build_gallery_index(state: CrawlStateManager, category: str, sources: tuple[str, ...]) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
    if "Repo Assets" in sources:
        records.extend(build_repo_asset_gallery(category))
    for row in state.load_records_for_category(category):
        if row["source_name"] not in sources:
            continue
        imgs = [
            u for u in (json.loads(row["images_json"] or "[]"))
            if isinstance(u, str)
            and u.startswith("https://")
            and not any(b in u.lower() for b in BAD_HOST_SUBSTRS)
        ]
        if not imgs:
            continue
        records.append(
            {
                "source": row["source_name"],
                "priority": GALLERY_SOURCE_PRIORITY.get(row["source_name"], 1),
                "display_name": row["display_name"],
                "brand": row["brand"] or "",
                "ckey": row["canonical_key"],
                "bkey": normalize_for_brand_match(row["display_name"]),
                "tokens": set(re.findall(r"[a-z0-9]{2,}", _clean_match_string(row["display_name"]))),
                "images": imgs,
            }
        )
    return records


def _clean_match_string(s: str) -> str:
    low = (s or "").lower().replace("+", " plus ")
    low = re.sub(r"\b(fold|flip|xcover|tab|pad)(\d)", r"\1 \2", low)
    low = re.sub(r"\b\d{4,5}\s*mah\b", " ", low)
    low = low.replace("10. nesil", "10th 2022").replace("9. nesil", "9th 2021")
    low = low.replace("(katlanabilir)", "fold")
    low = re.sub(r"\((?:m1|m2|m3|m4|a15|a14|a16|20\d\d)\)", lambda m: f" {m.group(0)[1:-1]} ", low)
    low = re.sub(r"\([^)]*\)", " ", low)
    noise = (
        "tray", "mpk", "kutulu", "fansız", "fansiz", "soket", "ön bellek", "onbellek",
        "çekirdek", "cekirdek", "işlemci", "islemci", "ekran kartı", "ekran karti",
        "anakart", "ram bellek", "freedos", "taşınabilir bilgisayar", "tasinabilir bilgisayar",
        "oyuncu laptop", "gaming laptop", "oyun bilgisayarı", "oyun bilgisayari",
        "sunucu", "intel xmp", "amd expo", "ram", "ssd",
    )
    for n in noise:
        low = low.replace(n, " ")
    low = re.sub(r"\b\d+(?:\.\d+)?\s*(?:ghz|mhz|bit|nm|mb|gb|tb|w)\b", " ", low)
    return re.sub(r"\s+", " ", low).strip()


_GALLERY_CACHE: dict[tuple[int, int], dict[str, Any]] = {}
_IGNORED_MATCH_TOKS = frozenset({"geforce", "radeon", "graphics", "card", "edition", "laptop"})


def _get_gallery_cache(gallery_records: list[dict[str, Any]]) -> dict[str, Any]:
    key = (id(gallery_records), len(gallery_records))
    cached = _GALLERY_CACHE.get(key)
    if cached is not None:
        return cached
    prepped: list[dict[str, Any]] = []
    by_ck: dict[str, list[int]] = collections.defaultdict(list)
    by_ck_clean: dict[str, list[int]] = collections.defaultdict(list)
    by_bk: dict[str, list[int]] = collections.defaultdict(list)
    by_tok: dict[str, list[int]] = collections.defaultdict(list)
    for idx, rec in enumerate(gallery_records):
        rec_clean = _clean_match_string(rec["display_name"])
        rec_plus = re.sub(r"\+", " plus ", rec["display_name"] or "")
        rec_ck = normalize_canonical_key(rec_plus)
        rec_ck_clean = normalize_canonical_key(rec_clean)
        rec_bk = normalize_for_brand_match(rec_plus)
        rec_toks = set(re.findall(r"[a-z0-9]{2,}", rec_clean)) - _IGNORED_MATCH_TOKS
        prepped.append({
            "rec": rec,
            "rec_clean": rec_clean,
            "rec_ck": rec_ck,
            "rec_ck_clean": rec_ck_clean,
            "rec_bk": rec_bk,
            "rec_toks": rec_toks,
        })
        if rec_ck:
            by_ck[rec_ck].append(idx)
        if rec_ck_clean:
            by_ck_clean[rec_ck_clean].append(idx)
        if rec_bk:
            by_bk[rec_bk].append(idx)
        for tk in rec_toks:
            by_tok[tk].append(idx)
    if len(_GALLERY_CACHE) > 32:
        _GALLERY_CACHE.clear()
    entry = {
        "prepped": prepped,
        "by_ck": by_ck,
        "by_ck_clean": by_ck_clean,
        "by_bk": by_bk,
        "by_tok": by_tok,
    }
    _GALLERY_CACHE[key] = entry
    return entry


def find_best_gallery_match(
    category: str,
    brand: str,
    name: str,
    chipset: str,
    gallery_records: list[dict[str, Any]],
    health_map: dict[str, bool],
    score_out: list[float] | None = None,
) -> list[str]:
    """Finds the best token-verified studio gallery strictly matching brand & model code."""
    if not gallery_records:
        if score_out is not None:
            score_out[:] = [0.0]
        return []
    clean_name = _clean_match_string(name)
    name_plus = re.sub(r"\+", " plus ", name or "")
    ck = normalize_canonical_key(name_plus)
    ck_clean = normalize_canonical_key(clean_name)
    bk = normalize_for_brand_match(name_plus)
    t_str = f"{brand} {clean_name} {chipset}".lower()
    t_toks = set(re.findall(r"[a-z0-9]{2,}", t_str)) - _IGNORED_MATCH_TOKS
    b_low = (brand or (name.split()[0] if name.split() else "")).lower()

    cache = _get_gallery_cache(gallery_records)
    prepped = cache["prepped"]
    if len(prepped) <= 250:
        cand_indices = range(len(prepped))
    else:
        exact_set: set[int] = set()
        exact_set.update(cache["by_ck"].get(ck, ()))
        if ck_clean:
            exact_set.update(cache["by_ck_clean"].get(ck_clean, ()))
        if bk:
            exact_set.update(cache["by_bk"].get(bk, ()))
        tok_hits: collections.Counter[int] = collections.Counter()
        by_tok = cache["by_tok"]
        for tk in t_toks:
            postings = by_tok.get(tk)
            if postings and len(postings) <= 3500:
                for idx in postings:
                    tok_hits[idx] += 1
        top_fuzzy = [idx for idx, cnt in tok_hits.most_common(140) if cnt >= 2]
        cand_indices = list(dict.fromkeys(list(exact_set) + top_fuzzy))

    matched_candidates: list[tuple[float, str, list[str]]] = []
    for idx in cand_indices:
        p = prepped[idx]
        rec = p["rec"]
        rec_clean = p["rec_clean"]
        rec_ck = p["rec_ck"]
        rec_ck_clean = p["rec_ck_clean"]
        rec_bk = p["rec_bk"]
        rec_toks = p["rec_toks"]

        if category in ("SMARTPHONE", "TABLET"):
            t_nums = set(re.findall(r"\b(\d{1,3}[a-z]?)\b", clean_name))
            r_nums = set(re.findall(r"\b(\d{1,3}[a-z]?)\b", rec_clean))
            if t_nums != r_nums and not (t_nums and t_nums <= r_nums):
                continue
            tier_mismatch = False
            for tier in ("ultra", "pro", "plus", "mini", "air", "fe", "fold", "flip", "max", "lite"):
                if (tier in t_toks) != (tier in rec_toks):
                    tier_mismatch = True
                    break
            if tier_mismatch:
                continue

        if category in ("CASE", "COOLER", "RAM", "PSU", "GPU", "MOTHERBOARD"):
            color_w = ("white", "beyaz", "snow", "ice", "aero")
            t_col_words = set(re.findall(r"[a-z0-9]+", (name or "").lower()))
            r_col_words = set(re.findall(r"[a-z0-9]+", (rec["display_name"] or "").lower()))
            if any(w in t_col_words for w in color_w) != any(w in r_col_words for w in color_w):
                continue
            if category == "RAM" and ("rgb" in t_col_words) != ("rgb" in r_col_words):
                continue

        score = 0.0
        prio_bonus = float(rec.get("priority", 1)) * 0.5
        if rec_ck == ck or (ck_clean and rec_ck_clean == ck_clean) or (bk and rec_bk == bk):
            score = 120.0 + prio_bonus
        else:
            if b_low and b_low not in rec["display_name"].lower() and b_low not in rec["brand"].lower():
                continue
            rec_slug = re.sub(r"[^a-z0-9]+", "-", (rec["display_name"] or "").lower()).strip("-")
            if rec_slug and not is_image_compatible_with_item(category, brand, name, chipset, f"https://rec-title.example/{rec_slug}.png"):
                continue
            if category == "GPU":
                g1 = _extract_gpu_sig(t_str)
                g2 = _extract_gpu_sig(rec["display_name"])
                if not g1 or not g2 or g1 != g2:
                    continue
                t_g_norm = re.sub(r"[^a-z0-9]", " ", t_str)
                r_g_norm = re.sub(r"[^a-z0-9]", " ", rec["display_name"].lower())
                sub_conflict = False
                for sub_grp in _GPU_SUB_SERIES_GROUPS:
                    ts = {s.replace("_", " ") for s in sub_grp if s.replace("_", " ") in t_g_norm}
                    rs = {s.replace("_", " ") for s in sub_grp if s.replace("_", " ") in r_g_norm}
                    if ts and rs and not (ts & rs):
                        sub_conflict = True
                        break
                if sub_conflict:
                    continue
                if any(w in t_g_norm.split() for w in ("white", "beyaz", "ice", "aero", "snow")) != any(w in r_g_norm.split() for w in ("white", "beyaz", "ice", "aero", "snow")):
                    continue
            if category == "CPU":
                if ("threadripper" in clean_name) != ("threadripper" in rec_clean):
                    continue
                if ("x3d" in clean_name) != ("x3d" in rec_clean):
                    continue
                m_c1 = re.findall(r"\b(\d{3,5}[a-z0-9]{0,4})\b", clean_name)
                m_c2 = re.findall(r"\b(\d{3,5}[a-z0-9]{0,4})\b", rec_clean)
                c1_set = {c for c in m_c1 if len(c) >= 4 or c.endswith(("k", "f", "x"))}
                c2_set = {c for c in m_c2 if len(c) >= 4 or c.endswith(("k", "f", "x"))}
                if not (c1_set and c2_set and (c1_set & c2_set)):
                    continue
            if category == "MOTHERBOARD":
                mb1 = set(re.findall(r"(?:^|[^a-z0-9])([abxz]\d{3}[a-z]{0,2})", clean_name))
                mb2 = set(re.findall(r"(?:^|[^a-z0-9])([abxz]\d{3}[a-z]{0,2})", rec_clean))
                is_nzxt_b650 = ("nzxt" in clean_name and "n7" in clean_name and "b650" in mb1 and "b650e" in mb2)
                if mb1 and mb2 and not (mb1 & mb2) and not is_nzxt_b650:
                    continue
                mb_series = ("strix", "tuf", "prime", "proart", "aorus", "tomahawk", "mortar", "steel", "taichi", "phantom", "nova", "carbon", "ace", "godlike", "edge", "eagle", "hero", "apex", "crosshair", "lightning", "riptide", "nitro", "pure", "pulse", "btf")
                ms1 = {s for s in mb_series if s in clean_name}
                ms2 = {s for s in mb_series if s in rec_clean}
                if ms1 and ms2 and not (ms1 & ms2):
                    continue
            if category == "STORAGE":
                st_pat = r"\b(?:sn\d{3,4}[a-z]?|nm\d{3}|kc\d{4}|mp\d{3}|m\d{3}[a-z]?|t\d{3}|p\d{1,3}|e\d{3}|[789]\d{2})\b"
                s1 = set(re.findall(st_pat, clean_name))
                s2 = set(re.findall(st_pat, rec_clean))
                if s1 and s2 and not (s1 & s2):
                    continue
                for tier in ("pro", "evo", "plus", "blade", "renegade", "g2", "g3"):
                    if (tier in t_toks) != (tier in rec_toks):
                        s1 = {"_mismatch_"}
                        s2 = {"_other_"}
                        break
                if s1 == {"_mismatch_"}:
                    continue
            if category == "PSU":
                w1 = re.findall(r"\b(\d{3,4})\s*w\b", name.lower())
                w2 = re.findall(r"\b(\d{3,4})\s*w\b", (rec["display_name"] or "").lower())
                if w1 and w2 and not (set(w1) & set(w2)):
                    continue
                psu_ser = ("rmx", "rme", "hxi", "sf", "thor", "loki", "edge", "core", "cybercore", "vita", "hydro", "focus", "vertex")
                p1 = {s for s in psu_ser if s in t_toks}
                p2 = {s for s in psu_ser if s in rec_toks}
                if p1 and p2 and not (p1 & p2):
                    continue
            if category == "LAPTOP":
                # Match chassis code (g834, g634, 83jh, ph18, ge78, fa507, fx507, anv15, etc.) when present on both sides
                sku_codes_t = set(re.findall(r"\b([a-z]{1,4}\d{2,4}[a-z]{0,4}|\d{2}[a-z]{2,4}\d{2,4}[a-z0-9]*)\b", t_str))
                sku_codes_r = set(re.findall(r"\b([a-z]{1,4}\d{2,4}[a-z]{0,4}|\d{2}[a-z]{2,4}\d{2,4}[a-z0-9]*)\b", rec_clean))
                if sku_codes_t and sku_codes_r and (sku_codes_t & sku_codes_r):
                    score += 35.0

            overlap_set = t_toks & rec_toks
            overlap = len(overlap_set)
            has_model_digit = any(re.search(r"\d", tok) for tok in overlap_set)
            min_req = 2 if (
                category in (
                    "CPU", "SMARTPHONE", "TABLET", "STORAGE", "PSU", "COOLER", "RAM", "CASE", "MOTHERBOARD",
                    "ROUTER", "UPS", "MONITOR", "KEYBOARD", "MOUSE", "HEADSET",
                )
                and has_model_digit
            ) else 3
            if category == "GPU" and has_model_digit:
                min_req = 2
            if overlap < min_req:
                continue
            score += overlap * 10.0 - abs(len(rec_toks) - len(t_toks)) * 0.5 + prio_bonus
        if score < 40.0:
            continue
        url_titles = _get_opaque_url_titles()
        healthy = []
        for u in rec["images"]:
            if not health_map.get(u, False):
                continue
            if not is_image_compatible_with_item(category, brand, name, chipset, u):
                continue
            kt_list = url_titles.get(u)
            if kt_list:
                if not any(
                    is_image_compatible_with_item(category, brand, name, chipset, f"https://source-title.example/{re.sub(r'[^a-z0-9]+', '-', kt.lower()).strip('-')}.png")
                    for kt in kt_list
                ):
                    continue
            healthy.append(u)
        if healthy:
            imgs_cap = healthy[:1] if rec.get("source") == "PCPartPicker Dataset" else healthy[:6]
            matched_candidates.append((score, str(rec.get("source") or ""), imgs_cap))

    if not matched_candidates:
        if score_out is not None:
            score_out[:] = [0.0]
        return []

    matched_candidates.sort(key=lambda x: (-x[0], -len(x[2])))
    best_score = matched_candidates[0][0]
    best_imgs: list[str] = []
    seen_u: set[str] = set()
    if best_score >= 120.0:
        # Combine exact multi-angle galleries across clean sources (preferring multi-photo store galleries over 1-photo PCP)
        exact_group = sorted([c for c in matched_candidates if c[0] >= 120.0], key=lambda x: (-len(x[2]), -x[0]))
        for _, _, imgs in exact_group:
            for u in imgs:
                if u not in seen_u:
                    seen_u.add(u)
                    best_imgs.append(u)
                    if len(best_imgs) >= 6:
                        break
            if len(best_imgs) >= 6:
                break
    else:
        best_imgs = matched_candidates[0][2][:6]

    best_set = set(best_imgs)
    best_imgs = [
        u for u in best_imgs
        if not (u.endswith(".256p.jpg") and u.replace(".256p.jpg", ".1600.jpg") in best_set)
    ][:6]
    if score_out is not None:
        score_out[:] = [best_score]
    return best_imgs


def backfill_missing_via_wikimedia(
    state: CrawlStateManager,
    items: list[dict[str, Any]],
    category: str,
    http_client: Any,
    health_map: dict[str, bool],
    max_queries: int = 35,
) -> None:
    """Politely queries Wikimedia Commons ONLY for high-profile missing items."""
    queried = 0
    new_urls: list[str] = []
    for it in items:
        if queried >= max_queries:
            break
        cur = it.get("images") or []
        if cur:
            continue
        brand = str(it.get("brand") or "")
        name = str(it.get("name") or it.get("model") or "")
        q = f"{brand} {name}".strip() if brand and not name.lower().startswith(brand.lower()) else name
        q = q.replace("10. Nesil", "10th generation").replace("9. Nesil", "9th generation").replace("(Katlanabilir)", "")
        if category == "LAPTOP":
            q = re.sub(r"\b\d+(?:\.\d+)?\s*inç.*", "", q, flags=re.I)
            q = " ".join(q.split()[:5])
        q = re.sub(r"\([^)]*\)", "", q).strip()
        if not q or len(q) < 4:
            continue
        cache_task = f"img:wiki:{category}:{normalize_canonical_key(q)}"
        prog = state.get_progress(cache_task)
        if prog:
            u = prog["cursor"] if prog["status"] == "DONE" and prog["cursor"].startswith("https://") else None
        else:
            queried += 1
            u = search_wikimedia_image(http_client, q)
            state.save_progress(cache_task, "DONE", cursor=u or "")
        if u and is_image_compatible_with_item(category, brand, name, str(it.get("chipset") or ""), u):
            it["images"] = [u]
            if "imageUrl" in it or category not in ("GPU",):
                it["imageUrl"] = u
            if "image_url" in it:
                it["image_url"] = u
            new_urls.append(u)
    if new_urls:
        verified = verify_image_url_pool(state, new_urls, max_workers=8)
        health_map.update(verified)
        for it in items:
            imgs = it.get("images") or []
            if imgs and imgs[0] in verified and not verified[imgs[0]]:
                it["images"] = []
                it.pop("imageUrl", None)
                it["image_url"] = None


def quarantine_cross_model_collisions(
    items: list[dict[str, Any]],
    category: str,
    image_key: str = "imageUrl",
    max_allowed_variants: int = 10,
) -> None:
    """Clears opaque shared hash URLs when shared across incompatible models/brands/AIB families."""
    by_url: dict[str, list[dict[str, Any]]] = collections.defaultdict(list)
    for it in items:
        u = it.get(image_key) or it.get("imageUrl") or it.get("image_url") or ((it.get("images") or [None])[0])
        if u:
            by_url[u].append(it)

    for u, clist in by_url.items():
        if len(clist) <= 1:
            continue
        # Group by "visual base identity": brand + chipset/model core without capacity/timing/Hz suffixes
        sigs: set[str] = set()
        sig_to_items: dict[str, list[dict[str, Any]]] = collections.defaultdict(list)
        for it in clist:
            nm = str(it.get("name") or it.get("model") or "").lower()
            br = str(it.get("brand") or (nm.split()[0] if nm.split() else "")).lower()
            if nm.startswith(br + " "):
                nm = nm[len(br):].strip()
            core = re.sub(r"\b(?:\d+\s*(?:gb|tb|mhz|mts|watt|w|va|oc|bit)|cl\d+)\b", "", nm)
            core = re.sub(r"\([^)]*\)", "", core)
            if category == "RAM":
                has_rgb = "rgb" in nm
                is_wh = any(c in nm for c in ("white", "beyaz", "silver", "gümüş", "snow"))
                core = re.sub(r"\b(ddr[345])[\-\s]*\d{3,5}\b", "", core)
                core = re.sub(r"\b(amd expo|intel xmp|xmp|expo|neo|\d+\s*x\s*\d+|non-ecc|unbuffered|dimm|sodimm|u-dimm)\b", "", core)
                core = re.sub(r"\b(white|beyaz|silver|black|gray|grey)\b", "", core)
                core = " ".join(w for w in re.findall(r"[a-z0-9]+", core) if len(w) >= 2)[:32]
                skey = f"{br}|{core}|{'rgb' if has_rgb else 'plain'}|{'w' if is_wh else 'b'}"
            elif category == "GPU":
                gpu_toks = sorted(
                    t for t in re.findall(r"[a-z0-9]+", core)
                    if len(t) >= 2 and t not in {
                        "geforce", "radeon", "intel", "edition", "gaming", "oc",
                        "black", "v1", "v2", "lhr", "12g", "16g", "24g", "8g", "10g", "32g",
                        "gddr6", "gddr6x", "gddr7", "dlss", "ekran", "karti",
                    }
                )
                skey = f"{br}|{' '.join(gpu_toks[:7])}"
            elif category == "CPU":
                cpu_core = re.sub(r"\b(?:tray|mpk|boxed|kutusuz|kutulu|fansiz|fansız|wraith|oem|processor|islemci|işlemci)\b", "", core)
                cpu_toks = [t for t in re.findall(r"[a-z0-9]+", cpu_core) if len(t) >= 2]
                skey = f"{br}|{' '.join(cpu_toks[:5])}"
            elif category == "MOTHERBOARD":
                mb_toks = sorted(
                    t for t in re.findall(r"[a-z0-9]+", core)
                    if len(t) >= 2 and t not in {"wifi", "wi", "fi", "gaming", "ddr4", "ddr5", "atx", "matx", "micro", "v2", "rev", "csm"}
                )
                skey = f"{br}|{' '.join(mb_toks[:5])}"
            elif category == "STORAGE":
                st_core = re.sub(r"\b(?:nvme|pcie|gen\d|sata|ssd|internal|hard|drive|m\.?2|2280|2242|2230|heatsink|okuma|yazma|with|without|3\.0|4\.0|5\.0|x4)\b", "", core)
                st_toks = [t for t in re.findall(r"[a-z0-9]+", st_core) if len(t) >= 2]
                skey = f"{br}|{' '.join(st_toks[:5])}"
            elif category == "LAPTOP":
                lap_core = re.sub(r"\b(?:freedos|w11|win11|windows\s*\d*|home|pro|ubuntu|144hz|165hz|240hz|300hz|360hz|fhd|qhd|uhd|oled|ips)\b", "", core)
                lap_core = re.sub(r"\d+(?:\.\d+)?\s*(?:inc|inç|\")", "", lap_core)
                lap_toks = [t for t in re.findall(r"[a-z0-9]+", lap_core) if len(t) >= 2 and t not in {"laptop", "notebook", "gaming", "tasinabilir", "bilgisayar", "canta", "hediyeli"}]
                skey = f"{br}|{' '.join(lap_toks[:3])}"
            else:
                core = re.sub(r"\s+", " ", core).strip()
                if category in ("COOLER", "CASE", "PSU"):
                    is_wh_g = any(c in nm for c in ("white", "beyaz", "silver", "gümüş", "snow"))
                    skey = f"{br}|{core[:32]}|{'w' if is_wh_g else 'b'}"
                else:
                    skey = f"{br}|{core[:32]}"
            sigs.add(skey)
            sig_to_items[skey].append(it)

        def _clear_item_img(it: dict[str, Any]) -> None:
            if image_key == "image_url":
                it["image_url"] = None
            else:
                it.pop("imageUrl", None)
                it.pop("image_url", None)
            it["images"] = []

        # When multiple different models share one image: if descriptive URL clearly matches one signature, keep it and clear misfits
        if len(sigs) > 1:
            path_l = urlparse(u).path.lower()
            matching_sigs = []
            for skey, group_items in sig_to_items.items():
                parts = skey.split("|")
                br_part, model_part = parts[0], parts[1]
                toks = [t for t in model_part.split() if len(t) >= 3 and t not in {"geforce", "radeon", "intel", "gaming", "edition"}]
                if br_part in path_l and toks and sum(1 for t in toks if t in path_l) >= min(2, len(toks)):
                    matching_sigs.append(skey)
            if len(matching_sigs) == 1:
                winner_sig = matching_sigs[0]
                for skey, group_items in sig_to_items.items():
                    if skey != winner_sig:
                        for it in group_items:
                            _clear_item_img(it)
                continue

            # If one model won by a decisive gallery match score margin (e.g. exact canonical hit vs weak overlap), preserve winner cleanly
            cap_limit = 10 if category == "STORAGE" else (15 if category == "LAPTOP" else (6 if category == "GPU" else max_allowed_variants))
            ranked_sigs = sorted(
                ((max(float(it.get("_img_match_score") or 0.0) for it in grp), sk, grp) for sk, grp in sig_to_items.items()),
                key=lambda x: -x[0],
            )
            if len(ranked_sigs) >= 2 and ranked_sigs[0][0] >= 40.0 and (ranked_sigs[0][0] - ranked_sigs[1][0]) >= 10.0 and len(ranked_sigs[0][2]) <= cap_limit:
                winner_sig = ranked_sigs[0][1]
                for skey, group_items in sig_to_items.items():
                    if skey != winner_sig:
                        for it in group_items:
                            _clear_item_img(it)
                continue

            pcp_sigs = [sk for sk, grp in sig_to_items.items() if any(it.get("_pcp_backed") for it in grp)]
            if len(pcp_sigs) == 1 and len(sig_to_items[pcp_sigs[0]]) <= cap_limit:
                winner_sig = pcp_sigs[0]
                for skey, group_items in sig_to_items.items():
                    if skey != winner_sig:
                        for it in group_items:
                            _clear_item_img(it)
                continue

        # Dominant majority preservation for RAM (speed/capacity/timing kits share one visual line cleanly)
        if category == "RAM":
            largest_sig, largest_items = max(sig_to_items.items(), key=lambda kv: len(kv[1]))
            if len(largest_items) <= 75 and (len(sigs) == 1 or (len(largest_items) >= 2 and len(largest_items) / len(clist) >= 0.45)):
                url_white = any(c in u.lower() for c in ("white", "beyaz", "silver"))
                kept_color = [
                    it for it in largest_items
                    if any(c in str(it.get("name", "")).lower() for c in ("white", "beyaz", "silver", "gümüş", "snow")) == url_white
                ]
                keep_set = set(id(it) for it in (kept_color if kept_color else largest_items))
                for it in clist:
                    if id(it) not in keep_set:
                        _clear_item_img(it)
                continue
            for it in clist:
                _clear_item_img(it)
            continue

        # Fallback: quarantine ambiguous collisions
        if category == "LAPTOP":
            if len(sigs) > 1 or len(clist) > 15:
                largest_sig, largest_items = max(sig_to_items.items(), key=lambda kv: len(kv[1]))
                if len(largest_items) >= 2 and len(largest_items) <= 15 and (len(largest_items) / len(clist)) >= 0.65:
                    for skey, group_items in sig_to_items.items():
                        if skey != largest_sig:
                            for it in group_items:
                                _clear_item_img(it)
                else:
                    for it in clist:
                        _clear_item_img(it)
        elif category == "GPU":
            if len(sigs) > 1 or len(clist) > 10:
                largest_sig, largest_items = max(sig_to_items.items(), key=lambda kv: len(kv[1]))
                if len(largest_items) >= 2 and len(largest_items) <= 10 and (len(largest_items) / len(clist)) >= 0.65:
                    for skey, group_items in sig_to_items.items():
                        if skey != largest_sig:
                            for it in group_items:
                                _clear_item_img(it)
                else:
                    for it in clist:
                        _clear_item_img(it)
        elif category in ("SMARTPHONE", "TABLET", "CPU"):
            if len(sigs) > 1 or len(clist) > max_allowed_variants:
                for it in clist:
                    _clear_item_img(it)
        elif len(sigs) >= 2:
            largest_sig, largest_items = max(sig_to_items.items(), key=lambda kv: len(kv[1]))
            if category == "MOTHERBOARD" and len(largest_items) >= 2 and (len(largest_items) / len(clist)) >= 0.55:
                for skey, group_items in sig_to_items.items():
                    if skey != largest_sig:
                        for it in group_items:
                            _clear_item_img(it)
            else:
                for it in clist:
                    _clear_item_img(it)
        elif len(clist) > (10 if category == "STORAGE" else max_allowed_variants):
            for it in clist:
                _clear_item_img(it)

    for it in items:
        it.pop("_img_match_score", None)
        it.pop("_pcp_backed", None)
