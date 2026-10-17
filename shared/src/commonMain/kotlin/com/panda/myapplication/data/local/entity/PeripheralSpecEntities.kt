package com.panda.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "mouse_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MouseSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "sensor") val sensor: String,
    @ColumnInfo(name = "max_dpi") val maxDpi: Int,
    @ColumnInfo(name = "polling_rate_hz") val pollingRateHz: Int,
    @ColumnInfo(name = "switch_type") val switchType: String,
    @ColumnInfo(name = "weight_g") val weightG: Int,
    @ColumnInfo(name = "connectivity") val connectivity: String,
    @ColumnInfo(name = "battery_hours") val batteryHours: Int?,
)

@Entity(
    tableName = "keyboard_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class KeyboardSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "switch_type") val switchType: String,
    @ColumnInfo(name = "rapid_trigger_supported") val rapidTriggerSupported: Boolean,
    @ColumnInfo(name = "min_actuation_mm") val minActuationMm: Double,
    @ColumnInfo(name = "polling_rate_hz") val pollingRateHz: Int,
    @ColumnInfo(name = "layout") val layout: String,
    @ColumnInfo(name = "hot_swap") val hotSwap: Boolean,
    @ColumnInfo(name = "connectivity") val connectivity: String,
)

@Entity(
    tableName = "headset_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class HeadsetSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "driver_type") val driverType: String,
    @ColumnInfo(name = "driver_size_mm") val driverSizeMm: Int,
    @ColumnInfo(name = "impedance_ohm") val impedanceOhm: Int,
    @ColumnInfo(name = "frequency_response") val frequencyResponse: String,
    @ColumnInfo(name = "dac_amp_required") val dacAmpRequired: Boolean,
    @ColumnInfo(name = "mic_type") val micType: String,
    @ColumnInfo(name = "weight_g") val weightG: Int,
)

@Entity(
    tableName = "monitor_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MonitorSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "panel_type") val panelType: String,
    @ColumnInfo(name = "screen_size_inch") val screenSizeInch: Double,
    @ColumnInfo(name = "resolution") val resolution: String,
    @ColumnInfo(name = "refresh_rate_hz") val refreshRateHz: Int,
    @ColumnInfo(name = "response_speed_gtg_ms") val responseSpeedGtGMs: Double,
    @ColumnInfo(name = "ppi") val ppi: Int,
    @ColumnInfo(name = "hdr_certification") val hdrCertification: String,
)
