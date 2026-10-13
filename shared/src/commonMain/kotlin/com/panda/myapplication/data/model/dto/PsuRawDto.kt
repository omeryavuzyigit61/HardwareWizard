package com.panda.myapplication.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class PsuRawDto(
    val name: String? = null,
    val price: Double? = null,
    val type: String? = null,
    val efficiency: String? = null,
    val wattage: Int? = null,
    val color: String? = null,
)
