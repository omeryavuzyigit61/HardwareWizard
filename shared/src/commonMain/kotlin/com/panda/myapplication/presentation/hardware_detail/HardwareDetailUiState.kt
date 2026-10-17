package com.panda.myapplication.presentation.hardware_detail

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.HardwareItem

@Immutable
data class HardwareDetailUiState(
    val isLoading: Boolean = true,
    val item: HardwareItem? = null,
    val errorMessage: String? = null,
    val isAddedToBuild: Boolean = false,
)
