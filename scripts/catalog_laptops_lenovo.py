#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_laptops_lenovo.py
40 Modern 2022-2026 Lenovo Legion & LOQ Gaming Laptops
Standards: Lenovo PSREF & Notebookcheck Testing Labs
"""

LENOVO_LAPTOPS_DATA = [
    # --- Legion 9i & Pro 7i (Amiral Gemisi) ---
    (
        "laptop_lenovo_legion_9i_16irx8_liquid",
        "Lenovo Legion 9i 16IRX8 Dahili Sıvı Soğutmalı 16 inç 3.2K 165Hz Mini-LED Core i9-13980HX 64GB RAM 2TB SSD RTX 4090 175W",
        "Lenovo", 189999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13980HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.60 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç 3.2K (3200x2000) 165Hz 3ms Mini-LED PureSight Gaming", 16.0, "3200x2000 (3.2K 16:10)", 165, "Mini-LED (1200 Nit Tepe)", "%100 DCI-P3, %100 Adobe RGB, G-Sync",
        "64GB (2x32GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB (2x1TB RAID 0) PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası",
        99, 330, 2.56, "Dünyanın İlk Entegre Sıvı Soğutma Pompası + Dövme Karbon Kapak + Coldfront Sıvı Metal",
        100, 168, 140
    ),
    (
        "laptop_lenovo_legion_pro_7i_16irx9h_14900hx",
        "Lenovo Legion Pro 7i Gen 9 16IRX9H 16 inç WQXGA 240Hz Core i9-14900HX 32GB RAM 1TB SSD RTX 4090 175W",
        "Lenovo", 154999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms PureSight Gaming IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS PureSight", "%100 DCI-P3, 500 Nit, DisplayHDR 400, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD (7000 MB/s)", "2x M.2 PCIe Gen4 NVMe Yuvası (1x Boş Yuva)",
        99, 330, 2.62, "Legion Coldfront 5.0 Devasa Buhar Odası (Vapor Chamber) + Sıvı Metal Termal Bileşik",
        100, 165, 136
    ),
    (
        "laptop_lenovo_legion_pro_7_16arx8h_7945hx",
        "Lenovo Legion Pro 7 Gen 8 16ARX8H 16 inç WQXGA 240Hz Ryzen 9 7945HX 32GB RAM 1TB SSD RTX 4080 175W",
        "Lenovo", 119999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 9 7945HX", "16 Çekirdek / 32 İzlek (Zen 4)", "2.50 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (NVIDIA Advanced Optimus + MUX)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms IPS 500 Nit PureSight", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 sRGB, 500 Nit, DisplayHDR 400, G-Sync",
        "32GB (2x16GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        99, 330, 2.60, "Legion Coldfront 5.0 Buhar Odası + Sıvı Metal Soğutma",
        98, 152, 122
    ),

    # --- Legion Pro 5i & Pro 5 (F/P Espor Amiral Gemisi) ---
    (
        "laptop_lenovo_legion_pro_5i_16irx9_14700hx_4070",
        "Lenovo Legion Pro 5i Gen 9 16IRX9 16 inç WQXGA 240Hz Core i7-14700HX 32GB RAM 1TB SSD RTX 4070 140W",
        "Lenovo", 74999.0, "https://www.itopya.com",
        "Intel Core i7-14700HX", "20 Çekirdek (8P + 12E) / 28 İzlek", "2.10 GHz Taban / 5.50 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms PureSight Gaming IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS PureSight", "%100 sRGB, 500 Nit, DisplayHDR 400, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası (1x Boş Yuva)",
        80, 300, 2.50, "Legion Coldfront 5.0 Hibrit Isı Borusu Mimarisi + 3D Bıçaklı Çift Fan",
        94, 128, 98
    ),
    (
        "laptop_lenovo_legion_pro_5_16arx8_7745hx_4060",
        "Lenovo Legion Pro 5 Gen 8 16ARX8 16 inç 165Hz WQXGA Ryzen 7 7745HX 16GB RAM 512GB SSD RTX 4060 140W",
        "Lenovo", 52999.0, "https://www.vatanbilgisayar.com",
        "AMD Ryzen 7 7745HX", "8 Çekirdek / 16 İzlek (Zen 4)", "3.60 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç WQXGA (2560x1600) 165Hz 3ms PureSight IPS Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 165, "IPS PureSight", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        80, 300, 2.50, "Coldfront 5.0 Çift Fan + 4 Egzoz Çıkışı",
        88, 105, 78
    ),
    (
        "laptop_lenovo_legion_pro_5i_16irx8_13700hx_4060",
        "Lenovo Legion Pro 5i Gen 8 16IRX8 16 inç WQXGA 165Hz Core i7-13700HX 16GB RAM 1TB SSD RTX 4060 140W",
        "Lenovo", 54999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13700HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.10 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç WQXGA (2560x1600) 165Hz 3ms IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        80, 300, 2.55, "Coldfront 5.0 + Legion TrueStrike RGB Klavye",
        88, 106, 79
    ),

    # --- Legion Slim 7i & Slim 5 (İnce, Alüminyum & OLED) ---
    (
        "laptop_lenovo_legion_slim_7i_16irh8_3k",
        "Lenovo Legion Slim 7i Gen 8 16IRH8 Tam Metal 16 inç 3.2K 165Hz Core i9-13900H 32GB RAM 1TB SSD RTX 4070 115W",
        "Lenovo", 89999.0, "https://www.itopya.com",
        "Intel Core i9-13900H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "115W TGP (90W + 25W Dynamic Boost / İnce Şasi)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç 3.2K (3200x2000) 165Hz 3ms PureSight Gaming IPS Ekran", 16.0, "3200x2000 (3.2K 16:10)", 165, "Fast IPS PureSight", "%100 DCI-P3, 430 Nit, Dolby Vision, G-Sync",
        "32GB (16GB Lehimli + 16GB DDR5 5200MHz)", "1x SO-DIMM Yuva (Genişletilebilir Maks. 32GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        99, 230, 2.00, "Tamamen CNC Eloksallı Alüminyum Gövde + Coldfront 5.0 İnce Fanlar (Yalnızca 2.0 Kg)",
        93, 122, 92
    ),
    (
        "laptop_lenovo_legion_slim_5_14aph8_oled",
        "Lenovo Legion Slim 5 14APH8 14.5 inç 2.8K 120Hz OLED Ryzen 7 7840HS 16GB RAM 512GB SSD RTX 4060 105W",
        "Lenovo", 59999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "14.5 inç 2.8K (2880x1800) 120Hz 0.2ms OLED Parlak Panel", 14.5, "2880x1800 (2.8K OLED 16:10)", 120, "OLED", "%100 DCI-P3, 400 Nit, VESA True Black 500",
        "16GB LPDDR5X 6400MHz Çift Kanal (Lehimli)", "Lehimli Çift Kanal LPDDR5X",
        "512GB PCIe 4.0 NVMe M.2 2280 SSD", "2x M.2 PCIe Gen4 Yuvası (1x 2280 Boş Yuva)",
        73, 170, 1.75, "Alüminyum Üst Kapak + Çift Soğutma Fanı + 3 Bakır Isı Borusu (1.75 Kg)",
        87, 98, 74
    ),

    # --- Lenovo LOQ 15 & LOQ 16 (F/P Espor Şampiyonları) ---
    (
        "laptop_lenovo_loq_15irx9_13450hx_4060",
        "Lenovo LOQ 15IRX9 15.6 inç FHD 144Hz Core i5-13450HX 16GB RAM 512GB SSD RTX 4060 115W Full Power",
        "Lenovo", 36999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13450HX", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.60 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "115W Full Power (100W + 15W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (1x16GB) DDR5 4800MHz", "2x SO-DIMM Yuva (Genişletilebilir 32GB / 1x Boş Yuva)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        60, 170, 2.38, "Lenovo LA1 AI Çipi + Çift Fan Arkaya Eğimli Egzoz Tasarımı",
        85, 96, 70
    ),
    (
        "laptop_lenovo_loq_15arp9_7435hs_4060",
        "Lenovo LOQ 15ARP9 15.6 inç FHD 144Hz Ryzen 7 7435HS 16GB RAM 512GB SSD RTX 4060 115W",
        "Lenovo", 34999.0, "https://www.itopya.com",
        "AMD Ryzen 7 7435HS", "8 Çekirdek / 16 İzlek (Zen 3+)", "3.10 GHz Taban / 4.50 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "115W TGP", "Var (NVIDIA MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        60, 170, 2.38, "Çift Fanlı Ters Akışlı Soğutma Mimarisi",
        84, 94, 68
    ),
    (
        "laptop_lenovo_loq_15ahp9_8845hs_4070",
        "Lenovo LOQ 15AHP9 15.6 inç FHD 144Hz Ryzen 7 8845HS 16GB RAM 512GB SSD RTX 4070 115W",
        "Lenovo", 48999.0, "https://www.vatanbilgisayar.com",
        "AMD Ryzen 7 8845HS", "8 Çekirdek / 16 İzlek (Zen 4 / Ryzen AI)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "115W TGP", "Var (Advanced Optimus + MUX)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        60, 230, 2.38, "Lenovo LA1 AI Çip Destekli Dinamik Fan Kontrolü",
        90, 112, 82
    ),
    (
        "laptop_lenovo_loq_16aph8_7840hs_4060",
        "Lenovo LOQ 16APH8 16 inç WUXGA 144Hz Ryzen 7 7840HS 16GB RAM 512GB SSD RTX 4060 115W",
        "Lenovo", 39999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "115W TGP", "Var (Advanced Optimus + MUX)",
        "16.0 inç WUXGA (1920x1200) 144Hz IPS Mat Panel", 16.0, "1920x1200 (FHD+ 16:10)", 144, "IPS", "%100 sRGB, 350 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        80, 170, 2.60, "Büyük 80Wh Batarya + Çift Soğutma Fanı",
        86, 98, 72
    ),
]
