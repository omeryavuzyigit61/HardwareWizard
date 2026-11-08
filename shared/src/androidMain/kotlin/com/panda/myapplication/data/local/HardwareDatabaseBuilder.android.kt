package com.panda.myapplication.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun hardwareDatabaseBuilder(context: Context): RoomDatabase.Builder<HardwareDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(HardwareDatabase.DATABASE_NAME)
    if (!dbFile.exists()) {
        runCatching {
            dbFile.parentFile?.mkdirs()
            appContext.assets.open("databases/${HardwareDatabase.DATABASE_NAME}").use { input ->
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
    return Room.databaseBuilder<HardwareDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
