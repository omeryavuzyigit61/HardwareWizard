package com.panda.myapplication.domain.repository

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import kotlinx.coroutines.flow.StateFlow

enum class VsArenaMode(val title: String, val subtitle: String) {
    COMPONENT_VS_COMPONENT(
        title = "Bileşen vs Bileşen",
        subtitle = "CPU, GPU, Anakart, RAM, SSD, PSU, Soğutucu, Kasa",
    ),
    PREBUILT_VS_PREBUILT(
        title = "Hazır Sistem vs Sistem",
        subtitle = "OEM Kasa Kalitesi, Oyun FPS ve Tek Tek Toplama Kârı",
    ),
    LAPTOP_VS_LAPTOP(
        title = "Laptop vs Laptop",
        subtitle = "GPU TGP Watt, MUX Switch, Panel Hz, Ağırlık ve F/P",
    ),
}

enum class VsSlot {
    LEFT,
    RIGHT,
}

@Immutable
data class VsArenaSelectionState(
    val mode: VsArenaMode = VsArenaMode.COMPONENT_VS_COMPONENT,
    val componentCategory: HardwareCategory = HardwareCategory.GPU,
    val leftItem: HardwareItem? = null,
    val rightItem: HardwareItem? = null,
) {
    val activeCategory: HardwareCategory
        get() = when (mode) {
            VsArenaMode.COMPONENT_VS_COMPONENT -> componentCategory
            VsArenaMode.PREBUILT_VS_PREBUILT -> HardwareCategory.PREBUILT_PC
            VsArenaMode.LAPTOP_VS_LAPTOP -> HardwareCategory.LAPTOP
        }

    val filledCount: Int
        get() = (if (leftItem != null) 1 else 0) + (if (rightItem != null) 1 else 0)
}

interface VsArenaRepository {
    val state: StateFlow<VsArenaSelectionState>

    fun selectMode(mode: VsArenaMode)
    fun selectComponentCategory(category: HardwareCategory)
    fun setSlotItem(slot: VsSlot, item: HardwareItem?)
    fun swapSlots()
    fun clearArena()

    /**
     * Intelligently adds [item] from Catalog or Detail screens into the appropriate VS Arena mode & slot.
     * Returns a Turkish status message suitable for a Snackbar.
     */
    fun addQuickItem(item: HardwareItem): String

    fun addItemToComparison(item: HardwareItem): String = addQuickItem(item)
}
