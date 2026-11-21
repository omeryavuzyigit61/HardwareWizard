#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/supabase_catalog_sync.py

Authoritative Single-Source-of-Truth catalog orchestrator for Hardware Wizard (v5.0):
  1. --action seed-from-room      : One-time migration of verified Room DB (7,571 items) -> Supabase (revision=1, spec_locked=true)
  2. --action delta               : Daily delta-only discovery from trusted sources -> inserts ONLY truly new items into Supabase (revision++)
  3. --action build-room-snapshot : Weekly / pre-release rebuild of pre-populated Room v36 SQLite DB directly from Supabase
  4. --action repair-local-room   : Local validation/repair pass (fixes 0W GPU TDP, stamps catalog_revision=1 & v5.0.0) without network secrets

Python 3.11+ & 3.12+ compatible. Zero external dependencies beyond `requests` (already in requirements.txt).
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
import re
import shutil
import sqlite3
import sys
import time
from datetime import datetime, timezone
from typing import Any

import requests

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)

from scripts.crawlers.state_manager import normalize_canonical_key
from scripts.prepare_prepopulated_db import (
    DDL_STATEMENTS,
    ROOM_IDENTITY_HASH,
    ROOM_SCHEMA_VERSION,
    TARGET_DB_PATH,
    _clean_str,
    _extract_brand_and_model,
    _gen_hardware_id,
    _is_stock_code,
    _sanitize_image_url,
)

CATALOG_SCHEMA_VERSION = "5.0.0"

# Authoritative 28-category mapping: HardwareCategory -> (Room SQLite table, primary key column)
CATEGORY_SPEC_META: dict[str, tuple[str, str]] = {
    "CPU": ("cpu_specs", "item_id"),
    "GPU": ("gpu_specs", "item_id"),
    "RAM": ("ram_specs", "item_id"),
    "MOTHERBOARD": ("motherboard_specs", "item_id"),
    "PSU": ("psu_specs", "item_id"),
    "STORAGE": ("storage_specs", "item_id"),
    "COOLER": ("cooler_specs", "item_id"),
    "CASE": ("case_specs", "item_id"),
    "LAPTOP": ("laptop_specs", "item_id"),
    "MOUSE": ("mouse_specs", "hardware_id"),
    "KEYBOARD": ("keyboard_specs", "hardware_id"),
    "HEADSET": ("headset_specs", "hardware_id"),
    "MONITOR": ("monitor_specs", "hardware_id"),
    "MICROPHONE": ("microphone_specs", "hardware_id"),
    "MOUSEPAD": ("mousepad_specs", "hardware_id"),
    "WEBCAM": ("webcam_specs", "hardware_id"),
    "GAMING_CHAIR": ("gaming_chair_specs", "hardware_id"),
    "CASE_FAN": ("case_fan_specs", "hardware_id"),
    "UPS": ("ups_specs", "hardware_id"),
    "GAMING_DESK": ("gaming_desk_specs", "hardware_id"),
    "GAMING_LIGHTING": ("gaming_lighting_specs", "hardware_id"),
    "SMARTWATCH": ("smartwatch_specs", "hardware_id"),
    "ROUTER": ("router_specs", "hardware_id"),
    "MODEM": ("modem_specs", "hardware_id"),
    "ACCESS_POINT": ("access_point_specs", "hardware_id"),
    "RANGE_EXTENDER": ("range_extender_specs", "hardware_id"),
    "SMARTPHONE": ("phone_specs", "item_id"),
    "TABLET": ("tablet_specs", "item_id"),
}

# Reference manufacturer TDP (Watts) & Recommended PSU (Watts) for GPU chipset validation gate
GPU_CHIPSET_TDP_REF: list[tuple[str, int, int]] = [
    ("RTX 5090", 575, 1000),
    ("RTX 5080", 360, 850),
    ("RTX 5070 TI", 300, 750),
    ("RTX 5070", 250, 650),
    ("RTX 5060 TI", 180, 600),
    ("RTX 5060", 145, 550),
    ("RTX 5050", 130, 550),
    ("RTX 4090 D", 425, 850),
    ("RTX 4090", 450, 850),
    ("RTX 4080 SUPER", 320, 750),
    ("RTX 4080", 320, 750),
    ("RTX 4070 TI SUPER", 285, 700),
    ("RTX 4070 TI", 285, 700),
    ("RTX 4070 SUPER", 220, 650),
    ("RTX 4070", 200, 650),
    ("RTX 4060 TI", 165, 550),
    ("RTX 4060", 115, 550),
    ("RTX 3090 TI", 450, 850),
    ("RTX 3090", 350, 750),
    ("RTX 3080 TI", 350, 750),
    ("RTX 3080", 320, 750),
    ("RTX 3070 TI", 290, 750),
    ("RTX 3070", 220, 650),
    ("RTX 3060 TI", 200, 600),
    ("RTX 3060", 170, 550),
    ("RTX 3050", 130, 550),
    ("RTX 2080 TI", 250, 650),
    ("RTX 2080", 215, 650),
    ("RTX 2070", 175, 550),
    ("RTX 2060", 160, 500),
    ("GTX 1660", 125, 450),
    ("GTX 1650", 75, 350),
    ("RX 9070 XT", 304, 750),
    ("RX 9070 GRE", 220, 650),
    ("RX 9070", 220, 650),
    ("RX 9060 XT", 160, 550),
    ("RX 7900 XTX", 355, 800),
    ("RX 7900 XT", 315, 750),
    ("RX 7900 GRE", 260, 700),
    ("RX 7800 XT", 263, 700),
    ("RX 7700 XT", 245, 650),
    ("RX 7600 XT", 190, 600),
    ("RX 7600", 165, 550),
    ("RX 6950 XT", 335, 850),
    ("RX 6900 XT", 300, 850),
    ("RX 6800 XT", 300, 750),
    ("RX 6800", 250, 650),
    ("RX 6750 XT", 250, 650),
    ("RX 6700 XT", 230, 650),
    ("RX 6650 XT", 180, 550),
    ("RX 6600 XT", 160, 500),
    ("RX 6600", 132, 500),
    ("RX 6500 XT", 107, 450),
    ("ARC B580", 190, 600),
    ("ARC B570", 150, 550),
    ("ARC A770", 225, 650),
    ("ARC A750", 225, 650),
    ("ARC A580", 185, 600),
    ("ARC A380", 75, 350),
    ("RTX PRO 6000", 600, 1000),
    ("RTX 6000 ADA", 300, 750),
    ("RTX 5000 ADA", 250, 650),
    ("RTX 4000 ADA", 130, 500),
    ("RTX A6000", 300, 750),
    ("RTX A5000", 230, 650),
    ("RTX A4000", 140, 500),
    ("RTX A2000", 70, 350),
    ("GT 1030", 30, 300),
    ("GT 710", 19, 300),
]

