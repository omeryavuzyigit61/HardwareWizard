package com.panda.myapplication.domain.model

/**
 * @property supportsPcie5Power Native 12VHPWR / 12V-2x6 connector for PCIe 5.0 GPUs.
 */
data class PsuSpec(
    val wattage: Wattage,
    val efficiency: PsuEfficiencyRating,
    val isAtx3Compliant: Boolean,
    val supportsPcie5Power: Boolean,
    val tierRating: String = "Tier B",
    val capacitors: String = "105°C Japon Kapasitörler",
    val modularity: String = "Tam Modüler",
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.PSU
}
