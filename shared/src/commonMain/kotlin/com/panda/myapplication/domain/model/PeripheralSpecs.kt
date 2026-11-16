package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain models for Gaming Peripherals, Cooling Fans, Smartwatches, Networking & UPS.
 * All optional telemetry fields default to neutral empty/zero/null values rather than fabricated specs.
 */

@Serializable
data class MouseSpec(
    val sensor: String = "",
    val maxDpi: Int = 0,
    val pollingRateHz: Int = 0,
    val switchType: String = "",
    val weightG: Int = 0,
    val connectivity: String = "",
    val batteryHours: Int? = null,
    val mcu: String = "",
    val clickLatencyMs: Double = 0.0,
    val shapeType: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MOUSE
}

@Serializable
data class KeyboardSpec(
    val switchType: String = "",
    val rapidTriggerSupported: Boolean = false,
    val minActuationMm: Double = 0.0,
    val pollingRateHz: Int = 0,
    val layout: String = "",
    val hotSwap: Boolean = false,
    val connectivity: String = "",
    val maxActuationMm: Double = 0.0,
    val caseMaterial: String = "",
    val isHallEffect: Boolean = false,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.KEYBOARD
}

@Serializable
data class HeadsetSpec(
    val driverType: String = "",
    val driverSizeMm: Int = 0,
    val impedanceOhm: Int = 0,
    val frequencyResponse: String = "",
    val dacAmpRequired: Boolean = false,
    val micType: String = "",
    val weightG: Int = 0,
    val connectivity: String = "",
    val soundIsolation: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.HEADSET
}

@Serializable
data class MonitorSpec(
    val panelType: String = "",
    val screenSizeInch: Double = 0.0,
    val resolution: String = "",
    val refreshRateHz: Int = 0,
    val responseSpeedGtGMs: Double = 0.0,
    val ppi: Int = 0,
    val hdrCertification: String = "",
    val bitDepth: String = "",
    val responseMprtMs: Double? = null,
    val brightnessSdrNit: Int = 0,
    val brightnessHdrNit: Int = 0,
    val colorGamut: String = "",
    val vrrSupport: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MONITOR
}

@Serializable
data class MicrophoneSpec(
    val capsuleType: String = "",
    val polarPattern: String = "",
    val sampleRateAndBit: String = "",
    val frequencyResponse: String = "",
    val connectivity: String = "",
    val maxSplDb: Int = 0,
    val hasBuiltInDsp: Boolean = false,
    val requiresPhantomPower: Boolean = false,
    val hasZeroLatencyMonitoring: Boolean = false,
    val hasPopFilter: Boolean = false,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MICROPHONE
}

@Serializable
data class MousepadSpec(
    val surfaceType: String = "",
    val speedGlideTier: String = "",
    val baseMaterial: String = "",
    val dimensionsMm: String = "",
    val thicknessMm: Double = 0.0,
    val stitchedEdges: Boolean = false,
    val stoppingPowerScore: Int = 0,
    val humidityResistance: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MOUSEPAD
}

@Serializable
data class WebcamSpec(
    val maxResolution: String = "",
    val maxFrameRateFps: Int = 0,
    val sensorModel: String = "",
    val sensorSizeInch: String = "",
    val aperture: String = "",
    val fieldOfViewDeg: Int = 0,
    val focusType: String = "",
    val hasAiTracking: Boolean = false,
    val hasHardwareHdr: Boolean = false,
    val connectionType: String = "",
    val privacyShutter: Boolean = false,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.WEBCAM
}

@Serializable
data class GamingChairSpec(
    val chairType: String = "",
    val upholsteryMaterial: String = "",
    val lumbarSupportType: String = "",
    val armrestType: String = "",
    val gasLiftClass: String = "",
    val maxWeightCapacityKg: Int = 0,
    val recommendedHeightRange: String = "",
    val reclineAngleDeg: Int = 0,
    val tiltMechanism: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.GAMING_CHAIR
}

@Serializable
data class CaseFanSpec(
    val fanSizeMm: Int = 0,
    val minRpm: Int = 0,
    val maxRpm: Int = 0,
    val maxAirflowCfm: Double = 0.0,
    val staticPressureMmH2o: Double = 0.0,
    val noiseLevelDba: Double = 0.0,
    val bearingType: String = "",
    val pwmPins: Int = 0,
    val isDaisyChain: Boolean = false,
    val argbType: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.CASE_FAN
}

@Serializable
data class UpsSpec(
    val vaRating: Int = 0,
    val wattCapacity: Int = 0,
    val topology: String = "",
    val sinewaveType: String = "",
    val batterySpecs: String = "",
    val runtimeMinFullLoad: Int = 0,
    val runtimeMinHalfLoad: Int = 0,
    val outletCount: Int = 0,
    val hasUsbInterface: Boolean = false,
    val surgeProtectionJoules: Int = 0,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.UPS
}

@Serializable
data class GamingDeskSpec(
    val deskWidthCm: Int = 0,
    val deskDepthCm: Int = 0,
    val isMotorizedHeight: Boolean = false,
    val minHeightCm: Int = 0,
    val maxHeightCm: Int = 0,
    val maxLoadKg: Int = 0,
    val surfaceFinish: String = "",
    val hasCableManagement: Boolean = false,
    val integratedMousepad: Boolean = false,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.GAMING_DESK
}

@Serializable
data class GamingLightingSpec(
    val formFactor: String = "",
    val maxLumens: Int = 0,
    val colorTempKelvin: String = "",
    val criScore: Int = 0,
    val hasScreenMirroring: Boolean = false,
    val supportedEcosystems: String = "",
    val connectionType: String = "",
    val powerSource: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.GAMING_LIGHTING
}

@Serializable
data class SmartwatchSpec(
    val screenSizeInch: Double = 0.0,
    val screenPanel: String = "",
    val peakBrightnessNits: Int = 0,
    val batteryLifeDays: Int = 0,
    val waterResistanceAtm: Int = 0,
    val osName: String = "",
    val sensorsList: String = "",
    val hasCellularLte: Boolean = false,
    val caseMaterial: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.SMARTWATCH
}

@Serializable
data class RouterSpec(
    val wifiStandard: String = "",
    val speedClass: String = "",
    val bands: String = "",
    val ethernetPorts: String = "",
    val hasGamingQos: Boolean = false,
    val hasSqmBufferbloat: Boolean = false,
    val processorAndRam: String = "",
    val usbSharingPorts: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.ROUTER
}

@Serializable
data class ModemSpec(
    val vdslProfile: String = "",
    val fiberWanSupport: Boolean = false,
    val wifiStandard: String = "",
    val lanPorts: String = "",
    val voipPorts: String = "",
    val chipset: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MODEM
}

@Serializable
data class AccessPointSpec(
    val formFactor: String = "",
    val wifiStandard: String = "",
    val poeSupport: String = "",
    val maxClients: Int = 0,
    val muMimoStreams: String = "",
    val uplinkPort: String = "",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.ACCESS_POINT
}

@Serializable
data class RangeExtenderSpec(
    val extenderType: String = "",
    val wifiStandard: String = "",
    val dedicatedBackhaul: String = "",
    val meshProtocol: String = "",
    val ethernetPorts: String = "",
    val coverageAreaM2: Int = 0,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.RANGE_EXTENDER
}
