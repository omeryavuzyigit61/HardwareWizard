#!/usr/bin/env python3
"""Cross-source catalog merger building enriched catalog JSONs with verified, title-matched images."""

from __future__ import annotations

import collections
import json
import os
import re
import subprocess
import sys
from datetime import datetime, timezone
from typing import Any

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)

from scripts.crawlers.http_client import PoliteHttpClient
from scripts.crawlers.image_curator import (
    AIB_SERIES_TOKENS,
    _clean_match_string,
    _extract_gpu_sig,
    backfill_missing_via_wikimedia,
    build_gallery_index,
    clean_doubled_gpu_name,
    collect_repo_asset_urls,
    dedupe_synthetic_peripheral_clones,
    filter_and_prune_item_images,
    find_best_gallery_match,
    is_image_compatible_with_item,
    is_valid_gpu_product,
    quarantine_cross_model_collisions,
)
from scripts.crawlers.sources_images import verify_image_url_pool
from scripts.crawlers.state_manager import (
    CrawlStateManager,
    normalize_canonical_key,
    normalize_for_brand_match,
)

FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")
DATA_CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
DATA_DIR = os.path.join(ROOT_DIR, "data")
VERIFIED_DATE = datetime.now(timezone.utc).strftime("%Y-%m-%d")


def _load_json(path: str, default: Any) -> Any:
    if not os.path.exists(path):
        return default
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def _load_baseline_29c4a2e(filename: str) -> list[dict[str, Any]]:
    """Loads baseline catalog products from commit 29c4a2e so no real products or images are ever dropped."""
    try:
        raw = subprocess.check_output(
            ["git", "show", f"29c4a2e:shared/src/commonMain/composeResources/files/{filename}"],
            cwd=ROOT_DIR,
            stderr=subprocess.DEVNULL,
        ).decode("utf-8")
        data = json.loads(raw)
        return data if isinstance(data, list) else []
    except Exception:
        return []


_MOBILE_CPU_RE = re.compile(
    r"(?:\b\d{4}(?:U|HS|HX|H|V)\b|\b2[23568][68]V\b|\b1[0-6]\d(?:HL|UL|H|U)\b|\bRYZEN\s+(?:Z1|AI\s+MAX)\b|\bRYZEN\s+[357]\s+\d{2,3}\b)",
    re.I,
)


def _save_json_both(filename: str, data: Any) -> None:
    for folder in (FILES_DIR, DATA_CATALOG_DIR):
        os.makedirs(folder, exist_ok=True)
        out_path = os.path.join(folder, filename)
        with open(out_path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)


def _index_raw_by_source(state: CrawlStateManager, category: str) -> dict[str, dict[str, dict[str, Any]]]:
    by_src: dict[str, dict[str, dict[str, Any]]] = collections.defaultdict(dict)
    for row in state.load_records_for_category(category):
        rec = {
            "display_name": row["display_name"],
            "brand": row["brand"],
            "payload": json.loads(row["payload_json"] or "{}"),
            "extra_specs": json.loads(row["extra_specs_json"] or "{}"),
            "images": json.loads(row["images_json"] or "[]"),
            "source_url": row["source_url"],
        }
        ckey = row["canonical_key"]
        by_src[row["source_name"]][ckey] = rec
        bkey = normalize_for_brand_match(row["display_name"])
        if bkey and bkey not in by_src[row["source_name"]]:
            by_src[row["source_name"]][bkey] = rec
    return by_src


def _match_lookup(src_map: dict[str, dict[str, Any]], *names: str | None) -> dict[str, Any] | None:
    for raw in names:
        if not raw:
            continue
        for candidate in (raw, re.sub(r"\s+(?:Wireless|Xbox|PlayStation|SE|Edition|Gaming)\b", "", raw, flags=re.I)):
            k1 = normalize_canonical_key(candidate)
            if k1 in src_map:
                return src_map[k1]
            k2 = normalize_for_brand_match(candidate)
            if k2 in src_map:
                return src_map[k2]
    return None


def _is_modern_cpu_name(name: str, socket: str = "") -> bool:
    u = f"{name} {socket}".upper()
    bad = ("A4-", "A6-", "A8-", "A10-", "A12-", "ATHLON II", "PHENOM", "SEMPRON", "OPTERON", "FX-", "PENTIUM", "CELERON", "CORE 2", "XEON", "ATOM", "EMBEDDED", "AM1", "AM2", "AM3", "FM1", "FM2", "775", "1156", "1155", "1150", "1151")
    if any(b in u for b in bad):
        return False
    is_amd = any(k in u for k in ("RYZEN", "THREADRIPPER", "3000G", "200GE")) and any(
        s in u for s in ("AM4", "AM5", "TR4", "TRX4", "STRX4", "STR5", "WRX8", "SWRX8", "RYZEN")
    )
    is_intel = any(
        k in u
        for k in (
            "CORE ULTRA", "285K", "275K", "265K", "255K", "245K",
            "14900", "14700", "14600", "14500", "14400", "14100",
            "13900", "13700", "13600", "13500", "13400", "13100",
            "12900", "12700", "12600", "12500", "12400", "12100",
            "11900", "11700", "11600", "11500", "11400",
            "10900", "10700", "10600", "10500", "10400", "10100", "10105",
        )
    )
    return is_amd or is_intel


def _hid(category: str, name: str) -> str:
    san = re.sub(r"_+", "_", re.sub(r"[^a-z0-9]", "_", (name or "").lower())).strip("_")
    return f"{category.lower()}_{san}"


def _seed_prices_tr_image(
    item: dict[str, Any],
    category: str,
    prices_tr: dict[str, Any] | None,
    image_key: str = "imageUrl",
) -> None:
    if not prices_tr or item.get("images") or item.get(image_key):
        return
    item_id = item.get("id") or _hid(category, item.get("name") or "")
    entry = prices_tr.get(item_id)
    if isinstance(entry, dict):
        u = entry.get("imageUrl")
        if isinstance(u, str) and u.startswith("https://"):
            item["images"] = [u]
            item[image_key] = u


def _apply_curated_gallery(
    item: dict[str, Any],
    category: str,
    gallery_recs: list[dict[str, Any]],
    health_map: dict[str, bool],
    image_key: str = "imageUrl",
    prefer_gallery: bool = False,
    prices_tr: dict[str, Any] | None = None,
) -> None:
    _seed_prices_tr_image(item, category, prices_tr, image_key=image_key)
    brand = str(item.get("brand") or (item.get("name", "").split()[0] if item.get("name") else ""))
    name = str(item.get("name") or item.get("model") or "")
    chipset = str(item.get("chipset") or "")

    existing_clean = filter_and_prune_item_images(item, category, health_map, image_key=image_key)
    score_holder: list[float] = []
    matched = find_best_gallery_match(category, brand, name, chipset, gallery_recs, health_map, score_out=score_holder) if gallery_recs else []
    best_score = score_holder[0] if score_holder else 0.0

    if prefer_gallery and matched and (best_score >= 100.0 or not existing_clean):
        final = list(dict.fromkeys(matched + existing_clean))[:6]
        if category in ("CPU", "GPU", "MOTHERBOARD", "RAM", "STORAGE", "COOLER", "CASE", "LAPTOP", "SMARTPHONE", "TABLET"):
            item["_img_match_score"] = best_score
    elif not existing_clean and matched:
        final = matched[:6]
        if category in ("CPU", "GPU", "MOTHERBOARD", "RAM", "STORAGE", "COOLER", "CASE", "LAPTOP", "SMARTPHONE", "TABLET"):
            item["_img_match_score"] = best_score
    elif len(existing_clean) < 2 and matched:
        final = list(dict.fromkeys(existing_clean + matched))[:6]
        if category in ("CPU", "GPU", "MOTHERBOARD", "RAM", "STORAGE", "COOLER", "CASE", "LAPTOP", "SMARTPHONE", "TABLET"):
            item["_img_match_score"] = best_score
    else:
        final = existing_clean

    item["images"] = final
    if final:
        item[image_key] = final[0]
        if image_key == "image_url":
            item.pop("imageUrl", None)
        else:
            item.pop("image_url", None)
    else:
        item.pop("imageUrl", None)
        if image_key == "image_url":
            item["image_url"] = None
        else:
            item.pop("image_url", None)


