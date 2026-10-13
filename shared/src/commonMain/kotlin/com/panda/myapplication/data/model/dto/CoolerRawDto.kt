package com.panda.myapplication.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class CoolerRawDto(
    val name: String? = null,
    val price: Double? = null,
    val size: Int? = null,
    val color: String? = null,
)
