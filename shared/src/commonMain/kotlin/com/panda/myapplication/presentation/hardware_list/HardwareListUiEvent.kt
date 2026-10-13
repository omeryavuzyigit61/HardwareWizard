package com.panda.myapplication.presentation.hardware_list

import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem

sealed interface HardwareListUiEvent {
    data class OnCategorySelected(val category: HardwareCategory?) : HardwareListUiEvent
    data class OnSearchQueryChanged(val query: String) : HardwareListUiEvent
    data class OnHardwareItemClicked(val item: HardwareItem) : HardwareListUiEvent
    data object OnToggleFilterSheet : HardwareListUiEvent
    data class OnFiltersChanged(val filters: HardwareFilterOptions) : HardwareListUiEvent
    data object OnResetFilters : HardwareListUiEvent
    data object OnRefresh : HardwareListUiEvent
}

sealed interface HardwareListEffect {
    data class NavigateToDetail(val item: HardwareItem) : HardwareListEffect
    data class ShowToast(val message: String) : HardwareListEffect
}