# Spec columns that store booleans as 0/1 in SQLite -> convert cleanly to JSON booleans in Supabase
BOOL_SPEC_COLUMNS = {
    "has_integrated_graphics",
    "unlocked_multiplier",
    "ecc_support",
    "heat_spreader",
    "rgb",
    "has_wifi",
    "is_atx3_compliant",
    "supports_pcie5_power",
    "rapid_trigger_supported",
    "hot_swap",
    "is_hall_effect",
    "dac_amp_required",
    "has_built_in_dsp",
    "requires_phantom_power",
    "has_zero_latency_monitoring",
    "has_pop_filter",
    "stitched_edges",
    "has_ai_tracking",
    "has_hardware_hdr",
    "privacy_shutter",
    "is_daisy_chain",
    "has_usb_interface",
    "is_motorized_height",
    "has_cable_management",
    "integrated_mousepad",
    "has_screen_mirroring",
    "has_cellular_lte",
    "has_gaming_qos",
    "has_sqm_bufferbloat",
    "fiber_wan_support",
    "has_micro_sd",
    "has_ois",
    "has_reverse_charging",
    "has_stylus",
    "has_dolby_atmos",
}

# Spec columns that store JSON strings in Room SQLite -> decode to JSON arrays/objects in Supabase
JSON_SPEC_COLUMNS = {"supported_sockets", "variants"}


def _safe_json_loads(raw: Any, default: Any) -> Any:
    if raw is None:
        return default
    if isinstance(raw, (dict, list)):
        return raw
    if isinstance(raw, str) and raw.strip():
        try:
            return json.loads(raw)
        except Exception:
            return default
    return default


def infer_gpu_tdp_and_psu(text: str, vram_mb: int = 0) -> tuple[int, int]:
    """Derives accurate reference GPU TDP and Recommended PSU from chipset/model."""
    u = (text or "").upper()
    for pattern, tdp, psu in GPU_CHIPSET_TDP_REF:
        if pattern in u:
            return tdp, psu
    if vram_mb >= 24576:
        return 350, 850
    if vram_mb >= 16384:
        return 260, 700
    if vram_mb >= 12288:
        return 210, 650
    if vram_mb >= 8192:
        return 165, 550
    return 115, 450


