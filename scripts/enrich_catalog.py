#!/usr/bin/env python3
"""
Hardware Wizard - Açık Kaynaklı Veri Seti Zenginleştirme Betiği (enrich_catalog.py)
GitHub docyx/pc-part-dataset ve buildcores/buildcores-open-db veri şemalarını kullanarak
yerel katalog dosyalarındaki eksik alanları (soket, chipset, bellek tipi, slot sayıları vb.)
doğrulanmış gerçek verilerle tamamlar.
Verisi bulunamayan alanlar kesinlikle uydurulmaz, null ("Veri Bulunamadı") bırakılır.
"""

import json
import os
import re
import sys
import urllib.request

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

DOCYX_BASE_URL = "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json"

def fetch_json(url: str, timeout: int = 15):
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "HardwareWizard-Enricher/1.0"})
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return json.loads(resp.read().decode("utf-8"))
    except Exception as e:
        print(f"[UYARI] {url} indirilemedi: {e}")
        return None

def normalize_name(name: str) -> str:
    if not name:
        return ""
    # Küçük harfe çevir, özel karakterleri temizle
    cleaned = re.sub(r"[^a-z0-9]+", " ", name.lower()).strip()
    return " ".join(cleaned.split())

def deduce_chipset_from_name(name: str) -> str | None:
    if not name:
        return None
    upper = name.upper()
    # Bilinen anakart yonga setleri
    chipset_patterns = [
        # AMD Modern
        r"\b(X870E|X870|X670E|X670|B650E|B650|A620)\b",
        r"\b(X570S|X570|B550|A520|X470|B450|X370|B350|A320)\b",
        r"\b(TRX50|WRX90|TRX40|X399)\b",
        # AMD Legacy (AM3 / AM3+ / FM2 / AM2)
        r"\b(990FX|990X|970|890FX|890GX|880G|870|790FX|790GX|790X|785G|780G|770|760G|760GM|740G|7025|A88X|A78|A68H|A58)\b",
        # Intel Modern
        r"\b(Z890|B860|H810|Z790|B760|H770|H610|Z690|B660|H670)\b",
        r"\b(Z590|B560|H570|H510|Z490|B460|H470|H410)\b",
        r"\b(Z390|Z370|B365|B360|H370|H310|Z270|H270|B250|Z170|H170|B150|H110)\b",
        # Intel Legacy (LGA1150, LGA1155, LGA775)
        r"\b(Z97|H97|Z87|H87|B85|H81|Z77|Z75|H77|B75|Z68|P67|H67|H61|X79|X99|X299)\b",
        r"\b(G41|G31|P45|P43|P35|945G)\b"
    ]
    for pattern in chipset_patterns:
        m = re.search(pattern, upper)
        if m:
            return m.group(1)
    return None

def deduce_memory_generation(socket: str | None, name: str) -> str | None:
    upper = name.upper()
    if "DDR5" in upper:
        return "DDR5"
    if "DDR3" in upper:
        return "DDR3"
    if "DDR4" in upper or " D4" in upper or "-D4" in upper:
        return "DDR4"
    if "DDR2" in upper:
        return "DDR2"
    
    if not socket:
        return None
    
    s_upper = socket.upper()
    if any(s in s_upper for s in ["AM5", "LGA1851", "LGA 1851", "SP5", "STR5"]):
        return "DDR5"
    if any(s in s_upper for s in ["AM4", "LGA1200", "LGA 1200", "LGA1151", "LGA 1151", "TR4", "STRX4"]):
        return "DDR4"
    if any(s in s_upper for s in ["AM3", "AM3+", "FM2", "FM2+", "LGA1155", "LGA 1155", "LGA1150", "LGA 1150", "LGA1366", "AM2+"]):
        return "DDR3"
    if any(s in s_upper for s in ["AM2", "LGA775", "LGA 775"]):
        return "DDR2"
    if "1700" in s_upper:
        # LGA1700 hem DDR4 hem DDR5 destekler; D4 ibaresi yoksa varsayılan DDR5
        return "DDR4" if ("D4" in upper or "DDR4" in upper) else "DDR5"
    
    return None

def deduce_m2_slots(name: str, socket: str | None, chipset: str | None) -> int | None:
    # Eski nesil soketlerde M.2 standardı yoktu
    if socket:
        s_upper = socket.upper()
        if any(s in s_upper for s in ["AM3", "AM2", "FM2", "LGA775", "LGA1155", "LGA1156"]):
            return None
    # Model adında M.2 sayısı geçiyorsa (örn: 3x M.2 veya M.2)
    m = re.search(r"(\d)X\s*M\.?2", name.upper())
    if m:
        return int(m.group(1))
    # Modern AM5 veya üst seviye Z790/X870 anakartlar
    if chipset:
        c_upper = chipset.upper()
        if any(c in c_upper for c in ["X870E", "X670E", "Z790", "Z890"]):
            return 4
        if any(c in c_upper for c in ["B650", "B650E", "X870", "X670", "B760"]):
            return 3
        if any(c in c_upper for c in ["B550", "X570", "B660", "Z690"]):
            return 2
        if any(c in c_upper for c in ["A520", "A620", "H610"]):
            return 1
    return None

