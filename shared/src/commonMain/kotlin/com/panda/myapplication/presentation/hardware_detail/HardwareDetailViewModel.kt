package com.panda.myapplication.presentation.hardware_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.usecase.GetHardwareDetailUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HardwareDetailEffect {
    data class ShowToast(val message: String) : HardwareDetailEffect
}

class HardwareDetailViewModel(
    private val getHardwareDetailUseCase: GetHardwareDetailUseCase,
    private val activeBuildRepository: ActiveBuildRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareDetailUiState())
    val uiState: StateFlow<HardwareDetailUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<HardwareDetailEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    fun loadItem(id: String) {
        if (id.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Geçersiz donanım kimliği.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getHardwareDetailUseCase(HardwareId(id)).collect { result ->
                when (result) {
                    is DomainResult.Success -> {
                        val isAdded = activeBuildRepository.isItemInBuild(result.data.id)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                item = result.data,
                                isAddedToBuild = isAdded,
                                errorMessage = null,
                            )
                        }
                    }
                    is DomainResult.Failure -> {
                        val message = when (val err = result.error) {
                            DomainError.Network -> "Ağ bağlantı hatası: Donanım detayları alınamadı."
                            DomainError.NotFound -> "Aradığınız donanım parçası bulunamadı."
                            DomainError.Storage -> "Yerel veritabanı okuma hatası."
                            is DomainError.Unknown -> err.cause.message ?: "Beklenmeyen bir hata oluştu."
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = message,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onAddToBuild() {
        val current = _uiState.value
        val item = current.item ?: return
        activeBuildRepository.toggleComponent(item)
        val isNowAdded = activeBuildRepository.isItemInBuild(item.id)
        _uiState.update { it.copy(isAddedToBuild = isNowAdded) }

        val name = item.displayName
        val message = if (isNowAdded) "$name sihirbaza eklendi!" else "$name sihirbazdan çıkarıldı."
        viewModelScope.launch {
            effectChannel.send(HardwareDetailEffect.ShowToast(message))
        }
    }
}
