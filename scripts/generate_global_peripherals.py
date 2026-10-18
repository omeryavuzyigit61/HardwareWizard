#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/generate_global_peripherals.py
Generates the comprehensive Global Peripherals Catalog (2022+ models)
according to DisplaySpecifications, EloShapes, and Rtings standards.
"""

import os
import sys
import json
import re
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

from fetch_web_images import search_bing_images, verify_image_url
from peripherals_db import MONITORS_DATA, MICE_DATA, KEYBOARDS_DATA, HEADSETS_DATA

# Cache file for verified image URLs so repeat runs are instant
IMG_CACHE_FILE = os.path.join(ROOT_DIR, "scripts", "image_url_cache.json")
if os.path.exists(IMG_CACHE_FILE):
    try:
        with open(IMG_CACHE_FILE, "r", encoding="utf-8") as f:
            IMG_CACHE = json.load(f)
    except Exception:
        IMG_CACHE = {}
else:
    IMG_CACHE = {}

def save_img_cache():
    try:
        with open(IMG_CACHE_FILE, "w", encoding="utf-8") as f:
            json.dump(IMG_CACHE, f, ensure_ascii=False, indent=2)
    except Exception:
        pass

def fetch_verified_images(product_name: str, brand: str, category: str, fallback_urls: list[str]) -> list[str]:
    cache_key = f"{brand}_{product_name}".lower().strip()
    if cache_key in IMG_CACHE and len(IMG_CACHE[cache_key]) >= 2:
        # Verify first image is still valid
        if verify_image_url(IMG_CACHE[cache_key][0]):
            return IMG_CACHE[cache_key]

    valid_urls = []
    # Test fallbacks first
    for u in fallback_urls:
        if u and verify_image_url(u):
            if u not in valid_urls:
                valid_urls.append(u)
        if len(valid_urls) >= 4:
            break

    if len(valid_urls) < 3:
        # Search web for transparent/studio product images from different angles
        queries = [
            f"{brand} {product_name} front studio",
            f"{brand} {product_name} angle view studio",
            f"{brand} {product_name} back ports studio",
        ]
        for q in queries:
            try:
                found = search_bing_images(q, count=5)
                for u in found:
                    if u not in valid_urls and verify_image_url(u):
                        valid_urls.append(u)
                        break
            except Exception:
                pass
            if len(valid_urls) >= 4:
                break

    if not valid_urls and fallback_urls:
        valid_urls = [fallback_urls[0]]

    IMG_CACHE[cache_key] = valid_urls
    return valid_urls

def process_item(item: dict, category_name: str) -> dict:
    item_copy = dict(item)
    item_copy["category"] = category_name

    brand = item_copy.get("brand", "")
    model = item_copy.get("model", "")
    name = item_copy.get("name", "")

    existing_imgs = item_copy.get("images", [])
    if item_copy.get("image_url") and item_copy["image_url"] not in existing_imgs:
        existing_imgs = [item_copy["image_url"]] + existing_imgs

    # Fetch verified multi-angle images
    verified_imgs = fetch_verified_images(f"{brand} {model}", brand, category_name, existing_imgs)

    item_copy["images"] = verified_imgs
    item_copy["image_url"] = verified_imgs[0] if verified_imgs else ""

    # Price handling: strict null if no TR price
    price = item_copy.get("current_price_try")
    if price is not None:
        item_copy["current_price_try"] = float(price)
        item_copy["lowest_price_try"] = item_copy.get("lowest_price_try", float(price))
        item_copy["highest_price_try"] = item_copy.get("highest_price_try", float(price))
    else:
        item_copy["current_price_try"] = None
        item_copy["lowest_price_try"] = None
        item_copy["highest_price_try"] = None

    return item_copy

def main():
    print("=" * 60)
    print("GENERATING COMPREHENSIVE GLOBAL PERIPHERALS CATALOG (2022+)")
    print("=" * 60)

    all_tasks = []
    for item in MONITORS_DATA:
        all_tasks.append((item, "MONITOR"))
    for item in MICE_DATA:
        all_tasks.append((item, "MOUSE"))
    for item in KEYBOARDS_DATA:
        all_tasks.append((item, "KEYBOARD"))
    for item in HEADSETS_DATA:
        all_tasks.append((item, "HEADSET"))

    total = len(all_tasks)
    print(f"Total items to process: {total}")

    processed_items = []
    with ThreadPoolExecutor(max_workers=8) as executor:
        future_to_item = {
            executor.submit(process_item, item, cat): (item.get("id"), cat)
            for item, cat in all_tasks
        }
        done_count = 0
        for future in as_completed(future_to_item):
            pid, cat = future_to_item[future]
            try:
                result = future.result()
                processed_items.append(result)
                done_count += 1
                img_count = len(result.get("images", []))
                print(f"[{done_count}/{total}] ✓ [{cat}] {result.get('name')[:45]} ({img_count} imgs)")
                if done_count % 10 == 0:
                    save_img_cache()
            except Exception as e:
                print(f"Error processing {pid}: {e}")

    save_img_cache()

    # Sort processed items: by category then benchmark_score desc
    processed_items.sort(key=lambda x: (x.get("category", ""), -(x.get("benchmark_score") or 0)))

    # Save to data/catalog/peripherals.json
    out_cat = os.path.join(CATALOG_DIR, "peripherals.json")
    with open(out_cat, "w", encoding="utf-8") as f:
        json.dump(processed_items, f, ensure_ascii=False, indent=2)

    # Save to shared/src/commonMain/composeResources/files/peripherals.json
    out_compose = os.path.join(COMPOSE_DIR, "peripherals.json")
    with open(out_compose, "w", encoding="utf-8") as f:
        json.dump(processed_items, f, ensure_ascii=False, indent=2)

    print("\n" + "=" * 60)
    print(f"SUCCESS: Saved {len(processed_items)} verified global peripherals to:")
    print(f"  1. {out_cat}")
    print(f"  2. {out_compose}")
    print("=" * 60)

if __name__ == "__main__":
    main()
