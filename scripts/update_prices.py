#!/usr/bin/env python3
"""
Hardware Wizard - Türkiye Canlı Donanım Fiyat Scraper Motoru (Akakçe & TR Mağazaları)
100% Gerçek verilere dayalı, sıfır tahmin / sıfır sentetik formül.
Tüm kategorilerdeki donanımları Türkiye piyasasında arar, filtreler,
en ucuzdan pahalıya mağaza tekliflerini (offers) ve güncel fiyatı kaydeder.
Eğer ürün Türkiye pazarında bulunamazsa: currentPriceTry = null, offers = [].
"""

import json
import os
import re
import sys
import time
import urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
from bs4 import BeautifulSoup

try:
    from curl_cffi import requests as cffi_requests
    HAS_CURL_CFFI = True
except ImportError:
    import requests as cffi_requests
    HAS_CURL_CFFI = False

# Windows konsolunda UTF-8 karakterlerin (örn. ₺) düzgün yazılması için
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

TARGET_FILES = [
    os.path.join(ROOT_DIR, "data", "prices_tr.json"),
    os.path.join(FILES_DIR, "prices_tr.json")
]

CATEGORY_FILE_MAP = {
    "cpu": ["cpu.json"],
    "gpu": ["video-card.json", "gpu.json"],
    "motherboard": ["motherboard.json"],
    "ram": ["memory.json", "ram.json"],
    "storage": ["internal-hard-drive.json", "storage.json"],
    "psu": ["power-supply.json", "psu.json"],
    "cooler": ["cpu-cooler.json", "cooler.json"],
    "case": ["case.json"]
}

NEGATIVE_KEYWORDS = [
    "hazır sistem", "hazir sistem", "tavsiye sistem", "oem paket",
    "oyuncu bilgisayarı", "oyuncu bilgisayari", "masaüstü bilgisayar", "masaustu bilgisayar",
    "masaüstü", "masaustu", "kasa toplama", "gaming pc", "hazır kasa",
    "laptop", "notebook", "dizüstü", "dizustu",
    "tutucu", "kablo", "aparat", "braket", "bracket", "dönüştürücü", "donusturucu",
    "termal macun", "adaptör", "adaptor", "vida", "fan vidası", "ekipman",
    "çanta", "canta", "askı", "aski", "temizlik", "temizleme", "stant", "stand"
]

STORE_BADGE_WORDS = ["kargo dahil", "en ucuz", "bugün kargo", "yüksek puanlı", "tek fiyat", "+1 fiyat", "taksitli"]


def sanitize_name(name: str) -> str:
    cleaned = re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")
    return cleaned


def generate_hardware_id(category_prefix: str, name: str) -> str:
    return f"{category_prefix}_{sanitize_name(name)}"


def clean_price(txt: str) -> float | None:
    if not txt:
        return None
    m = re.search(r"([\d\.]+)\s*(?:,\s*(\d+))?", txt)
    if m:
        main = m.group(1).replace(".", "")
        sub = m.group(2) if m.group(2) else "0"
        try:
            val = float(f"{main}.{sub}")
            return val if val > 0 else None
        except ValueError:
            return None
    return None


def clean_store_name(raw: str) -> str:
    if not raw:
        return "Piyasa Satıcısı"
    cleaned = raw.strip()
    cleaned = re.sub(r"^Satıcı:\s*", "", cleaned, flags=re.I).strip()
    lower = cleaned.lower()
    for badge in STORE_BADGE_WORDS:
        if badge in lower:
            return "Akakçe En Ucuz"
    if len(cleaned) < 2 or "/" in cleaned or len(cleaned) > 40:
        return "Piyasa Satıcısı"
    return cleaned


def extract_search_and_tokens(category: str, name: str) -> tuple[str, list[str]]:
    clean_name = re.sub(r"[\(\)\[\],]", " ", name).strip()
    words = clean_name.split()
    if not words:
        return name, [name.lower()]

    if category == "cpu":
        m = re.search(r"([0-9]{4,5}[A-Z0-9]*)", name, re.I)
        if m:
            token = m.group(1).upper()
            return f"{words[0]} {token}", [token]
        return " ".join(words[:3]), [words[1]] if len(words) > 1 else words

    if category == "gpu":
        m = re.search(r"(RTX\s*[0-9]{4}(?:\s*Ti)?(?:\s*SUPER)?|RX\s*[0-9]{4}(?:\s*XTX|\s*XT|\s*GRE)?)", name, re.I)
        if m:
            chipset = re.sub(r"\s+", " ", m.group(1)).strip().upper()
            tokens = chipset.split()
            return chipset, tokens
        return " ".join(words[:4]), words[:2]

    if category == "motherboard":
        m = re.search(r"\b([ABXZ][0-9]{3}[A-Z]?)\b", name, re.I)
        if m:
            chipset = m.group(1).upper()
            return f"{words[0]} {chipset}", [chipset]
        return " ".join(words[:3]), [words[0]]

    if category in ["ram", "storage", "psu", "cooler", "case"]:
        brand = words[0]
        model_part = words[1] if len(words) > 1 else ""
        return f"{brand} {model_part}".strip(), [brand.lower(), model_part.lower()]

    return " ".join(words[:3]), [words[0].lower()]


