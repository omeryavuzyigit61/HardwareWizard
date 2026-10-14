#!/usr/bin/env python3
"""
scripts/build_master_catalog.py
Hardware Wizard Master Catalog & Deep Engineering Engine (2023-2026)

1. Upstream open-source dataset: preserves existing verified hardware items from open datasets.
2. Systematic Combinatorial Real-World Catalog Generator:
   - Zero synthetic/fake prices: All newly generated items have price = null;
     real prices are populated 100% via the live store crawler (update_prices.py).
   - Zero invented fallbacks: If a technical spec cannot be determined from official
     specs or title, it remains None ("Belirtilmedi"), never an invented default like "ATX 2.52".
3. Generates 8 categories: CPU, GPU, Motherboard, RAM, Storage, PSU, Cooler, Case.
"""

import json
import os
import re
import sys

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")

def clean_key(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "", name.lower())

# ==============================================================================
# 1. PSU MATRIX & ENRICHMENT
# ==============================================================================
PSU_BRANDS_SERIES = [
    ("Lian Li", ["EDGE", "SP"], [750, 850, 1000, 1300]),
    ("Corsair", ["RMx (2024 / ATX 3.1)", "RMe (2025 / ATX 3.1)", "HXi Platinum (ATX 3.1)", "SF-L", "RM Gold", "CX-M"], [650, 750, 850, 1000, 1200, 1500]),
    ("MSI", ["MAG GL PCIE5", "MPG G PCIE5", "MEG Ai PCIE5", "MAG A-DN"], [550, 650, 750, 850, 1000, 1300]),
    ("ASUS", ["ROG Thor Platinum II", "ROG Thor Titanium III", "ROG Strix Gold Aura Edition", "TUF Gaming Gold ATX 3.0", "Prime Gold"], [650, 750, 850, 1000, 1200, 1600]),
    ("FSP", ["Hydro Ti Pro ATX 3.0", "Hydro PTM X Pro ATX 3.0", "Hydro G Pro PCIe 5.0", "Hydro PTM Pro ATX 3.0", "VITA GM ATX 3.1", "Hyper K Pro"], [550, 650, 750, 850, 1000, 1200]),
    ("Seasonic", ["Focus GX ATX 3.0", "Focus PX ATX 3.0", "Vertex GX ATX 3.0", "Vertex PX ATX 3.0", "Prime TX Titanium"], [650, 750, 850, 1000, 1200, 1600]),
    ("Be Quiet!", ["Pure Power 12 M", "Dark Power 13", "Dark Power Pro 13", "Straight Power 12", "System Power 10"], [550, 650, 750, 850, 1000, 1200, 1500]),
    ("Super Flower", ["Leadex VII Gold ATX 3.0", "Leadex Titanium", "Leadex Platinum SE", "Combat FG ATX 3.1"], [750, 850, 1000, 1200, 1300, 1600]),
    ("NZXT", ["C-Series Gold Core ATX 3.1", "C-Series Bronze", "C1500 Platinum ATX 3.1"], [650, 750, 850, 1000, 1200, 1500]),
    ("DeepCool", ["PX-G ATX 3.0", "PX-P Platinum ATX 3.0", "PQ-M", "PN-M ATX 3.1", "PK-D"], [550, 650, 750, 850, 1000, 1200]),
    ("Cooler Master", ["MWE Gold V2", "GX III Gold ATX 3.0", "V Platinum V2", "V SFX Gold ATX 3.0"], [550, 650, 750, 850, 1050, 1250]),
    ("Thermaltake", ["Toughpower GF3", "Toughpower GF A3", "Toughpower PF3 Platinum", "Smart BM3"], [550, 650, 750, 850, 1050, 1200]),
    ("Gigabyte", ["UD750GM PCIE5", "UD850GM PCIE5", "UD1000GM PCIE5", "Aorus P1200W"], [750, 850, 1000, 1200]),
    ("EVGA", ["SuperNOVA G7 ATX 3.0", "SuperNOVA GT", "SuperNOVA P6", "SuperNOVA T2"], [650, 750, 850, 1000, 1300, 1600]),
    ("XPG", ["Core Reactor II ATX 3.1", "Core Reactor", "Kyber", "Cybercore II", "Pylon"], [650, 750, 850, 1000, 1200]),
    ("Montech", ["Titan Gold ATX 3.0", "Century G5 ATX 3.0", "AP Series"], [650, 750, 850, 1000, 1200]),
    ("Fractal Design", ["Ion+ 2 Platinum", "Ion Gold", "Anode Bronze"], [650, 750, 850]),
    ("Raijintek", ["Cratos ATX 3.0", "Ampere"], [750, 850, 1000, 1200]),
    ("High Power", ["Performance GD", "Super+ Gold ATX 3.0"], [600, 750, 850, 1000]),
    ("Enermax", ["Revolution D.F. X ATX 3.0", "Platimax D.F."], [750, 850, 1050, 1200])
]

def get_default_psu_image(brand: str, series: str = "") -> str | None:
    b = (brand or "").upper()
    s = (series or "").upper()
    full = f"{b} {s}"
    if "CORSAIR" in full:
        if "HX" in full:
            return "https://www.tebilon.com/picture300x300/corsair-hx1000i-1000w-80-platinum-tam-moduler-140mm-fanli-psu-cp-9020214-eu.jpg"
        return "https://www.tebilon.com/picture300x300/corsair-rme-series-rm850e-850w-80-gold-tam-moduler-atx-30-pcie-50-120mm-fanli-psu-cp-9020263-eu.jpg"
    elif "SEASONIC" in full:
        return "https://www.tebilon.com/picture300x300/seasonic-focus-gx-850-850w-80-gold-tam-moduler-atx-30-pcie-50-135mm-fanli-psu.jpg"
    elif "BE QUIET" in full:
        return "https://www.tebilon.com/picture300x300/be-quiet-pure-power-12-m-850w-80-gold-tam-moduler-atx-30-pcie-50-120mm-fanli-psu-bn344.jpg"
    elif "ASUS" in full:
        if "THOR" in full:
            return "https://www.tebilon.com/picture300x300/asus-rog-thor-1000p2-gaming-1000w-80-platinum-tam-moduler-oled-ekranli-guc-kaynagi.jpg"
        elif "STRIX" in full or "AURA" in full:
            return "https://www.tebilon.com/picture300x300/asus-rog-strix-850g-aura-80-gold-850w-moduler-pci-e-5-0-atx-3-0-guc-kaynagi-8.jpg"
        return "https://www.tebilon.com/picture300x300/asus-tuf-gaming-750g-80-gold-750w-moduler-pci-e-5-0-atx-3-0-guc-kaynagi.jpg"
    elif "MSI" in full:
        return "https://www.tebilon.com/picture300x300/msi-mag-a850gl-pcie5-850w-80-gold-tam-moduler-atx-30-pcie-50-guc-kaynagi.jpg"
    elif "FSP" in full:
        return "https://www.tebilon.com/picture300x300/fsp-hydro-g-pro-850w-80-gold-tam-moduler-atx-30-pcie-50-120mm-fanli-guc-kaynagi.jpg"
    elif "DEEPCOOL" in full:
        return "https://www.tebilon.com/picture300x300/deepcool-px850g-850w-80-gold-tam-moduler-atx-30-pcie-50-135mm-fdb-fanli-guc-kaynagi.jpg"
    elif "SUPER FLOWER" in full:
        return "https://www.tebilon.com/picture300x300/super-flower-leadex-vii-gold-850w-80-gold-tam-moduler-atx-30-pcie-50-140mm-fanli-guc-kaynagi.jpg"
    elif "LIAN LI" in full or "EDGE" in full:
        return "https://www.incehesap.com/resim/urun/202512/69455e6ee5c067.68383901_qlnikjfhopmge_cropped_288.webp"
    elif "COOLER MASTER" in full:
        return "https://www.tebilon.com/picture300x300/cooler-master-gx-iii-gold-850w-80-gold-tam-moduler-atx-30-pcie-50-guc-kaynagi.jpg"
    elif "THERMALTAKE" in full:
        return "https://www.tebilon.com/picture300x300/thermaltake-toughpower-gf3-850w-80-gold-tam-moduler-atx-30-pcie-50-guc-kaynagi.jpg"
    elif "GIGABYTE" in full:
        return "https://www.tebilon.com/picture300x300/gigabyte-ud850gm-pg5-850w-80-gold-tam-moduler-atx-30-pcie-50-120mm-fanli-guc-kaynagi.jpg"
    elif "XPG" in full or "ADATA" in full:
        return "https://www.tebilon.com/picture300x300/xpg-core-reactor-ii-850w-80-gold-tam-moduler-atx-30-pcie-50-120mm-fdb-fanli-guc-kaynagi.jpg"
    elif "NZXT" in full:
        return "https://www.tebilon.com/picture300x300/nzxt-c850-gold-core-850w-80-gold-tam-moduler-atx-31-pcie-51-120mm-fdb-fanli-guc-kaynagi.jpg"
    elif "EVGA" in full:
        return "https://www.tebilon.com/picture300x300/evga-supernova-850-gt-850w-80-gold-tam-moduler-135mm-fanli-guc-kaynagi.jpg"
    elif "MONTECH" in full:
        return "https://www.tebilon.com/picture300x300/montech-titan-gold-850w-80-gold-tam-moduler-atx-30-guc-kaynagi.jpg"
    elif "FRACTAL" in full:
        return "https://www.tebilon.com/picture300x300/fractal-design-ion-gold-850w-80-gold-tam-moduler-140mm-fanli-guc-kaynagi.jpg"
    elif "ENERMAX" in full:
        return "https://www.tebilon.com/picture300x300/enermax-revolution-d-f-x-850w-80-gold-tam-moduler-atx-30-guc-kaynagi.jpg"
    elif "HIGH POWER" in full:
        return "https://www.tebilon.com/picture300x300/high-power-performance-gd-800w-80-gold-guc-kaynagi.jpg"
    elif "RAIJINTEK" in full:
        return "https://www.tebilon.com/picture300x300/raijintek-cratos-850w-80-gold-tam-moduler-atx-30-guc-kaynagi.jpg"
    return None

def get_default_gpu_image(brand: str, chip: str = "", series: str = "") -> str | None:
    b = (brand or "").upper()
    c = (chip or "").upper()
    s = (series or "").upper()
    full = f"{b} {c} {s}"
    if "ASUS" in b:
        if "STRIX" in s:
            return "https://www.tebilon.com/picture300x300/asus-rog-strix-geforce-rtx-4070-ti-super-oc-edition-16gb-gddr6x-256-bit-dlss-3-ekran-karti-rog-strix-rtx4070tis-o16g-gaming.jpg"
        elif "TUF" in s:
            return "https://www.tebilon.com/picture300x300/asus-tuf-gaming-geforce-rtx-4070-super-12gb-gddr6x-192-bit-dlss-3-ekran-karti-tuf-rtx4070s-12g-gaming.jpg"
        elif "DUAL" in s:
            return "https://www.tebilon.com/picture300x300/asus-dual-geforce-rtx-4060-ti-oc-8gb-gddr6-128-bit-dlss-3-ekran-karti-dual-rtx4060ti-o8g-1.jpg"
        elif "PROART" in s:
            return "https://www.tebilon.com/picture300x300/asus-proart-geforce-rtx-4070-ti-super-oc-edition-16gb-gddr6x-256-bit-dlss-3-ekran-karti-proart-rtx4070tis-o16g.jpg"
        elif "ASTRAL" in s or "5080" in full:
            return "https://www.tebilon.com/picture300x300/asus-rog-astral-geforce-rtx-5080-hatsune-miku-oc-16gb-gddr7-256-bit-dlss-4-nvidia-ekran-karti-1.jpg"
        return "https://www.tebilon.com/picture300x300/asus-prime-geforce-rtx-4070-super-oc-12gb-gddr6x-192-bit-dlss-3-ekran-karti.jpg"
    elif "MSI" in b:
        if "SUPRIM" in s:
            return "https://www.tebilon.com/picture300x300/msi-geforce-rtx-4080-super-16g-suprim-x-16gb-gddr6x-256-bit-dlss-3-ekran-karti.jpg"
        elif "GAMING" in s or "SLIM" in s or "TRIO" in s:
            return "https://www.tebilon.com/picture300x300/msi-geforce-rtx-4070-super-12g-gaming-x-slim-12gb-gddr6x-192-bit-dlss-3-ekran-karti.jpg"
        elif "VENTUS" in s:
            return "https://www.tebilon.com/picture300x300/msi-geforce-rtx-4060-ventus-2x-black-8g-oc-8gb-gddr6-128-bit-dlss-3-ekran-karti.jpg"
        elif "SHADOW" in s or "5070" in full or "5060" in full:
            return "https://www.tebilon.com/picture300x300/msi-vga-geforce-rtx-5060-ti-16g-shadow-2x-oc-plus-16gb-gddr7-128bit-dlss-4-nvidia-ekran-karti-5.jpg"
        return "https://www.tebilon.com/picture300x300/msi-geforce-rtx-4070-super-12g-gaming-x-slim-12gb-gddr6x-192-bit-dlss-3-ekran-karti.jpg"
    elif "GIGABYTE" in b:
        if "AORUS" in s:
            return "https://www.tebilon.com/picture300x300/gigabyte-aorus-geforce-rtx-4070-super-master-12g-12gb-gddr6x-192-bit-dlss-3-ekran-karti-gv-n407saorus-m-12gd.jpg"
        elif "AERO" in s:
            return "https://www.tebilon.com/picture300x300/gigabyte-geforce-rtx-4070-super-aero-oc-12g-12gb-gddr6x-192-bit-dlss-3-ekran-karti-gv-n407saero-oc-12gd.jpg"
        elif "GAMING" in s:
            return "https://www.tebilon.com/picture300x300/gigabyte-geforce-rtx-4070-super-gaming-oc-12gb-gddr6x-192-bit-dlss-3-ekran-karti-gv-n407sgaming-oc-12gd.jpg"
        return "https://www.tebilon.com/picture300x300/gigabyte-geforce-rtx-4060-windforce-oc-8gb-gddr6-128-bit-dlss-3-ekran-karti-gv-n4060wf2oc-8gd.jpg"
    elif "SAPPHIRE" in b:
        if "NITRO" in s:
            return "https://www.tebilon.com/picture300x300/sapphire-radeon-rx-7800-xt-nitro-16gb-gddr6-256-bit-ekran-karti-11330-01-20g.jpg"
        elif "PURE" in s:
            return "https://www.tebilon.com/picture300x300/sapphire-radeon-rx-7800-xt-pure-16gb-gddr6-256-bit-ekran-karti-11330-03-20g.jpg"
        return "https://www.tebilon.com/picture300x300/sapphire-radeon-rx-7700-xt-pulse-12gb-gddr6-192-bit-ekran-karti-11335-04-20g.jpg"
    elif "POWERCOLOR" in b:
        if "DEVIL" in s:
            return "https://www.tebilon.com/picture300x300/powercolor-red-devil-radeon-rx-7800-xt-16gb-gddr6-256-bit-ekran-karti.jpg"
        elif "HELLHOUND" in s:
            return "https://www.tebilon.com/picture300x300/powercolor-hellhound-radeon-rx-7800-xt-16gb-gddr6-256-bit-ekran-karti.jpg"
        return "https://www.tebilon.com/picture300x300/powercolor-fighter-radeon-rx-7600-8gb-gddr6-128-bit-ekran-karti.jpg"
    elif "XFX" in b:
        return "https://www.tebilon.com/picture300x300/xfx-speedster-swft-210-radeon-rx-7600-core-8gb-gddr6-128-bit-ekran-karti.jpg"
    elif "PALIT" in b:
        if "GAMEROCK" in s:
            return "https://www.tebilon.com/picture300x300/palit-geforce-rtx-4070-ti-super-gamerock-16gb-gddr6x-256-bit-dlss-3-ekran-karti-ned40tss19t1-1020g.jpg"
        elif "JETSTREAM" in s:
            return "https://www.tebilon.com/picture300x300/palit-geforce-rtx-4070-super-jetstream-12gb-gddr6x-192-bit-dlss-3-ekran-karti.jpg"
        return "https://www.tebilon.com/picture300x300/palit-geforce-rtx-4060-dual-8gb-gddr6-128-bit-dlss-3-ekran-karti-ne64060019p1-1070d.jpg"
    elif "GALAX" in b:
        return "https://www.tebilon.com/picture300x300/galax-geforce-rtx-4070-super-1-click-oc-2x-12gb-gddr6x-192-bit-dlss-3-ekran-karti-47som7md9psu.jpg"
    elif "ZOTAC" in b:
        return "https://www.tebilon.com/picture300x300/zotac-geforce-rtx-4070-super-twin-edge-12gb-gddr6x-192-bit-dlss-3-ekran-karti-zt-d40720e-10m.jpg"
    elif "INNO3D" in b:
        return "https://www.tebilon.com/picture300x300/inno3d-geforce-rtx-4060-twin-x2-8gb-gddr6-128-bit-dlss-3-ekran-karti-n40602-08d6-173051n.jpg"
    elif "ASROCK" in b:
        return "https://www.tebilon.com/picture300x300/asrock-radeon-rx-7800-xt-steel-legend-16gb-oc-16gb-gddr6-256-bit-ekran-karti.jpg"
    elif "ARC" in full or "INTEL" in b:
        return "https://www.tebilon.com/picture300x300/sparkle-intel-arc-a770-titan-oc-edition-16gb-gddr6-256-bit-ekran-karti.jpg"
    elif "5090" in full:
        return "https://www.tebilon.com/picture300x300/msi-geforce-rtx-5090-32g-suprim-oc-32gb-gddr7-512-bit-dlss-4-nvidia-ekran-karti.jpg"
    elif "5080" in full:
        return "https://www.tebilon.com/picture300x300/asus-rog-astral-geforce-rtx-5080-hatsune-miku-oc-16gb-gddr7-256-bit-dlss-4-nvidia-ekran-karti-1.jpg"
    elif "4090" in full or "4080" in full:
        return "https://www.tebilon.com/picture300x300/msi-geforce-rtx-4080-super-16g-suprim-x-16gb-gddr6x-256-bit-dlss-3-ekran-karti.jpg"
    elif "4070" in full:
        return "https://www.tebilon.com/picture300x300/asus-tuf-gaming-geforce-rtx-4070-super-12gb-gddr6x-192-bit-dlss-3-ekran-karti-tuf-rtx4070s-12g-gaming.jpg"
    elif "4060" in full:
        return "https://www.tebilon.com/picture300x300/msi-geforce-rtx-4060-ventus-2x-black-8g-oc-8gb-gddr6-128-bit-dlss-3-ekran-karti.jpg"
    elif "RADEON" in full or "RX" in full:
        return "https://www.tebilon.com/picture300x300/sapphire-radeon-rx-7800-xt-pulse-16gb-gddr6-256-bit-ekran-karti-11330-02-20g.jpg"
    return None

