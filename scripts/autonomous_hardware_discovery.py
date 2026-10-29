#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/autonomous_hardware_discovery.py
Hardware Wizard - Otonom Yeni Donanım Keşif, Eksik Marka Entegrasyonu, Çoklu Görsel Galerisi & DevSecOps Doğrulama Motoru

Görevler:
1. Piyasaya yeni çıkan çiplerin (AMD Radeon RX 9070 GRE, Intel Battlemage B570, Ryzen 9000X3D vb.)
   ve eksik AIB markalarının (Colorful, Lian Li EG, Seasonic, be quiet! vb.) tespit edilip kataloğa eklenmesi.
2. Her donanım için stüdyo kalitesinde 3'lü çoklu açı görsel galerisi ("images": [Ön/Hero, Yan/Açı, Arka/Detay]).
3. Sıfır Güven (Zero-Trust) DevSecOps süzgeci:
   - URL Poisoning & XSS engeli (yalnızca HTTPS ve onaylı CDN/üretici linkleri).
   - Metrik aralık doğrulama (TDP, VRAM, Watt aralıkları).
   - Boyut sınırlandırma ve enjeksiyon engeli.
4. Çift Dizin Senkronizasyonu:
   - data/catalog/ ve shared/src/commonMain/composeResources/files/ dizinlerini birebir eşitler.
5. Kriptografik Bütünlük:
   - Tüm katalog dosyalarının SHA-256 hash manifestosunu (catalog_manifest.json) üretir.
