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
    @ColumnInfo(name = "sensor") val sensor: String = "",
    @ColumnInfo(name = "max_dpi") val maxDpi: Int = 0,
    @ColumnInfo(name = "polling_rate_hz") val pollingRateHz: Int = 0,
    @ColumnInfo(name = "switch_type") val switchType: String = "",
    @ColumnInfo(name = "weight_g") val weightG: Int = 0,
    @ColumnInfo(name = "connectivity") val connectivity: String = "",
    @ColumnInfo(name = "battery_hours") val batteryHours: Int? = null,
    @ColumnInfo(name = "mcu") val mcu: String = "",
    @ColumnInfo(name = "click_latency_ms") val clickLatencyMs: Double = 0.0,
    @ColumnInfo(name = "shape_type") val shapeType: String = "",
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
    @ColumnInfo(name = "switch_type") val switchType: String = "",
    @ColumnInfo(name = "rapid_trigger_supported") val rapidTriggerSupported: Boolean = false,
    @ColumnInfo(name = "min_actuation_mm") val minActuationMm: Double = 0.0,
    @ColumnInfo(name = "polling_rate_hz") val pollingRateHz: Int = 0,
    @ColumnInfo(name = "layout") val layout: String = "",
    @ColumnInfo(name = "hot_swap") val hotSwap: Boolean = false,
    @ColumnInfo(name = "connectivity") val connectivity: String = "",
    @ColumnInfo(name = "max_actuation_mm") val maxActuationMm: Double = 0.0,
    @ColumnInfo(name = "case_material") val caseMaterial: String = "",
    @ColumnInfo(name = "is_hall_effect") val isHallEffect: Boolean = false,
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
    @ColumnInfo(name = "driver_type") val driverType: String = "",
    @ColumnInfo(name = "driver_size_mm") val driverSizeMm: Int = 0,
    @ColumnInfo(name = "impedance_ohm") val impedanceOhm: Int = 0,
    @ColumnInfo(name = "frequency_response") val frequencyResponse: String = "",
    @ColumnInfo(name = "dac_amp_required") val dacAmpRequired: Boolean = false,
    @ColumnInfo(name = "mic_type") val micType: String = "",
    @ColumnInfo(name = "weight_g") val weightG: Int = 0,
    @ColumnInfo(name = "connectivity") val connectivity: String = "",
    @ColumnInfo(name = "sound_isolation") val soundIsolation: String = "",
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
    @ColumnInfo(name = "panel_type") val panelType: String = "",
    @ColumnInfo(name = "screen_size_inch") val screenSizeInch: Double = 0.0,
    @ColumnInfo(name = "resolution") val resolution: String = "",
    @ColumnInfo(name = "refresh_rate_hz") val refreshRateHz: Int = 0,
    @ColumnInfo(name = "response_speed_gtg_ms") val responseSpeedGtGMs: Double = 0.0,
    @ColumnInfo(name = "ppi") val ppi: Int = 0,
    @ColumnInfo(name = "hdr_certification") val hdrCertification: String = "",
    @ColumnInfo(name = "bit_depth") val bitDepth: String = "",
    @ColumnInfo(name = "response_mprt_ms") val responseMprtMs: Double? = null,
    @ColumnInfo(name = "brightness_sdr_nit") val brightnessSdrNit: Int = 0,
    @ColumnInfo(name = "brightness_hdr_nit") val brightnessHdrNit: Int = 0,
    @ColumnInfo(name = "color_gamut") val colorGamut: String = "",
    @ColumnInfo(name = "vrr_support") val vrrSupport: String = "",
)

@Entity(
    tableName = "microphone_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MicrophoneSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "capsule_type") val capsuleType: String = "",
    @ColumnInfo(name = "polar_pattern") val polarPattern: String = "",
    @ColumnInfo(name = "sample_rate_and_bit") val sampleRateAndBit: String = "",
    @ColumnInfo(name = "frequency_response") val frequencyResponse: String = "",
    @ColumnInfo(name = "connectivity") val connectivity: String = "",
    @ColumnInfo(name = "max_spl_db") val maxSplDb: Int = 0,
    @ColumnInfo(name = "has_built_in_dsp") val hasBuiltInDsp: Boolean = false,
    @ColumnInfo(name = "requires_phantom_power") val requiresPhantomPower: Boolean = false,
    @ColumnInfo(name = "has_zero_latency_monitoring") val hasZeroLatencyMonitoring: Boolean = false,
    @ColumnInfo(name = "has_pop_filter") val hasPopFilter: Boolean = false,
)

