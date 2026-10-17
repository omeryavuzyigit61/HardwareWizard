package com.panda.myapplication.presentation.hardware_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.repository.VsArenaRepository
import com.panda.myapplication.domain.usecase.GetHardwareListUseCase
import com.panda.myapplication.domain.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class HardwareListViewModel(
    private val getHardwareListUseCase: GetHardwareListUseCase,
    private val repository: HardwareRepository,
    private val vsArenaRepository: VsArenaRepository,
) : ViewModel() {

    private val segmentMode = MutableStateFlow(CatalogSegmentMode.INDIVIDUAL)
    private val selectedCategory = MutableStateFlow<HardwareCategory?>(null)
    private val rawSearchQuery = MutableStateFlow("")
    private val filterOptions = MutableStateFlow(HardwareFilterOptions())
    private val isFilterSheetVisible = MutableStateFlow(false)
    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow(HardwareListUiState(isLoading = true))
    val uiState: StateFlow<HardwareListUiState> = _uiState.asStateFlow()

    private val effectChannel = Channel<HardwareListEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()

    private val debouncedSearchQuery: Flow<String> = rawSearchQuery
        .debounce(300L)
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)

    private val hardwareItemsFlow: Flow<DomainResult<List<HardwareItem>>> = combine(
        segmentMode,
        selectedCategory,
    ) { mode, category ->
        mode to category
    }.flatMapLatest { (mode, category) ->
        if (category != null) {
            getHardwareListUseCase(category)
        } else {
            val categories = when (mode) {
                CatalogSegmentMode.INDIVIDUAL -> INDIVIDUAL_CATEGORIES.toList()
                CatalogSegmentMode.PERIPHERALS -> PERIPHERAL_CATEGORIES.toList()
                CatalogSegmentMode.PREBUILT -> listOf(HardwareCategory.PREBUILT_PC)
                CatalogSegmentMode.LAPTOP -> listOf(HardwareCategory.LAPTOP)
            }
            combine(categories.map { getHardwareListUseCase(it) }) { results ->
                val all = mutableListOf<HardwareItem>()
                for (res in results) {
                    if (res is DomainResult.Success) {
                        all.addAll(res.data)
                    }
                }
                DomainResult.Success(all) as DomainResult<List<HardwareItem>>
            }
        }
    }

    private val availableBrandsFlow: Flow<List<String>> = hardwareItemsFlow.map { result ->
        when (result) {
            is DomainResult.Success -> result.data
                .map { it.brand.trim() }
                .filter { it.isNotBlank() }
                .distinct()
                .sorted()
            is DomainResult.Failure -> emptyList()
        }
    }

    private val filterStateFlow = combine(
        filterOptions,
        availableBrandsFlow,
        isFilterSheetVisible,
    ) { filters, brands, sheetVisible ->
        Triple(filters, brands, sheetVisible)
    }

    init {
        observeHardwareData()
        refresh(isManual = false)
    }

    fun onSearchQueryChange(newQuery: String) {
        rawSearchQuery.value = newQuery
    }

    fun onEvent(event: HardwareListUiEvent) {
        when (event) {
            is HardwareListUiEvent.OnSegmentModeSelected -> {
                segmentMode.value = event.mode
                val nextCategory = when (event.mode) {
                    CatalogSegmentMode.INDIVIDUAL -> null
                    CatalogSegmentMode.PERIPHERALS -> null
                    CatalogSegmentMode.PREBUILT -> HardwareCategory.PREBUILT_PC
                    CatalogSegmentMode.LAPTOP -> HardwareCategory.LAPTOP
                }
                selectedCategory.value = nextCategory
                filterOptions.value = filterOptions.value.copy(
                    selectedBrands = emptySet(),
                    sortOption = HardwareSortOption.DEFAULT,
                )
                _uiState.update {
                    it.copy(
                        segmentMode = event.mode,
                        selectedCategory = nextCategory,
                        filters = filterOptions.value,
                    )
                }
            }
            is HardwareListUiEvent.OnCategorySelected -> {
                val targetCategory = event.category
                val inferredMode = when {
                    targetCategory == null -> segmentMode.value.let { current ->
                        if (current == CatalogSegmentMode.PERIPHERALS) CatalogSegmentMode.PERIPHERALS else CatalogSegmentMode.INDIVIDUAL
                    }
                    targetCategory.isPeripheral -> CatalogSegmentMode.PERIPHERALS
                    targetCategory == HardwareCategory.PREBUILT_PC -> CatalogSegmentMode.PREBUILT
                    targetCategory == HardwareCategory.LAPTOP -> CatalogSegmentMode.LAPTOP
                    else -> CatalogSegmentMode.INDIVIDUAL
                }
                segmentMode.value = inferredMode
                selectedCategory.value = targetCategory
                val validSorts = HardwareSortOption.optionsForCategory(targetCategory)
                val nextSort = if (validSorts.contains(filterOptions.value.sortOption)) {
                    filterOptions.value.sortOption
                } else {
                    HardwareSortOption.DEFAULT
                }
                filterOptions.value = filterOptions.value.copy(
                    selectedBrands = emptySet(),
                    sortOption = nextSort,
                )
                _uiState.update {
                    it.copy(
                        segmentMode = inferredMode,
                        selectedCategory = targetCategory,
                        filters = filterOptions.value,
                    )
                }
            }
            is HardwareListUiEvent.OnSearchQueryChanged -> {
                onSearchQueryChange(event.query)
            }
            is HardwareListUiEvent.OnHardwareItemClicked -> {
                viewModelScope.launch {
                    effectChannel.send(HardwareListEffect.NavigateToDetail(event.item))
                }
            }
            is HardwareListUiEvent.OnAddToVsArena -> {
                val msg = vsArenaRepository.addQuickItem(event.item)
                viewModelScope.launch {
                    effectChannel.send(HardwareListEffect.ShowVsAddedToast(msg))
                }
            }
            is HardwareListUiEvent.OnSaveCustomPrice -> {
                viewModelScope.launch {
                    repository.setCustomPrice(event.itemId, event.customPriceTry, event.note)
                    effectChannel.send(HardwareListEffect.ShowToast("🏷️ Özel fiyat kaydedildi."))
                }
            }
            is HardwareListUiEvent.OnClearCustomPrice -> {
                viewModelScope.launch {
                    repository.clearCustomPrice(event.itemId)
                    effectChannel.send(HardwareListEffect.ShowToast("🏷️ Özel fiyat sıfırlandı."))
                }
            }
            HardwareListUiEvent.OnToggleFilterSheet -> {
                val next = !isFilterSheetVisible.value
                isFilterSheetVisible.value = next
                _uiState.update { it.copy(isFilterSheetVisible = next) }
            }
            is HardwareListUiEvent.OnFiltersChanged -> {
                filterOptions.value = event.filters
                _uiState.update { it.copy(filters = event.filters) }
            }
            HardwareListUiEvent.OnResetFilters -> {
                filterOptions.value = HardwareFilterOptions()
                _uiState.update { it.copy(filters = HardwareFilterOptions()) }
            }
            HardwareListUiEvent.OnRefresh -> {
                refresh(isManual = true)
            }
        }
    }

    private fun observeHardwareData() {
        viewModelScope.launch {
            combine(
                hardwareItemsFlow,
                debouncedSearchQuery,
                filterStateFlow,
                isRefreshing,
            ) { result, query, (filters, brands, sheetVisible), _ ->
                when (result) {
                    is DomainResult.Success -> {
                        val total = result.data.size
                        val filtered = filterItems(result.data, query, filters)
                        val sorted = sortItems(filtered, filters.sortOption, query)
                        Triple(true, sorted to total, Triple(query, filters, brands to sheetVisible))
                    }
                    is DomainResult.Failure -> {
                        Triple(false, emptyList<HardwareItem>() to 0, Triple(query, filters, brands to sheetVisible))
                    }
                }
            }
                .flowOn(Dispatchers.Default)
                .collect { (isSuccess, itemsAndTotal, meta) ->
                    val (items, total) = itemsAndTotal
                    val (query, filters, brandsAndSheet) = meta
                    val (brands, sheetVisible) = brandsAndSheet
                    if (isSuccess) {
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                items = items,
                                totalCount = total,
                                segmentMode = segmentMode.value,
                                selectedCategory = selectedCategory.value,
                                searchQuery = query,
                                filters = filters,
                                availableBrands = brands,
                                isFilterSheetVisible = sheetVisible,
                                errorMessage = null,
                            )
                        }
                    } else if (_uiState.value.items.isEmpty()) {
                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                items = emptyList(),
                                totalCount = 0,
                                segmentMode = segmentMode.value,
                                selectedCategory = selectedCategory.value,
                                searchQuery = query,
                                filters = filters,
                                availableBrands = brands,
                                isFilterSheetVisible = sheetVisible,
                                errorMessage = "Donanım parçaları yüklenemedi.",
                            )
                        }
                    }
                }
        }
    }

    private fun filterItems(
        items: List<HardwareItem>,
        query: String,
        filters: HardwareFilterOptions,
    ): List<HardwareItem> {
        return items.filter { item ->
            // 0. Brand Filter
            if (filters.selectedBrands.isNotEmpty()) {
                if (!filters.selectedBrands.contains(item.brand)) return@filter false
            }

            // 1. Search Query
            if (query.isNotBlank()) {
                if (!com.panda.myapplication.domain.engine.HardwareSearchEngine.matches(item, query)) {
                    return@filter false
                }
            }

            // 2. Category Filters
            when (val s = item.spec) {
                is CpuSpec -> {
                    if (filters.cpuSocket != null && !s.socket.equals(filters.cpuSocket, ignoreCase = true)) return@filter false
                    if (filters.minCoreCount != null && s.coreCount < filters.minCoreCount) return@filter false
                    if (filters.maxTdpWatts != null && s.tdp.watts > filters.maxTdpWatts) return@filter false
                }
                is GpuSpec -> {
                    if (filters.minVramGb != null && (s.vram.megabytes / 1024) < filters.minVramGb) return@filter false
                    if (filters.gpuBrand != null && !item.brand.contains(filters.gpuBrand, ignoreCase = true) && !s.chipset.contains(filters.gpuBrand, ignoreCase = true)) return@filter false
                    if (filters.maxGpuLengthMm != null && (s.lengthMm ?: 0) > filters.maxGpuLengthMm) return@filter false
                }
                is RamSpec -> {
                    if (filters.ramGeneration != null && s.generation != filters.ramGeneration) return@filter false
                    if (filters.minRamSpeedMts != null && s.speed.mhz < filters.minRamSpeedMts) return@filter false
                }
                is StorageSpec -> {
                    if (filters.storageMediaType != null && s.mediaType != filters.storageMediaType) return@filter false
                    if (filters.storageInterface != null && s.storageInterface != filters.storageInterface) return@filter false
                    if (filters.storagePcieGen != null && s.pcieGeneration != filters.storagePcieGen) return@filter false
                    if (filters.minStorageCapacityGb != null && (s.capacity.megabytes / 1024) < filters.minStorageCapacityGb) return@filter false
                }
                is MotherboardSpec -> {
                    if (filters.motherboardSocket != null && !s.socket.equals(filters.motherboardSocket, ignoreCase = true)) return@filter false
                    if (filters.motherboardFormFactor != null && s.formFactor != filters.motherboardFormFactor) return@filter false
                    if (filters.minM2Slots != null && (s.m2Slots ?: 0) < filters.minM2Slots) return@filter false
                }
                is PsuSpec -> {
                    if (filters.minPsuWattage != null && s.wattage.watts < filters.minPsuWattage) return@filter false
                    if (filters.psuEfficiency != null && s.efficiency != filters.psuEfficiency) return@filter false
                    if (filters.requireAtx3 && !s.isAtx3Compliant) return@filter false
                    if (filters.requirePcie5 && !s.supportsPcie5Power) return@filter false
                }
                is CoolerSpec, is CaseSpec, is PrebuiltSpec, is LaptopSpec,
                is MouseSpec, is KeyboardSpec, is HeadsetSpec, is MonitorSpec -> Unit
            }
            true
        }
    }

    private fun sortItems(
        items: List<HardwareItem>,
        sortOption: HardwareSortOption,
        query: String = "",
    ): List<HardwareItem> {
        return when (sortOption) {
            HardwareSortOption.DEFAULT -> {
                if (query.isNotBlank()) {
                    items.sortedByDescending { com.panda.myapplication.domain.engine.HardwareSearchEngine.calculateScore(it, query) }
                } else {
                    items.sortedWith(
                        compareBy<HardwareItem> { SecurityUtils.sanitizeTryPrice(it.currentPriceTry) ?: Double.MAX_VALUE }
                            .thenBy { it.displayName.lowercase() },
                    )
                }
            }
            HardwareSortOption.PRICE_ASC -> items.sortedWith(
                compareBy<HardwareItem> { SecurityUtils.sanitizeTryPrice(it.currentPriceTry) ?: Double.MAX_VALUE }
                    .thenBy { it.displayName.lowercase() },
            )
            HardwareSortOption.PRICE_DESC -> items.sortedByDescending { SecurityUtils.sanitizeTryPrice(it.currentPriceTry) ?: -1.0 }
            HardwareSortOption.NAME_ASC -> items.sortedBy { it.displayName }

            // CPU
            HardwareSortOption.CPU_CORES_DESC -> items.sortedByDescending { (it.spec as? CpuSpec)?.coreCount ?: 0 }
            HardwareSortOption.CPU_CLOCK_DESC -> items.sortedByDescending { (it.spec as? CpuSpec)?.boostClock?.mhz ?: (it.spec as? CpuSpec)?.baseClock?.mhz ?: 0 }
            HardwareSortOption.CPU_TDP_ASC -> items.sortedBy { (it.spec as? CpuSpec)?.tdp?.watts ?: Int.MAX_VALUE }
            HardwareSortOption.CPU_TDP_DESC -> items.sortedByDescending { (it.spec as? CpuSpec)?.tdp?.watts ?: 0 }

            // GPU
            HardwareSortOption.GPU_VRAM_DESC -> items.sortedByDescending { (it.spec as? GpuSpec)?.vram?.megabytes ?: 0 }
            HardwareSortOption.GPU_TDP_ASC -> items.sortedBy { (it.spec as? GpuSpec)?.tdp?.watts ?: Int.MAX_VALUE }
            HardwareSortOption.GPU_TDP_DESC -> items.sortedByDescending { (it.spec as? GpuSpec)?.tdp?.watts ?: 0 }

            // RAM
            HardwareSortOption.RAM_SPEED_DESC -> items.sortedByDescending { (it.spec as? RamSpec)?.speed?.mhz ?: 0 }
            HardwareSortOption.RAM_CL_ASC -> items.sortedBy { (it.spec as? RamSpec)?.casLatency ?: Int.MAX_VALUE }
            HardwareSortOption.RAM_CAPACITY_DESC -> items.sortedByDescending { (it.spec as? RamSpec)?.capacity?.megabytes ?: 0 }

            // Storage
            HardwareSortOption.STORAGE_CAPACITY_DESC -> items.sortedByDescending { (it.spec as? StorageSpec)?.capacity?.megabytes ?: 0 }
            HardwareSortOption.STORAGE_READ_SPEED_DESC -> items.sortedByDescending { (it.spec as? StorageSpec)?.sequentialReadMbps ?: 0 }

            // Motherboard
            HardwareSortOption.MOTHERBOARD_M2_SLOTS_DESC -> items.sortedByDescending { (it.spec as? MotherboardSpec)?.m2Slots ?: 0 }
            HardwareSortOption.MOTHERBOARD_RAM_SLOTS_DESC -> items.sortedByDescending { (it.spec as? MotherboardSpec)?.ramSlots ?: 0 }

            // PSU
            HardwareSortOption.PSU_WATTAGE_DESC -> items.sortedByDescending { (it.spec as? PsuSpec)?.wattage?.watts ?: 0 }
        }
    }

    private fun refresh(isManual: Boolean = false) {
        viewModelScope.launch {
            isRefreshing.value = true
            when (repository.refreshHardwareItems()) {
                is DomainResult.Success -> {
                    isRefreshing.value = false
                    errorMessage.value = null
                }
                is DomainResult.Failure -> {
                    isRefreshing.value = false
                    if (isManual) {
                        errorMessage.value = "Veri senkronizasyonu başarısız oldu."
                        effectChannel.send(HardwareListEffect.ShowToast("Veri senkronizasyonu başarısız oldu."))
                    }
                }
            }
        }
    }

    companion object {
        val INDIVIDUAL_CATEGORIES = setOf(
            HardwareCategory.CPU,
            HardwareCategory.GPU,
            HardwareCategory.RAM,
            HardwareCategory.MOTHERBOARD,
            HardwareCategory.STORAGE,
            HardwareCategory.PSU,
            HardwareCategory.COOLER,
            HardwareCategory.CASE,
        )

        val PERIPHERAL_CATEGORIES = setOf(
            HardwareCategory.MOUSE,
            HardwareCategory.KEYBOARD,
            HardwareCategory.HEADSET,
            HardwareCategory.MONITOR,
        )
    }
}
