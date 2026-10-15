package com.panda.myapplication.domain.model

data class CpuSpec(
    val socket: String,
    val coreCount: Int,
    val threadCount: Int,
    val baseClock: Frequency,
    val boostClock: Frequency?,
    val tdp: Wattage,
    val hasIntegratedGraphics: Boolean,
    val architecture: String? = null,
    val l2CacheMb: Double? = null,
    val l3CacheMb: Int? = null,
    val processNode: String? = null,
    val pcieVersion: String? = null,
    val maxMemorySpeed: String? = null,
    val unlockedMultiplier: Boolean? = null,
    val coreTopology: String? = null,
    val powerLimits: String? = null,
    val igpuModel: String? = null,
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.CPU

    init {
        require(socket.isNotBlank()) { "CPU socket must not be blank." }
        require(coreCount > 0) { "coreCount must be positive: $coreCount" }
        require(threadCount >= coreCount) { "threadCount ($threadCount) must be >= coreCount ($coreCount)." }
        require(boostClock == null || boostClock.mhz >= baseClock.mhz) {
            "boostClock (${boostClock?.mhz}) must be >= baseClock (${baseClock.mhz})."
        }
    }
}
