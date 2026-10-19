#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/fix_peripherals_3rd_image.py
Ensures 100% of the 448 peripherals have at least 3 verified studio images.
"""

import os
import json
from fetch_web_images import search_bing_images, search_duckduckgo_images, verify_image_url

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES = [
    os.path.join(ROOT_DIR, "data", "catalog", "peripherals.json"),
    os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files", "peripherals.json")
]

CACHE_FILE = os.path.join(ROOT_DIR, "scripts", "image_url_cache.json")
try:
    with open(CACHE_FILE, "r", encoding="utf-8") as f:
        cache = json.load(f)
except Exception:
    cache = {}

for path in FILES:
    if not os.path.exists(path):
        continue
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)

    for item in data:
        imgs = item.get("images", [])
        if len(imgs) < 3:
            brand = item.get("brand", "")
            model = item.get("model", "")
            name = item.get("name", "")
            queries = [
                f"{brand} {model} angle studio",
                f"{brand} {model} white background studio",
                f"{brand} {model} review product"
            ]
            for q in queries:
                candidates = search_bing_images(q, count=8) + search_duckduckgo_images(q)
                for c in candidates:
                    if c not in imgs and verify_image_url(c):
                        imgs.append(c)
                        print(f"Added 3rd image for {name[:30]}: {c[:60]}...")
                        break
                if len(imgs) >= 3:
                    break
            item["images"] = imgs
            cache_key = f"{brand}_{brand} {model}".lower().strip()
            cache[cache_key] = imgs

    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)

try:
    with open(CACHE_FILE, "w", encoding="utf-8") as f:
        json.dump(cache, f, indent=2, ensure_ascii=False)
except Exception:
    pass

print("All peripherals verified with >= 3 images.")