def _quarantine_with_web_rescue(
    state: CrawlStateManager,
    items: list[dict[str, Any]],
    category: str,
    health_map: dict[str, bool],
    image_key: str = "imageUrl",
    max_allowed_variants: int = 10,
) -> None:
    """Quarantines shared-image collisions, then rescues empty items from unique Web Curated records."""
    quarantine_cross_model_collisions(items, category, image_key=image_key, max_allowed_variants=max_allowed_variants)
    wc_gallery = build_gallery_index(state, category, ("Web Curated",))
    if not wc_gallery:
        return
    rescued = False
    for it in items:
        if not it.get(image_key) and not it.get("images"):
            _apply_curated_gallery(it, category, wc_gallery, health_map, image_key=image_key, prefer_gallery=False, prices_tr=None)
            if it.get(image_key):
                rescued = True
    if rescued:
        quarantine_cross_model_collisions(items, category, image_key=image_key, max_allowed_variants=max_allowed_variants)


_PCP_JSONL_TO_CAT = {
    "cpu": "CPU",
    "video-card": "GPU",
    "motherboard": "MOTHERBOARD",
    "memory": "RAM",
    "internal-hard-drive": "STORAGE",
    "power-supply": "PSU",
    "cpu-cooler": "COOLER",
    "case": "CASE",
}


def _load_pcp_jsonl_galleries() -> dict[str, list[dict[str, Any]]]:
    """Loads authentic studio product photos from data/pcpartpicker_parts.jsonl by category."""
    path = os.path.join(DATA_DIR, "pcpartpicker_parts.jsonl")
    by_cat: dict[str, list[dict[str, Any]]] = collections.defaultdict(list)
    if not os.path.exists(path):
        return by_cat
    seen_name: dict[str, set[str]] = collections.defaultdict(set)
    with open(path, "r", encoding="utf-8") as f:
        for line in f:
            try:
                rec = json.loads(line)
            except Exception:
                continue
            cat = _PCP_JSONL_TO_CAT.get(str(rec.get("category") or ""))
            if not cat:
                continue
            raw_u = str(rec.get("image_url") or "").strip()
            if "cdna.pcpartpicker.com" not in raw_u or "no-image" in raw_u:
                continue
            name = str(rec.get("name") or "").strip()
            if not name:
                continue
            ck = normalize_canonical_key(name)
            if not ck or ck in seen_name[cat]:
                continue
            seen_name[cat].add(ck)
            hi_u = raw_u.replace(".256p.jpg", ".1600.jpg")
            imgs = [hi_u, raw_u] if hi_u != raw_u else [raw_u]
            brand = str(rec.get("brand") or (name.split()[0] if name.split() else "")).strip()
            by_cat[cat].append(
                {
                    "source": "PCPartPicker Dataset",
                    "priority": 3,
                    "display_name": name,
                    "brand": brand,
                    "ckey": ck,
                    "bkey": normalize_for_brand_match(name),
                    "tokens": set(re.findall(r"[a-z0-9]{2,}", _clean_match_string(name))),
                    "images": imgs,
                }
            )
    return by_cat


def _merge_cpus(
    state: CrawlStateManager,
    benchmarks: dict[str, Any],
    health_map: dict[str, bool],
    prices_tr: dict[str, Any] | None = None,
    pcp_gallery: list[dict[str, Any]] | None = None,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "cpu.json"), [])
    baseline = _load_baseline_29c4a2e("cpu.json")
    baseline_keys = {
        normalize_canonical_key(it.get("name") or "")
        for it in baseline
        if it.get("name")
    }
    by_name: dict[str, dict[str, Any]] = {}
    for item in baseline + existing:
        nm = item.get("name") or ""
        if not nm or "EMBEDDED" in nm.upper():
            continue
        ck = normalize_canonical_key(nm)
        is_orig = ck in baseline_keys
        if not is_orig and (_MOBILE_CPU_RE.search(nm) or not _is_modern_cpu_name(nm, str(item.get("socket") or ""))):
            continue
        if ck not in by_name:
            by_name[ck] = dict(item)
        else:
            merged = by_name[ck]
            for k, v in item.items():
                if v not in (None, "", []) and not merged.get(k):
                    merged[k] = v

    raw_src = _index_raw_by_source(state, "CPU")
    pcp = raw_src.get("pc-part-dataset", {})
    tpu = raw_src.get("TechPowerUp Ajax", {})
    pm = raw_src.get("PassMark", {})
    gg_gallery = build_gallery_index(state, "CPU", ("GamingGen Store API", "Itopya Catalog", "Repo Assets", "Web Curated"))
    if pcp_gallery:
        gg_gallery.extend(pcp_gallery)

    for rec in list(pcp.values()) + list(tpu.values()):
        name = rec["display_name"]
        p = rec["payload"]
        sock = str(p.get("socket") or "")
        if _MOBILE_CPU_RE.search(name) or not _is_modern_cpu_name(name, sock):
            continue
        ckey = normalize_canonical_key(name)
        if ckey not in by_name:
            entry: dict[str, Any] = {
                "name": name,
                "core_count": p.get("core_count"),
                "threads": p.get("threads"),
                "core_clock": p.get("core_clock"),
                "boost_clock": p.get("boost_clock"),
                "microarchitecture": p.get("microarchitecture"),
                "tdp": p.get("tdp"),
                "graphics": p.get("graphics"),
                "socket": sock or None,
                "l3_cache": p.get("l3_cache"),
                "process_node": p.get("process_node"),
                "release_date": p.get("release_date"),
                "images": [],
            }
            by_name[ckey] = {k: v for k, v in entry.items() if v is not None}

    cpu_bench_map = benchmarks.setdefault("cpus", {})
    enriched_list: list[dict[str, Any]] = []
    for ckey, item in by_name.items():
        name = item["name"]
        prov: list[str] = ["HardwareWizard Katalog"] if item.get("price") or item.get("l2_cache") else []
        extra: dict[str, str] = dict(item.get("extra_specs") or {})

        m_pcp = _match_lookup(pcp, name)
        if m_pcp:
            prov.append("pc-part-dataset")
            pp = m_pcp["payload"]
            for fld in ("core_count", "core_clock", "boost_clock", "microarchitecture", "tdp", "graphics"):
                if not item.get(fld) and pp.get(fld) is not None:
                    item[fld] = pp[fld]

        m_tpu = _match_lookup(tpu, name)
        if m_tpu:
            prov.append("TechPowerUp DB")
            tp = m_tpu["payload"]
            for fld in ("socket", "core_count", "threads", "core_clock", "boost_clock", "l3_cache", "tdp", "process_node", "microarchitecture", "release_date"):
                if not item.get(fld) and tp.get(fld) is not None:
                    item[fld] = tp[fld]
            extra.update(m_tpu["extra_specs"])

        m_pm = _match_lookup(pm, name)
        if m_pm:
            prov.append("PassMark")
            extra.update(m_pm["extra_specs"])
            pm_score = m_pm["payload"].get("passmark_cpu_mark")
            if pm_score:
                hw_id = f"cpu_{re.sub(r'_+', '_', re.sub(r'[^a-z0-9]', '_', name.lower())).strip('_')}"
                b_rec = cpu_bench_map.setdefault(hw_id, {"synthetic": {}})
                syn = b_rec.setdefault("synthetic", {})
                syn["passmark_cpu_mark"] = pm_score
                if "score" not in b_rec:
                    rel = round(min(0.99, max(0.25, pm_score / 62000.0)), 3)
                    b_rec["gaming_relative_index"] = rel
                    b_rec["score"] = int(rel * 100)

        if item.get("core_count") and not item.get("threads"):
            item["threads"] = item["core_count"]

        _apply_curated_gallery(item, "CPU", gg_gallery, health_map, image_key="imageUrl", prefer_gallery=True, prices_tr=prices_tr)
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov)) or ["Katalog Verisi"]
        item["spec_verified_at"] = VERIFIED_DATE
        item["_is_orig"] = ckey in baseline_keys
        enriched_list.append(item)

    enriched_list.sort(
        key=lambda x: (
            1 if x.get("_is_orig") else 0,
            1 if x.get("price") else 0,
            x.get("release_date") or "",
            x.get("name") or "",
        ),
        reverse=True,
    )
    for it in enriched_list:
        it.pop("_is_orig", None)
    cpus_final = enriched_list[:550]
    _quarantine_with_web_rescue(state, cpus_final, "CPU", health_map, image_key="imageUrl", max_allowed_variants=6)
    return cpus_final


