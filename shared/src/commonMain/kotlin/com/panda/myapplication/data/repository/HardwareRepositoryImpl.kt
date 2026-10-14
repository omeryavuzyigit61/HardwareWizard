package com.panda.myapplication.data.repository

import com.panda.myapplication.data.local.HardwareLocalDataSource
import com.panda.myapplication.data.local.dao.HardwareDao
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
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class HardwareRepositoryImpl(
    private val hardwareDao: HardwareDao,
    private val localDataSource: HardwareLocalDataSource,
    private val remoteDataSource: HardwareRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : HardwareRepository {

    private val seedMutex = Mutex()
    private var isSeeded = false
    private val memoryCache = MutableStateFlow<List<HardwareItem>?>(null)

    private suspend fun ensureSeeded() {
        if (isSeeded) return
        seedMutex.withLock {
            if (isSeeded) return
            localDataSource.seedDatabaseIfEmpty()
            isSeeded = true
        }
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
        // 3. Observe Room updates
        emitAll(
            hardwareDao.observeAll()
                .map { list ->
                    val items = list.mapNotNull { HardwareMapper.toDomain(it) }
                    memoryCache.value = items
                    DomainResult.Success(items) as DomainResult<List<HardwareItem>>
                }
                .catch {
                    val fallback = memoryCache.value ?: emptyList()
                    emit(DomainResult.Success(fallback))
                }
        )
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
        // 3. Observe Room updates
        emitAll(
            hardwareDao.observeByCategory(category)
                .map { list ->
                    val items = list.mapNotNull { HardwareMapper.toDomain(it) }
                    DomainResult.Success(items) as DomainResult<List<HardwareItem>>
                }
                .catch {
                    val fallback = memoryCache.value?.filter { it.category == category } ?: emptyList()
                    emit(DomainResult.Success(fallback))
                }
        )
    }.flowOn(ioDispatcher)

    override fun observeHardwareItem(id: HardwareId): Flow<DomainResult<HardwareItem>> = flow {
        ensureSeeded()
        emitAll(
            hardwareDao.observeById(id.value)
                .map { itemWithSpec ->
                    if (itemWithSpec == null) {
                        DomainResult.Failure(DomainError.NotFound)
                    } else {
                        val domainItem = HardwareMapper.toDomain(itemWithSpec)
                        if (domainItem != null) {
                            DomainResult.Success(domainItem)
                        } else {
                            DomainResult.Failure(DomainError.NotFound)
                        }
                    }
                }
                .catch {
                    emit(DomainResult.Failure(DomainError.Storage))
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

    override suspend fun refreshHardwareItems(): DomainResult<Unit> = withContext(ioDispatcher) {
        ensureSeeded()
        val now = Clock.System.now()
        runCatching {
            val remotePrices = remoteDataSource.fetchPricesTr().getOrDefault(emptyMap())
            val remoteBenchmarks = remoteDataSource.fetchBenchmarks().getOrDefault(emptyMap())
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
                    cpus = catalog.cpus,
                    gpus = catalog.gpus,
                    rams = catalog.rams,
                    motherboards = catalog.motherboards,
                    psus = catalog.psus,
                    storages = catalog.storages,
                    coolers = catalog.coolers,
                    cases = catalog.cases,
                    pricesTr = allPrices,
                    benchmarks = allBenchmarks,
                    existingImages = existingImages,
                    syncedAt = now,
                )
                if (snapshot.items.isNotEmpty() && snapshot.items.size >= (existing.size * 0.7)) {
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
            DomainResult.Success(Unit)
        }.getOrElse {
            // Silent fallback to local cache - background network error never fails UI
            DomainResult.Success(Unit)
        }
    }
}
