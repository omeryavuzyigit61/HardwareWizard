package com.panda.myapplication.data.local

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

internal var appContext: Context? = null

fun setHardwareContext(context: Context) {
    appContext = context.applicationContext
}

actual val platformDatabaseModule: Module = module {
    single<FastSettingsStore> {
        val context: Context = runCatching { androidContext() }.getOrNull()
            ?: (appContext ?: error("Context is not initialized. Ensure HardwareWizardApp or setHardwareContext is used."))
        AndroidFastSettingsStore(context)
    }
    single<HardwareDatabase> {
        val context: Context = runCatching { androidContext() }.getOrNull()
            ?: (appContext ?: error("Context is not initialized. Ensure HardwareWizardApp or setHardwareContext is used."))
        buildHardwareDatabase(hardwareDatabaseBuilder(context), get())
    }
    single<com.panda.myapplication.domain.util.SecureStorage> {
        val context: Context = runCatching { androidContext() }.getOrNull()
            ?: (appContext ?: error("Context is not initialized. Ensure HardwareWizardApp or setHardwareContext is used."))
        AndroidSecureStorage(context)
    }
}
