package com.panda.myapplication.data.local

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import org.koin.core.module.Module
import org.koin.dsl.module

internal var appContext: Context? = null

fun setHardwareContext(context: Context) {
    appContext = context.applicationContext
}

class HardwareContextProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        appContext = context?.applicationContext
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}

actual val platformDatabaseModule: Module = module {
    single<HardwareDatabase> {
        val context = appContext ?: error("Context is not initialized. Ensure HardwareContextProvider is active or call setHardwareContext(context).")
        buildHardwareDatabase(hardwareDatabaseBuilder(context), get())
    }
}
