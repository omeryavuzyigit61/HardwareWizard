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
    @ColumnInfo(name = "mcu") val mcu: String = "Nordic 52840",
    @ColumnInfo(name = "click_latency_ms") val clickLatencyMs: Double = 0.8,
    @ColumnInfo(name = "shape_type") val shapeType: String = "Simetrik",
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
    @ColumnInfo(name = "max_actuation_mm") val maxActuationMm: Double = 4.0,
    @ColumnInfo(name = "case_material") val caseMaterial: String = "Alüminyum CNC / ABS",
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
    @ColumnInfo(name = "driver_type") val driverType: String,
    @ColumnInfo(name = "driver_size_mm") val driverSizeMm: Int,
    @ColumnInfo(name = "impedance_ohm") val impedanceOhm: Int,
    @ColumnInfo(name = "frequency_response") val frequencyResponse: String,
    @ColumnInfo(name = "dac_amp_required") val dacAmpRequired: Boolean,
    @ColumnInfo(name = "mic_type") val micType: String,
    @ColumnInfo(name = "weight_g") val weightG: Int,
    @ColumnInfo(name = "connectivity") val connectivity: String = "2.4GHz / Bluetooth / 3.5mm",
    @ColumnInfo(name = "sound_isolation") val soundIsolation: String = "Pasif / ANC",
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
    @ColumnInfo(name = "bit_depth") val bitDepth: String = "8-bit + FRC",
    @ColumnInfo(name = "response_mprt_ms") val responseMprtMs: Double? = 0.5,
    @ColumnInfo(name = "brightness_sdr_nit") val brightnessSdrNit: Int = 350,
    @ColumnInfo(name = "brightness_hdr_nit") val brightnessHdrNit: Int = 400,
    @ColumnInfo(name = "color_gamut") val colorGamut: String = "%100 sRGB, %98 DCI-P3",
    @ColumnInfo(name = "vrr_support") val vrrSupport: String = "G-Sync Compatible / FreeSync Premium Pro",
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
    @ColumnInfo(name = "capsule_type") val capsuleType: String,
    @ColumnInfo(name = "polar_pattern") val polarPattern: String,
    @ColumnInfo(name = "sample_rate_and_bit") val sampleRateAndBit: String,
    @ColumnInfo(name = "frequency_response") val frequencyResponse: String,
    @ColumnInfo(name = "connectivity") val connectivity: String,
    @ColumnInfo(name = "max_spl_db") val maxSplDb: Int = 130,
    @ColumnInfo(name = "has_built_in_dsp") val hasBuiltInDsp: Boolean = true,
    @ColumnInfo(name = "requires_phantom_power") val requiresPhantomPower: Boolean = false,
    @ColumnInfo(name = "has_zero_latency_monitoring") val hasZeroLatencyMonitoring: Boolean = true,
    @ColumnInfo(name = "has_pop_filter") val hasPopFilter: Boolean = true,
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
    @ColumnInfo(name = "surface_type") val surfaceType: String,
    @ColumnInfo(name = "speed_glide_tier") val speedGlideTier: String,
    @ColumnInfo(name = "base_material") val baseMaterial: String,
    @ColumnInfo(name = "dimensions_mm") val dimensionsMm: String,
    @ColumnInfo(name = "thickness_mm") val thicknessMm: Double = 4.0,
    @ColumnInfo(name = "stitched_edges") val stitchedEdges: Boolean = true,
    @ColumnInfo(name = "stopping_power_score") val stoppingPowerScore: Int = 8,
    @ColumnInfo(name = "humidity_resistance") val humidityResistance: String = "Yüksek (%95+)",
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
    @ColumnInfo(name = "max_resolution") val maxResolution: String,
    @ColumnInfo(name = "max_frame_rate_fps") val maxFrameRateFps: Int,
    @ColumnInfo(name = "sensor_model") val sensorModel: String,
    @ColumnInfo(name = "sensor_size_inch") val sensorSizeInch: String = "1/2.8\"",
    @ColumnInfo(name = "aperture") val aperture: String = "f/2.0",
    @ColumnInfo(name = "field_of_view_deg") val fieldOfViewDeg: Int = 82,
    @ColumnInfo(name = "focus_type") val focusType: String = "PDAF Otomatik Odak",
    @ColumnInfo(name = "has_ai_tracking") val hasAiTracking: Boolean = false,
    @ColumnInfo(name = "has_hardware_hdr") val hasHardwareHdr: Boolean = true,
    @ColumnInfo(name = "connection_type") val connectionType: String = "USB 3.0 Type-C",
    @ColumnInfo(name = "privacy_shutter") val privacyShutter: Boolean = true,
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
    @ColumnInfo(name = "chair_type") val chairType: String,
    @ColumnInfo(name = "upholstery_material") val upholsteryMaterial: String,
    @ColumnInfo(name = "lumbar_support_type") val lumbarSupportType: String,
    @ColumnInfo(name = "armrest_type") val armrestType: String = "4D Metal Mekanizma",
    @ColumnInfo(name = "gas_lift_class") val gasLiftClass: String = "Class 4 Ağır Hizmet (BIFMA)",
    @ColumnInfo(name = "max_weight_capacity_kg") val maxWeightCapacityKg: Int = 150,
    @ColumnInfo(name = "recommended_height_range") val recommendedHeightRange: String = "165 - 195 cm",
    @ColumnInfo(name = "recline_angle_deg") val reclineAngleDeg: Int = 165,
    @ColumnInfo(name = "tilt_mechanism") val tiltMechanism: String = "Multi-Tilt (Kurbağa Mekanizma)",
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
    @ColumnInfo(name = "fan_size_mm") val fanSizeMm: Int = 120,
    @ColumnInfo(name = "min_rpm") val minRpm: Int = 600,
    @ColumnInfo(name = "max_rpm") val maxRpm: Int = 2000,
    @ColumnInfo(name = "max_airflow_cfm") val maxAirflowCfm: Double = 65.0,
    @ColumnInfo(name = "static_pressure_mmh2o") val staticPressureMmH2o: Double = 2.5,
    @ColumnInfo(name = "noise_level_dba") val noiseLevelDba: Double = 26.5,
    @ColumnInfo(name = "bearing_type") val bearingType: String = "FDB (Fluid Dynamic)",
    @ColumnInfo(name = "pwm_pins") val pwmPins: Int = 4,
    @ColumnInfo(name = "is_daisy_chain") val isDaisyChain: Boolean = true,
    @ColumnInfo(name = "argb_type") val argbType: String = "Adreslenebilir 5V ARGB",
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
    @ColumnInfo(name = "va_rating") val vaRating: Int = 1500,
    @ColumnInfo(name = "watt_capacity") val wattCapacity: Int = 900,
    @ColumnInfo(name = "topology") val topology: String = "Line-Interactive",
    @ColumnInfo(name = "sinewave_type") val sinewaveType: String = "Tam Sinüs (Pure Sine Wave)",
    @ColumnInfo(name = "battery_specs") val batterySpecs: String = "12V 9Ah x 2",
    @ColumnInfo(name = "runtime_min_full_load") val runtimeMinFullLoad: Int = 4,
    @ColumnInfo(name = "runtime_min_half_load") val runtimeMinHalfLoad: Int = 14,
    @ColumnInfo(name = "outlet_count") val outletCount: Int = 6,
    @ColumnInfo(name = "has_usb_interface") val hasUsbInterface: Boolean = true,
    @ColumnInfo(name = "surge_protection_joules") val surgeProtectionJoules: Int = 450,
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
    @ColumnInfo(name = "desk_width_cm") val deskWidthCm: Int = 150,
    @ColumnInfo(name = "desk_depth_cm") val deskDepthCm: Int = 75,
    @ColumnInfo(name = "is_motorized_height") val isMotorizedHeight: Boolean = true,
    @ColumnInfo(name = "min_height_cm") val minHeightCm: Int = 65,
    @ColumnInfo(name = "max_height_cm") val maxHeightCm: Int = 125,
    @ColumnInfo(name = "max_load_kg") val maxLoadKg: Int = 120,
    @ColumnInfo(name = "surface_finish") val surfaceFinish: String = "Manyetik Çelik / Karbon MDF",
    @ColumnInfo(name = "has_cable_management") val hasCableManagement: Boolean = true,
    @ColumnInfo(name = "integrated_mousepad") val integratedMousepad: Boolean = true,
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
    @ColumnInfo(name = "lighting_form_factor") val formFactor: String = "Monitör ScreenBar",
    @ColumnInfo(name = "max_lumens") val maxLumens: Int = 500,
    @ColumnInfo(name = "color_temp_kelvin") val colorTempKelvin: String = "2700K - 6500K + RGB",
    @ColumnInfo(name = "cri_score") val criScore: Int = 95,
    @ColumnInfo(name = "has_screen_mirroring") val hasScreenMirroring: Boolean = true,
    @ColumnInfo(name = "supported_ecosystems") val supportedEcosystems: String = "Razer Chroma / Matter / Alexa",
    @ColumnInfo(name = "connection_type") val connectionType: String = "Wi-Fi + Bluetooth",
    @ColumnInfo(name = "power_source") val powerSource: String = "USB-C 5V / Adaptör",
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
    @ColumnInfo(name = "screen_size_inch") val screenSizeInch: Double = 1.43,
    @ColumnInfo(name = "screen_panel") val screenPanel: String = "AMOLED Always-On",
    @ColumnInfo(name = "peak_brightness_nits") val peakBrightnessNits: Int = 2000,
    @ColumnInfo(name = "battery_life_days") val batteryLifeDays: Int = 5,
    @ColumnInfo(name = "water_resistance_atm") val waterResistanceAtm: Int = 5,
    @ColumnInfo(name = "os_name") val osName: String = "Wear OS / WatchOS",
    @ColumnInfo(name = "sensors_list") val sensorsList: String = "EKG, SpO2, Kalp Atışı, Çift Frekans GPS, Sıcaklık",
    @ColumnInfo(name = "has_cellular_lte") val hasCellularLte: Boolean = false,
    @ColumnInfo(name = "case_material") val caseMaterial: String = "Havacılık Sınıfı Alüminyum / Titanyum",
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
    @ColumnInfo(name = "wifi_standard") val wifiStandard: String = "Wi-Fi 7 (802.11be)",
    @ColumnInfo(name = "speed_class") val speedClass: String = "BE19000 Tri-Band",
    @ColumnInfo(name = "bands") val bands: String = "2.4GHz + 5GHz + 6GHz",
    @ColumnInfo(name = "ethernet_ports") val ethernetPorts: String = "2x 10GbE + 4x 2.5GbE LAN",
    @ColumnInfo(name = "has_gaming_qos") val hasGamingQos: Boolean = true,
    @ColumnInfo(name = "has_sqm_bufferbloat") val hasSqmBufferbloat: Boolean = true,
    @ColumnInfo(name = "processor_and_ram") val processorAndRam: String = "Dört Çekirdek 2.6GHz + 2GB RAM",
    @ColumnInfo(name = "usb_sharing_ports") val usbSharingPorts: String = "1x USB 3.2 Gen 1",
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
    @ColumnInfo(name = "vdsl_profile") val vdslProfile: String = "VDSL2 35b (Supervectoring 300M)",
    @ColumnInfo(name = "fiber_wan_support") val fiberWanSupport: Boolean = true,
    @ColumnInfo(name = "wifi_standard") val wifiStandard: String = "Wi-Fi 6 (AX3000)",
    @ColumnInfo(name = "lan_ports") val lanPorts: String = "1x 2.5GbE + 3x 1GbE LAN",
    @ColumnInfo(name = "voip_ports") val voipPorts: String = "2x FXS Telefon Hattı",
    @ColumnInfo(name = "chipset") val chipset: String = "Broadcom / MediaTek",
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
    @ColumnInfo(name = "form_factor") val formFactor: String = "Tavan / Duvar Tipi (Ceiling Mount)",
    @ColumnInfo(name = "wifi_standard") val wifiStandard: String = "Wi-Fi 7 / Wi-Fi 6E",
    @ColumnInfo(name = "poe_support") val poeSupport: String = "802.3at PoE+ (30W)",
    @ColumnInfo(name = "max_clients") val maxClients: Int = 300,
    @ColumnInfo(name = "mu_mimo_streams") val muMimoStreams: String = "4x4 MU-MIMO",
    @ColumnInfo(name = "uplink_port") val uplinkPort: String = "2.5GbE PoE Uplink",
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
    @ColumnInfo(name = "extender_type") val extenderType: String = "Mesh Düğümü (Mesh Node)",
    @ColumnInfo(name = "wifi_standard") val wifiStandard: String = "Wi-Fi 6E (AX5400)",
    @ColumnInfo(name = "dedicated_backhaul") val dedicatedBackhaul: String = "Ayrılmış 5GHz / 6GHz Kablosuz Omurga + GbE LAN",
    @ColumnInfo(name = "mesh_protocol") val meshProtocol: String = "EasyMesh / AiMesh / OneMesh",
    @ColumnInfo(name = "ethernet_ports") val ethernetPorts: String = "2x Gigabit LAN",
    @ColumnInfo(name = "coverage_area_m2") val coverageAreaM2: Int = 220,
)
