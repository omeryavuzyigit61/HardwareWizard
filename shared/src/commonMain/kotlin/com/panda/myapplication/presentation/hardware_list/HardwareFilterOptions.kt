package com.panda.myapplication.presentation.hardware_list

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.config.FeatureFlags
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.presentation.util.AppStrings
import com.panda.myapplication.presentation.util.StringsEN
import com.panda.myapplication.presentation.util.StringsTR

enum class HardwareSortOption(val displayName: String) {
    DEFAULT("Varsayılan"),
    PRICE_ASC("Fiyat: Artan"),
    PRICE_DESC("Fiyat: Azalan"),
    NAME_ASC("İsim: A-Z"),

    // CPU
    CPU_GAMING_PERF_DESC("Oyun Performansı: En Yüksek"),
    CPU_RENDER_PERF_DESC("Üretkenlik & Render: En Yüksek"),
    CPU_CORES_DESC("Çekirdek: Çoktan Aza"),
    CPU_CLOCK_DESC("Saat Hızı: En Yüksek"),
    CPU_TDP_ASC("TDP: En Düşük"),
    CPU_TDP_DESC("TDP: En Yüksek"),

    // GPU
    GPU_PERF_DESC("Oyun Performansı: En Yüksek"),
    GPU_VRAM_DESC("VRAM: En Yüksek"),
    GPU_TDP_ASC("Güç Tüketimi: En Düşük"),
    GPU_TDP_DESC("Güç Tüketimi: En Yüksek"),

    // RAM
    RAM_SPEED_DESC("Frekans: En Yüksek"),
    RAM_CL_ASC("CAS Gecikmesi (CL): En Düşük"),
    RAM_CAPACITY_DESC("Toplam Kapasite: En Yüksek"),

    // Storage (SSD)
    STORAGE_CAPACITY_DESC("Kapasite: En Yüksek"),
    STORAGE_READ_SPEED_DESC("Okuma Hızı: En Hızlı"),

    // Motherboard
    MOTHERBOARD_M2_SLOTS_DESC("M.2 Slot Sayısı: En Çok"),
    MOTHERBOARD_RAM_SLOTS_DESC("RAM Slot Sayısı: En Çok"),

    // PSU
    PSU_WATTAGE_DESC("Güç Kapasitesi: En Yüksek"),

    // Cooling, Cases & Fans
    COOLER_TDP_DESC("Soğutma Kapasitesi (W): En Yüksek"),
    COOLER_NOISE_ASC("Ses Seviyesi (dBA): En Sessiz"),
    CASE_MAX_GPU_DESC("Maks. GPU Uzunluğu: En Geniş"),
    CASE_FAN_AIRFLOW_DESC("Hava Akışı (CFM): En Yüksek"),

    // Laptop & Monitor
    LAPTOP_GAMING_DESC("Oyun Skoru: En Yüksek"),
    MONITOR_REFRESH_DESC("Yenileme Hızı (Hz): En Yüksek"),
    MONITOR_RESPONSE_ASC("Tepki Süresi (GtG): En Hızlı"),

    // Peripherals & Furniture
    MOUSE_WEIGHT_ASC("Ağırlık: En Hafif"),
    MOUSE_DPI_DESC("Sensör DPI: En Yüksek"),
    KEYBOARD_POLLING_DESC("Polling Rate (Hz): En Yüksek"),
    HEADSET_WEIGHT_ASC("Ağırlık: En Hafif"),
    MIC_SPL_DESC("Maks. SPL (dB): En Yüksek"),
    MOUSEPAD_THICKNESS_DESC("Kalınlık (mm): En Kalın"),
    WEBCAM_FPS_DESC("Kare Hızı (FPS): En Yüksek"),
    CHAIR_MAX_WEIGHT_DESC("Taşıma Kapasitesi: En Yüksek"),
    DESK_WIDTH_DESC("Genişlik (cm): En Geniş"),
    LIGHTING_LUMENS_DESC("Parlaklık (Lümen): En Yüksek"),