def _gpu_pcp_sig_key(name: str, brand: str, chip: str) -> tuple[str, str, tuple[str, ...]] | None:
    nm_l = re.sub(r"[^a-z0-9]+", " ", (name or "").lower())
    br = (brand or (nm_l.split()[0] if nm_l.split() else "")).lower()
    sig = _extract_gpu_sig(f"{name} {chip}")
    if not br or not sig:
        return None
    aibs = tuple(sorted(s for s in AIB_SERIES_TOKENS if s.replace("_", " ") in nm_l))
    return (br, sig, aibs)


def _infer_gpu_brand(name: str, chipset: str = "") -> str:
    first = name.split()[0] if name.split() else ""
    up_first = first.upper()
    if up_first in ("ASUS", "MSI", "GIGABYTE", "ASROCK", "SAPPHIRE", "POWERCOLOR", "XFX", "ZOTAC", "PALIT", "PNY", "GAINWARD", "INNO3D", "GALAX", "KFA2", "EVGA", "AMD", "NVIDIA", "INTEL", "BIOSTAR", "MAXSUN", "COLORFUL", "SPARKLE", "YESTON", "ACER", "LENOVO", "DELL", "HP"):
        return first
    cl = f"{name} {chipset}".lower()
    if "radeon" in cl:
        return "AMD"
    if "geforce" in cl or "rtx" in cl or "gtx" in cl:
        return "NVIDIA"
    if "arc" in cl:
        return "Intel"
    return first or "GPU"


def _merge_gpus(
    state: CrawlStateManager,
    benchmarks: dict[str, Any],
    health_map: dict[str, bool],
    prices_tr: dict[str, Any] | None = None,
    pcp_gallery: list[dict[str, Any]] | None = None,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "video-card.json"), [])
    baseline = _load_baseline_29c4a2e("video-card.json")
    raw_src = _index_raw_by_source(state, "GPU")
    tpu = raw_src.get("TechPowerUp Ajax", {})
    pm = raw_src.get("PassMark", {})
    pcp = raw_src.get("pc-part-dataset", {})
    gg_gallery = build_gallery_index(state, "GPU", ("GamingGen Store API", "Itopya Catalog", "Repo Assets", "Web Curated"))
    if pcp_gallery:
        gg_gallery.extend(pcp_gallery)
    tpu_gallery = build_gallery_index(state, "GPU", ("TechPowerUp Ajax",))

    by_name: dict[str, dict[str, Any]] = {}
    by_vram_trimmed: dict[str, str] = {}
    for item in existing + baseline:
        nm_raw = item.get("name") or ""
        ch = item.get("chipset") or ""
        nm = clean_doubled_gpu_name(nm_raw)
        if nm and is_valid_gpu_product(nm, ch):
            it_clean = dict(item)
            it_clean["name"] = nm
            br_inf = item.get("brand") or _infer_gpu_brand(nm, ch)
            it_clean["brand"] = br_inf
            ckey = normalize_canonical_key(nm)
            vtrim = re.sub(r"_+", "_", re.sub(r"\d+(?:gb|tb)", "", ckey)).strip("_")
            if br_inf.upper() in ("AMD", "NVIDIA", "INTEL") and vtrim in by_vram_trimmed:
                prev = by_name[by_vram_trimmed[vtrim]]
                if len(nm) > len(prev["name"]):
                    for k, v in prev.items():
                        it_clean.setdefault(k, v)
                    del by_name[by_vram_trimmed[vtrim]]
                    by_name[ckey] = it_clean
                    by_vram_trimmed[vtrim] = ckey
                continue
            if ckey not in by_name:
                by_name[ckey] = it_clean
                by_vram_trimmed.setdefault(vtrim, ckey)
            else:
                prev = by_name[ckey]
                for k, v in it_clean.items():
                    if v not in (None, "", []) and not prev.get(k):
                        prev[k] = v

    # Add modern discrete desktop cards from TPU (with real tpucdn.com photos) and major imaged pc-part-dataset entries
    for rec in list(tpu.values()) + list(pcp.values()):
        name = clean_doubled_gpu_name(rec["display_name"])
        chip = str(rec["payload"].get("chipset") or name)
        if not is_valid_gpu_product(name, chip):
            continue
        up = f"{name} {chip}".upper()
        if not any(k in up for k in ("RTX 50", "RX 90", "ARC B5", "RTX 40", "RX 7")):
            continue
        ckey = normalize_canonical_key(name)
        imgs = [u for u in (rec.get("images") or []) if health_map.get(u, False)]
        # If reference TPU entry matches existing card with VRAM suffix (e.g. "AMD Radeon RX 9070 XT 16GB"), enrich existing instead of duplicating
        existing_alias_key = by_vram_trimmed.get(re.sub(r"_+", "_", re.sub(r"\d+(?:gb|tb)", "", ckey)).strip("_"))
        target_key = ckey if ckey in by_name else (existing_alias_key if existing_alias_key in by_name else ckey)
        if target_key not in by_name:
            gg_hit = find_best_gallery_match("GPU", rec.get("brand") or "", name, chip, gg_gallery, health_map)
            if not imgs and not gg_hit and not any(k in up for k in ("RTX 5090", "RTX 5080", "RTX 5070", "RX 9070", "ARC B580", "ARC B570")):
                continue
            final_imgs = gg_hit or imgs
            by_name[ckey] = {
                "name": name,
                "brand": rec.get("brand") or _infer_gpu_brand(name, chip),
                "chipset": chip,
                "memory": rec["payload"].get("memory"),
                "memory_type": rec["payload"].get("memory_type"),
                "core_clock": rec["payload"].get("core_clock"),
                "boost_clock": rec["payload"].get("boost_clock"),
                "length": rec["payload"].get("length"),
                "images": final_imgs,
                "imageUrl": final_imgs[0] if final_imgs else None,
            }
            by_vram_trimmed.setdefault(re.sub(r"_+", "_", re.sub(r"\d+(?:gb|tb)", "", ckey)).strip("_"), ckey)
        elif imgs and not by_name[target_key].get("images"):
            by_name[target_key]["images"] = imgs
            by_name[target_key]["imageUrl"] = imgs[0]

    gpu_bench_map = benchmarks.setdefault("gpus", {})
    pcp_gpu_sigs = {
        k
        for rec in pcp.values()
        for k in (_gpu_pcp_sig_key(rec["display_name"], rec.get("brand") or "", str(rec["payload"].get("chipset") or "")),)
        if k is not None
    }
    result: list[dict[str, Any]] = []
    for item in by_name.values():
        name = item["name"]
        chip = item.get("chipset") or name
        prov: list[str] = ["HardwareWizard Katalog"] if item.get("price") or item.get("power_connectors") else []
        extra: dict[str, str] = dict(item.get("extra_specs") or {})

        m_pcp = _match_lookup(pcp, name, chip)
        if m_pcp:
            prov.append("pc-part-dataset")
            if not item.get("length") and m_pcp["payload"].get("length"):
                item["length"] = m_pcp["payload"]["length"]
        pcp_sig_k = _gpu_pcp_sig_key(name, item.get("brand") or "", chip)
        item["_pcp_backed"] = bool(m_pcp or (pcp_sig_k in pcp_gpu_sigs))

        m_tpu = _match_lookup(tpu, chip, name)
        if m_tpu:
            prov.append("TechPowerUp DB")
            tp = m_tpu["payload"]
            for fld in ("tmus", "rops", "pcie_interface", "gpu_die", "release_date", "cuda_cores", "bus_width", "memory_type"):
                if not item.get(fld) and tp.get(fld) is not None:
                    item[fld] = tp[fld]
            extra.update(m_tpu["extra_specs"])
            shaders = item.get("cuda_cores") or tp.get("cuda_cores")
            clk_mhz = item.get("boost_clock") or item.get("core_clock")
            if shaders and clk_mhz and not item.get("fp32_tflops"):
                try:
                    tflops = round((int(shaders) * int(clk_mhz) * 2) / 1_000_000.0, 2)
                    if 0.5 <= tflops <= 150.0:
                        item["fp32_tflops"] = tflops
                        extra["FP32 Tek Duyarlıklı Güç"] = f"{tflops} TFLOPS"
                except Exception:
                    pass

        m_pm = _match_lookup(pm, chip, name)
        if m_pm:
            prov.append("PassMark")
            extra.update(m_pm["extra_specs"])
            g3d = m_pm["payload"].get("passmark_g3d_mark")
            if g3d:
                hw_id = f"gpu_{re.sub(r'_+', '_', re.sub(r'[^a-z0-9]', '_', name.lower())).strip('_')}"
                if hw_id in gpu_bench_map:
                    gpu_bench_map[hw_id].setdefault("synthetic", {})["passmark_g3d_mark"] = g3d

        # Strictly prefer GamingGen/Itopya/Repo AIB photos first; fall back to TPU ONLY for real AMD/NVIDIA/Intel reference cards
        first_word_u = (name.split()[0] if name.split() else "").upper()
        _apply_curated_gallery(item, "GPU", gg_gallery, health_map, image_key="imageUrl", prefer_gallery=True, prices_tr=prices_tr)
        if not item.get("images") and first_word_u in ("NVIDIA", "AMD", "INTEL", "RADEON", "GEFORCE", "ARC"):
            _apply_curated_gallery(item, "GPU", tpu_gallery, health_map, image_key="imageUrl", prefer_gallery=True, prices_tr=prices_tr)

        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov)) or ["Katalog Verisi"]
        item["spec_verified_at"] = VERIFIED_DATE
        result.append(item)

    _quarantine_with_web_rescue(state, result, "GPU", health_map, image_key="imageUrl", max_allowed_variants=10)
    return result


