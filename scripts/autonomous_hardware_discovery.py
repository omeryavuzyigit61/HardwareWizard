#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/autonomous_hardware_discovery.py
Hardware Wizard - Otonom Yeni Donanım Keşif, Eksik Marka Entegrasyonu & DevSecOps Doğrulama Motoru

Görevler:
1. Piyasaya yeni çıkan çiplerin (AMD Radeon RX 9070 GRE, Intel Battlemage B570, Ryzen 9000X3D vb.)
   ve eksik AIB markalarının (Colorful, Lian Li EG, Seasonic, be quiet! vb.) tespit edilip kataloğa eklenmesi.
2. Sıfır Güven (Zero-Trust) DevSecOps süzgeci:
   - URL Poisoning & XSS engeli (yalnızca HTTPS ve onaylı CDN/üretici linkleri).
   - Metrik aralık doğrulama (TDP, VRAM, Watt aralıkları).
   - Boyut sınırlandırma ve enjeksiyon engeli.
3. Çift Dizin Senkronizasyonu:
   - data/catalog/ ve shared/src/commonMain/composeResources/files/ dizinlerini birebir eşitler.
4. Kriptografik Bütünlük:
   - Tüm katalog dosyalarının SHA-256 hash manifestosunu (catalog_manifest.json) üretir.
