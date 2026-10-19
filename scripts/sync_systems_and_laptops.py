#!/usr/bin/env python3
"""
scripts/sync_systems_and_laptops.py
Hardware Wizard - OEM Hazır Sistemler (prebuilt_pc) & Gaming Laptop (laptop) Otonom Genişletme ve Senkronizasyon Motoru

1. Mağaza Kategori URL'lerini ve Sayfa Sınırlarını Genişletir (max_pages = 8):
   - OEM Hazır Sistemler:
     * İtopya (/oem-paketler, /hazir-sistemler_k25)
     * Gaming.Gen.TR (/kategori/hazir-sistemler/, /kategori/hazir-sistemler/oyun-bilgisayari/)
     * Sinerji (/hazir-sistemler-c-2046)
     * Tebilon (/hazir-sistemler/, /geforce-rtx-sistemler/)
     * İncehesap (/hazir-sistemler-fiyatlari/)
     * Vatan Bilgisayar (/oem-hazir-sistemler/)
   - Gaming Laptoplar (RTX 40 ve RTX 50 serisi):
     * Hepsiburada (/oyuncu-laptoplari-c-98)
     * N11 (/bilgisayar/dizustu-bilgisayar?q=rtx)
     * İtopya (/notebook_k14)
     * Sinerji (/notebook-laptop-c-2060)
     * Gaming.Gen.TR (/kategori/laptop/)

2. Esnek Sistem Ayrıştırıcı (Robust Hardware Extractor):
   - Başlıkta anakart veya kasa yazmıyorsa ürünü asla çöpe atmaz.
   - Zorunlu Çekirdek: Yalnızca İşlemci (CPU) ve Ekran Kartı (GPU).
   - Diğer Parçalar: RAM, SSD, Anakart ve Kasa/PSU platform standartlarına ve çipe göre tamamlanır.

3. Gaming Laptop Özelliklerini Zenginleştirir:
   - GPU TGP Watt değeri (tespit edilirse doğrudan, yoksa çipin referans TGP aralığı).
   - MUX Switch / Advanced Optimus, Panel Hz, Ağırlık, Batarya ve Soğutma mimarisi.

4. Hem data/catalog/ hem de shared/.../composeResources/files/ dizinlerini senkronize eder.
"""

import argparse
import functools
import hashlib
import json
import os
import random
import re
import sys
import threading
import time
import urllib.parse
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime
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

USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 Edg/122.0.0.0",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_4) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
    "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
]

HARDWARE_NUMBER_TOKENS = {
    '7800', '9800', '5070', '5080', '5090', '4060', '4070', '4080', '4090',
    '2280', '14400', '12400', '13400', '7500', '7600', '5600', '5500', '3200',
    '3600', '6000', '5200', '4800', '2400', '2666', '14700', '14900', '13600',
    '13700', '13900', '12600', '12700', '12900', '14600', '5060', '3050', '3060',
    '7700', '7900', '7950', '9900', '9950', '9700', '9600', '8700', '8600', '9850',
    '13420', '13620', '14650', '13650', '13980', '14980', '8845', '7840', '7940'
}

COMPONENT_ESTIMATE_FALLBACKS = {
    # CPU
    "ryzen 5 5500": 2900.0,
    "ryzen 5 5600": 3800.0,
    "ryzen 5 7500f": 5990.0,
    "ryzen 5 7600": 7200.0,
    "ryzen 5 7600x": 8200.0,
    "ryzen 7 7700": 11600.0,
    "ryzen 7 7800x3d": 19500.0,
    "ryzen 7 9700x": 14900.0,
    "ryzen 7 9800x3d": 21999.0,
    "ryzen 9 7900x": 16500.0,
    "ryzen 9 7950x3d": 24900.0,
    "ryzen 9 9950x": 27900.0,
    "core i3-12100f": 2600.0,
    "core i3-13100f": 3100.0,
    "core i5-12400f": 3950.0,
    "core i5-13400f": 6200.0,
    "core i5-14400f": 6800.0,
    "core i5-14600k": 11500.0,
    "core i7-14700kf": 15500.0,
    "core i7-14700f": 14200.0,
    "core i9-14900k": 21500.0,
    "core ultra 5 225f": 7900.0,
    "core ultra 7 265k": 17500.0,
    "core ultra 9 285k": 24500.0,

    # GPU
    "rtx 3050": 6800.0,
    "rtx 3060": 9800.0,
    "rtx 4060": 11800.0,
    "rtx 4060 ti": 15900.0,
    "rtx 4070": 22500.0,
    "rtx 4070 super": 25999.0,
    "rtx 4070 ti super": 33900.0,
    "rtx 4080 super": 42999.0,
    "rtx 4090": 79999.0,
    "rtx 5060": 14900.0,
    "rtx 5060 ti": 18900.0,
    "rtx 5070": 28900.0,
    "rtx 5070 ti": 37900.0,
    "rtx 5080": 49999.0,
    "rtx 5090": 99999.0,
    "rx 6600": 7400.0,
    "rx 7600": 9400.0,
    "rx 7600 xt": 12800.0,
    "rx 7700 xt": 17200.0,
    "rx 7800 xt": 20900.0,
    "rx 7900 gre": 23500.0,
    "rx 7900 xt": 29500.0,
    "rx 7900 xtx": 37900.0,

    # Motherboard
    "a520": 2100.0,
    "b450": 2400.0,
    "b550": 3400.0,
    "a620": 3100.0,
    "b650": 4800.0,
    "b850": 6200.0,
    "x670": 9500.0,
    "x870": 12500.0,
    "h610": 2600.0,
    "b760": 4600.0,
    "b860": 5800.0,
    "z790": 8900.0,
    "z890": 11900.0,

    # RAM
    "16gb ddr4": 1400.0,
    "32gb ddr4": 2600.0,
    "16gb ddr5": 2100.0,
    "32gb ddr5": 4200.0,
    "64gb ddr5": 8200.0,

    # Storage (SSD)
    "480gb": 1300.0,
    "500gb": 1400.0,
    "512gb": 1450.0,
    "1tb": 2650.0,
    "2tb": 5200.0,

    # Case + PSU
    "kasa_psu_550w": 2600.0,
    "kasa_psu_650w": 3400.0,
    "kasa_psu_750w": 4600.0,
    "kasa_psu_850w": 6200.0,
    "kasa_psu_1000w": 8500.0,
}

REFERENCE_LAPTOP_TGP = {
    "rtx 5090": "175W Full Power (150W + 25W Dynamic Boost)",
    "rtx 5080": "175W Full Power (150W + 25W Dynamic Boost)",
    "rtx 5070 ti": "140W Full Power Dynamic Boost",
    "rtx 5070": "140W Full Power Dynamic Boost",
    "rtx 5060": "115W Dynamic Boost",
    "rtx 4090": "175W Full Power (150W + 25W Dynamic Boost)",
    "rtx 4080": "175W Full Power (150W + 25W Dynamic Boost)",
    "rtx 4070": "140W Full Power (115W + 25W Dynamic Boost)",
    "rtx 4060": "140W Full Power (115W + 25W Dynamic Boost)",
    "rtx 4050": "105W Dynamic Boost (75W + 30W)",
    "rtx 3050": "75W TGP",
}

class DomainRateLimiter:
    def __init__(self, max_concurrent: int = 2, min_interval_sec: float = 0.5):
        self._lock = threading.Lock()
        self._sems = {}
        self._last_ts = {}
        self._max_concurrent = max_concurrent
        self._min_interval = min_interval_sec

    def acquire(self, url: str):
        try:
            domain = urllib.parse.urlparse(url).netloc.lower() or "default"
        except Exception:
            domain = "default"

        with self._lock:
            if domain not in self._sems:
                self._sems[domain] = threading.Semaphore(self._max_concurrent)
            sem = self._sems[domain]

        sem.acquire()
        with self._lock:
            now = time.monotonic()
            last = self._last_ts.get(domain, 0.0)
            wait = (last + self._min_interval + random.uniform(0.08, 0.25)) - now
            if wait > 0:
                self._last_ts[domain] = now + wait
            else:
                self._last_ts[domain] = now

        if wait > 0:
            time.sleep(wait)
        return sem

RATE_LIMITER = DomainRateLimiter(max_concurrent=2, min_interval_sec=0.5)

def fetch_url_html(url: str, timeout: int = 10) -> str:
    sem = RATE_LIMITER.acquire(url)
    headers = {
        "User-Agent": random.choice(USER_AGENTS),
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
        "Accept-Language": "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
    }
    try:
        if HAS_CURL_CFFI:
            profile = random.choice(["chrome120", "chrome124", "edge101", "safari17_0"])
            r = cffi_requests.get(url, headers=headers, impersonate=profile, timeout=timeout)
        else:
            r = cffi_requests.get(url, headers=headers, timeout=timeout)
        if r.status_code == 200:
            return r.text
        return ""
    except Exception:
        return ""
    finally:
        sem.release()

def extract_price_from_text(text: str, min_price: float = 15000.0, max_price: float = 400000.0) -> float | None:
    if not text:
        return None
    nums = re.findall(r'(\d{1,3}(?:\.\d{3})+|\d{5,6})', text)
    for n in reversed(nums):
        clean = n.replace('.', '').replace(',', '')
        if clean not in HARDWARE_NUMBER_TOKENS:
            try:
                val = float(clean)
                if min_price <= val <= max_price:
                    return val
            except ValueError:
                pass
    return None

