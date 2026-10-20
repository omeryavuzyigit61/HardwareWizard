package com.panda.myapplication.presentation.di

import com.panda.myapplication.presentation.hardware_detail.HardwareDetailViewModel
import com.panda.myapplication.presentation.hardware_list.HardwareListViewModel
import com.panda.myapplication.presentation.settings.SettingsViewModel
import com.panda.myapplication.presentation.vs_arena.VsArenaViewModel
import com.panda.myapplication.presentation.wizard.WizardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::HardwareListViewModel)
    viewModelOf(::HardwareDetailViewModel)
    viewModelOf(::WizardViewModel)
    viewModelOf(::VsArenaViewModel)
    viewModelOf(::SettingsViewModel)
}
