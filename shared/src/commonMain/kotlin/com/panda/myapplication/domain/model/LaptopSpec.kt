package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LaptopSpec(
    val brand: String = "",
    val cpuModel: String,
    val gpuModel: String,
    val gpuWattTgp: String,
    val muxSwitch: String,
    val ramSpec: String,
    val storageSpec: String,
    val displaySpec: String,
    val batteryWh: Int,
    val weightKg: Double,
    val coolingTech: String,
    val gamingScore: Int,
    val fps1080p: Int = 0,
    val fps1440p: Int = 0,
    // 6 Ana Donanım Bileşeni Detayları
    val cpuCores: String = "",
    val cpuClockGhz: String = "",
    val screenResolution: String = "",
    val refreshRateHz: Int = 0,
    val panelTech: String = "",
    val colorGamut: String = "",
    val screenSizeInch: Double = 0.0,
    val ramSlots: String = "",
    val storageSlots: String = "",
    val chargerWatt: Int = 0,
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.LAPTOP

    val effectiveCpuCores: String
        get() = cpuCores

    val effectiveCpuClockGhz: String
        get() = cpuClockGhz

    val effectiveRamSlots: String
        get() = ramSlots

    val effectiveStorageSlots: String
        get() = storageSlots

    val effectiveChargerWatt: String
        get() = if (chargerWatt > 0) "${chargerWatt}W" else ""

    val effectivePanelDetails: String
        get() = when {
            screenResolution.isNotBlank() && refreshRateHz > 0 ->
                listOfNotNull(
                    screenSizeInch.takeIf { it > 0.0 }?.let { "${it}\"" },
                    screenResolution.ifBlank { null },
                    "${refreshRateHz}Hz",
                    panelTech.ifBlank { null },
                    colorGamut.ifBlank { null },
                ).joinToString(" • ")
            else -> displaySpec
        }
}

