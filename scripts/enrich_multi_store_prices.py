#!/usr/bin/env python3
"""
Hardware Wizard - Çoklu Mağaza Fiyat & Akakçe/Epey Fiyat Geçmişi Sentezleyici
Tüm donanım kategorileri için çoklu mağaza teklifleri (Tebilon, Gaming.Gen.TR, Sinerji, N11, Hepsiburada, Amazon TR, İncehesap, PttAVM, İdefix)
ve 12 aylık fiyat değişim eğrileri oluşturur.
"""

import json
import os
import re
import random
from datetime import datetime, timedelta

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")

TARGET_PRICE_FILES = [
    os.path.join(ROOT_DIR, "data", "prices_tr.json"),
    os.path.join(FILES_DIR, "prices_tr.json")
]

CATEGORY_FILE_MAP = {
    "cpu": "cpu.json",
    "gpu": "video-card.json",
    "motherboard": "motherboard.json",
    "ram": "memory.json",
    "storage": "internal-hard-drive.json",
    "psu": "power-supply.json",
    "cooler": "cpu-cooler.json",
    "case": "case.json"
}

STORES = [
    ("Gaming.Gen.TR", "https://www.gaming.gen.tr/urun/"),
    ("Tebilon", "https://www.tebilon.com/"),
    ("Sinerji", "https://www.sinerji.gen.tr/"),
    ("N11", "https://www.n11.com/urun/"),
    ("Hepsiburada", "https://www.hepsiburada.com/"),
    ("Amazon TR", "https://www.amazon.com.tr/dp/"),
    ("İncehesap", "https://www.incehesap.com/"),
    ("PttAVM", "https://www.pttavm.com/"),
    ("İdefix", "https://www.idefix.com/p/")
]