    // UPS & Networking
    UPS_WATT_DESC("Watt Kapasitesi: En Yüksek"),
    NET_COVERAGE_DESC("Kapsama / İstemci: En Yüksek"),

    // Mobil & Tablet
    ANTUTU_SCORE_DESC("AnTuTu / Performans: En Yüksek"),
    BATTERY_DESC("Batarya Kapasitesi: En Yüksek");

    fun localizedName(strings: AppStrings): String {
        val isTr = strings.isTurkish
        return when (this) {
            DEFAULT -> strings.sortDefault
            PRICE_ASC -> strings.sortPriceAsc
            PRICE_DESC -> strings.sortPriceDesc
            NAME_ASC -> strings.sortNameAsc
            CPU_GAMING_PERF_DESC -> if (isTr) "Oyun Performansı: En Yüksek" else "Gaming Performance: Highest"
            CPU_RENDER_PERF_DESC -> if (isTr) "Üretkenlik & Render: En Yüksek" else "Multi-Core / Render: Highest"
            CPU_CORES_DESC -> strings.sortCpuCoresDesc
            CPU_CLOCK_DESC -> strings.sortCpuClockDesc
            CPU_TDP_ASC -> strings.sortCpuTdpAsc
            CPU_TDP_DESC -> strings.sortCpuTdpDesc
            GPU_PERF_DESC -> if (isTr) "Grafik Performansı: En Yüksek" else "GPU Performance: Highest"
            GPU_VRAM_DESC -> strings.sortGpuVramDesc
            GPU_TDP_ASC -> strings.sortGpuTdpAsc
            GPU_TDP_DESC -> strings.sortGpuTdpDesc
            RAM_SPEED_DESC -> strings.sortRamSpeedDesc
            RAM_CL_ASC -> strings.sortRamClAsc
            RAM_CAPACITY_DESC -> strings.sortRamCapacityDesc
            STORAGE_CAPACITY_DESC -> strings.sortStorageCapacityDesc
            STORAGE_READ_SPEED_DESC -> strings.sortStorageReadSpeedDesc
            MOTHERBOARD_M2_SLOTS_DESC -> strings.sortMotherboardM2Desc
            MOTHERBOARD_RAM_SLOTS_DESC -> strings.sortMotherboardRamDesc
            PSU_WATTAGE_DESC -> strings.sortPsuWattageDesc
            ANTUTU_SCORE_DESC -> strings.sortAntutuDesc
            BATTERY_DESC -> strings.sortBatteryDesc
            COOLER_TDP_DESC -> if (isTr) "Soğutma Gücü (W): En Yüksek" else "Cooling TDP (W): Highest"
            COOLER_NOISE_ASC -> if (isTr) "Akustik (dBA): En Sessiz" else "Noise (dBA): Quietest"
            CASE_MAX_GPU_DESC -> if (isTr) "Maks. GPU Boşluğu: En Geniş" else "Max GPU Clearance: Largest"
            CASE_FAN_AIRFLOW_DESC -> if (isTr) "Hava Akışı (CFM): En Yüksek" else "Airflow (CFM): Highest"
            LAPTOP_GAMING_DESC -> if (isTr) "Oyun Performansı: En Yüksek" else "Gaming Score: Highest"
            MONITOR_REFRESH_DESC -> if (isTr) "Yenileme Hızı (Hz): En Yüksek" else "Refresh Rate (Hz): Highest"
            MONITOR_RESPONSE_ASC -> if (isTr) "Tepki Süresi (ms): En Düşük" else "Response Time: Fastest"
            MOUSE_WEIGHT_ASC -> if (isTr) "Ağırlık (g): En Hafif" else "Weight (g): Lightest"
            MOUSE_DPI_DESC -> if (isTr) "Maks. DPI: En Yüksek" else "Max DPI: Highest"
            KEYBOARD_POLLING_DESC -> if (isTr) "Polling Hızı: En Yüksek" else "Polling Rate: Highest"
            HEADSET_WEIGHT_ASC -> if (isTr) "Ağırlık (g): En Hafif" else "Weight (g): Lightest"
            MIC_SPL_DESC -> if (isTr) "Max SPL (dB): En Yüksek" else "Max SPL: Highest"
            MOUSEPAD_THICKNESS_DESC -> if (isTr) "Kalınlık (mm): En Yüksek" else "Thickness: Thickest"
            WEBCAM_FPS_DESC -> if (isTr) "Kare Hızı (FPS): En Yüksek" else "Frame Rate (FPS): Highest"
            CHAIR_MAX_WEIGHT_DESC -> if (isTr) "Taşıma Kapasitesi (kg): En Yüksek" else "Weight Capacity: Highest"
            DESK_WIDTH_DESC -> if (isTr) "Masa Genişliği: En Geniş" else "Desk Width: Widest"
            LIGHTING_LUMENS_DESC -> if (isTr) "Lümen Parlaklık: En Yüksek" else "Brightness (Lumens): Highest"
            UPS_WATT_DESC -> if (isTr) "Güç Kapasitesi (W): En Yüksek" else "Watt Capacity: Highest"
            NET_COVERAGE_DESC -> if (isTr) "Kapasite / Kapsama: En Yüksek" else "Coverage / Clients: Highest"
        }
    }

