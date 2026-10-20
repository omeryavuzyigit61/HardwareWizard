package com.panda.myapplication.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panda.myapplication.domain.model.AppCurrency
import com.panda.myapplication.domain.model.AppLanguage
import com.panda.myapplication.domain.model.AppSettings
import com.panda.myapplication.domain.model.FpsTarget
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.domain.model.ResolutionTarget
import com.panda.myapplication.domain.model.TemperatureUnit
import com.panda.myapplication.domain.model.ThemeMode
import com.panda.myapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun onThemeModeChanged(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(mode)
        }
    }

    fun onLanguageChanged(lang: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.updateLanguage(lang)
        }
    }

    fun onCurrencyChanged(curr: AppCurrency) {
        viewModelScope.launch {
            settingsRepository.updateCurrency(curr)
        }
    }

    fun onDataSaverToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDataSaver(enabled)
        }
    }

    fun onTemperatureUnitChanged(unit: TemperatureUnit) {
        viewModelScope.launch {
            settingsRepository.updateTemperatureUnit(unit)
        }
    }

    fun onLengthUnitChanged(unit: LengthUnit) {
        viewModelScope.launch {
            settingsRepository.updateLengthUnit(unit)
        }
    }

    fun onResolutionTargetChanged(target: ResolutionTarget) {
        viewModelScope.launch {
            settingsRepository.updateResolutionTarget(target)
        }
    }

    fun onFpsTargetChanged(target: FpsTarget) {
        viewModelScope.launch {
            settingsRepository.updateFpsTarget(target)
        }
    }

    fun onEnthusiastModeToggled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateEnthusiastMode(enabled)
        }
    }

    fun onClearCache(onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.clearCache()
            onDone()
        }
    }
}
