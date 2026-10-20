package com.panda.myapplication.data.repository

import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.VsArenaMode
import com.panda.myapplication.domain.repository.VsArenaRepository
import com.panda.myapplication.domain.repository.VsQuickAddFeedback
import com.panda.myapplication.domain.repository.VsArenaSelectionState
import com.panda.myapplication.domain.repository.VsSlot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class VsArenaRepositoryImpl : VsArenaRepository {

    private val _state = MutableStateFlow(VsArenaSelectionState())
    override val state: StateFlow<VsArenaSelectionState> = _state.asStateFlow()

    override fun selectMode(mode: VsArenaMode) {
        _state.update { current ->
            if (current.mode == mode) return@update current
            val targetCategory = when (mode) {
                VsArenaMode.COMPONENT_VS_COMPONENT -> current.componentCategory
                VsArenaMode.PREBUILT_VS_PREBUILT -> HardwareCategory.PREBUILT_PC
                VsArenaMode.LAPTOP_VS_LAPTOP -> HardwareCategory.LAPTOP
            }
            val keepLeft = current.leftItem?.takeIf { it.category == targetCategory }
            val keepRight = current.rightItem?.takeIf { it.category == targetCategory }
            current.copy(
                mode = mode,
                leftItem = keepLeft,
                rightItem = keepRight,
            )
        }
    }

    override fun selectComponentCategory(category: HardwareCategory) {
        if (category == HardwareCategory.PREBUILT_PC || category == HardwareCategory.LAPTOP) return
        _state.update { current ->
            val keepLeft = current.leftItem?.takeIf { it.category == category }
            val keepRight = current.rightItem?.takeIf { it.category == category }
            current.copy(
                mode = VsArenaMode.COMPONENT_VS_COMPONENT,
                componentCategory = category,
                leftItem = keepLeft,
                rightItem = keepRight,
            )
        }
    }

    override fun setSlotItem(slot: VsSlot, item: HardwareItem?) {
        _state.update { current ->
            when (slot) {
                VsSlot.LEFT -> current.copy(leftItem = item)
                VsSlot.RIGHT -> current.copy(rightItem = item)
            }
        }
    }

    override fun swapSlots() {
        _state.update { current ->
            current.copy(
                leftItem = current.rightItem,
                rightItem = current.leftItem,
            )
        }
    }

    override fun clearArena() {
        _state.update { current ->
            current.copy(leftItem = null, rightItem = null)
        }
    }

    override fun addQuickItemBilingual(item: HardwareItem): VsQuickAddFeedback {
        var feedbackTr = ""
        var feedbackEn = ""
        _state.update { current ->
            val targetMode = when (item.category) {
                HardwareCategory.PREBUILT_PC -> VsArenaMode.PREBUILT_VS_PREBUILT
                HardwareCategory.LAPTOP -> VsArenaMode.LAPTOP_VS_LAPTOP
                else -> VsArenaMode.COMPONENT_VS_COMPONENT
            }
            val newComponentCategory = if (targetMode == VsArenaMode.COMPONENT_VS_COMPONENT) {
                item.category
            } else {
                current.componentCategory
            }

            val sameCategoryLeft = current.leftItem?.takeIf { it.category == item.category }
            val sameCategoryRight = current.rightItem?.takeIf { it.category == item.category }

            when {
                sameCategoryLeft?.id == item.id -> {
                    feedbackTr = "⚔️ ${item.displayName} zaten VS Arena 1. Köşede seçili."
                    feedbackEn = "⚔️ ${item.displayName} is already selected in Corner 1."
                    current.copy(
                        mode = targetMode,
                        componentCategory = newComponentCategory,
                        leftItem = item,
                        rightItem = sameCategoryRight,
                    )
                }
                sameCategoryRight?.id == item.id -> {
                    feedbackTr = "⚔️ ${item.displayName} zaten VS Arena 2. Köşede seçili."
                    feedbackEn = "⚔️ ${item.displayName} is already selected in Corner 2."
                    current.copy(
                        mode = targetMode,
                        componentCategory = newComponentCategory,
                        leftItem = sameCategoryLeft,
                        rightItem = item,
                    )
                }
                sameCategoryLeft == null -> {
                    feedbackTr = "⚡ ${item.displayName} VS Arena 1. Köşeye eklendi! Karşılaştırmak için 2. ürünü seçin."
                    feedbackEn = "⚡ ${item.displayName} added to Corner 1! Select a 2nd product to compare."
                    current.copy(
                        mode = targetMode,
                        componentCategory = newComponentCategory,
                        leftItem = item,
                        rightItem = sameCategoryRight,
                    )
                }
                sameCategoryRight == null -> {
                    feedbackTr = "🔥 ${item.displayName} VS Arena 2. Köşeye eklendi! Kafa kafaya kıyaslama hazır."
                    feedbackEn = "🔥 ${item.displayName} added to Corner 2! Head-to-head comparison is ready."
                    current.copy(
                        mode = targetMode,
                        componentCategory = newComponentCategory,
                        leftItem = sameCategoryLeft,
                        rightItem = item,
                    )
                }
                else -> {
                    feedbackTr = "🔥 ${item.displayName} VS Arena 2. Köşeye alındı (${sameCategoryRight.model} 1. Köşeye kaydırıldı)."
                    feedbackEn = "🔥 ${item.displayName} moved to Corner 2 (${sameCategoryRight.model} shifted to Corner 1)."
                    current.copy(
                        mode = targetMode,
                        componentCategory = newComponentCategory,
                        leftItem = sameCategoryRight,
                        rightItem = item,
                    )
                }
            }
        }
        return VsQuickAddFeedback(feedbackTr, feedbackEn)
    }

    override fun addQuickItem(item: HardwareItem): String = addQuickItemBilingual(item).messageTr
}
