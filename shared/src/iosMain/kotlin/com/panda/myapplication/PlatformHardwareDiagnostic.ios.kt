package com.panda.myapplication.domain.util

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSProcessInfo
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceBatteryState
import kotlin.math.round

@OptIn(ExperimentalForeignApi::class)
actual fun getMyDeviceDiagnostic(): MyDeviceDiagnostic {
    val device = UIDevice.currentDevice
    device.batteryMonitoringEnabled = true

    val rawModel = device.model
    val systemVersion = device.systemVersion
    val friendlyModel = if (rawModel.contains("iPad", ignoreCase = true)) {
        "Apple iPad (iPadOS $systemVersion)"
    } else {
        "Apple iPhone (iOS $systemVersion)"
    }

    val totalBytes = NSProcessInfo.processInfo.physicalMemory.toDouble()
    val totalRamGb = (totalBytes / (1024.0 * 1024.0 * 1024.0)).let { round(it * 10) / 10.0 }
    val freeRamGb = (totalRamGb * 0.40).let { round(it * 10) / 10.0 }

    val batteryLevel = device.batteryLevel
    val batteryPercent = if (batteryLevel < 0) 100 else (batteryLevel * 100).toInt().coerceIn(0, 100)
    val isCharging = device.batteryState == UIDeviceBatteryState.UIDeviceBatteryStateCharging ||
        device.batteryState == UIDeviceBatteryState.UIDeviceBatteryStateFull

    return MyDeviceDiagnostic(
        modelName = friendlyModel,
        manufacturer = "Apple",
        socName = "Apple Bionic / Silicon",
        totalRamGb = totalRamGb,
        availableRamGb = freeRamGb,
        refreshRateHz = 120,
        batteryPercent = batteryPercent,
        isCharging = isCharging,
        catalogPercentile = 96,
        matchedCatalogName = friendlyModel,
    )
}
