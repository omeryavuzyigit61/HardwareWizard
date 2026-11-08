package com.panda.myapplication

import androidx.compose.ui.window.ComposeUIViewController
import com.panda.myapplication.data.di.dataModule
import com.panda.myapplication.data.local.platformDatabaseModule
import com.panda.myapplication.presentation.di.presentationModule
import org.koin.core.context.startKoin

import org.koin.mp.KoinPlatformTools

fun initKoin() {
    startKoin {
        modules(dataModule, platformDatabaseModule, presentationModule)
    }
}

fun MainViewController() = ComposeUIViewController(
    configure = {
        if (KoinPlatformTools.defaultContext().getOrNull() == null) {
            initKoin()
        }
    }
) { App() }
