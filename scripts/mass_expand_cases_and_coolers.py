#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import json

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
DATA_CATALOG = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

ADDITIONAL_CASES = [
    # Corsair
    {
        "name": "Corsair 4000D AIRFLOW (White)",
        "price": 3499.0,
        "type": "ATX Mid Tower Airflow",
        "color": "White",
        "side_panel": "Tempered Glass",
        "external_volume": 48.5,
        "max_gpu_length": 360,
        "max_cpu_cooler_height": 170,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Ön: 360mm / Üst: 240mm / Arka: 120mm",
        "included_fans": "2x 120mm AirGuide Fan",
        "front_panel_usb": "1x USB 3.1 Type-C, 1x USB 3.0, Audio",
        "imageUrl": "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CC-9011201-WW/Gallery/4000D_AF_WHITE_01.png",
        "images": [
            "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CC-9011201-WW/Gallery/4000D_AF_WHITE_01.png",
            "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CC-9011201-WW/Gallery/4000D_AF_WHITE_02.png",
            "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CC-9011201-WW/Gallery/4000D_AF_WHITE_03.png"
        ]
    },
    {
        "name": "Corsair 5000D AIRFLOW",
        "price": 5499.0,
        "type": "ATX Mid Tower",
        "color": "Black",
        "side_panel": "Tempered Glass",
        "external_volume": 66.0,
        "max_gpu_length": 400,
        "max_cpu_cooler_height": 170,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Ön: 360mm / Üst: 360mm / Yan: 360mm",
        "included_fans": "2x 120mm AirGuide Fan",
        "front_panel_usb": "1x USB 3.1 Type-C, 2x USB 3.0, Audio",
        "imageUrl": "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CC-9011210-WW/Gallery/5000D_AF_BLACK_01.png",
        "images": [
            "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CC-9011210-WW/Gallery/5000D_AF_BLACK_01.png",
            "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CC-9011210-WW/Gallery/5000D_AF_BLACK_02.png",
            "https://www.corsair.com/medias/sys_master/images/images/h67/h9a/9546700685342/CC-9011210-WW/Gallery/5000D_AF_BLACK_03.png"
        ]
    },
    {
        "name": "Corsair 6500X Dual Chamber",
        "price": 7999.0,
        "type": "Dual Chamber Panoramic ATX",
        "color": "Black",
        "side_panel": "Seamless Tempered Glass",
        "external_volume": 72.0,
        "max_gpu_length": 420,
        "max_cpu_cooler_height": 190,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 3x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Alt: 360mm (Ters Konnektör Anakart Uyumlu)",
        "included_fans": "Dahili Fan Yok (iCUE LINK Uyumlu)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 4x USB 3.0, Combo Audio",
        "imageUrl": "https://www.corsair.com/medias/sys_master/images/images/h31/h57/10542385102878/CC-9011257-WW/Gallery/6500X_BLACK_01.png",
        "images": [
            "https://www.corsair.com/medias/sys_master/images/images/h31/h57/10542385102878/CC-9011257-WW/Gallery/6500X_BLACK_01.png",
            "https://www.corsair.com/medias/sys_master/images/images/h31/h57/10542385102878/CC-9011257-WW/Gallery/6500X_BLACK_02.png",
            "https://www.corsair.com/medias/sys_master/images/images/h31/h57/10542385102878/CC-9011257-WW/Gallery/6500X_BLACK_03.png"
        ]
    },
    {
        "name": "Corsair 2500X Micro ATX Dual Chamber",
        "price": 5799.0,
        "type": "Micro ATX Dual Chamber",
        "color": "White",
        "side_panel": "Dual Tempered Glass",
        "external_volume": 49.0,
        "max_gpu_length": 400,
        "max_cpu_cooler_height": 180,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 240mm / Alt: 360mm",
        "included_fans": "Dahili Fan Yok",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0",
        "imageUrl": "https://www.corsair.com/medias/sys_master/images/images/h12/h42/10542386085918/CC-9011266-WW/Gallery/2500X_WHITE_01.png",
        "images": [
            "https://www.corsair.com/medias/sys_master/images/images/h12/h42/10542386085918/CC-9011266-WW/Gallery/2500X_WHITE_01.png",
            "https://www.corsair.com/medias/sys_master/images/images/h12/h42/10542386085918/CC-9011266-WW/Gallery/2500X_WHITE_02.png",
            "https://www.corsair.com/medias/sys_master/images/images/h12/h42/10542386085918/CC-9011266-WW/Gallery/2500X_WHITE_03.png"
        ]
    },
    # Fractal Design
    {
        "name": "Fractal Design Torrent",
        "price": 7699.0,
        "type": "High Airflow E-ATX Full Tower",
        "color": "Black",
        "side_panel": "Light Tint Tempered Glass",
        "external_volume": 69.5,
        "max_gpu_length": 423,
        "max_cpu_cooler_height": 188,
        "max_radiator_size": 420,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Ön: 420mm / Alt: 420mm (Maksimum Pozitif Basınçlı Hava Akışı)",
        "included_fans": "2x Dynamic GP-18 180mm PWM + 3x Dynamic GP-14 140mm PWM",
        "front_panel_usb": "1x USB 3.1 Gen 2 Type-C, 2x USB 3.0, Audio",
        "imageUrl": "https://www.fractal-design.com/app/uploads/2021/07/Torrent_Black_TG_Light_1-1000x1000.jpg",
        "images": [
            "https://www.fractal-design.com/app/uploads/2021/07/Torrent_Black_TG_Light_1-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2021/07/Torrent_Black_TG_Light_2-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2021/07/Torrent_Black_TG_Light_3-1000x1000.jpg"
        ]
    },
    {
        "name": "Fractal Design Torrent Compact",
        "price": 5499.0,
        "type": "High Airflow ATX Mid Tower",
        "color": "White",
        "side_panel": "Clear Tempered Glass",
        "external_volume": 46.7,
        "max_gpu_length": 330,
        "max_cpu_cooler_height": 174,
        "max_radiator_size": 360,
        "drive_bays": "1x 3.5 Inch, 3x 2.5 Inch",
        "radiator_support": "Ön: 360mm / Alt: 280mm",
        "included_fans": "2x Dynamic GP-18 180mm Dev Ön Fan",
        "front_panel_usb": "1x USB 3.1 Type-C, 2x USB 3.0",
        "imageUrl": "https://www.fractal-design.com/app/uploads/2022/01/Torrent_Compact_White_TG_1-1000x1000.jpg",
        "images": [
            "https://www.fractal-design.com/app/uploads/2022/01/Torrent_Compact_White_TG_1-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2022/01/Torrent_Compact_White_TG_2-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2022/01/Torrent_Compact_White_TG_3-1000x1000.jpg"
        ]
    },
    {
        "name": "Fractal Design Pop Air RGB",
        "price": 3199.0,
        "type": "ATX Mid Tower Mesh",
        "color": "Black / Cyan Core",
        "side_panel": "Tempered Glass",
        "external_volume": 43.4,
        "max_gpu_length": 405,
        "max_cpu_cooler_height": 170,
        "max_radiator_size": 280,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch + Gizli Çekmece",
        "radiator_support": "Ön: 280mm / Üst: 240mm",
        "included_fans": "3x Aspect 12 RGB PWM Fan",
        "front_panel_usb": "2x USB 3.0, Audio (Opsiyonel Type-C)",
        "imageUrl": "https://www.fractal-design.com/app/uploads/2022/05/Pop_Air_RGB_Black_Cyan_Core_TG_1-1000x1000.jpg",
        "images": [
            "https://www.fractal-design.com/app/uploads/2022/05/Pop_Air_RGB_Black_Cyan_Core_TG_1-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2022/05/Pop_Air_RGB_Black_Cyan_Core_TG_2-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2022/05/Pop_Air_RGB_Black_Cyan_Core_TG_3-1000x1000.jpg"
        ]
    },
    {
        "name": "Fractal Design Ridge",
        "price": 4999.0,
        "type": "Console SFF Slim Mini ITX",
        "color": "Black / Fabric",
        "side_panel": "Perforated Steel & Kvadrat Kumaş Ön Yüzey",
        "external_volume": 12.6,
        "max_gpu_length": 335,
        "max_cpu_cooler_height": 70,
        "max_radiator_size": 280,
        "drive_bays": "4x 2.5 Inch SSD",
        "radiator_support": "Yan: 280mm (GPU takılmadığında)",
        "included_fans": "2x Aspect 140mm PWM İnce Fan",
        "front_panel_usb": "1x USB 3.1 Gen 2 Type-C, 2x USB 3.0, Audio",
        "imageUrl": "https://www.fractal-design.com/app/uploads/2022/10/Ridge_Black_1-1000x1000.jpg",
        "images": [
            "https://www.fractal-design.com/app/uploads/2022/10/Ridge_Black_1-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2022/10/Ridge_Black_2-1000x1000.jpg",
            "https://www.fractal-design.com/app/uploads/2022/10/Ridge_Black_3-1000x1000.jpg"
        ]
    },
    # Lian Li
    {
        "name": "Lian Li O11 Dynamic MINI",
        "price": 3999.0,
        "type": "Modular Dual Chamber Compact ATX",
        "color": "Black",
        "side_panel": "Tempered Glass & Alüminyum",
        "external_volume": 43.0,
        "max_gpu_length": 395,
        "max_cpu_cooler_height": 170,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 280mm / Alt: 360mm (SFX/SFX-L PSU Destekli)",
        "included_fans": "Dahili Fan Yok (Modüler Arka Panel)",
        "front_panel_usb": "1x USB 3.1 Type-C, 2x USB 3.0, HD Audio",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2020/11/o11d-mini-01.png",
        "images": [
            "https://lian-li.com/wp-content/uploads/2020/11/o11d-mini-01.png",
            "https://lian-li.com/wp-content/uploads/2020/11/o11d-mini-02.png",
            "https://lian-li.com/wp-content/uploads/2020/11/o11d-mini-03.png"
        ]
    },
    {
        "name": "Lian Li Lancool III RGB",
        "price": 5499.0,
        "type": "High End Airflow E-ATX",
        "color": "Black",
        "side_panel": "Dual 4mm Tempered Glass Hinged",
        "external_volume": 68.0,
        "max_gpu_length": 435,
        "max_cpu_cooler_height": 187,
        "max_radiator_size": 420,
        "drive_bays": "4x 3.5 Inch, 8x 2.5 Inch",
        "radiator_support": "Ön: 420mm / Üst: 420mm / Alt: 360mm",
        "included_fans": "4x 140mm ARGB PWM Fan",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, Audio",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2022/07/lancool-3-01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2022/07/lancool-3-01.webp",
            "https://lian-li.com/wp-content/uploads/2022/07/lancool-3-02.webp",
            "https://lian-li.com/wp-content/uploads/2022/07/lancool-3-03.webp"
        ]
    },
    {
        "name": "Lian Li DAN Cases A4-H2O",
        "price": 5299.0,
        "type": "Ultra SFF Mini ITX Liquid Ready",
        "color": "Silver Aluminum",
        "side_panel": "Fine Perforated Aluminum",
        "external_volume": 11.0,
        "max_gpu_length": 322,
        "max_cpu_cooler_height": 55,
        "max_radiator_size": 240,
        "drive_bays": "1x 2.5 Inch SSD",
        "radiator_support": "Üst: 240mm AIO Sıvı Soğutma (55mm toplam kalınlık)",
        "included_fans": "Dahili Fan Yok (PCIe 4.0 Riser Dahil)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 1x USB 3.0, Mic/Audio",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2022/02/A4-H2O-01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2022/02/A4-H2O-01.webp",
            "https://lian-li.com/wp-content/uploads/2022/02/A4-H2O-02.webp",
            "https://lian-li.com/wp-content/uploads/2022/02/A4-H2O-03.webp"
        ]
    },
    # NZXT
    {
        "name": "NZXT H7 Flow RGB (2024)",
        "price": 5299.0,
        "type": "High Airflow ATX Mid Tower",
        "color": "White",
        "side_panel": "Tempered Glass",
        "external_volume": 54.4,
        "max_gpu_length": 410,
        "max_cpu_cooler_height": 185,
        "max_radiator_size": 420,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Ön: 420mm / Üst: 360mm",
        "included_fans": "1x F360 RGB Core Tek Parça Ön Fan Kiti",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.2 Gen 1, Audio",
        "imageUrl": "https://nzxt.com/assets/cms/34299/1718137351-h7-flow-rgb-white-primary.png",
        "images": [
            "https://nzxt.com/assets/cms/34299/1718137351-h7-flow-rgb-white-primary.png",
            "https://nzxt.com/assets/cms/34299/1718137367-h7-flow-rgb-white-angle.png",
            "https://nzxt.com/assets/cms/34299/1718137384-h7-flow-rgb-white-open.png"
        ]
    },
    {
        "name": "NZXT H5 Flow (2024)",
        "price": 3299.0,
        "type": "Compact ATX Mid Tower",
        "color": "Black",
        "side_panel": "Tempered Glass",
        "external_volume": 41.6,
        "max_gpu_length": 410,
        "max_cpu_cooler_height": 170,
        "max_radiator_size": 360,
        "drive_bays": "1x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Ön: 360mm / Üst: 240mm",
        "included_fans": "2x F120Q Kasa Fanı",
        "front_panel_usb": "1x USB 3.2 Type-C, 1x USB 3.2 Type-A, Audio",
        "imageUrl": "https://nzxt.com/assets/cms/34299/1718137108-h5-flow-black-primary.png",
        "images": [
            "https://nzxt.com/assets/cms/34299/1718137108-h5-flow-black-primary.png",
            "https://nzxt.com/assets/cms/34299/1718137125-h5-flow-black-angle.png",
            "https://nzxt.com/assets/cms/34299/1718137142-h5-flow-black-open.png"
        ]
    },
    # Phanteks & Hyte
    {
        "name": "Phanteks NV7",
        "price": 6899.0,
        "type": "Full Tower Showcase E-ATX",
        "color": "Matte White",
        "side_panel": "Near-Seamless Panoramic Glass",
        "external_volume": 85.0,
        "max_gpu_length": 450,
        "max_cpu_cooler_height": 185,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 6x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Alt: 360mm / Arka: 240mm",
        "included_fans": "Dahili Fan Yok (Entegre Gizli Kablo Yönetim Kanalları)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, D-RGB Kontroller",
        "imageUrl": "https://www.phanteks.com/images/product/NV-Series/NV7/White/NV7-1.jpg",
        "images": [
            "https://www.phanteks.com/images/product/NV-Series/NV7/White/NV7-1.jpg",
            "https://www.phanteks.com/images/product/NV-Series/NV7/White/NV7-2.jpg",
            "https://www.phanteks.com/images/product/NV-Series/NV7/White/NV7-3.jpg"
        ]
    },
    {
        "name": "Hyte Y60",
        "price": 7499.0,
        "type": "3-Piece Panoramic Glass ATX",
        "color": "Red / Black",
        "side_panel": "3-Piece Panoramic Tempered Glass",
        "external_volume": 60.0,
        "max_gpu_length": 375,
        "max_cpu_cooler_height": 160,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 4x 2.5 Inch",
        "radiator_support": "Yan: 280mm / Üst: 360mm / Alt: 2x 140mm (Dikey GPU Standart)",
        "included_fans": "3x Flow FE12 120mm Fan + PCIe 4.0 Luxury Riser",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, Audio",
        "imageUrl": "https://hyte.com/media/catalog/product/y/6/y60-red-hero.png",
        "images": [
            "https://hyte.com/media/catalog/product/y/6/y60-red-hero.png",
            "https://hyte.com/media/catalog/product/y/6/y60-red-side.png",
            "https://hyte.com/media/catalog/product/y/6/y60-red-angle.png"
        ]
    },
    {
        "name": "Hyte Y40",
        "price": 5499.0,
        "type": "Panoramic Compact Mid Tower ATX",
        "color": "Snow White",
        "side_panel": "2-Piece Panoramic Tempered Glass",
        "external_volume": 49.0,
        "max_gpu_length": 422,
        "max_cpu_cooler_height": 183,
        "max_radiator_size": 360,
        "drive_bays": "1x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Yan: 280mm / Alt: 120mm",
        "included_fans": "2x 120mm Fan + PCIe 4.0 Riser Dahil",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, Combo Audio",
        "imageUrl": "https://hyte.com/media/catalog/product/y/4/y40-snow-white-hero.png",
        "images": [
            "https://hyte.com/media/catalog/product/y/4/y40-snow-white-hero.png",
            "https://hyte.com/media/catalog/product/y/4/y40-snow-white-side.png",
            "https://hyte.com/media/catalog/product/y/4/y40-snow-white-angle.png"
        ]
    },
    # ASUS & Cooler Master
    {
        "name": "ASUS TUF Gaming GT502",
        "price": 5799.0,
        "type": "Dual Chamber ATX Mid Tower",
        "color": "Black",
        "side_panel": "Tempered Glass & Dayanıklı Kumaş Taşıma Kolları",
        "external_volume": 62.0,
        "max_gpu_length": 400,
        "max_cpu_cooler_height": 163,
        "max_radiator_size": 360,
        "drive_bays": "4x 3.5/2.5 Inch Combo",
        "radiator_support": "Üst: 360mm / Yan: 360mm / Arka: 120mm",
        "included_fans": "Dahili Fan Yok (Gelişmiş Çift Bölmeli Soğutma)",
        "front_panel_usb": "1x USB 3.2 Gen 2 Type-C, 2x USB 3.0, LED Tuşu",
        "imageUrl": "https://dlcdnwebimgs.asus.com/gain/49F44E72-D46C-4965-B474-9A4B3A8B3B5C/w1000/h732",
        "images": [
            "https://dlcdnwebimgs.asus.com/gain/49F44E72-D46C-4965-B474-9A4B3A8B3B5C/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/B2A2EB55-3844-4BCF-8AE5-4A97ACBB89FF/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/E30F135A-F56D-4573-A3B4-846DC22EB746/w1000/h732"
        ]
    },
    {
        "name": "Cooler Master NR200P V2",
        "price": 4299.0,
        "type": "Compact Mini ITX SFF",
        "color": "Black",
        "side_panel": "Tempered Glass & Vented Panel (İkisi de Kutu İçinde)",
        "external_volume": 18.2,
        "max_gpu_length": 356,
        "max_cpu_cooler_height": 67,
        "max_radiator_size": 280,
        "drive_bays": "1x 3.5 Inch, 2x 2.5 Inch",
        "radiator_support": "Üst: 280mm / 240mm AIO Sıvı Soğutma Desteği",
        "included_fans": "1x SickleFlow 120mm İnce Üst Fan + PCIe 4.0 Dikey Riser",
        "front_panel_usb": "1x USB 3.2 Gen 2x2 Type-C (20Gbps), 2x USB 3.0, Audio",
        "imageUrl": "https://coolermaster.egnyte.com/dl/vJ857Tz7xP/NR200P_V2_Black_01.png",
        "images": [
            "https://coolermaster.egnyte.com/dl/vJ857Tz7xP/NR200P_V2_Black_01.png",
            "https://coolermaster.egnyte.com/dl/4ZkL6bE0w1/NR200P_V2_Black_02.png",
            "https://coolermaster.egnyte.com/dl/2n5v6j8m1L/NR200P_V2_Black_03.png"
        ]
    },
    {
        "name": "Jonsbo D31 MESH Screen",
        "price": 4499.0,
        "type": "Micro ATX with 8-inch HD LCD Screen",
        "color": "Black",
        "side_panel": "Tempered Glass & 8 inç 1280x800 Entegre Donanım Monitörü",
        "external_volume": 31.3,
        "max_gpu_length": 400,
        "max_cpu_cooler_height": 168,
        "max_radiator_size": 360,
        "drive_bays": "2x 3.5 Inch, 3x 2.5 Inch",
        "radiator_support": "Üst: 360mm / Alt: 360mm / Arka: 120mm",
        "included_fans": "Dahili Fan Yok",
        "front_panel_usb": "1x Type-C, 1x USB 3.0, Audio",
        "imageUrl": "https://www.jonsbo.com/uploads/221124/1-22112411512N92.png",
        "images": [
            "https://www.jonsbo.com/uploads/221124/1-22112411512N92.png",
            "https://www.jonsbo.com/uploads/221124/1-2211241151433E.png",
            "https://www.jonsbo.com/uploads/221124/1-221124115201c8.png"
        ]
    }
]

