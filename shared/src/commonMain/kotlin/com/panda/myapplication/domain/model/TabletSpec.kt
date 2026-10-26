package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TabletSpec(
    // 1. Temel Donanım & Yonga Seti (SoC)
    val chipset: String,              // örn: "Apple M4", "Dimensity 9300+", "Snapdragon 8 Gen 2"
    val cpuArch: String = "",         // örn: "9 Çekirdek (3P + 6E)"
    val gpuModel: String = "",        // örn: "Apple 10-Core GPU", "Immortalis-G720"
    val processNodeNm: Int = 3,
    val ramGb: Int = 8,               // örn: 8, 12, 16 (GB)
    val storageGb: Int = 256,         // örn: 128, 256, 512, 1024, 2048 (GB)
    val storageType: String = "NVMe", // örn: "NVMe", "UFS 4.0"
    val hasMicroSd: Boolean = false,

    // 2. Sentetik Benchmark Skorları
    val antutuScore: Int? = null,     // örn: 2750000, 2100000
    val geekbenchSingle: Int? = null, // örn: 3750, 2200
    val geekbenchMulti: Int? = null,  // örn: 14500, 7500
    val stabilityPercent: Int? = null,// örn: 82 (% 3DMark Termal Kararlılık)

    // 3. Ekran Özellikleri
    val displaySizeInch: Double = 11.0, // örn: 13.0, 11.0, 8.3
    val displayTech: String = "OLED",   // örn: "Tandem OLED (Ultra Retina XDR)", "Dynamic AMOLED 2X", "Liquid Retina IPS"
    val resolution: String = "2064 x 2752",
    val ppi: Int = 264,
    val refreshRateHz: Int = 120,      // örn: 120, 60
    val peakBrightnessNit: Int = 1000, // örn: 1600, 1000, 500
    val stylusSupport: String = "Apple Pencil Pro", // örn: "Apple Pencil Pro", "Dahili S-Pen", "Xiaomi Smart Pen"

    // 4. Kamera & Ses
    val mainCameraMp: Int = 12,
    val frontCameraMp: Int = 12,
    val speakerCount: Int = 4,        // örn: 4 Hoparlör Stereo
    val hasDolbyAtmos: Boolean = true,

    // 5. Batarya & Şarj
    val batteryMah: Int = 10000,      // örn: 10290, 11200, 5124
    val wiredChargingWatt: Int = 35,  // örn: 35, 45, 67
    val wirelessChargingWatt: Int? = null,

    // 6. Tasarım & Ağırlık
    val weightGrams: Int = 550,       // örn: 579, 718, 293
    val thicknessMm: Double = 5.5,    // örn: 5.1 mm
    val bodyMaterial: String = "Alüminyum Kasa",

    // 7. Yazılım & Bağlantı
    val osName: String = "iPadOS 18", // örn: "iPadOS 18", "Android 14 (One UI 6.1)"
    val updateYears: Int = 6,
    val cellularOption: String = "Wi-Fi + 5G", // örn: "Wi-Fi Only", "Wi-Fi + 5G"

    // 8. Donanım & Fiyat Varyantları
    val variants: List<DeviceVariant> = emptyList(),
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.TABLET
}