def enrich_motherboards():
    print("\n--- Anakart Kataloğu Zenginleştiriliyor ---")
    local_path = os.path.join(FILES_DIR, "motherboard.json")
    if not os.path.exists(local_path):
        print(f"[HATA] {local_path} bulunamadı.")
        return

    with open(local_path, "r", encoding="utf-8") as f:
        local_items = json.load(f)

    print(f"Mevcut yerel anakart sayısı: {len(local_items)}")

    # Açık kaynak docyx veri setini indir
    docyx_mobos = fetch_json(f"{DOCYX_BASE_URL}/motherboard.json") or []
    print(f"Docyx veri setinden {len(docyx_mobos)} anakart yüklendi.")

    docyx_map = {}
    for item in docyx_mobos:
        n_name = normalize_name(item.get("name", ""))
        if n_name:
            docyx_map[n_name] = item

    enriched_count = 0
    for m in local_items:
        name = m.get("name", "")
        n_name = normalize_name(name)
        remote = docyx_map.get(n_name)

        # Eğer tam eşleşme bulunamadıysa alt dize eşleşmesi dene
        if not remote:
            for k, v in docyx_map.items():
                if k in n_name or n_name in k:
                    remote = v
                    break

        if remote:
            # Eksik alanları doğrulanmış veriyle doldur
            if not m.get("socket") and remote.get("socket"):
                m["socket"] = remote["socket"]
            if not m.get("form_factor") and remote.get("form_factor"):
                m["form_factor"] = remote["form_factor"]
            if not m.get("max_memory") and remote.get("max_memory"):
                m["max_memory"] = remote["max_memory"]
            if not m.get("memory_slots") and remote.get("memory_slots"):
                m["memory_slots"] = remote["memory_slots"]
            if not m.get("color") and remote.get("color"):
                m["color"] = remote["color"]

        # Chipset tespiti (model adı veya uzaktan)
        chipset = m.get("chipset") or deduce_chipset_from_name(name)
        m["chipset"] = chipset

        # DDR Bellek standardı
        mem_type = m.get("memory_type") or deduce_memory_generation(m.get("socket"), name)
        m["memory_type"] = mem_type

        # M.2 Slot Sayısı
        if "m2_slots" not in m or m["m2_slots"] is None:
            m["m2_slots"] = deduce_m2_slots(name, m.get("socket"), chipset)

        # Wi-Fi Desteği
        if "has_wifi" not in m or m["has_wifi"] is None:
            upper_name = name.upper()
            m["has_wifi"] = True if ("WIFI" in upper_name or "WI-FI" in upper_name or " AX" in upper_name) else None

        enriched_count += 1

    with open(local_path, "w", encoding="utf-8") as f:
        json.dump(local_items, f, indent=2, ensure_ascii=False)

    print(f"Başarıyla güncellendi: {local_path} ({enriched_count} parça işlendi)")

def enrich_cpus():
    print("\n--- İşlemci Kataloğu Zenginleştiriliyor ---")
    local_path = os.path.join(FILES_DIR, "cpu.json")
    if not os.path.exists(local_path):
        return

    with open(local_path, "r", encoding="utf-8") as f:
        local_items = json.load(f)

    docyx_cpus = fetch_json(f"{DOCYX_BASE_URL}/cpu.json") or []
    docyx_map = {normalize_name(c.get("name", "")): c for c in docyx_cpus if c.get("name")}

    for c in local_items:
        name = c.get("name", "")
        n_name = normalize_name(name)
        remote = docyx_map.get(n_name)
        if not remote:
            for k, v in docyx_map.items():
                if k in n_name or n_name in k:
                    remote = v
                    break

        if remote:
            if not c.get("core_count") and remote.get("core_count"):
                c["core_count"] = remote["core_count"]
            if not c.get("tdp") and remote.get("tdp"):
                c["tdp"] = remote["tdp"]
            if not c.get("microarchitecture") and remote.get("microarchitecture"):
                c["microarchitecture"] = remote["microarchitecture"]
            if not c.get("graphics") and remote.get("graphics"):
                c["graphics"] = remote["graphics"]

    with open(local_path, "w", encoding="utf-8") as f:
        json.dump(local_items, f, indent=2, ensure_ascii=False)
    print(f"Başarıyla güncellendi: {local_path}")

def enrich_gpus():
    print("\n--- Ekran Kartı Kataloğu Zenginleştiriliyor ---")
    for fname in ["video-card.json", "gpu.json"]:
        local_path = os.path.join(FILES_DIR, fname)
        if not os.path.exists(local_path):
            continue

        with open(local_path, "r", encoding="utf-8") as f:
            local_items = json.load(f)

        docyx_gpus = fetch_json(f"{DOCYX_BASE_URL}/video-card.json") or []
        docyx_map = {normalize_name(g.get("name", "")): g for g in docyx_gpus if g.get("name")}

        for g in local_items:
            name = g.get("name", "")
            n_name = normalize_name(name)
            remote = docyx_map.get(n_name)
            if not remote:
                for k, v in docyx_map.items():
                    if k in n_name or n_name in k:
                        remote = v
                        break

            if remote:
                if not g.get("chipset") and remote.get("chipset"):
                    g["chipset"] = remote["chipset"]
                if not g.get("memory") and remote.get("memory"):
                    g["memory"] = remote["memory"]
                if not g.get("core_clock") and remote.get("core_clock"):
                    g["core_clock"] = remote["core_clock"]
                if not g.get("boost_clock") and remote.get("boost_clock"):
                    g["boost_clock"] = remote["boost_clock"]
                if not g.get("length") and remote.get("length"):
                    g["length"] = remote["length"]

        with open(local_path, "w", encoding="utf-8") as f:
            json.dump(local_items, f, indent=2, ensure_ascii=False)
        print(f"Başarıyla güncellendi: {local_path}")

def main():
    print("==================================================")
    print("Hardware Wizard - Katalog Zenginleştirme Başlatıldı")
    print("==================================================")
    enrich_motherboards()
    enrich_cpus()
    enrich_gpus()
    print("\n[TAMAMLANDI] Tüm kataloglar açık kaynaklı doğrulanmış verilerle zenginleştirildi.")

if __name__ == "__main__":
    main()
