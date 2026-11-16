"""Checkpointed multi-source crawler for all 9 core PC hardware categories."""

from __future__ import annotations

import csv
import io
import json
import re
from typing import Any

from .http_client import BlockedAntiBotError, PoliteHttpClient
from .state_manager import (
    CrawlStateManager,
    normalize_canonical_key,
    normalize_for_brand_match,
)

PCP_PC_URLS = {
    "CPU": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/cpu.json",
    "GPU": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/video-card.json",
    "MOTHERBOARD": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/motherboard.json",
    "RAM": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/memory.json",
    "STORAGE": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/internal-hard-drive.json",
    "PSU": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/power-supply.json",
    "COOLER": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/cpu-cooler.json",
    "CASE": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/case.json",
    "CASE_FAN": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/case-fan.json",
}

GEEKBENCH_CPU_URL = "https://browser.geekbench.com/processor-benchmarks.json"
PASSMARK_CPU_URL = "https://www.cpubenchmark.net/cpu_list.php"
PASSMARK_GPU_URL = "https://www.videocardbenchmark.net/gpu_list.php"
NEWMAXX_SSD_CSV = "https://docs.google.com/spreadsheets/d/1B27_j9NDPU3cNlj2HKcrfpJKHkOf-Oi1DbuuQva2gT4/gviz/tq?tqx=out:csv"
IRONSCALE_PSU_URL = "https://ironscalelabs.com/data/psu-data.js?v=204"
SPL_PSU_CSV = "https://docs.google.com/spreadsheets/d/1akCHL7Vhzk_EhrpIGkz8zTEvYfLDcaSpZRB6Xt6JWkc/gviz/tq?tqx=out:csv&sheet=Main%20List"
CULTISTS_PSU_CSV = "https://docs.google.com/spreadsheets/d/1eL0893Ramlwk6E3s3uSvH1_juom7SMG5SCNzP2Uov8w/gviz/tq?tqx=out:csv&gid=1529225916"


def crawl_pc_components(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    tpu_years: list[int] | None = None,
    delta_only: bool = False,
) -> dict[str, int]:
    years = tpu_years or ([2025, 2026] if delta_only else [2020, 2021, 2022, 2023, 2024, 2025, 2026])
    counts: dict[str, int] = {}
    counts["pcp_pc"] = _crawl_pcp_pc(http, state)
    counts["geekbench_cpu"] = _crawl_geekbench_cpu(http, state)
    counts["passmark_cpu"] = _crawl_passmark_cpu(http, state, delta_only=delta_only)
    counts["passmark_gpu"] = _crawl_passmark_gpu(http, state, delta_only=delta_only)
    counts["newmaxx_ssd"] = _crawl_newmaxx_ssd(http, state)
    counts["ironscale_psu"] = _crawl_ironscale_psu(http, state)
    counts["tpu_ajax"] = _crawl_tpu_ajax_tables(http, state, years=years, delta_only=delta_only)
    return counts


def _strip_tags(html: str) -> str:
    return re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", html)).strip()


def _crawl_pcp_pc(http: PoliteHttpClient, state: CrawlStateManager) -> int:
    total = 0
    for cat, url in PCP_PC_URLS.items():
        task = f"pc:pcp:{cat}"
        prog = state.get_progress(task)
        try:
            data, status, etag, lm = http.get_json(
                url,
                etag=prog["etag"] if prog else None,
                last_modified=prog["last_modified"] if prog else None,
            )
        except Exception as exc:
            state.log_audit(cat, "pc-part-dataset", "ERROR", 0, str(exc))
            continue

        if status == 304 or not data:
            state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
            continue

        rows: list[dict[str, Any]] = []
        for item in data:
            name = (item.get("name") or "").strip()
            if not name:
                continue
            brand = name.split()[0]
            payload, extra = _map_pcp_pc_item(cat, item)
            rows.append(
                {
                    "category": cat,
                    "source_name": "pc-part-dataset",
                    "canonical_key": normalize_canonical_key(name),
                    "display_name": name,
                    "brand": brand,
                    "payload": payload,
                    "extra_specs": extra,
                    "images": [],
                    "source_url": url,
                }
            )
        inserted = state.upsert_raw_batch(rows)
        total += inserted
        state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
        state.log_audit(cat, "pc-part-dataset", "OK", inserted, "GitHub JSON")
    return total