_HANDHELD_NON_SSD_TOKENS = ("ayaneo", "steam deck", "rog ally", "legion go")


def _clean_storage_name(name: str) -> str:
    return re.sub(r"\bWD[_\-]+BLACK\b", "WD Black", name or "", flags=re.I).strip()


def _merge_storage(
    state: CrawlStateManager,
    health_map: dict[str, bool],
    prices_tr: dict[str, Any] | None = None,
    pcp_gallery: list[dict[str, Any]] | None = None,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "internal-hard-drive.json"), [])
    baseline = _load_baseline_29c4a2e("internal-hard-drive.json")
    raw_src = _index_raw_by_source(state, "STORAGE")
    newmaxx = raw_src.get("NewMaxx SSD Sheet", {})
    tpu = raw_src.get("TechPowerUp Ajax", {})
    pcp = raw_src.get("pc-part-dataset", {})
    gallery = build_gallery_index(state, "STORAGE", ("GamingGen Store API", "Itopya Catalog", "Repo Assets", "TechPowerUp Ajax", "Web Curated"))
    if pcp_gallery:
        gallery.extend(pcp_gallery)

    by_key: dict[str, dict[str, Any]] = {}
    for it in existing + baseline:
        raw_nm = _clean_storage_name(it.get("name") or "")
        if not raw_nm or any(h in raw_nm.lower() for h in _HANDHELD_NON_SSD_TOKENS):
            continue
        bkey = normalize_for_brand_match(raw_nm)
        cleaned = dict(it)
        cleaned["name"] = raw_nm
        if bkey not in by_key:
            by_key[bkey] = cleaned
        else:
            for k, v in cleaned.items():
                if v not in (None, "", []) and not by_key[bkey].get(k):
                    by_key[bkey][k] = v

    for rec in list(tpu.values())[:180] + list(newmaxx.values())[:220]:
        name = _clean_storage_name(rec["display_name"])
        if any(h in name.lower() for h in _HANDHELD_NON_SSD_TOKENS):
            continue
        bkey = normalize_for_brand_match(name)
        p = rec["payload"]
        imgs = [u for u in (rec.get("images") or []) if health_map.get(u, False)]
        if bkey not in by_key and p.get("controller"):
            by_key[bkey] = {
                "name": name,
                "capacity": 1000,
                "type": "SSD",
                "form_factor": p.get("form_factor") or "M.2-2280",
                "interface": p.get("interface") or "PCIe 4.0 x4",
                "seq_read": p.get("seq_read"),
                "seq_write": p.get("seq_write"),
                "controller": p.get("controller"),
                "dram_cache": p.get("dram_cache"),
                "nand_type": p.get("nand_type"),
                "images": imgs,
                "imageUrl": imgs[0] if imgs else None,
            }

    out: list[dict[str, Any]] = []
    for item in by_key.values():
        name = item["name"]
        prov: list[str] = ["HardwareWizard Katalog"] if item.get("price") or item.get("endurance_tbw") else []
        extra: dict[str, str] = dict(item.get("extra_specs") or {})

        for src_name, src_map in (("NewMaxx SSD Sheet", newmaxx), ("TechPowerUp DB", tpu), ("pc-part-dataset", pcp)):
            m = _match_lookup(src_map, name)
            if m:
                prov.append(src_name)
                mp = m["payload"]
                for fld in ("controller", "dram_cache", "nand_type", "seq_read", "seq_write"):
                    if not item.get(fld) and mp.get(fld) is not None:
                        item[fld] = mp[fld]
                extra.update(m["extra_specs"])

        _apply_curated_gallery(item, "STORAGE", gallery, health_map, image_key="imageUrl", prefer_gallery=True, prices_tr=prices_tr)
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov)) or ["Katalog Verisi"]
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)

    _quarantine_with_web_rescue(state, out, "STORAGE", health_map, image_key="imageUrl", max_allowed_variants=8)
    return out


