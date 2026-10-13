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
)
