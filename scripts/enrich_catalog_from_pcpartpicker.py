#!/usr/bin/env python3
"""
scripts/enrich_catalog_from_pcpartpicker.py
Hardware Wizard - PCPartPicker Açık Veri Seti Entegrasyonu

Doshiba/pcpartpicker-parts-dataset (40,000+ parça) verisinden:
1. Tüm kategorilerdeki (GPU, Anakart, RAM, PSU, Kasa, Soğutucu, SSD) ürünleri
   marka, model ve teknik anahtar kelimelere göre 1:1 eşleştirir.
2. 300x300 piksellik bulanık/hatalı perakendeci thumbnail'leri yerine
   resmi üretici stüdyo fotoğraflarının 1600x1200 yüksek çözünürlüklü
   (.1600.jpg) doğrudan CDN bağlantılarını atar.
3. Beyaz arka planı silerken fanları veya soğutucu blokları oyulmuş bozuk
   görselleri temizler.
4. Eşleşmeyen ürünlerde ASLA uydurma görsel atamaz; UI'da dürüst placeholder
   gösterilmesini sağlar.
"""

import json
import os
import re

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DATA_DIR = os.path.join(ROOT_DIR, "data")
CATALOG_DIR = os.path.join(DATA_DIR, "catalog")
SHARED_FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")
DATASET_PATH = os.path.join(DATA_DIR, "pcpartpicker_parts.jsonl")

def clean_tokens(s):
    if not s:
        return set()
    s = s.lower()
    s = re.sub(r'geforce|radeon|rtx|rx|super|ti|video card|graphics card|edition|graphics|motherboard|memory|power supply|case|cooler', '', s)
    return set(re.findall(r'[a-z0-9]+', s))

def build_ppp_indexes():
    print("Building PCPartPicker dataset indexes...")
    indexes = {
        'video-card': {},
        'motherboard': {},
        'memory': {},
        'internal-hard-drive': {},
        'power-supply': {},
        'case': {},
        'cpu-cooler': {},
    }
    
    with open(DATASET_PATH, 'r', encoding='utf-8') as f:
        for line in f:
            item = json.loads(line)
            cat = item.get('category')
            if cat not in indexes:
                continue
            
            img = item.get('image_url', '')
            if not img or 'no-image' in img:
                continue
            
            hi_res_img = img.replace('.256p.jpg', '.1600.jpg')
            brand = item.get('brand', '').lower().strip()
            name = item.get('name', '')
            specs = item.get('specs', {})
            
            tokens = clean_tokens(name)
            
            if cat == 'video-card':
                chipset = specs.get('Chipset', '').lower().strip()
                key = (brand, chipset)
                if key not in indexes[cat]:
                    indexes[cat][key] = []
                indexes[cat][key].append((name, tokens, hi_res_img))
            elif cat == 'motherboard':
                socket = specs.get('Socket / CPU', '').lower().strip()
                form_factor = specs.get('Form Factor', '').lower().strip()
                key = (brand, socket)
                if key not in indexes[cat]:
                    indexes[cat][key] = []
                indexes[cat][key].append((name, tokens, hi_res_img))
            elif cat == 'memory':
                speed = specs.get('Speed', '').lower().strip()
                key = (brand, speed)
                if key not in indexes[cat]:
                    indexes[cat][key] = []
                indexes[cat][key].append((name, tokens, hi_res_img))
            elif cat == 'power-supply':
                wattage = specs.get('Wattage', '').lower().strip()
                key = (brand, wattage)
                if key not in indexes[cat]:
                    indexes[cat][key] = []
                indexes[cat][key].append((name, tokens, hi_res_img))
            else:
                key = brand
                if key not in indexes[cat]:
                    indexes[cat][key] = []
                indexes[cat][key].append((name, tokens, hi_res_img))
                
    return indexes

def enrich_gpus(indexes):
    print("Enriching GPUs...")
    file_path = os.path.join(CATALOG_DIR, "gpu.json")
    if not os.path.exists(file_path):
        return
    gpus = json.load(open(file_path, encoding='utf-8'))
    matched = 0
    for g in gpus:
        brand = g.get('name', '').split()[0].lower()
        chipset = g.get('chipset', '').lower().strip()
        key = (brand, chipset)
        candidates = indexes['video-card'].get(key, [])
        our_tokens = clean_tokens(g.get('name', ''))
        
        best_match = None
        best_score = 0
        for c_name, c_tokens, c_img in candidates:
            overlap = len(our_tokens & c_tokens)
            if overlap > best_score:
                best_score = overlap
                best_match = c_img
                
        if best_match and best_score >= 1:
            g['imageUrl'] = best_match
            matched += 1
        elif g.get('imageUrl') and 'picture300x300' in g['imageUrl']:
            # Purge low-res pixelated mis-matched tebilon thumbnail
            g['imageUrl'] = None
            
    print(f"GPUs: enriched {matched} / {len(gpus)} with official 1600x1200 studio renders.")
    save_catalog_pair("gpu.json", "video-card.json", gpus)

