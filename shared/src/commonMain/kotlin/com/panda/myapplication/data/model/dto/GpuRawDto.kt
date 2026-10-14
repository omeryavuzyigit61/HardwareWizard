package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GpuRawDto(
    val name: String? = null,
    val price: Double? = null,
    val chipset: String? = null,
    @Serializable(with = LenientIntSerializer::class)
    val memory: Int? = null,
    @SerialName("core_clock")
    @Serializable(with = LenientIntSerializer::class)
    val coreClockMhz: Int? = null,
    @SerialName("boost_clock")
    @Serializable(with = LenientIntSerializer::class)
    val boostClockMhz: Int? = null,
    val color: String? = null,
    @Serializable(with = LenientDoubleSerializer::class)
    val length: Double? = null,
    @SerialName("memory_type") val memoryType: String? = null,
    @SerialName("bus_width") val busWidthBits: Int? = null,
    @SerialName("tdp") val tdpWatts: Int? = null,
    @SerialName("power_connectors") val powerConnectors: String? = null,
    @SerialName("display_outputs") val displayOutputs: String? = null,
    @SerialName("slot_width") val slotWidth: String? = null,
    @SerialName("recommended_psu") val recommendedPsuWatts: Int? = null,
)
