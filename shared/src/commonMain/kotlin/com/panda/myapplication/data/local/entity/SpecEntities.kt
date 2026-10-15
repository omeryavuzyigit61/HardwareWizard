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
    @ColumnInfo(name = "l2_cache_mb") val l2CacheMb: Double? = null,
    @ColumnInfo(name = "l3_cache_mb") val l3CacheMb: Int? = null,
    @ColumnInfo(name = "process_node") val processNode: String? = null,
    @ColumnInfo(name = "pcie_version") val pcieVersion: String? = null,
    @ColumnInfo(name = "max_memory_speed") val maxMemorySpeed: String? = null,
    @ColumnInfo(name = "unlocked_multiplier") val unlockedMultiplier: Boolean? = null,
    @ColumnInfo(name = "core_topology") val coreTopology: String? = null,
    @ColumnInfo(name = "power_limits") val powerLimits: String? = null,
    @ColumnInfo(name = "igpu_model") val igpuModel: String? = null,
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
    @ColumnInfo(name = "bus_width_bits") val busWidthBits: Int? = null,
    @ColumnInfo(name = "length_mm") val lengthMm: Int? = null,
    @ColumnInfo(name = "power_connectors") val powerConnectors: String? = null,
    @ColumnInfo(name = "display_outputs") val displayOutputs: String? = null,
    @ColumnInfo(name = "slot_width") val slotWidth: String? = null,
    @ColumnInfo(name = "part_number") val partNumber: String? = null,
    val architecture: String? = null,
    @ColumnInfo(name = "process_node") val processNode: String? = null,
    @ColumnInfo(name = "cuda_cores") val cudaCores: Int? = null,
    @ColumnInfo(name = "tensor_ai_tops") val tensorAiTops: String? = null,
    @ColumnInfo(name = "rt_cores") val rtCores: Int? = null,
    @ColumnInfo(name = "memory_speed_gbps") val memorySpeedGbps: Double? = null,
    @ColumnInfo(name = "bandwidth_gbs") val bandwidthGbs: Double? = null,
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
    @ColumnInfo(name = "first_word_latency_ns") val firstWordLatencyNs: Double? = null,
    val voltage: Double? = null,
    @ColumnInfo(name = "heat_spreader") val heatSpreader: Boolean? = null,
    val rgb: Boolean? = null,
    @ColumnInfo(name = "die_type") val dieType: String? = null,
    val timings: String? = null,
    val profile: String? = null,
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
    @ColumnInfo(name = "max_memory_gb") val maxMemoryGb: Int? = null,
    @ColumnInfo(name = "m2_slots") val m2Slots: Int? = null,
    @ColumnInfo(name = "pcie_slot_count") val pcieSlotCount: Int? = null,
    @ColumnInfo(name = "sata_ports") val sataPorts: Int? = null,
    @ColumnInfo(name = "has_wifi") val hasWifi: Boolean? = null,
    @ColumnInfo(name = "vrm_phases") val vrmPhases: String? = null,
    @ColumnInfo(name = "interface_ports") val interfacePorts: String? = null,
    @ColumnInfo(name = "pcb_layers") val pcbLayers: String? = null,
    @ColumnInfo(name = "m2_slots_detail") val m2SlotsDetail: String? = null,
    @ColumnInfo(name = "memory_speed_oc") val memorySpeedOc: String? = null,
    val network: String? = null,
    @ColumnInfo(name = "audio_codec") val audioCodec: String? = null,
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
    val modularity: String? = null,
    @ColumnInfo(name = "form_factor") val formFactor: String? = null,
    val outputs: String? = null,
    @ColumnInfo(name = "tier_rating") val tierRating: String? = null,
    val capacitors: String? = null,
    @ColumnInfo(name = "atx_standard") val atxStandard: String? = null,
    @ColumnInfo(name = "pcie_12vhpwr") val pcie12Vhpwr: String? = null,
    @ColumnInfo(name = "eps_connectors") val epsConnectors: String? = null,
    @ColumnInfo(name = "pcie_connectors") val pcieConnectors: String? = null,
    @ColumnInfo(name = "sata_connectors") val sataConnectors: String? = null,
    val topology: String? = null,
    val protections: String? = null,
    @ColumnInfo(name = "fan_and_size") val fanAndSize: String? = null,
    @ColumnInfo(name = "cybenetics_cert") val cybeneticsCert: String? = null,
    @ColumnInfo(name = "cybenetics_lambda") val cybeneticsLambda: String? = null,
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
    @ColumnInfo(name = "nand_type") val nandType: String? = null,
    @ColumnInfo(name = "endurance_tbw") val enduranceTbw: Int? = null,
    @ColumnInfo(name = "cache_mb") val cacheMb: Int? = null,
    val controller: String? = null,
    @ColumnInfo(name = "dram_cache") val dramCache: String? = null,
    @ColumnInfo(name = "random_iops") val randomIops: String? = null,
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
    @ColumnInfo(name = "height_mm") val heightMm: Int? = null,
    @ColumnInfo(name = "fan_rpm") val fanRpm: String? = null,
    @ColumnInfo(name = "noise_dba") val noiseDba: Double? = null,
    @ColumnInfo(name = "radiator_dimensions") val radiatorDimensions: String? = null,
    @ColumnInfo(name = "fan_airflow") val fanAirflow: String? = null,
)

@Entity(
    tableName = "case_specs",
    foreignKeys = [ForeignKey(HardwareItemEntity::class, [PARENT_ID], [ITEM_ID], onDelete = ForeignKey.CASCADE)],
)
data class CaseSpecEntity(
    @PrimaryKey @ColumnInfo(name = ITEM_ID) val itemId: String,
    val type: String? = null,
    @ColumnInfo(name = "max_gpu_length_mm") val maxGpuLengthMm: Int? = null,
    @ColumnInfo(name = "max_cpu_cooler_height_mm") val maxCpuCoolerHeightMm: Int? = null,
    @ColumnInfo(name = "max_radiator_size_mm") val maxRadiatorSizeMm: Int? = null,
    @ColumnInfo(name = "side_panel") val sidePanel: String? = null,
    @ColumnInfo(name = "drive_bays") val driveBays: String? = null,
    @ColumnInfo(name = "radiator_support") val radiatorSupport: String? = null,
    @ColumnInfo(name = "included_fans") val includedFans: String? = null,
    @ColumnInfo(name = "front_panel_usb") val frontPanelUsb: String? = null,
)
