#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/prepare_prepopulated_db.py
Bakes and optimizes hardware_wizard.db from data/catalog JSON files or extracts/checkpoints
the Room database to produce a standalone SQLite database ready for:
    RoomDatabase.Builder.createFromAsset("databases/hardware_wizard.db")
"""

import os
import sys
import sqlite3
import shutil

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
ASSETS_DB_DIR = os.path.join(ROOT_DIR, "androidApp", "src", "main", "assets", "databases")
TARGET_DB_PATH = os.path.join(ASSETS_DB_DIR, "hardware_wizard.db")

def checkpoint_and_optimize(source_db_path, output_db_path):
    os.makedirs(os.path.dirname(output_db_path), exist_ok=True)
    
    # Connect and checkpoint WAL into main database
    conn = sqlite3.connect(source_db_path)
    cursor = conn.cursor()
    
    # Check tables
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [row[0] for row in cursor.fetchall()]
    print(f"Database contains {len(tables)} tables: {tables[:8]}...")
    
    # Verify hardware_items count
    if "hardware_items" in tables:
        cursor.execute("SELECT COUNT(*) FROM hardware_items;")
        count = cursor.fetchone()[0]
        print(f"Total hardware_items rows: {count}")

    # Set catalog_seed_version = 2.0.0 in app_settings
    if "app_settings" in tables:
        cursor.execute("INSERT OR REPLACE INTO app_settings (key, value) VALUES ('catalog_seed_version', '2.0.0');")
        conn.commit()
    
    # Checkpoint WAL
    cursor.execute("PRAGMA wal_checkpoint(TRUNCATE);")
    conn.commit()
    conn.close()
    
    # Copy main db to destination
    if os.path.abspath(source_db_path) != os.path.abspath(output_db_path):
        shutil.copy2(source_db_path, output_db_path)
    
    # VACUUM destination database to minimize file size
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