ADDITIONAL_COOLERS = [
    {
        "name": "Thermalright Phantom Spirit 120 EVO",
        "price": 2199.0,
        "size": None,
        "type": "Air",
        "color": "Full All-Black Stealth",
        "height": 157,
        "fan_rpm": "2150 RPM ± 10% (TL-K12)",
        "noise_level": 27.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851", "LGA1200"],
        "max_tdp": 290,
        "radiator_dimensions": "125 x 110 x 157 mm (7x 6mm AGHP Gen 4.0 Siyah Nikel Borular)",
        "fan_airflow": "69.0 CFM",
        "imageUrl": "https://www.thermalright.com/wp-content/uploads/2023/12/PS120-EVO_1.jpg",
        "images": [
            "https://www.thermalright.com/wp-content/uploads/2023/12/PS120-EVO_1.jpg",
            "https://www.thermalright.com/wp-content/uploads/2023/12/PS120-EVO_2.jpg",
            "https://www.thermalright.com/wp-content/uploads/2023/12/PS120-EVO_3.jpg"
        ]
    },
    {
        "name": "Thermalright Frozen Warframe 360 Black",
        "price": 3899.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "2000 RPM (TL-P12-S)",
        "noise_level": 22.1,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 340,
        "radiator_dimensions": "397 x 120 x 27 mm (2.4 inç 320x240 LCD Ekran)",
        "fan_airflow": "70.84 CFM",
        "imageUrl": "https://www.thermalright.com/wp-content/uploads/2023/10/Frozen-Warframe-360-BLACK_1.jpg",
        "images": [
            "https://www.thermalright.com/wp-content/uploads/2023/10/Frozen-Warframe-360-BLACK_1.jpg",
            "https://www.thermalright.com/wp-content/uploads/2023/10/Frozen-Warframe-360-BLACK_2.jpg",
            "https://www.thermalright.com/wp-content/uploads/2023/10/Frozen-Warframe-360-BLACK_3.jpg"
        ]
    },
    {
        "name": "Thermalright AXP90-X47 Full Copper",
        "price": 1899.0,
        "size": None,
        "type": "Air",
        "color": "Pure Raw Copper",
        "height": 47,
        "fan_rpm": "2700 RPM (TL-9015R)",
        "noise_level": 22.4,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 160,
        "radiator_dimensions": "94.5 x 95 x 47 mm (%100 Saf Bakır Mini ITX Kule)",
        "fan_airflow": "42.58 CFM",
        "imageUrl": "https://www.thermalright.com/wp-content/uploads/2021/04/AXP90-X47-Full-1.jpg",
        "images": [
            "https://www.thermalright.com/wp-content/uploads/2021/04/AXP90-X47-Full-1.jpg",
            "https://www.thermalright.com/wp-content/uploads/2021/04/AXP90-X47-Full-2.jpg",
            "https://www.thermalright.com/wp-content/uploads/2021/04/AXP90-X47-Full-3.jpg"
        ]
    },
    {
        "name": "Arctic Liquid Freezer III 360 A-RGB (White)",
        "price": 4299.0,
        "size": 360,
        "type": "Liquid",
        "color": "White",
        "height": 65,
        "fan_rpm": "200-2000 RPM (P12 PWM A-RGB)",
        "noise_level": 22.5,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 320,
        "radiator_dimensions": "398 x 120 x 38 mm (Ekstra Kalın 38mm Beyaz Radyatör & VRM Fanı)",
        "fan_airflow": "48.8 CFM",
        "imageUrl": "https://www.arctic.de/media/4e/f2/31/1707987823/Liquid_Freezer_III_360_A-RGB_White_G00.jpg",
        "images": [
            "https://www.arctic.de/media/4e/f2/31/1707987823/Liquid_Freezer_III_360_A-RGB_White_G00.jpg",
            "https://www.arctic.de/media/4e/f2/31/1707987823/Liquid_Freezer_III_360_A-RGB_White_G02.jpg",
            "https://www.arctic.de/media/4e/f2/31/1707987823/Liquid_Freezer_III_360_A-RGB_White_G04.jpg"
        ]
    },
    {
        "name": "Arctic Freezer 36 A-RGB Black",
        "price": 1499.0,
        "size": None,
        "type": "Air",
        "color": "Black",
        "height": 159,
        "fan_rpm": "200-2000 RPM (2x P12 PWM A-RGB Push-Pull)",
        "noise_level": 22.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 220,
        "radiator_dimensions": "104 x 126 x 159 mm (Intel LGA1700/1851 İletişim Çerçevesi Dahil)",
        "fan_airflow": "48.8 CFM",
        "imageUrl": "https://www.arctic.de/media/31/d1/8a/1707987821/Freezer_36_A-RGB_Black_G00.jpg",
        "images": [
            "https://www.arctic.de/media/31/d1/8a/1707987821/Freezer_36_A-RGB_Black_G00.jpg",
            "https://www.arctic.de/media/31/d1/8a/1707987821/Freezer_36_A-RGB_Black_G02.jpg",
            "https://www.arctic.de/media/31/d1/8a/1707987821/Freezer_36_A-RGB_Black_G03.jpg"
        ]
    },
    {
        "name": "NZXT Kraken Elite 240 RGB",
        "price": 8499.0,
        "size": 240,
        "type": "Liquid",
        "color": "Black",
        "height": 53,
        "fan_rpm": "500-1800 RPM (F120 RGB Core)",
        "noise_level": 23.5,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 280,
        "radiator_dimensions": "275 x 123 x 30 mm (2.36 inç 640x640 TFT LCD)",
        "fan_airflow": "78.86 CFM",
        "imageUrl": "https://nzxt.com/assets/cms/34299/1681285038-kraken-elite-240-rgb-black-hero.png",
        "images": [
            "https://nzxt.com/assets/cms/34299/1681285038-kraken-elite-240-rgb-black-hero.png",
            "https://nzxt.com/assets/cms/34299/1681285055-kraken-elite-240-rgb-black-top.png",
            "https://nzxt.com/assets/cms/34299/1681285072-kraken-elite-240-rgb-black-screen.png"
        ]
    },
    {
        "name": "Corsair A115 Dual Tower Air Cooler",
        "price": 4399.0,
        "size": None,
        "type": "Air",
        "color": "Black",
        "height": 165,
        "fan_rpm": "400-1600 RPM (2x AF140 ELITE)",
        "noise_level": 25.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 270,
        "radiator_dimensions": "153 x 155 x 164.8 mm (6x 6mm Isı Borusu & Kaydırmalı Fan Rayı)",
        "fan_airflow": "84.5 CFM",
        "imageUrl": "https://www.corsair.com/medias/sys_master/images/images/h6a/h7a/10542387134494/CT-9010011-WW/Gallery/A115_BLACK_01.png",
        "images": [
            "https://www.corsair.com/medias/sys_master/images/images/h6a/h7a/10542387134494/CT-9010011-WW/Gallery/A115_BLACK_01.png",
            "https://www.corsair.com/medias/sys_master/images/images/h6a/h7a/10542387134494/CT-9010011-WW/Gallery/A115_BLACK_02.png",
            "https://www.corsair.com/medias/sys_master/images/images/h6a/h7a/10542387134494/CT-9010011-WW/Gallery/A115_BLACK_03.png"
        ]
    },
    {
        "name": "Lian Li Galahad II LCD 360 (White)",
        "price": 10999.0,
        "size": 360,
        "type": "Liquid",
        "color": "White",
        "height": 55,
        "fan_rpm": "300-2450 RPM (UNI FAN SL-INF)",
        "noise_level": 28.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 350,
        "radiator_dimensions": "399.5 x 120 x 32 mm (2.88 inç 480x480 IPS LCD Ekran)",
        "fan_airflow": "79.9 CFM (Asetek 8. Nesil Pompa)",
        "imageUrl": "https://lian-li.com/wp-content/uploads/2023/10/GA-II-LCD-01.webp",
        "images": [
            "https://lian-li.com/wp-content/uploads/2023/10/GA-II-LCD-01.webp",
            "https://lian-li.com/wp-content/uploads/2023/10/GA-II-LCD-03.webp",
            "https://lian-li.com/wp-content/uploads/2023/10/GA-II-LCD-05.webp"
        ]
    },
    {
        "name": "DeepCool AK620 Digital",
        "price": 3299.0,
        "size": None,
        "type": "Air",
        "color": "Black",
        "height": 162,
        "fan_rpm": "500-1850 RPM (2x FK120 PWM)",
        "noise_level": 28.0,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 260,
        "radiator_dimensions": "129 x 138 x 162 mm (Gerçek Zamanlı Donanım Sıcaklık Ekranı)",
        "fan_airflow": "68.99 CFM",
        "imageUrl": "https://www.deepcool.com/download/image/AK620_Digital_01.png",
        "images": [
            "https://www.deepcool.com/download/image/AK620_Digital_01.png",
            "https://www.deepcool.com/download/image/AK620_Digital_02.png",
            "https://www.deepcool.com/download/image/AK620_Digital_03.png"
        ]
    },
    {
        "name": "DeepCool LS720 SE Digital",
        "price": 4899.0,
        "size": 360,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "500-2250 RPM",
        "noise_level": 32.9,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 300,
        "radiator_dimensions": "402 x 120 x 27 mm (Entegre Dijital CPU Sıcaklık Göstergesi)",
        "fan_airflow": "85.85 CFM",
        "imageUrl": "https://www.deepcool.com/download/image/LS720_SE_Digital_01.png",
        "images": [
            "https://www.deepcool.com/download/image/LS720_SE_Digital_01.png",
            "https://www.deepcool.com/download/image/LS720_SE_Digital_02.png",
            "https://www.deepcool.com/download/image/LS720_SE_Digital_03.png"
        ]
    },
    {
        "name": "ASUS ProArt LC 420",
        "price": 11999.0,
        "size": 420,
        "type": "Liquid",
        "color": "Minimalist Black",
        "height": 60,
        "fan_rpm": "450-2000 RPM (3x Noctua NF-A14 IndustrialPPC-2000 PWM)",
        "noise_level": 31.5,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 380,
        "radiator_dimensions": "456 x 140 x 30 mm (İçerik Üretici Sessiz & Güçlü 420mm)",
        "fan_airflow": "107.0 CFM (Alphacool Motor)",
        "imageUrl": "https://dlcdnwebimgs.asus.com/gain/43E19BC4-28B9-4596-857A-3B64197E6390/w1000/h732",
        "images": [
            "https://dlcdnwebimgs.asus.com/gain/43E19BC4-28B9-4596-857A-3B64197E6390/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/3D7A2542-E93C-4310-85A3-9F939766946A/w1000/h732",
            "https://dlcdnwebimgs.asus.com/gain/D91C281B-87A4-48FE-996F-CF2BCF1B9C49/w1000/h732"
        ]
    },
    {
        "name": "be quiet! Dark Rock Pro 5",
        "price": 3899.0,
        "size": None,
        "type": "Air",
        "color": "Black",
        "height": 168,
        "fan_rpm": "1500 / 2000 RPM",
        "noise_level": 23.3,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 270,
        "radiator_dimensions": "145 x 136 x 168 mm (7x Yüksek Verimli Bakır Isı Borusu)",
        "fan_airflow": "68.5 CFM",
        "imageUrl": "https://www.bequiet.com/admin/ImageServer.php?ID=43101&ext=jpg",
        "images": [
            "https://www.bequiet.com/admin/ImageServer.php?ID=43101&ext=jpg",
            "https://www.bequiet.com/admin/ImageServer.php?ID=43102&ext=jpg",
            "https://www.bequiet.com/admin/ImageServer.php?ID=43103&ext=jpg"
        ]
    },
    {
        "name": "Cooler Master MasterLiquid 240L Core ARGB",
        "price": 2899.0,
        "size": 240,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "650-1750 RPM",
        "noise_level": 27.2,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 260,
        "radiator_dimensions": "277 x 119.6 x 27.2 mm (Geliştirilmiş Çift Odacıklı Gen S Pompa)",
        "fan_airflow": "71.93 CFM",
        "imageUrl": "https://coolermaster.egnyte.com/dl/vJ857Tz7xP/Core_240L_01.png",
        "images": [
            "https://coolermaster.egnyte.com/dl/vJ857Tz7xP/Core_240L_01.png",
            "https://coolermaster.egnyte.com/dl/4ZkL6bE0w1/Core_240L_02.png",
            "https://coolermaster.egnyte.com/dl/2n5v6j8m1L/Core_240L_03.png"
        ]
    },
    {
        "name": "ID-Cooling FrostFlow X 240",
        "price": 1999.0,
        "size": 240,
        "type": "Liquid",
        "color": "Black",
        "height": 52,
        "fan_rpm": "700-1800 RPM",
        "noise_level": 35.2,
        "sockets": ["AM5", "AM4", "LGA1700", "LGA1851"],
        "max_tdp": 250,
        "radiator_dimensions": "276 x 120 x 27 mm",
        "fan_airflow": "74.5 CFM",
        "imageUrl": "https://www.idcooling.com/files/product/image/20191024104928_4057.jpg",
        "images": [
            "https://www.idcooling.com/files/product/image/20191024104928_4057.jpg",
            "https://www.idcooling.com/files/product/image/20191024104928_4058.jpg",
            "https://www.idcooling.com/files/product/image/20191024104928_4059.jpg"
        ]
    }
]

