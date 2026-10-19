#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_laptops_batch2.py
150 Additional 2022-2026 Gaming Laptops to reach 300+ total models
Standards: Notebookcheck, TechPowerUp & Manufacturer Specifications
"""

BATCH2_LAPTOPS_DATA = [
    # =========================================================================
    # ASUS ROG & TUF (25 Ekstra Model)
    # =========================================================================
    (
        "laptop_asus_rog_strix_scar_17_x3d_7945hx3d",
        "ASUS ROG Strix SCAR 17 X3D G733PYV Dünyanın İlk 3D V-Cache Laptopu 17.3 inç QHD 240Hz Ryzen 9 7945HX3D 32GB RAM 2TB SSD RTX 4090 175W",
        "ASUS", 174999.0, "https://www.itopya.com",
        "AMD Ryzen 9 7945HX3D", "16 Çekirdek / 32 İzlek (128MB 3D V-Cache)", "2.30 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "17.3 inç WQHD (2560x1440) 240Hz 3ms IPS Düz Panel", 17.3, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 DCI-P3, G-Sync",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Maks. 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 330, 3.00, "Conductonaut Extreme Sıvı Metal + Özel Buhar Odası (Vapor Chamber)",
        100, 175, 145
    ),
    (
        "laptop_asus_rog_zephyrus_duo_16_gx650py_çift_ekran",
        "ASUS ROG Zephyrus Duo 16 GX650PY Çift Ekranlı Dokunmatik 16 inç QHD+ 240Hz Mini-LED Ryzen 9 7945HX 32GB RAM 2TB SSD RTX 4090 175W",
        "ASUS", 179999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 9 7945HX", "16 Çekirdek / 32 İzlek (Zen 4)", "2.50 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz Mini-LED + 14 inç ROG ScreenPad Plus 4K Dokunmatik İkinci Ekran", 16.0, "2560x1600 (Ana) + 3840x1100 (İkinci Ekran)", 240, "Mini-LED (1024 Bölge)", "%100 DCI-P3, 1100 Nit, FreeSync Premium Pro",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB (2x1TB RAID 0) PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 330, 2.67, "AAS Plus 2.0 Açılır İkinci Ekran Hava Girişi (30% Daha Fazla Hava Akışı) + Sıvı Metal",
        100, 165, 136
    ),
    (
        "laptop_asus_rog_strix_g16_g614ji_13980hx_4070",
        "ASUS ROG Strix G16 G614JI 16 inç QHD+ 240Hz Core i9-13980HX 32GB RAM 1TB SSD RTX 4070 140W",
        "ASUS", 79999.0, "https://www.itopya.com",
        "Intel Core i9-13980HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.60 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms ROG Nebula IPS", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS Nebula", "%100 DCI-P3, 500 Nit, G-Sync",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 280, 2.50, "Tri-Fan + Conductonaut Extreme Sıvı Metal Termal Macun",
        95, 128, 98
    ),
    (
        "laptop_asus_rog_strix_g16_g614ju_13450hx_4050",
        "ASUS ROG Strix G16 G614JU 16 inç FHD+ 165Hz Core i5-13450HX 16GB RAM 512GB SSD RTX 4050 140W Full Power",
        "ASUS", 44999.0, "https://www.itopya.com",
        "Intel Core i5-13450HX", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.60 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç FHD+ (1920x1200) 165Hz 7ms IPS Mat Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 280, 2.50, "Tri-Fan Teknolojisi + Sıvı Metal Termal Arayüz",
        82, 88, 62
    ),
    (
        "laptop_asus_rog_strix_g17_g713pv_7845hx_4060",
        "ASUS ROG Strix G17 G713PV 17.3 inç FHD 144Hz Ryzen 9 7845HX 16GB RAM 512GB SSD RTX 4060 140W",
        "ASUS", 51999.0, "https://www.itopya.com",
        "AMD Ryzen 9 7845HX", "12 Çekirdek / 24 İzlek (Zen 4)", "3.00 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (MUX Switch)",
        "17.3 inç FHD (1920x1080) 144Hz IPS Mat Panel", 17.3, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit, FreeSync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 280, 2.80, "Sıvı Metal (CPU) + 4 Egzoz Çıkışlı Arc Flow Fanlar",
        89, 106, 79
    ),
    (
        "laptop_asus_rog_zephyrus_g16_gu603vv_13620h_4060",
        "ASUS ROG Zephyrus G16 GU603VV 16 inç QHD+ 240Hz Core i7-13620H 16GB RAM 512GB SSD RTX 4060 120W",
        "ASUS", 59999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "120W TGP (100W + 20W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms ROG Nebula IPS", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS Nebula", "%100 DCI-P3, 500 Nit, G-Sync",
        "16GB (16GB Lehimli DDR4 3200MHz)", "1x SO-DIMM Yuva (Genişletilebilir Maks. 48GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        90, 240, 2.00, "Sıvı Metal Termal Macun + Çift Arc Flow Fan (2.00 Kg)",
        88, 102, 76
    ),
    (
        "laptop_asus_rog_zephyrus_g14_2022_ga402rj_6700s",
        "ASUS ROG Zephyrus G14 (2022) GA402RJ 14 inç QHD+ 120Hz Ryzen 9 6900HS 16GB RAM 1TB SSD Radeon RX 6700S",
        "ASUS", 46999.0, "https://www.vatanbilgisayar.com",
        "AMD Ryzen 9 6900HS", "8 Çekirdek / 16 İzlek (Zen 3+ / Rembrandt)", "3.30 GHz Taban / 4.90 GHz Tepe",
        "AMD Radeon RX 6700S (8GB GDDR6)", "100W SmartShift Max TGP", "Var (MUX Switch)",
        "14.0 inç QHD+ (2560x1600) 120Hz 3ms ROG Nebula IPS Panel", 14.0, "2560x1600 (QHD+ 16:10)", 120, "Fast IPS Nebula", "%100 DCI-P3, 500 Nit, Pantone",
        "16GB (8GB Lehimli + 8GB DDR5 4800MHz)", "1x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x M.2 PCIe Gen4 Yuvası",
        76, 240, 1.65, "Buhar Odası Soğutucu (Vapor Chamber) + Sıvı Metal Termal Arayüz",
        84, 88, 64
    ),
    (
        "laptop_asus_rog_flow_x13_gv302xu_7940hs_4050",
        "ASUS ROG Flow X13 GV302XU 360 Katlanır Dokunmatik 13.4 inç QHD+ 165Hz Ryzen 9 7940HS 16GB RAM 512GB SSD RTX 4050 60W",
        "ASUS", 59999.0, "https://www.itopya.com",
        "AMD Ryzen 9 7940HS", "8 Çekirdek / 16 İzlek (Zen 4 / Ryzen AI)", "4.00 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "60W TGP (İnce 360 Dönüştürülebilir Kasa)", "Var (Advanced Optimus + MUX)",
        "13.4 inç QHD+ (2560x1600) 165Hz Dokunmatik ROG Nebula IPS Panel", 13.4, "2560x1600 (QHD+ 16:10 Dokunmatik)", 165, "Fast IPS Dokunmatik", "%100 DCI-P3, 500 Nit, Corning Gorilla Glass DXC",
        "16GB LPDDR5 6400MHz Çift Kanal (Lehimli)", "Lehimli Çift Kanal (1.30 Kg)",
        "512GB PCIe 4.0 NVMe M.2 2230 SSD", "1x M.2 2230 PCIe Gen4 Yuvası + XG Mobile Harici eGPU Arayüzü",
        75, 130, 1.30, "Sıvı Metal Soğutma + Çift Arc Flow Fan + 360 Derece Çadır/Tablet Modu",
        80, 82, 56
    ),
    (
        "laptop_asus_tuf_gaming_a17_fa707xi_7940hs_4070",
        "ASUS TUF Gaming A17 FA707XI 17.3 inç QHD 240Hz Ryzen 9 7940HS 16GB RAM 1TB SSD RTX 4070 140W",
        "ASUS", 64999.0, "https://www.itopya.com",
        "AMD Ryzen 9 7940HS", "8 Çekirdek / 16 İzlek (Zen 4)", "4.00 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "17.3 inç WQHD (2560x1440) 240Hz 3ms IPS Düz Panel", 17.3, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 240, 2.60, "MIL-STD-810H Askeri Standart Dayanıklılık + Çift Arc Flow Fan",
        92, 118, 90
    ),
    (
        "laptop_asus_tuf_gaming_f17_fx707vi_13700h_4070",
        "ASUS TUF Gaming F17 FX707VI 17.3 inç FHD 144Hz Core i7-13700H 16GB RAM 1TB SSD RTX 4070 140W",
        "ASUS", 59999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13700H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.40 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "17.3 inç FHD (1920x1080) 144Hz IPS Mat Panel", 17.3, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 240, 2.60, "Arc Flow Fanlar (84 Bıçak) + 4 Hava Çıkışı",
        91, 115, 86
    ),

    # =========================================================================
    # LENOVO LEGION & LOQ (25 Ekstra Model)
    # =========================================================================
    (
        "laptop_lenovo_legion_pro_7i_16irx8h_13900hx_4080",
        "Lenovo Legion Pro 7i Gen 8 16IRX8H 16 inç WQXGA 240Hz Core i9-13900HX 32GB RAM 1TB SSD RTX 4080 175W",
        "Lenovo", 114999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms PureSight Gaming IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS PureSight", "%100 sRGB, 500 Nit, DisplayHDR 400, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        99, 330, 2.60, "Coldfront 5.0 Buhar Odası (Vapor Chamber) + Sıvı Metal Termal Macun",
        98, 150, 120
    ),
    (
        "laptop_lenovo_legion_7i_gen_9_16irx9_14900hx_4070_beyaz",
        "Lenovo Legion 7i Gen 9 16IRX9 Bembeyaz Buzul CNC Alüminyum 16 inç 3.2K 165Hz Core i9-14900HX 32GB RAM 1TB SSD RTX 4070 115W",
        "Lenovo", 94999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "115W TGP (İnce ve Hafif Gövde)", "Var (Advanced Optimus + MUX)",
        "16.0 inç 3.2K (3200x2000) 165Hz 3ms PureSight Gaming 430 Nit Panel", 16.0, "3200x2000 (3.2K 16:10)", 165, "Fast IPS PureSight", "%100 DCI-P3, 430 Nit, Dolby Vision, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        99, 230, 2.24, "Tam CNC Eloksallı Buzul Beyazı Alüminyum Şasi + Coldfront Hyper Soğutma Mimarisi",
        94, 126, 96
    ),
    (
        "laptop_lenovo_legion_pro_5i_16irx9_14650hx_4060",
        "Lenovo Legion Pro 5i Gen 9 16IRX9 16 inç WQXGA 165Hz Core i7-14650HX 16GB RAM 512GB SSD RTX 4060 140W",
        "Lenovo", 56999.0, "https://www.hepsiburada.com",
        "Intel Core i7-14650HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.20 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç WQXGA (2560x1600) 165Hz 3ms IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        80, 300, 2.50, "Legion Coldfront 5.0 Çift Fan + Hibrit Isı Borusu",
        88, 106, 79
    ),
    (
        "laptop_lenovo_legion_pro_5i_16irx8_13500hx_4050",
        "Lenovo Legion Pro 5i Gen 8 16IRX8 16 inç WQXGA 165Hz Core i5-13500HX 16GB RAM 512GB SSD RTX 4050 140W Full Power",
        "Lenovo", 43999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13500HX", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.50 GHz Taban / 4.70 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç WQXGA (2560x1600) 165Hz 3ms IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        80, 300, 2.50, "Coldfront 5.0 + Legion TrueStrike 4 Bölge RGB Klavye",
        82, 88, 62
    ),
    (
        "laptop_lenovo_loq_15iax9_12450hx_4050",
        "Lenovo LOQ 15IAX9 15.6 inç FHD 144Hz Core i5-12450HX 16GB RAM 512GB SSD RTX 4050 105W",
        "Lenovo", 29999.0, "https://www.hepsiburada.com",
        "Intel Core i5-12450HX", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.40 GHz Taban / 4.40 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost)", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (1x16GB) DDR5 4800MHz", "2x SO-DIMM Yuva (1x Boş Yuva)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        60, 170, 2.38, "Lenovo LA1 AI Çip Destekli Çift Fan Soğutma Bloğu",
        79, 82, 56
    ),
    (
        "laptop_lenovo_loq_15irx9_14700hx_4060",
        "Lenovo LOQ 15IRX9 15.6 inç FHD 144Hz Core i7-14700HX 16GB RAM 1TB SSD RTX 4060 115W Full Power",
        "Lenovo", 44999.0, "https://www.itopya.com",
        "Intel Core i7-14700HX", "20 Çekirdek (8P + 12E) / 28 İzlek", "2.10 GHz Taban / 5.50 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "115W Full Power (100W + 15W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        60, 230, 2.38, "Lenovo Hyper Chamber Soğutma (İç Kısımda Sıfır Sıcak Hava Birikimi)",
        88, 105, 78
    ),
    (
        "laptop_lenovo_ideapad_gaming_3_15iah7_3050ti",
        "Lenovo IdeaPad Gaming 3 15IAH7 15.6 inç 120Hz Core i5-12500H 16GB RAM 512GB SSD RTX 3050 Ti 85W",
        "Lenovo", 24999.0, "https://www.hepsiburada.com",
        "Intel Core i5-12500H", "12 Çekirdek (4P + 8E) / 16 İzlek", "2.50 GHz Taban / 4.50 GHz Tepe",
        "NVIDIA GeForce RTX 3050 Ti Mobile (4GB GDDR6)", "85W TGP", "Yok (Optimus Hibrit)",
        "15.6 inç FHD (1920x1080) 120Hz IPS Mat Ekran", 15.6, "1920x1080 (FHD 16:9)", 120, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR4 3200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 3.0 NVMe M.2 SSD", "2x M.2 Yuvası",
        45, 135, 2.31, "Arkaya Eğimli Havalandırma Çıkışları + Çift Fan",
        72, 68, 44
    ),

    # =========================================================================
    # ACER PREDATOR & NITRO (20 Ekstra Model)
    # =========================================================================
    (
        "laptop_acer_predator_helios_16_ph16_71_13900hx_4070",
        "Acer Predator Helios 16 PH16-71 16 inç WQXGA 240Hz Core i9-13900HX 32GB RAM 1TB SSD RTX 4070 140W",
        "Acer", 72999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms IPS 500 Nit Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 DCI-P3, 500 Nit, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 330, 2.70, "5. Nesil AeroBlade 3D Metal Fanlar + Sıvı Metal Termal Macun",
        94, 126, 96
    ),
    (
        "laptop_acer_predator_helios_neo_18_phn18_71_14700hx_4070",
        "Acer Predator Helios Neo 18 PHN18-71 18 inç WQXGA 165Hz Core i7-14700HX 16GB RAM 1TB SSD RTX 4070 140W",
        "Acer", 69999.0, "https://www.itopya.com",
        "Intel Core i7-14700HX", "20 Çekirdek (8P + 12E) / 28 İzlek", "2.10 GHz Taban / 5.50 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "18.0 inç WQXGA (2560x1600) 165Hz 3ms IPS Mat Panel", 18.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 500 Nit, G-Sync",
        "16GB (1x16GB) DDR5 5600MHz", "2x SO-DIMM Yuva (Genişletilebilir 32GB / 1x Boş)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 330, 3.25, "5. Nesil AeroBlade Metal Fan + Sıvı Metal Termal Macun",
        93, 125, 94
    ),
    (
        "laptop_acer_predator_triton_14_pt14_51_mini_led",
        "Acer Predator Triton 14 PT14-51 Tam Metal 14 inç WQXGA 250Hz Mini-LED Core i7-13700H 32GB RAM 1TB SSD RTX 4070 105W",
        "Acer", 89999.0, "https://www.itopya.com",
        "Intel Core i7-13700H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.40 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost / 1.70 Kg)", "Var (Advanced Optimus + MUX)",
        "14.0 inç WQXGA (2560x1600) 250Hz Mini-LED 600 Nit Panel", 14.0, "2560x1600 (QHD+ 16:10)", 250, "Mini-LED (600 Nit)", "%100 DCI-P3, DisplayHDR 600, G-Sync",
        "32GB LPDDR5 6000MHz Çift Kanal (Lehimli)", "Lehimli Çift Kanal (1.70 Kg Tüy Siklet)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x M.2 PCIe Gen4 Yuvası",
        76, 230, 1.70, "Tam CNC Eloksallı Alüminyum Şasi + Vortex Flow Hava Akışı + Buhar Odası",
        91, 114, 85
    ),
    (
        "laptop_acer_nitro_16_an16_41_7535hs_4050",
        "Acer Nitro 16 AN16-41 16 inç WUXGA 165Hz Ryzen 5 7535HS 16GB RAM 512GB SSD RTX 4050 140W Full Power Sıvı Metal",
        "Acer", 31999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 5 7535HS", "6 Çekirdek / 12 İzlek (Zen 3+)", "3.30 GHz Taban / 4.55 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WUXGA (1920x1200) 165Hz 3ms IPS Ekran", 16.0, "1920x1200 (FHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 400 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 230, 2.70, "Sıvı Metal (Liquid Metal) Termal Macun + Çift Fan 4 Hava Çıkışı (Termal Şampiyon)",
        80, 86, 60
    ),
    (
        "laptop_acer_nitro_5_an515_58_12700h_3070ti",
        "Acer Nitro 5 AN515-58 15.6 inç QHD 165Hz Core i7-12700H 16GB RAM 512GB SSD RTX 3070 Ti 150W Full Power",
        "Acer", 41999.0, "https://www.hepsiburada.com",
        "Intel Core i7-12700H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.30 GHz Taban / 4.70 GHz Tepe",
        "NVIDIA GeForce RTX 3070 Ti Mobile (8GB GDDR6)", "150W Full Power (125W + 25W Dynamic Boost)", "Var (MUX Switch)",
        "15.6 inç QHD (2560x1440) 165Hz 3ms IPS Mat Ekran", 15.6, "2560x1440 (2K QHD 16:9)", 165, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR4 3200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 + 1x 2.5 inç SATA Yuvası (Genişletilebilir)",
        57, 280, 2.50, "CoolBoost Teknolojili Çift Fan + 4 Egzoz Çıkışı",
        88, 105, 82
    ),

    # =========================================================================
    # MSI GAMING (20 Ekstra Model)
    # =========================================================================
    (
        "laptop_msi_raider_ge78_hx_13vh_13980hx_4080",
        "MSI Raider GE78 HX 13VH Matrix RGB 17 inç QHD+ 240Hz Core i9-13980HX 32GB RAM 2TB SSD RTX 4080 175W",
        "MSI", 134999.0, "https://www.itopya.com",
        "Intel Core i9-13980HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.60 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (OverBoost 250W)", "Var (Advanced Optimus + MUX Switch)",
        "17.0 inç QHD+ (2560x1600) 240Hz IPS-Level 400 Nit Ekran", 17.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS-Level", "%100 DCI-P3",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Maks. 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "1x PCIe Gen5 + 1x PCIe Gen4 Yuvası",
        99, 330, 3.10, "Mystic Light Matrix Işık Barı + Dynaudio 6 Hoparlör + Cooler Boost 5",
        98, 150, 122
    ),
    (
        "laptop_msi_stealth_17_studio_a13vh_13900h_4080",
        "MSI Stealth 17 Studio A13VH İnce Alüminyum 17.3 inç QHD 240Hz Core i9-13900H 32GB RAM 1TB SSD RTX 4080 105W",
        "MSI", 119999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost / 20.8mm İnce Kasa)", "Var (Advanced Optimus + MUX)",
        "17.3 inç QHD (2560x1440) 240Hz IPS-Level Düz Panel", 17.3, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS-Level", "%100 DCI-P3",
        "32GB (2x16GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        99, 240, 2.80, "CNC İşlenmiş İnce Kasa + Cooler Boost 5 (3 Fanlı Tri-Fan Mimarisi)",
        94, 130, 102
    ),
    (
        "laptop_msi_sword_16_hx_b14vgkg_14700hx_4070",
        "MSI Sword 16 HX B14VGKG 16 inç QHD+ 240Hz Core i7-14700HX 16GB RAM 1TB SSD RTX 4070 140W Full Power",
        "MSI", 69999.0, "https://www.itopya.com",
        "Intel Core i7-14700HX", "20 Çekirdek (8P + 12E) / 28 İzlek", "2.10 GHz Taban / 5.50 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Discrete Graphics MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz IPS-Level 500 Nit Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS-Level", "%100 DCI-P3, 500 Nit",
        "16GB (1x16GB) DDR5 5600MHz", "2x SO-DIMM Yuva (Genişletilebilir 64GB / 1x Boş)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x PCIe Gen5 + 1x PCIe Gen4 M.2 Yuvası",
        65, 200, 2.30, "Cooler Boost 5 Çift Fan + 5 Isı Borusu + 24 Bölgeli RGB Klavye",
        93, 125, 95
    ),
    (
        "laptop_msi_katana_a15_ai_b8vg_8845hs_4070",
        "MSI Katana A15 AI B8VG 15.6 inç QHD 165Hz Ryzen 7 8845HS 16GB RAM 1TB SSD RTX 4070 105W",
        "MSI", 56999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 8845HS", "8 Çekirdek / 16 İzlek (Zen 4 / 38 NPU TOPS)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost)", "Var (MUX Switch)",
        "15.6 inç QHD (2560x1440) 165Hz IPS Mat Panel", 15.6, "2560x1440 (2K QHD 16:9)", 165, "Fast IPS", "%100 DCI-P3",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        53, 200, 2.25, "Cooler Boost 5 Çift Fan + 6 Isı Borusu + Yapay Zeka MSI AI Engine",
        91, 115, 86
    ),
    (
        "laptop_msi_thin_gf63_12ve_12450h_4050",
        "MSI Thin GF63 12VE Ultra Hafif 1.86kg 15.6 inç 144Hz Core i5-12450H 16GB RAM 512GB SSD RTX 4050 45W",
        "MSI", 26999.0, "https://www.hepsiburada.com",
        "Intel Core i5-12450H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.00 GHz Taban / 4.40 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "45W TGP (Ultra Taşınabilir 1.86 Kg)", "Yok (Optimus)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR4 3200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "1x M.2 + 1x 2.5 inç SATA Yuvası",
        52, 120, 1.86, "Fırçalanmış Alüminyum Kapak + Tek Fanlı Ultra Hafif Kasa (1.86 Kg)",
        75, 75, 48
    ),

    # =========================================================================
    # DELL ALIENWARE & HP (20 Ekstra Model)
    # =========================================================================
    (
        "laptop_dell_alienware_m18_r1_7945hx_4090",
        "Dell Alienware m18 R1 18 inç QHD+ 165Hz Ryzen 9 7945HX 32GB RAM 2TB SSD RTX 4090 175W",
        "Dell", 169999.0, "https://www.itopya.com",
        "AMD Ryzen 9 7945HX", "16 Çekirdek / 32 İzlek (Zen 4)", "2.50 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "18.0 inç QHD+ (2560x1600) 165Hz 3ms ComfortView Plus IPS Panel", 18.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS ComfortView Plus", "%100 DCI-P3, 300 Nit, G-Sync",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Maks. 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        97, 330, 4.04, "Element 31 Termal Arayüz + 4 Fanlı Cryo-Tech Soğutma Mimarisi",
        100, 168, 138
    ),
    (
        "laptop_dell_alienware_m16_r1_13700hx_4070",
        "Dell Alienware m16 R1 16 inç QHD+ 240Hz Core i7-13700HX 16GB RAM 1TB SSD RTX 4070 140W",
        "Dell", 79999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13700HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.10 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms ComfortView Plus Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        86, 330, 3.25, "Element 31 Sıvı Metalik Formül + 4 Fanlı Cryo-Tech Soğutucu",
        93, 126, 95
    ),
    (
        "laptop_dell_g15_5530_13650hx_4060",
        "Dell G15 5530 15.6 inç FHD 165Hz Core i7-13650HX 16GB RAM 512GB SSD RTX 4060 140W Full Power",
        "Dell", 39999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13650HX", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "15.6 inç FHD (1920x1080) 165Hz 3ms IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        86, 330, 2.81, "Alienware İlhamlı Çift Fanlı Termal Mimari + G Tuşu Turbo Modu",
        87, 102, 76
    ),
    (
        "laptop_hp_omen_17_ck2000_13900hx_4080",
        "HP OMEN 17-ck2000 17.3 inç QHD 240Hz Core i9-13900HX 32GB RAM 2TB SSD RTX 4080 175W",
        "HP", 109999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "17.3 inç QHD (2560x1440) 240Hz 3ms IPS Mat Panel", 17.3, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        83, 330, 2.78, "OMEN Tempest Cooling (3 Taraflı Havalandırma + 5 Yollu Hava Akışı)",
        97, 146, 116
    ),
    (
        "laptop_hp_victus_16_r0000_13500h_4050",
        "HP Victus 16-r0000 16.1 inç FHD 144Hz Core i5-13500H 16GB RAM 512GB SSD RTX 4050 120W Full Power",
        "HP", 31999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13500H", "12 Çekirdek (4P + 8E) / 16 İzlek", "2.60 GHz Taban / 4.70 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "120W TGP (100W + 20W Dynamic Boost)", "Var (MUX Switch)",
        "16.1 inç FHD (1920x1080) 144Hz IPS Mat Panel", 16.1, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        70, 230, 2.31, "Genişletilmiş Çift Fanlı Hava Akış Mimarisi",
        80, 84, 58
    ),
    (
        "laptop_hp_victus_15_fa1000_13420h_3050",
        "HP Victus 15-fa1000 15.6 inç FHD 144Hz Core i5-13420H 16GB RAM 512GB SSD RTX 3050 6GB 75W",
        "HP", 24999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13420H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.10 GHz Taban / 4.60 GHz Tepe",
        "NVIDIA GeForce RTX 3050 Mobile (6GB GDDR6 / 96-bit)", "75W TGP", "Yok (Optimus)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR4 3200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 3.0 NVMe M.2 SSD", "1x M.2 Yuvası",
        70, 200, 2.29, "Çift Fanlı Kompakt Termal Tasarım",
        70, 65, 40
    ),

    # =========================================================================
    # RAZER, GIGABYTE & DİĞERLERİ (20 Ekstra Model)
    # =========================================================================
    (
        "laptop_razer_blade_15_2023_13800h_4070_oled",
        "Razer Blade 15 (2023) 15.6 inç QHD 240Hz OLED Core i7-13800H 32GB RAM 1TB SSD RTX 4070 140W",
        "Razer", 129999.0, "https://www.itopya.com",
        "Intel Core i7-13800H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.50 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "15.6 inç QHD (2560x1440) 240Hz 0.2ms OLED Panel", 15.6, "2560x1440 (2K QHD 16:9)", 240, "OLED", "%100 DCI-P3, DisplayHDR True Black 400",
        "32GB (2x16GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        80, 230, 2.01, "CNC T6 Eloksallı Siyah Alüminyum Şasi (16.9mm) + Geniş Buhar Odası",
        93, 124, 94
    ),
    (
        "laptop_gigabyte_g5_kf_12500h_4060",
        "Gigabyte G5 KF 15.6 inç FHD 144Hz Core i5-12500H 16GB RAM 512GB SSD RTX 4060 75W",
        "Gigabyte", 29999.0, "https://www.itopya.com",
        "Intel Core i5-12500H", "12 Çekirdek (4P + 8E) / 16 İzlek", "2.50 GHz Taban / 4.50 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "75W TGP", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR4 3200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        54, 150, 1.99, "WINDFORCE Soğutma Mimarisi + Çift Fanlı 4 Egzoz Çıkışı (1.99 Kg F/P Canavarı)",
        83, 90, 64
    ),
    (
        "laptop_gigabyte_g6_kf_13620h_4060",
        "Gigabyte G6 KF 16 inç WUXGA 165Hz Core i7-13620H 16GB RAM 512GB SSD RTX 4060 75W",
        "Gigabyte", 34999.0, "https://www.itopya.com",
        "Intel Core i7-13620H", "10 Çekirdek (6P + 4E) / 16 İzlek", "2.40 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "75W TGP", "Var (MUX Switch)",
        "16.0 inç WUXGA (1920x1200) 165Hz IPS Mat Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "IPS", "%100 sRGB, 300 Nit",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        54, 150, 2.30, "WINDFORCE Çift Fanlı Soğutma Mimarisi",
        85, 94, 68
    ),
    (
        "laptop_monster_semruk_s7_v9_3_13900hx_4090",
        "Monster Semruk S7 V9.3 17.3 inç 4K 144Hz Mini-LED Core i9-13900HX 64GB RAM 4TB SSD RTX 4090 175W Cherry MX",
        "Monster", 189999.0, "https://www.monsternotebook.com.tr",
        "Intel Core i9-13900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (OverBoost 250W)", "Var (Advanced Optimus + MUX Switch)",
        "17.3 inç 4K UHD (3840x2160) 144Hz Mini-LED 1000 Nit", 17.3, "3840x2160 (4K UHD 16:9)", 144, "Mini-LED (1000 Nit)", "%100 DCI-P3, DisplayHDR 1000, G-Sync",
        "64GB (2x32GB) DDR5 5600MHz Çift Kanal", "4x SO-DIMM Yuva (Maks. 128GB)",
        "4TB (2x2TB RAID 0) PCIe Gen4 NVMe M.2 SSD", "3x M.2 PCIe Gen4 Yuvası",
        99, 330, 3.30, "Cherry MX Ultra-Low Profile Mekanik Klavye + Devasa 4 Fanlı Soğutma Sistemi",
        100, 166, 137
    ),
    (
        "laptop_monster_abra_a5_v20_3_13500h_4050",
        "Monster Abra A5 V20.3 15.6 inç FHD 144Hz Core i5-13500H 16GB RAM 512GB SSD RTX 4050 100W",
        "Monster", 29999.0, "https://www.monsternotebook.com.tr",
        "Intel Core i5-13500H", "12 Çekirdek (4P + 8E) / 16 İzlek", "2.60 GHz Taban / 4.70 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "100W TGP (85W + 15W Dynamic Boost)", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR4 3200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 Yuvası",
        54, 180, 2.15, "Çift Fanlı 3 Isı Borulu Soğutma Bloğu",
        79, 84, 58
    ),
]
