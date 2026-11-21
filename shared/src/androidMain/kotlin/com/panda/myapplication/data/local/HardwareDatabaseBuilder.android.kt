package com.panda.myapplication.data.local

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

private const val PREPOPULATED_DB_ASSET_VERSION = "5.0.0"

private data class PreservedPriceOverride(
    val hardwareId: String,
    val customPriceTry: Double,
    val note: String?,
    val updatedAt: Long,
)

private data class PreservedUserSession(
    val id: String,
    val userId: String,
    val email: String,
    val username: String,
    val birthDateIso: String,
    val gender: String,
    val spiritHardware: String,
    val createdAtEpochMs: Long,
)

private data class PreservedUserData(
    val priceOverrides: List<PreservedPriceOverride> = emptyList(),
    val userSessions: List<PreservedUserSession> = emptyList(),
    val userSettings: List<Pair<String, String>> = emptyList(),
)

private val CATALOG_INTERNAL_KEYS = setOf(
    "catalog_version",
    "catalog_seed_version",
    "catalog_revision",
    "last_remote_sync_at",
    "catalog_synced_source",
)

private fun readPreservedUserData(dbFile: File): PreservedUserData {
    if (!dbFile.exists() || dbFile.length() == 0L) return PreservedUserData()
    return runCatching {
        val overrides = mutableListOf<PreservedPriceOverride>()
        val sessions = mutableListOf<PreservedUserSession>()
        val settings = mutableListOf<Pair<String, String>>()

        SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            runCatching {
                db.rawQuery(
                    "SELECT hardware_id, custom_price_try, note, updated_at FROM user_price_overrides",
                    null,
                ).use { c ->
                    while (c.moveToNext()) {
                        overrides += PreservedPriceOverride(
                            hardwareId = c.getString(0),
                            customPriceTry = c.getDouble(1),
                            note = if (c.isNull(2)) null else c.getString(2),
                            updatedAt = c.getLong(3),
                        )
                    }
                }
            }
            runCatching {
                db.rawQuery(
                    "SELECT id, user_id, email, username, birth_date_iso, gender, spirit_hardware, created_at_epoch_ms FROM user_sessions",
                    null,
                ).use { c ->
                    while (c.moveToNext()) {
                        sessions += PreservedUserSession(
                            id = c.getString(0),
                            userId = c.getString(1),
                            email = c.getString(2),
                            username = c.getString(3),
                            birthDateIso = c.getString(4),
                            gender = c.getString(5),
                            spiritHardware = c.getString(6),
                            createdAtEpochMs = c.getLong(7),
                        )
                    }
                }
            }
            runCatching {
                db.rawQuery("SELECT key, value FROM app_settings", null).use { c ->
                    while (c.moveToNext()) {
                        val key = c.getString(0)
                        val value = c.getString(1)
                        if (key !in CATALOG_INTERNAL_KEYS) {
                            settings += key to value
                        }
                    }
                }
            }
        }
        PreservedUserData(overrides, sessions, settings)
    }.getOrDefault(PreservedUserData())
}

private fun restorePreservedUserData(dbFile: File, preserved: PreservedUserData) {
    if (preserved.priceOverrides.isEmpty() && preserved.userSessions.isEmpty() && preserved.userSettings.isEmpty()) {
        return
    }
    runCatching {
        SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READWRITE).use { db ->
            db.beginTransaction()
            try {
                preserved.priceOverrides.forEach { row ->
                    val cv = ContentValues().apply {
                        put("hardware_id", row.hardwareId)
                        put("custom_price_try", row.customPriceTry)
                        if (row.note != null) put("note", row.note) else putNull("note")
                        put("updated_at", row.updatedAt)
                    }
                    db.insertWithOnConflict("user_price_overrides", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                preserved.userSessions.forEach { row ->
                    val cv = ContentValues().apply {
                        put("id", row.id)
                        put("user_id", row.userId)
                        put("email", row.email)
                        put("username", row.username)
                        put("birth_date_iso", row.birthDateIso)
                        put("gender", row.gender)
                        put("spirit_hardware", row.spiritHardware)
                        put("created_at_epoch_ms", row.createdAtEpochMs)
                    }
                    db.insertWithOnConflict("user_sessions", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                preserved.userSettings.forEach { (key, value) ->
                    val cv = ContentValues().apply {
                        put("key", key)
                        put("value", value)
                    }
                    db.insertWithOnConflict("app_settings", null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}

fun hardwareDatabaseBuilder(context: Context): RoomDatabase.Builder<HardwareDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(HardwareDatabase.DATABASE_NAME)
    val prefs = appContext.getSharedPreferences("hardware_wizard_fast_prefs", Context.MODE_PRIVATE)
    val apkUpdateTime = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.packageManager.getPackageInfo(
                appContext.packageName,
                PackageManager.PackageInfoFlags.of(0),
            ).lastUpdateTime
        } else {
            @Suppress("DEPRECATION")
            appContext.packageManager.getPackageInfo(appContext.packageName, 0).lastUpdateTime
        }
    }.getOrDefault(0L)
    val expectedAssetFingerprint = "$PREPOPULATED_DB_ASSET_VERSION:$apkUpdateTime"
    val installedAssetVersion = prefs.getString("prepopulated_db_asset_version", null)
    if (installedAssetVersion != expectedAssetFingerprint || !dbFile.exists()) {
        runCatching {
            val preservedUserData = readPreservedUserData(dbFile)
            dbFile.parentFile?.mkdirs()
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            if (dbFile.exists()) dbFile.delete()
            appContext.assets.open("databases/${HardwareDatabase.DATABASE_NAME}").use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            restorePreservedUserData(dbFile, preservedUserData)
            prefs.edit().putString("prepopulated_db_asset_version", expectedAssetFingerprint).apply()
        }
    }
    return Room.databaseBuilder<HardwareDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
