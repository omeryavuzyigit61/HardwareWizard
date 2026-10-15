package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CpuRawDto(
    val name: String? = null,
    val price: Double? = null,
    @SerialName("core_count") val coreCount: Int? = null,
    @SerialName("core_clock") val coreClockGhz: Double? = null,
    @SerialName("boost_clock") val boostClockGhz: Double? = null,
    val microarchitecture: String? = null,
    val tdp: Int? = null,
    val graphics: String? = null,
    val socket: String? = null,
    @SerialName("threads") val threads: Int? = null,
    @SerialName("l2_cache") val l2CacheMb: Double? = null,
    @SerialName("l3_cache") val l3CacheMb: Int? = null,
    @SerialName("process_node") val processNode: String? = null,
    @SerialName("pcie_version") val pcieVersion: String? = null,
    @SerialName("max_memory_speed") val maxMemorySpeed: String? = null,
    @SerialName("unlocked_multiplier") val unlockedMultiplier: Boolean? = null,
    @SerialName("core_topology") val coreTopology: String? = null,
    @SerialName("power_limits") val powerLimits: String? = null,
    @SerialName("igpu_model") val igpuModel: String? = null,
    val imageUrl: String? = null,
)
