package com.panda.myapplication.domain.util

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.view.Display
import org.koin.mp.KoinPlatformTools
import java.io.File
import kotlin.math.round
import kotlin.math.roundToInt

actual fun getMyDeviceDiagnostic(): MyDeviceDiagnostic {
    val manufacturer = Build.MANUFACTURER.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val rawModel = Build.MODEL
    val hardware = Build.HARDWARE

    val friendlyModel = resolveFriendlyModelName(rawModel)
    val socName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val soc = Build.SOC_MODEL
        resolveFriendlySocName(soc, hardware, rawModel)
    } else {
        resolveFriendlySocName("", hardware, rawModel)
    }

    val appContext: Context? = runCatching {
        KoinPlatformTools.defaultContext().getOrNull()?.getOrNull<Context>()
    }.getOrNull()

    val (totalRamGb, freeRamGb) = readRealMemoryGb(appContext)
    val (batteryPercent, isCharging) = readRealBatteryInfo(appContext)
    val refreshRateHz = readRealRefreshRateHz(appContext)

    return MyDeviceDiagnostic(
        modelName = friendlyModel,
        manufacturer = manufacturer,
        socName = socName,
        totalRamGb = totalRamGb,
        availableRamGb = freeRamGb,
        refreshRateHz = refreshRateHz,
        batteryPercent = batteryPercent,
        isCharging = isCharging,
        catalogPercentile = estimateDevicePercentile(totalRamGb, refreshRateHz),
        matchedCatalogName = "$manufacturer $friendlyModel",
    )
}

private fun readRealMemoryGb(context: Context?): Pair<Double, Double> {
    // 1. Primary: Android ActivityManager.getMemoryInfo (zero permissions, real physical RAM)
    if (context != null) {
        runCatching {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            if (am != null) {
                val info = ActivityManager.MemoryInfo()
                am.getMemoryInfo(info)
                if (info.totalMem > 0L) {
                    val bytesPerGb = 1024.0 * 1024.0 * 1024.0
                    val totalGb = round((info.totalMem / bytesPerGb) * 10.0) / 10.0
                    val availGb = round((info.availMem / bytesPerGb) * 10.0) / 10.0
                    return totalGb to availGb.coerceAtLeast(0.0)
                }
            }
        }
    }
    // 2. Fallback: /proc/meminfo (works on Linux/Android even without Context)
    runCatching {
        var totalKb = 0L
        var availKb = 0L
        File("/proc/meminfo").useLines { lines ->
            lines.forEach { line ->
                when {
                    line.startsWith("MemTotal:") -> totalKb = line.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    line.startsWith("MemAvailable:") -> availKb = line.filter { it.isDigit() }.toLongOrNull() ?: 0L
                }
            }
        }
        if (totalKb > 0L) {
            val kbPerGb = 1024.0 * 1024.0
            val totalGb = round((totalKb / kbPerGb) * 10.0) / 10.0
            val freeGb = round((availKb / kbPerGb) * 10.0) / 10.0
            return totalGb to freeGb.coerceAtLeast(0.0)
        }
    }
    return 8.0 to 3.6
}

private fun readRealBatteryInfo(context: Context?): Pair<Int, Boolean> {
    if (context != null) {
        runCatching {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val cap = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val chargingFromBm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                bm?.isCharging
            } else null

            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val intentPct = if (level >= 0 && scale > 0) ((level * 100f) / scale).roundToInt() else null
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val chargingFromIntent = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

            val finalPct = (cap?.takeIf { it in 1..100 } ?: intentPct ?: 100).coerceIn(0, 100)
            val finalCharging = chargingFromBm ?: chargingFromIntent
            return finalPct to finalCharging
        }
    }
    return 100 to false
}

private fun readRealRefreshRateHz(context: Context?): Int {
    if (context != null) {
        runCatching {
            val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
            val hz = dm?.getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate?.roundToInt()
            if (hz != null && hz in 30..240) return hz
        }
    }
    return 60
}

private fun estimateDevicePercentile(totalRamGb: Double, refreshRateHz: Int): Int {
    // Account for OS/modem reserved carveouts (8GB physical RAM reports ~7.3-7.6 GiB in MemTotal)
    val marketedRamGb = when {
        totalRamGb >= 14.2 -> 16
        totalRamGb >= 10.4 -> 12
        totalRamGb >= 7.0 -> 8
        totalRamGb >= 5.1 -> 6
        else -> 4
    }
    return when {
        marketedRamGb >= 16 && refreshRateHz >= 120 -> 97
        marketedRamGb >= 12 && refreshRateHz >= 120 -> 93
        marketedRamGb >= 8 && refreshRateHz >= 120 -> 86
        marketedRamGb >= 8 -> 78
        marketedRamGb >= 6 -> 66
        else -> 52
    }
}