def fetch_real_offers_from_akakce(query: str, required_tokens: list[str]) -> list[dict]:
    url = f"https://www.akakce.com/arama/?q={urllib.parse.quote(query)}"
    try:
        if HAS_CURL_CFFI:
            r = cffi_requests.get(url, impersonate="chrome124", timeout=12)
        else:
            headers = {"User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/124.0.0.0 Safari/537.36"}
            r = cffi_requests.get(url, headers=headers, timeout=12)

        if r.status_code != 200:
            return []
        soup = BeautifulSoup(r.text, "html.parser")
    except Exception:
        return []

    candidate_products = []
    for li in soup.find_all("li", attrs={"data-pr": True}):
        t_el = li.find("h3", class_="pn_v8") or li.find("a", class_="pn_v8") or li.find("a", title=True)
        title = t_el.get_text(strip=True) if t_el else ""
        title_lower = title.lower()

        # Strict filtering: exclude negative items
        if any(neg in title_lower for neg in NEGATIVE_KEYWORDS):
            continue

        # Strict token match: all required tokens must be in title
        if not all(tok.lower() in title_lower for tok in required_tokens):
            continue

        link_el = li.find("a", class_="iC") or li.find("a")
        href = link_el.get("href", "") if link_el else ""
        if href and not href.startswith("http"):
            href = f"https://www.akakce.com{href}"

        p_el = li.find(lambda tag: tag.name == "span" and tag.get("class") and any(c.startswith("pt_") for c in tag.get("class")))
        price = clean_price(p_el.get_text(separator=" ", strip=True)) if p_el else None

        candidate_products.append({
            "title": title,
            "url": href,
            "price": price
        })

    if not candidate_products:
        return []

    best_candidate = candidate_products[0]
    detail_url = best_candidate.get("url", "")
    offers = []

    # If detail URL exists on Akakçe, scrape multi-store offers list (#PL)
    if detail_url and "akakce.com" in detail_url:
        try:
            if HAS_CURL_CFFI:
                rd = cffi_requests.get(detail_url, impersonate="chrome124", timeout=12)
            else:
                rd = cffi_requests.get(detail_url, timeout=12)

            if rd.status_code == 200:
                dsoup = BeautifulSoup(rd.text, "html.parser")
                ul = dsoup.find("ul", id="PL")
                if ul:
                    for s_li in ul.find_all("li"):
                        store = None
                        v_el = s_li.find(class_=re.compile(r"v_v|seller|logo"))
                        if v_el:
                            img = v_el.find("img")
                            if img and img.get("alt"):
                                store = clean_store_name(img.get("alt"))
                            else:
                                store = clean_store_name(v_el.get_text(strip=True))

                        if not store or store == "Akakçe En Ucuz":
                            img = s_li.find("img")
                            if img and img.get("alt"):
                                store = clean_store_name(img.get("alt"))

                        if not store or store == "Akakçe En Ucuz":
                            seller_tag = s_li.find(class_=re.compile(r"s_v|merchant|sh_"))
                            if seller_tag:
                                store = clean_store_name(seller_tag.get_text(strip=True))

                        if not store:
                            store = "Piyasa Satıcısı"

                        p_el = s_li.find(class_=re.compile(r"pt_v|price|pr_"))
                        price = clean_price(p_el.get_text(separator=" ", strip=True)) if p_el else None

                        link = s_li.find("a", href=True)
                        href = link.get("href", "") if link else ""
                        if href and not href.startswith("http"):
                            href = f"https://www.akakce.com{href}"

                        if price and price > 0 and href:
                            offers.append({
                                "storeName": store,
                                "priceTry": price,
                                "url": href,
                                "inStock": True
                            })
        except Exception:
            pass

    # If #PL was empty or unlisted, fall back to best candidate price
    if not offers and best_candidate.get("price") and best_candidate.get("url"):
        offers.append({
            "storeName": "Akakçe En Ucuz Mağaza",
            "priceTry": best_candidate["price"],
            "url": best_candidate["url"],
            "inStock": True
        })

    # Deduplicate offers by storeName and sort by priceTry ascending
    offers.sort(key=lambda x: x["priceTry"])
    seen_stores = set()
    deduped = []
    for o in offers:
        s_key = o["storeName"].lower()
        if s_key not in seen_stores:
            seen_stores.add(s_key)
            deduped.append(o)

    return deduped


