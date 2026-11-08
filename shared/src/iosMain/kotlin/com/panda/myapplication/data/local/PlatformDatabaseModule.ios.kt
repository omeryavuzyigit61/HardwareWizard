package com.panda.myapplication.data.local

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    single<FastSettingsStore> {
        IosFastSettingsStore()
    }
    single<HardwareDatabase> {
        buildHardwareDatabase(hardwareDatabaseBuilder(), get())
    }
    single<com.panda.myapplication.domain.util.SecureStorage> {
        IosSecureStorage()
    }
}
