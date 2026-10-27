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
    MICROPHONE("Yayıncı & Stüdyo Mikrofonu"),
    MOUSEPAD("Oyuncu Mousepad"),
    WEBCAM("Yayıncı Kamerası (Webcam)"),
    GAMING_CHAIR("Oyuncu & Ergonomi Koltuğu"),
    CASE_FAN("Kasa Fanı"),
    UPS("Kesintisiz Güç Kaynağı (UPS)"),
    GAMING_DESK("Oyuncu Masası"),
    GAMING_LIGHTING("Akıllı Oyuncu Aydınlatması"),
    SMARTWATCH("Akıllı Saat"),
    ROUTER("Wi-Fi Router"),
    MODEM("Modem (VDSL/Fiber)"),
    ACCESS_POINT("Access Point (AP)"),
    RANGE_EXTENDER("Menzil Genişletici & Mesh"),
    SMARTPHONE("Akıllı Telefon"),
    TABLET("Tablet");

    val isPeripheral: Boolean
        get() = this == MOUSE || this == KEYBOARD || this == HEADSET || this == MONITOR ||
            this == MICROPHONE || this == MOUSEPAD || this == WEBCAM || this == GAMING_CHAIR ||
            this == GAMING_DESK || this == GAMING_LIGHTING

    val isNetworking: Boolean
        get() = this == ROUTER || this == MODEM || this == ACCESS_POINT || this == RANGE_EXTENDER

    val isMobile: Boolean
        get() = this == SMARTPHONE || this == TABLET || this == SMARTWATCH

    val isPowerBackup: Boolean
        get() = this == UPS

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
            MICROPHONE -> "Studio & Streaming Microphone"
            MOUSEPAD -> "Gaming Mousepad"
            WEBCAM -> "Streaming Webcam"
            GAMING_CHAIR -> "Gaming & Ergonomic Chair"
            CASE_FAN -> "Case Cooling Fan"
            UPS -> "Uninterruptible Power Supply (UPS)"
            GAMING_DESK -> "Gaming & Standing Desk"
            GAMING_LIGHTING -> "Smart Gaming Lighting"
            SMARTWATCH -> "Smartwatch"
            ROUTER -> "Wi-Fi Router"
            MODEM -> "Modem (VDSL/Fiber)"
            ACCESS_POINT -> "Access Point (AP)"
            RANGE_EXTENDER -> "Range Extender & Mesh Node"
            SMARTPHONE -> "Smartphone"
            TABLET -> "Tablet"
        }

    fun localizedName(isTr: Boolean = true): String = if (isTr) displayName else englishName
}
