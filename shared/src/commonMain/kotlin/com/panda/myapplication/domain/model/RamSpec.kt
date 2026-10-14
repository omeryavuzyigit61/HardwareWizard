package com.panda.myapplication.domain.model

/**
 * @property speed Effective data rate (MT/s), e.g. DDR5-6000 → 6000.
 * @property capacity Total kit capacity.
 * @property moduleCount Number of sticks in the kit (2 → dual-channel, 4 → quad).
 */
data class RamSpec(
    val generation: DdrGeneration,
    val speed: Frequency,
    val casLatency: Int,
    val capacity: MemorySize,
    val moduleCount: Int,
    val firstWordLatencyNs: Double? = null,
    val voltage: Double? = null,
    val heatSpreader: Boolean? = null,
    val rgb: Boolean? = null,
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.RAM

    init {
        require(casLatency > 0) { "casLatency must be positive: $casLatency" }
        require(moduleCount > 0) { "moduleCount must be positive: $moduleCount" }
    }
}