def repair_gpu_spec(brand: str, model: str, spec: dict[str, Any]) -> bool:
    """Validation gate fixing 0W GPU TDP / 0W PSU in place. Returns True if modified."""
    changed = False
    tdp = int(spec.get("tdp_watts") or 0)
    psu = int(spec.get("recommended_psu_watts") or 0)
    vram_mb = int(spec.get("vram_mb") or 0)
    chipset_text = f"{brand} {model} {spec.get('chipset') or ''}"
    ref_tdp, ref_psu = infer_gpu_tdp_and_psu(chipset_text, vram_mb)
    if tdp <= 0:
        spec["tdp_watts"] = ref_tdp
        changed = True
    if psu <= 0:
        effective_tdp = int(spec.get("tdp_watts") or ref_tdp)
        spec["recommended_psu_watts"] = max(ref_psu, ((effective_tdp + 350 + 49) // 50) * 50)
        changed = True
    return changed


def sanitize_item_images(image_url: str | None, images: list[str] | None) -> tuple[str | None, list[str]]:
    """Enforces SecurityUtils-compatible image URL sanitization across primary + gallery."""
    clean_gallery: list[str] = []
    seen: set[str] = set()
    for raw in ([image_url] if image_url else []) + list(images or []):
        safe = _sanitize_image_url(raw)
        if safe and safe not in seen:
            seen.add(safe)
            clean_gallery.append(safe)
    return (clean_gallery[0] if clean_gallery else None), clean_gallery


def make_unique_canonical_key(category: str, brand: str, model: str, item_id: str, seen_keys: set[tuple[str, str]]) -> str:
    """Generates a deterministic canonical_key unique per (category, canonical_key)."""
    display = model if model.lower().startswith(brand.lower()) else f"{brand} {model}"
    base = normalize_canonical_key(display) or normalize_canonical_key(item_id)
    if (category, base) not in seen_keys:
        seen_keys.add((category, base))
        return base
    # Collision (e.g., OEM SKU variants sharing model name): suffix with stable 6-char id hash
    suffix = hashlib.sha1(item_id.encode("utf-8")).hexdigest()[:6]
    candidate = f"{base}_{suffix}"
    seen_keys.add((category, candidate))
    return candidate


# ==============================================================================
# Supabase PostgREST Lightweight Client (uses SUPABASE_URL + SUPABASE_SERVICE_ROLE_KEY)
# ==============================================================================
class SupabaseAdminClient:
    def __init__(self, url: str, service_role_key: str) -> None:
        self.base_url = url.rstrip("/")
        self.rest_url = f"{self.base_url}/rest/v1"
        self.session = requests.Session()
        self.session.headers.update(
            {
                "apikey": service_role_key,
                "Authorization": f"Bearer {service_role_key}",
                "Content-Type": "application/json",
                "Accept": "application/json",
            }
        )

    def get_catalog_meta(self) -> dict[str, Any]:
        resp = self.session.get(
            f"{self.rest_url}/catalog_meta",
            params={"select": "latest_revision,total_items,last_delta_at,last_checked_at", "limit": "1"},
            timeout=20,
        )
        resp.raise_for_status()
        rows = resp.json()
        return rows[0] if rows else {"latest_revision": 0, "total_items": 0}

    def fetch_existing_keys(self, page_size: int = 1000) -> tuple[set[str], set[tuple[str, str]]]:
        """Lightweight paginated fetch (~300 KB total for 7,571 rows) of existing IDs & canonical keys."""
        existing_ids: set[str] = set()
        existing_ckeys: set[tuple[str, str]] = set()
        offset = 0
        while True:
            headers = {"Range-Unit": "items", "Range": f"{offset}-{offset + page_size - 1}"}
            resp = self.session.get(
                f"{self.rest_url}/hardware_catalog",
                params={"select": "id,category,canonical_key", "order": "id.asc"},
                headers=headers,
                timeout=30,
            )
            if resp.status_code == 416:
                break
            resp.raise_for_status()
            batch = resp.json()
            if not batch:
                break
            for row in batch:
                existing_ids.add(row["id"])
                existing_ckeys.add((row["category"], row["canonical_key"]))
            if len(batch) < page_size:
                break
            offset += page_size
        return existing_ids, existing_ckeys

    def fetch_all_active_catalog(self, page_size: int = 1000) -> list[dict[str, Any]]:
        """Downloads all active rows from hardware_catalog for weekly Room DB snapshot builds."""
        all_rows: list[dict[str, Any]] = []
        offset = 0
        while True:
            headers = {"Range-Unit": "items", "Range": f"{offset}-{offset + page_size - 1}"}
            resp = self.session.get(
                f"{self.rest_url}/hardware_catalog",
                params={"select": "*", "is_active": "eq.true", "order": "category.asc,id.asc"},
                headers=headers,
                timeout=45,
            )
            if resp.status_code == 416:
                break
            resp.raise_for_status()
            batch = resp.json()
            if not batch:
                break
            all_rows.extend(batch)
            if len(batch) < page_size:
                break
            offset += page_size
        return all_rows

    def upsert_catalog_batches(
        self,
        rows: list[dict[str, Any]],
        batch_size: int = 200,
        ignore_duplicates: bool = False,
    ) -> int:
        if not rows:
            return 0
        resolution = "ignore-duplicates" if ignore_duplicates else "merge-duplicates"
        headers = {"Prefer": f"resolution={resolution},return=minimal"}
        written = 0
        for i in range(0, len(rows), batch_size):
            chunk = rows[i : i + batch_size]
            resp = self.session.post(
                f"{self.rest_url}/hardware_catalog",
                params={"on_conflict": "id"},
                headers=headers,
                data=json.dumps(chunk, ensure_ascii=False),
                timeout=60,
            )
            if not resp.ok:
                raise RuntimeError(
                    f"Supabase hardware_catalog upsert failed at batch {i}: HTTP {resp.status_code} -> {resp.text[:400]}"
                )
            written += len(chunk)
        return written

    def refresh_meta(self, new_revision: int | None = None) -> dict[str, Any]:
        payload = {"p_new_revision": new_revision}
        resp = self.session.post(
            f"{self.rest_url}/rpc/refresh_catalog_meta",
            data=json.dumps(payload),
            timeout=20,
        )
        resp.raise_for_status()
        res = resp.json()
        if isinstance(res, list) and res:
            return res[0]
        return res if isinstance(res, dict) else {}

    def log_crawler_run(
        self,
        run_mode: str,
        new_items_added: int,
        sources_checked: int,
        catalog_revision: int,
        status: str = "OK",
        notes: str = "",
    ) -> None:
        try:
            self.session.post(
                f"{self.rest_url}/crawler_runs",
                headers={"Prefer": "return=minimal"},
                data=json.dumps(
                    {
                        "run_mode": run_mode,
                        "new_items_added": new_items_added,
                        "sources_checked": sources_checked,
                        "catalog_revision": catalog_revision,
                        "status": status,
                        "notes": notes[:1000],
                    },
                    ensure_ascii=False,
                ),
                timeout=15,
            )
        except Exception as exc:
            print(f"[WARN] Could not log crawler_runs audit row: {exc}")


# ==============================================================================
# Reading Local Room SQLite DB -> Supabase Rows (Lossless across all 28 tables)
# ==============================================================================
def extract_rows_from_room_db(db_path: str, catalog_revision: int = 1) -> tuple[list[dict[str, Any]], int]:
    if not os.path.exists(db_path):
        raise FileNotFoundError(f"Room DB not found at {db_path}")

    con = sqlite3.connect(db_path)
    con.row_factory = sqlite3.Row
    cur = con.cursor()

    # Load all 28 spec tables indexed by primary key
    specs_by_cat: dict[str, dict[str, dict[str, Any]]] = {}
    for cat, (table_name, pk_col) in CATEGORY_SPEC_META.items():
        cat_map: dict[str, dict[str, Any]] = {}
        for row in cur.execute(f"SELECT * FROM {table_name}").fetchall():
            d = dict(row)
            key = d.pop(pk_col)
            # Convert SQLite 0/1 booleans and JSON strings into proper typed JSON values
            clean_spec: dict[str, Any] = {}
            for col, val in d.items():
                if val is None:
                    clean_spec[col] = None
                elif col in BOOL_SPEC_COLUMNS:
                    clean_spec[col] = bool(val)
                elif col in JSON_SPEC_COLUMNS:
                    clean_spec[col] = _safe_json_loads(val, [])
                else:
                    clean_spec[col] = val
            cat_map[key] = clean_spec
        specs_by_cat[cat] = cat_map

    rows_out: list[dict[str, Any]] = []
    seen_keys: set[tuple[str, str]] = set()
    repaired_gpu_count = 0

    for item in cur.execute("SELECT * FROM hardware_items ORDER BY category, id").fetchall():
        cat = item["category"]
        if cat not in CATEGORY_SPEC_META:
            continue
        spec_table, _ = CATEGORY_SPEC_META[cat]
        spec_payload = dict(specs_by_cat.get(cat, {}).get(item["id"], {}))
        if not spec_payload:
            continue

        if cat == "GPU" and repair_gpu_spec(item["brand"], item["model"], spec_payload):
            repaired_gpu_count += 1

        primary_img, gallery_imgs = sanitize_item_images(
            item["image_url"], _safe_json_loads(item["images"], [])
        )
        ckey = make_unique_canonical_key(cat, item["brand"], item["model"], item["id"], seen_keys)

        rows_out.append(
            {
                "id": item["id"],
                "category": cat,
                "brand": item["brand"],
                "model": item["model"],
                "canonical_key": ckey,
                "spec_table": spec_table,
                "spec_payload": spec_payload,
                "benchmark_score": item["benchmark_score"],
                "benchmark_tier": item["benchmark_tier"],
                "benchmark_test_suite": item["benchmark_test_suite"],
                "benchmark_gaming_index": item["benchmark_gaming_index"],
                "cpu_synthetic": _safe_json_loads(item["cpu_synthetic"], None),
                "gpu_synthetic": _safe_json_loads(item["gpu_synthetic"], None),
                "game_fps": _safe_json_loads(item["game_fps"], None),
                "image_url": primary_img,
                "images": gallery_imgs,
                "extra_specs": _safe_json_loads(item["extra_specs"], {}),
                "source_provenance": _safe_json_loads(item["source_provenance"], ["HardwareWizard Katalog"]),
                "spec_verified_at": item["spec_verified_at"] or datetime.now(timezone.utc).strftime("%Y-%m-%d"),
                "spec_locked": True,
                "is_active": True,
                "catalog_revision": catalog_revision,
            }
        )

    con.close()
    return rows_out, repaired_gpu_count


# ==============================================================================
# Building Pre-populated Room v36 SQLite DB directly FROM Supabase Rows
# ==============================================================================
def build_room_db_from_catalog_rows(
    catalog_rows: list[dict[str, Any]],
    latest_revision: int,
    output_path: str = TARGET_DB_PATH,
) -> dict[str, int]:
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    tmp_path = output_path + ".tmp"
    if os.path.exists(tmp_path):
        os.remove(tmp_path)

    con = sqlite3.connect(tmp_path)
    cur = con.cursor()
    cur.execute("PRAGMA journal_mode = DELETE")
    cur.execute("PRAGMA synchronous = OFF")
    cur.execute("PRAGMA foreign_keys = OFF")
    for ddl in DDL_STATEMENTS:
        cur.execute(ddl)
    cur.execute(f"PRAGMA user_version = {ROOM_SCHEMA_VERSION}")

    # Introspect exact column order for each of the 28 spec tables from the newly created DDL
    table_columns: dict[str, list[str]] = {}
    for _, (tbl, _) in CATEGORY_SPEC_META.items():
        cols = [r[1] for r in cur.execute(f"PRAGMA table_info({tbl})").fetchall()]
        table_columns[tbl] = cols

    now_ms = int(time.time() * 1000)
    counts: dict[str, int] = {}

    item_tuples: list[tuple[Any, ...]] = []
    spec_batches: dict[str, list[tuple[Any, ...]]] = {tbl: [] for _, (tbl, _) in CATEGORY_SPEC_META.items()}

    for row in catalog_rows:
        cat = row.get("category")
        if cat not in CATEGORY_SPEC_META:
            continue
        tbl, pk_col = CATEGORY_SPEC_META[cat]
        spec = dict(row.get("spec_payload") or {})
        if not spec:
            continue
        if cat == "GPU":
            repair_gpu_spec(row["brand"], row["model"], spec)

        primary_img, gallery_imgs = sanitize_item_images(row.get("image_url"), row.get("images"))

        item_tuples.append(
            (
                row["id"],
                row["brand"],
                row["model"],
                cat,
                None,  # price_minor
                "TRY",
                now_ms,
                None,  # current_price_try (isolated in hardware_prices when pricing enabled)
                None,
                None,
                None,
                None,
                row.get("benchmark_score"),
                row.get("benchmark_tier"),
                row.get("benchmark_test_suite"),
                row.get("benchmark_gaming_index"),
                json.dumps(row["cpu_synthetic"], ensure_ascii=False) if row.get("cpu_synthetic") is not None else None,
                json.dumps(row["gpu_synthetic"], ensure_ascii=False) if row.get("gpu_synthetic") is not None else None,
                json.dumps(row["game_fps"], ensure_ascii=False) if row.get("game_fps") is not None else None,
                primary_img,
                json.dumps(gallery_imgs, ensure_ascii=False) if gallery_imgs else None,
                json.dumps(row.get("extra_specs") or {}, ensure_ascii=False) if row.get("extra_specs") else None,
                json.dumps(row.get("source_provenance") or ["HardwareWizard Katalog"], ensure_ascii=False),
                row.get("spec_verified_at") or datetime.now(timezone.utc).strftime("%Y-%m-%d"),
            )
        )

        cols = table_columns[tbl]
        spec_vals: list[Any] = []
        for col in cols:
            if col == pk_col:
                spec_vals.append(row["id"])
            else:
                v = spec.get(col)
                if col in BOOL_SPEC_COLUMNS and v is not None:
                    spec_vals.append(1 if v else 0)
                elif col in JSON_SPEC_COLUMNS:
                    spec_vals.append(json.dumps(v if v is not None else [], ensure_ascii=False))
                else:
                    spec_vals.append(v)
        spec_batches[tbl].append(tuple(spec_vals))
        counts[cat] = counts.get(cat, 0) + 1

    cur.executemany(
        """
        INSERT OR REPLACE INTO hardware_items (
            id, brand, model, category, price_minor, currency_code, cached_at,
            current_price_try, lowest_price_try, highest_price_try, offers, price_history,
            benchmark_score, benchmark_tier, benchmark_test_suite, benchmark_gaming_index,
            cpu_synthetic, gpu_synthetic, game_fps, image_url, images, extra_specs,
            source_provenance, spec_verified_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        item_tuples,
    )

    for tbl, batch in spec_batches.items():
        if not batch:
            continue
        cols = table_columns[tbl]
        placeholders = ",".join("?" for _ in cols)
        col_sql = ",".join(f"`{c}`" for c in cols)
        cur.executemany(f"INSERT OR REPLACE INTO `{tbl}` ({col_sql}) VALUES ({placeholders})", batch)

    # Stamp exact catalog version & Supabase delta revision into app_settings
    cur.executemany(
        "INSERT OR REPLACE INTO app_settings (`key`, `value`) VALUES (?, ?)",
        [
            ("catalog_seed_version", CATALOG_SCHEMA_VERSION),
            ("catalog_revision", str(latest_revision)),
            ("catalog_synced_source", "supabase"),
        ],
    )
    con.commit()
    cur.execute("PRAGMA foreign_keys = ON")
    fk_errors = cur.execute("PRAGMA foreign_key_check").fetchall()
    if fk_errors:
        con.close()
        raise RuntimeError(f"Foreign key integrity error in built Room DB: {fk_errors[:5]}")
    cur.execute("VACUUM")
    con.close()

    shutil.move(tmp_path, output_path)
    return counts


# ==============================================================================
# True Delta-Only Discovery Engine (Never touches existing locked Supabase rows)
# ==============================================================================
def _build_new_item_row(
    category: str,
    display_name: str,
    brand_hint: str | None,
    payload: dict[str, Any],
    extra_specs: dict[str, str],
    images: list[str],
    source_name: str,
    catalog_revision: int,
    verified_date: str,
) -> dict[str, Any] | None:
    """Validates & converts a newly discovered raw record into a strict Room/Supabase catalog row."""
    clean_name = re.sub(r"\s+", " ", (display_name or "").strip())
    if len(clean_name) < 4 or _is_stock_code(clean_name):
        return None
    if category not in CATEGORY_SPEC_META:
        return None

    brand, model = _extract_brand_and_model(clean_name)
    if brand == "Generic" and brand_hint and len(brand_hint.strip()) >= 2:
        brand = brand_hint.strip()
    if not brand or not model or len(model) < 2:
        return None

    item_id = _gen_hardware_id(category, f"{brand} {model}" if not model.lower().startswith(brand.lower()) else model)
    spec_table, _ = CATEGORY_SPEC_META[category]

    spec: dict[str, Any] | None = None
    if category == "CPU":
        cores = int(payload.get("core_count") or 0)
        tdp = int(payload.get("tdp") or 0)
        base_clk = float(payload.get("core_clock") or 0)
        boost_clk = float(payload.get("boost_clock") or 0)
        if cores <= 0 or tdp <= 0:
            return None
        base_mhz = int(base_clk * 1000) if 0 < base_clk < 20 else int(base_clk or 3000)
        boost_mhz = (int(boost_clk * 1000) if 0 < boost_clk < 20 else int(boost_clk)) if boost_clk > 0 else None
        arch = _clean_str(payload.get("microarchitecture"))
        u = f"{brand} {model} {arch or ''}".upper()
        if any(k in u for k in ("ZEN 5", "ZEN 4", "9950", "9900", "9800", "9700", "9600", "7950", "7900", "7800", "7700", "7600", "7500", "8700G", "8600G", "AM5")):
            socket = "AM5"
        elif any(k in u for k in ("CORE ULTRA", "ARROW LAKE", "285K", "265K", "245K", "LGA1851")):
            socket = "LGA1851"
        elif any(k in u for k in ("14900", "14700", "14600", "14400", "13900", "13700", "13600", "12900", "12700", "12400", "LGA1700")):
            socket = "LGA1700"
        elif "RYZEN" in u:
            socket = "AM4"
        else:
            raw_sock = (_clean_str(payload.get("socket")) or "AM5").upper().replace(" ", "").replace("SOCKET", "")
            socket = f"LGA{raw_sock}" if raw_sock in ("1851", "1700", "1200", "1151", "1150", "1155", "2066") else raw_sock
        spec = {
            "socket": socket,
            "core_count": cores,
            "thread_count": int(payload.get("thread_count") or (cores * 2 if cores >= 4 else cores)),
            "base_clock_mhz": base_mhz,
            "boost_clock_mhz": boost_mhz,
            "tdp_watts": tdp,
            "has_integrated_graphics": bool(payload.get("graphics")) and str(payload.get("graphics")).lower() != "none",
            "architecture": arch,
            "igpu_model": _clean_str(payload.get("graphics")),
            "release_date": _clean_str(payload.get("release_date")),
        }
    elif category == "GPU":
        chipset = _clean_str(payload.get("chipset")) or model
        mem_gb = float(payload.get("memory") or 0)
        if mem_gb <= 0:
            m = re.search(r"(\d+)\s*GB", clean_name, re.I)
            mem_gb = float(m.group(1)) if m else 0
        if mem_gb <= 0:
            return None
        vram_mb = int(mem_gb * 1024) if mem_gb < 256 else int(mem_gb)
        u = f"{clean_name} {chipset}".upper()
        is_blackwell_gddr7 = bool(re.search(r"\bRTX\s*50[5-9]0\b", u)) or "RTX PRO 6000" in u or "RTX PRO 5000" in u
        mem_type = (
            "GDDR7"
            if is_blackwell_gddr7
            else ("GDDR6X" if any(k in u for k in ("4090", "4080", "4070 TI", "4070 SUPER", "3090", "3080")) else "GDDR6")
        )
        core_clk = int(float(payload.get("core_clock") or 1800))
        boost_clk = int(float(payload.get("boost_clock") or 0)) or None
        tdp_val, psu_val = infer_gpu_tdp_and_psu(f"{brand} {model} {chipset}", vram_mb)
        spec = {
            "chipset": chipset,
            "vram_mb": vram_mb,
            "memory_type": mem_type,
            "core_clock_mhz": core_clk if core_clk > 0 else 1800,
            "boost_clock_mhz": boost_clk,
            "tdp_watts": int(payload.get("tdp") or tdp_val),
            "recommended_psu_watts": psu_val,
            "length_mm": int(float(payload["length"])) if payload.get("length") else None,
        }
        repair_gpu_spec(brand, model, spec)
    elif category == "RAM":
        spd = payload.get("speed")
        gen = "DDR5"
        mts = 6000
        if isinstance(spd, list) and len(spd) >= 2:
            gen = f"DDR{spd[0]}" if str(spd[0]) in ("3", "4", "5") else "DDR5"
            mts = int(spd[1])
        elif isinstance(spd, (int, float)) and spd >= 1600:
            mts = int(spd)
            gen = "DDR5" if mts >= 4800 else "DDR4"
        else:
            return None
        mods = payload.get("modules")
        mod_cnt, cap_gb = (int(mods[0]), int(mods[1])) if isinstance(mods, list) and len(mods) >= 2 else (2, 16)
        spec = {
            "generation": gen if gen in ("DDR3", "DDR4", "DDR5") else "DDR5",
            "speed_mts": mts,
            "cas_latency": int(payload.get("cas_latency") or (36 if gen == "DDR5" else 16)),
            "capacity_mb": mod_cnt * cap_gb * 1024,
            "module_count": mod_cnt,
            "first_word_latency_ns": payload.get("first_word_latency"),
        }
    elif category == "MOTHERBOARD":
        raw_sock = _clean_str(payload.get("socket"))
        if not raw_sock:
            return None
        compact_sock = raw_sock.upper().replace(" ", "").replace("SOCKET", "")
        sock = f"LGA{compact_sock}" if compact_sock in ("1851", "1700", "1200", "1151", "1150", "1155", "2066") else raw_sock
        ff_raw = str(payload.get("form_factor") or "ATX").upper()
        ff = "E_ATX" if "E-ATX" in ff_raw or "EATX" in ff_raw else ("MINI_ITX" if "ITX" in ff_raw else ("MICRO_ATX" if "MICRO" in ff_raw else "ATX"))
        ddr = "DDR5" if any(k in sock.upper() for k in ("AM5", "LGA1851")) else "DDR4"
        pcie = "GEN5" if ddr == "DDR5" else "GEN4"
        chip_m = re.search(r"\b([XZBHAB]\d{3}E?)\b", clean_name, re.I)
        spec = {
            "socket": sock,
            "chipset": chip_m.group(1).upper() if chip_m else sock,
            "form_factor": ff,
            "memory_generation": ddr,
            "ram_slots": int(payload.get("memory_slots") or 4),
            "pcie_generation": pcie,
            "max_memory_gb": int(payload.get("max_memory") or 128),
        }
    elif category == "PSU":
        watt = int(payload.get("wattage") or 0)
        if watt < 300:
            return None
        eff_raw = str(payload.get("efficiency") or "GOLD").upper()
        eff = next((e for e in ("TITANIUM", "PLATINUM", "GOLD", "SILVER", "BRONZE") if e in eff_raw), "STANDARD")
        is_atx3 = "ATX 3" in clean_name.upper() or watt >= 850
        spec = {
            "wattage": watt,
            "efficiency": eff,
            "is_atx3_compliant": is_atx3,
            "supports_pcie5_power": is_atx3,
            "modularity": _clean_str(payload.get("modular")),
            "form_factor": _clean_str(payload.get("type")) or "ATX",
        }
    elif category == "STORAGE":
        cap = float(payload.get("capacity") or 0)
        if cap <= 0:
            return None
        cap_mb = int(cap * 1000) if cap < 50000 else int(cap)
        iface_raw = str(payload.get("interface") or "").upper()
        is_nvme = "NVME" in iface_raw or "PCIE" in iface_raw or "M.2" in str(payload.get("form_factor") or "").upper()
        is_hdd = "HDD" in str(payload.get("type") or "").upper() or str(payload.get("type") or "").isdigit()
        pcie = "GEN5" if "5.0" in iface_raw else ("GEN4" if "4.0" in iface_raw else ("GEN3" if is_nvme else None))
        spec = {
            "media_type": "HDD" if is_hdd else "SSD",
            "storage_interface": "NVME" if is_nvme else "SATA",
            "form_factor": "M2_2280" if is_nvme else ("INCH_3_5" if is_hdd else "INCH_2_5"),
            "pcie_generation": pcie,
            "capacity_mb": cap_mb,
            "seq_read_mbps": 7000 if pcie == "GEN4" else (12000 if pcie == "GEN5" else (3500 if is_nvme else 550)),
            "seq_write_mbps": 6000 if pcie == "GEN4" else (10000 if pcie == "GEN5" else (3000 if is_nvme else 500)),
        }
    elif category == "COOLER":
        rad = payload.get("size")
        rad_mm = int(rad) if isinstance(rad, (int, float)) and rad >= 120 else None
        is_liq = rad_mm is not None and rad_mm >= 120
        spec = {
            "type": "LIQUID" if is_liq else "AIR",
            "radiator_size_mm": rad_mm,
            "max_cooling_tdp_watts": 300 if (rad_mm or 0) >= 360 else (250 if is_liq else 220),
            "supported_sockets": ["AM5", "AM4", "LGA1851", "LGA1700"],
            "fan_rpm": _clean_str(payload.get("fan_rpm")),
            "noise_dba": float(payload["noise_level"]) if payload.get("noise_level") is not None else None,
        }
    elif category == "CASE":
        spec = {
            "type": _clean_str(payload.get("type")) or "ATX Mid Tower",
            "max_gpu_length_mm": 400,
            "max_cpu_cooler_height_mm": 170,
            "max_radiator_size_mm": 360,
            "side_panel": _clean_str(payload.get("side_panel")),
            "drive_bays": _clean_str(payload.get("drive_bays")),
        }
    elif category == "CASE_FAN":
        sz = int(payload.get("fan_size_mm") or 120)
        spec = {
            "fan_size_mm": sz,
            "min_rpm": int(payload.get("min_rpm") or 500),
            "max_rpm": int(payload.get("max_rpm") or 1800),
            "max_airflow_cfm": float(payload.get("max_airflow_cfm") or 60.0),
            "static_pressure_mmh2o": float(payload.get("static_pressure_mmh2o") or 2.0),
            "noise_level_dba": float(payload.get("noise_level_dba") or 25.0),
            "bearing_type": "FDB",
            "pwm_pins": int(payload.get("pwm_pins") or 4),
            "is_daisy_chain": False,
            "argb_type": "ARGB" if "RGB" in clean_name.upper() else "Yok",
        }
    else:
        # Other categories (SMARTPHONE/TABLET/PERIPHERALS/NETWORK) require complete verified payload
        return None

    primary_img, gallery_imgs = sanitize_item_images(images[0] if images else None, images)
    return {
        "id": item_id,
        "category": category,
        "brand": brand,
        "model": model,
        "canonical_key": normalize_canonical_key(f"{brand} {model}"),
        "spec_table": spec_table,
        "spec_payload": spec,
        "benchmark_score": None,
        "benchmark_tier": None,
        "benchmark_test_suite": None,
        "benchmark_gaming_index": None,
        "cpu_synthetic": None,
        "gpu_synthetic": None,
        "game_fps": None,
        "image_url": primary_img,
        "images": gallery_imgs,
        "extra_specs": extra_specs or {},
        "source_provenance": ["HardwareWizard Katalog", source_name],
        "spec_verified_at": verified_date,
        "spec_locked": True,
        "is_active": True,
        "catalog_revision": catalog_revision,
    }


def run_seed_from_room(client: SupabaseAdminClient, force: bool = False) -> int:
    meta = client.get_catalog_meta()
    existing_total = int(meta.get("total_items") or 0)
    if existing_total > 0 and not force:
        print(f"[SEED] Supabase hardware_catalog already contains {existing_total} items (rev={meta.get('latest_revision')}). Use --force to re-seed.")
        return 0

    print(f"[SEED] Reading verified Room DB: {TARGET_DB_PATH}")
    rows, repaired_gpus = extract_rows_from_room_db(TARGET_DB_PATH, catalog_revision=max(1, int(meta.get("latest_revision") or 1)))
    print(f"[SEED] Extracted {len(rows)} rows across 28 categories (auto-repaired {repaired_gpus} 0W TDP GPUs).")

    written = client.upsert_catalog_batches(rows, batch_size=200, ignore_duplicates=False)
    updated_meta = client.refresh_meta(new_revision=max(1, int(meta.get("latest_revision") or 1)))
    client.log_crawler_run(
        run_mode="seed-from-room",
        new_items_added=written,
        sources_checked=1,
        catalog_revision=int(updated_meta.get("latest_revision") or 1),
        status="OK",
        notes=f"Initial authoritative seed from Room DB ({repaired_gpus} GPU TDPs repaired)",
    )
    print(f"[SEED] Done! Supabase now holds {updated_meta.get('total_items')} active items at revision {updated_meta.get('latest_revision')}.")
    return 0


# Modern-generation regex filters so daily delta NEVER imports 2011-2021 legacy hardware (e.g. AMD Sempron/A10/FX/DDR3)
_MODERN_CPU_RE = re.compile(
    r"(?:"
    r"RYZEN\s+(?:[3579]|AI\s+MAX)\s+(?:[789]\d{3}|10\d{3})|"
    r"THREADRIPPER\s+(?:PRO\s+)?[79]\d{3}|"
    r"CORE\s+ULTRA\s+[579]\s+[234]\d{2}|"
    r"CORE\s+I[3579]-1[34]\d{3}"
    r")",
    re.I,
)
_LEGACY_OR_MOBILE_CPU_RE = re.compile(
    r"(?:\b(?:ATHLON|SEMPRON|FX-|OPTERON|PENTIUM|CELERON|A[468]-|A10-|A12-|CORE\s+2|XEON\s+E[357])\b|\b\d{4}(?:U|HS|HX|H|V)\b)",
    re.I,
)
_MODERN_GPU_RE = re.compile(
    r"(?:RTX\s*(?:40|50|60)[5-9]0|RX\s*(?:7[6-9]|8\d|90)\d{2}|ARC\s*[BC][57]\d{2}|RTX\s*[456]000\s*ADA|RTX\s*PRO\s*[456]000)",
    re.I,
)
_LEGACY_QUADRO_RE = re.compile(r"\bQUADRO\b", re.I)
_MODERN_MOBO_CHIPSET_RE = re.compile(
    r"\b(?:X870E?|B850|B840|Z890|B860|H810|X670E?|B650E?|Z790|B760|TRX50|WRX90)\b",
    re.I,
)
_FLAGSHIP_LAUNCH_RE = re.compile(
    r"(?:RTX\s*(?:50|60)\d{2}|RX\s*90\d{2}|RYZEN\s+[579]\s+(?:9|10)\d{3}|CORE\s+ULTRA\s+[579]\s+[234]\d{2}|ARC\s*B[57]\d{2})",
    re.I,
)

# Retail stores that only publish marketing titles + product photos (no structured specs).
# They are used EXCLUSIVELY for image gallery enrichment (`gallery_by_key`), NEVER for creating new catalog rows.
IMAGE_ONLY_RETAIL_SOURCES = {
    "gaminggen store api",
    "gaminggen",
    "hepsiburada",
    "trendyol",
    "itopya",
    "itopya store",
    "vatan",
    "sinerji",
    "incehesap",
}

_RETAIL_SEO_TITLE_RE = re.compile(
    r"(?:EKRAN\s+KART|İŞLEMCİ\s+(?:SIVI|HAVA)?\s*SOĞUT|INTEL-AMD\s+UYUML|DLSS\s*4\b|\bBULK\b|\b128\s*BIT\b|\b256\s*BIT\b|\b192\s*BIT\b|OYUNCU\s+KASAS)",
    re.I,
)


def is_image_only_retail_source(source_name: str) -> bool:
    return (source_name or "").strip().lower() in IMAGE_ONLY_RETAIL_SOURCES


def passes_modern_quality_gate(
    category: str,
    brand: str,
    model: str,
    spec: dict[str, Any],
    verified_image_count: int,
    source_name: str = "",
) -> bool:
    """Strict delta quality gate: admits ONLY modern-generation, clean, structured hardware items."""
    if is_image_only_retail_source(source_name):
        return False
    full_name = f"{brand} {model}".strip()
    if len(full_name) > 85 or _RETAIL_SEO_TITLE_RE.search(full_name):
        return False
    if category == "CPU":
        if _LEGACY_OR_MOBILE_CPU_RE.search(full_name):
            return False
        if not _MODERN_CPU_RE.search(full_name):
            return False
        return verified_image_count >= 1 or bool(_FLAGSHIP_LAUNCH_RE.search(full_name))
    if category == "GPU":
        chip_full = f"{full_name} {spec.get('chipset') or ''}"
        if _LEGACY_QUADRO_RE.search(chip_full):
            return False
        if not _MODERN_GPU_RE.search(chip_full):
            return False
        if int(spec.get("vram_mb") or 0) < 8192 or int(spec.get("tdp_watts") or 0) < 100:
            return False
        return verified_image_count >= 1 or bool(_FLAGSHIP_LAUNCH_RE.search(chip_full))
    if category == "MOTHERBOARD":
        sock = str(spec.get("socket") or "").upper()
        if sock not in ("AM5", "LGA1851", "LGA1700", "STR5"):
            return False
        if not _MODERN_MOBO_CHIPSET_RE.search(f"{full_name} {spec.get('chipset') or ''}"):
            return False
        return verified_image_count >= 1
    if category == "RAM":
        if str(spec.get("generation") or "") != "DDR5":
            return False
        if int(spec.get("speed_mts") or 0) < 5600 or int(spec.get("capacity_mb") or 0) < 16384:
            return False
        return verified_image_count >= 1
    if category == "PSU":
        if int(spec.get("wattage") or 0) < 650 or not spec.get("is_atx3_compliant"):
            return False
        if str(spec.get("efficiency") or "") not in ("GOLD", "PLATINUM", "TITANIUM"):
            return False
        return verified_image_count >= 1
    if category == "STORAGE":
        if str(spec.get("storage_interface") or "") != "NVME":
            return False
        if str(spec.get("pcie_generation") or "") not in ("GEN4", "GEN5"):
            return False
        if int(spec.get("capacity_mb") or 0) < 1_000_000:
            return False
        return verified_image_count >= 1
    if category == "COOLER":
        has_structured = bool(spec.get("radiator_size_mm") or spec.get("height_mm"))
        return has_structured and verified_image_count >= 1
    if category == "CASE":
        has_structured = bool(spec.get("max_gpu_length_mm") and spec.get("max_cpu_cooler_height_mm"))
        return has_structured and verified_image_count >= 1
    if category == "CASE_FAN":
        return int(spec.get("fan_size_mm") or 0) >= 80 and verified_image_count >= 1
    return False


def run_delta_sync(client: SupabaseAdminClient, gsmarena_limit: int = 10, max_new_per_run: int = 50) -> int:
    """True delta-only discovery: fetches only existing keys from Supabase and inserts ONLY brand-new products."""
    meta = client.get_catalog_meta()
    current_rev = int(meta.get("latest_revision") or 0)
    existing_ids, existing_ckeys = client.fetch_existing_keys()

    if not existing_ids:
        print("[DELTA] Supabase hardware_catalog is empty — running initial seed from Room DB first...")
        return run_seed_from_room(client, force=True)

    print(f"[DELTA] Loaded {len(existing_ids)} existing locked items from Supabase (rev={current_rev}).")

    # Run lightweight Tier 1 + polite Tier 2 discovery into temporary local SQLite state
    from scripts.crawlers.http_client import PoliteHttpClient
    from scripts.crawlers.sources_images import crawl_gaming_gen_store
    from scripts.crawlers.sources_mobile_laptop import crawl_mobile_and_laptop
    from scripts.crawlers.sources_network_ups import crawl_networking_and_ups
    from scripts.crawlers.sources_pc import crawl_pc_components
    from scripts.crawlers.sources_peripherals import crawl_peripherals
    from scripts.crawlers.state_manager import CrawlStateManager

    http = PoliteHttpClient()
    state = CrawlStateManager()
    sources_checked = 0

    for label, fn in (
        ("PC (Tier 1/2)", lambda: crawl_pc_components(http, state, delta_only=True)),
        ("Peripherals (Tier 1)", lambda: crawl_peripherals(http, state, max_shopify_pages=1, delta_only=True)),
        ("Network & UPS (Tier 1)", lambda: crawl_networking_and_ups(http, state, max_nut_vendors=3, delta_only=True)),
        ("Mobile & Laptop (Tier 1/2)", lambda: crawl_mobile_and_laptop(http, state, max_gsmarena_devices=gsmarena_limit, delta_only=True)),
        ("GamingGen Gallery API", lambda: crawl_gaming_gen_store(http, state, max_pages_per_cat=1, delta_only=True)),
    ):
        try:
            res = fn()
            sources_checked += 1
            print(f"  [SOURCE OK] {label}: {res}")
        except Exception as exc:
            print(f"  [SOURCE SKIP] {label}: {exc}")

    # Filter strictly for genuinely NEW items not in Supabase (never mutate locked rows)
    verified_date = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    next_rev = current_rev + 1
    new_rows: list[dict[str, Any]] = []
    seen_new_ids: set[str] = set()
    seen_new_ckeys: set[tuple[str, str]] = set()

    # Build quick gallery lookup from GamingGen / store records for newly discovered items
    gallery_by_key: dict[tuple[str, str], list[str]] = {}
    for row in state.conn.execute("SELECT category, canonical_key, images_json FROM raw_records WHERE images_json != '[]'").fetchall():
        imgs = _safe_json_loads(row["images_json"], [])
        if imgs:
            gallery_by_key.setdefault((row["category"], row["canonical_key"]), []).extend(imgs)

    # Only consider authoritative structured PC categories for automatic schema-verified insertion
    # (prevents unverified noise from entering production catalog without full specs)
    for cat in ("CPU", "GPU", "RAM", "MOTHERBOARD", "PSU", "STORAGE", "COOLER", "CASE", "CASE_FAN"):
        for rec in state.load_records_for_category(cat):
            # Never create new catalog entries from retail-only SEO stores (they only supply gallery images)
            if is_image_only_retail_source(rec["source_name"]):
                continue
            ckey = rec["canonical_key"]
            if (cat, ckey) in existing_ckeys or (cat, ckey) in seen_new_ckeys:
                continue
            payload = _safe_json_loads(rec["payload_json"], {})
            if not payload:
                continue
            extra = _safe_json_loads(rec["extra_specs_json"], {})
            imgs = _safe_json_loads(rec["images_json"], []) + gallery_by_key.get((cat, ckey), [])

            candidate = _build_new_item_row(
                category=cat,
                display_name=rec["display_name"],
                brand_hint=rec["brand"],
                payload=payload,
                extra_specs=extra,
                images=imgs,
                source_name=rec["source_name"],
                catalog_revision=next_rev,
                verified_date=verified_date,
            )
            if not candidate:
                continue
            if candidate["id"] in existing_ids or candidate["id"] in seen_new_ids:
                continue
            if (cat, candidate["canonical_key"]) in existing_ckeys or (cat, candidate["canonical_key"]) in seen_new_ckeys:
                continue

            # Verify live HTTP 200 image URLs ONLY for truly new candidates that pass initial modern checks
            if not passes_modern_quality_gate(
                cat,
                candidate["brand"],
                candidate["model"],
                candidate["spec_payload"],
                verified_image_count=len(candidate["images"]),
                source_name=rec["source_name"],
            ):
                continue

            verified_imgs: list[str] = []
            for img_u in candidate["images"][:5]:
                ok, _, _ = http.check_image_url(img_u)
                if ok:
                    verified_imgs.append(img_u)
            if not passes_modern_quality_gate(
                cat,
                candidate["brand"],
                candidate["model"],
                candidate["spec_payload"],
                verified_image_count=len(verified_imgs),
                source_name=rec["source_name"],
            ):
                continue
            candidate["images"] = verified_imgs
            candidate["image_url"] = verified_imgs[0] if verified_imgs else None

            seen_new_ids.add(candidate["id"])
            seen_new_ckeys.add((cat, candidate["canonical_key"]))
            new_rows.append(candidate)
            if len(new_rows) >= max_new_per_run:
                break
        if len(new_rows) >= max_new_per_run:
            break

    state.close()

    if not new_rows:
        # Touch catalog_meta.last_checked_at so Supabase Free Tier stays permanently awake
        updated = client.refresh_meta(new_revision=None)
        client.log_crawler_run(
            run_mode="delta",
            new_items_added=0,
            sources_checked=sources_checked,
            catalog_revision=int(updated.get("latest_revision") or current_rev),
            status="OK",
            notes="No new hardware products detected; existing locked items untouched.",
        )
        print(f"[DELTA] 0 new hardware products detected. Existing {len(existing_ids)} locked items untouched. (rev={current_rev})")
        return 0

    inserted = client.upsert_catalog_batches(new_rows, batch_size=100, ignore_duplicates=True)
    updated = client.refresh_meta(new_revision=next_rev)
    sample_names = ", ".join(f"{r['brand']} {r['model']}" for r in new_rows[:5])
    client.log_crawler_run(
        run_mode="delta",
        new_items_added=inserted,
        sources_checked=sources_checked,
        catalog_revision=int(updated.get("latest_revision") or next_rev),
        status="OK",
        notes=f"Added {inserted} new items (rev={next_rev}): {sample_names}",
    )
    print(f"[DELTA] Inserted {inserted} brand-new hardware items at revision {next_rev}! Sample: {sample_names}")
    return 0


def run_build_room_snapshot(client: SupabaseAdminClient, output_path: str = TARGET_DB_PATH) -> int:
    meta = client.get_catalog_meta()
    latest_rev = int(meta.get("latest_revision") or 0)
    if latest_rev <= 0:
        print("[SNAPSHOT] Supabase catalog_meta has latest_revision=0; skipping snapshot build.")
        return 1

    print(f"[SNAPSHOT] Pulling authoritative catalog from Supabase (rev={latest_rev})...")
    rows = client.fetch_all_active_catalog(page_size=1000)
    if len(rows) < 1000:
        raise RuntimeError(f"Safety abort: Supabase returned only {len(rows)} rows (expected >= 1000).")

    counts = build_room_db_from_catalog_rows(rows, latest_revision=latest_rev, output_path=output_path)
    total = sum(counts.values())
    size_mb = os.path.getsize(output_path) / (1024 * 1024)
    print(f"[SNAPSHOT] Built clean Room v{ROOM_SCHEMA_VERSION} DB ({total} items, rev={latest_rev}, {size_mb:.2f} MB) -> {output_path}")
    return 0


def run_repair_local_room(output_path: str = TARGET_DB_PATH) -> int:
    """Offline validation & repair: repairs 0W GPU TDPs, validates all 28 tables, stamps v5.0.0 & rev=1."""
    rows, repaired_gpus = extract_rows_from_room_db(output_path, catalog_revision=1)
    counts = build_room_db_from_catalog_rows(rows, latest_revision=1, output_path=output_path)
    total = sum(counts.values())
    size_mb = os.path.getsize(output_path) / (1024 * 1024)
    print(
        f"[REPAIR-LOCAL] Verified & rebuilt {output_path}: "
        f"{total} items across {len(counts)} categories, "
        f"{repaired_gpus} GPU 0W TDPs fixed, stamped v{CATALOG_SCHEMA_VERSION} rev=1 ({size_mb:.2f} MB)."
    )
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description="HardwareWizard Supabase Authoritative Catalog & Delta Sync")
    parser.add_argument(
        "--action",
        choices=("seed-from-room", "delta", "build-room-snapshot", "repair-local-room"),
        required=True,
    )
    parser.add_argument("--force", action="store_true", help="Force re-seed even if Supabase already has rows")
    parser.add_argument("--gsmarena-limit", type=int, default=10, help="Max GSMArena pages in delta mode")
    parser.add_argument("--max-new", type=int, default=50, help="Safety cap on new items per daily run")
    parser.add_argument("--output-db", default=TARGET_DB_PATH, help="Target Room SQLite DB path")
    args = parser.parse_args()

    if args.action == "repair-local-room":
        return run_repair_local_room(output_path=args.output_db)

    supabase_url = (os.environ.get("SUPABASE_URL") or "").strip()
    service_key = (os.environ.get("SUPABASE_SERVICE_ROLE_KEY") or "").strip()
    if not supabase_url or not service_key:
        print(
            "[SETUP REQUIRED] SUPABASE_URL veya SUPABASE_SERVICE_ROLE_KEY secret'ları henüz GitHub Actions'a eklenmemiş.\n"
            "  GitHub -> Settings -> Secrets and variables -> Actions -> New repository secret:\n"
            "    1. SUPABASE_URL = https://<proje-ref>.supabase.co\n"
            "    2. SUPABASE_SERVICE_ROLE_KEY = <Supabase Project Settings -> API -> service_role key>"
        )
        return 0

    client = SupabaseAdminClient(supabase_url, service_key)
    if args.action == "seed-from-room":
        return run_seed_from_room(client, force=args.force)
    if args.action == "delta":
        return run_delta_sync(client, gsmarena_limit=args.gsmarena_limit, max_new_per_run=args.max_new)
    if args.action == "build-room-snapshot":
        return run_build_room_snapshot(client, output_path=args.output_db)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
