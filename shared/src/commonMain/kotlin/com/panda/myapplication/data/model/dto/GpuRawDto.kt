package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GpuRawDto(
    val name: String? = null,
    val price: Double? = null,
    val chipset: String? = null,
    @Serializable(with = LenientIntSerializer::class)
    val memory: Int? = null,
    @SerialName("core_clock")
    @Serializable(with = LenientIntSerializer::class)
    val coreClockMhz: Int? = null,
    @SerialName("boost_clock")
    @Serializable(with = LenientIntSerializer::class)
    val boostClockMhz: Int? = null,
    val color: String? = null,
    @Serializable(with = LenientDoubleSerializer::class)
    val length: Double? = null,
    @SerialName("memory_type") val memoryType: String? = null,
    @SerialName("bus_width") val busWidthBits: Int? = null,
    @SerialName("tdp") val tdpWatts: Int? = null,
    @SerialName("power_connectors") val powerConnectors: String? = null,
    @SerialName("display_outputs") val displayOutputs: String? = null,
    @SerialName("slot_width") val slotWidth: String? = null,
    @SerialName("recommended_psu") val recommendedPsuWatts: Int? = null,
    val architecture: String? = null,
    @SerialName("gpu_die") val gpuDie: String? = null,
    @SerialName("process_node") val processNode: String? = null,
    @SerialName("cuda_cores") val cudaCores: Int? = null,
    @SerialName("tensor_ai_tops") val tensorAiTops: String? = null,
    @SerialName("rt_cores") val rtCores: Int? = null,
    @SerialName("memory_speed") val memorySpeedGbps: Double? = null,
    @SerialName("bandwidth_gbs") val bandwidthGbs: Double? = null,
    val tmus: Int? = null,
    val rops: Int? = null,
    @SerialName("l2_cache_mb") val l2CacheMb: Double? = null,
    @SerialName("fp32_tflops") val fp32Tflops: Double? = null,
    @SerialName("pcie_interface") val pcieInterface: String? = null,
    @SerialName("upscaling_tech") val upscalingTech: String? = null,
    @SerialName("media_engines") val mediaEngines: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    @SerialName("extra_specs") val extraSpecs: Map<String, String> = emptyMap(),
    @SerialName("source_provenance") val sourceProvenance: List<String> = emptyList(),
    @SerialName("spec_verified_at") val specVerifiedAt: String? = null,
)