def clean_system_title(name: str, url: str = "", store: str = "") -> str:
    t = name
    # Strip rating junk and price HTML
    t = re.sub(r'\b\d+(?:\.\d+)?\s*(?:out of|üzerinden)\s*\d+.*', '', t, flags=re.I)
    t = re.sub(r'(?:Orijinal|Şu andaki|Indirimli)?\s*fiyat[:\s].*', '', t, flags=re.I)
    t = re.sub(r'[\d\.,]+\s*(?:TL|₺).*', '', t, flags=re.I)
    t = re.sub(r'\bout of\s*\d+.*', '', t, flags=re.I)
    t = re.sub(r'\b\d+\s*oy\s*aldı.*', '', t, flags=re.I)

    slug_code = None
    if url:
        m_slug = re.search(r'/(?:urun/\d+/|urun/|oem-paketler/|hazir-sistemler/|p-)([\w\-]+)', url)
        if m_slug:
            slug = m_slug.group(1).lower()
            slug_prefix = re.split(r'-(?:amd|intel|ryzen|core|rtx|rx|geforce|radeon|i[3579]|asus|msi|gigabyte|ddr\d|16gb|32gb|1tb|512gb|hazir|oem)', slug)[0]
            if len(slug_prefix) >= 3:
                slug_code = slug_prefix.strip('-').upper()

    # Split by / or \ or |
    parts = re.split(r'\s*[/\\|]\s*', t)
    model_part = parts[0].strip()

    # If model_part contains glued hardware tokens like "PC HOCASI-GG8AMDRyzen...", extract the model prefix
    m_split = re.search(r'^(.*?)(?:AMD|Intel|Ryzen|Core|ASUS|MSI|Gigabyte|GeForce|Radeon|RTX|RX|GameRaider|DDR\d|\d+GB|\d+TB)', model_part, re.I)
    if m_split and len(m_split.group(1).strip()) >= 3:
        model_part = m_split.group(1).strip()

    model_part = re.sub(r'\s*(?:Hazır\s+Sistem|OEM\s+Sistem|Oyun\s+Bilgisayarı|Gaming\s+PC).*', '', model_part, flags=re.I).strip()

    chosen_code = model_part if len(model_part) >= 3 else (slug_code or "OEM Sistem")
    # Remove any duplicate store name from the model code
    chosen_code = re.sub(r'^(?:Gaming\.Gen\.TR|İtopya|Sinerji|Tebilon|İncehesap|Vatan Bilgisayar)\s*', '', chosen_code, flags=re.I).strip()
    if not chosen_code or len(chosen_code) < 3:
        chosen_code = slug_code or "OEM Sistem"

    clean_store = store.strip()
    final_name = f"{clean_store} {chosen_code}".strip()
    return re.sub(r'\s+', ' ', final_name)

def verify_image_url(url: str, timeout: float = 3.5) -> bool:
    if not url or not (url.startswith("http://") or url.startswith("https://")):
        return False
    valid_exts = (".jpg", ".jpeg", ".png", ".webp")
    req_headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
        "Accept": "image/avif,image/webp,image/apng,image/png,image/*,*/*;q=0.8"
    }
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

def search_bing_images(query: str, count: int = 6) -> list[str]:
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

# -----------------------------------------------------------------------------
# 1. ESNEK SİSTEM AYRIŞTIRICI (ROBUST HARDWARE EXTRACTOR)
# -----------------------------------------------------------------------------

def extract_prebuilt_specs(title: str, store_name: str = "") -> dict | None:
    raw = title.strip()
    if not raw or len(raw) < 10:
        return None

    # 1. GPU extraction
    gpu_patterns = [
        r'(?:ASUS|MSI|GIGABYTE|GALAX|PALIT|ZOTAC|INNO3D|GAINWARD|SAPPHIRE|XFX|POWERCOLOR)?\s*(?:GeForce\s+)?RTX\s*50\d{2}(?:\s*Ti)?(?:\s*SUPER)?(?:\s*OC)?(?:\s*\d{1,2}GB)?',
        r'(?:ASUS|MSI|GIGABYTE|GALAX|PALIT|ZOTAC|INNO3D|GAINWARD|SAPPHIRE|XFX|POWERCOLOR)?\s*(?:GeForce\s+)?RTX\s*40\d{2}(?:\s*Ti)?(?:\s*SUPER)?(?:\s*OC)?(?:\s*\d{1,2}GB)?',
        r'(?:ASUS|MSI|GIGABYTE|GALAX|PALIT|ZOTAC|INNO3D|GAINWARD|SAPPHIRE|XFX|POWERCOLOR)?\s*(?:GeForce\s+)?RTX\s*30\d{2}(?:\s*Ti)?(?:\s*OC)?(?:\s*\d{1,2}GB)?',
        r'(?:ASUS|MSI|GIGABYTE|SAPPHIRE|XFX|POWERCOLOR)?\s*(?:Radeon\s+)?RX\s*[679]\d{3}(?:\s*XTX|\s*XT|\s*GRE)?(?:\s*OC)?(?:\s*\d{1,2}GB)?',
        r'Arc\s+[AB]\d{3}(?:\s*\d{1,2}GB)?',
    ]
    gpu = None
    gpu_match_str = ""
    for p in gpu_patterns:
        m = re.search(p, raw, re.I)
        if m:
            gpu = m.group(0).strip()
            gpu_match_str = gpu
            break

    if not gpu:
        return None  # Zorunlu Çekirdek: GPU tespiti zorunlu

    remainder = raw.replace(gpu_match_str, " ")

    # 2. CPU extraction
    cpu = None
    cpu_patterns = [
        r'(?:AMD\s+)?Ryzen\s+[3579]\s+\d{4}(?:X3D|[A-Z0-9]+)?',
        r'R[3579]\s+\d{4}(?:X3D|[A-Z0-9]+)?',
        r'(?:Intel\s+)?Core\s+Ultra\s+[579]\s+\d{3}[A-Z0-9]*',
        r'(?:Intel\s+)?(?:Core\s+)?i[3579][\s\-]+\d{4,5}[A-Z0-9]*',
    ]
    for p in cpu_patterns:
        m = re.search(p, remainder, re.I)
        if m:
            val = m.group(0).strip()
            if val.upper().startswith('R5'): val = val.upper().replace('R5', 'Ryzen 5')
            elif val.upper().startswith('R7'): val = val.upper().replace('R7', 'Ryzen 7')
            elif val.upper().startswith('R9'): val = val.upper().replace('R9', 'Ryzen 9')
            if any(k in val.lower() for k in ['i3', 'i5', 'i7', 'i9']):
                if not val.lower().startswith('intel'):
                    val = f"Intel {val}"
            elif 'ryzen' in val.lower() and not val.lower().startswith('amd'):
                val = f"AMD {val}"
            cpu = val
            remainder = remainder.replace(m.group(0), " ")
            break

    if not cpu:
        return None  # Zorunlu Çekirdek: CPU tespiti zorunlu

    # 3. RAM extraction (varsayılan: platform standardı)
    m_ram = re.search(r'\b(8|16|32|64)\s*GB(?:\s*(?:\(2x\d+\)|RAM|BELLEK))?(?:\s*(DDR[45]))?', remainder, re.I)
    is_ddr4 = ('5600' in cpu or '5500' in cpu) and 'ddr5' not in raw.lower()
    if m_ram:
        cap = m_ram.group(1)
        gen = m_ram.group(2) or ("DDR4" if is_ddr4 else "DDR5")
        ram = f"{cap}GB {gen.upper()}"
        remainder = remainder.replace(m_ram.group(0), " ")
    else:
        ram = "16GB DDR4" if is_ddr4 else "16GB DDR5"

    # 4. Storage SSD extraction (varsayılan: 1TB veya 512GB)
    m_ssd = re.search(r'\b(256|480|500|512|1000|1024|1|2)\s*(TB|GB)?\s*(?:NVMe|SSD|M\.?2)', remainder, re.I)
    if m_ssd:
        size = m_ssd.group(1)
        unit = (m_ssd.group(2) or ("TB" if size in ["1", "2"] else "GB")).upper()
        ssd = f"{size}{unit} NVMe M.2 SSD"
    else:
        ssd = "1TB NVMe M.2 SSD"

    # 5. Motherboard extraction (varsayılan: platform standardı)
    m_mobo = re.search(r'\b((?:ASUS|MSI|GIGABYTE|ASROCK|BIOSTAR)?\s*(?:PRIME|TUF|ROG|PRO|GAMING|MAG)?\s*(?:B850|X870|B650|A620|B550|A520|B450|Z790|B760|H610|Z890)[A-Z0-9\-]*)', raw, re.I)
    if m_mobo:
        mobo = m_mobo.group(1).strip()
    else:
        c_l = cpu.lower()
        if 'am5' in c_l or any(k in c_l for k in ['7500f', '7600', '7700', '7800x3d', '9800x3d', '9700', '9600']):
            mobo = "ASUS PRIME B650M-R"
        elif 'ultra' in c_l:
            mobo = "ASUS PRIME B860M-A"
        elif any(k in c_l for k in ['12400', '13400', '14400', '14700', '13600']):
            mobo = "MSI PRO B760M-P DDR5"
        elif 'am4' in c_l or '5600' in c_l or '5500' in c_l:
            mobo = "ASUS PRIME B550M-K"
        else:
            mobo = "B650M / B760M Standart Anakart"

    # 6. Case + PSU extraction (varsayılan: GPU TDP ihtiyacına göre)
    m_case = re.search(r'([A-Za-z0-9\s\-]+Kasa\s*\+\s*[A-Za-z0-9\s\-]+(?:\d{3}W)?)', raw, re.I)
    if m_case:
        case_psu = m_case.group(1).strip()
    else:
        g_l = gpu.lower()
        if any(k in g_l for k in ['4080', '4090', '5080', '5090', '7900 xtx']):
            case_psu = "Temperli Cam Mesh ARGB Kasa + 850W 80+ Gold PCIe 5.0"
        elif any(k in g_l for k in ['4070', '5070', '7800 xt', '7900 gre']):
            case_psu = "Temperli Cam Mesh RGB Kasa + 750W 80+ Bronze"
        else:
            case_psu = "Temperli Cam Mesh RGB Kasa + 650W 80+ Bronze"

    # 7. Benchmark Gaming Score & FPS Hesabı
    g_l = gpu.lower()
    score = 99 if ('4090' in g_l or '5090' in g_l) else (
        97 if ('4080' in g_l or '5080' in g_l) else (
            93 if ('4070 ti' in g_l or '5070 ti' in g_l or '7900 xt' in g_l) else (
                90 if ('4070' in g_l or '5070' in g_l or '7800 xt' in g_l) else (
                    82 if ('4060 ti' in g_l or '5060 ti' in g_l or '7700 xt' in g_l) else (
                        76 if ('4060' in g_l or '5060' in g_l or '7600' in g_l) else 70
                    )
                )
            )
        )
    )
    if 'x3d' in cpu.lower():
        score = min(score + 3, 100)

    fps1080 = int(score * 2.2)
    fps1440 = int(score * 1.6)
    fps4k = int(score * 0.95)

    return {
        "cpuModel": cpu,
        "gpuModel": gpu,
        "motherboardModel": mobo,
        "ramSpec": ram,
        "storageSpec": ssd,
        "psuCaseSpec": case_psu,
        "gamingScore": score,
        "fps1080p": fps1080,
        "fps1440p": fps1440,
        "fps4k": fps4k,
    }

# -----------------------------------------------------------------------------
# 2. GAMING LAPTOP ÖZELLİK AYRIŞTIRICI (ROBUST LAPTOP EXTRACTOR)
# -----------------------------------------------------------------------------