def _merge_psu(
    state: CrawlStateManager,
    health_map: dict[str, bool],
    prices_tr: dict[str, Any] | None = None,
    pcp_gallery: list[dict[str, Any]] | None = None,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "power-supply.json"), [])
    baseline_by_key = {
        normalize_canonical_key(b.get("name") or ""): b
        for b in _load_baseline_29c4a2e("power-supply.json")
        if b.get("name")
    }
    raw_src = _index_raw_by_source(state, "PSU")
    iron = raw_src.get("IRONScale Labs", {})
    pcp = raw_src.get("pc-part-dataset", {})
    gallery = build_gallery_index(state, "PSU", ("GamingGen Store API", "Itopya Catalog", "Repo Assets", "IRONScale Labs", "Web Curated"))
    if pcp_gallery:
        gallery.extend(pcp_gallery)

    out: list[dict[str, Any]] = []
    for raw_item in existing:
        item = dict(raw_item)
        name = item.get("name", "")
        if not item.get("images") and not item.get("imageUrl"):
            b = baseline_by_key.get(normalize_canonical_key(name))
            if b and (b.get("imageUrl") or b.get("images")):
                u = b.get("imageUrl") or (b.get("images") or [None])[0]
                if u:
                    item["imageUrl"] = u
                    item["images"] = [u]
        prov = ["IRONScale Labs"]
        extra: dict[str, str] = dict(item.get("extra_specs") or {})
        m_iron = _match_lookup(iron, name)
        if m_iron:
            extra.update(m_iron["extra_specs"])
        m_pcp = _match_lookup(pcp, name)
        if m_pcp:
            prov.append("pc-part-dataset")

        _apply_curated_gallery(item, "PSU", gallery, health_map, image_key="imageUrl", prefer_gallery=True, prices_tr=prices_tr)
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)
    _quarantine_with_web_rescue(state, out, "PSU", health_map, image_key="imageUrl", max_allowed_variants=4)
    return out


def _merge_generic_pcp_category(
    state: CrawlStateManager,
    filename: str,
    cat_key: str,
    health_map: dict[str, bool],
    prices_tr: dict[str, Any] | None = None,
    pcp_gallery: list[dict[str, Any]] | None = None,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, filename), [])
    baseline_by_key = {
        normalize_canonical_key(b.get("name") or ""): b
        for b in _load_baseline_29c4a2e(filename)
        if b.get("name")
    }
    raw_src = _index_raw_by_source(state, cat_key)
    pcp = raw_src.get("pc-part-dataset", {})
    gallery = build_gallery_index(state, cat_key, ("GamingGen Store API", "Itopya Catalog", "Repo Assets", "Web Curated"))
    if pcp_gallery:
        gallery.extend(pcp_gallery)

    out: list[dict[str, Any]] = []
    for raw_item in existing:
        item = dict(raw_item)
        name = item.get("name", "")
        if not item.get("images") and not item.get("imageUrl"):
            b = baseline_by_key.get(normalize_canonical_key(name))
            if b and (b.get("imageUrl") or b.get("images")):
                u = b.get("imageUrl") or (b.get("images") or [None])[0]
                if u:
                    item["imageUrl"] = u
                    item["images"] = [u]
        if cat_key == "MOTHERBOARD" and "sapphire" in name.lower() and "a520m" in name.lower():
            item["socket"] = "AM4"
        prov = ["HardwareWizard Katalog"]
        extra: dict[str, str] = dict(item.get("extra_specs") or {})
        m = _match_lookup(pcp, name)
        if m:
            prov.append("pc-part-dataset")
            extra.update(m["extra_specs"])
        item["_pcp_backed"] = bool(m)

        _apply_curated_gallery(item, cat_key, gallery, health_map, image_key="imageUrl", prefer_gallery=True, prices_tr=prices_tr)
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)

    _quarantine_with_web_rescue(state, out, cat_key, health_map, image_key="imageUrl", max_allowed_variants=8)
    return out


def _is_spare_accessory_misentry(cat: str, name: str) -> bool:
    low = (name or "").lower()
    if any(k in low for k in ("vip reservation", "layout collection")):
        return True
    if cat == "KEYBOARD" and any(k in low for k in ("aluminum plate", "brass plate", "pc plate", "fr4 plate", "z-plate")):
        return True
    if cat == "GAMING_CHAIR" and "armrest" in low:
        return True
    if cat == "GAMING_DESK" and any(k in low for k in ("desk mat", "magpad", "led strip - collection")):
        return True
    return False


