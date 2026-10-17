#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/fetch_web_images.py
Hardware Wizard - Web Görsel Arama Motoru ile Eksik ve Logolu Görselleri Tamamlayıcı

1. Mağazalardan gelen itopya.com logolarını, site placeholder'larını ve yanlış seri/marka görsellerini temizler.
2. Görseli olmayan veya silinen tüm katalog ürünleri için Bing Images Web Arama Motoru ile
   yüksek çözünürlüklü ve temiz ürün görseli arar ve doğrular.
3. Hem data/catalog hem de shared/.../files dizinlerindeki katalog JSON'larına ve prices_tr.json dosyalarına kaydeder.
"""

import os
import sys
import json
import re
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
import time
import functools

print = functools.partial(print, flush=True)

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

CATEGORY_SEARCH_SUFFIXES = {
    "case": "Case",
    "cooler": "Cooler",
    "cpu": "Processor",
    "gpu": "",
    "storage": "SSD",
    "ram": "RAM",
    "motherboard": "Motherboard",
    "psu": "PSU",
}

FORBIDDEN_IMAGE_PATTERNS = [
    "logo", "itopya", "sinerji", "incehesap", "tebilon", "gaming.gen",
    "pttavm", "idefix", "n11", "banner", "default", "no-image", "no_image",
    "placeholder", "lazy", "loading", "blank", "1x1", "icon", "avatar",
    "assets/img", "theme", ".svg", "svg%20", "data:", "base64", "roundnew",
    "yenieera", "watermark", "kampanya", "firsat"
]

KNOWN_BRANDS = [
    "1ST PLAYER", "ASUS", "MSI", "GIGABYTE", "CORSAIR", "SEASONIC", "FSP", "DEEPCOOL",
    "THERMALTAKE", "BE QUIET", "NZXT", "LIAN LI", "COOLER MASTER", "KINGSTON",
    "CRUCIAL", "SAMSUNG", "WESTERN DIGITAL", "WD", "KIOXIA", "SAPPHIRE", "ASROCK",
    "POWERCOLOR", "PALIT", "GALAX", "ZOTAC", "INNO3D", "ARCTIC", "THERMALRIGHT",
    "NOCTUA", "PATRIOT", "TEAMGROUP", "RAMPAGE", "OCYPUS"
]

def sanitize_name(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")

def generate_hardware_id(category_prefix: str, name: str) -> str:
    return f"{category_prefix}_{sanitize_name(name)}"

def is_bad_image(url: str | None, item_name: str = "", category: str = "") -> bool:
    if not url or not isinstance(url, str):
        return True
    u = url.strip().lower()
    if not (u.startswith("http://") or u.startswith("https://")):
        return True

    # 1. Store logos / placeholders
    if any(pat in u for pat in FORBIDDEN_IMAGE_PATTERNS):
        return True

    if item_name:
        name_upper = item_name.upper()

        # 2. Check cross-brand mismatch in URL path
        item_brand = None
        for b in KNOWN_BRANDS:
            if name_upper.startswith(b):
                item_brand = b.lower()
                break

        if item_brand:
            for other_b in ["1st_player", "asus", "msi", "gigabyte", "corsair", "seasonic", "fsp", "deepcool", "cooler_master", "thermaltake", "be_quiet", "nzxt", "lian_li"]:
                cleaned_other = other_b.replace("_", "")
                cleaned_item = item_brand.replace(" ", "").replace("_", "")
                if cleaned_other != cleaned_item and (f"/{other_b}" in u or f"_{other_b}" in u or f"/{cleaned_other}" in u):
                    return True

        # 3. PSU Wattage mismatch
        if category == "psu":
            w_item = re.search(r"\b([0-9]{3,4})\s*W\b", name_upper)
            if w_item:
                w_val = w_item.group(1)
                w_urls = re.findall(r"([0-9]{3,4})w", u)
                for w in w_urls:
                    if w != w_val and abs(int(w) - int(w_val)) >= 50:
                        return True

        # 4. GPU / Cooler series mismatch
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
        if "STEEL LEGEND" in name_upper and "taichi" in u:
            return True
        if "TAICHI" in name_upper and "steel" in u:
            return True

    return False

def verify_image_url(url: str, timeout: float = 3.0) -> bool:
    if not url or not (url.startswith("http://") or url.startswith("https://")):
        return False
    lower = url.lower().split("?")[0]
    valid_exts = (".jpg", ".jpeg", ".png", ".webp")
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Accept": "image/avif,image/webp,image/apng,image/png,image/*,*/*;q=0.8"
    }
    # Fast path: HEAD request
    try:
        req = urllib.request.Request(url, headers=headers, method="HEAD")
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if resp.status == 200:
                ct = resp.headers.get("Content-Type", "").lower()
                if "image" in ct or lower.endswith(valid_exts):
                    return True
    except Exception:
        pass

    # Fallback GET with partial read
    try:
        req = urllib.request.Request(url, headers=headers, method="GET")
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if resp.status == 200:
                ct = resp.headers.get("Content-Type", "").lower()
                chunk = resp.read(512)
                if len(chunk) > 0 and ("image" in ct or lower.endswith(valid_exts)):
                    return True
    except Exception:
        pass
    return False

def search_bing_images(query: str, count: int = 10) -> list[str]:
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "en-US,en;q=0.9",
    }
    q_enc = urllib.parse.quote_plus(query)
    url = f"https://www.bing.com/images/async?q={q_enc}&first=0&count={count}&scenario=ImageBasicHover&datsrc=N_RS&layout=RowBased&mmasync=1"
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=6) as resp:
            html = resp.read().decode("utf-8", errors="ignore")
            murls = re.findall(r'murl&quot;:&quot;(https?://[^&]+)&quot;', html)
            if not murls:
                murls = re.findall(r'\"murl\":\"(https?://[^\"]+)\"', html)
            return murls
    except Exception:
        return []

def search_duckduckgo_images(query: str) -> list[str]:
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    }
    try:
        token_url = f"https://duckduckgo.com/?q={urllib.parse.quote_plus(query)}&iax=images&ia=images"
        req = urllib.request.Request(token_url, headers=headers)
        with urllib.request.urlopen(req, timeout=6) as resp:
            html = resp.read().decode("utf-8", errors="ignore")
            vqd_match = re.search(r'vqd=([0-9\-]+)', html) or re.search(r'vqd=\"([^\"]+)\"', html)
            if not vqd_match:
                return []
            vqd = vqd_match.group(1)

        img_url = f"https://duckduckgo.com/i.js?l=us-en&o=json&q={urllib.parse.quote_plus(query)}&vqd={vqd}&f=,,,type:photo,&p=1"
        req2 = urllib.request.Request(img_url, headers=headers)
        with urllib.request.urlopen(req2, timeout=6) as resp2:
            data = json.loads(resp2.read().decode("utf-8", errors="ignore"))
            return [r["image"] for r in data.get("results", []) if "image" in r]
    except Exception:
        return []

def fetch_product_image(category: str, item_name: str, model_code: str) -> str | None:
    suffix = CATEGORY_SEARCH_SUFFIXES.get(category, "")
    query = f"{item_name} {suffix}".strip()
    query = re.sub(r"\s+", " ", query)

    # 1. Bing Images search
    candidates = search_bing_images(query, count=8)
    if not candidates and model_code:
        candidates = search_bing_images(f"{model_code} {suffix}".strip(), count=8)

    # 2. DuckDuckGo fallback
    if not candidates:
        candidates = search_duckduckgo_images(query)

    for candidate in candidates:
        if is_bad_image(candidate, item_name, category):
            continue
        if verify_image_url(candidate):
            return candidate

    return None

def main():
    print("================================================================================")
    print(" HARDWARE WIZARD - WEB GÖRSEL ARAMA & EKSİK GÖRSELLERİ TAMAMLAMA BETİĞİ        ")
    print("================================================================================")

    # 1. prices_tr.json yükle
    prices_data = {}
    p_main = TARGET_PRICE_FILES[0]
    if os.path.exists(p_main):
        with open(p_main, "r", encoding="utf-8") as f:
            prices_data = json.load(f)

    # 2. Katalogları tara, zehirli görselleri temizle ve eksik olanları topla
    total_scanned = 0
    total_poisoned_cleared = 0
    items_to_search = []  # (category, item_dict, prod_id)

    catalogs = {}
    for cat, filenames in CATEGORY_FILES.items():
        fname = filenames[0]
        cat_file = os.path.join(DATA_CATALOG_DIR, fname)
        if not os.path.exists(cat_file):
            continue
        with open(cat_file, "r", encoding="utf-8") as f:
            items = json.load(f)
        catalogs[cat] = items

        for it in items:
            total_scanned += 1
            name = it.get("name", "")
            pid = generate_hardware_id(cat, name)
            cur_img = it.get("imageUrl")

            if cur_img and is_bad_image(cur_img, name, cat):
                it["imageUrl"] = None
                total_poisoned_cleared += 1
                if pid in prices_data:
                    prices_data[pid]["imageUrl"] = None

            if not it.get("imageUrl"):
                items_to_search.append((cat, it, pid))

    print(f"Toplam Taranan Ürün: {total_scanned}")
    print(f"Temizlenen Hatalı/Logolu/Yanlış Seri Görsel: {total_poisoned_cleared}")
    print(f"Görseli Eksik Olan Ürün Sayısı: {len(items_to_search)}")
    print(f"\n[+] Web Görsel Arama Motoru ThreadPoolExecutor ile başlatılıyor (Hedef: {len(items_to_search)} ürün)...")

    newly_found_count = 0
    start_time = time.time()

    def process_item(cat, item, pid):
        name = item.get("name", "")
        model = item.get("model", "")
        img = fetch_product_image(cat, name, model)
        return (cat, item, pid, img)

    with ThreadPoolExecutor(max_workers=10) as executor:
        future_to_item = {
            executor.submit(process_item, cat, it, pid): (cat, it["name"])
            for cat, it, pid in items_to_search
        }

        completed = 0
        for future in as_completed(future_to_item):
            cat, name = future_to_item[future]
            completed += 1
            try:
                cat, it, pid, found_img = future.result()
                if found_img:
                    it["imageUrl"] = found_img
                    if pid in prices_data:
                        prices_data[pid]["imageUrl"] = found_img
                    newly_found_count += 1
                if completed % 10 == 0 or completed == len(items_to_search):
                    print(f"  [{completed}/{len(items_to_search)}] Bulunan: {newly_found_count} | Son: [{cat.upper()}] {name[:40]}")
            except Exception:
                pass

    elapsed = round(time.time() - start_time, 2)
    print(f"\n[+] Görsel arama tamamlandı: {newly_found_count} ürüne yeni temiz görsel bağlandı ({elapsed} saniye).")

    # Tüm katalog dosyalarına kaydet
    for cat, filenames in CATEGORY_FILES.items():
        items = catalogs.get(cat, [])
        for fname in filenames:
            for base_dir in [DATA_CATALOG_DIR, COMPOSE_FILES_DIR]:
                dest = os.path.join(base_dir, fname)
                os.makedirs(os.path.dirname(dest), exist_ok=True)
                with open(dest, "w", encoding="utf-8") as f:
                    json.dump(items, f, indent=2, ensure_ascii=False)

    # prices_tr.json dosyalarına kaydet
    for p_file in TARGET_PRICE_FILES:
        os.makedirs(os.path.dirname(p_file), exist_ok=True)
        with open(p_file, "w", encoding="utf-8") as f:
            json.dump(prices_data, f, indent=2, ensure_ascii=False)

    print("================================================================================")
    print(f"[BAŞARILI] Görsel senkronizasyonu tamamlandı:")
    print(f"  - Temizlenen Hatalı/Logolu Görsel: {total_poisoned_cleared}")
    print(f"  - Web'den Bulunup Kaydedilen Yeni Görsel: {newly_found_count}")
    print(f"  - data/catalog ve shared/.../files dizinleri senkronize edildi.")
    print("================================================================================")

if __name__ == "__main__":
    main()
