#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_laptops_dell_hp.py
35 Modern 2022-2026 Dell Alienware & HP OMEN / Victus Gaming Laptops
Standards: Dell / Alienware & HP OMEN Technical Specifications
"""

DELL_HP_LAPTOPS_DATA = [
    # --- Dell Alienware (Amiral Gemisi & Lüks Espor) ---
    (
        "laptop_dell_alienware_m18_r2_14900hx_4090",
        "Dell Alienware m18 R2 18 inç QHD+ 165Hz Core i9-14900HX 64GB RAM 2TB SSD RTX 4090 175W Cherry MX",
        "Dell", 179999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "18.0 inç QHD+ (2560x1600) 165Hz 3ms ComfortView Plus IPS Panel", 18.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS ComfortView Plus", "%100 DCI-P3, 300 Nit, G-Sync",
        "64GB (2x32GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB (2x1TB RAID 0) PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası",
        97, 360, 4.04, "Element 31 Galyum-Silikon Termal Arayüz + 4 Fanlı Cryo-Tech Soğutma Mimarisi + Cherry MX Mekanik",
        100, 166, 137
    ),
    (
        "laptop_dell_alienware_m16_r2_ultra_9_4070",
        "Dell Alienware m16 R2 16 inç QHD+ 240Hz Intel Core Ultra 9 185H 32GB RAM 1TB SSD RTX 4070 140W Stealth Modu",
        "Dell", 99999.0, "https://www.hepsiburada.com",
        "Intel Core Ultra 9 185H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "2.30 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms ComfortView Plus IPS Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 240, 2.61, "Alienware Cryo-Tech Soğutma + F2 Tek Tuşla Stealth Modu (Sessiz Ofis Modu)",
        94, 126, 96
    ),
    (
        "laptop_dell_alienware_x16_r1_13900hk_4080",
        "Dell Alienware x16 R1 Ultra İnce Lüks 16 inç QHD+ 240Hz Core i9-13900HK 32GB RAM 1TB SSD RTX 4080 175W",
        "Dell", 134999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900HK", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (İnce Gövdede Tam Güç)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms IPS Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "32GB LPDDR5 6000MHz Çift Kanal (Lehimli)", "Lehimli Yüksek Hızlı LPDDR5",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 330, 2.72, "Magnezyum Alaşımlı Gövde + Arka Stadyum RGB Işık Halkası + Buhar Odası Cryo-Tech",
        97, 146, 116
    ),
    (
        "laptop_dell_alienware_x14_r2_13620h_4060",
        "Dell Alienware x14 R2 Dünyanın En İnce 14 inç Oyuncu Laptopu QHD+ 165Hz Core i7-13620H 16GB RAM 1TB SSD RTX 4060 85W",
        "Dell", 74999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "85W TGP (65W + 20W Dynamic Boost / 14.5mm Kalınlık)", "Var (Advanced Optimus + MUX)",
        "14.0 inç QHD+ (2560x1600) 165Hz 3ms IPS Ekran", 14.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "16GB LPDDR5 4800MHz (Lehimli)", "Lehimli Çift Kanal",
        "1TB PCIe 4.0 NVMe M.2 2230 SSD", "1x M.2 2230 PCIe Gen4 Yuvası",
        80, 130, 1.91, "Patentli Çift Eksenli Menteşe + GaN Type-C Şarj Adaptörü + Buhar Odası (14.5mm İncecik)",
        86, 94, 70
    ),

    # --- Dell G16 & G15 (F/P Canavarları) ---
    (
        "laptop_dell_g16_7630_13900hx_4070_cherry",
        "Dell G16 7630 16 inç QHD+ 240Hz Core i9-13900HX 32GB RAM 1TB SSD RTX 4070 140W Cherry MX Klavye",
        "Dell", 67999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms IPS Mat Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        86, 330, 2.87, "Alienware İlhamlı Buhar Odası Termal Tasarımı + Cherry MX Mekanik Klavye",
        93, 126, 95
    ),
    (
        "laptop_dell_g15_5530_13450hx_4050",
        "Dell G15 5530 15.6 inç 120Hz Core i5-13450HX 16GB RAM 512GB SSD RTX 4050 140W Full Power",
        "Dell", 31999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13450HX", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.60 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 120Hz IPS Panel", 15.6, "1920x1080 (FHD 16:9)", 120, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        56, 240, 2.81, "Alienware İlhamlı Termal Soğutma Bloğu + G Tuşu Oyun Güçlendirmesi",
        80, 86, 60
    ),

    # --- HP OMEN (Transcend & OMEN 16/17) ---
    (
        "laptop_hp_omen_transcend_16_u0000_oled",
        "HP OMEN Transcend 16 16 inç WQXGA 240Hz Mini-LED Core i9-13900HX 32GB RAM 1TB SSD RTX 4080 145W",
        "HP", 119999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "145W TGP (120W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms Mini-LED HDR 1000", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Mini-LED (1000 Nit Tepe)", "%100 DCI-P3, Eyesafe, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        97, 330, 2.17, "Magnezyum-Alüminyum Hafif Şasi (2.17 Kg) + OMEN Tempest Cooling",
        96, 142, 114
    ),
    (
        "laptop_hp_omen_transcend_14_fb0000_oled",
        "HP OMEN Transcend 14 14 inç 2.8K 120Hz OLED Intel Core Ultra 9 185H 32GB RAM 1TB SSD RTX 4070 80W",
        "HP", 84999.0, "https://www.hepsiburada.com",
        "Intel Core Ultra 9 185H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "2.30 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "80W TGP (65W + 15W Dynamic Boost / 1.63 Kg)", "Var (Advanced Optimus + MUX)",
        "14.0 inç 2.8K (2880x1800) 120Hz 0.2ms OLED Panel", 14.0, "2880x1800 (2.8K OLED 16:10)", 120, "OLED", "%100 DCI-P3, 500 Nit HDR True Black 500",
        "32GB LPDDR5X 7467MHz (Lehimli)", "Lehimli Yüksek Hızlı LPDDR5X",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x M.2 PCIe Gen4 Yuvası",
        71, 140, 1.63, "Intel Çift Kanallı Buhar Odası Termal Tasarımı (1.63 Kg Tüy Siklet)",
        89, 108, 80
    ),
    (
        "laptop_hp_omen_16_wf0000_13700hx_4070",
        "HP OMEN 16-wf0000 16.1 inç QHD 240Hz Core i7-13700HX 16GB RAM 1TB SSD RTX 4070 140W Full Power",
        "HP", 61999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13700HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.10 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.1 inç QHD (2560x1440) 240Hz 3ms IPS Mat Ekran", 16.1, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        83, 280, 2.37, "OMEN Tempest Cooling Teknolojisi + Bang & Olufsen Ses Sistemi",
        93, 125, 94
    ),

    # --- HP Victus 16 & Victus 15 (F/P Satış Liderleri) ---
    (
        "laptop_hp_victus_16_r1000_14700hx_4060",
        "HP Victus 16-r1000 16.1 inç FHD 144Hz Core i7-14700HX 16GB RAM 1TB SSD RTX 4060 120W",
        "HP", 43999.0, "https://www.hepsiburada.com",
        "Intel Core i7-14700HX", "20 Çekirdek (8P + 12E) / 28 İzlek", "2.10 GHz Taban / 5.50 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "120W TGP (100W + 20W Dynamic Boost)", "Var (MUX Switch)",
        "16.1 inç FHD (1920x1080) 144Hz IPS Mat Panel", 16.1, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 300 Nit",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        70, 230, 2.31, "Geliştirilmiş Çift Fanlı Hava Akışı Mimarisi",
        87, 102, 75
    ),
    (
        "laptop_hp_victus_16_s0000_7840hs_4060",
        "HP Victus 16-s0000 16.1 inç FHD 144Hz Ryzen 7 7840HS 16GB RAM 512GB SSD RTX 4060 120W",
        "HP", 38999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "120W TGP", "Var (MUX Switch)",
        "16.1 inç FHD (1920x1080) 144Hz IPS Mat Panel", 16.1, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 300 Nit",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        70, 230, 2.31, "OMEN Gaming Hub Akıllı Güç Yönetimi",
        86, 98, 73
    ),
]
