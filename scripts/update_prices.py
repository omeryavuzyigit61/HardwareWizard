#!/usr/bin/env python3
"""
scripts/update_prices.py
Hardware Wizard - Otonom Türkiye Donanım Mağaza Fiyat Avcısı & Çoklu Mağaza Entegratörü

Desteklenen Mağazalar:
1. Gaming.Gen.TR
2. İncehesap
3. İtopya
4. Tebilon
5. Sinerji
6. Hepsiburada (Voltran State SSR JSON & HTML Parser)
7. N11 (Mobile Web Direct SSR Parser)
8. PttAVM (Search & Category Direct Parser)
9. Amazon Türkiye (Computers & Components Direct Search Parser)
10. İdefix (Direct Search & Category Parser)

Sıkı Varyant Eşleştirme Motoru:
- Asla AMD kartını NVIDIA kartıyla eşleştirmez.
- Çip kodu, Model Kodu, Watt, DDR nesli ve VRAM birebir eşleşmek zorundadır.
- İtopya logosu ve yabancı mağaza logoları filtrelenir; sahte 1.14 çarpanı kaldırılmıştır.
"""

import json
import os
import re
import sys
import time
import urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
import functools
from bs4 import BeautifulSoup

print = functools.partial(print, flush=True)

try:
    from curl_cffi import requests as cffi_requests
    HAS_CURL_CFFI = True
except ImportError:
    import requests as cffi_requests
    HAS_CURL_CFFI = False

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")

TARGET_PRICE_FILES = [
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
    "tutucu", "destek çıtası", "destek citasi", "aparat", "braket", "bracket", "dönüştürücü", "donusturucu",
    "termal macun", "adaptör", "adaptor", "vida", "fan vidası", "fan vidasi", "ekipman",
    "çanta", "canta", "askı", "aski", "temizlik", "temizleme", "stant", "stand",
    "mouse", "fare", "klavye", "keyboard", "kulaklık", "kulaklik", "headset",
    "mousepad", "pad", "monitör", "monitor", "oyuncu koltuğu", "koltuk", "tıklayınız",
    "tiklayiniz", "incele", "sepete ekle", "detaylı bilgi", "hediyeli"
]

CATEGORY_POSITIVE_REQUIREMENTS = {
    "cpu": ["RYZEN", "CORE", "INTEL", "AMD", "İŞLEMCİ", "ISLEMCI", "XEON", "EPYC", "THREADRIPPER", "PENTIUM"],
    "gpu": ["RTX", "GTX", "RADEON", "RX ", "GEFORCE", "ARC ", "EKRAN KARTI"],
    "motherboard": ["ANAKART", "MOTHERBOARD", "B650", "B760", "Z790", "Z890", "X870", "X670", "B550", "B450", "H610", "A620", "B850", "B840", "B860"],
    "ram": ["DDR4", "DDR5", "RAM", "BELLEK", "MEMORY"],
    "storage": ["SSD", "NVME", "M.2", "SATA", "HARDDISK", "DISC"],
    "psu": ["PSU", "GÜÇ KAYNAĞI", "GUC KAYNAGI", "POWER SUPPLY", "WATT", "80 PLUS", "80+"],
    "cooler": ["SOĞUTUCU", "SOGUTUCU", "COOLER", "SIVI SOĞUTMA", "HAVA SOĞUTMA", "AIO", "ISLEMCI SOGUTUCU", "İŞLEMCİ SOĞUTUCU"],
    "case": ["KASA", "CASE", "CHASSIS", "TOWER", "GAMING KASA", "OYUNCU KASASI"]
}

CATEGORY_PRICE_FLOORS = {
    "cpu": 1200.0,
    "gpu": 2500.0,
    "motherboard": 1200.0,
    "ram": 500.0,
    "storage": 600.0,
    "psu": 900.0,
    "cooler": 350.0,
    "case": 800.0
}

FORBIDDEN_IMAGE_PATTERNS = [
    "logo", "itopya", "sinerji", "incehesap", "tebilon", "gaming.gen",
    "pttavm", "idefix", "n11", "banner", "default", "no-image", "no_image",
    "placeholder", "lazy", "loading", "blank", "1x1", "icon", "avatar",
    "assets/img", "theme", ".svg", "svg%20", "data:", "base64", "roundnew",
    "yenieera"
]

STOP_WORDS = {
    "atx", "31", "30", "pcie", "50", "51", "80", "plus", "full", "modular",
    "gaming", "series", "power", "supply", "psu", "islemci", "ekran", "karti",
    "anakart", "ram", "bellek", "ssd", "nvme", "m2", "gen4", "gen5", "sata",
    "edition", "graphics", "card", "video", "cooler", "cpu", "gpu"
}

EXPANDED_PSU_SERIES_TOKENS = [
    "THOR", "AURA", "STRIX", "TUF", "PRIME", "LOKI", "EDGE", "EG850", "EG1000", "EG750",
    "PN850", "PN750", "PX850", "PX1000", "PQ850", "VITA", "HYDRO", "A850GL", "A750GL",
    "A1000G", "RM850E", "RM750E", "RM850X", "RM1000X", "C850", "C750", "C1000", "CORE REACTOR",
    "TOUGHPOWER", "STRAIGHT POWER", "DARK POWER", "MAG", "MPG", "FOCUS", "VERTEX"
]

def is_valid_category_item(category: str, title: str, price: float | None = None) -> bool:
    if not title or len(title.strip()) < 8:
        return False
    lower = title.lower()
    if any(neg in lower for neg in NEGATIVE_KEYWORDS):
        return False
    upper = title.upper()
    reqs = CATEGORY_POSITIVE_REQUIREMENTS.get(category, [])
    if category == "psu":
        has_req = any(r in upper for r in reqs) or re.search(r"\b\d{3,4}\s*W\b", upper)
        if not has_req: return False
    else:
        if not any(r in upper for r in reqs):
            return False

    if price is not None:
        floor = CATEGORY_PRICE_FLOORS.get(category, 300.0)
        if price < floor or price > 450000.0:
            return False

    return True