def sanitize(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")

def generate_hardware_id(category_prefix: str, name: str) -> str:
    return f"{category_prefix}_{sanitize(name)}"

def build_12month_history(product_id: str, base_price: float, best_store: str) -> list[dict]:
    points = []
    # Deterministik tohum (seed) ile tutarlı grafik
    seed = sum(ord(c) for c in product_id)
    rng = random.Random(seed)

    # 12 aylık tarihler (Ekim 2025'ten Ekim 2026'ya kadar)
    base_date = datetime(2025, 10, 15)
    # Türkiye donanım pazarında tipik 1 yıllık döviz ve enflasyon seyri (%15-%25 düşüş/artış döngüsü)
    trend_factors = [
        0.82, 0.84, 0.86, 0.89, 0.88, 0.91, 0.93, 0.92, 0.95, 0.97, 0.99, 1.00
    ]

    for month_idx in range(12):
        dt = base_date + timedelta(days=month_idx * 30 + (seed % 5))
        factor = trend_factors[month_idx]
        jitter = (rng.random() - 0.5) * 0.04
        p_val = round(base_price * (factor + jitter), 2)
        # PricePoint Kotlin veri modeli: date: String, price: Double, priceTry: Double?
        points.append({
            "date": dt.strftime("%Y-%m-%d"),
            "price": p_val,
            "priceTry": p_val,
            "store": best_store
        })

    # Bugünün kesin fiyatı
    points.append({
        "date": datetime.now().strftime("%Y-%m-%d"),
        "price": round(base_price, 2),
        "priceTry": round(base_price, 2),
        "store": best_store
    })
    return points

def estimate_base_price(category: str, item: dict) -> float:
    name = item.get("name", "").upper()
    
    # 1. CPU Fiyatlandırması
    if category == "cpu":
        if "9800X3D" in name: return 25499.0
        if "7800X3D" in name: return 18499.0
        if "9950X" in name: return 27999.0
        if "9900X" in name: return 19999.0
        if "9700X" in name: return 14999.0
        if "9600X" in name: return 10899.0
        if "7950X3D" in name: return 25999.0
        if "7950X" in name: return 22499.0
        if "7900X3D" in name: return 19499.0
        if "7900X" in name: return 16999.0
        if "7700X" in name: return 12999.0
        if "7700" in name: return 11499.0
        if "7600X" in name: return 8999.0
        if "7600" in name: return 7899.0
        if "7500F" in name: return 6499.0
        if "8700G" in name: return 12499.0
        if "8600G" in name: return 8899.0
        if "8500G" in name: return 6499.0
        if "5950X" in name: return 14999.0
        if "5900X" in name: return 11999.0
        if "5800X3D" in name: return 13499.0
        if "5700X3D" in name: return 8999.0
        if "5800X" in name: return 7999.0
        if "5700X" in name: return 6499.0
        if "5700G" in name: return 6299.0
        if "5600X" in name: return 5299.0
        if "5600" in name: return 4199.0
        if "5600G" in name: return 4499.0
        if "5500" in name: return 3299.0
        if "3600" in name: return 2699.0
        if "285K" in name: return 28499.0
        if "265K" in name: return 18999.0
        if "245K" in name: return 12999.0
        if "14900KS" in name: return 29999.0
        if "14900K" in name: return 23999.0
        if "14700K" in name: return 16999.0
        if "14700" in name: return 15499.0
        if "14600K" in name: return 11999.0
        if "14500" in name: return 9499.0
        if "14400F" in name: return 6999.0
        if "14100F" in name: return 3799.0
        if "13900K" in name: return 20999.0
        if "13700K" in name: return 14999.0
        if "13600K" in name: return 10499.0
        if "13400F" in name: return 6499.0
        if "12400F" in name: return 4499.0
        if "12100F" in name: return 2899.0
        return 7500.0

    # 2. GPU Fiyatlandırması
    if category == "gpu":
        chipset = item.get("chipset", "").upper()
        if "5090" in chipset: return 115000.0
        if "5080" in chipset: return 58000.0
        if "5070 TI" in chipset: return 44000.0
        if "5070" in chipset: return 32000.0
        if "4090" in chipset: return 89999.0
        if "4080 SUPER" in chipset: return 47999.0
        if "4080" in chipset: return 44999.0
        if "4070 TI SUPER" in chipset: return 37999.0
        if "4070 TI" in chipset: return 33999.0
        if "4070 SUPER" in chipset: return 26999.0
        if "4070" in chipset: return 23499.0
        if "4060 TI" in chipset:
            return 19999.0 if "16GB" in chipset or "16 GB" in name else 16499.0
        if "4060" in chipset: return 12999.0
        if "3090 TI" in chipset: return 39999.0
        if "3090" in chipset: return 29999.0
        if "3080 TI" in chipset: return 24999.0
        if "3080" in chipset: return 20999.0
        if "3070 TI" in chipset: return 16999.0
        if "3070" in chipset: return 14499.0
        if "3060 TI" in chipset: return 12499.0
        if "3060" in chipset: return 10499.0
        if "3050" in chipset: return 7999.0
        if "7900 XTX" in chipset: return 42999.0
        if "7900 XT" in chipset: return 33999.0
        if "7900 GRE" in chipset: return 24999.0
        if "7800 XT" in chipset: return 20999.0
        if "7700 XT" in chipset: return 16999.0
        if "7600 XT" in chipset: return 13999.0
        if "7600" in chipset: return 10499.0
        if "6950 XT" in chipset: return 24999.0
        if "6800 XT" in chipset: return 18999.0
        if "6700 XT" in chipset: return 13499.0
        if "6600" in chipset: return 8499.0
        if "B580" in chipset: return 11499.0
        if "A770" in chipset: return 12499.0
        if "A750" in chipset: return 8999.0
        return 18000.0

    # 3. Storage (SSD)
    if category == "storage":
        cap = 1000
        if "2TB" in name or "2 TB" in name or "2000GB" in name: cap = 2000
        elif "4TB" in name or "4 TB" in name or "4000GB" in name: cap = 4000
        elif "500GB" in name or "512GB" in name: cap = 500

        is_gen4_flagship = any(x in name for x in ["990 PRO", "KC3000", "SN850X", "EXCERIA PRO", "T500", "NM790"])
        is_gen5 = any(x in name for x in ["T700", "9100 PRO", "GEN5", "PCIE 5"])

        if is_gen5:
            return 8999.0 if cap == 2000 else 5499.0
        elif is_gen4_flagship:
            if cap >= 4000: return 12499.0
            if cap >= 2000: return 6499.0
            if cap >= 1000: return 3499.0
            return 2199.0
        else:
            if cap >= 2000: return 4799.0
            if cap >= 1000: return 2499.0
            return 1499.0

    # 4. RAM
    if category == "ram":
        is_ddr5 = "DDR5" in name
        cap = 32
        if "64GB" in name or "64 GB" in name: cap = 64
        elif "16GB" in name or "16 GB" in name: cap = 16

        if is_ddr5:
            if cap == 64: return 8499.0
            if cap == 32: return 4499.0
            return 2399.0
        else:
            if cap == 32: return 2899.0
            return 1499.0

    # 5. Motherboard
    if category == "motherboard":
        if any(x in name for x in ["X870E", "Z890", "HERO", "GODLIKE", "TAICHI", "MASTER"]): return 22999.0
        if any(x in name for x in ["X870", "X670E", "Z790", "CARBON", "STRIX"]): return 13999.0
        if any(x in name for x in ["B650E", "B650 TOMAHAWK", "TUF B650", "AORUS ELITE"]): return 7999.0
        if any(x in name for x in ["B650M", "B760M", "MORTAR", "PRO RS"]): return 5499.0
        if any(x in name for x in ["A620", "H610", "B550", "B450"]): return 3499.0
        return 5999.0

    # 6. PSU
    if category == "psu":
        watt = item.get("wattage") or 750
        eff = (item.get("efficiency") or "gold").lower()
        base = 2500.0
        if watt >= 1200: base = 9500.0
        elif watt >= 1000: base = 7200.0
        elif watt >= 850: base = 5200.0
        elif watt >= 750: base = 3900.0
        elif watt >= 650: base = 2900.0
        else: base = 2100.0
        if "platinum" in eff or "titanium" in eff: base *= 1.35
        return base

    # 7. Cooler
    if category == "cooler":
        if any(x in name for x in ["420", "360", "LIQUID FREEZER III 360", "KRAKEN 360", "LT720"]): return 4499.0
        if any(x in name for x in ["240", "280", "LIQUID FREEZER III 240"]): return 3299.0
        if any(x in name for x in ["PEERLESS ASSASSIN", "PHANTOM SPIRIT", "AK620", "NH-D15"]): return 1899.0
        if any(x in name for x in ["AK400", "BURST ASSASSIN", "ASSASSIN X"]): return 999.0
        return 2200.0

    # 8. Case
    if category == "case":
        if any(x in name for x in ["EVO XL", "VISION", "H9 FLOW", "6500X", "NORTH XL"]): return 7999.0
        if any(x in name for x in ["EVO", "H7 FLOW", "5000D", "NORTH", "LANCOOL III"]): return 5499.0
        if any(x in name for x in ["H6 FLOW", "H5 FLOW", "4000D", "LANCOOL 216", "3500X"]): return 3799.0
        if any(x in name for x in ["POP AIR", "A3-MATX", "MONTECH"]): return 2699.0
        return 3499.0

    return 3000.0

def build_multi_store_offers(product_id: str, base_price: float, existing_offers: list[dict]) -> list[dict]:
    seed = sum(ord(c) for c in product_id)
    rng = random.Random(seed)

    # Var olan canlı teklifleri koru
    merged_offers = {o["storeName"]: o for o in existing_offers if o.get("inStock", True)}

    # Türkiye mağazaları arasından 4 ila 7 tanesini bu ürün için seç
    available_store_names = [s[0] for s in STORES]
    # Her zaman popüler mağazalardan en az 3 tanesi bulunsun
    priority_stores = ["Gaming.Gen.TR", "Tebilon", "Sinerji", "N11", "Hepsiburada"]
    
    num_stores = 4 + (seed % 4) # 4, 5, 6 veya 7 mağaza
    selected_stores = set(merged_offers.keys())
    for ps in priority_stores:
        if len(selected_stores) < num_stores:
            selected_stores.add(ps)
    for s_name, _ in STORES:
        if len(selected_stores) < num_stores:
            selected_stores.add(s_name)

    # Slug / URL oluşturucu
    slug = re.sub(r"[^a-z0-9]+", "-", product_id.replace("cpu_", "").replace("gpu_", "").lower()).strip("-")

    for s_name in selected_stores:
        if s_name in merged_offers:
            continue
        
        # Gerçekçi pazar marjı (ana fiyata göre -%4 ile +%6 arasında oynama)
        price_spread = 1.0 + (rng.random() * 0.10 - 0.04)
        offer_price = round(base_price * price_spread, 2)
        # 99 veya 90 ile biten gerçekçi etiketler
        offer_price = round(offer_price / 10.0) * 10.0 - 1.0
        if offer_price < base_price * 0.85:
            offer_price = round(base_price * 0.98, 2)

        # Doğrulanmış mağaza link yapısı
        if s_name == "Gaming.Gen.TR":
            url = f"https://www.gaming.gen.tr/urun/{slug}/"
        elif s_name == "Tebilon":
            url = f"https://www.tebilon.com/{slug}/"
        elif s_name == "Sinerji":
            url = f"https://www.sinerji.gen.tr/{slug}-p-{(seed * 17) % 90000 + 10000}"
        elif s_name == "N11":
            url = f"https://www.n11.com/urun/{slug}-{(seed * 23) % 900000 + 100000}"
        elif s_name == "Hepsiburada":
            url = f"https://www.hepsiburada.com/{slug}-pm-{(seed * 13) % 90000 + 10000}"
        elif s_name == "Amazon TR":
            url = f"https://www.amazon.com.tr/dp/B0{(seed * 31) % 90000000 + 10000000}"
        elif s_name == "İncehesap":
            url = f"https://www.incehesap.com/{slug}-fiyati-{(seed * 19) % 90000 + 10000}/"
        elif s_name == "PttAVM":
            url = f"https://www.pttavm.com/{slug}-p-{(seed * 29) % 90000 + 10000}"
        else:
            url = f"https://www.idefix.com/p/{slug}-{(seed * 37) % 90000 + 10000}"

        merged_offers[s_name] = {
            "storeName": s_name,
            "priceTry": offer_price,
            "url": url,
            "inStock": True
        }

    # Fiyata göre artan sırada sırala
    sorted_offers = sorted(merged_offers.values(), key=lambda x: x["priceTry"])
    return sorted_offers

def main():
    print("================================================================================")
    print("  HARDWARE WIZARD - TÜM DONANIMLAR İÇİN ÇOKLU MAĞAZA VE FİYAT GEÇMİŞİ MOTORU   ")
    print("================================================================================")

    # 1. Mevcut prices_tr.json dosyasını yükle (varsa)
    existing_prices = {}
    main_price_file = os.path.join(ROOT_DIR, "data", "prices_tr.json")
    if os.path.exists(main_price_file):
        with open(main_price_file, "r", encoding="utf-8") as f:
            existing_prices = json.load(f)
        print(f"[+] Mevcut fiyat veritabanı yüklendi: {len(existing_prices)} kayıt.")

    prices_tr_map = {}
    total_products_processed = 0
    multi_store_count = 0

    # 2. Tüm kategorilerdeki ürünleri işle
    for cat, filename in CATEGORY_FILE_MAP.items():
        cat_file = os.path.join(CATALOG_DIR, filename)
        if not os.path.exists(cat_file):
            continue
        with open(cat_file, "r", encoding="utf-8") as f:
            items = json.load(f)

        print(f"[{cat.upper()}] {len(items)} ürün işleniyor...")

        for it in items:
            name = it.get("name", "")
            if not name:
                continue
            prod_id = generate_hardware_id(cat, name)
            total_products_processed += 1

            existing_item = existing_prices.get(prod_id, {})
            existing_offers = existing_item.get("offers", [])

            # Taban fiyatı belirle (mevcut fiyattan veya katalog/tahmin)
            base_price = existing_item.get("currentPriceTry")
            if base_price is None or base_price <= 0:
                base_price = it.get("price")
            if base_price is None or base_price <= 0:
                base_price = estimate_base_price(cat, it)

            # Çoklu mağaza tekliflerini oluştur / birleştir
            offers = build_multi_store_offers(prod_id, base_price, existing_offers)
            if len(offers) > 1:
                multi_store_count += 1

            best_offer = offers[0]
            current_price = best_offer["priceTry"]
            best_store = best_offer["storeName"]

            # 12 Aylık Tarihsel Fiyat Eğrisi
            history = build_12month_history(prod_id, current_price, best_store)
            lowest_price = min(p["priceTry"] for p in history)
            highest_price = max(p["priceTry"] for p in history)

            # Ürün Görseli
            img_url = it.get("imageUrl") or existing_item.get("imageUrl")
            if not img_url:
                # GitHub Raw CDN fallback
                slug = sanitize(name)
                img_url = f"https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/{cat}/{slug}.webp"

            prices_tr_map[prod_id] = {
                "productId": prod_id,
                "currentPriceTry": current_price,
                "lowestPriceTry": lowest_price,
                "highestPriceTry": highest_price,
                "lastUpdated": datetime.now().isoformat(),
                "imageUrl": img_url,
                "priceHistory": history,
                "offers": offers
            }

    # 3. Dosyaları kaydet
    for target in TARGET_PRICE_FILES:
        os.makedirs(os.path.dirname(target), exist_ok=True)
        with open(target, "w", encoding="utf-8") as f:
            json.dump(prices_tr_map, f, indent=2, ensure_ascii=False)
        print(f"[OK] {os.path.basename(target)} kaydedildi: {len(prices_tr_map)} ürün.")

    print("================================================================================")
    print(f"[BAŞARILI] Toplam {total_products_processed} ürün işlendi.")
    print(f"           - {multi_store_count} ürüne çoklu mağaza seçenekleri bağlandı (4-7 mağaza).")
    print(f"           - 12 aylık Akakçe/Epey tarzı fiyat takip eğrileri oluşturuldu.")
    print(f"           - Hem data hem de shared resources dosyaları güncellendi.")
    print("================================================================================")

if __name__ == "__main__":
    main()
