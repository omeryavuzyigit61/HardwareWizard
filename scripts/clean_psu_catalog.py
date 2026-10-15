import json
import os
import re

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")

with open(os.path.join(ROOT_DIR, "data", "ironscale_psu_db.json"), "r", encoding="utf-8") as f:
    ironscale_db = json.load(f)

records = ironscale_db.get("records", [])

def fix_mojibake(s: str) -> str:
    if not s:
        return ""
    s = re.sub(r"[\r\n]+", " ", s)
    s = re.sub(r"\s+", " ", s).strip()
    
    # Generic replacement character cleanup
    s = re.sub(r"Kapasit[\uFFFD\?]?r", "Kapasitör", s)
    s = re.sub(r"105[\uFFFD\?]?C", "105°C", s)
    s = re.sub(r"85[\uFFFD\?]?C", "85°C", s)
    s = re.sub(r"25[\uFFFD\?]?C", "25°C", s)
    s = re.sub(r"Mod[\uFFFD\?]?ler", "Modüler", s)
    s = re.sub(r"De[\uFFFD\?]?il", "Değil", s)
    s = re.sub(r"Do[\uFFFD\?]?rultma", "Doğrultma", s)
    s = re.sub(r"Reg[\uFFFD\?]?lasyon", "Regülasyon", s)
    s = re.sub(r"reg[\uFFFD\?]?le", "regüle", s)
    s = re.sub(r"Kanal[\uFFFD\?]?", "Kanalı", s)
    s = s.replace("\ufffd", "")
    return s.strip()

def determine_efficiency(name: str, raw_eff: str) -> str:
    combined = (name + " " + (raw_eff or "")).lower()
    if "titanium" in combined:
        return "titanium"
    if "platinum" in combined:
        return "platinum"
    if "gold" in combined:
        return "gold"
    if "silver" in combined:
        return "silver"
    if "bronze" in combined:
        return "bronze"
    if "80 plus" in combined or "standard" in combined or "white" in combined:
        return "80 plus"
    return "gold"

def determine_modular(raw_mod: str) -> str:
    if not raw_mod:
        return "Full"
    low = raw_mod.lower()
    if "full" in low or "tam" in low:
        return "Full"
    if "yarı" in low or "semi" in low:
        return "Semi"
    if "değil" in low or "non" in low or "yok" in low:
        return "No"
    return "Semi"

psu_catalog_path = os.path.join(CATALOG_DIR, "power-supply.json")
with open(psu_catalog_path, "r", encoding="utf-8") as f:
    psus = json.load(f)

rec_lookup = {}
for r in records:
    d_name = r.get("display_name", "")
    b = r.get("brand", "")
    s = r.get("series", "")
    rec_lookup[d_name.lower()] = r
    rec_lookup[f"{b} {s}".lower()] = r

cleaned_psus = []
for p in psus:
    name = p.get("name", "")
    name_low = name.lower()

    matched_rec = None
    for k, r in rec_lookup.items():
        if k in name_low or (r.get("brand", "").lower() in name_low and r.get("series", "").lower() in name_low):
            matched_rec = r
            break

    raw_eff = matched_rec.get("efficiency", "") if matched_rec else ""
    efficiency = determine_efficiency(name, raw_eff)

    raw_mod = matched_rec.get("modular", "") if matched_rec else ""
    modular = determine_modular(raw_mod) if raw_mod else p.get("modular", "Semi")

    oem = fix_mojibake(matched_rec.get("oem", "")) if matched_rec else ""
    prim = matched_rec.get("primary", {}) if matched_rec else {}
    top = fix_mojibake(prim.get("topology", "")) if prim else ""
    cap = fix_mojibake(prim.get("bulk_capacitor", "")) if prim else ""

    parts = []
    if oem and oem != "-":
        parts.append(f"OEM: {oem}")
    if top and top != "-":
        parts.append(f"Topoloji: {top}")
    if cap and cap != "-":
        parts.append(f"Kapasitör: {cap}")

    if parts:
        new_topology = " | ".join(parts)
    else:
        new_topology = fix_mojibake(p.get("topology", ""))

    p["efficiency"] = efficiency
    p["modular"] = modular
    p["topology"] = new_topology
    p["tier"] = fix_mojibake(p.get("tier", ""))

    cleaned_psus.append(p)

targets = [
    os.path.join(CATALOG_DIR, "power-supply.json"),
    os.path.join(CATALOG_DIR, "psu.json"),
    os.path.join(FILES_DIR, "power-supply.json"),
    os.path.join(FILES_DIR, "psu.json"),
]

for t in targets:
    os.makedirs(os.path.dirname(t), exist_ok=True)
    with open(t, "w", encoding="utf-8") as f:
        json.dump(cleaned_psus, f, indent=2, ensure_ascii=False)
    print(f"Saved: {t}")
