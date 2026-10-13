package com.panda.myapplication.domain.model

data class GpuSpec(
    val chipset: String,
    val vram: MemorySize,
    val memoryType: String,
    val coreClock: Frequency,
    val boostClock: Frequency?,
    val tdp: Wattage,
    val recommendedPsu: Wattage,
    val lengthMm: Int = 300,
    val powerConnectors: String = "1x 16-pin (12VHPWR)",
    val slotWidth: String = "2.5 Yuva",
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
