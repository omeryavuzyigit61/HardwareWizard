package com.panda.myapplication.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun hardwareDatabaseBuilder(): RoomDatabase.Builder<HardwareDatabase> =
    Room.databaseBuilder<HardwareDatabase>(
        name = "${documentDirectory()}/${HardwareDatabase.DATABASE_NAME}",
    )

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )
    return requireNotNull(url?.path) { "Unable to resolve iOS document directory." }
}
