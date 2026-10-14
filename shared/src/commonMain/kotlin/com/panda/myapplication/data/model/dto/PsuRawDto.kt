package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PsuRawDto(
    val name: String? = null,
    val price: Double? = null,
    val type: String? = null,
    val efficiency: String? = null,
    val wattage: Int? = null,
    val color: String? = null,
    val modularity: String? = null,
    val outputs: String? = null,
    @SerialName("tier") val tierRating: String? = null,
    val capacitors: String? = null,
)