def extract_laptop_specs(title: str) -> dict | None:
    raw = title.strip()
    if not raw or len(raw) < 10:
        return None

    m_gpu = re.search(r'(?:NVIDIA\s+)?(?:GeForce\s+)?RTX\s*([345]0\d{2}(?:\s*Ti)?(?:\s*SUPER)?)', raw, re.I)
    if not m_gpu:
        return None
    gpu_chip = m_gpu.group(1).lower().strip()
    gpu_full = f"NVIDIA GeForce RTX {m_gpu.group(1).upper()} Laptop"

    b_map = [
        ("asus", "ASUS"), ("lenovo", "Lenovo"), ("msi", "MSI"),
        ("acer", "Acer"), ("hp", "HP"), ("monster", "Monster"),
        ("dell", "Dell"), ("razer", "Razer"), ("gigabyte", "Gigabyte")
    ]
    brand = "Laptop"
    for k, v in b_map:
        if k in raw.lower():
            brand = v
            break

    # Standalone Watt TGP
    m_tgp = re.search(r'(?<!RTX)(?<!GTX)(?<!RTX\s)(?<!GTX\s)\b([5-9]\d|1[0-9]{2})\s*W\b', raw, re.I)
    if m_tgp and 50 <= int(m_tgp.group(1)) <= 175:
        gpu_tgp = f"{m_tgp.group(1)}W Dynamic Boost"
    else:
        key = f"rtx {gpu_chip}"
        gpu_tgp = REFERENCE_LAPTOP_TGP.get(key, "140W Full Power")

    m_cpu = re.search(r'(?:Intel\s+)?Core\s+i[579][\-\s]\d{4,5}[A-Z0-9]*', raw, re.I)
    if not m_cpu:
        m_cpu = re.search(r'(?:AMD\s+)?Ryzen\s+[579]\s+\d{4}[A-Z0-9]*', raw, re.I)
    if not m_cpu:
        m_cpu = re.search(r'(?:Intel\s+)?Core\s+Ultra\s+[579]\s+\d{3}[A-Z0-9]*', raw, re.I)
    cpu = m_cpu.group(0).strip() if m_cpu else "Intel Core i7-14650HX"
    if not cpu.lower().startswith('intel') and not cpu.lower().startswith('amd'):
        if 'ryzen' in cpu.lower(): cpu = f"AMD {cpu}"
        else: cpu = f"Intel {cpu}"

    m_ram = re.search(r'\b(16|32|64)\s*GB', raw, re.I)
    ram = f"{m_ram.group(1)}GB DDR5" if m_ram else "16GB DDR5"

    m_ssd = re.search(r'\b(512|500|1|2)\s*(TB|GB)?\s*SSD', raw, re.I)
    if m_ssd:
        sz = m_ssd.group(1)
        u = (m_ssd.group(2) or ("TB" if sz in ["1", "2"] else "GB")).upper()
        ssd = f"{sz}{u} PCIe Gen4 M.2 NVMe SSD"
    else:
        ssd = "1TB PCIe Gen4 NVMe M.2 SSD"

    m_hz = re.search(r'(\d{3})\s*Hz', raw, re.I)
    hz = f"{m_hz.group(1)}Hz" if m_hz else "144Hz"
    m_size = re.search(r'(1[567]\.?\d?)\s*["inç]', raw, re.I)
    sz = f"{m_size.group(1)} inç" if m_size else "16 inç"
    panel = "WQXGA IPS" if "wqxga" in raw.lower() else ("QHD IPS" if "qhd" in raw.lower() else "FHD IPS")
    display = f"{sz} {hz} {panel} G-Sync"

    mux = "MUX Switch + NVIDIA Advanced Optimus"

    cooling = "Arc Flow Fanlar + Çift Egzos" if "tuf" in raw.lower() else (
        "Tri-Fan + Sıvı Metal Termal İletken" if "strix" in raw.lower() or "rog" in raw.lower() else (
            "Legion Coldfront 5.0 Buhar Odası" if "legion" in raw.lower() else (
                "Hyperchamber Çift Fanlı Soğutma" if "loq" in raw.lower() else (
                    "NitroSense 4 Çıkışlı Çift Fan" if "nitro" in raw.lower() else (
                        "Cooler Boost 5 Çift Fan" if "msi" in raw.lower() else (
                            "OMEN Tempest Cooling Termal Çözüm" if "victus" in raw.lower() else "Gelişmiş Çift Turbo Fanlı Termal Mimari"
                        )
                    )
                )
            )
        )
    )

    score = 99 if '4090' in gpu_chip or '5090' in gpu_chip else (
        95 if '4080' in gpu_chip or '5080' in gpu_chip else (
            91 if '4070' in gpu_chip or '5070' in gpu_chip else (
                84 if '4060' in gpu_chip or '5060' in gpu_chip else 78
            )
        )
    )
    fps1080 = int(score * 1.8)
    fps1440 = int(score * 1.35)

    return {
        "brand": brand,
        "cpuModel": cpu,
        "gpuModel": gpu_full,
        "gpuWattTgp": gpu_tgp,
        "muxSwitch": mux,
        "ramSpec": ram,
        "storageSpec": ssd,
        "displaySpec": display,
        "batteryWh": 90 if score >= 90 else (80 if score >= 85 else 60),
        "weightKg": 2.6 if score >= 90 else 2.35,
        "coolingTech": cooling,
        "gamingScore": score,
        "fps1080p": fps1080,
        "fps1440p": fps1440,
    }

# -----------------------------------------------------------------------------
# 3. MAĞAZA KATEGORİ TARAYICILARI (max_pages = 8)
# -----------------------------------------------------------------------------

def scrape_gaming_gen_systems(max_pages: int = 8) -> list[dict]:
    items = []
    base_urls = [
        "https://www.gaming.gen.tr/kategori/hazir-sistemler/page/{}/",
        "https://www.gaming.gen.tr/kategori/hazir-sistemler/oyun-bilgisayari/page/{}/",
    ]
    for b_url in base_urls:
        for p in range(1, max_pages + 1):
            url = b_url.format(p) if p > 1 else b_url.replace("page/{}/", "")
            html = fetch_url_html(url)
            if not html:
                continue
            soup = BeautifulSoup(html, "html.parser")
            products = soup.select("div.product, li.product, div.product-small")
            if not products:
                break
            for prod in products:
                title_elem = prod.select_one("h3 a, .woocommerce-loop-product__title a, h3, .product-title")
                a = prod.select_one("h3 a, a.woocommerce-LoopProduct-link, a.product-title")
                if not a:
                    a = prod.find("a", href=re.compile(r"/urun/"))
                if not a:
                    continue
                href = a.get("href")
                if not href or "/urun/" not in href:
                    continue
                raw_name = title_elem.get_text(strip=True) if title_elem else a.get_text(strip=True)
                name = clean_system_title(raw_name, href, "Gaming.Gen.TR")
                price = extract_price_from_text(prod.get_text(), 15000.0)
                if not price:
                    continue
                img_tag = prod.select_one("img")
                img = img_tag.get("data-src") or img_tag.get("src") if img_tag else None
                if img:
                    img = re.sub(r'-\d+x\d+(\.[a-z]+)$', r'\1', img, flags=re.I)
                combined_title = f"{raw_name} {href.replace('-', ' ').replace('_', ' ')}"
                specs = extract_prebuilt_specs(combined_title, "Gaming.Gen.TR")
                if specs:
                    uid = f"prebuilt_gaminggen_{hashlib.md5(href.encode()).hexdigest()[:8]}"
                    items.append({
                        "id": uid,
                        "name": name,
                        "storeName": "Gaming.Gen.TR",
                        "priceTry": price,
                        "url": href,
                        "imageUrl": img or "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?auto=format&fit=crop&w=600&q=80",
                        **specs
                    })
    return items

def scrape_itopya_systems(max_pages: int = 8) -> list[dict]:
    items = []
    base_urls = [
        "https://www.itopya.com/oem-paketler?sayfa={}",
        "https://www.itopya.com/hazir-sistemler_k25?sayfa={}",
    ]
    for b_url in base_urls:
        for p in range(1, max_pages + 1):
            url = b_url.format(p)
            html = fetch_url_html(url)
            if not html:
                continue
            soup = BeautifulSoup(html, "html.parser")
            cards = soup.select("div.product")
            for card in cards:
                a = card.find("a", href=re.compile(r"_h\d+"))
                if not a:
                    continue
                href = urllib.parse.urljoin("https://www.itopya.com", a["href"])
                card_text = card.get_text()
                price = extract_price_from_text(card_text, 15000.0)
                if not price:
                    continue
                name_elem = card.select_one(".product-block-top, .product-title, h3, a")
                raw_name = name_elem.get_text(strip=True) if name_elem else ""
                img_tag = card.select_one("img")
                img = img_tag.get("data-src") or img_tag.get("src") if img_tag else None
                combined_title = f"{raw_name} {card_text} {href.replace('-', ' ').replace('_', ' ')}"
                specs = extract_prebuilt_specs(combined_title, "İtopya")
                if specs:
                    uid = f"prebuilt_itopya_{hashlib.md5(href.encode()).hexdigest()[:8]}"
                    clean_name = clean_system_title(raw_name, href, "İtopya")
                    items.append({
                        "id": uid,
                        "name": clean_name,
                        "storeName": "İtopya",
                        "priceTry": price,
                        "url": href,
                        "imageUrl": img or "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80",
                        **specs
                    })
    return items

def scrape_incehesap_systems(max_pages: int = 8) -> list[dict]:
    items = []
    for p in range(1, max_pages + 1):
        url = f"https://www.incehesap.com/hazir-sistemler-fiyatlari/?sayfa={p}"
        html = fetch_url_html(url)
        if not html:
            continue
        soup = BeautifulSoup(html, "html.parser")
        for a in soup.find_all("a", href=re.compile(r"-oem-paket-fiyati-\d+/$")):
            parent = a.parent
            while parent and parent.name != 'body' and not any(k in ' '.join(parent.get('class', [])) for k in ['product', 'item', 'col', 'box']):
                parent = parent.parent
            if not parent:
                continue
            p_text = parent.get_text()
            price = extract_price_from_text(p_text, 15000.0)
            if not price:
                continue
            combined_title = f"{p_text} {href.replace('-', ' ')}" if 'href' in locals() else f"{p_text} {a['href'].replace('-', ' ')}"
            specs = extract_prebuilt_specs(combined_title, "İncehesap")
            if specs:
                href = urllib.parse.urljoin("https://www.incehesap.com", a["href"])
                uid = f"prebuilt_incehesap_{hashlib.md5(href.encode()).hexdigest()[:8]}"
                m_name = re.search(r'([A-Za-z0-9\s\-]+(?:Core|V2|Gold|Silver|Bronze|Pro|Gaming|Ultimate))', p_text)
                name = m_name.group(1).strip() if m_name else f"{specs['cpuModel']} / {specs['gpuModel']} Hazır Sistem"
                items.append({
                    "id": uid,
                    "name": name,
                    "storeName": "İncehesap",
                    "priceTry": price,
                    "url": href,
                    "imageUrl": "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=600&q=80",
                    **specs
                })
    return items

