package com.panda.myapplication.domain.repository

import com.panda.myapplication.domain.model.AppCurrency
import com.panda.myapplication.domain.model.AppLanguage
import com.panda.myapplication.domain.model.AppSettings
import com.panda.myapplication.domain.model.FpsTarget
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.domain.model.ResolutionTarget
import com.panda.myapplication.domain.model.TemperatureUnit
import com.panda.myapplication.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<AppSettings>

    suspend fun updateThemeMode(themeMode: ThemeMode)
    suspend fun updateLanguage(language: AppLanguage)
    suspend fun updateCurrency(currency: AppCurrency)
    suspend fun updateDataSaver(enabled: Boolean)
    suspend fun updateTemperatureUnit(unit: TemperatureUnit)
    suspend fun updateLengthUnit(unit: LengthUnit)
    suspend fun updateResolutionTarget(target: ResolutionTarget)
    suspend fun updateFpsTarget(target: FpsTarget)
    suspend fun updateEnthusiastMode(enabled: Boolean)
    suspend fun clearCache(): Boolean
}
