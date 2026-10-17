#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/clean_catalog_names.py
Hardware Wizard - Katalog İsim Temizliği ve Paket (Bundle) Ürünlerin Tasfiyesi

1. Paket (Bundle) / Kampanya Ürünlerini Katalogdan Tamamen Tasfiye Et:
   - Anakart kategorisinde PSU (1000W, 850W, 750W, 80Plus vb.) veya Sıvı Soğutma barındıran bundle ürünleri sil.
   - Kasa kategorisinde dahili PSU içeren (750W 80+ PSU vb.) birleşik paket ürünleri sil.
   - Ürün adında '+' ile iki farklı donanımı bağlayan tüm kampanya ürünlerini sil.
   (Not: Sapphire NITRO+, NZXT RGB+, PSU 80+ gibi meşru model adları korunur).

2. Mağaza SEO Dolgu Kelimelerini Temizle:
   - name ve model alanlarından (AMD Ryzen, Intel Core, AM5 Soket, AM4 Soket,
     Soket 1700, LGA1851, Anakart, Ekran Kartı, İşlemci, Güç Kaynağı, Oyuncu, Gaming PC vb.)
     tüm mağaza SEO kelimelerini temizle.
   - Model alanına saf model kodunu yaz.
   - prices_tr.json dosyalarındaki ID eşleştirmelerini senkronize et.
