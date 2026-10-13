package com.panda.myapplication.domain.model

data class MotherboardSpec(
    val socket: String,
    val chipset: String,
    val formFactor: MotherboardFormFactor,
    val memoryGeneration: DdrGeneration,
    val ramSlots: Int,
    val pcieGeneration: PcieGeneration,
    val vrmPhases: String = "14+2+1 Fazlı Güç",
    val m2Slots: Int = 3,
    val hasWifi: Boolean = true,
    val interfacePorts: String = "USB 3.2 Gen 2 Type-C, 2.5G LAN",
) : HardwareSpec {

    override val category: HardwareCategory get() = HardwareCategory.MOTHERBOARD

    init {
        require(socket.isNotBlank()) { "Motherboard socket must not be blank." }
        require(chipset.isNotBlank()) { "Motherboard chipset must not be blank." }
        require(ramSlots > 0) { "ramSlots must be positive: $ramSlots" }
    }
}
