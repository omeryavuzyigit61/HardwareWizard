package com.panda.myapplication.domain.model

data class CaseSpec(
    val type: String = "ATX Mid Tower",
    val maxGpuLengthMm: Int = 380,
    val maxCpuCoolerHeightMm: Int = 165,
    val maxRadiatorSizeMm: Int = 360,
    val supportedMotherboardFormFactors: List<MotherboardFormFactor> = listOf(
        MotherboardFormFactor.ATX,
        MotherboardFormFactor.MICRO_ATX,
        MotherboardFormFactor.MINI_ITX,
    ),
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.CASE
}
