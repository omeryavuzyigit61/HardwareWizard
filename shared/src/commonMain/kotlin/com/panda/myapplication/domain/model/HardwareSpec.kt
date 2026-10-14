package com.panda.myapplication.domain.model

/** Category-specific technical specification of a [HardwareItem]. */
sealed interface HardwareSpec {
    val category: HardwareCategory
}

enum class HardwareCategory(val displayName: String) {
    CPU("İşlemci (CPU)"),
    GPU("Ekran Kartı (GPU)"),
    RAM("Bellek (RAM)"),
    MOTHERBOARD("Anakart"),
    PSU("Güç Kaynağı (PSU)"),
    STORAGE("Depolama (SSD/HDD)"),
    COOLER("İşlemci Soğutucu"),
    CASE("Kasa"),
}