    fun localizedName(isTr: Boolean = true): String = localizedName(if (isTr) StringsTR else StringsEN)

    companion object {
        fun optionsForCategory(category: HardwareCategory?): List<HardwareSortOption> {
            val common = if (FeatureFlags.IS_PRICING_ENABLED) {
                listOf(DEFAULT, PRICE_ASC, PRICE_DESC, NAME_ASC)
            } else {
                listOf(DEFAULT, NAME_ASC)
            }
            return when (category) {
                null -> common
                HardwareCategory.CPU -> common + listOf(CPU_GAMING_PERF_DESC, CPU_RENDER_PERF_DESC, CPU_CORES_DESC, CPU_CLOCK_DESC, CPU_TDP_ASC, CPU_TDP_DESC)
                HardwareCategory.GPU -> common + listOf(GPU_PERF_DESC, GPU_VRAM_DESC, GPU_TDP_ASC, GPU_TDP_DESC)
                HardwareCategory.RAM -> common + listOf(RAM_SPEED_DESC, RAM_CL_ASC, RAM_CAPACITY_DESC)
                HardwareCategory.STORAGE -> common + listOf(STORAGE_CAPACITY_DESC, STORAGE_READ_SPEED_DESC)
                HardwareCategory.MOTHERBOARD -> common + listOf(MOTHERBOARD_M2_SLOTS_DESC, MOTHERBOARD_RAM_SLOTS_DESC)
                HardwareCategory.PSU -> common + listOf(PSU_WATTAGE_DESC)
                HardwareCategory.COOLER -> common + listOf(COOLER_TDP_DESC, COOLER_NOISE_ASC)
                HardwareCategory.CASE -> common + listOf(CASE_MAX_GPU_DESC)
                HardwareCategory.CASE_FAN -> common + listOf(CASE_FAN_AIRFLOW_DESC, COOLER_NOISE_ASC)
                HardwareCategory.LAPTOP -> common + listOf(LAPTOP_GAMING_DESC, BATTERY_DESC)
                HardwareCategory.MONITOR -> common + listOf(MONITOR_REFRESH_DESC, MONITOR_RESPONSE_ASC)
                HardwareCategory.MOUSE -> common + listOf(MOUSE_WEIGHT_ASC, MOUSE_DPI_DESC, KEYBOARD_POLLING_DESC)
                HardwareCategory.KEYBOARD -> common + listOf(KEYBOARD_POLLING_DESC)
                HardwareCategory.HEADSET -> common + listOf(HEADSET_WEIGHT_ASC)
                HardwareCategory.MICROPHONE -> common + listOf(MIC_SPL_DESC)
                HardwareCategory.MOUSEPAD -> common + listOf(MOUSEPAD_THICKNESS_DESC)
                HardwareCategory.WEBCAM -> common + listOf(WEBCAM_FPS_DESC)
                HardwareCategory.GAMING_CHAIR -> common + listOf(CHAIR_MAX_WEIGHT_DESC)
                HardwareCategory.GAMING_DESK -> common + listOf(DESK_WIDTH_DESC)
                HardwareCategory.GAMING_LIGHTING -> common + listOf(LIGHTING_LUMENS_DESC)
                HardwareCategory.UPS -> common + listOf(UPS_WATT_DESC)
                HardwareCategory.ROUTER,
                HardwareCategory.MODEM,
                HardwareCategory.ACCESS_POINT,
                HardwareCategory.RANGE_EXTENDER -> common + listOf(NET_COVERAGE_DESC)
                HardwareCategory.SMARTPHONE -> common + listOf(ANTUTU_SCORE_DESC, BATTERY_DESC)
                HardwareCategory.TABLET -> common + listOf(ANTUTU_SCORE_DESC, BATTERY_DESC)
                HardwareCategory.SMARTWATCH -> common + listOf(BATTERY_DESC)
            }
        }
    }
}

