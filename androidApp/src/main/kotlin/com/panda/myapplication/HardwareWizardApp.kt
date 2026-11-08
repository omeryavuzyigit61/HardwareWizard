package com.panda.myapplication

import android.app.Application
import com.panda.myapplication.data.di.dataModule
import com.panda.myapplication.data.local.platformDatabaseModule
import com.panda.myapplication.presentation.di.presentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HardwareWizardApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HardwareWizardApp)
            modules(dataModule, platformDatabaseModule, presentationModule)
        }
    }
}
