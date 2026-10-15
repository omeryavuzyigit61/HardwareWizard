import json
import os
import re

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
catalog_file = os.path.join(ROOT, "data", "catalog", "power-supply.json")
shared_catalog_file = os.path.join(ROOT, "shared", "src", "commonMain", "composeResources", "files", "power-supply.json")
psu_dir = os.path.join(ROOT, "data", "images", "psu")

available_images = os.listdir(psu_dir)
print(f"Total available PSU images: {len(available_images)}")

with open(catalog_file, "r", encoding="utf-8") as f:
    psus = json.load(f)

def sanitize(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")

matched = 0
for p in psus:
    name = p.get("name", "")
    s_name = sanitize(name)
    best_img = None

    # 1. Exact match
    if f"{s_name}.webp" in available_images:
        best_img = f"{s_name}.webp"
    else:
        # 2. Token matching
        tokens = set(s_name.split("_"))
        best_score = 0
        for img in available_images:
            img_tokens = set(img.replace(".webp", "").split("_"))
            common = tokens.intersection(img_tokens)
            # Wattage token must match if present in both
            w1 = re.search(r"\b\d{3,4}w\b", s_name)
            w2 = re.search(r"\b\d{3,4}w\b", img)
            if w1 and w2 and w1.group(0) != w2.group(0):
                continue
            if len(common) > best_score and len(common) >= 3:
                best_score = len(common)
                best_img = img

    if best_img:
        p["imageUrl"] = f"https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/{best_img}"
        matched += 1
    elif not p.get("imageUrl"):
        # Brand fallback
        if "corsair" in s_name:
            p["imageUrl"] = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/corsair_rmx_2024_atx_3_1_1000w.webp"
            matched += 1
        elif "asus" in s_name:
            p["imageUrl"] = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/asus_tuf_gaming_gold_atx_3_0_1000w.webp"
            matched += 1
        elif "cooler_master" in s_name:
            p["imageUrl"] = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/cooler_master_mwe_gold_v2_1050w.webp"
            matched += 1

print(f"Matched {matched} / {len(psus)} PSUs with official transparent images.")

with open(catalog_file, "w", encoding="utf-8") as f:
    json.dump(psus, f, indent=2, ensure_ascii=False)

with open(shared_catalog_file, "w", encoding="utf-8") as f:
    json.dump(psus, f, indent=2, ensure_ascii=False)

# Also update psu.json
for d in [os.path.join(ROOT, "data", "catalog"), os.path.join(ROOT, "shared", "src", "commonMain", "composeResources", "files")]:
    with open(os.path.join(d, "psu.json"), "w", encoding="utf-8") as f:
        json.dump(psus, f, indent=2, ensure_ascii=False)

print("Saved all PSU catalog files.")
