#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/catalog_mice_extra.py
36 Modern 2022-2026 Gaming Mice to bring total Mice to 106+
Standards: EloShapes / Rtings
"""

EXTRA_MICE_DATA = [
    # --- VAXEE (4 items) ---
    ("mouse_vaxee_zygen_np_01s_wireless", "VAXEE ZYGEN NP-01S Wireless 4K Espor Faresi", "ZYGEN NP-01S Wireless", "PixArt PAW3395 (Espor Kalibre)", 26000, 4000, "Huano 80M Özel Kalibre Espor Switch", 68, "2.4GHz VAXEE Esports Wireless / USB-C", 100, "Espor Odaklı MCU", 0.45, "Hibrit Ergonomik/Simetrik Pençe", 4699.0, "Wraith Esports", 96, "S+"),
    ("mouse_vaxee_outset_ax_wireless", "VAXEE Outset AX Wireless Ergonomik Espor Faresi", "Outset AX Wireless", "PixArt PAW3395", 26000, 4000, "Huano 80M Espor Switch", 73, "2.4GHz Esports Wireless / USB-C", 100, "VAXEE MCU", 0.45, "Yüksek Ergonomik Sağ El Avuç İçi", 4699.0, "Wraith Esports", 95, "S"),
    ("mouse_vaxee_xe_wireless", "VAXEE XE Wireless Çift Yönlü Espor Faresi", "XE Wireless", "PixArt PAW3395", 26000, 4000, "Huano 70M Espor", 67, "2.4GHz Esports Wireless / USB-C", 90, "VAXEE MCU", 0.5, "Alçak Profil Simetrik", 4499.0, "Wraith Esports", 95, "S"),
    ("mouse_vaxee_np_01_wireless", "VAXEE NP-01 Wireless Klasik Boyutlu Espor Faresi", "NP-01 Wireless", "PixArt PAW3395", 26000, 4000, "Huano 80M", 72, "2.4GHz Esports / USB-C", 100, "VAXEE MCU", 0.45, "Ergonomik Hibrit Pençe", 4699.0, "Wraith Esports", 95, "S"),

    # --- Corsair (5 items) ---
    ("mouse_corsair_m75_air_wireless", "Corsair M75 AIR WIRELESS Ultra Hafif 60g 2000Hz Espor Faresi", "M75 AIR Wireless", "CORSAIR MARKSMAN 26K (26000 DPI / 650 IPS)", 26000, 2000, "CORSAIR QUICKSTRIKE Optik Switch (100M)", 60, "SLIPSTREAM Kablosuz 2000Hz / Bluetooth / USB-C", 100, "CORSAIR MCU", 0.5, "Simetrik Espor Pençe/Avuç", 3899.0, "İtopya", 92, "A+"),
    ("mouse_corsair_sabre_rgb_pro_wireless", "Corsair Sabre RGB PRO Wireless Champion Series 79g Espor Faresi", "Sabre RGB PRO Wireless", "CORSAIR MARKSMAN 26K", 26000, 2000, "Omron Mekanik 50M", 79, "SLIPSTREAM Kablosuz / Bluetooth / USB-C", 90, "CORSAIR AXON", 0.8, "Ergonomik Sağ El FPS/MOBA", 2899.0, "Hepsiburada", 89, "A"),
    ("mouse_corsair_scimitar_elite_wireless", "Corsair Scimitar Elite Wireless 16 Tuşlu MMO/MOBA Faresi", "Scimitar Elite Wireless", "CORSAIR MARKSMAN 26K", 26000, 2000, "Optik Switch 100M", 114, "SLIPSTREAM / Bluetooth / Kayar 12 Tuşlu Yan Tuş Takımı", 150, "CORSAIR MCU", 1.0, "Geniş Ergonomik MMO", 4999.0, "İtopya", 91, "A+"),
    ("mouse_corsair_katar_pro_wireless", "Corsair Katar PRO Wireless Kompakt Hafif Oyuncu Faresi", "Katar PRO Wireless", "PixArt PMW3335 (10000 DPI)", 10000, 1000, "Omron 20M", 96, "SLIPSTREAM 2.4G / Bluetooth (Tek AA Pil 135 Saat)", 135, "Corsair MCU", 1.8, "Simetrik Kompakt Parmak Ucu", 1399.0, "Amazon TR", 80, "B"),
    ("mouse_corsair_darkstar_wireless", "Corsair Darkstar Wireless RGB 15 Tuşlu MMO/RPG/FPS Kablosuz Fare", "Darkstar Wireless", "CORSAIR MARKSMAN 26K", 26000, 2000, "CORSAIR QUICKSTRIKE Optik", 96, "SLIPSTREAM Kablosuz / BT / 6 Düğmeli Yan Daire", 80, "CORSAIR MCU", 0.9, "Ergonomik Çok Amaçlı", 5499.0, "İtopya", 90, "A+"),

    # --- HyperX (4 items) ---
    ("mouse_hyperx_pulsefire_haste_2_wireless", "HyperX Pulsefire Haste 2 Wireless 61g Katı Gövde 100 Saat Pil Faresi", "Pulsefire Haste 2 Wireless", "HyperX 26K Sensör (26000 DPI / 650 IPS)", 26000, 1000, "HyperX Switch (100 Milyon Tıklama)", 61, "2.4GHz Kablosuz / Bluetooth 5.0 / USB-C", 100, "HyperX MCU", 0.7, "Simetrik Düşük Profil Pençe/Parmak", 2999.0, "Hepsiburada", 91, "A+"),
    ("mouse_hyperx_pulsefire_haste_2_mini", "HyperX Pulsefire Haste 2 Mini Wireless Ultra Hafif 59g Espor Faresi", "Pulsefire Haste 2 Mini", "HyperX 26K", 26000, 1000, "HyperX Switch 100M", 59, "2.4GHz / Bluetooth / USB-C", 100, "HyperX MCU", 0.7, "Kompakt Simetrik Parmak Ucu", 3199.0, "İtopya", 91, "A+"),
    ("mouse_hyperx_pulsefire_haste_2_kablolu", "HyperX Pulsefire Haste 2 8000Hz Ultra Hafif 53g Espor Faresi", "Pulsefire Haste 2 Wired", "HyperX 26K", 26000, 8000, "HyperX Switch 100M", 53, "HyperFlex 2 Esnek Kablo (8000Hz)", None, "HyperX MCU", 0.25, "Simetrik Ultra Hafif", 1999.0, "Hepsiburada", 93, "S"),
    ("mouse_hyperx_pulsefire_dart_wireless", "HyperX Pulsefire Dart Kablosuz Qi Şarj Destekli Ergonomik Fare", "Pulsefire Dart", "PixArt PMW3389 (16000 DPI)", 16000, 1000, "Omron 50M", 110, "2.4GHz Kablosuz / Qi Sertifikalı Kablosuz Şarj", 50, "HyperX MCU", 1.4, "Ergonomik Yastıklı Yanlar", 3499.0, "Vatan", 84, "B+"),

    # --- Roccat / Turtle Beach (3 items) ---
    ("mouse_turtle_beach_pure_air", "Turtle Beach Pure Air Ultra Hafif 54g Ergonomik Kablosuz Fare", "Pure Air", "Owl-Eye 26K Optik Sensör", 26000, 1000, "TITAN Optical Switch (100M)", 54, "Stellar Wireless 2.4G / Bluetooth / PhantomFlex USB-C", 125, "Nordic", 0.6, "Ergonomik Sağ El Avuç İçi", 3499.0, "Amazon TR", 92, "A+"),
    ("mouse_roccat_kone_xp_air", "Roccat Kone XP Air 3D RGB Aydınlatmalı Hızlı Şarj Yuvalı Kablosuz Fare", "Kone XP Air", "Owl-Eye 19K", 19000, 1000, "TITAN Switch Optical", 99, "Stellar Wireless / Bluetooth / RGB Manyetik Şarj Standı", 100, "Roccat MCU", 0.9, "Ergonomik 15 Tuşlu 29 Fonksiyonlu", 4899.0, "İtopya", 90, "A+"),
    ("mouse_roccat_burst_pro_air", "Roccat Burst Pro Air Su ve Toz Geçirmez Petekli Kablosuz Espor Faresi", "Burst Pro Air", "Owl-Eye 19K", 19000, 1000, "TITAN Switch Optical", 81, "Stellar Wireless / Bluetooth / Bionic Shell IP54", 100, "Roccat MCU", 1.0, "Simetrik Simetrik Pençe", 2499.0, "Amazon TR", 86, "A"),

    # --- Keychron (3 items) ---
    ("mouse_keychron_m3_mini_4k", "Keychron M3 Mini 4K Kablosuz Espor Faresi (PAW3395 / 4000Hz)", "M3 Mini 4K", "PixArt PAW3395 (26000 DPI)", 26000, 4000, "Huano 80M Blue Shell Pink Dot", 55, "2.4GHz 4K / Bluetooth 5.1 / USB-C", 70, "Nordic nRF52840", 0.5, "Kompakt Simetrik Claw/Fingertip", 2799.0, "Wraith Esports", 93, "S"),
    ("mouse_keychron_m4_4k_ultra_light", "Keychron M4 4K 35g Dünyanın En Küçük Parmak Ucu (Fingertip) Espor Faresi", "M4 4K Ultra-Light", "PixArt PAW3395", 26000, 4000, "Huano 80M Micro Switch", 35, "2.4GHz 4K Dongle Dahil / Type-C", 40, "Nordic nRF52840", 0.35, "Saf Parmak Ucu (Fingertip Only)", 2999.0, "Wraith Esports", 95, "S"),
    ("mouse_keychron_m6_wireless", "Keychron M6 Kablosuz Ergonomik Çift Kaydırma Tekerlekli Fare", "M6 Wireless", "PixArt PAW3395", 26000, 1000, "Huano 80M", 78, "2.4GHz / Bluetooth 5.1 / Sonsuz Dikey ve Yatay Çift Tekerlek", 80, "Keychron MCU", 0.9, "Ergonomik Başparmak Dayamalı Üretkenlik/Oyun", 2899.0, "Wraith Esports", 90, "A+"),

    # --- Waizowl & Fantech (4 items) ---
    ("mouse_waizowl_ogm_cloud_4k", "Waizowl OGM Cloud 4K Efsane Sensei Şekilli Espor Faresi", "OGM Cloud 4K", "PixArt PAW3395", 26000, 4000, "Huano Transparent Blue Shell Pink Dot", 55, "2.4GHz 4K / Bluetooth / Paracord Type-C", 80, "Nordic nRF52840", 0.45, "Efsanevi Sensei Şekli Simetrik", 3699.0, "Wraith Esports", 95, "S"),
    ("mouse_waizowl_ogm_pro_ink_feather", "Waizowl OGM Pro Ink Feather Tüy Sanatı Özel Kaplama Kablosuz Fare", "OGM Pro Ink Feather", "PixArt PAW3395", 26000, 1000, "Huano Blue Pink Dot", 68, "2.4G / BT / Tüy Tüy Yumuşak Dokulu Gövde", 70, "Nordic nRF52840", 0.8, "Ergonomik Sağ El Avuç İçi", 3499.0, "Wraith Esports", 91, "A+"),
    ("mouse_fantech_helios_ii_pro_s_8k", "Fantech Helios II Pro S XD3V3 8K Polling TTC Optik Espor Faresi", "Helios II Pro S 8K", "PixArt PAW3395", 26000, 8000, "TTC Nihil Şeffaf Optik Switch (100M)", 55, "2.4GHz 8000Hz Dongle Dahil / USB-C", 60, "Nordic nRF52840", 0.28, "Simetrik ZOWIE S2 Şekli", 3499.0, "Wraith Esports", 96, "S+"),
    ("mouse_fantech_aria_xd7", "Fantech Aria XD7 PAW3395 Yumurta Şekilli Espor Faresi", "Aria XD7", "PixArt PAW3395", 26000, 1000, "Huano Blue Shell Pink Dot 80M", 59, "2.4GHz / Bluetooth 5.0 / Type-C Paracord", 60, "CompX CX52850", 0.8, "Yumurta (Egg) Pençe/Parmak", 2499.0, "Wraith Esports", 89, "A"),

    # --- Attack Shark (4 items) ---
    ("mouse_attack_shark_x3_pro_8k", "Attack Shark X3 Pro 8K Ultra Hafif 49g PAW3395 Espor Faresi", "X3 Pro 8K", "PixArt PAW3395", 26000, 8000, "Huano Pink Dot 80M", 49, "2.4GHz 8K Dongle Dahil / BT / USB-C", 65, "Nordic nRF52840", 0.3, "Simetrik Kompakt Pençe", 2299.0, "Amazon TR", 93, "S"),
    ("mouse_attack_shark_x6_dock", "Attack Shark X6 Manyetik RGB Şarj Docklu 49g Kablosuz Fare", "X6 RGB Dock", "PixArt PAW3395", 26000, 1000, "Huano 80M", 49, "2.4GHz / Bluetooth / Manyetik Hızlı Şarj İstasyonu", 65, "Broadcom MCU", 0.8, "Simetrik Avuç/Pençe", 2199.0, "Amazon TR", 89, "A"),
    ("mouse_attack_shark_r1", "Attack Shark R1 59g F/P Canavarı PAW3311 Ergonomik Kablosuz Fare", "R1 Ergonomic", "PixArt PAW3311 (18000 DPI)", 18000, 1000, "Huano White Dot 20M", 59, "2.4G / Bluetooth / USB-C", 65, "Beken MCU", 1.2, "Ergonomik Sağ El Avuç İçi", 1199.0, "Amazon TR", 81, "B"),
    ("mouse_attack_shark_r3_magnesium_8k", "Attack Shark R3 Magnezyum Alaşım İskelet 46g 8000Hz Kablosuz Fare", "R3 Magnesium 8K", "PixArt PAW3395", 26000, 8000, "TTC Dustproof Gold Switch", 46, "2.4GHz 8K / Bluetooth / USB-C", 60, "Nordic nRF52840", 0.3, "Magnezyum Dış İskelet Delikli", 3799.0, "Amazon TR", 95, "S"),

    # --- Darmoshark & G-Wolves (6 items) ---
    ("mouse_darmoshark_m3s_varun_2k", "Darmoshark M3-S Varun 2K Polling 54g Espor Faresi", "M3-S Varun 2K", "PixArt PAW3395", 26000, 2000, "TTC Gold 60M Mikro Switch", 54, "2.4GHz 2K / USB-C Paracord", 80, "Nordic nRF52840", 0.6, "Simetrik Küçük/Orta Pençe", 2199.0, "Wraith Esports", 91, "A+"),
    ("mouse_darmoshark_m2_magnesium", "Darmoshark M2 Magnezyum Alaşım 37g 4000Hz Kablosuz Espor Faresi", "M2 Magnesium", "PixArt PAW3395", 26000, 4000, "TTC Dustproof Gold", 37, "2.4GHz 4K / USB-C", 60, "Nordic nRF52840", 0.4, "Magnezyum Alaşımlı CNC İskelet", 3999.0, "Wraith Esports", 96, "S+"),
    ("mouse_darmoshark_n3", "Darmoshark N3 Kablosuz Ergonomik 65g Sağ El Espor Faresi", "N3 Wireless", "PixArt PAW3395", 26000, 1000, "Kailh GM8.0 Black Mamba", 65, "2.4G / BT 5.0 / USB-C", 80, "Telink MCU", 1.0, "Ergonomik Sağ El", 1899.0, "Wraith Esports", 87, "A"),
    ("mouse_gwolves_htx_4k", "G-Wolves HTX 4K Ultra Hafif 36g Petekli Karbon Fiber Espor Faresi", "HTX 4K", "PixArt PAW3395", 26000, 4000, "Zippy DF3-P1 Espor Switch", 36, "2.4GHz 4K / USB-C Ultra Flex", 40, "Nordic nRF52840", 0.35, "Ultra Düşük Profil Simetrik Pençe", 4999.0, "Wraith Esports", 97, "S+"),
    ("mouse_gwolves_hts_plus_4k", "G-Wolves HTS Plus 4K 49g Yüksek Sırt Pençe Tutuşlu Espor Faresi", "HTS Plus 4K", "PixArt PAW3395", 26000, 4000, "Zippy 60M Switch", 49, "2.4GHz 4K / USB-C", 50, "Nordic nRF52840", 0.38, "Yüksek Sırt Agresif Pençe", 4799.0, "Wraith Esports", 96, "S+"),
    ("mouse_gwolves_hsk_pro_4k", "G-Wolves HSK Pro 4K 26g Dünyanın En Hafif Parmak Ucu Faresi", "HSK Pro 4K", "PixArt PAW3395", 26000, 4000, "Zippy Micro", 26, "2.4GHz 4K Dongle Dahil / Type-C", 20, "Nordic nRF52840", 0.25, "Saf Parmak Ucu Mikro Gövde (26g)", 5499.0, "Wraith Esports", 98, "S+"),

    # --- ASUS ROG Ekstra (3 items) ---
    ("mouse_asus_rog_harpe_ace_mini", "ASUS ROG Harpe Ace Mini 49g AimPoint Pro 42K 8000Hz Espor Faresi", "ROG Harpe Ace Mini", "ROG AimPoint Pro (42000 DPI / 750 IPS)", 42000, 8000, "ROG 100M Optik Switch", 49, "ROG SpeedNova 8K / Bluetooth / USB-C", 105, "ROG 8K Polling Booster", 0.22, "Kompakt Simetrik Espor Pençe", 5299.0, "İtopya", 98, "S+"),
    ("mouse_asus_rog_spatha_x", "ASUS ROG Spatha X Manyetik Şarj Yuvalı 12 Tuşlu Kablosuz MMO Faresi", "ROG Spatha X", "ROG 19K Optik Sensör", 19000, 1000, "ROG 70M Mekanik Switch (Hotswap)", 168, "2.4GHz Kablosuz / Manyetik Dikey Şarj Standı / USB-C", 67, "ROG MCU", 1.2, "Büyük Ergonomik Magnezyum Alaşım MMO", 6499.0, "İtopya", 89, "A"),
    ("mouse_asus_rog_chakram_x_origin", "ASUS ROG Chakram X Origin AimPoint 36K Programlanabilir Joystickli Fare", "ROG Chakram X Origin", "ROG AimPoint (36000 DPI / 650 IPS)", 36000, 8000, "ROG 70M Mekanik (Hotswap Yuvalı)", 123, "Tri-Mode 2.4G 8K / Bluetooth 5.2 / USB-C / Qi Kablosuz", 114, "ROG MCU", 0.4, "Ergonomik Analog Başparmak Joystickli", 5999.0, "İtopya", 92, "A+"),
]
