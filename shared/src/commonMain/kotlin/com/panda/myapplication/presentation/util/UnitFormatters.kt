package com.panda.myapplication.presentation.util

import androidx.compose.runtime.staticCompositionLocalOf
import com.panda.myapplication.domain.engine.ResolutionOption
import com.panda.myapplication.domain.model.AppSettings
import com.panda.myapplication.domain.model.FpsTarget
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.domain.model.ResolutionTarget
import com.panda.myapplication.domain.model.TemperatureUnit
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Provides reactive user preferences (`temperatureUnit`, `lengthUnit`, `defaultResolution`,
 * `defaultFps`, `dataSaver`, `enthusiastMode`, etc.) across all Compose screens.
 */
val LocalAppSettings = staticCompositionLocalOf { AppSettings() }

fun celsiusToFahrenheit(celsius: Int): Int = ((celsius * 9.0 / 5.0) + 32.0).roundToInt()

fun formatTemperature(celsius: Int, unit: TemperatureUnit): String = when (unit) {
    TemperatureUnit.CELSIUS -> "$celsius °C"
    TemperatureUnit.FAHRENHEIT -> "${celsiusToFahrenheit(celsius)} °F"
}

fun formatTemperatureCompact(celsius: Int, unit: TemperatureUnit): String = when (unit) {
    TemperatureUnit.CELSIUS -> "${celsius}°C"
    TemperatureUnit.FAHRENHEIT -> "${celsiusToFahrenheit(celsius)}°F"
}

fun formatLengthMm(mm: Int, unit: LengthUnit): String = when (unit) {
    LengthUnit.METRIC -> "${mm}mm"
    LengthUnit.IMPERIAL -> {
        val sign = if (mm < 0) "-" else ""
        val tenths = ((abs(mm) / 25.4) * 10.0).roundToInt()
        "$sign${tenths / 10}.${tenths % 10} in"
    }
}

fun formatLengthDeltaMm(marginMm: Int, unit: LengthUnit): String {
    val prefix = if (marginMm >= 0) "+" else ""
    return "$prefix${formatLengthMm(marginMm, unit)}"
}

fun ResolutionTarget.toResolutionOption(): ResolutionOption = when (this) {
    ResolutionTarget.FHD_1080P -> ResolutionOption.P1080
    ResolutionTarget.QHD_1440P -> ResolutionOption.P1440
    ResolutionTarget.UHD_4K -> ResolutionOption.P2160
}

fun ResolutionTarget.toGamingResolution(): GamingResolution = when (this) {
    ResolutionTarget.FHD_1080P -> GamingResolution.P1080
    ResolutionTarget.QHD_1440P -> GamingResolution.P1440
    ResolutionTarget.UHD_4K -> GamingResolution.P4K
}

fun FpsTarget.minFpsThreshold(): Int = when (this) {
    FpsTarget.FPS_60 -> 60
    FpsTarget.FPS_144 -> 144
    FpsTarget.FPS_240 -> 240
}

fun FpsTarget.meetsTarget(fps: Int): Boolean = fps >= minFpsThreshold()
