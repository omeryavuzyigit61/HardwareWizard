package com.panda.myapplication.domain.model

data class MotherboardSpec(
    val socket: String,
    val chipset: String,
    val formFactor: MotherboardFormFactor,
    val memoryGeneration: DdrGeneration,
    val ramSlots: Int,
    val pcieGeneration: PcieGeneration,
    val maxMemoryGb: Int? = null,
    val m2Slots: Int? = null,
    val pcieSlotCount: Int? = null,
    val sataPorts: Int? = null,
    val hasWifi: Boolean? = null,
    val vrmPhases: String? = null,
    val interfacePorts: String? = null,
    val pcbLayers: String? = null,
    val m2SlotsDetail: String? = null,
    val memorySpeedOc: String? = null,
    val network: String? = null,
    val audioCodec: String? = null,
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.MOTHERBOARD

    init {
        require(socket.isNotBlank()) { "Motherboard socket must not be blank." }
        require(ramSlots > 0) { "ramSlots must be positive: $ramSlots" }
    }
}
