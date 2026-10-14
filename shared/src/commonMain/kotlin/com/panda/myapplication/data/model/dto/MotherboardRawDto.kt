package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MotherboardRawDto(
    val name: String? = null,
    val price: Double? = null,
    val socket: String? = null,
    @SerialName("form_factor") val formFactor: String? = null,
    @SerialName("max_memory") val maxMemoryGb: Int? = null,
    @SerialName("memory_slots") val memorySlots: Int? = null,
    val color: String? = null,
    val chipset: String? = null,
    @SerialName("memory_type") val memoryType: String? = null,
    @SerialName("m2_slots") val m2Slots: Int? = null,
    @SerialName("pcie_slots") val pcieSlots: Int? = null,
    @SerialName("sata_ports") val sataPorts: Int? = null,
    @SerialName("pcie_generation") val pcieGeneration: String? = null,
    @SerialName("vrm_phases") val vrmPhases: String? = null,
    @SerialName("interface_ports") val interfacePorts: String? = null,
    @SerialName("has_wifi") val hasWifi: Boolean? = null,
)