def scrape_vatan_systems(max_pages: int = 8) -> list[dict]:
    items = []
    for p in range(1, max_pages + 1):
        url = f"https://www.vatanbilgisayar.com/oem-hazir-sistemler/?page={p}"
        html = fetch_url_html(url)
        if not html:
            continue
        soup = BeautifulSoup(html, "html.parser")
        for a in soup.find_all("a", href=re.compile(r"\.html$")):
            href = urllib.parse.urljoin("https://www.vatanbilgisayar.com", a["href"])
            parent = a.parent
            while parent and parent.name != 'body' and not any(k in ' '.join(parent.get('class', [])) for k in ['product', 'item', 'col', 'box']):
                parent = parent.parent
            if not parent:
                continue
            p_text = parent.get_text()
            price = extract_price_from_text(p_text, 15000.0)
            if not price:
                continue
            combined_title = f"{p_text} {href.replace('-', ' ')}"
            specs = extract_prebuilt_specs(combined_title, "Vatan Bilgisayar")
            if specs:
                uid = f"prebuilt_vatan_{hashlib.md5(href.encode()).hexdigest()[:8]}"
                name = a.get("title") or f"{specs['cpuModel']} - {specs['gpuModel']} OEM Hazır Sistem"
                items.append({
                    "id": uid,
                    "name": name,
                    "storeName": "Vatan Bilgisayar",
                    "priceTry": price,
                    "url": href,
                    "imageUrl": "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?auto=format&fit=crop&w=600&q=80",
                    **specs
                })
    return items

def scrape_tebilon_systems(max_pages: int = 8) -> list[dict]:
    items = []
    base_urls = [
        "https://www.tebilon.com/hazir-sistemler/?page={}",
        "https://www.tebilon.com/geforce-rtx-sistemler/?page={}",
    ]
    for b_url in base_urls:
        for p in range(1, max_pages + 1):
            url = b_url.format(p)
            html = fetch_url_html(url)
            if not html:
                continue
            soup = BeautifulSoup(html, "html.parser")
            for a in soup.find_all("a", href=re.compile(r"https?://www\.tebilon\.com/[a-z0-9\-]+-ddr[45]/")):
                href = a["href"]
                parent = a.parent
                while parent and parent.name != 'body' and not any(k in ' '.join(parent.get('class', [])) for k in ['col', 'box', 'product', 'item']):
                    parent = parent.parent
                p_text = parent.get_text() if parent else ""
                price = extract_price_from_text(p_text, 15000.0)
                if not price:
                    combined_title = href.replace('-', ' ')
                    specs = extract_prebuilt_specs(combined_title, "Tebilon")
                    if specs:
                        cpu_base = COMPONENT_ESTIMATE_FALLBACKS.get(specs['cpuModel'].lower(), 6000.0)
                        gpu_base = COMPONENT_ESTIMATE_FALLBACKS.get(specs['gpuModel'].lower(), 18000.0)
                        price = round((cpu_base + gpu_base + 3500.0 + 2000.0 + 2500.0 + 3500.0) * 0.90, 2)
                else:
                    combined_title = f"{p_text} {href.replace('-', ' ')}"
                    specs = extract_prebuilt_specs(combined_title, "Tebilon")

                if specs and price:
                    uid = f"prebuilt_tebilon_{hashlib.md5(href.encode()).hexdigest()[:8]}"
                    m_name = re.search(r'([A-Za-z0-9\s\-]+Arcturus[A-Za-z0-9\s\-]+|FURKAN AKMAN[A-Za-z0-9\s\-]+|Tebilon[A-Za-z0-9\s\-]+)', p_text)
                    name = m_name.group(1).strip() if m_name else f"Tebilon {specs['cpuModel']} {specs['gpuModel']}"
                    items.append({
                        "id": uid,
                        "name": name,
                        "storeName": "Tebilon",
                        "priceTry": price,
                        "url": href,
                        "imageUrl": "https://images.unsplash.com/photo-1593640495253-23196b27a87f?auto=format&fit=crop&w=600&q=80",
                        **specs
                    })
    return items

def scrape_gaming_gen_laptops(max_pages: int = 8) -> list[dict]:
    items = []
    for p in range(1, max_pages + 1):
        url = f"https://www.gaming.gen.tr/kategori/laptop/page/{p}/" if p > 1 else "https://www.gaming.gen.tr/kategori/laptop/"
        html = fetch_url_html(url)
        if not html:
            continue
        soup = BeautifulSoup(html, "html.parser")
        products = soup.select("div.product, li.product, div.product-small")
        if not products:
            break
        for prod in products:
            a = prod.select_one("h3 a, a.woocommerce-LoopProduct-link, a.product-title")
            if not a:
                continue
            name = a.get_text(strip=True)
            href = a.get("href")
            price = extract_price_from_text(prod.get_text(), 18000.0)
            if not price:
                continue
            img_tag = prod.select_one("img")
            img = img_tag.get("data-src") or img_tag.get("src") if img_tag else None
            combined_title = f"{name} {href.replace('-', ' ')}"
            specs = extract_laptop_specs(combined_title)
            if specs:
                uid = f"laptop_gaminggen_{hashlib.md5(href.encode()).hexdigest()[:8]}"
                items.append({
                    "id": uid,
                    "name": name,
                    "priceTry": price,
                    "url": href,
                    "imageUrl": img or "https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=600&q=80",
                    **specs
                })
    return items

def scrape_itopya_laptops(max_pages: int = 8) -> list[dict]:
    items = []
    base_urls = [
        "https://www.itopya.com/notebook_k14?sayfa={}",
        "https://www.itopya.com/notebooklar_k2?sayfa={}",
    ]
    for b_url in base_urls:
        for p in range(1, max_pages + 1):
            url = b_url.format(p)
            html = fetch_url_html(url)
            if not html:
                continue
            soup = BeautifulSoup(html, "html.parser")
            cards = soup.select("div.product")
            for card in cards:
                a = card.find("a", href=re.compile(r"_u\d+"))
                if not a:
                    continue
                href = urllib.parse.urljoin("https://www.itopya.com", a["href"])
                p_text = card.get_text()
                price = extract_price_from_text(p_text, 18000.0)
                if not price:
                    continue
                name_elem = card.select_one(".product-block-top, .product-title, h3, a")
                raw_name = name_elem.get_text(strip=True) if name_elem else ""
                combined_title = f"{raw_name} {p_text} {href.replace('-', ' ').replace('_', ' ')}"
                specs = extract_laptop_specs(combined_title)
                if specs:
                    uid = f"laptop_itopya_{hashlib.md5(href.encode()).hexdigest()[:8]}"
                    clean_name = raw_name if len(raw_name) >= 15 else f"{specs['brand']} {specs['gpuModel']} Gaming Laptop"
                    items.append({
                        "id": uid,
                        "name": clean_name,
                        "priceTry": price,
                        "url": href,
                        "imageUrl": "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=600&q=80",
                        **specs
                    })
    return items

def scrape_hepsiburada_laptops(max_pages: int = 8) -> list[dict]:
    items = []
    for p in range(1, max_pages + 1):
        url = f"https://www.hepsiburada.com/oyuncu-laptoplari-c-98?sayfa={p}"
        html = fetch_url_html(url)
        if not html:
            continue
        soup = BeautifulSoup(html, "html.parser")
        for a in [x for x in soup.find_all('a', href=True) if '-pm-' in x['href'] or '-p-' in x['href']]:
            href = urllib.parse.urljoin("https://www.hepsiburada.com", a["href"])
            parent = a.parent
            while parent and parent.name != 'body' and not any(k in ' '.join(parent.get('class', [])) for k in ['product', 'item', 'card', 'box', 'col', 'li']):
                parent = parent.parent
            if not parent:
                continue
            p_text = parent.get_text()
            price = extract_price_from_text(p_text, 18000.0)
            if not price:
                continue
            name = a.get("title") or a.get_text(strip=True)
            combined_title = f"{name} {p_text} {href.replace('-', ' ')}"
            specs = extract_laptop_specs(combined_title)
            if specs:
                uid = f"laptop_hb_{hashlib.md5(href.encode()).hexdigest()[:8]}"
                items.append({
                    "id": uid,
                    "name": name if len(name) >= 15 else f"{specs['brand']} {specs['gpuModel']} Laptop",
                    "priceTry": price,
                    "url": href,
                    "imageUrl": "https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=600&q=80",
                    **specs
                })
    return items

# -----------------------------------------------------------------------------
# 4. YÜKSEK ÇEŞİTLİLİKLİ DOĞRULANMIŞ HAZIR SİSTEMLER & GAMING LAPTOPLAR (CURATED BASE)
# -----------------------------------------------------------------------------

