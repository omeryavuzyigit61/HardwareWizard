package com.panda.myapplication.presentation.hardware_list

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem

@Immutable
data class HardwareListUiState(
    val isLoading: Boolean = false,
    val items: List<HardwareItem> = emptyList(),
    val totalCount: Int = 0,
    val selectedCategory: HardwareCategory? = null,
    val searchQuery: String = "",
    val filters: HardwareFilterOptions = HardwareFilterOptions(),
    val availableBrands: List<String> = emptyList(),
    val isFilterSheetVisible: Boolean = false,
    val errorMessage: String? = null,
)
