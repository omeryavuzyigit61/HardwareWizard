#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_laptops_acer.py
35 Modern 2022-2026 Acer Predator & Nitro Gaming Laptops
Standards: Acer Global Technical Specifications & Notebookcheck Testing Labs
"""

ACER_LAPTOPS_DATA = [
    # --- Predator Helios 18 & 16 (Amiral Gemisi Mini-LED) ---
    (
        "laptop_acer_predator_helios_18_ph18_72_14900hx",
        "Acer Predator Helios 18 PH18-72 18 inç WQXGA 250Hz Mini-LED Core i9-14900HX 32GB RAM 2TB SSD RTX 4090 175W",
        "Acer", 159999.0, "https://www.hepsiburada.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "18.0 inç WQXGA (2560x1600) 250Hz 3ms Mini-LED WQXGA Ekran", 18.0, "2560x1600 (QHD+ 16:10)", 250, "Mini-LED (1000 Nit Tepe)", "%100 DCI-P3, DisplayHDR 1000, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "2TB (2x1TB RAID 0) PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası",
        90, 330, 3.25, "5. Nesil AeroBlade 3D Metal Fanlar + Sıvı Metal Termal Macun + Vektör Isı Boruları",
        100, 166, 137
    ),
    (
        "laptop_acer_predator_helios_16_ph16_72_14900hx_4080",
        "Acer Predator Helios 16 PH16-72 16 inç WQXGA 240Hz Mini-LED Core i9-14900HX 32GB RAM 1TB SSD RTX 4080 175W",
        "Acer", 114999.0, "https://www.itopya.com",
        "Intel Core i9-14900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.80 GHz Tepe",
        "NVIDIA GeForce RTX 4080 Mobile (12GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms Mini-LED 1000 Nit Panel", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Mini-LED (1000 Nit)", "%100 DCI-P3, DisplayHDR 1000, G-Sync",
        "32GB (2x16GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 64GB)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        90, 330, 2.70, "AeroBlade 3D Çift Metal Fan + Sıvı Metal Soğutma Bloğu",
        98, 150, 120
    ),
    (
        "laptop_acer_predator_triton_17_x_ptx17_71",
        "Acer Predator Triton 17 X PTX17-71 Tam CNC Alüminyum 17 inç Mini-LED Core i9-13900HX 64GB RAM 2TB SSD RTX 4090 175W",
        "Acer", 169999.0, "https://www.hepsiburada.com",
        "Intel Core i9-13900HX", "24 Çekirdek (8P + 16E) / 32 İzlek", "2.20 GHz Taban / 5.40 GHz Tepe",
        "NVIDIA GeForce RTX 4090 Mobile (16GB GDDR6)", "175W Maximum TGP (150W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "17.0 inç WQXGA (2560x1600) 250Hz Mini-LED 1000 Nit Ekran", 17.0, "2560x1600 (QHD+ 16:10)", 250, "Mini-LED (1000 Nit)", "%100 DCI-P3, Eyesafe Sertifikası, G-Sync",
        "64GB (2x32GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "2TB (RAID 0) PCIe Gen4 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        99, 330, 3.00, "Tam CNC İşlenmiş Monoblok Alüminyum Şasi + Buhar Odası + 3 Fanlı Tri-Fan AeroBlade",
        100, 165, 136
    ),

    # --- Predator Helios Neo 16 & Neo 18 (F/P Canavarları) ---
    (
        "laptop_acer_predator_helios_neo_16_phn16_72_14700hx",
        "Acer Predator Helios Neo 16 PHN16-72 16 inç WQXGA 240Hz Core i7-14700HX 16GB RAM 1TB SSD RTX 4070 140W",
        "Acer", 62999.0, "https://www.hepsiburada.com",
        "Intel Core i7-14700HX", "20 Çekirdek (8P + 12E) / 28 İzlek", "2.10 GHz Taban / 5.50 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 240Hz 3ms IPS 500 Nit Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 240, "Fast IPS", "%100 sRGB, 500 Nit, G-Sync",
        "16GB (1x16GB) DDR5 5600MHz", "2x SO-DIMM Yuva (Genişletilebilir 32GB / 1x Boş Yuva)",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası (1x Boş)",
        90, 330, 2.60, "5. Nesil AeroBlade 3D Metal Fan + Sıvı Metal Termal Bileşik (CPU)",
        93, 126, 95
    ),
    (
        "laptop_acer_predator_helios_neo_16_phn16_71_13700hx",
        "Acer Predator Helios Neo 16 PHN16-71 16 inç WQXGA 165Hz Core i7-13700HX 16GB RAM 512GB SSD RTX 4060 140W",
        "Acer", 48999.0, "https://www.hepsiburada.com",
        "Intel Core i7-13700HX", "16 Çekirdek (8P + 8E) / 24 İzlek", "2.10 GHz Taban / 5.00 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 165Hz 3ms IPS Panel", 16.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 500 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 330, 2.60, "AeroBlade 3D Fan + Sıvı Metal Termal Arayüz",
        88, 106, 79
    ),

    # --- Acer Nitro 16 (Efsanevi Sıvı Metal Soğutmalı F/P) ---
    (
        "laptop_acer_nitro_16_an16_41_7840hs_4070",
        "Acer Nitro 16 AN16-41 16 inç WQXGA 165Hz Ryzen 7 7840HS 16GB RAM 512GB SSD RTX 4070 140W Full Power",
        "Acer", 53999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WQXGA (2560x1600) 165Hz 3ms IPS Ekran", 16.0, "2560x1600 (QHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 500 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 NVMe Yuvası (1x Boş Yuva)",
        90, 330, 2.70, "Çift Fan + Sıvı Metal (Liquid Metal) Termal Macun + 4 Çıkışlı Soğutma",
        92, 118, 88
    ),
    (
        "laptop_acer_nitro_16_an16_41_7840hs_4060",
        "Acer Nitro 16 AN16-41 16 inç WUXGA 165Hz Ryzen 7 7840HS 16GB RAM 512GB SSD RTX 4060 140W",
        "Acer", 42999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX Switch)",
        "16.0 inç WUXGA (1920x1200) 165Hz 3ms IPS Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "Fast IPS", "%100 sRGB, 400 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva (Genişletilebilir 32GB)",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş)",
        90, 230, 2.70, "Sıvı Metal Soğutma + Çift Fan 4 Hava Çıkışı (F/P Termal Şampiyonu)",
        87, 102, 76
    ),
    (
        "laptop_acer_nitro_16_an16_51_13500h_4050",
        "Acer Nitro 16 AN16-51 16 inç WUXGA 165Hz Core i5-13500H 16GB RAM 512GB SSD RTX 4050 140W",
        "Acer", 34999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13500H", "12 Çekirdek (4P + 8E) / 16 İzlek", "2.60 GHz Taban / 4.70 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (MUX Switch)",
        "16.0 inç WUXGA (1920x1200) 165Hz IPS Ekran", 16.0, "1920x1200 (FHD+ 16:10)", 165, "IPS", "%100 sRGB, 400 Nit, G-Sync",
        "16GB (2x8GB) DDR5 4800MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 230, 2.70, "Sıvı Metal Termal Macun + 4 Bölge RGB Klavye",
        81, 88, 62
    ),

    # --- Acer Nitro 17 & Nitro V 15 / 16 ---
    (
        "laptop_acer_nitro_17_an17_41_7840hs_4070",
        "Acer Nitro 17 AN17-41 17.3 inç QHD 165Hz Ryzen 7 7840HS 16GB RAM 1TB SSD RTX 4070 140W",
        "Acer", 58999.0, "https://www.itopya.com",
        "AMD Ryzen 7 7840HS", "8 Çekirdek / 16 İzlek (Zen 4)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4070 Mobile (8GB GDDR6)", "140W Full Power (115W + 25W Dynamic Boost)", "Var (Advanced Optimus + MUX)",
        "17.3 inç QHD (2560x1440) 165Hz 3ms IPS Düz Panel", 17.3, "2560x1440 (2K QHD 16:9)", 165, "Fast IPS", "%100 DCI-P3, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "1TB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        90, 330, 3.00, "Sıvı Metal Termal Macun + Çift Fanlı Geniş Soğutma Kasası",
        92, 116, 88
    ),
    (
        "laptop_acer_nitro_v_15_anv15_51_13420h_4050",
        "Acer Nitro V 15 ANV15-51 15.6 inç 144Hz Core i5-13420H 16GB RAM 512GB SSD RTX 4050 75W",
        "Acer", 27999.0, "https://www.hepsiburada.com",
        "Intel Core i5-13420H", "8 Çekirdek (4P + 4E) / 12 İzlek", "2.10 GHz Taban / 4.60 GHz Tepe",
        "NVIDIA GeForce RTX 4050 Mobile (6GB GDDR6)", "75W TGP (60W + 15W Dynamic Boost)", "Yok (Optimus Hibrit)",
        "15.6 inç FHD (1920x1080) 144Hz IPS Mat Panel", 15.6, "1920x1080 (FHD 16:9)", 144, "IPS", "%65 sRGB, 250 Nit",
        "16GB (2x8GB) DDR5 5200MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası (1x Boş Yuva)",
        57, 135, 2.10, "Çift Fanlı Kompakt Soğutma Mimarisi",
        76, 78, 52
    ),
    (
        "laptop_acer_nitro_v_16_anv16_41_8845hs_4060",
        "Acer Nitro V 16 ANV16-41 16 inç WUXGA 165Hz Ryzen 7 8845HS 16GB RAM 512GB SSD RTX 4060 85W",
        "Acer", 37999.0, "https://www.hepsiburada.com",
        "AMD Ryzen 7 8845HS", "8 Çekirdek / 16 İzlek (Zen 4 / Ryzen AI 38 TOPS)", "3.80 GHz Taban / 5.10 GHz Tepe",
        "NVIDIA GeForce RTX 4060 Mobile (8GB GDDR6)", "85W TGP", "Var (Advanced Optimus + MUX)",
        "16.0 inç WUXGA (1920x1200) 165Hz 3ms IPS Panel", 16.0, "1920x1200 (FHD+ 16:10)", 165, "IPS", "%100 sRGB, 300 Nit, G-Sync",
        "16GB (2x8GB) DDR5 5600MHz Çift Kanal", "2x SO-DIMM Yuva",
        "512GB PCIe 4.0 NVMe M.2 SSD", "2x M.2 PCIe Gen4 Yuvası",
        57, 135, 2.50, "Ryzen AI Entegre Çift Fan Soğutma",
        84, 94, 68
    ),
]
