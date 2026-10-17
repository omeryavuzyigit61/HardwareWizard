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
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.LAPTOP
}
