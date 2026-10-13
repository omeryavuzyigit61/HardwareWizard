package com.panda.myapplication.domain.model

/** Category-specific technical specification of a [HardwareItem]. */
sealed interface HardwareSpec {
    val category: HardwareCategory
}

enum class HardwareCategory {
    CPU,
    GPU,
    RAM,
    MOTHERBOARD,
    PSU,
    STORAGE,
    COOLER,
    CASE,
}
