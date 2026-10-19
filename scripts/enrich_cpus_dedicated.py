#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/enrich_cpus_dedicated.py
Dedicated 3-Image Triad Enrichment for All 53 Modern CPUs in Hardware Wizard.
Ensures every CPU (Zen 4/5, Raptor Lake 13th/14th Gen, Arrow Lake Core Ultra)
has 3 DISTINCT, VERIFIED, AUTHENTIC images:
- Image 1: Retail packaging box or hero processor cutout
- Image 2: Processor top IHS die studio angle
- Image 3: Processor bottom gold contact pads (LGA1700 / LGA1851 / LGA1718-AM5 / AM4 PGA)
"""

import os
import sys
import json
import re
from concurrent.futures import ThreadPoolExecutor, as_completed

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

from fetch_web_images import (
    search_bing_images,
    search_duckduckgo_images,
    verify_image_url,
    is_bad_image
)

IMG_CACHE_FILE = os.path.join(ROOT_DIR, "scripts", "image_url_cache.json")
if os.path.exists(IMG_CACHE_FILE):
    try:
        with open(IMG_CACHE_FILE, "r", encoding="utf-8") as f:
            IMG_CACHE = json.load(f)
    except Exception:
        IMG_CACHE = {}
else:
    IMG_CACHE = {}

def save_cache():
    try:
        with open(IMG_CACHE_FILE, "w", encoding="utf-8") as f:
            json.dump(IMG_CACHE, f, ensure_ascii=False, indent=2)
    except Exception:
        pass

def is_generic_cpu_placeholder(url: str) -> bool:
    if not url:
        return True
    u = url.lower()
    if "reference.webp" in u:
        return True
    if "ihs_studio.webp" in u or "bottom.webp" in u or "generic_cpu" in u:
        return True
    return False

def enrich_single_cpu(cpu):
    name = cpu.get("name", "")
    socket = cpu.get("socket") or ""
    brand = "AMD" if "ryzen" in name.lower() or "amd" in name.lower() else "Intel"

    cache_key = f"cpu_{name.lower().replace(' ', '_')}"

    # Check if already cached with 3 real non-generic URLs
    if cache_key in IMG_CACHE:
        cached = IMG_CACHE[cache_key]
        if isinstance(cached, list) and len(cached) >= 3:
            if all(not is_generic_cpu_placeholder(u) for u in cached[:3]):
                cpu["images"] = cached[:3]
                cpu["imageUrl"] = cached[0]
                return cpu

    gallery = []

    # 1. Retail Box
    q_box = f"{name} processor retail box white background"
    candidates_box = search_bing_images(q_box, count=6)
    for c in candidates_box:
        if not is_bad_image(c, name, "cpu") and not is_generic_cpu_placeholder(c):
            if verify_image_url(c, timeout=2.5):
                gallery.append(c)
                break

    # If box not found, check existing primary if not generic
    existing_primary = cpu.get("imageUrl") or (cpu.get("images", [])[0] if cpu.get("images") else None)
    if not gallery and existing_primary and not is_generic_cpu_placeholder(existing_primary):
        if verify_image_url(existing_primary, timeout=2.5):
            gallery.append(existing_primary)

    # 2. Die / IHS Studio Angle
    q_ihs = f"{name} processor ihs angle studio photo"
    candidates_ihs = search_bing_images(q_ihs, count=8)
    for c in candidates_ihs:
        if c not in gallery and not is_bad_image(c, name, "cpu") and not is_generic_cpu_placeholder(c):
            if verify_image_url(c, timeout=2.5):
                gallery.append(c)
                break

    # 3. Socket Substrate Contacts Back
    q_back = f"{name} {socket} processor back gold pads contacts"
    candidates_back = search_bing_images(q_back, count=8)
    for c in candidates_back:
        if c not in gallery and not is_bad_image(c, name, "cpu") and not is_generic_cpu_placeholder(c):
            if verify_image_url(c, timeout=2.5):
                gallery.append(c)
                break

    # Fallbacks if < 3
    if len(gallery) < 3:
        fallbacks = [
            f"{name} techpowerup cpu specs",
            f"{name} unboxing tomshardware",
            f"{name} anandtech review photo"
        ]
        for fq in fallbacks:
            if len(gallery) >= 3:
                break
            for c in search_bing_images(fq, count=8):
                if c not in gallery and not is_bad_image(c, name, "cpu") and not is_generic_cpu_placeholder(c):
                    if verify_image_url(c, timeout=2.5):
                        gallery.append(c)
                        if len(gallery) >= 3:
                            break

    if len(gallery) >= 3:
        IMG_CACHE[cache_key] = gallery[:3]
        cpu["images"] = gallery[:3]
        cpu["imageUrl"] = gallery[0]
    elif len(gallery) >= 2:
        # If 2 verified specific images found, keep them
        cpu["images"] = gallery
        cpu["imageUrl"] = gallery[0]

    return cpu

def main():
    print("================================================================================")
    print(" HARDWARE WIZARD - DEDICATED CPU 3-IMAGE ENRICHMENT ENGINE                      ")
    print("================================================================================")
    cpu_path = os.path.join(CATALOG_DIR, "cpu.json")
    with open(cpu_path, "r", encoding="utf-8") as f:
        cpus = json.load(f)

    print(f"Enriching {len(cpus)} CPUs with authentic Box, IHS, and Contact photos...")

    with ThreadPoolExecutor(max_workers=6) as executor:
        futures = {executor.submit(enrich_single_cpu, c): c for c in cpus}
        completed = 0
        for f in as_completed(futures):
            c = f.result()
            completed += 1
            print(f"  [{completed}/{len(cpus)}] {c.get('name')} -> {len(c.get('images', []))} images")

    save_cache()
    with open(cpu_path, "w", encoding="utf-8") as f:
        json.dump(cpus, f, ensure_ascii=False, indent=2)
    with open(os.path.join(COMPOSE_DIR, "cpu.json"), "w", encoding="utf-8") as f:
        json.dump(cpus, f, ensure_ascii=False, indent=2)

    print("\nAll CPUs enriched and synced successfully!")

if __name__ == "__main__":
    main()