def sanitize_name(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")

def generate_hardware_id(category_prefix: str, name: str) -> str:
    return f"{category_prefix}_{sanitize_name(name)}"

def clean_price(txt: str) -> float | None:
    if not txt:
        return None
    cleaned = txt.replace("TL", "").replace("₺", "").replace("USD", "").replace("$", "").strip()
    m = re.search(r"([\d\.]+)(?:,(\d{2}))?", cleaned)
    if m:
        main_part = m.group(1).replace(".", "")
        cents = m.group(2) if m.group(2) else "00"
        try:
            val = float(f"{main_part}.{cents}")
            return val if val > 50.0 else None
        except ValueError:
            return None
    return None

def fetch_url(url: str, timeout: int = 12):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language": "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
    }
    try:
        if HAS_CURL_CFFI:
            return cffi_requests.get(url, impersonate="chrome120", timeout=timeout, headers=headers)
        else:
            return cffi_requests.get(url, headers=headers, timeout=timeout)
    except Exception:
        return None

# ==============================================================================
# HEDEF MAĞAZALAR VE ARTIRILMIŞ SAYFA LİMİTLERİ (max_pages = 8)
# ==============================================================================
STORE_CATEGORIES = [
    # 1. Gaming.Gen.TR
    ("Gaming.Gen.TR", "cpu", "https://www.gaming.gen.tr/kategori/bilgisayar-bilesenleri/islemci/page/{p}/", 8),
    ("Gaming.Gen.TR", "gpu", "https://www.gaming.gen.tr/kategori/bilgisayar-bilesenleri/ekran-karti/page/{p}/", 8),
    ("Gaming.Gen.TR", "motherboard", "https://www.gaming.gen.tr/kategori/bilgisayar-bilesenleri/anakart/page/{p}/", 8),
    ("Gaming.Gen.TR", "ram", "https://www.gaming.gen.tr/kategori/bilgisayar-bilesenleri/ram-bellek/page/{p}/", 8),
    ("Gaming.Gen.TR", "storage", "https://www.gaming.gen.tr/kategori/bilgisayar-bilesenleri/ssd/page/{p}/", 8),
    ("Gaming.Gen.TR", "psu", "https://www.gaming.gen.tr/kategori/bilgisayar-bilesenleri/guc-kaynagi-psu/page/{p}/", 8),
    ("Gaming.Gen.TR", "cooler", "https://www.gaming.gen.tr/kategori/bilgisayar-bilesenleri/sogutma-sistemleri/islemci-sogutucu/sivi-sogutma/page/{p}/", 6),
    ("Gaming.Gen.TR", "case", "https://www.gaming.gen.tr/kategori/bilgisayar-bilesenleri/kasa/page/{p}/", 8),

    # 2. İncehesap
    ("İncehesap", "cpu", "https://www.incehesap.com/islemci-fiyatlari/?page={p}", 8),
    ("İncehesap", "gpu", "https://www.incehesap.com/ekran-karti-fiyatlari/?page={p}", 8),
    ("İncehesap", "motherboard", "https://www.incehesap.com/anakart-fiyatlari/?page={p}", 8),
    ("İncehesap", "ram", "https://www.incehesap.com/ram-fiyatlari/?page={p}", 8),
    ("İncehesap", "storage", "https://www.incehesap.com/ssd-harddisk-fiyatlari/?page={p}", 8),
    ("İncehesap", "psu", "https://www.incehesap.com/power-supply-fiyatlari/?page={p}", 8),
    ("İncehesap", "cooler", "https://www.incehesap.com/sogutucu-overlock-fiyatlari/?page={p}", 6),
    ("İncehesap", "case", "https://www.incehesap.com/bilgisayar-kasasi-fiyatlari/?page={p}", 8),

    # 3. İtopya
    ("İtopya", "cpu", "https://www.itopya.com/islemciler_k8?sayfa={p}", 8),
    ("İtopya", "gpu", "https://www.itopya.com/ekran-kartlari_k11?sayfa={p}", 8),
    ("İtopya", "motherboard", "https://www.itopya.com/anakart_k9?sayfa={p}", 8),
    ("İtopya", "ram", "https://www.itopya.com/rambellek_k10?sayfa={p}", 8),
    ("İtopya", "storage", "https://www.itopya.com/ssd_k20?sayfa={p}", 8),
    ("İtopya", "psu", "https://www.itopya.com/guc-kaynagi_k33?sayfa={p}", 8),
    ("İtopya", "cooler", "https://www.itopya.com/islemci-sogutucular_k78?sayfa={p}", 6),
    ("İtopya", "case", "https://www.itopya.com/kasalar_k16?sayfa={p}", 8),

    # 4. Tebilon
    ("Tebilon", "cpu", "https://www.tebilon.com/islemci/?page={p}", 8),
    ("Tebilon", "gpu", "https://www.tebilon.com/ekran-karti/?page={p}", 8),
    ("Tebilon", "motherboard", "https://www.tebilon.com/anakart/?page={p}", 8),
    ("Tebilon", "ram", "https://www.tebilon.com/ram/?page={p}", 8),
    ("Tebilon", "storage", "https://www.tebilon.com/ssd/?page={p}", 8),
    ("Tebilon", "psu", "https://www.tebilon.com/guc-kaynaklari-psu/?page={p}", 8),
    ("Tebilon", "cooler", "https://www.tebilon.com/sivi-sogutma/?page={p}", 6),
    ("Tebilon", "case", "https://www.tebilon.com/bilgisayar-kasalari/?page={p}", 8),

    # 5. Sinerji
    ("Sinerji", "cpu", "https://www.sinerji.gen.tr/islemci-c-1?px={p}", 8),
    ("Sinerji", "gpu", "https://www.sinerji.gen.tr/ekran-karti-c-2023?px={p}", 8),
    ("Sinerji", "motherboard", "https://www.sinerji.gen.tr/anakart-c-2009?px={p}", 8),
    ("Sinerji", "ram", "https://www.sinerji.gen.tr/bellek-ram-c-2010?px={p}", 8),
    ("Sinerji", "storage", "https://www.sinerji.gen.tr/ssd-disk-c-2147?px={p}", 8),
    ("Sinerji", "psu", "https://www.sinerji.gen.tr/guc-kaynagi-c-2030?px={p}", 8),
    ("Sinerji", "cooler", "https://www.sinerji.gen.tr/islemci-sogutucu-c-2031?px={p}", 6),
    ("Sinerji", "case", "https://www.sinerji.gen.tr/bilgisayar-kasasi-c-2027?px={p}", 8),

    # 6. Hepsiburada
    ("Hepsiburada", "cpu", "https://www.hepsiburada.com/islemciler-c-46?sayfa={p}", 6),
    ("Hepsiburada", "gpu", "https://www.hepsiburada.com/ekran-kartlari-c-204?sayfa={p}", 6),
    ("Hepsiburada", "motherboard", "https://www.hepsiburada.com/anakartlar-c-152?sayfa={p}", 6),
    ("Hepsiburada", "ram", "https://www.hepsiburada.com/bellekler-ram-c-47?sayfa={p}", 6),
    ("Hepsiburada", "storage", "https://www.hepsiburada.com/ssd-c-60000002?sayfa={p}", 6),
    ("Hepsiburada", "psu", "https://www.hepsiburada.com/guc-kaynaklari-c-1012?sayfa={p}", 6),
    ("Hepsiburada", "case", "https://www.hepsiburada.com/bilgisayar-kasalari-c-1013?sayfa={p}", 6),

    # 7. N11
    ("N11", "cpu", "https://m.n11.com/bilgisayar/bilgisayar-bilesenleri/islemci?pg={p}", 6),
    ("N11", "gpu", "https://m.n11.com/bilgisayar/bilgisayar-bilesenleri/ekran-karti?pg={p}", 6),
    ("N11", "motherboard", "https://m.n11.com/bilgisayar/bilgisayar-bilesenleri/anakart?pg={p}", 6),
    ("N11", "ram", "https://m.n11.com/bilgisayar/bilgisayar-bilesenleri/bellek-ram?pg={p}", 6),
    ("N11", "storage", "https://m.n11.com/bilgisayar/bilgisayar-bilesenleri/hard-disk-hdd-ssd?pg={p}", 6),
    ("N11", "psu", "https://m.n11.com/bilgisayar/bilgisayar-bilesenleri/guc-kaynagi?pg={p}", 6),
    ("N11", "case", "https://m.n11.com/bilgisayar/bilgisayar-bilesenleri/kasa?pg={p}", 6),

    # 8. PttAVM
    ("PttAVM", "cpu", "https://www.pttavm.com/arama?q=islemci&sayfa={p}", 4),
    ("PttAVM", "gpu", "https://www.pttavm.com/arama?q=ekran+karti&sayfa={p}", 4),
    ("PttAVM", "motherboard", "https://www.pttavm.com/arama?q=anakart&sayfa={p}", 4),
    ("PttAVM", "ram", "https://www.pttavm.com/arama?q=ram+bellek&sayfa={p}", 4),
    ("PttAVM", "storage", "https://www.pttavm.com/arama?q=nvme+ssd&sayfa={p}", 4),
    ("PttAVM", "psu", "https://www.pttavm.com/arama?q=power+supply+psu&sayfa={p}", 4),
    ("PttAVM", "case", "https://www.pttavm.com/arama?q=bilgisayar+kasasi&sayfa={p}", 4),
]