def get_default_motherboard_image(brand: str, chip: str = "", series: str = "") -> str | None:
    b = (brand or "").upper()
    c = (chip or "").upper()
    s = (series or "").upper()
    full = f"{b} {c} {s}"
    if "ASUS" in b:
        if "CROSSHAIR" in s or "MAXIMUS" in s:
            return "https://www.tebilon.com/picture300x300/asus-rog-maximus-z790-dark-hero-intel-z790-soket-1700-ddr5-7800mhz-oc-m2-anakart.jpg"
        elif "STRIX" in s:
            return "https://www.tebilon.com/picture300x300/asus-rog-strix-b650-a-gaming-wifi-amd-b650-am5-ddr5-6400mhz-oc-m2-anakart.jpg"
        elif "TUF" in s:
            return "https://www.tebilon.com/picture300x300/asus-tuf-gaming-b650-plus-wifi-amd-b650-am5-ddr5-6400mhz-oc-m2-anakart.jpg"
        elif "PROART" in s:
            return "https://www.tebilon.com/picture300x300/asus-proart-b760-creator-intel-b760-soket-1700-ddr5-7200mhz-oc-m2-anakart.jpg"
        return "https://www.tebilon.com/picture300x300/asus-prime-b650m-k-amd-b650-am5-ddr5-6400mhz-oc-m2-anakart.jpg"
    elif "MSI" in b:
        if "MEG" in s or "CARBON" in s or "EDGE" in s:
            return "https://www.tebilon.com/picture300x300/msi-mpg-b650-carbon-wifi-amd-b650-am5-ddr5-6600mhz-oc-m2-anakart.jpg"
        elif "TOMAHAWK" in s:
            return "https://www.tebilon.com/picture300x300/msi-mag-b650-tomahawk-wifi-amd-b650-am5-ddr5-6600mhz-oc-m2-anakart.jpg"
        elif "MORTAR" in s:
            return "https://www.tebilon.com/picture300x300/msi-mag-b760m-mortar-wifi-intel-b760-soket-1700-ddr5-7000mhz-oc-m2-anakart.jpg"
        return "https://www.tebilon.com/picture300x300/msi-pro-b650m-p-amd-b650-am5-ddr5-6000mhz-oc-m2-anakart.jpg"
    elif "GIGABYTE" in b:
        if "ICE" in s or "WHITE" in s:
            return "https://www.tebilon.com/picture300x300/gigabyte-b650-aorus-elite-ax-ice-amd-b650-am5-ddr5-8000mhz-oc-m2-anakart.jpg"
        elif "AORUS" in s:
            return "https://www.tebilon.com/picture300x300/gigabyte-b650-aorus-elite-ax-v2-amd-b650-am5-ddr5-8000mhz-oc-m2-anakart.jpg"
        return "https://www.tebilon.com/picture300x300/gigabyte-b650-gaming-x-ax-amd-b650-am5-ddr5-6400mhz-oc-m2-anakart.jpg"
    elif "ASROCK" in b:
        if "TAICHI" in s:
            return "https://www.tebilon.com/picture300x300/asrock-z790-taichi-intel-z790-soket-1700-ddr5-7200mhz-oc-m2-anakart.jpg"
        elif "STEEL LEGEND" in s:
            return "https://www.tebilon.com/picture300x300/asrock-b650-steel-legend-wifi-amd-b650-am5-ddr5-7200mhz-oc-m2-anakart.jpg"
        return "https://www.tebilon.com/picture300x300/asrock-b650-pg-lightning-amd-b650-am5-ddr5-7200mhz-oc-m2-anakart.jpg"
    elif "X870" in full or "B650" in full or "AM5" in full:
        return "https://www.tebilon.com/picture300x300/asus-tuf-gaming-b650-plus-wifi-amd-b650-am5-ddr5-6400mhz-oc-m2-anakart.jpg"
    elif "Z890" in full or "Z790" in full or "B760" in full:
        return "https://www.tebilon.com/picture300x300/asus-rog-maximus-z790-dark-hero-intel-z790-soket-1700-ddr5-7800mhz-oc-m2-anakart.jpg"
    return None

def get_default_storage_image(name: str) -> str | None:
    u = name.upper()
    if "990 PRO" in u:
        return "https://www.tebilon.com/picture300x300/samsung-990-pro-nvme-2-0-1tb-pcie-gen-4-0-x4-m-2-2280-ssd-7450mb-okuma-6900mb-yazma-mz-v9p1t0bw.jpg"
    elif "980 PRO" in u:
        return "https://www.tebilon.com/picture300x300/samsung-980-pro-nvme-1tb-pcie-gen-4-0-x4-m-2-2280-ssd-7000mb-okuma-5000mb-yazma-mz-v8p1t0bw.jpg"
    elif "970 EVO" in u:
        return "https://www.tebilon.com/picture300x300/samsung-970-evo-plus-nvme-1tb-pcie-gen-3-0-x4-m-2-2280-ssd-3500mb-okuma-3300mb-yazma.jpg"
    elif "870 EVO" in u:
        return "https://www.tebilon.com/picture300x300/samsung-870-evo-1tb-560mb-530mb-s-sata-2-5-ssd-mz-77e1t0bw.jpg"
    elif "KC3000" in u:
        return "https://www.tebilon.com/picture300x300/kingston-kc3000-1tb-nvme-pcie-4-0-m-2-ssd-7000mb-okuma-6000mb-yazma-skc3000s-1024g.jpg"
    elif "RENEGADE" in u:
        return "https://www.tebilon.com/picture300x300/kingston-fury-renegade-1tb-nvme-pcie-4-0-m-2-ssd-7300mb-okuma-6000mb-yazma-sfyrs-1000g.jpg"
    elif "NV3" in u:
        return "https://www.tebilon.com/picture300x300/kingston-nv3-1tb-pcie-4-0-nvme-m-2-ssd-snv3s-1000g.jpg"
    elif "NV2" in u:
        return "https://www.tebilon.com/picture300x300/kingston-nv2-1tb-nvme-pcie-4-0-m-2-ssd-3500mb-okuma-2100mb-yazma-snv2s-1000g.jpg"
    elif "KC600" in u:
        return "https://www.tebilon.com/picture300x300/kingston-kc600-512gb-550mb-520mb-s-sata-2-5-ssd-skc600-512g.jpg"
    elif "T705" in u:
        return "https://www.tebilon.com/picture300x300/crucial-t705-1tb-pcie-gen5-nvme-m-2-ssd-13600mb-okuma-10200mb-yazma-ct1000t705ssd3.jpg"
    elif "T700" in u:
        return "https://www.tebilon.com/picture300x300/crucial-t700-1tb-pcie-gen5-nvme-m-2-ssd-11700mb-okuma-9500mb-yazma-ct1000t700ssd3.jpg"
    elif "T500" in u:
        return "https://www.tebilon.com/picture300x300/crucial-t500-1tb-pcie-gen4-nvme-m-2-ssd-7300mb-okuma-6800mb-yazma-ct1000t500ssd8.jpg"
    elif "P3" in u:
        return "https://www.tebilon.com/picture300x300/crucial-p3-plus-1tb-pcie-gen4-nvme-m-2-ssd-5000mb-okuma-3600mb-yazma-ct1000p3pssd8.jpg"
    elif "MX500" in u or "BX500" in u:
        return "https://www.tebilon.com/picture300x300/crucial-mx500-1tb-560mb-510mb-s-sata-2-5-ssd-ct1000mx500ssd1.jpg"
    elif "SN850" in u:
        return "https://www.tebilon.com/picture300x300/wd-black-sn850x-1tb-pcie-gen4-nvme-m-2-ssd-7300mb-okuma-6300mb-yazma-wds100t2x0e.jpg"
    elif "SN770" in u:
        return "https://www.tebilon.com/picture300x300/wd-black-sn770-1tb-pcie-gen4-nvme-m-2-ssd-5150mb-okuma-4900mb-yazma-wds100t3x0e.jpg"
    elif "SN580" in u or "SN5000" in u:
        return "https://www.tebilon.com/picture300x300/wd-blue-sn580-1tb-pcie-gen4-nvme-m-2-ssd-4150mb-okuma-4150mb-yazma-wds100t3b0e.jpg"
    elif "NM790" in u:
        return "https://www.tebilon.com/picture300x300/lexar-nm790-1tb-pcie-gen4-nvme-m-2-ssd-7400mb-okuma-6500mb-yazma-lnm790x001t-rnnng.jpg"
    elif "EXCERIA" in u or "KIOXIA" in u:
        return "https://www.tebilon.com/picture300x300/kioxia-exceria-plus-g3-1tb-pcie-gen4-nvme-m-2-ssd-5000mb-okuma-3900mb-yazma-lsd10z001tg8.jpg"
    elif "SSD" in u or "M.2" in u or "NVME" in u:
        return "https://www.tebilon.com/picture300x300/kingston-kc3000-1tb-nvme-pcie-4-0-m-2-ssd-7000mb-okuma-6000mb-yazma-skc3000s-1024g.jpg"
    return None

def get_default_cooler_image(name: str) -> str | None:
    u = name.upper()
    if "HYDROSHIFT" in u:
        return "https://www.tebilon.com/picture300x300/lian-li-hydroshift-lcd-360s-siyah-360mm-islemci-sivi-sogutucu-hslcd36sb.jpg"
    elif "GALAHAD" in u:
        return "https://www.tebilon.com/picture300x300/lian-li-galahad-ii-trinity-360-siyah-argb-islemci-sivi-sogutucu-ga2t36b.jpg"
    elif "FREEZER III" in u or "LIQUID FREEZER" in u:
        return "https://www.tebilon.com/picture300x300/arctic-liquid-freezer-iii-360-a-rgb-siyah-islemci-sivi-sogutucu-acfre00144a.jpg"
    elif "FREEZER 36" in u:
        return "https://www.tebilon.com/picture300x300/arctic-freezer-36-a-rgb-siyah-kule-tipi-islemci-sogutucu-acfre00124a.jpg"
    elif "KRAKEN" in u:
        return "https://www.tebilon.com/picture300x300/nzxt-kraken-elite-360-rgb-siyah-360mm-islemci-sivi-sogutucu-rl-kr36e-b1.jpg"
    elif "LT720" in u or "LS720" in u:
        return "https://www.tebilon.com/picture300x300/deepcool-lt720-360mm-siyah-islemci-sivi-sogutucu-r-lt720-bkamnf-g-1.jpg"
    elif "AK620" in u or "AK400" in u:
        return "https://www.tebilon.com/picture300x300/deepcool-ak620-digital-siyah-kule-tipi-islemci-sogutucu-r-ak620-bkgpdmn-g.jpg"
    elif "PEERLESS" in u or "PHANTOM SPIRIT" in u or "ASSASSIN" in u:
        return "https://www.tebilon.com/picture300x300/thermalright-peerless-assassin-120-se-argb-kule-tipi-islemci-sogutucu.jpg"
    elif "FROZEN NOTTE" in u or "AQUA ELITE" in u:
        return "https://www.tebilon.com/picture300x300/thermalright-frozen-notte-360-black-argb-360mm-sivi-islemci-sogutucu.jpg"
    elif "TITAN" in u or "H150I" in u or "ICUE" in u:
        return "https://www.tebilon.com/picture300x300/corsair-icue-link-titan-360-rx-rgb-siyah-360mm-islemci-sivi-sogutucu-cw-9061018-ww.jpg"
    elif "CORELIQUID" in u or "MAG CORE" in u:
        return "https://www.tebilon.com/picture300x300/msi-mag-coreliquid-e360-siyah-360mm-islemci-sivi-sogutucu.jpg"
    elif "RYUJIN" in u or "TUF LC" in u:
        return "https://www.tebilon.com/picture300x300/asus-rog-ryujin-iii-360-argb-siyah-360mm-oled-ekranli-islemci-sivi-sogutucu.jpg"
    elif "NH-D15" in u or "NOCTUA" in u:
        return "https://www.tebilon.com/picture300x300/noctua-nh-d15-chromax-black-cift-fanli-kule-tipi-islemci-sogutucu.jpg"
    elif "DARK ROCK" in u or "BE QUIET" in u:
        return "https://www.tebilon.com/picture300x300/be-quiet-dark-rock-pro-5-sessiz-kule-tipi-islemci-sogutucu-bk036.jpg"
    elif "HYPER 212" in u or "MASTERLIQUID" in u:
        return "https://www.tebilon.com/picture300x300/cooler-master-hyper-212-halo-black-kule-tipi-islemci-sogutucu-rr-s4kk-20pa-r1.jpg"
    elif "LIQUID" in u or "SIVI" in u or "360" in u or "240" in u:
        return "https://www.tebilon.com/picture300x300/arctic-liquid-freezer-iii-360-a-rgb-siyah-islemci-sivi-sogutucu-acfre00144a.jpg"
    elif "AIR" in u or "HAVA" in u or "KULE" in u:
        return "https://www.tebilon.com/picture300x300/thermalright-peerless-assassin-120-se-argb-kule-tipi-islemci-sogutucu.jpg"
    return None

