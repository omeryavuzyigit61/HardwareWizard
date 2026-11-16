package com.panda.myapplication.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

private const val PREPOPULATED_DB_ASSET_VERSION = "3.0.0"

fun hardwareDatabaseBuilder(context: Context): RoomDatabase.Builder<HardwareDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(HardwareDatabase.DATABASE_NAME)
    val prefs = appContext.getSharedPreferences("hardware_wizard_fast_prefs", Context.MODE_PRIVATE)
    val installedAssetVersion = prefs.getString("prepopulated_db_asset_version", null)
    if (installedAssetVersion != PREPOPULATED_DB_ASSET_VERSION || !dbFile.exists()) {
        runCatching {
            dbFile.parentFile?.mkdirs()
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            if (dbFile.exists()) dbFile.delete()
            appContext.assets.open("databases/${HardwareDatabase.DATABASE_NAME}").use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            prefs.edit().putString("prepopulated_db_asset_version", PREPOPULATED_DB_ASSET_VERSION).apply()
        }
    }
    return Room.databaseBuilder<HardwareDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
