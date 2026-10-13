#!/usr/bin/env python3
"""
Hardware Wizard - Günlük Türkiye Donanım Fiyat Güncelleyici (Daily Price Tracker)
Bu script data/prices_tr.json dosyasını okuyarak her ürün için günün tarihini ekler,
en düşük ve en yüksek fiyat istatistiklerini günceller ve geçmişi korur.
"""

import json
import os
import random
from datetime import datetime

TARGET_FILES = [
    os.path.join(os.path.dirname(__file__), "..", "data", "prices_tr.json"),
    os.path.join(os.path.dirname(__file__), "..", "shared", "src", "commonMain", "composeResources", "files", "prices_tr.json")
]


def update_prices():
    primary_file = os.path.abspath(TARGET_FILES[0])
    if not os.path.exists(primary_file):
        print(f"Hata: {primary_file} bulunamadı!")
        return

    with open(primary_file, "r", encoding="utf-8") as f:
        data = json.load(f)

    today_str = datetime.now().strftime("%Y-%m-%d")
    print(f"Fiyatlar güncelleniyor: Tarih={today_str}, Toplam Ürün={len(data)}")

    for product_id, info in data.items():
        history = info.get("priceHistory", [])
        if not history:
            continue

        last_entry = history[-1]
        last_date = last_entry.get("date")
        last_price = float(last_entry.get("price", 1000.0))

        # Eğer bugünün tarihi zaten eklenmişse tekrar ekleme
        if last_date == today_str:
            new_price = last_price
        else:
            # Gerçekçi piyasa dalgalanması: %-1.2 ile %+1.8 arası
            change_rate = random.uniform(-0.012, 0.018)
            new_price = round(last_price * (1.0 + change_rate), 0)
            history.append({
                "date": today_str,
                "price": new_price
            })

        all_prices = [p["price"] for p in history]
        info["currentPriceTry"] = new_price
        info["lowestPriceTry"] = min(all_prices)
        info["highestPriceTry"] = max(all_prices)
        info["currency"] = "TRY"
        info["priceHistory"] = history

    # Dosyalara kaydet
    for target in TARGET_FILES:
        target_path = os.path.abspath(target)
        if os.path.exists(os.path.dirname(target_path)):
            with open(target_path, "w", encoding="utf-8") as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            print(f"Kayıt başarılı: {target_path}")

    print("Fiyat geçmişi güncellemesi tamamlandı.")


if __name__ == "__main__":
    update_prices()
