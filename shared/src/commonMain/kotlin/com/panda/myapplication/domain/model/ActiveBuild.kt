package com.panda.myapplication.domain.model

data class ActiveBuild(
    val cpu: HardwareItem? = null,
    val motherboard: HardwareItem? = null,
    val ram: HardwareItem? = null,
    val gpu: HardwareItem? = null,
    val storage: HardwareItem? = null,
    val cooler: HardwareItem? = null,
    val case: HardwareItem? = null,
    val psu: HardwareItem? = null,
) {
    val items: List<HardwareItem>
        get() = listOfNotNull(cpu, motherboard, ram, gpu, storage, cooler, case, psu)

    val totalPriceMinor: Long
        get() = items.sumOf { it.price?.amountMinor ?: 0L }

    val totalCount: Int
        get() = items.size

    fun getSlot(category: HardwareCategory): HardwareItem? = when (category) {
        HardwareCategory.CPU -> cpu
        HardwareCategory.MOTHERBOARD -> motherboard
        HardwareCategory.RAM -> ram
        HardwareCategory.GPU -> gpu
        HardwareCategory.STORAGE -> storage
        HardwareCategory.COOLER -> cooler
        HardwareCategory.CASE -> case
        HardwareCategory.PSU -> psu
    }

    fun withComponent(item: HardwareItem): ActiveBuild = when (item.category) {
        HardwareCategory.CPU -> copy(cpu = item)
        HardwareCategory.MOTHERBOARD -> copy(motherboard = item)
        HardwareCategory.RAM -> copy(ram = item)
        HardwareCategory.GPU -> copy(gpu = item)
        HardwareCategory.STORAGE -> copy(storage = item)
        HardwareCategory.COOLER -> copy(cooler = item)
        HardwareCategory.CASE -> copy(case = item)
        HardwareCategory.PSU -> copy(psu = item)
    }

    fun withoutCategory(category: HardwareCategory): ActiveBuild = when (category) {
        HardwareCategory.CPU -> copy(cpu = null)
        HardwareCategory.MOTHERBOARD -> copy(motherboard = null)
        HardwareCategory.RAM -> copy(ram = null)
        HardwareCategory.GPU -> copy(gpu = null)
        HardwareCategory.STORAGE -> copy(storage = null)
        HardwareCategory.COOLER -> copy(cooler = null)
        HardwareCategory.CASE -> copy(case = null)
        HardwareCategory.PSU -> copy(psu = null)
    }
}

data class BuildCompatibilityReport(
    val isCompatible: Boolean,
    val socketMismatch: String? = null,
    val ramMismatch: String? = null,
    val coolerMismatch: String? = null,
    val clearanceMismatch: String? = null,
    val psuWarning: String? = null,
    val warnings: List<String> = emptyList(),
)