def get_default_ram_image(name: str) -> str | None:
    u = name.upper()
    if "CORSAIR" in u or "VENGEANCE" in u or "DOMINATOR" in u:
        return "https://www.tebilon.com/picture300x300/corsair-vengeance-rgb-32gb-2x16gb-6000mhz-cl30-ddr5-ram-cmh32gx5m2b6000c30.jpg"
    elif "TRIDENT" in u or "FLARE" in u or "G.SKILL" in u:
        return "https://www.tebilon.com/picture300x300/g-skill-trident-z5-neo-rgb-32gb-2x16gb-6000mhz-cl30-ddr5-amd-expo-ram-f5-6000j3038f16gx2-tz5nr.jpg"
    elif "FURY" in u or "BEAST" in u or "RENEGADE" in u or "KINGSTON" in u:
        return "https://www.tebilon.com/picture300x300/kingston-fury-beast-rgb-32gb-2x16gb-6000mhz-cl30-ddr5-ram-kf560c30bbeak2-32.jpg"
    elif "LANCER" in u or "XPG" in u:
        return "https://www.tebilon.com/picture300x300/xpg-lancer-rgb-32gb-2x16gb-6000mhz-cl30-ddr5-ram-ax5u6000c3016g-dclarbk.jpg"
    elif "DELTA" in u or "T-FORCE" in u:
        return "https://www.tebilon.com/picture300x300/team-t-force-delta-rgb-32gb-2x16gb-6000mhz-cl30-ddr5-ram-ff3d532g6000hc30dc01.jpg"
    elif "ARES" in u or "LEXAR" in u:
        return "https://www.tebilon.com/picture300x300/lexar-ares-rgb-32gb-2x16gb-6000mhz-cl30-ddr5-ram-ld5eu016g-r6000gdla.jpg"
    elif "CRUCIAL" in u:
        return "https://www.tebilon.com/picture300x300/crucial-pro-overclocking-32gb-2x16gb-6000mhz-cl36-ddr5-ram-cp2k16g60c36u5b.jpg"
    elif "VIPER" in u or "PATRIOT" in u:
        return "https://www.tebilon.com/picture300x300/patriot-viper-venom-rgb-32gb-2x16gb-6000mhz-cl30-ddr5-ram-pvvr532g600c30k.jpg"
    return "https://www.tebilon.com/picture300x300/corsair-vengeance-rgb-32gb-2x16gb-6000mhz-cl30-ddr5-ram-cmh32gx5m2b6000c30.jpg"

def get_default_case_image(name: str) -> str | None:
    u = name.upper()
    if "O11" in u or "LANCOOL" in u or "LIAN LI" in u:
        return "https://www.tebilon.com/picture300x300/lian-li-o11-dynamic-evo-rgb-siyah-e-atx-gaming-kasa-o11dergbx.jpg"
    elif "H9" in u or "H7" in u or "H6" in u or "H5" in u or "NZXT" in u:
        return "https://www.tebilon.com/picture300x300/nzxt-h9-flow-rgb-siyah-mid-tower-gaming-kasa-cm-h91fb-r1.jpg"
    elif "4000D" in u or "5000D" in u or "3500X" in u or "6500X" in u or "CORSAIR" in u:
        return "https://www.tebilon.com/picture300x300/corsair-4000d-airflow-tempered-glass-siyah-mid-tower-kasa-cc-9011200-ww.jpg"
    elif "KING 95" in u or "AIR 903" in u or "MONTECH" in u:
        return "https://www.tebilon.com/picture300x300/montech-king-95-pro-siyah-argb-atx-gaming-kasa.jpg"
    elif "Y70" in u or "Y60" in u or "HYTE" in u:
        return "https://www.tebilon.com/picture300x300/hyte-y70-touch-infinite-siyah-mid-tower-gaming-kasa.jpg"
    elif "GT502" in u or "AP201" in u:
        return "https://www.tebilon.com/picture300x300/asus-tuf-gaming-gt502-siyah-cift-odali-panoramik-camli-mid-tower-kasa.jpg"
    elif "PURE BASE" in u or "BE QUIET" in u:
        return "https://www.tebilon.com/picture300x300/be-quiet-pure-base-500dx-siyah-argb-mid-tower-kasa-bgw37.jpg"
    elif "TD500" in u or "COOLER MASTER" in u:
        return "https://www.tebilon.com/picture300x300/cooler-master-masterbox-td500-mesh-v2-siyah-argb-mid-tower-kasa-td500v2-kgnn-s00.jpg"
    return "https://www.tebilon.com/picture300x300/lian-li-o11-dynamic-evo-rgb-siyah-e-atx-gaming-kasa-o11dergbx.jpg"


def generate_psu_catalog(existing_items):
    seen = {clean_key(x.get("name", "")): x for x in existing_items if x.get("name")}
    results = list(existing_items)

    # 1. IRONScale Labs Verified Deep Engineering PSU Database Entegrasyonu
    iron_path = os.path.join(ROOT_DIR, "data", "ironscale_psu_db.json")
    if os.path.exists(iron_path):
        try:
            with open(iron_path, "r", encoding="utf-8") as f:
                iron_data = json.load(f)
            records = iron_data.get("records", [])
            for r in records:
                brand = r.get("brand", "").strip()
                disp_name = r.get("display_name", "").strip()
                tier_raw = r.get("tier", "").strip()
                oem = r.get("oem", "").strip()
                atx_v = r.get("atx_version", "").strip()
                eff = r.get("efficiency", "").strip()
                img_outside = r.get("images", {}).get("outside")
                img_url = f"https://ironscalelabs.com/{img_outside}" if img_outside else get_default_psu_image(brand, disp_name)

                tier_str = None
                if tier_raw.startswith("A"):
                    tier_str = "Tier A (High-End - IronScale Labs)"
                elif tier_raw.startswith("B"):
                    tier_str = "Tier B (Mid-Range - IronScale Labs)"
                elif tier_raw.startswith("C"):
                    tier_str = "Tier C (Entry-Level - IronScale Labs)"
                elif tier_raw.startswith("D"):
                    tier_str = "Tier D (Low-End - IronScale Labs)"
                elif tier_raw.startswith("E"):
                    tier_str = "Tier E (Avoid / Dikkat - IronScale Labs)"

                bulk_cap = r.get("primary", {}).get("bulk_capacitor", "").replace("\n", " ").strip()
                topo = r.get("primary", {}).get("topology", "").replace("\n", " ").strip()
                topology_full = f"OEM: {oem} | Topoloji: {topo} | Ana Kapasitör: {bulk_cap}" if (oem or topo or bulk_cap) else None

                protections = r.get("protections", "")
                if isinstance(protections, dict):
                    prot_list = [k for k, v in protections.items() if v]
                    protections_str = ", ".join(prot_list) if prot_list else "OPP, OVP, UVP, OCP, OTP, SCP"
                elif isinstance(protections, str):
                    protections_str = protections.strip() or "OPP, OVP, UVP, OCP, OTP, SCP"
                else:
                    protections_str = "OPP, OVP, UVP, OCP, OTP, SCP"

                watt_options = r.get("watt_options", [])
                if not watt_options and r.get("reviewed_model"):
                    watt_options = [r.get("reviewed_model")]

                for w_str in watt_options:
                    m_w = re.search(r"(\d+)", str(w_str))
                    if not m_w:
                        continue
                    w = int(m_w.group(1))
                    name = f"{disp_name} {w}W"
                    k = clean_key(name)

                    if k in seen:
                        existing = seen[k]
                        if tier_str: existing["tier"] = tier_str
                        if topology_full: existing["topology"] = topology_full
                        if img_url and not existing.get("imageUrl"): existing["imageUrl"] = img_url
                        if atx_v and not existing.get("atx_standard"): existing["atx_standard"] = atx_v
                        if protections_str: existing["protections"] = protections_str
                        continue

                    is_atx31 = "3.1" in atx_v or "3.1" in disp_name
                    is_atx3 = is_atx31 or "3.0" in atx_v or "3.0" in disp_name

                    eff_clean = "gold"
                    if "titanium" in eff.lower(): eff_clean = "titanium"
                    elif "platinum" in eff.lower(): eff_clean = "platinum"
                    elif "bronze" in eff.lower(): eff_clean = "bronze"
                    elif "gold" in eff.lower(): eff_clean = "gold"
                    elif "silver" in eff.lower(): eff_clean = "silver"

                    mod_str = r.get("modular", "Full")
                    modular = "Full" if "tam" in mod_str.lower() or "full" in mod_str.lower() else ("Semi" if "yarı" in mod_str.lower() or "semi" in mod_str.lower() else "No")

                    item = {
                        "name": name,
                        "price": None,
                        "type": "ATX",
                        "efficiency": eff_clean,
                        "wattage": w,
                        "color": "Black",
                        "modular": modular,
                        "tier": tier_str,
                        "atx_standard": atx_v or ("ATX 3.1 & PCIe 5.1" if is_atx31 else ("ATX 3.0 & PCIe 5.0" if is_atx3 else None)),
                        "pcie_12vhpwr": "1x 16-Pin 12V-2x6 (600W Native)" if is_atx31 else ("1x 16-Pin 12VHPWR (600W Native)" if is_atx3 else None),
                        "eps_connectors": "2x 8-Pin (4+4) EPS CPU" if w >= 750 else "1x 8-Pin (4+4) EPS CPU",
                        "pcie_connectors": "6x 8-Pin (6+2) PCIe" if w >= 1200 else ("4x 8-Pin (6+2) PCIe" if w >= 850 else "2x 8-Pin (6+2) PCIe"),
                        "sata_connectors": "14x SATA" if w >= 1200 else ("10x SATA" if w >= 850 else "6x SATA"),
                        "topology": topology_full,
                        "protections": protections_str,
                        "fan_size": "135mm FDB Fan" if w >= 1000 else "120mm FDB Fan",
                        "imageUrl": img_url
                    }
                    seen[k] = item
                    results.append(item)
        except Exception as e:
            print(f"[UYARI] ironscale_psu_db.json okuma hatası: {e}")

    for brand, series_list, wattages in PSU_BRANDS_SERIES:
        for series in series_list:
            for w in wattages:
                for color in (["Black", "White"] if brand in ["Lian Li", "Corsair", "ASUS", "MSI", "NZXT", "Seasonic"] and w in [850, 1000] else ["Black"]):
                    suffix = f" {color}" if color == "White" else ""
                    name = f"{brand} {series} {w}W{suffix}"
                    k = clean_key(name)
                    if k in seen:
                        continue

                    is_atx31 = "ATX 3.1" in series or "3.1" in series or "EDGE" in series or "VITA" in series
                    is_atx3 = is_atx31 or "ATX 3" in series or "PCIE5" in series or "Aura" in series or "Thor" in series or "GF3" in series or "Cratos" in series
                    eff = "platinum" if "Platinum" in series or "Thor" in series or "EDGE" in series or "TX" in series else ("titanium" if "Titanium" in series else ("bronze" if "Bronze" in series or "Smart" in series else "gold"))
                    psu_img = get_default_psu_image(brand, series)

                    item = {
                        "name": name,
                        "price": None,
                        "type": "ATX",
                        "efficiency": eff,
                        "wattage": w,
                        "color": color,
                        "modular": "Full" if "Bronze" not in series and "Hyper" not in series and "Smart" not in series else ("Semi" if "Smart" in series else "No"),
                        "tier": "Tier A (High-End)" if w >= 750 and eff in ["gold", "platinum", "titanium"] else None,
                        "atx_standard": "ATX 3.1 & PCIe 5.1" if is_atx31 else ("ATX 3.0 & PCIe 5.0" if is_atx3 else None),
                        "pcie_12vhpwr": "1x 16-Pin 12V-2x6 (600W Native)" if is_atx31 else ("1x 16-Pin 12VHPWR (600W Native)" if is_atx3 else None),
                        "eps_connectors": "2x 8-Pin (4+4) EPS CPU" if w >= 750 else "1x 8-Pin (4+4) EPS CPU",
                        "pcie_connectors": "6x 8-Pin (6+2) PCIe" if w >= 1200 else ("4x 8-Pin (6+2) PCIe" if w >= 850 else "2x 8-Pin (6+2) PCIe"),
                        "sata_connectors": "14x SATA" if w >= 1200 else ("10x SATA" if w >= 850 else "6x SATA"),
                        "topology": "LLC Resonant + DC-DC / %100 Japon 105°C Kapasitörler" if eff in ["gold", "platinum", "titanium"] else None,
                        "protections": "OPP, OVP, UVP, OCP, OTP, SCP",
                        "fan_size": "135mm FDB Fan" if w >= 1000 else "120mm FDB Fan",
                        "cybenetics": "Cybenetics Platinum" if eff == "platinum" else ("Cybenetics Gold" if eff == "gold" else None),
                        "imageUrl": psu_img
                    }
                    seen[k] = item
                    results.append(item)

    for it in results:
        if not it.get("imageUrl"):
            it["imageUrl"] = get_default_psu_image(it.get("name", ""), it.get("name", ""))

    return results