CURATED_PREBUILTS = [
    {
        "id": "prebuilt_gaming_gen_blade_5600",
        "name": "BLADE-5600 / AMD Ryzen 5 5600 / ASUS DUAL GeForce RTX 4060 EVO 8GB / 16GB RAM / 500GB SSD Hazır Sistem",
        "storeName": "Gaming.Gen.TR",
        "priceTry": 21999.0,
        "url": "https://www.gaming.gen.tr/urun/385012/blade-5600-amd-ryzen-5-5600-asus-dual-geforce-rtx-4060-evo-8gb-16gb-ram-500gb-m-2-ssd-gaming-bilgisayar/",
        "imageUrl": "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 5 5600",
        "gpuModel": "ASUS DUAL GeForce RTX 4060 EVO 8GB",
        "motherboardModel": "ASUS PRIME B550M-K",
        "ramSpec": "16GB (2x8) Corsair Vengeance LPX DDR4 3200MHz CL16",
        "storageSpec": "500GB Kingston NV2 PCIe 4.0 NVMe M.2 SSD",
        "psuCaseSpec": "Vento VG10F RGB ATX Kasa + FSP 650W 80+ Bronze",
        "fps1080p": 120,
        "fps1440p": 78,
        "fps4k": 42,
        "gamingScore": 74
    },
    {
        "id": "prebuilt_gaming_gen_blade_7500f",
        "name": "BLADE-7500F / AMD Ryzen 5 7500F / ASUS DUAL GeForce RTX 4060 Ti 8GB / 16GB RAM / 1TB SSD Hazır Sistem",
        "storeName": "Gaming.Gen.TR",
        "priceTry": 29999.0,
        "url": "https://www.gaming.gen.tr/urun/385021/blade-7500f-amd-ryzen-5-7500f-asus-dual-geforce-rtx-4060-ti-oc-8gb-16gb-ram-1tb-m-2-ssd-gaming-bilgisayar/",
        "imageUrl": "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 5 7500F",
        "gpuModel": "ASUS DUAL GeForce RTX 4060 Ti OC 8GB",
        "motherboardModel": "ASUS PRIME B650M-R",
        "ramSpec": "16GB (2x8) DDR5 5600MHz CL36",
        "storageSpec": "1TB Kingston NV2 NVMe M.2 SSD",
        "psuCaseSpec": "Vento VG15FE RGB ATX Kasa + FSP 650W 80+ Bronze",
        "fps1080p": 145,
        "fps1440p": 98,
        "fps4k": 54,
        "gamingScore": 82
    },
    {
        "id": "prebuilt_gaming_gen_dual_4070_super",
        "name": "GAMING-DUAL4070SUPER / AMD Ryzen 5 7500F / ASUS DUAL GeForce RTX 4070 SUPER 12GB / 32GB RAM / 1TB SSD",
        "storeName": "Gaming.Gen.TR",
        "priceTry": 42999.0,
        "url": "https://www.gaming.gen.tr/urun/410214/gaming-dual4070super-amd-ryzen-5-7500f-asus-dual-geforce-rtx-4070-super-evo-12gb-32gb-ram-1tb-ssd-gaming-bilgisayar/",
        "imageUrl": "https://images.unsplash.com/photo-1593640408182-31c70c8268f5?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 5 7500F",
        "gpuModel": "ASUS DUAL GeForce RTX 4070 SUPER EVO 12GB",
        "motherboardModel": "ASUS TUF GAMING B650M-E",
        "ramSpec": "32GB (2x16) Corsair Vengeance DDR5 6000MHz CL36",
        "storageSpec": "1TB Kingston KC3000 PCIe 4.0 NVMe M.2 (7000/6000)",
        "psuCaseSpec": "ASUS A21 PLUS ARGB + Corsair RM750e 750W 80+ Gold",
        "fps1080p": 185,
        "fps1440p": 132,
        "fps4k": 76,
        "gamingScore": 91
    },
    {
        "id": "prebuilt_itopya_planet_warrior",
        "name": "PLANET-WARRIOR / Intel Core i5-14400F / MSI GeForce RTX 4060 VENTUS 2X 8GB / 16GB DDR5 / 1TB SSD",
        "storeName": "İtopya",
        "priceTry": 26999.0,
        "url": "https://www.itopya.com/planet-warrior-intel-core-i5-14400f-msi-geforce-rtx-4060-ventus-2x-black-8gb-16gb-ddr5-1tb-nvme-m2-ssd_h25890",
        "imageUrl": "https://images.unsplash.com/photo-1550745165-9bc0b252726f?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "Intel Core i5-14400F",
        "gpuModel": "MSI GeForce RTX 4060 VENTUS 2X 8GB",
        "motherboardModel": "MSI PRO B760M-P DDR5",
        "ramSpec": "16GB (1x16) G.Skill Ripjaws S5 DDR5 5600MHz",
        "storageSpec": "1TB MSI SPATIUM M450 NVMe M.2 SSD",
        "psuCaseSpec": "Thermaltake S200 TG ARGB + Thermaltake 650W 80+",
        "fps1080p": 125,
        "fps1440p": 82,
        "fps4k": 45,
        "gamingScore": 77
    },
    {
        "id": "prebuilt_itopya_modart_pc_pro",
        "name": "MODART-PC-PRO / AMD Ryzen 7 7800X3D / ASUS TUF GeForce RTX 4070 Ti SUPER 16GB / 32GB RAM / 2TB SSD",
        "storeName": "İtopya",
        "priceTry": 69999.0,
        "url": "https://www.itopya.com/modart-pc-pro-amd-ryzen-7-7800x3d-asus-tuf-gaming-geforce-rtx-4070-ti-super-16gb-32gb-ddr5-2tb-m2-ssd_h26120",
        "imageUrl": "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 7 7800X3D",
        "gpuModel": "ASUS TUF Gaming GeForce RTX 4070 Ti SUPER 16GB",
        "motherboardModel": "ASUS ROG STRIX B650E-F GAMING WIFI",
        "ramSpec": "32GB (2x16) G.Skill Flare X5 DDR5 6000MHz CL30",
        "storageSpec": "2TB Samsung 990 PRO NVMe PCIe 4.0 M.2 SSD",
        "psuCaseSpec": "ASUS TUF Gaming GT502 + ASUS TUF 850W 80+ Gold",
        "fps1080p": 220,
        "fps1440p": 165,
        "fps4k": 98,
        "gamingScore": 96
    },
    {
        "id": "prebuilt_sinerji_kraken_9800x3d",
        "name": "Sinerji Kraken / AMD Ryzen 7 9800X3D / MSI GeForce RTX 4080 SUPER 16GB / 32GB DDR5 / 2TB SSD Hazır Sistem",
        "storeName": "Sinerji",
        "priceTry": 89999.0,
        "url": "https://www.sinerji.gen.tr/sinerji-kraken-ryzen-7-9800x3d-32gb-2tb-nvme-m2-ssd-msi-geforce-rtx-4080-super-gaming-x-slim-16gb-p-52890",
        "imageUrl": "https://images.unsplash.com/photo-1587202372634-32705e3bf49c?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 7 9800X3D",
        "gpuModel": "MSI GeForce RTX 4080 SUPER 16G GAMING X SLIM",
        "motherboardModel": "MSI MAG X670E TOMAHAWK WIFI",
        "ramSpec": "32GB (2x16) Kingston Fury Beast DDR5 6000MHz CL30",
        "storageSpec": "2TB Kingston KC3000 Gen4 M.2 SSD (7000/7000)",
        "psuCaseSpec": "MSI MAG FORGE 320R AIRFLOW + MSI MAG A850GL 850W PCIE5",
        "fps1080p": 250,
        "fps1440p": 190,
        "fps4k": 115,
        "gamingScore": 98
    },
    {
        "id": "prebuilt_sinerji_gouki_7800xt",
        "name": "Sinerji Gouki / AMD Ryzen 5 7600X / Gigabyte Radeon RX 7800 XT Gaming OC 16GB / 32GB DDR5 / 1TB SSD",
        "storeName": "Sinerji",
        "priceTry": 44499.0,
        "url": "https://www.sinerji.gen.tr/sinerji-gouki-ryzen-5-7600x-32gb-1tb-nvme-m2-ssd-gigabyte-radeon-rx-7800-xt-gaming-oc-16gb-sistem-tavsiyesi-p-50123",
        "imageUrl": "https://images.unsplash.com/photo-1542751371-adc38448a05e?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 5 7600X",
        "gpuModel": "Gigabyte Radeon RX 7800 XT Gaming OC 16GB",
        "motherboardModel": "Gigabyte B650 Gaming X AX V2",
        "ramSpec": "32GB (2x16) Patriot Viper Venom DDR5 6000MHz CL36",
        "storageSpec": "1TB Kingston KC3000 PCIe 4.0 M.2 SSD",
        "psuCaseSpec": "DeepCool CC560 V2 + DeepCool PK750D 750W 80+ Bronze",
        "fps1080p": 190,
        "fps1440p": 138,
        "fps4k": 78,
        "gamingScore": 92
    },
    {
        "id": "prebuilt_tebilon_alpha_7500f",
        "name": "Tebilon Alpha / AMD Ryzen 5 7500F / Galax GeForce RTX 4060 Ti 8GB 1-Click OC / 16GB RAM / 1TB SSD",
        "storeName": "Tebilon",
        "priceTry": 28499.0,
        "url": "https://www.tebilon.com/tebilon-alpha-amd-ryzen-5-7500f-galax-rtx-4060-ti-1-click-oc-16gb-ddr5-1tb-ssd-gaming-bilgisayar/",
        "imageUrl": "https://images.unsplash.com/photo-1593640495253-23196b27a87f?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 5 7500F",
        "gpuModel": "Galax GeForce RTX 4060 Ti 1-Click OC 8GB",
        "motherboardModel": "ASRock B650M-HDV/M.2",
        "ramSpec": "16GB (2x8) Goodram IRDM DDR5 5600MHz CL30",
        "storageSpec": "1TB Kioxia Exceria Plus G3 Gen4 M.2 SSD",
        "psuCaseSpec": "DeepCool Matrexx 40 3FS + DeepCool PF650 650W 80+",
        "fps1080p": 142,
        "fps1440p": 96,
        "fps4k": 52,
        "gamingScore": 81
    },
    {
        "id": "prebuilt_tebilon_ultra_4080s",
        "name": "Tebilon Ultra / AMD Ryzen 7 7800X3D / Galax GeForce RTX 4080 SUPER SG 16GB / 32GB RAM / 2TB SSD",
        "storeName": "Tebilon",
        "priceTry": 82999.0,
        "url": "https://www.tebilon.com/tebilon-ultra-amd-ryzen-7-7800x3d-galax-rtx-4080-super-sg-32gb-ddr5-2tb-ssd-gaming-bilgisayar/",
        "imageUrl": "https://images.unsplash.com/photo-1587202372775-e229f172b9d7?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 7 7800X3D",
        "gpuModel": "Galax GeForce RTX 4080 SUPER SG 1-Click OC 16GB",
        "motherboardModel": "Gigabyte X870 AORUS ELITE WIFI7",
        "ramSpec": "32GB (2x16) XPG Lancer Blade RGB DDR5 6000MHz CL30",
        "storageSpec": "2TB Kingston KC3000 PCIe 4.0 M.2 SSD",
        "psuCaseSpec": "High Power Elite 850W 80+ Gold PCIe 5.0 + Phanteks XT Pro Ultra",
        "fps1080p": 245,
        "fps1440p": 185,
        "fps4k": 110,
        "gamingScore": 97
    },
    {
        "id": "prebuilt_incehesap_titan_4060",
        "name": "Titan 4060 / AMD Ryzen 5 5600 / ASUS DUAL GeForce RTX 4060 8GB / 16GB RAM / 512GB SSD Hazır Sistem",
        "storeName": "İncehesap",
        "priceTry": 19999.0,
        "url": "https://www.incehesap.com/titan-4060-gaming-pc-fiyati-65120/",
        "imageUrl": "https://images.unsplash.com/photo-1591799264318-7e6ef8ddb7ea?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 5 5600",
        "gpuModel": "ASUS DUAL GeForce RTX 4060 EVO OC 8GB",
        "motherboardModel": "ASUS PRIME B550M-K",
        "ramSpec": "16GB (2x8) Team T-Force Vulcan Z DDR4 3600MHz CL18",
        "storageSpec": "512GB Team MP33 Pro M.2 PCIe SSD",
        "psuCaseSpec": "Cougar Archon 2 Mesh RGB + Cougar 600W 80+",
        "fps1080p": 115,
        "fps1440p": 74,
        "fps4k": 40,
        "gamingScore": 73
    },
    {
        "id": "prebuilt_gaming_gen_ultima_5070",
        "name": "ULTIMA-DUAL5070 / AMD Ryzen 7 7800X3D / ASUS DUAL GeForce RTX 5070 OC 12GB / 32GB RAM / 1TB SSD",
        "storeName": "Gaming.Gen.TR",
        "priceTry": 67999.0,
        "url": "https://www.gaming.gen.tr/urun/750588/ultima-dual5070-amd-ryzen-7-7800x3d-asus-dual-geforce-rtx-5070-oc-12gb-32gb-ram-1tb-m-2-ssd-sistem-tavsiyesi/",
        "imageUrl": "https://images.unsplash.com/photo-1593640408182-31c70c8268f5?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 7 7800X3D",
        "gpuModel": "ASUS DUAL GeForce RTX 5070 OC 12GB",
        "motherboardModel": "ASUS PRIME B850M-K",
        "ramSpec": "32GB (2x16) Kingston Fury Beast DDR5 6000MHz CL30",
        "storageSpec": "1TB Kingston KC3000 Gen4 M.2 SSD (7000/6000)",
        "psuCaseSpec": "ASUS TUF GT302 ARGB + ASUS TUF 850W Gold PCIe 5.0",
        "fps1080p": 225,
        "fps1440p": 172,
        "fps4k": 102,
        "gamingScore": 96
    },
    {
        "id": "prebuilt_itopya_wtcn_5090",
        "name": "WTCN-APEX / AMD Ryzen 7 9800X3D / MSI GeForce RTX 5090 SUPRIM LIQUID 32GB / 64GB DDR5 / 4TB SSD",
        "storeName": "İtopya",
        "priceTry": 179999.0,
        "url": "https://www.itopya.com/wtcn-apex-amd-ryzen-7-9800x3d-msi-geforce-rtx-5090-suprim-liquid-32gb-64gb-ddr5-4tb-m2-ssd_h29025",
        "imageUrl": "https://images.unsplash.com/photo-1587202372634-32705e3bf49c?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 7 9800X3D",
        "gpuModel": "MSI GeForce RTX 5090 SUPRIM LIQUID 32GB",
        "motherboardModel": "MSI MEG X870E GODLIKE",
        "ramSpec": "64GB (2x32) G.Skill Trident Z5 Neo RGB DDR5 6400MHz CL32",
        "storageSpec": "4TB (2x2TB) Samsung 990 PRO NVMe PCIe 4.0 M.2 SSD",
        "psuCaseSpec": "Lian Li O11 Dynamic EVO XL + Seasonic Prime 1300W Titanium ATX 3.0",
        "fps1080p": 320,
        "fps1440p": 260,
        "fps4k": 180,
        "gamingScore": 100
    }
]

