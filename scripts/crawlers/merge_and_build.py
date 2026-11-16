#!/usr/bin/env python3
"""Cross-source catalog merger building enriched catalog JSONs without fabricated specs."""

from __future__ import annotations

import collections
import json
import os
import re
import sys
from datetime import datetime, timezone
from typing import Any

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)

from scripts.crawlers.state_manager import (
    CrawlStateManager,
    normalize_canonical_key,
    normalize_for_brand_match,
)

FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")
DATA_CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
VERIFIED_DATE = datetime.now(timezone.utc).strftime("%Y-%m-%d")

# Maximum distinct items allowed to share the same non-manufacturer image URL
MAX_SHARED_IMAGE_REPEAT = 4


def _load_json(path: str, default: Any) -> Any:
    if not os.path.exists(path):
        return default
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def _save_json_both(filename: str, data: Any) -> None:
    for folder in (FILES_DIR, DATA_CATALOG_DIR):
        os.makedirs(folder, exist_ok=True)
        out_path = os.path.join(folder, filename)
        with open(out_path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)


def _index_raw_by_source(state: CrawlStateManager, category: str) -> dict[str, dict[str, dict[str, Any]]]:
    """Returns {source_name: {canonical_key: record_dict}} and also a normalized alias index."""
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
    bad = ("A4-", "A6-", "A8-", "A10-", "A12-", "ATHLON II", "PHENOM", "SEMPRON", "OPTERON", "FX-", "PENTIUM", "CELERON", "CORE 2", "XEON", "ATOM", "AM1", "AM2", "AM3", "FM1", "FM2", "775", "1156", "1155", "1150", "1151")
    if any(b in u for b in bad):
        return False
    is_amd = any(k in u for k in ("RYZEN", "THREADRIPPER", "3000G", "200GE")) and any(
        s in u for s in ("AM4", "AM5", "TR4", "STRX4", "STR5", "SWRX8", "RYZEN")
    )
    is_intel = any(
        k in u
        for k in (
            "CORE ULTRA", "285K", "275K", "265K", "255K", "245K",
            "14900", "14700", "14600", "14500", "14400", "14100",
            "13900", "13700", "13600", "13500", "13400", "13100",
            "12900", "12700", "12600", "12500", "12400", "12100",
            "11900", "11700", "11600", "11500", "11400",
            "10900", "10700", "10600", "10500", "10400", "10100",
        )
    )
    return is_amd or is_intel


def _merge_cpus(state: CrawlStateManager, benchmarks: dict[str, Any]) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "cpu.json"), [])
    by_name: dict[str, dict[str, Any]] = {}
    for item in existing:
        if item.get("name"):
            by_name[normalize_canonical_key(item["name"])] = dict(item)

    raw_src = _index_raw_by_source(state, "CPU")
    pcp = raw_src.get("pc-part-dataset", {})
    tpu = raw_src.get("TechPowerUp Ajax", {})
    pm = raw_src.get("PassMark", {})

    # Expand catalog with modern CPUs from pc-part-dataset and TechPowerUp
    for rec in list(pcp.values()) + list(tpu.values()):
        name = rec["display_name"]
        p = rec["payload"]
        sock = str(p.get("socket") or "")
        if not _is_modern_cpu_name(name, sock):
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
        prov: list[str] = ["HardwareWizard Katalog"] if item in existing else []
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
                    # Relative 10-99 index anchored around PassMark 65,000 flagship
                    rel = round(min(0.99, max(0.25, pm_score / 62000.0)), 3)
                    b_rec["gaming_relative_index"] = rel
                    b_rec["score"] = int(rel * 100)

        if item.get("core_count") and not item.get("threads"):
            item["threads"] = item["core_count"]

        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov)) or ["Katalog Verisi"]
        item["spec_verified_at"] = VERIFIED_DATE
        enriched_list.append(item)

    enriched_list.sort(key=lambda x: (x.get("release_date") or "", x.get("name") or ""), reverse=True)
    return enriched_list[:450]