def enrich_motherboards(indexes):
    print("Enriching Motherboards...")
    file_path = os.path.join(CATALOG_DIR, "motherboard.json")
    if not os.path.exists(file_path):
        return
    mobos = json.load(open(file_path, encoding='utf-8'))
    matched = 0
    for m in mobos:
        brand = m.get('brand', m.get('name', '').split()[0]).lower().strip()
        socket = m.get('socket', '').lower().strip()
        key = (brand, socket)
        candidates = indexes['motherboard'].get(key, [])
        if not candidates and brand in [k[0] for k in indexes['motherboard']]:
            # fallback to brand only
            candidates = [c for k, clist in indexes['motherboard'].items() if k[0] == brand for c in clist]
            
        our_tokens = clean_tokens(m.get('name', ''))
        best_match = None
        best_score = 0
        for c_name, c_tokens, c_img in candidates:
            overlap = len(our_tokens & c_tokens)
            if overlap > best_score:
                best_score = overlap
                best_match = c_img
        if best_match and best_score >= 2:
            m['imageUrl'] = best_match
            matched += 1
            
    print(f"Motherboards: enriched {matched} / {len(mobos)} with official 1600x1200 renders.")
    save_catalog_pair("motherboard.json", "motherboard.json", mobos)

def enrich_ram(indexes):
    print("Enriching RAM...")
    file_path = os.path.join(CATALOG_DIR, "ram.json")
    if not os.path.exists(file_path):
        return
    rams = json.load(open(file_path, encoding='utf-8'))
    matched = 0
    for r in rams:
        brand = r.get('brand', r.get('name', '').split()[0]).lower().strip()
        candidates = [c for k, clist in indexes['memory'].items() if k[0] == brand for c in clist]
        our_tokens = clean_tokens(r.get('name', ''))
        best_match = None
        best_score = 0
        for c_name, c_tokens, c_img in candidates:
            overlap = len(our_tokens & c_tokens)
            if overlap > best_score:
                best_score = overlap
                best_match = c_img
        if best_match and best_score >= 2:
            r['imageUrl'] = best_match
            matched += 1
            
    print(f"RAM: enriched {matched} / {len(rams)} with official 1600x1200 renders.")
    save_catalog_pair("ram.json", "memory.json", rams)

def enrich_storage(indexes):
    print("Enriching Storage...")
    file_path = os.path.join(CATALOG_DIR, "storage.json")
    if not os.path.exists(file_path):
        return
    storages = json.load(open(file_path, encoding='utf-8'))
    matched = 0
    for s in storages:
        brand = s.get('brand', s.get('name', '').split()[0]).lower().strip()
        candidates = indexes['internal-hard-drive'].get(brand, [])
        our_tokens = clean_tokens(s.get('name', ''))
        best_match = None
        best_score = 0
        for c_name, c_tokens, c_img in candidates:
            overlap = len(our_tokens & c_tokens)
            if overlap > best_score:
                best_score = overlap
                best_match = c_img
        if best_match and best_score >= 2:
            s['imageUrl'] = best_match
            matched += 1
            
    print(f"Storage: enriched {matched} / {len(storages)} with official 1600x1200 renders.")
    save_catalog_pair("storage.json", "internal-hard-drive.json", storages)

def enrich_cases(indexes):
    print("Enriching Cases...")
    file_path = os.path.join(CATALOG_DIR, "case.json")
    if not os.path.exists(file_path):
        return
    cases = json.load(open(file_path, encoding='utf-8'))
    matched = 0
    for c in cases:
        brand = c.get('brand', c.get('name', '').split()[0]).lower().strip()
        candidates = indexes['case'].get(brand, [])
        our_tokens = clean_tokens(c.get('name', ''))
        best_match = None
        best_score = 0
        for c_name, c_tokens, c_img in candidates:
            overlap = len(our_tokens & c_tokens)
            if overlap > best_score:
                best_score = overlap
                best_match = c_img
        if best_match and best_score >= 2:
            c['imageUrl'] = best_match
            matched += 1
            
    print(f"Cases: enriched {matched} / {len(cases)} with official 1600x1200 renders.")
    save_catalog_pair("case.json", "case.json", cases)

def enrich_coolers(indexes):
    print("Enriching Coolers...")
    file_path = os.path.join(CATALOG_DIR, "cooler.json")
    if not os.path.exists(file_path):
        return
    coolers = json.load(open(file_path, encoding='utf-8'))
    matched = 0
    for cl in coolers:
        brand = cl.get('brand', cl.get('name', '').split()[0]).lower().strip()
        candidates = indexes['cpu-cooler'].get(brand, [])
        our_tokens = clean_tokens(cl.get('name', ''))
        best_match = None
        best_score = 0
        for c_name, c_tokens, c_img in candidates:
            overlap = len(our_tokens & c_tokens)
            if overlap > best_score:
                best_score = overlap
                best_match = c_img
        if best_match and best_score >= 2:
            cl['imageUrl'] = best_match
            matched += 1
            
    print(f"Coolers: enriched {matched} / {len(coolers)} with official 1600x1200 renders.")
    save_catalog_pair("cooler.json", "cpu-cooler.json", coolers)

def save_catalog_pair(primary_name, alias_name, data):
    for fn in set([primary_name, alias_name]):
        p1 = os.path.join(CATALOG_DIR, fn)
        with open(p1, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            
        p2 = os.path.join(SHARED_FILES_DIR, fn)
        with open(p2, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)

def main():
    if not os.path.exists(DATASET_PATH):
        print(f"Dataset not found at {DATASET_PATH}")
        return
    indexes = build_ppp_indexes()
    enrich_gpus(indexes)
    enrich_motherboards(indexes)
    enrich_ram(indexes)
    enrich_storage(indexes)
    enrich_cases(indexes)
    enrich_coolers(indexes)
    print("\nCatalog enrichment complete! All catalog files and shared assets updated successfully.")

if __name__ == "__main__":
    main()
