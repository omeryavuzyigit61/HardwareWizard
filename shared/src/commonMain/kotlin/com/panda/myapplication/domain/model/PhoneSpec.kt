package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PhoneSpec(
    // 1. Temel Donanım & Yonga Seti (SoC)
    val chipset: String,              // örn: "Apple A18 Pro", "Snapdragon 8 Gen 3 for Galaxy"
    val cpuArch: String = "",         // örn: "6 Çekirdek (2P + 4E)", "8 Çekirdek (1+3+2+2)"
    val gpuModel: String = "",        // örn: "Apple 6-Core GPU", "Adreno 750"
    val processNodeNm: Int = 3,       // örn: 3, 4 (nm)
    val ramGb: Int = 8,               // örn: 8, 12, 16 (GB)
    val ramType: String = "LPDDR5X",  // örn: "LPDDR5X"
    val storageGb: Int = 256,         // örn: 128, 256, 512, 1024 (GB)
    val storageType: String = "UFS 4.0", // örn: "NVMe", "UFS 4.0", "UFS 3.1"
    val hasMicroSd: Boolean = false,

    // 2. Sentetik Benchmark Skorları (Resmi & Güvenilir)
    val antutuScore: Int? = null,     // örn: 1820000, 1980000
    val geekbenchSingle: Int? = null, // örn: 3420, 2280
    val geekbenchMulti: Int? = null,  // örn: 8450, 7150
    val stabilityPercent: Int? = null,// örn: 68, 62 (% 3DMark Termal Kararlılık)

    // 3. Ekran Özellikleri (Display)
    val displaySizeInch: Double = 6.8, // örn: 6.9, 6.8
    val displayTech: String = "OLED",  // örn: "LTPO Super Retina XDR OLED", "Dynamic AMOLED 2X"
    val resolution: String = "FHD+",   // örn: "1320 x 2868", "1440 x 3120"
    val ppi: Int = 460,                // örn: 460, 505
    val refreshRateHz: Int = 120,      // örn: 120, 144
    val peakBrightnessNit: Int = 2000, // örn: 2000, 2600
    val glassProtection: String = "Gorilla Glass", // örn: "Ceramic Shield 2", "Gorilla Glass Armor"
    val hdrSupport: String = "Dolby Vision / HDR10+",

    // 4. Kamera Sistemi (Epey.com Derinliği)
    val mainCameraMp: Int = 50,        // örn: 48, 200, 50
    val mainSensorSize: String = "1/1.3 inç", // örn: "1/1.28 inç", "1/1.3 inç", "1.0 inç"
    val mainAperture: String = "f/1.8", // örn: "f/1.78", "f/1.7"
    val hasOis: Boolean = true,
    val oisType: String = "Sensör Kaydırmalı OIS", // veya "OIS"
    val telephotoMp: Int? = null,      // örn: 12, 50
    val opticalZoomX: Double? = null,  // örn: 5.0, 3.0
    val ultraWideMp: Int? = null,      // örn: 48, 12
    val videoResolution: String = "4K 60 FPS", // örn: "4K 120 FPS Dolby Vision", "8K 30 FPS"
    val selfieCameraMp: Int = 12,

    // 5. Batarya & Şarj
    val batteryMah: Int = 5000,        // örn: 4685, 5000
    val wiredChargingWatt: Int = 45,   // örn: 27, 45, 120
    val wirelessChargingWatt: Int? = null, // örn: 25, 15
    val hasReverseCharging: Boolean = false,
    val chargingTimeToFullMin: Int? = null, // örn: 65, 30

    // 6. Tasarım & Dayanıklılık
    val weightGrams: Int = 220,        // örn: 227, 232
    val thicknessMm: Double = 8.2,     // örn: 8.25, 8.6
    val frameMaterial: String = "Titanyum", // örn: "Titanyum", "Zırhlı Alüminyum"
    val waterResistance: String = "IP68",   // örn: "IP68 (6m)", "IP68 (1.5m)"
    val hasStylus: Boolean = false,

    // 7. Yazılım & Destek
    val osName: String = "Android 14", // örn: "iOS 18", "Android 14 (One UI 6.1)"
    val updateYears: Int = 7,          // örn: 7, 6

    // 8. Oyun Yetenek Rozetleri (Uydurma FPS yerine resmi tavanlar)
    val pubgFpsSupport: String = "120 FPS", // örn: "120 FPS", "90 FPS"
    val genshinStabilityScore: String = "%70+ Kararlı",

    // 9. Donanım & Fiyat Varyantları (Depolama & RAM Seçenekleri)
    val variants: List<DeviceVariant> = emptyList(),
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.SMARTPHONE
}