@Entity(
    tableName = "mousepad_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class MousepadSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "surface_type") val surfaceType: String = "",
    @ColumnInfo(name = "speed_glide_tier") val speedGlideTier: String = "",
    @ColumnInfo(name = "base_material") val baseMaterial: String = "",
    @ColumnInfo(name = "dimensions_mm") val dimensionsMm: String = "",
    @ColumnInfo(name = "thickness_mm") val thicknessMm: Double = 0.0,
    @ColumnInfo(name = "stitched_edges") val stitchedEdges: Boolean = false,
    @ColumnInfo(name = "stopping_power_score") val stoppingPowerScore: Int = 0,
    @ColumnInfo(name = "humidity_resistance") val humidityResistance: String = "",
)

@Entity(
    tableName = "webcam_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class WebcamSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "max_resolution") val maxResolution: String = "",
    @ColumnInfo(name = "max_frame_rate_fps") val maxFrameRateFps: Int = 0,
    @ColumnInfo(name = "sensor_model") val sensorModel: String = "",
    @ColumnInfo(name = "sensor_size_inch") val sensorSizeInch: String = "",
    @ColumnInfo(name = "aperture") val aperture: String = "",
    @ColumnInfo(name = "field_of_view_deg") val fieldOfViewDeg: Int = 0,
    @ColumnInfo(name = "focus_type") val focusType: String = "",
    @ColumnInfo(name = "has_ai_tracking") val hasAiTracking: Boolean = false,
    @ColumnInfo(name = "has_hardware_hdr") val hasHardwareHdr: Boolean = false,
    @ColumnInfo(name = "connection_type") val connectionType: String = "",
    @ColumnInfo(name = "privacy_shutter") val privacyShutter: Boolean = false,
)

@Entity(
    tableName = "gaming_chair_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class GamingChairSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "chair_type") val chairType: String = "",
    @ColumnInfo(name = "upholstery_material") val upholsteryMaterial: String = "",
    @ColumnInfo(name = "lumbar_support_type") val lumbarSupportType: String = "",
    @ColumnInfo(name = "armrest_type") val armrestType: String = "",
    @ColumnInfo(name = "gas_lift_class") val gasLiftClass: String = "",
    @ColumnInfo(name = "max_weight_capacity_kg") val maxWeightCapacityKg: Int = 0,
    @ColumnInfo(name = "recommended_height_range") val recommendedHeightRange: String = "",
    @ColumnInfo(name = "recline_angle_deg") val reclineAngleDeg: Int = 0,
    @ColumnInfo(name = "tilt_mechanism") val tiltMechanism: String = "",
)

@Entity(
    tableName = "case_fan_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CaseFanSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "fan_size_mm") val fanSizeMm: Int = 0,
    @ColumnInfo(name = "min_rpm") val minRpm: Int = 0,
    @ColumnInfo(name = "max_rpm") val maxRpm: Int = 0,
    @ColumnInfo(name = "max_airflow_cfm") val maxAirflowCfm: Double = 0.0,
    @ColumnInfo(name = "static_pressure_mmh2o") val staticPressureMmH2o: Double = 0.0,
    @ColumnInfo(name = "noise_level_dba") val noiseLevelDba: Double = 0.0,
    @ColumnInfo(name = "bearing_type") val bearingType: String = "",
    @ColumnInfo(name = "pwm_pins") val pwmPins: Int = 0,
    @ColumnInfo(name = "is_daisy_chain") val isDaisyChain: Boolean = false,
    @ColumnInfo(name = "argb_type") val argbType: String = "",
)

@Entity(
    tableName = "ups_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class UpsSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "va_rating") val vaRating: Int = 0,
    @ColumnInfo(name = "watt_capacity") val wattCapacity: Int = 0,
    @ColumnInfo(name = "topology") val topology: String = "",
    @ColumnInfo(name = "sinewave_type") val sinewaveType: String = "",
    @ColumnInfo(name = "battery_specs") val batterySpecs: String = "",
    @ColumnInfo(name = "runtime_min_full_load") val runtimeMinFullLoad: Int = 0,
    @ColumnInfo(name = "runtime_min_half_load") val runtimeMinHalfLoad: Int = 0,
    @ColumnInfo(name = "outlet_count") val outletCount: Int = 0,
    @ColumnInfo(name = "has_usb_interface") val hasUsbInterface: Boolean = false,
    @ColumnInfo(name = "surge_protection_joules") val surgeProtectionJoules: Int = 0,
)

