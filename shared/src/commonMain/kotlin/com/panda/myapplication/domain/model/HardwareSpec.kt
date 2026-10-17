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
    PREBUILT_PC("OEM Hazır Sistem"),
    LAPTOP("Gaming Laptop"),
    MOUSE("Oyuncu Faresi"),
    KEYBOARD("Oyuncu Klavyesi"),
    HEADSET("Oyuncu Kulaklığı"),
    MONITOR("Oyuncu Monitörü");

    val isPeripheral: Boolean
        get() = this == MOUSE || this == KEYBOARD || this == HEADSET || this == MONITOR
}
