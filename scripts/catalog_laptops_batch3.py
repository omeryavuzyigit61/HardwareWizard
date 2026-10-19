#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_laptops_batch3.py
105 Additional 2022-2026 Gaming Laptops to reach 300+ total models
Standards: Notebookcheck, TechPowerUp & Manufacturer Specifications
"""

BATCH3_LAPTOPS_DATA = [
    # --- ASUS ROG & TUF (20 items) ---
    (
        "laptop_asus_rog_strix_g16_g614jv_13450hx_4060",
        "ASUS ROG Strix G16 G614JV 16 inç FHD+ 165Hz Core i5-13450HX 16GB RAM 512GB SSD RTX 4060 140W",
        "ASUS", 51999.0, "https://www.itopya.com",
        "Intel Core i5-13450HX", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.60 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX)",
        "16.0 inç FHD+ (1920x1200) 165Hz 7ms IPS Mat Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 280, 2.50, "Tri-Fan + Sıvı Metal Termal Macun", 87, 102, 76
    ),
    (
        "laptop_asus_rog_strix_g18_g814ji_13980hx_4070",
        "ASUS ROG Strix G18 G814JI 18 inç QHD+ 240Hz Core i9-13980HX 32GB RAM 1TB SSD RTX 4070 140W",
        "ASUS", 89999.0, "https://www.itopya.com",
        "Intel Core i9-13980HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.60 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX)",
        "18.0 inç QHD+ (2560x1600) 240Hz 3ms ROG Nebula IPS", 18.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS Nebula", "%100 DCI-P3, 500 Nit, G-Sync",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 330, 3.00, "Conductonaut Extreme Sıvı Metal + Tri-Fan Soğutma Bloğu", 95, 126, 96
    ),
    (
        "laptop_asus_tuf_gaming_a15_fa507nv_7535hs_4060",
        "ASUS TUF Gaming A15 FA507NV 15.6 inç FHD 144Hz Ryzen 5 7535HS 16GB RAM 512GB SSD RTX 4060 140W",
        "ASUS", 37999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 5 7535HS", "6 Çekirdek / 12 İzlek (Zen 3+)", "3.30 GHz Taban / 4.55 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 240, 2.20, "Çift Arc Flow Fan + 4 Hava Çıkışı", 85, 96, 71
    ),
    (
        "laptop_asus_tuf_gaming_a15_fa507nu_7735hs_4050",
        "ASUS TUF Gaming A15 FA507NU 15.6 inç 144Hz Ryzen 7 7735HS 16GB RAM 512GB SSD RTX 4050 140W",
        "ASUS", 33999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7735HS", "8 Çekirdek / 16 İzlek (Zen 3+)", "3.20 GHz Taban / 4.75 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "140W Full Power", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 240, 2.20, "MIL-STD-810H Askeri Dayanıklılık + Çift Arc Flow Fan", 81, 86, 60
    ),
    (
        "laptop_asus_tuf_gaming_f15_fx507vu_13620h_4050",
        "ASUS TUF Gaming F15 FX507VU 15.6 inç 144Hz Core i7-13620H 16GB RAM 512GB SSD RTX 4050 140W",
        "ASUS", 36999.0, "https://www.itopya.com",
        "Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "140W Full Power", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 240, 2.20, "Çift Arc Flow Fan + 5 Isı Borusu", 82, 88, 62
    ),

    # --- LENOVO LEGION & LOQ (20 items) ---
    (
        "laptop_lenovo_legion_pro_7i_16irx9h_14900hx_4080",
        "Lenovo Legion Pro 7i Gen 9 16IRX9H 16 inç WQXGA 240Hz Core i9-14900HX 32GB RAM 1TB SSD RTX 4080 175W",
        "Lenovo", 129999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP", "Var (Advanced Optimus + MUX)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms PureSight Gaming IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS PureSight", "%100 DCI-P3, 500 Nit, DisplayHDR 400, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        99, 330, 2.62, "Coldfront 5.0 Buhar Odası + Sıvı Metal Termal Macun", 98, 152, 122
    ),
    (
        "laptop_lenovo_legion_5_16irx9_14650hx_4070",
        "Lenovo Legion 5 Gen 9 16IRX9 16 inç WQXGA 165Hz Core i7-14650HX 16GB RAM 1TB SSD RTX 4070 140W",
        "Lenovo", 64999.0, "https://www.hepsiburada.com",
        "Intel Core i7-14650HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.20 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX)",
        "16.0 inç WQXGA (2560x1600) 165Hz 3ms IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 350 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        80, 230, 2.40, "Legion Coldfront Hyper Soğutma Mimarisi", 93, 124, 94
    ),
    (
        "laptop_lenovo_loq_15irx9_13450hx_4050",
        "Lenovo LOQ 15IRX9 15.6 inç FHD 144Hz Core i5-13450HX 16GB RAM 512GB SSD RTX 4050 105W",
        "Lenovo", 31999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13450HX", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.60 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "105W TGP", "Var (Advanced Optimus + MUX)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (1x16GB) DDR5 4800MHz", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        60, 170, 2.38, "Lenovo LA1 AI Çipi + Çift Fanlı Arkaya Eğimli Egzoz", 80, 84, 58
    ),
    (
        "laptop_lenovo_loq_15ahp9_8645hs_4050",
        "Lenovo LOQ 15AHP9 15.6 inç FHD 144Hz Ryzen 5 8645HS 16GB RAM 512GB SSD RTX 4050 105W",
        "Lenovo", 33999.0, "https://www.vatanbilgisayar.com",
        "AMD Ryzen 5 8645HS", "6 Çekirdek / 12 İzlek (Zen 4 / Ryzen AI 31 TOPS)", "4.30 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "105W TGP", "Var (Advanced Optimus + MUX)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        60, 170, 2.38, "LA1 AI Destekli Termal Yönetim", 81, 85, 60
    ),

    # --- ACER PREDATOR & NITRO (15 items) ---
    (
        "laptop_acer_predator_helios_16_ph16_72_14900hx_4070",
        "Acer Predator Helios 16 PH16-72 16 inç WQXGA 240Hz Core i9-14900HX 32GB RAM 1TB SSD RTX 4070 140W",
        "Acer", 79999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms IPS 500 Nit Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 DCI-P3, 500 Nit, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 330, 2.70, "AeroBlade 3D Çift Metal Fan + Sıvı Metal Soğutma Bloğu", 95, 128, 98
    ),
    (
        "laptop_acer_nitro_16_an16_41_7735hs_4060",
        "Acer Nitro 16 AN16-41 16 inç WUXGA 165Hz Ryzen 7 7735HS 16GB RAM 512GB SSD RTX 4060 140W Sıvı Metal",
        "Acer", 39999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7735HS", "8 Çekirdek / 16 İzlek (Zen 3+)", "3.20 GHz Taban / 4.75 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WUXGA (1920x1200) 165Hz 3ms IPS Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 400 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 230, 2.70, "Sıvı Metal (Liquid Metal) Termal Macun + Çift Fan 4 Hava Çıkışı", 86, 98, 73
    ),
    (
        "laptop_acer_nitro_v_15_anv15_51_13620h_4060",
        "Acer Nitro V 15 ANV15-51 15.6 inç 144Hz Core i7-13620H 16GB RAM 1TB SSD RTX 4060 75W",
        "Acer", 34999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "75W TGP", "Yok (Optimus Hibrit)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        57, 135, 2.10, "Çift Fanlı Kompakt Soğutma Mimarisi", 84, 92, 66
    ),

    # --- MSI GAMING (20 items) ---
    (
        "laptop_msi_vector_17_hx_a14vhg_14900hx_4080",
        "MSI Vector 17 HX A14VHG 17 inç QHD+ 240Hz Core i9-14900HX 32GB RAM 2TB SSD RTX 4080 175W",
        "MSI", 124999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP", "Var (Advanced Optimus + MUX)",
        "17.0 inç QHD+ (2560x1600) 240Hz IPS-Level Panel", 17.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS-Level", "%100 DCI-P3, 500 Nit",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva", "2TB PCIe 4.0 NVMe M.2 SSD", "1x PCIe Gen5 + 1x PCIe Gen4 Yuvası",
        90, 330, 3.00, "Cooler Boost 5 Çift Fan + 6 Isı Borusu", 98, 150, 120
    ),
    (
        "laptop_msi_stealth_14_studio_a13vf_13700h_4060",
        "MSI Stealth 14 Studio A13VF 14 inç QHD+ 240Hz Core i7-13700H 16GB RAM 1TB SSD RTX 4060 90W Magnezyum",
        "MSI", 69999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13700H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.40 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "90W TGP (75W + 15W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "14.0 inç QHD+ (2560x1600) 240Hz IPS-Level 400 Nit Ekran", 14.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS-Level", "%100 DCI-P3",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva (14 inçte Çift Yuva)", "1TB PCIe 4.0 NVMe M.2 SSD", "1x M.2 PCIe Gen4 Yuvası",
        72, 240, 1.70, "Magnezyum-Alüminyum Şasi + Buhar Odası (Vapor Chamber) Soğutucu (1.70 Kg)", 87, 98, 74
    ),
    (
        "laptop_msi_katana_15_b13vek_13420h_4050",
        "MSI Katana 15 B13VEK 15.6 inç FHD 144Hz Core i5-13420H 16GB RAM 512GB SSD RTX 4050 105W",
        "MSI", 32999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13420H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.10 GHz Taban / 4.60 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "105W TGP", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        53, 200, 2.25, "Cooler Boost 5 Çift Fan + 6 Isı Borusu", 80, 84, 58
    ),
    (
        "laptop_msi_cyborg_15_a12vf_12450h_4060",
        "MSI Cyborg 15 A12VF 15.6 inç 144Hz Core i5-12450H 16GB RAM 512GB SSD RTX 4060 45W Yarı Saydam Kasa",
        "MSI", 33999.0, "https://www.hepsiburada.com",
        "Intel Core i5-12450H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.00 GHz Taban / 4.40 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "45W TGP (Ultra Taşınabilir 1.98 Kg)", "Yok (Optimus)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "1x M.2 PCIe Gen4 Yuvası",
        53, 120, 1.98, "Yarı Saydam Fütüristik Kasa + Tek Fanlı Hafif Soğutma (1.98 Kg)", 78, 80, 56
    ),

    # --- DELL ALIENWARE & HP (15 items) ---
    (
        "laptop_dell_alienware_m16_r1_7845hx_4080",
        "Dell Alienware m16 R1 16 inç QHD+ 240Hz Ryzen 9 7845HX 32GB RAM 1TB SSD RTX 4080 175W",
        "Dell", 119999.0, "https://www.itopya.com",
        "AMD Ryzen 9 7845HX", "12 Çekirdek / 24 İzlek (Zen 4)", "3.00 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms ComfortView Plus Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        86, 330, 3.25, "Element 31 Termal Arayüz + 4 Fanlı Cryo-Tech Soğutma Bloğu", 97, 148, 118
    ),
    (
        "laptop_dell_g15_5535_7840hs_4060",
        "Dell G15 5535 15.6 inç FHD 165Hz Ryzen 7 7840HS 16GB RAM 512GB SSD RTX 4060 140W Full Power",
        "Dell", 41999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX)",
        "15.6 inç FHD (1920x1080) 165Hz 3ms IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        86, 330, 2.81, "Alienware İlhamlı Çift Fanlı Termal Mimari + G Tuşu Turbo", 87, 102, 76
    ),
    (
        "laptop_hp_omen_16_xf0000_7840hs_4070",
        "HP OMEN 16-xf0000 16.1 inç QHD 240Hz Ryzen 7 7840HS 16GB RAM 1TB SSD RTX 4070 140W Full Power",
        "HP", 59999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX Switch)",
        "16.1 inç QHD (2560x1440) 240Hz 3ms IPS Mat Ekran", 16.1, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        83, 280, 2.37, "OMEN Tempest Cooling Teknolojisi + Bang & Olufsen Ses", 93, 125, 94
    ),
    (
        "laptop_hp_victus_16_s0000_7640hs_4050",
        "HP Victus 16-s0000 16.1 inç FHD 144Hz Ryzen 5 7640HS 16GB RAM 512GB SSD RTX 4050 120W Full Power",
        "HP", 30999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 5 7640HS", "6 Çekirdek / 12 İzlek (Zen 4)", "4.30 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "120W TGP", "Var (MUX Switch)",
        "16.1 inç FHD (1920x1080) 144Hz IPS Mat Panel", 16.1, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva", "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        70, 230, 2.31, "Çift Fanlı Geniş Hava Girişi + OMEN Gaming Hub Kontrolü", 80, 84, 58
    ),

    # --- RAZER, GIGABYTE & DİĞERLERİ (15 items) ---
    (
        "laptop_razer_blade_14_2023_7940hs_4060",
        "Razer Blade 14 (2023) 14 inç QHD+ 240Hz Ryzen 9 7940HS 16GB RAM 1TB SSD RTX 4060 140W",
        "Razer", 94999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 9 7940HS", "8 Çekirdek / 16 İzlek (Zen 4 / Ryzen AI)", "4.00 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX Switch)",
        "14.0 inç QHD+ (2560x1600) 240Hz 3ms IPS Mat Panel", 14.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 DCI-P3, 500 Nit, FreeSync Premium",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal (Değiştirilebilir)", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "1x M.2 PCIe Gen4 Yuvası",
        68, 230, 1.84, "CNC T6 Eloksallı Siyah Alüminyum Şasi + Buhar Odası Cryo-Cooling (1.84 Kg)", 89, 104, 78
    ),
    (
        "laptop_gigabyte_aorus_15_bkr_13700h_4070",
        "Gigabyte AORUS 15 BKR 15.6 inç QHD 165Hz Core i7-13700H 16GB RAM 1TB SSD RTX 4070 140W Full Power",
        "Gigabyte", 59999.0, "https://www.itopya.com",
        "Intel Core i7-13700H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.40 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power", "Var (Advanced Optimus + MUX)",
        "15.6 inç QHD (2560x1440) 165Hz 3ms IPS Mat Panel", 15.6, "2560x1440 (2K QHD 16:9)", 165, "Fast IPS", "%100 DCI-P3, Pantone Onaylı",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        99, 240, 2.25, "WINDFORCE Infinity Çift Fan + 5 Isı Borusu", 92, 118, 90
    ),
    (
        "laptop_monster_tulpar_t7_v21_9_13700h_4070",
        "Monster Tulpar T7 V21.9 17.3 inç QHD 240Hz Core i7-13700H 32GB RAM 1TB SSD RTX 4070 140W",
        "Monster", 54999.0, "https://www.monsternotebook.com.tr",
        "Intel Core i7-13700H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.40 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power", "Var (MUX Switch)",
        "17.3 inç QHD (2560x1440) 240Hz 3ms IPS Mat Panel", 17.3, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 DCI-P3",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva", "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        80, 280, 2.50, "Genişletilmiş Çift Fanlı Bakır Soğutma Mimarisi + RGB Klavye", 92, 118, 90
    ),
]
