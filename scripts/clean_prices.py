#!/usr/bin/env python3
"""
scripts/clean_prices.py
Hızlı Katalog ve Fiyat Geçmişi Temizleyicisi:
- Katalog JSON dosyalarındaki (data/catalog/*.json ve composeResources/files/*.json)
  taban fiyat altındaki eski USD fiyatlarını (70 TL, 105 TL vb.) tespit edip null yapar.
- prices_tr.json içindeki teklifi olmayan veya taban fiyatın altında kalan ürünleri
  kesinlikle currentPriceTry = null, offers = [], priceHistory = [] yapar.
- priceHistory listesini her tarih (YYYY-MM-DD) için SADECE 1 ADET en ucuz mağaza noktası kalacak
  şekilde tekilleştirir (upsert by date).
"""

import json
import os
import re

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")

TARGET_PRICE_FILES = [
    os.path.join(ROOT_DIR, "data", "prices_tr.json"),
    os.path.join(FILES_DIR, "prices_tr.json")
]

CATEGORY_PRICE_FLOORS = {
    "cpu": 1500.0,
    "gpu": 3000.0,
    "motherboard": 1500.0,
    "ram": 600.0,
    "storage": 600.0,
    "psu": 1000.0,
    "cooler": 400.0,
    "case": 800.0
}

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

def clean_history_list(history: list[dict], floor: float, current_price: float | None = None, best_store: str = "Türkiye Pazarı") -> list[dict]:
    history_by_date = {}
    if history:
        for p in history:
            raw_ts = p.get("date") or p.get("timestamp") or ""
            date_str = str(raw_ts).strip()[:10]
            if len(date_str) < 10 or not re.match(r"^\d{4}-\d{2}-\d{2}$", date_str):
                continue

            pr = p.get("priceTry") or p.get("price")
            if pr is None:
                continue
            try:
                pr = float(pr)
            except (ValueError, TypeError):
                continue

            # Taban fiyat altındaki sızıntıları ve eski sentetik simülasyonları sil
            if pr < floor:
                continue
            if date_str.startswith("2025-") or (date_str.startswith("2026-0") and date_str.endswith("-15")):
                continue

            st = p.get("storeName") or p.get("store") or best_store

            # Aynı gün için sadece EN DÜŞÜK (en ucuz) fiyatı sakla
            if date_str not in history_by_date or pr < history_by_date[date_str]["priceTry"]:
                history_by_date[date_str] = {
                    "date": date_str,
                    "timestamp": date_str,
                    "price": round(pr, 2),
                    "priceTry": round(pr, 2),
                    "storeName": st
                }

    # Eğer geçerli current_price varsa ve listede hiç kayıt kalmadıysa bugünün kaydı olarak ekle
    if current_price and current_price >= floor:
        if not history_by_date:
            from datetime import datetime
            today_str = datetime.now().strftime("%Y-%m-%d")
            history_by_date[today_str] = {
                "date": today_str,
                "timestamp": today_str,
                "price": round(float(current_price), 2),
                "priceTry": round(float(current_price), 2),
                "storeName": best_store
            }
        else:
            # En son günün kaydını kontrol et
            latest_date = max(history_by_date.keys())
            # En ucuz teklif o gün için daha düşükse güncelle
            if current_price < history_by_date[latest_date]["priceTry"]:
                history_by_date[latest_date]["price"] = round(float(current_price), 2)
                history_by_date[latest_date]["priceTry"] = round(float(current_price), 2)
                history_by_date[latest_date]["storeName"] = best_store

    return [history_by_date[d] for d in sorted(history_by_date.keys())]