@Entity(
    tableName = "gaming_desk_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class GamingDeskSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "desk_width_cm") val deskWidthCm: Int = 0,
    @ColumnInfo(name = "desk_depth_cm") val deskDepthCm: Int = 0,
    @ColumnInfo(name = "is_motorized_height") val isMotorizedHeight: Boolean = false,
    @ColumnInfo(name = "min_height_cm") val minHeightCm: Int = 0,
    @ColumnInfo(name = "max_height_cm") val maxHeightCm: Int = 0,
    @ColumnInfo(name = "max_load_kg") val maxLoadKg: Int = 0,
    @ColumnInfo(name = "surface_finish") val surfaceFinish: String = "",
    @ColumnInfo(name = "has_cable_management") val hasCableManagement: Boolean = false,
    @ColumnInfo(name = "integrated_mousepad") val integratedMousepad: Boolean = false,
)

@Entity(
    tableName = "gaming_lighting_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class GamingLightingSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "lighting_form_factor") val formFactor: String = "",
    @ColumnInfo(name = "max_lumens") val maxLumens: Int = 0,
    @ColumnInfo(name = "color_temp_kelvin") val colorTempKelvin: String = "",
    @ColumnInfo(name = "cri_score") val criScore: Int = 0,
    @ColumnInfo(name = "has_screen_mirroring") val hasScreenMirroring: Boolean = false,
    @ColumnInfo(name = "supported_ecosystems") val supportedEcosystems: String = "",
    @ColumnInfo(name = "connection_type") val connectionType: String = "",
    @ColumnInfo(name = "power_source") val powerSource: String = "",
)

@Entity(
    tableName = "smartwatch_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class SmartwatchSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "screen_size_inch") val screenSizeInch: Double = 0.0,
    @ColumnInfo(name = "screen_panel") val screenPanel: String = "",
    @ColumnInfo(name = "peak_brightness_nits") val peakBrightnessNits: Int = 0,
    @ColumnInfo(name = "battery_life_days") val batteryLifeDays: Int = 0,
    @ColumnInfo(name = "water_resistance_atm") val waterResistanceAtm: Int = 0,
    @ColumnInfo(name = "os_name") val osName: String = "",
    @ColumnInfo(name = "sensors_list") val sensorsList: String = "",
    @ColumnInfo(name = "has_cellular_lte") val hasCellularLte: Boolean = false,
    @ColumnInfo(name = "case_material") val caseMaterial: String = "",
)

@Entity(
    tableName = "router_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RouterSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "wifi_standard") val wifiStandard: String = "",
    @ColumnInfo(name = "speed_class") val speedClass: String = "",
    @ColumnInfo(name = "bands") val bands: String = "",
    @ColumnInfo(name = "ethernet_ports") val ethernetPorts: String = "",
    @ColumnInfo(name = "has_gaming_qos") val hasGamingQos: Boolean = false,
    @ColumnInfo(name = "has_sqm_bufferbloat") val hasSqmBufferbloat: Boolean = false,
    @ColumnInfo(name = "processor_and_ram") val processorAndRam: String = "",
    @ColumnInfo(name = "usb_sharing_ports") val usbSharingPorts: String = "",
)

@Entity(
    tableName = "modem_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class ModemSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "vdsl_profile") val vdslProfile: String = "",
    @ColumnInfo(name = "fiber_wan_support") val fiberWanSupport: Boolean = false,
    @ColumnInfo(name = "wifi_standard") val wifiStandard: String = "",
    @ColumnInfo(name = "lan_ports") val lanPorts: String = "",
    @ColumnInfo(name = "voip_ports") val voipPorts: String = "",
    @ColumnInfo(name = "chipset") val chipset: String = "",
)

@Entity(
    tableName = "access_point_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class AccessPointSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "form_factor") val formFactor: String = "",
    @ColumnInfo(name = "wifi_standard") val wifiStandard: String = "",
    @ColumnInfo(name = "poe_support") val poeSupport: String = "",
    @ColumnInfo(name = "max_clients") val maxClients: Int = 0,
    @ColumnInfo(name = "mu_mimo_streams") val muMimoStreams: String = "",
    @ColumnInfo(name = "uplink_port") val uplinkPort: String = "",
)

@Entity(
    tableName = "range_extender_specs",
    foreignKeys = [
        ForeignKey(
            entity = HardwareItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["hardware_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RangeExtenderSpecEntity(
    @PrimaryKey @ColumnInfo(name = "hardware_id") val hardwareId: String,
    @ColumnInfo(name = "extender_type") val extenderType: String = "",
    @ColumnInfo(name = "wifi_standard") val wifiStandard: String = "",
    @ColumnInfo(name = "dedicated_backhaul") val dedicatedBackhaul: String = "",
    @ColumnInfo(name = "mesh_protocol") val meshProtocol: String = "",
    @ColumnInfo(name = "ethernet_ports") val ethernetPorts: String = "",
    @ColumnInfo(name = "coverage_area_m2") val coverageAreaM2: Int = 0,
)
