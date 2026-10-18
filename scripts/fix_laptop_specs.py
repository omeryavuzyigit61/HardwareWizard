#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import sys
import json
import re
from fetch_web_images import search_bing_images, verify_image_url

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DATA_CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

CPU_SPECS_DB = {
    # Intel Core (Series 1 & 2 Raptor Lake Refresh)
    "Core 5 210H": ("Intel Core 5 210H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.00 GHz Taban / 4.80 GHz Tepe (Boost)"),
    "Core 7 240H": ("Intel Core 7 240H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.50 GHz Taban / 5.20 GHz Tepe (Boost)"),
    "Core 7 250H": ("Intel Core 7 250H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.50 GHz Taban / 5.40 GHz Tepe (Boost)"),
    "Core 9 270H": ("Intel Core 9 270H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.70 GHz Taban / 5.80 GHz Tepe (Boost)"),
    "Core 7 150H": ("Intel Core 7 150H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.50 GHz Taban / 5.40 GHz Tepe (Boost)"),
    "Core 5 120H": ("Intel Core 5 120H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.00 GHz Taban / 4.80 GHz Tepe (Boost)"),
    "Core Ultra 9 275HX": ("Intel Core Ultra 9 275HX", "24 Çekirdek (8P + 16E) / 24 İzlek", "2.70 GHz Taban / 5.40 GHz Tepe (Arrow Lake)"),
    "Core Ultra 9 185H": ("Intel Core Ultra 9 185H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "2.30 GHz Taban / 5.10 GHz Tepe (Boost)"),
    "Core Ultra 7 155H": ("Intel Core Ultra 7 155H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "1.40 GHz Taban / 4.80 GHz Tepe (Boost)"),
    "Core Ultra 5 125H": ("Intel Core Ultra 5 125H", "14 Çekirdek (4P + 8E + 2LP) / 18 İzlek", "1.20 GHz Taban / 4.50 GHz Tepe (Boost)"),

    # Intel 14th Gen
    "i9-14900HX": ("Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe (Boost)"),
    "i7-14700HX": ("Intel Core i7-14700HX", "20 Çekirdek (8P + 12E) / 28 İzlek", "2.10 GHz Taban / 5.50 GHz Tepe (Boost)"),
    "i7-14650HX": ("Intel Core i7-14650HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.20 GHz Taban / 5.20 GHz Tepe (Boost)"),
    "i5-14500HX": ("Intel Core i5-14500HX", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 4.90 GHz Tepe (Boost)"),
    "i5-14450HX": ("Intel Core i5-14450HX", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.80 GHz Tepe (Boost)"),

    # Intel 13th & 12th Gen
    "i9-13980HX": ("Intel Core i9-13980HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.60 GHz Tepe (Boost)"),
    "i9-13900HX": ("Intel Core i9-13900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.40 GHz Tepe (Boost)"),
    "i7-13700HX": ("Intel Core i7-13700HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.10 GHz Taban / 5.00 GHz Tepe (Boost)"),
    "i7-13650HX": ("Intel Core i7-13650HX", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 4.90 GHz Tepe (Boost)"),
    "i7-13620H": ("Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe (Boost)"),
    "i5-13500HX": ("Intel Core i5-13500HX", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.50 GHz Taban / 4.70 GHz Tepe (Boost)"),
    "i5-13450HX": ("Intel Core i5-13450HX", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.60 GHz Tepe (Boost)"),
    "i5-13420H": ("Intel Core i5-13420H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.10 GHz Taban / 4.60 GHz Tepe (Boost)"),
    "i5-12450H": ("Intel Core i5-12450H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.00 GHz Taban / 4.40 GHz Tepe (Boost)"),

    # AMD Strix Point Zen 5
    "Ryzen AI 9 HX 370": ("AMD Ryzen AI 9 HX 370", "12 Çekirdek (4 Zen 5 + 8 Zen 5c) / 24 İzlek", "2.00 GHz Taban / 5.10 GHz Tepe (Ryzen AI 50 NPU TOPS)"),
    "Ryzen AI 9 365": ("AMD Ryzen AI 9 365", "10 Çekirdek (4 Zen 5 + 6 Zen 5c) / 20 İzlek", "2.00 GHz Taban / 5.00 GHz Tepe (Ryzen AI 50 NPU TOPS)"),

    # AMD Zen 4 & Zen 3+
    "Ryzen 9 8940HX": ("AMD Ryzen 9 8940HX", "16 Çekirdek / 32 İzlek (Zen 4)", "2.40 GHz Taban / 5.20 GHz Tepe (Boost)"),
    "Ryzen 9 8945HS": ("AMD Ryzen 9 8945HS", "8 Çekirdek / 16 İzlek (Zen 4 / Ryzen AI)", "4.00 GHz Taban / 5.20 GHz Tepe (Boost)"),
    "Ryzen 9 7945HX": ("AMD Ryzen 9 7945HX", "16 Çekirdek / 32 İzlek (Zen 4)", "2.50 GHz Taban / 5.40 GHz Tepe (Boost)"),
    "Ryzen 7 8845HS": ("AMD Ryzen 7 8845HS", "8 Çekirdek / 16 İzlek (Zen 4 / Ryzen AI)", "3.80 GHz Taban / 5.10 GHz Tepe (Boost)"),
    "Ryzen 7 7840HS": ("AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe (Boost)"),
    "Ryzen 7 7735HS": ("AMD Ryzen 7 7735HS", "8 Çekirdek / 16 İzlek (Zen 3+)", "3.20 GHz Taban / 4.75 GHz Tepe (Boost)"),
    "Ryzen 7 7445HS": ("AMD Ryzen 7 7445HS", "8 Çekirdek / 16 İzlek (Zen 3+)", "3.20 GHz Taban / 4.70 GHz Tepe (Boost)"),
    "Ryzen 7 7435HS": ("AMD Ryzen 7 7435HS", "8 Çekirdek / 16 İzlek (Zen 3+)", "3.10 GHz Taban / 4.50 GHz Tepe (Boost)"),
    "Ryzen 7 260": ("AMD Ryzen 7 260", "8 Çekirdek / 16 İzlek (Hawk Point Refresh)", "3.80 GHz Taban / 5.10 GHz Tepe (Boost)"),
    "Ryzen 7 250": ("AMD Ryzen 7 250", "8 Çekirdek / 16 İzlek (Hawk Point Refresh)", "3.80 GHz Taban / 5.10 GHz Tepe (Boost)"),
    "Ryzen 7 170": ("AMD Ryzen 7 170", "8 Çekirdek / 16 İzlek (Rembrandt Refresh)", "3.20 GHz Taban / 4.75 GHz Tepe (Boost)"),
    "Ryzen 5 7640HS": ("AMD Ryzen 5 7640HS", "6 Çekirdek / 12 İzlek (Zen 4)", "4.30 GHz Taban / 5.00 GHz Tepe (Boost)"),
    "Ryzen 5 7535HS": ("AMD Ryzen 5 7535HS", "6 Çekirdek / 12 İzlek (Zen 3+)", "3.30 GHz Taban / 4.55 GHz Tepe (Boost)"),
    "Ryzen 5 150": ("AMD Ryzen 5 150", "6 Çekirdek / 12 İzlek (Zen 3+)", "3.30 GHz Taban / 4.50 GHz Tepe (Boost)"),
}

def extract_cpu_from_name(name: str):
    name_clean = name.replace("\xa0", " ")
    
    # Check 255HX
    if "255HX" in name_clean:
        return ("Intel Core Ultra 9 255HX", "24 Çekirdek (8P + 16E) / 24 İzlek", "2.40 GHz Taban / 5.20 GHz Tepe (Arrow Lake)")

    # Check Ultra
    m = re.search(r'Ultra\s+([579])[\s\-]+([0-9]{3}[A-Z]*)', name_clean, re.I)
    if m:
        key = f"Core Ultra {m.group(1)} {m.group(2).upper()}"
        if key in CPU_SPECS_DB:
            return CPU_SPECS_DB[key]
        return (f"Intel {key}", "16 Çekirdek / 22 İzlek", "2.00 GHz Taban / 5.00 GHz Tepe (Boost)")

    # Check Core 3/5/7/9 series (new naming scheme without 'i')
    m = re.search(r'Core\s+([3579])[\s\-]+([12][0-9]{2}[A-Z]*)', name_clean, re.I)
    if m:
        key = f"Core {m.group(1)} {m.group(2).upper()}"
        if key in CPU_SPECS_DB:
            return CPU_SPECS_DB[key]
        return (f"Intel {key}", "10 Çekirdek / 16 İzlek", "2.50 GHz Taban / 5.00 GHz Tepe (Boost)")

    # Check Core iX-XXXXX
    m = re.search(r'Core\s+(i[3579])[\-\s]+([0-9]{4,5}[A-Z]*)', name_clean, re.I)
    if not m:
        m = re.search(r'\b(i[3579])[\-\s]+([0-9]{4,5}[A-Z]*)\b', name_clean, re.I)
    if m:
        key = f"{m.group(1).lower()}-{m.group(2).upper()}"
        if key in CPU_SPECS_DB:
            return CPU_SPECS_DB[key]
        return (f"Intel Core {key}", "14 Çekirdek / 20 İzlek", "2.50 GHz Taban / 5.00 GHz Tepe (Boost)")

    # Check Ryzen AI
    m = re.search(r'Ryzen\s+AI\s+([79])\s*(?:HX\s*)?([0-9]{3}[A-Z]*)', name_clean, re.I)
    if m:
        key = f"Ryzen AI {m.group(1)} {m.group(2).upper()}"
        if key in CPU_SPECS_DB:
            return CPU_SPECS_DB[key]
        return (f"AMD {key}", "10 Çekirdek / 20 İzlek", "2.00 GHz Taban / 5.00 GHz Tepe (Ryzen AI 50 NPU TOPS)")

    # Check Ryzen 9/7/5
    m = re.search(r'Ryzen\s+([3579])[\-\s]+([0-9]{4}[A-Z]*)', name_clean, re.I)
    if m:
        key = f"Ryzen {m.group(1)} {m.group(2).upper()}"
        if key in CPU_SPECS_DB:
            return CPU_SPECS_DB[key]
        return (f"AMD {key}", "8 Çekirdek / 16 İzlek", "3.80 GHz Taban / 5.10 GHz Tepe (Boost)")

    # Check Ryzen series 150/170/250/260
    m = re.search(r'Ryzen\s+([3579])[\-\s]+([0-9]{3}[A-Z]*)', name_clean, re.I)
    if m:
        key = f"Ryzen {m.group(1)} {m.group(2).upper()}"
        if key in CPU_SPECS_DB:
            return CPU_SPECS_DB[key]
        return (f"AMD {key}", "8 Çekirdek / 16 İzlek", "3.50 GHz Taban / 5.00 GHz Tepe (Boost)")

    return None

def clean_tgp(tgp_str: str) -> str:
    if not tgp_str:
        return "140W Full Power"
    # Remove duplicate trailing 'W' or 'W Dynamic BoostW'
    s = re.sub(r'W+', 'W', tgp_str.strip())
    s = re.sub(r'Dynamic BoostW', 'Dynamic Boost', s)
    s = re.sub(r'Full PowerW', 'Full Power', s)
    return s.strip()

def fix_all_laptops():
    path = os.path.join(DATA_CATALOG_DIR, "laptop.json")
    with open(path, "r", encoding="utf-8") as f:
        laptops = json.load(f)

    fixed_count = 0
    for idx, lp in enumerate(laptops):
        name = lp.get("name", "")
        extracted = extract_cpu_from_name(name)
        if extracted:
            cpu_model, cpu_cores, cpu_clock = extracted
            if lp.get("cpuModel") != cpu_model:
                print(f"[{idx+1}] FIXING CPU: '{lp.get('cpuModel')}' -> '{cpu_model}' ({name[:50]}...)")
                lp["cpuModel"] = cpu_model
                fixed_count += 1
            lp["cpuCores"] = cpu_cores
            lp["cpuClockGhz"] = cpu_clock
        else:
            # Re-evaluate current cpuModel if name did not contain explicit CPU
            curr = lp.get("cpuModel", "")
            for k, (m, c, cl) in CPU_SPECS_DB.items():
                if k.lower() in curr.lower():
                    lp["cpuModel"] = m
                    lp["cpuCores"] = c
                    lp["cpuClockGhz"] = cl
                    break

        # Clean TGP duplicate W
        lp["gpuWattTgp"] = clean_tgp(lp.get("gpuWattTgp", ""))

    print(f"\nTotal laptops fixed: {fixed_count} / {len(laptops)}")

    # Save to both catalog and compose files
    with open(path, "w", encoding="utf-8") as f:
        json.dump(laptops, f, ensure_ascii=False, indent=2)

    compose_path = os.path.join(COMPOSE_FILES_DIR, "laptop.json")
    with open(compose_path, "w", encoding="utf-8") as f:
        json.dump(laptops, f, ensure_ascii=False, indent=2)

    print(f"Successfully saved {len(laptops)} laptops to both locations.")

if __name__ == "__main__":
    fix_all_laptops()
