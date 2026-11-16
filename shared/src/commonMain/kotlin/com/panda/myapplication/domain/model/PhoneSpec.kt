package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PhoneSpec(
    // 1. Temel Donanım & Yonga Seti (SoC)
    val chipset: String,
    val cpuArch: String = "",
    val gpuModel: String = "",
    val processNodeNm: Int = 0,
    val ramGb: Int = 0,
    val ramType: String = "",
    val storageGb: Int = 0,
    val storageType: String = "",
    val hasMicroSd: Boolean = false,

    // 2. Sentetik Benchmark & DXOMARK Skorları
    val antutuScore: Int? = null,
    val geekbenchSingle: Int? = null,
    val geekbenchMulti: Int? = null,
    val stabilityPercent: Int? = null,
    val dxomarkCamera: Int? = null,
    val dxomarkSelfie: Int? = null,
    val dxomarkDisplay: Int? = null,
    val dxomarkBattery: Int? = null,
    val dxomarkAudio: Int? = null,

    // 3. Ekran Özellikleri (Display)
    val displaySizeInch: Double = 0.0,
    val displayTech: String = "",
    val resolution: String = "",
    val ppi: Int = 0,
    val refreshRateHz: Int = 0,
    val peakBrightnessNit: Int = 0,
    val glassProtection: String = "",
    val hdrSupport: String = "",
    val pwmDimmingHz: Int? = null,

    // 4. Kamera Sistemi
    val mainCameraMp: Int = 0,
    val mainSensorSize: String = "",
    val mainAperture: String = "",
    val hasOis: Boolean = false,
    val oisType: String = "",
    val telephotoMp: Int? = null,
    val opticalZoomX: Double? = null,
    val ultraWideMp: Int? = null,
    val videoResolution: String = "",
    val selfieCameraMp: Int = 0,

    // 5. Batarya & Şarj
    val batteryMah: Int = 0,
    val wiredChargingWatt: Int = 0,
    val wirelessChargingWatt: Int? = null,
    val hasReverseCharging: Boolean = false,
    val chargingTimeToFullMin: Int? = null,

    // 6. Tasarım, Dayanıklılık & Bağlantı
    val weightGrams: Int = 0,
    val thicknessMm: Double = 0.0,
    val frameMaterial: String = "",
    val waterResistance: String = "",
    val hasStylus: Boolean = false,
    val usbSpeed: String? = null,
    val wifiBluetooth: String? = null,

    // 7. Yazılım & Destek
    val osName: String = "",
    val updateYears: Int = 0,

    // 8. Oyun Yetenek Rozetleri (Yalnız doğrulanmış resmi tavanlar)
    val pubgFpsSupport: String = "",
    val genshinStabilityScore: String = "",

    // 9. Donanım & Fiyat Varyantları (Depolama & RAM Seçenekleri)
    val variants: List<DeviceVariant> = emptyList(),
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.SMARTPHONE
}
