package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CpuRawDto(
    val name: String? = null,
    val price: Double? = null,
    @SerialName("core_count") val coreCount: Int? = null,
    @SerialName("core_clock") val coreClockGhz: Double? = null,
    @SerialName("boost_clock") val boostClockGhz: Double? = null,
    val microarchitecture: String? = null,
    val tdp: Int? = null,
    val graphics: String? = null,
    val socket: String? = null,
)