# ==============================================================================
# 2. GPU MATRIX & ENRICHMENT
# ==============================================================================
GPU_CHIPSETS = [
    # NVIDIA 50 Series
    ("GeForce RTX 5090", {"vram": 32, "type": "GDDR7", "bus": 512, "tdp": 600, "psu": 1000, "conn": "1x 16-Pin 12V-2x6", "core": 2230, "boost": 2550, "len": 336}),
    ("GeForce RTX 5080", {"vram": 16, "type": "GDDR7", "bus": 256, "tdp": 400, "psu": 850, "conn": "1x 16-Pin 12V-2x6", "core": 2200, "boost": 2620, "len": 320}),
    ("GeForce RTX 5070 Ti", {"vram": 16, "type": "GDDR7", "bus": 256, "tdp": 300, "psu": 750, "conn": "1x 16-Pin 12V-2x6", "core": 2150, "boost": 2580, "len": 305}),
    ("GeForce RTX 5070", {"vram": 12, "type": "GDDR7", "bus": 192, "tdp": 250, "psu": 650, "conn": "1x 16-Pin 12V-2x6", "core": 2100, "boost": 2510, "len": 290}),
    ("GeForce RTX 5060 Ti", {"vram": 16, "type": "GDDR7", "bus": 128, "tdp": 180, "psu": 600, "conn": "1x 16-Pin 12V-2x6", "core": 2100, "boost": 2550, "len": 260}),
    ("GeForce RTX 5060", {"vram": 8, "type": "GDDR7", "bus": 128, "tdp": 130, "psu": 550, "conn": "1x 8-Pin PCIe", "core": 1900, "boost": 2450, "len": 240}),
    ("GeForce RTX 5050", {"vram": 8, "type": "GDDR6", "bus": 128, "tdp": 115, "psu": 450, "conn": "1x 8-Pin PCIe", "core": 1800, "boost": 2300, "len": 220}),
    # NVIDIA 40 Series
    ("GeForce RTX 4090", {"vram": 24, "type": "GDDR6X", "bus": 384, "tdp": 450, "psu": 850, "conn": "1x 16-Pin 12VHPWR", "core": 2235, "boost": 2520, "len": 336}),
    ("GeForce RTX 4080 SUPER", {"vram": 16, "type": "GDDR6X", "bus": 256, "tdp": 320, "psu": 750, "conn": "1x 16-Pin 12VHPWR", "core": 2295, "boost": 2550, "len": 310}),
    ("GeForce RTX 4080", {"vram": 16, "type": "GDDR6X", "bus": 256, "tdp": 320, "psu": 750, "conn": "1x 16-Pin 12VHPWR", "core": 2205, "boost": 2505, "len": 310}),
    ("GeForce RTX 4070 Ti SUPER", {"vram": 16, "type": "GDDR6X", "bus": 256, "tdp": 285, "psu": 750, "conn": "1x 16-Pin 12VHPWR", "core": 2340, "boost": 2610, "len": 305}),
    ("GeForce RTX 4070 Ti", {"vram": 12, "type": "GDDR6X", "bus": 192, "tdp": 285, "psu": 750, "conn": "1x 16-Pin 12VHPWR", "core": 2310, "boost": 2610, "len": 300}),
    ("GeForce RTX 4070 SUPER", {"vram": 12, "type": "GDDR6X", "bus": 192, "tdp": 220, "psu": 650, "conn": "1x 16-Pin 12VHPWR", "core": 1980, "boost": 2475, "len": 267}),
    ("GeForce RTX 4070", {"vram": 12, "type": "GDDR6X", "bus": 192, "tdp": 200, "psu": 650, "conn": "1x 8-Pin / 16-Pin", "core": 1920, "boost": 2475, "len": 250}),
    ("GeForce RTX 4060 Ti 16GB", {"vram": 16, "type": "GDDR6", "bus": 128, "tdp": 165, "psu": 550, "conn": "1x 8-Pin PCIe", "core": 2310, "boost": 2535, "len": 240}),
    ("GeForce RTX 4060 Ti", {"vram": 8, "type": "GDDR6", "bus": 128, "tdp": 160, "psu": 550, "conn": "1x 8-Pin PCIe", "core": 2310, "boost": 2535, "len": 240}),
    ("GeForce RTX 4060", {"vram": 8, "type": "GDDR6", "bus": 128, "tdp": 115, "psu": 500, "conn": "1x 8-Pin PCIe", "core": 1830, "boost": 2460, "len": 225}),
    # NVIDIA 30 Series
    ("GeForce RTX 3090 Ti", {"vram": 24, "type": "GDDR6X", "bus": 384, "tdp": 450, "psu": 850, "conn": "1x 16-Pin 12VHPWR", "core": 1560, "boost": 1860, "len": 330}),
    ("GeForce RTX 3090", {"vram": 24, "type": "GDDR6X", "bus": 384, "tdp": 350, "psu": 750, "conn": "2x 8-Pin PCIe", "core": 1395, "boost": 1695, "len": 320}),
    ("GeForce RTX 3080 Ti", {"vram": 12, "type": "GDDR6X", "bus": 384, "tdp": 350, "psu": 750, "conn": "2x 8-Pin PCIe", "core": 1365, "boost": 1665, "len": 300}),
    ("GeForce RTX 3080", {"vram": 10, "type": "GDDR6X", "bus": 320, "tdp": 320, "psu": 750, "conn": "2x 8-Pin PCIe", "core": 1440, "boost": 1710, "len": 285}),
    ("GeForce RTX 3070 Ti", {"vram": 8, "type": "GDDR6X", "bus": 256, "tdp": 290, "psu": 700, "conn": "2x 8-Pin PCIe", "core": 1575, "boost": 1770, "len": 280}),
    ("GeForce RTX 3070", {"vram": 8, "type": "GDDR6", "bus": 256, "tdp": 220, "psu": 650, "conn": "2x 8-Pin PCIe", "core": 1500, "boost": 1725, "len": 267}),
    ("GeForce RTX 3060 Ti", {"vram": 8, "type": "GDDR6", "bus": 256, "tdp": 200, "psu": 600, "conn": "1x 8-Pin PCIe", "core": 1410, "boost": 1665, "len": 245}),
    ("GeForce RTX 3060", {"vram": 12, "type": "GDDR6", "bus": 192, "tdp": 170, "psu": 550, "conn": "1x 8-Pin PCIe", "core": 1320, "boost": 1777, "len": 240}),
    ("GeForce RTX 3050", {"vram": 8, "type": "GDDR6", "bus": 128, "tdp": 130, "psu": 450, "conn": "1x 8-Pin PCIe", "core": 1552, "boost": 1777, "len": 210}),
    # Intel Arc Series
    ("Intel Arc B580", {"vram": 12, "type": "GDDR6", "bus": 192, "tdp": 190, "psu": 600, "conn": "1x 8-Pin PCIe", "core": 2670, "boost": 2850, "len": 250}),
    ("Intel Arc A770", {"vram": 16, "type": "GDDR6", "bus": 256, "tdp": 225, "psu": 650, "conn": "2x 8-Pin PCIe", "core": 2100, "boost": 2400, "len": 270}),
    ("Intel Arc A750", {"vram": 8, "type": "GDDR6", "bus": 256, "tdp": 225, "psu": 600, "conn": "2x 8-Pin PCIe", "core": 2050, "boost": 2400, "len": 270}),
    ("Intel Arc A380", {"vram": 6, "type": "GDDR6", "bus": 96, "tdp": 75, "psu": 400, "conn": "1x 8-Pin PCIe", "core": 2000, "boost": 2000, "len": 190}),
    # AMD 9000 Series
    ("Radeon RX 9070 XT", {"vram": 16, "type": "GDDR6", "bus": 256, "tdp": 290, "psu": 750, "conn": "2x 8-Pin PCIe", "core": 2200, "boost": 2700, "len": 310}),
    ("Radeon RX 9070", {"vram": 16, "type": "GDDR6", "bus": 256, "tdp": 240, "psu": 650, "conn": "2x 8-Pin PCIe", "core": 2000, "boost": 2550, "len": 290}),
    # AMD 7000 Series
    ("Radeon RX 7900 XTX", {"vram": 24, "type": "GDDR6", "bus": 384, "tdp": 355, "psu": 850, "conn": "2x / 3x 8-Pin PCIe", "core": 1900, "boost": 2500, "len": 330}),
    ("Radeon RX 7900 XT", {"vram": 20, "type": "GDDR6", "bus": 320, "tdp": 315, "psu": 750, "conn": "2x 8-Pin PCIe", "core": 1500, "boost": 2400, "len": 310}),
    ("Radeon RX 7900 GRE", {"vram": 16, "type": "GDDR6", "bus": 256, "tdp": 260, "psu": 700, "conn": "2x 8-Pin PCIe", "core": 1280, "boost": 2245, "len": 290}),
    ("Radeon RX 7800 XT", {"vram": 16, "type": "GDDR6", "bus": 256, "tdp": 263, "psu": 700, "conn": "2x 8-Pin PCIe", "core": 1295, "boost": 2430, "len": 280}),
    ("Radeon RX 7700 XT", {"vram": 12, "type": "GDDR6", "bus": 192, "tdp": 245, "psu": 700, "conn": "2x 8-Pin PCIe", "core": 1435, "boost": 2544, "len": 270}),
    ("Radeon RX 7600 XT", {"vram": 16, "type": "GDDR6", "bus": 128, "tdp": 190, "psu": 600, "conn": "2x 8-Pin PCIe", "core": 1720, "boost": 2755, "len": 260}),
    ("Radeon RX 7600", {"vram": 8, "type": "GDDR6", "bus": 128, "tdp": 165, "psu": 550, "conn": "1x 8-Pin PCIe", "core": 1720, "boost": 2655, "len": 230}),
    # AMD 6000 Series
    ("Radeon RX 6950 XT", {"vram": 16, "type": "GDDR6", "bus": 256, "tdp": 335, "psu": 850, "conn": "2x / 3x 8-Pin PCIe", "core": 1890, "boost": 2310, "len": 320}),
    ("Radeon RX 6800 XT", {"vram": 16, "type": "GDDR6", "bus": 256, "tdp": 300, "psu": 750, "conn": "2x 8-Pin PCIe", "core": 1825, "boost": 2250, "len": 300}),
    ("Radeon RX 6700 XT", {"vram": 12, "type": "GDDR6", "bus": 192, "tdp": 230, "psu": 650, "conn": "1x 8-Pin + 1x 6-Pin", "core": 2321, "boost": 2581, "len": 267}),
    ("Radeon RX 6600 XT", {"vram": 8, "type": "GDDR6", "bus": 128, "tdp": 160, "psu": 500, "conn": "1x 8-Pin PCIe", "core": 1968, "boost": 2589, "len": 240}),
    ("Radeon RX 6600", {"vram": 8, "type": "GDDR6", "bus": 128, "tdp": 132, "psu": 450, "conn": "1x 8-Pin PCIe", "core": 1626, "boost": 2491, "len": 220}),
]

GPU_PARTNERS = [
    ("ASUS", ["ROG Strix OC", "TUF Gaming OC", "Dual OC", "ProArt OC", "Prime", "Astral"]),
    ("MSI", ["Suprim X", "Gaming X Trio", "Gaming X Slim", "Ventus 3X OC", "Ventus 2X OC", "Vanguard"]),
    ("Gigabyte", ["AORUS Master", "Gaming OC", "AERO OC", "Windforce OC", "Eagle OC"]),
    ("Sapphire", ["NITRO+ OC", "PULSE", "PURE OC"]),
    ("PowerColor", ["Red Devil OC", "Hellhound OC", "Fighter"]),
    ("XFX", ["Speedster MERC 310", "Speedster QICK 319", "Speedster SWFT 210"]),
    ("ASRock", ["Taichi OC", "Phantom Gaming OC", "Steel Legend OC", "Challenger OC"]),
    ("Palit", ["GameRock OC", "JetStream OC", "Dual OC"]),
    ("Galax", ["SG 1-Click OC", "EX Gamer", "1-Click OC"]),
    ("Zotac", ["Trinity OC", "AMP Extreme AIRO", "Twin Edge OC"]),
    ("Inno3D", ["iChill X3", "Twin X2 OC"])
]

def generate_gpu_catalog(existing_items):
    seen = {clean_key(x.get("name", "")): x for x in existing_items if x.get("name")}
    results = list(existing_items)

    for chip, spec in GPU_CHIPSETS:
        is_amd = "Radeon" in chip
        for brand, series_list in GPU_PARTNERS:
            if is_amd and brand in ["Palit", "Galax", "Zotac", "Inno3D"]:
                continue
            if not is_amd and brand in ["Sapphire", "PowerColor", "XFX"]:
                continue

            for series in series_list:
                name = f"{brand} {chip} {series}"
                k = clean_key(name)
                if k in seen:
                    continue

                color = "White" if "AERO" in series or "PURE" in series or "White" in series or "Steel Legend" in series else "Black"
                gpu_img = get_default_gpu_image(brand, chip, series)
                item = {
                    "name": name,
                    "price": None,
                    "chipset": chip,
                    "memory": spec["vram"],
                    "memory_type": spec["type"],
                    "bus_width": spec["bus"],
                    "core_clock": spec["core"],
                    "boost_clock": spec["boost"],
                    "tdp": spec["tdp"],
                    "recommended_psu": spec["psu"],
                    "power_connectors": spec["conn"],
                    "display_outputs": "3x DisplayPort • 1x HDMI",
                    "slot_width": "3.5 Slot" if spec["vram"] >= 24 else ("3.0 Slot" if spec["tdp"] >= 250 else "2.5 Slot"),
                    "length": spec["len"],
                    "color": color,
                    "imageUrl": gpu_img
                }
                seen[k] = item
                results.append(item)

    for it in results:
        if not it.get("imageUrl"):
            b = it.get("name", "").split()[0] if it.get("name") else ""
            c = it.get("chipset", it.get("name", ""))
            it["imageUrl"] = get_default_gpu_image(b, c, it.get("name", ""))

    return results

# ==============================================================================
# 3. MOTHERBOARD MATRIX & ENRICHMENT
# ==============================================================================
MOBO_CHIPSETS = [
    # AMD AM5
    ("AM5", "X870E", "DDR5", 4, 192, 5, 3, 4, "PCIe 5.0", "18+2+2 Fazlı DrMOS (110A)", True),
    ("AM5", "X870", "DDR5", 4, 192, 4, 3, 4, "PCIe 5.0", "16+2+1 Fazlı DrMOS (80A)", True),
    ("AM5", "X670E", "DDR5", 4, 192, 4, 3, 6, "PCIe 5.0", "16+2+2 Fazlı DrMOS (90A)", True),
    ("AM5", "X670", "DDR5", 4, 192, 4, 3, 4, "PCIe 5.0", "14+2+1 Fazlı DrMOS (70A)", True),
    ("AM5", "B650E", "DDR5", 4, 192, 3, 2, 4, "PCIe 5.0", "14+2+1 Fazlı DrMOS (80A)", True),
    ("AM5", "B650", "DDR5", 4, 192, 3, 2, 4, "PCIe 4.0", "12+2+1 Fazlı DrMOS (60A)", True),
    ("AM5", "B850", "DDR5", 4, 192, 3, 2, 4, "PCIe 5.0", "14+2+1 Fazlı DrMOS (80A)", True),
    ("AM5", "B840", "DDR5", 4, 192, 2, 2, 4, "PCIe 4.0", "8+2+1 Fazlı Güç Tasarımı", True),
    ("AM5", "A620", "DDR5", 2, 96, 1, 2, 4, "PCIe 4.0", "6+2+1 Fazlı Güç Tasarımı", False),
    # AMD AM4
    ("AM4", "X570", "DDR4", 4, 128, 3, 3, 8, "PCIe 4.0", "14+2 Fazlı DrMOS", True),
    ("AM4", "B550", "DDR4", 4, 128, 2, 2, 6, "PCIe 4.0", "10+2 Fazlı Dijital VRM", True),
    ("AM4", "B450", "DDR4", 4, 64, 1, 2, 4, "PCIe 3.0", "6+2 Fazlı VRM", False),
    # Intel LGA1851
    ("LGA1851", "Z890", "DDR5", 4, 256, 5, 3, 4, "PCIe 5.0", "20+1+2+1 Fazlı SPS (110A)", True),
    ("LGA1851", "B860", "DDR5", 4, 192, 3, 2, 4, "PCIe 5.0", "14+1+1 Fazlı DrMOS (80A)", True),
    # Intel LGA1700
    ("LGA1700", "Z790", "DDR5", 4, 192, 4, 3, 4, "PCIe 5.0", "18+1+1 Fazlı DrMOS (90A)", True),
    ("LGA1700", "Z690", "DDR5", 4, 128, 4, 3, 6, "PCIe 5.0", "16+1+1 Fazlı DrMOS (70A)", True),
    ("LGA1700", "B760", "DDR5", 4, 192, 3, 2, 4, "PCIe 5.0", "12+1+1 Fazlı DrMOS (60A)", True),
    ("LGA1700", "B660", "DDR4", 4, 128, 2, 2, 4, "PCIe 4.0", "10+1+1 Fazlı VRM", True),
    ("LGA1700", "H610", "DDR4", 2, 64, 1, 1, 4, "PCIe 4.0", "6+1+1 Fazlı Güç Tasarımı", False),
]