def _merge_gpus(state: CrawlStateManager, benchmarks: dict[str, Any]) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "video-card.json"), [])
    raw_src = _index_raw_by_source(state, "GPU")
    tpu = raw_src.get("TechPowerUp Ajax", {})
    pm = raw_src.get("PassMark", {})
    pcp = raw_src.get("pc-part-dataset", {})

    by_name: dict[str, dict[str, Any]] = {}
    for item in existing:
        if item.get("name"):
            by_name[normalize_canonical_key(item["name"])] = dict(item)

    # Add 2025/2026 new RTX 50 / RX 9000 / Arc B cards discovered in TechPowerUp or PCP
    for rec in list(tpu.values()) + list(pcp.values()):
        name = rec["display_name"]
        chip = str(rec["payload"].get("chipset") or name)
        up = f"{name} {chip}".upper()
        if not any(k in up for k in ("RTX 50", "RX 90", "ARC B5", "RTX 40", "RX 7")):
            continue
        ckey = normalize_canonical_key(name)
        if ckey not in by_name:
            by_name[ckey] = {
                "name": name,
                "chipset": chip,
                "memory": rec["payload"].get("memory"),
                "memory_type": rec["payload"].get("memory_type"),
                "core_clock": rec["payload"].get("core_clock"),
                "boost_clock": rec["payload"].get("boost_clock"),
                "length": rec["payload"].get("length"),
                "images": [],
            }

    gpu_bench_map = benchmarks.setdefault("gpus", {})
    result: list[dict[str, Any]] = []
    for item in by_name.values():
        name = item["name"]
        chip = item.get("chipset") or name
        prov: list[str] = ["HardwareWizard Katalog"] if item in existing else []
        extra: dict[str, str] = dict(item.get("extra_specs") or {})

        m_pcp = _match_lookup(pcp, name, chip)
        if m_pcp:
            prov.append("pc-part-dataset")
            if not item.get("length") and m_pcp["payload"].get("length"):
                item["length"] = m_pcp["payload"]["length"]

        m_tpu = _match_lookup(tpu, chip, name)
        if m_tpu:
            prov.append("TechPowerUp DB")
            tp = m_tpu["payload"]
            for fld in ("tmus", "rops", "pcie_interface", "gpu_die", "release_date", "cuda_cores", "bus_width", "memory_type"):
                if not item.get(fld) and tp.get(fld) is not None:
                    item[fld] = tp[fld]
            extra.update(m_tpu["extra_specs"])
            # Compute FP32 TFLOPS from Shaders * Boost/Core Clock * 2 if missing
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

        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov)) or ["Katalog Verisi"]
        item["spec_verified_at"] = VERIFIED_DATE
        result.append(item)

    return result


def _merge_storage(state: CrawlStateManager) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "internal-hard-drive.json"), [])
    raw_src = _index_raw_by_source(state, "STORAGE")
    newmaxx = raw_src.get("NewMaxx SSD Sheet", {})
    tpu = raw_src.get("TechPowerUp Ajax", {})
    pcp = raw_src.get("pc-part-dataset", {})

    by_key = {normalize_for_brand_match(it["name"]): dict(it) for it in existing if it.get("name")}

    # Add notable NVMe drives from NewMaxx / TPU
    for rec in list(newmaxx.values())[:220] + list(tpu.values())[:100]:
        name = rec["display_name"]
        bkey = normalize_for_brand_match(name)
        p = rec["payload"]
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
                "images": [],
            }

    out: list[dict[str, Any]] = []
    for item in by_key.values():
        name = item["name"]
        prov: list[str] = ["HardwareWizard Katalog"] if item in existing else []
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

        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov)) or ["Katalog Verisi"]
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)
    return out


def _merge_psu(state: CrawlStateManager) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "power-supply.json"), [])
    raw_src = _index_raw_by_source(state, "PSU")
    iron = raw_src.get("IRONScale Labs", {})
    pcp = raw_src.get("pc-part-dataset", {})

    out: list[dict[str, Any]] = []
    for raw_item in existing:
        item = dict(raw_item)
        name = item.get("name", "")
        prov = ["IRONScale Labs"]
        extra: dict[str, str] = dict(item.get("extra_specs") or {})
        m_iron = _match_lookup(iron, name)
        if m_iron:
            extra.update(m_iron["extra_specs"])
            if not item.get("images") and m_iron["images"]:
                item["images"] = m_iron["images"]
                item["imageUrl"] = m_iron["images"][0]
        m_pcp = _match_lookup(pcp, name)
        if m_pcp:
            prov.append("pc-part-dataset")
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)
    return out