def _map_pcp_pc_item(cat: str, item: dict[str, Any]) -> tuple[dict[str, Any], dict[str, str]]:
    payload: dict[str, Any] = {}
    extra: dict[str, str] = {}
    if cat == "CPU":
        payload = {
            "core_count": item.get("core_count"),
            "core_clock": item.get("core_clock"),
            "boost_clock": item.get("boost_clock"),
            "microarchitecture": item.get("microarchitecture"),
            "tdp": item.get("tdp"),
            "graphics": item.get("graphics"),
        }
    elif cat == "GPU":
        payload = {
            "chipset": item.get("chipset"),
            "memory": item.get("memory"),
            "core_clock": item.get("core_clock"),
            "boost_clock": item.get("boost_clock"),
            "length": item.get("length"),
            "color": item.get("color"),
        }
    elif cat == "MOTHERBOARD":
        payload = {
            "socket": item.get("socket"),
            "form_factor": item.get("form_factor"),
            "max_memory": item.get("max_memory"),
            "memory_slots": item.get("memory_slots"),
            "color": item.get("color"),
        }
    elif cat == "RAM":
        payload = {
            "speed": item.get("speed"),
            "modules": item.get("modules"),
            "cas_latency": item.get("cas_latency"),
            "first_word_latency": item.get("first_word_latency"),
            "color": item.get("color"),
        }
    elif cat == "STORAGE":
        payload = {
            "capacity": item.get("capacity"),
            "type": item.get("type"),
            "cache": item.get("cache"),
            "form_factor": item.get("form_factor"),
            "interface": item.get("interface"),
        }
    elif cat == "PSU":
        payload = {
            "type": item.get("type"),
            "efficiency": item.get("efficiency"),
            "wattage": item.get("wattage"),
            "modular": item.get("modular"),
            "color": item.get("color"),
        }
    elif cat == "COOLER":
        rpm = item.get("rpm")
        rpm_str = f"{rpm[0]}-{rpm[1]}" if isinstance(rpm, list) and len(rpm) >= 2 else (str(rpm) if rpm else None)
        noise = item.get("noise_level")
        noise_val = max(noise) if isinstance(noise, list) and noise else (noise if isinstance(noise, (int, float)) else None)
        payload = {
            "size": item.get("size"),
            "fan_rpm": rpm_str,
            "noise_level": noise_val,
            "color": item.get("color"),
        }
    elif cat == "CASE":
        payload = {
            "type": item.get("type"),
            "color": item.get("color"),
            "side_panel": item.get("side_panel"),
            "external_volume": item.get("external_volume"),
            "drive_bays": f"{item['internal_35_bays']}x 3.5\"" if item.get("internal_35_bays") else None,
        }
        if item.get("external_volume"):
            extra["Kasa İç Hacmi (Litre)"] = f"{item['external_volume']} L"
    elif cat == "CASE_FAN":
        rpm = item.get("rpm")
        min_r = rpm[0] if isinstance(rpm, list) and len(rpm) >= 2 else None
        max_r = rpm[-1] if isinstance(rpm, list) and rpm else (rpm if isinstance(rpm, int) else None)
        air = item.get("airflow")
        max_air = air[-1] if isinstance(air, list) and air else (air if isinstance(air, (int, float)) else None)
        ns = item.get("noise_level")
        max_ns = ns[-1] if isinstance(ns, list) and ns else (ns if isinstance(ns, (int, float)) else None)
        payload = {
            "fan_size_mm": item.get("size"),
            "min_rpm": min_r,
            "max_rpm": max_r,
            "max_airflow_cfm": max_air,
            "noise_level_dba": max_ns,
            "pwm_pins": 4 if item.get("pwm") is True else (3 if item.get("pwm") is False else None),
        }
    return payload, extra


