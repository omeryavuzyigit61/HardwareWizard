package com.panda.myapplication.presentation.vs_arena

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.engine.HardwareSearchEngine
import com.panda.myapplication.domain.engine.VsArenaEngine
import com.panda.myapplication.domain.model.GameCategory
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.repository.VsArenaMode
import com.panda.myapplication.domain.repository.VsArenaRepository
import com.panda.myapplication.domain.repository.VsSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class LocalArenaControls(
    val selectedResolution: GamingResolution = GamingResolution.P1440,
    val selectedGameCategoryFilter: GameCategory? = null,
    val activePickerSlot: VsSlot? = null,
)

@OptIn(FlowPreview::class)
class VsArenaViewModel(
    private val hardwareRepository: HardwareRepository,
    private val vsArenaRepository: VsArenaRepository,
) : ViewModel() {

    private val localControls = MutableStateFlow(LocalArenaControls())
    private val _pickerSearchQuery = MutableStateFlow("")

    private val debouncedSearchQuery = _pickerSearchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    val uiState: StateFlow<VsArenaUiState> = combine(
        vsArenaRepository.state,
        hardwareRepository.observeHardwareItems(),
        localControls,
        debouncedSearchQuery,
    ) { arenaState, domainResult, controls, debouncedQuery ->
        val allItems = when (domainResult) {
            is DomainResult.Success -> domainResult.data
            else -> emptyList()
        }
        val isLoading = false

        val activeCategory = arenaState.activeCategory
        val categoryItems = allItems
            .filter { it.category == activeCategory }
            .sortedWith(
                compareByDescending<HardwareItem> { it.currentPriceTry != null }
                    .thenByDescending { VsArenaEngine.computeCompositePerformanceScore(it) }
            )

        // Ensure slot items stay enriched with the latest catalog data if their IDs match
        val resolvedLeft = arenaState.leftItem?.let { slotItem ->
            categoryItems.firstOrNull { it.id == slotItem.id } ?: slotItem.takeIf { it.category == activeCategory }
        }
        val resolvedRight = arenaState.rightItem?.let { slotItem ->
            categoryItems.firstOrNull { it.id == slotItem.id } ?: slotItem.takeIf { it.category == activeCategory }
        }

        val report = if (resolvedLeft != null && resolvedRight != null) {
            VsArenaEngine.compare(
                left = resolvedLeft,
                right = resolvedRight,
                mode = arenaState.mode,
            )
        } else null

        val filteredPickerItems = if (debouncedQuery.isBlank()) {
            categoryItems
        } else {
            HardwareSearchEngine.search(categoryItems, debouncedQuery)
        }

        val suggestedPair = if (categoryItems.size >= 2) {
            val priced = categoryItems.filter { it.currentPriceTry != null }
            if (priced.size >= 2) {
                priced[0] to priced[1]
            } else {
                categoryItems[0] to categoryItems[1]
            }
        } else null

        VsArenaUiState(
            mode = arenaState.mode,
            componentCategory = arenaState.componentCategory,
            leftItem = resolvedLeft,
            rightItem = resolvedRight,
            report = report,
            selectedResolution = controls.selectedResolution,
            selectedGameCategoryFilter = controls.selectedGameCategoryFilter,
            activePickerSlot = controls.activePickerSlot,
            pickerSearchQuery = debouncedQuery,
            pickerItems = filteredPickerItems,
            suggestedPair = suggestedPair,
            isLoadingCatalog = isLoading,
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VsArenaUiState(isLoadingCatalog = true),
    )

    init {
        viewModelScope.launch {
            hardwareRepository.refreshHardwareItems()
        }
    }

    fun selectMode(mode: VsArenaMode) {
        vsArenaRepository.selectMode(mode)
    }

    fun selectComponentCategory(category: HardwareCategory) {
        vsArenaRepository.selectComponentCategory(category)
    }

    fun openPicker(slot: VsSlot) {
        _pickerSearchQuery.value = ""
        localControls.value = localControls.value.copy(
            activePickerSlot = slot,
        )
    }

    fun closePicker() {
        _pickerSearchQuery.value = ""
        localControls.value = localControls.value.copy(
            activePickerSlot = null,
        )
    }

    fun updatePickerSearch(query: String) {
        _pickerSearchQuery.value = query
    }

    fun selectItemForActiveSlot(item: HardwareItem) {
        val slot = localControls.value.activePickerSlot ?: return
        vsArenaRepository.setSlotItem(slot, item)
        closePicker()
    }

    fun clearSlot(slot: VsSlot) {
        vsArenaRepository.setSlotItem(slot, null)
    }

    fun swapSlots() {
        vsArenaRepository.swapSlots()
    }

    fun clearAll() {
        vsArenaRepository.clearArena()
    }

    fun applySuggestedPair() {
        val pair = uiState.value.suggestedPair ?: return
        vsArenaRepository.setSlotItem(VsSlot.LEFT, pair.first)
        vsArenaRepository.setSlotItem(VsSlot.RIGHT, pair.second)
    }

    fun selectResolution(resolution: GamingResolution) {
        localControls.value = localControls.value.copy(selectedResolution = resolution)
    }

    fun selectGameCategoryFilter(category: GameCategory?) {
        localControls.value = localControls.value.copy(selectedGameCategoryFilter = category)
    }
}
