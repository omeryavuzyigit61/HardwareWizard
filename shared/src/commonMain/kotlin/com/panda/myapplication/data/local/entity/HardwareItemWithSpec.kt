package com.panda.myapplication.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/** Read model joining a hardware row with its category-specific spec row (exactly one is non-null). */
data class HardwareItemWithSpec(
    @Embedded val item: HardwareItemEntity,
    @Relation(parentColumn = "id", entityColumn = "item_id") val cpu: CpuSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val gpu: GpuSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val ram: RamSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val motherboard: MotherboardSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val psu: PsuSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val storage: StorageSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val cooler: CoolerSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val case: CaseSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val prebuilt: PrebuiltSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "item_id") val laptop: LaptopSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "hardware_id") val mouse: MouseSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "hardware_id") val keyboard: KeyboardSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "hardware_id") val headset: HeadsetSpecEntity? = null,
    @Relation(parentColumn = "id", entityColumn = "hardware_id") val monitor: MonitorSpecEntity? = null,
)

/** Write model for an atomic catalog sync. */
data class HardwareCatalogSnapshot(
    val items: List<HardwareItemEntity>,
    val cpus: List<CpuSpecEntity> = emptyList(),
    val gpus: List<GpuSpecEntity> = emptyList(),
    val rams: List<RamSpecEntity> = emptyList(),
    val motherboards: List<MotherboardSpecEntity> = emptyList(),
    val psus: List<PsuSpecEntity> = emptyList(),
    val storages: List<StorageSpecEntity> = emptyList(),
    val coolers: List<CoolerSpecEntity> = emptyList(),
    val cases: List<CaseSpecEntity> = emptyList(),
    val prebuilts: List<PrebuiltSpecEntity> = emptyList(),
    val laptops: List<LaptopSpecEntity> = emptyList(),
    val mice: List<MouseSpecEntity> = emptyList(),
    val keyboards: List<KeyboardSpecEntity> = emptyList(),
    val headsets: List<HeadsetSpecEntity> = emptyList(),
    val monitors: List<MonitorSpecEntity> = emptyList(),
)
