package com.panda.myapplication.presentation.hardware_list

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType

enum class HardwareSortOption(val displayName: String) {
    DEFAULT("Varsayılan"),
    // Ortak (Her Parçada)
    PRICE_ASC("Fiyat: Artan"),
    PRICE_DESC("Fiyat: Azalan"),
    NAME_ASC("İsim: A-Z"),

    // CPU
    CPU_CORES_DESC("Çekirdek: Çoktan Aza"),
    CPU_CLOCK_DESC("Saat Hızı: En Yüksek"),
    CPU_TDP_ASC("TDP: En Düşük"),
    CPU_TDP_DESC("TDP: En Yüksek"),

    // GPU
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
    PSU_WATTAGE_DESC("Güç Kapasitesi: En Yüksek");

    companion object {
        fun optionsForCategory(category: HardwareCategory?): List<HardwareSortOption> {
            val common = listOf(DEFAULT, PRICE_ASC, PRICE_DESC, NAME_ASC)
            return when (category) {
                null -> common
                HardwareCategory.CPU -> common + listOf(CPU_CORES_DESC, CPU_CLOCK_DESC, CPU_TDP_ASC, CPU_TDP_DESC)
                HardwareCategory.GPU -> common + listOf(GPU_VRAM_DESC, GPU_TDP_ASC, GPU_TDP_DESC)
                HardwareCategory.RAM -> common + listOf(RAM_SPEED_DESC, RAM_CL_ASC, RAM_CAPACITY_DESC)
                HardwareCategory.STORAGE -> common + listOf(STORAGE_CAPACITY_DESC, STORAGE_READ_SPEED_DESC)
                HardwareCategory.MOTHERBOARD -> common + listOf(MOTHERBOARD_M2_SLOTS_DESC, MOTHERBOARD_RAM_SLOTS_DESC)
                HardwareCategory.PSU -> common + listOf(PSU_WATTAGE_DESC)
                HardwareCategory.COOLER -> common
                HardwareCategory.CASE -> common
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
                requirePcie5
}
