#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/prepare_prepopulated_db.py
Bakes and optimizes hardware_wizard.db from data/catalog JSON files, ensuring all categories
including Smartphones and Tablets have complete Room SQLite tables and pre-populated rows.
"""

import os
import sys
import json
import re
import sqlite3
import shutil
import time

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ASSETS_DB_DIR = os.path.join(ROOT_DIR, "androidApp", "src", "main", "assets", "databases")
TARGET_DB_PATH = os.path.join(ASSETS_DB_DIR, "hardware_wizard.db")
CATALOG_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

def ensure_tables(cursor):
    cursor.execute("""
    CREATE TABLE IF NOT EXISTS `phone_specs` (
        `item_id` TEXT NOT NULL,
        `chipset` TEXT NOT NULL,
        `cpu_arch` TEXT NOT NULL,
        `gpu_model` TEXT NOT NULL,
        `process_node_nm` INTEGER NOT NULL,
        `ram_gb` INTEGER NOT NULL,
        `ram_type` TEXT NOT NULL,
        `storage_gb` INTEGER NOT NULL,
        `storage_type` TEXT NOT NULL,
        `has_micro_sd` INTEGER NOT NULL,
        `antutu_score` INTEGER,
        `geekbench_single` INTEGER,
        `geekbench_multi` INTEGER,
        `stability_percent` INTEGER,
        `display_size_inch` REAL NOT NULL,
        `display_tech` TEXT NOT NULL,
        `resolution` TEXT NOT NULL,
        `ppi` INTEGER NOT NULL,
        `refresh_rate_hz` INTEGER NOT NULL,
        `peak_brightness_nit` INTEGER NOT NULL,
        `glass_protection` TEXT NOT NULL,
        `hdr_support` TEXT NOT NULL,
        `main_camera_mp` INTEGER NOT NULL,
        `main_sensor_size` TEXT NOT NULL,
        `main_aperture` TEXT NOT NULL,
        `has_ois` INTEGER NOT NULL,
        `ois_type` TEXT NOT NULL,
        `telephoto_mp` INTEGER,
        `optical_zoom_x` REAL,
        `ultra_wide_mp` INTEGER,
        `video_resolution` TEXT NOT NULL,
        `selfie_camera_mp` INTEGER NOT NULL,
        `battery_mah` INTEGER NOT NULL,
        `wired_charging_watt` INTEGER NOT NULL,
        `wireless_charging_watt` INTEGER,
        `has_reverse_charging` INTEGER NOT NULL,
        `charging_time_to_full_min` INTEGER,
        `weight_grams` INTEGER NOT NULL,
        `thickness_mm` REAL NOT NULL,
        `frame_material` TEXT NOT NULL,
        `water_resistance` TEXT NOT NULL,
        `has_stylus` INTEGER NOT NULL,
        `os_name` TEXT NOT NULL,
        `update_years` INTEGER NOT NULL,
        `pubg_fps_support` TEXT NOT NULL,
        `genshin_stability_score` TEXT NOT NULL,
        `variants` TEXT NOT NULL,
        PRIMARY KEY(`item_id`),
        FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    """)

    cursor.execute("""
    CREATE TABLE IF NOT EXISTS `tablet_specs` (
        `item_id` TEXT NOT NULL,
        `chipset` TEXT NOT NULL,
        `cpu_arch` TEXT NOT NULL,
        `gpu_model` TEXT NOT NULL,
        `process_node_nm` INTEGER NOT NULL,
        `ram_gb` INTEGER NOT NULL,
        `storage_gb` INTEGER NOT NULL,
        `storage_type` TEXT NOT NULL,
        `has_micro_sd` INTEGER NOT NULL,
        `antutu_score` INTEGER,
        `geekbench_single` INTEGER,
        `geekbench_multi` INTEGER,
        `stability_percent` INTEGER,
        `display_size_inch` REAL NOT NULL,
        `display_tech` TEXT NOT NULL,
        `resolution` TEXT NOT NULL,
        `ppi` INTEGER NOT NULL,
        `refresh_rate_hz` INTEGER NOT NULL,
        `peak_brightness_nit` INTEGER NOT NULL,
        `stylus_support` TEXT NOT NULL,
        `main_camera_mp` INTEGER NOT NULL,
        `front_camera_mp` INTEGER NOT NULL,
        `speaker_count` INTEGER NOT NULL,
        `has_dolby_atmos` INTEGER NOT NULL,
        `battery_mah` INTEGER NOT NULL,
        `wired_charging_watt` INTEGER NOT NULL,
        `wireless_charging_watt` INTEGER,
        `weight_grams` INTEGER NOT NULL,
        `thickness_mm` REAL NOT NULL,
        `body_material` TEXT NOT NULL,
        `os_name` TEXT NOT NULL,
        `update_years` INTEGER NOT NULL,
        `cellular_option` TEXT NOT NULL,
        `variants` TEXT NOT NULL,
        PRIMARY KEY(`item_id`),
        FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
    );
    """)

def populate_smartphones(cursor):
    path = os.path.join(CATALOG_DIR, "smartphone.json")
    if not os.path.exists(path):
        return
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    now_ms = int(time.time() * 1000)
    for p in data:
        pid = p["id"]
        s = p.get("spec") or {}
        img = p.get("imageUrl") or (p.get("images")[0] if p.get("images") else "")
        imgs = json.dumps(p.get("images") or [img])
        offers = json.dumps(p.get("stores") or [])
        history = json.dumps(p.get("priceHistory") or [])
        price_minor = int(p.get("priceTry", 0.0) * 100)
        
        cursor.execute("""
        INSERT OR REPLACE INTO `hardware_items` (
            `id`, `brand`, `model`, `category`, `price_minor`, `currency_code`, `cached_at`,
            `current_price_try`, `lowest_price_try`, `highest_price_try`, `offers`, `price_history`,
            `benchmark_score`, `benchmark_tier`, `benchmark_test_suite`, `benchmark_gaming_index`,
            `cpu_synthetic`, `gpu_synthetic`, `game_fps`, `image_url`, `images`
        ) VALUES (?, ?, ?, 'SMARTPHONE', ?, 'TRY', ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, NULL, NULL, NULL, ?, ?)
        """, (
            pid, p.get("brand") or "", p.get("model") or p.get("name") or "",
            price_minor, now_ms, p.get("priceTry"), p.get("priceTry"), p.get("priceTry"),
            offers, history, p.get("benchmarkScore") or s.get("antutuScore"),
            p.get("tier") or "", p.get("gamingIndex") or 0, img, imgs
        ))
        
        variants_json = json.dumps(s.get("variants") or [])
        cursor.execute("""
        INSERT OR REPLACE INTO `phone_specs` (
            `item_id`, `chipset`, `cpu_arch`, `gpu_model`, `process_node_nm`, `ram_gb`, `ram_type`,
            `storage_gb`, `storage_type`, `has_micro_sd`, `antutu_score`, `geekbench_single`, `geekbench_multi`,
            `stability_percent`, `display_size_inch`, `display_tech`, `resolution`, `ppi`, `refresh_rate_hz`,
            `peak_brightness_nit`, `glass_protection`, `hdr_support`, `main_camera_mp`, `main_sensor_size`,
            `main_aperture`, `has_ois`, `ois_type`, `telephoto_mp`, `optical_zoom_x`, `ultra_wide_mp`,
            `video_resolution`, `selfie_camera_mp`, `battery_mah`, `wired_charging_watt`, `wireless_charging_watt`,
            `has_reverse_charging`, `charging_time_to_full_min`, `weight_grams`, `thickness_mm`, `frame_material`,
            `water_resistance`, `has_stylus`, `os_name`, `update_years`, `pubg_fps_support`,
            `genshin_stability_score`, `variants`
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            pid, s.get("chipset") or "", s.get("cpuArch") or "", s.get("gpuModel") or "",
            s.get("processNodeNm") or 0, s.get("ramGb") or 0, s.get("ramType") or "",
            s.get("storageGb") or 0, s.get("storageType") or "", 1 if s.get("hasMicroSd") else 0,
            s.get("antutuScore"), s.get("geekbenchSingle"), s.get("geekbenchMulti"),
            s.get("stabilityPercent"), s.get("displaySizeInch") or 0.0, s.get("displayTech") or "",
            s.get("resolution") or "", s.get("ppi") or 0, s.get("refreshRateHz") or 0,
            s.get("peakBrightnessNit") or 0, s.get("glassProtection") or "",
            s.get("hdrSupport") or "", s.get("mainCameraMp") or 0, s.get("mainSensorSize") or "",
            s.get("mainAperture") or "", 1 if s.get("hasOis") else 0, s.get("oisType") or "",
            s.get("telephotoMp"), s.get("opticalZoomX"), s.get("ultraWideMp"),
            s.get("videoResolution") or "", s.get("selfieCameraMp") or 0, s.get("batteryMah") or 0,
            s.get("wiredChargingWatt") or 0, s.get("wirelessChargingWatt"),
            1 if s.get("hasReverseCharging") else 0, s.get("chargingTimeToFullMin"),
            s.get("weightGrams") or 0, s.get("thicknessMm") or 0.0, s.get("frameMaterial") or "",
            s.get("waterResistance") or "", 1 if s.get("hasStylus") else 0,
            s.get("osName") or "", s.get("updateYears") or 0,
            s.get("pubgFpsSupport") or "", s.get("genshinStabilityScore") or "",
            variants_json
        ))
    print(f"  [OK] {len(data)} akıllı telefon Room SQLite veritabanına işlendi.")

