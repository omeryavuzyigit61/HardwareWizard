package com.panda.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.panda.myapplication.data.local.entity.CoolerSpecEntity
import com.panda.myapplication.data.local.entity.CpuSpecEntity
import com.panda.myapplication.data.local.entity.GpuSpecEntity
import com.panda.myapplication.data.local.entity.HardwareCatalogSnapshot
import com.panda.myapplication.data.local.entity.HardwareItemEntity
import com.panda.myapplication.data.local.entity.HardwareItemWithSpec
import com.panda.myapplication.data.local.entity.MotherboardSpecEntity
import com.panda.myapplication.data.local.entity.PsuSpecEntity
import com.panda.myapplication.data.local.entity.RamSpecEntity
import com.panda.myapplication.data.local.entity.StorageSpecEntity
import com.panda.myapplication.domain.model.HardwareCategory
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
abstract class HardwareDao {

    // region Reads

    @Transaction
    @Query("SELECT * FROM hardware_items ORDER BY brand, model")
    abstract fun observeAll(): Flow<List<HardwareItemWithSpec>>

    @Transaction
    @Query("SELECT * FROM hardware_items WHERE category = :category ORDER BY brand, model")
    abstract fun observeByCategory(category: HardwareCategory): Flow<List<HardwareItemWithSpec>>

    @Transaction
    @Query("SELECT * FROM hardware_items WHERE id = :id")
    abstract fun observeById(id: String): Flow<HardwareItemWithSpec?>

    @Query("SELECT DISTINCT brand FROM hardware_items WHERE category = :category ORDER BY brand ASC")
    abstract fun observeBrandsByCategory(category: HardwareCategory): Flow<List<String>>

    @Query("SELECT DISTINCT brand FROM hardware_items ORDER BY brand ASC")
    abstract fun observeAllBrands(): Flow<List<String>>

    // endregion

    // region Writes

    @Upsert
    protected abstract suspend fun upsertItems(items: List<HardwareItemEntity>)

    @Upsert
    protected abstract suspend fun upsertCpuSpecs(specs: List<CpuSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertGpuSpecs(specs: List<GpuSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertRamSpecs(specs: List<RamSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertMotherboardSpecs(specs: List<MotherboardSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertPsuSpecs(specs: List<PsuSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertStorageSpecs(specs: List<StorageSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertCoolerSpecs(specs: List<CoolerSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertCaseSpecs(specs: List<com.panda.myapplication.data.local.entity.CaseSpecEntity>)

    /** Spec rows are removed via `ON DELETE CASCADE`. */
    @Query("DELETE FROM hardware_items WHERE cached_at < :threshold")
    protected abstract suspend fun deleteItemsCachedBefore(threshold: Instant)

    /**
     * Atomically upserts the snapshot and evicts rows not refreshed in this sync.
     * All snapshot items must share the same [syncedAt] as their `cachedAt`.
     */
    @Transaction
    open suspend fun replaceCatalog(snapshot: HardwareCatalogSnapshot, syncedAt: Instant) {
        val chunkSize = 500
        snapshot.items.chunked(chunkSize).forEach { upsertItems(it) }
        snapshot.cpus.chunked(chunkSize).forEach { upsertCpuSpecs(it) }
        snapshot.gpus.chunked(chunkSize).forEach { upsertGpuSpecs(it) }
        snapshot.rams.chunked(chunkSize).forEach { upsertRamSpecs(it) }
        snapshot.motherboards.chunked(chunkSize).forEach { upsertMotherboardSpecs(it) }
        snapshot.psus.chunked(chunkSize).forEach { upsertPsuSpecs(it) }
        snapshot.storages.chunked(chunkSize).forEach { upsertStorageSpecs(it) }
        snapshot.coolers.chunked(chunkSize).forEach { upsertCoolerSpecs(it) }
        snapshot.cases.chunked(chunkSize).forEach { upsertCaseSpecs(it) }
        deleteItemsCachedBefore(syncedAt)
    }

    // endregion
}
