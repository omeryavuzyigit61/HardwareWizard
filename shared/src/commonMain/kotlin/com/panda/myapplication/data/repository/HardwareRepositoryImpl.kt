package com.panda.myapplication.data.repository

import com.panda.myapplication.data.local.HardwareLocalDataSource
import com.panda.myapplication.data.local.dao.HardwareDao
import com.panda.myapplication.data.local.entity.UserPriceOverrideEntity
import com.panda.myapplication.data.mapper.HardwareMapper
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


    private inline fun <T> mergeCatalogs(remote: List<T>, local: List<T>, keySelector: (T) -> String?): List<T> {
        if (remote.isEmpty()) return local
        if (local.isEmpty()) return remote
        val remoteKeys = remote.mapNotNull { keySelector(it)?.trim()?.lowercase() }.toSet()
        val missingLocals = local.filter { item ->
            val key = keySelector(item)?.trim()?.lowercase()
            key != null && !remoteKeys.contains(key)
        }
        return remote + missingLocals
    }

    private fun mergePeripherals(
        remote: List<com.panda.myapplication.data.model.dto.PeripheralRawDto>,
        local: List<com.panda.myapplication.data.model.dto.PeripheralRawDto>,
    ): List<com.panda.myapplication.data.model.dto.PeripheralRawDto> {
        if (remote.isEmpty()) return local
        if (local.isEmpty()) return remote
        val localByKey = local.associateBy { it.name.trim().lowercase() }
        val enrichedRemote = remote.map { rem ->
            val loc = localByKey[rem.name.trim().lowercase()]
            if (loc != null && loc.images.size >= 3 && rem.images.size < 3) {
                rem.copy(images = loc.images, imageUrl = loc.imageUrl ?: rem.imageUrl)
            } else {
                rem
            }
        }
        val remoteKeys = enrichedRemote.map { it.name.trim().lowercase() }.toSet()
        val missingLocals = local.filter { it.name.trim().lowercase() !in remoteKeys }
        return enrichedRemote + missingLocals
    }

    private suspend fun ensureSeeded() {
        if (isSeeded) return
        seedMutex.withLock {
            if (isSeeded) return
            val seededVersion = hardwareDao.getSetting("catalog_seed_version")
            val currentCatalogVersion = "2.1.0"
            if (seededVersion == currentCatalogVersion) {
                isSeeded = true
                return
            }

            val counts = hardwareDao.getCategoryCounts().associate { it.category to it.cnt }
            val hasCpus = (counts[HardwareCategory.CPU] ?: 0) > 0
            val hasGpus = (counts[HardwareCategory.GPU] ?: 0) > 0
            val hasPrebuilts = (counts[HardwareCategory.PREBUILT_PC] ?: 0) > 0
            val laptopCount = counts[HardwareCategory.LAPTOP] ?: 0
            val phoneCount = counts[HardwareCategory.SMARTPHONE] ?: 0
            val tabletCount = counts[HardwareCategory.TABLET] ?: 0
            val caseCount = counts[HardwareCategory.CASE] ?: 0
            val coolerCount = counts[HardwareCategory.COOLER] ?: 0
            val moboCount = counts[HardwareCategory.MOTHERBOARD] ?: 0
            val micCount = counts[HardwareCategory.MICROPHONE] ?: 0
            val padCount = counts[HardwareCategory.MOUSEPAD] ?: 0
            val camCount = counts[HardwareCategory.WEBCAM] ?: 0
            val chairCount = counts[HardwareCategory.GAMING_CHAIR] ?: 0
            val fanCount = counts[HardwareCategory.CASE_FAN] ?: 0
            val upsCount = counts[HardwareCategory.UPS] ?: 0
            val deskCount = counts[HardwareCategory.GAMING_DESK] ?: 0
            val lightCount = counts[HardwareCategory.GAMING_LIGHTING] ?: 0
            val swCount = counts[HardwareCategory.SMARTWATCH] ?: 0
            val routerCount = counts[HardwareCategory.ROUTER] ?: 0
            val modemCount = counts[HardwareCategory.MODEM] ?: 0
            val apCount = counts[HardwareCategory.ACCESS_POINT] ?: 0
            val reCount = counts[HardwareCategory.RANGE_EXTENDER] ?: 0

            val peripheralCount = (counts[HardwareCategory.KEYBOARD] ?: 0) +
                (counts[HardwareCategory.MOUSE] ?: 0) +
                (counts[HardwareCategory.HEADSET] ?: 0) +
                (counts[HardwareCategory.MONITOR] ?: 0)

            val isMissingCore = !hasCpus || !hasGpus || !hasPrebuilts ||
                laptopCount < 300 || phoneCount < 200 || tabletCount < 30 || peripheralCount < 950 || caseCount < 150 || coolerCount < 140 ||
                moboCount < 640 || micCount < 120 || padCount < 190 || camCount < 75 || chairCount < 140 ||
                fanCount < 180 || upsCount < 140 || deskCount < 100 || lightCount < 90 ||
                swCount < 180 || routerCount < 140 || modemCount < 60 || apCount < 80 || reCount < 80

            if (counts.isEmpty() || isMissingCore) {
                localDataSource.seedDatabaseIfEmpty()
            }
            hardwareDao.saveSetting(
                com.panda.myapplication.data.local.entity.AppSettingEntity(
                    key = "catalog_seed_version",
                    value = currentCatalogVersion,
                )
            )
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
            HardwareCategory.PREBUILT_PC -> combine(
                hardwareDao.observeItemEntitiesByCategory(category),
                hardwareDao.observeAllPrebuiltSpecs(),
                hardwareDao.observeAllPriceOverrides(),
            ) { items, specs, overrides ->
                val specMap = specs.associateBy { it.itemId }
                val overrideMap = overrides.associateBy { it.hardwareId }
                items.mapNotNull { item ->
                    HardwareMapper.toDomain(
                        com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                            item = item,
                            prebuilt = specMap[item.id],
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
        val lastSyncStr = hardwareDao.getSetting("last_remote_sync_at")
        val lastSyncMillis = lastSyncStr?.toLongOrNull()
        if (lastSyncMillis != null && (now.toEpochMilliseconds() - lastSyncMillis) < 24 * 3600 * 1000L) {
            return@withContext DomainResult.Success(Unit)
        }
        try {
            val remotePrices = remoteDataSource.fetchPricesTr().getOrDefault(emptyMap())
            val remoteBenchmarks = remoteDataSource.fetchBenchmarks().getOrDefault(emptyMap())
            val remoteVerified = remoteDataSource.fetchVerifiedLabBenchmarks().getOrDefault(emptyList())
            if (remoteVerified.isNotEmpty()) {
                com.panda.myapplication.domain.model.VerifiedLabBenchmarks.updateRemoteTests(remoteVerified)
            }
            val remoteSmartphones: List<com.panda.myapplication.data.model.dto.SmartphoneCatalogItemDto> = runCatching {
                val res = remoteDataSource.fetchSmartphonesJson()
                if (res.isSuccess) {
                    json.decodeFromString<List<com.panda.myapplication.data.model.dto.SmartphoneCatalogItemDto>>(res.getOrThrow())
                } else emptyList()
            }.getOrDefault(emptyList())

            val remoteTablets: List<com.panda.myapplication.data.model.dto.TabletCatalogItemDto> = runCatching {
                val res = remoteDataSource.fetchTabletsJson()
                if (res.isSuccess) {
                    json.decodeFromString<List<com.panda.myapplication.data.model.dto.TabletCatalogItemDto>>(res.getOrThrow())
                } else emptyList()
            }.getOrDefault(emptyList())
            val localPrices = localDataSource.loadLocalPricesTr()
            val localBenchmarks = localDataSource.loadLocalBenchmarks()
            val allPrices = localPrices.toMutableMap().apply {
                remotePrices.forEach { (key, remoteInfo) ->
                    val localInfo = this[key]
                    if (localInfo == null || (remoteInfo.currentPriceTry != null && (localInfo.currentPriceTry == null || remoteInfo.offers.isNotEmpty()))) {
                        this[key] = remoteInfo
                    }
                }
            }
            val allBenchmarks = localBenchmarks.toMutableMap().apply {
                remoteBenchmarks.forEach { (key, remoteInfo) ->
                    if (!containsKey(key)) {
                        this[key] = remoteInfo
                    }
                }
            }

            val existing = hardwareDao.getAllHardwareItems()
            val existingImages = existing.associate { it.id to it.imageUrl }

            val catalogResult = remoteDataSource.fetchAllHardware()
            if (catalogResult.isSuccess) {
                val catalog = catalogResult.getOrThrow()
                val snapshot = HardwareMapper.buildCatalogSnapshot(
                    cpus = mergeCatalogs(catalog.cpus, localDataSource.loadLocalCpus()) { it.name },
                    gpus = mergeCatalogs(catalog.gpus, localDataSource.loadLocalGpus()) { it.name },
                    rams = mergeCatalogs(catalog.rams, localDataSource.loadLocalRams()) { it.name },
                    motherboards = mergeCatalogs(catalog.motherboards, localDataSource.loadLocalMotherboards()) { it.name },
                    psus = mergeCatalogs(catalog.psus, localDataSource.loadLocalPsus()) { it.name },
                    storages = mergeCatalogs(catalog.storages, localDataSource.loadLocalStorages()) { it.name },
                    coolers = mergeCatalogs(catalog.coolers, localDataSource.loadLocalCoolers()) { it.name },
                    cases = mergeCatalogs(catalog.cases, localDataSource.loadLocalCases()) { it.name },
                    prebuilts = mergeCatalogs(catalog.prebuilts, localDataSource.loadLocalPrebuilts()) { it.name },
                    laptops = mergeCatalogs(catalog.laptops, localDataSource.loadLocalLaptops()) { it.name },
                    peripherals = mergePeripherals(catalog.peripherals, localDataSource.loadLocalPeripherals()),
                    smartphones = mergeCatalogs(remoteSmartphones, localDataSource.loadLocalSmartphones()) { it.id },
                    tablets = mergeCatalogs(remoteTablets, localDataSource.loadLocalTablets()) { it.id },
                    pricesTr = allPrices,
                    benchmarks = allBenchmarks,
                    existingImages = existingImages,
                    syncedAt = now,
                )
                if (snapshot.items.isNotEmpty()) {
                    hardwareDao.replaceCatalog(snapshot, syncedAt = now)
                } else if (existing.isNotEmpty() && (allPrices.isNotEmpty() || allBenchmarks.isNotEmpty())) {
                    val updated = existing.map { item ->
                        val priceData = allPrices[item.id]
                        val gpuKey = if (item.category == HardwareCategory.GPU) {
                            com.panda.myapplication.domain.engine.BenchmarkEngine.normalizeGpuChipsetKey(item.model)
                        } else null
                        val benchData = allBenchmarks[item.id] ?: (gpuKey?.let { allBenchmarks["gpu_$it"] ?: allBenchmarks[it] })
                        item.copy(
                            currentPriceTry = priceData?.currentPriceTry ?: item.currentPriceTry,
                            lowestPriceTry = priceData?.lowestPriceTry ?: item.lowestPriceTry,
                            highestPriceTry = priceData?.highestPriceTry ?: item.highestPriceTry,
                            offers = priceData?.offers ?: item.offers,
                            priceHistory = priceData?.priceHistory ?: item.priceHistory,
                            benchmarkScore = benchData?.score ?: item.benchmarkScore,
                            benchmarkTier = benchData?.tier ?: item.benchmarkTier,
                            benchmarkTestSuite = benchData?.testSuite ?: item.benchmarkTestSuite,
                            benchmarkGamingIndex = benchData?.gamingIndex ?: item.benchmarkGamingIndex,
                            cpuSynthetic = benchData?.cpuSynthetic ?: item.cpuSynthetic,
                            gpuSynthetic = benchData?.gpuSynthetic ?: item.gpuSynthetic,
                            gameFps = benchData?.gameFps ?: item.gameFps,
                        )
                    }
                    hardwareDao.upsertHardwareItems(updated)
                }
            } else {
                // If remote catalog fetch fails (offline or 404), update existing Room items with any available prices & benchmarks
                if (existing.isNotEmpty() && (allPrices.isNotEmpty() || allBenchmarks.isNotEmpty())) {
                    val updated = existing.map { item ->
                        val priceData = allPrices[item.id]
                        val gpuKey = if (item.category == HardwareCategory.GPU) {
                            com.panda.myapplication.domain.engine.BenchmarkEngine.normalizeGpuChipsetKey(item.model)
                        } else null
                        val benchData = allBenchmarks[item.id] ?: (gpuKey?.let { allBenchmarks["gpu_$it"] ?: allBenchmarks[it] })
                        item.copy(
                            currentPriceTry = priceData?.currentPriceTry ?: item.currentPriceTry,
                            lowestPriceTry = priceData?.lowestPriceTry ?: item.lowestPriceTry,
                            highestPriceTry = priceData?.highestPriceTry ?: item.highestPriceTry,
                            offers = priceData?.offers ?: item.offers,
                            priceHistory = priceData?.priceHistory ?: item.priceHistory,
                            benchmarkScore = benchData?.score ?: item.benchmarkScore,
                            benchmarkTier = benchData?.tier ?: item.benchmarkTier,
                            benchmarkTestSuite = benchData?.testSuite ?: item.benchmarkTestSuite,
                            benchmarkGamingIndex = benchData?.gamingIndex ?: item.benchmarkGamingIndex,
                            cpuSynthetic = benchData?.cpuSynthetic ?: item.cpuSynthetic,
                            gpuSynthetic = benchData?.gpuSynthetic ?: item.gpuSynthetic,
                            gameFps = benchData?.gameFps ?: item.gameFps,
                        )
                    }
                    hardwareDao.upsertHardwareItems(updated)
                }
            }
            hardwareDao.saveSetting(
                com.panda.myapplication.data.local.entity.AppSettingEntity(
                    key = "last_remote_sync_at",
                    value = now.toEpochMilliseconds().toString(),
                )
            )
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            DomainResult.Failure(DomainError.Network)
        }
    }
}
