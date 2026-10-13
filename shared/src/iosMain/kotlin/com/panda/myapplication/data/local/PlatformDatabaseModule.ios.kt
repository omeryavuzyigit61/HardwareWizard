package com.panda.myapplication.data.local

import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformDatabaseModule: Module = module {
    single<HardwareDatabase> {
        buildHardwareDatabase(hardwareDatabaseBuilder(), get())
    }
}