def run():
    print("=== Mass Expansion of Global Cases and Coolers ===")
    
    # 1. Expand Cases
    case_file = os.path.join(DATA_CATALOG, "case.json")
    with open(case_file, "r", encoding="utf-8") as f:
        cases = json.load(f)
    case_names = {c.get("name", "").strip().lower() for c in cases}
    
    added_cases = 0
    for nc in ADDITIONAL_CASES:
        if nc["name"].strip().lower() not in case_names:
            cases.append(nc)
            case_names.add(nc["name"].strip().lower())
            added_cases += 1
            
    print(f"Total Cases: {len(cases)} (+{added_cases})")
    
    for p in [
        os.path.join(DATA_CATALOG, "case.json"),
        os.path.join(COMPOSE_DIR, "case.json")
    ]:
        with open(p, "w", encoding="utf-8") as f:
            json.dump(cases, f, indent=2, ensure_ascii=False)
        print("Updated:", p)
        
    # 2. Expand Coolers
    cooler_file = os.path.join(DATA_CATALOG, "cpu-cooler.json")
    with open(cooler_file, "r", encoding="utf-8") as f:
        coolers = json.load(f)
    cooler_names = {c.get("name", "").strip().lower() for c in coolers}
    
    added_coolers = 0
    for nc in ADDITIONAL_COOLERS:
        if nc["name"].strip().lower() not in cooler_names:
            coolers.append(nc)
            cooler_names.add(nc["name"].strip().lower())
            added_coolers += 1
            
    print(f"Total Coolers: {len(coolers)} (+{added_coolers})")
    
    for p in [
        os.path.join(DATA_CATALOG, "cooler.json"),
        os.path.join(DATA_CATALOG, "cpu-cooler.json"),
        os.path.join(COMPOSE_DIR, "cooler.json"),
        os.path.join(COMPOSE_DIR, "cpu-cooler.json")
    ]:
        with open(p, "w", encoding="utf-8") as f:
            json.dump(coolers, f, indent=2, ensure_ascii=False)
        print("Updated:", p)

if __name__ == "__main__":
    run()