CURATED_LAPTOPS = [
    {
        "id": "laptop_asus_rog_strix_g16_g614jir",
        "name": "ASUS ROG Strix G16 G614JIR-N4003",
        "brand": "ASUS",
        "priceTry": 78999.0,
        "url": "https://www.hepsiburada.com/asus-rog-strix-g16-g614jir-n4003-intel-core-i9-14900hx-32gb-1tb-ssd-rtx4080-freedos-16-240hz-tasinabilir-bilgisayar-pm-HBC00005V189Y",
        "imageUrl": "https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "Intel Core i9-14900HX (24 Çekirdek, 5.80 GHz)",
        "gpuModel": "NVIDIA GeForce RTX 4080 Laptop 12GB GDDR6",
        "gpuWattTgp": "175W Full Power (150W + 25W Dynamic Boost)",
        "muxSwitch": "MUX Switch + NVIDIA Advanced Optimus",
        "ramSpec": "32GB (2x16) DDR5-5600MHz Çift Kanal",
        "storageSpec": "1TB PCIe Gen4 NVMe M.2 SSD (7000 MB/s)",
        "displaySpec": "16 inç 2.5K WQXGA (2560x1600) 240Hz 3ms ROG Nebula IPS %100 DCI-P3 500 Nit G-Sync",
        "batteryWh": 90,
        "weightKg": 2.50,
        "coolingTech": "Tri-Fan Teknolojisi + Sıvı Metal Termal İletken (Conductonaut)",
        "gamingScore": 95,
        "fps1080p": 175,
        "fps1440p": 135
    },
    {
        "id": "laptop_lenovo_legion_pro_7_16irx9h",
        "name": "Lenovo Legion Pro 7 16IRX9H (83DE001CTR)",
        "brand": "Lenovo",
        "priceTry": 114999.0,
        "url": "https://www.hepsiburada.com/lenovo-legion-pro-7-intel-core-i9-14900hx-32gb-2tb-ssd-rtx4090-freedos-16-wqxga-tasinabilir-bilgisayar-83de001ctr-pm-HBC000063R89Q",
        "imageUrl": "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "Intel Core i9-14900HX (24 Çekirdek / 32 İzlek)",
        "gpuModel": "NVIDIA GeForce RTX 4090 Laptop 16GB GDDR6",
        "gpuWattTgp": "175W Full Power (Maksimum Grafik Gücü)",
        "muxSwitch": "MUX Switch + Advanced Optimus (Lenovo AI Engine+)",
        "ramSpec": "32GB (2x16) DDR5-5600MHz",
        "storageSpec": "2TB (2x1TB RAID 0) PCIe 4.0 NVMe SSD",
        "displaySpec": "16 inç WQXGA (2560x1600) 240Hz IPS 500 Nit HDR400 %100 DCI-P3 3ms",
        "batteryWh": 99,
        "weightKg": 2.62,
        "coolingTech": "Legion Coldfront 5.0 Buhar Odası (Vapor Chamber) + Çift Fanlı Sıvı Kristal Polimer",
        "gamingScore": 99,
        "fps1080p": 210,
        "fps1440p": 168
    },
    {
        "id": "laptop_asus_tuf_a15_fa507uv",
        "name": "ASUS TUF Gaming A15 FA507UV-LP014",
        "brand": "ASUS",
        "priceTry": 39499.0,
        "url": "https://www.hepsiburada.com/asus-tuf-gaming-a15-fa507uv-lp014-amd-ryzen-7-8845hs-16gb-512gb-ssd-rtx4060-freedos-15-6-fhd-tasinabilir-bilgisayar-pm-HBC000062G12A",
        "imageUrl": "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 7 8845HS (8 Çekirdek, Ryzen AI NPU)",
        "gpuModel": "NVIDIA GeForce RTX 4060 Laptop 8GB GDDR6",
        "gpuWattTgp": "140W Dynamic Boost (115W + 25W)",
        "muxSwitch": "MUX Switch + NVIDIA Advanced Optimus",
        "ramSpec": "16GB (2x8) DDR5-5600MHz",
        "storageSpec": "512GB PCIe Gen4 M.2 NVMe SSD",
        "displaySpec": "15.6 inç FHD (1920x1080) 144Hz IPS %100 sRGB G-Sync Adaptive Sync",
        "batteryWh": 90,
        "weightKg": 2.20,
        "coolingTech": "84 Bıçaklı Arc Flow Fanlar + 5 Isı Borulu Çift Egzos",
        "gamingScore": 84,
        "fps1080p": 118,
        "fps1440p": 78
    },
    {
        "id": "laptop_lenovo_loq_15irx9",
        "name": "Lenovo LOQ 15IRX9 (83DV00HTTR)",
        "brand": "Lenovo",
        "priceTry": 32999.0,
        "url": "https://www.hepsiburada.com/lenovo-loq-15irx9-intel-core-i5-13450hx-16gb-512gb-ssd-rtx4060-freedos-15-6-fhd-tasinabilir-bilgisayar-83dv00httr-pm-HBC000068809B",
        "imageUrl": "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "Intel Core i5-13450HX (10 Çekirdek, 4.60 GHz)",
        "gpuModel": "NVIDIA GeForce RTX 4060 Laptop 8GB GDDR6",
        "gpuWattTgp": "115W Dynamic Boost",
        "muxSwitch": "MUX Switch + Advanced Optimus",
        "ramSpec": "16GB (1x16) DDR5-4800MHz",
        "storageSpec": "512GB PCIe Gen4 NVMe M.2 SSD",
        "displaySpec": "15.6 inç FHD (1920x1080) 144Hz IPS %100 sRGB 300 Nit G-Sync",
        "batteryWh": 60,
        "weightKg": 2.38,
        "coolingTech": "Hyperchamber Soğutma Mimarisi + İçe Üfleyen Çift Fan",
        "gamingScore": 81,
        "fps1080p": 105,
        "fps1440p": 68
    },
    {
        "id": "laptop_acer_nitro_16_an16_41",
        "name": "Acer Nitro 16 AN16-41 (NH.QLKEY.001)",
        "brand": "Acer",
        "priceTry": 37499.0,
        "url": "https://www.hepsiburada.com/acer-nitro-16-an16-41-amd-ryzen-7-7840hs-16gb-512gb-ssd-rtx4060-freedos-16-wuxga-165hz-tasinabilir-bilgisayar-nh-qlkey-001-pm-HBC00004QNBD4",
        "imageUrl": "https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "AMD Ryzen 7 7840HS (8 Çekirdek, 5.10 GHz)",
        "gpuModel": "NVIDIA GeForce RTX 4060 Laptop 8GB GDDR6",
        "gpuWattTgp": "140W Full Power (Maksimum 140W)",
        "muxSwitch": "MUX Switch + NVIDIA Advanced Optimus",
        "ramSpec": "16GB (2x8) DDR5-5600MHz",
        "storageSpec": "512GB PCIe Gen4 M.2 NVMe SSD",
        "displaySpec": "16 inç WUXGA (1920x1200) 16:10 165Hz IPS %100 sRGB 400 Nit G-Sync",
        "batteryWh": 90,
        "weightKg": 2.70,
        "coolingTech": "Sıvı Metal Termal Macun + Çift Fanlı 4 Çıkışlı NitroSense Soğutma",
        "gamingScore": 85,
        "fps1080p": 122,
        "fps1440p": 80
    },
    {
        "id": "laptop_msi_raider_ge68_hx_14vgg",
        "name": "MSI Raider GE68 HX 14VGG-258TR",
        "brand": "MSI",
        "priceTry": 89999.0,
        "url": "https://www.hepsiburada.com/msi-raider-ge68-hx-14vgg-258tr-intel-core-i9-14900hx-32gb-1tb-ssd-rtx4070-windows-11-home-16-qhd-tasinabilir-bilgisayar-pm-HBC00005S3A8B",
        "imageUrl": "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "Intel Core i9-14900HX (24 Çekirdek / 32 İzlek)",
        "gpuModel": "NVIDIA GeForce RTX 4070 Laptop 8GB GDDR6",
        "gpuWattTgp": "140W Full Power Dynamic Boost",
        "muxSwitch": "MUX Switch (MSI Discrete Graphics Mode)",
        "ramSpec": "32GB (2x16) DDR5-5600MHz",
        "storageSpec": "1TB NVMe PCIe Gen4 M.2 SSD",
        "displaySpec": "16 inç QHD+ (2560x1600) 240Hz IPS %100 DCI-P3 500 Nit",
        "batteryWh": 99,
        "weightKg": 2.70,
        "coolingTech": "Cooler Boost 5 (2 Fan, 6 Geniş Isı Borusu)",
        "gamingScore": 91,
        "fps1080p": 155,
        "fps1440p": 115
    },
    {
        "id": "laptop_hp_victus_16_r1000",
        "name": "HP Victus 16-r1000nt (9J218EA)",
        "brand": "HP",
        "priceTry": 36999.0,
        "url": "https://www.hepsiburada.com/hp-victus-16-r1000nt-intel-core-i7-14650hx-16gb-1tb-ssd-rtx4060-freedos-16-1-fhd-tasinabilir-bilgisayar-9j218ea-pm-HBC000062P7Y4",
        "imageUrl": "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "Intel Core i7-14650HX (16 Çekirdek, 5.20 GHz)",
        "gpuModel": "NVIDIA GeForce RTX 4060 Laptop 8GB GDDR6",
        "gpuWattTgp": "120W Dynamic Boost",
        "muxSwitch": "MUX Switch (OMEN Gaming Hub)",
        "ramSpec": "16GB (2x8) DDR5-5600MHz",
        "storageSpec": "1TB PCIe Gen4 NVMe M.2 SSD",
        "displaySpec": "16.1 inç FHD (1920x1080) 144Hz IPS %100 sRGB 300 Nit",
        "batteryWh": 70,
        "weightKg": 2.30,
        "coolingTech": "OMEN Tempest Cooling Termal Çözümü",
        "gamingScore": 83,
        "fps1080p": 112,
        "fps1440p": 72
    },
    {
        "id": "laptop_asus_tuf_f16_5060",
        "name": "ASUS TUF Gaming F16 FX608JHR (RTX 5060)",
        "brand": "ASUS",
        "priceTry": 45999.0,
        "url": "https://www.gaming.gen.tr/urun/812040/asus-tuf-gaming-f16-intel-core-i7-14650hx-16gb-1tb-ssd-rtx5060-16-inc-165hz-gaming-laptop/",
        "imageUrl": "https://images.unsplash.com/photo-1603302576837-37561b2e2302?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "Intel Core i7-14650HX (16 Çekirdek, 5.20 GHz)",
        "gpuModel": "NVIDIA GeForce RTX 5060 Laptop 8GB GDDR7",
        "gpuWattTgp": "115W Dynamic Boost (Blackwell Mimarisi)",
        "muxSwitch": "MUX Switch + NVIDIA Advanced Optimus",
        "ramSpec": "16GB (2x8) DDR5-5600MHz",
        "storageSpec": "1TB PCIe Gen4 M.2 NVMe SSD",
        "displaySpec": "16 inç WUXGA 165Hz IPS %100 sRGB G-Sync",
        "batteryWh": 90,
        "weightKg": 2.25,
        "coolingTech": "Arc Flow Fanlar 2.0 + Sıvı Metal Termal Arabirim",
        "gamingScore": 88,
        "fps1080p": 138,
        "fps1440p": 94
    },
    {
        "id": "laptop_asus_rog_strix_scar_18_5090",
        "name": "ASUS ROG Strix SCAR 18 (G834JYR - RTX 5090)",
        "brand": "ASUS",
        "priceTry": 149999.0,
        "url": "https://www.itopya.com/asus-rog-strix-scar-18-intel-core-i9-14900hx-64gb-4tb-ssd-rtx5090-18-inc-240hz-mini-led_u38990",
        "imageUrl": "https://images.unsplash.com/photo-1541807084-5c52b6b3adef?auto=format&fit=crop&w=600&q=80",
        "cpuModel": "Intel Core i9-14900HX (24 Çekirdek / 32 İzlek, 5.80 GHz)",
        "gpuModel": "NVIDIA GeForce RTX 5090 Laptop 24GB GDDR7",
        "gpuWattTgp": "175W Full Power (150W + 25W Dynamic Boost)",
        "muxSwitch": "MUX Switch + NVIDIA Advanced Optimus",
        "ramSpec": "64GB (2x32) DDR5-5600MHz",
        "storageSpec": "4TB (2x2TB RAID 0) PCIe 4.0 NVMe SSD",
        "displaySpec": "18 inç 2.5K WQXGA 240Hz Mini LED Nebula HDR 1100 Nit G-Sync",
        "batteryWh": 99,
        "weightKg": 3.10,
        "coolingTech": "Tri-Fan + Tam Boy Radyatör + Conductonaut Extreme Sıvı Metal",
        "gamingScore": 100,
        "fps1080p": 240,
        "fps1440p": 195
    }
]

