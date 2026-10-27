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
import com.panda.myapplication.data.local.entity.CaseSpecEntity
import com.panda.myapplication.data.local.entity.GamingChairSpecEntity
import com.panda.myapplication.data.local.entity.CaseFanSpecEntity
import com.panda.myapplication.data.local.entity.UpsSpecEntity
import com.panda.myapplication.data.local.entity.GamingDeskSpecEntity
import com.panda.myapplication.data.local.entity.GamingLightingSpecEntity
import com.panda.myapplication.data.local.entity.SmartwatchSpecEntity
import com.panda.myapplication.data.local.entity.RouterSpecEntity
import com.panda.myapplication.data.local.entity.ModemSpecEntity
import com.panda.myapplication.data.local.entity.AccessPointSpecEntity
import com.panda.myapplication.data.local.entity.RangeExtenderSpecEntity
import com.panda.myapplication.data.local.entity.HeadsetSpecEntity
import com.panda.myapplication.data.local.entity.KeyboardSpecEntity
import com.panda.myapplication.data.local.entity.LaptopSpecEntity
import com.panda.myapplication.data.local.entity.MicrophoneSpecEntity
import com.panda.myapplication.data.local.entity.MonitorSpecEntity
import com.panda.myapplication.data.local.entity.MouseSpecEntity
import com.panda.myapplication.data.local.entity.MousepadSpecEntity
import com.panda.myapplication.data.local.entity.PrebuiltSpecEntity
import com.panda.myapplication.data.local.entity.UserPriceOverrideEntity
import com.panda.myapplication.data.local.entity.UserSessionEntity
import com.panda.myapplication.data.local.entity.AppSettingEntity
import com.panda.myapplication.data.local.entity.WebcamSpecEntity
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
    @Query("SELECT * FROM hardware_items WHERE category = :category ORDER BY CASE WHEN current_price_try IS NULL THEN 1 ELSE 0 END, current_price_try ASC, brand ASC, model ASC")
    abstract fun observeByCategory(category: HardwareCategory): Flow<List<HardwareItemWithSpec>>

    @Query("SELECT * FROM hardware_items WHERE category = :category ORDER BY CASE WHEN current_price_try IS NULL THEN 1 ELSE 0 END, current_price_try ASC, brand ASC, model ASC")
    abstract fun observeItemEntitiesByCategory(category: HardwareCategory): Flow<List<HardwareItemEntity>>

    @Query("SELECT * FROM hardware_items ORDER BY CASE WHEN current_price_try IS NULL THEN 1 ELSE 0 END, current_price_try ASC, brand ASC, model ASC")
    abstract fun observeAllItemEntities(): Flow<List<HardwareItemEntity>>

    @Query("SELECT * FROM cpu_specs")
    abstract fun observeAllCpuSpecs(): Flow<List<CpuSpecEntity>>

    @Query("SELECT * FROM gpu_specs")
    abstract fun observeAllGpuSpecs(): Flow<List<GpuSpecEntity>>

    @Query("SELECT * FROM ram_specs")
    abstract fun observeAllRamSpecs(): Flow<List<RamSpecEntity>>

    @Query("SELECT * FROM motherboard_specs")
    abstract fun observeAllMotherboardSpecs(): Flow<List<MotherboardSpecEntity>>

    @Query("SELECT * FROM psu_specs")
    abstract fun observeAllPsuSpecs(): Flow<List<PsuSpecEntity>>

    @Query("SELECT * FROM storage_specs")
    abstract fun observeAllStorageSpecs(): Flow<List<StorageSpecEntity>>

    @Query("SELECT * FROM cooler_specs")
    abstract fun observeAllCoolerSpecs(): Flow<List<CoolerSpecEntity>>

    @Query("SELECT * FROM case_specs")
    abstract fun observeAllCaseSpecs(): Flow<List<CaseSpecEntity>>

    @Query("SELECT * FROM prebuilt_specs")
    abstract fun observeAllPrebuiltSpecs(): Flow<List<PrebuiltSpecEntity>>

    @Query("SELECT * FROM laptop_specs")
    abstract fun observeAllLaptopSpecs(): Flow<List<LaptopSpecEntity>>

    @Query("SELECT * FROM mouse_specs")
    abstract fun observeAllMouseSpecs(): Flow<List<MouseSpecEntity>>

    @Query("SELECT * FROM keyboard_specs")
    abstract fun observeAllKeyboardSpecs(): Flow<List<KeyboardSpecEntity>>

    @Query("SELECT * FROM headset_specs")
    abstract fun observeAllHeadsetSpecs(): Flow<List<HeadsetSpecEntity>>

    @Query("SELECT * FROM monitor_specs")
    abstract fun observeAllMonitorSpecs(): Flow<List<MonitorSpecEntity>>

    @Query("SELECT * FROM microphone_specs")
    abstract fun observeAllMicrophoneSpecs(): Flow<List<MicrophoneSpecEntity>>

    @Query("SELECT * FROM mousepad_specs")
    abstract fun observeAllMousepadSpecs(): Flow<List<MousepadSpecEntity>>

    @Query("SELECT * FROM webcam_specs")
    abstract fun observeAllWebcamSpecs(): Flow<List<WebcamSpecEntity>>

    @Query("SELECT * FROM gaming_chair_specs")
    abstract fun observeAllGamingChairSpecs(): Flow<List<GamingChairSpecEntity>>

    @Query("SELECT * FROM case_fan_specs")
    abstract fun observeAllCaseFanSpecs(): Flow<List<CaseFanSpecEntity>>

    @Query("SELECT * FROM ups_specs")
    abstract fun observeAllUpsSpecs(): Flow<List<UpsSpecEntity>>

    @Query("SELECT * FROM gaming_desk_specs")
    abstract fun observeAllGamingDeskSpecs(): Flow<List<GamingDeskSpecEntity>>

    @Query("SELECT * FROM gaming_lighting_specs")
    abstract fun observeAllGamingLightingSpecs(): Flow<List<GamingLightingSpecEntity>>

    @Query("SELECT * FROM smartwatch_specs")
    abstract fun observeAllSmartwatchSpecs(): Flow<List<SmartwatchSpecEntity>>

    @Query("SELECT * FROM router_specs")
    abstract fun observeAllRouterSpecs(): Flow<List<RouterSpecEntity>>

    @Query("SELECT * FROM modem_specs")
    abstract fun observeAllModemSpecs(): Flow<List<ModemSpecEntity>>

    @Query("SELECT * FROM access_point_specs")
    abstract fun observeAllAccessPointSpecs(): Flow<List<AccessPointSpecEntity>>

    @Query("SELECT * FROM range_extender_specs")
    abstract fun observeAllRangeExtenderSpecs(): Flow<List<RangeExtenderSpecEntity>>

    @Query("SELECT * FROM hardware_items WHERE id = :id LIMIT 1")
    abstract fun observeItemEntityById(id: String): Flow<HardwareItemEntity?>

    @Query("SELECT * FROM hardware_items WHERE id = :id LIMIT 1")
    abstract suspend fun getItemEntityById(id: String): HardwareItemEntity?

    @Transaction
    @Query("SELECT * FROM hardware_items WHERE id = :id")
    abstract fun observeById(id: String): Flow<HardwareItemWithSpec?>

    @Query("SELECT DISTINCT brand FROM hardware_items WHERE category = :category ORDER BY brand ASC")
    abstract fun observeBrandsByCategory(category: HardwareCategory): Flow<List<String>>

    @Query("SELECT DISTINCT brand FROM hardware_items ORDER BY brand ASC")
    abstract fun observeAllBrands(): Flow<List<String>>

    // endregion

    // region Writes

    @Query("SELECT * FROM hardware_items")
    abstract suspend fun getAllHardwareItems(): List<HardwareItemEntity>

    @Upsert
    abstract suspend fun upsertHardwareItems(items: List<HardwareItemEntity>)

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
    protected abstract suspend fun upsertCaseSpecs(specs: List<CaseSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertPrebuiltSpecs(specs: List<PrebuiltSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertLaptopSpecs(specs: List<LaptopSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertMouseSpecs(specs: List<MouseSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertKeyboardSpecs(specs: List<KeyboardSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertHeadsetSpecs(specs: List<HeadsetSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertMonitorSpecs(specs: List<MonitorSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertMicrophoneSpecs(specs: List<MicrophoneSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertMousepadSpecs(specs: List<MousepadSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertWebcamSpecs(specs: List<WebcamSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertGamingChairSpecs(specs: List<GamingChairSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertCaseFanSpecs(specs: List<CaseFanSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertUpsSpecs(specs: List<UpsSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertGamingDeskSpecs(specs: List<GamingDeskSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertGamingLightingSpecs(specs: List<GamingLightingSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertSmartwatchSpecs(specs: List<SmartwatchSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertRouterSpecs(specs: List<RouterSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertModemSpecs(specs: List<ModemSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertAccessPointSpecs(specs: List<AccessPointSpecEntity>)

    @Upsert
    protected abstract suspend fun upsertRangeExtenderSpecs(specs: List<RangeExtenderSpecEntity>)

    // region User Price Overrides (BÖLÜM 2)

    @Query("SELECT * FROM user_price_overrides")
    abstract fun observeAllPriceOverrides(): Flow<List<UserPriceOverrideEntity>>

    @Upsert
    abstract suspend fun upsertPriceOverride(overrideEntity: UserPriceOverrideEntity)

    @Query("DELETE FROM user_price_overrides WHERE hardware_id = :hardwareId")
    abstract suspend fun deletePriceOverride(hardwareId: String)

    // endregion

    // region User Session (Local Persistence)

    @Query("SELECT * FROM user_sessions WHERE id = :id LIMIT 1")
    abstract suspend fun getUserSession(id: String = UserSessionEntity.ACTIVE_SESSION_ID): UserSessionEntity?

    @Upsert
    abstract suspend fun saveUserSession(session: UserSessionEntity)

    @Query("DELETE FROM user_sessions WHERE id = :id")
    abstract suspend fun clearUserSession(id: String = UserSessionEntity.ACTIVE_SESSION_ID)

    // endregion

    // region App Settings

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    abstract suspend fun getSetting(key: String): String?

    @Upsert
    abstract suspend fun saveSetting(setting: AppSettingEntity)

    @Query("SELECT * FROM app_settings")
    abstract suspend fun getAllSettings(): List<AppSettingEntity>

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    abstract suspend fun deleteSetting(key: String)

    // endregion

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
        snapshot.prebuilts.chunked(chunkSize).forEach { upsertPrebuiltSpecs(it) }
        snapshot.laptops.chunked(chunkSize).forEach { upsertLaptopSpecs(it) }
        snapshot.mice.chunked(chunkSize).forEach { upsertMouseSpecs(it) }
        snapshot.keyboards.chunked(chunkSize).forEach { upsertKeyboardSpecs(it) }
        snapshot.headsets.chunked(chunkSize).forEach { upsertHeadsetSpecs(it) }
        snapshot.monitors.chunked(chunkSize).forEach { upsertMonitorSpecs(it) }
        snapshot.microphones.chunked(chunkSize).forEach { upsertMicrophoneSpecs(it) }
        snapshot.mousepads.chunked(chunkSize).forEach { upsertMousepadSpecs(it) }
        snapshot.webcams.chunked(chunkSize).forEach { upsertWebcamSpecs(it) }
        snapshot.gamingChairs.chunked(chunkSize).forEach { upsertGamingChairSpecs(it) }
        snapshot.caseFans.chunked(chunkSize).forEach { upsertCaseFanSpecs(it) }
        snapshot.upss.chunked(chunkSize).forEach { upsertUpsSpecs(it) }
        snapshot.gamingDesks.chunked(chunkSize).forEach { upsertGamingDeskSpecs(it) }
        snapshot.gamingLightings.chunked(chunkSize).forEach { upsertGamingLightingSpecs(it) }
        snapshot.smartwatches.chunked(chunkSize).forEach { upsertSmartwatchSpecs(it) }
        snapshot.routers.chunked(chunkSize).forEach { upsertRouterSpecs(it) }
        snapshot.modems.chunked(chunkSize).forEach { upsertModemSpecs(it) }
        snapshot.accessPoints.chunked(chunkSize).forEach { upsertAccessPointSpecs(it) }
        snapshot.rangeExtenders.chunked(chunkSize).forEach { upsertRangeExtenderSpecs(it) }
        deleteItemsCachedBefore(syncedAt)
    }

    // endregion
}
