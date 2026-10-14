package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoolerRawDto(
    val name: String? = null,
    val price: Double? = null,
    val size: Int? = null,
    val color: String? = null,
    val type: String? = null,
    @SerialName("height") val heightMm: Int? = null,
    @SerialName("fan_rpm") val fanRpm: String? = null,
    @SerialName("noise_level") val noiseLevel: Double? = null,
    @SerialName("sockets") val supportedSockets: List<String>? = null,
    @SerialName("max_tdp") val maxTdp: Int? = null,
    val imageUrl: String? = null,
)