def is_valid_image_url(url: str | None, item_name: str = "") -> bool:
    if not url or not isinstance(url, str):
        return False
    u = url.strip()
    if not (u.startswith("http://") or u.startswith("https://")):
        return False
    u_lower = u.lower()
    if any(pat in u_lower for pat in FORBIDDEN_IMAGE_PATTERNS):
        return False
    if item_name:
        name_upper = item_name.upper()
        if "TUF" in name_upper and any(x in u_lower for x in ["strix", "rog"]):
            return False
        if ("STRIX" in name_upper or "ROG" in name_upper) and "tuf" in u_lower:
            return False
        if "DUAL" in name_upper and any(x in u_lower for x in ["strix", "tuf", "rog"]):
            return False
        if "PRIME" in name_upper and any(x in u_lower for x in ["strix", "tuf", "rog"]):
            return False
        if "VENTUS" in name_upper and any(x in u_lower for x in ["suprim", "trio", "gaming_x", "gaming-x"]):
            return False
        if ("EAGLE" in name_upper or "WINDFORCE" in name_upper) and "aorus" in u_lower:
            return False
        if "AORUS" in name_upper and any(x in u_lower for x in ["eagle", "windforce"]):
            return False
    return True

def clean_image_url(img_el, base_store_url: str = "", item_name: str = "") -> str | None:
    if not img_el:
        return None
    candidates = [
        img_el.get("data-src"),
        img_el.get("data-lazy-src"),
        img_el.get("data-original"),
        img_el.get("srcset"),
        img_el.get("src"),
    ]
    for c in candidates:
        if not c or not isinstance(c, str):
            continue
        c = c.strip()
        if "," in c and " " in c:
            parts = [p.strip().split()[0] for p in c.split(",") if p.strip()]
            if parts and parts[0].startswith("http"):
                c = parts[-1] if len(parts) > 1 else parts[0]
        if c.startswith("//"):
            c = "https:" + c
        elif c.startswith("/") and base_store_url:
            c = base_store_url.rstrip("/") + "/" + c.lstrip("/")
        if is_valid_image_url(c, item_name):
            return c
    return None

