package com.panda.myapplication.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun hardwareDatabaseBuilder(context: Context): RoomDatabase.Builder<HardwareDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath(HardwareDatabase.DATABASE_NAME)
    return Room.databaseBuilder<HardwareDatabase>(
        context = appContext,
        name = dbFile.absolutePath,
    )
}
