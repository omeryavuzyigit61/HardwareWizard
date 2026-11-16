package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TabletSpec(
    // 1. Temel Donanım & Yonga Seti (SoC)
    val chipset: String,
    val cpuArch: String = "",
    val gpuModel: String = "",
    val processNodeNm: Int = 0,
    val ramGb: Int = 0,
    val storageGb: Int = 0,
    val storageType: String = "",
    val hasMicroSd: Boolean = false,

    // 2. Sentetik Benchmark Skorları
    val antutuScore: Int? = null,
    val geekbenchSingle: Int? = null,
    val geekbenchMulti: Int? = null,
    val stabilityPercent: Int? = null,

    // 3. Ekran Özellikleri
    val displaySizeInch: Double = 0.0,
    val displayTech: String = "",
    val resolution: String = "",
    val ppi: Int = 0,
    val refreshRateHz: Int = 0,
    val peakBrightnessNit: Int = 0,
    val stylusSupport: String = "",
    val aspectRatio: String? = null,

    // 4. Kamera & Ses
    val mainCameraMp: Int = 0,
    val frontCameraMp: Int = 0,
    val speakerCount: Int = 0,
    val hasDolbyAtmos: Boolean = false,

    // 5. Batarya & Şarj
    val batteryMah: Int = 0,
    val wiredChargingWatt: Int = 0,
    val wirelessChargingWatt: Int? = null,

    // 6. Tasarım & Ağırlık
    val weightGrams: Int = 0,
    val thicknessMm: Double = 0.0,
    val bodyMaterial: String = "",

    // 7. Yazılım & Bağlantı
    val osName: String = "",
    val updateYears: Int = 0,
    val cellularOption: String = "",
    val usbSpeed: String? = null,
    val externalDisplaySupport: String? = null,

    // 8. Donanım & Fiyat Varyantları
    val variants: List<DeviceVariant> = emptyList(),
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.TABLET
}