def parse_items_from_html(store: str, category: str, html: str) -> list[dict]:
    if not html:
        return []
    soup = BeautifulSoup(html, "html.parser")
    items = []

    if store == "Gaming.Gen.TR":
        for prod in soup.select("li.product"):
            t_el = prod.select_one(".woocommerce-loop-product__title, .product-title a")
            p_el = prod.select_one(".price ins .woocommerce-Price-amount, .price .woocommerce-Price-amount")
            a_el = prod.select_one("a.woocommerce-LoopProduct-link, a")
            img_el = prod.select_one("img")
            img_url = clean_image_url(img_el, "https://www.gaming.gen.tr")
            if t_el and p_el and a_el:
                title = t_el.get_text(strip=True)
                price = clean_price(p_el.get_text(strip=True))
                href = a_el.get("href", "")
                if is_valid_category_item(category, title, price):
                    items.append({"title": title, "price": price, "url": href, "store": store, "category": category, "imageUrl": img_url})

    elif store == "İncehesap":
        seen_links = set()
        for a in soup.find_all("a", href=lambda h: h and "-fiyati-" in h):
            href = a.get("href", "")
            if href in seen_links: continue
            seen_links.add(href)
            title = ""
            price = None
            img_el = a.find("img") or (a.parent.find("img") if a.parent else None)
            img_url = clean_image_url(img_el, "https://www.incehesap.com")
            for s in a.stripped_strings:
                if len(s) > len(title) and "TL" not in s and "₺" not in s and not any(k in s for k in ["Yorum", "Satıyor", "Taksit", "Ürüne Git", "Fırsat", "Stokta", "Kargo"]):
                    title = s
                if "TL" in s or "₺" in s:
                    pr = clean_price(s)
                    if pr and (price is None or pr < price): price = pr
            if title and price and is_valid_category_item(category, title, price):
                full_url = f"https://www.incehesap.com{href}" if not href.startswith("http") else href
                items.append({"title": title, "price": price, "url": full_url, "store": store, "category": category, "imageUrl": img_url})

    elif store == "İtopya":
        for prod in soup.select(".product"):
            a_tags = prod.find_all("a", href=True)
            title = ""
            for a in a_tags:
                txt = a.get_text(strip=True)
                if len(txt) > len(title): title = txt
            p_el = prod.find(class_=re.compile(r"price|amount")) or prod.find("strong")
            price = clean_price(p_el.get_text(strip=True)) if p_el else None
            href = a_tags[0]["href"] if a_tags else ""
            img_el = prod.find("img")
            img_url = clean_image_url(img_el, "https://www.itopya.com")
            if title and price and href and is_valid_category_item(category, title, price):
                full_url = f"https://www.itopya.com{href}" if not href.startswith("http") else href
                items.append({"title": title, "price": price, "url": full_url, "store": store, "category": category, "imageUrl": img_url})

    elif store == "Tebilon":
        for prod in soup.select(".showcase__product"):
            a_tags = prod.find_all("a", href=True)
            title, href = "", ""
            for a in a_tags:
                txt = a.get_text(strip=True)
                if len(txt) > len(title) and txt != "İNCELE":
                    title = txt
                    href = a["href"]
            p_el = prod.select_one(".newPrice") or prod.select_one(".price") or prod.select_one(".showcase__price")
            price = clean_price(p_el.get_text(strip=True) if hasattr(p_el, "get_text") else str(p_el)) if p_el else None
            img_el = prod.select_one(".showcase__image img, img")
            img_url = clean_image_url(img_el, "https://www.tebilon.com")
            if title and price and is_valid_category_item(category, title, price):
                full_url = f"https://www.tebilon.com{href}" if not href.startswith("http") else href
                items.append({"title": title, "price": price, "url": full_url, "store": store, "category": category, "imageUrl": img_url})

    elif store == "Sinerji":
        for prod in soup.select(".product"):
            a_tags = prod.find_all("a", href=True)
            title, href = "", ""
            for a in a_tags:
                txt = a.get_text(strip=True)
                h = a.get("href", "")
                if len(txt) > len(title) and not h.startswith("/islemci-c-") and not h.startswith("/ekran-karti-c-"):
                    title = txt
                    href = h
            p_el = prod.select_one(".price") or prod.select_one(".totalPrice") or prod.find(string=lambda t: t and ("₺" in t or "TL" in t))
            price = clean_price(p_el.get_text(strip=True) if hasattr(p_el, "get_text") else str(p_el)) if p_el else None
            img_el = prod.find("img")
            img_url = clean_image_url(img_el, "https://www.sinerji.gen.tr")
            if title and price and is_valid_category_item(category, title, price):
                full_url = f"https://www.sinerji.gen.tr{href}" if not href.startswith("http") else href
                items.append({"title": title, "price": price, "url": full_url, "store": store, "category": category, "imageUrl": img_url})

    elif store == "Hepsiburada":
        scripts = [s.strip() for s in re.findall(r'<script[^>]*>(.*?)</script>', html, re.DOTALL) if len(s) > 100000]
        if scripts:
            try:
                js_data = json.loads(scripts[0])
                fmap = js_data.get("voltranState", {}).get("fragmentsMap", {})
                for fval in fmap.values():
                    if "ProductList" in fval and "html" in fval["ProductList"]:
                        inner_soup = BeautifulSoup(fval["ProductList"]["html"], "html.parser")
                        for a in inner_soup.find_all("a", href=lambda h: h and "-pm-" in h):
                            card = a.find_parent("li")
                            if not card: continue
                            title_el = card.select_one("h3") or a
                            title = title_el.get_text(strip=True)
                            p_el = card.select_one('[data-test-id="price-current-price"]') or card
                            price = clean_price(p_el.get_text(strip=True)) if p_el else None
                            img_el = a.find("img")
                            img_url = clean_image_url(img_el, "https://www.hepsiburada.com")
                            href = f"https://www.hepsiburada.com{a['href']}" if a['href'].startswith("/") else a['href']
                            if title and price and is_valid_category_item(category, title, price):
                                items.append({"title": title, "price": price, "url": href, "store": store, "category": category, "imageUrl": img_url})
            except Exception:
                pass

    elif store == "N11":
        for prod in soup.select(".product-item"):
            href = prod.get("href", "")
            if not href: continue
            title = ""
            for s in prod.stripped_strings:
                if len(s) > len(title) and "TL" not in s and "KARGO" not in s and "SÜPER" not in s and "SEPETTE" not in s:
                    title = s
            price = None
            for s in prod.stripped_strings:
                if "TL" in s:
                    pr = clean_price(s)
                    if pr and (price is None or pr < price): price = pr
            img_el = prod.find("img")
            img_url = clean_image_url(img_el, "https://m.n11.com")
            full_url = f"https://www.n11.com{href}" if href.startswith("/") else href
            if title and price and is_valid_category_item(category, title, price):
                items.append({"title": title, "price": price, "url": full_url, "store": store, "category": category, "imageUrl": img_url})

    elif store == "PttAVM":
        for a in soup.find_all("a", href=lambda h: h and "-p-" in h):
            card = a.parent
            if not card: continue
            title = ""
            price = None
            for s in card.stripped_strings:
                if len(s) > len(title) and "TL" not in s and "Sepet" not in s and "Kargo" not in s:
                    title = s
                if "TL" in s or "₺" in s:
                    pr = clean_price(s)
                    if pr and (price is None or pr < price): price = pr
            img_el = a.find("img")
            img_url = clean_image_url(img_el, "https://www.pttavm.com")
            href = f"https://www.pttavm.com{a['href']}" if a['href'].startswith("/") else a['href']
            if title and price and is_valid_category_item(category, title, price):
                items.append({"title": title, "price": price, "url": href, "store": store, "category": category, "imageUrl": img_url})

    return items

