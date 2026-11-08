package com.panda.myapplication.data.repository

import com.panda.myapplication.data.local.FastSettingsStore
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

internal class SettingsRepositoryImpl(
    private val hardwareDao: HardwareDao? = null,
    private val ioDispatcher: CoroutineDispatcher,
    private val fastSettingsStore: FastSettingsStore? = null,
) : SettingsRepository {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // Synchronously read initial values from fast platform storage (instantaneous on startup)
    private val initialTheme = fastSettingsStore?.getString(KEY_THEME_MODE)?.let { ThemeMode.fromString(it) } ?: ThemeMode.DARK
    private val initialLang = fastSettingsStore?.getString(KEY_LANGUAGE)?.let { AppLanguage.fromCode(it) } ?: AppLanguage.TR
    private val initialCurr = fastSettingsStore?.getString(KEY_CURRENCY)?.let { AppCurrency.fromCode(it) } ?: AppCurrency.TRY

    private val _settings = MutableStateFlow(
        AppSettings(
            themeMode = initialTheme,
            language = initialLang,
            currency = initialCurr,
        )
    )
    override val settings: StateFlow<AppSettings> = _settings.asStateFlow()
    private val _isLoaded = MutableStateFlow(false)
    override val isLoaded: StateFlow<Boolean> = _isLoaded.asStateFlow()

    init {
        loadInitialSettings()
    }

    private fun loadInitialSettings() {
        scope.launch {
            if (hardwareDao == null) {
                _isLoaded.value = true
                return@launch
            }
            try {
                val list = hardwareDao.getAllSettings()
                val map = list.associate { it.key to it.value }

                val themeStr = map[KEY_THEME_MODE] ?: fastSettingsStore?.getString(KEY_THEME_MODE)
                val langStr = map[KEY_LANGUAGE] ?: fastSettingsStore?.getString(KEY_LANGUAGE)
                val currStr = map[KEY_CURRENCY] ?: fastSettingsStore?.getString(KEY_CURRENCY)

                val loaded = AppSettings(
                    themeMode = ThemeMode.fromString(themeStr),
                    language = AppLanguage.fromCode(langStr),
                    currency = AppCurrency.fromCode(currStr),
                    dataSaver = map[KEY_DATA_SAVER]?.toBooleanStrictOrNull() ?: false,
                    temperatureUnit = TemperatureUnit.fromString(map[KEY_TEMPERATURE_UNIT]),
                    lengthUnit = LengthUnit.fromString(map[KEY_LENGTH_UNIT]),
                    defaultResolution = ResolutionTarget.fromString(map[KEY_RESOLUTION]),
                    defaultFps = FpsTarget.fromString(map[KEY_FPS]),
                    enthusiastMode = map[KEY_ENTHUSIAST_MODE]?.toBooleanStrictOrNull() ?: true,
                )
                _settings.value = loaded

                // Keep fast settings store in sync
                map[KEY_THEME_MODE]?.let { fastSettingsStore?.setString(KEY_THEME_MODE, it) }
                map[KEY_LANGUAGE]?.let { fastSettingsStore?.setString(KEY_LANGUAGE, it) }
                map[KEY_CURRENCY]?.let { fastSettingsStore?.setString(KEY_CURRENCY, it) }
            } catch (e: Throwable) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                // Keep default/fast settings if loading fails
            } finally {
                _isLoaded.value = true
            }
        }
    }

    private fun persistSetting(key: String, value: String) {
        fastSettingsStore?.setString(key, value)
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
