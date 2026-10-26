package com.panda.myapplication.domain.util

import androidx.compose.runtime.Immutable

@Immutable
data class MyDeviceDiagnostic(
    val modelName: String,
    val manufacturer: String,
    val socName: String,
    val totalRamGb: Double,
    val availableRamGb: Double,
    val refreshRateHz: Int,
    val batteryPercent: Int,
    val isCharging: Boolean,
    val catalogPercentile: Int = 92,
    val matchedCatalogName: String? = null,
)

expect fun getMyDeviceDiagnostic(): MyDeviceDiagnostic

expect fun compressProofImageToWebp(rawBytes: ByteArray, maxDimension: Int = 1080, targetQuality: Int = 78): ByteArray