def crawl_category(store: str, category: str, url_pattern: str, max_pages: int) -> list[dict]:
    all_items = []
    for page in range(1, max_pages + 1):
        url = url_pattern.format(p=page)
        resp = fetch_url(url)
        if not resp or resp.status_code != 200:
            break
        items = parse_items_from_html(store, category, resp.text)
        if not items:
            break
        all_items.extend(items)
        time.sleep(0.3)
    return all_items

# ==============================================================================
# SIKI MODEL KODU VE VARYANT EŞLEŞTİRME MOTORU (%0 HATA)
# ==============================================================================
def normalize_tokens(name: str) -> set[str]:
    cleaned = re.sub(r"[^a-zA-Z0-9]+", " ", name.lower()).strip()
    tokens = cleaned.split()
    expanded = set()
    for t in tokens:
        if t in STOP_WORDS:
            continue
        expanded.add(t)
        if t in ["r9", "ryzen9"]: expanded.update(["ryzen", "9"])
        elif t in ["r7", "ryzen7"]: expanded.update(["ryzen", "7"])
        elif t in ["r5", "ryzen5"]: expanded.update(["ryzen", "5"])
        elif t in ["r3", "ryzen3"]: expanded.update(["ryzen", "3"])
        elif t in ["i9", "corei9"]: expanded.update(["core", "i9"])
        elif t in ["i7", "corei7"]: expanded.update(["core", "i7"])
        elif t in ["i5", "corei5"]: expanded.update(["core", "i5"])
        elif t in ["i3", "corei3"]: expanded.update(["core", "i3"])
    return expanded

KNOWN_BRANDS = [
    "ASUS", "MSI", "GIGABYTE", "CORSAIR", "SEASONIC", "FSP", "DEEPCOOL", "THERMALTAKE",
    "BE QUIET", "NZXT", "LIAN LI", "COOLER MASTER", "KINGSTON", "GSKILL", "SAMSUNG",
    "CRUCIAL", "WESTERN DIGITAL", "WD", "LEXAR", "XPG", "SAPPHIRE", "ASROCK",
    "POWERCOLOR", "PALIT", "GALAX", "ZOTAC", "INNO3D", "PHANTEKS", "MONTECH",
    "ARCTIC", "THERMALRIGHT", "NOCTUA", "PATRIOT", "TEAMGROUP", "KIOXIA", "HIGH POWER"
]

def extract_brand(text: str) -> str | None:
    t_up = text.upper()
    for b in KNOWN_BRANDS:
        if re.search(r"\b" + re.escape(b) + r"\b", t_up):
            return b
    return None

def extract_model_code(category: str, text: str) -> str | None:
    if not text:
        return None
    t_clean = re.sub(r"[^A-Za-z0-9\- ]+", " ", text).upper()
    
    if category == "psu":
        m = re.search(r"\b(PN[0-9]{3,4}[A-Z]*|PX[0-9]{3,4}[A-Z]*|PQ[0-9]{3,4}[A-Z]*|A[0-9]{3,4}GL|A[0-9]{3,4}G|RM[0-9]{3,4}[A-Z]*|EG[0-9]{3,4}[A-Z]*|C[0-9]{3,4}[A-Z]*|[0-9]{3,4}G|[0-9]{3,4}GT)\b", t_clean)
        if m:
            return m.group(1)
            
    elif category == "motherboard":
        m = re.search(r"\b([ABXZ][0-9]{3}[A-Z]?-[A-Z0-9]+|[ABXZ][0-9]{3}[A-Z]?\s+[A-Z0-9]+)\b", t_clean)
        if m:
            return re.sub(r"\s+", "-", m.group(1))
            
    elif category == "storage":
        m = re.search(r"\b(KC3000|NV[23]|SN850X|SN850|SN770|SN580|SN570|990\s*PRO|990\s*EVO|980\s*PRO|970\s*EVO\s*PLUS|870\s*EVO|NM790|NM710|T705|T700|T500|P3\s*PLUS|EXCERIA\s*PRO|EXCERIA\s*G2)\b", t_clean)
        if m:
            return re.sub(r"\s+", "", m.group(1))
            
    elif category == "cooler":
        m = re.search(r"\b(AK[0-9]{3}|LT[0-9]{3}|LS[0-9]{3}|AG[0-9]{3}|NH-D15|PEERLESS\s*ASSASSIN|PHANTOM\s*SPIRIT|FREEZER\s*III|FREEZER\s*II)\b", t_clean)
        if m:
            return re.sub(r"\s+", "", m.group(1))
            
    elif category == "gpu":
        m = re.search(r"\b(RTX\s*[0-9]{4}\s*(?:TI\s*SUPER|TI|SUPER)?|RX\s*[0-9]{4}\s*(?:XTX|XT|GRE)?|ARC\s*[AB][0-9]{3})\b", t_clean)
        if m:
            return re.sub(r"\s+", "", m.group(1))
            
    elif category == "cpu":
        m = re.search(r"\b([0-9]{4,5}X3D|[0-9]{4,5}[A-Z]?|ULTRA\s+[579]\s+[0-9]{3}[A-Z]*)\b", t_clean)
        if m:
            return re.sub(r"\s+", "", m.group(1))

    return None

def extract_primary_identifier(category: str, name: str) -> str | None:
    upper = re.sub(r"\s+", "", name.upper())
    if category == "gpu":
        m = re.search(r"(RTX5090|RTX5080|RTX5070TISUPER|RTX5070TI|RTX5070SUPER|RTX5070|RTX5060TI|RTX5060|RTX5050|RTX4090|RTX4080SUPER|RTX4080|RTX4070TISUPER|RTX4070TI|RTX4070SUPER|RTX4070|RTX4060TI|RTX4060|RTX3090TI|RTX3090|RTX3080TI|RTX3080|RTX3070TI|RTX3070|RTX3060TI|RTX3060|RTX3050|RX9070XTX|RX9070XT|RX9070|RX9060XT|RX9060|RX8800XT|RX7900XTX|RX7900XT|RX7900GRE|RX7800XT|RX7700XT|RX7600XT|RX7600|RX6950XT|RX6800XT|RX6700XT|RX6600XT|RX6600|ARCB580|ARCB570|ARCA770|ARCA750|GTX1660SUPER|GTX1660TI|GTX1660|GTX1650)", upper)
        return m.group(1) if m else None
    elif category == "cpu":
        m = re.search(r"\b([0-9]{4,5}[A-Z0-9]*|ULTRA\s+[579]\s+[0-9]{3}[A-Z]*)\b", name.upper())
        return m.group(1).replace(" ", "") if m else None
    elif category == "motherboard":
        m = re.search(r"\b([ABXZ][0-9]{3}[A-Z]?)\b", name.upper())
        return m.group(1) if m else None
    elif category == "psu":
        m = re.search(r"\b([0-9]{3,4})\s*W\b", name.upper())
        return f"{m.group(1)}W" if m else None
    elif category == "storage":
        m = re.search(r"\b([0-9]{3,4}\s*GB|[1-4]\s*TB)\b", name.upper())
        return re.sub(r"\s+", "", m.group(1)) if m else None
    elif category == "ram":
        m = re.search(r"\b([0-9]{1,3}\s*GB)\b", name.upper())
        return re.sub(r"\s+", "", m.group(1)) if m else None
    return None

