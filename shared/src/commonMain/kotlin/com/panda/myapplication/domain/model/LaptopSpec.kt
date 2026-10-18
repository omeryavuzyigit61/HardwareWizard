package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LaptopSpec(
    val brand: String = "",
    val cpuModel: String,
    val gpuModel: String,
    val gpuWattTgp: String,
    val muxSwitch: String,
    val ramSpec: String,
    val storageSpec: String,
    val displaySpec: String,
    val batteryWh: Int,
    val weightKg: Double,
    val coolingTech: String,
    val gamingScore: Int,
    val fps1080p: Int = 0,
    val fps1440p: Int = 0,
    // 6 Ana Donanım Bileşeni Detayları
    val cpuCores: String = "",
    val cpuClockGhz: String = "",
    val screenResolution: String = "",
    val refreshRateHz: Int = 0,
    val panelTech: String = "",
    val colorGamut: String = "",
    val screenSizeInch: Double = 0.0,
    val ramSlots: String = "",
    val storageSlots: String = "",
    val chargerWatt: Int = 0,
) : HardwareSpec {
    override val category: HardwareCategory get() = HardwareCategory.LAPTOP

    val effectiveCpuCores: String
        get() = cpuCores.ifBlank {
            when {
                cpuModel.contains("14900", ignoreCase = true) || cpuModel.contains("13900", ignoreCase = true) -> "24 Çekirdek (8P + 16E) / 32 İzlek"
                cpuModel.contains("14700", ignoreCase = true) || cpuModel.contains("13700", ignoreCase = true) -> "20 Çekirdek (8P + 12E) / 28 İzlek"
                cpuModel.contains("14650", ignoreCase = true) || cpuModel.contains("13650", ignoreCase = true) -> "16 Çekirdek (8P + 8E) / 24 İzlek"
                cpuModel.contains("13500", ignoreCase = true) || cpuModel.contains("12500", ignoreCase = true) -> "12 Çekirdek (4P + 8E) / 16 İzlek"
                cpuModel.contains("7945", ignoreCase = true) || cpuModel.contains("9945", ignoreCase = true) -> "16 Çekirdek / 32 İzlek"
                cpuModel.contains("7840", ignoreCase = true) || cpuModel.contains("8845", ignoreCase = true) -> "8 Çekirdek / 16 İzlek"
                cpuModel.contains("7735", ignoreCase = true) || cpuModel.contains("7535", ignoreCase = true) -> "8 Çekirdek / 16 İzlek"
                else -> "Yüksek Performans Çoklu Çekirdek"
            }
        }

    val effectiveCpuClockGhz: String
        get() = cpuClockGhz.ifBlank {
            when {
                cpuModel.contains("14900", ignoreCase = true) -> "2.20 GHz Taban / 5.80 GHz Tepe (Boost)"
                cpuModel.contains("13900", ignoreCase = true) -> "2.20 GHz Taban / 5.40 GHz Tepe (Boost)"
                cpuModel.contains("14700", ignoreCase = true) -> "2.10 GHz Taban / 5.50 GHz Tepe (Boost)"
                cpuModel.contains("8845", ignoreCase = true) || cpuModel.contains("7840", ignoreCase = true) -> "3.80 GHz Taban / 5.10 GHz Tepe (Boost)"
                else -> "3.20 GHz Taban / 5.00 GHz Tepe Frekans"
            }
        }

    val effectiveRamSlots: String
        get() = ramSlots.ifBlank { "2x SO-DIMM Yuva (Genişletilebilir, Maks 64GB)" }

    val effectiveStorageSlots: String
        get() = storageSlots.ifBlank { "PCIe Gen4 NVMe M.2 (2. Yuva Mevcut)" }

    val effectiveChargerWatt: String
        get() = if (chargerWatt > 0) "${chargerWatt}W Hızlı Şarj Adaptörü" else "230W - 330W Adaptör"

    val effectivePanelDetails: String
        get() = when {
            screenResolution.isNotBlank() && refreshRateHz > 0 ->
                "${if (screenSizeInch > 0) "${screenSizeInch}\" " else ""}$screenResolution • ${refreshRateHz}Hz • ${panelTech.ifBlank { "IPS" }} • ${colorGamut.ifBlank { "%100 sRGB" }}"
            displaySpec.isNotBlank() -> displaySpec
            else -> "16\" WQXGA (2560x1600) 240Hz Fast IPS %100 sRGB"
        }
}

