package com.panda.myapplication.domain.model

/**
 * @property radiatorSizeMm Required for [CoolerType.LIQUID] (e.g. 240, 360); `null` for air coolers.
 * @property maxCoolingTdp Maximum heat dissipation the cooler is rated for.
 * @property supportedSockets CPU sockets supported by included mounting hardware.
 */
data class CoolerSpec(
    val type: CoolerType,
    val radiatorSizeMm: Int?,
    val maxCoolingTdp: Wattage,
    val supportedSockets: List<String>,
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.COOLER

    init {
        require((type == CoolerType.LIQUID) == (radiatorSizeMm != null)) {
            "radiatorSizeMm must be set for liquid coolers and null for air coolers."
        }
        require(radiatorSizeMm == null || radiatorSizeMm > 0) { "radiatorSizeMm must be positive: $radiatorSizeMm" }
    }
}
