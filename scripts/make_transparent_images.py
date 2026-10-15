#!/usr/bin/env python3
"""
scripts/make_transparent_images.py
Hardware Wizard - Otomatik Şeffaf Görsel & Arka Plan Temizleme Motoru
Beyaz / açık renkli dikdörtgen arka planları köşe flood-fill algoritması ile
saf alfa kanalına (transparan WebP) dönüştürür. Ürünlerin kendisi (beyaz yazı/logo/soğutucu blok)
asla bozulmaz.
"""

import io
import os
import re
import sys
import urllib.request
from collections import deque
from concurrent.futures import ThreadPoolExecutor, as_completed
from PIL import Image

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
IMAGE_OUTPUT_DIR = os.path.join(ROOT_DIR, "data", "images")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8",
}

def remove_white_background(img: Image.Image, threshold_avg: int = 232, max_diff: int = 25) -> Image.Image:
    """
    4 kenardan flood-fill yaparak sadece dış arka planı şeffaf yapar.
    Ürün içindeki beyaz kısımlara dokunmaz.
    """
    img = img.convert("RGBA")
    w, h = img.size
    pix = img.load()

    visited = set()
    queue = deque()

    # Kenar piksellerini kuyruğa ekle
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

        # Beyaz veya açık gri tonlu arka plan pikseli mi?
        if avg >= threshold_avg and diff <= max_diff:
            pix[x, y] = (r, g, b, 0)
            for nx, ny in [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]:
                if 0 <= nx < w and 0 <= ny < h and (nx, ny) not in visited:
                    visited.add((nx, ny))
                    queue.append((nx, ny))

    # Transparan sınırları kırp (autocrop bounding box with slight margin)
    bbox = img.getbbox()
    if bbox:
        pad = 8
        left = max(0, bbox[0] - pad)
        top = max(0, bbox[1] - pad)
        right = min(w, bbox[2] + pad)
        bottom = min(h, bbox[3] + pad)
        img = img.crop((left, top, right, bottom))

    return img

def download_and_process_image(url: str, save_path: str) -> bool:
    if not url or not url.startswith("http"):
        return False
    if os.path.exists(save_path) and os.path.getsize(save_path) > 1000:
        return True

    os.makedirs(os.path.dirname(save_path), exist_ok=True)
    try:
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=12) as resp:
            data = resp.read()
        
        raw_img = Image.open(io.BytesIO(data))
        
        # Eğer zaten transparan PNG/WebP ise olduğu gibi kaydet
        if raw_img.mode in ("RGBA", "LA") or (raw_img.mode == "P" and "transparency" in raw_img.info):
            raw_img.convert("RGBA").save(save_path, "WEBP", quality=90, method=6)
            return True

        # Beyaz arka planı kaldır
        clean_img = remove_white_background(raw_img)
        clean_img.save(save_path, "WEBP", quality=90, method=6)
        return True
    except Exception as e:
        return False

def sanitize_filename(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")
