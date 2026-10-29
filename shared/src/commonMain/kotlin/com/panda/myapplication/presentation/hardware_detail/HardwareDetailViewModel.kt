package com.panda.myapplication.presentation.hardware_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.CommunityDeal
import com.panda.myapplication.domain.model.CommunityDealType
import com.panda.myapplication.domain.model.DealStatus
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareProTip
import com.panda.myapplication.domain.model.PriceDropAlert
import com.panda.myapplication.domain.model.TuningComment
import com.panda.myapplication.domain.model.TuningProfile
import com.panda.myapplication.domain.model.TuningReactionType
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.CommunityPriceRepository
import com.panda.myapplication.domain.repository.TuningVaultRepository
import com.panda.myapplication.domain.repository.UserRepository
import com.panda.myapplication.domain.repository.VsArenaRepository
import com.panda.myapplication.domain.usecase.GetHardwareDetailUseCase
import kotlinx.coroutines.Job
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
    data class ShowToast(val message: String, val messageEn: String? = null) : HardwareDetailEffect
    data class ShowVsAddedToast(val message: String, val messageEn: String? = null) : HardwareDetailEffect
}

class HardwareDetailViewModel(
    private val getHardwareDetailUseCase: GetHardwareDetailUseCase,
    private val activeBuildRepository: ActiveBuildRepository,
    private val vsArenaRepository: VsArenaRepository,
    private val communityPriceRepository: CommunityPriceRepository,
    private val userRepository: UserRepository,
    private val tuningVaultRepository: TuningVaultRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareDetailUiState())
    val uiState: StateFlow<HardwareDetailUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<HardwareDetailEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private val tuningCommentsJobs = mutableMapOf<String, Job>()

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
            _uiState.update { it.copy(isLoading = false, errorMessage = "Geçersiz donanım kimliği.", errorMessageEn = "Invalid hardware ID.") }
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
                        val (message, messageEn) = when (val err = result.error) {
                            DomainError.Network -> "Ağ bağlantı hatası: Donanım detayları alınamadı." to "Network error: Hardware details could not be loaded."
                            DomainError.NotFound -> "Aradığınız donanım parçası bulunamadı." to "The requested hardware component could not be found."
                            DomainError.Storage -> "Yerel veritabanı okuma hatası." to "Local database read error."
                            is DomainError.Unknown -> (err.cause.message ?: "Beklenmeyen bir hata oluştu.") to (err.cause.message ?: "An unexpected error occurred.")
                        }
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = message,
                                errorMessageEn = messageEn,
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

        // Topluluk OC/UV Ayarlarını Dinle
        viewModelScope.launch {
            tuningVaultRepository.observeTuningsForHardware(id).collect { tunings ->
                _uiState.update { it.copy(tuningProfiles = tunings) }
                tunings.forEach { profile ->
                    if (!tuningCommentsJobs.containsKey(profile.id)) {
                        tuningCommentsJobs[profile.id] = viewModelScope.launch {
                            tuningVaultRepository.observeComments(profile.id).collect { comments ->
                                _uiState.update { state ->
                                    state.copy(tuningComments = state.tuningComments + (profile.id to comments))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Topluluk Mikro İpuçlarını Dinle
        viewModelScope.launch {
            tuningVaultRepository.observeTipsForHardware(id).collect { tips ->
                _uiState.update { it.copy(proTips = tips) }
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

    fun onSubmitTuningProfile(profile: TuningProfile) {
        viewModelScope.launch {
            when (val res = tuningVaultRepository.submitTuningProfile(profile)) {
                is DomainResult.Success -> {
                    effectChannel.send(
                        HardwareDetailEffect.ShowToast(
                            "⚡ OC/UV ayarınız başarıyla onaya gönderildi! Yönetici onayından sonra yayınlanacaktır.",
                            "⚡ Your OC/UV tuning profile was sent for review!"
                        )
                    )
                }
                is DomainResult.Failure -> {
                    val errorMsg = (res.error as? DomainError.Unknown)?.cause?.message ?: "Ayar gönderilemedi."
                    effectChannel.send(HardwareDetailEffect.ShowToast(errorMsg))
                }
            }
        }
    }

    fun onToggleReaction(tuningId: String, reactionType: TuningReactionType) {
        val user = _uiState.value.currentUser
        val userId = user?.id ?: "guest_${(1000..9999).random()}"
        val tuning = _uiState.value.tuningProfiles.find { it.id == tuningId }
        if (tuning != null && (tuning.userId == userId || tuning.userDisplayName == user?.username)) {
            viewModelScope.launch {
                effectChannel.send(
                    HardwareDetailEffect.ShowToast(
                        "⚠️ Kendi paylaştığınız ayara tepki veremezsiniz.",
                        "⚠️ You cannot react to your own profile."
                    )
                )
            }
            return
        }
        viewModelScope.launch {
            tuningVaultRepository.toggleReaction(tuningId, reactionType, userId)
        }
    }

    fun onAddTuningComment(tuningId: String, text: String) {
        if (text.isBlank()) return
        val user = _uiState.value.currentUser
        val comment = TuningComment(
            id = "comment-${System.currentTimeMillis()}-${(100..999).random()}",
            tuningId = tuningId,
            userId = user?.id ?: "usr-guest",
            userDisplayName = user?.username ?: "DonanımSever",
            userBadge = user?.effectiveBadge ?: "Lehim Çırağı",
            userReputation = user?.reputationScore ?: 10,
            commentText = text.trim(),
            createdAtEpochMs = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            when (val res = tuningVaultRepository.addComment(comment)) {
                is DomainResult.Success -> {
                    effectChannel.send(
                        HardwareDetailEffect.ShowToast(
                            "💬 Yorumunuz başarıyla paylaşıldı (+2 itibar puanı)!",
                            "💬 Comment posted (+2 reputation)!"
                        )
                    )
                }
                is DomainResult.Failure -> {
                    val errorMsg = (res.error as? DomainError.Unknown)?.cause?.message ?: "Yorum gönderilemedi."
                    effectChannel.send(HardwareDetailEffect.ShowToast(errorMsg))
                }
            }
        }
    }

    fun onSubmitProTip(tipText: String, rating: Int = 5) {
        val user = _uiState.value.currentUser
        val item = _uiState.value.item ?: return
        val tip = HardwareProTip(
            id = "review-${System.currentTimeMillis()}-${(100..999).random()}",
            hardwareId = item.id.value,
            hardwareName = item.displayName,
            userId = user?.id ?: "usr-guest",
            userDisplayName = user?.username ?: "DonanımSever",
            userBadge = user?.effectiveBadge ?: "Lehim Çırağı",
            tipText = tipText,
            createdAtEpochMs = System.currentTimeMillis(),
            rating = rating,
        )
        viewModelScope.launch {
            when (val res = tuningVaultRepository.submitTip(tip)) {
                is DomainResult.Success -> {
                    effectChannel.send(HardwareDetailEffect.ShowToast("💬 Değerlendirmeniz ve yorumunuz paylaşıldı (+10 itibar puanı)!", "💬 Your review has been posted!"))
                }
                is DomainResult.Failure -> {
                    val errorMsg = (res.error as? DomainError.Unknown)?.cause?.message ?: "Yorum gönderilemedi."
                    effectChannel.send(HardwareDetailEffect.ShowToast(errorMsg))
                }
            }
        }
    }

    fun onDeleteTip(tipId: String) {
        viewModelScope.launch {
            tuningVaultRepository.deleteTip(tipId)
        }
    }

    fun onUpvoteProTip(tipId: String) {
        val user = _uiState.value.currentUser
        val userId = user?.id ?: "guest"
        val tip = _uiState.value.proTips.find { it.id == tipId }
        if (tip != null && (tip.userId == userId || tip.userDisplayName == user?.username)) {
            viewModelScope.launch {
                effectChannel.send(
                    HardwareDetailEffect.ShowToast(
                        "⚠️ Kendi paylaştığınız mikro ipucuna oy veremezsiniz.",
                        "⚠️ You cannot vote on your own tip."
                    )
                )
            }
            return
        }
        viewModelScope.launch {
            tuningVaultRepository.upvoteTip(tipId, userId)
        }
    }

    fun onCreatePriceDropAlert(targetPriceTry: Double) {
        val user = _uiState.value.currentUser
        val item = _uiState.value.item ?: return
        val alert = PriceDropAlert(
            id = "alert-${System.currentTimeMillis()}-${(100..999).random()}",
            userId = user?.id ?: "usr-guest",
            hardwareId = item.id.value,
            hardwareName = item.displayName,
            targetPriceTry = targetPriceTry,
            onlyRetailCampaign = true,
            createdAtEpochMs = System.currentTimeMillis(),
        )
        viewModelScope.launch {
            tuningVaultRepository.createPriceAlert(alert)
            effectChannel.send(
                HardwareDetailEffect.ShowToast(
                    "🔔 ${targetPriceTry.toInt()} ₺ için sıfır/perakende fiyat alarmı kuruldu!",
                    "🔔 Price drop alert set for retail campaigns at ${targetPriceTry.toInt()} ₺!"
                )
            )
        }
    }

    fun onRefreshCommunityDeals() {
        viewModelScope.launch {
            communityPriceRepository.refreshDeals()
            effectChannel.send(HardwareDetailEffect.ShowToast("🔄 Fiyat defteri güncellendi.", "🔄 Price ledger refreshed."))
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
        val messageEn = if (isNowAdded) "$name added to Wizard!" else "$name removed from Wizard."
        viewModelScope.launch {
            effectChannel.send(HardwareDetailEffect.ShowToast(message, messageEn))
        }
    }

    fun onAddToVsArena() {
        val item = _uiState.value.item ?: return
        val feedback = vsArenaRepository.addQuickItemBilingual(item)
        viewModelScope.launch {
            effectChannel.send(HardwareDetailEffect.ShowVsAddedToast(feedback.messageTr, feedback.messageEn))
        }
    }

    fun onSaveCustomPrice(customPriceTry: Double, note: String?) {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            getHardwareDetailUseCase.setCustomPrice(item.id, customPriceTry, note)
            effectChannel.send(HardwareDetailEffect.ShowToast("🏷️ Özel yerel fiyat kaydedildi.", "🏷️ Local custom price saved."))
        }
    }

    fun onClearCustomPrice() {
        val item = _uiState.value.item ?: return
        viewModelScope.launch {
            getHardwareDetailUseCase.clearCustomPrice(item.id)
            effectChannel.send(HardwareDetailEffect.ShowToast("Özel fiyat kaldırıldı.", "Custom price cleared."))
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
                effectChannel.send(HardwareDetailEffect.ShowToast("⚠️ Fiyat talebi oluşturmak için lütfen önce giriş yapın!", "⚠️ Please sign in first to submit a community deal!"))
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
                HardwareDetailEffect.ShowToast("🚀 Fiyat bildiriminiz (@${user.username}) gönderildi! Yönetici onayından sonra yayınlanacaktır.", "🚀 Your price deal submission was sent! It will appear after admin review.")
            )
        }
    }

    // Yönetici Hızlı Onaylama
    fun onApproveCommunityDeal(dealId: String) {
        viewModelScope.launch {
            communityPriceRepository.approveDeal(dealId)
            effectChannel.send(HardwareDetailEffect.ShowToast("✅ Fiyat bildirimi onaylandı ve deftere eklendi.", "✅ Price submission approved and added to ledger."))
        }
    }

    // Yönetici Reddetme
    fun onRejectCommunityDeal(dealId: String, reason: String? = null) {
        viewModelScope.launch {
            communityPriceRepository.rejectDeal(dealId, reason)
            effectChannel.send(HardwareDetailEffect.ShowToast("❌ Fiyat bildirimi reddedildi.", "❌ Price submission rejected."))
        }
    }

    // Admin Giriş/Çıkış
    fun loginAdmin(pin: String): Boolean = communityPriceRepository.loginAdmin(pin)

    fun logoutAdmin() = communityPriceRepository.logoutAdmin()
}
