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
import sqlite3
import shutil
import time

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
            pid, p.get("brand", "Akıllı Telefon"), p.get("model", p.get("name", "")),
            price_minor, now_ms, p.get("priceTry"), p.get("priceTry"), p.get("priceTry"),
            offers, history, p.get("benchmarkScore") or s.get("antutuScore"),
            p.get("tier", "A"), p.get("gamingIndex", 80), img, imgs
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
            pid, s.get("chipset", ""), s.get("cpuArch", ""), s.get("gpuModel", ""),
            s.get("processNodeNm", 3), s.get("ramGb", 8), s.get("ramType", "LPDDR5X"),
            s.get("storageGb", 256), s.get("storageType", "UFS 4.0"), 1 if s.get("hasMicroSd") else 0,
            s.get("antutuScore"), s.get("geekbenchSingle"), s.get("geekbenchMulti"),
            s.get("stabilityPercent"), s.get("displaySizeInch", 6.8), s.get("displayTech", "OLED"),
            s.get("resolution", "FHD+"), s.get("ppi", 460), s.get("refreshRateHz", 120),
            s.get("peakBrightnessNit", 2000), s.get("glassProtection", "Gorilla Glass"),
            s.get("hdrSupport", "Dolby Vision"), s.get("mainCameraMp", 50), s.get("mainSensorSize", "1/1.3"),
            s.get("mainAperture", "f/1.8"), 1 if s.get("hasOis", True) else 0, s.get("oisType", "OIS"),
            s.get("telephotoMp"), s.get("opticalZoomX"), s.get("ultraWideMp"),
            s.get("videoResolution", "4K 60 FPS"), s.get("selfieCameraMp", 12), s.get("batteryMah", 5000),
            s.get("wiredChargingWatt", 45), s.get("wirelessChargingWatt"),
            1 if s.get("hasReverseCharging") else 0, s.get("chargingTimeToFullMin"),
            s.get("weightGrams", 220), s.get("thicknessMm", 8.2), s.get("frameMaterial", "Titanyum"),
            s.get("waterResistance", "IP68"), 1 if s.get("hasStylus") else 0,
            s.get("osName", "Android 14"), s.get("updateYears", 7),
            s.get("pubgFpsSupport", "120 FPS"), s.get("genshinStabilityScore", "%70+ Kararlı"),
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
            pid, p.get("brand", "Tablet"), p.get("model", p.get("name", "")),
            price_minor, now_ms, p.get("priceTry"), p.get("priceTry"), p.get("priceTry"),
            offers, history, p.get("benchmarkScore") or s.get("antutuScore"),
            p.get("tier", "A"), p.get("gamingIndex", 80), img, imgs
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
            pid, s.get("chipset", ""), s.get("cpuArch", ""), s.get("gpuModel", ""),
            s.get("processNodeNm", 3), s.get("ramGb", 8), s.get("storageGb", 256),
            s.get("storageType", "NVMe"), 1 if s.get("hasMicroSd") else 0,
            s.get("antutuScore"), s.get("geekbenchSingle"), s.get("geekbenchMulti"),
            s.get("stabilityPercent"), s.get("displaySizeInch", 11.0), s.get("displayTech", "OLED"),
            s.get("resolution", "2064 x 2752"), s.get("ppi", 264), s.get("refreshRateHz", 120),
            s.get("peakBrightnessNit", 1000), s.get("stylusSupport", "Stylus Destekli"),
            s.get("mainCameraMp", 12), s.get("frontCameraMp", 12), s.get("speakerCount", 4),
            1 if s.get("hasDolbyAtmos", True) else 0, s.get("batteryMah", 10000),
            s.get("wiredChargingWatt", 35), s.get("wirelessChargingWatt"),
            s.get("weightGrams", 550), s.get("thicknessMm", 5.5), s.get("bodyMaterial", "Alüminyum"),
            s.get("osName", "Android / iPadOS"), s.get("updateYears", 5),
            s.get("cellularOption", "Wi-Fi"), variants_json
        ))
    print(f"  [OK] {len(data)} tablet Room SQLite veritabanına işlendi.")

def checkpoint_and_optimize(source_db_path, output_db_path):
    os.makedirs(os.path.dirname(output_db_path), exist_ok=True)
    
    conn = sqlite3.connect(source_db_path)
    cursor = conn.cursor()
    
    ensure_tables(cursor)
    populate_smartphones(cursor)
    populate_tablets(cursor)
    
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

    if "app_settings" in tables:
        cursor.execute("INSERT OR REPLACE INTO app_settings (key, value) VALUES ('catalog_seed_version', '2.1.0');")
        conn.commit()
    
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
