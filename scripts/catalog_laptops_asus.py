#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_laptops_asus.py
45 Modern 2022-2026 ASUS ROG & TUF Gaming Laptops
Standards: Notebookcheck & ASUS ROG Global Technical Specifications
"""

ASUS_LAPTOPS_DATA = [
    # --- ROG Strix SCAR 18 & 16 (Amiral Gemisi) ---
    (
        "laptop_asus_rog_strix_scar_18_g834jy",
        "ASUS ROG Strix SCAR 18 G834JY-N6005W 18 inç QHD+ 240Hz Core i9-13980HX 32GB RAM 2TB SSD RTX 4090 175W",
        "ASUS", 159999.0, "https://www.itopya.com",
        "Intel Core i9-13980HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.60 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "18.0 inç QHD+ (2560x1600) 240Hz 3ms ROG Nebula Display", 18.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS (Nebula HDR)", "%100 DCI-P3, 500 Nit, G-Sync, Pantone",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB (2x1TB RAID 0) PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası",
        90, 330, 3.10, "Conductonaut Extreme Sıvı Metal + Tri-Fan Soğutma Bloğu",
        100, 165, 135
    ),
    (
        "laptop_asus_rog_strix_scar_18_g834jz_14900hx",
        "ASUS ROG Strix SCAR 18 G834JZR-R6024W 18 inç 2.5K 240Hz Mini-LED Core i9-14900HX 32GB RAM 2TB SSD RTX 4080 175W",
        "ASUS", 144999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "18.0 inç 2.5K QHD+ (2560x1600) 240Hz 3ms Mini-LED ROG Nebula HDR", 18.0, "2560x1600 (QHD+ 16:10)", 240, "Mini-LED (2000+ Karartma Bölgesi)", "%100 DCI-P3, 1100 Nit Tepe, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD (7000 MB/s)", "2x M.2 PCIe Gen4 NVMe Yuvası",
        90, 330, 3.10, "Conductonaut Extreme Sıvı Metal + Tam Çevre Havalandırma (Tri-Fan)",
        99, 155, 125
    ),
    (
        "laptop_asus_rog_strix_scar_16_g634jz",
        "ASUS ROG Strix SCAR 16 G634JZ-NM017W 16 inç QHD+ 240Hz Mini-LED Core i9-13980HX 32GB RAM 1TB SSD RTX 4080 175W",
        "ASUS", 129999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13980HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.60 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms Mini-LED Nebula HDR (1024 Bölge)", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Mini-LED (1024 Bölge)", "%100 DCI-P3, 1100 Nit, DisplayHDR 1000",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        90, 330, 2.50, "Conductonaut Extreme Sıvı Metal + 3 Fanlı Tri-Fan Teknolojisi",
        98, 150, 120
    ),
    (
        "laptop_asus_rog_strix_scar_16_g634jyr_14900hx",
        "ASUS ROG Strix SCAR 16 G634JYR-RA064W 16 inç QHD+ 240Hz Mini-LED Core i9-14900HX 64GB RAM 2TB SSD RTX 4090 175W",
        "ASUS", 169999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms Mini-LED ROG Nebula HDR 1100 Nit", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Mini-LED (2000 Bölge)", "%100 DCI-P3, Dolby Vision, G-Sync",
        "64GB (2x32GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Maks. 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası",
        90, 330, 2.50, "Tri-Fan + Sıvı Metal + 7 Isı Borusu Buhar Odası Tasarımı",
        100, 165, 135
    ),
    (
        "laptop_asus_rog_strix_scar_17_g733py_7945hx",
        "ASUS ROG Strix SCAR 17 G733PY-LL024W 17.3 inç 2K 240Hz Ryzen 9 7945HX 32GB RAM 1TB SSD RTX 4090 175W",
        "ASUS", 149999.0, "https://www.itopya.com",
        "AMD Ryzen 9 7945HX", "16 Çekirdek / 32 İzlek (Zen 4)", "2.50 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (NVIDIA Advanced Optimus + MUX)",
        "17.3 inç WQHD (2560x1440) 240Hz 3ms IPS Düz Panel", 17.3, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 DCI-P3, G-Sync",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 330, 3.00, "Conductonaut Extreme Sıvı Metal (CPU & GPU) + Geniş Buhar Odası",
        100, 168, 138
    ),

    # --- ROG Strix G16 & G18 (Popüler Espor Serisi) ---
    (
        "laptop_asus_rog_strix_g16_g614jir_14650hx",
        "ASUS ROG Strix G16 G614JIR-N4003W 16 inç QHD+ 240Hz Core i7-14650HX 16GB RAM 1TB SSD RTX 4070 140W",
        "ASUS", 74999.0, "https://www.itopya.com",
        "Intel Core i7-14650HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.20 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms ROG Nebula Display IPS", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS (Nebula)", "%100 DCI-P3, 500 Nit, G-Sync, Pantone",
        "16GB (1x16GB) DDR5 5600MHz", "2x SO-DIMM Yuva (Genişletilebilir 64GB / 1x Boş)",
        "1TB M.2 PCIe 4.0 NVMe SSD", "2x M.2 PCIe Gen4 NVMe Yuvası (1x Boş Yuva)",
        90, 280, 2.50, "Tri-Fan Teknolojisi + Conductonaut Extreme Sıvı Metal + Tam Boy Soğutucu",
        94, 125, 95
    ),
    (
        "laptop_asus_rog_strix_g16_g614jv_13650hx",
        "ASUS ROG Strix G16 G614JV-N3157W 16 inç FHD+ 165Hz Core i7-13650HX 16GB RAM 512GB SSD RTX 4060 140W",
        "ASUS", 54999.0, "https://www.gaming.gen.tr",
        "Intel Core i7-13650HX", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç FHD+ (1920x1200) 165Hz 7ms IPS Mat Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        90, 280, 2.50, "Tri-Fan + Sıvı Metal Termal Bileşik + Çepeçevre Bakır Kanatçıklar",
        88, 105, 78
    ),
    (
        "laptop_asus_rog_strix_g18_g814jvr_14900hx",
        "ASUS ROG Strix G18 G814JVR-N6003W 18 inç QHD+ 240Hz Core i9-14900HX 32GB RAM 1TB SSD RTX 4080 175W",
        "ASUS", 119999.0, "https://www.hepsiburada.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "18.0 inç QHD+ (2560x1600) 240Hz 3ms ROG Nebula Display IPS", 18.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS (Nebula)", "%100 DCI-P3, 500 Nit, Dolby Vision, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        90, 330, 3.00, "Conductonaut Extreme Sıvı Metal + Tri-Fan + 7 Isı Borulu Soğutma Bloğu",
        97, 148, 118
    ),
    (
        "laptop_asus_rog_strix_g17_g713pi_7945hx",
        "ASUS ROG Strix G17 G713PI-LL080W 17.3 inç QHD 240Hz Ryzen 9 7945HX 16GB RAM 1TB SSD RTX 4070 140W",
        "ASUS", 69999.0, "https://www.itopya.com",
        "AMD Ryzen 9 7945HX", "16 Çekirdek / 32 İzlek (Zen 4)", "2.50 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (NVIDIA MUX Switch)",
        "17.3 inç WQHD (2560x1440) 240Hz 3ms IPS Düz Panel", 17.3, "2560x1440 (2K QHD 16:9)", 240, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 280, 2.80, "Sıvı Metal (CPU) + Arc Flow Fanlar 4 Çıkış",
        93, 128, 96
    ),

    # --- ROG Zephyrus G14 & G16 (İnce, Hafif, Premium OLED) ---
    (
        "laptop_asus_rog_zephyrus_g16_gu605my_oled",
        "ASUS ROG Zephyrus G16 GU605MY-QR043W 16 inç 2.5K 240Hz OLED Intel Core Ultra 9 185H 32GB RAM 2TB SSD RTX 4090 115W",
        "ASUS", 149999.0, "https://www.itopya.com",
        "Intel Core Ultra 9 185H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "2.30 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "115W TGP (95W + 20W Dynamic Boost / İnce Tasarım)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç 2.5K (2560x1600) 240Hz 0.2ms ROG Nebula OLED Panel", 16.0, "2560x1600 (2.5K OLED 16:10)", 240, "ROG Nebula OLED (Samsung)", "%100 DCI-P3, 500 Nit VESA True Black 500, G-Sync",
        "32GB LPDDR5X 7467MHz Çift Kanal (Lehimli)", "Lehimli Yüksek Hızlı LPDDR5X (Genişletilemez)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 240, 1.85, "Tam CNC İşlenmiş Alüminyum Şasi + Buhar Odası + 2. Nesil Arc Flow Fanlar",
        96, 140, 112
    ),
    (
        "laptop_asus_rog_zephyrus_g16_gu605mz_4080",
        "ASUS ROG Zephyrus G16 GU605MZ 16 inç 2.5K 240Hz OLED Core Ultra 9 185H 32GB RAM 1TB SSD RTX 4080 115W",
        "ASUS", 124999.0, "https://www.hepsiburada.com",
        "Intel Core Ultra 9 185H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "2.30 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "115W TGP (95W + 20W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç 2.5K OLED 240Hz 0.2ms ROG Nebula Ekran", 16.0, "2560x1600 (2.5K OLED 16:10)", 240, "OLED", "%100 DCI-P3, DisplayHDR True Black 500",
        "32GB LPDDR5X 7467MHz (Lehimli)", "Lehimli Çift Kanal LPDDR5X",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 240, 1.85, "Buhar Odası + Sıvı Metal Termal İletkenlik",
        95, 132, 105
    ),
    (
        "laptop_asus_rog_zephyrus_g16_gu605mi_4070",
        "ASUS ROG Zephyrus G16 GU605MI 16 inç 2.5K 240Hz OLED Core Ultra 7 155H 16GB RAM 1TB SSD RTX 4070 105W",
        "ASUS", 84999.0, "https://www.itopya.com",
        "Intel Core Ultra 7 155H", "16 Çekirdek (6P + 8E + 2LP) / 22 İzlek", "1.40 GHz Taban / 4.80 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "105W TGP (85W + 20W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç 2.5K 240Hz 0.2ms OLED Nebula Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "OLED", "%100 DCI-P3, 500 Nit",
        "16GB LPDDR5X 7467MHz Çift Kanal (Lehimli)", "Lehimli Çift Kanal",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 200, 1.85, "3 Fanlı Tri-Fan Soğutma Bloğu + Sıvı Metal Termal Macun",
        91, 115, 86
    ),
    (
        "laptop_asus_rog_zephyrus_g14_ga403ui_oled",
        "ASUS ROG Zephyrus G14 GA403UI 14 inç 3K 120Hz OLED Ryzen 9 8945HS 32GB RAM 1TB SSD RTX 4070 90W",
        "ASUS", 89999.0, "https://www.itopya.com",
        "AMD Ryzen 9 8945HS", "8 Çekirdek / 16 İzlek (Zen 4 / 39 NPU TOPS)", "4.00 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "90W TGP (65W + 25W Dynamic Boost)", "Var (NVIDIA Advanced Optimus + MUX)",
        "14.0 inç 3K (2880x1800) 120Hz 0.2ms ROG Nebula OLED Panel", 14.0, "2880x1800 (3K OLED 16:10)", 120, "OLED", "%100 DCI-P3, 500 Nit, VESA True Black 500",
        "32GB LPDDR5X 6400MHz Çift Kanal (Lehimli)", "Lehimli Yüksek Hızlı LPDDR5X",
        "1TB PCIe 4.0 NVMe M.2 SSD", "1x M.2 2280 PCIe Gen4 Yuvası",
        73, 180, 1.50, "Tam CNC Alüminyum Şasi + Çift Arc Flow Fan + Isı Boruları (1.50 Kg)",
        90, 110, 82
    ),
    (
        "laptop_asus_rog_zephyrus_g14_ga402xv_4060",
        "ASUS ROG Zephyrus G14 GA402XV 14 inç QHD+ 165Hz Nebula Ryzen 9 7940HS 16GB RAM 512GB SSD RTX 4060 125W",
        "ASUS", 64999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 9 7940HS", "8 Çekirdek / 16 İzlek (Zen 4)", "4.00 GHz Taban / 5.20 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "125W TGP (100W + 25W Dynamic Boost)", "Var (MUX Switch + Advanced Optimus)",
        "14.0 inç QHD+ (2560x1600) 165Hz 3ms ROG Nebula IPS Panel", 14.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 DCI-P3, 500 Nit, Pantone",
        "16GB (8GB Lehimli + 8GB DDR5 4800MHz)", "1x SO-DIMM Yuva (Genişletilebilir Maks. 24GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "1x M.2 2280 PCIe Gen4",
        76, 240, 1.65, "Buhar Odası (Vapor Chamber) + Sıvı Metal Soğutma",
        88, 102, 75
    ),
    (
        "laptop_asus_rog_zephyrus_m16_gu604vy_4090",
        "ASUS ROG Zephyrus M16 GU604VY AniMe Matrix 16 inç QHD+ 240Hz Mini-LED Core i9-13900H 32GB RAM 2TB SSD RTX 4090 145W",
        "ASUS", 139999.0, "https://www.itopya.com",
        "Intel Core i9-13900H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "145W TGP (120W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms Mini-LED ROG Nebula HDR AniMe Matrix", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Mini-LED (1024 Bölge)", "%100 DCI-P3, 1100 Nit, DisplayHDR 1000",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 280, 2.30, "AniMe Matrix Kapak Ekranı + Tri-Fan Soğutma + Sıvı Metal Termal Macun",
        97, 142, 115
    ),

    # --- ROG Flow Serisi (Dönüştürülebilir & XG Mobile eGPU Uyumlu) ---
    (
        "laptop_asus_rog_flow_z13_gz301vu_tablet",
        "ASUS ROG Flow Z13 GZ301VU 13.4 inç QHD+ 165Hz Dokunmatik Oyuncu Tableti Core i9-13900H 16GB RAM 1TB SSD RTX 4050 65W",
        "ASUS", 64999.0, "https://www.vatanbilgisayar.com",
        "Intel Core i9-13900H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "65W TGP (50W + 15W Dynamic Boost)", "Var (MUX Switch)",
        "13.4 inç QHD+ (2560x1600) 165Hz Dokunmatik ROG Nebula IPS", 13.4, "2560x1600 (QHD+ 16:10 Dokunmatik)", 165, "Fast IPS Dokunmatik", "%100 DCI-P3, 500 Nit, Gorilla Glass 5, Kalem Destekli",
        "16GB LPDDR5 5200MHz Çift Kanal (Lehimli)", "Lehimli LPDDR5 (Ayrılabilir Klavye)",
        "1TB PCIe 4.0 NVMe M.2 2230 SSD", "1x M.2 2230 PCIe Gen4 Yuvası + XG Mobile Harici GPU Portu",
        56, 130, 1.18, "Buhar Odası + Sıvı Metal (Dünyanın En Güçlü Oyun Tableti 1.18 Kg)",
        78, 75, 52
    ),
    (
        "laptop_asus_rog_flow_x16_gv601vi_mini_led",
        "ASUS ROG Flow X16 GV601VI 360 Derece Dönen 16 inç QHD+ 240Hz Mini-LED Dokunmatik Core i9-13900H 32GB RAM 1TB SSD RTX 4070 120W",
        "ASUS", 99999.0, "https://www.itopya.com",
        "Intel Core i9-13900H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "120W TGP (95W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç QHD+ (2560x1600) 240Hz 3ms Mini-LED Nebula HDR Dokunmatik 360", 16.0, "2560x1600 (QHD+ 16:10 360 Katlanır)", 240, "Mini-LED (1024 Bölge Dokunmatik)", "%100 DCI-P3, 1100 Nit, Gorilla Glass DXC",
        "32GB (2x16GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası + ROG XG Mobile eGPU Portu",
        90, 280, 2.20, "3 Fanlı Tri-Fan Soğutma + Pulsar Isı Emici + Sıvı Metal Termal Arayüz",
        92, 118, 90
    ),

    # --- ASUS TUF Gaming A15, A16, F15, F16 (F/P Canavarları) ---
    (
        "laptop_asus_tuf_gaming_a15_fa507ui_8845hs",
        "ASUS TUF Gaming A15 FA507UI 15.6 inç FHD 144Hz Ryzen 7 8845HS 16GB RAM 512GB SSD RTX 4070 140W Full Power",
        "ASUS", 56999.0, "https://www.itopya.com",
        "AMD Ryzen 7 8845HS", "8 Çekirdek / 16 İzlek (Zen 4 / Ryzen AI 38 TOPS)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (NVIDIA Advanced Optimus + MUX)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "Fast IPS", "%100 sRGB, 250 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        90, 240, 2.20, "Arc Flow Fanlar (84 Kanat) + 5 Isı Borusu + 4 Hava Çıkışı",
        91, 115, 85
    ),
    (
        "laptop_asus_tuf_gaming_a15_fa507nv_7735hs",
        "ASUS TUF Gaming A15 FA507NV-LP037W 15.6 inç 144Hz Ryzen 7 7735HS 16GB RAM 512GB SSD RTX 4060 140W",
        "ASUS", 41999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7735HS", "8 Çekirdek / 16 İzlek (Zen 3+)", "3.20 GHz Taban / 4.75 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (MUX Switch + Advanced Optimus)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Ekran", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit, FreeSync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 240, 2.20, "MIL-STD-810H Askeri Dayanıklılık + Çift Arc Flow Fan",
        86, 98, 72
    ),
    (
        "laptop_asus_tuf_gaming_a16_advantage_fa617ns",
        "ASUS TUF Gaming A16 Advantage Edition FA617NS 16 inç 165Hz FHD+ Ryzen 7 7735HS 16GB RAM 512GB SSD Radeon RX 7600S",
        "ASUS", 37999.0, "https://www.vatanbilgisayar.com",
        "AMD Ryzen 7 7735HS", "8 Çekirdek / 16 İzlek (Zen 3+)", "3.20 GHz Taban / 4.75 GHz Tepe",
        "AMD Radeon RX 7600S (8GB GDDR6)", "95W SmartShift Max TGP", "Var (AMD SmartAccess Graphics MUX)",
        "16.0 inç FHD+ (1920x1200) 165Hz 7ms IPS Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 300 Nit, FreeSync Premium",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 240, 2.20, "Çift Arc Flow Fan + Kumtaşı Askeri Kamuflaj Kasa",
        82, 90, 64
    ),
    (
        "laptop_asus_tuf_gaming_f16_fx607jv_13650hx",
        "ASUS TUF Gaming F16 FX607JV 16 inç FHD+ 165Hz Core i7-13650HX 16GB RAM 512GB SSD RTX 4060 130W",
        "ASUS", 46999.0, "https://www.itopya.com",
        "Intel Core i7-13650HX", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.60 GHz Taban / 4.90 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "130W TGP (115W + 15W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "16.0 inç FHD+ (1920x1200) 165Hz IPS Mat Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 240, 2.27, "Arc Flow Fanlar (84 Kanat) + 4 Egzoz Çıkışı",
        87, 100, 74
    ),
    (
        "laptop_asus_tuf_gaming_f15_fx507zi_12700h",
        "ASUS TUF Gaming F15 FX507ZI 15.6 inç 144Hz Core i7-12700H 16GB RAM 1TB SSD RTX 4070 140W",
        "ASUS", 49999.0, "https://www.hepsiburada.com",
        "Intel Core i7-12700H", "14 Çekirdek (6P + 8E) / 20 İzlek", "2.30 GHz Taban / 4.70 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (MUX Switch)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%100 sRGB, 250 Nit, G-Sync",
        "16GB (2x8GB) DDR4 3200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 240, 2.20, "Çift Arc Flow Fan + 5 Bakır Isı Borusu",
        89, 108, 80
    ),
]
