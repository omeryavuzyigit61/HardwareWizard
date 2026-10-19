package com.panda.myapplication.presentation.hardware_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.DealStatus
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.CommunityPriceRepository
import com.panda.myapplication.domain.repository.UserRepository
import com.panda.myapplication.domain.repository.VsArenaRepository
import com.panda.myapplication.domain.usecase.GetHardwareDetailUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface HardwareDetailEffect {
    data class ShowToast(val message: String) : HardwareDetailEffect
    data class ShowVsAddedToast(val message: String) : HardwareDetailEffect
}

class HardwareDetailViewModel(
    private val getHardwareDetailUseCase: GetHardwareDetailUseCase,
    private val activeBuildRepository: ActiveBuildRepository,
    private val vsArenaRepository: VsArenaRepository,
    private val communityPriceRepository: CommunityPriceRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareDetailUiState())
    val uiState: StateFlow<HardwareDetailUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<HardwareDetailEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    init {
        viewModelScope.launch {
            communityPriceRepository.isAdminLoggedIn.collect { isAdmin ->
                _uiState.update { it.copy(isAdmin = isAdmin) }
            }
        }

        viewModelScope.launch {
            userRepository.currentUser.collect { user ->
                _uiState.update { it.copy(currentUser = user, isLoggedIn = user != null) }
            }
        }
    }

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

        // Topluluk Fırsat Akışını (Deals) Dinle
        viewModelScope.launch {
            communityPriceRepository.observeDealsForHardware(id).collect { deals ->
                _uiState.update { it.copy(communityDeals = deals) }
            }
        }

        // Canlı Arka Plan Senkronizasyonu: Desktop Admin veya Supabase'deki onayları anında yakala
        viewModelScope.launch {
            communityPriceRepository.refreshDeals()
            while (isActive) {
                delay(5000)
                communityPriceRepository.refreshDeals()
            }
        }
    }

    fun onRefreshCommunityDeals() {
        viewModelScope.launch {
            communityPriceRepository.refreshDeals()
            effectChannel.send(HardwareDetailEffect.ShowToast("🔄 Fiyat defteri güncellendi."))
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

    fun onAddToVsArena() {
        val item = _uiState.value.item ?: return
        val statusMessage = vsArenaRepository.addItemToComparison(item)
        viewModelScope.launch {
            effectChannel.send(HardwareDetailEffect.ShowVsAddedToast(statusMessage))
        }
    }

    fun onSaveCustomPrice(customPriceTry: Double, note: String?) {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            getHardwareDetailUseCase.setCustomPrice(item.id, customPriceTry, note)
            effectChannel.send(HardwareDetailEffect.ShowToast("🏷️ Özel yerel fiyat kaydedildi."))
        }
    }

    fun onClearCustomPrice() {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            getHardwareDetailUseCase.clearCustomPrice(item.id)
            effectChannel.send(HardwareDetailEffect.ShowToast("Özel fiyat kaldırıldı."))
        }
    }

    // Topluluk Fiyat Bildirimi (Supabase & Canlı Defter)
    fun onSubmitCommunityDeal(
        priceTry: Double,
        dealType: CommunityDealType,
        note: String?,
        proofUrl: String?,
        userDisplayName: String,
    ) {
        val user = userRepository.currentUser.value
        if (user == null || !userRepository.isLoggedIn.value) {
            viewModelScope.launch {
                effectChannel.send(HardwareDetailEffect.ShowToast("⚠️ Fiyat talebi oluşturmak için lütfen önce giriş yapın!"))
            }
            return
        }

        val item = _uiState.value.item ?: return
        val dealId = "deal-${item.id.value}-${kotlin.random.Random.nextInt(10000, 99999)}"
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()

        val deal = CommunityDeal(
            id = dealId,
            hardwareId = item.id.value,
            hardwareName = item.displayName,
            priceTry = priceTry,
            dealType = dealType,
            note = note?.takeIf { it.isNotBlank() },
            proofUrl = proofUrl?.takeIf { it.isNotBlank() },
            userDisplayName = "@${user.username}",
            userEmail = user.email,
            createdAtEpochMs = now,
            status = DealStatus.PENDING,
        )

        viewModelScope.launch {
            communityPriceRepository.submitDeal(deal)
            effectChannel.send(
                HardwareDetailEffect.ShowToast("🚀 Fiyat bildiriminiz (@${user.username}) gönderildi! Yönetici onayından sonra yayınlanacaktır.")
            )
        }
    }

    // Yönetici Hızlı Onaylama
    fun onApproveCommunityDeal(dealId: String) {
        viewModelScope.launch {
            communityPriceRepository.approveDeal(dealId)
            effectChannel.send(HardwareDetailEffect.ShowToast("✅ Fiyat bildirimi onaylandı ve deftere eklendi."))
        }
    }

    // Yönetici Reddetme
    fun onRejectCommunityDeal(dealId: String, reason: String? = null) {
        viewModelScope.launch {
            communityPriceRepository.rejectDeal(dealId, reason)
            effectChannel.send(HardwareDetailEffect.ShowToast("❌ Fiyat bildirimi reddedildi."))
        }
    }

    // Admin Giriş/Çıkış
    fun loginAdmin(pin: String): Boolean = communityPriceRepository.loginAdmin(pin)

    fun logoutAdmin() = communityPriceRepository.logoutAdmin()
}
