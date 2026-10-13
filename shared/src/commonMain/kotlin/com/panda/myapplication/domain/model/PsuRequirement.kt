package com.panda.myapplication.domain.model

/**
 * @property estimatedLoad Sum of component TDPs plus base system overhead.
 * @property recommendedWattage Suggested PSU capacity after headroom, rounded up to a standard step.
 */
data class PsuRequirement(
    val estimatedLoad: Wattage,
    val recommendedWattage: Wattage,
)

/**
 * Tunable parameters for PSU sizing. Defaults reflect common builder guidelines.
 * @property baseSystemOverhead Motherboard, case fans and USB peripherals.
 */
data class PsuCalculationPolicy(
    val baseSystemOverhead: Wattage = Wattage(50),
    val ramPerModule: Wattage = Wattage(5),
    val ssdDraw: Wattage = Wattage(8),
    val hddDraw: Wattage = Wattage(10),
    val airCoolerDraw: Wattage = Wattage(5),
    val liquidCoolerDraw: Wattage = Wattage(15),
    val headroomPercent: Int = 30,
    val roundingStep: Wattage = Wattage(50),
) {
    init {
        require(headroomPercent >= 0) { "headroomPercent must not be negative: $headroomPercent" }
    }
}
