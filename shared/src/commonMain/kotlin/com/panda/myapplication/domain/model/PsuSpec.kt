package com.panda.myapplication.domain.model

/**
 * @property supportsPcie5Power Native 12VHPWR / 12V-2x6 connector for PCIe 5.0 GPUs.
 */
data class PsuSpec(
    val wattage: Wattage,
    val efficiency: PsuEfficiencyRating,
    val isAtx3Compliant: Boolean,
    val supportsPcie5Power: Boolean,
    val modularity: String? = null,
    val formFactor: String? = null,
    val outputs: String? = null,
    val tierRating: String? = null,
    val capacitors: String? = null,
    val atxStandard: String? = null,
    val pcie12Vhpwr: String? = null,
    val epsConnectors: String? = null,
    val pcieConnectors: String? = null,
    val sataConnectors: String? = null,
    val topology: String? = null,
    val protections: String? = null,
    val fanAndSize: String? = null,
    val cybeneticsCert: String? = null,
    val cybeneticsLambda: String? = null,
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.PSU
}
