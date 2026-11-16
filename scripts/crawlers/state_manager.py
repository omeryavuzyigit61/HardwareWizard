"""SQLite-backed checkpoint and cross-source raw ingestion store."""

from __future__ import annotations

import json
import os
import re
import sqlite3
from datetime import datetime, timezone
from typing import Any, Optional

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
DEFAULT_STATE_DB = os.path.join(ROOT_DIR, "scripts", "cache", "crawl_state.db")

_BRAND_PREFIXES = (
    "amd ", "intel ", "nvidia ", "asus ", "msi ", "gigabyte ", "asrock ", "evga ",
    "zotac ", "sapphire ", "xfx ", "powercolor ", "pny ", "palit ", "gainward ",
    "inno3d ", "galax ", "kfa2 ", "corsair ", "g.skill ", "kingston ", "crucial ",
    "teamgroup ", "team ", "patriot ", "adata ", "xpg ", "samsung ", "western digital ",
    "wd ", "seagate ", "sabrent ", "solidigm ", "sk hynix ", "kioxia ", "lexar ",
    "seasonic ", "be quiet! ", "thermaltake ", "cooler master ", "evga ", "fsp ",
    "super flower ", "nzxt ", "lian li ", "fractal design ", "phanteks ", "noctua ",
    "arctic ", "thermalright ", "deepcool ", "ek ", "logitech ", "logitech g ",
    "razer ", "steelseries ", "hyperx ", "keychron ", "wooting ", "pulsar ", "lamzu ",
    "endgame gear ", "zowie ", "benq ", "lg ", "dell ", "alienware ", "aoc ", "acer ",
    "lenovo ", "hp ", "apple ", "google ", "xiaomi ", "oneplus ", "oppo ", "vivo ",
    "motorola ", "sony ", "garmin ", "huawei ", "tp-link ", "ubiquiti ", "mikrotik ",
    "netgear ", "keenetic ", "apc ", "cyberpower ", "eaton ", "secretlab ", "flexispot ",
    "rode ", "røde ", "shure ", "elgato ", "govee ", "nanoleaf ", "philips ",
)


def utc_now_iso() -> str:
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat()


def normalize_canonical_key(text: str) -> str:
    """Normalizes hardware model strings across heterogeneous sources for joining."""
    raw = (text or "").lower().strip()
    raw = raw.replace("®", "").replace("™", "").replace("with radeon graphics", "")
    raw = raw.replace("geforce ", "").replace("radeon ", "").replace("ryzen ", "ryzen ")
    raw = re.sub(r"\([^)]*\)", " ", raw)
    raw = re.sub(r"[^a-z0-9]+", " ", raw)
    tokens = [t for t in raw.split() if t not in {"processor", "cpu", "gpu", "graphics", "card", "edition", "gaming", "oc"}]
    return "_".join(tokens)


def normalize_for_brand_match(text: str) -> str:
    """Strips common leading vendor prefixes when matching cross-source OEM chipsets."""
    lower = (text or "").lower().strip()
    for prefix in _BRAND_PREFIXES:
        if lower.startswith(prefix):
            return normalize_canonical_key(lower[len(prefix):])
    return normalize_canonical_key(lower)


