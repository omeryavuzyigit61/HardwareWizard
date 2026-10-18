package com.panda.myapplication.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class LaptopRawDto(
    val id: String? = null,
    val name: String? = null,
    val brand: String? = null,
    val price: Double? = null,
    val priceTry: Double? = null,
    val url: String? = null,
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    val cpuModel: String? = null,
    val gpuModel: String? = null,
    val gpuWattTgp: String? = null,
    val muxSwitch: String? = null,
    val ramSpec: String? = null,
    val storageSpec: String? = null,
    val displaySpec: String? = null,
    val batteryWh: Int? = null,
    val weightKg: Double? = null,
    val coolingTech: String? = null,
    val gamingScore: Int? = null,
    val fps1080p: Int? = null,
    val fps1440p: Int? = null,
    val cpuCores: String? = null,
    val cpuClockGhz: String? = null,
    val screenResolution: String? = null,
    val refreshRateHz: Int? = null,
    val panelTech: String? = null,
    val colorGamut: String? = null,
    val screenSizeInch: Double? = null,
    val ramSlots: String? = null,
    val storageSlots: String? = null,
    val chargerWatt: Int? = null,
)
