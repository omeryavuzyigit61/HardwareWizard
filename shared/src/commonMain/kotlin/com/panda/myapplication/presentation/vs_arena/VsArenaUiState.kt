package com.panda.myapplication.presentation.vs_arena

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.engine.VsComparisonReport
import com.panda.myapplication.domain.model.GameCategory
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.VsArenaMode
import com.panda.myapplication.domain.repository.VsSlot

@Immutable
data class VsArenaUiState(
    val mode: VsArenaMode = VsArenaMode.COMPONENT_VS_COMPONENT,
    val componentCategory: HardwareCategory = HardwareCategory.GPU,
    val leftItem: HardwareItem? = null,
    val rightItem: HardwareItem? = null,
    val report: VsComparisonReport? = null,
    val selectedResolution: GamingResolution = GamingResolution.P1440,
    val selectedGameCategoryFilter: GameCategory? = null,
    val activePickerSlot: VsSlot? = null,
    val pickerSearchQuery: String = "",
    val pickerItems: List<HardwareItem> = emptyList(),
    val suggestedPair: Pair<HardwareItem, HardwareItem>? = null,
    val isLoadingCatalog: Boolean = false,
) {
    val activeCategory: HardwareCategory
        get() = when (mode) {
            VsArenaMode.COMPONENT_VS_COMPONENT -> componentCategory
            VsArenaMode.PREBUILT_VS_PREBUILT -> HardwareCategory.PREBUILT_PC
            VsArenaMode.LAPTOP_VS_LAPTOP -> HardwareCategory.LAPTOP
        }

    companion object {
        val COMPONENT_CATEGORIES = listOf(
            HardwareCategory.GPU,
            HardwareCategory.CPU,
            HardwareCategory.MOTHERBOARD,
            HardwareCategory.RAM,
            HardwareCategory.STORAGE,
            HardwareCategory.PSU,
            HardwareCategory.COOLER,
            HardwareCategory.CASE,
            HardwareCategory.MONITOR,
            HardwareCategory.MOUSE,
            HardwareCategory.KEYBOARD,
            HardwareCategory.HEADSET,
        )
    }
}
