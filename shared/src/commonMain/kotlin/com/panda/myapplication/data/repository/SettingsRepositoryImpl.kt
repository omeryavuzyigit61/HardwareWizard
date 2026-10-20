package com.panda.myapplication.data.repository

import com.panda.myapplication.data.local.dao.HardwareDao
import com.panda.myapplication.data.local.entity.AppSettingEntity
import com.panda.myapplication.domain.model.AppCurrency
import com.panda.myapplication.domain.model.AppLanguage
import com.panda.myapplication.domain.model.AppSettings
import com.panda.myapplication.domain.model.FpsTarget
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.domain.model.ResolutionTarget
import com.panda.myapplication.domain.model.TemperatureUnit
import com.panda.myapplication.domain.model.ThemeMode
import com.panda.myapplication.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val hardwareDao: HardwareDao? = null,
    private val ioDispatcher: CoroutineDispatcher,
) : SettingsRepository {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private val _settings = MutableStateFlow(AppSettings())
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        loadInitialSettings()
    }

    private fun loadInitialSettings() {
        scope.launch {
            if (hardwareDao == null) return@launch
            try {
                val list = hardwareDao.getAllSettings()
                val map = list.associate { it.key to it.value }

                val loaded = AppSettings(
                    themeMode = ThemeMode.fromString(map[KEY_THEME_MODE]),
                    language = AppLanguage.fromCode(map[KEY_LANGUAGE]),
                    currency = AppCurrency.fromCode(map[KEY_CURRENCY]),
                    dataSaver = map[KEY_DATA_SAVER]?.toBooleanStrictOrNull() ?: false,
                    temperatureUnit = TemperatureUnit.fromString(map[KEY_TEMPERATURE_UNIT]),
                    lengthUnit = LengthUnit.fromString(map[KEY_LENGTH_UNIT]),
                    defaultResolution = ResolutionTarget.fromString(map[KEY_RESOLUTION]),
                    defaultFps = FpsTarget.fromString(map[KEY_FPS]),
                    enthusiastMode = map[KEY_ENTHUSIAST_MODE]?.toBooleanStrictOrNull() ?: true,
                )
                _settings.value = loaded
            } catch (_: Throwable) {
                // Keep default settings if loading fails
            }
        }
    }

    private fun persistSetting(key: String, value: String) {
        scope.launch {
            try {
                hardwareDao?.saveSetting(AppSettingEntity(key = key, value = value))
            } catch (_: Throwable) {}
        }
    }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        _settings.value = _settings.value.copy(themeMode = themeMode)
        persistSetting(KEY_THEME_MODE, themeMode.name)
    }

    override suspend fun updateLanguage(language: AppLanguage) {
        _settings.value = _settings.value.copy(language = language)
        persistSetting(KEY_LANGUAGE, language.code)
    }

    override suspend fun updateCurrency(currency: AppCurrency) {
        _settings.value = _settings.value.copy(currency = currency)
        persistSetting(KEY_CURRENCY, currency.code)
    }

    override suspend fun updateDataSaver(enabled: Boolean) {
        _settings.value = _settings.value.copy(dataSaver = enabled)
        persistSetting(KEY_DATA_SAVER, enabled.toString())
    }

    override suspend fun updateTemperatureUnit(unit: TemperatureUnit) {
        _settings.value = _settings.value.copy(temperatureUnit = unit)
        persistSetting(KEY_TEMPERATURE_UNIT, unit.name)
    }

    override suspend fun updateLengthUnit(unit: LengthUnit) {
        _settings.value = _settings.value.copy(lengthUnit = unit)
        persistSetting(KEY_LENGTH_UNIT, unit.name)
    }

    override suspend fun updateResolutionTarget(target: ResolutionTarget) {
        _settings.value = _settings.value.copy(defaultResolution = target)
        persistSetting(KEY_RESOLUTION, target.name)
    }

    override suspend fun updateFpsTarget(target: FpsTarget) {
        _settings.value = _settings.value.copy(defaultFps = target)
        persistSetting(KEY_FPS, target.name)
    }

    override suspend fun updateEnthusiastMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(enthusiastMode = enabled)
        persistSetting(KEY_ENTHUSIAST_MODE, enabled.toString())
    }

    override suspend fun clearCache(): Boolean = withContext(ioDispatcher) {
        // Cache clear acknowledgement
        true
    }

    companion object {
        private const val KEY_THEME_MODE = "setting_theme_mode"
        private const val KEY_LANGUAGE = "setting_language"
        private const val KEY_CURRENCY = "setting_currency"
        private const val KEY_DATA_SAVER = "setting_data_saver"
        private const val KEY_TEMPERATURE_UNIT = "setting_temperature_unit"
        private const val KEY_LENGTH_UNIT = "setting_length_unit"
        private const val KEY_RESOLUTION = "setting_resolution"
        private const val KEY_FPS = "setting_fps"
        private const val KEY_ENTHUSIAST_MODE = "setting_enthusiast_mode"
    }
}
