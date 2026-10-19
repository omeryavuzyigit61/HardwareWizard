#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_laptops_razer_others.py
30 Modern 2022-2026 Razer Blade, Gigabyte AORUS, XMG & Framework Gaming Laptops
Standards: Razer / Gigabyte / XMG Technical Specifications
"""

RAZER_OTHERS_LAPTOPS_DATA = [
    # --- Razer Blade (Ultra Lüks CNC Alüminyum Amiral Gemileri) ---
    (
        "laptop_razer_blade_16_2024_oled_240hz_14900hx",
        "Razer Blade 16 (2024) 16 inç QHD+ 240Hz OLED Core i9-14900HX 32GB RAM 2TB SSD RTX 4090 175W",
        "Razer", 189999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz 0.2ms Calman Onaylı OLED Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "OLED (Samsung)", "%100 DCI-P3, 500 Nit, VESA True Black 500",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir Maks. 96GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası",
        95, 330, 2.45, "Monoblok CNC T6 Uçak Alüminyum Gövde + Patentli Çift Fanlı Geniş Buhar Odası",
        100, 168, 138
    ),
    (
        "laptop_razer_blade_16_2023_dual_mode_mini_led",
        "Razer Blade 16 (2023) Çift Modlu Mini-LED (4K 120Hz & FHD 240Hz) Core i9-13950HX 32GB RAM 1TB SSD RTX 4080 175W",
        "Razer", 149999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13950HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.50 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç Çift Modlu Mini-LED (UHD+ 120Hz 1000 Nit & FHD+ 240Hz)", 16.0, "3840x2400 (4K 120Hz) / 1920x1200 (FHD 240Hz)", 240, "Mini-LED Çift Modlu Panel", "%100 DCI-P3, 1000 Nit Tepe, HDR 1000",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        95, 330, 2.45, "CNC Eloksallı Alüminyum Kasa + Buhar Odası Cryo-Cooling",
        98, 150, 122
    ),
    (
        "laptop_razer_blade_18_2024_thunderbolt_5",
        "Razer Blade 18 (2024) Dünyanın İlk Thunderbolt 5 Laptopu 18 inç 4K 165Hz Core i9-14900HX 64GB RAM 4TB SSD RTX 4090 175W",
        "Razer", 219999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP", "Var (Advanced Optimus + MUX)",
        "18.0 inç 4K UHD+ (3840x2400) 165Hz 3ms IPS Calman Renkli Ekran", 18.0, "3840x2400 (4K UHD+ 16:10)", 165, "Fast IPS Calman", "%100 DCI-P3, 500 Nit, G-Sync",
        "64GB (2x32GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 96GB)",
        "4TB (2x2TB RAID 0) PCIe Gen4 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası",
        91, 330, 3.10, "Thunderbolt 5 (120 Gbps) + 3 Fanlı Özel Buhar Odası + 6 Hoparlörlü THX Uzamsal Ses",
        100, 168, 140
    ),
    (
        "laptop_razer_blade_14_2024_8945hs_4070",
        "Razer Blade 14 (2024) 14 inç QHD+ 240Hz Ryzen 9 8945HS 32GB RAM 1TB SSD RTX 4070 140W Full Power",
        "Razer", 109999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 9 8945HS", "8 Çekirdek / 16 İzlek (Zen 4 / Ryzen AI 39 TOPS)", "4.00 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "14.0 inç QHD+ (2560x1600) 240Hz 3ms IPS Mat Panel", 14.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 DCI-P3, 500 Nit, FreeSync Premium",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal (Değiştirilebilir)", "2x SO-DIMM Yuva (14 inçte Çift Yuva İstisnası)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x M.2 PCIe Gen4 Yuvası",
        68, 230, 1.84, "CNC T6 Alüminyum Şasi (17.99mm) + Çift Fanlı İncecik Buhar Odası (Vapor Chamber)",
        94, 125, 95
    ),

    # --- Gigabyte AORUS & AERO (Windforce Infinity & OLED) ---
    (
        "laptop_gigabyte_aorus_17x_azf_14900hx_4090",
        "Gigabyte AORUS 17X AZF 17.3 inç QHD 240Hz Core i9-14900HX 32GB RAM 2TB SSD RTX 4090 175W",
        "Gigabyte", 149999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "17.3 inç QHD (2560x1440) 240Hz 3ms IPS Düz Panel", 17.3, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 DCI-P3, Pantone Onaylı",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        99, 330, 2.80, "WINDFORCE Infinity Buhar Odası + 4 Fanlı Çepeçevre Soğutma Mimarisi",
        99, 164, 135
    ),
    (
        "laptop_gigabyte_aorus_16x_akg_14650hx_4060",
        "Gigabyte AORUS 16X AKG 16 inç WQXGA 165Hz Pantone Core i7-14650HX 16GB RAM 1TB SSD RTX 4060 140W",
        "Gigabyte", 54999.0, "https://www.itopya.com",
        "Intel Core i7-14650HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.20 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 165Hz 3ms IPS 400 Nit Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, Pantone Doğrulamalı",
        "16GB (1x16GB) DDR5 5600MHz", "2x SO-DIMM Yuva (Genişletilebilir 64GB / 1x Boş)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        99, 240, 2.30, "WINDFORCE Infinity Soğutma + Arka Işıklı Projeksiyon Lambası",
        88, 105, 78
    ),
    (
        "laptop_gigabyte_aero_16_oled_bsf_13900h_4070",
        "Gigabyte AERO 16 OLED BSF 16 inç 4K+ 60Hz AMOLED Core i9-13900H 32GB RAM 1TB SSD RTX 4070 105W",
        "Gigabyte", 79999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost)", "Var (MUX Switch)",
        "16.0 inç 4K UHD+ (3840x2400) 60Hz 0.2ms Samsung AMOLED Panel", 16.0, "3840x2400 (4K+ AMOLED 16:10)", 60, "AMOLED (Samsung)", "%100 DCI-P3, Delta E < 1 Pantone Fabrika Kalibre",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        88, 240, 2.10, "CNC Alüminyum Şasi + Çift Fanlı Sessiz Soğutma Bloğu",
        89, 105, 78
    ),

    # --- XMG & Butik Sıvı Soğutmalı Canavarlar ---
    (
        "laptop_xmg_neo_16_e24_liquid_cooling_4090",
        "XMG Neo 16 (E24) Harici OASIS Sıvı Soğutma Kutulu 16 inç 240Hz Core i9-14900HX 32GB RAM 2TB SSD RTX 4090 175W",
        "XMG", None, "https://www.xmg.gg/en/xmg-neo-16-e24",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (Sıvı Soğutma ile 0 Termal Kısılma)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms IPS Mat Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 sRGB, 500 Nit, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        99, 330, 2.50, "XMG OASIS Harici Masaüstü Sıvı Soğutma Ünitesi Entegrasyonu + Cherry MX Mekanik Klavye",
        100, 172, 144
    ),
    (
        "laptop_framework_laptop_16_diy_edition",
        "Framework Laptop 16 Modüler Değiştirilebilir GPU'lu 16 inç 165Hz Ryzen 7 7840HS 32GB RAM 1TB SSD Radeon RX 7700S",
        "Framework", None, "https://frame.work/products/laptop16-diy-edition",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "AMD Radeon RX 7700S Modüler Genişleme Modülü (8GB GDDR6)", "100W SmartShift Max TGP (Tamamen Çıkarılabilir/Yükseltilebilir)", "Var (Doğrudan Ekran Kartı Bağlantısı)",
        "16.0 inç WQXGA (2560x1600) 165Hz IPS Mat Panel", 16.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 DCI-P3, 500 Nit, FreeSync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 2280 SSD", "1x M.2 2280 + 1x M.2 2230 Yuvası (Genişletilebilir)",
        85, 240, 2.40, "Dünyanın İlk %100 Modüler Parça Değiştirilebilir ve Tamir Edilebilir Oyuncu Laptopu",
        85, 95, 70
    ),

    # --- Monster Tulpar Serisi (Yerel Pazar Liderleri) ---
    (
        "laptop_monster_tulpar_t7_v20_6_13700h_4060",
        "Monster Tulpar T7 V20.6 17.3 inç FHD 144Hz Core i7-13700H 32GB RAM 1TB SSD RTX 4060 140W",
        "Monster", 44999.0, "https://www.monsternotebook.com.tr",
        "Intel Core i7-13700H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.40 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (NVIDIA MUX Switch)",
        "17.3 inç FHD (1920x1080) 144Hz IPS Mat Panel", 17.3, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Maks. 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        80, 230, 2.50, "Çift Fanlı Geniş Bakır Kanatçıklı Soğutma Mimarisi + RGB Klavye",
        87, 102, 75
    ),
    (
        "laptop_monster_tulpar_t5_v23_3_12450h_4050",
        "Monster Tulpar T5 V23.3 15.6 inç FHD 144Hz Core i5-12450H 16GB RAM 512GB SSD RTX 4050 100W",
        "Monster", 28999.0, "https://www.monsternotebook.com.tr",
        "Intel Core i5-12450H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.00 GHz Taban / 4.40 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "100W TGP (85W + 15W Dynamic Boost)", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (1x16GB) DDR4 3200MHz", "2x SO-DIMM Yuva (1x Boş Yuva)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 Yuvası",
        54, 180, 2.10, "Kompakt Çift Fanlı Soğutma Bloğu",
        78, 80, 54
    ),
]
