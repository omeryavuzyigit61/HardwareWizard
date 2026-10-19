#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_laptops_msi.py
40 Modern 2022-2026 MSI Gaming Laptops (Titan, Raider, Vector, Stealth, Katana, Cyborg)
Standards: MSI Global Technical Specifications & Notebookcheck Testing Labs
"""

MSI_LAPTOPS_DATA = [
    # --- Titan 18 HX & GT77 HX (Nihai Masaüstü Değişimi Canavarları) ---
    (
        "laptop_msi_titan_18_hx_a14vig_14900hx",
        "MSI Titan 18 HX A14VIG 18 inç 4K 120Hz Mini-LED Core i9-14900HX 128GB RAM 4TB SSD RTX 4090 175W Cherry MX",
        "MSI", 249999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (MSI OverBoost Ultra 270W Kombine Güç)", "Var (Discrete Graphics Mode MUX Switch)",
        "18.0 inç 4K UHD+ (3840x2400) 120Hz Mini-LED IPS-Level Panel", 18.0, "3840x2400 (4K+ 16:10)", 120, "Mini-LED (1000 Nit Tepe)", "%100 DCI-P3, VESA DisplayHDR 1000",
        "128GB (4x32GB) DDR5 5600MHz Dört Kanal", "4x SO-DIMM Yuva (Genişletilebilir Maks. 192GB)",
        "4TB (2x2TB PCIe Gen5 & Gen4) NVMe M.2 SSD", "1x PCIe Gen5 + 3x PCIe Gen4 M.2 Yuvası (Toplam 4 Yuva)",
        99, 400, 3.60, "Cherry MX Ultra-Low Profile Mekanik Klavye + Devasa Buhar Odası Soğutucu + 4 Fan Yuvası",
        100, 170, 142
    ),
    (
        "laptop_msi_titan_gt77_hx_13vi_13980hx",
        "MSI Titan GT77 HX 13VI 17.3 inç 4K 144Hz Mini-LED Core i9-13980HX 64GB RAM 2TB SSD RTX 4090 175W",
        "MSI", 199999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13980HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.60 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (OverBoost 250W)", "Var (MUX Switch)",
        "17.3 inç 4K UHD (3840x2160) 144Hz 3ms Mini-LED 1000 Nit", 17.3, "3840x2160 (4K UHD 16:9)", 144, "Mini-LED (1000 Bölge)", "%100 DCI-P3, DisplayHDR 1000",
        "64GB (2x32GB) DDR5 4800MHz", "4x SO-DIMM Yuva (Maks. 128GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "1x PCIe Gen5 + 2x PCIe Gen4 Yuvası",
        99, 330, 3.30, "Cherry MX Mekanik Switchler + Cooler Boost Titan 4 Fanlı Soğutma Mimarisi",
        100, 166, 138
    ),

    # --- Raider GE78 HX & GE68 HX (RGB Matrix Bar Amiral Gemileri) ---
    (
        "laptop_msi_raider_ge78_hx_14vig_14900hx",
        "MSI Raider GE78 HX 14VIG Matrix Işık Barlı 17 inç QHD+ 240Hz Core i9-14900HX 32GB RAM 2TB SSD RTX 4090 175W",
        "MSI", 169999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (MSI OverBoost 250W)", "Var (Advanced Optimus + MUX Switch)",
        "17.0 inç QHD+ (2560x1600) 240Hz IPS-Level Mat Panel", 17.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS-Level", "%100 DCI-P3, 400 Nit",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "1x PCIe Gen5 + 1x PCIe Gen4 Yuvası",
        99, 330, 3.10, "Mystic Light Matrix RGB Işık Çubuğu + Dynaudio 6 Hoparlörlü Ses Sistemi + Cooler Boost 5",
        99, 164, 135
    ),
    (
        "laptop_msi_raider_ge68_hx_14vgg_14900hx_4070",
        "MSI Raider GE68 HX 14VGG 16 inç QHD+ 240Hz OLED Core i9-14900HX 32GB RAM 1TB SSD RTX 4070 140W",
        "MSI", 99999.0, "https://www.hepsiburada.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz 0.2ms OLED Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "OLED", "%100 DCI-P3, 500 Nit, DisplayHDR True Black 500",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x PCIe Gen5 + 1x PCIe Gen4 M.2 Yuvası",
        99, 280, 2.70, "Cooler Boost 5 Çift Fan + 6 Isı Borusu + Matrix Işık Barı",
        94, 126, 96
    ),

    # --- Vector GP78 HX & GP68 HX (İş İstasyonu Gücünde Saf Espor) ---
    (
        "laptop_msi_vector_16_hx_a14vhg_14900hx_4080",
        "MSI Vector 16 HX A14VHG 16 inç QHD+ 240Hz Core i9-14900HX 32GB RAM 1TB SSD RTX 4080 175W",
        "MSI", 114999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz IPS-Level 500 Nit Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS-Level", "%100 DCI-P3, 500 Nit",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x PCIe Gen5 + 1x PCIe Gen4 M.2 Yuvası",
        90, 330, 2.70, "Cooler Boost 5 Çift Fan + 6 Isı Borusu + SteelSeries Tuş Başına RGB Klavye",
        97, 148, 118
    ),
    (
        "laptop_msi_vector_gp68_hx_13vh_13950hx_4080",
        "MSI Vector GP68 HX 13VH 16 inç FHD+ 144Hz Core i9-13950HX 16GB RAM 1TB SSD RTX 4080 175W",
        "MSI", 94999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13950HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.50 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP", "Var (MUX Switch)",
        "16.0 inç FHD+ (1920x1200) 144Hz IPS Mat Panel", 16.0, "1920x1200 (FHD+ 16:10)", 144, "Fast IPS", "%100 sRGB",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x PCIe Gen5 + 1x PCIe Gen4 Yuvası",
        90, 330, 2.70, "Cooler Boost 5 + Tam Performans TGP Canavarı",
        96, 145, 115
    ),

    # --- Stealth 16 AI Studio & 14 Studio (Magnezyum Şasi, Ultra İnce) ---
    (
        "laptop_msi_stealth_16_ai_studio_a1vgg_oled",
        "MSI Stealth 16 AI Studio A1VGG 16 inç UHD+ 120Hz OLED Intel Core Ultra 9 185H 32GB RAM 2TB SSD RTX 4070 105W",
        "MSI", 119999.0, "https://www.itopya.com",
        "Intel Core Ultra 9 185H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "2.30 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost / İnce Kasa)", "Var (Advanced Optimus + MUX)",
        "16.0 inç UHD+ (3840x2400) 120Hz 0.2ms OLED Panel", 16.0, "3840x2400 (4K+ OLED 16:10)", 120, "OLED", "%100 DCI-P3, VESA True Black 600",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        99, 240, 1.99, "Magnezyum-Alüminyum Alaşımlı Hafif Gövde (1.99 Kg) + Dynaudio 6 Hoparlör + Cooler Boost 5",
        92, 118, 90
    ),

    # --- Katana 15 / 17 & Pulse 16 AI & Cyborg 15 (Popüler F/P Serileri) ---
    (
        "laptop_msi_katana_15_b13vgk_13620h_4070",
        "MSI Katana 15 B13VGK 15.6 inç QHD 165Hz Core i7-13620H 16GB RAM 1TB SSD RTX 4070 105W",
        "MSI", 52999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost)", "Var (Discrete Graphics MUX Switch)",
        "15.6 inç QHD (2560x1440) 165Hz IPS Mat Panel", 15.6, "2560x1440 (2K QHD 16:9)", 165, "Fast IPS", "%100 DCI-P3",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        53, 200, 2.25, "Cooler Boost 5 Çift Fan + 6 Isı Borusu + 4 Bölge RGB Klavye",
        90, 112, 84
    ),
    (
        "laptop_msi_katana_17_b13vfk_13620h_4060",
        "MSI Katana 17 B13VFK 17.3 inç FHD 144Hz Core i7-13620H 16GB RAM 1TB SSD RTX 4060 105W",
        "MSI", 46999.0, "https://www.itopya.com",
        "Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "105W TGP", "Var (MUX Switch)",
        "17.3 inç FHD (1920x1080) 144Hz IPS Mat Panel", 17.3, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        53, 200, 2.60, "Cooler Boost 5 + 17 inç Geniş Ekran Deneyimi",
        86, 96, 72
    ),
    (
        "laptop_msi_pulse_16_ai_c1vgkg_14900hx",
        "MSI Pulse 16 AI C1VGKG 16 inç QHD+ 240Hz Intel Core Ultra 9 185H 32GB RAM 1TB SSD RTX 4070 140W",
        "MSI", 79999.0, "https://www.hepsiburada.com",
        "Intel Core Ultra 9 185H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "2.30 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz IPS-Level 500 Nit Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS-Level", "%100 DCI-P3, 500 Nit",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 240, 2.50, "Cooler Boost 5 Çift Fan + 5 Isı Borusu + 24 Bölgeli RGB Klavye",
        93, 124, 94
    ),
    (
        "laptop_msi_cyborg_15_a13vf_13420h_4060",
        "MSI Cyborg 15 A13VF Yarı Saydam Gövde 15.6 inç 144Hz Core i7-13620H 16GB RAM 512GB SSD RTX 4060 45W",
        "MSI", 36999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "45W TGP (İnce ve Hafif Siber Kasa)", "Yok (Optimus)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "1x M.2 PCIe Gen4 Yuvası",
        53, 120, 1.98, "Yarı Saydam Fütüristik Kasa + Tek Fanlı Hafif Soğutma (1.98 Kg)",
        79, 82, 58
    ),
]
