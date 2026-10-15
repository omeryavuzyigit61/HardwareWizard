import json
import os

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")

categories = ["cpu", "video-card", "motherboard", "memory", "internal-hard-drive", "power-supply", "cpu-cooler", "case"]

total_items = 0
with_image = 0
cdn_images = 0
other_images = 0
missing_images = 0

for cat in categories:
    fpath = os.path.join(CATALOG_DIR, f"{cat}.json")
    if not os.path.exists(fpath):
        continue
    with open(fpath, "r", encoding="utf-8") as f:
        items = json.load(f)
    cat_missing = 0
    for it in items:
        total_items += 1
        img = it.get("imageUrl")
        if img:
            with_image += 1
            if "raw.githubusercontent.com" in img:
                cdn_images += 1
            else:
                other_images += 1
        else:
            cat_missing += 1
            missing_images += 1
    print(f"[{cat}] {len(items)} items, with image: {len(items) - cat_missing}, missing: {cat_missing}")

print(f"\nTotal items: {total_items}")
print(f"With image: {with_image}")
print(f"CDN images: {cdn_images}")
print(f"Other external images: {other_images}")
print(f"Missing images: {missing_images}")
