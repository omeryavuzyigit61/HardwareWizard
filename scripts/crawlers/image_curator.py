"""Strict cross-category image authenticity validator, clone deduplicator, and multi-source gallery matcher."""

from __future__ import annotations

import collections
import json
import re
from typing import Any
from urllib.parse import unquote, urlparse

from .sources_images import search_wikimedia_image, verify_image_url_pool
from .state_manager import CrawlStateManager, normalize_canonical_key, normalize_for_brand_match

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
)

CPU_SOCKET_NUMBERS = frozenset({
    "1150", "1151", "1155", "1156", "1200", "1366",
    "1700", "1851", "1954", "2011", "2066", "3647", "4677",
})

GALLERY_SOURCE_PRIORITY = {
    "GamingGen Store API": 4,
    "GSMArena Gallery": 4,
    "Keychron Official": 4,
    "Pulsar Official": 4,
    "Secretlab Official": 4,
    "LethalGamingGear Official": 4,
    "TechPowerUp Ajax": 3,
    "IRONScale Labs": 3,
    "DXOMARK": 2,
    "GSMArena": 1,
}

GPU_CHIP_PATTERNS = (
    re.compile(r"\b(rtx|gtx|rx|arc)[_\-\s]*(\d{3,4})\s*(ti\s*super|super|xtx|xt|ti|gre)?\b", re.I),
    re.compile(r"\b(b580|b570|b390|a770|a750|a580|a380)\b", re.I),
)