def _merge_generic_pcp_category(
    state: CrawlStateManager,
    filename: str,
    cat_key: str,
    dedup_image_cleanup: bool = False,
) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, filename), [])
    raw_src = _index_raw_by_source(state, cat_key)
    pcp = raw_src.get("pc-part-dataset", {})

    url_counts = collections.Counter(it.get("imageUrl") or it.get("image_url") for it in existing if (it.get("imageUrl") or it.get("image_url")))
    out: list[dict[str, Any]] = []
    for raw_item in existing:
        item = dict(raw_item)
        name = item.get("name", "")
        prov = ["HardwareWizard Katalog"]
        extra: dict[str, str] = dict(item.get("extra_specs") or {})
        m = _match_lookup(pcp, name)
        if m:
            prov.append("pc-part-dataset")
            extra.update(m["extra_specs"])

        if dedup_image_cleanup:
            img = item.get("imageUrl") or item.get("image_url")
            if img and url_counts[img] > MAX_SHARED_IMAGE_REPEAT:
                item.pop("imageUrl", None)
                item.pop("image_url", None)
                item["images"] = []

        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)
    return out


def _merge_peripherals(state: CrawlStateManager) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "peripherals.json"), [])
    url_counts_by_cat: dict[str, collections.Counter[str]] = collections.defaultdict(collections.Counter)
    for it in existing:
        img = it.get("image_url") or (it.get("images") or [None])[0]
        if img:
            url_counts_by_cat[it.get("category", "")][img] += 1

    # Load all non-PC raw sources
    sensor_fyi = _index_raw_by_source(state, "MOUSE").get("Sensor.fyi", {})
    autoeq = _index_raw_by_source(state, "HEADSET").get("AutoEq Acoustic DB", {})
    openwrt = _index_raw_by_source(state, "ROUTER").get("OpenWrt Releases", {})
    nut_ups = _index_raw_by_source(state, "UPS").get("NUT UPS DDL", {})
    pcp_ups = _index_raw_by_source(state, "UPS").get("pc-part-dataset", {})

    shopify_by_cat: dict[str, dict[str, dict[str, Any]]] = {}
    for cat in ("MOUSE", "KEYBOARD", "MOUSEPAD", "GAMING_CHAIR", "GAMING_DESK"):
        cat_srcs = _index_raw_by_source(state, cat)
        merged_shop: dict[str, dict[str, Any]] = {}
        for sname, smap in cat_srcs.items():
            if "Official" in sname:
                merged_shop.update(smap)
        shopify_by_cat[cat] = merged_shop

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

        # Clean over-shared placeholder thumbnails
        cur_img = item.get("image_url")
        if cur_img and url_counts_by_cat[cat][cur_img] > MAX_SHARED_IMAGE_REPEAT:
            item["image_url"] = None
            item["images"] = []

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

        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        by_id[item["id"]] = item

    # Add top newly crawled authentic OpenWrt routers, NUT/PCP UPS, & Shopify official items
    _append_new_peripherals(by_id, "MOUSE", shopify_by_cat.get("MOUSE", {}), "Üretici Resmi Katalog", limit=45)
    _append_new_peripherals(by_id, "KEYBOARD", shopify_by_cat.get("KEYBOARD", {}), "Üretici Resmi Katalog", limit=55)
    _append_new_peripherals(by_id, "MOUSEPAD", shopify_by_cat.get("MOUSEPAD", {}), "Üretici Resmi Katalog", limit=40)
    _append_new_peripherals(by_id, "GAMING_CHAIR", shopify_by_cat.get("GAMING_CHAIR", {}), "Secretlab Resmi", limit=25)
    _append_new_peripherals(by_id, "GAMING_DESK", shopify_by_cat.get("GAMING_DESK", {}), "Secretlab Resmi", limit=15)
    _append_new_peripherals(by_id, "ROUTER", openwrt, "OpenWrt Releases", limit=60)
    _append_new_peripherals(by_id, "UPS", pcp_ups, "pc-part-dataset", limit=45)

    return list(by_id.values())


