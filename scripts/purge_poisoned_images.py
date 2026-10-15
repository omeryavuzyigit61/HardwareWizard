#!/usr/bin/env python3
"""
scripts/purge_poisoned_images.py
Hardware Wizard - Zehirlenmiş / İtopya Logo / Hatalı Seri Görsellerini Temizleme Betiği
"""

import json
import os
import glob

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
SHARED_FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

FORBIDDEN_IMAGE_PATTERNS = [
    "logo", "itopya", "sinerji", "incehesap", "tebilon", "gaming.gen",
    "pttavm", "idefix", "n11", "banner", "default", "no-image", "no_image",
    "placeholder", "lazy", "loading", "blank", "1x1", "icon", "avatar",
    "assets/img", "theme", ".svg", "svg%20", "data:", "base64", "roundnew",
    "yenieera"
]

def is_bad_image(url: str | None, item_name: str = "") -> bool:
    if not url or not isinstance(url, str):
        return False
    u = url.strip().lower()
    if not (u.startswith("http://") or u.startswith("https://")):
        return True
    if any(pat in u for pat in FORBIDDEN_IMAGE_PATTERNS):
        return True
    if item_name:
        name_upper = item_name.upper()
        if "TUF" in name_upper and any(x in u for x in ["strix", "rog"]):
            return True
        if ("STRIX" in name_upper or "ROG" in name_upper) and "tuf" in u:
            return True
        if "DUAL" in name_upper and any(x in u for x in ["strix", "tuf", "rog"]):
            return True
        if "PRIME" in name_upper and any(x in u for x in ["strix", "tuf", "rog"]):
            return True
        if "VENTUS" in name_upper and any(x in u for x in ["suprim", "trio", "gaming_x", "gaming-x"]):
            return True
        if ("EAGLE" in name_upper or "WINDFORCE" in name_upper) and "aorus" in u:
            return True
        if "AORUS" in name_upper and any(x in u for x in ["eagle", "windforce"]):
            return True
    return False

def clean_catalog_files():
    total_cleaned = 0
    all_json_files = glob.glob(os.path.join(CATALOG_DIR, "*.json")) + glob.glob(os.path.join(SHARED_FILES_DIR, "*.json"))
    
    for fpath in all_json_files:
        if "hardware_benchmarks.json" in fpath or "prices_tr.json" in fpath:
            continue
        try:
            with open(fpath, "r", encoding="utf-8") as f:
                data = json.load(f)
            
            if not isinstance(data, list):
                continue
                
            modified = False
            for it in data:
                img = it.get("imageUrl")
                name = it.get("name", "")
                if is_bad_image(img, name):
                    it["imageUrl"] = None
                    total_cleaned += 1
                    modified = True
                    
            if modified:
                with open(fpath, "w", encoding="utf-8") as f:
                    json.dump(data, f, ensure_ascii=False, indent=2)
                print(f"[OK] Cleaned bad images from {os.path.basename(fpath)}")
        except Exception as e:
            print(f"[HATA] {fpath}: {e}")
            
    # Clean prices_tr.json files
    price_files = [
        os.path.join(ROOT_DIR, "data", "prices_tr.json"),
        os.path.join(SHARED_FILES_DIR, "prices_tr.json")
    ]
    for pf in price_files:
        if os.path.exists(pf):
            with open(pf, "r", encoding="utf-8") as f:
                prices_map = json.load(f)
            p_modified = False
            for pid, pdata in prices_map.items():
                img = pdata.get("imageUrl")
                if is_bad_image(img, pid):
                    pdata["imageUrl"] = None
                    p_modified = True
                # Clean offers inside
                for off in pdata.get("offers", []):
                    o_img = off.get("imageUrl")
                    if is_bad_image(o_img, pid):
                        off["imageUrl"] = None
                        p_modified = True
            if p_modified:
                with open(pf, "w", encoding="utf-8") as f:
                    json.dump(prices_map, f, ensure_ascii=False, indent=2)
                print(f"[OK] Cleaned bad images from {os.path.basename(pf)}")
                
    print(f"\n[BAŞARILI] Toplam {total_cleaned} bozuk/logo/hatalı görsel sıfırlandı.")

if __name__ == "__main__":
    clean_catalog_files()
