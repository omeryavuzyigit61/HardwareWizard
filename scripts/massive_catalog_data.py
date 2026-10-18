#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/massive_catalog_data.py
Master Encyclopedia of 2022-2026 Gaming Peripherals (400+ Products)
Standards: DisplaySpecifications, EloShapes, and Rtings
Price logic: Real Turkish store price if officially sold in TR; strictly None if global boutique/import.
"""

def get_all_monitors():
    items = []
    # 1. ASUS ROG Swift & Strix (18 items)
    asus_rog = [
        ("asus_rog_swift_oled_pg27aqdp", "ASUS ROG Swift OLED PG27AQDP 26.5 inç 480Hz 0.03ms QHD Espor Oyuncu Monitörü", "ROG Swift OLED PG27AQDP", "WOLED (Gen 3 Primary OLED)", 26.5, "2560x1440 (2K QHD 16:9)", 480, 0.03, 0.02, 111, "DisplayHDR True Black 400", "10-bit (1.07 Milyar Renk)", 450, 1300, "%99 DCI-P3, %137 sRGB", "G-Sync Compatible, FreeSync Premium Pro, VESA AdaptiveSync 480Hz", "Düz (Flat)", 46999.0, "İtopya", 100, "S+"),
        ("asus_rog_swift_oled_pg27aqdm", "ASUS ROG Swift OLED PG27AQDM 27 inç 240Hz 0.03ms QHD G-Sync Gaming Monitör", "ROG Swift OLED PG27AQDM", "WOLED", 26.5, "2560x1440 (2K QHD 16:9)", 240, 0.03, 0.03, 111, "HDR10 (1000 Nit Tepe)", "10-bit", 450, 1000, "%99 DCI-P3, %135 sRGB", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 35999.0, "İtopya", 98, "S+"),
        ("asus_rog_swift_oled_pg32ucdm", "ASUS ROG Swift OLED PG32UCDM 32 inç 4K 240Hz 0.03ms QD-OLED Gaming Monitör", "ROG Swift OLED PG32UCDM", "QD-OLED (Samsung Gen 3)", 31.5, "3840x2160 (4K UHD 16:9)", 240, 0.03, 0.03, 140, "DisplayHDR True Black 400, Dolby Vision", "10-bit", 450, 1000, "%99 DCI-P3, Type-C 90W PD, KVM", "G-Sync Compatible, FreeSync Premium Pro", "Düz (Flat)", 54999.0, "İtopya", 99, "S+"),
        ("asus_rog_swift_pro_pg248qp_540hz", "ASUS ROG Swift Pro PG248QP 24.1 inç 540Hz 0.2ms ULMB 2 Espor Monitörü", "ROG Swift Pro PG248QP", "Esports-TN (E-TN)", 24.1, "1920x1080 (FHD 16:9)", 540, 0.2, 0.1, 91, "DisplayHDR 400", "8-bit", 400, 450, "%125 sRGB", "NVIDIA G-Sync Donanımsal Çip + Reflex Analyzer + ULMB 2", "Düz (Flat)", 39999.0, "Tebilon", 97, "S+"),
        ("asus_rog_swift_oled_pg34wcdm", "ASUS ROG Swift OLED PG34WCDM 34 inç 240Hz 0.03ms UWQHD 800R Kavisli Monitör", "ROG Swift OLED PG34WCDM", "WOLED", 33.9, "3440x1440 (UWQHD 21:9)", 240, 0.03, 0.03, 110, "DisplayHDR True Black 400", "10-bit", 450, 1300, "%99 DCI-P3, USB-C 90W", "G-Sync Compatible, FreeSync Premium Pro", "800R Kavisli", 49999.0, "İtopya", 98, "S+"),
        ("asus_rog_swift_oled_pg49wcd", "ASUS ROG Swift OLED PG49WCD 49 inç 144Hz 0.03ms DQHD 1800R QD-OLED Monitör", "ROG Swift OLED PG49WCD", "QD-OLED", 49.0, "5120x1440 (Dual QHD 32:9)", 144, 0.03, 0.03, 109, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, Type-C 90W", "G-Sync Compatible, FreeSync Premium Pro", "1800R Kavisli", 59999.0, "Vatan", 97, "S+"),
        ("asus_rog_swift_pg27aqn_360hz", "ASUS ROG Swift 360Hz PG27AQN 27 inç 360Hz 1ms Ultrafast IPS 2K Espor Monitörü", "ROG Swift PG27AQN", "Ultrafast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 360, 1.0, 1.0, 109, "DisplayHDR 600", "10-bit", 400, 600, "%98 DCI-P3, Dual-Format 25 inç 1080p Modu", "NVIDIA G-Sync Donanım + Reflex Analyzer", "Düz (Flat)", 37999.0, "İtopya", 96, "S+"),
        ("asus_rog_swift_pg259qn_360hz", "ASUS ROG Swift 360Hz PG259QN 24.5 inç 360Hz 1ms Fast IPS FHD Espor Monitörü", "ROG Swift PG259QN", "Fast IPS", 24.5, "1920x1080 (FHD 16:9)", 360, 1.0, 1.0, 90, "HDR10", "8-bit", 400, 400, "%100 sRGB", "NVIDIA G-Sync İşlemci", "Düz (Flat)", 24999.0, "İtopya", 93, "S"),
        ("asus_rog_swift_pg38uq", "ASUS ROG Swift PG38UQ 38 inç 4K UHD 144Hz 1ms Fast IPS HDMI 2.1 Monitör", "ROG Swift PG38UQ", "Fast IPS", 38.0, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 116, "DisplayHDR 600", "10-bit", 450, 600, "%98 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro", "Düz (Flat)", 37999.0, "İtopya", 92, "A+"),
        ("asus_rog_swift_pg42uq_oled", "ASUS ROG Swift PG42UQ 41.5 inç 4K 138Hz 0.1ms OLED Mat Yansıma Engelleyici Monitör", "ROG Swift PG42UQ", "OLED (LG Panel)", 41.5, "3840x2160 (4K UHD 16:9)", 138, 0.1, 0.1, 106, "HDR10 (900 Nit)", "10-bit", 450, 900, "%98 DCI-P3, Özel Soğutma Bloğu", "G-Sync Compatible", "Düz (Flat)", 44999.0, "Vatan", 95, "S"),
        ("asus_rog_strix_xg27aqdmg", "ASUS ROG Strix OLED XG27AQDMG 27 inç 240Hz 0.03ms Glossy WOLED Gaming Monitör", "ROG Strix OLED XG27AQDMG", "Glossy WOLED", 26.5, "2560x1440 (2K QHD 16:9)", 240, 0.03, 0.03, 111, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, %135 sRGB", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 31999.0, "İtopya", 97, "S+"),
        ("asus_rog_strix_xg27acs", "ASUS ROG Strix XG27ACS 27 inç 180Hz 1ms 2K Fast IPS USB-C Espor Monitörü", "ROG Strix XG27ACS", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 180, 1.0, 0.5, 109, "DisplayHDR 400", "8-bit + FRC", 350, 400, "%97 DCI-P3, %133 sRGB, Type-C DP Alt", "G-Sync Compatible, FreeSync, ELMB Sync", "Düz (Flat)", 11999.0, "Gaming.Gen.TR", 88, "A"),
        ("asus_rog_strix_xg259qns_380hz", "ASUS ROG Strix XG259QNS 24.5 inç 380Hz 0.3ms Fast IPS Espor Oyuncu Monitörü", "ROG Strix XG259QNS", "Fast IPS", 24.5, "1920x1080 (FHD 16:9)", 380, 1.0, 0.3, 90, "DisplayHDR 400", "8-bit", 400, 400, "%110 sRGB", "FreeSync Premium, ELMB Sync, Type-C", "Düz (Flat)", 19999.0, "İtopya", 94, "S"),
        ("asus_rog_strix_xg27aqmr_300hz", "ASUS ROG Strix XG27AQMR 27 inç 300Hz 1ms 2K Fast IPS HDR600 Espor Monitörü", "ROG Strix XG27AQMR", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 300, 1.0, 1.0, 109, "DisplayHDR 600", "10-bit (8-bit + FRC)", 350, 600, "%97 DCI-P3, %120 sRGB", "G-Sync Compatible, FreeSync Premium Pro", "Düz (Flat)", 22999.0, "İtopya", 94, "S"),
        ("asus_rog_strix_xg32uq", "ASUS ROG Strix XG32UQ 32 inç 4K UHD 160Hz 1ms Fast IPS HDMI 2.1 Monitör", "ROG Strix XG32UQ", "Fast IPS", 32.0, "3840x2160 (4K UHD 16:9)", 160, 1.0, 1.0, 138, "DisplayHDR 600", "10-bit", 450, 600, "%96 DCI-P3, %130 sRGB", "G-Sync Compatible, FreeSync Premium Pro", "Düz (Flat)", 32999.0, "İtopya", 93, "S"),
        ("asus_rog_strix_xg349c", "ASUS ROG Strix XG349C 34 inç UWQHD 180Hz 1ms Fast IPS 1900R Kavisli Monitör", "ROG Strix XG349C", "Fast IPS", 34.14, "3440x1440 (UWQHD 21:9)", 180, 1.0, 1.0, 110, "DisplayHDR 400", "10-bit (8-bit + FRC)", 400, 400, "%98 DCI-P3, USB-C 18W, KVM", "G-Sync Compatible, ELMB Sync", "1900R Kavisli", 34999.0, "Hepsiburada", 91, "A+"),
        ("asus_rog_strix_xg49wcr", "ASUS ROG Strix XG49WCR 49 inç Dual QHD 165Hz 1ms 1800R Kavisli KVM Monitör", "ROG Strix XG49WCR", "Fast VA (3000:1 Kontrast)", 49.0, "5120x1440 (Dual QHD 32:9)", 165, 1.0, 1.0, 109, "DisplayHDR 400", "8-bit + FRC", 450, 450, "%90 DCI-P3, %125 sRGB, Smart KVM, Type-C 90W", "FreeSync Premium Pro, ELMB Sync", "1800R Kavisli", 42999.0, "İtopya", 92, "A+"),
    ]
    for row in asus_rog:
        items.append(_make_monitor(row, "ASUS"))

    # 2. ASUS TUF & ProArt (10 items)
    asus_tuf = [
        ("asus_tuf_vg27aqml1a", "ASUS TUF Gaming VG27AQML1A 27 inç 260Hz 1ms 2K Fast IPS Espor Monitörü", "TUF Gaming VG27AQML1A", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 260, 1.0, 0.5, 109, "DisplayHDR 400", "8-bit + FRC", 400, 450, "%100 sRGB, %95 DCI-P3", "G-Sync Compatible, FreeSync Premium, ELMB Sync", "Düz (Flat)", 13999.0, "Hepsiburada", 88, "A"),
        ("asus_tuf_vg27aq3a", "ASUS TUF Gaming VG27AQ3A 27 inç 180Hz 1ms Fast IPS 2K QHD Gaming Monitör", "TUF Gaming VG27AQ3A", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 180, 1.0, 1.0, 109, "HDR10", "8-bit", 250, 250, "%130 sRGB", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 9999.0, "İtopya", 83, "B+"),
        ("asus_tuf_vg249qm1a", "ASUS TUF Gaming VG249QM1A 23.8 inç 270Hz 1ms FHD Fast IPS Oyuncu Monitörü", "TUF Gaming VG249QM1A", "Fast IPS", 23.8, "1920x1080 (FHD 16:9)", 270, 1.0, 0.5, 93, "HDR10", "8-bit", 350, 350, "%99 sRGB", "G-Sync Compatible, FreeSync Premium, ELMB", "Düz (Flat)", 8499.0, "Gaming.Gen.TR", 85, "A"),
        ("asus_tuf_vg259qm_280hz", "ASUS TUF Gaming VG259QM 24.5 inç 280Hz 1ms Fast IPS G-Sync Espor Monitörü", "TUF Gaming VG259QM", "Fast IPS", 24.5, "1920x1080 (FHD 16:9)", 280, 1.0, 1.0, 90, "DisplayHDR 400", "8-bit", 400, 400, "%99 sRGB", "G-Sync Compatible, ELMB Sync", "Düz (Flat)", 10999.0, "İtopya", 86, "A"),
        ("asus_tuf_vg279qm_280hz", "ASUS TUF Gaming VG279QM 27 inç 280Hz 1ms Fast IPS FHD G-Sync Oyuncu Monitörü", "TUF Gaming VG279QM", "Fast IPS", 27.0, "1920x1080 (FHD 16:9)", 280, 1.0, 1.0, 82, "DisplayHDR 400", "8-bit", 400, 400, "%99 sRGB", "G-Sync Compatible, ELMB Sync", "Düz (Flat)", 11999.0, "İtopya", 86, "A"),
        ("asus_tuf_vg249q3a", "ASUS TUF Gaming VG249Q3A 23.8 inç 180Hz 1ms Fast IPS FHD Gaming Monitör", "TUF Gaming VG249Q3A", "Fast IPS", 23.8, "1920x1080 (FHD 16:9)", 180, 1.0, 1.0, 93, "HDR Ready", "8-bit", 250, 250, "%99 sRGB", "FreeSync Premium, ELMB", "Düz (Flat)", 5299.0, "Gaming.Gen.TR", 80, "B"),
        ("asus_tuf_vg34vql3a", "ASUS TUF Gaming VG34VQL3A 34 inç 180Hz 1ms UWQHD 1500R Kavisli Monitör", "TUF Gaming VG34VQL3A", "Fast VA (4000:1)", 34.0, "3440x1440 (UWQHD 21:9)", 180, 1.0, 1.0, 110, "DisplayHDR 400", "8-bit + FRC", 400, 400, "%125 sRGB", "FreeSync Premium Pro, ELMB", "1500R Kavisli", 15999.0, "İtopya", 86, "A"),
        ("asus_tuf_vg28uql1a", "ASUS TUF Gaming VG28UQL1A 28 inç 4K 144Hz 1ms Fast IPS HDMI 2.1 Monitör", "TUF Gaming VG28UQL1A", "Fast IPS", 28.0, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 157, "DisplayHDR 400", "10-bit", 400, 450, "%90 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro, HDMI 2.1 FRL", "Düz (Flat)", 21999.0, "İtopya", 90, "A+"),
        ("asus_proart_pa279crv", "ASUS ProArt PA279CRV 27 inç 4K UHD IPS %99 DCI-P3 Type-C 96W Referans Monitör", "ProArt PA279CRV", "IPS Profesyonel", 27.0, "3840x2160 (4K UHD 16:9)", 60, 5.0, 5.0, 163, "DisplayHDR 400", "10-bit (Delta E < 2)", 350, 400, "%99 DCI-P3, %99 Adobe RGB, %100 sRGB Calman", "Adaptive-Sync, Type-C 96W PD", "Düz (Flat)", 18999.0, "Hepsiburada", 90, "A+"),
        ("asus_proart_pa329crv", "ASUS ProArt PA329CRV 31.5 inç 4K UHD IPS %98 DCI-P3 Type-C 96W HDR Monitör", "ProArt PA329CRV", "IPS Profesyonel", 31.5, "3840x2160 (4K UHD 16:9)", 60, 5.0, 5.0, 140, "DisplayHDR 400", "10-bit (Delta E < 2)", 350, 400, "%98 DCI-P3, %100 sRGB Calman", "Adaptive-Sync, Type-C 96W PD", "Düz (Flat)", 26999.0, "Hepsiburada", 91, "A+"),
    ]
    for row in asus_tuf:
        items.append(_make_monitor(row, "ASUS"))

    # 3. LG UltraGear (18 items)
    lg_monitors = [
        ("monitor_lg_ultragear_32gs95ue", "LG UltraGear 32GS95UE 31.5 inç Çift Modlu 4K 240Hz / FHD 480Hz 0.03ms WOLED Monitör", "UltraGear 32GS95UE", "WOLED (Micro Lens Array+)", 31.5, "3840x2160 (4K 240Hz) / 1920x1080 (FHD 480Hz)", 480, 0.03, 0.02, 140, "DisplayHDR True Black 400", "10-bit (1.07 Milyar)", 450, 1300, "%98.5 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro, Pixel Sound Hoparlör", "Düz (Flat)", 54999.0, "MediaMarkt", 100, "S+"),
        ("monitor_lg_ultragear_27gs95qe", "LG UltraGear 27GS95QE 27 inç 240Hz 0.03ms QHD WOLED Espor Gaming Monitör", "UltraGear 27GS95QE", "WOLED (MLA+)", 26.5, "2560x1440 (2K QHD 16:9)", 240, 0.03, 0.03, 111, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%98.5 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro, HDMI 2.1", "Düz (Flat)", 29999.0, "Vatan", 97, "S+"),
        ("monitor_lg_ultragear_45gs96qb", "LG UltraGear 45GS96QB 44.5 inç 240Hz 0.03ms UWQHD 800R WOLED USB-C Monitör", "UltraGear 45GS96QB", "WOLED", 44.5, "3440x1440 (UWQHD 21:9)", 240, 0.03, 0.03, 84, "DisplayHDR True Black 400", "10-bit", 450, 1300, "%98.5 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro, USB-C 65W PD", "800R Kavisli", 59999.0, "Hepsiburada", 98, "S+"),
        ("monitor_lg_ultragear_34gs95qe", "LG UltraGear 34GS95QE 34 inç 240Hz 0.03ms UWQHD 800R WOLED Monitör", "UltraGear 34GS95QE", "WOLED", 33.9, "3440x1440 (UWQHD 21:9)", 240, 0.03, 0.03, 110, "DisplayHDR True Black 400", "10-bit", 450, 1300, "%98.5 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro", "800R Kavisli", 47999.0, "Hepsiburada", 97, "S+"),
        ("monitor_lg_ultragear_39gs95qe", "LG UltraGear 39GS95QE 39 inç 240Hz 0.03ms UWQHD 800R WOLED Gaming Monitör", "UltraGear 39GS95QE", "WOLED", 39.0, "3440x1440 (UWQHD 21:9)", 240, 0.03, 0.03, 96, "DisplayHDR True Black 400", "10-bit", 450, 1300, "%98.5 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro", "800R Kavisli", 52999.0, "MediaMarkt", 97, "S+"),
        ("monitor_lg_ultragear_27gr95qe", "LG UltraGear 27GR95QE-B 27 inç 240Hz 0.03ms QHD OLED Espor Monitörü", "UltraGear 27GR95QE-B", "WOLED", 26.5, "2560x1440 (2K QHD 16:9)", 240, 0.03, 0.03, 111, "HDR10", "10-bit", 450, 1000, "%98.5 DCI-P3", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 27999.0, "Vatan", 96, "S+"),
        ("monitor_lg_ultragear_45gr95qe", "LG UltraGear 45GR95QE-B 45 inç 240Hz 0.03ms UWQHD 800R OLED Monitör", "UltraGear 45GR95QE-B", "WOLED", 44.5, "3440x1440 (UWQHD 21:9)", 240, 0.03, 0.03, 84, "HDR10", "10-bit", 450, 1000, "%98.5 DCI-P3", "G-Sync Compatible, FreeSync Premium", "800R Kavisli", 49999.0, "Vatan", 96, "S+"),
        ("monitor_lg_ultragear_48gq900", "LG UltraGear 48GQ900-B 47.5 inç 4K UHD 138Hz 0.1ms OLED Gaming Monitör", "UltraGear 48GQ900", "OLED", 47.5, "3840x2160 (4K UHD 16:9)", 138, 0.1, 0.1, 93, "HDR10", "10-bit", 450, 900, "%99 DCI-P3, Parlama Önleyici Kaplama", "G-Sync Compatible, HDMI 2.1", "Düz (Flat)", 41999.0, "İtopya", 94, "S"),
        ("monitor_lg_ultragear_27gr93u", "LG UltraGear 27GR93U-B 27 inç 4K UHD 144Hz 1ms IPS HDMI 2.1 Gaming Monitör", "UltraGear 27GR93U-B", "Fast IPS", 27.0, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 163, "DisplayHDR 400", "10-bit (8-bit + FRC)", 400, 450, "%95 DCI-P3", "G-Sync Compatible, FreeSync Premium, HDMI 2.1 VRR", "Düz (Flat)", 18999.0, "Amazon TR", 90, "A+"),
        ("monitor_lg_ultragear_32gr93u", "LG UltraGear 32GR93U-B 31.5 inç 4K UHD 144Hz 1ms IPS HDMI 2.1 Monitör", "UltraGear 32GR93U-B", "Fast IPS", 31.5, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 140, "DisplayHDR 400", "10-bit", 400, 450, "%95 DCI-P3", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 23999.0, "Hepsiburada", 91, "A+"),
        ("monitor_lg_ultragear_32gq950", "LG UltraGear 32GQ950-B 32 inç 4K 160Hz 1ms Nano IPS ATW Polarizer HDR1000", "UltraGear 32GQ950-B", "Nano IPS (ATW Polarizer)", 31.5, "3840x2160 (4K UHD 16:9)", 160, 1.0, 1.0, 140, "VESA DisplayHDR 1000", "10-bit", 450, 1000, "%98 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro, HDMI 2.1", "Düz (Flat)", 34999.0, "İtopya", 95, "S"),
        ("monitor_lg_ultragear_27gp850p", "LG UltraGear 27GP850P-B 27 inç 180Hz 1ms 2K QHD Nano IPS Oyuncu Monitörü", "UltraGear 27GP850P-B", "Nano IPS", 27.0, "2560x1440 (2K QHD 16:9)", 180, 1.0, 1.0, 109, "DisplayHDR 400", "8-bit + FRC", 400, 400, "%98 DCI-P3, %135 sRGB", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 11499.0, "İtopya", 86, "A"),
        ("monitor_lg_ultragear_32gp850", "LG UltraGear 32GP850-B 31.5 inç 180Hz 1ms 2K QHD Nano IPS Oyuncu Monitörü", "UltraGear 32GP850-B", "Nano IPS", 31.5, "2560x1440 (2K QHD 16:9)", 180, 1.0, 1.0, 93, "DisplayHDR 400", "8-bit + FRC", 350, 400, "%98 DCI-P3", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 14999.0, "İtopya", 87, "A"),
        ("monitor_lg_ultragear_27gn800p", "LG UltraGear 27GN800P-B 27 inç 144Hz 1ms 2K QHD IPS Oyuncu Monitörü", "UltraGear 27GN800P-B", "IPS", 27.0, "2560x1440 (2K QHD 16:9)", 144, 1.0, 1.0, 109, "HDR10", "8-bit + FRC", 350, 350, "%99 sRGB", "G-Sync Compatible, FreeSync", "Düz (Flat)", 8999.0, "Vatan", 82, "B+"),
        ("monitor_lg_ultragear_24gn65r", "LG UltraGear 24GN65R-B 23.8 inç 144Hz 1ms FHD IPS Espor Monitörü", "UltraGear 24GN65R-B", "IPS", 23.8, "1920x1080 (FHD 16:9)", 144, 1.0, 1.0, 93, "HDR10", "8-bit", 300, 300, "%99 sRGB", "FreeSync Premium, G-Sync Uyumlu", "Düz (Flat)", 4499.0, "Hepsiburada", 78, "B"),
        ("monitor_lg_ultragear_24gs60f", "LG UltraGear 24GS60F-B 23.8 inç 180Hz 1ms Fast IPS FHD Oyuncu Monitörü", "UltraGear 24GS60F-B", "Fast IPS", 23.8, "1920x1080 (FHD 16:9)", 180, 1.0, 1.0, 93, "HDR10", "8-bit", 300, 300, "%99 sRGB", "G-Sync Compatible, FreeSync", "Düz (Flat)", 4999.0, "İtopya", 80, "B"),
        ("monitor_lg_ultragear_27gs60f", "LG UltraGear 27GS60F-B 27 inç 180Hz 1ms Fast IPS FHD Espor Monitörü", "UltraGear 27GS60F-B", "Fast IPS", 27.0, "1920x1080 (FHD 16:9)", 180, 1.0, 1.0, 82, "HDR10", "8-bit", 300, 300, "%99 sRGB", "G-Sync Compatible, FreeSync", "Düz (Flat)", 5999.0, "İtopya", 81, "B"),
        ("monitor_lg_ultragear_34gp950g", "LG UltraGear 34GP950G-B 34 inç 180Hz 1ms UWQHD G-Sync Ultimate Nano IPS", "UltraGear 34GP950G-B", "Nano IPS", 34.0, "3440x1440 (UWQHD 21:9)", 180, 1.0, 1.0, 110, "DisplayHDR 600", "10-bit", 400, 600, "%98 DCI-P3", "NVIDIA G-Sync Ultimate Donanımsal Modül", "1900R Kavisli", 39999.0, "Vatan", 93, "S"),
    ]
    for row in lg_monitors:
        items.append(_make_monitor(row, "LG"))

    # 4. Samsung Odyssey (16 items)
    samsung_monitors = [
        ("monitor_samsung_odyssey_oled_g6_g60sd", "Samsung Odyssey OLED G6 (G60SD) 27 inç 360Hz 0.03ms QHD QD-OLED Monitör", "Odyssey OLED G6 (G60SD)", "QD-OLED (Samsung Gen 3)", 27.0, "2560x1440 (2K QHD 16:9)", 360, 0.03, 0.03, 109, "DisplayHDR True Black 400, HDR10+", "10-bit (1.07 Milyar)", 450, 1000, "%99 DCI-P3, OLED Safeguard+", "FreeSync Premium Pro, G-Sync Uyumlu", "Düz (Flat)", 28999.0, "Samsung TR", 99, "S+"),
        ("monitor_samsung_odyssey_oled_g8_g80sd", "Samsung Odyssey OLED G8 (G80SD) 32 inç 4K UHD 240Hz 0.03ms QD-OLED Monitör", "Odyssey OLED G8 (G80SD)", "QD-OLED (Gen 3)", 32.0, "3840x2160 (4K UHD 16:9)", 240, 0.03, 0.03, 138, "DisplayHDR True Black 400, HDR10+", "10-bit", 450, 1000, "%99 DCI-P3, NQ8 AI Gen3 İşlemci", "FreeSync Premium Pro, Smart TV Tizen", "Düz (Flat)", 44999.0, "Samsung TR", 99, "S+"),
        ("monitor_samsung_odyssey_oled_g9_g95sc", "Samsung Odyssey OLED G9 (G95SC) 49 inç 240Hz 0.03ms Dual QHD 1800R QD-OLED Monitör", "Odyssey OLED G9 (G95SC)", "QD-OLED", 49.0, "5120x1440 (Dual QHD 32:9)", 240, 0.03, 0.03, 109, "DisplayHDR True Black 400, HDR10+", "10-bit", 450, 1000, "%99 DCI-P3, Neo Quantum Processor Pro", "FreeSync Premium Pro, G-Sync Compatible", "1800R Kavisli", 54999.0, "Samsung TR", 99, "S+"),
        ("monitor_samsung_odyssey_oled_g9_g93sc", "Samsung Odyssey OLED G9 (G93SC) 49 inç 240Hz 0.03ms DQHD 1800R QD-OLED Monitör", "Odyssey OLED G9 (G93SC)", "QD-OLED", 49.0, "5120x1440 (Dual QHD 32:9)", 240, 0.03, 0.03, 109, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3", "FreeSync Premium Pro", "1800R Kavisli", 48999.0, "Hepsiburada", 98, "S+"),
        ("monitor_samsung_odyssey_neo_g9_57", "Samsung Odyssey Neo G9 57 inç Dual 4K UHD 240Hz 1ms Mini-LED 1000R Gaming Monitör", "Odyssey Neo G9 (G95NC)", "Quantum Mini-LED VA (2392 Bölge)", 57.0, "7680x2160 (Dual UHD 32:9)", 240, 1.0, 1.0, 140, "VESA DisplayHDR 1000, Quantum HDR 2000", "10-bit", 420, 2000, "%95 DCI-P3, DisplayPort 2.1", "FreeSync Premium Pro", "1000R Kavisli", 82999.0, "Samsung TR", 99, "S+"),
        ("monitor_samsung_odyssey_neo_g9_49", "Samsung Odyssey Neo G9 49 inç 240Hz 1ms Mini-LED 1000R Quantum HDR2000 Monitör", "Odyssey Neo G9 (G95NA)", "Quantum Mini-LED VA (2048 Bölge)", 49.0, "5120x1440 (Dual QHD 32:9)", 240, 1.0, 1.0, 109, "Quantum HDR 2000", "10-bit", 420, 2000, "%95 DCI-P3", "G-Sync Compatible, FreeSync Premium Pro", "1000R Kavisli", 44999.0, "Vatan", 96, "S+"),
        ("monitor_samsung_odyssey_neo_g8_32", "Samsung Odyssey Neo G8 32 inç 4K UHD 240Hz 1ms 1000R Mini-LED Monitör", "Odyssey Neo G8", "Quantum Mini-LED VA (1196 Bölge)", 32.0, "3840x2160 (4K UHD 16:9)", 240, 1.0, 1.0, 138, "Quantum HDR 2000", "10-bit", 350, 2000, "%95 DCI-P3", "FreeSync Premium Pro, G-Sync Compatible", "1000R Kavisli", 36999.0, "Vatan", 94, "S"),
        ("monitor_samsung_odyssey_neo_g7_32", "Samsung Odyssey Neo G7 32 inç 4K UHD 165Hz 1ms 1000R Mini-LED Monitör", "Odyssey Neo G7", "Quantum Mini-LED VA (1196 Bölge)", 32.0, "3840x2160 (4K UHD 16:9)", 165, 1.0, 1.0, 138, "Quantum HDR 2000", "10-bit", 350, 2000, "%95 DCI-P3", "FreeSync Premium Pro", "1000R Kavisli", 31999.0, "Vatan", 93, "S"),
        ("monitor_samsung_odyssey_g70b_4k", "Samsung Odyssey G7 G70B 28 inç 4K 144Hz 1ms IPS Smart Gaming Monitör", "Odyssey G70B", "Fast IPS", 28.0, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 157, "DisplayHDR 400", "10-bit", 400, 400, "%90 DCI-P3, Smart TV Tizen", "G-Sync Compatible, FreeSync Premium Pro", "Düz (Flat)", 19999.0, "Hepsiburada", 89, "A"),
        ("monitor_samsung_odyssey_g6_g65b", "Samsung Odyssey G6 G65B 27 inç 240Hz 1ms QHD 1000R Smart Oyuncu Monitörü", "Odyssey G65B", "Fast VA (2500:1)", 27.0, "2560x1440 (2K QHD 16:9)", 240, 1.0, 1.0, 109, "DisplayHDR 600", "10-bit (8-bit + FRC)", 350, 600, "%95 DCI-P3, Smart TV", "FreeSync Premium Pro, G-Sync Uyumlu", "1000R Kavisli", 15499.0, "Samsung TR", 88, "A"),
        ("monitor_samsung_odyssey_g5_g50d", "Samsung Odyssey G5 G50D 27 inç 180Hz 1ms Fast IPS QHD Gaming Monitör", "Odyssey G5 G50D", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 180, 1.0, 1.0, 109, "DisplayHDR 400", "8-bit + FRC", 350, 400, "%99 sRGB", "FreeSync, G-Sync Uyumlu", "Düz (Flat)", 8799.0, "Hepsiburada", 82, "B+"),
        ("monitor_samsung_odyssey_g5_g55c", "Samsung Odyssey G5 G55C 27 inç 165Hz 1ms QHD 1000R Kavisli Monitör", "Odyssey G55C", "Fast VA (2500:1)", 27.0, "2560x1440 (2K QHD 16:9)", 165, 1.0, 1.0, 109, "HDR10", "8-bit", 300, 300, "%99 sRGB", "FreeSync", "1000R Kavisli", 7499.0, "İtopya", 80, "B"),
        ("monitor_samsung_odyssey_g4_g40b", "Samsung Odyssey G4 G40B 25 inç 240Hz 1ms IPS FHD Espor Monitörü", "Odyssey G40B", "Fast IPS", 25.0, "1920x1080 (FHD 16:9)", 240, 1.0, 1.0, 88, "HDR10", "8-bit", 400, 400, "%99 sRGB", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 7999.0, "Vatan", 83, "B+"),
        ("monitor_samsung_odyssey_g3_g30d", "Samsung Odyssey G3 G30D 24 inç 180Hz 1ms FHD Gaming Monitör", "Odyssey G30D", "Fast VA", 24.0, "1920x1080 (FHD 16:9)", 180, 1.0, 1.0, 92, "Standart", "8-bit", 250, 250, "%95 sRGB", "FreeSync", "Düz (Flat)", 4299.0, "Hepsiburada", 75, "C+"),
        ("monitor_samsung_odyssey_ark_55_gen2", "Samsung Odyssey Ark 55 inç 4K 165Hz 1ms 1000R Kokpit Modu Mini-LED Monitör", "Odyssey Ark Gen 2", "Quantum Mini-LED VA", 55.0, "3840x2160 (4K UHD 16:9)", 165, 1.0, 1.0, 80, "Quantum HDR 2000", "10-bit", 600, 2000, "%95 DCI-P3, 60W Sound Dome 2.2.2 Ses", "FreeSync Premium Pro, KVM, 4 Girişli Çoklu Görünüm", "1000R Kavisli (Dikey Döner)", 89999.0, "Samsung TR", 98, "S+"),
        ("monitor_samsung_viewfinity_s9_5k", "Samsung ViewFinity S9 27 inç 5K 5120x2880 IPS Mat Ekran Thunderbolt 4 Monitör", "ViewFinity S9", "IPS Profesyonel Renk", 27.0, "5120x2880 (5K 16:9)", 60, 5.0, 5.0, 218, "DisplayHDR 600", "10-bit", 600, 600, "%99 DCI-P3, Dahili 4K SlimFit Kamera, Thunderbolt 4 90W", "Adaptive-Sync, Calman Doğrulama", "Düz (Flat)", 39999.0, "Samsung TR", 94, "S"),
    ]
    for row in samsung_monitors:
        items.append(_make_monitor(row, "Samsung"))

    # 5. Dell Alienware (10 items)
    dell_alienware = [
        ("monitor_dell_alienware_aw2725df", "Dell Alienware AW2725DF 26.7 inç 360Hz 0.03ms QD-OLED Espor Gaming Monitör", "AW2725DF", "QD-OLED (Samsung Gen 3)", 26.7, "2560x1440 (2K QHD 16:9)", 360, 0.03, 0.03, 110, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99.3 DCI-P3 Delta E < 2", "FreeSync Premium Pro, VESA AdaptiveSync 360Hz", "Düz (Flat)", 37999.0, "Gaming.Gen.TR", 99, "S+"),
        ("monitor_dell_alienware_aw3225qf", "Dell Alienware AW3225QF 31.6 inç 4K UHD 240Hz 0.03ms 1700R QD-OLED Monitör", "AW3225QF", "QD-OLED (Gen 3)", 31.6, "3840x2160 (4K UHD 16:9)", 240, 0.03, 0.03, 140, "DisplayHDR True Black 400, Dolby Vision", "10-bit", 450, 1000, "%99 DCI-P3, eARC HDMI 2.1", "G-Sync Compatible, VESA AdaptiveSync 240Hz", "1700R Kavisli", 52999.0, "Hepsiburada", 99, "S+"),
        ("monitor_dell_alienware_aw3423dwf", "Dell Alienware AW3423DWF 34 inç 165Hz 0.1ms UWQHD 1800R QD-OLED Monitör", "AW3423DWF", "QD-OLED", 34.18, "3440x1440 (UWQHD 21:9)", 165, 0.1, 0.05, 110, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99.3 DCI-P3", "FreeSync Premium Pro, VESA AdaptiveSync", "1800R Kavisli", 41999.0, "İtopya", 96, "S+"),
        ("monitor_dell_alienware_aw2524h_500hz", "Dell Alienware AW2524H 24.5 inç 500Hz Fast IPS NVIDIA Reflex Espor Monitörü", "AW2524H", "Fast IPS", 24.5, "1920x1080 (FHD 16:9)", 500, 0.5, 0.5, 90, "DisplayHDR 400", "8-bit", 400, 450, "%99 sRGB", "NVIDIA G-Sync Donanımsal Çip + Reflex Analyzer", "Düz (Flat)", 32999.0, "Gaming.Gen.TR", 95, "S"),
        ("monitor_dell_alienware_aw2524hf_500hz", "Dell Alienware AW2524HF 24.5 inç 500Hz 0.5ms Fast IPS FreeSync Espor Monitörü", "AW2524HF", "Fast IPS", 24.5, "1920x1080 (FHD 16:9)", 500, 0.5, 0.5, 90, "HDR10", "8-bit", 400, 400, "%99 sRGB", "FreeSync Premium, VESA AdaptiveSync 500Hz", "Düz (Flat)", 28999.0, "Gaming.Gen.TR", 94, "S"),
        ("monitor_dell_alienware_aw2724hf_360hz", "Dell Alienware AW2724HF 27 inç 360Hz 0.5ms Fast IPS FHD Espor Monitörü", "AW2724HF", "Fast IPS", 27.0, "1920x1080 (FHD 16:9)", 360, 0.5, 0.5, 82, "HDR10", "8-bit", 400, 400, "%99 sRGB", "FreeSync Premium, VESA AdaptiveSync 360Hz", "Düz (Flat)", 18499.0, "Gaming.Gen.TR", 91, "A+"),
        ("monitor_dell_alienware_aw2724dm", "Dell Alienware AW2724DM 27 inç 180Hz 1ms 2K QHD Fast IPS HDR600 Monitör", "AW2724DM", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 180, 1.0, 1.0, 109, "VESA DisplayHDR 600", "10-bit (8-bit + FRC)", 400, 600, "%95 DCI-P3, %100 sRGB", "G-Sync Compatible, FreeSync Premium Pro", "Düz (Flat)", 15999.0, "Hepsiburada", 89, "A"),
        ("monitor_dell_alienware_aw2523hf_360hz", "Dell Alienware AW2523HF 24.5 inç 360Hz 0.5ms Fast IPS FHD Espor Monitörü", "AW2523HF", "Fast IPS", 24.5, "1920x1080 (FHD 16:9)", 360, 0.5, 0.5, 90, "HDR10", "8-bit", 400, 400, "%99 sRGB", "FreeSync Premium, Kulaklık Askısı", "Düz (Flat)", 16999.0, "Gaming.Gen.TR", 90, "A+"),
        ("monitor_dell_g2724d", "Dell G2724D 27 inç 165Hz 1ms Fast IPS 2K QHD G-Sync Gaming Monitör", "G2724D", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 165, 1.0, 1.0, 109, "DisplayHDR 400", "8-bit + FRC", 400, 400, "%99 sRGB", "G-Sync Compatible, FreeSync Premium Pro", "Düz (Flat)", 10499.0, "Gaming.Gen.TR", 85, "A"),
        ("monitor_dell_alienware_aw3821dw", "Dell Alienware AW3821DW 37.5 inç UWQHD+ 144Hz 1ms Nano IPS 2300R G-Sync Ultimate", "AW3821DW", "Nano IPS", 37.5, "3840x1600 (UWQHD+ 24:10)", 144, 1.0, 1.0, 111, "DisplayHDR 600", "10-bit", 450, 600, "%95 DCI-P3", "NVIDIA G-Sync Ultimate Donanımsal Modül", "2300R Kavisli", 49999.0, "İtopya", 93, "S"),
    ]
    for row in dell_alienware:
        items.append(_make_monitor(row, "Dell Alienware"))

    # 6. MSI MAG & MPG & MEG (12 items)
    msi_monitors = [
        ("monitor_msi_mpg_321urx_qd_oled", "MSI MPG 321URX QD-OLED 31.5 inç 4K UHD 240Hz 0.03ms KVM Type-C 90W Monitör", "MPG 321URX QD-OLED", "QD-OLED (Samsung Gen 3)", 31.5, "3840x2160 (4K UHD 16:9)", 240, 0.03, 0.03, 140, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, Grafen Film Soğutma, KVM, 90W PD", "Adaptive-Sync, G-Sync Uyumlu", "Düz (Flat)", 49999.0, "İtopya", 99, "S+"),
        ("monitor_msi_mpg_271qrx_qd_oled", "MSI MPG 271QRX QD-OLED 27 inç 360Hz 0.03ms 2K QHD Espor Oyuncu Monitörü", "MPG 271QRX QD-OLED", "QD-OLED (Gen 3)", 26.5, "2560x1440 (2K QHD 16:9)", 360, 0.03, 0.03, 111, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, MSI OLED Care 2.0, Type-C 90W", "Adaptive-Sync, G-Sync Uyumlu", "Düz (Flat)", 35999.0, "Hepsiburada", 99, "S+"),
        ("monitor_msi_mag_321upx_qd_oled", "MSI MAG 321UPX QD-OLED 31.5 inç 4K UHD 240Hz 0.03ms Grafen Soğutmalı Monitör", "MAG 321UPX QD-OLED", "QD-OLED (Gen 3)", 31.5, "3840x2160 (4K UHD 16:9)", 240, 0.03, 0.03, 140, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, Type-C 15W", "Adaptive-Sync, G-Sync Compatible", "Düz (Flat)", 41999.0, "İtopya", 98, "S+"),
        ("monitor_msi_mag_271qpx_qd_oled", "MSI MAG 271QPX QD-OLED 27 inç 360Hz 0.03ms 2K QHD Espor Monitörü", "MAG 271QPX QD-OLED", "QD-OLED (Gen 3)", 26.5, "2560x1440 (2K QHD 16:9)", 360, 0.03, 0.03, 111, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, OLED Care 2.0", "Adaptive-Sync", "Düz (Flat)", 32999.0, "İtopya", 98, "S+"),
        ("monitor_msi_mag_341cqp_qd_oled", "MSI MAG 341CQP QD-OLED 34 inç UWQHD 175Hz 0.03ms 1800R Kavisli Monitör", "MAG 341CQP QD-OLED", "QD-OLED", 34.0, "3440x1440 (UWQHD 21:9)", 175, 0.03, 0.03, 110, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99.3 DCI-P3, KVM, Type-C 15W", "Adaptive-Sync", "1800R Kavisli", 38999.0, "İtopya", 96, "S+"),
        ("monitor_msi_mag_274qrf_qd_e2", "MSI MAG 274QRF QD E2 27 inç 180Hz 1ms Rapid IPS Quantum Dot Gaming Monitör", "MAG 274QRF QD E2", "Rapid IPS + Quantum Dot", 27.0, "2560x1440 (2K QHD 16:9)", 180, 1.0, 0.5, 109, "DisplayHDR 400", "8-bit + FRC", 400, 450, "%98 DCI-P3, %150 sRGB, Type-C 65W", "G-Sync Compatible, Adaptive-Sync", "Düz (Flat)", 12999.0, "İtopya", 88, "A"),
        ("monitor_msi_g274qpx_240hz", "MSI G274QPX 27 inç 240Hz 1ms Rapid IPS 2K QHD Type-C 65W Espor Monitörü", "G274QPX", "Rapid IPS", 27.0, "2560x1440 (2K QHD 16:9)", 240, 1.0, 1.0, 109, "DisplayHDR 400", "10-bit (8-bit + FRC)", 400, 400, "%98 DCI-P3, %150 sRGB, Type-C 65W PD", "G-Sync Compatible", "Düz (Flat)", 14999.0, "İtopya", 89, "A"),
        ("monitor_msi_g274qpf_qd", "MSI G274QPF-QD 27 inç 170Hz 1ms Rapid IPS Quantum Dot 2K Oyuncu Monitörü", "G274QPF-QD", "Rapid IPS + QD", 27.0, "2560x1440 (2K QHD 16:9)", 170, 1.0, 1.0, 109, "DisplayHDR 400", "8-bit + FRC", 400, 400, "%93 DCI-P3, %150 sRGB, Type-C", "G-Sync Compatible", "Düz (Flat)", 11499.0, "Hepsiburada", 86, "A"),
        ("monitor_msi_g244f_e2", "MSI G244F E2 23.8 inç 180Hz 1ms Rapid IPS FHD Espor Monitörü", "G244F E2", "Rapid IPS", 23.8, "1920x1080 (FHD 16:9)", 180, 1.0, 0.5, 93, "HDR Ready", "8-bit", 300, 300, "%91 DCI-P3, %118 sRGB", "Adaptive-Sync, Night Vision", "Düz (Flat)", 4799.0, "İtopya", 79, "B"),
        ("monitor_msi_mag_256f", "MSI MAG 256F 24.5 inç 180Hz 1ms Rapid IPS FHD Espor Oyuncu Monitörü", "MAG 256F", "Rapid IPS", 24.5, "1920x1080 (FHD 16:9)", 180, 1.0, 0.5, 90, "HDR Ready", "8-bit", 300, 300, "%96 DCI-P3, %127 sRGB", "Adaptive-Sync, HDMI 2.0", "Düz (Flat)", 5299.0, "Tebilon", 81, "B"),
        ("monitor_msi_mag_323upf_4k", "MSI MAG 323UPF 32 inç 4K UHD 160Hz 1ms Rapid IPS Type-C 90W KVM Monitör", "MAG 323UPF", "Rapid IPS", 32.0, "3840x2160 (4K UHD 16:9)", 160, 1.0, 1.0, 138, "DisplayHDR 600", "10-bit (8-bit + FRC)", 450, 600, "%95 DCI-P3, %125 sRGB, KVM, 90W PD", "FreeSync Premium Pro, G-Sync Compatible", "Düz (Flat)", 29999.0, "İtopya", 92, "A+"),
        ("monitor_msi_meg_342c_qd_oled", "MSI MEG 342C QD-OLED 34 inç UWQHD 175Hz 0.1ms KVM Ambiglow Gaming Monitör", "MEG 342C QD-OLED", "QD-OLED", 34.18, "3440x1440 (UWQHD 21:9)", 175, 0.1, 0.1, 110, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99.3 DCI-P3, SpectrumBar Işıklandırma, KVM 65W", "FreeSync Premium Pro", "1800R Kavisli", 44999.0, "Vatan", 97, "S+"),
    ]
    for row in msi_monitors:
        items.append(_make_monitor(row, "MSI"))

    # 7. Gigabyte AORUS & Gaming (10 items)
    gb_monitors = [
        ("monitor_gigabyte_aorus_fo32u2p", "Gigabyte AORUS FO32U2P 31.5 inç 4K UHD 240Hz 0.03ms DisplayPort 2.1 UHBR20 QD-OLED Monitör", "AORUS FO32U2P", "QD-OLED (Gen 3)", 31.5, "3840x2160 (4K UHD 16:9)", 240, 0.03, 0.03, 140, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, DP 2.1 UHBR20 80Gbps Bant Genişliği", "FreeSync Premium Pro, Tactical Switch, KVM", "Düz (Flat)", 54999.0, "Sinerji", 100, "S+"),
        ("monitor_gigabyte_aorus_fo32u2", "Gigabyte AORUS FO32U2 31.5 inç 4K UHD 240Hz 0.03ms QD-OLED Gaming Monitör", "AORUS FO32U2", "QD-OLED (Gen 3)", 31.5, "3840x2160 (4K UHD 16:9)", 240, 0.03, 0.03, 140, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, DP 1.4, KVM, Type-C 18W", "FreeSync Premium Pro", "Düz (Flat)", 48999.0, "İtopya", 98, "S+"),
        ("monitor_gigabyte_aorus_fo27q3", "Gigabyte AORUS FO27Q3 27 inç 360Hz 0.03ms QHD QD-OLED Espor Monitörü", "AORUS FO27Q3", "QD-OLED (Gen 3)", 26.5, "2560x1440 (2K QHD 16:9)", 360, 0.03, 0.03, 111, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, KVM, Type-C 18W", "FreeSync Premium Pro, G-Sync Compatible", "Düz (Flat)", 36999.0, "İtopya", 99, "S+"),
        ("monitor_gigabyte_aorus_co49dq", "Gigabyte AORUS CO49DQ 49 inç Dual QHD 144Hz 0.03ms 1800R OLED Oyuncu Monitörü", "AORUS CO49DQ", "OLED", 49.0, "5120x1440 (Dual QHD 32:9)", 144, 0.03, 0.03, 109, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, KVM, Type-C", "FreeSync Premium Pro, G-Sync Compatible", "1800R Kavisli", 46999.0, "İtopya", 97, "S+"),
        ("monitor_gigabyte_m27q_x", "Gigabyte M27Q X 27 inç 240Hz 1ms 2K QHD SuperSpeed IPS KVM Monitör", "M27Q X", "SuperSpeed IPS", 27.0, "2560x1440 (2K QHD 16:9)", 240, 1.0, 0.5, 109, "DisplayHDR 400", "10-bit (8-bit + FRC)", 400, 450, "%92 DCI-P3, %140 sRGB, KVM Butonu", "FreeSync Premium, G-Sync Uyumlu", "Düz (Flat)", 14999.0, "İncehesap", 87, "A"),
        ("monitor_gigabyte_m27q", "Gigabyte M27Q 27 inç 170Hz 0.5ms 2K QHD SuperSpeed IPS KVM Oyuncu Monitörü", "M27Q", "SS IPS", 27.0, "2560x1440 (2K QHD 16:9)", 170, 1.0, 0.5, 109, "DisplayHDR 400", "8-bit", 350, 400, "%92 DCI-P3, %140 sRGB, Dahili KVM", "FreeSync Premium", "Düz (Flat)", 10999.0, "İtopya", 85, "A"),
        ("monitor_gigabyte_m28u_4k", "Gigabyte M28U 28 inç 4K UHD 144Hz 1ms SuperSpeed IPS HDMI 2.1 KVM Monitör", "M28U", "SS IPS", 28.0, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 157, "DisplayHDR 400", "8-bit + FRC", 350, 400, "%94 DCI-P3, %120 sRGB, Type-C KVM", "FreeSync Premium Pro", "Düz (Flat)", 19499.0, "İncehesap", 90, "A+"),
        ("monitor_gigabyte_m32u_4k", "Gigabyte M32U 31.5 inç 4K UHD 144Hz 1ms SS IPS HDMI 2.1 KVM Gaming Monitör", "M32U", "SS IPS", 31.5, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 140, "DisplayHDR 400", "10-bit (8-bit + FRC)", 350, 400, "%90 DCI-P3, %123 sRGB, KVM", "FreeSync Premium Pro", "Düz (Flat)", 24999.0, "İncehesap", 91, "A+"),
        ("monitor_gigabyte_gs27qc", "Gigabyte GS27QC 27 inç 170Hz 1ms QHD 1500R Kavisli Gaming Monitör", "GS27QC", "VA (4000:1)", 27.0, "2560x1440 (2K QHD 16:9)", 170, 1.0, 1.0, 109, "HDR Ready", "8-bit", 250, 250, "%108 sRGB", "FreeSync Premium", "1500R Kavisli", 7999.0, "Sinerji", 80, "B"),
        ("monitor_gigabyte_g24f_2", "Gigabyte G24F 2 23.8 inç 180Hz 1ms SS IPS FHD Espor Monitörü", "G24F 2", "SS IPS", 23.8, "1920x1080 (FHD 16:9)", 180, 1.0, 1.0, 93, "HDR Ready", "8-bit", 300, 300, "%95 DCI-P3, %125 sRGB", "FreeSync Premium", "Düz (Flat)", 4899.0, "İtopya", 79, "B"),
    ]
    for row in gb_monitors:
        items.append(_make_monitor(row, "Gigabyte"))

    # 8. BenQ ZOWIE & MOBIUZ (8 items)
    benq_monitors = [
        ("monitor_benq_zowie_xl2586x", "BenQ ZOWIE XL2586X 24.1 inç 540Hz Fast TN DyAc 2 Profesyonel Espor Monitörü", "XL2586X", "Fast TN (Esports Özel)", 24.1, "1920x1080 (FHD 16:9)", 540, 0.5, 0.2, 91, "Standart", "8-bit", 320, 320, "%95 sRGB (Maksimum Hareket Netliği)", "DyAc 2 (Çift Arka Işık Strobe), S-Switch", "Düz (Flat)", 38999.0, "Tebilon", 98, "S+"),
        ("monitor_benq_zowie_xl2566k", "BenQ ZOWIE XL2566K 24.5 inç 360Hz 0.5ms Fast TN DyAc+ Espor Monitörü", "XL2566K", "Fast TN", 24.5, "1920x1080 (FHD 16:9)", 360, 0.5, 0.3, 90, "Standart", "8-bit", 320, 320, "%95 sRGB", "DyAc+ Dinamik Netlik, S-Switch, Siperlik", "Düz (Flat)", 26999.0, "İtopya", 94, "S"),
        ("monitor_benq_zowie_xl2546k", "BenQ ZOWIE XL2546K 24.5 inç 240Hz 0.5ms Fast TN DyAc Espor Turnuva Monitörü", "XL2546K", "Fast TN", 24.5, "1920x1080 (FHD 16:9)", 240, 0.5, 0.5, 90, "Standart", "8-bit", 320, 320, "%95 sRGB", "DyAc Teknolojisi, Black eQualizer, S-Switch", "Düz (Flat)", 18999.0, "Gaming.Gen.TR", 89, "A"),
        ("monitor_benq_zowie_xl2540k", "BenQ ZOWIE XL2540K 24.5 inç 240Hz 0.5ms Fast TN Espor Monitörü", "XL2540K", "Fast TN", 24.5, "1920x1080 (FHD 16:9)", 240, 0.5, 0.5, 90, "Standart", "8-bit", 320, 320, "%95 sRGB", "Black eQualizer, Color Vibrance", "Düz (Flat)", 14999.0, "Tebilon", 86, "A"),
        ("monitor_benq_zowie_xl2411k", "BenQ ZOWIE XL2411K 24 inç 144Hz 1ms TN DyAc Espor Başlangıç Monitörü", "XL2411K", "TN", 24.0, "1920x1080 (FHD 16:9)", 144, 1.0, 1.0, 92, "Standart", "8-bit", 320, 320, "%90 sRGB", "DyAc, Black eQualizer", "Düz (Flat)", 6999.0, "İtopya", 78, "B"),
        ("monitor_benq_mobiuz_ex270qm", "BenQ MOBIUZ EX270QM 27 inç 240Hz 1ms 2K QHD IPS treVolo 2.1 Gaming Monitör", "EX270QM", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 240, 1.0, 0.5, 109, "DisplayHDR 600", "10-bit (8-bit + FRC)", 400, 600, "%98 DCI-P3, treVolo 2.1 Dahili Subwoofer", "FreeSync Premium Pro, HDMI 2.1", "Düz (Flat)", 23999.0, "Amazon TR", 91, "A+"),
        ("monitor_benq_mobiuz_ex3210u_4k", "BenQ MOBIUZ EX3210U 32 inç 4K 144Hz 1ms Quantum Dot IPS HDMI 2.1 Monitör", "EX3210U", "IPS + Quantum Dot", 32.0, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 138, "DisplayHDR 600", "10-bit", 400, 600, "%98 DCI-P3, %99 Adobe RGB, treVolo 2.1 Ses", "FreeSync Premium Pro, Mikrofon Dahil", "Düz (Flat)", 31999.0, "Amazon TR", 92, "A+"),
        ("monitor_benq_mobiuz_ex2710q", "BenQ MOBIUZ EX2710Q 27 inç 165Hz 1ms 2K QHD IPS treVolo Sesli Oyuncu Monitörü", "EX2710Q", "IPS", 27.0, "2560x1440 (2K QHD 16:9)", 165, 1.0, 1.0, 109, "DisplayHDR 400", "10-bit (8-bit + FRC)", 400, 400, "%95 DCI-P3, treVolo 2.1 Ses Sistemi", "FreeSync Premium", "Düz (Flat)", 12999.0, "Hepsiburada", 85, "A"),
    ]
    for row in benq_monitors:
        items.append(_make_monitor(row, "BenQ"))

    # 9. AOC, ViewSonic, Corsair, Philips, Innocn, KTC, Acer (20 items)
    others = [
        ("monitor_aoc_agon_pro_ag276qzd", "AOC AGON PRO AG276QZD 27 inç 240Hz 0.03ms QHD WOLED Espor Gaming Monitör", "AGON PRO AG276QZD", "WOLED", 26.5, "2560x1440 (2K QHD 16:9)", 240, 0.03, 0.03, 111, "HDR10 (1000 Nit)", "10-bit", 450, 1000, "%98.5 DCI-P3, Light FX RGB", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 28999.0, "İtopya", 97, "S+"),
        ("monitor_aoc_agon_pro_ag275qzn", "AOC AGON PRO AG275QZN 27 inç 240Hz 0.5ms Fast VA 2K QHD Espor Monitörü", "AGON PRO AG275QZN", "Fast VA (3000:1)", 27.0, "2560x1440 (2K QHD 16:9)", 240, 1.0, 0.5, 109, "DisplayHDR 400", "8-bit + FRC", 400, 400, "%100 sRGB, %90 DCI-P3", "FreeSync Premium, G-Sync Compatible", "Düz (Flat)", 14499.0, "Tebilon", 87, "A"),
        ("monitor_aoc_gaming_24g4", "AOC Gaming 24G4 23.8 inç 180Hz 0.5ms Fast IPS FHD Espor Monitörü", "24G4", "Fast IPS", 23.8, "1920x1080 (FHD 16:9)", 180, 1.0, 0.5, 93, "HDR10", "8-bit", 300, 300, "%126 sRGB, %93 DCI-P3", "Adaptive-Sync, G-Sync Uyumlu", "Düz (Flat)", 5299.0, "Tebilon", 80, "B"),
        ("monitor_aoc_gaming_q27g4x", "AOC Gaming Q27G4X 27 inç 180Hz 1ms Fast IPS 2K QHD Oyuncu Monitörü", "Q27G4X", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 180, 1.0, 0.5, 109, "DisplayHDR 400", "8-bit + FRC", 350, 400, "%99 sRGB, %93 DCI-P3", "G-Sync Compatible, Adaptive-Sync", "Düz (Flat)", 9899.0, "İtopya", 85, "A"),
        ("monitor_viewsonic_xg2431_240hz", "ViewSonic OMNI XG2431 24 inç 240Hz 0.5ms Blur Busters Onaylı Fast IPS Espor Monitörü", "OMNI XG2431", "Fast IPS", 23.8, "1920x1080 (FHD 16:9)", 240, 1.0, 0.5, 93, "DisplayHDR 400", "8-bit", 350, 400, "%104 sRGB, Blur Busters PureXP", "FreeSync Premium", "Düz (Flat)", 10999.0, "Amazon TR", 89, "A"),
        ("monitor_viewsonic_elite_xg272_2k_oled", "ViewSonic Elite XG272-2K-OLED 27 inç 240Hz 0.02ms Beyaz WOLED Espor Monitörü", "Elite XG272-2K-OLED", "WOLED", 26.5, "2560x1440 (2K QHD 16:9)", 240, 0.02, 0.02, 111, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%98.5 DCI-P3, Kar Beyazı Tasarım", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", None, None, 97, "S+"),
        ("monitor_corsair_xeneon_27qhd240", "Corsair XENEON 27QHD240 27 inç 240Hz 0.03ms OLED 3 Yıl Yanık Garantili Monitör", "XENEON 27QHD240", "WOLED (LG Panel)", 27.0, "2560x1440 (2K QHD 16:9)", 240, 0.03, 0.03, 110, "HDR10 (1000 Nit)", "10-bit", 450, 1000, "%98.5 DCI-P3, Sıfır Yanık Garantisi", "G-Sync Compatible, FreeSync Premium", "Düz (Flat)", 34999.0, "İtopya", 97, "S+"),
        ("monitor_corsair_xeneon_flex_45", "Corsair Xeneon Flex 45WQHD240 45 inç 240Hz 0.03ms Manuel Bükülebilir OLED Monitör", "Xeneon Flex 45WQHD240", "WOLED (Bükülebilir)", 45.0, "3440x1440 (UWQHD 21:9)", 240, 0.03, 0.03, 84, "HDR10 (1000 Nit)", "10-bit", 450, 1000, "%98.5 DCI-P3, Düz ile 800R Arası Ayarlanabilir", "G-Sync Compatible, FreeSync Premium", "Manuel 800R Kavisli", None, None, 99, "S+"),
        ("monitor_philips_evnia_34m2c8600", "Philips Evnia 34M2C8600 34 inç UWQHD 175Hz 0.1ms 1800R QD-OLED Ambiglow Monitör", "Evnia 34M2C8600", "QD-OLED (Samsung)", 34.0, "3440x1440 (UWQHD 21:9)", 175, 0.1, 0.1, 110, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99.3 DCI-P3, 4 Taraflı Ambiglow, Type-C 90W", "FreeSync Premium Pro, KVM", "1800R Kavisli", 37999.0, "Hepsiburada", 96, "S+"),
        ("monitor_philips_evnia_27m2n5500", "Philips Evnia 27M2N5500 27 inç 180Hz 0.5ms Fast IPS 2K QHD Oyuncu Monitörü", "Evnia 27M2N5500", "Fast IPS", 27.0, "2560x1440 (2K QHD 16:9)", 180, 1.0, 0.5, 109, "DisplayHDR 400", "10-bit (8-bit + FRC)", 400, 450, "%93 DCI-P3, %125 sRGB", "Adaptive-Sync, G-Sync Uyumlu", "Düz (Flat)", 10499.0, "İtopya", 86, "A"),
        ("monitor_innocn_27m2v_mini_led", "Innocn 27M2V 27 inç 4K 160Hz 1ms 1152 Bölge Mini-LED HDR 1000 Monitör", "27M2V", "Mini-LED Fast IPS (1152 Bölge)", 27.0, "3840x2160 (4K UHD 16:9)", 160, 1.0, 1.0, 163, "DisplayHDR 1000", "10-bit", 450, 1000, "%99 DCI-P3, %99 Adobe RGB, Type-C 90W", "FreeSync Premium, G-Sync Compatible", "Düz (Flat)", None, None, 95, "S"),
        ("monitor_innocn_32m2v_mini_led", "Innocn 32M2V 32 inç 4K 144Hz 1ms 1152 Bölge Mini-LED HDR 1000 Profesyonel Monitör", "32M2V", "Mini-LED Fast IPS (1152 Bölge)", 32.0, "3840x2160 (4K UHD 16:9)", 144, 1.0, 1.0, 140, "DisplayHDR 1000", "10-bit", 450, 1000, "%99 DCI-P3, %99 Adobe RGB, Type-C 90W", "FreeSync Premium, KVM", "Düz (Flat)", None, None, 95, "S"),
        ("monitor_ktc_g27p6_oled", "KTC G27P6 27 inç 240Hz 0.03ms LG WOLED QHD Type-C 65W Gaming Monitör", "G27P6", "WOLED (LG Panel)", 26.5, "2560x1440 (2K QHD 16:9)", 240, 0.03, 0.03, 111, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%100 sRGB, %97 DCI-P3, Type-C 65W", "FreeSync, G-Sync Compatible, KVM", "Düz (Flat)", None, None, 96, "S+"),
        ("monitor_acer_predator_x27u", "Acer Predator X27U 27 inç 240Hz 0.03ms 2K QHD WOLED Type-C 90W KVM Monitör", "Predator X27U", "WOLED", 26.5, "2560x1440 (2K QHD 16:9)", 240, 0.03, 0.01, 111, "HDR10 (1000 Nit)", "10-bit", 450, 1000, "%99 DCI-P3, KVM, USB-C 90W PD", "FreeSync Premium, G-Sync Compatible", "Düz (Flat)", 29999.0, "Vatan", 96, "S+"),
        ("monitor_acer_predator_x34_v", "Acer Predator X34 V 34 inç UWQHD 175Hz 0.1ms 1800R OLED Gaming Monitör", "Predator X34 V", "OLED", 34.0, "3440x1440 (UWQHD 21:9)", 175, 0.1, 0.1, 110, "DisplayHDR True Black 400", "10-bit", 450, 1000, "%99 DCI-P3, Type-C 65W", "FreeSync Premium", "1800R Kavisli", 37999.0, "Vatan", 95, "S"),
        ("monitor_acer_nitro_xv252qf_390hz", "Acer Nitro XV252QF 24.5 inç 390Hz 0.5ms Fast IPS FHD Espor Monitörü", "Nitro XV252Q F", "Fast IPS", 24.5, "1920x1080 (FHD 16:9)", 390, 0.5, 0.5, 90, "DisplayHDR 400", "8-bit", 400, 400, "%99 sRGB", "AMD FreeSync Premium", "Düz (Flat)", 17499.0, "Hepsiburada", 91, "A+"),
        ("monitor_cooler_master_tempest_gp27q", "Cooler Master Tempest GP27Q 27 inç 2K 165Hz 1ms Mini-LED 576 Bölge Monitör", "Tempest GP27Q", "Mini-LED Ultra-Speed IPS", 27.0, "2560x1440 (2K QHD 16:9)", 165, 1.0, 0.5, 109, "DisplayHDR 1000", "10-bit", 600, 1200, "%98 DCI-P3, %99 Adobe RGB, Type-C 90W", "FreeSync Premium, G-Sync Compatible", "Düz (Flat)", None, None, 93, "S"),
    ]
    for row in others:
        brand = row[1].split()[0]
        items.append(_make_monitor(row, brand))

    return items

def _make_monitor(row, brand):
    uid, name, model, panel, sz, res, hz, gtg, mprt, ppi, hdr, bit, sdr_nit, hdr_nit, gamut, vrr, curved, price, store, score, tier = row
    if not uid.startswith("monitor_"):
        uid = f"monitor_{uid}"
    return {
        "id": uid,
        "category": "MONITOR",
        "name": name,
        "brand": brand,
        "model": model,
        "panel_type": panel,
        "screen_size_inch": sz,
        "resolution": res,
        "refresh_rate_hz": hz,
        "response_speed_gtg_ms": gtg,
        "response_mprt_ms": mprt,
        "ppi": ppi,
        "hdr_certification": hdr,
        "bit_depth": bit,
        "brightness_sdr_nit": sdr_nit,
        "brightness_hdr_nit": hdr_nit,
        "color_gamut": gamut,
        "vrr_support": vrr,
        "curved": curved,
        "current_price_try": price,
        "lowest_price_try": price,
        "highest_price_try": price,
        "store_name": store,
        "store_url": f"https://www.{store.lower().replace(' ', '')}.com" if store else None,
        "benchmark_score": score,
        "benchmark_tier": tier,
    }

print("Loaded monitor data generator module.")

def get_all_mice():
    items = []
    # 1. Logitech G (12 items)
    logitech = [
        ("mouse_logitech_g_pro_x_superlight_2", "Logitech G PRO X SUPERLIGHT 2 LIGHTSPEED Kablosuz Oyuncu Faresi", "G PRO X SUPERLIGHT 2", "HERO 2 (44K DPI / 888 IPS / 88G)", 44000, 8000, "LIGHTFORCE Hibrit Optik-Mekanik", 60, "2.4GHz LIGHTSPEED 8K / USB-C", 95, "Nordic nRF52840", 0.4, "Simetrik (Ambidextrous)", 5899.0, "İtopya", 98, "S+"),
        ("mouse_logitech_g_pro_x_superlight_2_dex", "Logitech G PRO X SUPERLIGHT 2 DEX Asimetrik Ergonomik 8K Faresi", "G PRO X SUPERLIGHT 2 DEX", "HERO 2 (44K DPI / 888 IPS)", 44000, 8000, "LIGHTFORCE Optik-Mekanik", 60, "2.4GHz LIGHTSPEED 8K / USB-C", 95, "Nordic nRF52840", 0.4, "Ergonomik (Sağ El)", 6499.0, "İtopya", 98, "S+"),
        ("mouse_logitech_g_pro_x_superlight", "Logitech G PRO X SUPERLIGHT Kablosuz Efsane Espor Faresi", "G PRO X SUPERLIGHT", "HERO 25K (25600 DPI)", 25600, 1000, "Omron 20M Beyaz", 63, "2.4GHz LIGHTSPEED / USB Micro-B", 70, "Nordic nRF52840", 1.1, "Simetrik", 4499.0, "Hepsiburada", 94, "S"),
        ("mouse_logitech_g502_x_plus", "Logitech G502 X PLUS LIGHTSPEED Kablosuz RGB Oyuncu Faresi", "G502 X PLUS", "HERO 25K", 25600, 1000, "LIGHTFORCE Optik-Mekanik", 106, "2.4GHz LIGHTSPEED / USB-C", 120, "Nordic", 1.2, "Ergonomik (Çok Tuşlu MMO/FPS)", 5499.0, "MediaMarkt", 89, "A"),
        ("mouse_logitech_g502_x_lightspeed", "Logitech G502 X LIGHTSPEED Kablosuz Oyuncu Faresi (RGB'siz Hafif Kasa)", "G502 X LIGHTSPEED", "HERO 25K", 25600, 1000, "LIGHTFORCE Optik-Mekanik", 99, "2.4GHz LIGHTSPEED / USB-C", 140, "Nordic", 1.2, "Ergonomik (Çok Tuşlu)", 4699.0, "Amazon TR", 90, "A+"),
        ("mouse_logitech_g502_x_kablolu", "Logitech G502 X Kablolu LIGHTFORCE Switch Oyuncu Faresi", "G502 X", "HERO 25K", 25600, 1000, "LIGHTFORCE Optik-Mekanik", 89, "Ultra Esnek İnce Kablo", None, "Logitech MCU", 1.0, "Ergonomik", 2899.0, "İtopya", 87, "A"),
        ("mouse_logitech_g303_shroud", "Logitech G303 Shroud Edition Kablosuz Espor Faresi", "G303 Shroud Edition", "HERO 25K", 25600, 1000, "Omron Mekanik", 75, "2.4GHz LIGHTSPEED / USB-C", 145, "Nordic", 1.5, "Agresif Pençe (Claw Grip Özel)", 3999.0, "Hepsiburada", 88, "A"),
        ("mouse_logitech_g703_lightspeed", "Logitech G703 LIGHTSPEED Kablosuz Ergonomik Oyuncu Faresi", "G703 LIGHTSPEED", "HERO 25K", 25600, 1000, "Omron Mekanik 50M", 95, "2.4GHz LIGHTSPEED / USB Micro-B", 60, "Nordic", 1.4, "Ergonomik Sağ El Avuç İçi", 3499.0, "Vatan", 85, "A"),
        ("mouse_logitech_g305_lightspeed", "Logitech G305 LIGHTSPEED Kablosuz F/P Oyuncu Faresi", "G305 LIGHTSPEED", "HERO 12K", 12000, 1000, "Omron 10M", 99, "2.4GHz LIGHTSPEED (Tek AA Pil ile 250 Saat)", 250, "Nordic", 2.0, "Simetrik Kompakt", 1499.0, "Hepsiburada", 80, "B"),
        ("mouse_logitech_g203_lightsync", "Logitech G203 LIGHTSYNC RGB Kablolu Gaming Fare", "G203 LIGHTSYNC", "Mercury 8000 DPI", 8000, 1000, "Mekanik Gergi Sistemi", 85, "Ultra Hafif USB Kablo", None, "Logitech MCU", 2.2, "Simetrik Kompakt", 899.0, "Vatan", 75, "C+"),
        ("mouse_logitech_g403_hero", "Logitech G403 HERO 25K Ergonomik Kablolu Oyuncu Faresi", "G403 HERO", "HERO 25K", 25600, 1000, "Omron 20M", 87, "Örgülü USB Kablo", None, "Logitech MCU", 1.3, "Ergonomik", 1799.0, "İtopya", 82, "B+"),
        ("mouse_logitech_mx518_legendary", "Logitech MX518 Legendary Efsanevi Ergonomik Oyuncu Faresi", "MX518 Legendary", "HERO 16K", 16000, 1000, "Omron 20M", 101, "Klasik USB Kablo", None, "Logitech MCU", 1.8, "Klasik Efsanevi Ergonomik", 2499.0, "Amazon TR", 81, "B"),
    ]
    for row in logitech:
        items.append(_make_mouse(row, "Logitech"))

    # 2. Razer (14 items)
    razer = [
        ("mouse_razer_viper_v3_pro", "Razer Viper V3 Pro Ultra Hafif 8000Hz Kablosuz Espor Faresi", "Viper V3 Pro", "Focus Pro 35K Gen-2 Optik", 35000, 8000, "Razer Gen-3 Optik Fare Switch (90M)", 54, "2.4GHz HyperSpeed Kablosuz 8K / USB-C", 95, "Nordic nRF52840", 0.2, "Simetrik Espor (Claw / Fingertip)", 6299.0, "İtopya", 100, "S+"),
        ("mouse_razer_viper_v3_hyperspeed", "Razer Viper V3 HyperSpeed Kablosuz Espor Faresi (4000Hz Uyumlu)", "Viper V3 HyperSpeed", "Focus Pro 30K Optik", 30000, 4000, "Razer Gen-2 Mekanik (60M)", 82, "HyperSpeed Kablosuz / AA Pil (280 Saat)", 280, "Nordic nRF52840", 0.6, "Simetrik Espor", 2899.0, "İtopya", 91, "A+"),
        ("mouse_razer_viper_v2_pro", "Razer Viper V2 Pro Ultra Hafif Kablosuz Espor Faresi", "Viper V2 Pro", "Focus Pro 30K Optik", 30000, 8000, "Razer Gen-3 Optik", 58, "HyperSpeed Kablosuz / USB-C Speedflex", 80, "Nordic nRF52840", 0.4, "Simetrik Alçak Profil", 4499.0, "Hepsiburada", 95, "S"),
        ("mouse_razer_viper_mini_signature_edition", "Razer Viper Mini Signature Edition Magnezyum Dış İskelet 8000Hz Fare", "Viper Mini Signature Edition", "Focus Pro 30K Optik", 30000, 8000, "Razer Gen-3 Optik", 49, "HyperPolling Kablosuz 8K / USB-C", 60, "Nordic nRF52840", 0.18, "Kompakt Simetrik Magnezyum CNC İskelet", None, None, 100, "S+"),
        ("mouse_razer_deathadder_v3_pro", "Razer DeathAdder V3 Pro Kablosuz Ergonomik Espor Faresi", "DeathAdder V3 Pro", "Focus Pro 30K Optik Sensör", 30000, 8000, "Razer Gen-3 Optik Switch", 63, "2.4GHz HyperSpeed Kablosuz / USB-C Speedflex", 90, "Nordic nRF52840", 0.3, "Ergonomik Efsane (Sağ El Palm/Claw)", 4999.0, "İtopya", 97, "S+"),
        ("mouse_razer_deathadder_v3_hyperspeed", "Razer DeathAdder V3 HyperSpeed Kompakt Ergonomik 8K Uyumlu Kablosuz Fare", "DeathAdder V3 HyperSpeed", "Focus X 26K Optik", 26000, 8000, "Razer Gen-3 Optik", 55, "HyperSpeed Kablosuz / USB-C", 100, "Nordic nRF52840", 0.35, "Kompakt Ergonomik Sağ El", 3899.0, "Amazon TR", 95, "S"),
        ("mouse_razer_deathadder_v3_kablolu", "Razer DeathAdder V3 8000Hz Ultra Düşük Gecikmeli Kablolu Espor Faresi", "DeathAdder V3 Wired", "Focus Pro 30K Optik", 30000, 8000, "Razer Gen-3 Optik", 59, "8000Hz Speedflex USB Kablo", None, "Razer MCU", 0.125, "Ergonomik Sağ El", 2799.0, "İtopya", 95, "S"),
        ("mouse_razer_deathadder_essential", "Razer DeathAdder Essential Klasik Ergonomik Kablolu Fare", "DeathAdder Essential", "6400 DPI Optik", 6400, 1000, "Mekanik 10M", 96, "Standart USB Kablo", None, "Razer MCU", 2.5, "Ergonomik Sağ El", 799.0, "Vatan", 72, "C"),
        ("mouse_razer_basilisk_v3_pro", "Razer Basilisk V3 Pro 35K HyperScroll Eğimli Kablosuz Fare", "Basilisk V3 Pro 35K", "Focus Pro 35K Gen-2 Optik", 35000, 8000, "Razer Gen-3 Optik", 112, "HyperSpeed 2.4G / Bluetooth / USB-C / Kablosuz Şarj", 140, "Nordic nRF52840", 0.5, "Ergonomik Başparmak Dayamalı Çok Amaçlı", 5999.0, "İtopya", 93, "S"),
        ("mouse_razer_basilisk_v3_kablolu", "Razer Basilisk V3 RGB 11 Programlanabilir Tuşlu Ergonomik Fare", "Basilisk V3", "Focus+ 26K Optik", 26000, 1000, "Razer Gen-2 Optik", 101, "Speedflex USB", None, "Razer MCU", 1.0, "Ergonomik", 2499.0, "Hepsiburada", 88, "A"),
        ("mouse_razer_basilisk_v3_x_hyperspeed", "Razer Basilisk V3 X HyperSpeed Kablosuz Oyuncu Faresi", "Basilisk V3 X HyperSpeed", "5G Gelişmiş 18K", 18000, 1000, "Razer Gen-2 Mekanik", 110, "HyperSpeed 2.4G / Bluetooth (535 Saat)", 535, "Nordic", 1.5, "Ergonomik", 2299.0, "İtopya", 84, "B+"),
        ("mouse_razer_cobra_pro", "Razer Cobra Pro 11 Bölgeli Chroma RGB Kablosuz Kompakt Espor Faresi", "Cobra Pro", "Focus Pro 30K Optik", 30000, 8000, "Razer Gen-3 Optik", 77, "HyperSpeed Kablosuz / BT / USB-C", 100, "Nordic nRF52840", 0.4, "Kompakt Simetrik", 4499.0, "MediaMarkt", 91, "A+"),
        ("mouse_razer_cobra_kablolu", "Razer Cobra Ultra Hafif 58g Chroma RGB Kablolu Oyuncu Faresi", "Cobra Wired", "8500 DPI Optik", 8500, 1000, "Razer Gen-3 Optik", 58, "Speedflex Kablo", None, "Razer MCU", 0.8, "Kompakt Simetrik", 1599.0, "İtopya", 82, "B+"),
        ("mouse_razer_naga_v2_pro", "Razer Naga V2 Pro Değiştirilebilir 3 Yan Plakalı Kablosuz MMO/MOBA Faresi", "Naga V2 Pro", "Focus Pro 30K Optik", 30000, 4000, "Razer Gen-3 Optik", 134, "HyperSpeed / BT / HyperScroll Pro Wheel", 150, "Nordic", 1.2, "Modüler MMO/FPS/MOBA", 6999.0, "İtopya", 92, "A+"),
    ]
    for row in razer:
        items.append(_make_mouse(row, "Razer"))

    # 3. SteelSeries (10 items)
    steelseries = [
        ("mouse_steelseries_aerox_3_wireless_2022", "SteelSeries Aerox 3 Wireless (2022) 68g IP54 Su Geçirmez Kablosuz Fare", "Aerox 3 Wireless 2022", "TrueMove Air (18000 CPI)", 18000, 1000, "Next-Gen Golden Micro IP54 (80M)", 68, "Quantum 2.0 2.4GHz / Bluetooth 5.0 / USB-C", 200, "SteelSeries MCU", 1.2, "Simetrik Petekli Gövde", 3199.0, "İtopya", 87, "A"),
        ("mouse_steelseries_aerox_3_2022_kablolu", "SteelSeries Aerox 3 (2022) Ultra Hafif 59g IP54 RGB Kablolu Espor Faresi", "Aerox 3 2022", "TrueMove Core (8500 CPI)", 8500, 1000, "Golden Micro IP54", 59, "Süper Örgülü USB-C Kablo", None, "SteelSeries MCU", 1.0, "Simetrik Delikli Kasa", 1999.0, "Vatan", 83, "B+"),
        ("mouse_steelseries_aerox_5_wireless", "SteelSeries Aerox 5 Wireless 74g 9 Programlanabilir Tuşlu FPS/MOBA Faresi", "Aerox 5 Wireless", "TrueMove Air (18000 CPI)", 18000, 1000, "Golden Micro IP54", 74, "Quantum 2.0 / BT / USB-C", 180, "SteelSeries MCU", 1.2, "Ergonomik 9 Tuşlu Çok Amaçlı", 4199.0, "MediaMarkt", 89, "A"),
        ("mouse_steelseries_aerox_9_wireless", "SteelSeries Aerox 9 Wireless 89g 18 Tuşlu Ultra Hafif MMO/MOBA Faresi", "Aerox 9 Wireless", "TrueMove Air (18000 CPI)", 18000, 1000, "Golden Micro IP54", 89, "Quantum 2.0 / BT / USB-C", 180, "SteelSeries MCU", 1.4, "12 Tuşlu Yan Panel MMO", 4899.0, "İtopya", 88, "A"),
        ("mouse_steelseries_rival_3_wireless", "SteelSeries Rival 3 Wireless Çift Modlu 400+ Saat Pil Ömürlü Fare", "Rival 3 Wireless", "TrueMove Air (18000 CPI)", 18000, 1000, "Mekanik 60M", 106, "Quantum 2.0 2.4G / BT 5.0 (2x AAA)", 400, "SteelSeries MCU", 1.8, "Simetrik Pençe/Parmak Ucu", 1799.0, "Amazon TR", 82, "B+"),
        ("mouse_steelseries_rival_3_kablolu", "SteelSeries Rival 3 RGB 77g TrueMove Core Espor Oyuncu Faresi", "Rival 3", "TrueMove Core (8500 CPI)", 8500, 1000, "SteelSeries 60M Mekanik", 77, "Yumuşak Kauçuk USB Kablo", None, "SteelSeries MCU", 1.5, "Simetrik Prism RGB", 1299.0, "Hepsiburada", 80, "B"),
        ("mouse_steelseries_rival_5", "SteelSeries Rival 5 85g 9 Tuşlu Hızlı Aksiyon FPS/Battle Royale Faresi", "Rival 5", "TrueMove Air (18000 CPI)", 18000, 1000, "Golden Micro 80M IP54", 85, "Süper Örgülü Kablo", None, "SteelSeries MCU", 1.0, "Ergonomik Çok Amaçlı", 2399.0, "İtopya", 85, "A"),
        ("mouse_steelseries_prime_wireless", "SteelSeries Prime Wireless Manyetik Optik Switchli Espor Faresi", "Prime Wireless", "TrueMove Air (18000 CPI)", 18000, 1000, "Prestige OM Manyetik Optik Switch (100M)", 80, "Quantum 2.0 / USB-C", 100, "Nordic", 0.8, "Espor Profesyonel Ergonomik (Sağ El)", 3699.0, "İtopya", 91, "A+"),
        ("mouse_steelseries_prime_mini_wireless", "SteelSeries Prime Mini Wireless Kompakt 73g Manyetik Optik Espor Faresi", "Prime Mini Wireless", "TrueMove Air (18000 CPI)", 18000, 1000, "Prestige OM Manyetik Optik", 73, "Quantum 2.0 / USB-C", 100, "Nordic", 0.8, "Kompakt Ergonomik", 3499.0, "Vatan", 90, "A+"),
        ("mouse_steelseries_prime_kablolu", "SteelSeries Prime 69g Prestige OM Manyetik Optik Turnuva Faresi", "Prime Wired", "TrueMove Pro (18000 CPI / 450 IPS)", 18000, 1000, "Prestige OM Manyetik Optik", 69, "Ayrılabilir Mikro Örgülü USB-C", None, "SteelSeries MCU", 0.6, "Ergonomik Turnuva", 1999.0, "Hepsiburada", 88, "A"),
    ]
    for row in steelseries:
        items.append(_make_mouse(row, "SteelSeries"))

    # 4. Pulsar, Lamzu, Finalmouse, WLmouse (16 items)
    boutique = [
        ("mouse_pulsar_x2v2_wireless", "Pulsar X2 V2 Kablosuz Espor Faresi (Optik Switch / Pulsar Blue Encoder)", "X2 V2", "PixArt PAW3395 (26000 DPI)", 26000, 4000, "Pulsar Optik Switch", 53, "2.4GHz Kablosuz 4K / USB-C", 100, "Nordic nRF52840", 0.5, "Simetrik Düşük Profil Claw", 3499.0, "Wraith Esports", 94, "S"),
        ("mouse_pulsar_x2v2_mini", "Pulsar X2 V2 Mini Ultra Hafif 51g Kablosuz Espor Faresi", "X2 V2 Mini", "PixArt PAW3395", 26000, 4000, "Pulsar Optik Switch", 51, "2.4GHz 4K Uyumlu / USB-C", 100, "Nordic nRF52840", 0.5, "Kompakt Simetrik Fingertip", 3499.0, "Wraith Esports", 94, "S"),
        ("mouse_pulsar_x2h_wireless", "Pulsar X2H Wireless Yüksek Sırt Claw Grip Kablosuz Espor Faresi", "X2H Wireless", "PixArt PAW3395", 26000, 4000, "Pulsar Optik Switch", 54, "2.4GHz 4K / USB-C", 100, "Nordic nRF52840", 0.5, "Yüksek Arka Sırt (Avuç İçi Destekli Pençe)", 3599.0, "Wraith Esports", 95, "S"),
        ("mouse_pulsar_x2a_wireless", "Pulsar X2A Çift El Uyumlu Yan Düğmeli Kablosuz Espor Faresi", "X2A Wireless", "PixArt PAW3395", 26000, 4000, "Pulsar Optik Switch", 57, "2.4GHz 4K / USB-C", 100, "Nordic nRF52840", 0.5, "Tam Çift El Simetrik (Sol/Sağ Tuşlar)", 3599.0, "Wraith Esports", 93, "S"),
        ("mouse_pulsar_xlite_v3_es", "Pulsar Xlite V3 eS OLED Ekranlı Donanım Kontrollü 4K/8K Ergonomik Espor Faresi", "Xlite V3 eS", "PixArt PAW3395", 26000, 8000, "Pulsar Optik Switch", 65, "2.4GHz 4K Kablosuz / USB-C 8K Kablolu", 70, "Nordic nRF52840 + OLED Taban", 0.3, "Ergonomik Sağ El Avuç/Pençe", 4699.0, "Wraith Esports", 97, "S+"),
        ("mouse_pulsar_xlite_v3_medium", "Pulsar Xlite V3 Medium Deliksiz Katı Gövde 55g Ergonomik Kablosuz Fare", "Xlite V3 Medium", "PixArt PAW3395", 26000, 4000, "Optik Switch", 55, "2.4GHz 4K / USB-C", 100, "Nordic nRF52840", 0.5, "Ergonomik Orta Boy", 3499.0, "Wraith Esports", 94, "S"),
        ("mouse_lamzu_maya_x_8k", "Lamzu Maya X 8K Wireless Espor Faresi (PAW3950 / Nordic MCU)", "Maya X 8K", "PixArt PAW3950 (30000 DPI / 750 IPS)", 30000, 8000, "Omron Optik Switch", 47, "2.4GHz 8K Dongle Dahil / USB-C", 80, "Nordic nRF52840", 0.3, "Evrensel Simetrik (Her Tutuşa Uygun)", 4299.0, "Wraith Esports", 98, "S+"),
        ("mouse_lamzu_maya_4k", "Lamzu Maya 4K Ultra Hafif 45g Kablosuz Espor Faresi", "Maya 4K", "PixArt PAW3395", 26000, 4000, "Huano Transparent Blue Shell Pink Dot", 45, "2.4GHz 4K / USB-C", 80, "Nordic nRF52840", 0.4, "Kompakt Simetrik Claw/Fingertip", 3799.0, "Wraith Esports", 96, "S+"),
        ("mouse_lamzu_atlantis_og_v2_4k", "Lamzu Atlantis OG V2 Pro 4K Wireless Espor Faresi", "Atlantis OG V2 Pro 4K", "PixArt PAW3395", 26000, 4000, "Huano Pink Dot", 55, "2.4GHz 4K / USB-C Paracord", 70, "Nordic nRF52840", 0.5, "Agresif Pençe / Claw Grip Kralı", 3899.0, "Wraith Esports", 96, "S+"),
        ("mouse_lamzu_atlantis_mini_4k", "Lamzu Atlantis Mini 4K 49g Pençe Tutuşlu Kablosuz Espor Faresi", "Atlantis Mini 4K", "PixArt PAW3395", 26000, 4000, "Huano Pink Dot", 49, "2.4GHz 4K Dongle Dahil / USB-C", 70, "Nordic nRF52840", 0.45, "Kompakt Agresif Pençe", 3899.0, "Wraith Esports", 96, "S+"),
        ("mouse_lamzu_thorn_4k", "Lamzu Thorn 4K Ergonomik Kablosuz Espor Faresi (Fnatic Edition)", "Thorn 4K", "PixArt PAW3395", 26000, 4000, "Optik Switch", 52, "2.4GHz 4K Uyumlu / USB-C", 80, "Nordic nRF52840", 0.45, "Ergonomik Sağ El Avuç/Pençe", 4199.0, "Wraith Esports", 96, "S+"),
        ("mouse_lamzu_paro_8k", "Lamzu Paro 8K Kompakt Ergonomik PAW3950 45g Kablosuz Espor Faresi", "Paro 8K", "PixArt PAW3950", 30000, 8000, "Omron Optik Switch", 45, "2.4GHz 8K Dongle Dahil / USB-C", 80, "Nordic nRF52840", 0.25, "Kompakt Ergonomik", 4499.0, "Wraith Esports", 98, "S+"),
        ("mouse_finalmouse_ultralightx_lion", "Finalmouse UltralightX Lion (M) 35g Karbon Fiber 8000Hz Espor Faresi", "UltralightX Lion", "PixArt PAW3395 (Özel Kalibre)", 26000, 8000, "Kailh Blue Sword Özel Mekanik", 35, "2.4GHz 8K / USB-C", 70, "Nordic nRF52840", 0.25, "Simetrik Karbon Fiber Kompozit", None, None, 99, "S+"),
        ("mouse_finalmouse_ultralightx_cheetah", "Finalmouse UltralightX Cheetah (S) 31g Dünyanın En Hafif Karbon Faresi", "UltralightX Cheetah", "PixArt PAW3395", 26000, 8000, "Kailh Blue Sword", 31, "2.4GHz 8K / USB-C", 65, "Nordic nRF52840", 0.22, "Küçük Boy Saf Parmak Ucu", None, None, 100, "S+"),
        ("mouse_wlmouse_beast_x", "WLmouse Beast X Magnezyum CNC Alaşım 39g 8000Hz Kablosuz Fare", "Beast X", "PixArt PAW3395", 26000, 8000, "TTC Nihil Şeffaf Optik Switch", 39, "2.4GHz 8K Küp Dongle / USB-C", 65, "Nordic nRF52840", 0.28, "Magnezyum Dış İskelet Delikli Simetrik", 5499.0, "Wraith Esports", 98, "S+"),
        ("mouse_wlmouse_beast_x_max", "WLmouse Beast X Max Magnezyum 42g PAW3950 8000Hz Kablosuz Fare", "Beast X Max", "PixArt PAW3950", 30000, 8000, "Omron Optik Switch", 42, "2.4GHz 8K / USB-C", 75, "Nordic nRF52840", 0.22, "Genişletilmiş Simetrik Magnezyum", 6299.0, "Wraith Esports", 99, "S+"),
    ]
    for row in boutique:
        brand = row[1].split()[0]
        items.append(_make_mouse(row, brand))

    # 5. Endgame, VXE, ATK, Ninjutso, Scyrox, ZOWIE, Glorious, ASUS (18 items)
    esports_all = [
        ("mouse_endgame_gear_op1_8k", "Endgame Gear OP1 8K Dünyanın En Düşük Gecikmeli (0.125ms) Kablolu Espor Faresi", "OP1 8K", "PixArt PAW3395", 26000, 8000, "Kailh GX Mekanik (Hotswap Paket Destekli)", 50, "8000Hz Yüksek Hızlı USB Flex Kablo", None, "Nuvoton M483 MCU", 0.125, "Kompakt Dar Bel Simetrik Pençe", 3199.0, "Wraith Esports", 99, "S+"),
        ("mouse_endgame_gear_op1we", "Endgame Gear OP1we Kablosuz 58g Kailh GO Optik Switch Espor Faresi", "OP1we", "CompX CX52850 (PAW3370)", 19000, 1000, "Kailh GO Optik Switch", 58, "2.4GHz / USB-C", 70, "CompX", 0.7, "Kompakt Dar Bel Pençe", 3299.0, "Wraith Esports", 92, "A+"),
        ("mouse_endgame_gear_xm2we", "Endgame Gear XM2we Kablosuz 63g Efsanevi Pençe Tutuş Faresi", "XM2we", "PixArt PAW3370", 19000, 1000, "Kailh GO Optik", 63, "2.4GHz / USB-C", 80, "CompX", 0.8, "Efsanevi Geniş Arka Pençe", 3499.0, "Wraith Esports", 93, "S"),
        ("mouse_ninjutso_sora_v2", "Ninjutso Sora V2 Deliksiz 39g SnappyFire 8K Kablosuz Espor Faresi", "Sora V2", "PixArt PAW3395", 26000, 8000, "Omron Optik Switch", 39, "2.4GHz SnappyFire 8K Uyumlu / USB-C", 80, "Nordic nRF52840", 0.25, "Deliksiz Katı Karbon Fiber Kompozit", 4499.0, "Wraith Esports", 98, "S+"),
        ("mouse_vxe_r1_pro_max", "VXE Dragonfly R1 Pro Max F/P Canavarı 54g PAW3395 4K Kablosuz Fare", "Dragonfly R1 Pro Max", "PixArt PAW3395", 26000, 4000, "Kailh White Blade Dot / Huano Pink Dot", 54, "2.4GHz Smart Speed 4K / USB-C", 150, "Nordic nRF52840", 0.6, "Simetrik Her Tutuşa Uygun", 2399.0, "Wraith Esports", 93, "S"),
        ("mouse_vxe_mad_r_major", "VXE Mad R Major 36g Ultra Hafif PAW3950 8000Hz Kablosuz Fare", "Mad R Major", "PixArt PAW3950", 30000, 8000, "Omron Optik Switch", 36, "2.4GHz 8K Dongle Dahil / USB-C", 70, "Nordic nRF52840", 0.28, "Deliksiz Katı Gövde 36g Tüy Siklet", 2999.0, "Wraith Esports", 97, "S+"),
        ("mouse_vgn_dragonfly_f1_pro_max", "VGN Dragonfly F1 Pro Max 54g PAW3395 4K Smart Speed Kablosuz Fare", "Dragonfly F1 Pro Max", "PixArt PAW3395", 26000, 4000, "Kailh Golden Black Mamba 80M", 54, "2.4GHz Smart Speed 4K / USB-C", 130, "Nordic nRF52840", 0.5, "Simetrik Düşük Profil", 2499.0, "Wraith Esports", 92, "A+"),
        ("mouse_atk_blazing_sky_f1_ultimate", "ATK Blazing Sky F1 Ultimate 38g PAW3950 Ultra 8000Hz Espor Faresi", "Blazing Sky F1 Ultimate", "PixArt PAW3950 Ultra (30000 DPI / 750 IPS)", 30000, 8000, "Omron Optik Switch", 38, "2.4GHz 8000Hz Kablosuz / USB-C", 75, "Nordic nRF52840", 0.22, "Simetrik Agresif Espor Pençe", 3799.0, "Wraith Esports", 98, "S+"),
        ("mouse_atk_blazing_sky_x1_ultra", "ATK Blazing Sky X1 Ultra 49g PAW3950 Viper V3 Şekilli 8K Espor Faresi", "Blazing Sky X1 Ultra", "PixArt PAW3950", 30000, 8000, "Omron Optik", 49, "2.4GHz 8K / USB-C", 75, "Nordic nRF52840", 0.25, "Simetrik Yüksek Sırt", 3899.0, "Wraith Esports", 97, "S+"),
        ("mouse_scyrox_v8_8k", "Scyrox V8 8K 36g PAW3950 Omron Optik 8000Hz Kablosuz Espor Faresi", "V8 8K", "PixArt PAW3950", 30000, 8000, "Omron Optik Switch", 36, "2.4GHz 8K Dongle Dahil / USB-C", 70, "Nordic nRF52840", 0.25, "Yüksek Sırt Pençe / Claw Grip", 3499.0, "Wraith Esports", 98, "S+"),
        ("mouse_zaopin_z1_pro", "Zaopin Z1 Pro 46g Yumurta Şekilli Parmak Ucu (Fingertip) Kablosuz Fare", "Z1 Pro", "PixArt PAW3395", 26000, 1000, "Huano Pink Dot", 46, "2.4GHz / USB-C", 80, "Nordic nRF52840", 0.7, "Yumurta / Egg (Fingertip)", 2299.0, "Wraith Esports", 91, "A+"),
        ("mouse_zaopin_z2_4k", "Zaopin Z2 4K Modüler Hotswap Switch Yuvalı Ergonomik Espor Faresi", "Z2 4K", "PixArt PAW3395", 26000, 4000, "Değiştirilebilir Hotswap Switch Soketi", 65, "2.4GHz 4K / USB-C", 80, "Nordic nRF52840", 0.5, "Ergonomik Sağ El", 3199.0, "Wraith Esports", 93, "S"),
        ("mouse_darmoshark_m3_pro_4k", "Darmoshark M3 Pro 4K 55g PAW3395 Nordic 52840 Espor Faresi", "M3 Pro 4K", "PixArt PAW3395", 26000, 4000, "TTC Falcon Optik / Huano Pink Dot", 55, "2.4GHz 4K / Bluetooth / USB-C", 140, "Nordic nRF52840", 0.55, "Büyük El Simetrik (Geniş ve Rahat)", 2599.0, "Wraith Esports", 93, "S"),
        ("mouse_zowie_ec2_cw", "BenQ ZOWIE EC2-CW Kablosuz Espor Ergonomik Turnuva Faresi", "EC2-CW", "PixArt PAW3370 Espor Özel Ayarlı", 3200, 1000, "Huano 20M Özel Kalibre Espor Switch", 77, "Gelişmiş Kablosuz Verici İstasyonu / USB-C", 70, "ZOWIE Espor MCU", 1.1, "Ergonomik Espor Referans Standardı", 5499.0, "Tebilon", 93, "S"),
        ("mouse_zowie_u2", "BenQ ZOWIE U2 İçe Kavisli Çevik Espor Pençe Tutuşlu Kablosuz Fare", "U2", "PixArt PAW3395", 3200, 1000, "Huano Mekanik", 60, "Çift Yönlü Kablosuz Baz İstasyonu / USB-C", 70, "ZOWIE Espor MCU", 0.8, "İçe Kavisli Yanlar (Çevik Espor Pençe)", 5799.0, "Tebilon", 95, "S"),
        ("mouse_glorious_model_o_2_wireless", "Glorious Model O 2 Wireless BAMF 2.0 26K 210 Saat Pil Kablosuz Fare", "Model O 2 Wireless", "BAMF 2.0 26K", 26000, 1000, "Glorious 80M Mekanik Switch", 68, "2.4GHz / Bluetooth 5.2 / USB-C", 210, "Glorious MCU", 1.2, "Simetrik Düşük Profil Petekli Gövde", 2999.0, "İtopya", 87, "A"),
        ("mouse_asus_rog_keris_ii_ace", "ASUS ROG Keris II Ace 54g AimPoint Pro 42K 8000Hz Polling Booster Kablosuz Fare", "ROG Keris II Ace", "ROG AimPoint Pro (42000 DPI / 750 IPS)", 42000, 8000, "ROG 100M Optik Mikro Switch", 54, "ROG SpeedNova 2.4GHz 8K / BT / USB-C", 107, "ROG 8K Polling Booster", 0.25, "Ergonomik Sağ El Espor Tasarımı", 5699.0, "İtopya", 97, "S+"),
        ("mouse_asus_rog_harpe_ace", "ASUS ROG Harpe Ace Aim Lab Edition 54g AimPoint 36K Kablosuz Espor Faresi", "ROG Harpe Ace Aim Lab Edition", "ROG AimPoint (36000 DPI / 650 IPS)", 36000, 1000, "ROG 70M Mekanik", 54, "ROG SpeedNova 2.4GHz / BT / USB-C", 90, "ROG MCU", 0.6, "Biyo-Bazlı Naylon Gövde Simetrik", 4299.0, "İtopya", 95, "S"),
    ]
    for row in esports_all:
        brand = row[1].split()[0]
        items.append(_make_mouse(row, brand))

    # Extra modern 2022-2026 mice from catalog_mice_extra
    try:
        from catalog_mice_extra import EXTRA_MICE_DATA
        for row in EXTRA_MICE_DATA:
            brand = row[1].split()[0]
            items.append(_make_mouse(row, brand))
    except ImportError:
        pass

    return items

def _make_mouse(row, brand):
    uid, name, model, sensor, max_dpi, polling, switch, weight, conn, battery, mcu, latency, shape, price, store, score, tier = row
    if not uid.startswith("mouse_"):
        uid = f"mouse_{uid}"
    return {
        "id": uid,
        "category": "MOUSE",
        "name": name,
        "brand": brand,
        "model": model,
        "sensor": sensor,
        "max_dpi": max_dpi,
        "polling_rate_hz": polling,
        "switch_type": switch,
        "weight_g": weight,
        "connectivity": conn,
        "battery_hours": battery,
        "mcu": mcu,
        "click_latency_ms": latency,
        "shape_type": shape,
        "current_price_try": price,
        "lowest_price_try": price,
        "highest_price_try": price,
        "store_name": store,
        "store_url": f"https://www.{store.lower().replace(' ', '')}.com" if store else None,
        "benchmark_score": score,
        "benchmark_tier": tier,
    }

def get_all_keyboards():
    items = []
    from catalog_keyboards import KEYBOARDS_DATA_ROWS
    for row in KEYBOARDS_DATA_ROWS:
        brand = row[1].split()[0]
        items.append(_make_keyboard(row, brand))
    return items

def _make_keyboard(row, brand):
    uid, name, model, switch, is_he, rapid_trig, min_act, max_act, polling, layout, hot_swap, case_mat, conn, price, store, score, tier = row
    if not uid.startswith("keyboard_"):
        uid = f"keyboard_{uid}"
    return {
        "id": uid,
        "category": "KEYBOARD",
        "name": name,
        "brand": brand,
        "model": model,
        "switch_type": switch,
        "is_hall_effect": is_he,
        "rapid_trigger_supported": rapid_trig,
        "min_actuation_mm": min_act,
        "max_actuation_mm": max_act,
        "polling_rate_hz": polling,
        "layout": layout,
        "hot_swap": hot_swap,
        "case_material": case_mat,
        "connectivity": conn,
        "current_price_try": price,
        "lowest_price_try": price,
        "highest_price_try": price,
        "store_name": store,
        "store_url": f"https://www.{store.lower().replace(' ', '')}.com" if store else None,
        "benchmark_score": score,
        "benchmark_tier": tier,
    }

def get_all_headsets():
    items = []
    from catalog_headsets import HEADSETS_DATA_ROWS
    for row in HEADSETS_DATA_ROWS:
        brand = row[1].split()[0]
        items.append(_make_headset(row, brand))
    return items

def _make_headset(row, brand):
    uid, name, model, driver, driver_size, imp, freq, dac_amp, mic, isolation, conn, price, store, score, tier = row
    if not uid.startswith("headset_"):
        uid = f"headset_{uid}"
    return {
        "id": uid,
        "category": "HEADSET",
        "name": name,
        "brand": brand,
        "model": model,
        "driver_type": driver,
        "driver_size_mm": driver_size,
        "impedance_ohm": imp,
        "frequency_response": freq,
        "dac_amp_required": dac_amp,
        "mic_type": mic,
        "sound_isolation": isolation,
        "connectivity": conn,
        "current_price_try": price,
        "lowest_price_try": price,
        "highest_price_try": price,
        "store_name": store,
        "store_url": f"https://www.{store.lower().replace(' ', '')}.com" if store else None,
        "benchmark_score": score,
        "benchmark_tier": tier,
    }

print("Loaded all massive catalog peripheral generators.")