"""

import os
import sys
import json
import re

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DATA_CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

TARGET_PRICE_FILES = [
    os.path.join(ROOT_DIR, "data", "prices_tr.json"),
    os.path.join(COMPOSE_FILES_DIR, "prices_tr.json")
]

CATEGORY_FILES = {
    "case": ["case.json"],
    "cooler": ["cooler.json", "cpu-cooler.json"],
    "cpu": ["cpu.json"],
    "gpu": ["gpu.json", "video-card.json"],
    "storage": ["storage.json", "internal-hard-drive.json"],
    "ram": ["ram.json", "memory.json"],
    "motherboard": ["motherboard.json"],
    "psu": ["psu.json", "power-supply.json"],
}

KNOWN_BRANDS = [
    "ASUS", "MSI", "GIGABYTE", "ASROCK", "COLORFUL", "SAPPHIRE", "POWERCOLOR", "XFX",
    "INNO3D", "ZOTAC", "PALIT", "GALAX", "CORSAIR", "DEEPCOOL", "ARCTIC", "THERMALRIGHT",
    "NOCTUA", "NZXT", "LIAN LI", "COOLER MASTER", "BE QUIET!", "BE QUIET", "SEASONIC",
    "FSP", "THERMALTAKE", "KINGSTON", "CRUCIAL", "SAMSUNG", "WESTERN DIGITAL", "WD",
    "KIOXIA", "LEXAR", "TEAMGROUP", "G.SKILL", "ANTEC", "COUGAR", "FRACTAL DESIGN",
    "MONTECH", "BITFENIX", "SEGOTEP", "RAMPAGE", "GAMEPOWER", "JAMES DONKEY", "MAXSUN",
    "AMD", "INTEL"
]

def sanitize_name(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")

def generate_hardware_id(category_prefix: str, name: str) -> str:
    return f"{category_prefix}_{sanitize_name(name)}"

def is_bundle_product(category: str, name: str) -> bool:
    upper = name.upper()

    # Legit '+' patterns
    # NITRO+, RGB+, 80+, 85+, (+) are NOT bundles
    legit_plus_tokens = ["NITRO+", "RGB+", "80+", "85+", "PRO+", "(+)"]
    clean_plus = upper
    for lp in legit_plus_tokens:
        clean_plus = clean_plus.replace(lp, "")

    # Check if there's a '+' combining two parts
    if "+" in clean_plus:
        # If '+' has words on both sides (e.g. WOOD+GP, Anakart+PSU)
        if re.search(r"[A-Z0-9]+\s*\+\s*[A-Z0-9]+", clean_plus):
            return True

    # Category-specific cross-component contamination
    if category == "motherboard":
        # Motherboard containing PSU specs
        if re.search(r"\b(1200W|1000W|850W|750W|650W|600W|550W|500W|80PLUS|80\s*PLUS|80\+)\b", upper):
            return True
        if re.search(r"\b(GP-UD|UD1000|UD850|GÜÇ KAYNAĞI|GUC KAYNAGI)\b", upper):
            return True
        if re.search(r"\b(SIVI SOĞUTMA|SIVI SOGUTMA|LIQUID COOLER|AIO SOĞUTMA)\b", upper):
            return True

    elif category == "case":
        # Case bundled with a PSU
        has_watts = bool(re.search(r"\b(1200|1000|850|750|650|600|550|500)\s*W\b", upper))
        has_psu_tag = bool(re.search(r"\b(PSU|80\+|BRONZE|GOLD|GÜÇ KAYNAĞI|GUC KAYNAGI|ATX3\.1)\b", upper))
        if has_watts and has_psu_tag:
            return True

    elif category in ["gpu", "cpu", "ram", "storage", "cooler"]:
        # Shouldn't contain bundled PSU or unrelated parts
        if re.search(r"\b(1200W|1000W|850W|750W|650W)\b", upper) and category != "psu":
            return True

    return False

def clean_product_name_and_model(category: str, raw_name: str) -> tuple[str, str]:
    n = raw_name.strip()

    # 1. Clean encoding issues
    n = n.replace("\ufffd", "").replace("", "")

    # 2. Category-specific SEO/Store noise removal
    if category == "motherboard":
        # Strip trailing store SEO text
        n = re.sub(r"\s+AMD\s+Ryzen\b.*$", "", n, flags=re.I)
        n = re.sub(r"\s+Intel\s+Core.*$", "", n, flags=re.I)
        n = re.sub(r"\s+Intel\s+Z[0-9]{3}.*$", "", n, flags=re.I)
        n = re.sub(r"\s+Intel\s+B[0-9]{3}.*$", "", n, flags=re.I)
        n = re.sub(r"\s+AMD\s+A[0-9]{3}.*$", "", n, flags=re.I)
        n = re.sub(r"\s+AMD\s+B[0-9]{3}.*$", "", n, flags=re.I)
        n = re.sub(r"\s+AMD\s+X[0-9]{3}.*$", "", n, flags=re.I)
        n = re.sub(r"\s+AMD\s+DDR[45].*$", "", n, flags=re.I)
        n = re.sub(r"\s+AMD\s+Soket.*$", "", n, flags=re.I)
        n = re.sub(r"\s+INTEL\s+Soket.*$", "", n, flags=re.I)
        n = re.sub(r"\s+Soket\s+(?:AM[45]|1700|1851|LGA\s*[0-9]+).*$", "", n, flags=re.I)
        n = re.sub(r"\s+LGA\s*1[78]5?1?\b.*$", "", n, flags=re.I)
        n = re.sub(r"\s+1[78]5?1?p\b.*$", "", n, flags=re.I)
        n = re.sub(r"\s+\d{4,5}\+?\s*\(OC\)\s*(?:MHz|MT/s|Mhz)?.*$", "", n, flags=re.I)
        n = re.sub(r"\s+\d{4,5}\+?\s*(?:MHz|MT/s|Mhz)\s*\(OC\)?.*$", "", n, flags=re.I)
        n = re.sub(r"\s+\d{4,5}\s*(?:MHz|MT/s|Mhz|MT/S).*$", "", n, flags=re.I)
        n = re.sub(r"\s+DDR[45]\s+\d{4,5}.*$", "", n, flags=re.I)
        n = re.sub(r"\s+Kurumsal.*$", "", n, flags=re.I)
        n = re.sub(r"\s+M\.2\s+(?:HDMI|DP|VGA|WIFI).*$", "", n, flags=re.I)
        n = re.sub(r"\s+(?:mATX|Micro\s*ATX|ATX|E-ATX|Mini-ITX)\s*(?:Gaming)?\s*(?:\(Oyuncu\))?\s*Anakart.*$", "", n, flags=re.I)
        n = re.sub(r"\s+Anakart.*$", "", n, flags=re.I)

    elif category == "gpu":
        # If store attached factory part numbers or trailing GDDR specs
        # e.g.: GV-N5080GAMING OC-16GD GDDR7 16G 256bit Gaming (Oyuncu)
        # e.g.: 12GB N50702-12D7X-195064N GDDR7 192Bit Gaming (Oyuncu)
        m = re.match(r"^(.*?)\s+(?:GV-[A-Z0-9]+|N[0-9]{5}-[A-Z0-9\-]+|[0-9]{1,2}GB\s+N[0-9]+|GDDR[67]|Gaming\s*\(Oyuncu\)).*$", n, flags=re.I)
        if m and len(m.group(1)) > 10:
            n = m.group(1)
        n = re.sub(r"\s+Gaming\s*\(Oyuncu\).*$", "", n, flags=re.I)
        n = re.sub(r"\s+\(Oyuncu\).*$", "", n, flags=re.I)

    elif category == "cooler":
        # Strip compatibility spam: "240mm Intel-AMD Uyumlu İşlemci Sıvı Soğutucu"
        n = re.sub(r"\s+\d{3}mm\s+(?:Intel-AMD|LGA|AM|Dijital|Beyaz|Siyah|Sıvı|Isı).*$", "", n, flags=re.I)
        n = re.sub(r"\s+(?:AIO\s+)?LGA\s*1[78]5?1?.*$", "", n, flags=re.I)
        n = re.sub(r"\s+AURA\s+SYNC.*$", "", n, flags=re.I)
        n = re.sub(r"\s+\d{2}cm\s+RGB.*$", "", n, flags=re.I)
        n = re.sub(r"\s+Fanlı\s+\d{3}mm.*$", "", n, flags=re.I)
        n = re.sub(r"\s+(?:İşlemci|Islemci|Sıvı|Sivi|Kule)\s+(?:Soğutucu|Sogutucu|Soğutma|Sogutma).*$", "", n, flags=re.I)

    elif category == "case":
        # Strip trailing fan/glass specs: "Siyah 3x140mm Fanlı Back-Connect Anakart Destekli..."
        n = re.sub(r"\s+(?:Siyah|Beyaz)?\s*\d+x\d+mm.*$", "", n, flags=re.I)
        n = re.sub(r"\s+\d+\s*x\s*ARGB.*$", "", n, flags=re.I)
        n = re.sub(r"\s+(?:Back-Connect|Temperli|Tempered|Gaming|Oyuncu|Kasa|Kasası).*$", "", n, flags=re.I)

    # 3. Universal SEO fluff cleanup across all categories
    n = re.sub(r"\bGaming\s*\((?:Oyuncu)\)", "", n, flags=re.I)
    n = re.sub(r"\(Oyuncu\)", "", n, flags=re.I)
    n = re.sub(r"\bOyuncu\b", "", n, flags=re.I)
    n = re.sub(r"\bGaming\s+PC\b", "", n, flags=re.I)
    n = re.sub(r"\bAM5\s+Soket\b", "", n, flags=re.I)
    n = re.sub(r"\bAM4\s+Soket\b", "", n, flags=re.I)
    n = re.sub(r"\bSoket\s+AM5\b", "", n, flags=re.I)
    n = re.sub(r"\bSoket\s+AM4\b", "", n, flags=re.I)
    n = re.sub(r"\bSoket\s+1700\b", "", n, flags=re.I)
    n = re.sub(r"\bSoket\s+1851\b", "", n, flags=re.I)
    n = re.sub(r"\bSoket\s+LGA\s*1851\b", "", n, flags=re.I)
    n = re.sub(r"\bSoket\s+LGA\s*1700\b", "", n, flags=re.I)
    n = re.sub(r"\bLGA1851\b", "", n, flags=re.I)
    n = re.sub(r"\bLGA1700\b", "", n, flags=re.I)
    n = re.sub(r"\bAnakart\b", "", n, flags=re.I)
    n = re.sub(r"\bEkran\s+Kartı\b", "", n, flags=re.I)
    n = re.sub(r"\bEkran\s+Karti\b", "", n, flags=re.I)
    if category != "cpu":
        n = re.sub(r"\bİşlemci\b", "", n, flags=re.I)
        n = re.sub(r"\bIslemci\b", "", n, flags=re.I)
    n = re.sub(r"\bGüç\s+Kaynağı\b", "", n, flags=re.I)
    n = re.sub(r"\bGuc\s+Kaynagi\b", "", n, flags=re.I)

    # Clean redundant whitespaces
    n = re.sub(r"\s+", " ", n).strip(" .-")

    # 4. Extract Pure Model Code
    # Determine Brand
    matched_brand = None
    for b in KNOWN_BRANDS:
        if n.upper().startswith(b + " ") or n.upper() == b:
            matched_brand = b
            break

    if matched_brand:
        pure_model = n[len(matched_brand):].strip()
    else:
        parts = n.split(" ", 1)
        pure_model = parts[1].strip() if len(parts) > 1 else n

    # Refine pure_model: remove duplicate brand if repeated
    if matched_brand and pure_model.upper().startswith(matched_brand + " "):
        pure_model = pure_model[len(matched_brand):].strip()

    # Clean pure_model
    pure_model = re.sub(r"\s+", " ", pure_model).strip(" .-")

    # Ensure Brand formatting in name is canonical
    if matched_brand:
        # Keep proper case
        formatted_name = f"{matched_brand} {pure_model}".strip()
    else:
        formatted_name = n

    return formatted_name, pure_model

def main():
    print("================================================================================")
    print(" HARDWARE WIZARD - KATALOG İSİM TEMİZLİĞİ VE BUNDLE TASFİYESİ                 ")
    print("================================================================================")

    # Load existing prices_tr.json to migrate IDs if product name changes
    prices_data = {}
    p_main = TARGET_PRICE_FILES[0]
    if os.path.exists(p_main):
        with open(p_main, "r", encoding="utf-8") as f:
            prices_data = json.load(f)

    total_purged_bundles = 0
    total_cleaned_names = 0
    id_migrations = {}  # old_id -> new_id
    purged_ids = set()

    # Process all categories
    for cat, filenames in CATEGORY_FILES.items():
        primary_fname = filenames[0]
        cat_file = os.path.join(DATA_CATALOG_DIR, primary_fname)
        if not os.path.exists(cat_file):
            continue

        with open(cat_file, "r", encoding="utf-8") as f:
            items = json.load(f)

        original_count = len(items)
        surviving_items = []

        for it in items:
            raw_name = it.get("name", "")
            old_id = generate_hardware_id(cat, raw_name)

            # 1. Check if bundle
            if is_bundle_product(cat, raw_name):
                total_purged_bundles += 1
                purged_ids.add(old_id)
                print(f"  [BUNDLE SİLİNDİ] [{cat.upper()}] {raw_name}")
                continue

            # 2. Clean name and model
            clean_name, pure_model = clean_product_name_and_model(cat, raw_name)

            if clean_name != raw_name or it.get("model") != pure_model:
                total_cleaned_names += 1
                new_id = generate_hardware_id(cat, clean_name)
                if old_id != new_id:
                    id_migrations[old_id] = new_id

                it["name"] = clean_name
                it["model"] = pure_model
            else:
                it["model"] = pure_model

            surviving_items.append(it)

        # Save to all mapped filenames in both data/catalog and shared/.../files
        for fname in filenames:
            for base_dir in [DATA_CATALOG_DIR, COMPOSE_FILES_DIR]:
                dest = os.path.join(base_dir, fname)
                os.makedirs(os.path.dirname(dest), exist_ok=True)
                with open(dest, "w", encoding="utf-8") as f:
                    json.dump(surviving_items, f, indent=2, ensure_ascii=False)

        print(f"[{cat.upper()}] {original_count} üründen {len(surviving_items)} ürün kaldı ({original_count - len(surviving_items)} bundle silindi).")

    # Update prices_tr.json: purge bundle products and migrate renamed products
    updated_prices = {}
    purged_price_entries = 0
    migrated_price_entries = 0

    for pid, pdata in prices_data.items():
        if pid in purged_ids:
            purged_price_entries += 1
            continue

        if pid in id_migrations:
            new_id = id_migrations[pid]
            pdata["productId"] = new_id
            updated_prices[new_id] = pdata
            migrated_price_entries += 1
        else:
            updated_prices[pid] = pdata

    for p_file in TARGET_PRICE_FILES:
        os.makedirs(os.path.dirname(p_file), exist_ok=True)
        with open(p_file, "w", encoding="utf-8") as f:
            json.dump(updated_prices, f, indent=2, ensure_ascii=False)

    print("================================================================================")
    print(f"[BAŞARILI] Temizlik tamamlandı:")
    print(f"  - Silinen Paket/Bundle Ürün Sayısı: {total_purged_bundles}")
    print(f"  - İsmi/Modeli Temizlenen Ürün Sayısı: {total_cleaned_names}")
    print(f"  - prices_tr.json İçinde Taşınan ID Sayısı: {migrated_price_entries}")
    print(f"  - prices_tr.json İçinden Silinen Bundle Sayısı: {purged_price_entries}")
    print("================================================================================")

if __name__ == "__main__":
    main()
