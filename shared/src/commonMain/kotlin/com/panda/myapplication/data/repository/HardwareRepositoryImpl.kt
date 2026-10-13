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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class HardwareRepositoryImpl(
    private val hardwareDao: HardwareDao,
    private val localDataSource: HardwareLocalDataSource,
    private val remoteDataSource: HardwareRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher,
) : HardwareRepository {

    override fun observeHardwareItems(): Flow<DomainResult<List<HardwareItem>>> =
        hardwareDao.observeAll()
            .onStart {
                localDataSource.seedDatabaseIfEmpty()
            }
            .map { list ->
                val items = list.mapNotNull { HardwareMapper.toDomain(it) }
                DomainResult.Success(items) as DomainResult<List<HardwareItem>>
            }
            .catch { error ->
                emit(DomainResult.Failure(DomainError.Storage))
            }
            .flowOn(ioDispatcher)

    override fun observeHardwareItemsByCategory(category: HardwareCategory): Flow<DomainResult<List<HardwareItem>>> =
        hardwareDao.observeByCategory(category)
            .onStart {
                localDataSource.seedDatabaseIfEmpty()
            }
            .map { list ->
                val items = list.mapNotNull { HardwareMapper.toDomain(it) }
                DomainResult.Success(items) as DomainResult<List<HardwareItem>>
            }
            .catch { error ->
                emit(DomainResult.Failure(DomainError.Storage))
            }
            .flowOn(ioDispatcher)

    override fun observeHardwareItem(id: HardwareId): Flow<DomainResult<HardwareItem>> =
        hardwareDao.observeById(id.value)
            .onStart {
                localDataSource.seedDatabaseIfEmpty()
            }
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
            .catch { error ->
                emit(DomainResult.Failure(DomainError.Storage))
            }
            .flowOn(ioDispatcher)

    override fun observeBrands(category: HardwareCategory?): Flow<DomainResult<List<String>>> {
        val flow = if (category == null) {
            hardwareDao.observeAllBrands()
        } else {
            hardwareDao.observeBrandsByCategory(category)
        }
        return flow.map { brands ->
            DomainResult.Success(brands.filter { it.isNotBlank() }) as DomainResult<List<String>>
        }.catch {
            emit(DomainResult.Failure(DomainError.Storage))
        }.flowOn(ioDispatcher)
    }

    override suspend fun refreshHardwareItems(): DomainResult<Unit> = withContext(ioDispatcher) {
        val result = remoteDataSource.fetchAllHardware()
        result.fold(
            onSuccess = { catalog ->
                runCatching {
                    val now = Clock.System.now()
                    val remotePrices = remoteDataSource.fetchPricesTr().getOrDefault(emptyMap())
                    val localPrices = localDataSource.loadLocalPricesTr()
                    val allPrices = localPrices + remotePrices

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
                        syncedAt = now,
                    )
                    hardwareDao.replaceCatalog(snapshot, syncedAt = now)
                }.fold(
                    onSuccess = { DomainResult.Success(Unit) },
                    onFailure = { error -> DomainResult.Failure(DomainError.Storage) },
                )
            },
            onFailure = { error ->
                DomainResult.Failure(DomainError.Network)
            },
        )
    }
}