private fun resolveFriendlyModelName(rawModel: String): String {
    val upper = rawModel.uppercase().trim()
    return when {
        upper.startsWith("SM-S938") -> "Galaxy S25 Ultra ($rawModel)"
        upper.startsWith("SM-S936") -> "Galaxy S25+ ($rawModel)"
        upper.startsWith("SM-S931") -> "Galaxy S25 ($rawModel)"
        upper.startsWith("SM-S721") -> "Galaxy S24 FE ($rawModel)"
        upper.startsWith("SM-S921") -> "Galaxy S24 ($rawModel)"
        upper.startsWith("SM-S926") -> "Galaxy S24+ ($rawModel)"
        upper.startsWith("SM-S928") -> "Galaxy S24 Ultra ($rawModel)"
        upper.startsWith("SM-S711") -> "Galaxy S23 FE ($rawModel)"
        upper.startsWith("SM-S911") -> "Galaxy S23 ($rawModel)"
        upper.startsWith("SM-S916") -> "Galaxy S23+ ($rawModel)"
        upper.startsWith("SM-S918") -> "Galaxy S23 Ultra ($rawModel)"
        upper.startsWith("SM-S901") -> "Galaxy S22 ($rawModel)"
        upper.startsWith("SM-S906") -> "Galaxy S22+ ($rawModel)"
        upper.startsWith("SM-S908") -> "Galaxy S22 Ultra ($rawModel)"
        upper.startsWith("SM-A556") -> "Galaxy A55 ($rawModel)"
        upper.startsWith("SM-A546") -> "Galaxy A54 ($rawModel)"
        upper.startsWith("SM-A356") -> "Galaxy A35 ($rawModel)"
        upper.startsWith("SM-A346") -> "Galaxy A34 ($rawModel)"
        upper.startsWith("SM-F946") -> "Galaxy Z Fold5 ($rawModel)"
        upper.startsWith("SM-F956") -> "Galaxy Z Fold6 ($rawModel)"
        upper.startsWith("SM-F731") -> "Galaxy Z Flip5 ($rawModel)"
        upper.startsWith("SM-F741") -> "Galaxy Z Flip6 ($rawModel)"
        else -> rawModel
    }
}

private fun resolveFriendlySocName(rawSoc: String, hardware: String, rawModel: String = ""): String {
    val soc = rawSoc.lowercase().trim()
    val hw = hardware.lowercase().trim()
    val modelUpper = rawModel.uppercase().trim()
    return when {
        soc == "s5e9955" || hw.contains("s5e9955") -> "Exynos 2500"
        soc == "s5e9945" || hw.contains("s5e9945") -> if (modelUpper.startsWith("SM-S721")) "Exynos 2400e" else "Exynos 2400"
        soc == "s5e9935" || hw.contains("s5e9935") -> "Exynos 2400"
        soc == "s5e9925" || hw.contains("s5e9925") -> "Exynos 2200"
        soc == "s5e9840" || hw.contains("s5e9840") -> "Exynos 2100"
        soc == "s5e9830" || hw.contains("s5e9830") -> "Exynos 990"
        soc == "s5e8845" || hw.contains("s5e8845") -> "Exynos 1480"
        soc == "s5e8835" || hw.contains("s5e8835") -> "Exynos 1380"
        soc == "s5e8535" || hw.contains("s5e8535") -> "Exynos 1280"
        soc == "sm8750" || hw.contains("sm8750") -> "Snapdragon 8 Elite"
        soc == "sm8650" || hw.contains("sm8650") -> "Snapdragon 8 Gen 3"
        soc == "sm8550" || hw.contains("sm8550") -> "Snapdragon 8 Gen 2"
        soc == "sm8450" || hw.contains("sm8450") -> "Snapdragon 8 Gen 1"
        soc == "sm8475" || hw.contains("sm8475") -> "Snapdragon 8+ Gen 1"
        soc == "sm8350" || hw.contains("sm8350") -> "Snapdragon 888"
        soc == "sm8250" || hw.contains("sm8250") -> "Snapdragon 865"
        soc == "sm7675" || hw.contains("sm7675") -> "Snapdragon 7+ Gen 3"
        soc == "sm7550" || hw.contains("sm7550") -> "Snapdragon 7 Gen 3"
        soc == "sm7475" || hw.contains("sm7475") -> "Snapdragon 7+ Gen 2"
        soc == "mt6991" || hw.contains("mt6991") -> "Dimensity 9400"
        soc == "mt6989" || hw.contains("mt6989") -> "Dimensity 9300"
        soc == "mt6985" || hw.contains("mt6985") -> "Dimensity 9200"
        soc.isNotBlank() && soc != "unknown" -> rawSoc
        hw.isNotBlank() && hw != "unknown" -> "Platform ($hardware)"
        else -> "Snapdragon / Dimensity"
    }
}