def _merge_peripherals(
    state: CrawlStateManager,
    health_map: dict[str, bool],
    prices_tr: dict[str, Any] | None = None,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "peripherals.json"), [])
    existing = [it for it in dedupe_synthetic_peripheral_clones(existing) if not _is_spare_accessory_misentry(it.get("category", ""), it.get("name", ""))]
    baseline_by_id = {
        b.get("id"): b
        for b in _load_baseline_29c4a2e("peripherals.json")
        if isinstance(b, dict) and b.get("id")
    }

    sensor_fyi = _index_raw_by_source(state, "MOUSE").get("Sensor.fyi", {})
    autoeq = _index_raw_by_source(state, "HEADSET").get("AutoEq Acoustic DB", {})
    openwrt = _index_raw_by_source(state, "ROUTER").get("OpenWrt Releases", {})
    nut_ups = _index_raw_by_source(state, "UPS").get("NUT UPS DDL", {})
    pcp_ups = _index_raw_by_source(state, "UPS").get("pc-part-dataset", {})

    shopify_by_cat: dict[str, dict[str, dict[str, Any]]] = {}
    galleries_by_cat: dict[str, list[dict[str, Any]]] = {}
    for cat in (
        "MOUSE", "KEYBOARD", "HEADSET", "MONITOR", "MICROPHONE", "MOUSEPAD",
        "WEBCAM", "GAMING_CHAIR", "CASE_FAN", "UPS", "GAMING_DESK",
        "GAMING_LIGHTING", "SMARTWATCH", "ROUTER", "MODEM", "ACCESS_POINT", "RANGE_EXTENDER",
    ):
        cat_srcs = _index_raw_by_source(state, cat)
        merged_shop: dict[str, dict[str, Any]] = {}
        for sname, smap in cat_srcs.items():
            if "Official" in sname:
                merged_shop.update(smap)
        shopify_by_cat[cat] = merged_shop
        galleries_by_cat[cat] = build_gallery_index(
            state,
            cat,
            ("Keychron Official", "Pulsar Official", "Secretlab Official", "LethalGamingGear Official", "GamingGen Store API", "Itopya Catalog", "GSMArena Gallery", "Web Curated"),
        )

    by_id: dict[str, dict[str, Any]] = {}
    for raw in existing:
        item = dict(raw)
        cat = item.get("category", "")
        name = item.get("name", "")
        brand = item.get("brand", "")
        model = item.get("model", "")
        full_model = f"{brand} {model}".strip() if brand and model and not model.lower().startswith(brand.lower()) else (model or name)
        prov = ["HardwareWizard Katalog"]
        extra: dict[str, str] = dict(item.get("extra_specs") or {})

        if cat == "MOUSE":
            m_sf = _match_lookup(sensor_fyi, full_model, model, name)
            if m_sf:
                prov.append("Sensor.fyi")
                extra.update(m_sf["extra_specs"])
                for fld in ("weight_g", "click_latency_ms", "polling_rate_hz", "sensor", "shape_type"):
                    if not item.get(fld) and m_sf["payload"].get(fld) is not None:
                        item[fld] = m_sf["payload"][fld]

        if cat == "HEADSET":
            m_aeq = _match_lookup(autoeq, full_model, model, name)
            if m_aeq:
                prov.append("AutoEq Acoustic DB")
                extra.update(m_aeq["extra_specs"])

        if cat in shopify_by_cat:
            m_sh = _match_lookup(shopify_by_cat[cat], full_model, model, name)
            if m_sh:
                prov.append("Üretici Resmi Katalog")
                extra.update(m_sh["extra_specs"])
                if m_sh["images"]:
                    item["images"] = m_sh["images"]
                    item["image_url"] = m_sh["images"][0]

        if cat == "UPS":
            for sname, smap in (("NUT UPS DDL", nut_ups), ("pc-part-dataset", pcp_ups)):
                m_u = _match_lookup(smap, full_model, model, name)
                if m_u:
                    prov.append(sname)
                    extra.update(m_u["extra_specs"])

        if cat in ("ROUTER", "MODEM", "ACCESS_POINT", "RANGE_EXTENDER"):
            m_ow = _match_lookup(openwrt, full_model, model, name)
            if m_ow:
                prov.append("OpenWrt Releases")
                extra.update(m_ow["extra_specs"])

        # Normalize Keychron / Lemokey vendor strings mistakenly saved as Shopify collection names (preserve SteelSeries!)
        br_low_per = str(item.get("brand") or "").lower()
        if "steelseries" in item.get("id", "").lower() or "steelseries" in name.lower():
            item["brand"] = "SteelSeries"
        elif br_low_per.endswith(" series") or item.get("brand") in ("ISO Keyboards", "Trackball Series"):
            item["brand"] = "Lemokey" if "lemokey" in name.lower() else "Keychron"

        if not item.get("images") and not item.get("image_url") and not item.get("imageUrl"):
            b_per = baseline_by_id.get(item.get("id"))
            if b_per and (b_per.get("image_url") or b_per.get("imageUrl") or b_per.get("images")):
                bu = b_per.get("image_url") or b_per.get("imageUrl") or (b_per.get("images") or [None])[0]
                if bu:
                    item["image_url"] = bu
                    item["images"] = [bu]

        _apply_curated_gallery(item, cat, galleries_by_cat.get(cat, []), health_map, image_key="image_url", prefer_gallery=False, prices_tr=prices_tr)
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        by_id[item["id"]] = item

    # Idempotently top up only up to target category ceilings so repeated runs never bloat peripherals.json
    cat_caps = {
        "MOUSE": 330,
        "KEYBOARD": 380,
        "MOUSEPAD": 350,
        "GAMING_CHAIR": 250,
        "GAMING_DESK": 65,
        "ROUTER": 260,
        "UPS": 210,
    }
    _append_new_peripherals(by_id, "MOUSE", shopify_by_cat.get("MOUSE", {}), "Üretici Resmi Katalog", health_map, limit=45, max_total=cat_caps["MOUSE"])
    _append_new_peripherals(by_id, "KEYBOARD", shopify_by_cat.get("KEYBOARD", {}), "Üretici Resmi Katalog", health_map, limit=55, max_total=cat_caps["KEYBOARD"])
    _append_new_peripherals(by_id, "MOUSEPAD", shopify_by_cat.get("MOUSEPAD", {}), "Üretici Resmi Katalog", health_map, limit=40, max_total=cat_caps["MOUSEPAD"])
    _append_new_peripherals(by_id, "GAMING_CHAIR", shopify_by_cat.get("GAMING_CHAIR", {}), "Secretlab Resmi", health_map, limit=25, max_total=cat_caps["GAMING_CHAIR"])
    _append_new_peripherals(by_id, "GAMING_DESK", shopify_by_cat.get("GAMING_DESK", {}), "Secretlab Resmi", health_map, limit=15, max_total=cat_caps["GAMING_DESK"])
    _append_new_peripherals(by_id, "ROUTER", openwrt, "OpenWrt Releases", health_map, limit=60, max_total=cat_caps["ROUTER"])
    _append_new_peripherals(by_id, "UPS", pcp_ups, "pc-part-dataset", health_map, limit=45, max_total=cat_caps["UPS"])

    return list(by_id.values())


def _append_new_peripherals(
    by_id: dict[str, dict[str, Any]],
    category: str,
    src_map: dict[str, dict[str, Any]],
    source_label: str,
    health_map: dict[str, bool],
    limit: int,
    max_total: int = 9999,
) -> None:
    cur_count = sum(1 for v in by_id.values() if v.get("category") == category)
    if cur_count >= max_total:
        return
    existing_keys = {normalize_canonical_key(v.get("name", "")) for v in by_id.values() if v.get("category") == category}
    added = 0
    seen_recs: set[str] = set()
    for rec in src_map.values():
        if added >= limit or (cur_count + added) >= max_total:
            break
        name = rec["display_name"]
        if _is_spare_accessory_misentry(category, name):
            continue
        ckey = normalize_canonical_key(name)
        if not ckey or ckey in existing_keys or ckey in seen_recs:
            continue
        seen_recs.add(ckey)
        new_id = f"{category.lower()}_{ckey}"
        imgs = [u for u in (rec.get("images") or []) if health_map.get(u, False)]
        raw_br = str(rec.get("brand") or name.split()[0]).strip()
        if raw_br.lower().endswith(" series") or raw_br in ("ISO Keyboards", "Trackball Series"):
            brand_clean = "Lemokey" if "lemokey" in name.lower() else "Keychron"
        else:
            brand_clean = raw_br
        model_clean = name[len(brand_clean):].strip() if name.lower().startswith(brand_clean.lower() + " ") else name
        entry: dict[str, Any] = {
            "id": new_id,
            "category": category,
            "name": name,
            "brand": brand_clean,
            "model": model_clean or name,
            "image_url": imgs[0] if imgs else None,
            "images": imgs,
            "extra_specs": rec.get("extra_specs") or {},
            "source_provenance": [source_label],
            "spec_verified_at": VERIFIED_DATE,
        }
        entry.update({k: v for k, v in (rec.get("payload") or {}).items() if v is not None})
        by_id[new_id] = entry
        added += 1


def _dedupe_leading_brand(brand: str, model: str) -> str:
    br = (brand or "").strip()
    md = (model or "").strip()
    if br and md and md.lower().startswith(br.lower() + " "):
        return md[len(br):].strip()
    return md


