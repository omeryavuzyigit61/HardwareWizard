package com.panda.myapplication.data.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrebuiltRawDto(
    val id: String? = null,
    val name: String? = null,
    val brand: String? = null,
    val storeName: String? = null,
    val price: Double? = null,
    val priceTry: Double? = null,
    val url: String? = null,
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    val cpuModel: String? = null,
    val gpuModel: String? = null,
    val motherboardModel: String? = null,
    val ramSpec: String? = null,
    val storageSpec: String? = null,
    val psuCaseSpec: String? = null,
    val estimatedSeparatePriceTry: Double? = null,
    val priceAdvantageTry: Double? = null,
    val fps1080p: Int? = null,
    val fps1440p: Int? = null,
    val fps4k: Int? = null,
    val gamingScore: Int? = null,
    val cpuId: String? = null,
    val gpuId: String? = null,
)