AIB_SERIES_TOKENS = (
    "strix", "tuf", "prime", "proart", "astral", "dual", "suprim", "ventus",
    "inspire", "shadow", "expert", "vanguard", "aero", "aorus", "eagle",
    "windforce", "gaming_oc", "gaming_trio", "trio", "nitro", "pulse", "pure", "hellhound", "red_devil",
    "fighter", "taichi", "steel_legend", "phantom", "challenger", "merc", "qick",
    "swft", "trinity", "amp", "twin_edge", "ichill", "phoenix", "panther", "jetstream",
    "ex_gamer", "gamerock", "1_click", "liquid",
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

    parsed = urlparse(low_u)
    path = parsed.path
    # Opaque CDN hashes (cdna.pcpartpicker.com/<hex>, amazon /I/<asin>, shopify numeric files without names)
    stem = re.sub(r"\.[a-z0-9]+$", "", path.rsplit("/", 1)[-1])
    is_opaque_hash = bool(
        "cdna.pcpartpicker.com" in low_u
        or "m.media-amazon.com" in low_u
        or "images-na.ssl-images-amazon.com" in low_u
        or re.match(r"^[0-9a-f\-_]{20,}$", stem)
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
            # Allow Keychron CDN paths on Keychron/Lemokey sub-brand items
            if not ("keychron" in non_silicon and ("lemokey" in t_flat or "lseries" in b_flat or "isokeyboards" in b_flat or "trackball" in b_flat or "orca" in t_flat)):
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
            if ("liquid" in t_low) != ("liquid" in path):
                return False
            u_white_g = any(w in p_norm_u.split() for w in ("white", "beyaz", "ice", "aero"))
            t_white_g = any(w in t_norm_u.split() for w in ("white", "beyaz", "ice", "aero"))
            if u_white_g != t_white_g:
                return False

    elif category == "CPU":
        # Reject generic local placeholder thumbnails applied to non-exact SKUs
        if "raw.githubusercontent.com/omeryavuzyigit61/hardwarewizard/main/data/images/cpu/" in low_u:
            sku_slug = stem.replace("_", "")
            # Must contain exact model code like 9800x3d, 7800x3d, 5800x3d, 285k, 14700k, 14400
            m_sku = re.search(r"(9800x3d|7800x3d|5800x3d|285k|14700k|14400)", sku_slug)
            if not m_sku or m_sku.group(1) not in t_flat:
                return False
        if not is_opaque_hash:
            t_nums = set(re.findall(r"\b(\d{4,5}[a-z]{0,4})\b", t_low))
            u_nums = set(re.findall(r"\b(\d{4,5}[a-z]{0,4})\b", re.sub(r"[-_/]", " ", path)))
            bad = [
                n for n in u_nums
                if n not in CPU_SOCKET_NUMBERS
                and n not in t_nums
                and not re.match(r"^20[12]\d$", n)
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
            # Specific Casper / Asus / Lenovo chassis model checks
            for ch in ("loq", "legion", "thinkbook", "ideapad", "victus", "omen", "strix", "tuf", "zephyrus", "katana", "raider", "vector", "thin", "cyborg", "nitro", "predator", "excalibur"):
                if ch in path and ch not in t_low:
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
            t_norm = t_low.replace("+", " plus ").replace("10. nesil", "10th 2022").replace("9. nesil", "9th 2021 102")
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
            slug_toks = [t for t in re.split(r"[^a-z0-9]+", slug) if t and t not in {"5g", "4g", "lte", "sm", "2024", "2025", "2026", "1", "2"}]
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
        if "nothing_phone.jpg" in low_u and "(1)" not in t_low:
            return False

    elif category == "RAM":
        if not is_opaque_hash:
            if ("notebook-ram" in path or "sodimm" in path) and "sodimm" not in t_low and "notebook" not in t_low:
                return False
            if ("server-premier" in path or "ecc-registered" in path or "sunucu-ram" in path) and "ecc" not in t_low:
                return False
            ram_series = (
                "vengeance", "dominator", "trident", "ripjaws", "flare", "lancer",
                "spectrix", "beast", "renegade", "impact", "venom", "viper",
                "thor", "ares", "signature", "delta", "vulcan",
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
            if ("ddr4" in path_words_ram and "ddr5" in title_words_ram) or ("ddr5" in path_words_ram and "ddr4" in title_words_ram):
                return False
            if ("rgb" in path_words_ram) and "rgb" not in title_words_ram:
                return False
            u_white = any(w in path_words_ram for w in ("white", "beyaz"))
            t_white = any(w in title_words_ram for w in ("white", "beyaz"))
            if u_white != t_white:
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

    elif category == "MOUSE":
        if not is_opaque_hash and "arctis" in path and "arctis" not in t_low:
            return False

    elif category == "MOTHERBOARD":
        if not is_opaque_hash:
            m_mb = re.search(r"\b([abxz]\d{3}[a-z]{0,2}(?:\-[a-z0-9]{1,2})?)\b", path)
            if m_mb:
                mb_full = re.sub(r"[^a-z0-9]", "", m_mb.group(1))
                mb_chip = re.match(r"([abxz]\d{3}[a-z]{0,2})", m_mb.group(1)).group(1)
                if mb_chip not in t_flat or (mb_full != mb_chip and mb_full not in t_flat):
                    return False
            for ms in ("tomahawk", "mortar", "carbon", "godlike", "ace", "edge", "gaming-plus", "pro-rs", "steel-legend", "taichi", "pg-lightning", "riptide", "phantom-gaming", "aorus", "tuf", "prime", "proart", "strix", "crosshair", "hero", "apex"):
                if ms in path and ms.replace("-", " ") not in re.sub(r"[-_/]+", " ", t_low):
                    return False

    return True


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

    valid: list[str] = []
    for u in candidates:
        if not health_map.get(u, False):
            continue
        if not is_image_compatible_with_item(category, brand, name, chipset, u):
            continue
        valid.append(u)
    return valid[:6]


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


def build_gallery_index(state: CrawlStateManager, category: str, sources: tuple[str, ...]) -> list[dict[str, Any]]:
    records: list[dict[str, Any]] = []
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


def find_best_gallery_match(
    category: str,
    brand: str,
    name: str,
    chipset: str,
    gallery_records: list[dict[str, Any]],
    health_map: dict[str, bool],
) -> list[str]:
    """Finds the best token-verified studio gallery strictly matching brand & model code."""
    clean_name = _clean_match_string(name)
    name_plus = re.sub(r"\+", " plus ", name or "")
    ck = normalize_canonical_key(name_plus)
    ck_clean = normalize_canonical_key(clean_name)
    bk = normalize_for_brand_match(name_plus)
    t_str = f"{brand} {clean_name} {chipset}".lower()
    t_toks = set(re.findall(r"[a-z0-9]{2,}", t_str)) - {
        "geforce", "radeon", "graphics", "card", "edition", "laptop",
    }
    b_low = (brand or (name.split()[0] if name.split() else "")).lower()

    best_score = 0.0
    best_imgs: list[str] = []
    for rec in gallery_records:
        rec_clean = _clean_match_string(rec["display_name"])
        rec_plus = re.sub(r"\+", " plus ", rec["display_name"] or "")
        rec_ck = normalize_canonical_key(rec_plus)
        rec_ck_clean = normalize_canonical_key(rec_clean)
        rec_bk = normalize_for_brand_match(rec_plus)
        rec_toks = set(re.findall(r"[a-z0-9]{2,}", rec_clean)) - {
            "geforce", "radeon", "graphics", "card", "edition", "laptop",
        }

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

        score = 0.0
        prio_bonus = float(rec.get("priority", 1)) * 0.5
        if rec_ck == ck or (ck_clean and rec_ck_clean == ck_clean) or (bk and rec_bk == bk):
            score = 120.0 + prio_bonus
        else:
            if b_low and b_low not in rec["display_name"].lower() and b_low not in rec["brand"].lower():
                continue
            if category == "GPU":
                g1 = _extract_gpu_sig(t_str)
                g2 = _extract_gpu_sig(rec["display_name"])
                if not g1 or not g2 or g1 != g2:
                    continue
            if category == "CPU":
                m_c1 = re.search(r"\b(\d{4,5}[a-z0-9]{0,4})\b", clean_name)
                m_c2 = re.search(r"\b(\d{4,5}[a-z0-9]{0,4})\b", rec_clean)
                if not (m_c1 and m_c2 and m_c1.group(1) == m_c2.group(1)):
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
                category in ("CPU", "SMARTPHONE", "TABLET", "STORAGE", "PSU", "COOLER", "RAM", "CASE", "MOTHERBOARD")
                and has_model_digit
            ) else 3
            if category == "GPU" and has_model_digit:
                min_req = 2
            if overlap < min_req:
                continue
            score += overlap * 10.0 - abs(len(rec_toks) - len(t_toks)) * 0.5 + prio_bonus
        if score <= best_score:
            continue
        healthy = [
            u for u in rec["images"]
            if health_map.get(u, False) and is_image_compatible_with_item(category, brand, name, chipset, u)
        ]
        if healthy:
            best_score = score
            best_imgs = healthy[:6]
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
            core = re.sub(r"\b(?:\d+(?:gb|tb|mhz|mts|watt|w|va|oc|bit)|cl\d+)\b", "", nm)
            core = re.sub(r"\([^)]*\)", "", core)
            if category == "RAM":
                has_white = any(c in nm for c in ("white", "beyaz", "silver", "gümüş", "snow"))
                has_rgb = "rgb" in nm
                core = re.sub(r"\b(ddr[345])[\-\s]*\d{3,5}\b", "", core)
                core = re.sub(r"\b(amd expo|intel xmp|xmp|expo|\d+\s*x\s*\d+|non-ecc|unbuffered|dimm|sodimm|u-dimm)\b", "", core)
                core = re.sub(r"\b(white|black|silver|gray|grey|neo)\b", "", core)
                core = " ".join(w for w in re.findall(r"[a-z0-9]+", core) if len(w) >= 2)[:32]
                skey = f"{br}|{core}|{'w' if has_white else 'd'}|{'rgb' if has_rgb else 'plain'}"
            elif category == "GPU":
                gpu_toks = sorted(
                    t for t in re.findall(r"[a-z0-9]+", core)
                    if len(t) >= 2 and t not in {"geforce", "radeon", "intel", "edition", "gaming", "oc"}
                )
                skey = f"{br}|{' '.join(gpu_toks[:6])}"
            elif category == "MOTHERBOARD":
                mb_toks = sorted(
                    t for t in re.findall(r"[a-z0-9]+", core)
                    if len(t) >= 2 and t not in {"wifi", "wi", "fi", "gaming", "ddr4", "ddr5", "atx", "matx", "micro"}
                )
                skey = f"{br}|{' '.join(mb_toks[:5])}"
            else:
                core = re.sub(r"\s+", " ", core).strip()
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

        # Dominant majority preservation for RAM (speed/capacity/timing kits share one visual line cleanly)
        if category == "RAM":
            if len(sigs) == 1 and len(clist) <= 65:
                continue
            largest_sig, largest_items = max(sig_to_items.items(), key=lambda kv: len(kv[1]))
            if len(largest_items) >= 3 and (len(largest_items) / len(clist)) >= 0.55 and len(largest_items) <= 65:
                for skey, group_items in sig_to_items.items():
                    if skey != largest_sig:
                        for it in group_items:
                            _clear_item_img(it)
                continue
            for it in clist:
                _clear_item_img(it)
            continue

        # Fallback: quarantine ambiguous collisions
        if category in ("GPU", "LAPTOP", "SMARTPHONE", "TABLET", "CPU"):
            if len(sigs) > 1 or len(clist) > max_allowed_variants:
                for it in clist:
                    _clear_item_img(it)
        elif len(sigs) >= 2:
            largest_sig, largest_items = max(sig_to_items.items(), key=lambda kv: len(kv[1]))
            if category == "MOTHERBOARD" and len(largest_items) >= 2 and (len(largest_items) / len(clist)) >= 0.65:
                for skey, group_items in sig_to_items.items():
                    if skey != largest_sig:
                        for it in group_items:
                            _clear_item_img(it)
            else:
                for it in clist:
                    _clear_item_img(it)
        elif len(clist) > max_allowed_variants:
            for it in clist:
                _clear_item_img(it)
