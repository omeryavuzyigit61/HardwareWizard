package com.panda.myapplication.domain.model

data class GpuSpec(
    val chipset: String,
    val vram: MemorySize,
    val memoryType: String,
    val coreClock: Frequency,
    val boostClock: Frequency?,
    val tdp: Wattage,
    val recommendedPsu: Wattage,
    val busWidthBits: Int? = null,
    val lengthMm: Int? = null,
    val powerConnectors: String? = null,
    val displayOutputs: String? = null,
    val slotWidth: String? = null,
    val partNumber: String? = null,
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.GPU

    init {
        require(chipset.isNotBlank()) { "GPU chipset must not be blank." }
        require(memoryType.isNotBlank()) { "GPU memoryType must not be blank." }
        require(boostClock == null || boostClock.mhz >= coreClock.mhz) {
            "boostClock (${boostClock?.mhz}) must be >= coreClock (${coreClock.mhz})."
        }
        require(recommendedPsu.watts >= tdp.watts) {
            "recommendedPsu (${recommendedPsu.watts}W) must be >= tdp (${tdp.watts}W)."
        }
    }
}
