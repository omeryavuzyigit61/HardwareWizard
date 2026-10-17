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
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MONITOR
}
