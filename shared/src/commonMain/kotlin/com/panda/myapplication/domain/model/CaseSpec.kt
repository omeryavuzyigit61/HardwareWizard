package com.panda.myapplication.domain.model

data class CaseSpec(
    val type: String? = null,
    val maxGpuLengthMm: Int? = null,
    val maxCpuCoolerHeightMm: Int? = null,
    val maxRadiatorSizeMm: Int? = null,
    val supportedMotherboardFormFactors: List<MotherboardFormFactor> = emptyList(),
    val sidePanel: String? = null,
    val driveBays: String? = null,
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.CASE
}
