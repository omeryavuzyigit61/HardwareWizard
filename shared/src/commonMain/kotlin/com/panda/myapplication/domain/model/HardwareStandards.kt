package com.panda.myapplication.domain.model

enum class DdrGeneration { 
    DDR3, DDR4, DDR5;
    val displayName: String get() = name
}

enum class PcieGeneration { 
    GEN2, GEN3, GEN4, GEN5;
    val displayName: String get() = when (this) {
        GEN2 -> "PCIe 2.0"
        GEN3 -> "PCIe 3.0"
        GEN4 -> "PCIe 4.0"
        GEN5 -> "PCIe 5.0"
    }
}

enum class MotherboardFormFactor { 
    E_ATX, ATX, MICRO_ATX, MINI_ITX;
    val displayName: String get() = when (this) {
        E_ATX -> "E-ATX"
        ATX -> "ATX"
        MICRO_ATX -> "Micro-ATX"
        MINI_ITX -> "Mini-ITX"
    }
}

enum class PsuEfficiencyRating { 
    STANDARD, BRONZE, SILVER, GOLD, PLATINUM, TITANIUM;
    val displayName: String get() = when (this) {
        STANDARD -> "80 PLUS Standart"
        BRONZE -> "80 PLUS Bronze"
        SILVER -> "80 PLUS Silver"
        GOLD -> "80 PLUS Gold"
        PLATINUM -> "80 PLUS Platinum"
        TITANIUM -> "80 PLUS Titanium"
    }
}

enum class StorageMediaType { 
    SSD, HDD;
    val displayName: String get() = when (this) {
        SSD -> "SSD (Katı Hal Sürücüsü)"
        HDD -> "HDD (Sabit Disk)"
    }
}

enum class StorageInterface { 
    NVME, SATA;
    val displayName: String get() = when (this) {
        NVME -> "NVMe / M.2"
        SATA -> "SATA III"
    }
}

enum class StorageFormFactor { 
    M2_2230, M2_2242, M2_2280, INCH_2_5, INCH_3_5;
    val displayName: String get() = when (this) {
        M2_2230 -> "M.2 2230"
        M2_2242 -> "M.2 2242"
        M2_2280 -> "M.2 2280"
        INCH_2_5 -> "2.5 inç"
        INCH_3_5 -> "3.5 inç"
    }
}

enum class CoolerType { 
    AIR, LIQUID;
    val displayName: String get() = when (this) {
        AIR -> "Kule Tipi Hava Soğutma"
        LIQUID -> "Sıvı Soğutma (AIO)"
    }
}