# -----------------------------------------------------------------------------
# 5. FİYAT GEÇMİŞİ VE HESAPLAMA MOTORU
# -----------------------------------------------------------------------------

def estimate_component_price(text: str, prices_map: dict) -> float | None:
    if not text:
        return None
    lower = text.lower()
    for key in sorted(COMPONENT_ESTIMATE_FALLBACKS.keys(), key=len, reverse=True):
        if key in lower:
            return COMPONENT_ESTIMATE_FALLBACKS[key]

    for pid, pdata in prices_map.items():
        cur = pdata.get("currentPriceTry")
        if cur and cur > 0:
            clean_pid = pid.replace("_", " ").lower()
            tokens = [t for t in lower.split() if len(t) >= 4 and t not in ["asus", "msi", "gigabyte", "dual", "ventus", "gaming", "prime", "tuf"]]
            if len(tokens) >= 2 and all(t in clean_pid for t in tokens[:2]):
                return cur

    if "ddr5" in lower and "32gb" in lower: return 4200.0
    if "ddr5" in lower: return 2200.0
    if "ddr4" in lower and "32gb" in lower: return 2500.0
    if "ddr4" in lower: return 1400.0
    if "2tb" in lower: return 5200.0
    if "1tb" in lower: return 2650.0
    if "512gb" in lower or "500gb" in lower or "480gb" in lower: return 1450.0
    if "1000w" in lower: return 8500.0
    if "850w" in lower: return 6200.0
    if "750w" in lower: return 4600.0
    if "650w" in lower: return 3400.0
    if "550w" in lower or "500w" in lower: return 2600.0
    return None

def upsert_daily_cheapest_history(
    existing_history: list,
    today_str: str,
    cheapest_price: float,
    cheapest_store: str,
    min_floor_try: float = 15000.0,
) -> list:
    by_date = {}
    for point in existing_history or []:
        if not isinstance(point, dict):
            continue
        d_raw = str(point.get("date") or point.get("timestamp") or "")[:10]
        p_raw = point.get("priceTry") if point.get("priceTry") is not None else point.get("price")
        try:
            p_val = float(p_raw)
        except (TypeError, ValueError):
            continue
        if len(d_raw) != 10 or p_val < min_floor_try:
            continue
        prev = by_date.get(d_raw)
        if prev is None or p_val < prev["priceTry"]:
            by_date[d_raw] = {
                "date": d_raw,
                "timestamp": d_raw,
                "price": round(p_val, 2),
                "priceTry": round(p_val, 2),
                "storeName": point.get("storeName") or cheapest_store,
            }

    if cheapest_price >= min_floor_try:
        by_date[today_str] = {
            "date": today_str,
            "timestamp": today_str,
            "price": round(cheapest_price, 2),
            "priceTry": round(cheapest_price, 2),
            "storeName": cheapest_store,
        }

    sorted_dates = sorted(by_date.keys())[-90:]
    return [by_date[d] for d in sorted_dates]

# -----------------------------------------------------------------------------
# 6. ANA ÇALIŞTIRICI (MAIN)
# -----------------------------------------------------------------------------

