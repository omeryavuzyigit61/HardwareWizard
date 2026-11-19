package com.panda.myapplication.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
fun hardwareDatabaseBuilder(): RoomDatabase.Builder<HardwareDatabase> {
    val dbPath = "${documentDirectory()}/${HardwareDatabase.DATABASE_NAME}"
    val fileManager = NSFileManager.defaultManager
    if (!fileManager.fileExistsAtPath(dbPath)) {
        val bundledPath = NSBundle.mainBundle.pathForResource(
            name = HardwareDatabase.DATABASE_NAME.removeSuffix(".db"),
            ofType = "db",
        )
        if (bundledPath != null) {
            runCatching {
                fileManager.copyItemAtPath(bundledPath, toPath = dbPath, error = null)
            }
        }
    }
    return Room.databaseBuilder<HardwareDatabase>(
        name = dbPath,
    )
}

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
