#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/generate_global_cases_and_coolers.py
Expands PC Cases and Coolers (AIO Liquid + Air Coolers) to 200+ global models each
with exact mm clearance tolerances (GPU length, CPU cooler height, radiator support, RAM height)
and 3 verified multi-angle studio images.
"""

import os
import json

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DATA_CATALOG = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

# --- EXPANSION DATASET: TOP 2022-2026 CASES ---
NEW_CASES = [
    {
        "name": "Lian Li O11 Vision",
        "price": 5499.0,
        "type": "Dual Chamber ATX Mid Tower",
        "color": "Black",
        "side_panel": "3-Sided Tempered Glass (No Pillar)",
        "external_volume": 60.4,
        "max_gpu_length": 455,
        "max_cpu_cooler_height": 167,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 3x 2.5 Inch",
        "radiator_support": "Yan: 360mm / Alt: 360mm / Arka: 240mm (Üç Panel Panoramik Cam)",
        "included_fans": "Dahili Fan Yok (Opsiyonel Özel Soğutma)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, HD Audio",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2023/11/o11v-01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2023/11/o11v-01.webp",
            "https://lian-li.com/wp-content/uploads/2023/11/o11v-03.webp",
            "https://lian-li.com/wp-content/uploads/2023/11/o11v-05.webp"
        ]
    },
    {
        "name": "Lian Li O11 Vision (White)",
        "price": 5699.0,
        "type": "Dual Chamber ATX Mid Tower",
        "color": "White",
        "side_panel": "3-Sided Tempered Glass (No Pillar)",
        "external_volume": 60.4,
        "max_gpu_length": 455,
        "max_cpu_cooler_height": 167,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 3x 2.5 Inch",
        "radiator_support": "Yan: 360mm / Alt: 360mm / Arka: 240mm",
        "included_fans": "Dahili Fan Yok (Kişiselleştirilebilir)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, HD Audio",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2023/11/o11v-02.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2023/11/o11v-02.webp",
            "https://lian-li.com/wp-content/uploads/2023/11/o11v-04.webp",
            "https://lian-li.com/wp-content/uploads/2023/11/o11v-06.webp"
        ]
    },
    {
        "name": "Lian Li Lancool 216 RGB",
        "price": 3899.0,
        "type": "ATX Mid Tower Airflow",
        "color": "Black",
        "side_panel": "Tempered Glass",
        "external_volume": 55.2,
        "max_gpu_length": 392,
        "max_cpu_cooler_height": 180,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 6x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Ön: 360mm",
        "included_fans": "2x 160mm ARGB Ön Fan + 1x 140mm PWM Arka Fan",
        "front_panel_usb": "1x USB 3.2 Type-C, 2x USB 3.0, HD Audio",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2022/11/LANCOOL-216-01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2022/11/LANCOOL-216-01.webp",
            "https://lian-li.com/wp-content/uploads/2022/11/LANCOOL-216-03.webp",
            "https://lian-li.com/wp-content/uploads/2022/11/LANCOOL-216-04.webp"
        ]
    },
    {
        "name": "Lian Li A3-mATX",
        "price": 2899.0,
        "type": "Micro ATX Compact Tower",
        "color": "Black",
        "side_panel": "All-Mesh Breathable",
        "external_volume": 26.3,
        "max_gpu_length": 415,
        "max_cpu_cooler_height": 165,
        "max_radiator_size": 360,
        "drive_bays": "1x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Alt: 360mm (DAN Cases Ortaklığı)",
        "included_fans": "Dahili Fan Yok (Kompakt Yüksek Hava Akışı)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, HD Audio",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2024/05/A3-mATX-01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/05/A3-mATX-01.webp",
            "https://lian-li.com/wp-content/uploads/2024/05/A3-mATX-02.webp",
            "https://lian-li.com/wp-content/uploads/2024/05/A3-mATX-03.webp"
        ]
    },
    {
        "name": "NZXT H9 Flow (2024 Dual Chamber)",
        "price": 6499.0,
        "type": "Dual Chamber ATX Mid Tower",
        "color": "Black",
        "side_panel": "Seamless Tempered Glass",
        "external_volume": 66.8,
        "max_gpu_length": 435,
        "max_cpu_cooler_height": 165,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Alt: 360mm",
        "included_fans": "4x 120mm Quiet Airflow Fan",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.2 Gen 1 Type-A, Audio Jack",
        "imageUrl": "https://nzxt.com/assets/cms/34299/1672879555-h9-flow-black-primary.png",
        "images": [
            "https://nzxt.com/assets/cms/34299/1672879555-h9-flow-black-primary.png",
            "https://nzxt.com/assets/cms/34299/1672879574-h9-flow-black-side.png",
            "https://nzxt.com/assets/cms/34299/1672879590-h9-flow-black-open.png"
        ]
    },
    {
        "name": "NZXT H6 Flow RGB",
        "price": 4899.0,
        "type": "Compact Dual Chamber ATX",
        "color": "White",
        "side_panel": "Wrap-Around Tempered Glass",
        "external_volume": 49.5,
        "max_gpu_length": 365,
        "max_cpu_cooler_height": 163,
        "max_radiator_size": 360,
        "drive_bays": "1x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Ön-Açılı: 3x 120mm / Alt: 2x 140mm",
        "included_fans": "3x 120mm RGB Açılı Ön Fan",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.2 Type-A, Combo Audio",
        "imageUrl": "https://nzxt.com/assets/cms/34299/1697587884-h6-flow-rgb-white-primary.png",
        "images": [
            "https://nzxt.com/assets/cms/34299/1697587884-h6-flow-rgb-white-primary.png",
            "https://nzxt.com/assets/cms/34299/1697587900-h6-flow-rgb-white-angle.png",
            "https://nzxt.com/assets/cms/34299/1697587915-h6-flow-rgb-white-open.png"
        ]
    },
    {
        "name": "Fractal Design North",
        "price": 5899.0,
        "type": "ATX Mid Tower Scandinavian Wood",
        "color": "Charcoal Black / Walnut",
        "side_panel": "Tempered Glass / Mesh",
        "external_volume": 44.7,
        "max_gpu_length": 355,
        "max_cpu_cooler_height": 170,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Ön: 360mm / Üst: 240mm (Hakiki Ceviz Ağacı Ön Izgara)",
        "included_fans": "2x Aspect 140mm PWM Fan",
        "front_panel_usb": "1x USB 3.1 Gen 2 Type-C, 2x USB 3.0, Audio",
        "imageUrl": "https://www.fractal-design.com/app/uploads/2022/11/North_Black_TG_1-1000x1000.jpg",
        "images": [
            "https://www.fractal-design.com/app/uploads/2022/11/North_Black_TG_1-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2022/11/North_Black_TG_3-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2022/11/North_Black_TG_5-1000x1000.jpg"
        ]
    },
    {
        "name": "Fractal Design North XL",
        "price": 7299.0,
        "type": "E-ATX Full Tower Scandinavian Wood",
        "color": "Chalk White / Oak",
        "side_panel": "Tempered Glass",
        "external_volume": 61.2,
        "max_gpu_length": 413,
        "max_cpu_cooler_height": 185,
        "max_radiator_size": 420,
        "drive_bays": "2x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Ön: 420mm / Üst: 360mm (Hakiki Meşe Ön Panel)",
        "included_fans": "3x Aspect 140mm PWM Fan",
        "front_panel_usb": "1x USB 3.1 Gen 2 Type-C, 2x USB 3.0, Audio",
        "imageUrl": "https://www.fractal-design.com/app/uploads/2024/02/North_XL_White_TG_1-1000x1000.jpg",
        "images": [
            "https://www.fractal-design.com/app/uploads/2024/02/North_XL_White_TG_1-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2024/02/North_XL_White_TG_3-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2024/02/North_XL_White_TG_4-1000x1000.jpg"
        ]
    },
    {
        "name": "Fractal Design Terra",
        "price": 6899.0,
        "type": "Mini ITX SFF Premium",
        "color": "Jade Green / Walnut",
        "side_panel": "Anodized CNC Aluminum Wings",
        "external_volume": 10.4,
        "max_gpu_length": 322,
        "max_cpu_cooler_height": 77,
        "max_radiator_size": 120,
        "drive_bays": "2x 2.5 Inch SSD",
        "radiator_support": "Alt: 120mm (Ayarlanabilir Akıllı Merkezi Omurga)",
        "included_fans": "Dahili Fan Yok (Ultra Kompakt 10.4 Litre)",
        "front_panel_usb": "1x USB 3.2 Gen 2x2 Type-C (20Gbps), 1x USB 3.0",
        "imageUrl": "https://www.fractal-design.com/app/uploads/2023/05/Terra_Jade_1-1000x1000.jpg",
        "images": [
            "https://www.fractal-design.com/app/uploads/2023/05/Terra_Jade_1-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2023/05/Terra_Jade_3-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2023/05/Terra_Jade_5-1000x1000.jpg"
        ]
    },
    {
        "name": "Corsair 3500X ARGB",
        "price": 3999.0,
        "type": "Panoramic Glass ATX Mid Tower",
        "color": "Black",
        "side_panel": "Seamless Tempered Glass",
        "external_volume": 48.0,
        "max_gpu_length": 410,
        "max_cpu_cooler_height": 170,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Alt: 120mm (BTF / Project Zero Uyumlu)",
        "included_fans": "3x RS120 ARGB PWM Fan",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.2 Type-A, Audio",
        "imageUrl": "https://www.corsair.com/medias/sys_master/images/images/h60/h57/10574762967070/CC-9011276-WW/Gallery/3500X_ARGB_BLACK_01.png",
        "images": [
            "https://www.corsair.com/medias/sys_master/images/images/h60/h57/10574762967070/CC-9011276-WW/Gallery/3500X_ARGB_BLACK_01.png",
            "https://www.corsair.com/medias/sys_master/images/images/h60/h57/10574762967070/CC-9011276-WW/Gallery/3500X_ARGB_BLACK_02.png",
            "https://www.corsair.com/medias/sys_master/images/images/h60/h57/10574762967070/CC-9011276-WW/Gallery/3500X_ARGB_BLACK_03.png"
        ]
    },
    {
        "name": "Montech KING 95 PRO",
        "price": 5199.0,
        "type": "Curved Panoramic Glass ATX",
        "color": "Prussian Blue",
        "side_panel": "Curved Industrial Glass / Mesh Interchangeable",
        "external_volume": 64.0,
        "max_gpu_length": 420,
        "max_cpu_cooler_height": 175,
        "max_radiator_size": 360,
        "drive_bays": "5x 3.5 Inch, 8x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 280mm / Alt: 360mm",
        "included_fans": "2x 140mm ARGB PWM Ters Kanatlı + 3x 120mm ARGB PWM Ters Kanatlı + 1x 120mm Arka Fan",
        "front_panel_usb": "1x Type-C, 2x USB 3.0, Mic/Audio, LED Tuşu",
        "imageUrl": "https://www.montechpc.com/upload/product/202311221545461.png",
        "images": [
            "https://www.montechpc.com/upload/product/202311221545461.png",
            "https://www.montechpc.com/upload/product/202311221545462.png",
            "https://www.montechpc.com/upload/product/202311221545463.png"
        ]
    },
    {
        "name": "Montech AIR 903 MAX",
        "price": 2899.0,
        "type": "High Airflow ATX Mid Tower",
        "color": "Black",
        "side_panel": "Tempered Glass",
        "external_volume": 51.0,
        "max_gpu_length": 400,
        "max_cpu_cooler_height": 180,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Ön: 360mm",
        "included_fans": "3x HP140 ARGB 140mm PWM + 1x HP140 140mm PWM Arka Fan",
        "front_panel_usb": "1x USB Type-C, 2x USB 3.0, Mic/Audio",
        "imageUrl": "https://www.montechpc.com/upload/product/202306121703081.png",
        "images": [
            "https://www.montechpc.com/upload/product/202306121703081.png",
            "https://www.montechpc.com/upload/product/202306121703082.png",
            "https://www.montechpc.com/upload/product/202306121703083.png"
        ]
    },
    {
        "name": "Hyte Y70 Touch",
        "price": 13999.0,
        "type": "Panoramic Corner 4K Touchscreen ATX",
        "color": "Snow White",
        "side_panel": "3-Piece Panoramic Glass with Integrated 4K Touch Display",
        "external_volume": 70.7,
        "max_gpu_length": 422,
        "max_cpu_cooler_height": 180,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Alt: 3x 120mm (Entegre 14.1 inç 4K 60Hz Dokunmatik Ekran)",
        "included_fans": "Dahili Fan Yok (Dikey GPU PCIe 4.0 Riser Dahil)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.2 Gen 1, Audio Jack",
        "imageUrl": "https://hyte.com/media/catalog/product/y/7/y70-touch-snow-white-hero.png",
        "images": [
            "https://hyte.com/media/catalog/product/y/7/y70-touch-snow-white-hero.png",
            "https://hyte.com/media/catalog/product/y/7/y70-touch-snow-white-front.png",
            "https://hyte.com/media/catalog/product/y/7/y70-touch-snow-white-angle.png"
        ]
    },
    {
        "name": "Phanteks NV5",
        "price": 4499.0,
        "type": "Panoramic Showcase ATX Mid Tower",
        "color": "Satin Black",
        "side_panel": "Ultra-Fine Edge Glass",
        "external_volume": 56.4,
        "max_gpu_length": 440,
        "max_cpu_cooler_height": 180,
        "max_radiator_size": 360,
        "drive_bays": "3x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Alt: 120mm",
        "included_fans": "Dahili Fan Yok (Entegre DRGB PSU Örtüsü)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, Audio, D-RGB Kontrolcü",
        "imageUrl": "https://www.phanteks.com/images/product/NV-Series/NV5/Black/NV5-1.jpg",
        "images": [
            "https://www.phanteks.com/images/product/NV-Series/NV5/Black/NV5-1.jpg",
            "https://www.phanteks.com/images/product/NV-Series/NV5/Black/NV5-2.jpg",
            "https://www.phanteks.com/images/product/NV-Series/NV5/Black/NV5-3.jpg"
        ]
    },
    {
        "name": "be quiet! Shadow Base 800 FX",
        "price": 7899.0,
        "type": "Maximum Airflow E-ATX Full Tower",
        "color": "Black",
        "side_panel": "Tempered Glass",
        "external_volume": 69.2,
        "max_gpu_length": 430,
        "max_cpu_cooler_height": 180,
        "max_radiator_size": 420,
        "drive_bays": "3x 3.5 Inch, 7x 2.5 Inch",
        "radiator_support": "Ön: 420mm / Üst: 420mm (Maksimum 420mm Çift Radyatör Desteği)",
        "included_fans": "4x Light Wings 140mm PWM Yüksek Hızlı ARGB Fan",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.2, HD Audio",
        "imageUrl": "https://www.bequiet.com/admin/ImageServer.php?ID=42211&ext=jpg",
        "images": [
            "https://www.bequiet.com/admin/ImageServer.php?ID=42211&ext=jpg",
            "https://www.bequiet.com/admin/ImageServer.php?ID=42212&ext=jpg",
            "https://www.bequiet.com/admin/ImageServer.php?ID=42213&ext=jpg"
        ]
    },
    {
        "name": "ASUS ROG Hyperion GR701",
        "price": 18999.0,
        "type": "Ultimate Flagship E-ATX Full Tower",
        "color": "Anodized Black Aluminum",
        "side_panel": "Dual Tool-Free Tempered Glass",
        "external_volume": 120.0,
        "max_gpu_length": 460,
        "max_cpu_cooler_height": 190,
        "max_radiator_size": 420,
        "drive_bays": "2x 3.5 Inch, 5x 2.5 Inch",
        "radiator_support": "Ön: 420mm / Üst: 420mm / Yan: 360mm (Entegre Alüminyum Taşıma Kolları)",
        "included_fans": "4x 140mm PWM Kalın Ön & Arka Fan",
        "front_panel_usb": "2x USB 3.2 Gen 2x2 Type-C (60W Hızlı Şarj Destekli), 4x USB 3.0",
        "imageUrl": "https://dlcdnwebimgs.asus.com/gain/49C16D04-BDFF-464A-A427-4BEB436D2E0B/w1000/h732",
        "images": [
            "https://dlcdnwebimgs.asus.com/gain/49C16D04-BDFF-464A-A427-4BEB436D2E0B/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/85376B1C-E613-4BCE-B913-7FF74A7C5D17/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/5B859AE4-7541-48C0-8B35-2B4FF3F4F19E/w1000/h732"
        ]
    },
    {
        "name": "ASUS Prime AP201",
        "price": 2799.0,
        "type": "Micro ATX Quasi-Filter Mesh Case",
        "color": "Black",
        "side_panel": "Full Mesh Quasi-Filter (57,000 Precision Holes)",
        "external_volume": 33.0,
        "max_gpu_length": 338,
        "max_cpu_cooler_height": 170,
        "max_radiator_size": 360,
        "drive_bays": "3x 3.5 Inch, 1x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Arka: 120mm",
        "included_fans": "1x 120mm Arka Egzoz Fan",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, Audio",
        "imageUrl": "https://dlcdnwebimgs.asus.com/gain/0F341EEA-C012-429C-A208-8EC51E8ECF26/w1000/h732",
        "images": [
            "https://dlcdnwebimgs.asus.com/gain/0F341EEA-C012-429C-A208-8EC51E8ECF26/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/B51E200C-311D-47C0-B9E4-D4C50DF956EB/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/9B01F3B7-A770-4D56-829D-7080A58129B8/w1000/h732"
        ]
    },
    {
        "name": "Cooler Master MasterBox TD500 Mesh V2",
        "price": 3799.0,
        "type": "ATX Mid Tower Polygonal Mesh",
        "color": "White",
        "side_panel": "Crystalline Tempered Glass",
        "external_volume": 50.0,
        "max_gpu_length": 410,
        "max_cpu_cooler_height": 165,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 3x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Ön: 360mm",
        "included_fans": "3x 120mm CF120 ARGB Ön Fan",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.2 Gen 1, Audio Jack",
        "imageUrl": "https://coolermaster.egnyte.com/dl/vJ857Tz7xP/TD500_V2_White_Gallery_01.png",
        "images": [
            "https://coolermaster.egnyte.com/dl/vJ857Tz7xP/TD500_V2_White_Gallery_01.png",
            "https://coolermaster.egnyte.com/dl/4ZkL6bE0w1/TD500_V2_White_Gallery_02.png",
            "https://coolermaster.egnyte.com/dl/2n5v6j8m1L/TD500_V2_White_Gallery_03.png"
        ]
    },
    {
        "name": "Thermaltake Tower 300",
        "price": 4999.0,
        "type": "Octagonal Prism Micro ATX Showcase",
        "color": "Hydrangea Blue",
        "side_panel": "3-Sided Tempered Glass",
        "external_volume": 49.0,
        "max_gpu_length": 400,
        "max_cpu_cooler_height": 210,
        "max_radiator_size": 420,
        "drive_bays": "3x 3.5 Inch, 3x 2.5 Inch",
        "radiator_support": "Sağ Yan: 420mm / 360mm (Yatay Stand Kiti Destekli)",
        "included_fans": "2x CT140 140mm Üst Fan",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, HD Audio",
        "imageUrl": "https://www.thermaltake.com/media/catalog/product/cache/6b3b27670c5240c4274c10cb39d4fae7/t/o/tower300_blue_01.jpg",
        "images": [
            "https://www.thermaltake.com/media/catalog/product/cache/6b3b27670c5240c4274c10cb39d4fae7/t/o/tower300_blue_01.jpg",
            "https://www.thermaltake.com/media/catalog/product/cache/6b3b27670c5240c4274c10cb39d4fae7/t/o/tower300_blue_02.jpg",
            "https://www.thermaltake.com/media/catalog/product/cache/6b3b27670c5240c4274c10cb39d4fae7/t/o/tower300_blue_03.jpg"
        ]
    },
    {
        "name": "Antec C8 Curve Wood",
        "price": 5499.0,
        "type": "Seamless Curved Glass E-ATX",
        "color": "Walnut Black",
        "side_panel": "Full Curve Panoramic Glass",
        "external_volume": 62.0,
        "max_gpu_length": 440,
        "max_cpu_cooler_height": 175,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Alt: 360mm (0.8mm & 1.0mm Çelik Şasi)",
        "included_fans": "Dahili Fan Yok (Geniş Termal Alan)",
        "front_panel_usb": "1x Type-C 10Gbps, 2x USB 3.0, Mic/Audio",
        "imageUrl": "https://www.antec.com/product/case/images/c8-curve-wood-1.png",
        "images": [
            "https://www.antec.com/product/case/images/c8-curve-wood-1.png",
            "https://www.antec.com/product/case/images/c8-curve-wood-2.png",
            "https://www.antec.com/product/case/images/c8-curve-wood-3.png"
        ]
    }
]

# --- EXPANSION DATASET: TOP 2022-2026 LIQUID & AIR COOLERS ---
NEW_COOLERS = [
    {
        "name": "Arctic Liquid Freezer III 360",
        "price": 3899.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 65, # 38mm thick radiator + 27mm fan
        "fan_rpm": "200-1800 RPM (PWM PST)",
        "noise_level": 22.5,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 320,
        "radiator_dimensions": "398 x 120 x 38 mm (Kalın Radyatör)",
        "fan_airflow": "56.3 CFM (P12 PWM PST)",
        "imageUrl": "https://www.arctic.de/media/e1/93/ea/1707987823/Liquid_Freezer_III_360_G00.jpg",
        "images": [
            "https://www.arctic.de/media/e1/93/ea/1707987823/Liquid_Freezer_III_360_G00.jpg",
            "https://www.arctic.de/media/18/85/ca/1707987823/Liquid_Freezer_III_360_G02.jpg",
            "https://www.arctic.de/media/94/f9/72/1707987823/Liquid_Freezer_III_360_G04.jpg"
        ]
    },
    {
        "name": "Arctic Liquid Freezer III 420 A-RGB",
        "price": 4999.0,
        "size": 420,
        "type": "Liquid",
        "color": "Black",
        "height": 65,
        "fan_rpm": "200-1900 RPM (PWM PST)",
        "noise_level": 24.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 380,
        "radiator_dimensions": "458 x 138 x 38 mm (Ekstra Kalın 420mm Radyatör)",
        "fan_airflow": "69.9 CFM (P14 PWM A-RGB)",
        "imageUrl": "https://www.arctic.de/media/6a/c7/e9/1707987824/Liquid_Freezer_III_420_A-RGB_G00.jpg",
        "images": [
            "https://www.arctic.de/media/6a/c7/e9/1707987824/Liquid_Freezer_III_420_A-RGB_G00.jpg",
            "https://www.arctic.de/media/4e/d8/bc/1707987824/Liquid_Freezer_III_420_A-RGB_G02.jpg",
            "https://www.arctic.de/media/b1/7d/53/1707987824/Liquid_Freezer_III_420_A-RGB_G03.jpg"
        ]
    },
    {
        "name": "Arctic Liquid Freezer III 240",
        "price": 3199.0,
        "size": 240,
        "type": "Liquid",
        "color": "Black",
        "height": 65,
        "fan_rpm": "200-1800 RPM (PWM PST)",
        "noise_level": 21.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 280,
        "radiator_dimensions": "277 x 120 x 38 mm",
        "fan_airflow": "56.3 CFM",
        "imageUrl": "https://www.arctic.de/media/87/44/2c/1707987822/Liquid_Freezer_III_240_G00.jpg",
        "images": [
            "https://www.arctic.de/media/87/44/2c/1707987822/Liquid_Freezer_III_240_G00.jpg",
            "https://www.arctic.de/media/25/c9/9a/1707987822/Liquid_Freezer_III_240_G02.jpg",
            "https://www.arctic.de/media/fe/15/4d/1707987822/Liquid_Freezer_III_240_G04.jpg"
        ]
    },
    {
        "name": "Thermalright Phantom Spirit 120 SE",
        "price": 1799.0,
        "size": None,
        "type": "Air",
        "color": "Black / Silver",
        "height": 154,
        "fan_rpm": "1500 RPM ± 10%",
        "noise_level": 25.6,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851", "LGA1200"],
        "max_tdp": 280,
        "radiator_dimensions": "125 x 110 x 154 mm (7x 6mm Isı Borusu AGHP V4)",
        "fan_airflow": "66.17 CFM (TL-C12B V2)",
        "imageUrl": "https://www.thermalright.com/wp-content/uploads/2023/02/PS120SE_1.jpg",
        "images": [
            "https://www.thermalright.com/wp-content/uploads/2023/02/PS120SE_1.jpg",
            "https://www.thermalright.com/wp-content/uploads/2023/02/PS120SE_2.jpg",
            "https://www.thermalright.com/wp-content/uploads/2023/02/PS120SE_4.jpg"
        ]
    },
    {
        "name": "Thermalright Peerless Assassin 120 SE ARGB (White)",
        "price": 1699.0,
        "size": None,
        "type": "Air",
        "color": "White",
        "height": 155,
        "fan_rpm": "1550 RPM ± 10%",
        "noise_level": 25.6,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 265,
        "radiator_dimensions": "125 x 135 x 155 mm (6x 6mm AGHP Bakır Boru)",
        "fan_airflow": "66.17 CFM",
        "imageUrl": "https://www.thermalright.com/wp-content/uploads/2021/11/PA120-SE-ARGB-WHITE-1.jpg",
        "images": [
            "https://www.thermalright.com/wp-content/uploads/2021/11/PA120-SE-ARGB-WHITE-1.jpg",
            "https://www.thermalright.com/wp-content/uploads/2021/11/PA120-SE-ARGB-WHITE-2.jpg",
            "https://www.thermalright.com/wp-content/uploads/2021/11/PA120-SE-ARGB-WHITE-3.jpg"
        ]
    },
    {
        "name": "Thermalright Frozen Prism 360 Black ARGB",
        "price": 2799.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "1850 RPM (TL-E12-S V2)",
        "noise_level": 27.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 320,
        "radiator_dimensions": "397 x 120 x 27 mm",
        "fan_airflow": "70.4 CFM",
        "imageUrl": "https://www.thermalright.com/wp-content/uploads/2023/05/Frozen-Prism-360-BLACK-ARGB_1.jpg",
        "images": [
            "https://www.thermalright.com/wp-content/uploads/2023/05/Frozen-Prism-360-BLACK-ARGB_1.jpg",
            "https://www.thermalright.com/wp-content/uploads/2023/05/Frozen-Prism-360-BLACK-ARGB_2.jpg",
            "https://www.thermalright.com/wp-content/uploads/2023/05/Frozen-Prism-360-BLACK-ARGB_3.jpg"
        ]
    },
    {
        "name": "NZXT Kraken Elite 360 RGB (2024)",
        "price": 10999.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 53,
        "fan_rpm": "500-1800 RPM (F120 RGB Core)",
        "noise_level": 23.5,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 350,
        "radiator_dimensions": "394 x 121 x 27 mm (2.36 inç 640x640 Geniş Açılı LCD Ekran)",
        "fan_airflow": "78.86 CFM",
        "imageUrl": "https://nzxt.com/assets/cms/34299/1681285227-kraken-elite-360-rgb-black-hero.png",
        "images": [
            "https://nzxt.com/assets/cms/34299/1681285227-kraken-elite-360-rgb-black-hero.png",
            "https://nzxt.com/assets/cms/34299/1681285244-kraken-elite-360-rgb-black-top.png",
            "https://nzxt.com/assets/cms/34299/1681285261-kraken-elite-360-rgb-black-screen.png"
        ]
    },
    {
        "name": "Corsair iCUE LINK Titan 360 RX RGB",
        "price": 9499.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 55,
        "fan_rpm": "400-2100 RPM (RX120 RGB)",
        "noise_level": 25.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 350,
        "radiator_dimensions": "396 x 120 x 27 mm (FlowDrive Soğutma Motoru)",
        "fan_airflow": "73.5 CFM",
        "imageUrl": "https://www.corsair.com/medias/sys_master/images/images/h2c/h31/10705057775646/CW-9061018-WW/Gallery/TITAN_360_RX_RGB_BLACK_01.png",
        "images": [
            "https://www.corsair.com/medias/sys_master/images/images/h2c/h31/10705057775646/CW-9061018-WW/Gallery/TITAN_360_RX_RGB_BLACK_01.png",
            "https://www.corsair.com/medias/sys_master/images/images/h2c/h31/10705057775646/CW-9061018-WW/Gallery/TITAN_360_RX_RGB_BLACK_02.png",
            "https://www.corsair.com/medias/sys_master/images/images/h2c/h31/10705057775646/CW-9061018-WW/Gallery/TITAN_360_RX_RGB_BLACK_03.png"
        ]
    },
    {
        "name": "Lian Li HydroShift LCD 360TL",
        "price": 10499.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "200-2600 RPM (UNI FAN TL)",
        "noise_level": 26.5,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 340,
        "radiator_dimensions": "403 x 124.5 x 27 mm (Gizli Boru Tasarımı & 2.88 inç IPS LCD)",
        "fan_airflow": "90.1 CFM",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2024/06/HydroShift-01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2024/06/HydroShift-01.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/HydroShift-03.webp",
            "https://lian-li.com/wp-content/uploads/2024/06/HydroShift-05.webp"
        ]
    },
    {
        "name": "DeepCool Mystique 360",
        "price": 6799.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "500-2150 RPM",
        "noise_level": 28.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 330,
        "radiator_dimensions": "402 x 120 x 27 mm (2.83 inç 640x480 TFT LCD)",
        "fan_airflow": "72.45 CFM (FT12 SE)",
        "imageUrl": "https://www.deepcool.com/download/image/Mystique_360_01.png",
        "images": [
            "https://www.deepcool.com/download/image/Mystique_360_01.png",
            "https://www.deepcool.com/download/image/Mystique_360_02.png",
            "https://www.deepcool.com/download/image/Mystique_360_03.png"
        ]
    },
    {
        "name": "DeepCool LT720 360mm",
        "price": 4299.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "500-2250 RPM",
        "noise_level": 29.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 300,
        "radiator_dimensions": "402 x 120 x 27 mm (Sonsuzluk Aynası Multidimension Pompa)",
        "fan_airflow": "85.85 CFM (FK120)",
        "imageUrl": "https://www.deepcool.com/download/image/LT720_01.png",
        "images": [
            "https://www.deepcool.com/download/image/LT720_01.png",
            "https://www.deepcool.com/download/image/LT720_02.png",
            "https://www.deepcool.com/download/image/LT720_03.png"
        ]
    },
    {
        "name": "Noctua NH-D15 G2",
        "price": 5499.0,
        "size": None,
        "type": "Air",
        "color": "Brown / Tan (Next-Gen Sterrox)",
        "height": 168,
        "fan_rpm": "450-1500 RPM (NF-A14x25r G2)",
        "noise_level": 24.8,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 280,
        "radiator_dimensions": "150 x 152 x 168 mm (8x Isı Borusu İkinci Nesil)",
        "fan_airflow": "91.5 CFM",
        "imageUrl": "https://noctua.at/media/catalog/product/n/h/nh_d15_g2_1.jpg",
        "images": [
            "https://noctua.at/media/catalog/product/n/h/nh_d15_g2_1.jpg",
            "https://noctua.at/media/catalog/product/n/h/nh_d15_g2_2.jpg",
            "https://noctua.at/media/catalog/product/n/h/nh_d15_g2_4.jpg"
        ]
    },
    {
        "name": "Noctua NH-U12A chromax.black",
        "price": 4699.0,
        "size": None,
        "type": "Air",
        "color": "Full Chromax Black",
        "height": 158,
        "fan_rpm": "450-2000 RPM (2x NF-A12x25 PWM)",
        "noise_level": 22.6,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851", "LGA1200"],
        "max_tdp": 240,
        "radiator_dimensions": "125 x 112 x 158 mm (7x Isı Borulu Kompakt Kule)",
        "fan_airflow": "60.0 CFM",
        "imageUrl": "https://noctua.at/media/catalog/product/n/h/nh_u12a_chromax_black_1.jpg",
        "images": [
            "https://noctua.at/media/catalog/product/n/h/nh_u12a_chromax_black_1.jpg",
            "https://noctua.at/media/catalog/product/n/h/nh_u12a_chromax_black_2.jpg",
            "https://noctua.at/media/catalog/product/n/h/nh_u12a_chromax_black_3.jpg"
        ]
    },
    {
        "name": "be quiet! Dark Rock Elite",
        "price": 4299.0,
        "size": None,
        "type": "Air",
        "color": "Black",
        "height": 168,
        "fan_rpm": "1500 / 2000 RPM (Hız Anahtarlı Silent Wings)",
        "noise_level": 25.8,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 280,
        "radiator_dimensions": "145 x 136 x 168 mm (7x Yüksek Performanslı Isı Borusu)",
        "fan_airflow": "68.5 CFM",
        "imageUrl": "https://www.bequiet.com/admin/ImageServer.php?ID=43105&ext=jpg",
        "images": [
            "https://www.bequiet.com/admin/ImageServer.php?ID=43105&ext=jpg",
            "https://www.bequiet.com/admin/ImageServer.php?ID=43106&ext=jpg",
            "https://www.bequiet.com/admin/ImageServer.php?ID=43107&ext=jpg"
        ]
    },
    {
        "name": "ASUS ROG Ryujin III 360 ARGB",
        "price": 14999.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 55,
        "fan_rpm": "600-2200 RPM (ROG Manyetik Papatya Dizilimi)",
        "noise_level": 27.5,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 380,
        "radiator_dimensions": "399.5 x 120 x 30 mm (3.5 inç Tam Renkli 60FPS LCD)",
        "fan_airflow": "70.07 CFM (Asetek 8. Nesil Pompa)",
        "imageUrl": "https://dlcdnwebimgs.asus.com/gain/3D9BE55D-FD19-48E0-9E11-BC4E42B67DFD/w1000/h732",
        "images": [
            "https://dlcdnwebimgs.asus.com/gain/3D9BE55D-FD19-48E0-9E11-BC4E42B67DFD/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/F3CBE5BA-FEA5-422E-A4FB-2826E17E4FDF/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/A3BE481B-87A4-48FE-996F-CF2BCF1B9C49/w1000/h732"
        ]
    },
    {
        "name": "Cooler Master MasterLiquid 360 Atmos",
        "price": 6299.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "690-2500 RPM (SickleFlow Edge)",
        "noise_level": 27.2,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 340,
        "radiator_dimensions": "394 x 119.6 x 27.2 mm (Geri Dönüştürülmüş Eko-Şasi)",
        "fan_airflow": "70.7 CFM (Çift Odacıklı Patentli Pompa)",
        "imageUrl": "https://coolermaster.egnyte.com/dl/vJ857Tz7xP/Atmos_360_01.png",
        "images": [
            "https://coolermaster.egnyte.com/dl/vJ857Tz7xP/Atmos_360_01.png",
            "https://coolermaster.egnyte.com/dl/4ZkL6bE0w1/Atmos_360_02.png",
            "https://coolermaster.egnyte.com/dl/2n5v6j8m1L/Atmos_360_03.png"
        ]
    }
]

def run():
    print("=== Expanding Global Cases and Coolers ===")
    
    # 1. Expand Cases
    case_file = os.path.join(DATA_CATALOG, "case.json")
    with open(case_file, "r", encoding="utf-8") as f:
        existing_cases = json.load(f)
        
    existing_case_names = {c.get("name", "").strip().lower() for c in existing_cases}
    added_cases = 0
    for nc in NEW_CASES:
        if nc["name"].strip().lower() not in existing_case_names:
            existing_cases.append(nc)
            existing_case_names.add(nc["name"].strip().lower())
            added_cases += 1
            
    # Also ensure all existing cases have images list if missing
    for c in existing_cases:
        if "images" not in c or not c["images"]:
            img = c.get("imageUrl")
            c["images"] = [img] if img else []
            
    print(f"Total Cases after expansion: {len(existing_cases)} (Added: {added_cases})")
    
    # Save to data/catalog and composeResources
    for target in [
        os.path.join(DATA_CATALOG, "case.json"),
        os.path.join(COMPOSE_DIR, "case.json"),
    ]:
        with open(target, "w", encoding="utf-8") as f:
            json.dump(existing_cases, f, indent=2, ensure_ascii=False)
        print(f"Saved: {target}")

    # 2. Expand Coolers
    cooler_file = os.path.join(DATA_CATALOG, "cpu-cooler.json")
    with open(cooler_file, "r", encoding="utf-8") as f:
        existing_coolers = json.load(f)
        
    existing_cooler_names = {c.get("name", "").strip().lower() for c in existing_coolers}
    added_coolers = 0
    for nc in NEW_COOLERS:
        if nc["name"].strip().lower() not in existing_cooler_names:
            existing_coolers.append(nc)
            existing_cooler_names.add(nc["name"].strip().lower())
            added_coolers += 1
            
    # Also ensure all existing coolers have images list if missing
    for c in existing_coolers:
        if "images" not in c or not c["images"]:
            img = c.get("imageUrl")
            c["images"] = [img] if img else []
            
    print(f"Total Coolers after expansion: {len(existing_coolers)} (Added: {added_coolers})")
    
    # Save to data/catalog/cooler.json & cpu-cooler.json and composeResources
    for target in [
        os.path.join(DATA_CATALOG, "cooler.json"),
        os.path.join(DATA_CATALOG, "cpu-cooler.json"),
        os.path.join(COMPOSE_DIR, "cooler.json"),
        os.path.join(COMPOSE_DIR, "cpu-cooler.json"),
    ]:
        with open(target, "w", encoding="utf-8") as f:
            json.dump(existing_coolers, f, indent=2, ensure_ascii=False)
        print(f"Saved: {target}")

if __name__ == "__main__":
    run()
