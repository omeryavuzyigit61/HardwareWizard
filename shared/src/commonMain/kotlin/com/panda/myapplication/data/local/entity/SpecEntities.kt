package com.panda.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.StorageFormFactor
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType

/*
 * Table-per-spec layout: each spec table shares `item_id` as both PK and FK to `hardware_items`.
 * Deleting a parent row cascades to its spec row.
 */

private const val ITEM_ID = "item_id"
private const val PARENT_ID = "id"

@Entity(
    tableName = "cpu_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["socket"])],
)
data class CpuSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    val socket: String,
    @ColumnInfo(name = "core_count") val coreCount: Int,
    @ColumnInfo(name = "thread_count") val threadCount: Int,
    @ColumnInfo(name = "base_clock_mhz") val baseClockMhz: Int,
    @ColumnInfo(name = "boost_clock_mhz") val boostClockMhz: Int?,
    @ColumnInfo(name = "tdp_watts") val tdpWatts: Int,
    @ColumnInfo(name = "has_integrated_graphics") val hasIntegratedGraphics: Boolean,
    val architecture: String? = null,
    @ColumnInfo(name = "l3_cache_mb") val l3CacheMb: Int = 32,
)

@Entity(
    tableName = "gpu_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
)
data class GpuSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    val chipset: String,
    @ColumnInfo(name = "vram_mb") val vramMb: Int,
    @ColumnInfo(name = "memory_type") val memoryType: String,
    @ColumnInfo(name = "core_clock_mhz") val coreClockMhz: Int,
    @ColumnInfo(name = "boost_clock_mhz") val boostClockMhz: Int?,
    @ColumnInfo(name = "tdp_watts") val tdpWatts: Int,
    @ColumnInfo(name = "recommended_psu_watts") val recommendedPsuWatts: Int,
    @ColumnInfo(name = "length_mm") val lengthMm: Int = 300,
    @ColumnInfo(name = "power_connectors") val powerConnectors: String = "1x 16-pin (12VHPWR)",
    @ColumnInfo(name = "slot_width") val slotWidth: String = "2.5 Yuva",
    @ColumnInfo(name = "part_number") val partNumber: String? = null,
)

@Entity(
    tableName = "ram_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["generation"])],
)
data class RamSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    val generation: DdrGeneration,
    @ColumnInfo(name = "speed_mts") val speedMts: Int,
    @ColumnInfo(name = "cas_latency") val casLatency: Int,
    @ColumnInfo(name = "capacity_mb") val capacityMb: Int,
    @ColumnInfo(name = "module_count") val moduleCount: Int,
)

@Entity(
    tableName = "motherboard_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["socket"])],
)
data class MotherboardSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    val socket: String,
    val chipset: String,
    @ColumnInfo(name = "form_factor") val formFactor: MotherboardFormFactor,
    @ColumnInfo(name = "memory_generation") val memoryGeneration: DdrGeneration,
    @ColumnInfo(name = "ram_slots") val ramSlots: Int,
    @ColumnInfo(name = "pcie_generation") val pcieGeneration: PcieGeneration,
    @ColumnInfo(name = "vrm_phases") val vrmPhases: String = "14+2+1 Fazlı Güç",
    @ColumnInfo(name = "m2_slots") val m2Slots: Int = 3,
    @ColumnInfo(name = "has_wifi") val hasWifi: Boolean = true,
    @ColumnInfo(name = "interface_ports") val interfacePorts: String = "USB 3.2 Gen 2 Type-C, 2.5G LAN",
)

@Entity(
    tableName = "psu_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
)
data class PsuSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    @ColumnInfo(name = "wattage") val wattage: Int,
    val efficiency: PsuEfficiencyRating,
    @ColumnInfo(name = "is_atx3_compliant") val isAtx3Compliant: Boolean,
    @ColumnInfo(name = "supports_pcie5_power") val supportsPcie5Power: Boolean,
    @ColumnInfo(name = "tier_rating") val tierRating: String = "Tier B",
    @ColumnInfo(name = "capacitors") val capacitors: String = "105°C Japon Kapasitörler",
    @ColumnInfo(name = "modularity") val modularity: String = "Tam Modüler",
)

@Entity(
    tableName = "storage_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
)
data class StorageSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    @ColumnInfo(name = "media_type") val mediaType: StorageMediaType,
    @ColumnInfo(name = "storage_interface") val storageInterface: StorageInterface,
    @ColumnInfo(name = "form_factor") val formFactor: StorageFormFactor,
    @ColumnInfo(name = "pcie_generation") val pcieGeneration: PcieGeneration?,
    @ColumnInfo(name = "capacity_mb") val capacityMb: Int,
    @ColumnInfo(name = "seq_read_mbps") val sequentialReadMbps: Int,
    @ColumnInfo(name = "seq_write_mbps") val sequentialWriteMbps: Int,
    @ColumnInfo(name = "nand_type") val nandType: String = "3D TLC NAND",
    @ColumnInfo(name = "endurance_tbw") val enduranceTbw: Int = 1200,
)

@Entity(
    tableName = "cooler_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
)
data class CoolerSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    val type: CoolerType,
    @ColumnInfo(name = "radiator_size_mm") val radiatorSizeMm: Int?,
    @ColumnInfo(name = "max_cooling_tdp_watts") val maxCoolingTdpWatts: Int,
    @ColumnInfo(name = "supported_sockets") val supportedSockets: List<String>,
)

@Entity(
    tableName = "case_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
)
data class CaseSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    val type: String = "ATX Mid Tower",
    @ColumnInfo(name = "max_gpu_length_mm") val maxGpuLengthMm: Int = 380,
    @ColumnInfo(name = "max_cpu_cooler_height_mm") val maxCpuCoolerHeightMm: Int = 165,
    @ColumnInfo(name = "max_radiator_size_mm") val maxRadiatorSizeMm: Int = 360,
)
