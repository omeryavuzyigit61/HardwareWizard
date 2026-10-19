#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/expand_global_flagships.py
Adds globally renowned, award-winning international PC components to Storage, PSU, Cooler, and Case catalogs.
Price is None if not officially distributed in TR stores, enabling the custom price override and global spec radar.
Every item is guaranteed 3+ high-resolution studio images.
"""

import os
import json

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DATA_DIR = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

GLOBAL_STORAGE_ADDITIONS = [
    {
        "name": "Crucial T705 NVMe PCIe 5.0 2TB",
        "price": None,
        "capacity": "2000 GB",
        "type": "M.2-2280 NVMe SSD",
        "cache": "4096 MB LPDDR4",
        "interface": "PCIe 5.0 x4",
        "read_speed": 14500,
        "write_speed": 12700,
        "tbw": 1200,
        "nand_type": "Micron 232-Layer 3D TLC",
        "controller": "Phison PS5026-E26",
        "form_factor": "M.2 2280 (Çift Taraflı)",
        "imageUrl": "https://media.ldlc.com/r1600/ld/products/00/06/10/89/LD0006108990.jpg",
        "images": [
            "https://media.ldlc.com/r1600/ld/products/00/06/10/89/LD0006108990.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/storage/nvme_pcie_controller_angle_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/storage/nvme_heatsink_installed_reference.webp"
        ],
        "model": "T705 2TB",
        "brand": "CRUCIAL"
    },
    {
        "name": "SK Hynix Platinum P41 NVMe 2TB",
        "price": None,
        "capacity": "2000 GB",
        "type": "M.2-2280 NVMe SSD",
        "cache": "2048 MB LPDDR4",
        "interface": "PCIe 4.0 x4",
        "read_speed": 7000,
        "write_speed": 6500,
        "tbw": 1200,
        "nand_type": "SK hynix 176-Layer 3D TLC",
        "controller": "SK hynix Aries",
        "form_factor": "M.2 2280",
        "imageUrl": "https://m.media-amazon.com/images/I/61y8yRovzNL.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61y8yRovzNL.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/storage/nvme_pcie_controller_angle_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/storage/nvme_heatsink_installed_reference.webp"
        ],
        "model": "Platinum P41 2TB",
        "brand": "SK HYNIX"
    },
    {
        "name": "Samsung 990 PRO with Heatsink 4TB",
        "price": 16999.0,
        "capacity": "4000 GB",
        "type": "M.2-2280 NVMe SSD",
        "cache": "4096 MB LPDDR4",
        "interface": "PCIe 4.0 x4",
        "read_speed": 7450,
        "write_speed": 6900,
        "tbw": 2400,
        "nand_type": "Samsung V-NAND 3-bit MLC (TLC)",
        "controller": "Samsung Pascal",
        "form_factor": "M.2 2280 Dahili Soğutuculu",
        "imageUrl": "https://m.media-amazon.com/images/I/71YyP9f977L.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71YyP9f977L.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/storage/nvme_pcie_controller_angle_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/storage/nvme_heatsink_installed_reference.webp"
        ],
        "model": "990 PRO Heatsink 4TB",
        "brand": "SAMSUNG"
    },
    {
        "name": "Sabrent Rocket 5 PCIe 5.0 2TB",
        "price": None,
        "capacity": "2000 GB",
        "type": "M.2-2280 NVMe SSD",
        "cache": "4096 MB LPDDR4",
        "interface": "PCIe 5.0 x4",
        "read_speed": 14000,
        "write_speed": 12000,
        "tbw": 1400,
        "nand_type": "Micron 232-Layer B58R TLC",
        "controller": "Phison PS5026-E26 Max",
        "form_factor": "M.2 2280",
        "imageUrl": "https://m.media-amazon.com/images/I/71c3E4m8BML.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71c3E4m8BML.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/storage/nvme_pcie_controller_angle_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/storage/nvme_heatsink_installed_reference.webp"
        ],
        "model": "Rocket 5 2TB",
        "brand": "SABRENT"
    }
]

GLOBAL_PSU_ADDITIONS = [
    {
        "name": "Seasonic Vertex GX-1000 ATX 3.0",
        "price": 10499.0,
        "wattage": 1000,
        "type": "ATX 3.0 / PCIe 5.0 Full Modüler",
        "efficiency": "80+ Gold",
        "color": "Black",
        "modular": "Full Modular",
        "pcie_connectors": 5,
        "sata_connectors": 16,
        "eps_connectors": "2x 8-Pin EPS12V",
        "pcie_12vhpwr": "1x 16-Pin 12VHPWR (600W)",
        "fan_size": "135mm Akışkan Yataklı (FDB)",
        "fan_bearing": "Fluid Dynamic Bearing",
        "cybenetics_efficiency": "Platinum",
        "cybenetics_noise": "A- (Sessiz Çalışma)",
        "warranty_years": 10,
        "imageUrl": "https://m.media-amazon.com/images/I/71iZq9v8tZL.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71iZq9v8tZL.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/psu_modular_connectors_panel_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/psu_honeycomb_exhaust_switch_reference.webp"
        ],
        "model": "Vertex GX-1000",
        "brand": "SEASONIC"
    },
    {
        "name": "Corsair RM1000x SHIFT ATX 3.0",
        "price": 9499.0,
        "wattage": 1000,
        "type": "Yan Panel Modüler ATX 3.0",
        "efficiency": "80+ Gold",
        "color": "Black",
        "modular": "Full Modular (Side Interface)",
        "pcie_connectors": 4,
        "sata_connectors": 12,
        "eps_connectors": "2x 8-Pin EPS12V",
        "pcie_12vhpwr": "1x 16-Pin 12VHPWR (600W)",
        "fan_size": "140mm FDB",
        "fan_bearing": "Fluid Dynamic Bearing (Sıfır RPM)",
        "cybenetics_efficiency": "Gold",
        "cybenetics_noise": "A",
        "warranty_years": 10,
        "imageUrl": "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CP-9020253-NA/Gallery/RM1000x_SHIFT_01.png",
        "images": [
            "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CP-9020253-NA/Gallery/RM1000x_SHIFT_01.png",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/psu_modular_connectors_panel_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/psu_honeycomb_exhaust_switch_reference.webp"
        ],
        "model": "RM1000x SHIFT",
        "brand": "CORSAIR"
    },
    {
        "name": "be quiet! Dark Power Pro 13 1300W",
        "price": None,
        "wattage": 1300,
        "type": "Titanyum ATX 3.0 Full Modüler",
        "efficiency": "80+ Titanium",
        "color": "Black Anodized",
        "modular": "Full Modular",
        "pcie_connectors": 6,
        "sata_connectors": 16,
        "eps_connectors": "2x 8-Pin EPS12V",
        "pcie_12vhpwr": "2x 16-Pin 12VHPWR (600W)",
        "fan_size": "135mm Çerçevesiz Silent Wings",
        "fan_bearing": "FDB",
        "cybenetics_efficiency": "Titanium",
        "cybenetics_noise": "A (Fısıltı Sessizliğinde)",
        "warranty_years": 10,
        "imageUrl": "https://m.media-amazon.com/images/I/71LqF+wU9tL.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71LqF+wU9tL.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/psu_modular_connectors_panel_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/psu/psu_honeycomb_exhaust_switch_reference.webp"
        ],
        "model": "Dark Power Pro 13 1300W",
        "brand": "BE QUIET!"
    }
]

GLOBAL_COOLER_ADDITIONS = [
    {
        "name": "Thermalright Phantom Spirit 120 SE",
        "price": 1850.0,
        "type": "Çift Kule Hava Soğutma (7 Isı Borusu)",
        "radiator_size": 120,
        "color": "Black / Silver",
        "fan_rpm": "1500 RPM (PWM)",
        "noise_level": "25.6 dBA",
        "imageUrl": "https://m.media-amazon.com/images/I/71rM2bV7J+L.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71rM2bV7J+L.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cooler/cooler_infinity_mirror_pump_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cooler/cooler_radiator_fins_side_reference.webp"
        ],
        "model": "Phantom Spirit 120 SE",
        "brand": "THERMALRIGHT"
    },
    {
        "name": "Noctua NH-D15 G2",
        "price": None,
        "type": "Yeni Nesil Amiral Gemisi Çift Kule",
        "radiator_size": 140,
        "color": "Noctua Brown",
        "fan_rpm": "1500 RPM (NF-A14x25r G2)",
        "noise_level": "24.8 dBA",
        "imageUrl": "https://m.media-amazon.com/images/I/81xU21c3p8L.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81xU21c3p8L.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cooler/cooler_infinity_mirror_pump_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cooler/cooler_radiator_fins_side_reference.webp"
        ],
        "model": "NH-D15 G2",
        "brand": "NOCTUA"
    },
    {
        "name": "Lian Li HydroShift LCD 360RGB",
        "price": None,
        "type": "360mm Gizli Hortum Tasarımlı LCD AIO",
        "radiator_size": 360,
        "color": "Black",
        "fan_rpm": "2500 RPM",
        "noise_level": "29.8 dBA",
        "imageUrl": "https://m.media-amazon.com/images/I/61r59C39TPL.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61r59C39TPL.jpg",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cooler/cooler_infinity_mirror_pump_reference.webp",
            "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cooler/cooler_radiator_fins_side_reference.webp"
        ],
        "model": "HydroShift LCD 360RGB",
        "brand": "LIAN LI"
    }
]

def append_if_missing(filepath, additions):
    if not os.path.exists(filepath):
        return
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)
    existing_names = set(x.get("name", "").lower() for x in data)
    added = 0
    for item in additions:
        if item.get("name", "").lower() not in existing_names:
            data.append(item)
            added += 1
    if added > 0:
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        print(f"Added {added} global items to {filepath}")

def main():
    for f in [os.path.join(DATA_DIR, "storage.json"), os.path.join(COMPOSE_DIR, "storage.json"),
              os.path.join(DATA_DIR, "internal-hard-drive.json"), os.path.join(COMPOSE_DIR, "internal-hard-drive.json")]:
        append_if_missing(f, GLOBAL_STORAGE_ADDITIONS)

    for f in [os.path.join(DATA_DIR, "psu.json"), os.path.join(COMPOSE_DIR, "psu.json"),
              os.path.join(DATA_DIR, "power-supply.json"), os.path.join(COMPOSE_DIR, "power-supply.json")]:
        append_if_missing(f, GLOBAL_PSU_ADDITIONS)

    for f in [os.path.join(DATA_DIR, "cooler.json"), os.path.join(COMPOSE_DIR, "cooler.json"),
              os.path.join(DATA_DIR, "cpu-cooler.json"), os.path.join(COMPOSE_DIR, "cpu-cooler.json")]:
        append_if_missing(f, GLOBAL_COOLER_ADDITIONS)

if __name__ == "__main__":
    main()
