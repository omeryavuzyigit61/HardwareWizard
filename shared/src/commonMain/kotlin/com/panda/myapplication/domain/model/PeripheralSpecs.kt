package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain models for Gaming Peripherals (BÖLÜM 1: Çevre Birimleri Kataloğu):
 * - [MouseSpec]: Oyuncu Faresi
 * - [KeyboardSpec]: Mekanik & Manyetik (Hall Effect) Klavye
 * - [HeadsetSpec]: Oyuncu & Stüdyo Kulaklığı
 * - [MonitorSpec]: Oyuncu Monitörü
 */

@Serializable
data class MouseSpec(
    val sensor: String,
    val maxDpi: Int,
    val pollingRateHz: Int,
    val switchType: String,
    val weightG: Int,
    val connectivity: String,
    val batteryHours: Int? = null,
    val mcu: String = "Nordic 52840",
    val clickLatencyMs: Double = 0.8,
    val shapeType: String = "Simetrik",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MOUSE
}

@Serializable
data class KeyboardSpec(
    val switchType: String,
    val rapidTriggerSupported: Boolean,
    val minActuationMm: Double,
    val pollingRateHz: Int,
    val layout: String,
    val hotSwap: Boolean,
    val connectivity: String,
    val maxActuationMm: Double = 4.0,
    val caseMaterial: String = "Alüminyum CNC / ABS",
    val isHallEffect: Boolean = false,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.KEYBOARD
}

@Serializable
data class HeadsetSpec(
    val driverType: String,
    val driverSizeMm: Int,
    val impedanceOhm: Int,
    val frequencyResponse: String,
    val dacAmpRequired: Boolean,
    val micType: String,
    val weightG: Int,
    val connectivity: String = "2.4GHz / Bluetooth / 3.5mm",
    val soundIsolation: String = "Pasif / ANC",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.HEADSET
}

@Serializable
data class MonitorSpec(
    val panelType: String,
    val screenSizeInch: Double,
    val resolution: String,
    val refreshRateHz: Int,
    val responseSpeedGtGMs: Double,
    val ppi: Int,
    val hdrCertification: String,
    val bitDepth: String = "8-bit + FRC",
    val responseMprtMs: Double? = 0.5,
    val brightnessSdrNit: Int = 350,
    val brightnessHdrNit: Int = 400,
    val colorGamut: String = "%100 sRGB, %98 DCI-P3",
    val vrrSupport: String = "G-Sync Compatible / FreeSync Premium Pro",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MONITOR
}

