package com.panda.myapplication.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.panda.myapplication.data.local.converter.HardwareTypeConverters
import com.panda.myapplication.data.local.dao.HardwareDao
import com.panda.myapplication.data.local.entity.CaseSpecEntity
import com.panda.myapplication.data.local.entity.CoolerSpecEntity
import com.panda.myapplication.data.local.entity.CpuSpecEntity
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
import com.panda.myapplication.data.local.entity.GpuSpecEntity
import com.panda.myapplication.data.local.entity.HardwareItemEntity
import com.panda.myapplication.data.local.entity.HeadsetSpecEntity
import com.panda.myapplication.data.local.entity.KeyboardSpecEntity
import com.panda.myapplication.data.local.entity.LaptopSpecEntity
import com.panda.myapplication.data.local.entity.MicrophoneSpecEntity
import com.panda.myapplication.data.local.entity.MonitorSpecEntity
import com.panda.myapplication.data.local.entity.MotherboardSpecEntity
import com.panda.myapplication.data.local.entity.MouseSpecEntity
import com.panda.myapplication.data.local.entity.MousepadSpecEntity
import com.panda.myapplication.data.local.entity.PrebuiltSpecEntity
import com.panda.myapplication.data.local.entity.PsuSpecEntity
import com.panda.myapplication.data.local.entity.RamSpecEntity
import com.panda.myapplication.data.local.entity.StorageSpecEntity
import com.panda.myapplication.data.local.entity.UserPriceOverrideEntity
import com.panda.myapplication.data.local.entity.UserSessionEntity
import com.panda.myapplication.data.local.entity.AppSettingEntity
import com.panda.myapplication.data.local.entity.WebcamSpecEntity
import kotlinx.coroutines.CoroutineDispatcher

@Database(
    entities = [
        HardwareItemEntity::class,
        CpuSpecEntity::class,
        GpuSpecEntity::class,
        RamSpecEntity::class,
        MotherboardSpecEntity::class,
        PsuSpecEntity::class,
        StorageSpecEntity::class,
        CoolerSpecEntity::class,
        CaseSpecEntity::class,
        PrebuiltSpecEntity::class,
        LaptopSpecEntity::class,
        MouseSpecEntity::class,
        KeyboardSpecEntity::class,
        HeadsetSpecEntity::class,
        MonitorSpecEntity::class,
        MicrophoneSpecEntity::class,
        MousepadSpecEntity::class,
        WebcamSpecEntity::class,
        GamingChairSpecEntity::class,
        CaseFanSpecEntity::class,
        UpsSpecEntity::class,
        GamingDeskSpecEntity::class,
        GamingLightingSpecEntity::class,
        SmartwatchSpecEntity::class,
        RouterSpecEntity::class,
        ModemSpecEntity::class,
        AccessPointSpecEntity::class,
        RangeExtenderSpecEntity::class,
        UserPriceOverrideEntity::class,
        UserSessionEntity::class,
        AppSettingEntity::class,
    ],
    version = 32,
    exportSchema = false,
)
@TypeConverters(HardwareTypeConverters::class)
@ConstructedBy(HardwareDatabaseConstructor::class)
abstract class HardwareDatabase : RoomDatabase() {

    abstract fun hardwareDao(): HardwareDao

    companion object {
        const val DATABASE_NAME = "hardware_wizard.db"
    }
}

/** Implemented per platform by the Room KSP processor. */
@Suppress("KotlinNoActualForExpect")
expect object HardwareDatabaseConstructor : RoomDatabaseConstructor<HardwareDatabase> {
    override fun initialize(): HardwareDatabase
}

/**
 * Finalizes a platform-provided builder. Destructive migration is acceptable because the
 * database is a re-fetchable cache, not a source of user-generated data.
 */
fun buildHardwareDatabase(
    builder: RoomDatabase.Builder<HardwareDatabase>,
    queryDispatcher: CoroutineDispatcher,
): HardwareDatabase = builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(queryDispatcher)
    .fallbackToDestructiveMigration(dropAllTables = true)
    .build()
