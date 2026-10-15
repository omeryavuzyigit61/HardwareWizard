#!/usr/bin/env python3
"""
scripts/batch_convert_catalog_images.py
Hardware Wizard - Tüm Katalog Görsellerini Şeffaf WebP'ye Dönüştürüp Kendi Repomuza Aktarma Betiği
1. data/catalog/*.json içindeki tüm ürünlerin görsellerini indirir.
2. 4 kenardan flood-fill ile beyaz arka planları saf alfaya (transparan) çevirir (PSU gibi).
3. data/images/{category}/{id}.webp olarak yerel repoya kaydeder.
4. JSON kataloglarındaki imageUrl adresini kendi repo URL'mize günceller:
   https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/{category}/{id}.webp
5. Hem data/catalog hem de shared/.../composeResources/files/ dizinlerini günceller.
"""

import io
import json
import os
import re
import sys
import urllib.request
from collections import deque
from concurrent.futures import ThreadPoolExecutor, as_completed
from PIL import Image

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
IMAGES_DIR = os.path.join(ROOT_DIR, "data", "images")
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

REPO_IMAGE_BASE = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images"

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8",
}

def clean_key(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")

def remove_white_background(img: Image.Image, threshold_avg: int = 228, max_diff: int = 28) -> Image.Image:
    img = img.convert("RGBA")
    w, h = img.size
    pix = img.load()

    visited = set()
    queue = deque()

    # 4 kenardaki tüm pikselleri kuyruğa ekle
    for x in range(w):
        queue.append((x, 0))
        queue.append((x, h - 1))
        visited.add((x, 0))
        visited.add((x, h - 1))

    for y in range(h):
        queue.append((0, y))
        queue.append((w - 1, y))
        visited.add((0, y))
        visited.add((w - 1, y))

    while queue:
        x, y = queue.popleft()
        r, g, b, a = pix[x, y]
        avg = (r + g + b) / 3.0
        diff = max(r, g, b) - min(r, g, b)

        # Beyaz veya açık gri/açık renkli arka plan mı?
        if avg >= threshold_avg and diff <= max_diff:
            pix[x, y] = (r, g, b, 0)
            for nx, ny in [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]:
                if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in visited:
                    visited.add((nx, ny))
                    queue.append((nx, ny))

    # Transparan sınırları kırp (autocrop bounding box)
    bbox = img.getbbox()
    if bbox:
        pad = 6
        left = max(0, bbox[0] - pad)
        top = max(0, bbox[1] - pad)
        right = min(w, bbox[2] + pad)
        bottom = min(h, bbox[3] + pad)
        img = img.crop((left, top, right, bottom))

    return img

def process_one_item(cat: str, item: dict) -> tuple[dict, bool]:
    url = item.get("imageUrl")
    name = item.get("name", "")
    if not url or not url.startswith("http"):
        return item, False

    # Zaten kendi repomuzdaki resimse veya IronScale Labs transparan resmi ise
    if "ironscalelabs.com" in url:
        return item, False

    item_id = clean_key(name)
    filename = f"{item_id}.webp"
    cat_dir = os.path.join(IMAGES_DIR, cat)
    os.makedirs(cat_dir, exist_ok=True)
    save_path = os.path.join(cat_dir, filename)

    repo_url = f"{REPO_IMAGE_BASE}/{cat}/{filename}"

    if os.path.exists(save_path) and os.path.getsize(save_path) > 1000:
        item["imageUrl"] = repo_url
        return item, True

    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=12) as resp:
            data = resp.read()

        raw_img = Image.open(io.BytesIO(data))
        clean_img = remove_white_background(raw_img)
        clean_img.save(save_path, "WEBP", quality=90, method=6)

        item["imageUrl"] = repo_url
        return item, True
    except Exception as e:
        return item, False

def main():
    print("================================================================================")
    print("   HARDWARE WIZARD - TÜM GÖRSELLERİ ŞEFFAF WEBP'YE DÖNÜŞTÜRME & REPO AKTARIMI    ")
    print("================================================================================")

    category_files = [
        ("cpu", "cpu.json"),
        ("gpu", "video-card.json"),
        ("motherboard", "motherboard.json"),
        ("ram", "memory.json"),
        ("storage", "internal-hard-drive.json"),
        ("psu", "power-supply.json"),
        ("cooler", "cpu-cooler.json"),
        ("case", "case.json")
    ]

    total_processed = 0

    for cat, filename in category_files:
        src_path = os.path.join(CATALOG_DIR, filename)
        if not os.path.exists(src_path):
            continue

        with open(src_path, "r", encoding="utf-8") as f:
            items = json.load(f)

        print(f"\n[{cat.upper()}] {len(items)} ürünün görselleri işleniyor...")

        updated_items = []
        with ThreadPoolExecutor(max_workers=10) as executor:
            future_to_idx = {executor.submit(process_one_item, cat, item): i for i, item in enumerate(items)}
            results = [None] * len(items)
            for future in as_completed(future_to_idx):
                idx = future_to_idx[future]
                try:
                    res_item, was_proc = future.result()
                    results[idx] = res_item
                    if was_proc:
                        total_processed += 1
                except Exception:
                    results[idx] = items[idx]

        updated_items = results

        # JSON dosyalarını güncelle
        for d in [CATALOG_DIR, FILES_DIR]:
            p = os.path.join(d, filename)
            with open(p, "w", encoding="utf-8") as f:
                json.dump(updated_items, f, indent=2, ensure_ascii=False)

        # Alternatif dosya adları varsa (gpu.json, cooler.json, psu.json, storage.json, ram.json)
        alt_names = {
            "gpu": ["gpu.json"],
            "cooler": ["cooler.json"],
            "psu": ["psu.json"],
            "storage": ["storage.json"],
            "ram": ["ram.json"]
        }.get(cat, [])

        for alt in alt_names:
            for d in [CATALOG_DIR, FILES_DIR]:
                p = os.path.join(d, alt)
                with open(p, "w", encoding="utf-8") as f:
                    json.dump(updated_items, f, indent=2, ensure_ascii=False)

        print(f"[{cat.upper()}] Tamamlandı: {len(updated_items)} ürün güncellendi.")

    print("\n================================================================================")
    print(f"[BAŞARILI] Toplam {total_processed} görsel arka planından arındırılıp transparan WebP yapıldı.")
    print("Tüm kataloglar kendi repo URL'lerimizle (data/images/...) senkronize edildi.")
    print("================================================================================")

if __name__ == "__main__":
    main()
