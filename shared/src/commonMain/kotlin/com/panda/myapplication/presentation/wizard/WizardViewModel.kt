package com.panda.myapplication.presentation.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.engine.AnalysisState
import com.panda.myapplication.domain.engine.BuildAnalysisEngine
import com.panda.myapplication.domain.engine.ResolutionOption
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.ActiveBuildRepository
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase
import com.panda.myapplication.domain.usecase.GetCompatibleHardwareForSlotUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.panda.myapplication.presentation.util.formatPrice

sealed interface WizardEffect {
    data class ShowToast(val message: String) : WizardEffect
    data class ShareBuild(val text: String) : WizardEffect
}

private data class PickerParams(
    val isOpen: Boolean,
    val category: HardwareCategory?,
    val query: String,
)

class WizardViewModel(
    private val activeBuildRepository: ActiveBuildRepository,
    private val hardwareRepository: HardwareRepository,
    private val checkBuildCompatibilityUseCase: CheckBuildCompatibilityUseCase,
    private val getCompatibleHardwareForSlotUseCase: GetCompatibleHardwareForSlotUseCase,
) : ViewModel() {

    private val effectChannel = Channel<WizardEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private val _selectedResolution = MutableStateFlow(ResolutionOption.P1440)
    private val _isPickerOpen = MutableStateFlow(false)
    private val _pickerCategory = MutableStateFlow<HardwareCategory?>(null)
    private val _pickerSearchQuery = MutableStateFlow("")
    private val _isPickerLoading = MutableStateFlow(false)
    private val _isShareDialogOpen = MutableStateFlow(false)

    private val buildAnalysisFlow = combine(
        activeBuildRepository.activeBuild,
        _selectedResolution,
    ) { build, resolution ->
        val cockpit = BuildAnalysisEngine.calculateWattageCockpit(build)
        val platform = BuildAnalysisEngine.calculatePlatformStatus(build)
        val bottleneck = BuildAnalysisEngine.calculateBottleneck(build)
        val clearance = BuildAnalysisEngine.calculateClearanceRadar(build)
        val gaming = BuildAnalysisEngine.calculateGamingSimulation(build, resolution)
        val (_, compat) = checkBuildCompatibilityUseCase(build)

        AnalysisState(
            build = build,
            cockpitInfo = cockpit,
            platformStatus = platform,
            bottleneck = bottleneck,
            clearance = clearance,
            gamingSim = gaming,
            compatReport = compat,
            resolution = resolution,
        )
    }

    private val pickerParamsFlow = combine(
        _isPickerOpen,
        _pickerCategory,
        _pickerSearchQuery,
    ) { isOpen, category, query ->
        PickerParams(isOpen, category, query)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val pickerRawItemsFlow = _pickerCategory.flatMapLatest { category ->
        if (category == null) {
            flowOf(emptyList())
        } else {
            _isPickerLoading.value = true
            hardwareRepository.observeHardwareItemsByCategory(category).map { result ->
                _isPickerLoading.value = false
                when (result) {
                    is DomainResult.Success -> result.data
                    is DomainResult.Failure -> emptyList()
                }
            }
        }
    }

    val uiState: StateFlow<WizardUiState> = combine(
        buildAnalysisFlow,
        pickerParamsFlow,
        pickerRawItemsFlow,
        _isShareDialogOpen,
    ) { analysis, pickerParams, rawCandidates, isShareOpen ->
        val (isPickerOpen, pickerCategory, searchQuery) = pickerParams

        val (items, constraintDesc) = if (isPickerOpen && pickerCategory != null) {
            val compatible = getCompatibleHardwareForSlotUseCase(pickerCategory, analysis.build, rawCandidates)
            val filtered = if (searchQuery.isNotBlank()) {
                compatible.items.filter {
                    it.displayName.contains(searchQuery, ignoreCase = true) ||
                            it.brand.contains(searchQuery, ignoreCase = true)
                }
            } else {
                compatible.items
            }
            filtered to compatible.filterConstraintDescription
        } else {
            emptyList<HardwareItem>() to null
        }

        val shareText = if (isShareOpen) generateShareSummary(analysis) else ""

        WizardUiState(
            activeBuild = analysis.build,
            cockpitInfo = analysis.cockpitInfo,
            platformStatus = analysis.platformStatus,
            bottleneckAnalysis = analysis.bottleneck,
            clearanceRadar = analysis.clearance,
            selectedResolution = analysis.resolution,
            gamingSimulation = analysis.gamingSim,
            compatibilityReport = analysis.compatReport,
            isPickerOpen = isPickerOpen,
            pickerCategory = pickerCategory,
            pickerSearchQuery = searchQuery,
            pickerConstraintDescription = constraintDesc,
            pickerItems = items,
            isPickerLoading = _isPickerLoading.value,
            isShareDialogOpen = isShareOpen,
            shareCardText = shareText,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = WizardUiState(),
    )

    fun onSlotClicked(category: HardwareCategory) {
        _pickerCategory.value = category
        _pickerSearchQuery.value = ""
        _isPickerOpen.value = true
    }

    fun onPickerSearchQueryChange(query: String) {
        _pickerSearchQuery.value = query
    }

    fun onSelectComponent(item: HardwareItem) {
        activeBuildRepository.setComponent(item)
        _isPickerOpen.value = false
        _pickerCategory.value = null
        _pickerSearchQuery.value = ""
        viewModelScope.launch {
            effectChannel.send(WizardEffect.ShowToast("${item.displayName} sisteme eklendi."))
        }
    }

    fun onRemoveSlot(category: HardwareCategory) {
        activeBuildRepository.removeComponent(category)
        viewModelScope.launch {
            effectChannel.send(WizardEffect.ShowToast("${category.name} sistemden çıkarıldı."))
        }
    }

    fun onClearBuild() {
        activeBuildRepository.clearBuild()
        viewModelScope.launch {
            effectChannel.send(WizardEffect.ShowToast("Tüm parçalar sıfırlandı."))
        }
    }

    fun onResolutionChanged(resolution: ResolutionOption) {
        _selectedResolution.value = resolution
    }

    fun onOpenShareDialog() {
        _isShareDialogOpen.value = true
    }

    fun onDismissShareDialog() {
        _isShareDialogOpen.value = false
    }

    fun onDismissPicker() {
        _isPickerOpen.value = false
        _pickerCategory.value = null
        _pickerSearchQuery.value = ""
    }

    fun onShareIntentRequested() {
        val text = generateShareSummary(
            AnalysisState(
                build = uiState.value.activeBuild,
                cockpitInfo = uiState.value.cockpitInfo,
                platformStatus = uiState.value.platformStatus,
                bottleneck = uiState.value.bottleneckAnalysis,
                clearance = uiState.value.clearanceRadar,
                gamingSim = uiState.value.gamingSimulation,
                compatReport = uiState.value.compatibilityReport,
                resolution = uiState.value.selectedResolution,
            )
        )
        viewModelScope.launch {
            effectChannel.send(WizardEffect.ShareBuild(text))
        }
    }

    private fun generateShareSummary(analysis: AnalysisState): String {
        val b = analysis.build
        val priceFormatted = if (b.totalPriceTry > 0) "₺${b.totalPriceTry.formatPrice()}" else "N/A"
        return buildString {
            appendLine("═════════════════════════════════")
            appendLine("⚡ HARDWARE WIZARD // SYSTEM BUILD")
            appendLine("═════════════════════════════════")
            appendLine("Platform: ${analysis.platformStatus.platformName}")
            appendLine("Durum: ${if (analysis.compatReport.isCompatible) "✅ %100 Uyumlu" else "⚠️ Uyumsuzluk Bildirimi"}")
            appendLine()
            appendLine("BİLEŞENLER:")
            appendLine("• CPU:       ${b.cpu?.displayName ?: "Seçilmedi"}")
            appendLine("• Anakart:   ${b.motherboard?.displayName ?: "Seçilmedi"}")
            appendLine("• RAM:       ${b.ram?.displayName ?: "Seçilmedi"}")
            appendLine("• GPU:       ${b.gpu?.displayName ?: "Seçilmedi"}")
            appendLine("• Depolama:  ${b.storage?.displayName ?: "Seçilmedi"}")
            appendLine("• Soğutucu:  ${b.cooler?.displayName ?: "Seçilmedi"}")
            appendLine("• Kasa:      ${b.case?.displayName ?: "Seçilmedi"}")
            appendLine("• PSU:       ${b.psu?.displayName ?: "Seçilmedi"}")
            appendLine()
            appendLine("KOKPİT GÖSTERGELERİ:")
            appendLine("• Güç: ${analysis.cockpitInfo.componentTdpSum}W TDP (Tepe: ${analysis.cockpitInfo.peakTdpWithTolerance}W) | Önerilen PSU: ${analysis.cockpitInfo.recommendedPsuWattage}W")
            appendLine("• Darboğaz: ${analysis.bottleneck.message}")
            if (analysis.gamingSim.isReady) {
                appendLine()
                appendLine("OYUN PERFORMANSI (${analysis.gamingSim.resolution.label}):")
                analysis.gamingSim.games.forEach { g ->
                    appendLine("• ${g.gameName}: ~${g.fps} FPS (${g.settings})")
                }
                appendLine("SENTETİK PUANLAR:")
                analysis.gamingSim.synthetics.forEach { s ->
                    appendLine("• ${s.name}: ${s.score} ${s.unit}")
                }
            }
            appendLine()
            appendLine("Toplam Maliyet: $priceFormatted")
            appendLine("═════════════════════════════════")
            appendLine("Built with Hardware Wizard 🛠️")
        }
    }
}
