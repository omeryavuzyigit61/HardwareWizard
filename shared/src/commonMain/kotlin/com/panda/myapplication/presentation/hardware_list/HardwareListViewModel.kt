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
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.repository.HardwareRepository
import com.panda.myapplication.domain.usecase.GetHardwareListUseCase
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class HardwareListViewModel(
    private val getHardwareListUseCase: GetHardwareListUseCase,
    private val repository: HardwareRepository,
) : ViewModel() {

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
        .debounce { query -> if (query.isBlank()) 0L else 300L }
        .distinctUntilChanged()

    private val hardwareItemsFlow: Flow<DomainResult<List<HardwareItem>>> = selectedCategory.flatMapLatest { category ->
        getHardwareListUseCase(category)
    }

    private val availableBrandsFlow: Flow<List<String>> = selectedCategory.flatMapLatest { category ->
        repository.observeBrands(category).map { result ->
            when (result) {
                is DomainResult.Success -> result.data
                is DomainResult.Failure -> emptyList()
            }
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
        _uiState.update { it.copy(searchQuery = newQuery) }
        rawSearchQuery.value = newQuery
    }

    fun onEvent(event: HardwareListUiEvent) {
        when (event) {
            is HardwareListUiEvent.OnCategorySelected -> {
                selectedCategory.value = event.category
                val validSorts = HardwareSortOption.optionsForCategory(event.category)
                if (!validSorts.contains(filterOptions.value.sortOption)) {
                    filterOptions.value = filterOptions.value.copy(sortOption = HardwareSortOption.DEFAULT)
                }
                _uiState.update { it.copy(selectedCategory = event.category) }
            }
            is HardwareListUiEvent.OnSearchQueryChanged -> {
                onSearchQueryChange(event.query)
            }
            is HardwareListUiEvent.OnHardwareItemClicked -> {
                viewModelScope.launch {
                    effectChannel.send(HardwareListEffect.NavigateToDetail(event.item))
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
            ) { result, query, (filters, brands, sheetVisible), refreshing ->
                when (result) {
                    is DomainResult.Success -> {
                        val total = result.data.size
                        val filtered = filterItems(result.data, query, filters)
                        val sorted = sortItems(filtered, filters.sortOption)

                        _uiState.update { current ->
                            current.copy(
                                isLoading = false,
                                items = sorted,
                                totalCount = total,
                                selectedCategory = selectedCategory.value,
                                filters = filters,
                                availableBrands = brands,
                                isFilterSheetVisible = sheetVisible,
                                errorMessage = null,
                            )
                        }
                    }
                    is DomainResult.Failure -> {
                        _uiState.update { current ->
                            current.copy(
                                isLoading = refreshing,
                                items = emptyList(),
                                totalCount = 0,
                                selectedCategory = selectedCategory.value,
                                filters = filters,
                                availableBrands = brands,
                                isFilterSheetVisible = sheetVisible,
                                errorMessage = errorMessage.value ?: "Donanım parçaları yüklenemedi.",
                            )
                        }
                    }
                }
            }.collect()
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
                val q = query.trim().lowercase()
                val matches = item.displayName.lowercase().contains(q) ||
                        item.brand.lowercase().contains(q) ||
                        item.model.lowercase().contains(q) ||
                        (item.spec as? GpuSpec)?.chipset?.lowercase()?.contains(q) == true ||
                        (item.spec as? GpuSpec)?.partNumber?.lowercase()?.contains(q) == true
                if (!matches) return@filter false
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
                    if (filters.maxGpuLengthMm != null && s.lengthMm > filters.maxGpuLengthMm) return@filter false
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
                    if (filters.minM2Slots != null && s.m2Slots < filters.minM2Slots) return@filter false
                }
                is PsuSpec -> {
                    if (filters.minPsuWattage != null && s.wattage.watts < filters.minPsuWattage) return@filter false
                    if (filters.psuEfficiency != null && s.efficiency != filters.psuEfficiency) return@filter false
                    if (filters.requireAtx3 && !s.isAtx3Compliant) return@filter false
                    if (filters.requirePcie5 && !s.supportsPcie5Power) return@filter false
                }
                is CoolerSpec, is CaseSpec -> Unit
            }
            true
        }
    }

    private fun sortItems(
        items: List<HardwareItem>,
        sortOption: HardwareSortOption,
    ): List<HardwareItem> {
        return when (sortOption) {
            HardwareSortOption.DEFAULT -> items
            HardwareSortOption.PRICE_ASC -> items.sortedBy { it.price?.amountMinor ?: Long.MAX_VALUE }
            HardwareSortOption.PRICE_DESC -> items.sortedByDescending { it.price?.amountMinor ?: 0L }
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
}