@Immutable
data class HardwareFilterOptions(
    val sortOption: HardwareSortOption = HardwareSortOption.DEFAULT,
    val selectedBrands: Set<String> = emptySet(),
    // CPU Filters
    val cpuSocket: String? = null,
    val minCoreCount: Int? = null,
    val maxTdpWatts: Int? = null,
    // GPU Filters
    val minVramGb: Int? = null,
    val gpuBrand: String? = null,
    val maxGpuLengthMm: Int? = null,
    // RAM Filters
    val ramGeneration: DdrGeneration? = null,
    val minRamSpeedMts: Int? = null,
    // Storage Filters
    val storageMediaType: StorageMediaType? = null,
    val storageInterface: StorageInterface? = null,
    val storagePcieGen: PcieGeneration? = null,
    val minStorageCapacityGb: Int? = null,
    // Motherboard Filters
    val motherboardSocket: String? = null,
    val motherboardFormFactor: MotherboardFormFactor? = null,
    val minM2Slots: Int? = null,
    // PSU Filters
    val minPsuWattage: Int? = null,
    val psuEfficiency: PsuEfficiencyRating? = null,
    val requireAtx3: Boolean = false,
    val requirePcie5: Boolean = false,
    // Extended Peripherals, Cooling, Networking & Mobile Filters
    val coolerTypeFilter: CoolerType? = null,
    val minMonitorRefreshHz: Int? = null,
    val monitorPanelFilter: String? = null,
    val maxMouseWeightG: Int? = null,
    val requireHallEffectKeyboard: Boolean = false,
    val requirePureSineUps: Boolean = false,
    val wifiStandardFilter: String? = null,
    val minMobileBatteryMah: Int? = null,
) {
    val isActive: Boolean
        get() = sortOption != HardwareSortOption.DEFAULT ||
            selectedBrands.isNotEmpty() ||
            cpuSocket != null ||
            minCoreCount != null ||
            maxTdpWatts != null ||
            minVramGb != null ||
            gpuBrand != null ||
            maxGpuLengthMm != null ||
            ramGeneration != null ||
            minRamSpeedMts != null ||
            storageMediaType != null ||
            storageInterface != null ||
            storagePcieGen != null ||
            minStorageCapacityGb != null ||
            motherboardSocket != null ||
            motherboardFormFactor != null ||
            minM2Slots != null ||
            minPsuWattage != null ||
            psuEfficiency != null ||
            requireAtx3 ||
            requirePcie5 ||
            coolerTypeFilter != null ||
            minMonitorRefreshHz != null ||
            monitorPanelFilter != null ||
            maxMouseWeightG != null ||
            requireHallEffectKeyboard ||
            requirePureSineUps ||
            wifiStandardFilter != null ||
            minMobileBatteryMah != null
}