def populate_tablets(cursor):
    path = os.path.join(CATALOG_DIR, "tablet.json")
    if not os.path.exists(path):
        return
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    now_ms = int(time.time() * 1000)
    for p in data:
        pid = p["id"]
        s = p.get("spec") or {}
        img = p.get("imageUrl") or (p.get("images")[0] if p.get("images") else "")
        imgs = json.dumps(p.get("images") or [img])
        offers = json.dumps(p.get("stores") or [])
        history = json.dumps(p.get("priceHistory") or [])
        price_minor = int(p.get("priceTry", 0.0) * 100)
        
        cursor.execute("""
        INSERT OR REPLACE INTO `hardware_items` (
            `id`, `brand`, `model`, `category`, `price_minor`, `currency_code`, `cached_at`,
            `current_price_try`, `lowest_price_try`, `highest_price_try`, `offers`, `price_history`,
            `benchmark_score`, `benchmark_tier`, `benchmark_test_suite`, `benchmark_gaming_index`,
            `cpu_synthetic`, `gpu_synthetic`, `game_fps`, `image_url`, `images`
        ) VALUES (?, ?, ?, 'TABLET', ?, 'TRY', ?, ?, ?, ?, ?, ?, ?, ?, NULL, ?, NULL, NULL, NULL, ?, ?)
        """, (
            pid, p.get("brand") or "", p.get("model") or p.get("name") or "",
            price_minor, now_ms, p.get("priceTry"), p.get("priceTry"), p.get("priceTry"),
            offers, history, p.get("benchmarkScore") or s.get("antutuScore"),
            p.get("tier") or "", p.get("gamingIndex") or 0, img, imgs
        ))
        
        variants_json = json.dumps(s.get("variants") or [])
        cursor.execute("""
        INSERT OR REPLACE INTO `tablet_specs` (
            `item_id`, `chipset`, `cpu_arch`, `gpu_model`, `process_node_nm`, `ram_gb`, `storage_gb`,
            `storage_type`, `has_micro_sd`, `antutu_score`, `geekbench_single`, `geekbench_multi`,
            `stability_percent`, `display_size_inch`, `display_tech`, `resolution`, `ppi`, `refresh_rate_hz`,
            `peak_brightness_nit`, `stylus_support`, `main_camera_mp`, `front_camera_mp`, `speaker_count`,
            `has_dolby_atmos`, `battery_mah`, `wired_charging_watt`, `wireless_charging_watt`, `weight_grams`,
            `thickness_mm`, `body_material`, `os_name`, `update_years`, `cellular_option`, `variants`
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, (
            pid, s.get("chipset") or "", s.get("cpuArch") or "", s.get("gpuModel") or "",
            s.get("processNodeNm") or 0, s.get("ramGb") or 0, s.get("storageGb") or 0,
            s.get("storageType") or "", 1 if s.get("hasMicroSd") else 0,
            s.get("antutuScore"), s.get("geekbenchSingle"), s.get("geekbenchMulti"),
            s.get("stabilityPercent"), s.get("displaySizeInch") or 0.0, s.get("displayTech") or "",
            s.get("resolution") or "", s.get("ppi") or 0, s.get("refreshRateHz") or 0,
            s.get("peakBrightnessNit") or 0, s.get("stylusSupport") or "",
            s.get("mainCameraMp") or 0, s.get("frontCameraMp") or 0, s.get("speakerCount") or 0,
            1 if s.get("hasDolbyAtmos") else 0, s.get("batteryMah") or 0,
            s.get("wiredChargingWatt") or 0, s.get("wirelessChargingWatt"),
            s.get("weightGrams") or 0, s.get("thicknessMm") or 0.0, s.get("bodyMaterial") or "",
            s.get("osName") or "", s.get("updateYears") or 0,
            s.get("cellularOption") or "", variants_json
        ))
    print(f"  [OK] {len(data)} tablet Room SQLite veritabanına işlendi.")


def _gen_hardware_id(category: str, name: str) -> str:
    sanitized = re.sub(r"[^a-z0-9]", "_", name.lower())
    sanitized = re.sub(r"_+", "_", sanitized).strip("_")
    return f"{category.lower()}_{sanitized}"


def _extract_brand_and_model(full_name: str):
    trimmed = full_name.strip()
    known_brands = [
        "AMD", "Intel", "NVIDIA", "Asus", "ASUS", "MSI", "Gigabyte", "GIGABYTE", "ASRock",
        "Corsair", "G.Skill", "Kingston", "Crucial", "Teamgroup", "TEAMGROUP",
        "EVGA", "Zotac", "ZOTAC", "Sapphire", "PowerColor", "XFX", "Inno3D", "PNY", "Gainward",
        "Samsung", "Western Digital", "WD", "Seagate", "Sabrent",
        "be quiet!", "Noctua", "Thermalright", "Deepcool", "DEEPCOOL", "NZXT", "Cooler Master",
        "Lian Li", "Seasonic", "SeaSonic", "Thermaltake", "1st Player", "SAMA",
    ]
    lower = trimmed.lower()
    for kb in known_brands:
        if lower.startswith(kb.lower()):
            brand = trimmed[:len(kb)].strip()
            model = trimmed[len(kb):].strip() or brand
            return brand, model
    if " " in trimmed:
        idx = trimmed.index(" ")
        return trimmed[:idx], trimmed[idx + 1:].strip()
    return "Generic", trimmed


def _normalize_gpu_chipset_key(chipset: str, model: str = ""):
    text = f"{chipset} {model}".lower()
    mapping = [
        ("5090", "rtx_5090"), ("5080", "rtx_5080"),
        ("5070 ti", "rtx_5070_ti"), ("5070ti", "rtx_5070_ti"), ("5070", "rtx_5070"),
        ("5060 ti", "rtx_5060_ti"), ("5060ti", "rtx_5060_ti"),
        ("4090", "rtx_4090"),
        ("4080 super", "rtx_4080_super"), ("4080s", "rtx_4080_super"), ("4080", "rtx_4080"),
        ("4070 ti super", "rtx_4070_ti_super"), ("4070tis", "rtx_4070_ti_super"),
        ("4070 ti", "rtx_4070_ti"), ("4070ti", "rtx_4070_ti"),
        ("4070 super", "rtx_4070_super"), ("4070s", "rtx_4070_super"), ("4070", "rtx_4070"),
        ("4060 ti", "rtx_4060_ti"), ("4060ti", "rtx_4060_ti"), ("4060", "rtx_4060"),
        ("3090 ti", "rtx_3090_ti"), ("3090ti", "rtx_3090_ti"), ("3090", "rtx_3090"),
        ("3080 ti", "rtx_3080_ti"), ("3080ti", "rtx_3080_ti"), ("3080", "rtx_3080"),
        ("3070 ti", "rtx_3070_ti"), ("3070ti", "rtx_3070_ti"), ("3070", "rtx_3070"),
        ("3060 ti", "rtx_3060_ti"), ("3060ti", "rtx_3060_ti"), ("3060", "rtx_3060"), ("3050", "rtx_3050"),
        ("b580", "arc_b580"), ("b570", "intel_arc_b570"), ("a770", "arc_a770"), ("a750", "arc_a750"),
        ("9070 xt", "rx_9070_xt"), ("9070xt", "rx_9070_xt"),
        ("9070 gre", "radeon_rx_9070_gre"), ("9070", "rx_9070"),
        ("7900 xtx", "rx_7900_xtx"), ("7900xtx", "rx_7900_xtx"),
        ("7900 xt", "rx_7900_xt"), ("7900xt", "rx_7900_xt"),
        ("7900 gre", "rx_7900_gre"), ("7900gre", "rx_7900_gre"),
        ("7800 xt", "rx_7800_xt"), ("7800xt", "rx_7800_xt"),
        ("7700 xt", "rx_7700_xt"), ("7700xt", "rx_7700_xt"),
        ("7650 gre", "radeon_rx_7650_gre"),
        ("7600 xt", "rx_7600_xt"), ("7600xt", "rx_7600_xt"), ("7600", "rx_7600"),
        ("6950 xt", "rx_6950_xt"), ("6950xt", "rx_6950_xt"),
        ("6800 xt", "rx_6800_xt"), ("6800xt", "rx_6800_xt"),
        ("6700 xt", "rx_6700_xt"), ("6700xt", "rx_6700_xt"),
        ("6600 xt", "rx_6600_xt"), ("6600xt", "rx_6600_xt"), ("6600", "rx_6600"),
    ]
    for needle, key in mapping:
        if needle in text:
            return key
    return None


def _ensure_game_fps_keys(games: dict) -> dict:
    if not isinstance(games, dict):
        return {}
    out = {}
    for g_key, g_val in games.items():
        if not isinstance(g_val, dict):
            continue
        item = dict(g_val)
        if "p1080" not in item and "1080p" in item:
            item["p1080"] = item["1080p"]
        if "p1440" not in item and "1440p" in item:
            item["p1440"] = item["1440p"]
        if "p4k" not in item and "4k" in item:
            item["p4k"] = item["4k"]
        out[g_key] = item
    return out


def populate_benchmarks(cursor):
    path = os.path.join(CATALOG_DIR, "hardware_benchmarks.json")
    if not os.path.exists(path):
        return
    with open(path, "r", encoding="utf-8") as f:
        bench_data = json.load(f)

    cpus_bench = bench_data.get("cpus") or {}
    gpus_bench = bench_data.get("gpus") or {}

    cpu_rows = cursor.execute("SELECT id, brand, model FROM hardware_items WHERE category = 'CPU'").fetchall()
    cpu_updated = 0
    for row_id, brand, model in cpu_rows:
        b = cpus_bench.get(row_id) or cpus_bench.get(_gen_hardware_id("CPU", f"{brand} {model}"))
        if not b:
            continue
        score = b.get("score")
        rel = b.get("gaming_relative_index")
        gaming_idx = int(round(rel * 100)) if rel is not None else score
        tier = b.get("tier") or "A-Tier"
        syn = b.get("synthetic")
        games = _ensure_game_fps_keys(b.get("games"))
        cursor.execute(
            """
            UPDATE hardware_items
            SET benchmark_score = ?,
                benchmark_tier = ?,
                benchmark_test_suite = ?,
                benchmark_gaming_index = ?,
                cpu_synthetic = ?,
                game_fps = ?
            WHERE id = ?
            """,
            (
                score,
                tier,
                "CPU-Monkey & Cinebench / Geekbench Çoklu Test Paketi",
                gaming_idx,
                json.dumps(syn, ensure_ascii=False) if syn else None,
                json.dumps(games, ensure_ascii=False) if games else None,
                row_id,
            ),
        )
        cpu_updated += 1

    gpu_rows = cursor.execute(
        """
        SELECT h.id, h.model, COALESCE(g.chipset, '')
        FROM hardware_items h
        LEFT JOIN gpu_specs g ON g.item_id = h.id
        WHERE h.category = 'GPU'
        """
    ).fetchall()
    gpu_updated = 0
    for row_id, model, chipset in gpu_rows:
        b = gpus_bench.get(row_id)
        if not b:
            k = _normalize_gpu_chipset_key(chipset, model)
            if k:
                b = gpus_bench.get(f"gpu_{k}") or gpus_bench.get(k)
        if not b:
            continue
        score = b.get("score")
        rel = b.get("relative_performance_pct")
        gaming_idx = int(round(rel * 100)) if rel is not None else score
        tier = b.get("tier") or "A-Tier"
        syn = b.get("synthetic")
        games = _ensure_game_fps_keys(b.get("games"))
        cursor.execute(
            """
            UPDATE hardware_items
            SET benchmark_score = ?,
                benchmark_tier = ?,
                benchmark_test_suite = ?,
                benchmark_gaming_index = ?,
                gpu_synthetic = ?,
                game_fps = ?
            WHERE id = ?
            """,
            (
                score,
                tier,
                "3DMark Time Spy / Steel Nomad Çoklu Test Paketi",
                gaming_idx,
                json.dumps(syn, ensure_ascii=False) if syn else None,
                json.dumps(games, ensure_ascii=False) if games else None,
                row_id,
            ),
        )
        gpu_updated += 1

    print(f"  [OK] Benchmarklar SQLite'a işlendi: {cpu_updated} CPU, {gpu_updated} GPU (cpu_synthetic/gpu_synthetic/game_fps).")


def populate_enriched_component_specs(cursor):
    now_ms = int(time.time() * 1000)

    # 1. CPU specs
    cpu_path = os.path.join(CATALOG_DIR, "cpu.json")
    if os.path.exists(cpu_path):
        cpus = json.load(open(cpu_path, "r", encoding="utf-8"))
        c_cnt = 0
        for item in cpus:
            name = (item.get("name") or "").strip()
            if not name:
                continue
            item_id = _gen_hardware_id("CPU", name)
            if not cursor.execute("SELECT 1 FROM cpu_specs WHERE item_id = ?", (item_id,)).fetchone():
                continue
            base_mhz = int(round((item.get("core_clock") or 3.5) * 1000))
            boost_mhz = int(round((item.get("boost_clock") or item.get("core_clock") or 4.0) * 1000))
            has_igpu = 1 if (item.get("graphics") and str(item.get("graphics")).lower() != "none") else 0
            cursor.execute(
                """
                UPDATE cpu_specs
                SET core_count = COALESCE(?, core_count),
                    thread_count = COALESCE(?, thread_count),
                    base_clock_mhz = ?,
                    boost_clock_mhz = ?,
                    tdp_watts = COALESCE(?, tdp_watts),
                    has_integrated_graphics = ?,
                    architecture = COALESCE(?, architecture),
                    l2_cache_mb = COALESCE(?, l2_cache_mb),
                    l3_cache_mb = COALESCE(?, l3_cache_mb),
                    process_node = COALESCE(?, process_node),
                    pcie_version = COALESCE(?, pcie_version),
                    max_memory_speed = COALESCE(?, max_memory_speed),
                    unlocked_multiplier = ?,
                    core_topology = COALESCE(?, core_topology),
                    power_limits = COALESCE(?, power_limits),
                    igpu_model = COALESCE(?, igpu_model)
                WHERE item_id = ?
                """,
                (
                    item.get("core_count"), item.get("threads"), base_mhz, max(base_mhz, boost_mhz),
                    item.get("tdp"), has_igpu, item.get("microarchitecture"), item.get("l2_cache"),
                    item.get("l3_cache"), item.get("process_node"), item.get("pcie_version"),
                    item.get("max_memory_speed"),
                    None if item.get("unlocked_multiplier") is None else (1 if item.get("unlocked_multiplier") else 0),
                    item.get("core_topology"), item.get("power_limits"), item.get("igpu_model"), item_id
                ),
            )
            c_cnt += 1
        print(f"  [OK] {c_cnt} CPU derin mühendislik spec satırı güncellendi.")

    # 2. GPU specs
    gpu_path = os.path.join(CATALOG_DIR, "video-card.json")
    if os.path.exists(gpu_path):
        gpus = json.load(open(gpu_path, "r", encoding="utf-8"))
        g_cnt = 0
        for item in gpus:
            name = (item.get("name") or "").strip()
            if not name:
                continue
            item_id = _gen_hardware_id("GPU", name)
            if not cursor.execute("SELECT 1 FROM gpu_specs WHERE item_id = ?", (item_id,)).fetchone():
                continue
            vram_mb = int(item["memory"]) * 1024 if item.get("memory") else None
            length_mm = int(item["length"]) if item.get("length") else None
            cursor.execute(
                """
                UPDATE gpu_specs
                SET vram_mb = COALESCE(?, vram_mb),
                    memory_type = COALESCE(?, memory_type),
                    tdp_watts = COALESCE(?, tdp_watts),
                    recommended_psu_watts = COALESCE(?, recommended_psu_watts),
                    bus_width_bits = COALESCE(?, bus_width_bits),
                    length_mm = COALESCE(?, length_mm),
                    power_connectors = COALESCE(?, power_connectors),
                    display_outputs = COALESCE(?, display_outputs),
                    slot_width = COALESCE(?, slot_width),
                    architecture = COALESCE(?, architecture),
                    process_node = COALESCE(?, process_node),
                    cuda_cores = COALESCE(?, cuda_cores),
                    tensor_ai_tops = COALESCE(?, tensor_ai_tops),
                    rt_cores = COALESCE(?, rt_cores),
                    memory_speed_gbps = COALESCE(?, memory_speed_gbps),
                    bandwidth_gbs = COALESCE(?, bandwidth_gbs)
                WHERE item_id = ?
                """,
                (
                    vram_mb, item.get("memory_type"), item.get("tdp"), item.get("recommended_psu"),
                    item.get("bus_width"), length_mm, item.get("power_connectors"),
                    item.get("display_outputs"), item.get("slot_width"), item.get("architecture"),
                    item.get("process_node"), item.get("cuda_cores"), item.get("tensor_ai_tops"),
                    item.get("rt_cores"), item.get("memory_speed"), item.get("bandwidth_gbs"), item_id
                ),
            )
            g_cnt += 1
        print(f"  [OK] {g_cnt} GPU derin mühendislik spec satırı güncellendi.")

    # 3. PSU specs (update existing + insert new IRONScale PSUs)
    psu_path = os.path.join(CATALOG_DIR, "power-supply.json")
    if os.path.exists(psu_path):
        psus = json.load(open(psu_path, "r", encoding="utf-8"))
        p_upd = 0
        p_ins = 0
        for item in psus:
            name = (item.get("name") or "").strip()
            if not name:
                continue
            item_id = _gen_hardware_id("PSU", name)
            brand, model = _extract_brand_and_model(name)
            eff_raw = str(item.get("efficiency") or "").lower().strip()
            upper = name.upper()
            if eff_raw == "titanium" or "TITANIUM" in upper:
                eff_enum = "TITANIUM"
            elif eff_raw == "platinum" or "PLATINUM" in upper or "EDGE" in upper or "THOR" in upper:
                eff_enum = "PLATINUM"
            elif eff_raw == "bronze" or "BRONZE" in upper or "BN" in upper:
                eff_enum = "BRONZE"
            elif eff_raw == "silver" or "SILVER" in upper:
                eff_enum = "SILVER"
            else:
                eff_enum = "GOLD"
            watt = max(300, int(item.get("wattage") or 750))
            is_atx3 = 1 if ("ATX 3" in upper or "ATX3" in upper or "PCIE 5" in upper or "EDGE" in upper or "VITA GM" in upper or (item.get("atx_standard") and "ATX 3" in str(item.get("atx_standard")).upper())) else 0
            img = item.get("imageUrl") or (item.get("images")[0] if item.get("images") else "")
            imgs = json.dumps(item.get("images") or ([img] if img else []), ensure_ascii=False)
            outputs_val = item.get("outputs") or " • ".join([v for v in (item.get("pcie_12vhpwr"), item.get("eps_connectors"), item.get("pcie_connectors")) if v]) or None

            exists = cursor.execute("SELECT 1 FROM hardware_items WHERE id = ?", (item_id,)).fetchone()
            if not exists:
                cursor.execute(
                    """
                    INSERT OR REPLACE INTO `hardware_items` (
                        `id`, `brand`, `model`, `category`, `price_minor`, `currency_code`, `cached_at`,
                        `current_price_try`, `lowest_price_try`, `highest_price_try`, `offers`, `price_history`,
                        `benchmark_score`, `benchmark_tier`, `benchmark_test_suite`, `benchmark_gaming_index`,
                        `cpu_synthetic`, `gpu_synthetic`, `game_fps`, `image_url`, `images`
                    ) VALUES (?, ?, ?, 'PSU', NULL, NULL, ?, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, ?, ?)
                    """,
                    (item_id, brand, model, now_ms, img or None, imgs),
                )
                p_ins += 1
            else:
                p_upd += 1

            cursor.execute(
                """
                INSERT OR REPLACE INTO `psu_specs` (
                    `item_id`, `wattage`, `efficiency`, `is_atx3_compliant`, `supports_pcie5_power`,
                    `modularity`, `form_factor`, `outputs`, `tier_rating`, `capacitors`,
                    `atx_standard`, `pcie_12vhpwr`, `eps_connectors`, `pcie_connectors`, `sata_connectors`,
                    `topology`, `protections`, `fan_and_size`, `cybenetics_cert`, `cybenetics_lambda`
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    item_id, watt, eff_enum, is_atx3, is_atx3,
                    item.get("modular"), item.get("type"), outputs_val, item.get("tier"), item.get("capacitors"),
                    item.get("atx_standard"), item.get("pcie_12vhpwr"), item.get("eps_connectors"),
                    item.get("pcie_connectors"), item.get("sata_connectors"), item.get("topology"),
                    item.get("protections"), item.get("fan_size"), item.get("cybenetics"), item.get("cybenetics_lambda")
                ),
            )
        print(f"  [OK] PSU specs işlendi: {p_upd} güncellendi, {p_ins} yeni IRONScale modeli eklendi.")

    # 4. Storage specs
    ssd_path = os.path.join(CATALOG_DIR, "internal-hard-drive.json")
    if os.path.exists(ssd_path):
        ssds = json.load(open(ssd_path, "r", encoding="utf-8"))
        s_cnt = 0
        for item in ssds:
            name = (item.get("name") or "").strip()
            if not name:
                continue
            item_id = _gen_hardware_id("STORAGE", name)
            if not cursor.execute("SELECT 1 FROM storage_specs WHERE item_id = ?", (item_id,)).fetchone():
                continue
            cursor.execute(
                """
                UPDATE storage_specs
                SET seq_read_mbps = COALESCE(?, seq_read_mbps),
                    seq_write_mbps = COALESCE(?, seq_write_mbps),
                    nand_type = COALESCE(?, nand_type),
                    endurance_tbw = COALESCE(?, endurance_tbw),
                    controller = COALESCE(?, controller),
                    dram_cache = COALESCE(?, dram_cache),
                    random_iops = COALESCE(?, random_iops)
                WHERE item_id = ?
                """,
                (
                    item.get("seq_read"), item.get("seq_write"), item.get("nand_type"),
                    item.get("endurance_tbw"), item.get("controller"), item.get("dram_cache"),
                    item.get("random_iops"), item_id
                ),
            )
            s_cnt += 1
        print(f"  [OK] {s_cnt} Storage (SSD) spec satırı güncellendi.")

    # 5. Cooler specs
    cooler_path = os.path.join(CATALOG_DIR, "cpu-cooler.json")
    if os.path.exists(cooler_path):
        coolers = json.load(open(cooler_path, "r", encoding="utf-8"))
        cl_cnt = 0
        for item in coolers:
            name = (item.get("name") or "").strip()
            if not name:
                continue
            item_id = _gen_hardware_id("COOLER", name)
            if not cursor.execute("SELECT 1 FROM cooler_specs WHERE item_id = ?", (item_id,)).fetchone():
                continue
            cursor.execute(
                """
                UPDATE cooler_specs
                SET max_cooling_tdp_watts = COALESCE(?, max_cooling_tdp_watts),
                    fan_rpm = COALESCE(?, fan_rpm),
                    noise_dba = COALESCE(?, noise_dba),
                    radiator_dimensions = COALESCE(?, radiator_dimensions),
                    fan_airflow = COALESCE(?, fan_airflow)
                WHERE item_id = ?
                """,
                (
                    item.get("max_tdp"), item.get("fan_rpm"), item.get("noise_level"),
                    item.get("radiator_dimensions"), item.get("fan_airflow"), item_id
                ),
            )
            cl_cnt += 1
        print(f"  [OK] {cl_cnt} Cooler spec satırı güncellendi.")