def main():
    print("================================================================================")
    print("   HARDWARE WIZARD - MEVCUT VERİ VE KATALOGLARI HIZLI TEMİZLEME MOTORU          ")
    print("================================================================================")

    # 1. prices_tr.json yükle
    p_main = TARGET_PRICE_FILES[0]
    if not os.path.exists(p_main):
        print(f"[HATA] {p_main} bulunamadı!")
        return

    with open(p_main, "r", encoding="utf-8") as f:
        prices_tr_map = json.load(f)

    print(f"[+] Toplam {len(prices_tr_map)} ürün fiyat kaydı inceleniyor...")

    cleaned_product_count = 0
    nullified_fake_prices = 0
    deduped_history_points = 0

    for prod_id, entry in prices_tr_map.items():
        cat = prod_id.split("_")[0]
        floor = CATEGORY_PRICE_FLOORS.get(cat, 300.0)

        raw_offers = entry.get("offers", [])
        # Taban fiyat altındaki teklifleri temizle ve mağaza bazında tekilleştir
        unique_offers = {}
        for o in raw_offers:
            pr_val = o.get("priceTry")
            if pr_val is None or pr_val < floor:
                continue
            s_name = o["storeName"]
            if s_name not in unique_offers or pr_val < unique_offers[s_name]["priceTry"]:
                unique_offers[s_name] = o

        sorted_offers = sorted(unique_offers.values(), key=lambda x: x["priceTry"])

        old_cur = entry.get("currentPriceTry")
        if not sorted_offers:
            # Mağaza teklifi yoksa kesinlikle null olmalı! Eski USD fiyat sızıntısını sil
            if old_cur is not None:
                nullified_fake_prices += 1
            entry["currentPriceTry"] = None
            entry["lowestPriceTry"] = None
            entry["highestPriceTry"] = None
            entry["priceHistory"] = []
            entry["offers"] = []
        else:
            current_price = sorted_offers[0]["priceTry"]
            best_store = sorted_offers[0]["storeName"]
            old_hist = entry.get("priceHistory", [])
            new_hist = clean_history_list(old_hist, floor, current_price, best_store)

            if len(old_hist) != len(new_hist):
                deduped_history_points += (len(old_hist) - len(new_hist))

            entry["currentPriceTry"] = current_price
            entry["lowestPriceTry"] = min(p["priceTry"] for p in new_hist) if new_hist else current_price
            entry["highestPriceTry"] = max(p["priceTry"] for p in new_hist) if new_hist else current_price
            entry["priceHistory"] = new_hist
            entry["offers"] = sorted_offers
            cleaned_product_count += 1

    # prices_tr.json dosyalarını kaydet
    for p_file in TARGET_PRICE_FILES:
        os.makedirs(os.path.dirname(p_file), exist_ok=True)
        with open(p_file, "w", encoding="utf-8") as f:
            json.dump(prices_tr_map, f, indent=2, ensure_ascii=False)
        print(f"[OK] {os.path.basename(p_file)} başarıyla kaydedildi.")

    print(f"\n[+] prices_tr.json Temizlik Raporu:")
    print(f"    - Null yapılan sahte/USD fiyat: {nullified_fake_prices}")
    print(f"    - Temizlenen mükerrer grafik veri noktası: {deduped_history_points}")
    print(f"    - Aktif ve doğrulanmış TR mağaza fiyatı olan ürün: {cleaned_product_count}")

    # 2. Katalog Dosyalarını Temizle
    print(f"\n[+] Katalog JSON dosyaları taranıyor ve taban fiyat altındaki USD fiyatları siliniyor...")
    catalog_items_cleaned = 0

    for cat, filenames in CATEGORY_FILE_MAP.items():
        floor = CATEGORY_PRICE_FLOORS.get(cat, 300.0)
        for fname in filenames:
            for d in [FILES_DIR, CATALOG_DIR]:
                p = os.path.join(d, fname)
                if not os.path.exists(p):
                    continue
                with open(p, "r", encoding="utf-8") as f:
                    items = json.load(f)

                modified = False
                for it in items:
                    pr = it.get("price")
                    if pr is not None and pr < floor:
                        it["price"] = None
                        catalog_items_cleaned += 1
                        modified = True

                    # Ayrıca prices_tr_map içinde bu ürünün geçerli TL fiyatı varsa güncelle
                    from update_prices import generate_hardware_id
                    pid = generate_hardware_id(cat, it["name"])
                    ptr_entry = prices_tr_map.get(pid)
                    if ptr_entry:
                        cur_tr = ptr_entry.get("currentPriceTry")
                        if cur_tr is not None and cur_tr >= floor:
                            if it.get("price") != cur_tr:
                                it["price"] = cur_tr
                                modified = True
                        else:
                            if it.get("price") is not None:
                                it["price"] = None
                                modified = True

                with open(p, "w", encoding="utf-8") as f:
                    json.dump(items, f, indent=2, ensure_ascii=False)

    print(f"[OK] Kataloglarda toplam {catalog_items_cleaned} adet sahte USD fiyatı temizlendi (null yapıldı).")
    print("================================================================================")
    print("   TEMİZLİK VE SENKRONİZASYON BAŞARIYLA TAMAMLANDI!                             ")
    print("================================================================================")

if __name__ == "__main__":
    main()
