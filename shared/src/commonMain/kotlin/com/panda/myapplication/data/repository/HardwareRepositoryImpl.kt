package com.panda.myapplication.data.repository

import com.panda.myapplication.data.local.HardwareLocalDataSource
import com.panda.myapplication.data.local.dao.HardwareDao
import com.panda.myapplication.data.local.entity.UserPriceOverrideEntity
import com.panda.myapplication.data.mapper.HardwareMapper
import com.panda.myapplication.data.mapper.SupabaseCatalogMapper
import com.panda.myapplication.data.remote.HardwareRemoteDataSource
import com.panda.myapplication.domain.common.DomainError
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.repository.HardwareRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class HardwareRepositoryImpl(
    private val hardwareDao: HardwareDao,
    private val localDataSource: HardwareLocalDataSource,
    private val remoteDataSource: HardwareRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : HardwareRepository {

    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    private val seedMutex = Mutex()
    private var isSeeded = false
    private val memoryCache = MutableStateFlow<List<HardwareItem>?>(null)

    private suspend fun ensureSeeded() {
        if (isSeeded) return
        seedMutex.withLock {
            if (isSeeded) return
            // Load lightweight bundled verified lab benchmarks (40 KB)
            runCatching {
                val verified = localDataSource.loadLocalVerifiedBenchmarks()
                if (verified.isNotEmpty()) {
                    com.panda.myapplication.domain.model.VerifiedLabBenchmarks.updateRemoteTests(verified)
                }
            }

            // Fast path: Pre-populated Room DB already contains the full offline catalog (0ms startup)
            val existingCount = hardwareDao.getTotalItemCount()
            if (existingCount > 0) {
                if (hardwareDao.getSetting(SETTING_CATALOG_VERSION) != CURRENT_CATALOG_VERSION) {
                    hardwareDao.saveSetting(
                        com.panda.myapplication.data.local.entity.AppSettingEntity(
                            key = SETTING_CATALOG_VERSION,
                            value = CURRENT_CATALOG_VERSION,
                        )
                    )
                }
                if (hardwareDao.getSetting(SETTING_CATALOG_REVISION) == null) {
                    hardwareDao.saveSetting(
                        com.panda.myapplication.data.local.entity.AppSettingEntity(
                            key = SETTING_CATALOG_REVISION,
                            value = INITIAL_BUNDLED_REVISION.toString(),
                        )
                    )
                }
                isSeeded = true
                return
            }

            // Empty Room fallback (e.g., fresh iOS install without bundled DB): bootstrap once from Supabase
            val bootstrapped = syncDeltaFromSupabase(forceBootstrap = true)
            if (!bootstrapped) {
                // Legacy fallback if bundled JSON assets are ever present in a test/custom build
                localDataSource.seedDatabaseIfEmpty()
            }
            if (hardwareDao.getTotalItemCount() > 0) {
                hardwareDao.saveSetting(
                    com.panda.myapplication.data.local.entity.AppSettingEntity(
                        key = SETTING_CATALOG_VERSION,
                        value = CURRENT_CATALOG_VERSION,
                    )
                )
            }
            isSeeded = true
        }
    }

    private fun HardwareItem.applyOverride(overrideMap: Map<String, UserPriceOverrideEntity>): HardwareItem {
        val ov = overrideMap[id.value] ?: return this
        return copy(
            customPriceTry = ov.customPriceTry,
            customPriceNote = ov.note,
        )
    }

    override fun observeHardwareItems(): Flow<DomainResult<List<HardwareItem>>> = flow {
        // 1. Emit in-memory cache instantly if available (0ms tab switch)
        memoryCache.value?.let { cached ->
            if (cached.isNotEmpty()) {
                emit(DomainResult.Success(cached))
            }
        }
        // 2. Ensure database is fully seeded before querying Room
        ensureSeeded()
        // 3. Observe all categories by combining their robust, direct spec streams
        val allFlow = combine(
            HardwareCategory.entries.map { cat -> observeHardwareItemsByCategory(cat) }
        ) { categoryResults ->
            val allItems = mutableListOf<HardwareItem>()
            for (res in categoryResults) {
                if (res is DomainResult.Success) {
                    allItems.addAll(res.data)
                }
            }
            memoryCache.value = allItems
            DomainResult.Success(allItems) as DomainResult<List<HardwareItem>>
        }.catch {
            val fallback = memoryCache.value ?: emptyList()
            emit(DomainResult.Success(fallback))
        }
        emitAll(allFlow)
    }.flowOn(ioDispatcher)

    override fun observeHardwareItemsByCategory(category: HardwareCategory): Flow<DomainResult<List<HardwareItem>>> = flow {
        // 1. Emit in-memory cache instantly if available (0ms tab switch)
        memoryCache.value?.let { cached ->
            val filtered = cached.filter { it.category == category }
            if (filtered.isNotEmpty()) {
                emit(DomainResult.Success(filtered))
            }
        }
        // 2. Ensure database is fully seeded before querying Room
        ensureSeeded()
        // 3. Observe Room updates without multi-table @Relation to prevent SQLite 999 bind parameter limits
        val domainItemsFlow: Flow<List<HardwareItem>> = when (category) {
            HardwareCategory.CPU -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllCpuSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            cpu = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.GPU -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllGpuSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            gpu = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.RAM -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllRamSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            ram = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.MOTHERBOARD -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllMotherboardSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            motherboard = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.PSU -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllPsuSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            psu = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.STORAGE -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllStorageSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            storage = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.COOLER -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllCoolerSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            cooler = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.CASE -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllCaseSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            case = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.LAPTOP -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllLaptopSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            laptop = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.MOUSE -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllMouseSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            mouse = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.KEYBOARD -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllKeyboardSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            keyboard = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.HEADSET -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllHeadsetSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            headset = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.MONITOR -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllMonitorSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            monitor = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.MICROPHONE -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllMicrophoneSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            microphone = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.MOUSEPAD -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllMousepadSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            mousepad = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.WEBCAM -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllWebcamSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            webcam = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.GAMING_CHAIR -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllGamingChairSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            gamingChair = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.CASE_FAN -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllCaseFanSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            caseFan = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.UPS -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllUpsSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            ups = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.GAMING_DESK -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllGamingDeskSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            gamingDesk = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.GAMING_LIGHTING -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllGamingLightingSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            gamingLighting = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.SMARTWATCH -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllSmartwatchSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            smartwatch = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.ROUTER -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllRouterSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            router = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.MODEM -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllModemSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            modem = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.ACCESS_POINT -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllAccessPointSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            accessPoint = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.RANGE_EXTENDER -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllRangeExtenderSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.hardwareId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            rangeExtender = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.SMARTPHONE -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllPhoneSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            phone = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
            HardwareCategory.TABLET -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllTabletSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            tablet = specMap[item.id],
                        )
                    )?.applyOverride(overrideMap)
                }
            }
        }
        emitAll(
            domainItemsFlow
                .map { items ->
                    val currentMem = memoryCache.value.orEmpty()
                    val otherCats = currentMem.filter { it.category != category }
                    memoryCache.value = otherCats + items
                    DomainResult.Success(items) as DomainResult<List<HardwareItem>>
                }
                .catch {
                    val fallback = memoryCache.value?.filter { it.category == category } ?: emptyList()
                    emit(DomainResult.Success(fallback))
                }
        )
    }.flowOn(ioDispatcher)

    override fun observeHardwareItem(id: HardwareId): Flow<DomainResult<HardwareItem>> = flow {
        // 1. Fast path: Emit instantly from memory cache if already loaded (0ms latency)
        val cachedInstant = memoryCache.value?.find { it.id == id }
        if (cachedInstant != null) {
            emit(DomainResult.Success(cachedInstant))
        }



        ensureSeeded()

        // 3. Query base item to determine exact category
        val entity = hardwareDao.getItemEntityById(id.value)
        if (entity == null) {
            val fallback = memoryCache.value?.find { it.id == id }
            if (fallback != null) {
                emit(DomainResult.Success(fallback))
            } else {
                emit(DomainResult.Failure(DomainError.NotFound))
            }
            return@flow
        }

        // 4. Delegate to category flow: loads exact spec for that category and emits the matching item
        emitAll(
            observeHardwareItemsByCategory(entity.category).map { res ->
                when (res) {
                    is DomainResult.Success -> {
                        val item = res.data.find { it.id.value == id.value }
                        if (item != null) {
                            DomainResult.Success(item)
                        } else {
                            DomainResult.Failure(DomainError.NotFound)
                        }
                    }
                    is DomainResult.Failure -> res
                }
            }
        )
    }.flowOn(ioDispatcher)

    override fun observeBrands(category: HardwareCategory?): Flow<DomainResult<List<String>>> = flow {
        ensureSeeded()

        val daoFlow = if (category == null) {
            hardwareDao.observeAllBrands()
        } else {
            hardwareDao.observeBrandsByCategory(category)
        }
        emitAll(
            daoFlow.map { brands ->
                DomainResult.Success(brands.filter { it.isNotBlank() }) as DomainResult<List<String>>
            }.catch {
                emit(DomainResult.Success(emptyList()))
            }
        )
    }.flowOn(ioDispatcher)

    override suspend fun setCustomPrice(
        id: HardwareId,
        customPriceTry: Double,
        note: String?,
    ): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            val sanitized = customPriceTry.coerceIn(1.0, 9_999_999.0)
            hardwareDao.upsertPriceOverride(
                UserPriceOverrideEntity(
                    hardwareId = id.value,
                    customPriceTry = sanitized,
                    note = note?.trim()?.takeIf { it.isNotBlank() },
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                ),
            )
            DomainResult.Success(Unit)
        }.getOrElse {
            DomainResult.Failure(DomainError.Storage)
        }
    }

    override suspend fun clearCustomPrice(id: HardwareId): DomainResult<Unit> = withContext(ioDispatcher) {
        runCatching {
            hardwareDao.deletePriceOverride(id.value)
            DomainResult.Success(Unit)
        }.getOrElse {
            DomainResult.Failure(DomainError.Storage)
        }
    }

    override suspend fun refreshHardwareItems(): DomainResult<Unit> = withContext(ioDispatcher) {
        ensureSeeded()
        val now = Clock.System.now()
        val localCount = hardwareDao.getTotalItemCount()
        val lastSyncMillis = hardwareDao.getSetting(SETTING_LAST_REMOTE_SYNC_AT)?.toLongOrNull()
        if (localCount > 0 && lastSyncMillis != null && (now.toEpochMilliseconds() - lastSyncMillis) < DELTA_SYNC_INTERVAL_MS) {
            return@withContext DomainResult.Success(Unit)
        }
        try {
            // Optional lightweight verified lab benchmarks refresh (40 KB)
            runCatching {
                val remoteVerified = remoteDataSource.fetchVerifiedLabBenchmarks().getOrDefault(emptyList())
                if (remoteVerified.isNotEmpty()) {
                    com.panda.myapplication.domain.model.VerifiedLabBenchmarks.updateRemoteTests(remoteVerified)
                }
            }

            // Authoritative Supabase Delta Sync: checks 200-byte catalog_meta first,
            // then pulls & upserts ONLY newly added rows (catalog_revision > local_revision).
            syncDeltaFromSupabase(forceBootstrap = localCount == 0)

            hardwareDao.saveSetting(
                com.panda.myapplication.data.local.entity.AppSettingEntity(
                    key = SETTING_LAST_REMOTE_SYNC_AT,
                    value = now.toEpochMilliseconds().toString(),
                )
            )
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            // Offline-first guarantee: never break the app when offline if Room already has items
            if (localCount > 0) DomainResult.Success(Unit) else DomainResult.Failure(DomainError.Network)
        }
    }

    /**
     * Checks Supabase `catalog_meta` (~120 bytes). Only downloads rows when `latest_revision > local_revision`
     * (or when Room is empty), then merges them incrementally into Room via `upsertDeltaCatalog`.
     */
    private suspend fun syncDeltaFromSupabase(forceBootstrap: Boolean): Boolean {
        val localCount = hardwareDao.getTotalItemCount()
        val localRevision = if (forceBootstrap || localCount == 0) {
            0L
        } else {
            hardwareDao.getSetting(SETTING_CATALOG_REVISION)?.toLongOrNull() ?: INITIAL_BUNDLED_REVISION
        }

        val meta = remoteDataSource.fetchSupabaseCatalogMeta().getOrNull() ?: return false
        if (!shouldFetchDelta(meta.latestRevision, localRevision, localCount)) {
            return true
        }

        val deltaRows = remoteDataSource.fetchSupabaseCatalogDelta(afterRevision = localRevision).getOrNull()
            ?: return false
        if (deltaRows.isEmpty()) {
            if (meta.latestRevision > localRevision) {
                hardwareDao.saveSetting(
                    com.panda.myapplication.data.local.entity.AppSettingEntity(
                        key = SETTING_CATALOG_REVISION,
                        value = meta.latestRevision.toString(),
                    )
                )
            }
            return true
        }

        val syncedAt = Clock.System.now()
        val snapshot = SupabaseCatalogMapper.toCatalogSnapshot(deltaRows, syncedAt)
        if (snapshot.items.isNotEmpty()) {
            hardwareDao.upsertDeltaCatalog(snapshot)
            memoryCache.value = null // Trigger clean emission of newly merged items from Room Flows
        }

        val appliedRevision = maxOf(
            meta.latestRevision,
            deltaRows.maxOfOrNull { it.catalogRevision } ?: localRevision,
        )
        hardwareDao.saveSetting(
            com.panda.myapplication.data.local.entity.AppSettingEntity(
                key = SETTING_CATALOG_REVISION,
                value = appliedRevision.toString(),
            )
        )
        return true
    }

    companion object {
        const val CURRENT_CATALOG_VERSION = "5.0.0"
        private const val INITIAL_BUNDLED_REVISION = 1L
        private const val DELTA_SYNC_INTERVAL_MS = 12 * 3600 * 1000L
        private const val SETTING_CATALOG_VERSION = "catalog_seed_version"
        private const val SETTING_CATALOG_REVISION = "catalog_revision"
        private const val SETTING_LAST_REMOTE_SYNC_AT = "last_remote_sync_at"

        internal fun shouldFetchDelta(
            remoteRevision: Long,
            localRevision: Long,
            localItemCount: Int,
        ): Boolean {
            if (remoteRevision <= 0L) return false
            if (localItemCount <= 0) return true
            return remoteRevision > localRevision
        }

        internal fun isRemoteVersionAtLeast(remoteVersion: String?, minVersion: String): Boolean {
            if (remoteVersion.isNullOrBlank()) return false
            val rParts = remoteVersion.trim().split(".").map { it.toIntOrNull() ?: 0 }
            val mParts = minVersion.trim().split(".").map { it.toIntOrNull() ?: 0 }
            val maxLen = maxOf(rParts.size, mParts.size)
            for (i in 0 until maxLen) {
                val r = rParts.getOrElse(i) { 0 }
                val m = mParts.getOrElse(i) { 0 }
                if (r > m) return true
                if (r < m) return false
            }
            return true
        }
    }
}