def _merge_mobile(
    state: CrawlStateManager,
    filename: str,
    category: str,
    health_map: dict[str, bool],
    prices_tr: dict[str, Any] | None = None,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, filename), [])
    baseline_by_id = {
        b.get("id"): b
        for b in _load_baseline_29c4a2e(filename)
        if isinstance(b, dict) and b.get("id")
    }
    raw_src = _index_raw_by_source(state, category)
    dxo = raw_src.get("DXOMARK", {})
    gsm = raw_src.get("GSMArena", {})
    nb_cpu = _index_raw_by_source(state, "CPU_MOBILE_REF").get("Notebookcheck", {})
    gallery = build_gallery_index(state, category, ("GSMArena Gallery", "DXOMARK", "GSMArena", "GamingGen Store API", "Itopya Catalog", "Web Curated"))

    out: list[dict[str, Any]] = []
    for raw_item in existing:
        item = dict(raw_item)
        spec = dict(item.get("spec") or {})
        brand_str = str(item.get("brand") or "").strip()
        clean_model = _dedupe_leading_brand(brand_str, str(item.get("model") or ""))
        if clean_model:
            item["model"] = clean_model
        raw_name = item.get("name") or f"{brand_str} {clean_model}".strip()
        name = re.sub(
            rf"^{re.escape(brand_str)}\s+{re.escape(brand_str)}\b",
            brand_str,
            raw_name,
            flags=re.I,
        ).strip() if brand_str else raw_name
        if "name" in item:
            item["name"] = name
        if not item.get("images") and not item.get("imageUrl") and not item.get("image_url"):
            b_mob = baseline_by_id.get(item.get("id"))
            if b_mob and (b_mob.get("imageUrl") or b_mob.get("image_url") or b_mob.get("images")):
                bu = b_mob.get("imageUrl") or b_mob.get("image_url") or (b_mob.get("images") or [None])[0]
                if bu:
                    item["imageUrl"] = bu
                    item["images"] = [bu]
        prov = ["HardwareWizard Katalog"]
        extra: dict[str, str] = dict(item.get("extra_specs") or {})

        m_dxo = _match_lookup(dxo, name, item.get("model"))
        if m_dxo:
            prov.append("DXOMARK")
            dp = m_dxo["payload"]
            for fld in ("dxomark_camera", "dxomark_selfie", "dxomark_display", "dxomark_battery", "dxomark_audio"):
                if dp.get(fld):
                    camel = "".join(w.title() if i > 0 else w for i, w in enumerate(fld.split("_")))
                    spec[camel] = dp[fld]
            extra.update(m_dxo["extra_specs"])

        m_gsm = _match_lookup(gsm, name, item.get("model"))
        if m_gsm:
            prov.append("GSMArena")
            extra.update(m_gsm["extra_specs"])
            if m_gsm["extra_specs"].get("USB Bağlantı Standardı"):
                spec["usbSpeed"] = m_gsm["extra_specs"]["USB Bağlantı Standardı"]
            if m_gsm["extra_specs"].get("Kablosuz Ağ (Wi-Fi)"):
                spec["wifiBluetooth"] = m_gsm["extra_specs"]["Kablosuz Ağ (Wi-Fi)"]

        if spec.get("chipset") and nb_cpu:
            m_soc = _match_lookup(nb_cpu, spec["chipset"])
            if m_soc:
                prov.append("Notebookcheck")
                extra.update(m_soc["extra_specs"])

        _apply_curated_gallery(item, category, gallery, health_map, image_key="imageUrl", prefer_gallery=True, prices_tr=prices_tr)
        item["spec"] = spec
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)

    quarantine_cross_model_collisions(out, category, image_key="imageUrl", max_allowed_variants=2)
    return out


def _merge_laptops(
    state: CrawlStateManager,
    health_map: dict[str, bool],
    prices_tr: dict[str, Any] | None = None,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "laptop.json"), [])
    baseline_by_id = {
        b.get("id"): b
        for b in _load_baseline_29c4a2e("laptop.json")
        if isinstance(b, dict) and b.get("id")
    }
    nb_cpu = _index_raw_by_source(state, "CPU_MOBILE_REF").get("Notebookcheck", {})
    gallery = build_gallery_index(state, "LAPTOP", ("GamingGen Store API", "Itopya Catalog", "Web Curated"))

    out: list[dict[str, Any]] = []
    for raw_item in existing:
        item = dict(raw_item)
        if not item.get("images") and not item.get("imageUrl") and not item.get("image_url"):
            b_lap = baseline_by_id.get(item.get("id"))
            if b_lap and (b_lap.get("imageUrl") or b_lap.get("image_url") or b_lap.get("images")):
                bu = b_lap.get("imageUrl") or b_lap.get("image_url") or (b_lap.get("images") or [None])[0]
                if bu:
                    item["imageUrl"] = bu
                    item["images"] = [bu]
        prov = ["HardwareWizard Katalog"]
        extra: dict[str, str] = dict(item.get("extra_specs") or {})
        cpu_model = item.get("cpuModel")
        if cpu_model:
            m_cpu = _match_lookup(nb_cpu, cpu_model)
            if m_cpu:
                prov.append("Notebookcheck")
                extra.update(m_cpu["extra_specs"])

        _apply_curated_gallery(item, "LAPTOP", gallery, health_map, image_key="imageUrl", prefer_gallery=False, prices_tr=prices_tr)
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)

    _quarantine_with_web_rescue(state, out, "LAPTOP", health_map, image_key="imageUrl", max_allowed_variants=15)
    return out


def _sanitize_and_save_prices_tr(
    health_map: dict[str, bool],
    catalog_image_by_id: dict[str, str | None],
) -> None:
    src_path = os.path.join(FILES_DIR, "prices_tr.json")
    pt = _load_json(src_path, {})
    cleaned = 0
    replaced = 0
    for item_id, entry in pt.items():
        if not isinstance(entry, dict):
            continue
        cur_u = entry.get("imageUrl")
        if item_id in catalog_image_by_id:
            target_u = catalog_image_by_id[item_id]
            if cur_u != target_u:
                entry["imageUrl"] = target_u
                if target_u is None:
                    cleaned += 1
                else:
                    replaced += 1
            continue
        cat = item_id.split("_", 1)[0].split("-", 1)[0].upper()
        name_guess = item_id.replace("_", " ").replace("-", " ")
        is_bad = (
            not isinstance(cur_u, str)
            or not health_map.get(cur_u, False)
            or not is_image_compatible_with_item(cat, "", name_guess, "", cur_u)
        )
        if is_bad and cur_u:
            entry["imageUrl"] = None
            cleaned += 1

    for folder in (FILES_DIR, DATA_CATALOG_DIR, DATA_DIR):
        os.makedirs(folder, exist_ok=True)
        with open(os.path.join(folder, "prices_tr.json"), "w", encoding="utf-8") as f:
            json.dump(pt, f, ensure_ascii=False, indent=2)
    print(f"  [OK] prices_tr.json sanitized: replaced={replaced}, cleared_bad={cleaned}")


