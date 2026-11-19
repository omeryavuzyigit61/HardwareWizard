#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/prepare_prepopulated_db.py
Rebuilds androidApp/src/main/assets/databases/hardware_wizard.db cleanly from
shared/src/commonMain/composeResources/files/*.json with exact Room v36 DDL
and identity_hash matching HardwareDatabase_Impl.kt.
"""

from __future__ import annotations

import json
import os
import re
import shutil
import sqlite3
import sys
import time
from typing import Any
from urllib.parse import urlparse

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ASSETS_DB_DIR = os.path.join(ROOT_DIR, "androidApp", "src", "main", "assets", "databases")
TARGET_DB_PATH = os.path.join(ASSETS_DB_DIR, "hardware_wizard.db")
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")

ROOM_SCHEMA_VERSION = 36
ROOM_IDENTITY_HASH = "67974b2b98fd0fd519c42be66d6f74f3"
CATALOG_SEED_VERSION = "5.0.0"

MIN_VALID_TRY_PRICE = 250.0
MAX_VALID_TRY_PRICE = 2_500_000.0

ALLOWED_STORE_DOMAINS = {
    "akakce.com", "cimri.com", "epey.com", "itopya.com", "sinerji.gen.tr",
    "gaming.gen.tr", "incehesap.com", "vatanbilgisayar.com", "teknobiyotik.com",
    "gamegaraj.com", "pckolik.com", "tebilon.com", "inventus.com.tr", "qp.com.tr",
    "hepsiburada.com", "trendyol.com", "amazon.com.tr", "n11.com", "pazarama.com",
    "idefix.com", "mediamarkt.com.tr", "teknosa.com", "monsternotebook.com.tr",
    "excalibur.com.tr", "casper.com.tr", "gameekstra.com", "adeksstore.com",
    "zoccoshop.com", "gelbura.com", "dfsbilgisayar.com", "novabilgisayar.com",
    "elmacik.com", "bilisimport.com",
}

FORBIDDEN_SCHEMES = (
    "javascript:", "vbscript:", "data:", "file:", "content:",
    "intent:", "about:", "blob:", "ftp:", "ws:", "wss:",
)

KNOWN_BRANDS = [
    "AMD", "Intel", "NVIDIA", "Asus", "ASUS", "MSI", "Gigabyte", "GIGABYTE", "ASRock",
    "Corsair", "G.Skill", "Kingston", "Crucial", "Teamgroup", "TEAMGROUP",
    "EVGA", "Zotac", "ZOTAC", "Sapphire", "PowerColor", "XFX", "Inno3D", "PNY", "Gainward",
    "Samsung", "Western Digital", "WD", "Seagate", "Sabrent",
    "be quiet!", "Noctua", "Thermalright", "Deepcool", "DEEPCOOL", "NZXT", "Cooler Master",
    "Lian Li", "Seasonic", "SeaSonic", "Thermaltake",
]

DDL_STATEMENTS = [
    "CREATE TABLE IF NOT EXISTS `hardware_items` (`id` TEXT NOT NULL, `brand` TEXT NOT NULL, `model` TEXT NOT NULL, `category` TEXT NOT NULL, `price_minor` INTEGER, `currency_code` TEXT, `cached_at` INTEGER NOT NULL, `current_price_try` REAL, `lowest_price_try` REAL, `highest_price_try` REAL, `offers` TEXT, `price_history` TEXT, `benchmark_score` INTEGER, `benchmark_tier` TEXT, `benchmark_test_suite` TEXT, `benchmark_gaming_index` INTEGER, `cpu_synthetic` TEXT, `gpu_synthetic` TEXT, `game_fps` TEXT, `image_url` TEXT, `images` TEXT, `extra_specs` TEXT, `source_provenance` TEXT, `spec_verified_at` TEXT, PRIMARY KEY(`id`))",
    "CREATE INDEX IF NOT EXISTS `index_hardware_items_category` ON `hardware_items` (`category`)",
    "CREATE INDEX IF NOT EXISTS `index_hardware_items_brand` ON `hardware_items` (`brand`)",
    "CREATE INDEX IF NOT EXISTS `index_hardware_items_current_price_try` ON `hardware_items` (`current_price_try`)",
    "CREATE INDEX IF NOT EXISTS `index_hardware_items_model` ON `hardware_items` (`model`)",
    "CREATE INDEX IF NOT EXISTS `index_hardware_items_cached_at` ON `hardware_items` (`cached_at`)",
    "CREATE TABLE IF NOT EXISTS `cpu_specs` (`item_id` TEXT NOT NULL, `socket` TEXT NOT NULL, `core_count` INTEGER NOT NULL, `thread_count` INTEGER NOT NULL, `base_clock_mhz` INTEGER NOT NULL, `boost_clock_mhz` INTEGER, `tdp_watts` INTEGER NOT NULL, `has_integrated_graphics` INTEGER NOT NULL, `architecture` TEXT, `l2_cache_mb` REAL, `l3_cache_mb` INTEGER, `process_node` TEXT, `pcie_version` TEXT, `max_memory_speed` TEXT, `unlocked_multiplier` INTEGER, `core_topology` TEXT, `power_limits` TEXT, `igpu_model` TEXT, `l1_cache` TEXT, `die_size_mm2` REAL, `transistors_billion` REAL, `memory_channels` INTEGER, `ecc_support` INTEGER, `pcie_lanes_detail` TEXT, `tj_max_celsius` INTEGER, `instruction_sets` TEXT, `npu_tops` TEXT, `release_date` TEXT, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE INDEX IF NOT EXISTS `index_cpu_specs_socket` ON `cpu_specs` (`socket`)",
    "CREATE TABLE IF NOT EXISTS `gpu_specs` (`item_id` TEXT NOT NULL, `chipset` TEXT NOT NULL, `vram_mb` INTEGER NOT NULL, `memory_type` TEXT NOT NULL, `core_clock_mhz` INTEGER NOT NULL, `boost_clock_mhz` INTEGER, `tdp_watts` INTEGER NOT NULL, `recommended_psu_watts` INTEGER NOT NULL, `bus_width_bits` INTEGER, `length_mm` INTEGER, `power_connectors` TEXT, `display_outputs` TEXT, `slot_width` TEXT, `part_number` TEXT, `architecture` TEXT, `process_node` TEXT, `cuda_cores` INTEGER, `tensor_ai_tops` TEXT, `rt_cores` INTEGER, `memory_speed_gbps` REAL, `bandwidth_gbs` REAL, `tmus` INTEGER, `rops` INTEGER, `l2_cache_mb` REAL, `fp32_tflops` REAL, `pcie_interface` TEXT, `upscaling_tech` TEXT, `media_engines` TEXT, `gpu_die` TEXT, `release_date` TEXT, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `ram_specs` (`item_id` TEXT NOT NULL, `generation` TEXT NOT NULL, `speed_mts` INTEGER NOT NULL, `cas_latency` INTEGER NOT NULL, `capacity_mb` INTEGER NOT NULL, `module_count` INTEGER NOT NULL, `first_word_latency_ns` REAL, `voltage` REAL, `heat_spreader` INTEGER, `rgb` INTEGER, `die_type` TEXT, `timings` TEXT, `profile` TEXT, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE INDEX IF NOT EXISTS `index_ram_specs_generation` ON `ram_specs` (`generation`)",
    "CREATE TABLE IF NOT EXISTS `motherboard_specs` (`item_id` TEXT NOT NULL, `socket` TEXT NOT NULL, `chipset` TEXT NOT NULL, `form_factor` TEXT NOT NULL, `memory_generation` TEXT NOT NULL, `ram_slots` INTEGER NOT NULL, `pcie_generation` TEXT NOT NULL, `max_memory_gb` INTEGER, `m2_slots` INTEGER, `pcie_slot_count` INTEGER, `sata_ports` INTEGER, `has_wifi` INTEGER, `vrm_phases` TEXT, `interface_ports` TEXT, `pcb_layers` TEXT, `m2_slots_detail` TEXT, `memory_speed_oc` TEXT, `network` TEXT, `audio_codec` TEXT, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE INDEX IF NOT EXISTS `index_motherboard_specs_socket` ON `motherboard_specs` (`socket`)",
    "CREATE TABLE IF NOT EXISTS `psu_specs` (`item_id` TEXT NOT NULL, `wattage` INTEGER NOT NULL, `efficiency` TEXT NOT NULL, `is_atx3_compliant` INTEGER NOT NULL, `supports_pcie5_power` INTEGER NOT NULL, `modularity` TEXT, `form_factor` TEXT, `outputs` TEXT, `tier_rating` TEXT, `capacitors` TEXT, `atx_standard` TEXT, `pcie_12vhpwr` TEXT, `eps_connectors` TEXT, `pcie_connectors` TEXT, `sata_connectors` TEXT, `topology` TEXT, `protections` TEXT, `fan_and_size` TEXT, `cybenetics_cert` TEXT, `cybenetics_lambda` TEXT, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `storage_specs` (`item_id` TEXT NOT NULL, `media_type` TEXT NOT NULL, `storage_interface` TEXT NOT NULL, `form_factor` TEXT NOT NULL, `pcie_generation` TEXT, `capacity_mb` INTEGER NOT NULL, `seq_read_mbps` INTEGER NOT NULL, `seq_write_mbps` INTEGER NOT NULL, `nand_type` TEXT, `endurance_tbw` INTEGER, `cache_mb` INTEGER, `controller` TEXT, `dram_cache` TEXT, `random_iops` TEXT, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `cooler_specs` (`item_id` TEXT NOT NULL, `type` TEXT NOT NULL, `radiator_size_mm` INTEGER, `max_cooling_tdp_watts` INTEGER NOT NULL, `supported_sockets` TEXT NOT NULL, `height_mm` INTEGER, `fan_rpm` TEXT, `noise_dba` REAL, `radiator_dimensions` TEXT, `fan_airflow` TEXT, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `case_specs` (`item_id` TEXT NOT NULL, `type` TEXT, `max_gpu_length_mm` INTEGER, `max_cpu_cooler_height_mm` INTEGER, `max_radiator_size_mm` INTEGER, `side_panel` TEXT, `drive_bays` TEXT, `radiator_support` TEXT, `included_fans` TEXT, `front_panel_usb` TEXT, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `laptop_specs` (`item_id` TEXT NOT NULL, `cpu_model` TEXT NOT NULL, `gpu_model` TEXT NOT NULL, `gpu_watt_tgp` TEXT NOT NULL, `mux_switch` TEXT NOT NULL, `ram_spec` TEXT NOT NULL, `storage_spec` TEXT NOT NULL, `display_spec` TEXT NOT NULL, `battery_wh` INTEGER NOT NULL, `weight_kg` REAL NOT NULL, `cooling_tech` TEXT NOT NULL, `gaming_score` INTEGER NOT NULL, `fps_1080p` INTEGER NOT NULL, `fps_1440p` INTEGER NOT NULL, `cpu_cores` TEXT NOT NULL, `cpu_clock_ghz` TEXT NOT NULL, `screen_resolution` TEXT NOT NULL, `refresh_rate_hz` INTEGER NOT NULL, `panel_tech` TEXT NOT NULL, `color_gamut` TEXT NOT NULL, `screen_size_inch` REAL NOT NULL, `ram_slots` TEXT NOT NULL, `storage_slots` TEXT NOT NULL, `charger_watt` INTEGER NOT NULL, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `mouse_specs` (`hardware_id` TEXT NOT NULL, `sensor` TEXT NOT NULL, `max_dpi` INTEGER NOT NULL, `polling_rate_hz` INTEGER NOT NULL, `switch_type` TEXT NOT NULL, `weight_g` INTEGER NOT NULL, `connectivity` TEXT NOT NULL, `battery_hours` INTEGER, `mcu` TEXT NOT NULL, `click_latency_ms` REAL NOT NULL, `shape_type` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `keyboard_specs` (`hardware_id` TEXT NOT NULL, `switch_type` TEXT NOT NULL, `rapid_trigger_supported` INTEGER NOT NULL, `min_actuation_mm` REAL NOT NULL, `polling_rate_hz` INTEGER NOT NULL, `layout` TEXT NOT NULL, `hot_swap` INTEGER NOT NULL, `connectivity` TEXT NOT NULL, `max_actuation_mm` REAL NOT NULL, `case_material` TEXT NOT NULL, `is_hall_effect` INTEGER NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `headset_specs` (`hardware_id` TEXT NOT NULL, `driver_type` TEXT NOT NULL, `driver_size_mm` INTEGER NOT NULL, `impedance_ohm` INTEGER NOT NULL, `frequency_response` TEXT NOT NULL, `dac_amp_required` INTEGER NOT NULL, `mic_type` TEXT NOT NULL, `weight_g` INTEGER NOT NULL, `connectivity` TEXT NOT NULL, `sound_isolation` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `monitor_specs` (`hardware_id` TEXT NOT NULL, `panel_type` TEXT NOT NULL, `screen_size_inch` REAL NOT NULL, `resolution` TEXT NOT NULL, `refresh_rate_hz` INTEGER NOT NULL, `response_speed_gtg_ms` REAL NOT NULL, `ppi` INTEGER NOT NULL, `hdr_certification` TEXT NOT NULL, `bit_depth` TEXT NOT NULL, `response_mprt_ms` REAL, `brightness_sdr_nit` INTEGER NOT NULL, `brightness_hdr_nit` INTEGER NOT NULL, `color_gamut` TEXT NOT NULL, `vrr_support` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `microphone_specs` (`hardware_id` TEXT NOT NULL, `capsule_type` TEXT NOT NULL, `polar_pattern` TEXT NOT NULL, `sample_rate_and_bit` TEXT NOT NULL, `frequency_response` TEXT NOT NULL, `connectivity` TEXT NOT NULL, `max_spl_db` INTEGER NOT NULL, `has_built_in_dsp` INTEGER NOT NULL, `requires_phantom_power` INTEGER NOT NULL, `has_zero_latency_monitoring` INTEGER NOT NULL, `has_pop_filter` INTEGER NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `mousepad_specs` (`hardware_id` TEXT NOT NULL, `surface_type` TEXT NOT NULL, `speed_glide_tier` TEXT NOT NULL, `base_material` TEXT NOT NULL, `dimensions_mm` TEXT NOT NULL, `thickness_mm` REAL NOT NULL, `stitched_edges` INTEGER NOT NULL, `stopping_power_score` INTEGER NOT NULL, `humidity_resistance` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `webcam_specs` (`hardware_id` TEXT NOT NULL, `max_resolution` TEXT NOT NULL, `max_frame_rate_fps` INTEGER NOT NULL, `sensor_model` TEXT NOT NULL, `sensor_size_inch` TEXT NOT NULL, `aperture` TEXT NOT NULL, `field_of_view_deg` INTEGER NOT NULL, `focus_type` TEXT NOT NULL, `has_ai_tracking` INTEGER NOT NULL, `has_hardware_hdr` INTEGER NOT NULL, `connection_type` TEXT NOT NULL, `privacy_shutter` INTEGER NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `gaming_chair_specs` (`hardware_id` TEXT NOT NULL, `chair_type` TEXT NOT NULL, `upholstery_material` TEXT NOT NULL, `lumbar_support_type` TEXT NOT NULL, `armrest_type` TEXT NOT NULL, `gas_lift_class` TEXT NOT NULL, `max_weight_capacity_kg` INTEGER NOT NULL, `recommended_height_range` TEXT NOT NULL, `recline_angle_deg` INTEGER NOT NULL, `tilt_mechanism` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `case_fan_specs` (`hardware_id` TEXT NOT NULL, `fan_size_mm` INTEGER NOT NULL, `min_rpm` INTEGER NOT NULL, `max_rpm` INTEGER NOT NULL, `max_airflow_cfm` REAL NOT NULL, `static_pressure_mmh2o` REAL NOT NULL, `noise_level_dba` REAL NOT NULL, `bearing_type` TEXT NOT NULL, `pwm_pins` INTEGER NOT NULL, `is_daisy_chain` INTEGER NOT NULL, `argb_type` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `ups_specs` (`hardware_id` TEXT NOT NULL, `va_rating` INTEGER NOT NULL, `watt_capacity` INTEGER NOT NULL, `topology` TEXT NOT NULL, `sinewave_type` TEXT NOT NULL, `battery_specs` TEXT NOT NULL, `runtime_min_full_load` INTEGER NOT NULL, `runtime_min_half_load` INTEGER NOT NULL, `outlet_count` INTEGER NOT NULL, `has_usb_interface` INTEGER NOT NULL, `surge_protection_joules` INTEGER NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `gaming_desk_specs` (`hardware_id` TEXT NOT NULL, `desk_width_cm` INTEGER NOT NULL, `desk_depth_cm` INTEGER NOT NULL, `is_motorized_height` INTEGER NOT NULL, `min_height_cm` INTEGER NOT NULL, `max_height_cm` INTEGER NOT NULL, `max_load_kg` INTEGER NOT NULL, `surface_finish` TEXT NOT NULL, `has_cable_management` INTEGER NOT NULL, `integrated_mousepad` INTEGER NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `gaming_lighting_specs` (`hardware_id` TEXT NOT NULL, `lighting_form_factor` TEXT NOT NULL, `max_lumens` INTEGER NOT NULL, `color_temp_kelvin` TEXT NOT NULL, `cri_score` INTEGER NOT NULL, `has_screen_mirroring` INTEGER NOT NULL, `supported_ecosystems` TEXT NOT NULL, `connection_type` TEXT NOT NULL, `power_source` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `smartwatch_specs` (`hardware_id` TEXT NOT NULL, `screen_size_inch` REAL NOT NULL, `screen_panel` TEXT NOT NULL, `peak_brightness_nits` INTEGER NOT NULL, `battery_life_days` INTEGER NOT NULL, `water_resistance_atm` INTEGER NOT NULL, `os_name` TEXT NOT NULL, `sensors_list` TEXT NOT NULL, `has_cellular_lte` INTEGER NOT NULL, `case_material` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `router_specs` (`hardware_id` TEXT NOT NULL, `wifi_standard` TEXT NOT NULL, `speed_class` TEXT NOT NULL, `bands` TEXT NOT NULL, `ethernet_ports` TEXT NOT NULL, `has_gaming_qos` INTEGER NOT NULL, `has_sqm_bufferbloat` INTEGER NOT NULL, `processor_and_ram` TEXT NOT NULL, `usb_sharing_ports` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `modem_specs` (`hardware_id` TEXT NOT NULL, `vdsl_profile` TEXT NOT NULL, `fiber_wan_support` INTEGER NOT NULL, `wifi_standard` TEXT NOT NULL, `lan_ports` TEXT NOT NULL, `voip_ports` TEXT NOT NULL, `chipset` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `access_point_specs` (`hardware_id` TEXT NOT NULL, `form_factor` TEXT NOT NULL, `wifi_standard` TEXT NOT NULL, `poe_support` TEXT NOT NULL, `max_clients` INTEGER NOT NULL, `mu_mimo_streams` TEXT NOT NULL, `uplink_port` TEXT NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `range_extender_specs` (`hardware_id` TEXT NOT NULL, `extender_type` TEXT NOT NULL, `wifi_standard` TEXT NOT NULL, `dedicated_backhaul` TEXT NOT NULL, `mesh_protocol` TEXT NOT NULL, `ethernet_ports` TEXT NOT NULL, `coverage_area_m2` INTEGER NOT NULL, PRIMARY KEY(`hardware_id`), FOREIGN KEY(`hardware_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `phone_specs` (`item_id` TEXT NOT NULL, `chipset` TEXT NOT NULL, `cpu_arch` TEXT NOT NULL, `gpu_model` TEXT NOT NULL, `process_node_nm` INTEGER NOT NULL, `ram_gb` INTEGER NOT NULL, `ram_type` TEXT NOT NULL, `storage_gb` INTEGER NOT NULL, `storage_type` TEXT NOT NULL, `has_micro_sd` INTEGER NOT NULL, `antutu_score` INTEGER, `geekbench_single` INTEGER, `geekbench_multi` INTEGER, `stability_percent` INTEGER, `dxomark_camera` INTEGER, `dxomark_selfie` INTEGER, `dxomark_display` INTEGER, `dxomark_battery` INTEGER, `dxomark_audio` INTEGER, `display_size_inch` REAL NOT NULL, `display_tech` TEXT NOT NULL, `resolution` TEXT NOT NULL, `ppi` INTEGER NOT NULL, `refresh_rate_hz` INTEGER NOT NULL, `peak_brightness_nit` INTEGER NOT NULL, `glass_protection` TEXT NOT NULL, `hdr_support` TEXT NOT NULL, `pwm_dimming_hz` INTEGER, `main_camera_mp` INTEGER NOT NULL, `main_sensor_size` TEXT NOT NULL, `main_aperture` TEXT NOT NULL, `has_ois` INTEGER NOT NULL, `ois_type` TEXT NOT NULL, `telephoto_mp` INTEGER, `optical_zoom_x` REAL, `ultra_wide_mp` INTEGER, `video_resolution` TEXT NOT NULL, `selfie_camera_mp` INTEGER NOT NULL, `battery_mah` INTEGER NOT NULL, `wired_charging_watt` INTEGER NOT NULL, `wireless_charging_watt` INTEGER, `has_reverse_charging` INTEGER NOT NULL, `charging_time_to_full_min` INTEGER, `weight_grams` INTEGER NOT NULL, `thickness_mm` REAL NOT NULL, `frame_material` TEXT NOT NULL, `water_resistance` TEXT NOT NULL, `has_stylus` INTEGER NOT NULL, `usb_speed` TEXT, `wifi_bluetooth` TEXT, `os_name` TEXT NOT NULL, `update_years` INTEGER NOT NULL, `pubg_fps_support` TEXT NOT NULL, `genshin_stability_score` TEXT NOT NULL, `variants` TEXT NOT NULL, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `tablet_specs` (`item_id` TEXT NOT NULL, `chipset` TEXT NOT NULL, `cpu_arch` TEXT NOT NULL, `gpu_model` TEXT NOT NULL, `process_node_nm` INTEGER NOT NULL, `ram_gb` INTEGER NOT NULL, `storage_gb` INTEGER NOT NULL, `storage_type` TEXT NOT NULL, `has_micro_sd` INTEGER NOT NULL, `antutu_score` INTEGER, `geekbench_single` INTEGER, `geekbench_multi` INTEGER, `stability_percent` INTEGER, `display_size_inch` REAL NOT NULL, `display_tech` TEXT NOT NULL, `resolution` TEXT NOT NULL, `ppi` INTEGER NOT NULL, `refresh_rate_hz` INTEGER NOT NULL, `peak_brightness_nit` INTEGER NOT NULL, `stylus_support` TEXT NOT NULL, `aspect_ratio` TEXT, `main_camera_mp` INTEGER NOT NULL, `front_camera_mp` INTEGER NOT NULL, `speaker_count` INTEGER NOT NULL, `has_dolby_atmos` INTEGER NOT NULL, `battery_mah` INTEGER NOT NULL, `wired_charging_watt` INTEGER NOT NULL, `wireless_charging_watt` INTEGER, `weight_grams` INTEGER NOT NULL, `thickness_mm` REAL NOT NULL, `body_material` TEXT NOT NULL, `os_name` TEXT NOT NULL, `update_years` INTEGER NOT NULL, `cellular_option` TEXT NOT NULL, `usb_speed` TEXT, `external_display_support` TEXT, `variants` TEXT NOT NULL, PRIMARY KEY(`item_id`), FOREIGN KEY(`item_id`) REFERENCES `hardware_items`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
    "CREATE TABLE IF NOT EXISTS `user_price_overrides` (`hardware_id` TEXT NOT NULL, `custom_price_try` REAL NOT NULL, `note` TEXT, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`hardware_id`))",
    "CREATE TABLE IF NOT EXISTS `user_sessions` (`id` TEXT NOT NULL, `user_id` TEXT NOT NULL, `email` TEXT NOT NULL, `username` TEXT NOT NULL, `birth_date_iso` TEXT NOT NULL, `gender` TEXT NOT NULL, `spirit_hardware` TEXT NOT NULL, `created_at_epoch_ms` INTEGER NOT NULL, PRIMARY KEY(`id`))",
    "CREATE TABLE IF NOT EXISTS `app_settings` (`key` TEXT NOT NULL, `value` TEXT NOT NULL, PRIMARY KEY(`key`))",
    "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)",
    f"INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, '{ROOM_IDENTITY_HASH}')",
]


def _load_json(filename: str, default: Any = None) -> Any:
    path = os.path.join(CATALOG_DIR, filename)
    if not os.path.exists(path):
        return [] if default is None else default
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def _as_int(val: Any) -> int | None:
    if val is None or isinstance(val, bool):
        return None
    if isinstance(val, (int, float)):
        return int(round(val))
    m = re.search(r"-?\d+(?:\.\d+)?", str(val).replace(",", ""))
    return int(round(float(m.group(0)))) if m else None


def _as_float(val: Any) -> float | None:
    if val is None or isinstance(val, bool):
        return None
    if isinstance(val, (int, float)):
        return float(val)
    m = re.search(r"-?\d+(?:\.\d+)?", str(val).replace(",", ""))
    return float(m.group(0)) if m else None


def _clean_str(val: Any) -> str | None:
    if val is None:
        return None
    s = str(val).strip()
    return s if s else None


def _extract_host(https_url: str) -> str | None:
    parsed = urlparse(https_url)
    if "@" in parsed.netloc or not parsed.netloc:
        return None
    host = parsed.hostname
    if not host or "." not in host:
        return None
    if not re.match(r"^[a-z0-9.\-]+$", host):
        return None
    return host.lower()


def _sanitize_image_url(raw_url: Any) -> str | None:
    if not raw_url or not isinstance(raw_url, str):
        return None
    trimmed = raw_url.strip()
    if not trimmed or len(trimmed) > 2048 or any(ord(c) < 32 for c in trimmed):
        return None
    lower = trimmed.lower()
    if any(lower.startswith(s) for s in FORBIDDEN_SCHEMES):
        return None
    if any(
        bad in lower
        for bad in (
            ".svg", "svg%20", "<svg", "placeholder", "blank.gif", "no-image", "no_image",
            "roundnew", "logo_2020", "/static/stores/", "/stores/amazon-", "iconic-features",
            "static0.gamerantimages.com", "images.unsplash.com", "itopya-logo", "5-year-qr",
            "_ihs_studio", "_reference.", "_bottom.", "generic_cpu",
        )
    ):
        return None
    if lower.startswith("https://"):
        norm = trimmed
    elif lower.startswith("http://"):
        norm = "https://" + trimmed[7:]
    else:
        return None
    host = _extract_host(norm)
    if not host or host == "localhost" or host.startswith(("127.", "10.", "192.168.", "169.254.")) or host.endswith((".local", ".internal")):
        return None
    return norm


def _validate_store_url(raw_url: Any) -> str | None:
    if not raw_url or not isinstance(raw_url, str):
        return None
    trimmed = raw_url.strip()
    if len(trimmed) > 2048 or any(ord(c) < 32 for c in trimmed):
        return None
    lower = trimmed.lower()
    if not lower.startswith("https://") or any(lower.startswith(s) for s in FORBIDDEN_SCHEMES):
        return None
    host = _extract_host(trimmed)
    if not host:
        return None
    if any(host == d or host.endswith("." + d) for d in ALLOWED_STORE_DOMAINS):
        return trimmed
    return None


def _sanitize_try_price(val: Any) -> float | None:
    f = _as_float(val)
    if f is None or f != f or f < MIN_VALID_TRY_PRICE or f > MAX_VALID_TRY_PRICE:
        return None
    return f


def _gen_hardware_id(category: str, name: str) -> str:
    sanitized = re.sub(r"[^a-z0-9]", "_", name.lower())
    sanitized = re.sub(r"_+", "_", sanitized).strip("_")
    return f"{category.lower()}_{sanitized}"


def _extract_brand_and_model(full_name: str) -> tuple[str, str]:
    trimmed = full_name.strip()
    lower = trimmed.lower()
    for kb in KNOWN_BRANDS:
        if lower.startswith(kb.lower()):
            brand = trimmed[:len(kb)].strip()
            model = trimmed[len(kb):].strip().lstrip(" _-.") or brand
            return brand, model
    if " " in trimmed:
        idx = trimmed.index(" ")
        return trimmed[:idx], trimmed[idx + 1:].strip()
    return "Generic", trimmed


def _is_stock_code(text: str) -> bool:
    t = text.strip()
    if not t:
        return False
    if all(ch.isdigit() or ch in "-_/" for ch in t):
        return True
    if re.match(r"^[0-9A-Z]{3,}(-[0-9A-Z]+)+$", t):
        return True
    return False


def _resolve_default_image(category: str, model: str, brand: str) -> str | None:
    return None


def _deduce_cpu_socket(dto: dict[str, Any], raw_name: str) -> str:
    if _clean_str(dto.get("socket")):
        return str(dto["socket"]).strip()
    arch = str(dto.get("microarchitecture") or "")
    u = raw_name.upper()
    if any(k in arch.upper() for k in ("ZEN 5", "ZEN 4")) or any(
        k in u for k in ("9950X", "9900X", "9800X", "9700X", "9600X", "7950X", "7900X", "7900", "7800X", "7700X", "7700", "7600X", "7600", "7500F", "8700G", "8600G", "8500G", "AM5")
    ):
        return "AM5"
    if "ZEN" in arch.upper() or any(
        k in u for k in ("5950X", "5900X", "5800X", "5700X", "5700", "5600X", "5600", "5500", "4500", "3950X", "3900X", "3800X", "3700X", "3600", "2600", "1600", "AM4")
    ):
        return "AM4"
    if any(k in u for k in ("CORE ULTRA", "285K", "275K", "270K", "265K", "255K", "250K", "245K", "LGA1851", "ARROW LAKE")):
        return "LGA1851"
    if any(k in u for k in ("14900", "14700", "14600", "14400", "14100", "13900", "13700", "13600", "13400", "13100", "12900", "12700", "12600", "12400", "12100", "LGA1700")):
        return "LGA1700"
    if any(k in u for k in ("11900", "11700", "11400", "10900", "10700", "10400", "10100", "LGA1200")):
        return "LGA1200"
    if any(k in u for k in ("A4-", "A6-", "A8-", "A10-")):
        return "FM2"
    if "FX-" in u or "PHENOM" in u:
        return "AM3+"
    return "Belirtilmedi"


def _is_modern_cpu(name: str, socket: str) -> bool:
    u = name.upper()
    s = socket.upper()
    if any(b in u for b in ("A4-", "A6-", "A8-", "A10-", "A12-", "ATHLON II", "PHENOM", "SEMPRON", "OPTERON", "FX-", "PENTIUM", "CELERON", "CORE 2", "XEON", "CORE DUO", "ATOM", "EMBEDDED")):
        return False
    if any(b in s for b in ("AM1", "AM2", "AM3", "FM1", "FM2", "775", "1156", "1155", "1150", "1151")):
        return False
    if re.search(r"\b(?:\d{4}(?:U|HS|HX|H|V)|2[23568][68]V)\b", u) or "RYZEN Z1" in u or "RYZEN AI" in u:
        return False
    is_modern_amd = any(k in u for k in ("RYZEN", "THREADRIPPER", "3000G", "200GE")) and (
        any(x in s for x in ("AM4", "AM5", "TR4", "TRX4", "STR5", "WRX8")) or s == "BELIRTILMEDI"
    )
    is_modern_intel = any(
        k in u
        for k in (
            "CORE ULTRA", "290K", "285K", "285", "275K", "265K", "265", "255K", "245K", "245", "235", "225",
            "14900", "14700", "14600", "14500", "14400", "14100",
            "13900", "13700", "13600", "13500", "13400", "13100",
            "12900", "12700", "12600", "12500", "12400", "12100",
            "11900", "11700", "11600", "11500", "11400",
            "10900", "10700", "10600", "10500", "10400", "10100", "10105",
        )
    ) and (any(x in s for x in ("1200", "1700", "1851")) or s == "BELIRTILMEDI")
    return is_modern_amd or is_modern_intel


def _is_modern_gpu(name: str, chipset: str) -> bool:
    u = f"{name} {chipset}".upper()
    if any(b in u for b in ("GTX", "GT 7", "GT 6", "GT 1030", "RX 580", "RX 570", "RX 560", "RX 550", "RX 480", "RX 470", "R9", "R7", "HD 7", "HD 6", "HD 5")):
        return False
    is_rtx = "RTX" in u and any(x in u for x in ("20", "30", "40", "50"))
    is_rx = ("RX " in u or "RADEON " in u) and any(
        x in u for x in ("5700", "5600", "5500", "6600", "6650", "6700", "6750", "6800", "6900", "6950", "7600", "7700", "7800", "7900", "9060", "9070", "9080", "9000")
    )
    return is_rtx or is_rx or ("ARC" in u)


def _deduce_mobo_socket(name: str) -> str:
    u = name.upper()
    if any(k in u for k in ("B650", "X670", "A620", "B850", "B840", "X870", "AM5")):
        return "AM5"
    if any(k in u for k in ("B550", "X570", "A520", "B450", "X470", "A320", "B350", "X370", "AM4")):
        return "AM4"
    if any(k in u for k in ("Z890", "B860", "H810", "LGA1851", "1851")):
        return "LGA1851"
    if any(k in u for k in ("Z790", "B760", "H770", "Z690", "B660", "H610", "LGA1700")):
        return "LGA1700"
    if any(k in u for k in ("Z590", "B560", "H570", "H510", "Z490", "B460", "H470", "H410", "LGA1200")):
        return "LGA1200"
    return "Belirtilmedi"


def _is_modern_mobo(name: str, socket: str) -> bool:
    u = name.upper()
    s = socket.upper()
    if any(b in u for b in ("AM3", "FM2", "FM1", "H81", "H61 ", "H61-", "H61M", "B85", "Z97", "H110", "B250", "Z270", "Z370", "H310", "B360", "B365", "Z390")):
        return False
    if any(b in s for b in ("AM3", "FM2", "FM1", "1155", "1150", "1151", "775")):
        return False
    return any(g in s for g in ("AM4", "AM5", "1200", "1700", "1851", "STRX4", "STR5"))


def _deduce_mobo_chipset(name: str) -> str:
    chipsets = (
        "X870E", "X870", "B850", "B840", "X670E", "X670", "B650E", "B650", "A620",
        "X570", "B550", "A520", "B450", "A320", "TRX40", "WRX80", "TRX50",
        "Z890", "B860", "H810", "Z790", "B760", "H770", "Z690", "B660", "H610",
        "Z590", "B560", "H570", "H510", "Z490", "B460", "H410",
    )
    u = name.upper()
    for c in chipsets:
        if c in u:
            return c
    return "Veri Bulunamadı"


def _normalize_gpu_chipset_key(chipset: str, model: str = "") -> str | None:
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


def _ensure_game_fps_keys(games: dict[str, Any] | None) -> dict[str, Any]:
    if not isinstance(games, dict):
        return {}
    out: dict[str, Any] = {}
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


def _serialize_item_enrichment(
    item_id: str,
    category: str,
    brand: str,
    model: str,
    dto_img: str | None,
    dto_imgs: list[str] | None,
    dto_current_try: float | None,
    dto_lowest_try: float | None,
    dto_highest_try: float | None,
    dto_offers: list[dict[str, Any]] | None,
    dto_history: list[dict[str, Any]] | None,
    prices_tr: dict[str, Any],
    benchmarks: dict[str, Any],
    legacy_images: dict[str, str],
) -> tuple[str | None, list[str], float | None, float | None, float | None, str | None, str | None, int | None, str | None, str | None, int | None, str | None, str | None, str | None]:
    p = prices_tr.get(item_id) or {}
    raw_img = dto_img or (dto_imgs[0] if dto_imgs else None) or p.get("imageUrl") or legacy_images.get(item_id)
    resolved_img = _sanitize_image_url(raw_img) or _resolve_default_image(category, model, brand) or _sanitize_image_url(legacy_images.get(item_id))

    sanitized_list: list[str] = []
    for u in (dto_imgs or []):
        su = _sanitize_image_url(u)
        if su and su not in sanitized_list:
            sanitized_list.append(su)
    if not sanitized_list and resolved_img:
        sanitized_list = [resolved_img]

    cur_try = _sanitize_try_price(p.get("currentPriceTry") if p.get("currentPriceTry") is not None else dto_current_try)
    low_try = _sanitize_try_price(p.get("lowestPriceTry") if p.get("lowestPriceTry") is not None else dto_lowest_try) or cur_try
    high_try = _sanitize_try_price(p.get("highestPriceTry") if p.get("highestPriceTry") is not None else dto_highest_try) or cur_try

    raw_offers = p.get("offers") if p.get("offers") is not None else dto_offers
    clean_offers = []
    if isinstance(raw_offers, list):
        for off in raw_offers:
            if not isinstance(off, dict):
                continue
            vp = _sanitize_try_price(off.get("priceTry"))
            if vp is None:
                continue
            clean_offers.append({
                "storeName": off.get("storeName") or "",
                "priceTry": vp,
                "url": _validate_store_url(off.get("url")) or "",
            })
        clean_offers.sort(key=lambda o: o["priceTry"])
    offers_json = json.dumps(clean_offers, ensure_ascii=False) if raw_offers is not None else None
    if cur_try is None and clean_offers:
        cur_try = clean_offers[0]["priceTry"]

    raw_hist = p.get("priceHistory") if p.get("priceHistory") is not None else dto_history
    clean_hist = []
    seen_dates = set()
    if isinstance(raw_hist, list):
        for pt in raw_hist:
            if not isinstance(pt, dict):
                continue
            ep = _sanitize_try_price(pt.get("priceTry") if pt.get("priceTry") is not None else pt.get("price"))
            if ep is None:
                continue
            dkey = str(pt.get("date") or "")[:10]
            if dkey in seen_dates:
                continue
            seen_dates.add(dkey)
            clean_hist.append({"date": str(pt.get("date") or ""), "priceTry": ep})
        clean_hist.sort(key=lambda x: x["date"])
    hist_json = json.dumps(clean_hist, ensure_ascii=False) if raw_hist is not None else None

    b_score = b_tier = b_suite = b_gidx = cpu_syn_json = gpu_syn_json = game_fps_json = None
    if category in ("CPU", "GPU"):
        sec = benchmarks.get("cpus" if category == "CPU" else "gpus") or {}
        b = sec.get(item_id)
        if not b and category == "GPU":
            gkey = _normalize_gpu_chipset_key(model, model)
            if gkey:
                b = sec.get(f"gpu_{gkey}") or sec.get(gkey)
        if b:
            b_score = b.get("score")
            b_tier = b.get("tier") or "A-Tier"
            b_suite = "PassMark & Cinebench / Geekbench Çoklu Test" if category == "CPU" else "3DMark & PassMark G3D Çoklu Test"
            rel = b.get("gaming_relative_index") if category == "CPU" else b.get("relative_performance_pct")
            b_gidx = int(round(rel * 100)) if rel is not None else b_score
            syn = b.get("synthetic")
            if syn:
                if category == "CPU":
                    cpu_syn_json = json.dumps(syn, ensure_ascii=False)
                else:
                    gpu_syn_json = json.dumps(syn, ensure_ascii=False)
            gf = _ensure_game_fps_keys(b.get("games"))
            if gf:
                game_fps_json = json.dumps(gf, ensure_ascii=False)

    return (
        resolved_img, sanitized_list, cur_try, low_try, high_try,
        offers_json, hist_json, b_score, b_tier, b_suite, b_gidx,
        cpu_syn_json, gpu_syn_json, game_fps_json,
    )


def _insert_hardware_item(
    cursor: sqlite3.Cursor,
    item_id: str,
    brand: str,
    model: str,
    category: str,
    price_minor: int | None,
    currency_code: str | None,
    now_ms: int,
    enrichment: tuple,
    extra_specs: dict[str, Any] | None,
    source_prov: list[str] | None,
    spec_verified_at: str | None,
    override_bscore: int | None = None,
    override_btier: str | None = None,
    override_bgidx: int | None = None,
) -> None:
    (
        img, imgs, cur_try, low_try, high_try,
        offers_json, hist_json, b_score, b_tier, b_suite, b_gidx,
        cpu_syn_json, gpu_syn_json, game_fps_json,
    ) = enrichment

    extra_clean = {str(k): str(v) for k, v in (extra_specs or {}).items() if v is not None and str(v).strip()}
    prov_clean = [str(s) for s in (source_prov or []) if s]

    cursor.execute(
        """
        INSERT OR REPLACE INTO `hardware_items` (
            `id`, `brand`, `model`, `category`, `price_minor`, `currency_code`, `cached_at`,
            `current_price_try`, `lowest_price_try`, `highest_price_try`, `offers`, `price_history`,
            `benchmark_score`, `benchmark_tier`, `benchmark_test_suite`, `benchmark_gaming_index`,
            `cpu_synthetic`, `gpu_synthetic`, `game_fps`, `image_url`, `images`,
            `extra_specs`, `source_provenance`, `spec_verified_at`
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            item_id, brand, model, category, price_minor, currency_code, now_ms,
            cur_try, low_try, high_try, offers_json, hist_json,
            override_bscore if override_bscore is not None else b_score,
            override_btier if override_btier is not None else b_tier,
            b_suite,
            override_bgidx if override_bgidx is not None else b_gidx,
            cpu_syn_json, gpu_syn_json, game_fps_json,
            img, json.dumps(imgs, ensure_ascii=False),
            json.dumps(extra_clean, ensure_ascii=False) if extra_clean else None,
            json.dumps(prov_clean, ensure_ascii=False) if prov_clean else None,
            _clean_str(spec_verified_at),
        ),
    )


def populate_all_catalogs(cursor: sqlite3.Cursor, legacy_images: dict[str, str]) -> None:
    now_ms = int(time.time() * 1000)
    prices_tr = _load_json("prices_tr.json", {})
    benchmarks = _load_json("hardware_benchmarks.json", {"cpus": {}, "gpus": {}})

    # 1. CPUs
    seen_cpu = set()
    for dto in _load_json("cpu.json", []):
        raw_name = _clean_str(dto.get("name"))
        if not raw_name:
            continue
        item_id = _gen_hardware_id("CPU", raw_name)
        if item_id in seen_cpu:
            continue
        socket = _deduce_cpu_socket(dto, raw_name)
        if not _is_modern_cpu(raw_name, socket):
            continue
        seen_cpu.add(item_id)
        brand, model = _extract_brand_and_model(raw_name)
        price_val = _as_float(dto.get("price"))
        price_minor = max(0, int(round(price_val * 100))) if price_val is not None else None
        cores = max(1, _as_int(dto.get("core_count") or dto.get("cores")) or 6)
        threads = max(cores, _as_int(dto.get("threads")) or cores * 2)
        base_mhz = max(500, int(round((_as_float(dto.get("core_clock") or dto.get("base_clock")) or 3.5) * 1000)))
        boost_raw = _as_float(dto.get("boost_clock")) or _as_float(dto.get("core_clock")) or 4.0
        boost_mhz = max(base_mhz, int(round(boost_raw * 1000)))
        tdp = max(15, _as_int(dto.get("tdp")) or 65)
        gfx = _clean_str(dto.get("graphics") or dto.get("integrated_gpu"))
        has_igpu = 1 if (gfx and gfx.lower() != "none") else 0

        enr = _serialize_item_enrichment(
            item_id, "CPU", brand, model, dto.get("imageUrl") or dto.get("image_url"),
            dto.get("images"), None, None, None, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, model, "CPU", price_minor, "USD" if price_minor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at")
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `cpu_specs` (
                `item_id`, `socket`, `core_count`, `thread_count`, `base_clock_mhz`, `boost_clock_mhz`,
                `tdp_watts`, `has_integrated_graphics`, `architecture`, `l2_cache_mb`, `l3_cache_mb`,
                `process_node`, `pcie_version`, `max_memory_speed`, `unlocked_multiplier`, `core_topology`,
                `power_limits`, `igpu_model`, `l1_cache`, `die_size_mm2`, `transistors_billion`,
                `memory_channels`, `ecc_support`, `pcie_lanes_detail`, `tj_max_celsius`,
                `instruction_sets`, `npu_tops`, `release_date`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                item_id, socket, cores, threads, base_mhz, boost_mhz,
                tdp, has_igpu, _clean_str(dto.get("microarchitecture") or dto.get("architecture")),
                _as_float(dto.get("l2_cache")), _as_int(dto.get("l3_cache") or dto.get("l3_cache_mb")),
                _clean_str(dto.get("process_node")), _clean_str(dto.get("pcie_version")),
                _clean_str(dto.get("max_memory_speed") or dto.get("memory_support")),
                None if dto.get("unlocked_multiplier") is None else (1 if dto.get("unlocked_multiplier") else 0),
                _clean_str(dto.get("core_topology")), _clean_str(dto.get("power_limits")), _clean_str(dto.get("igpu_model")),
                _clean_str(dto.get("l1_cache")), _as_float(dto.get("die_size_mm2")),
                _as_float(dto.get("transistors_billion")), _as_int(dto.get("memory_channels")),
                None if dto.get("ecc_support") is None else (1 if dto.get("ecc_support") else 0),
                _clean_str(dto.get("pcie_lanes_detail")), _as_int(dto.get("tj_max_celsius")),
                _clean_str(dto.get("instruction_sets")), _clean_str(dto.get("npu_tops")),
                _clean_str(dto.get("release_date")),
            ),
        )
    print(f"  [OK] {len(seen_cpu)} CPU")

    # 2. GPUs
    seen_gpu = set()
    for dto in _load_json("video-card.json", []):
        raw_name = _clean_str(dto.get("name"))
        if not raw_name:
            continue
        item_id = _gen_hardware_id("GPU", raw_name)
        if item_id in seen_gpu:
            continue
        raw_chipset = _clean_str(dto.get("chipset"))
        chipset = raw_chipset or raw_name
        if not _is_modern_gpu(raw_name, chipset):
            continue
        seen_gpu.add(item_id)
        brand, extracted_model = _extract_brand_and_model(raw_name)
        if brand.lower() == "generic":
            cl = chipset.lower()
            if "radeon" in cl:
                brand = "AMD"
            elif "geforce" in cl or "rtx" in cl or "gtx" in cl:
                brand = "NVIDIA"
            elif "arc" in cl:
                brand = "Intel"
        is_sku = _is_stock_code(extracted_model)
        part_num = extracted_model if is_sku else None
        chip_core = re.sub(r"^(?:GeForce|Radeon|Intel Arc|Intel)\s+", "", raw_chipset or "", flags=re.I).strip() if raw_chipset else ""
        if is_sku and raw_chipset:
            final_model = raw_chipset[len(brand):].strip() or raw_chipset if raw_chipset.lower().startswith(brand.lower()) else raw_chipset
        elif (
            raw_chipset
            and raw_chipset.lower() not in extracted_model.lower()
            and extracted_model.lower() not in raw_chipset.lower()
            and (not chip_core or chip_core.lower() not in extracted_model.lower())
        ):
            final_model = f"{raw_chipset} {extracted_model}"
        else:
            final_model = extracted_model

        price_val = _as_float(dto.get("price"))
        price_minor = max(0, int(round(price_val * 100))) if price_val is not None else None
        mem_gb = _as_int(dto.get("memory"))
        vram_mb = (mem_gb * 1024) if mem_gb is not None else 0

        enr = _serialize_item_enrichment(
            item_id, "GPU", brand, final_model, dto.get("imageUrl") or dto.get("image_url"),
            dto.get("images"), None, None, None, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, final_model, "GPU", price_minor, "USD" if price_minor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at")
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `gpu_specs` (
                `item_id`, `chipset`, `vram_mb`, `memory_type`, `core_clock_mhz`, `boost_clock_mhz`,
                `tdp_watts`, `recommended_psu_watts`, `bus_width_bits`, `length_mm`, `power_connectors`,
                `display_outputs`, `slot_width`, `part_number`, `architecture`, `process_node`,
                `cuda_cores`, `tensor_ai_tops`, `rt_cores`, `memory_speed_gbps`, `bandwidth_gbs`,
                `tmus`, `rops`, `l2_cache_mb`, `fp32_tflops`, `pcie_interface`, `upscaling_tech`,
                `media_engines`, `gpu_die`, `release_date`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                item_id, chipset, vram_mb, _clean_str(dto.get("memory_type")) or "",
                _as_int(dto.get("core_clock")) or 0, _as_int(dto.get("boost_clock")),
                _as_int(dto.get("tdp")) or 0, _as_int(dto.get("recommended_psu")) or 0,
                _as_int(dto.get("bus_width")), _as_int(dto.get("length")),
                _clean_str(dto.get("power_connectors") or dto.get("external_power")),
                _clean_str(dto.get("display_outputs")), _clean_str(dto.get("slot_width")), part_num,
                _clean_str(dto.get("architecture")), _clean_str(dto.get("process_node")),
                _as_int(dto.get("cuda_cores")), _clean_str(dto.get("tensor_ai_tops")),
                _as_int(dto.get("rt_cores")), _as_float(dto.get("memory_speed")),
                _as_float(dto.get("bandwidth_gbs")),
                _as_int(dto.get("tmus")), _as_int(dto.get("rops")),
                _as_float(dto.get("l2_cache_mb")), _as_float(dto.get("fp32_tflops")),
                _clean_str(dto.get("pcie_interface")), _clean_str(dto.get("upscaling_tech")),
                _clean_str(dto.get("media_engines")), _clean_str(dto.get("gpu_die")),
                _clean_str(dto.get("release_date")),
            ),
        )
    print(f"  [OK] {len(seen_gpu)} GPU")

    # 3. RAM
    seen_ram = set()
    for dto in _load_json("memory.json", []):
        raw_name = _clean_str(dto.get("name"))
        if not raw_name:
            continue
        item_id = _gen_hardware_id("RAM", raw_name)
        if item_id in seen_ram:
            continue
        seen_ram.add(item_id)
        brand, model = _extract_brand_and_model(raw_name)
        price_val = _as_float(dto.get("price"))
        price_minor = max(0, int(round(price_val * 100))) if price_val is not None else None
        sp = dto.get("speed") or []
        is_ddr3 = (sp and sp[0] == 3) or "DDR3" in raw_name.upper()
        is_ddr5 = (sp and sp[0] == 5) or "DDR5" in raw_name.upper()
        gen = "DDR3" if is_ddr3 else ("DDR5" if is_ddr5 else "DDR4")
        if len(sp) >= 2:
            speed_mts = max(800, int(sp[1]))
        elif len(sp) == 1:
            speed_mts = max(800, int(sp[0]))
        else:
            speed_mts = 1600 if is_ddr3 else (6000 if is_ddr5 else 3200)
        cl = max(7, _as_int(dto.get("cas_latency")) or (11 if is_ddr3 else (36 if is_ddr5 else 16)))
        mods = dto.get("modules") or []
        mod_cnt = max(1, int(mods[0])) if len(mods) >= 1 and mods[0] else 2
        per_mod = max(2, int(mods[1])) if len(mods) >= 2 and mods[1] else 16
        cap_mb = max(2048, mod_cnt * per_mod * 1024)

        enr = _serialize_item_enrichment(
            item_id, "RAM", brand, model, dto.get("imageUrl"), dto.get("images"),
            None, None, None, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, model, "RAM", price_minor, "USD" if price_minor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at")
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `ram_specs` (
                `item_id`, `generation`, `speed_mts`, `cas_latency`, `capacity_mb`, `module_count`,
                `first_word_latency_ns`, `voltage`, `heat_spreader`, `rgb`, `die_type`, `timings`, `profile`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                item_id, gen, speed_mts, cl, cap_mb, mod_cnt,
                _as_float(dto.get("first_word_latency")), _as_float(dto.get("voltage")),
                None if dto.get("heat_spreader") is None else (1 if dto.get("heat_spreader") else 0),
                None if dto.get("rgb") is None else (1 if dto.get("rgb") else 0),
                _clean_str(dto.get("die_type")), _clean_str(dto.get("timings")), _clean_str(dto.get("profile")),
            ),
        )
    print(f"  [OK] {len(seen_ram)} RAM")

    # 4. Motherboards
    seen_mb = set()
    for dto in _load_json("motherboard.json", []):
        raw_name = _clean_str(dto.get("name"))
        if not raw_name:
            continue
        item_id = _gen_hardware_id("MOTHERBOARD", raw_name)
        if item_id in seen_mb:
            continue
        socket = _clean_str(dto.get("socket")) or _deduce_mobo_socket(raw_name)
        if not _is_modern_mobo(raw_name, socket):
            continue
        seen_mb.add(item_id)
        brand, model = _extract_brand_and_model(raw_name)
        price_val = _as_float(dto.get("price"))
        price_minor = max(0, int(round(price_val * 100))) if price_val is not None else None
        chipset = _clean_str(dto.get("chipset")) or _deduce_mobo_chipset(raw_name)
        ff_raw = str(dto.get("form_factor") or "")
        ff = "MICRO_ATX" if "micro" in ff_raw.lower() else ("MINI_ITX" if "mini" in ff_raw.lower() else ("E_ATX" if "e-atx" in ff_raw.lower() else "ATX"))
        mt = str(dto.get("memory_type") or "")
        if "DDR3" in mt.upper():
            mgen = "DDR3"
        elif "DDR4" in mt.upper():
            mgen = "DDR4"
        elif "DDR5" in mt.upper():
            mgen = "DDR5"
        elif socket in ("AM4", "LGA1200", "LGA1151", "LGA2011-v3", "LGA2066"):
            mgen = "DDR4"
        elif socket in ("AM5", "LGA1851"):
            mgen = "DDR5"
        elif socket == "LGA1700":
            mgen = "DDR4" if ("D4" in raw_name.upper() or "DDR4" in raw_name.upper()) else "DDR5"
        else:
            mgen = "DDR4"
        ram_slots = max(2, _as_int(dto.get("memory_slots")) or (2 if ff == "MINI_ITX" else 4))
        pg_raw = str(dto.get("pcie_generation") or "")
        if "5.0" in pg_raw:
            pgen = "GEN5"
        elif "4.0" in pg_raw:
            pgen = "GEN4"
        elif "3.0" in pg_raw:
            pgen = "GEN3"
        elif "2.0" in pg_raw:
            pgen = "GEN2"
        elif socket in ("AM5", "LGA1851") or any(x in chipset for x in ("Z790", "X670", "X870", "Z890")):
            pgen = "GEN5"
        elif socket in ("AM4", "LGA1700", "LGA1200"):
            pgen = "GEN4"
        else:
            pgen = "GEN3"
        hw = dto.get("has_wifi")
        if hw is None:
            hw = bool(dto.get("wifi") or dto.get("wi_fi") or "WIFI" in raw_name.upper() or "AX" in raw_name.upper())

        enr = _serialize_item_enrichment(
            item_id, "MOTHERBOARD", brand, model, dto.get("imageUrl") or dto.get("image_url"),
            dto.get("images"), None, None, None, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, model, "MOTHERBOARD", price_minor, "USD" if price_minor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at")
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `motherboard_specs` (
                `item_id`, `socket`, `chipset`, `form_factor`, `memory_generation`, `ram_slots`,
                `pcie_generation`, `max_memory_gb`, `m2_slots`, `pcie_slot_count`, `sata_ports`,
                `has_wifi`, `vrm_phases`, `interface_ports`, `pcb_layers`, `m2_slots_detail`,
                `memory_speed_oc`, `network`, `audio_codec`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                item_id, socket, chipset, ff, mgen, ram_slots, pgen,
                _as_int(dto.get("max_memory")), _as_int(dto.get("m2_slots")),
                _as_int(dto.get("pcie_slots") or dto.get("pcie_x16_slots")),
                _as_int(dto.get("sata_ports")), 1 if hw else 0,
                _clean_str(dto.get("vrm_phases")), _clean_str(dto.get("interface_ports")),
                _clean_str(dto.get("pcb_layers")), _clean_str(dto.get("m2_slots_detail")),
                _clean_str(dto.get("memory_speed_oc")),
                _clean_str(dto.get("network") or dto.get("onboard_ethernet")),
                _clean_str(dto.get("audio_codec")),
            ),
        )
    print(f"  [OK] {len(seen_mb)} MOTHERBOARD")

    # 5. PSUs
    seen_psu = set()
    for dto in _load_json("power-supply.json", []):
        raw_name = _clean_str(dto.get("name"))
        if not raw_name:
            continue
        item_id = _gen_hardware_id("PSU", raw_name)
        if item_id in seen_psu:
            continue
        seen_psu.add(item_id)
        brand, model = _extract_brand_and_model(raw_name)
        price_val = _as_float(dto.get("price"))
        price_minor = max(0, int(round(price_val * 100))) if price_val is not None else None
        watt = max(300, _as_int(dto.get("wattage")) or 750)
        u = raw_name.upper()
        is_edge = "EDGE" in u
        is_atx31 = any(k in u for k in ("ATX 3.1", "ATX3.1", "PCIE 5.1", "VITA GM")) or is_edge
        is_atx3 = 1 if (is_atx31 or any(k in u for k in ("PCIE5", "ATX 3", "ATX3", "GL", "AURA"))) else 0
        eff_raw = str(dto.get("efficiency") or dto.get("efficiency_rating") or "").lower().strip()
        if eff_raw == "titanium" or "TITANIUM" in u:
            eff = "TITANIUM"
        elif eff_raw == "platinum" or is_edge or "PLATINUM" in u or "THOR" in u:
            eff = "PLATINUM"
        elif eff_raw == "bronze" or "BRONZE" in u or "BN" in u:
            eff = "BRONZE"
        elif eff_raw == "silver" or "SILVER" in u:
            eff = "SILVER"
        else:
            eff = "GOLD"
        p12 = _clean_str(dto.get("pcie_12vhpwr") or dto.get("pcie_5_connector"))
        eps = _clean_str(dto.get("eps_connectors"))
        pcie = _clean_str(dto.get("pcie_connectors"))
        outputs = _clean_str(dto.get("outputs")) or (" • ".join([v for v in (p12, eps, pcie) if v]) or None)

        enr = _serialize_item_enrichment(
            item_id, "PSU", brand, model, dto.get("imageUrl") or dto.get("image_url"),
            dto.get("images"), None, None, None, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, model, "PSU", price_minor, "USD" if price_minor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at")
        )
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
                item_id, watt, eff, is_atx3, is_atx3,
                _clean_str(dto.get("modular")), _clean_str(dto.get("type") or dto.get("form_factor")), outputs,
                _clean_str(dto.get("tier")), _clean_str(dto.get("capacitors")),
                _clean_str(dto.get("atx_standard")), p12, eps, pcie, _clean_str(dto.get("sata_connectors")),
                _clean_str(dto.get("topology")), _clean_str(dto.get("protections")),
                _clean_str(dto.get("fan_size")),
                _clean_str(dto.get("cybenetics") or dto.get("cybenetics_efficiency")),
                _clean_str(dto.get("cybenetics_lambda") or dto.get("cybenetics_noise")),
            ),
        )
    print(f"  [OK] {len(seen_psu)} PSU")

    # 6. Storage
    seen_st = set()
    for dto in _load_json("internal-hard-drive.json", []):
        raw_name = _clean_str(dto.get("name"))
        if not raw_name:
            continue
        cap = _as_int(dto.get("capacity"))
        tp = str(dto.get("type") or "")
        ff_raw = str(dto.get("form_factor") or "")
        is_hdd = any(k in tp.upper() for k in ("HDD", "RPM", "7200", "5400")) or "3.5" in ff_raw or any(k in raw_name.lower() for k in ("barracuda", "ironwolf"))
        if is_hdd or (cap is not None and cap < 120):
            continue
        item_id = _gen_hardware_id("STORAGE", raw_name)
        if item_id in seen_st:
            continue
        seen_st.add(item_id)
        brand, model = _extract_brand_and_model(raw_name)
        price_val = _as_float(dto.get("price"))
        price_minor = max(0, int(round(price_val * 100))) if price_val is not None else None
        iface_raw = str(dto.get("interface") or "")
        is_nvme = "m.2" in ff_raw.lower() or "pcie" in iface_raw.lower()
        st_iface = "NVME" if is_nvme else "SATA"
        pgen = ("GEN5" if "5.0" in iface_raw else "GEN4") if is_nvme else None
        ff = "M2_2280" if "2280" in ff_raw else ("M2_2242" if "2242" in ff_raw else ("M2_2230" if "2230" in ff_raw else ("INCH_2_5" if "2.5" in ff_raw else "M2_2280")))
        cap_mb = max(1024, (cap or 1000) * 1024)
        sread = max(100, _as_int(dto.get("seq_read") or dto.get("read_speed")) or (3500 if is_nvme else 500))
        swrite = max(100, _as_int(dto.get("seq_write") or dto.get("write_speed")) or (3000 if is_nvme else 450))

        enr = _serialize_item_enrichment(
            item_id, "STORAGE", brand, model, dto.get("imageUrl") or dto.get("image_url"),
            dto.get("images"), None, None, None, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, model, "STORAGE", price_minor, "USD" if price_minor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at")
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `storage_specs` (
                `item_id`, `media_type`, `storage_interface`, `form_factor`, `pcie_generation`,
                `capacity_mb`, `seq_read_mbps`, `seq_write_mbps`, `nand_type`, `endurance_tbw`,
                `cache_mb`, `controller`, `dram_cache`, `random_iops`
            ) VALUES (?, 'SSD', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                item_id, st_iface, ff, pgen, cap_mb, sread, swrite,
                _clean_str(dto.get("nand_type")), _as_int(dto.get("endurance_tbw") or dto.get("tbw")),
                _as_int(dto.get("cache")), _clean_str(dto.get("controller")),
                _clean_str(dto.get("dram_cache")), _clean_str(dto.get("random_iops")),
            ),
        )
    print(f"  [OK] {len(seen_st)} STORAGE")

    # 7. Coolers
    seen_cl = set()
    for dto in _load_json("cpu-cooler.json", []):
        raw_name = _clean_str(dto.get("name"))
        if not raw_name:
            continue
        item_id = _gen_hardware_id("COOLER", raw_name)
        if item_id in seen_cl:
            continue
        seen_cl.add(item_id)
        brand, model = _extract_brand_and_model(raw_name)
        price_val = _as_float(dto.get("price"))
        price_minor = max(0, int(round(price_val * 100))) if price_val is not None else None
        rad_sz = _as_int(dto.get("size") or dto.get("radiator_size") or dto.get("radiator_size_mm"))
        tp_str = str(dto.get("type") or dto.get("cooler_type") or "")
        is_liq = (rad_sz is not None and rad_sz > 0) or ("liquid" in tp_str.lower() or "aio" in tp_str.lower())
        ctype = "LIQUID" if is_liq else "AIR"
        max_tdp = _as_int(dto.get("max_tdp") or dto.get("tdp_capacity")) or (300 if is_liq else 220)
        socks = dto.get("sockets") or dto.get("supported_sockets") or ["AM5", "AM4", "LGA1700", "LGA1851"]

        enr = _serialize_item_enrichment(
            item_id, "COOLER", brand, model, dto.get("imageUrl") or dto.get("image_url"),
            dto.get("images"), None, None, None, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, model, "COOLER", price_minor, "USD" if price_minor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at")
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `cooler_specs` (
                `item_id`, `type`, `radiator_size_mm`, `max_cooling_tdp_watts`, `supported_sockets`,
                `height_mm`, `fan_rpm`, `noise_dba`, `radiator_dimensions`, `fan_airflow`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                item_id, ctype, rad_sz if is_liq else None, max_tdp, json.dumps(socks, ensure_ascii=False),
                _as_int(dto.get("height")), _clean_str(dto.get("fan_rpm") or dto.get("rpm")),
                _as_float(dto.get("noise_level") or dto.get("noise_level_db")),
                _clean_str(dto.get("radiator_dimensions")), _clean_str(dto.get("fan_airflow")),
            ),
        )
    print(f"  [OK] {len(seen_cl)} COOLER")

    # 8. Cases
    seen_cs = set()
    for dto in _load_json("case.json", []):
        raw_name = _clean_str(dto.get("name"))
        if not raw_name:
            continue
        item_id = _gen_hardware_id("CASE", raw_name)
        if item_id in seen_cs:
            continue
        seen_cs.add(item_id)
        brand, model = _extract_brand_and_model(raw_name)
        price_val = _as_float(dto.get("price"))
        price_minor = max(0, int(round(price_val * 100))) if price_val is not None else None

        enr = _serialize_item_enrichment(
            item_id, "CASE", brand, model, dto.get("imageUrl") or dto.get("image_url"),
            dto.get("images"), None, None, None, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, model, "CASE", price_minor, "USD" if price_minor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at")
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `case_specs` (
                `item_id`, `type`, `max_gpu_length_mm`, `max_cpu_cooler_height_mm`, `max_radiator_size_mm`,
                `side_panel`, `drive_bays`, `radiator_support`, `included_fans`, `front_panel_usb`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                item_id, _clean_str(dto.get("type") or dto.get("form_factor")),
                _as_int(dto.get("max_gpu_length")), _as_int(dto.get("max_cpu_cooler_height")),
                _as_int(dto.get("max_radiator_size")), _clean_str(dto.get("side_panel")),
                _clean_str(dto.get("drive_bays")), _clean_str(dto.get("radiator_support")),
                _clean_str(dto.get("included_fans")), _clean_str(dto.get("front_panel_usb")),
            ),
        )
    print(f"  [OK] {len(seen_cs)} CASE")

    # 9. Laptops
    seen_lp = set()
    for dto in _load_json("laptop.json", []):
        item_id = _clean_str(dto.get("id"))
        if not item_id or item_id in seen_lp:
            continue
        seen_lp.add(item_id)
        name = _clean_str(dto.get("name")) or "Gaming Laptop"
        brand = _clean_str(dto.get("brand")) or "Laptop"
        lap_model = (
            name[len(brand):].strip() or name
            if len(name) > len(brand) + 2 and name.lower().startswith(brand.lower() + " ")
            else name
        )
        ptry = _as_float(dto.get("priceTry"))
        pminor = int(round(ptry * 100)) if ptry is not None else None
        enr = _serialize_item_enrichment(
            item_id, "LAPTOP", brand, lap_model, dto.get("imageUrl") or dto.get("image_url"),
            dto.get("images"), ptry, ptry, ptry, None, None, prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, item_id, brand, lap_model, "LAPTOP", pminor, "TRY" if pminor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at"),
            override_bgidx=_as_int(dto.get("gamingScore")),
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `laptop_specs` (
                `item_id`, `cpu_model`, `gpu_model`, `gpu_watt_tgp`, `mux_switch`, `ram_spec`,
                `storage_spec`, `display_spec`, `battery_wh`, `weight_kg`, `cooling_tech`, `gaming_score`,
                `fps_1080p`, `fps_1440p`, `cpu_cores`, `cpu_clock_ghz`, `screen_resolution`,
                `refresh_rate_hz`, `panel_tech`, `color_gamut`, `screen_size_inch`, `ram_slots`,
                `storage_slots`, `charger_watt`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                item_id, dto.get("cpuModel") or "", dto.get("gpuModel") or "", dto.get("gpuWattTgp") or "",
                dto.get("muxSwitch") or "", dto.get("ramSpec") or "", dto.get("storageSpec") or "",
                dto.get("displaySpec") or "", _as_int(dto.get("batteryWh")) or 0, _as_float(dto.get("weightKg")) or 0.0,
                dto.get("coolingTech") or "", _as_int(dto.get("gamingScore")) or 0,
                _as_int(dto.get("fps1080p")) or 0, _as_int(dto.get("fps1440p")) or 0,
                dto.get("cpuCores") or "", dto.get("cpuClockGhz") or "", dto.get("screenResolution") or "",
                _as_int(dto.get("refreshRateHz")) or 0, dto.get("panelTech") or "", dto.get("colorGamut") or "",
                _as_float(dto.get("screenSizeInch")) or 0.0, dto.get("ramSlots") or "",
                dto.get("storageSlots") or "", _as_int(dto.get("chargerWatt")) or 0,
            ),
        )
    print(f"  [OK] {len(seen_lp)} LAPTOP")

    # 10. Peripherals & Networking (17 tables)
    seen_per = set()
    for dto in _load_json("peripherals.json", []):
        cat = str(dto.get("category") or "").upper()
        if not cat:
            continue
        name = _clean_str(dto.get("name")) or ""
        item_id = _clean_str(dto.get("id")) or _gen_hardware_id(cat, name)
        if not item_id or item_id in seen_per:
            continue
        seen_per.add(item_id)
        brand = _clean_str(dto.get("brand")) or "Çevre Birimi"
        model = _clean_str(dto.get("model")) or name
        c_try = _as_float(dto.get("current_price_try"))
        pminor = int(round(c_try * 100)) if c_try is not None else None
        offers_raw = (
            [{"storeName": dto["store_name"], "priceTry": c_try, "url": dto.get("store_url") or ""}]
            if (c_try is not None and _clean_str(dto.get("store_name"))) else None
        )
        enr = _serialize_item_enrichment(
            item_id, cat, brand, model, dto.get("image_url"), dto.get("images"),
            c_try, _as_float(dto.get("lowest_price_try")), _as_float(dto.get("highest_price_try")),
            offers_raw, None, prices_tr, benchmarks, legacy_images
        )
        bscore = _as_int(dto.get("benchmark_score"))
        _insert_hardware_item(
            cursor, item_id, brand, model, cat, pminor, "TRY" if pminor is not None else None,
            now_ms, enr, dto.get("extra_specs"), dto.get("source_provenance"), dto.get("spec_verified_at"),
            override_bscore=bscore, override_btier=dto.get("benchmark_tier"), override_bgidx=bscore,
        )

        if cat == "MOUSE":
            cursor.execute(
                "INSERT OR REPLACE INTO `mouse_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("sensor") or "", _as_int(dto.get("max_dpi")) or 0,
                    _as_int(dto.get("polling_rate_hz")) or 0, dto.get("switch_type") or "",
                    _as_int(dto.get("weight_g")) or 0, dto.get("connectivity") or "",
                    _as_int(dto.get("battery_hours")), dto.get("mcu") or "",
                    _as_float(dto.get("click_latency_ms")) or 0.0, dto.get("shape_type") or "",
                ),
            )
        elif cat == "KEYBOARD":
            sw = dto.get("switch_type") or ""
            is_he = dto.get("is_hall_effect")
            if is_he is None:
                is_he = "hall effect" in sw.lower()
            cursor.execute(
                "INSERT OR REPLACE INTO `keyboard_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, sw, 1 if dto.get("rapid_trigger_supported") else 0,
                    _as_float(dto.get("min_actuation_mm")) or 0.0, _as_int(dto.get("polling_rate_hz")) or 0,
                    dto.get("layout") or "", 1 if dto.get("hot_swap") else 0,
                    dto.get("connectivity") or "", _as_float(dto.get("max_actuation_mm")) or 0.0,
                    dto.get("case_material") or "", 1 if is_he else 0,
                ),
            )
        elif cat == "HEADSET":
            cursor.execute(
                "INSERT OR REPLACE INTO `headset_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("driver_type") or "", _as_int(dto.get("driver_size_mm")) or 0,
                    _as_int(dto.get("impedance_ohm")) or 0, dto.get("frequency_response") or "",
                    1 if dto.get("dac_amp_required") else 0, dto.get("mic_type") or "",
                    _as_int(dto.get("weight_g")) or 0, dto.get("connectivity") or "",
                    dto.get("sound_isolation") or "",
                ),
            )
        elif cat == "MONITOR":
            cursor.execute(
                "INSERT OR REPLACE INTO `monitor_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("panel_type") or "", _as_float(dto.get("screen_size_inch")) or 0.0,
                    dto.get("resolution") or "", _as_int(dto.get("refresh_rate_hz")) or 0,
                    _as_float(dto.get("response_speed_gtg_ms")) or 0.0, _as_int(dto.get("ppi")) or 0,
                    dto.get("hdr_certification") or "", dto.get("bit_depth") or "",
                    _as_float(dto.get("response_mprt_ms")) or 0.0,
                    _as_int(dto.get("brightness_sdr_nit")) or 0, _as_int(dto.get("brightness_hdr_nit")) or 0,
                    dto.get("color_gamut") or "", dto.get("vrr_support") or "",
                ),
            )
        elif cat == "MICROPHONE":
            cursor.execute(
                "INSERT OR REPLACE INTO `microphone_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("capsule_type") or "", dto.get("polar_pattern") or "",
                    dto.get("sample_rate_and_bit") or "", dto.get("frequency_response") or "",
                    dto.get("connectivity") or "", _as_int(dto.get("max_spl_db")) or 0,
                    1 if dto.get("has_built_in_dsp") else 0, 1 if dto.get("requires_phantom_power") else 0,
                    1 if dto.get("has_zero_latency_monitoring") else 0, 1 if dto.get("has_pop_filter") else 0,
                ),
            )
        elif cat == "MOUSEPAD":
            cursor.execute(
                "INSERT OR REPLACE INTO `mousepad_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("surface_type") or "", dto.get("speed_glide_tier") or "",
                    dto.get("base_material") or "", dto.get("dimensions_mm") or "",
                    _as_float(dto.get("thickness_mm")) or 0.0, 1 if dto.get("stitched_edges") else 0,
                    _as_int(dto.get("stopping_power_score")) or 0, dto.get("humidity_resistance") or "",
                ),
            )
        elif cat == "WEBCAM":
            cursor.execute(
                "INSERT OR REPLACE INTO `webcam_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("max_resolution") or "", _as_int(dto.get("max_frame_rate_fps")) or 0,
                    dto.get("sensor_model") or "", dto.get("sensor_size_inch") or "",
                    dto.get("aperture") or "", _as_int(dto.get("field_of_view_deg")) or 0,
                    dto.get("focus_type") or "", 1 if dto.get("has_ai_tracking") else 0,
                    1 if dto.get("has_hardware_hdr") else 0, dto.get("connection_type") or "",
                    1 if dto.get("privacy_shutter") else 0,
                ),
            )
        elif cat == "GAMING_CHAIR":
            cursor.execute(
                "INSERT OR REPLACE INTO `gaming_chair_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("chair_type") or "", dto.get("upholstery_material") or "",
                    dto.get("lumbar_support_type") or "", dto.get("armrest_type") or "",
                    dto.get("gas_lift_class") or "", _as_int(dto.get("max_weight_capacity_kg")) or 0,
                    dto.get("recommended_height_range") or "", _as_int(dto.get("recline_angle_deg")) or 0,
                    dto.get("tilt_mechanism") or "",
                ),
            )
        elif cat == "CASE_FAN":
            cursor.execute(
                "INSERT OR REPLACE INTO `case_fan_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, _as_int(dto.get("fan_size_mm")) or 0, _as_int(dto.get("min_rpm")) or 0,
                    _as_int(dto.get("max_rpm")) or 0, _as_float(dto.get("max_airflow_cfm")) or 0.0,
                    _as_float(dto.get("static_pressure_mmh2o")) or 0.0, _as_float(dto.get("noise_level_dba")) or 0.0,
                    dto.get("bearing_type") or "", _as_int(dto.get("pwm_pins")) or 0,
                    1 if dto.get("is_daisy_chain") else 0, dto.get("argb_type") or "",
                ),
            )
        elif cat == "UPS":
            cursor.execute(
                "INSERT OR REPLACE INTO `ups_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, _as_int(dto.get("va_rating")) or 0, _as_int(dto.get("watt_capacity")) or 0,
                    dto.get("topology") or "", dto.get("sinewave_type") or "",
                    dto.get("battery_specs") or "", _as_int(dto.get("runtime_min_full_load")) or 0,
                    _as_int(dto.get("runtime_min_half_load")) or 0, _as_int(dto.get("outlet_count")) or 0,
                    1 if dto.get("has_usb_interface") else 0, _as_int(dto.get("surge_protection_joules")) or 0,
                ),
            )
        elif cat == "GAMING_DESK":
            cursor.execute(
                "INSERT OR REPLACE INTO `gaming_desk_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, _as_int(dto.get("desk_width_cm")) or 0, _as_int(dto.get("desk_depth_cm")) or 0,
                    1 if dto.get("is_motorized_height") else 0, _as_int(dto.get("min_height_cm")) or 0,
                    _as_int(dto.get("max_height_cm")) or 0, _as_int(dto.get("max_load_kg")) or 0,
                    dto.get("surface_finish") or "", 1 if dto.get("has_cable_management") else 0,
                    1 if dto.get("integrated_mousepad") else 0,
                ),
            )
        elif cat == "GAMING_LIGHTING":
            cursor.execute(
                "INSERT OR REPLACE INTO `gaming_lighting_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("lighting_form_factor") or "", _as_int(dto.get("max_lumens")) or 0,
                    dto.get("color_temp_kelvin") or "", _as_int(dto.get("cri_score")) or 0,
                    1 if dto.get("has_screen_mirroring") else 0, dto.get("supported_ecosystems") or "",
                    dto.get("connection_type") or "", dto.get("power_source") or "",
                ),
            )
        elif cat == "SMARTWATCH":
            cursor.execute(
                "INSERT OR REPLACE INTO `smartwatch_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, _as_float(dto.get("screen_size_inch")) or 0.0, dto.get("screen_panel") or "",
                    _as_int(dto.get("peak_brightness_nits")) or 0, _as_int(dto.get("battery_life_days")) or 0,
                    _as_int(dto.get("water_resistance_atm")) or 0, dto.get("os_name") or "",
                    dto.get("sensors_list") or "", 1 if dto.get("has_cellular_lte") else 0,
                    dto.get("case_material") or "",
                ),
            )
        elif cat == "ROUTER":
            cursor.execute(
                "INSERT OR REPLACE INTO `router_specs` VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("wifi_standard") or "", dto.get("speed_class") or "",
                    dto.get("bands") or "", dto.get("ethernet_ports") or "",
                    1 if dto.get("has_gaming_qos") else 0, 1 if dto.get("has_sqm_bufferbloat") else 0,
                    dto.get("processor_and_ram") or "", dto.get("usb_sharing_ports") or "",
                ),
            )
        elif cat == "MODEM":
            cursor.execute(
                "INSERT OR REPLACE INTO `modem_specs` VALUES (?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("vdsl_profile") or "", 1 if dto.get("fiber_wan_support") else 0,
                    dto.get("wifi_standard") or "", dto.get("lan_ports") or "",
                    dto.get("voip_ports") or "", dto.get("chipset") or "",
                ),
            )
        elif cat == "ACCESS_POINT":
            cursor.execute(
                "INSERT OR REPLACE INTO `access_point_specs` VALUES (?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("form_factor") or "", dto.get("wifi_standard") or "",
                    dto.get("poe_support") or "", _as_int(dto.get("max_clients")) or 0,
                    dto.get("mu_mimo_streams") or "", dto.get("uplink_port") or "",
                ),
            )
        elif cat == "RANGE_EXTENDER":
            cursor.execute(
                "INSERT OR REPLACE INTO `range_extender_specs` VALUES (?, ?, ?, ?, ?, ?, ?)",
                (
                    item_id, dto.get("extender_type") or "", dto.get("wifi_standard") or "",
                    dto.get("dedicated_backhaul") or "", dto.get("mesh_protocol") or "",
                    dto.get("ethernet_ports") or "", _as_int(dto.get("coverage_area_m2")) or 0,
                ),
            )
    print(f"  [OK] {len(seen_per)} PERIPHERAL / NETWORKING")

    # 11. Smartphones
    seen_ph = set()
    for p in _load_json("smartphone.json", []):
        pid = _clean_str(p.get("id"))
        s = p.get("spec")
        if not pid or not isinstance(s, dict) or pid in seen_ph:
            continue
        seen_ph.add(pid)
        ptry = _as_float(p.get("priceTry")) or 0.0
        enr = _serialize_item_enrichment(
            pid, "SMARTPHONE", p.get("brand") or "Akıllı Telefon", p.get("model") or p.get("name") or "",
            p.get("imageUrl"), p.get("images"), ptry, ptry, ptry, p.get("stores"), p.get("priceHistory"),
            prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, pid, p.get("brand") or "Akıllı Telefon", p.get("model") or p.get("name") or "",
            "SMARTPHONE", int(round(ptry * 100)), "TRY", now_ms, enr,
            p.get("extra_specs"), p.get("source_provenance"), p.get("spec_verified_at"),
            override_bscore=p.get("benchmarkScore") or s.get("antutuScore"),
            override_btier=p.get("tier") or "",
            override_bgidx=p.get("gamingIndex") or 80,
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `phone_specs` (
                `item_id`, `chipset`, `cpu_arch`, `gpu_model`, `process_node_nm`, `ram_gb`, `ram_type`,
                `storage_gb`, `storage_type`, `has_micro_sd`, `antutu_score`, `geekbench_single`, `geekbench_multi`,
                `stability_percent`, `dxomark_camera`, `dxomark_selfie`, `dxomark_display`, `dxomark_battery`,
                `dxomark_audio`, `display_size_inch`, `display_tech`, `resolution`, `ppi`, `refresh_rate_hz`,
                `peak_brightness_nit`, `glass_protection`, `hdr_support`, `pwm_dimming_hz`, `main_camera_mp`,
                `main_sensor_size`, `main_aperture`, `has_ois`, `ois_type`, `telephoto_mp`, `optical_zoom_x`,
                `ultra_wide_mp`, `video_resolution`, `selfie_camera_mp`, `battery_mah`, `wired_charging_watt`,
                `wireless_charging_watt`, `has_reverse_charging`, `charging_time_to_full_min`, `weight_grams`,
                `thickness_mm`, `frame_material`, `water_resistance`, `has_stylus`, `usb_speed`,
                `wifi_bluetooth`, `os_name`, `update_years`, `pubg_fps_support`, `genshin_stability_score`, `variants`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                pid, s.get("chipset") or "", s.get("cpuArch") or "", s.get("gpuModel") or "",
                _as_int(s.get("processNodeNm")) or 0, _as_int(s.get("ramGb")) or 0, s.get("ramType") or "",
                _as_int(s.get("storageGb")) or 0, s.get("storageType") or "", 1 if s.get("hasMicroSd") else 0,
                _as_int(s.get("antutuScore")), _as_int(s.get("geekbenchSingle")), _as_int(s.get("geekbenchMulti")),
                _as_int(s.get("stabilityPercent")), _as_int(s.get("dxomarkCamera")), _as_int(s.get("dxomarkSelfie")),
                _as_int(s.get("dxomarkDisplay")), _as_int(s.get("dxomarkBattery")), _as_int(s.get("dxomarkAudio")),
                _as_float(s.get("displaySizeInch")) or 0.0, s.get("displayTech") or "", s.get("resolution") or "",
                _as_int(s.get("ppi")) or 0, _as_int(s.get("refreshRateHz")) or 0,
                _as_int(s.get("peakBrightnessNit")) or 0, s.get("glassProtection") or "", s.get("hdrSupport") or "",
                _as_int(s.get("pwmDimmingHz")), _as_int(s.get("mainCameraMp")) or 0,
                s.get("mainSensorSize") or "", s.get("mainAperture") or "", 1 if s.get("hasOis") else 0,
                s.get("oisType") or "", _as_int(s.get("telephotoMp")), _as_float(s.get("opticalZoomX")),
                _as_int(s.get("ultraWideMp")), s.get("videoResolution") or "", _as_int(s.get("selfieCameraMp")) or 0,
                _as_int(s.get("batteryMah")) or 0, _as_int(s.get("wiredChargingWatt")) or 0,
                _as_int(s.get("wirelessChargingWatt")), 1 if s.get("hasReverseCharging") else 0,
                _as_int(s.get("chargingTimeToFullMin")), _as_int(s.get("weightGrams")) or 0,
                _as_float(s.get("thicknessMm")) or 0.0, s.get("frameMaterial") or "",
                s.get("waterResistance") or "", 1 if s.get("hasStylus") else 0,
                _clean_str(s.get("usbSpeed")), _clean_str(s.get("wifiBluetooth")),
                s.get("osName") or "", _as_int(s.get("updateYears")) or 0,
                s.get("pubgFpsSupport") or "", s.get("genshinStabilityScore") or "",
                json.dumps(s.get("variants") or [], ensure_ascii=False),
            ),
        )
    print(f"  [OK] {len(seen_ph)} SMARTPHONE")

    # 12. Tablets
    seen_tb = set()
    for p in _load_json("tablet.json", []):
        pid = _clean_str(p.get("id"))
        s = p.get("spec")
        if not pid or not isinstance(s, dict) or pid in seen_tb:
            continue
        seen_tb.add(pid)
        ptry = _as_float(p.get("priceTry")) or 0.0
        enr = _serialize_item_enrichment(
            pid, "TABLET", p.get("brand") or "Tablet", p.get("model") or p.get("name") or "",
            p.get("imageUrl"), p.get("images"), ptry, ptry, ptry, p.get("stores"), p.get("priceHistory"),
            prices_tr, benchmarks, legacy_images
        )
        _insert_hardware_item(
            cursor, pid, p.get("brand") or "Tablet", p.get("model") or p.get("name") or "",
            "TABLET", int(round(ptry * 100)), "TRY", now_ms, enr,
            p.get("extra_specs"), p.get("source_provenance"), p.get("spec_verified_at"),
            override_bscore=p.get("benchmarkScore") or s.get("antutuScore"),
            override_btier=p.get("tier") or "",
            override_bgidx=p.get("gamingIndex") or 80,
        )
        cursor.execute(
            """
            INSERT OR REPLACE INTO `tablet_specs` (
                `item_id`, `chipset`, `cpu_arch`, `gpu_model`, `process_node_nm`, `ram_gb`, `storage_gb`,
                `storage_type`, `has_micro_sd`, `antutu_score`, `geekbench_single`, `geekbench_multi`,
                `stability_percent`, `display_size_inch`, `display_tech`, `resolution`, `ppi`, `refresh_rate_hz`,
                `peak_brightness_nit`, `stylus_support`, `aspect_ratio`, `main_camera_mp`, `front_camera_mp`,
                `speaker_count`, `has_dolby_atmos`, `battery_mah`, `wired_charging_watt`, `wireless_charging_watt`,
                `weight_grams`, `thickness_mm`, `body_material`, `os_name`, `update_years`, `cellular_option`,
                `usb_speed`, `external_display_support`, `variants`
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                pid, s.get("chipset") or "", s.get("cpuArch") or "", s.get("gpuModel") or "",
                _as_int(s.get("processNodeNm")) or 0, _as_int(s.get("ramGb")) or 0,
                _as_int(s.get("storageGb")) or 0, s.get("storageType") or "", 1 if s.get("hasMicroSd") else 0,
                _as_int(s.get("antutuScore")), _as_int(s.get("geekbenchSingle")), _as_int(s.get("geekbenchMulti")),
                _as_int(s.get("stabilityPercent")), _as_float(s.get("displaySizeInch")) or 0.0,
                s.get("displayTech") or "", s.get("resolution") or "", _as_int(s.get("ppi")) or 0,
                _as_int(s.get("refreshRateHz")) or 0, _as_int(s.get("peakBrightnessNit")) or 0,
                s.get("stylusSupport") or "", _clean_str(s.get("aspectRatio")),
                _as_int(s.get("mainCameraMp")) or 0, _as_int(s.get("frontCameraMp")) or 0,
                _as_int(s.get("speakerCount")) or 0, 1 if s.get("hasDolbyAtmos") else 0,
                _as_int(s.get("batteryMah")) or 0, _as_int(s.get("wiredChargingWatt")) or 0,
                _as_int(s.get("wirelessChargingWatt")), _as_int(s.get("weightGrams")) or 0,
                _as_float(s.get("thicknessMm")) or 0.0, s.get("bodyMaterial") or "",
                s.get("osName") or "", _as_int(s.get("updateYears")) or 0,
                s.get("cellularOption") or "", _clean_str(s.get("usbSpeed")),
                _clean_str(s.get("externalDisplaySupport")),
                json.dumps(s.get("variants") or [], ensure_ascii=False),
            ),
        )
    print(f"  [OK] {len(seen_tb)} TABLET")


def rebuild_prepopulated_db(output_db_path: str = TARGET_DB_PATH) -> None:
    os.makedirs(os.path.dirname(output_db_path), exist_ok=True)
    legacy_images: dict[str, str] = {}

    staging_path = output_db_path + ".tmp"
    if os.path.exists(staging_path):
        os.remove(staging_path)

    conn = sqlite3.connect(staging_path)
    cursor = conn.cursor()
    cursor.execute("PRAGMA foreign_keys = ON;")

    for stmt in DDL_STATEMENTS:
        cursor.execute(stmt)

    cursor.execute(f"PRAGMA user_version = {ROOM_SCHEMA_VERSION};")
    cursor.execute("INSERT OR REPLACE INTO app_settings (key, value) VALUES ('catalog_seed_version', ?);", (CATALOG_SEED_VERSION,))

    populate_all_catalogs(cursor, legacy_images)
    conn.commit()

    fk_errors = cursor.execute("PRAGMA foreign_key_check;").fetchall()
    if fk_errors:
        conn.close()
        raise RuntimeError(f"Foreign key check failed: {fk_errors[:5]}")

    integrity = cursor.execute("PRAGMA integrity_check;").fetchone()[0]
    if integrity.lower() != "ok":
        conn.close()
        raise RuntimeError(f"SQLite integrity check failed: {integrity}")

    total = cursor.execute("SELECT COUNT(*) FROM hardware_items;").fetchone()[0]
    with_prov = cursor.execute("SELECT COUNT(*) FROM hardware_items WHERE source_provenance IS NOT NULL;").fetchone()[0]
    with_extra = cursor.execute("SELECT COUNT(*) FROM hardware_items WHERE extra_specs IS NOT NULL;").fetchone()[0]
    print(f"  [SUMMARY] Total items={total}, with source_provenance={with_prov}, with extra_specs={with_extra}")

    cursor.execute("VACUUM;")
    conn.commit()
    conn.close()

    shutil.move(staging_path, output_db_path)
    size_mb = os.path.getsize(output_db_path) / (1024 * 1024)
    print(f"Optimized prepopulated Room v{ROOM_SCHEMA_VERSION} DB ready at {output_db_path} ({size_mb:.2f} MB)")


if __name__ == "__main__":
    out_arg = sys.argv[1] if len(sys.argv) > 1 else TARGET_DB_PATH
    rebuild_prepopulated_db(out_arg)
