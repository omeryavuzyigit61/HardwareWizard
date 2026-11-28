package com.panda.myapplication.presentation.hardware_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.AccessPointSpec
import com.panda.myapplication.domain.model.CaseFanSpec
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GamingChairSpec
import com.panda.myapplication.domain.model.GamingDeskSpec
import com.panda.myapplication.domain.model.GamingLightingSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MicrophoneSpec
import com.panda.myapplication.domain.model.ModemSpec
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.MousepadSpec
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.RangeExtenderSpec
import com.panda.myapplication.domain.model.RouterSpec
import com.panda.myapplication.domain.model.SmartwatchSpec
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.TabletSpec
import com.panda.myapplication.domain.model.UpsSpec
import com.panda.myapplication.domain.model.WebcamSpec
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
    private val pageSize = 50
    private val displayedLimit = MutableStateFlow(pageSize)

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
                CatalogSegmentMode.NETWORKING -> NETWORKING_CATEGORIES.toList()
                CatalogSegmentMode.LAPTOP -> listOf(HardwareCategory.LAPTOP)
                CatalogSegmentMode.MOBILE -> MOBILE_CATEGORIES.toList()
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
        displayedLimit.value = pageSize
    }

    fun onEvent(event: HardwareListUiEvent) {
        when (event) {
            is HardwareListUiEvent.OnSegmentModeSelected -> {
                segmentMode.value = event.mode
                val nextCategory = when (event.mode) {
                    CatalogSegmentMode.INDIVIDUAL -> null
                    CatalogSegmentMode.PERIPHERALS -> null
                    CatalogSegmentMode.NETWORKING -> null
                    CatalogSegmentMode.LAPTOP -> HardwareCategory.LAPTOP
                    CatalogSegmentMode.MOBILE -> null
                }
                selectedCategory.value = nextCategory
                displayedLimit.value = pageSize
                filterOptions.value = HardwareFilterOptions()
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
                        when (current) {
                            CatalogSegmentMode.PERIPHERALS -> CatalogSegmentMode.PERIPHERALS
                            CatalogSegmentMode.NETWORKING -> CatalogSegmentMode.NETWORKING
                            CatalogSegmentMode.MOBILE -> CatalogSegmentMode.MOBILE
                            else -> CatalogSegmentMode.INDIVIDUAL
                        }
                    }
                    targetCategory.isNetworking -> CatalogSegmentMode.NETWORKING
                    targetCategory.isMobile -> CatalogSegmentMode.MOBILE
                    targetCategory.isPeripheral -> CatalogSegmentMode.PERIPHERALS
                    targetCategory == HardwareCategory.LAPTOP -> CatalogSegmentMode.LAPTOP
                    else -> CatalogSegmentMode.INDIVIDUAL
                }
                segmentMode.value = inferredMode
                selectedCategory.value = targetCategory
                displayedLimit.value = pageSize
                val validSorts = HardwareSortOption.optionsForCategory(targetCategory)
                val nextSort = if (validSorts.contains(filterOptions.value.sortOption)) {
                    filterOptions.value.sortOption
                } else {
                    HardwareSortOption.DEFAULT
                }
                // Reset category-specific filters cleanly so stale CPU/GPU filters never linger on other categories
                filterOptions.value = HardwareFilterOptions(sortOption = nextSort)
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
                val feedback = vsArenaRepository.addQuickItemBilingual(event.item)
                viewModelScope.launch {
                    effectChannel.send(HardwareListEffect.ShowVsAddedToast(feedback.messageTr, feedback.messageEn))
                }
            }
            is HardwareListUiEvent.OnSaveCustomPrice -> {
                viewModelScope.launch {
                    repository.setCustomPrice(event.itemId, event.customPriceTry, event.note)
                    effectChannel.send(HardwareListEffect.ShowToast("🏷️ Özel fiyat kaydedildi.", "🏷️ Custom price saved."))
                }
            }
            is HardwareListUiEvent.OnClearCustomPrice -> {
                viewModelScope.launch {
                    repository.clearCustomPrice(event.itemId)
                    effectChannel.send(HardwareListEffect.ShowToast("🏷️ Özel fiyat sıfırlandı.", "🏷️ Custom price cleared."))
                }
            }
            HardwareListUiEvent.OnToggleFilterSheet -> {
                val next = !isFilterSheetVisible.value
                isFilterSheetVisible.value = next
                _uiState.update { it.copy(isFilterSheetVisible = next) }
            }
            is HardwareListUiEvent.OnFiltersChanged -> {
                displayedLimit.value = pageSize
                filterOptions.value = event.filters
                _uiState.update { it.copy(filters = event.filters) }
            }
            HardwareListUiEvent.OnResetFilters -> {
                displayedLimit.value = pageSize
                filterOptions.value = HardwareFilterOptions()
                _uiState.update { it.copy(filters = HardwareFilterOptions()) }
            }
            HardwareListUiEvent.OnRefresh -> {
                refresh(isManual = true)
            }
            HardwareListUiEvent.OnLoadMore -> {
                displayedLimit.update { it + pageSize }
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
                displayedLimit,
            ) { result, query, (filters, brands, sheetVisible), _, limit ->
                when (result) {
                    is DomainResult.Success -> {
                        val filtered = filterItems(result.data, query, filters)
                        val sorted = sortItems(filtered, filters.sortOption, query)
                        val windowed = sorted.take(limit)
                        Triple(true, windowed to sorted.size, Triple(query, filters, brands to sheetVisible))
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
                                errorMessageEn = "Could not load hardware components.",
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
                    if (filters.cpuSocket != null && !com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase.socketsMatch(s.socket, filters.cpuSocket)) return@filter false
                    if (filters.minCoreCount != null && s.coreCount < filters.minCoreCount) return@filter false
                    if (filters.maxTdpWatts != null && s.tdp.watts > filters.maxTdpWatts) return@filter false
                }
                is GpuSpec -> {
                    if (filters.minVramGb != null && (s.vram.megabytes / 1024) < filters.minVramGb) return@filter false
                    if (filters.gpuBrand != null && !item.brand.contains(filters.gpuBrand, ignoreCase = true) && !s.chipset.contains(filters.gpuBrand, ignoreCase = true)) return@filter false
                    if (filters.maxGpuLengthMm != null && (s.lengthMm == null || s.lengthMm <= 0 || s.lengthMm > filters.maxGpuLengthMm)) return@filter false
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
                    if (filters.motherboardSocket != null && !com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase.socketsMatch(s.socket, filters.motherboardSocket)) return@filter false
                    if (filters.motherboardFormFactor != null && s.formFactor != filters.motherboardFormFactor) return@filter false
                    if (filters.minM2Slots != null && (s.m2Slots ?: 0) < filters.minM2Slots) return@filter false
                }
                is PsuSpec -> {
                    if (filters.minPsuWattage != null && s.wattage.watts < filters.minPsuWattage) return@filter false
                    if (filters.psuEfficiency != null && s.efficiency != filters.psuEfficiency) return@filter false
                    if (filters.requireAtx3 && !s.isAtx3Compliant) return@filter false
                    if (filters.requirePcie5 && !s.supportsPcie5Power) return@filter false
                }
                is CoolerSpec -> {
                    if (filters.coolerTypeFilter != null && s.type != filters.coolerTypeFilter) return@filter false
                }
                is MonitorSpec -> {
                    if (filters.minMonitorRefreshHz != null && s.refreshRateHz < filters.minMonitorRefreshHz) return@filter false
                    if (filters.monitorPanelFilter != null && !s.panelType.contains(filters.monitorPanelFilter, ignoreCase = true)) return@filter false
                }
                is MouseSpec -> {
                    if (filters.maxMouseWeightG != null && (s.weightG <= 0 || s.weightG > filters.maxMouseWeightG)) return@filter false
                }
                is KeyboardSpec -> {
                    if (filters.requireHallEffectKeyboard && !s.isHallEffect && !s.rapidTriggerSupported) return@filter false
                }
                is UpsSpec -> {
                    if (filters.requirePureSineUps && !s.sinewaveType.contains("Tam Sinüs", ignoreCase = true) && !s.sinewaveType.contains("Pure Sine", ignoreCase = true)) return@filter false
                }
                is RouterSpec -> {
                    if (filters.wifiStandardFilter != null && !s.wifiStandard.contains(filters.wifiStandardFilter, ignoreCase = true)) return@filter false
                }
                is ModemSpec -> {
                    if (filters.wifiStandardFilter != null && !s.wifiStandard.contains(filters.wifiStandardFilter, ignoreCase = true)) return@filter false
                }
                is AccessPointSpec -> {
                    if (filters.wifiStandardFilter != null && !s.wifiStandard.contains(filters.wifiStandardFilter, ignoreCase = true)) return@filter false
                }
                is RangeExtenderSpec -> {
                    if (filters.wifiStandardFilter != null && !s.wifiStandard.contains(filters.wifiStandardFilter, ignoreCase = true)) return@filter false
                }
                is PhoneSpec -> {
                    if (filters.minMobileBatteryMah != null && s.batteryMah < filters.minMobileBatteryMah) return@filter false
                }
                is TabletSpec -> {
                    if (filters.minMobileBatteryMah != null && s.batteryMah < filters.minMobileBatteryMah) return@filter false
                }
                else -> Unit
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
                } else if (items.any { it.category.isMobile }) {
                    items.sortedWith(
                        compareByDescending<HardwareItem> { (it.spec as? PhoneSpec)?.antutuScore ?: (it.spec as? TabletSpec)?.antutuScore ?: it.benchmarkScore ?: 0 }
                            .thenBy { it.displayName.lowercase() }
                    )
                } else if (com.panda.myapplication.domain.config.FeatureFlags.IS_PRICING_ENABLED) {
                    items.sortedWith(
                        compareBy<HardwareItem> { SecurityUtils.sanitizeTryPrice(it.currentPriceTry) ?: Double.MAX_VALUE }
                            .thenBy { it.displayName.lowercase() },
                    )
                } else {
                    items.sortedWith(
                        compareByDescending<HardwareItem> { com.panda.myapplication.domain.engine.VsArenaEngine.computeCompositePerformanceScore(it) }
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

            // Mobil, Tablet, Laptop & Smartwatch Batarya
            HardwareSortOption.ANTUTU_SCORE_DESC -> items.sortedByDescending { (it.spec as? PhoneSpec)?.antutuScore ?: (it.spec as? TabletSpec)?.antutuScore ?: 0 }
            HardwareSortOption.BATTERY_DESC -> items.sortedByDescending {
                when (val s = it.spec) {
                    is PhoneSpec -> s.batteryMah
                    is TabletSpec -> s.batteryMah
                    is LaptopSpec -> s.batteryWh
                    is SmartwatchSpec -> s.batteryLifeDays
                    else -> 0
                }
            }

            // CPU
            HardwareSortOption.CPU_GAMING_PERF_DESC -> items.sortedByDescending {
                it.benchmarkInfo?.gamingIndex ?: it.benchmarkScore ?: 0
            }
            HardwareSortOption.CPU_RENDER_PERF_DESC -> items.sortedByDescending {
                it.benchmarkInfo?.effectiveMultiCore ?: it.benchmarkScore ?: 0
            }
            HardwareSortOption.CPU_CORES_DESC -> items.sortedByDescending { (it.spec as? CpuSpec)?.coreCount ?: 0 }
            HardwareSortOption.CPU_CLOCK_DESC -> items.sortedByDescending { (it.spec as? CpuSpec)?.boostClock?.mhz ?: (it.spec as? CpuSpec)?.baseClock?.mhz ?: 0 }
            HardwareSortOption.CPU_TDP_ASC -> items.sortedBy { (it.spec as? CpuSpec)?.tdp?.watts ?: Int.MAX_VALUE }
            HardwareSortOption.CPU_TDP_DESC -> items.sortedByDescending { (it.spec as? CpuSpec)?.tdp?.watts ?: 0 }

            // GPU
            HardwareSortOption.GPU_PERF_DESC -> items.sortedByDescending {
                it.benchmarkInfo?.gamingIndex ?: it.benchmarkScore ?: 0
            }
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

            // Cooling, Cases & Fans
            HardwareSortOption.COOLER_TDP_DESC -> items.sortedByDescending { (it.spec as? CoolerSpec)?.maxCoolingTdp?.watts ?: 0 }
            HardwareSortOption.COOLER_NOISE_ASC -> items.sortedBy {
                (it.spec as? CoolerSpec)?.noiseDba
                    ?: (it.spec as? CaseFanSpec)?.noiseLevelDba?.takeIf { d -> d > 0 }
                    ?: Double.MAX_VALUE
            }
            HardwareSortOption.CASE_MAX_GPU_DESC -> items.sortedByDescending { (it.spec as? CaseSpec)?.maxGpuLengthMm ?: 0 }
            HardwareSortOption.CASE_FAN_AIRFLOW_DESC -> items.sortedByDescending { (it.spec as? CaseFanSpec)?.maxAirflowCfm ?: 0.0 }

            // Laptop & Monitor
            HardwareSortOption.LAPTOP_GAMING_DESC -> items.sortedByDescending { (it.spec as? LaptopSpec)?.gamingScore ?: 0 }
            HardwareSortOption.MONITOR_REFRESH_DESC -> items.sortedByDescending { (it.spec as? MonitorSpec)?.refreshRateHz ?: 0 }
            HardwareSortOption.MONITOR_RESPONSE_ASC -> items.sortedBy {
                (it.spec as? MonitorSpec)?.responseSpeedGtGMs?.takeIf { v -> v > 0 } ?: Double.MAX_VALUE
            }

            // Peripherals & Furniture
            HardwareSortOption.MOUSE_WEIGHT_ASC -> items.sortedBy {
                (it.spec as? MouseSpec)?.weightG?.takeIf { w -> w > 0 } ?: Int.MAX_VALUE
            }
            HardwareSortOption.MOUSE_DPI_DESC -> items.sortedByDescending { (it.spec as? MouseSpec)?.maxDpi ?: 0 }
            HardwareSortOption.KEYBOARD_POLLING_DESC -> items.sortedByDescending {
                (it.spec as? KeyboardSpec)?.pollingRateHz ?: (it.spec as? MouseSpec)?.pollingRateHz ?: 0
            }
            HardwareSortOption.HEADSET_WEIGHT_ASC -> items.sortedBy {
                (it.spec as? HeadsetSpec)?.weightG?.takeIf { w -> w > 0 } ?: Int.MAX_VALUE
            }
            HardwareSortOption.MIC_SPL_DESC -> items.sortedByDescending { (it.spec as? MicrophoneSpec)?.maxSplDb ?: 0 }
            HardwareSortOption.MOUSEPAD_THICKNESS_DESC -> items.sortedByDescending { (it.spec as? MousepadSpec)?.thicknessMm ?: 0.0 }
            HardwareSortOption.WEBCAM_FPS_DESC -> items.sortedByDescending { (it.spec as? WebcamSpec)?.maxFrameRateFps ?: 0 }
            HardwareSortOption.CHAIR_MAX_WEIGHT_DESC -> items.sortedByDescending { (it.spec as? GamingChairSpec)?.maxWeightCapacityKg ?: 0 }
            HardwareSortOption.DESK_WIDTH_DESC -> items.sortedByDescending { (it.spec as? GamingDeskSpec)?.deskWidthCm ?: 0 }
            HardwareSortOption.LIGHTING_LUMENS_DESC -> items.sortedByDescending { (it.spec as? GamingLightingSpec)?.maxLumens ?: 0 }

            // UPS & Networking
            HardwareSortOption.UPS_WATT_DESC -> items.sortedByDescending { (it.spec as? UpsSpec)?.wattCapacity ?: 0 }
            HardwareSortOption.NET_COVERAGE_DESC -> items.sortedByDescending {
                when (val s = it.spec) {
                    is AccessPointSpec -> s.maxClients
                    is RangeExtenderSpec -> s.coverageAreaM2
                    is RouterSpec -> wifiGenRank(s.wifiStandard) * 100_000 + extractMbps(s.speedClass)
                    is ModemSpec -> wifiGenRank(s.wifiStandard) * 100_000 + (if (s.fiberWanSupport) 10_000 else 0) + (if (s.vdslProfile.contains("35b", ignoreCase = true)) 1_000 else 0)
                    else -> 0
                }
            }
        }
    }

    private fun wifiGenRank(std: String): Int = when {
        std.contains("Wi-Fi 7", ignoreCase = true) || std.contains("802.11be", ignoreCase = true) -> 5
        std.contains("Wi-Fi 6E", ignoreCase = true) -> 4
        std.contains("Wi-Fi 6", ignoreCase = true) || std.contains("802.11ax", ignoreCase = true) -> 3
        std.contains("Wi-Fi 5", ignoreCase = true) || std.contains("802.11ac", ignoreCase = true) -> 2
        std.isNotBlank() -> 1
        else -> 0
    }

    private fun extractMbps(speedClass: String): Int =
        Regex("""(\d{3,5})""").find(speedClass)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0

    private fun refresh(isManual: Boolean = false) {
        viewModelScope.launch {
            if (isManual) {
                isRefreshing.value = true
            }
            when (repository.refreshHardwareItems(force = isManual)) {
                is DomainResult.Success -> {
                    isRefreshing.value = false
                    errorMessage.value = null
                }
                is DomainResult.Failure -> {
                    isRefreshing.value = false
                    if (isManual) {
                        errorMessage.value = "Veri senkronizasyonu başarısız oldu."
                        effectChannel.send(HardwareListEffect.ShowToast("Veri senkronizasyonu başarısız oldu.", "Data synchronization failed."))
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
            HardwareCategory.CASE_FAN,
            HardwareCategory.UPS,
        )

        val PERIPHERAL_CATEGORIES = setOf(
            HardwareCategory.MOUSE,
            HardwareCategory.KEYBOARD,
            HardwareCategory.HEADSET,
            HardwareCategory.MONITOR,
            HardwareCategory.MICROPHONE,
            HardwareCategory.MOUSEPAD,
            HardwareCategory.WEBCAM,
            HardwareCategory.GAMING_CHAIR,
            HardwareCategory.GAMING_DESK,
            HardwareCategory.GAMING_LIGHTING,
        )

        val NETWORKING_CATEGORIES = setOf(
            HardwareCategory.ROUTER,
            HardwareCategory.MODEM,
            HardwareCategory.ACCESS_POINT,
            HardwareCategory.RANGE_EXTENDER,
        )

        val MOBILE_CATEGORIES = setOf(
            HardwareCategory.SMARTPHONE,
            HardwareCategory.TABLET,
            HardwareCategory.SMARTWATCH,
        )
    }
}
