package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PrebuiltSpec(
    val storeName: String,
    val cpuModel: String,
    val gpuModel: String,
    val motherboardModel: String,
    val ramSpec: String,
    val storageSpec: String,
    val psuCaseSpec: String,
    val estimatedSeparatePriceTry: Double? = null,
    val priceAdvantageTry: Double? = null,
    val fps1080p: Int = 0,
    val fps1440p: Int = 0,
    val fps4k: Int = 0,
    val gamingScore: Int = 0,
    val cpuId: String? = null,
    val gpuId: String? = null,
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.PREBUILT_PC

    val advantagePercent: Double
        get() = if (estimatedSeparatePriceTry != null && estimatedSeparatePriceTry > 0 && priceAdvantageTry != null) {
            (priceAdvantageTry / estimatedSeparatePriceTry) * 100.0
        } else 0.0

    val isAdvantageous: Boolean get() = (priceAdvantageTry ?: 0.0) > 0.0
}