def _crawl_geekbench_cpu(http: PoliteHttpClient, state: CrawlStateManager) -> int:
    task = "pc:geekbench:cpu"
    prog = state.get_progress(task)
    try:
        data, status, etag, lm = http.get_json(
            GEEKBENCH_CPU_URL,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except Exception as exc:
        state.log_audit("CPU", "Geekbench 6", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not data:
        state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
        return 0

    rows: list[dict[str, Any]] = []
    for dev in data.get("devices", []):
        name = (dev.get("name") or "").strip()
        sc = dev.get("score")
        mc = dev.get("multicore_score")
        if not name or not sc or not mc:
            continue
        extra: dict[str, str] = {
            "Geekbench 6 Tek Çekirdek": str(sc),
            "Geekbench 6 Çoklu Çekirdek": str(mc),
        }
        if dev.get("samples"):
            extra["Geekbench Örnek Test Sayısı"] = f"{dev['samples']} doğrulanmış sonuç"
        rows.append(
            {
                "category": "CPU",
                "source_name": "Geekbench 6",
                "canonical_key": normalize_canonical_key(name),
                "display_name": name,
                "brand": name.split()[0],
                "payload": {
                    "geekbench_6_single": int(sc),
                    "geekbench_6_multi": int(mc),
                    "description": dev.get("description"),
                },
                "extra_specs": extra,
                "images": [],
                "source_url": GEEKBENCH_CPU_URL,
            }
        )
    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("CPU", "Geekbench 6", "OK", inserted, "processor-benchmarks.json")
    return inserted


def _crawl_passmark_cpu(http: PoliteHttpClient, state: CrawlStateManager, delta_only: bool) -> int:
    task = "pc:passmark:cpu"
    prog = state.get_progress(task)
    try:
        html, status, etag, lm = http.get_text(
            PASSMARK_CPU_URL,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except BlockedAntiBotError as exc:
        state.log_audit("CPU", "PassMark", "BLOCKED_ANTIBOT", 0, str(exc))
        return 0
    except Exception as exc:
        state.log_audit("CPU", "PassMark", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not html:
        state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
        return 0

    rows: list[dict[str, Any]] = []
    for tr in re.findall(r'<tr[^>]*id="cpu\d+"[^>]*>(.*?)</tr>', html, re.S):
        tds = [_strip_tags(td) for td in re.findall(r"<td[^>]*>(.*?)</td>", tr, re.S)]
        if len(tds) < 3:
            continue
        raw_name = tds[0].split("@")[0].strip()
        score_s = re.sub(r"[^0-9]", "", tds[1])
        rank_s = re.sub(r"[^0-9]", "", tds[2])
        if not raw_name or not score_s:
            continue
        score = int(score_s)
        extra = {
            "PassMark CPU Mark": f"{score:,}".replace(",", "."),
            "PassMark Dünya Sıralaması": f"#{rank_s}" if rank_s else "-",
        }
        rows.append(
            {
                "category": "CPU",
                "source_name": "PassMark",
                "canonical_key": normalize_canonical_key(raw_name),
                "display_name": raw_name,
                "brand": raw_name.split()[0],
                "payload": {"passmark_cpu_mark": score, "rank": int(rank_s) if rank_s else None},
                "extra_specs": extra,
                "images": [],
                "source_url": PASSMARK_CPU_URL,
            }
        )
    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("CPU", "PassMark", "OK", inserted, "cpu_list.php")
    return inserted


def _crawl_passmark_gpu(http: PoliteHttpClient, state: CrawlStateManager, delta_only: bool) -> int:
    task = "pc:passmark:gpu"
    prog = state.get_progress(task)
    try:
        html, status, etag, lm = http.get_text(
            PASSMARK_GPU_URL,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except BlockedAntiBotError as exc:
        state.log_audit("GPU", "PassMark", "BLOCKED_ANTIBOT", 0, str(exc))
        return 0
    except Exception as exc:
        state.log_audit("GPU", "PassMark", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not html:
        state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
        return 0

    rows: list[dict[str, Any]] = []
    for tr in re.findall(r'<tr[^>]*id="gpu\d+"[^>]*>(.*?)</tr>', html, re.S | re.I):
        tds = [_strip_tags(td) for td in re.findall(r"<td[^>]*>(.*?)</td>", tr, re.S | re.I)]
        if len(tds) < 3:
            continue
        raw_name = tds[0].strip()
        score_s = re.sub(r"[^0-9]", "", tds[1])
        rank_s = re.sub(r"[^0-9]", "", tds[2])
        if not raw_name or not score_s:
            continue
        score = int(score_s)
        rows.append(
            {
                "category": "GPU",
                "source_name": "PassMark",
                "canonical_key": normalize_canonical_key(raw_name),
                "display_name": raw_name,
                "brand": raw_name.split()[0],
                "payload": {"passmark_g3d_mark": score, "rank": int(rank_s) if rank_s else None},
                "extra_specs": {
                    "PassMark G3D Mark": f"{score:,}".replace(",", "."),
                    "PassMark GPU Sıralaması": f"#{rank_s}" if rank_s else "-",
                },
                "images": [],
                "source_url": PASSMARK_GPU_URL,
            }
        )
    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("GPU", "PassMark", "OK", inserted, "gpu_list.php")
    return inserted


def _crawl_newmaxx_ssd(http: PoliteHttpClient, state: CrawlStateManager) -> int:
    task = "pc:newmaxx:ssd"
    prog = state.get_progress(task)
    try:
        text, status, etag, lm = http.get_text(
            NEWMAXX_SSD_CSV,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except Exception as exc:
        state.log_audit("STORAGE", "NewMaxx SSD Sheet", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not text:
        state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
        return 0

    reader = csv.reader(io.StringIO(text))
    headers_idx: dict[str, int] = {}
    rows: list[dict[str, Any]] = []
    for row in reader:
        if not row or len(row) < 6:
            continue
        if "Brand" in row and "Model" in row and "Controller" in row:
            headers_idx = {col.strip(): idx for idx, col in enumerate(row)}
            continue
        if not headers_idx:
            continue

        def get_col(col_name: str) -> str:
            idx = headers_idx.get(col_name)
            return row[idx].strip() if idx is not None and idx < len(row) else ""

        brand = get_col("Brand")
        model = get_col("Model")
        if not brand or not model or brand.upper() == "DEPRECATED":
            continue
        full_name = f"{brand} {model}"
        rw = get_col("R/W (Up to, in MB/s)")
        seq_r = seq_w = None
        m_rw = re.match(r"(\d+)\s*/\s*(\d+)", rw)
        if m_rw:
            seq_r, seq_w = int(m_rw.group(1)), int(m_rw.group(2))

        ctrl = get_col("Controller") or None
        dram = get_col("DRAM") or None
        hmb = get_col("HMB") or None
        nand_brand = get_col("NAND Brand") or None
        nand_type = get_col("NAND Type") or None
        cfg = get_col("Configuration") or None
        tier_cat = get_col("Categories") or None

        dram_desc = None
        if dram:
            if dram.lower() in {"yes", "y"}:
                dram_desc = "Dahili DRAM Önbellek"
            elif dram.lower() == "no" and hmb and hmb.lower() not in {"n/a", "no", "none"}:
                dram_desc = f"DRAM-Less (HMB: {hmb})"
            elif dram.lower() == "no":
                dram_desc = "DRAM-Less"
            else:
                dram_desc = dram

        nand_full = " ".join(x for x in (nand_brand, nand_type) if x) or None
        extra: dict[str, str] = {}
        if cfg:
            extra["Kontrolcü Kanal / Çekirdek Yapısı"] = cfg
        if hmb and hmb.lower() not in {"n/a", "no"}:
            extra["HMB (Host Memory Buffer)"] = hmb
        if tier_cat:
            extra["NewMaxx Performans Sınıfı"] = tier_cat
        if get_col("Notes (*)"):
            extra["Mühendislik Notu"] = get_col("Notes (*)")[:160]

        rows.append(
            {
                "category": "STORAGE",
                "source_name": "NewMaxx SSD Sheet",
                "canonical_key": normalize_for_brand_match(full_name),
                "display_name": full_name,
                "brand": brand,
                "payload": {
                    "interface": get_col("Interface") or None,
                    "form_factor": get_col("Form Factor") or None,
                    "controller": ctrl,
                    "dram_cache": dram_desc,
                    "nand_type": nand_full,
                    "seq_read": seq_r,
                    "seq_write": seq_w,
                    "capacities": get_col("Capacities") or None,
                },
                "extra_specs": extra,
                "images": [],
                "source_url": NEWMAXX_SSD_CSV,
            }
        )
    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("STORAGE", "NewMaxx SSD Sheet", "OK", inserted, "SSD Engineering CSV")
    return inserted


def _crawl_ironscale_psu(http: PoliteHttpClient, state: CrawlStateManager) -> int:
    task = "pc:ironscale:psu"
    prog = state.get_progress(task)
    try:
        raw, status, etag, lm = http.get_text(
            IRONSCALE_PSU_URL,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except Exception as exc:
        state.log_audit("PSU", "IRONScale Labs", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not raw or "IRON_PSU_DB" not in raw:
        state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
        return 0

    after_eq = raw.split("=", 1)[1].lstrip()
    try:
        data_obj, _ = json.JSONDecoder().raw_decode(after_eq)
    except Exception as exc:
        state.log_audit("PSU", "IRONScale Labs", "PARSE_ERROR", 0, str(exc))
        return 0
    records = data_obj.get("records", [])
    rows: list[dict[str, Any]] = []
    for rec in records:
        if not isinstance(rec, dict):
            continue
        name = str(rec.get("display_name") or rec.get("series") or rec.get("model") or "").strip()
        brand = str(rec.get("brand") or "").strip()
        if not name:
            continue
        full_name = name if not brand or name.lower().startswith(brand.lower()) else f"{brand} {name}"
        perf = rec.get("performance") or {}
        prim = rec.get("primary") or {}
        sec = rec.get("secondary") or {}
        dims = rec.get("dimensions") or {}
        imgs = [u for u in (rec.get("images") or []) if isinstance(u, str) and u.startswith("http")]

        extra: dict[str, str] = {}
        for val, label in [
            (rec.get("oem"), "OEM Üretici"),
            (rec.get("platform"), "İç Devre Platformu"),
            (rec.get("tier"), "IRONScale / SPL Tier"),
            (rec.get("atx_version"), "ATX Standardı"),
            (prim.get("capacitors") if isinstance(prim, dict) else None, "Birincil (Primary) Kapasitörler"),
            (sec.get("capacitors") if isinstance(sec, dict) else None, "İkincil (Secondary) Kapasitörler"),
            (perf.get("hold_up_time") if isinstance(perf, dict) else None, "Hold-Up Süresi"),
            (dims.get("length_mm") if isinstance(dims, dict) else None, "PSU Uzunluğu (mm)"),
        ]:
            if val is not None and str(val).strip():
                extra[label] = str(val).strip()

        watt_opts = rec.get("watt_options") or [None]
        for w_opt in watt_opts:
            w_str = str(w_opt or "").strip()
            variant_name = f"{full_name} {w_str}".strip() if w_str and w_str.lower() not in full_name.lower() else full_name
            rows.append(
                {
                    "category": "PSU",
                    "source_name": "IRONScale Labs",
                    "canonical_key": normalize_canonical_key(variant_name),
                    "display_name": variant_name,
                    "brand": brand or full_name.split()[0],
                    "payload": {
                        "watt_str": w_str,
                        "tier": rec.get("tier"),
                        "efficiency": rec.get("efficiency"),
                        "modular": rec.get("modular"),
                        "oem": rec.get("oem"),
                        "platform": rec.get("platform"),
                        "atx_version": rec.get("atx_version"),
                        "form_factor": rec.get("form_factor"),
                        "protections": rec.get("protections"),
                        "fan": rec.get("fan"),
                        "cables": rec.get("cables"),
                    },
                    "extra_specs": extra,
                    "images": imgs[:4],
                    "source_url": rec.get("official_url") or IRONSCALE_PSU_URL,
                }
            )
    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("PSU", "IRONScale Labs", "OK", inserted, "psu-data.js")
    return inserted


def _crawl_tpu_ajax_tables(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    years: list[int],
    delta_only: bool,
) -> int:
    total = 0
    # CPU (?mfgr=AMD|Intel&year=YYYY&ajax=1), GPU (?mfgr=NVIDIA|AMD|Intel&year=YYYY&ajax=1), SSD (?year=YYYY&ajax=1)
    queries: list[tuple[str, str]] = []
    for yr in years:
        for mfgr in ("AMD", "Intel"):
            queries.append(("CPU", f"https://www.techpowerup.com/cpu-specs/?mfgr={mfgr}&year={yr}&ajax=1"))
        for mfgr in ("NVIDIA", "AMD", "Intel"):
            queries.append(("GPU", f"https://www.techpowerup.com/gpu-specs/?mfgr={mfgr}&year={yr}&ajax=1"))
        queries.append(("SSD", f"https://www.techpowerup.com/ssd-specs/?year={yr}&ajax=1"))

    for cat, url in queries:
        task = f"pc:tpu_ajax:{cat}:{url}"
        prog = state.get_progress(task)
        if prog and prog["status"] == "DONE" and "year=2025" not in url and "year=2026" not in url and delta_only:
            continue
        if not delta_only and prog and prog["status"] == "DONE":
            continue

        try:
            data, _, _, _ = http.get_json(url)
        except BlockedAntiBotError as exc:
            state.log_audit(cat, "TechPowerUp Ajax", "BLOCKED_ANTIBOT", total, str(exc))
            return total
        except Exception as exc:
            state.save_progress(task, "ERROR", note=str(exc))
            continue

        list_html = (data or {}).get("list") or ""
        parsed_rows = _parse_tpu_rows(cat, list_html, url)
        inserted = state.upsert_raw_batch(parsed_rows)
        total += inserted
        state.save_progress(task, "DONE", cursor=str(inserted))

    state.log_audit("CPU", "TechPowerUp Ajax", "OK", total, f"Years {years}")
    return total


def _parse_tpu_rows(cat: str, list_html: str, source_url: str) -> list[dict[str, Any]]:
    rows: list[dict[str, Any]] = []
    for tr in re.findall(r"<tr[^>]*>(.*?)</tr>", list_html, re.S):
        raw_tds = re.findall(r"<td[^>]*>(.*?)</td>", tr, re.S)
        tds = [_strip_tags(td) for td in raw_tds]
        if not tds:
            continue
        if cat == "CPU" and len(tds) >= 9:
            name, codename, cores_str, clock_str, socket_s, proc_s, l3_s, tdp_s, rel_s = tds[:9]
            name_m = re.search(r"<a[^>]*>(.*?)</a>", raw_tds[0], re.S)
            if name_m:
                name = _strip_tags(name_m.group(1)) or name
            if not name:
                continue
            brand = "AMD" if "amd" in source_url.lower() or any(k in name.lower() for k in ("ryzen", "epyc", "threadripper", "athlon")) else "Intel"
            full_name = name if name.lower().startswith(brand.lower()) else f"{brand} {name}"
            if "BGA" in socket_s.upper() and "Core Ultra" not in name:
                if "Atom" in name or "Processor U" in name:
                    continue
            cores_m = re.match(r"(\d+)(?:\s*/\s*(\d+))?", cores_str)
            c_cnt = int(cores_m.group(1)) if cores_m else None
            t_cnt = int(cores_m.group(2)) if cores_m and cores_m.group(2) else c_cnt

            clks = [float(x) for x in re.findall(r"(\d+(?:\.\d+)?)\s*GHz", clock_str, re.I)]
            if not clks:
                clks = [round(float(x) / 1000.0, 2) for x in re.findall(r"(\d+(?:\.\d+)?)\s*MHz", clock_str, re.I)]
            base_ghz = clks[0] if clks else None
            boost_ghz = clks[-1] if len(clks) >= 2 else None

            l3_m = re.search(r"(\d+)\s*MB", l3_s, re.I)
            tdp_m = re.search(r"(\d+)\s*W", tdp_s, re.I)
            clean_sock = socket_s.replace("Socket ", "").strip()

            extra = {
                "Silikon Kod Adı (Codename)": codename,
                "Çıkış Tarihi (Release)": rel_s,
            }
            if proc_s:
                extra["Üretim Teknolojisi (Litografi)"] = proc_s
            rows.append(
                {
                    "category": "CPU",
                    "source_name": "TechPowerUp Ajax",
                    "canonical_key": normalize_canonical_key(full_name),
                    "display_name": full_name,
                    "brand": brand,
                    "payload": {
                        "socket": clean_sock,
                        "core_count": c_cnt,
                        "threads": t_cnt,
                        "core_clock": base_ghz,
                        "boost_clock": boost_ghz,
                        "l3_cache": int(l3_m.group(1)) if l3_m else None,
                        "tdp": int(tdp_m.group(1)) if tdp_m else None,
                        "process_node": proc_s or None,
                        "microarchitecture": codename or None,
                        "release_date": rel_s or None,
                    },
                    "extra_specs": extra,
                    "images": [],
                    "source_url": source_url,
                }
            )

        elif cat == "GPU" and len(tds) >= 6:
            raw_col0 = raw_tds[0]
            _, bus_s, mem_s, gclk_s, mclk_s, cfg_s = tds[:6]

            # Extract structured elements from TPU td[0]
            nm_m = re.search(r'class="item-name"[^>]*>(.*?)</div>', raw_col0, re.S)
            rel_m = re.search(r'class="item-released"[^>]*>(.*?)</div>', raw_col0, re.S)
            chip_m = re.search(r'class="item-chip"[^>]*>(.*?)</div>', raw_col0, re.S)
            img_m = re.search(r'<img[^>]+src=["\']?(https://tpucdn\.com/[^"\'\s>]+)', raw_col0, re.I)

            chip_name = _strip_tags(nm_m.group(1)) if nm_m else ""
            if not chip_name:
                a_m = re.search(r'<a[^>]*>(.*?)</a>', raw_col0, re.S)
                chip_name = _strip_tags(a_m.group(1)) if a_m else tds[0]
            chip_name = re.sub(
                r"\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\s+.*$",
                "",
                chip_name,
                flags=re.I,
            ).strip()
            rel_s = _strip_tags(rel_m.group(1)) if rel_m else ""
            gpu_die = _strip_tags(chip_m.group(1)) if chip_m else ""

            if not chip_name:
                continue
            cu_up = chip_name.upper()
            # Keep desktop discrete GPUs; skip embedded/laptop iGPUs
            if any(
                skip in cu_up
                for skip in (
                    "MOBILE", "MAX-Q", "LAPTOP", "EMBEDDED", "GRAPHICS ",
                    "RADEON 610M", "RADEON 660M", "RADEON 680M", "RADEON 740M",
                    "RADEON 760M", "RADEON 780M", "RADEON 840M", "RADEON 860M",
                    "RADEON 880M", "RADEON 890M", "RADEON 8050S", "RADEON 8060S",
                    "QUADRO", "RTX A", "RTX PRO", "CMP ", "MINING",
                )
            ):
                continue

            mem_m = re.search(r"(\d+)\s*GB\s*/\s*([A-Z0-9+]+)\s*/\s*(\d+)\s*bit", mem_s, re.I)
            sh_m = re.match(r"(\d+)\s*/\s*(\d+)\s*/\s*(\d+)", cfg_s)
            gclk_m = re.search(r"(\d+)\s*MHz", gclk_s)

            extra: dict[str, str] = {}
            if rel_s:
                extra["Çıkış Tarihi (Release)"] = rel_s
            if gpu_die:
                extra["GPU Silikon Çekirdeği (Die)"] = gpu_die
            if bus_s:
                extra["PCIe Veri Yolu Arayüzü"] = bus_s
            if sh_m:
                extra["Shaders / TMUs / ROPs"] = f"{sh_m.group(1)} / {sh_m.group(2)} / {sh_m.group(3)}"
            brand = "NVIDIA" if "nvidia" in source_url.lower() else ("AMD" if "amd" in source_url.lower() else "Intel")
            display_name = chip_name if chip_name.lower().startswith(brand.lower()) else f"{brand} {chip_name}"
            rows.append(
                {
                    "category": "GPU",
                    "source_name": "TechPowerUp Ajax",
                    "canonical_key": normalize_for_brand_match(display_name),
                    "display_name": display_name,
                    "brand": brand,
                    "payload": {
                        "chipset": chip_name,
                        "memory": int(mem_m.group(1)) if mem_m else None,
                        "memory_type": mem_m.group(2) if mem_m else None,
                        "bus_width": int(mem_m.group(3)) if mem_m else None,
                        "core_clock": int(gclk_m.group(1)) if gclk_m else None,
                        "cuda_cores": int(sh_m.group(1)) if sh_m else None,
                        "tmus": int(sh_m.group(2)) if sh_m else None,
                        "rops": int(sh_m.group(3)) if sh_m else None,
                        "gpu_die": gpu_die or None,
                        "pcie_interface": bus_s or None,
                        "release_date": rel_s or None,
                    },
                    "extra_specs": extra,
                    "images": [img_m.group(1)] if img_m else [],
                    "source_url": source_url,
                }
            )

        elif cat == "SSD" and len(tds) >= 7:
            raw_col0 = raw_tds[0]
            _, nand_s, fmt_s, iface_s, rel_s, ctrl_s, dram_s = tds[:7]
            nm_m = re.search(r'class="drive-name"[^>]*>(.*?)</a>', raw_col0, re.S)
            model_name = _strip_tags(nm_m.group(1)) if nm_m else re.split(r"\s{3,}", tds[0])[0].strip()
            img_m = re.search(r'<img[^>]+src=["\']?(https://tpucdn\.com/[^"\'\s>]+)', raw_col0, re.I)
            if not model_name:
                continue
            caps = [_strip_tags(c) for c in re.findall(r'class="drive-capacity"[^>]*>(.*?)</a>', raw_col0, re.S)]
            imgs: list[str] = []
            if img_m:
                small_u = img_m.group(1)
                full_u = small_u.replace(".small.jpg", ".jpg")
                imgs = [full_u, small_u] if full_u != small_u else [small_u]
            rows.append(
                {
                    "category": "STORAGE",
                    "source_name": "TechPowerUp Ajax",
                    "canonical_key": normalize_for_brand_match(model_name),
                    "display_name": model_name,
                    "brand": model_name.split()[0],
                    "payload": {
                        "nand_type": nand_s or None,
                        "form_factor": fmt_s or None,
                        "interface": iface_s or None,
                        "controller": ctrl_s if ctrl_s and ctrl_s.lower() != "unknown" else None,
                        "dram_cache": dram_s if dram_s and "unknown" not in dram_s.lower() else None,
                    },
                    "extra_specs": {
                        "Çıkış Tarihi (Release)": rel_s,
                        "Kapasite Varyantları": ", ".join(caps) if caps else "-",
                    },
                    "images": imgs,
                    "source_url": source_url,
                }
            )
    return rows
