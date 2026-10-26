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
    MONITOR("Oyuncu Monitörü"),
    SMARTPHONE("Akıllı Telefon"),
    TABLET("Tablet");

    val isPeripheral: Boolean
        get() = this == MOUSE || this == KEYBOARD || this == HEADSET || this == MONITOR

    val isMobile: Boolean
        get() = this == SMARTPHONE || this == TABLET

    val englishName: String
        get() = when (this) {
            CPU -> "Processor (CPU)"
            GPU -> "Graphics Card (GPU)"
            RAM -> "Memory (RAM)"
            MOTHERBOARD -> "Motherboard"
            PSU -> "Power Supply (PSU)"
            STORAGE -> "Storage (SSD/HDD)"
            COOLER -> "CPU Cooler"
            CASE -> "Case"
            PREBUILT_PC -> "Prebuilt PC"
            LAPTOP -> "Gaming Laptop"
            MOUSE -> "Gaming Mouse"
            KEYBOARD -> "Gaming Keyboard"
            HEADSET -> "Gaming Headset"
            MONITOR -> "Gaming Monitor"
            SMARTPHONE -> "Smartphone"
            TABLET -> "Tablet"
        }

    fun localizedName(isTr: Boolean = true): String = if (isTr) displayName else englishName
}