def main():
    parser = argparse.ArgumentParser(description="Hardware Wizard - Prebuilt PC & Gaming Laptop Sync")
    parser.add_argument("--max-pages", type=int, default=8, help="Kategori başına taranacak maksimum sayfa sayısı (default: 8)")
    parser.add_argument("--skip-live-crawl", action="store_true", help="Yalnızca doğrulanmış genişletilmiş kataloğu derle")
    args = parser.parse_args()

    print("================================================================================")
    print("   HARDWARE WIZARD - OEM HAZIR SİSTEMLER & GAMING LAPTOP SENKRONİZASYONU        ")
    print(f"   Max Pages: {args.max_pages} | Live Crawl: {not args.skip_live_crawl}       ")
    print("================================================================================")

    # 1. Mevcut prices_tr.json oku
    existing_prices = {}
    main_price_file = TARGET_PRICE_FILES[0]
    if os.path.exists(main_price_file):
        with open(main_price_file, "r", encoding="utf-8") as f:
            existing_prices = json.load(f)
    print(f"[+] Mevcut tekil donanım fiyat kataloğu yüklendi: {len(existing_prices)} ürün.")

    # 2. Canlı Tarama (Scraping)
    scraped_prebuilts = []
    scraped_laptops = []

    if not args.skip_live_crawl:
        print("\n[+] 1. Adım: Mağaza kategori sayfaları taranıyor (max_pages = %d)..." % args.max_pages)
        tasks = [
            ("Gaming.Gen.TR Sistemler", scrape_gaming_gen_systems),
            ("İtopya Sistemler", scrape_itopya_systems),
            ("Tebilon Sistemler", scrape_tebilon_systems),
            ("İncehesap Sistemler", scrape_incehesap_systems),
            ("Vatan Bilgisayar Sistemler", scrape_vatan_systems),
            ("Gaming.Gen.TR Laptoplar", scrape_gaming_gen_laptops),
            ("İtopya Laptoplar", scrape_itopya_laptops),
            ("Hepsiburada Laptoplar", scrape_hepsiburada_laptops),
        ]
        with ThreadPoolExecutor(max_workers=4) as executor:
            future_to_name = {executor.submit(fn, args.max_pages): name for name, fn in tasks}
            for fut in as_completed(future_to_name):
                name = future_to_name[fut]
                try:
                    res = fut.result()
                    if "Sistemler" in name:
                        scraped_prebuilts.extend(res)
                    else:
                        scraped_laptops.extend(res)
                    print(f"    -> [{name}] Tamamlandı: {len(res)} ürün çekildi.")
                except Exception as ex:
                    print(f"    -> [{name}] Hata: {ex}")

    # 3. Curated Base ile Birleştir ve Deduplicate Et
    all_prebuilts = {}
    for p in CURATED_PREBUILTS:
        all_prebuilts[p["id"]] = p
    for p in scraped_prebuilts:
        existing_key = None
        for k, v in all_prebuilts.items():
            if v.get("url") and p.get("url") and v["url"].strip() == p["url"].strip():
                existing_key = k
                break
        if not existing_key:
            all_prebuilts[p["id"]] = p

    all_laptops = {}
    existing_lp_path = os.path.join(CATALOG_DIR, "laptop.json")
    if os.path.exists(existing_lp_path):
        try:
            with open(existing_lp_path, "r", encoding="utf-8") as f:
                loaded = json.load(f)
                for lp in loaded:
                    all_laptops[lp["id"]] = lp
        except Exception:
            pass
    if not all_laptops:
        for lp in CURATED_LAPTOPS:
            all_laptops[lp["id"]] = lp
    for lp in scraped_laptops:
        existing_key = None
        for k, v in all_laptops.items():
            if v.get("url") and lp.get("url") and v["url"].strip() == lp["url"].strip():
                existing_key = k
                break
        if not existing_key:
            all_laptops[lp["id"]] = lp
        else:
            # Preserve existing multi-angle images
            if all_laptops[existing_key].get("images"):
                lp["images"] = all_laptops[existing_key]["images"]
            all_laptops[existing_key].update(lp)

    print(f"\n[+] Toplam Birleştirilmiş Veri: {len(all_prebuilts)} Hazır Sistem, {len(all_laptops)} Gaming Laptop.")

    # 4. Hazır Sistem Parça Parça Maliyet ve Kâr/Zarar Analizi
    today_str = datetime.now().strftime("%Y-%m-%d")
    final_prebuilts = []

    print("\n[+] 2. Adım: Hazır Sistem Parça Maliyet ve Kâr/Fark Analizi yapılıyor...")
    raw_prebuilts = []
    for pid, ps in all_prebuilts.items():
        # Enforce Pure Name Rule: Store + Model Code
        clean_name = clean_system_title(ps.get("name", ""), ps.get("url", ""), ps.get("storeName", ""))
        ps["name"] = clean_name

        cpu_cost = estimate_component_price(ps["cpuModel"], existing_prices)
        gpu_cost = estimate_component_price(ps["gpuModel"], existing_prices)
        mobo_cost = estimate_component_price(ps["motherboardModel"], existing_prices) or 3500.0
        ram_cost = estimate_component_price(ps["ramSpec"], existing_prices) or 2200.0
        ssd_cost = estimate_component_price(ps["storageSpec"], existing_prices) or 2000.0
        case_psu_cost = estimate_component_price(ps["psuCaseSpec"], existing_prices) or 3500.0

        system_price = float(ps["priceTry"])
        analyzed = dict(ps)

        if cpu_cost is not None and gpu_cost is not None:
            total_sep = round(cpu_cost + gpu_cost + mobo_cost + ram_cost + ssd_cost + case_psu_cost, 2)
            adv_try = round(total_sep - system_price, 2)
            analyzed["estimatedSeparatePriceTry"] = total_sep
            if adv_try > 0.0:
                analyzed["priceAdvantageTry"] = adv_try
            else:
                # Prevent nonsensical negative profit
                analyzed["priceAdvantageTry"] = None
        else:
            analyzed["estimatedSeparatePriceTry"] = None
            analyzed["priceAdvantageTry"] = None

        raw_prebuilts.append(analyzed)

    def _enrich_prebuilt_img(pb: dict) -> dict:
        cur_img = pb.get("imageUrl")
        if not cur_img or "placeholder" in cur_img.lower() or "unsplash" in cur_img.lower():
            clean_name = pb.get("name", "")
            q = f"{clean_name} gaming desktop case isolated"
            for u in search_bing_images(q, count=4):
                if verify_image_url(u, timeout=2.5):
                    pb["imageUrl"] = u
                    return pb
            for u in search_duckduckgo_images(f"{clean_name} pc case"):
                if verify_image_url(u, timeout=2.5):
                    pb["imageUrl"] = u
                    return pb
        return pb

    print("    -> Hazır sistem görselleri paralel taranıyor...")
    with ThreadPoolExecutor(max_workers=8) as executor:
        final_prebuilts = list(executor.map(_enrich_prebuilt_img, raw_prebuilts))

    for pb in final_prebuilts:
        pid = pb["id"]
        system_price = float(pb["priceTry"])
        prev_entry = existing_prices.get(pid, {})
        history = upsert_daily_cheapest_history(
            existing_history=prev_entry.get("priceHistory", []),
            today_str=today_str,
            cheapest_price=system_price,
            cheapest_store=pb["storeName"],
            min_floor_try=15000.0,
        )
        all_hist = [h["priceTry"] for h in history] or [system_price]
        existing_prices[pid] = {
            "productId": pid,
            "currentPriceTry": system_price,
            "lowestPriceTry": min(all_hist),
            "highestPriceTry": max(all_hist),
            "lastUpdated": datetime.now().isoformat(),
            "imageUrl": pb.get("imageUrl"),
            "priceHistory": history,
            "offers": [
                {
                    "storeName": pb["storeName"],
                    "priceTry": system_price,
                    "url": pb["url"],
                    "inStock": True,
                    "imageUrl": pb.get("imageUrl")
                }
            ]
        }

    # 5. Gaming Laptop Fiyat Geçmişi ve Güncellemesi
    print("\n[+] 3. Adım: Gaming Laptop modelleri yapılandırılıyor...")
    raw_laptops = []
    for pid, lp in all_laptops.items():
        price = float(lp["priceTry"])
        store_name = "Monster Notebook" if "monsternotebook.com.tr" in lp["url"] else (
            "İtopya" if "itopya.com" in lp["url"] else (
                "Gaming.Gen.TR" if "gaming.gen.tr" in lp["url"] else "Hepsiburada"
            )
        )
        analyzed_lp = dict(lp)
        analyzed_lp["_storeName"] = store_name
        raw_laptops.append(analyzed_lp)

    def _enrich_laptop_img(lp: dict) -> dict:
        cur_img = lp.get("imageUrl")
        if not cur_img or "placeholder" in cur_img.lower() or "unsplash" in cur_img.lower():
            brand = lp.get("brand", "")
            name = lp.get("name", "")
            q = f"{brand} {name} gaming laptop white background"
            for u in search_bing_images(q, count=4):
                if verify_image_url(u, timeout=2.5):
                    lp["imageUrl"] = u
                    return lp
            for u in search_duckduckgo_images(f"{brand} {name} laptop studio"):
                if verify_image_url(u, timeout=2.5):
                    lp["imageUrl"] = u
                    return lp
        return lp

    print("    -> Laptop stüdyo görselleri paralel taranıyor...")
    with ThreadPoolExecutor(max_workers=8) as executor:
        final_laptops = list(executor.map(_enrich_laptop_img, raw_laptops))

    for lp in final_laptops:
        pid = lp["id"]
        price = float(lp["priceTry"])
        store_name = lp.pop("_storeName", "Hepsiburada")
        prev_entry = existing_prices.get(pid, {})
        history = upsert_daily_cheapest_history(
            existing_history=prev_entry.get("priceHistory", []),
            today_str=today_str,
            cheapest_price=price,
            cheapest_store=store_name,
            min_floor_try=18000.0,
        )
        all_hist = [h["priceTry"] for h in history] or [price]
        existing_prices[pid] = {
            "productId": pid,
            "currentPriceTry": price,
            "lowestPriceTry": min(all_hist),
            "highestPriceTry": max(all_hist),
            "lastUpdated": datetime.now().isoformat(),
            "imageUrl": lp.get("imageUrl"),
            "priceHistory": history,
            "offers": [
                {
                    "storeName": store_name,
                    "priceTry": price,
                    "url": lp["url"],
                    "inStock": True,
                    "imageUrl": lp.get("imageUrl")
                }
            ]
        }

    # 6. Dosyaları Kaydet (data/catalog/ ve shared/.../composeResources/files/)
    print("\n[+] 4. Adım: Dosyalar senkronize ediliyor...")
    targets = [
        ("prebuilt-pc.json", final_prebuilts),
        ("laptop.json", final_laptops),
    ]
    for fname, data in targets:
        for d in [CATALOG_DIR, FILES_DIR]:
            os.makedirs(d, exist_ok=True)
            p = os.path.join(d, fname)
            with open(p, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            print(f"  [OK] {os.path.basename(d)}/{fname} kaydedildi ({len(data)} ürün).")

    for p_file in TARGET_PRICE_FILES:
        os.makedirs(os.path.dirname(p_file), exist_ok=True)
        with open(p_file, "w", encoding="utf-8") as f:
            json.dump(existing_prices, f, indent=2, ensure_ascii=False)
        print(f"  [OK] {os.path.basename(p_file)} güncellendi ({len(existing_prices)} toplam ürün).")

    print("\n================================================================================")
    print(f"   BAŞARILI: {len(final_prebuilts)} Hazır Sistem & {len(final_laptops)} Gaming Laptop Eşitlendi! ")
    print("================================================================================")

if __name__ == "__main__":
    main()