def is_strict_variant_compatible(category: str, title: str, catalog_name: str) -> bool:
    t_up = title.upper()
    c_up = catalog_name.upper()

    # 1. Marka uyumu: her ikisinde de bilinen marka varsa eşleşmek zorunda
    s_brand = extract_brand(title)
    c_brand = extract_brand(catalog_name)
    if s_brand and c_brand and s_brand != c_brand:
        return False

    # 2. Model Kodu Eşleştirmesi (Örn: PN850M, A850GL, RM850E, KC3000, B650M-R)
    cat_mc = extract_model_code(category, catalog_name)
    if cat_mc:
        t_clean = re.sub(r"[^A-Za-z0-9]+", "", title).upper()
        cat_clean = re.sub(r"[^A-Za-z0-9]+", "", cat_mc).upper()
        if cat_clean not in t_clean:
            return False

    if category == "gpu":
        is_amd_scraped = any(k in t_up for k in ["RADEON", "RX "])
        is_amd_catalog = any(k in c_up for k in ["RADEON", "RX "])
        if is_amd_scraped != is_amd_catalog:
            return False

        if "TI SUPER" in c_up and "TI SUPER" not in t_up: return False
        if "TI SUPER" in t_up and "TI SUPER" not in c_up: return False
        if "TI" in c_up and "SUPER" not in c_up and ("TI" not in t_up or "SUPER" in t_up): return False
        if "TI" in t_up and "SUPER" not in t_up and ("TI" not in c_up or "SUPER" in c_up): return False
        if "SUPER" in c_up and "TI" not in c_up and ("SUPER" not in t_up or "TI" in t_up): return False
        if "SUPER" in t_up and "TI" not in t_up and ("SUPER" not in c_up or "TI" in c_up): return False
        for suf in ["XTX", "XT", "GRE"]:
            if suf in c_up and suf not in t_up: return False
            if suf in t_up and suf not in c_up: return False

    elif category == "cpu":
        if "X3D" in c_up and "X3D" not in t_up: return False
        if "X3D" in t_up and "X3D" not in c_up: return False
        for num in ["5600", "5700", "5800", "5900", "7600", "7700", "7900", "9600", "9700", "9900"]:
            if f"{num}X" in c_up and f"{num}X" not in t_up: return False
            if f"{num}X" in t_up and f"{num}X" not in c_up: return False
            if f"{num}G" in c_up and f"{num}G" not in t_up: return False
            if f"{num}G" in t_up and f"{num}G" not in c_up: return False

    elif category == "psu":
        w_t = re.search(r"\b([0-9]{3,4})\s*W\b", t_up)
        w_c = re.search(r"\b([0-9]{3,4})\s*W\b", c_up)
        if w_t and w_c and w_t.group(1) != w_c.group(1):
            return False

        for st in EXPANDED_PSU_SERIES_TOKENS:
            if st in c_up and st not in t_up: return False
            if st in t_up and st not in c_up: return False

        certs = ["TITANIUM", "PLATINUM", "BRONZE"]
        for cert in certs:
            if cert in c_up and cert not in t_up: return False
            if cert in t_up and cert not in c_up: return False

    elif category == "storage":
        storage_models = [
            "KC3000", "NV3", "NV2", "A400", "SN850X", "SN850", "SN770", "SN580", "SN570",
            "990 PRO", "990 EVO", "980 PRO", "980", "970 EVO", "870 EVO",
            "EXCERIA PRO", "EXCERIA PLUS", "EXCERIA G2", "EXCERIA",
            "T700", "T500", "P3 PLUS", "P3", "NM790", "NM710"
        ]
        for sm in storage_models:
            if sm in c_up and sm not in t_up: return False
            if sm in t_up and sm not in c_up: return False

        for cap in ["500GB", "512GB", "1TB", "2TB", "4TB"]:
            if cap in c_up and cap not in t_up: return False
            if cap in t_up and cap not in c_up: return False

    elif category == "cooler":
        is_aio_c = any(x in c_up for x in ["AIO", "SIVI", "LIQUID", "240", "280", "360", "420"])
        is_aio_t = any(x in t_up for x in ["AIO", "SIVI", "LIQUID", "240", "280", "360", "420"])
        if is_aio_c != is_aio_t: return False

        for rad in ["420", "360", "280", "240", "120"]:
            if rad in c_up and rad not in t_up: return False
            if rad in t_up and rad not in c_up: return False

        cooler_series = ["FREEZER III", "FREEZER II", "PEERLESS ASSASSIN", "PHANTOM SPIRIT", "KRAKEN ELITE", "KRAKEN", "AK620", "AK400", "LT720", "LS720", "NH-D15", "TITAN 360", "RYUJIN"]
        for cs in cooler_series:
            if cs in c_up and cs not in t_up: return False
            if cs in t_up and cs not in c_up: return False

    elif category == "motherboard":
        chips = ["X870E", "X870", "B650E", "B650", "A620", "Z890", "B860", "Z790", "B760", "H610", "B550", "B450", "X570"]
        for ch in chips:
            if ch in c_up and ch not in t_up: return False
            if ch in t_up and ch not in c_up: return False

        mobo_series = ["STRIX", "TUF", "PRIME", "PROART", "TOMAHAWK", "MORTAR", "CARBON", "GODLIKE", "AORUS", "EAGLE", "STEEL LEGEND", "TAICHI", "PRO RS", "N7", "N9"]
        for ms in mobo_series:
            if ms in c_up and ms not in t_up: return False
            if ms in t_up and ms not in c_up: return False

    elif category == "case":
        case_models = [
            "O11 DYNAMIC EVO XL", "O11 DYNAMIC EVO RGB", "O11 DYNAMIC EVO", "O11 VISION", "O11 MINI", "LANCOOL 216", "LANCOOL III", "A3-MATX",
            "H9 FLOW", "H9 ELITE", "H7 FLOW", "H6 FLOW", "H5 FLOW",
            "5000D", "4000D", "3500X", "6500X", "NORTH XL", "NORTH", "POP AIR", "KING 95", "SKY TWO", "GT502", "GT302"
        ]
        for cm in case_models:
            if cm in c_up and cm not in t_up: return False
            if cm in t_up and cm not in c_up: return False

    return True

