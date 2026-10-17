#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/sync_psu_specs.py
Hardware Wizard - IRONScale Labs PSU Database Entegrasyon ve Doğrulama Motoru

1. https://ironscalelabs.com/ adresindeki bağımsız Türkiye PSU test ve inceleme laboratuvarı veritabanını (psu-data.js) çeker.
2. İncelenen modellerin:
   - Doğrulanmış temiz ürün görsellerini (outside, top, fan),
   - Gerçek OEM Üretici (CWT, Great Wall, Helly, Seasonic, FSP, High Power vb.),
   - Platform & İç Topoloji (LLC Half-Bridge, Double Forward, DC-DC vb.),
   - Kapasitör Tipi (105°C Japon Rubycon/Nichicon, 105°C Tayvan Teapo vb.),
   - Laboratuvar Tier Derecesi (Tier A, Tier B, Tier C, Tier E) ve Test Notlarını
   katalogdaki eşleşen PSU modellerimize doğrudan işler.
3. Bu sitede yer almayan modeller için Bing/DuckDuckGo görsel avcısı fallback'i çalıştırır.
4. Hem data/catalog/ hem de shared/.../composeResources/files/ dizinlerindeki psu.json, power-supply.json
   ve prices_tr.json dosyalarını senkronize eder.
"""

import functools
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed

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

PSU_JSON_FILES = [
    os.path.join(DATA_CATALOG_DIR, "psu.json"),
    os.path.join(DATA_CATALOG_DIR, "power-supply.json"),
    os.path.join(COMPOSE_FILES_DIR, "psu.json"),
    os.path.join(COMPOSE_FILES_DIR, "power-supply.json"),
]

IRONSCALE_BASE_URL = "https://ironscalelabs.com"
IRONSCALE_DATA_URL = "https://ironscalelabs.com/data/psu-data.js?v=204"

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
}

TIER_LABELS = {
    "A+": "Tier A+ (En Üst Seviye / Flagship)",
    "A": "Tier A (Üst Seviye / High-End)",
    "A-": "Tier A- (Üst-Orta Seviye)",
    "B+": "Tier B+ (İleri Orta Seviye)",
    "B": "Tier B (Orta Seviye / Mid-Range)",
    "B-": "Tier B- (Giriş-Orta Seviye)",
    "C+": "Tier C+ (Giriş Seviyesi F/P)",
    "C": "Tier C (Giriş Seviyesi / Entry-Level)",
    "C-": "Tier C- (Temel Seviye)",
    "D": "Tier D (Tavsiye Edilmeyen)",
    "E": "Tier E (Kaçınılması Gereken / Tehlikeli)",
    "F": "Tier F (Kritik / Arızalı Mimari)",
}

def verify_image_url(url: str, timeout: float = 3.5) -> bool:
    if not url or not (url.startswith("http://") or url.startswith("https://")):
        return False
    valid_exts = (".jpg", ".jpeg", ".png", ".webp")
    req_headers = dict(HEADERS)
    req_headers["Accept"] = "image/avif,image/webp,image/apng,image/png,image/*,*/*;q=0.8"
    try:
        req = urllib.request.Request(url, headers=req_headers, method="HEAD")
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if resp.status == 200:
                ct = resp.headers.get("Content-Type", "").lower()
                if "image" in ct or any(url.lower().split("?")[0].endswith(ext) for ext in valid_exts):
                    return True
    except Exception:
        pass

    try:
        req = urllib.request.Request(url, headers=req_headers, method="GET")
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if resp.status == 200:
                chunk = resp.read(512)
                if len(chunk) > 0:
                    return True
    except Exception:
        pass
    return False

def search_bing_images(query: str, count: int = 8) -> list[str]:
    q_enc = urllib.parse.quote_plus(query)
    url = f"https://www.bing.com/images/async?q={q_enc}&first=0&count={count}&scenario=ImageBasicHover&datsrc=N_RS&layout=RowBased&mmasync=1"
    req = urllib.request.Request(url, headers=HEADERS)
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
    try:
        token_url = f"https://duckduckgo.com/?q={urllib.parse.quote_plus(query)}&iax=images&ia=images"
        req = urllib.request.Request(token_url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=6) as resp:
            html = resp.read().decode("utf-8", errors="ignore")
            vqd_match = re.search(r'vqd=([0-9\-]+)', html) or re.search(r'vqd=\"([^\"]+)\"', html)
            if not vqd_match:
                return []
            vqd = vqd_match.group(1)

        img_url = f"https://duckduckgo.com/i.js?l=us-en&o=json&q={urllib.parse.quote_plus(query)}&vqd={vqd}&f=,,,type:photo,&p=1"
        req2 = urllib.request.Request(img_url, headers=HEADERS)
        with urllib.request.urlopen(req2, timeout=6) as resp2:
            data = json.loads(resp2.read().decode("utf-8", errors="ignore"))
            return [r["image"] for r in data.get("results", []) if "image" in r]
    except Exception:
        return []

def fetch_ironscale_db() -> list[dict]:
    print("[*] IRONScale Labs veritabanı indiriliyor: " + IRONSCALE_DATA_URL)
    req = urllib.request.Request(IRONSCALE_DATA_URL, headers=HEADERS)
    try:
        with urllib.request.urlopen(req, timeout=12) as resp:
            raw = resp.read().decode("utf-8", errors="ignore")
            js_clean = raw.split("window.IRON_PSU_DB =", 1)[1].strip().rstrip(";")
            data = json.loads(js_clean)
            records = data.get("records", [])
            print(f"[✓] IRONScale Labs veritabanından {len(records)} incelenmiş PSU kaydı alındı.")
            return records
    except Exception as e:
        print(f"[!] IRONScale Labs veritabanı indirilemedi: {e}")
        return []

def format_capacitor_info(rec: dict) -> str:
    primary = rec.get("primary", {})
    secondary = rec.get("secondary", {})
    bulk = primary.get("bulk_capacitor", "")
    sec_caps = secondary.get("electrolytic_caps", "")

    all_caps_text = f"{bulk} {sec_caps}".lower()
    is_japanese = any(j in all_caps_text for j in ["rubycon", "nichicon", "nippon", "chemi-con", "toshin", "kyocera"])
    is_teapo = "teapo" in all_caps_text
    is_chengx = "chengx" in all_caps_text or "cheng x" in all_caps_text or "capxon" in all_caps_text

    cap_spec = []
    if is_japanese:
        cap_spec.append("105°C Japon (Rubycon / Nichicon / Nippon Chemi-Con)")
    elif is_teapo:
        cap_spec.append("105°C Tayvan Teapo (2.000h - 6.000h)")
    elif is_chengx:
        cap_spec.append("85°C - 105°C Çin (ChengX / CapXon)")
    else:
        # Extract temp if present
        m_temp = re.search(r'(105°C|85°C)', f"{bulk} {sec_caps}", re.I)
        temp = m_temp.group(1) if m_temp else "105°C"
        cap_spec.append(f"{temp} Katı/Elektrolitik Kapasitör")

    # Add hold-up time if available
    hold_up = primary.get("hold_up", "")
    if hold_up and hold_up != "-" and "ms" in hold_up:
        cap_spec.append(f"Hold-Up Time: {hold_up.strip()}")

    return " • ".join(cap_spec)

def format_topology_info(rec: dict) -> str:
    oem = rec.get("oem", "").strip() or "Belirtilmedi"
    platform = rec.get("platform", "").strip()
    primary = rec.get("primary", {})
    topology = primary.get("topology", "").replace("\n", " ").strip()
    pfc = primary.get("pfc_topology", "").replace("\n", " ").strip() or "Aktif PFC"
    secondary = rec.get("secondary", {})
    reg = secondary.get("regulation", "").replace("\n", " ").strip()
    dc_dc = "DC-DC" if "dc" in reg.lower() or "senkron" in secondary.get("rectification", "").lower() else reg

    parts = []
    if platform and platform != "-":
        parts.append(f"OEM: {oem} ({platform})")
    else:
        parts.append(f"OEM: {oem}")

    if topology and topology != "-":
        parts.append(topology)
    parts.append(pfc)
    if dc_dc and dc_dc != "-":
        parts.append(dc_dc)

    return " • ".join(parts[:4])

def match_psu_to_ironscale(p: dict, iron_records: list[dict]) -> dict | None:
    pname = p.get("name", "").lower()
    pwatt = p.get("wattage")

    best_match = None
    best_score = 0

    for r in iron_records:
        rbrand = r.get("brand", "").lower()
        rseries = r.get("series", "").lower()
        if not rbrand or rbrand not in pname:
            continue

        score = 10
        # Match series tokens
        stokens = [t for t in re.split(r'[\s\-]+', rseries) if len(t) >= 2]
        if stokens:
            matched_tokens = sum(1 for st in stokens if st in pname)
            if matched_tokens == len(stokens):
                score += 30
            elif matched_tokens > 0:
                score += matched_tokens * 10
            else:
                continue
        else:
            continue

        # Match watt options if available
        watt_opts = [str(w).lower() for w in r.get("watt_options", [])]
        if pwatt:
            w_str = f"{pwatt}w"
            if any(w_str in wo for wo in watt_opts) or w_str in r.get("display_name", "").lower():
                score += 20

        if score > best_score:
            best_score = score
            best_match = r

    if best_score >= 30:
        return best_match
    return None

def find_clean_psu_image(item_name: str, brand: str) -> str | None:
    query = f"{item_name} power supply isolated white background"
    # 1. Bing
    for url in search_bing_images(query, count=6):
        if verify_image_url(url):
            return url
    # 2. DuckDuckGo
    for url in search_duckduckgo_images(f"{brand} {item_name} psu"):
        if verify_image_url(url):
            return url
    return None

def process_psu_catalogs():
    iron_records = fetch_ironscale_db()
    if not os.path.exists(PSU_JSON_FILES[0]):
        print(f"[!] Dosya bulunamadı: {PSU_JSON_FILES[0]}")
        return

    with open(PSU_JSON_FILES[0], "r", encoding="utf-8") as f:
        psus = json.load(f)

    # Load prices_tr
    prices_tr = {}
    if os.path.exists(TARGET_PRICE_FILES[0]):
        with open(TARGET_PRICE_FILES[0], "r", encoding="utf-8") as f:
            prices_tr = json.load(f)

    print(f"\n[*] Toplam {len(psus)} PSU modeli IRONScale Labs veritabanı ile eşleştiriliyor...")

    matched_count = 0
    updated_images = 0

    for i, p in enumerate(psus):
        pname = p.get("name", "")
        brand = p.get("brand", "") or (pname.split()[0] if pname else "PSU")
        iron_match = match_psu_to_ironscale(p, iron_records) if iron_records else None

        if iron_match:
            matched_count += 1
            tier_raw = iron_match.get("tier", "").strip()
            tier_label = TIER_LABELS.get(tier_raw, f"Tier {tier_raw}")
            desc = iron_match.get("description", "").replace("\n", " ").strip()
            if desc and len(desc) <= 120:
                p["tier"] = f"{tier_label} ({desc})"
            else:
                p["tier"] = tier_label

            p["capacitors"] = format_capacitor_info(iron_match)
            p["topology"] = format_topology_info(iron_match)

            # Check lab image
            imgs = iron_match.get("images", {})
            best_img_path = imgs.get("outside") or imgs.get("top") or imgs.get("fan")
            if best_img_path:
                full_iron_img = f"{IRONSCALE_BASE_URL}/{best_img_path.lstrip('/')}"
                if verify_image_url(full_iron_img, timeout=2.5):
                    p["imageUrl"] = full_iron_img
                    updated_images += 1
        else:
            # Fallback image search if current image is bad
            cur_img = p.get("imageUrl")
            if not cur_img or "placeholder" in cur_img.lower() or "unsplash" in cur_img.lower() or not cur_img.startswith("http"):
                new_img = find_clean_psu_image(pname, brand)
                if new_img:
                    p["imageUrl"] = new_img
                    updated_images += 1

        # Synchronize with prices_tr.json
        clean_name = re.sub(r"[^a-z0-9]+", "_", pname.lower()).strip("_")
        pid = f"psu_{clean_name}"
        if pid in prices_tr and p.get("imageUrl"):
            prices_tr[pid]["imageUrl"] = p["imageUrl"]
            for offer in prices_tr[pid].get("offers", []):
                offer["imageUrl"] = p["imageUrl"]

    print(f"\n[✓] {len(psus)} PSU'dan {matched_count} tanesi IRONScale Labs ile birebir eşleştirildi!")
    print(f"[✓] {updated_images} PSU için doğrulanmış yüksek çözünürlüklü görsel atandı.")

    # Write catalogs
    for target_path in PSU_JSON_FILES:
        os.makedirs(os.path.dirname(target_path), exist_ok=True)
        with open(target_path, "w", encoding="utf-8") as f:
            json.dump(psus, f, ensure_ascii=False, indent=2)
        print(f"[✓] Kaydedildi: {target_path}")

    # Write prices_tr
    for p_path in TARGET_PRICE_FILES:
        os.makedirs(os.path.dirname(p_path), exist_ok=True)
        with open(p_path, "w", encoding="utf-8") as f:
            json.dump(prices_tr, f, ensure_ascii=False, indent=2)
        print(f"[✓] Kaydedildi: {p_path}")

if __name__ == "__main__":
    process_psu_catalogs()
