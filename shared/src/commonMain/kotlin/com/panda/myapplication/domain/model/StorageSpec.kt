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
    val nandType: String = "3D TLC NAND",
    val enduranceTbw: Int = 1200,
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.STORAGE

    init {
        require(sequentialReadMbps > 0) { "sequentialReadMbps must be positive: $sequentialReadMbps" }
        require(sequentialWriteMbps > 0) { "sequentialWriteMbps must be positive: $sequentialWriteMbps" }
        require((storageInterface == StorageInterface.NVME) == (pcieGeneration != null)) {
            "pcieGeneration must be set for NVMe and null for SATA."
        }
        require(!(mediaType == StorageMediaType.HDD && storageInterface == StorageInterface.NVME)) {
            "HDD cannot use NVMe interface."
        }
    }
}