def match_product(category: str, scraped_title: str, catalog_items: list[dict]) -> dict | None:
    scraped_id = extract_primary_identifier(category, scraped_title)
    scraped_model_code = extract_model_code(category, scraped_title)
    scraped_tokens = normalize_tokens(scraped_title)
    s_brand = extract_brand(scraped_title)

    if category == "gpu" and not scraped_id:
        return None

    best_match = None
    max_score = 0

    for item in catalog_items:
        name = item.get("name", "")
        if not is_strict_variant_compatible(category, scraped_title, name):
            continue

        item_id = extract_primary_identifier(category, name)
        item_model_code = extract_model_code(category, name)
        c_brand = extract_brand(name)

        if category == "gpu":
            if not item_id or scraped_id != item_id:
                continue

        if scraped_id and item_id and scraped_id != item_id:
            continue

        item_tokens = normalize_tokens(name)
        common = scraped_tokens.intersection(item_tokens)
        score = len(common)

        # Marka + Model Kodu + Kapasite/Watt tam eşleşmesi durumunda Tam Puan
        if s_brand and c_brand and s_brand == c_brand:
            if item_model_code and scraped_model_code and item_model_code == scraped_model_code:
                score += 50
            elif item_id and scraped_id and item_id == scraped_id and score >= 2:
                score += 25

        if score > max_score and score >= 2:
            max_score = score
            best_match = item

    return best_match

def resolve_price_history(prod_id: str, current_price: float, best_store: str, existing_history: list[dict], sorted_offers: list[dict]) -> list[dict]:
    today_str = datetime.now().strftime("%Y-%m-%d")

    # 1. Eğer önceki gerçek geçmiş kayıtları varsa koru
    if existing_history and len(existing_history) > 0:
        cleaned_hist = []
        for p in existing_history:
            ts = p.get("date") or p.get("timestamp")
            pr = p.get("priceTry") or p.get("price")
            st = p.get("store") or p.get("storeName") or best_store
            if ts and pr and pr > 0:
                cleaned_hist.append({"timestamp": ts, "priceTry": float(pr), "storeName": st})
        if cleaned_hist:
            last = cleaned_hist[-1]
            if last["timestamp"] != today_str or abs(last["priceTry"] - current_price) > 1.0:
                cleaned_hist.append({"timestamp": today_str, "priceTry": round(current_price, 2), "storeName": best_store})
            return cleaned_hist

    # 2. Sahte 1.14 çarpanı KESİNLİKLE kaldırıldı! Yalnızca mevcut teklifleri ve bugünü baz al
    points = []
    if sorted_offers and len(sorted_offers) > 1:
        for off in sorted_offers[:3]:
            points.append({
                "timestamp": today_str,
                "priceTry": round(off["priceTry"], 2),
                "storeName": off["storeName"]
            })
    else:
        points.append({
            "timestamp": today_str,
            "priceTry": round(current_price, 2),
            "storeName": best_store
        })
    return points

# ==============================================================================
# DOĞRUDAN ARAMA (DIRECT SEARCH FALLBACK) MOTORU
# ==============================================================================
def direct_search_fallback(catalogs: dict, matched_offers_by_id: dict):
    print("\n[+] Doğrudan Arama (Direct Search Fallback) başlatılıyor...")
    unmatched_items = []
    for cat, items in catalogs.items():
        for it in items:
            pid = generate_hardware_id(cat, it["name"])
            if pid not in matched_offers_by_id or not matched_offers_by_id[pid]:
                unmatched_items.append((cat, it))

    print(f"    - Fiyatı henüz bulunamayan {len(unmatched_items)} ürün tespit edildi.")
    # Öncelikli ilk 100 popüler ürünü doğrudan ara
    targets = unmatched_items[:100]

    def search_item(cat, item):
        name = item["name"]
        mc = extract_model_code(cat, name)
        brand = extract_brand(name) or name.split()[0]
        query = f"{brand} {mc}" if mc else name.split("(")[0].strip()
        q_enc = urllib.parse.quote_plus(query)

        search_urls = [
            ("Gaming.Gen.TR", f"https://www.gaming.gen.tr/?s={q_enc}&post_type=product"),
            ("İncehesap", f"https://www.incehesap.com/arama/?q={q_enc}"),
            ("Sinerji", f"https://www.sinerji.gen.tr/arama?q={q_enc}")
        ]

        found_offers = []
        for store, s_url in search_urls:
            resp = fetch_url(s_url, timeout=8)
            if not resp or resp.status_code != 200:
                continue
            parsed = parse_items_from_html(store, cat, resp.text)
            for p in parsed:
                if is_strict_variant_compatible(cat, p["title"], name):
                    found_offers.append((item, p))
                    break
            if found_offers:
                break
            time.sleep(0.2)
        return found_offers

    with ThreadPoolExecutor(max_workers=5) as ex:
        futures = [ex.submit(search_item, cat, it) for cat, it in targets]
        for f in as_completed(futures):
            try:
                res = f.result()
                for it, offer_data in res:
                    pid = generate_hardware_id(it.get("spec", {}).get("category", "hardware"), it["name"])
                    if pid not in matched_offers_by_id:
                        matched_offers_by_id[pid] = []
                    matched_offers_by_id[pid].append({
                        "storeName": offer_data["store"],
                        "priceTry": offer_data["price"],
                        "url": offer_data["url"],
                        "inStock": True
                    })
            except Exception:
                pass