def checkpoint_and_optimize(source_db_path, output_db_path):
    os.makedirs(os.path.dirname(output_db_path), exist_ok=True)
    
    conn = sqlite3.connect(source_db_path)
    cursor = conn.cursor()

    cursor.execute("DROP TABLE IF EXISTS prebuilt_specs;")
    if cursor.execute("SELECT 1 FROM sqlite_master WHERE type='table' AND name='hardware_items';").fetchone():
        cursor.execute("DELETE FROM hardware_items WHERE category = 'PREBUILT_PC';")

    ensure_tables(cursor)
    populate_smartphones(cursor)
    populate_tablets(cursor)
    populate_enriched_component_specs(cursor)
    populate_benchmarks(cursor)

    cursor.execute("PRAGMA user_version = 35;")
    cursor.execute("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT);")
    cursor.execute("INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, '219d90c66fd6dc49f100818175002008');")

    if "app_settings" in [row[0] for row in cursor.execute("SELECT name FROM sqlite_master WHERE type='table';").fetchall()]:
        cursor.execute("DELETE FROM app_settings;")
        cursor.execute("INSERT INTO app_settings (key, value) VALUES ('catalog_seed_version', '3.0.0');")
        conn.commit()
    
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [row[0] for row in cursor.fetchall()]
    print(f"Database contains {len(tables)} tables: {tables[:12]}...")
    
    if "hardware_items" in tables:
        cursor.execute("SELECT COUNT(*) FROM hardware_items;")
        count = cursor.fetchone()[0]
        print(f"Total hardware_items rows: {count}")
    
    if "phone_specs" in tables:
        cursor.execute("SELECT COUNT(*) FROM phone_specs;")
        print(f"Total phone_specs rows: {cursor.fetchone()[0]}")
        
    if "tablet_specs" in tables:
        cursor.execute("SELECT COUNT(*) FROM tablet_specs;")
        print(f"Total tablet_specs rows: {cursor.fetchone()[0]}")
    
    cursor.execute("PRAGMA wal_checkpoint(TRUNCATE);")
    conn.commit()
    conn.close()
    
    if os.path.abspath(source_db_path) != os.path.abspath(output_db_path):
        shutil.copy2(source_db_path, output_db_path)
    
    out_conn = sqlite3.connect(output_db_path)
    out_cursor = out_conn.cursor()
    out_cursor.execute("VACUUM;")
    out_conn.commit()
    out_conn.close()
    
    size_mb = os.path.getsize(output_db_path) / (1024 * 1024)
    print(f"Optimized prepopulated DB ready at {output_db_path} ({size_mb:.2f} MB)")

if __name__ == "__main__":
    if len(sys.argv) > 1:
        src = sys.argv[1]
    else:
        src = TARGET_DB_PATH
    checkpoint_and_optimize(src, TARGET_DB_PATH)
