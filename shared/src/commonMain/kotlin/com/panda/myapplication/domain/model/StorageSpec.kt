package com.panda.myapplication.domain.model

/**
 * @property pcieGeneration Only applicable to [StorageInterface.NVME]; `null` otherwise.
 * @property sequentialReadMbps Sequential read speed in MB/s.
 * @property sequentialWriteMbps Sequential write speed in MB/s.
 */
data class StorageSpec(
    val mediaType: StorageMediaType,
    val storageInterface: StorageInterface,
    val formFactor: StorageFormFactor,
    val pcieGeneration: PcieGeneration?,
    val capacity: MemorySize,
    val sequentialReadMbps: Int,
    val sequentialWriteMbps: Int,
    val nandType: String? = null,
    val enduranceTbw: Int? = null,
    val cacheMb: Int? = null,
    val controller: String? = null,
    val dramCache: String? = null,
    val randomIops: String? = null,
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.STORAGE

    init {
        require(sequentialReadMbps > 0) { "sequentialReadMbps must be positive: $sequentialReadMbps" }
        require(sequentialWriteMbps > 0) { "sequentialWriteMbps must be positive: $sequentialWriteMbps" }
        require(!(mediaType == StorageMediaType.HDD && storageInterface == StorageInterface.NVME)) {
            "HDD cannot use NVMe interface."
        }
    }
}