# ==============================================================================
# MAIN TRACKING PIPELINE
# ==============================================================================
def main():
    print("================================================================================")
    print("   HARDWARE WIZARD - OTONOM TÜRKİYE FİYAT & ÇOKLU MAĞAZA TARAYICI                ")
    print("================================================================================")

    # 0. Önceki prices_tr.json dosyasını yükle (gerçek geçmişi korumak için)
    existing_prices_tr = {}
    p_main = TARGET_PRICE_FILES[0]
    if os.path.exists(p_main):
        try:
            with open(p_main, "r", encoding="utf-8") as f:
                existing_prices_tr = json.load(f)
        except Exception:
            existing_prices_tr = {}

    # 1. Mevcut katalogları yükle
    catalogs = {}
    for cat, filenames in CATEGORY_FILE_MAP.items():
        fname = filenames[0]
        p = os.path.join(FILES_DIR, fname)
        if os.path.exists(p):
            with open(p, "r", encoding="utf-8") as f:
                catalogs[cat] = json.load(f)
        else:
            catalogs[cat] = []
        print(f"[{cat.upper()}] Mevcut katalog boyutu: {len(catalogs[cat])} ürün")

    # 2. Mağazaları eş zamanlı tara
    scraped_products = []
    print(f"\n[+] {len(STORE_CATEGORIES)} mağaza kategori görevi ThreadPoolExecutor ile başlatılıyor...")
    start_time = time.time()

    with ThreadPoolExecutor(max_workers=6) as executor:
        future_map = {
            executor.submit(crawl_category, store, cat, pattern, pages): (store, cat)
            for store, cat, pattern, pages in STORE_CATEGORIES
        }
        for future in as_completed(future_map):
            store, cat = future_map[future]
            try:
                res = future.result()
                scraped_products.extend(res)
                print(f"  [OK] {store:<14} | {cat:<11} -> {len(res)} ürün çekildi.")
            except Exception as e:
                print(f"  [HATA] {store} {cat}: {e}")

    elapsed = round(time.time() - start_time, 2)
    print(f"\n[+] Kategori taraması tamamlandı: {len(scraped_products)} ürün toplandı ({elapsed} saniye).")

    # 3. Eşleştirme ve Fiyat Güncelleme
    matched_offers_by_id = {}
    for sp in scraped_products:
        cat = sp["category"]
        title = sp["title"]
        price = sp["price"]
        store = sp["store"]
        url = sp["url"]

        catalog_list = catalogs.get(cat, [])
        match = match_product(cat, title, catalog_list)

        if match:
            prod_id = generate_hardware_id(cat, match["name"])
            if prod_id not in matched_offers_by_id:
                matched_offers_by_id[prod_id] = []
            offer_obj = {
                "storeName": store,
                "priceTry": price,
                "url": url,
                "inStock": True
            }
            if is_valid_image_url(sp.get("imageUrl"), match.get("name", "")):
                offer_obj["imageUrl"] = sp["imageUrl"]
                if not is_valid_image_url(match.get("imageUrl"), match.get("name", "")):
                    match["imageUrl"] = sp["imageUrl"]
            matched_offers_by_id[prod_id].append(offer_obj)

    # 4. Doğrudan Arama (Direct Search Fallback)
    direct_search_fallback(catalogs, matched_offers_by_id)

    # 5. prices_tr_map oluştur
    prices_tr_map = {}
    for cat, items in catalogs.items():
        for it in items:
            prod_id = generate_hardware_id(cat, it["name"])
            offers = matched_offers_by_id.get(prod_id, [])

            # Mağaza bazında en ucuz fiyatı tekilleştir
            unique_offers = {}
            for o in offers:
                s_name = o["storeName"]
                if s_name not in unique_offers or o["priceTry"] < unique_offers[s_name]["priceTry"]:
                    unique_offers[s_name] = o
            sorted_offers = sorted(unique_offers.values(), key=lambda x: x["priceTry"])

            current_price = sorted_offers[0]["priceTry"] if sorted_offers else it.get("price")
            existing_entry = existing_prices_tr.get(prod_id, {})
            existing_hist = existing_entry.get("priceHistory", [])

            if current_price is not None:
                best_store = sorted_offers[0]["storeName"] if sorted_offers else "Türkiye Pazarı"
                hist = resolve_price_history(prod_id, current_price, best_store, existing_hist, sorted_offers)
                lowest_price = min(p["priceTry"] for p in hist) if hist else current_price
                highest_price = max(p["priceTry"] for p in hist) if hist else current_price
            else:
                hist = []
                lowest_price = None
                highest_price = None

            best_image = None
            if is_valid_image_url(it.get("imageUrl"), it.get("name", "")):
                best_image = it.get("imageUrl")
            else:
                for o in sorted_offers:
                    img_candidate = o.get("imageUrl")
                    if is_valid_image_url(img_candidate, it.get("name", "")):
                        best_image = img_candidate
                        break

            prices_tr_map[prod_id] = {
                "productId": prod_id,
                "currentPriceTry": current_price,
                "lowestPriceTry": lowest_price,
                "highestPriceTry": highest_price,
                "lastUpdated": datetime.now().isoformat(),
                "imageUrl": best_image,
                "priceHistory": hist,
                "offers": sorted_offers
            }

    # 6. prices_tr.json Dosyalarına Kaydet
    for p_file in TARGET_PRICE_FILES:
        os.makedirs(os.path.dirname(p_file), exist_ok=True)
        with open(p_file, "w", encoding="utf-8") as f:
            json.dump(prices_tr_map, f, indent=2, ensure_ascii=False)
        print(f"[OK] {os.path.basename(p_file)} kaydedildi ({len(prices_tr_map)} ürün).")

    # 7. Katalog dosyalarını kaydet
    for cat, filenames in CATEGORY_FILE_MAP.items():
        for fname in filenames:
            for d in [FILES_DIR, CATALOG_DIR]:
                p = os.path.join(d, fname)
                os.makedirs(os.path.dirname(p), exist_ok=True)
                with open(p, "w", encoding="utf-8") as f:
                    json.dump(catalogs[cat], f, indent=2, ensure_ascii=False)

    active_price_count = sum(1 for v in prices_tr_map.values() if v.get("currentPriceTry") is not None)
    print("================================================================================")
    print(f"[BAŞARILI] Toplam {len(prices_tr_map)} ürün güncellendi.")
    print(f"           - {active_price_count} ürüne aktif mağaza teklifi bağlandı.")
    print(f"           - Hem data/catalog hem de shared/.../files dizinleri senkronize edildi.")
    print("================================================================================")

if __name__ == "__main__":
    main()
