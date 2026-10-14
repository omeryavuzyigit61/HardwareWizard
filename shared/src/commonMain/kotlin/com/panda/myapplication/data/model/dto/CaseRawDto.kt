package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CaseRawDto(
    val name: String? = null,
    val price: Double? = null,
    val type: String? = null,
    val color: String? = null,
    @SerialName("side_panel") val sidePanel: String? = null,
    @SerialName("external_volume") val externalVolume: Double? = null,
    @SerialName("max_gpu_length") val maxGpuLengthMm: Int? = null,
    @SerialName("max_cpu_cooler_height") val maxCpuCoolerHeightMm: Int? = null,
    @SerialName("max_radiator_size") val maxRadiatorSizeMm: Int? = null,
    @SerialName("drive_bays") val driveBays: String? = null,
    val imageUrl: String? = null,
)