def run_merge_all() -> None:
    state = CrawlStateManager()
    http = PoliteHttpClient()

    # Load full cached URL health map (fast local SQLite read) and check any uncached catalog/source URLs
    health_map = state.load_image_health_map()
    all_urls: list[str] = []
    for (imgs_j,) in state.conn.execute("SELECT images_json FROM raw_records WHERE images_json != '[]'"):
        all_urls.extend(json.loads(imgs_j))
    all_urls.extend(collect_repo_asset_urls())
    for fn in ("cpu.json", "video-card.json", "motherboard.json", "memory.json", "internal-hard-drive.json", "power-supply.json", "cpu-cooler.json", "case.json", "laptop.json", "peripherals.json", "smartphone.json", "tablet.json"):
        for row in _load_json(os.path.join(FILES_DIR, fn), []) + _load_baseline_29c4a2e(fn):
            for k in ("imageUrl", "image_url"):
                if row.get(k):
                    all_urls.append(row[k])
            all_urls.extend(row.get("images") or [])
    pcp_galleries = _load_pcp_jsonl_galleries()
    pcp_fn_map = {
        "CPU": "cpu.json",
        "GPU": "video-card.json",
        "MOTHERBOARD": "motherboard.json",
        "RAM": "memory.json",
        "STORAGE": "internal-hard-drive.json",
        "PSU": "power-supply.json",
        "COOLER": "cpu-cooler.json",
        "CASE": "case.json",
    }
    for pcat, pfn in pcp_fn_map.items():
        cat_items = _load_json(os.path.join(FILES_DIR, pfn), []) + _load_baseline_29c4a2e(pfn)
        cat_cks = {normalize_canonical_key(it.get("name") or "") for it in cat_items if it.get("name")}
        cat_bks = {normalize_for_brand_match(it.get("name") or "") for it in cat_items if it.get("name")}
        cat_tok_sets = [
            set(re.findall(r"[a-z0-9]{2,}", _clean_match_string(it.get("name") or "")))
            for it in cat_items if it.get("name")
        ]
        tok_freq_index: dict[str, list[int]] = collections.defaultdict(list)
        for idx_t, ts in enumerate(cat_tok_sets):
            for tk in ts:
                if any(ch.isdigit() for ch in tk) or len(tk) >= 4:
                    tok_freq_index[tk].append(idx_t)
        for rec in pcp_galleries.get(pcat, []):
            if rec["ckey"] in cat_cks or rec["bkey"] in cat_bks:
                all_urls.extend(rec["images"])
                continue
            cand_counts: collections.Counter[int] = collections.Counter()
            for tk in rec["tokens"]:
                if tk in tok_freq_index:
                    for idx_t in tok_freq_index[tk]:
                        cand_counts[idx_t] += 1
            if cand_counts and cand_counts.most_common(1)[0][1] >= 2:
                all_urls.extend(rec["images"])

    missing_probe = [u for u in set(all_urls) if isinstance(u, str) and u.startswith("https://") and u not in health_map]
    if missing_probe:
        health_map.update(verify_image_url_pool(state, missing_probe, max_workers=16))

    benchmarks = _load_json(os.path.join(FILES_DIR, "hardware_benchmarks.json"), {"cpus": {}, "gpus": {}})
    prices_tr = _load_json(os.path.join(FILES_DIR, "prices_tr.json"), {})
    # Re-seed HTTP-200 verified legacy prices_tr studio URLs from git 29c4a2e and HEAD so valid unique/series photos aren't lost
    for rev in ("HEAD", "29c4a2e"):
        try:
            rev_raw = subprocess.check_output(
                ["git", "show", f"{rev}:data/prices_tr.json"],
                cwd=ROOT_DIR,
                stderr=subprocess.DEVNULL,
            ).decode("utf-8")
            rev_pt = json.loads(rev_raw)
            extra_urls = [
                v.get("imageUrl")
                for v in rev_pt.values()
                if isinstance(v, dict) and isinstance(v.get("imageUrl"), str) and v["imageUrl"].startswith("https://")
                and v["imageUrl"] not in health_map
            ]
            if extra_urls:
                health_map.update(verify_image_url_pool(state, extra_urls, max_workers=16))
            for item_id, hv in rev_pt.items():
                if not isinstance(hv, dict):
                    continue
                hu = hv.get("imageUrl")
                if isinstance(hu, str) and health_map.get(hu, False):
                    cur_entry = prices_tr.get(item_id)
                    if cur_entry is None and rev == "29c4a2e":
                        prices_tr[item_id] = dict(hv)
                    elif isinstance(cur_entry, dict) and not cur_entry.get("imageUrl"):
                        cur_entry["imageUrl"] = hu
        except Exception:
            pass

    print("  [1/4] Merging & matching studio galleries across all categories...", flush=True)
    cpus = _merge_cpus(state, benchmarks, health_map, prices_tr=prices_tr, pcp_gallery=pcp_galleries.get("CPU"))
    gpus = _merge_gpus(state, benchmarks, health_map, prices_tr=prices_tr, pcp_gallery=pcp_galleries.get("GPU"))
    storages = _merge_storage(state, health_map, prices_tr=prices_tr, pcp_gallery=pcp_galleries.get("STORAGE"))
    psus = _merge_psu(state, health_map, prices_tr=prices_tr, pcp_gallery=pcp_galleries.get("PSU"))
    motherboards = _merge_generic_pcp_category(state, "motherboard.json", "MOTHERBOARD", health_map, prices_tr=prices_tr, pcp_gallery=pcp_galleries.get("MOTHERBOARD"))
    rams = _merge_generic_pcp_category(state, "memory.json", "RAM", health_map, prices_tr=prices_tr, pcp_gallery=pcp_galleries.get("RAM"))
    coolers = _merge_generic_pcp_category(state, "cpu-cooler.json", "COOLER", health_map, prices_tr=prices_tr, pcp_gallery=pcp_galleries.get("COOLER"))
    cases = _merge_generic_pcp_category(state, "case.json", "CASE", health_map, prices_tr=prices_tr, pcp_gallery=pcp_galleries.get("CASE"))
    peripherals = _merge_peripherals(state, health_map, prices_tr=prices_tr)
    smartphones = _merge_mobile(state, "smartphone.json", "SMARTPHONE", health_map, prices_tr=prices_tr)
    tablets = _merge_mobile(state, "tablet.json", "TABLET", health_map, prices_tr=prices_tr)
    laptops = _merge_laptops(state, health_map, prices_tr=prices_tr)

    print("  [2/4] Backfilling reference hardware photos cleanly (cached + high-precision Wikimedia)...", flush=True)
    backfill_missing_via_wikimedia(state, tablets, "TABLET", http, health_map, max_queries=40)
    backfill_missing_via_wikimedia(state, smartphones, "SMARTPHONE", http, health_map, max_queries=45)
    backfill_missing_via_wikimedia(state, cpus, "CPU", http, health_map, max_queries=55)
    print("  [3/4] Sanitizing prices_tr.json & exporting unified JSON catalogs...", flush=True)

    # Build authoritative catalog image map by item_id (explicitly recording None for cleared items so prices_tr never resurrects stale URLs)
    catalog_image_by_id: dict[str, str | None] = {}
    for cat, lst in (("CPU", cpus), ("GPU", gpus), ("STORAGE", storages), ("PSU", psus), ("MOTHERBOARD", motherboards), ("RAM", rams), ("COOLER", coolers), ("CASE", cases)):
        for it in lst:
            catalog_image_by_id[_hid(cat, it.get("name", ""))] = it.get("imageUrl")
    for it in laptops + smartphones + tablets + peripherals:
        if it.get("id"):
            catalog_image_by_id[it["id"]] = it.get("imageUrl") or it.get("image_url")

    _sanitize_and_save_prices_tr(health_map, catalog_image_by_id)

    _save_json_both("cpu.json", cpus)
    _save_json_both("video-card.json", gpus)
    _save_json_both("gpu.json", gpus)
    _save_json_both("internal-hard-drive.json", storages)
    _save_json_both("storage.json", storages)
    _save_json_both("power-supply.json", psus)
    _save_json_both("psu.json", psus)
    _save_json_both("motherboard.json", motherboards)
    _save_json_both("memory.json", rams)
    _save_json_both("ram.json", rams)
    _save_json_both("cpu-cooler.json", coolers)
    _save_json_both("cooler.json", coolers)
    _save_json_both("case.json", cases)
    _save_json_both("peripherals.json", peripherals)
    _save_json_both("smartphone.json", smartphones)
    _save_json_both("tablet.json", tablets)
    _save_json_both("laptop.json", laptops)
    _save_json_both("hardware_benchmarks.json", benchmarks)

    manifest = {
        "catalogVersion": "4.5.0",
        "verifiedAt": VERIFIED_DATE,
        "counts": {
            "cpu": len(cpus),
            "gpu": len(gpus),
            "storage": len(storages),
            "psu": len(psus),
            "motherboard": len(motherboards),
            "ram": len(rams),
            "cooler": len(coolers),
            "case": len(cases),
            "peripherals": len(peripherals),
            "smartphone": len(smartphones),
            "tablet": len(tablets),
            "laptop": len(laptops),
        },
    }
    _save_json_both("catalog_manifest.json", manifest)
    state.close()

    print("=== Merged Catalog Summary (Cleaned & Curated) ===")
    for k, v in manifest["counts"].items():
        print(f"  {k:15s}: {v}")


if __name__ == "__main__":
    run_merge_all()