def scan_and_update_prices():
    today_str = datetime.now().strftime("%Y-%m-%d")
    primary_target = os.path.abspath(TARGET_FILES[0])

    data = {}
    if os.path.exists(primary_target):
        try:
            with open(primary_target, "r", encoding="utf-8") as f:
                data = json.load(f)
        except Exception as e:
            print(f"Uyarı: Mevcut prices_tr.json okunamadı ({e}), yeni oluşturuluyor.")

    print(f"Mevcut kayıtlı ürün sayısı: {len(data)}")

    # 1. Katalog parçalarını topla
    catalog_items = []
    seen_ids = set()

    for category_prefix, file_names in CATEGORY_FILE_MAP.items():
        for fname in file_names:
            file_path = os.path.join(FILES_DIR, fname)
            if not os.path.exists(file_path):
                continue

            try:
                with open(file_path, "r", encoding="utf-8") as f:
                    items = json.load(f)
            except Exception as e:
                print(f"Hata: {fname} okunamadı: {e}")
                continue

            if not isinstance(items, list):
                continue

            for item in items:
                if not isinstance(item, dict):
                    continue
                name = item.get("name")
                if not name or not name.strip():
                    continue

                product_id = generate_hardware_id(category_prefix, name)
                if product_id in seen_ids:
                    continue
                seen_ids.add(product_id)

                query, tokens = extract_search_and_tokens(category_prefix, name)
                catalog_items.append({
                    "product_id": product_id,
                    "category": category_prefix,
                    "name": name,
                    "query": query,
                    "tokens": tokens,
                })

    print(f"Toplam taranacak benzersiz katalog donanımı: {len(catalog_items)}")

    # 2. Canlı Türkiye Mağaza Scraper'ı (Çoklu İş parçacığı ile hızlı tarama)
    matched_count = 0
    unmatched_count = 0

    def process_item(item_info):
        q = item_info["query"]
        tokens = item_info["tokens"]
        time.sleep(0.4)
        offers = fetch_real_offers_from_akakce(q, tokens)
        return item_info["product_id"], offers

    print("Gerçek Türkiye mağaza ve Akakçe verileri taranıyor...")
    with ThreadPoolExecutor(max_workers=3) as executor:
        futures = {executor.submit(process_item, it): it for it in catalog_items}
        for future in as_completed(futures):
            product_id, offers = future.result()
            existing_info = data.get(product_id, {})
            history = existing_info.get("priceHistory", [])

            if offers:
                matched_count += 1
                cheapest_price = offers[0]["priceTry"]

                # Sadece gerçek tespit edilen günün en ucuz fiyatını geçmişe ekle
                if not history or history[-1].get("date") != today_str:
                    history.append({"date": today_str, "price": cheapest_price})
                else:
                    history[-1]["price"] = min(history[-1]["price"], cheapest_price)

                all_prices = [p["price"] for p in history if p.get("price")]
                data[product_id] = {
                    "currentPriceTry": cheapest_price,
                    "lowestPriceTry": min(all_prices) if all_prices else cheapest_price,
                    "highestPriceTry": max(all_prices) if all_prices else cheapest_price,
                    "currency": "TRY",
                    "offers": offers,
                    "priceHistory": history,
                }
            elif existing_info.get("offers") and existing_info.get("currentPriceTry") is not None:
                # Önceki taramalarda bulunan gerçek teklifleri koru
                matched_count += 1
                data[product_id] = existing_info
            else:
                unmatched_count += 1
                # Kesinlikle uydurma/sentetik fiyat yazma!
                # Eğer TR piyasasında stok veya fiyat yoksa null ve boş liste bırak!
                data[product_id] = {
                    "currentPriceTry": None,
                    "lowestPriceTry": existing_info.get("lowestPriceTry"),
                    "highestPriceTry": existing_info.get("highestPriceTry"),
                    "currency": "TRY",
                    "offers": [],
                    "priceHistory": history,
                }

    print(f"Tarama tamamlandı: {matched_count} parça için canlı TR mağaza fiyatı bulundu, {unmatched_count} parça stokta yok/listelenmedi.")

    # 3. Dosyaları kaydet
    for target in TARGET_FILES:
        target_path = os.path.abspath(target)
        target_dir = os.path.dirname(target_path)
        if os.path.exists(target_dir):
            with open(target_path, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            print(f"Başarıyla güncellendi: {target_path}")

    print("Tüm Türkiye mağaza fiyatları ve teklifleri başarıyla işlendi.")


if __name__ == "__main__":
    scan_and_update_prices()