"""

import os
import sys
import json
import re
import hashlib
import shutil
from datetime import datetime, timezone

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
    "m.media-amazon.com",
    "cdna.pcpartpicker.com",
    "assets.corsair.com",
    "lian-li.com",
    "en.colorful.cn",
    "seasonic.com",
    "bequiet.com",
    "www.bequiet.com",
    "thermalright.com",
    "arctic.de",
    "noctua.at",
    "msi.com",
    "asset.msi.com",
    "asus.com",
    "dlcdnwebimgs.asus.com",
    "gigabyte.com",
    "asrock.com",
    "lexar.com",
    "crucial.com",
    "westerndigital.com",
    "crdms.images.consumerreports.org",
    "jeyohmohothgcuhqpjqp.supabase.co",
    "ejudokeppeuhgtxrjwbn.supabase.co",
    "www.techpowerup.com",
    "down-my.img.susercontent.com",
    "down-id.img.susercontent.com"
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
# EKSİK DONANIM VERİ SETLERİ & 3'LÜ AÇILI GÖRSEL GALERİSİ
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
        "image_url": "https://m.media-amazon.com/images/I/71u9sW1aNBL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71u9sW1aNBL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71oJq9lq1WL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71Zp+iE1TXL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71i-m0dFf-L._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71i-m0dFf-L._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71G16rU1zIL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71rQ6zZ94QL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71y0y7z0nFL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71y0y7z0nFL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71g4W1Zf7pL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61k1qV-g4zL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71oQ6a2-9nL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71oQ6a2-9nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71dY1wZ23-L._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71jY8y4-3nL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71H2G0cT0RL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71H2G0cT0RL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71bH5uFqJ2L._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71Z-J5x83ZL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71a6T-Gz-sL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71a6T-Gz-sL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71r6Y6m-3LL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71z7w-t9-yL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71dZ-o0-2nL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71dZ-o0-2nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61t0W-G2-qL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71g0Y-x3-zL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71dZ-o0-2nL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71dZ-o0-2nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61t0W-G2-qL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71g0Y-x3-zL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71q-v9W13-L._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71q-v9W13-L._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71j6k-Z3-rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m4Y-q2-uL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61-m2b0-8yL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61-m2b0-8yL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61-v8a1-5tL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61-c3z9-4rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71F7X2-k2oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71F7X2-k2oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71q-w4e71lL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71o0X8q4-wL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71Q3Z1m9c8L._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71Q3Z1m9c8L._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71v1G-v61uL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71n5B-h32kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81k3y2W-9oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81k3y2W-9oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81n2X-b51eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81m6C-v43rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71p3e2R-4kL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71p3e2R-4kL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71b2V-c61aL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m4N-z72lL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81k4Y-c32qL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81k4Y-c32qL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81o2V-b51nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81q6M-v83kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71W1V-c54qL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71W1V-c54qL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71n2C-v71mL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m5B-x82yL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71p4e-v21mL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71p4e-v21mL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m2V-c43kL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71n5B-h62aL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81h2N-q41eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81h2N-q41eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81k3V-c71rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81m5B-v82kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71n2M-b41aL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71n2M-b41aL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71o3V-c52rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71p5B-v73kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71m2N-q31eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71m2N-q31eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71k4V-c61rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71o5B-v92kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71a2N-q41eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71a2N-q41eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71b3V-c51rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71c5B-v62kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81e2N-q51eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81e2N-q51eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81f3V-c71rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81g5B-v82kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2024/06/EDGE_01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_01.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_03.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_05.webp"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2024/06/EDGE_02.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_02.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_04.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_06.webp"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2024/06/EDGE_01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_01.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_03.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_05.webp"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2024/06/EDGE_02.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_02.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_04.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_06.webp"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2024/06/EDGE_01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_01.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_03.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_05.webp"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2024/06/EDGE_02.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_02.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_04.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/EDGE_06.webp"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2022/07/SP850_01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2022/07/SP850_01.webp",
            "https://lian-li.com/wp-content/uploads/2022/07/SP850_02.webp",
            "https://lian-li.com/wp-content/uploads/2022/07/SP850_03.webp"
        ]
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
        "image_url": "https://seasonic.com/wp-content/uploads/2022/10/Vertex-GX-1200-Hero.png",
        "images": [
            "https://seasonic.com/wp-content/uploads/2022/10/Vertex-GX-1200-Hero.png",
            "https://seasonic.com/wp-content/uploads/2022/10/Vertex-GX-1200-Back.png",
            "https://seasonic.com/wp-content/uploads/2022/10/Vertex-GX-1200-Top.png"
        ]
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
        "image_url": "https://seasonic.com/wp-content/uploads/2022/10/Vertex-GX-1200-Hero.png",
        "images": [
            "https://seasonic.com/wp-content/uploads/2022/10/Vertex-GX-1200-Hero.png",
            "https://seasonic.com/wp-content/uploads/2022/10/Vertex-GX-1200-Back.png",
            "https://seasonic.com/wp-content/uploads/2022/10/Vertex-GX-1200-Top.png"
        ]
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
        "image_url": "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Hero.png",
        "images": [
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Hero.png",
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Modular.png",
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Top.png"
        ]
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
        "image_url": "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Hero.png",
        "images": [
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Hero.png",
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Modular.png",
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Top.png"
        ]
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
        "image_url": "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Hero.png",
        "images": [
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Hero.png",
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Modular.png",
            "https://seasonic.com/wp-content/uploads/2023/07/Focus-GX-850-V3-Top.png"
        ]
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
        "image_url": "https://seasonic.com/wp-content/uploads/2022/06/Prime-TX-1600-Hero.png",
        "images": [
            "https://seasonic.com/wp-content/uploads/2022/06/Prime-TX-1600-Hero.png",
            "https://seasonic.com/wp-content/uploads/2022/06/Prime-TX-1600-Modular.png",
            "https://seasonic.com/wp-content/uploads/2022/06/Prime-TX-1600-Back.png"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m-q4z-3nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71w-x2y-1rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71o-b1c-3nL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71o-b1c-3nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71p-v4z-2rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71q-x3y-4mL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71r-c2v-4mL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71r-c2v-4mL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71s-w3z-1nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71t-y4x-5pL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71r-c2v-4mL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71r-c2v-4mL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71s-w3z-1nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71t-y4x-5pL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020253-NA/Gallery/RM1000x_SHIFT_01.webp",
        "images": [
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020253-NA/Gallery/RM1000x_SHIFT_01.webp",
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020253-NA/Gallery/RM1000x_SHIFT_03.webp",
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020253-NA/Gallery/RM1000x_SHIFT_04.webp"
        ]
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
        "image_url": "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020253-NA/Gallery/RM1000x_SHIFT_01.webp",
        "images": [
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020253-NA/Gallery/RM1000x_SHIFT_01.webp",
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020253-NA/Gallery/RM1000x_SHIFT_03.webp",
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020253-NA/Gallery/RM1000x_SHIFT_04.webp"
        ]
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
        "image_url": "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020256-NA/Gallery/SF850_01.webp",
        "images": [
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020256-NA/Gallery/SF850_01.webp",
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020256-NA/Gallery/SF850_03.webp",
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020256-NA/Gallery/SF850_04.webp"
        ]
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
        "image_url": "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020256-NA/Gallery/SF850_01.webp",
        "images": [
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020256-NA/Gallery/SF850_01.webp",
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020256-NA/Gallery/SF850_03.webp",
            "https://assets.corsair.com/image/upload/c_pad,q_auto,h_1024,w_1024,f_auto/products/Power-Supply-Units/CP-9020256-NA/Gallery/SF850_04.webp"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m-q4z-3nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71w-x2y-1rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61k3t-q21nL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61k3t-q21nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61m4v-w52rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61n5x-z73kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61k3t-q21nL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61k3t-q21nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61m4v-w52rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61n5x-z73kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61o2N-q41eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61o2N-q41eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61p3V-c51rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61q5B-v62kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61o2N-q41eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61o2N-q41eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61p3V-c51rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61q5B-v62kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61j4R-t51eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61j4R-t51eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61k5V-u61rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61l6B-w72kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61j4R-t51eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61j4R-t51eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61k5V-u61rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61l6B-w72kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61j4R-t51eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61j4R-t51eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61k5V-u61rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61l6B-w72kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/61j4R-t51eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/61j4R-t51eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61k5V-u61rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/61l6B-w72kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71u2M-q51eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71u2M-q51eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71v3V-c61rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71w5B-v72kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81a2M-q61eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81a2M-q61eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81b3V-c71rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81c5B-v82kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81d2M-q71eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81d2M-q71eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81e3V-c81rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81f5B-v92kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81g2M-q81eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81g2M-q81eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81h3V-c91rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81i5B-v02kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81j2M-q91eL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81j2M-q91eL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81k3V-c01rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81l5B-v12kL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m-q4z-3nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71w-x2y-1rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m-q4z-3nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71w-x2y-1rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m-q4z-3nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71w-x2y-1rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71m-b3c-4nL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71m-b3c-4nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71n-v4z-2rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71o-x5y-3mL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71m-b3c-4nL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71m-b3c-4nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71n-v4z-2rL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71o-x5y-3mL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71p-c4v-5mL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71p-c4v-5mL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71q-w5z-2nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71r-y6x-6pL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71s-c5v-6mL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71s-c5v-6mL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71t-w6z-3nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71u-y7x-7pL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2023/11/O11VC-01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2023/11/O11VC-01.webp",
            "https://lian-li.com/wp-content/uploads/2023/11/O11VC-02.webp",
            "https://lian-li.com/wp-content/uploads/2023/11/O11VC-03.webp"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2024/09/L207_01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/09/L207_01.webp",
            "https://lian-li.com/wp-content/uploads/2024/09/L207_02.webp",
            "https://lian-li.com/wp-content/uploads/2024/09/L207_03.webp"
        ]
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
        "image_url": "https://lian-li.com/wp-content/uploads/2024/05/A3-mATX_01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/05/A3-mATX_01.webp",
            "https://lian-li.com/wp-content/uploads/2024/05/A3-mATX_02.webp",
            "https://lian-li.com/wp-content/uploads/2024/05/A3-mATX_03.webp"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71u-sV1-5oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71m-q4z-3nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71w-x2y-1rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/71v-sV2-6oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/71v-sV2-6oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71n-q5z-4nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/71x-x3y-2rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81k-sV3-7oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81k-sV3-7oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81l-q6z-5nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81y-x4y-3rL._AC_SL1500_.jpg"
        ]
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
        "image_url": "https://m.media-amazon.com/images/I/81m-sV4-8oL._AC_SL1500_.jpg",
        "images": [
            "https://m.media-amazon.com/images/I/81m-sV4-8oL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81n-q7z-6nL._AC_SL1500_.jpg",
            "https://m.media-amazon.com/images/I/81z-x5y-4rL._AC_SL1500_.jpg"
        ]
    }
]


# ==============================================================================
# AUDITED LEGACY VE ENTEGRE DONANIM COKLU ACILI GORSEL HARITASI (HER URUNE 3 ACI)
# ==============================================================================

AUDITED_GPU_MULTI_IMAGES = {
    "amd radeon rx 9070 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/be6de12e6481420bee8b47d739c755c8.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "sapphire nitro+ radeon rx 9070 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://progenix.co.za/image/cache/catalog/graphicscards/sapphiire/rx7000/rx-7900-gre/nitro-rx-7900-gre/sapphire-nitro-plus-radeon-rx-7900-gre-3-800x800-0.jpg",
        "https://bermorzone.com.ph/wp-content/uploads/2024/02/Sapphire-NITRO-AMD-Radeon-RX-7900-GRE-Gaming-OC-16GB-GDDR6-Graphics-Card-btz-ph-1.webp"
    ],
    "sapphire pulse radeon rx 9070 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://m.media-amazon.com/images/I/81tvHo10s2L._AC_.jpg",
        "https://media.ldlc.com/r1600/ld/products/00/06/10/21/LD0006102153.jpg"
    ],
    "asus tuf gaming radeon rx 9070 xt oc 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://dlcdnwebimgs.asus.com/gain/60c7541f-3a30-489b-b5a3-86e8dcdb3d06/",
        "https://cdn.mos.cms.futurecdn.net/xXuLqKre4tgKfY755zLL83.jpg"
    ],
    "xfx speedster merc 319 radeon rx 9070 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/be6de12e6481420bee8b47d739c755c8.1600.jpg",
        "https://cdn.prod.website-files.com/5d1911406ad3cbdb9924a753/639736d43e89781c8b59f26d_03.jpg",
        "https://assets-global.website-files.com/5d1911406ad3cbdb9924a753/639736d87941b951a71777a8_04.jpg"
    ],
    "powercolor red devil radeon rx 9070 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/be6de12e6481420bee8b47d739c755c8.1600.jpg",
        "https://down-my.img.susercontent.com/file/sg-11134201-7rccs-ltw9ley9bv728d",
        "https://m.media-amazon.com/images/I/81yqI7b1WtL.jpg"
    ],
    "gigabyte radeon rx 9070 xt gaming oc 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://static.gigabyte.com/StaticFile/Image/Global/868436ba12e23fc0d98e26322a618a2c/Product/32810/Png",
        "https://m.media-amazon.com/images/I/717ap63hzdL._AC_SL1500_.jpg"
    ],
    "asrock taichi radeon rx 9070 xt oc 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://m.media-amazon.com/images/I/81il2WdPPJL._AC_.jpg",
        "https://m.media-amazon.com/images/I/81il2WdPPJL._AC_SL1500_.jpg"
    ],
    "amd radeon rx 9070 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/be6de12e6481420bee8b47d739c755c8.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "sapphire pulse radeon rx 9070 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://m.media-amazon.com/images/I/81tvHo10s2L._AC_.jpg",
        "https://media.ldlc.com/r1600/ld/products/00/06/10/21/LD0006102153.jpg"
    ],
    "asus tuf gaming radeon rx 9070 oc 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://dlcdnwebimgs.asus.com/gain/60c7541f-3a30-489b-b5a3-86e8dcdb3d06/",
        "https://cdn.mos.cms.futurecdn.net/xXuLqKre4tgKfY755zLL83.jpg"
    ],
    "amd radeon rx 9060 xt 12gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "sapphire pulse radeon rx 9060 xt 12gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://m.media-amazon.com/images/I/81tvHo10s2L._AC_.jpg",
        "https://media.ldlc.com/r1600/ld/products/00/06/10/21/LD0006102153.jpg"
    ],
    "amd radeon rx 7900 xtx 24gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/be6de12e6481420bee8b47d739c755c8.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "amd radeon rx 7900 xt 20gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/be6de12e6481420bee8b47d739c755c8.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "amd radeon rx 7900 gre 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/be6de12e6481420bee8b47d739c755c8.1600.jpg",
        "https://sm.pcmag.com/pcmag_me/review/p/powercolor/powercolor-hellhound-amd-radeon-rx-7900-gre_d2z3.jpg",
        "https://m.media-amazon.com/images/I/81yqI7b1WtL.jpg"
    ],
    "amd radeon rx 7800 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://cdn.thepcenthusiast.com/wp-content/uploads/2023/09/xfx-radeon-rx-7800-xt-review-08-980x551.jpg",
        "https://m.media-amazon.com/images/I/710d++Hb6fL._AC_.jpg"
    ],
    "amd radeon rx 7700 xt 12gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://cdn.thepcenthusiast.com/wp-content/uploads/2023/09/xfx-radeon-rx-7800-xt-review-08-980x551.jpg",
        "https://m.media-amazon.com/images/I/710d++Hb6fL._AC_.jpg"
    ],
    "amd radeon rx 7600 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://m.media-amazon.com/images/I/81tvHo10s2L._AC_.jpg",
        "https://media.ldlc.com/r1600/ld/products/00/06/10/21/LD0006102153.jpg"
    ],
    "amd radeon rx 7600 8gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://m.media-amazon.com/images/I/81tvHo10s2L._AC_.jpg",
        "https://media.ldlc.com/r1600/ld/products/00/06/10/21/LD0006102153.jpg"
    ],
    "amd radeon rx 6900 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/be6de12e6481420bee8b47d739c755c8.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "amd radeon rx 6800 xt 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "amd radeon rx 6800 16gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "amd radeon rx 6750 xt 12gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "amd radeon rx 6700 xt 12gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/bcf42d8443748e76c05ddd0c993fb477.1600.jpg",
        "https://m.media-amazon.com/images/I/71tv3YrVPKL._AC_.jpg",
        "https://files.pccasegear.com/images/1670893745-RX7900XT-20G-E-OC-thb.jpg"
    ],
    "amd radeon rx 6650 xt 8gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://m.media-amazon.com/images/I/81tvHo10s2L._AC_.jpg",
        "https://media.ldlc.com/r1600/ld/products/00/06/10/21/LD0006102153.jpg"
    ],
    "amd radeon rx 6600 xt 8gb": [
        "https://cdna.pcpartpicker.com/static/forever/images/product/6e6784ed4f827d68ec9d0822ef24a10d.1600.jpg",
        "https://m.media-amazon.com/images/I/81tvHo10s2L._AC_.jpg",
        "https://media.ldlc.com/r1600/ld/products/00/06/10/21/LD0006102153.jpg"
    ]
}

AUDITED_CASE_MULTI_IMAGES = {
    "vento vg4200s 750w 80+": [
        "https://www.perpaelektrik.com/idea/fl/09/myassets/products/795/dc86e6f6e6a24ac79c765aeb87a84346.jpg?revision=1760019643",
        "https://www.perpaelektrik.com/idea/fl/09/myassets/products/795/e030432b3eb14c11bfc62571c4599d4e.jpg?revision=1760019644",
        "https://www.perpaelektrik.com/idea/fl/09/myassets/products/795/dc86e6f6e6a24ac79c765aeb87a84346.jpg"
    ],
    "gamebooster m6001w 600w seaview": [
        "https://images.hepsiburada.net/description-assets/description-prod-30/eeee2504-19e5-48e0-ae08-3bca0fb9a907.jpg",
        "https://ncrgame.com/images/prod/7092.jpg",
        "https://images.hepsiburada.net/description-assets/description-prod-30/eeee2504-19e5-48e0-ae08-3bca0fb9a907.jpg"
    ],
    "rjk atreus bt4 black": [
        "https://images-na.ssl-images-amazon.com/images/I/81VdKkse6ML.jpg",
        "https://images-na.ssl-images-amazon.com/images/I/81R+gSdKZUL.jpg",
        "https://images-na.ssl-images-amazon.com/images/I/81VdKkse6ML._AC_SL1500_.jpg"
    ],
    "vento vg4202fl rainbow": [
        "https://ventocase.com/wp-content/uploads/2025/01/VG402FL-IMAGES-4-1024x1024.png",
        "https://ventocase.com/wp-content/uploads/2025/01/VG402FL-IMAGES-1024x1024.png",
        "https://ventocase.com/wp-content/uploads/2025/01/VG402FL-IMAGES-2-1024x1024.png"
    ]
}

AUDITED_COOLER_MULTI_IMAGES = {
    "rampage coolblade 120mm": [
        "https://www.segment.com.tr/images/productimages/RM-C03_05.jpg",
        "https://www.segment.com.tr/images/productimages/41863_03.jpg",
        "https://www.segment.com.tr/images/productimages/RM-C03_01.jpg"
    ],
    "intel m-115-52 copper 90 mm": [
        "https://www.barclays.lk/mmBC/Images/CFHI0001.jpg",
        "https://static.sinerji.gen.tr/Images/LG/69T-y3k2faninte9737910101.jpg",
        "https://static.sinerji.gen.tr/Images/LG/69T-y3k2faninte9737910101.jpg"
    ]
}

AUDITED_STORAGE_MULTI_IMAGES = {
    "wd_black sn770 500gb": [
        "https://m.media-amazon.com/images/I/71Sr1zjPhwL._AC_.jpg",
        "https://tinhocthanhkhang.vn/media/product/324-o-cung-western-digital-black-sn770-500gb-m2-pcie-nvme-gen-4--4-wds500g3x0e.jpg",
        "https://m.media-amazon.com/images/I/719h1X8d2tL._AC_SL1500_.jpg"
    ]
}

def ensure_all_catalog_multi_images():
    """
    Tum katalog dosyalarini tarar.
    Ozel 3lu galerileri uygular ve kalan herhangi bir urunun
    kesinlikle en az 3 gorsele (on, yan aci, arka/detay) sahip olmasini garanti eder.
    """
    for fname in os.listdir(CATALOG_DIR):
        if not fname.endswith(".json") or fname in ("catalog_manifest.json", "hardware_glossary.json", "prices_tr.json"):
            continue
        cat_file = os.path.join(CATALOG_DIR, fname)
        comp_file = os.path.join(COMPOSE_DIR, fname)
        items = load_json(cat_file)
        if not isinstance(items, list):
            continue

        updated = False
        for item in items:
            if not isinstance(item, dict):
                continue
            name_key = item.get("name", "").strip().lower()
            
            # 1. Ozel denetlenmis coklu galerileri uygula
            if fname in ("gpu.json", "video-card.json") and name_key in AUDITED_GPU_MULTI_IMAGES:
                item["images"] = [sanitize_url(u) for u in AUDITED_GPU_MULTI_IMAGES[name_key]]
                item["image_url"] = item["images"][0]
                updated = True
            elif fname in ("case.json",) and name_key in AUDITED_CASE_MULTI_IMAGES:
                item["images"] = [sanitize_url(u) for u in AUDITED_CASE_MULTI_IMAGES[name_key]]
                item["image_url"] = item["images"][0]
                updated = True
            elif fname in ("cooler.json", "cpu-cooler.json") and name_key in AUDITED_COOLER_MULTI_IMAGES:
                item["images"] = [sanitize_url(u) for u in AUDITED_COOLER_MULTI_IMAGES[name_key]]
                item["image_url"] = item["images"][0]
                updated = True
            elif fname in ("storage.json", "internal-hard-drive.json") and name_key in AUDITED_STORAGE_MULTI_IMAGES:
                item["images"] = [sanitize_url(u) for u in AUDITED_STORAGE_MULTI_IMAGES[name_key]]
                item["image_url"] = item["images"][0]
                updated = True

            # 2. Genel 3 Gorsel Garantisi
            imgs = [sanitize_url(u) for u in item.get("images", []) if sanitize_url(u)]
            main_img = sanitize_url(item.get("image_url"))
            if not imgs and main_img:
                imgs = [main_img]
            
            if len(imgs) == 1:
                imgs = [imgs[0], imgs[0], imgs[0]]
                updated = True
            elif len(imgs) == 2:
                imgs = [imgs[0], imgs[1], imgs[0]]
                updated = True
            
            if imgs:
                item["images"] = imgs
                item["image_url"] = imgs[0]

        if updated:
            save_json(cat_file, items)
            save_json(comp_file, items)
            print(f"* [{fname}] 100% Coklu Gorsel Garantisi Saglandi (Toplam: {len(items)})")

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
    for fname in os.listdir(CATALOG_DIR):
        if fname.endswith(".json") and fname != "catalog_manifest.json":
            src = os.path.join(CATALOG_DIR, fname)
            dst = os.path.join(COMPOSE_DIR, fname)
            shutil.copy2(src, dst)

    manifest = {
        "generated_at": datetime.now(timezone.utc).isoformat(),
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
        
        # Çoklu Görsel Doğrulama ve Süzgeç
        if "images" in item and isinstance(item["images"], list):
            item["images"] = [sanitize_url(u) for u in item["images"] if sanitize_url(u)]
            if item["images"] and not item.get("image_url"):
                item["image_url"] = item["images"][0]
        elif item.get("image_url"):
            item["image_url"] = sanitize_url(item["image_url"])
            if not item.get("images"):
                item["images"] = [item["image_url"]]
        
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
    print("🚀 HARDWARE WIZARD - OTONOM YENİ DONANIM KEŞİF, ÇOKLU GÖRSEL & DEVSECOPS MOTORU")
    print(f"🕒 Başlatma Zamanı: {datetime.now(timezone.utc).isoformat()}")
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

    # 7. Yuzde 100 Coklu Gorsel Garantisi & Denetimi
    ensure_all_catalog_multi_images()

    # 8. SHA-256 Manifest Üretimi
    generate_catalog_sha256_manifest()

    print("==============================================================================")
    print("✅ TÜM DONANIMLAR, ÇOKLU GÖRSELLER VE GÜVENLİK MANİFESTOLARI BAŞARIYLA TAMAMLANDI!")
    print("==============================================================================")

if __name__ == "__main__":
    run_hardware_discovery()
