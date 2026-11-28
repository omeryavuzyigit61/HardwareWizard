package com.panda.myapplication.domain.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryState
import platform.UIKit.UIScreen
import kotlin.math.round

@OptIn(ExperimentalForeignApi::class)
actual fun getMyDeviceDiagnostic(): MyDeviceDiagnostic {
    val device = UIDevice.currentDevice
    device.batteryMonitoringEnabled = true

    val rawModel = device.model
    val isIpad = rawModel.contains("iPad", ignoreCase = true)
    val systemVersion = device.systemVersion
    val friendlyModel = if (isIpad) {
        "iPad (iPadOS $systemVersion)"
    } else {
        "iPhone (iOS $systemVersion)"
    }

    val totalBytes = NSProcessInfo.processInfo.physicalMemory.toDouble()
    val rawRamGiB = totalBytes / (1024.0 * 1024.0 * 1024.0)
    val totalRamGb = when {
        rawRamGiB >= 14.5 -> 16.0
        rawRamGiB >= 10.8 -> 12.0
        rawRamGiB >= 7.2 -> 8.0
        rawRamGiB >= 5.2 -> 6.0
        rawRamGiB >= 3.4 -> 4.0
        else -> round(rawRamGiB * 10.0) / 10.0
    }

    val refreshHz = runCatching {
        UIScreen.mainScreen.maximumFramesPerSecond.toInt().coerceIn(60, 120)
    }.getOrDefault(60)

    val socLabel = when {
        isIpad && totalRamGb >= 8.0 && refreshHz >= 120 -> "Apple M-Series Silicon"
        isIpad -> "Apple Silicon / A-Series"
        totalRamGb >= 8.0 && refreshHz >= 120 -> "Apple A17 Pro / A18 Pro"
        totalRamGb >= 8.0 -> "Apple A18"
        refreshHz >= 120 -> "Apple A15 / A16 Pro Bionic"
        else -> "Apple Bionic"
    }

    val estimatedPercentile = when {
        totalRamGb >= 12.0 && refreshHz >= 120 -> 97
        totalRamGb >= 8.0 && refreshHz >= 120 -> 94
        totalRamGb >= 8.0 -> 86
        refreshHz >= 120 && totalRamGb >= 6.0 -> 84
        totalRamGb >= 6.0 -> 74
        else -> 58
    }

    val batteryLevel = device.batteryLevel
    val batteryPercent = if (batteryLevel < 0) 100 else (batteryLevel * 100).toInt().coerceIn(0, 100)
    val isCharging = device.batteryState == UIDeviceBatteryState.UIDeviceBatteryStateCharging ||
        device.batteryState == UIDeviceBatteryState.UIDeviceBatteryStateFull

    return MyDeviceDiagnostic(
        modelName = friendlyModel,
        manufacturer = "Apple",
        socName = socLabel,
        totalRamGb = totalRamGb,
        availableRamGb = 0.0,
        refreshRateHz = refreshHz,
        batteryPercent = batteryPercent,
        isCharging = isCharging,
        catalogPercentile = estimatedPercentile,
        matchedCatalogName = "Apple $friendlyModel",
        isAvailableRamMeasured = false,
    )
}
