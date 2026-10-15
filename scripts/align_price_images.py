import json
import os
import re

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
catalog_dir = os.path.join(ROOT, "data", "catalog")
prices_file = os.path.join(ROOT, "data", "prices_tr.json")
shared_prices_file = os.path.join(ROOT, "shared", "src", "commonMain", "composeResources", "files", "prices_tr.json")

cat_map = {
    "cpu.json": "cpu",
    "video-card.json": "gpu",
    "memory.json": "ram",
    "motherboard.json": "motherboard",
    "power-supply.json": "psu",
    "internal-hard-drive.json": "storage",
    "cpu-cooler.json": "cooler",
    "case.json": "case",
}

def sanitize_name(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")

with open(prices_file, "r", encoding="utf-8") as f:
    prices = json.load(f)

catalog_images = {}
for fname, cat in cat_map.items():
    p = os.path.join(catalog_dir, fname)
    if os.path.exists(p):
        with open(p, "r", encoding="utf-8") as f:
            items = json.load(f)
            for it in items:
                pid = f"{cat}_{sanitize_name(it['name'])}"
                if it.get("imageUrl"):
                    catalog_images[pid] = it["imageUrl"]

print(f"Found {len(catalog_images)} transparent catalog images.")
updated_count = 0
for pid, pdata in prices.items():
    if pid in catalog_images:
        cur = pdata.get("imageUrl")
        new_img = catalog_images[pid]
        if cur != new_img:
            pdata["imageUrl"] = new_img
            updated_count += 1

print(f"Updated {updated_count} price items to use transparent catalog image.")

with open(prices_file, "w", encoding="utf-8") as f:
    json.dump(prices, f, indent=2, ensure_ascii=False)

with open(shared_prices_file, "w", encoding="utf-8") as f:
    json.dump(prices, f, indent=2, ensure_ascii=False)

print("Saved both prices_tr.json files successfully.")
