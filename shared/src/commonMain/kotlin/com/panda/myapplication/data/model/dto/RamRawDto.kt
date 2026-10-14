package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RamRawDto(
    val name: String? = null,
    val price: Double? = null,
    @Serializable(with = FlexibleSpeedListSerializer::class)
    val speed: List<Int>? = null,
    @Serializable(with = FlexibleModulesListSerializer::class)
    val modules: List<Int>? = null,
    @SerialName("price_per_gb") val pricePerGb: Double? = null,
    val color: String? = null,
    @SerialName("first_word_latency") val firstWordLatency: Double? = null,
    @SerialName("cas_latency")
    @Serializable(with = LenientIntSerializer::class)
    val casLatency: Int? = null,
    val voltage: Double? = null,
    @SerialName("heat_spreader") val heatSpreader: Boolean? = null,
    val rgb: Boolean? = null,
    val imageUrl: String? = null,
)