class CrawlStateManager:
    """Persistent checkpoint DB enabling multi-hour resumable crawls and delta updates."""

    def __init__(self, db_path: str = DEFAULT_STATE_DB) -> None:
        self.db_path = db_path
        os.makedirs(os.path.dirname(db_path), exist_ok=True)
        self.conn = sqlite3.connect(db_path)
        self.conn.row_factory = sqlite3.Row
        self._init_schema()

    def _init_schema(self) -> None:
        self.conn.executescript(
            """
            PRAGMA journal_mode = WAL;
            CREATE TABLE IF NOT EXISTS crawl_progress (
                task_key TEXT PRIMARY KEY,
                status TEXT NOT NULL,
                cursor TEXT,
                etag TEXT,
                last_modified TEXT,
                note TEXT,
                updated_at TEXT NOT NULL
            );
            CREATE TABLE IF NOT EXISTS raw_records (
                category TEXT NOT NULL,
                source_name TEXT NOT NULL,
                canonical_key TEXT NOT NULL,
                display_name TEXT NOT NULL,
                brand TEXT,
                payload_json TEXT NOT NULL,
                extra_specs_json TEXT NOT NULL DEFAULT '{}',
                images_json TEXT NOT NULL DEFAULT '[]',
                source_url TEXT,
                fetched_at TEXT NOT NULL,
                PRIMARY KEY (category, source_name, canonical_key)
            );
            CREATE INDEX IF NOT EXISTS idx_raw_cat_key ON raw_records(category, canonical_key);
            CREATE TABLE IF NOT EXISTS source_audit (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                source_name TEXT NOT NULL,
                status TEXT NOT NULL,
                item_count INTEGER NOT NULL DEFAULT 0,
                note TEXT,
                logged_at TEXT NOT NULL
            );
            CREATE TABLE IF NOT EXISTS image_health (
                url TEXT PRIMARY KEY,
                ok INTEGER NOT NULL,
                status_code INTEGER,
                content_type TEXT,
                checked_at TEXT NOT NULL
            );
            """
        )
        self.conn.commit()

    def get_progress(self, task_key: str) -> Optional[sqlite3.Row]:
        return self.conn.execute(
            "SELECT * FROM crawl_progress WHERE task_key = ?", (task_key,)
        ).fetchone()

    def save_progress(
        self,
        task_key: str,
        status: str,
        cursor: str = "",
        etag: str | None = None,
        last_modified: str | None = None,
        note: str = "",
    ) -> None:
        self.conn.execute(
            """
            INSERT INTO crawl_progress (task_key, status, cursor, etag, last_modified, note, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(task_key) DO UPDATE SET
                status = excluded.status,
                cursor = excluded.cursor,
                etag = COALESCE(excluded.etag, crawl_progress.etag),
                last_modified = COALESCE(excluded.last_modified, crawl_progress.last_modified),
                note = excluded.note,
                updated_at = excluded.updated_at
            """,
            (task_key, status, cursor, etag, last_modified, note, utc_now_iso()),
        )
        self.conn.commit()

    def upsert_raw_batch(self, rows: list[dict[str, Any]]) -> int:
        if not rows:
            return 0
        now = utc_now_iso()
        params = [
            (
                r["category"],
                r["source_name"],
                r["canonical_key"],
                r["display_name"],
                r.get("brand"),
                json.dumps(r.get("payload") or {}, ensure_ascii=False),
                json.dumps(r.get("extra_specs") or {}, ensure_ascii=False),
                json.dumps(r.get("images") or [], ensure_ascii=False),
                r.get("source_url"),
                now,
            )
            for r in rows
            if r.get("canonical_key") and r.get("display_name")
        ]
        self.conn.executemany(
            """
            INSERT INTO raw_records (
                category, source_name, canonical_key, display_name, brand,
                payload_json, extra_specs_json, images_json, source_url, fetched_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(category, source_name, canonical_key) DO UPDATE SET
                display_name = excluded.display_name,
                brand = COALESCE(excluded.brand, raw_records.brand),
                payload_json = excluded.payload_json,
                extra_specs_json = excluded.extra_specs_json,
                images_json = excluded.images_json,
                source_url = COALESCE(excluded.source_url, raw_records.source_url),
                fetched_at = excluded.fetched_at
            """,
            params,
        )
        self.conn.commit()
        return len(params)

    def log_audit(
        self, category: str, source_name: str, status: str, item_count: int = 0, note: str = ""
    ) -> None:
        self.conn.execute(
            """
            INSERT INTO source_audit (category, source_name, status, item_count, note, logged_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            (category, source_name, status, item_count, note, utc_now_iso()),
        )
        self.conn.commit()

    def load_records_for_category(self, category: str) -> list[sqlite3.Row]:
        return self.conn.execute(
            "SELECT * FROM raw_records WHERE category = ? ORDER BY source_name, canonical_key",
            (category,),
        ).fetchall()

    def has_source_key(self, category: str, source_name: str, canonical_key: str) -> bool:
        row = self.conn.execute(
            "SELECT 1 FROM raw_records WHERE category = ? AND source_name = ? AND canonical_key = ?",
            (category, source_name, canonical_key),
        ).fetchone()
        return row is not None

    def get_image_health(self, url: str) -> Optional[sqlite3.Row]:
        return self.conn.execute("SELECT * FROM image_health WHERE url = ?", (url,)).fetchone()

    def set_image_health(self, url: str, ok: bool, status_code: int, content_type: str = "") -> None:
        self.set_image_health_batch([(url, ok, status_code, content_type)])

    def load_image_health_map(self) -> dict[str, bool]:
        rows = self.conn.execute("SELECT url, ok FROM image_health").fetchall()
        return {r["url"]: bool(r["ok"]) for r in rows}

    def set_image_health_batch(self, items: list[tuple[str, bool, int, str]]) -> None:
        if not items:
            return
        now = utc_now_iso()
        self.conn.executemany(
            """
            INSERT INTO image_health (url, ok, status_code, content_type, checked_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(url) DO UPDATE SET
                ok = excluded.ok,
                status_code = excluded.status_code,
                content_type = excluded.content_type,
                checked_at = excluded.checked_at
            """,
            [(u, 1 if ok else 0, sc, ct, now) for u, ok, sc, ct in items],
        )
        self.conn.commit()

    def summary_counts(self) -> list[tuple[str, str, int]]:
        rows = self.conn.execute(
            "SELECT category, source_name, COUNT(*) as cnt FROM raw_records GROUP BY category, source_name ORDER BY category, cnt DESC"
        ).fetchall()
        return [(r["category"], r["source_name"], r["cnt"]) for r in rows]

    def close(self) -> None:
        self.conn.close()