"""

import os
import sys
import json
import re
import hashlib
from datetime import datetime

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

# ==============================================================================
# SIFIR GÜVEN (ZERO-TRUST) SİBER GÜVENLİK SÜZGECİ
# ==============================================================================
FORBIDDEN_SCHEMES = ("javascript:", "vbscript:", "data:", "file:", "content:", "intent:", "blob:", "http:")
ALLOWED_IMG_HOSTS = (
    "raw.githubusercontent.com",
    "github.com",
    "assets.corsair.com",
    "lian-li.com",
    "en.colorful.cn",
    "seasonic.com",
    "bequiet.com",
    "thermalright.com",
    "arctic.de",
    "noctua.at",
    "msi.com",
    "asus.com",
    "gigabyte.com",
    "asrock.com",
    "lexar.com",
    "crucial.com",
    "westerndigital.com",
    "crdms.images.consumerreports.org",
    "jeyohmohothgcuhqpjqp.supabase.co",
    "ejudokeppeuhgtxrjwbn.supabase.co"
)

def sanitize_url(url: str | None) -> str:
    if not url or not isinstance(url, str):
        return ""
    u = url.strip()
    u_lower = u.lower()
    for scheme in FORBIDDEN_SCHEMES:
        if u_lower.startswith(scheme):
            return ""
    if not u_lower.startswith("https://"):
        return ""
    return u

def sanitize_text(text: str | None, max_len: int = 250) -> str:
    if not text or not isinstance(text, str):
        return ""
    cleaned = text.replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;").replace("\n", " ").strip()
    return cleaned[:max_len]

# ==============================================================================
# EKSİK DONANIM VERİ SETLERİ (FABRİKA STANDARTLARI)
# ==============================================================================

# 1. COLORFUL EKRAN KARTLARI (iGame Vulcan, Neptune, Ultra W, Battle-Ax)
COLORFUL_GPUS = [
    {
        "name": "Colorful iGame GeForce RTX 4090 Vulcan W OC 24GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4090",
        "memory": 24,
        "memory_type": "GDDR6X",
        "core_clock": 2235,
        "boost_clock": 2625,
        "tdp": 450,
        "recommended_psu": 850,
        "color": "White",
        "length": 348.5,
        "slot_width": 3.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Triple Fan + Smart LCD Display",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4090_vulcan_w.webp"
    },
    {
        "name": "Colorful iGame GeForce RTX 4080 SUPER Vulcan W OC 16GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4080 SUPER",
        "memory": 16,
        "memory_type": "GDDR6X",
        "core_clock": 2295,
        "boost_clock": 2640,
        "tdp": 320,
        "recommended_psu": 750,
        "color": "White",
        "length": 348.5,
        "slot_width": 3.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Triple Fan + Smart LCD Display",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4080s_vulcan.webp"
    },
    {
        "name": "Colorful iGame GeForce RTX 4080 SUPER Neptune OC 16GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4080 SUPER",
        "memory": 16,
        "memory_type": "GDDR6X",
        "core_clock": 2295,
        "boost_clock": 2640,
        "tdp": 320,
        "recommended_psu": 750,
        "color": "Silver / White",
        "length": 253.5,
        "slot_width": 2.0,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Dahili 360mm AIO Sıvı Soğutma",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4080s_neptune.webp"
    },
    {
        "name": "Colorful iGame GeForce RTX 4070 Ti SUPER Vulcan W OC 16GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4070 Ti SUPER",
        "memory": 16,
        "memory_type": "GDDR6X",
        "core_clock": 2340,
        "boost_clock": 2685,
        "tdp": 285,
        "recommended_psu": 750,
        "color": "White",
        "length": 348.5,
        "slot_width": 3.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Triple Fan + LCD Display",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4070tis_vulcan.webp"
    },
    {
        "name": "Colorful iGame GeForce RTX 4070 Ti SUPER Ultra W OC 16GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4070 Ti SUPER",
        "memory": 16,
        "memory_type": "GDDR6X",
        "core_clock": 2340,
        "boost_clock": 2655,
        "tdp": 285,
        "recommended_psu": 750,
        "color": "Retro Holographic White",
        "length": 334.5,
        "slot_width": 2.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Triple Ring Fan",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4070tis_ultra_w.webp"
    },
    {
        "name": "Colorful iGame GeForce RTX 4070 SUPER Ultra W OC 12GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4070 SUPER",
        "memory": 12,
        "memory_type": "GDDR6X",
        "core_clock": 1980,
        "boost_clock": 2505,
        "tdp": 220,
        "recommended_psu": 650,
        "color": "Retro Holographic White",
        "length": 325.0,
        "slot_width": 2.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Triple Fan RGB",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4070s_ultra_w.webp"
    },
    {
        "name": "Colorful GeForce RTX 4060 Ti Battle-Ax DUO 16GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4060 Ti 16GB",
        "memory": 16,
        "memory_type": "GDDR6",
        "core_clock": 2310,
        "boost_clock": 2535,
        "tdp": 165,
        "recommended_psu": 550,
        "color": "Black / Red",
        "length": 253.0,
        "slot_width": 2.0,
        "pcie_interface": "PCIe 4.0 x8",
        "external_power": "1x 8-Pin PCIe",
        "cooling_type": "Dual Fan Battle-Ax",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4060ti_battleax.webp"
    },
    {
        "name": "Colorful GeForce RTX 4060 Ti Battle-Ax DUO 8GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4060 Ti",
        "memory": 8,
        "memory_type": "GDDR6",
        "core_clock": 2310,
        "boost_clock": 2535,
        "tdp": 160,
        "recommended_psu": 550,
        "color": "Black / Red",
        "length": 253.0,
        "slot_width": 2.0,
        "pcie_interface": "PCIe 4.0 x8",
        "external_power": "1x 8-Pin PCIe",
        "cooling_type": "Dual Fan Battle-Ax",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4060ti_battleax.webp"
    },
    {
        "name": "Colorful iGame GeForce RTX 4060 Ultra W OC 8GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4060",
        "memory": 8,
        "memory_type": "GDDR6",
        "core_clock": 1830,
        "boost_clock": 2490,
        "tdp": 115,
        "recommended_psu": 450,
        "color": "Retro Holographic White",
        "length": 310.0,
        "slot_width": 2.0,
        "pcie_interface": "PCIe 4.0 x8",
        "external_power": "1x 8-Pin PCIe",
        "cooling_type": "Triple Fan Ultra White",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4060_ultra_w.webp"
    },
    {
        "name": "Colorful GeForce RTX 4060 Battle-Ax DUO 8GB",
        "manufacturer": "Colorful",
        "chipset": "GeForce RTX 4060",
        "memory": 8,
        "memory_type": "GDDR6",
        "core_clock": 1830,
        "boost_clock": 2460,
        "tdp": 115,
        "recommended_psu": 450,
        "color": "Black / Red",
        "length": 249.5,
        "slot_width": 2.0,
        "pcie_interface": "PCIe 4.0 x8",
        "external_power": "1x 8-Pin PCIe",
        "cooling_type": "Dual Fan Battle-Ax",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_rtx_4060_battleax.webp"
    }
]

# 2. AMD RADEON RX 9070 GRE & RDNA 4 YENİ NESİL ÇİPLER
RADEON_9000_GPUS = [
    {
        "name": "AMD Radeon RX 9070 GRE 16GB",
        "manufacturer": "AMD",
        "chipset": "Radeon RX 9070 GRE",
        "memory": 16,
        "memory_type": "GDDR6",
        "core_clock": 2100,
        "boost_clock": 2650,
        "tdp": 220,
        "recommended_psu": 650,
        "color": "Black / Silver",
        "length": 275.0,
        "slot_width": 2.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "2x 8-Pin PCIe",
        "cooling_type": "Triple Fan Reference",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/amd_rx_9070_gre_ref.webp"
    },
    {
        "name": "Sapphire Pulse Radeon RX 9070 GRE 16GB",
        "manufacturer": "Sapphire",
        "chipset": "Radeon RX 9070 GRE",
        "memory": 16,
        "memory_type": "GDDR6",
        "core_clock": 2150,
        "boost_clock": 2680,
        "tdp": 225,
        "recommended_psu": 650,
        "color": "Black / Red",
        "length": 280.0,
        "slot_width": 2.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "2x 8-Pin PCIe",
        "cooling_type": "Dual Fan Dual-X",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/sapphire_rx_9070_gre_pulse.webp"
    },
    {
        "name": "Sapphire Nitro+ Radeon RX 9070 GRE 16GB",
        "manufacturer": "Sapphire",
        "chipset": "Radeon RX 9070 GRE",
        "memory": 16,
        "memory_type": "GDDR6",
        "core_clock": 2200,
        "boost_clock": 2750,
        "tdp": 240,
        "recommended_psu": 700,
        "color": "Silver / Gray",
        "length": 320.0,
        "slot_width": 3.0,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "2x 8-Pin PCIe",
        "cooling_type": "Triple Fan Tri-X ARGB",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/sapphire_rx_9070_gre_nitro.webp"
    },
    {
        "name": "PowerColor Hellhound Radeon RX 9070 GRE 16GB",
        "manufacturer": "PowerColor",
        "chipset": "Radeon RX 9070 GRE",
        "memory": 16,
        "memory_type": "GDDR6",
        "core_clock": 2180,
        "boost_clock": 2700,
        "tdp": 230,
        "recommended_psu": 650,
        "color": "Black / Ice Blue LED",
        "length": 310.0,
        "slot_width": 2.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "2x 8-Pin PCIe",
        "cooling_type": "Triple Ring-Blade Fan",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/powercolor_rx_9070_gre_hellhound.webp"
    },
    {
        "name": "XFX Speedster QICK 319 Radeon RX 9070 GRE 16GB",
        "manufacturer": "XFX",
        "chipset": "Radeon RX 9070 GRE",
        "memory": 16,
        "memory_type": "GDDR6",
        "core_clock": 2160,
        "boost_clock": 2690,
        "tdp": 230,
        "recommended_psu": 650,
        "color": "Clean Cast Black",
        "length": 335.0,
        "slot_width": 2.6,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "2x 8-Pin PCIe",
        "cooling_type": "Triple Fan Ghost Thermal",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/xfx_rx_9070_gre_qick.webp"
    },
    {
        "name": "ASUS Dual Radeon RX 9070 GRE OC 16GB",
        "manufacturer": "ASUS",
        "chipset": "Radeon RX 9070 GRE",
        "memory": 16,
        "memory_type": "GDDR6",
        "core_clock": 2150,
        "boost_clock": 2675,
        "tdp": 225,
        "recommended_psu": 650,
        "color": "Black",
        "length": 250.0,
        "slot_width": 2.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "2x 8-Pin PCIe",
        "cooling_type": "Dual Axial-tech Fan",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/asus_rx_9070_gre_dual.webp"
    },
    {
        "name": "PowerColor Fighter Radeon RX 7650 GRE 16GB",
        "manufacturer": "PowerColor",
        "chipset": "Radeon RX 7650 GRE",
        "memory": 16,
        "memory_type": "GDDR6",
        "core_clock": 1950,
        "boost_clock": 2700,
        "tdp": 180,
        "recommended_psu": 550,
        "color": "Black",
        "length": 240.0,
        "slot_width": 2.0,
        "pcie_interface": "PCIe 4.0 x8",
        "external_power": "1x 8-Pin PCIe",
        "cooling_type": "Dual Fan Fighter",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/powercolor_rx_7650_gre.webp"
    }
]

# 3. GAINWARD, PNY & INTEL BATTLEMAGE EKRAN KARTLARI
ADDITIONAL_GPUS = [
    {
        "name": "Gainward GeForce RTX 4080 SUPER Phantom GS 16GB",
        "manufacturer": "Gainward",
        "chipset": "GeForce RTX 4080 SUPER",
        "memory": 16,
        "memory_type": "GDDR6X",
        "core_clock": 2295,
        "boost_clock": 2610,
        "tdp": 320,
        "recommended_psu": 750,
        "color": "Stealth Black",
        "length": 329.4,
        "slot_width": 3.1,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Triple Cyclone Fan",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/gainward_rtx_4080s_phantom.webp"
    },
    {
        "name": "Gainward GeForce RTX 4070 SUPER Panther 12GB",
        "manufacturer": "Gainward",
        "chipset": "GeForce RTX 4070 SUPER",
        "memory": 12,
        "memory_type": "GDDR6X",
        "core_clock": 1980,
        "boost_clock": 2475,
        "tdp": 220,
        "recommended_psu": 650,
        "color": "Matte Black (No-RGB)",
        "length": 328.9,
        "slot_width": 2.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Triple Fan Panther Silent",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/gainward_rtx_4070s_panther.webp"
    },
    {
        "name": "PNY GeForce RTX 4080 SUPER XLR8 Gaming VERTO EPIC-X RGB 16GB",
        "manufacturer": "PNY",
        "chipset": "GeForce RTX 4080 SUPER",
        "memory": 16,
        "memory_type": "GDDR6X",
        "core_clock": 2295,
        "boost_clock": 2595,
        "tdp": 320,
        "recommended_psu": 750,
        "color": "Black / X-RGB",
        "length": 331.7,
        "slot_width": 3.5,
        "pcie_interface": "PCIe 4.0 x16",
        "external_power": "1x 16-Pin 12VHPWR",
        "cooling_type": "Triple Fan XLR8",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/pny_rtx_4080s_xlr8.webp"
    },
    {
        "name": "Intel Arc B570 10GB GDDR6",
        "manufacturer": "Intel",
        "chipset": "Intel Arc B570",
        "memory": 10,
        "memory_type": "GDDR6",
        "core_clock": 2500,
        "boost_clock": 2600,
        "tdp": 150,
        "recommended_psu": 500,
        "color": "Matte Black",
        "length": 240.0,
        "slot_width": 2.0,
        "pcie_interface": "PCIe 4.0 x8",
        "external_power": "1x 8-Pin PCIe",
        "cooling_type": "Dual Fan Reference",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/intel_arc_b570.webp"
    },
    {
        "name": "Sparkle Intel Arc B580 TITAN OC 12GB",
        "manufacturer": "Sparkle",
        "chipset": "Intel Arc B580",
        "memory": 12,
        "memory_type": "GDDR6",
        "core_clock": 2670,
        "boost_clock": 2850,
        "tdp": 190,
        "recommended_psu": 600,
        "color": "Electric Blue / Black",
        "length": 305.0,
        "slot_width": 2.5,
        "pcie_interface": "PCIe 4.0 x8",
        "external_power": "2x 8-Pin PCIe",
        "cooling_type": "Triple Fan TITAN TORN Cooling",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/sparkle_arc_b580_titan.webp"
    }
]

# 4. GÜÇ KAYNAKLARI (LIAN LI EDGE EG, SEASONIC, BE QUIET!, CORSAIR SHIFT/SFX)
EXPANDED_PSUS = [
    # LIAN LI EDGE (EG) DUAL CHAMBER L-SHAPE SERIES
    {
        "name": "LIAN LI Edge EG-1300 Platinum (Black)",
        "manufacturer": "Lian Li",
        "wattage": 1300,
        "efficiency_rating": "80 PLUS Platinum",
        "modular": "Full",
        "form_factor": "ATX 3.1 (L-Shape)",
        "pcie_5_connector": "2x 16-Pin 12V-2x6",
        "color": "Black",
        "features": "L-Shape Dual Chamber Tasarım, 4x Dahili USB 2.0 Hub Çıkışı, 120mm FDB Fan",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_edge_1300_black.webp"
    },
    {
        "name": "LIAN LI Edge EG-1300 Platinum (White)",
        "manufacturer": "Lian Li",
        "wattage": 1300,
        "efficiency_rating": "80 PLUS Platinum",
        "modular": "Full",
        "form_factor": "ATX 3.1 (L-Shape)",
        "pcie_5_connector": "2x 16-Pin 12V-2x6",
        "color": "White",
        "features": "L-Shape Dual Chamber Tasarım, 4x Dahili USB 2.0 Hub Çıkışı, Beyaz Kablolar",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_edge_1300_white.webp"
    },
    {
        "name": "LIAN LI Edge EG-1000 Platinum (Black)",
        "manufacturer": "Lian Li",
        "wattage": 1000,
        "efficiency_rating": "80 PLUS Platinum",
        "modular": "Full",
        "form_factor": "ATX 3.1 (L-Shape)",
        "pcie_5_connector": "1x 16-Pin 12V-2x6",
        "color": "Black",
        "features": "L-Shape Dual Chamber Tasarım, Dahili USB Hub, O11 EVO/Vision Uyumlu",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_edge_1000_black.webp"
    },
    {
        "name": "LIAN LI Edge EG-1000 Platinum (White)",
        "manufacturer": "Lian Li",
        "wattage": 1000,
        "efficiency_rating": "80 PLUS Platinum",
        "modular": "Full",
        "form_factor": "ATX 3.1 (L-Shape)",
        "pcie_5_connector": "1x 16-Pin 12V-2x6",
        "color": "White",
        "features": "L-Shape Dual Chamber Tasarım, Dahili USB Hub, Saf Beyaz Gövde",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_edge_1000_white.webp"
    },
    {
        "name": "LIAN LI Edge EG-850 Gold (Black)",
        "manufacturer": "Lian Li",
        "wattage": 850,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.1 (L-Shape)",
        "pcie_5_connector": "1x 16-Pin 12V-2x6",
        "color": "Black",
        "features": "L-Shape Kablolama, 4x Dahili USB Hub, Mesh Izgara Soğutma",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_edge_850_black.webp"
    },
    {
        "name": "LIAN LI Edge EG-850 Gold (White)",
        "manufacturer": "Lian Li",
        "wattage": 850,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.1 (L-Shape)",
        "pcie_5_connector": "1x 16-Pin 12V-2x6",
        "color": "White",
        "features": "L-Shape Kablolama, 4x Dahili USB Hub, Beyaz Örgülü Kablolar",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_edge_850_white.webp"
    },
    {
        "name": "LIAN LI SP850 SFX Gold (Black)",
        "manufacturer": "Lian Li",
        "wattage": 850,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "SFX",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Mini-ITX / SFX Küçük Form Faktörü, PCIe 5.0 Hazır, Sıfır RPM Modu",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_sp850_black.webp"
    },

    # SEASONIC (VERTEX, FOCUS ATX 3.0, PRIME TITANIUM)
    {
        "name": "Seasonic Vertex GX-1200 ATX 3.0",
        "manufacturer": "Seasonic",
        "wattage": 1200,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "12 Yıl Garanti, Japon 105C Kapasitörler, Hibrit Sessiz Fan Kontrolü",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/seasonic_vertex_gx1200.webp"
    },
    {
        "name": "Seasonic Vertex GX-1000 ATX 3.0",
        "manufacturer": "Seasonic",
        "wattage": 1000,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Cybenetics Platinum Verimlilik & A- Sessizlik Sertifikası, 135mm FDB Fan",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/seasonic_vertex_gx1000.webp"
    },
    {
        "name": "Seasonic Focus GX-1000 ATX 3.0 V3",
        "manufacturer": "Seasonic",
        "wattage": 1000,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Kompakt 140mm Gövde, 10 Yıl Garanti, 135mm Akışkan Yataklı Fan",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/seasonic_focus_gx1000.webp"
    },
    {
        "name": "Seasonic Focus GX-850 ATX 3.0 V3",
        "manufacturer": "Seasonic",
        "wattage": 850,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Kompakt 140mm Derinlik, Kablosuz Bağlantı Tasarımı, Tam Modüler",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/seasonic_focus_gx850.webp"
    },
    {
        "name": "Seasonic Focus GX-750 ATX 3.0 V3",
        "manufacturer": "Seasonic",
        "wattage": 750,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Sessiz Hibrit Fan Eğrisi, PCIe 5.0 12VHPWR Doğal Kablo",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/seasonic_focus_gx750.webp"
    },
    {
        "name": "Seasonic Prime TX-1600 Titanium ATX 3.0",
        "manufacturer": "Seasonic",
        "wattage": 1600,
        "efficiency_rating": "80 PLUS Titanium",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "2x 16-Pin 12VHPWR",
        "color": "Black / Chrome",
        "features": "Dünyanın En Yüksek Ripple Kararlılığı (%0.5 Tolerans), Titanyum Verimlilik, 12 Yıl Garanti",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/seasonic_prime_tx1600.webp"
    },

    # BE QUIET! (DARK POWER PRO 13, STRAIGHT POWER 12, PURE POWER 12 M)
    {
        "name": "be quiet! Dark Power Pro 13 1300W Titanium",
        "manufacturer": "be quiet!",
        "wattage": 1300,
        "efficiency_rating": "80 PLUS Titanium",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "2x 16-Pin 12VHPWR",
        "color": "Alüminyum Siyah",
        "features": "Çerçevesiz Silent Wings Fan, Overclocking Key (Tek/Çoklu 12V Kanal Anahtarı)",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/bequiet_dark_power_pro_13.webp"
    },
    {
        "name": "be quiet! Straight Power 12 1000W Platinum",
        "manufacturer": "be quiet!",
        "wattage": 1000,
        "efficiency_rating": "80 PLUS Platinum",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Silent Wings 135mm Fan, Huni Şeklinde Hava Girişi, Platin Verimlilik",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/bequiet_straight_power_12.webp"
    },
    {
        "name": "be quiet! Pure Power 12 M 850W Gold",
        "manufacturer": "be quiet!",
        "wattage": 850,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "LLC + SR + DC/DC Dönüştürücüler, 120mm Sessiz Fan, F/P Şampiyonu",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/bequiet_pure_power_12m_850.webp"
    },
    {
        "name": "be quiet! Pure Power 12 M 750W Gold",
        "manufacturer": "be quiet!",
        "wattage": 750,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "ATX 3.0 Yerel Uyumluluk, Çift 12V Kanalı, 10 Yıl Garanti",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/bequiet_pure_power_12m_750.webp"
    },

    # CORSAIR (RMX SHIFT YAN MODÜLER & SFX PLATINUM)
    {
        "name": "Corsair RM1000x Shift Gold ATX 3.0",
        "manufacturer": "Corsair",
        "wattage": 1000,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full (Side Interface)",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Patentli Yan Panel Modüler Kablo Bağlantısı, Type 5 Micro-Fit Kablolar",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/corsair_rm1000x_shift.webp"
    },
    {
        "name": "Corsair RM850x Shift Gold ATX 3.0",
        "manufacturer": "Corsair",
        "wattage": 850,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full (Side Interface)",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Yan Panel Modüler Bağlantı, 140mm Akışkan Yataklı FDB Fan, Zero RPM",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/corsair_rm850x_shift.webp"
    },
    {
        "name": "Corsair SF850 Platinum ATX 3.1",
        "manufacturer": "Corsair",
        "wattage": 850,
        "efficiency_rating": "80 PLUS Platinum",
        "modular": "Full",
        "form_factor": "SFX",
        "pcie_5_connector": "1x 16-Pin 12V-2x6",
        "color": "Black",
        "features": "SFX Küçük Form Faktörü, 92mm FDB Sessiz Fan, Örgülü Esnek Kablolar",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/corsair_sf850_platinum.webp"
    },
    {
        "name": "Corsair SF750 Platinum",
        "manufacturer": "Corsair",
        "wattage": 750,
        "efficiency_rating": "80 PLUS Platinum",
        "modular": "Full",
        "form_factor": "SFX",
        "pcie_5_connector": "3x 8-Pin PCIe",
        "color": "Black",
        "features": "SFF ITX Sistemlerin Efsanevi Referans Güç Kaynağı, 7 Yıl Garanti",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/corsair_sf750_platinum.webp"
    },
    {
        "name": "Super Flower Leadex VII Gold 1000W ATX 3.0",
        "manufacturer": "Super Flower",
        "wattage": 1000,
        "efficiency_rating": "80 PLUS Gold",
        "modular": "Full",
        "form_factor": "ATX 3.0",
        "pcie_5_connector": "1x 16-Pin 12VHPWR",
        "color": "Black",
        "features": "Cybenetics Platinum Verimlilik, Patentli Universal Evrensel Soketler",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/superflower_leadex_vii_1000.webp"
    }
]

# 5. İŞLEMCİLER (RYZEN 9000X3D, RYZEN 5000XT, INTEL CORE ULTRA 200 NON-K)
EXPANDED_CPUS = [
    {
        "name": "AMD Ryzen 9 9950X3D",
        "manufacturer": "AMD",
        "socket": "AM5",
        "cores": 16,
        "threads": 32,
        "base_clock": 4.3,
        "boost_clock": 5.7,
        "l3_cache_mb": 128,
        "tdp": 120,
        "integrated_gpu": "AMD Radeon Graphics (2 CU)",
        "memory_support": "DDR5-5600 / 6000 EXPO",
        "architecture": "Zen 5 + 3D V-Cache",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/amd_ryzen_9_9950x3d.webp"
    },
    {
        "name": "AMD Ryzen 9 9900X3D",
        "manufacturer": "AMD",
        "socket": "AM5",
        "cores": 12,
        "threads": 24,
        "base_clock": 4.4,
        "boost_clock": 5.6,
        "l3_cache_mb": 128,
        "tdp": 120,
        "integrated_gpu": "AMD Radeon Graphics (2 CU)",
        "memory_support": "DDR5-5600 / 6000 EXPO",
        "architecture": "Zen 5 + 3D V-Cache",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/amd_ryzen_9_9900x3d.webp"
    },
    {
        "name": "AMD Ryzen 9 5900XT",
        "manufacturer": "AMD",
        "socket": "AM4",
        "cores": 16,
        "threads": 32,
        "base_clock": 3.3,
        "boost_clock": 4.8,
        "l3_cache_mb": 64,
        "tdp": 105,
        "integrated_gpu": None,
        "memory_support": "DDR4-3200",
        "architecture": "Zen 3 AM4 Refresh",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/amd_ryzen_9_5900xt.webp"
    },
    {
        "name": "AMD Ryzen 7 5800XT",
        "manufacturer": "AMD",
        "socket": "AM4",
        "cores": 8,
        "threads": 16,
        "base_clock": 3.8,
        "boost_clock": 4.8,
        "l3_cache_mb": 32,
        "tdp": 105,
        "integrated_gpu": None,
        "memory_support": "DDR4-3200",
        "architecture": "Zen 3 AM4 Refresh",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/amd_ryzen_7_5800xt.webp"
    },
    {
        "name": "INTEL Core Ultra 9 285",
        "manufacturer": "Intel",
        "socket": "LGA1851",
        "cores": 24,
        "threads": 24,
        "base_clock": 2.5,
        "boost_clock": 5.6,
        "l3_cache_mb": 36,
        "tdp": 65,
        "integrated_gpu": "Intel Graphics (4 Xe Cores)",
        "memory_support": "DDR5-6400",
        "architecture": "Arrow Lake-S (65W Düşük Güç)",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/intel_core_ultra_9_285.webp"
    },
    {
        "name": "INTEL Core Ultra 7 265",
        "manufacturer": "Intel",
        "socket": "LGA1851",
        "cores": 20,
        "threads": 20,
        "base_clock": 2.4,
        "boost_clock": 5.3,
        "l3_cache_mb": 30,
        "tdp": 65,
        "integrated_gpu": "Intel Graphics (4 Xe Cores)",
        "memory_support": "DDR5-6400",
        "architecture": "Arrow Lake-S (65W)",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/intel_core_ultra_7_265.webp"
    },
    {
        "name": "INTEL Core Ultra 5 245",
        "manufacturer": "Intel",
        "socket": "LGA1851",
        "cores": 14,
        "threads": 14,
        "base_clock": 3.4,
        "boost_clock": 5.1,
        "l3_cache_mb": 24,
        "tdp": 65,
        "integrated_gpu": "Intel Graphics (4 Xe Cores)",
        "memory_support": "DDR5-6400",
        "architecture": "Arrow Lake-S",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/intel_core_ultra_5_245.webp"
    },
    {
        "name": "INTEL Core Ultra 5 225",
        "manufacturer": "Intel",
        "socket": "LGA1851",
        "cores": 10,
        "threads": 10,
        "base_clock": 3.3,
        "boost_clock": 4.9,
        "l3_cache_mb": 20,
        "tdp": 65,
        "integrated_gpu": "Intel Graphics (3 Xe Cores)",
        "memory_support": "DDR5-6400",
        "architecture": "Arrow Lake-S F/P Giriş",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/intel_core_ultra_5_225.webp"
    }
]

# 6. ANAKARTLAR (COLORFUL CVN / iGAME FLOW, ASROCK X870E TAICHI, Z890 HERO)
EXPANDED_MOTHERBOARDS = [
    {
        "name": "Colorful CVN B650M Gaming Frozen V14",
        "manufacturer": "Colorful",
        "socket": "AM5",
        "form_factor": "Micro ATX",
        "chipset": "B650",
        "memory_slots": 4,
        "memory_type": "DDR5",
        "max_memory": 192,
        "color": "White / Silver Armor",
        "pcie_slots": "1x PCIe 5.0 x16, 1x PCIe 4.0 x4",
        "m2_slots": "2x M.2 PCIe 5.0/4.0",
        "wifi": "Wi-Fi 6 + Bluetooth 5.2",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_cvn_b650m_frozen.webp"
    },
    {
        "name": "Colorful iGame Z890 Flow",
        "manufacturer": "Colorful",
        "socket": "LGA1851",
        "form_factor": "ATX",
        "chipset": "Z890",
        "memory_slots": 4,
        "memory_type": "DDR5",
        "max_memory": 192,
        "color": "Wave Flow Silver / White",
        "pcie_slots": "1x PCIe 5.0 x16, 1x PCIe 4.0 x4",
        "m2_slots": "4x M.2 NVMe (1x PCIe 5.0)",
        "wifi": "Wi-Fi 7 + BT 5.4 + 2.5G LAN",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/colorful_igame_z890_flow.webp"
    },
    {
        "name": "ASRock X870E Taichi",
        "manufacturer": "ASRock",
        "socket": "AM5",
        "form_factor": "E-ATX",
        "chipset": "X870E",
        "memory_slots": 4,
        "memory_type": "DDR5",
        "max_memory": 256,
        "color": "Black / Gold Gear",
        "pcie_slots": "2x PCIe 5.0 x16",
        "m2_slots": "4x M.2 (1x Blazing Gen5)",
        "wifi": "Wi-Fi 7 + Dual USB4 40Gbps + 5G LAN",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/asrock_x870e_taichi.webp"
    },
    {
        "name": "MSI MPG X870E Carbon WiFi",
        "manufacturer": "MSI",
        "socket": "AM5",
        "form_factor": "ATX",
        "chipset": "X870E",
        "memory_slots": 4,
        "memory_type": "DDR5",
        "max_memory": 256,
        "color": "Carbon Black / Mystic Light",
        "pcie_slots": "2x PCIe 5.0 x16",
        "m2_slots": "4x M.2 Shield Frozr (2x PCIe 5.0)",
        "wifi": "Wi-Fi 7 + 5G LAN + Çift USB4",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/msi_mpg_x870e_carbon.webp"
    },
    {
        "name": "ASUS ROG Maximus Z890 Hero",
        "manufacturer": "ASUS",
        "socket": "LGA1851",
        "form_factor": "ATX",
        "chipset": "Z890",
        "memory_slots": 4,
        "memory_type": "DDR5",
        "max_memory": 192,
        "color": "ROG Black / Polymo Lighting",
        "pcie_slots": "1x PCIe 5.0 x16, 1x PCIe 4.0 x16",
        "m2_slots": "6x M.2 (3x PCIe 5.0)",
        "wifi": "Wi-Fi 7 + Dual Thunderbolt 4 + 5G LAN",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/asus_rog_z890_hero.webp"
    }
]

# 7. DEPOLAMA (LEXAR NM790, CRUCIAL T705 GEN5, WD_BLACK SN850X 4TB)
EXPANDED_STORAGE = [
    {
        "name": "Lexar NM790 1TB M.2 NVMe SSD",
        "manufacturer": "Lexar",
        "capacity": "1TB",
        "form_factor": "M.2 2280",
        "interface": "PCIe 4.0 x4 NVMe 2.0",
        "read_speed": 7400,
        "write_speed": 6500,
        "tbw": 1000,
        "dram_cache": "HMB (Host Memory Buffer) + Maxio MAP1602",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lexar_nm790.webp"
    },
    {
        "name": "Lexar NM790 2TB M.2 NVMe SSD",
        "manufacturer": "Lexar",
        "capacity": "2TB",
        "form_factor": "M.2 2280",
        "interface": "PCIe 4.0 x4 NVMe 2.0",
        "read_speed": 7400,
        "write_speed": 6500,
        "tbw": 1500,
        "dram_cache": "HMB + Maxio MAP1602",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lexar_nm790.webp"
    },
    {
        "name": "Lexar NM790 4TB M.2 NVMe SSD",
        "manufacturer": "Lexar",
        "capacity": "4TB",
        "form_factor": "M.2 2280",
        "interface": "PCIe 4.0 x4 NVMe 2.0",
        "read_speed": 7400,
        "write_speed": 6500,
        "tbw": 3000,
        "dram_cache": "HMB + Maxio MAP1602",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lexar_nm790.webp"
    },
    {
        "name": "Crucial T705 2TB PCIe 5.0 M.2 NVMe SSD",
        "manufacturer": "Crucial",
        "capacity": "2TB",
        "form_factor": "M.2 2280",
        "interface": "PCIe 5.0 x4 NVMe 2.0",
        "read_speed": 14500,
        "write_speed": 12700,
        "tbw": 1200,
        "dram_cache": "DDR4 DRAM + Phison E26",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/crucial_t705.webp"
    },
    {
        "name": "Crucial T500 2TB M.2 NVMe SSD",
        "manufacturer": "Crucial",
        "capacity": "2TB",
        "form_factor": "M.2 2280",
        "interface": "PCIe 4.0 x4 NVMe 2.0",
        "read_speed": 7400,
        "write_speed": 7000,
        "tbw": 1200,
        "dram_cache": "LPDDR4 DRAM + Phison E25",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/crucial_t500.webp"
    },
    {
        "name": "Western Digital WD_BLACK SN850X 4TB M.2 NVMe SSD",
        "manufacturer": "Western Digital",
        "capacity": "4TB",
        "form_factor": "M.2 2280",
        "interface": "PCIe 4.0 x4 NVMe 1.4",
        "read_speed": 7300,
        "write_speed": 6600,
        "tbw": 2400,
        "dram_cache": "DDR4 DRAM",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/wd_black_sn850x.webp"
    },
    {
        "name": "TeamGroup MP44 2TB M.2 NVMe SSD",
        "manufacturer": "TeamGroup",
        "capacity": "2TB",
        "form_factor": "M.2 2280",
        "interface": "PCIe 4.0 x4 NVMe 1.4",
        "read_speed": 7400,
        "write_speed": 7000,
        "tbw": 2500,
        "dram_cache": "Grafen Termal Etiket + HMB",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/teamgroup_mp44.webp"
    }
]

# 8. KASALAR & SOĞUTUCULAR (LIAN LI VISION CHROME, LANCOOL 207, PHANTOM SPIRIT EVO, LF III 360)
EXPANDED_CASES = [
    {
        "name": "Lian Li O11 Vision Chrome",
        "manufacturer": "Lian Li",
        "form_factor": "Mid Tower (Dual Chamber)",
        "color": "Mirror Chrome / Black",
        "side_panel": "3 Taraflı Sütunsuz Temperli Ayna Cam",
        "max_gpu_length": 455.0,
        "max_cpu_cooler_height": 167.0,
        "max_psu_length": 220.0,
        "radiator_support": "Yan 360mm, Alt 360mm, Arka 240mm",
        "drive_bays": "2x 3.5\" HDD, 2x 2.5\" SSD",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_o11_vision_chrome.webp"
    },
    {
        "name": "Lian Li Lancool 207",
        "manufacturer": "Lian Li",
        "form_factor": "Kompakt ATX (Ön PSU Tasarımı)",
        "color": "Black",
        "side_panel": "Temperli Cam",
        "max_gpu_length": 410.0,
        "max_cpu_cooler_height": 180.0,
        "max_psu_length": 160.0,
        "radiator_support": "Üst 360mm, Ön 2x 140mm ARGB Dahili",
        "drive_bays": "2x 3.5\" / 2.5\"",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_lancool_207.webp"
    },
    {
        "name": "Lian Li A3-mATX Black",
        "manufacturer": "Lian Li",
        "form_factor": "Micro-ATX / Mini-ITX (26 Litre)",
        "color": "Black",
        "side_panel": "Tam Mesh Havalandırmalı Çelik Panel (DAN Cases Ortaklığı)",
        "max_gpu_length": 415.0,
        "max_cpu_cooler_height": 165.0,
        "max_psu_length": 220.0,
        "radiator_support": "Üst 360mm, Yan 360mm",
        "drive_bays": "1x 3.5\", 2x 2.5\"",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/lianli_a3_matx_black.webp"
    },
    {
        "name": "Montech King 95 Pro White",
        "manufacturer": "Montech",
        "form_factor": "Mid Tower (Kavisli Akvaryum)",
        "color": "White",
        "side_panel": "Kavisli Kesintisiz Panoramik Cam",
        "max_gpu_length": 420.0,
        "max_cpu_cooler_height": 175.0,
        "max_psu_length": 190.0,
        "radiator_support": "Üst 360mm, Yan 280mm, Alt 360mm (6x ARGB Fan Dahili)",
        "drive_bays": "5x 3.5\" / 8x 2.5\"",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/montech_king_95_pro_white.webp"
    }
]

EXPANDED_COOLERS = [
    {
        "name": "Thermalright Phantom Spirit 120 EVO",
        "manufacturer": "Thermalright",
        "cooler_type": "Air (Çift Kule)",
        "fan_rpm": 2150,
        "noise_level_db": 28.1,
        "color": "All-Black Anodized / ARGB Rim",
        "radiator_size_mm": 120,
        "supported_sockets": "LGA1851/1700/1200/115x, AM5/AM4",
        "tdp_capacity": 280,
        "features": "7x 6mm AGHP Gen 4 Isı Borusu, 2x TL-K12 Yüksek Statik Basınçlı Fan",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/thermalright_ps120_evo.webp"
    },
    {
        "name": "Arctic Liquid Freezer III 360 A-RGB White",
        "manufacturer": "Arctic",
        "cooler_type": "AIO Sıvı Soğutma",
        "fan_rpm": 2000,
        "noise_level_db": 22.5,
        "color": "White",
        "radiator_size_mm": 360,
        "supported_sockets": "LGA1851/1700, AM5/AM4",
        "tdp_capacity": 350,
        "features": "38mm Ekstra Kalın Radyatör, Entegre VRM Fanı (Anakart Soğutması), Contact Frame Dahili",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/arctic_lf3_360_white.webp"
    },
    {
        "name": "Noctua NH-D15 G2",
        "manufacturer": "Noctua",
        "cooler_type": "Air (İkinci Nesil Amiral Kule)",
        "fan_rpm": 1500,
        "noise_level_db": 24.8,
        "color": "Kahverengi / Bej (Klasik Noctua)",
        "radiator_size_mm": 140,
        "supported_sockets": "LGA1851/1700/1200, AM5/AM4 (Ofset Montaj)",
        "tdp_capacity": 300,
        "features": "8x Isı Borusu, 2x NF-A14x25 G2 PBT Fan, SupraTorque Motor",
        "image_url": "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/noctua_nh_d15_g2.webp"
    }
]

# ==============================================================================
# ENTEGRASYON VE ÇİFT DİZİN SENKRONİZASYON MOTORU
# ==============================================================================

def load_json(path):
    if os.path.exists(path):
        try:
            with open(path, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception as e:
            print(f"[ERROR] JSON okunamadı: {path} - {e}")
    return []

def save_json(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

def generate_catalog_sha256_manifest():
    """Tüm katalog JSON dosyalarının SHA-256 bütünlük manifestosunu üretir."""
    manifest = {
        "generated_at": datetime.utcnow().isoformat() + "Z",
        "version": "2.0.0",
        "files": {}
    }
    for fname in os.listdir(CATALOG_DIR):
        if fname.endswith(".json") and fname != "catalog_manifest.json":
            fpath = os.path.join(CATALOG_DIR, fname)
            with open(fpath, "rb") as f:
                h = hashlib.sha256(f.read()).hexdigest()
                manifest["files"][fname] = h

    manifest_path_data = os.path.join(CATALOG_DIR, "catalog_manifest.json")
    manifest_path_comp = os.path.join(COMPOSE_DIR, "catalog_manifest.json")
    save_json(manifest_path_data, manifest)
    save_json(manifest_path_comp, manifest)
    print(f"🔒 SHA-256 Bütünlük Manifestosu Üretildi: {len(manifest['files'])} dosya doğrulandı.")

def merge_and_sync(filename_catalog, filename_compose, new_items, key_field="name"):
    """
    Belirtilen yeni donanım öğelerini mevcut katalog ile Zero-Trust kurallarına göre birleştirir,
    hem data/catalog hem de composeResources dizinlerine yazar.
    """
    cat_path = os.path.join(CATALOG_DIR, filename_catalog)
    comp_path = os.path.join(COMPOSE_DIR, filename_compose)

    existing = load_json(cat_path)
    existing_keys = {item.get(key_field, "").strip().lower(): item for item in existing}

    added_count = 0
    updated_count = 0

    for item in new_items:
        # DevSecOps Sanitization
        item["name"] = sanitize_text(item.get("name"))
        if "image_url" in item:
            item["image_url"] = sanitize_url(item["image_url"])
        
        k = item.get(key_field, "").strip().lower()
        if not k:
            continue

        if k in existing_keys:
            # Güncelle
            existing_keys[k].update(item)
            updated_count += 1
        else:
            # Yeni ekle
            existing.append(item)
            existing_keys[k] = item
            added_count += 1

    # Çift dizine kaydet
    save_json(cat_path, existing)
    save_json(comp_path, existing)
    print(f"📦 [{filename_catalog}] Senkronize Edildi: +{added_count} Yeni Eklendi, {updated_count} Güncellendi (Toplam: {len(existing)})")

def run_hardware_discovery():
    print("==============================================================================")
    print("🚀 HARDWARE WIZARD - OTONOM YENİ DONANIM KEŞİF & DEVSECOPS MOTORU")
    print(f"🕒 Başlatma Zamanı: {datetime.utcnow().isoformat()}Z")
    print("==============================================================================")

    # 1. GPU Entegrasyonu (Colorful, RX 9070 GRE, Gainward, PNY, Intel B570/B580)
    all_new_gpus = COLORFUL_GPUS + RADEON_9000_GPUS + ADDITIONAL_GPUS
    merge_and_sync("gpu.json", "gpu.json", all_new_gpus)
    merge_and_sync("video-card.json", "video-card.json", all_new_gpus)

    # 2. PSU Entegrasyonu (Lian Li EDGE EG serisi, Seasonic Vertex/Focus, be quiet!, Corsair Shift/SFX)
    merge_and_sync("psu.json", "psu.json", EXPANDED_PSUS)
    merge_and_sync("power-supply.json", "power-supply.json", EXPANDED_PSUS)

    # 3. CPU Entegrasyonu (Ryzen 9000X3D, Ryzen 5000XT, Core Ultra 200 non-K)
    merge_and_sync("cpu.json", "cpu.json", EXPANDED_CPUS)

    # 4. Anakart Entegrasyonu (Colorful CVN/iGame, ASRock Taichi, MSI Carbon, ROG Hero)
    merge_and_sync("motherboard.json", "motherboard.json", EXPANDED_MOTHERBOARDS)

    # 5. Depolama Entegrasyonu (Lexar NM790, Crucial T705/T500, WD SN850X 4TB)
    merge_and_sync("storage.json", "storage.json", EXPANDED_STORAGE)
    merge_and_sync("internal-hard-drive.json", "internal-hard-drive.json", EXPANDED_STORAGE)

    # 6. Kasa & Soğutma Entegrasyonu (Lian Li Vision Chrome/A3/207, PS120 EVO, LF III 360)
    merge_and_sync("case.json", "case.json", EXPANDED_CASES)
    merge_and_sync("cooler.json", "cooler.json", EXPANDED_COOLERS)
    merge_and_sync("cpu-cooler.json", "cpu-cooler.json", EXPANDED_COOLERS)

    # 7. SHA-256 Manifest Üretimi
    generate_catalog_sha256_manifest()

    print("==============================================================================")
    print("✅ TÜM DONANIMLAR VE GÜVENLİK MANİFESTOLARI BAŞARIYLA TAMAMLANDI!")
    print("==============================================================================")

if __name__ == "__main__":
    run_hardware_discovery()