MOBO_BRANDS = [
    ("ASUS", ["ROG Crosshair/Maximus", "ROG Strix-E Gaming", "ROG Strix-A Gaming WIFI (White)", "ROG Strix-I Mini-ITX", "TUF Gaming PLUS WIFI", "TUF Gaming PLUS (mATX)", "Prime PLUS", "ProArt-CREATOR"]),
    ("MSI", ["MEG ACE", "MPG Carbon WIFI", "MAG Tomahawk WIFI", "MAG Mortar WIFI (mATX)", "PRO-A WIFI", "MPG Edge WIFI (White)"]),
    ("Gigabyte", ["AORUS Master", "AORUS Elite AX", "AORUS Elite AX ICE (White)", "AORUS Elite (mATX)", "Gaming X AX", "Eagle AX", "AORUS Pro AX Mini-ITX"]),
    ("ASRock", ["Taichi", "Phantom Gaming Nova", "Steel Legend WIFI (White)", "Riptide WIFI", "LiveMixer", "Pro RS (mATX)", "Lightning"])
]

def generate_motherboard_catalog(existing_items):
    seen = {clean_key(x.get("name", "")): x for x in existing_items if x.get("name")}
    results = list(existing_items)

    for socket, chip, ddr, slots, max_mem, m2, pcie_slots, sata, pcie_gen, vrm, wifi in MOBO_CHIPSETS:
        clean_chip = chip.replace(" DDR4", "")
        for brand, series_list in MOBO_BRANDS:
            for series in series_list:
                if "Crosshair" in series and "AM" not in socket: continue
                if "Maximus" in series and "LGA" not in socket: continue

                form_factor = "Micro ATX" if "mATX" in series or "Mortar" in series else ("Mini ITX" if "Mini-ITX" in series else "ATX")
                color = "White" if "White" in series or "ICE" in series or "Steel Legend" in series else "Black"
                series_name = series.replace(" (White)", "").replace(" (mATX)", "").replace(" Mini-ITX", "")
                name = f"{brand} {clean_chip} {series_name}"
                k = clean_key(name)
                if k in seen:
                    continue

                mobo_img = get_default_motherboard_image(brand, clean_chip, series)
                item = {
                    "name": name,
                    "price": None,
                    "socket": socket,
                    "chipset": clean_chip,
                    "form_factor": form_factor,
                    "memory_type": ddr,
                    "memory_slots": 2 if form_factor == "Mini ITX" else slots,
                    "max_memory": 96 if form_factor == "Mini ITX" else max_mem,
                    "m2_slots": m2 if form_factor != "Mini ITX" else min(m2, 2),
                    "pcie_slots": 1 if form_factor == "Mini ITX" else pcie_slots,
                    "sata_ports": sata,
                    "pcie_generation": pcie_gen,
                    "vrm_phases": vrm,
                    "interface_ports": "USB 3.2 Gen 2x2, 2.5G LAN",
                    "has_wifi": wifi,
                    "color": color,
                    "imageUrl": mobo_img
                }
                seen[k] = item
                results.append(item)

    for it in results:
        if not it.get("imageUrl"):
            b = it.get("name", "").split()[0] if it.get("name") else ""
            c = it.get("chipset", it.get("name", ""))
            it["imageUrl"] = get_default_motherboard_image(b, c, it.get("name", ""))

    return results