def _append_new_peripherals(
    by_id: dict[str, dict[str, Any]],
    category: str,
    src_map: dict[str, dict[str, Any]],
    source_label: str,
    limit: int,
) -> None:
    existing_keys = {normalize_canonical_key(v.get("name", "")) for v in by_id.values() if v.get("category") == category}
    added = 0
    seen_recs: set[str] = set()
    for rec in src_map.values():
        if added >= limit:
            break
        name = rec["display_name"]
        ckey = normalize_canonical_key(name)
        if not ckey or ckey in existing_keys or ckey in seen_recs:
            continue
        seen_recs.add(ckey)
        new_id = f"{category.lower()}_{ckey}"
        imgs = rec.get("images") or []
        entry: dict[str, Any] = {
            "id": new_id,
            "category": category,
            "name": name,
            "brand": rec.get("brand") or name.split()[0],
            "model": name,
            "image_url": imgs[0] if imgs else None,
            "images": imgs,
            "extra_specs": rec.get("extra_specs") or {},
            "source_provenance": [source_label],
            "spec_verified_at": VERIFIED_DATE,
        }
        entry.update({k: v for k, v in (rec.get("payload") or {}).items() if v is not None})
        by_id[new_id] = entry
        added += 1


def _merge_mobile(state: CrawlStateManager, filename: str, category: str) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, filename), [])
    raw_src = _index_raw_by_source(state, category)
    dxo = raw_src.get("DXOMARK", {})
    gsm = raw_src.get("GSMArena", {})
    nb_cpu = _index_raw_by_source(state, "CPU_MOBILE_REF").get("Notebookcheck", {})

    out: list[dict[str, Any]] = []
    for raw_item in existing:
        item = dict(raw_item)
        spec = dict(item.get("spec") or {})
        name = item.get("name") or f"{item.get('brand', '')} {item.get('model', '')}".strip()
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
            if m_dxo["images"]:
                cur_imgs = list(item.get("images") or [])
                for im in m_dxo["images"]:
                    if im not in cur_imgs:
                        cur_imgs.append(im)
                item["images"] = cur_imgs[:5]
                if not item.get("imageUrl"):
                    item["imageUrl"] = cur_imgs[0]

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

        item["spec"] = spec
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)
    return out


def _merge_laptops(state: CrawlStateManager) -> list[dict[str, Any]]:
    existing = _load_json(os.path.join(FILES_DIR, "laptop.json"), [])
    nb_cpu = _index_raw_by_source(state, "CPU_MOBILE_REF").get("Notebookcheck", {})
    out: list[dict[str, Any]] = []
    for raw_item in existing:
        item = dict(raw_item)
        prov = ["HardwareWizard Katalog"]
        extra: dict[str, str] = dict(item.get("extra_specs") or {})
        cpu_model = item.get("cpuModel")
        if cpu_model:
            m_cpu = _match_lookup(nb_cpu, cpu_model)
            if m_cpu:
                prov.append("Notebookcheck")
                extra.update(m_cpu["extra_specs"])
        item["extra_specs"] = extra
        item["source_provenance"] = list(dict.fromkeys(prov))
        item["spec_verified_at"] = VERIFIED_DATE
        out.append(item)
    return out


def run_merge_all() -> None:
    state = CrawlStateManager()
    benchmarks = _load_json(os.path.join(FILES_DIR, "hardware_benchmarks.json"), {"cpus": {}, "gpus": {}})

    cpus = _merge_cpus(state, benchmarks)
    gpus = _merge_gpus(state, benchmarks)
    storages = _merge_storage(state)
    psus = _merge_psu(state)
    motherboards = _merge_generic_pcp_category(state, "motherboard.json", "MOTHERBOARD", dedup_image_cleanup=False)
    rams = _merge_generic_pcp_category(state, "memory.json", "RAM", dedup_image_cleanup=True)
    coolers = _merge_generic_pcp_category(state, "cpu-cooler.json", "COOLER", dedup_image_cleanup=False)
    cases = _merge_generic_pcp_category(state, "case.json", "CASE", dedup_image_cleanup=False)
    peripherals = _merge_peripherals(state)
    smartphones = _merge_mobile(state, "smartphone.json", "SMARTPHONE")
    tablets = _merge_mobile(state, "tablet.json", "TABLET")
    laptops = _merge_laptops(state)

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
        "catalogVersion": "4.0.0",
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

    print("=== Merged Catalog Summary ===")
    for k, v in manifest["counts"].items():
        print(f"  {k:15s}: {v}")


if __name__ == "__main__":
    run_merge_all()