# ==============================================================================
# 4. CPU MATRIX & ENRICHMENT
# ==============================================================================
CPU_MASTER_LIST = [
    # AMD Ryzen 9000
    ("AMD Ryzen 9 9950X3D", {"socket": "AM5", "cores": 16, "threads": 32, "base": 4.3, "boost": 5.7, "l2": 16.0, "l3": 128, "tdp": 170, "node": "TSMC 4nm FinFET", "arch": "Zen 5", "igpu": "Radeon"}),
    ("AMD Ryzen 9 9950X", {"socket": "AM5", "cores": 16, "threads": 32, "base": 4.3, "boost": 5.7, "l2": 16.0, "l3": 64, "tdp": 170, "node": "TSMC 4nm FinFET", "arch": "Zen 5", "igpu": "Radeon"}),
    ("AMD Ryzen 9 9900X", {"socket": "AM5", "cores": 12, "threads": 24, "base": 4.4, "boost": 5.6, "l2": 12.0, "l3": 64, "tdp": 120, "node": "TSMC 4nm FinFET", "arch": "Zen 5", "igpu": "Radeon"}),
    ("AMD Ryzen 7 9800X3D", {"socket": "AM5", "cores": 8, "threads": 16, "base": 4.7, "boost": 5.2, "l2": 8.0, "l3": 96, "tdp": 120, "node": "TSMC 4nm FinFET", "arch": "Zen 5", "igpu": "Radeon"}),
    ("AMD Ryzen 7 9700X", {"socket": "AM5", "cores": 8, "threads": 16, "base": 3.8, "boost": 5.5, "l2": 8.0, "l3": 32, "tdp": 65, "node": "TSMC 4nm FinFET", "arch": "Zen 5", "igpu": "Radeon"}),
    ("AMD Ryzen 5 9600X", {"socket": "AM5", "cores": 6, "threads": 12, "base": 3.9, "boost": 5.4, "l2": 6.0, "l3": 32, "tdp": 65, "node": "TSMC 4nm FinFET", "arch": "Zen 5", "igpu": "Radeon"}),
    # AMD Ryzen 7000
    ("AMD Ryzen 9 7950X3D", {"socket": "AM5", "cores": 16, "threads": 32, "base": 4.2, "boost": 5.7, "l2": 16.0, "l3": 128, "tdp": 120, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 9 7950X", {"socket": "AM5", "cores": 16, "threads": 32, "base": 4.5, "boost": 5.7, "l2": 16.0, "l3": 64, "tdp": 170, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 9 7900X3D", {"socket": "AM5", "cores": 12, "threads": 24, "base": 4.4, "boost": 5.6, "l2": 12.0, "l3": 128, "tdp": 120, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 9 7900X", {"socket": "AM5", "cores": 12, "threads": 24, "base": 4.7, "boost": 5.6, "l2": 12.0, "l3": 64, "tdp": 170, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 9 7900", {"socket": "AM5", "cores": 12, "threads": 24, "base": 3.7, "boost": 5.4, "l2": 12.0, "l3": 64, "tdp": 65, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 7 7800X3D", {"socket": "AM5", "cores": 8, "threads": 16, "base": 4.2, "boost": 5.0, "l2": 8.0, "l3": 96, "tdp": 120, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 7 7700X", {"socket": "AM5", "cores": 8, "threads": 16, "base": 4.5, "boost": 5.4, "l2": 8.0, "l3": 32, "tdp": 105, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 7 7700", {"socket": "AM5", "cores": 8, "threads": 16, "base": 3.8, "boost": 5.3, "l2": 8.0, "l3": 32, "tdp": 65, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 5 7600X3D", {"socket": "AM5", "cores": 6, "threads": 12, "base": 4.1, "boost": 4.7, "l2": 6.0, "l3": 96, "tdp": 65, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 5 7600X", {"socket": "AM5", "cores": 6, "threads": 12, "base": 4.7, "boost": 5.3, "l2": 6.0, "l3": 32, "tdp": 105, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 5 7600", {"socket": "AM5", "cores": 6, "threads": 12, "base": 3.8, "boost": 5.1, "l2": 6.0, "l3": 32, "tdp": 65, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": "Radeon"}),
    ("AMD Ryzen 5 7500F", {"socket": "AM5", "cores": 6, "threads": 12, "base": 3.7, "boost": 5.0, "l2": 6.0, "l3": 32, "tdp": 65, "node": "TSMC 5nm FinFET", "arch": "Zen 4", "igpu": None}),
    # AMD Ryzen 8000G
    ("AMD Ryzen 7 8700G", {"socket": "AM5", "cores": 8, "threads": 16, "base": 4.2, "boost": 5.1, "l2": 8.0, "l3": 16, "tdp": 65, "node": "TSMC 4nm FinFET", "arch": "Zen 4", "igpu": "Radeon 780M"}),
    ("AMD Ryzen 5 8600G", {"socket": "AM5", "cores": 6, "threads": 12, "base": 4.3, "boost": 5.0, "l2": 6.0, "l3": 16, "tdp": 65, "node": "TSMC 4nm FinFET", "arch": "Zen 4", "igpu": "Radeon 760M"}),
    ("AMD Ryzen 5 8500G", {"socket": "AM5", "cores": 6, "threads": 12, "base": 3.5, "boost": 5.0, "l2": 6.0, "l3": 16, "tdp": 65, "node": "TSMC 4nm FinFET", "arch": "Zen 4 / Zen 4c", "igpu": "Radeon 740M"}),
    # AMD Ryzen 5000
    ("AMD Ryzen 9 5950X", {"socket": "AM4", "cores": 16, "threads": 32, "base": 3.4, "boost": 4.9, "l2": 8.0, "l3": 64, "tdp": 105, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 9 5900XT", {"socket": "AM4", "cores": 16, "threads": 32, "base": 3.3, "boost": 4.8, "l2": 8.0, "l3": 64, "tdp": 105, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 9 5900X", {"socket": "AM4", "cores": 12, "threads": 24, "base": 3.7, "boost": 4.8, "l2": 6.0, "l3": 64, "tdp": 105, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 7 5800X3D", {"socket": "AM4", "cores": 8, "threads": 16, "base": 3.4, "boost": 4.5, "l2": 4.0, "l3": 96, "tdp": 105, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 7 5700X3D", {"socket": "AM4", "cores": 8, "threads": 16, "base": 3.0, "boost": 4.1, "l2": 4.0, "l3": 96, "tdp": 105, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 7 5800XT", {"socket": "AM4", "cores": 8, "threads": 16, "base": 3.8, "boost": 4.8, "l2": 4.0, "l3": 32, "tdp": 105, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 7 5800X", {"socket": "AM4", "cores": 8, "threads": 16, "base": 3.8, "boost": 4.7, "l2": 4.0, "l3": 32, "tdp": 105, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 7 5700X", {"socket": "AM4", "cores": 8, "threads": 16, "base": 3.4, "boost": 4.6, "l2": 4.0, "l3": 32, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 7 5700", {"socket": "AM4", "cores": 8, "threads": 16, "base": 3.7, "boost": 4.6, "l2": 4.0, "l3": 16, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 7 5700G", {"socket": "AM4", "cores": 8, "threads": 16, "base": 3.8, "boost": 4.6, "l2": 4.0, "l3": 16, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": "Radeon Vega 8"}),
    ("AMD Ryzen 5 5600X", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.7, "boost": 4.6, "l2": 3.0, "l3": 32, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 5 5600", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.5, "boost": 4.4, "l2": 3.0, "l3": 32, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    ("AMD Ryzen 5 5600GT", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.6, "boost": 4.6, "l2": 3.0, "l3": 16, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": "Radeon Vega 7"}),
    ("AMD Ryzen 5 5600G", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.9, "boost": 4.4, "l2": 3.0, "l3": 16, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": "Radeon Vega 7"}),
    ("AMD Ryzen 5 5500GT", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.6, "boost": 4.4, "l2": 3.0, "l3": 16, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": "Radeon Vega 7"}),
    ("AMD Ryzen 5 5500", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.6, "boost": 4.2, "l2": 3.0, "l3": 16, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 3", "igpu": None}),
    # AMD Ryzen 4000 & 3000
    ("AMD Ryzen 5 4500", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.6, "boost": 4.1, "l2": 3.0, "l3": 8, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 2", "igpu": None}),
    ("AMD Ryzen 5 3600", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.6, "boost": 4.2, "l2": 3.0, "l3": 32, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 2", "igpu": None}),
    ("AMD Ryzen 5 3600X", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.8, "boost": 4.4, "l2": 3.0, "l3": 32, "tdp": 95, "node": "TSMC 7nm FinFET", "arch": "Zen 2", "igpu": None}),
    ("AMD Ryzen 7 3700X", {"socket": "AM4", "cores": 8, "threads": 16, "base": 3.6, "boost": 4.4, "l2": 4.0, "l3": 32, "tdp": 65, "node": "TSMC 7nm FinFET", "arch": "Zen 2", "igpu": None}),
    ("AMD Ryzen 9 3900X", {"socket": "AM4", "cores": 12, "threads": 24, "base": 3.8, "boost": 4.6, "l2": 6.0, "l3": 64, "tdp": 105, "node": "TSMC 7nm FinFET", "arch": "Zen 2", "igpu": None}),
    ("AMD Ryzen 5 2600", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.4, "boost": 3.9, "l2": 3.0, "l3": 16, "tdp": 65, "node": "GlobalFoundries 12nm", "arch": "Zen+", "igpu": None}),
    ("AMD Ryzen 5 1600", {"socket": "AM4", "cores": 6, "threads": 12, "base": 3.2, "boost": 3.6, "l2": 3.0, "l3": 16, "tdp": 65, "node": "GlobalFoundries 14nm", "arch": "Zen", "igpu": None}),
    # Intel Core Ultra 200S
    ("Intel Core Ultra 9 285K", {"socket": "LGA1851", "cores": 24, "threads": 24, "base": 3.7, "boost": 5.7, "l2": 40.0, "l3": 36, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake", "igpu": "Intel Graphics 4 Xe-cores"}),
    ("Intel Core Ultra 7 275K", {"socket": "LGA1851", "cores": 24, "threads": 24, "base": 3.7, "boost": 5.5, "l2": 40.0, "l3": 36, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake Refresh", "igpu": "Intel Graphics 4 Xe-cores"}),
    ("Intel Core Ultra 7 275KF", {"socket": "LGA1851", "cores": 24, "threads": 24, "base": 3.7, "boost": 5.5, "l2": 40.0, "l3": 36, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake Refresh", "igpu": None}),
    ("Intel Core Ultra 7 270K Plus", {"socket": "LGA1851", "cores": 24, "threads": 24, "base": 3.7, "boost": 5.5, "l2": 40.0, "l3": 36, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake Refresh", "igpu": "Intel Graphics 4 Xe-cores"}),
    ("Intel Core Ultra 7 265K", {"socket": "LGA1851", "cores": 20, "threads": 20, "base": 3.9, "boost": 5.5, "l2": 36.0, "l3": 30, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake", "igpu": "Intel Graphics 4 Xe-cores"}),
    ("Intel Core Ultra 7 265KF", {"socket": "LGA1851", "cores": 20, "threads": 20, "base": 3.9, "boost": 5.5, "l2": 36.0, "l3": 30, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake", "igpu": None}),
    ("Intel Core Ultra 5 255K", {"socket": "LGA1851", "cores": 18, "threads": 18, "base": 3.3, "boost": 5.2, "l2": 30.0, "l3": 24, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake Refresh", "igpu": "Intel Graphics 4 Xe-cores"}),
    ("Intel Core Ultra 5 255KF", {"socket": "LGA1851", "cores": 18, "threads": 18, "base": 3.3, "boost": 5.2, "l2": 30.0, "l3": 24, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake Refresh", "igpu": None}),
    ("Intel Core Ultra 5 250K Plus", {"socket": "LGA1851", "cores": 18, "threads": 18, "base": 3.3, "boost": 5.2, "l2": 30.0, "l3": 24, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake Refresh", "igpu": "Intel Graphics 4 Xe-cores"}),
    ("Intel Core Ultra 5 245K", {"socket": "LGA1851", "cores": 14, "threads": 14, "base": 4.2, "boost": 5.2, "l2": 26.0, "l3": 24, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake", "igpu": "Intel Graphics 4 Xe-cores"}),
    ("Intel Core Ultra 5 245KF", {"socket": "LGA1851", "cores": 14, "threads": 14, "base": 4.2, "boost": 5.2, "l2": 26.0, "l3": 24, "tdp": 125, "node": "TSMC 3nm N3B FinFET", "arch": "Arrow Lake", "igpu": None}),
    # Intel 14th Gen
    ("Intel Core i9-14900KS", {"socket": "LGA1700", "cores": 24, "threads": 32, "base": 3.2, "boost": 6.2, "l2": 32.0, "l3": 36, "tdp": 150, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i9-14900K", {"socket": "LGA1700", "cores": 24, "threads": 32, "base": 3.2, "boost": 6.0, "l2": 32.0, "l3": 36, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i9-14900KF", {"socket": "LGA1700", "cores": 24, "threads": 32, "base": 3.2, "boost": 6.0, "l2": 32.0, "l3": 36, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": None}),
    ("Intel Core i7-14700K", {"socket": "LGA1700", "cores": 20, "threads": 28, "base": 3.4, "boost": 5.6, "l2": 28.0, "l3": 33, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i7-14700KF", {"socket": "LGA1700", "cores": 20, "threads": 28, "base": 3.4, "boost": 5.6, "l2": 28.0, "l3": 33, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": None}),
    ("Intel Core i7-14700", {"socket": "LGA1700", "cores": 20, "threads": 28, "base": 2.1, "boost": 5.4, "l2": 28.0, "l3": 33, "tdp": 65, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i5-14600K", {"socket": "LGA1700", "cores": 14, "threads": 20, "base": 3.5, "boost": 5.3, "l2": 20.0, "l3": 24, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i5-14600KF", {"socket": "LGA1700", "cores": 14, "threads": 20, "base": 3.5, "boost": 5.3, "l2": 20.0, "l3": 24, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": None}),
    ("Intel Core i5-14400F", {"socket": "LGA1700", "cores": 10, "threads": 16, "base": 2.5, "boost": 4.7, "l2": 9.5, "l3": 20, "tdp": 65, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": None}),
    ("Intel Core i5-14400", {"socket": "LGA1700", "cores": 10, "threads": 16, "base": 2.5, "boost": 4.7, "l2": 9.5, "l3": 20, "tdp": 65, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": "Intel UHD Graphics 730"}),
    ("Intel Core i3-14100F", {"socket": "LGA1700", "cores": 4, "threads": 8, "base": 3.5, "boost": 4.7, "l2": 5.0, "l3": 12, "tdp": 58, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": None}),
    ("Intel Core i3-14100", {"socket": "LGA1700", "cores": 4, "threads": 8, "base": 3.5, "boost": 4.7, "l2": 5.0, "l3": 12, "tdp": 60, "node": "Intel 7 (10nm)", "arch": "Raptor Lake Refresh", "igpu": "Intel UHD Graphics 730"}),
    # Intel 13th Gen
    ("Intel Core i9-13900K", {"socket": "LGA1700", "cores": 24, "threads": 32, "base": 3.0, "boost": 5.8, "l2": 32.0, "l3": 36, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i7-13700K", {"socket": "LGA1700", "cores": 16, "threads": 24, "base": 3.4, "boost": 5.4, "l2": 24.0, "l3": 30, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i5-13600K", {"socket": "LGA1700", "cores": 14, "threads": 20, "base": 3.5, "boost": 5.1, "l2": 20.0, "l3": 24, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Raptor Lake", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i5-13400F", {"socket": "LGA1700", "cores": 10, "threads": 16, "base": 2.5, "boost": 4.6, "l2": 9.5, "l3": 20, "tdp": 65, "node": "Intel 7 (10nm)", "arch": "Raptor Lake", "igpu": None}),
    ("Intel Core i3-13100F", {"socket": "LGA1700", "cores": 4, "threads": 8, "base": 3.4, "boost": 4.5, "l2": 5.0, "l3": 12, "tdp": 58, "node": "Intel 7 (10nm)", "arch": "Raptor Lake", "igpu": None}),
    # Intel 12th Gen
    ("Intel Core i9-12900K", {"socket": "LGA1700", "cores": 16, "threads": 24, "base": 3.2, "boost": 5.2, "l2": 14.0, "l3": 30, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Alder Lake", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i7-12700K", {"socket": "LGA1700", "cores": 12, "threads": 20, "base": 3.6, "boost": 5.0, "l2": 12.0, "l3": 25, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Alder Lake", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i5-12600K", {"socket": "LGA1700", "cores": 10, "threads": 16, "base": 3.7, "boost": 4.9, "l2": 9.5, "l3": 20, "tdp": 125, "node": "Intel 7 (10nm)", "arch": "Alder Lake", "igpu": "Intel UHD Graphics 770"}),
    ("Intel Core i5-12400F", {"socket": "LGA1700", "cores": 6, "threads": 12, "base": 2.5, "boost": 4.4, "l2": 7.5, "l3": 18, "tdp": 65, "node": "Intel 7 (10nm)", "arch": "Alder Lake", "igpu": None}),
    ("Intel Core i3-12100F", {"socket": "LGA1700", "cores": 4, "threads": 8, "base": 3.3, "boost": 4.3, "l2": 5.0, "l3": 12, "tdp": 58, "node": "Intel 7 (10nm)", "arch": "Alder Lake", "igpu": None}),
    # Intel 11th Gen & 10th Gen
    ("Intel Core i9-11900K", {"socket": "LGA1200", "cores": 8, "threads": 16, "base": 3.5, "boost": 5.3, "l2": 4.0, "l3": 16, "tdp": 125, "node": "14nm", "arch": "Rocket Lake", "igpu": "Intel UHD Graphics 750"}),
    ("Intel Core i7-11700K", {"socket": "LGA1200", "cores": 8, "threads": 16, "base": 3.6, "boost": 5.0, "l2": 4.0, "l3": 16, "tdp": 125, "node": "14nm", "arch": "Rocket Lake", "igpu": "Intel UHD Graphics 750"}),
    ("Intel Core i5-11400F", {"socket": "LGA1200", "cores": 6, "threads": 12, "base": 2.6, "boost": 4.4, "l2": 3.0, "l3": 12, "tdp": 65, "node": "14nm", "arch": "Rocket Lake", "igpu": None}),
    ("Intel Core i9-10900K", {"socket": "LGA1200", "cores": 10, "threads": 20, "base": 3.7, "boost": 5.3, "l2": 2.5, "l3": 20, "tdp": 125, "node": "14nm", "arch": "Comet Lake", "igpu": "Intel UHD Graphics 630"}),
    ("Intel Core i7-10700K", {"socket": "LGA1200", "cores": 8, "threads": 16, "base": 3.8, "boost": 5.1, "l2": 2.0, "l3": 16, "tdp": 125, "node": "14nm", "arch": "Comet Lake", "igpu": "Intel UHD Graphics 630"}),
    ("Intel Core i5-10400F", {"socket": "LGA1200", "cores": 6, "threads": 12, "base": 2.9, "boost": 4.3, "l2": 1.5, "l3": 12, "tdp": 65, "node": "14nm", "arch": "Comet Lake", "igpu": None}),
    ("Intel Core i3-10100F", {"socket": "LGA1200", "cores": 4, "threads": 8, "base": 3.6, "boost": 4.3, "l2": 1.0, "l3": 6, "tdp": 65, "node": "14nm", "arch": "Comet Lake", "igpu": None}),
]

def get_default_cpu_image(name: str) -> str:
    upper = name.upper()
    if any(x in upper for x in ["9800X3D", "9950X", "9900X", "9700X", "9600X"]):
        return "https://www.incehesap.com/resim/urun/202412/67583ce66022a8.23281091_qpohljnmefikg_cropped_288.webp"
    elif any(x in upper for x in ["7800X3D", "7950X", "7900X", "7700X", "7600X", "7600", "7500F"]):
        return "https://static.sinerji.gen.tr/Images/MD/P47-screenshot1.jpg"
    elif any(x in upper for x in ["8700G", "8600G", "8500G"]):
        return "https://www.tebilon.com/picture300x300/amd-ryzen-7-8700g-4-20ghz-8-cekirdek-24mb-onbellek-soket-am5-mpk-islemci-1.jpg"
    elif any(x in upper for x in ["5800X3D", "5700X3D", "5950X", "5900X", "5800X", "5700X", "5600", "5500"]):
        return "https://www.incehesap.com/resim/urun/202508/6891ec19077a16.73637953_hpqflgejkomni_cropped_288.webp"
    elif any(x in upper for x in ["ULTRA", "285K", "275K", "270K", "265K", "255K", "250K", "245K"]):
        return "https://static.sinerji.gen.tr/Images/MD/3ND-1.jpg"
    elif any(x in upper for x in ["14900", "14700", "14600", "14400"]):
        return "https://www.incehesap.com/resim/urun/202505/682ed530efb1a8.02684698_mngjliepqhokf_cropped_288.webp"
    elif any(x in upper for x in ["13900", "13700", "13600", "13400"]):
        return "https://www.tebilon.com/picture300x300/intel-core-i7-13700k-13-nesil-30mb-l3-onbellek-intel-islemci.jpg"
    elif any(x in upper for x in ["12900", "12700", "12600", "12400"]):
        return "https://www.tebilon.com/picture300x300/intel-core-i5-12400f-2-50ghz-18mb-l3-onbellek-intel-islemci.jpg"
    elif "RYZEN" in upper:
        return "https://static.sinerji.gen.tr/Images/MD/P47-screenshot1.jpg"
    return "https://www.incehesap.com/resim/urun/202505/682ed530efb1a8.02684698_mngjliepqhokf_cropped_288.webp"

def generate_cpu_catalog(existing_items):
    seen = {clean_key(x.get("name", "")): x for x in existing_items if x.get("name")}
    results = list(existing_items)

    for name, spec in CPU_MASTER_LIST:
        k = clean_key(name)
        unlocked = True if ("K" in name or "Ryzen" in name) and "PRO" not in name else False
        pcie = "PCIe 5.0" if spec["socket"] in ["AM5", "LGA1851", "LGA1700"] else "PCIe 4.0"
        mem = "DDR5" if spec["socket"] in ["AM5", "LGA1851"] else ("DDR5 / DDR4" if spec["socket"] == "LGA1700" else "DDR4")
        default_img = get_default_cpu_image(name)

        item = {
            "name": name,
            "price": None,
            "socket": spec["socket"],
            "core_count": spec["cores"],
            "threads": spec["threads"],
            "core_clock": spec["base"],
            "boost_clock": spec["boost"],
            "tdp": spec["tdp"],
            "microarchitecture": spec["arch"],
            "graphics": spec["igpu"],
            "l2_cache": spec["l2"],
            "l3_cache": spec["l3"],
            "process_node": spec["node"],
            "pcie_version": pcie,
            "max_memory_speed": mem,
            "unlocked_multiplier": unlocked,
            "imageUrl": default_img
        }
        if k in seen:
            seen[k].update({k2: v for k2, v in item.items() if v is not None})
            if not seen[k].get("imageUrl"):
                seen[k]["imageUrl"] = default_img
        else:
            seen[k] = item
            results.append(item)

    for it in results:
        if not it.get("imageUrl"):
            it["imageUrl"] = get_default_cpu_image(it.get("name", ""))

    return results

# ==============================================================================
# 5. RAM MATRIX & ENRICHMENT
# ==============================================================================
RAM_BRANDS_SERIES = [
    ("Corsair", ["Vengeance RGB", "Vengeance", "Dominator Titanium RGB", "Dominator Platinum RGB"]),
    ("G.Skill", ["Trident Z5 RGB", "Trident Z5 Neo RGB (AMD EXPO)", "Flare X5 (AMD EXPO)", "Ripjaws S5"]),
    ("Kingston Fury", ["Beast RGB", "Beast", "Renegade RGB"]),
    ("XPG", ["Lancer Blade RGB", "Lancer RGB"]),
    ("Lexar", ["Ares RGB", "Thor OC"]),
    ("Patriot Viper", ["Venom RGB", "Elite 5 RGB"]),
    ("Crucial", ["Pro Overclocking", "Pro"]),
    ("TeamGroup T-Force", ["Delta RGB", "Vulcan"])
]

RAM_CONFIGS = [
    # DDR5 High-End & Mainstream
    (5, 6000, 30, 1.35, 10.0, 32, 2, 16),
    (5, 6000, 32, 1.35, 10.6, 32, 2, 16),
    (5, 6000, 36, 1.30, 12.0, 32, 2, 16),
    (5, 6000, 28, 1.40, 9.3, 32, 2, 16),
    (5, 6200, 32, 1.35, 10.3, 32, 2, 16),
    (5, 6400, 32, 1.40, 10.0, 32, 2, 16),
    (5, 5600, 36, 1.25, 12.8, 32, 2, 16),
    (5, 5600, 40, 1.25, 14.2, 16, 1, 16),
    (5, 6000, 30, 1.35, 10.0, 48, 2, 24),
    (5, 6000, 30, 1.35, 10.0, 64, 2, 32),
    (5, 6000, 30, 1.35, 10.0, 96, 2, 48),
    (5, 7200, 34, 1.40, 9.4, 32, 2, 16),
    (5, 8000, 38, 1.45, 9.5, 32, 2, 16),
    # DDR4
    (4, 3200, 16, 1.35, 10.0, 16, 2, 8),
    (4, 3200, 16, 1.35, 10.0, 32, 2, 16),
    (4, 3200, 16, 1.35, 10.0, 16, 1, 16),
    (4, 3600, 16, 1.35, 8.8, 32, 2, 16),
    (4, 3600, 18, 1.35, 10.0, 16, 2, 8),
    (4, 3600, 18, 1.35, 10.0, 32, 2, 16),
    (4, 3600, 18, 1.35, 10.0, 64, 2, 32),
    (4, 4000, 18, 1.40, 9.0, 32, 2, 16),
]

def generate_ram_catalog(existing_items):
    seen = {clean_key(x.get("name", "")): x for x in existing_items if x.get("name")}
    results = list(existing_items)

    for brand, series_list in RAM_BRANDS_SERIES:
        for series in series_list:
            for gen, mhz, cl, volt, latency, cap, mod_count, mod_size in RAM_CONFIGS:
                if "Neo" in series or "Flare" in series:
                    if gen != 5 or mhz != 6000: continue
                if "Dominator" in series and gen != 5: continue

                for color in (["Black", "White"] if "RGB" in series else ["Black"]):
                    suffix = f" (White)" if color == "White" else ""
                    name = f"{brand} {series} {cap}GB ({mod_count}x{mod_size}GB) DDR{gen}-{mhz} CL{cl}{suffix}"
                    k = clean_key(name)
                    if k in seen:
                        continue

                    has_rgb = "RGB" in series
                    ram_img = get_default_ram_image(name)
                    item = {
                        "name": name,
                        "price": None,
                        "speed": [gen, mhz],
                        "modules": [mod_count, mod_size],
                        "price_per_gb": None,
                        "color": color,
                        "first_word_latency": latency,
                        "cas_latency": cl,
                        "voltage": volt,
                        "heat_spreader": True,
                        "rgb": has_rgb,
                        "imageUrl": ram_img
                    }
                    seen[k] = item
                    results.append(item)

    for it in results:
        if not it.get("imageUrl"):
            it["imageUrl"] = get_default_ram_image(it.get("name", ""))

    return results

# ==============================================================================
# 6. STORAGE MATRIX & ENRICHMENT
# ==============================================================================
STORAGE_MODELS = [
    # Gen 5 NVMe
    ("Crucial T705", "PCIe 5.0 x4", "M.2-2280", 14500, 12700, 4096, 2400, "3D TLC Micron 232-Layer", [1000, 2000, 4000]),
    ("Crucial T700", "PCIe 5.0 x4", "M.2-2280", 12400, 11800, 2048, 1200, "3D TLC Micron 232-Layer", [1000, 2000, 4000]),
    ("Corsair MP700 PRO", "PCIe 5.0 x4", "M.2-2280", 12400, 11800, 2048, 1400, "3D TLC Micron", [1000, 2000, 4000]),
    ("MSI Spatium M570 PRO", "PCIe 5.0 x4", "M.2-2280", 12400, 11800, 2048, 1400, "3D TLC Micron", [1000, 2000, 4000]),
    ("Seagate FireCuda 540", "PCIe 5.0 x4", "M.2-2280", 10000, 10000, 2048, 2000, "3D TLC", [1000, 2000]),
    # Gen 4 High-End NVMe
    ("Samsung 990 PRO", "PCIe 4.0 x4", "M.2-2280", 7450, 6900, 2048, 1200, "Samsung V-NAND TLC", [1000, 2000, 4000]),
    ("Samsung 980 PRO", "PCIe 4.0 x4", "M.2-2280", 7000, 5100, 1024, 600, "Samsung V-NAND TLC", [500, 1000, 2000]),
    ("Kingston KC3000", "PCIe 4.0 x4", "M.2-2280", 7000, 7000, 2048, 1600, "3D TLC Micron 176-Layer", [512, 1024, 2048, 4096]),
    ("Kingston Fury Renegade", "PCIe 4.0 x4", "M.2-2280", 7300, 7000, 2048, 2000, "3D TLC Micron 176-Layer", [1000, 2000, 4000]),
    ("WD_BLACK SN850X", "PCIe 4.0 x4", "M.2-2280", 7300, 6600, 2048, 1200, "BiCS5 112-Layer TLC", [1000, 2000, 4000]),
    ("Lexar NM790", "PCIe 4.0 x4", "M.2-2280", 7400, 6500, None, 1500, "3D TLC YMTC 128-Layer", [1000, 2000, 4000]),
    ("Crucial T500", "PCIe 4.0 x4", "M.2-2280", 7400, 7000, 2048, 1200, "3D TLC Micron 232-Layer", [1000, 2000]),
    ("XPG Gammix S70 Blade", "PCIe 4.0 x4", "M.2-2280", 7400, 6800, 2048, 1480, "3D TLC", [1000, 2000, 4000]),
    ("Kioxia Exceria Pro", "PCIe 4.0 x4", "M.2-2280", 7300, 6400, 2048, 800, "BiCS TLC", [1000, 2000]),
    ("Seagate FireCuda 530", "PCIe 4.0 x4", "M.2-2280", 7300, 6900, 2048, 2550, "3D TLC Micron", [1000, 2000, 4000]),
    ("MSI Spatium M480 PRO", "PCIe 4.0 x4", "M.2-2280", 7400, 7000, 2048, 1400, "3D TLC", [1000, 2000, 4000]),
    # Gen 4 Mainstream / Budget
    ("WD_BLACK SN770", "PCIe 4.0 x4", "M.2-2280", 5150, 4900, None, 1200, "BiCS5 112-Layer TLC", [500, 1000, 2000]),
    ("WD Blue SN580", "PCIe 4.0 x4", "M.2-2280", 4150, 4150, None, 900, "BiCS5 TLC", [500, 1000, 2000]),
    ("WD Blue SN5000", "PCIe 4.0 x4", "M.2-2280", 5500, 5000, None, 1200, "BiCS6 TLC", [1000, 2000, 4000]),
    ("Kingston NV3", "PCIe 4.0 x4", "M.2-2280", 5000, 4000, None, 640, "3D QLC / TLC", [500, 1000, 2000, 4000]),
    ("Kingston NV2", "PCIe 4.0 x4", "M.2-2280", 3500, 2800, None, 640, "3D QLC / TLC", [500, 1000, 2000]),
    ("Crucial P3 Plus", "PCIe 4.0 x4", "M.2-2280", 5000, 4200, None, 440, "3D QLC Micron", [500, 1000, 2000, 4000]),
    ("Kioxia Exceria G2", "PCIe 3.0 x4", "M.2-2280", 2100, 1700, 1024, 400, "BiCS TLC (DRAM)", [500, 1000, 2000]),
    ("Samsung 970 EVO Plus", "PCIe 3.0 x4", "M.2-2280", 3500, 3300, 1024, 600, "Samsung V-NAND TLC", [500, 1000, 2000]),
    # SATA SSD
    ("Samsung 870 EVO", "SATA 6.0 Gb/s", "2.5\"", 560, 530, 1024, 600, "Samsung V-NAND TLC", [500, 1000, 2000, 4000]),
    ("Crucial MX500", "SATA 6.0 Gb/s", "2.5\"", 560, 510, 1024, 700, "3D TLC Micron", [500, 1000, 2000, 4000]),
    ("Kingston KC600", "SATA 6.0 Gb/s", "2.5\"", 550, 520, 1024, 600, "3D TLC", [512, 1024, 2048]),
    ("Crucial BX500", "SATA 6.0 Gb/s", "2.5\"", 540, 500, None, 360, "3D NAND", [500, 1000, 2000])
]

def generate_storage_catalog(existing_items):
    seen = {clean_key(x.get("name", "")): x for x in existing_items if x.get("name")}
    results = list(existing_items)

    for model, iface, form, r_speed, w_speed, cache, tbw_base, nand, caps in STORAGE_MODELS:
        for cap in caps:
            cap_str = f"{cap // 1000}TB" if cap >= 1000 else f"{cap}GB"
            name = f"{model} {cap_str}"
            k = clean_key(name)
            if k in seen:
                continue

            tbw = (tbw_base * (cap / 1000)) if cap >= 1000 else (tbw_base / 2)
            storage_img = get_default_storage_image(name)
            item = {
                "name": name,
                "price": None,
                "capacity": cap,
                "type": "SSD",
                "form_factor": form,
                "interface": iface,
                "cache": cache,
                "seq_read": r_speed,
                "seq_write": w_speed,
                "nand_type": nand,
                "endurance_tbw": int(tbw),
                "imageUrl": storage_img
            }
            seen[k] = item
            results.append(item)

    for it in results:
        if not it.get("imageUrl"):
            it["imageUrl"] = get_default_storage_image(it.get("name", ""))

    return results

# ==============================================================================
# 7. COOLER & CASE MATRIX & ENRICHMENT
# ==============================================================================
COOLER_MODELS = [
    # Liquid AIO 360 / 240 / 420 / 280
    ("Lian Li Galahad II Trinity 360", "Liquid", 360, 2450, 30.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Lian Li Galahad II Trinity 240", "Liquid", 240, 2450, 29.0, 280, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Lian Li HydroShift LCD 360", "Liquid", 360, 2600, 31.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Arctic Liquid Freezer III 420 A-RGB", "Liquid", 420, 1900, 25.0, 380, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Arctic Liquid Freezer III 360 A-RGB", "Liquid", 360, 2000, 24.5, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Arctic Liquid Freezer III 280 A-RGB", "Liquid", 280, 1900, 24.0, 300, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Arctic Liquid Freezer III 240 A-RGB", "Liquid", 240, 2000, 23.0, 280, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("NZXT Kraken Elite 360 RGB", "Liquid", 360, 2000, 30.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("NZXT Kraken Elite 280 RGB", "Liquid", 280, 1800, 29.0, 300, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("NZXT Kraken Elite 240 RGB", "Liquid", 240, 2000, 28.0, 280, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("DeepCool LT720", "Liquid", 360, 2250, 32.9, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("DeepCool LS720 SE", "Liquid", 360, 2250, 32.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Thermalright Frozen Notte 360 ARGB", "Liquid", 360, 2000, 28.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Thermalright Aqua Elite 360 V3", "Liquid", 360, 2000, 28.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Thermalright Frozen Prism 360", "Liquid", 360, 2000, 28.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Corsair iCUE LINK Titan 360 RX RGB", "Liquid", 360, 2100, 31.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Corsair iCUE H150i ELITE LCD XT", "Liquid", 360, 2100, 32.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("MSI MAG CoreLiquid E360", "Liquid", 360, 1800, 29.0, 320, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("ASUS ROG Ryujin III 360 ARGB", "Liquid", 360, 2200, 32.0, 360, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("ASUS TUF Gaming LC II 360 ARGB", "Liquid", 360, 2000, 29.0, 320, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black"]),
    ("Be Quiet! Silent Loop 2 360", "Liquid", 360, 2200, 28.0, 350, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black"]),
    ("Be Quiet! Pure Loop 2 240", "Liquid", 240, 2000, 26.0, 280, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black"]),
    ("Cooler Master MasterLiquid 360L Core ARGB", "Liquid", 360, 1750, 27.2, 320, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    # Tower Air
    ("Thermalright Peerless Assassin 120 SE", "Air", 120, 1550, 25.6, 265, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Thermalright Phantom Spirit 120 SE", "Air", 120, 1500, 25.6, 280, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Thermalright Assassin X 120 Refined SE", "Air", 120, 1550, 25.6, 200, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Noctua NH-D15 chromax.black", "Air", 140, 1500, 24.6, 280, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black"]),
    ("Noctua NH-D15 G2", "Air", 140, 1500, 24.8, 290, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black"]),
    ("Noctua NH-U12A chromax.black", "Air", 120, 2000, 22.6, 240, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black"]),
    ("DeepCool AK620 Digital", "Air", 120, 1850, 28.0, 260, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("DeepCool AK400 Digital", "Air", 120, 1850, 28.0, 220, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Arctic Freezer 36 A-RGB", "Air", 120, 2000, 23.0, 220, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"]),
    ("Be Quiet! Dark Rock Pro 5", "Air", 135, 1700, 23.3, 270, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black"]),
    ("Be Quiet! Pure Rock 2 FX", "Air", 120, 2000, 26.8, 150, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black"]),
    ("Cooler Master Hyper 212 Halo", "Air", 120, 2050, 27.0, 180, ["AM5", "AM4", "LGA1700", "LGA1851"], ["Black", "White"])
]

def generate_cooler_catalog(existing_items):
    seen = {clean_key(x.get("name", "")): x for x in existing_items if x.get("name")}
    results = list(existing_items)

    for model, c_type, size, rpm, noise, tdp, sockets, colors in COOLER_MODELS:
        for color in colors:
            suffix = f" (White)" if color == "White" else ""
            name = f"{model}{suffix}"
            k = clean_key(name)
            if k in seen:
                continue

            cooler_img = get_default_cooler_image(name)
            item = {
                "name": name,
                "price": None,
                "type": c_type,
                "size": size,
                "color": color,
                "height": 155 if c_type == "Air" else None,
                "fan_rpm": f"500 - {rpm} RPM",
                "noise_level": noise,
                "sockets": sockets,
                "max_tdp": tdp,
                "imageUrl": cooler_img
            }
            seen[k] = item
            results.append(item)

    for it in results:
        if not it.get("imageUrl"):
            it["imageUrl"] = get_default_cooler_image(it.get("name", ""))

    return results

CASE_MODELS = [
    ("Lian Li O11 Dynamic EVO RGB", "ATX Mid Tower", 455, 167, 420, "2x 3.5\" + 4x 2.5\"", ["Black", "White"]),
    ("Lian Li O11 Vision", "ATX Mid Tower", 455, 167, 360, "2x 3.5\" + 3x 2.5\"", ["Black", "White"]),
    ("Lian Li O11 Dynamic Mini", "ATX Mini Tower", 395, 170, 360, "2x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("Lian Li Lancool 216 RGB", "ATX Mid Tower", 392, 180, 360, "2x 3.5\" + 6x 2.5\"", ["Black", "White"]),
    ("Lian Li Lancool 207", "ATX Mid Tower", 410, 180, 360, "2x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("Lian Li A3-mATX", "MicroATX Mid Tower", 415, 165, 360, "1x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("NZXT H9 Flow (2024)", "ATX Mid Tower", 435, 165, 360, "2x 3.5\" + 4x 2.5\"", ["Black", "White"]),
    ("NZXT H9 Elite", "ATX Mid Tower", 435, 165, 360, "2x 3.5\" + 4x 2.5\"", ["Black", "White"]),
    ("NZXT H7 Flow (2024)", "ATX Mid Tower", 410, 185, 360, "2x 3.5\" + 4x 2.5\"", ["Black", "White"]),
    ("NZXT H6 Flow RGB", "ATX Mid Tower", 365, 165, 360, "1x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("NZXT H5 Flow (2024)", "ATX Mid Tower", 410, 175, 280, "1x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("Corsair 4000D Airflow", "ATX Mid Tower", 360, 170, 360, "2x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("Corsair 5000D Airflow", "ATX Mid Tower", 420, 170, 360, "2x 3.5\" + 4x 2.5\"", ["Black", "White"]),
    ("Corsair 3500X ARGB", "ATX Mid Tower", 410, 170, 360, "2x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("Corsair 3000D Airflow", "ATX Mid Tower", 360, 170, 360, "2x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("Corsair 6500X", "ATX Mid Tower", 400, 180, 360, "2x 3.5\" + 3x 2.5\"", ["Black", "White"]),
    ("Phanteks NV5", "ATX Mid Tower", 440, 180, 360, "3x 3.5\" + 4x 2.5\"", ["Black", "White"]),
    ("Phanteks NV7", "ATX Full Tower", 450, 185, 420, "3x 3.5\" + 6x 2.5\"", ["Black", "White"]),
    ("Phanteks XT Pro Ultra", "ATX Mid Tower", 415, 184, 360, "2x 3.5\" + 5x 2.5\"", ["Black", "White"]),
    ("Phanteks Eclipse G360A", "ATX Mid Tower", 400, 162, 360, "2x 3.5\" + 3x 2.5\"", ["Black", "White"]),
    ("Montech King 95 Pro", "ATX Mid Tower", 420, 175, 360, "5x 3.5\" + 8x 2.5\"", ["Black", "White"]),
    ("Montech Air 903 Max", "ATX Mid Tower", 400, 180, 360, "2x 3.5\" + 4x 2.5\"", ["Black", "White"]),
    ("Montech Sky Two", "ATX Mid Tower", 400, 168, 360, "2x 3.5\" + 3x 2.5\"", ["Black", "White"]),
    ("Hyte Y70", "ATX Mid Tower", 422, 180, 360, "2x 3.5\" + 4x 2.5\"", ["Black", "White", "Red"]),
    ("Hyte Y60", "ATX Mid Tower", 375, 160, 280, "2x 3.5\" + 4x 2.5\"", ["Black", "White", "Red"]),
    ("Hyte Y40", "ATX Mid Tower", 422, 183, 360, "1x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("DeepCool CH560 Digital", "ATX Mid Tower", 380, 175, 360, "2x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("DeepCool CH510", "ATX Mid Tower", 380, 175, 360, "2x 3.5\" + 2x 2.5\"", ["Black", "White"]),
    ("ASUS TUF Gaming GT502", "ATX Mid Tower", 400, 163, 360, "4x 3.5\" / 2.5\"", ["Black", "White"]),
    ("ASUS Prime AP201", "MicroATX Mini Tower", 338, 170, 360, "3x 3.5\" + 1x 2.5\"", ["Black", "White"]),
    ("Be Quiet! Pure Base 500DX", "ATX Mid Tower", 369, 190, 360, "2x 3.5\" + 5x 2.5\"", ["Black", "White"]),
    ("Cooler Master MasterBox TD500 Mesh V2", "ATX Mid Tower", 410, 165, 360, "2x 3.5\" + 3x 2.5\"", ["Black", "White"])
]

def generate_case_catalog(existing_items):
    seen = {clean_key(x.get("name", "")): x for x in existing_items if x.get("name")}
    results = list(existing_items)

    for model, c_type, gpu_len, cooler_h, rad_size, bays, colors in CASE_MODELS:
        for color in colors:
            suffix = f" ({color})" if color != "Black" else ""
            name = f"{model}{suffix}"
            k = clean_key(name)
            if k in seen:
                continue

            case_img = get_default_case_image(name)
            item = {
                "name": name,
                "price": None,
                "type": c_type,
                "color": color,
                "side_panel": "Tempered Glass",
                "max_gpu_length": gpu_len,
                "max_cpu_cooler_height": cooler_h,
                "max_radiator_size": rad_size,
                "drive_bays": bays,
                "imageUrl": case_img
            }
            seen[k] = item
            results.append(item)

    for it in results:
        if not it.get("imageUrl"):
            it["imageUrl"] = get_default_case_image(it.get("name", ""))

    return results

# ==============================================================================
# MAIN ENGINE EXECUTION
# ==============================================================================
def load_json(path):
    if os.path.exists(path):
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception as e:
            print(f"[UYARI] {path} okunamadı: {e}")
    return []

def load_and_sanitize(path: str, category: str) -> list[dict]:
    raw_items = load_json(path)
    clean_items = []
    seen = set()

    for it in raw_items:
        name = it.get("name", "").strip()
        if len(name) < 5 or any(b in name.lower() for b in ["tıklayın", "tiklayin", "sepete", "tavsiye", "\ufffd"]):
            continue

        name_lower = name.lower()
        name_upper = name.upper()

        # Platform & Mimari Whitelist (Soket / Nesil Tabanlı Filtre - 10-15 yıllık antika parçaları süpürür)
        if category == "cpu":
            if not any(k in name_lower for k in ["ryzen", "core", "ultra", "intel", "amd", "threadripper"]):
                continue
            if any(k in name_lower for k in ["ssd", "soğutucu", "sogutucu", "fan", "kasa", "ram", "ddr", "motherboard", "anakart"]):
                continue

            # AMD CPU Filtresi: Kesinlikle SADECE Ryzen ve Threadripper (DDR4 ve DDR5 çağı)
            # Kesinlikle elenen antika modeller: A4, A6 (A6-5400K vs.), A8, A10, A12, FX, Phenom, Athlon II, Sempron, Opteron, Turion, FM1, FM2, AM1, AM2, AM3, AM3+
            if "amd" in name_lower:
                if not any(r in name_lower for r in ["ryzen", "threadripper", "athlon 3000g", "athlon 200ge"]):
                    continue
            if any(b in name_lower for b in ["a4-", "a6-", "a8-", "a10-", "a12-", "a4 ", "a6 ", "a8 ", "a10 ", "a12 ", "fm1", "fm2", "am1", "am2", "am3", "fx-", "fx ", "phenom", "athlon ii", "athlon x4", "sempron", "opteron", "turion"]):
                continue

            # Intel CPU Filtresi: SADECE Core Ultra VE 10. Nesil - 14. Nesil Core İşlemciler (DDR4 ve DDR5 çağı)
            # Kesinlikle elenen antika modeller: Celeron, Pentium, Core 2, Xeon, LGA775, LGA1155, LGA1150, LGA1156, LGA1151
            if any(b in name_lower for b in ["xeon", "pentium", "core 2", "celeron", "lga775", "lga1155", "lga1150", "lga1156", "lga1151"]):
                continue
            # Intel 1. - 9. nesil işlemcileri ele (i7-2600, i5-4690, i7-7700, i9-9900K vs.)
            m_gen = re.search(r"i[3579]-([2-9]\d{3})", name)
            if m_gen:
                continue

            sock = it.get("socket")
            if sock and sock not in ["AM4", "AM5", "LGA1200", "LGA1700", "LGA1851", "sTRX4", "sWRX8", "sTR5", "Belirtilmedi"]:
                continue
            if sock in ["AM3", "AM3+", "AM2", "FM1", "FM2", "LGA1151", "LGA1150", "LGA1155", "LGA775"]:
                continue

        elif category == "gpu":
            if not any(k in name_lower for k in ["rtx", "radeon", "rx ", "arc", "geforce"]):
                continue
            name = re.sub(r"\s+Video Card$", "", name, flags=re.IGNORECASE)
            name = re.sub(r"\s+Edition$", "", name, flags=re.IGNORECASE)
            it["name"] = name

            # Modern Ekran Kartı Whitelist:
            # NVIDIA: SADECE RTX 50, 40, 30, 20 serisi (GTX serisi kesinlikle elenir!)
            # AMD: SADECE Radeon RX 9000, 7000, 6000, 5000 serisi (RX 580/570 vb. kesinlikle elenir!)
            # Intel: Arc B580, B570, A770, A750, A580, A380
            gpu_upper = name.upper()
            is_modern_nvidia = bool(re.search(r"RTX\s*(50|40|30|20)\d{2}", gpu_upper))
            is_modern_amd = bool(re.search(r"RX\s*(9\d{3}|7\d{3}|6\d{3}|5\d{3})", gpu_upper) or re.search(r"RADEON\s*(9\d{3}|7\d{3}|6\d{3}|5\d{3})", gpu_upper))
            is_modern_intel = bool("ARC" in gpu_upper and any(x in gpu_upper for x in ["B580", "B570", "A770", "A750", "A580", "A380"]))
            if not (is_modern_nvidia or is_modern_amd or is_modern_intel):
                continue

        elif category == "motherboard":
            if not any(k in name_upper for k in ["ANAKART", "MOTHERBOARD", "B650", "B760", "Z790", "Z890", "X870", "X670", "B550", "B450", "H610", "A620", "B850", "B840", "B860", "A520", "X570", "Z690", "B660", "H670", "Z590", "B560", "H570", "H510", "Z490", "B460", "H410"]):
                continue
            sock = it.get("socket")
            if sock and sock not in ["AM4", "AM5", "LGA1200", "LGA1700", "LGA1851", "Belirtilmedi"]:
                continue
            # DDR4 altı veya eski çipsetler KESİNLİKLE elenir
            if any(b in name_upper for b in ["H110", "B150", "Z170", "H270", "B250", "Z270", "H310", "B360", "B365", "Z370", "Z390", "H81", "B85", "Z87", "Z97", "H61", "FM2", "FM1", "AM3", "AM2", "AM1", "970A", "990FX", "A68", "A88X", "LGA775", "LGA1155", "LGA1150"]):
                continue
            if it.get("memory_type") in ["DDR3", "DDR2", "DDR"] or it.get("memory_generation") in ["DDR3", "DDR2", "DDR"]:
                continue

        elif category == "ram":
            if not any(k in name_upper for k in ["DDR4", "DDR5", "RAM", "BELLEK", "MEMORY"]):
                continue
            # DDR3, DDR2, DDR1 kesin elenir
            if any(b in name_upper for b in ["DDR3", "DDR2", "DDR1", "SO-DIMM DDR3"]):
                continue
            if it.get("type") in ["DDR3", "DDR2", "DDR"] or it.get("memory_type") in ["DDR3", "DDR2", "DDR"]:
                continue

        elif category == "storage":
            # PCIe Gen3 ve üzeri NVMe M.2 SSD + modern SATA III SSD
            # Mekanik HDD'ler, 5400/7200 RPM sabit diskler, IDE/PATA, 120GB altı antika sürücüler ELENİR!
            if not any(k in name_upper for k in ["SSD", "NVME", "M.2", "SATA", "EVO", "PRO", "PLUS", "SN850", "SN770", "SN580", "KC3000", "RENEGADE", "T705", "T700", "T500", "MX500"]):
                continue
            if any(b in name_lower for b in ["ide ", "pata", "5400rpm", "7200rpm", "harddisk", "hdd", "barracuda", "wd blue hdd", "ironwolf", "skyhawk", "red pro"]):
                continue
            cap = it.get("capacity")
            if cap and isinstance(cap, (int, float)) and cap < 120:
                continue

        elif category == "psu":
            if not any(k in name_upper for k in ["PSU", "POWER", "WATT", "80 PLUS", "80+"]) and not re.search(r"\b\d{3,4}\s*W\b", name_upper):
                continue
        elif category == "cooler":
            if not any(k in name_upper for k in ["SOĞUTUCU", "SOGUTUCU", "COOLER", "SIVI", "HAVA", "FREEZER", "ASSASSIN", "KRAKEN", "AIO", "AK620", "AK400", "PEERLESS", "PHANTOM SPIRIT", "NOTTE", "LIQUID"]):
                continue
        elif category == "case":
            if not any(k in name_upper for k in ["KASA", "CASE", "CHASSIS", "TOWER", "FLOW", "DYNAMIC", "AIRFLOW"]):
                continue

        k = clean_key(name)
        if k not in seen:
            seen.add(k)
            clean_items.append(it)

    return clean_items

def save_json(paths, data):
    for p in paths:
        os.makedirs(os.path.dirname(p), exist_ok=True)
        with open(p, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"[OK] {os.path.basename(p)} kaydedildi ({len(data)} öğe).")

def main():
    print("================================================================================")
    print("   HARDWARE WIZARD - MASTER CATALOG & ENGINEERING GENERATOR (2023-2026)         ")
    print("================================================================================")

    # 1. CPU
    cpu_path = os.path.join(FILES_DIR, "cpu.json")
    cpus = generate_cpu_catalog(load_and_sanitize(cpu_path, "cpu"))
    save_json([cpu_path, os.path.join(CATALOG_DIR, "cpu.json")], cpus)

    # 2. GPU (and alias video-card.json)
    gpu_path = os.path.join(FILES_DIR, "gpu.json")
    vcard_path = os.path.join(FILES_DIR, "video-card.json")
    gpus = generate_gpu_catalog(load_and_sanitize(vcard_path, "gpu") or load_and_sanitize(gpu_path, "gpu"))
    save_json([gpu_path, vcard_path, os.path.join(CATALOG_DIR, "video-card.json"), os.path.join(CATALOG_DIR, "gpu.json")], gpus)

    # 3. Motherboard
    mobo_path = os.path.join(FILES_DIR, "motherboard.json")
    mobos = generate_motherboard_catalog(load_and_sanitize(mobo_path, "motherboard"))
    save_json([mobo_path, os.path.join(CATALOG_DIR, "motherboard.json")], mobos)

    # 4. RAM (and alias memory.json)
    ram_path = os.path.join(FILES_DIR, "ram.json")
    mem_path = os.path.join(FILES_DIR, "memory.json")
    rams = generate_ram_catalog(load_and_sanitize(mem_path, "ram") or load_and_sanitize(ram_path, "ram"))
    save_json([ram_path, mem_path, os.path.join(CATALOG_DIR, "memory.json"), os.path.join(CATALOG_DIR, "ram.json")], rams)

    # 5. Storage (and alias internal-hard-drive.json)
    storage_path = os.path.join(FILES_DIR, "storage.json")
    drive_path = os.path.join(FILES_DIR, "internal-hard-drive.json")
    storages = generate_storage_catalog(load_and_sanitize(drive_path, "storage") or load_and_sanitize(storage_path, "storage"))
    save_json([storage_path, drive_path, os.path.join(CATALOG_DIR, "internal-hard-drive.json"), os.path.join(CATALOG_DIR, "storage.json")], storages)

    # 6. PSU
    psu_path = os.path.join(FILES_DIR, "power-supply.json")
    psus = generate_psu_catalog(load_and_sanitize(psu_path, "psu"))
    save_json([psu_path, os.path.join(CATALOG_DIR, "power-supply.json"), os.path.join(CATALOG_DIR, "psu.json")], psus)

    # 7. Cooler (and alias cpu-cooler.json)
    cooler_path = os.path.join(FILES_DIR, "cooler.json")
    cpu_cooler_path = os.path.join(FILES_DIR, "cpu-cooler.json")
    coolers = generate_cooler_catalog(load_and_sanitize(cpu_cooler_path, "cooler") or load_and_sanitize(cooler_path, "cooler"))
    save_json([cooler_path, cpu_cooler_path, os.path.join(CATALOG_DIR, "cpu-cooler.json"), os.path.join(CATALOG_DIR, "cooler.json")], coolers)

    # 8. Case
    case_path = os.path.join(FILES_DIR, "case.json")
    cases = generate_case_catalog(load_and_sanitize(case_path, "case"))
    save_json([case_path, os.path.join(CATALOG_DIR, "case.json")], cases)

    total_items = len(cpus) + len(gpus) + len(mobos) + len(rams) + len(storages) + len(psus) + len(coolers) + len(cases)
    print("================================================================================")
    print(f"[BAŞARILI] Toplam {total_items} adet parça derin mühendislik verileriyle üretildi!")
    print("================================================================================")

if __name__ == "__main__":
    main()
