package com.panda.myapplication.data.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PeripheralRawDto(
    @SerialName("id") val id: String,
    @SerialName("category") val category: String,
    @SerialName("name") val name: String,
    @SerialName("brand") val brand: String,
    @SerialName("model") val model: String,
    @SerialName("image_url") val imageUrl: String? = null,
    @SerialName("images") val images: List<String> = emptyList(),
    @SerialName("current_price_try") val currentPriceTry: Double? = null,
    @SerialName("lowest_price_try") val lowestPriceTry: Double? = null,
    @SerialName("highest_price_try") val highestPriceTry: Double? = null,
    @SerialName("store_name") val storeName: String? = null,
    @SerialName("store_url") val storeUrl: String? = null,
    @SerialName("benchmark_score") val benchmarkScore: Int? = null,
    @SerialName("benchmark_tier") val benchmarkTier: String? = null,
    // Mouse fields
    @SerialName("sensor") val sensor: String? = null,
    @SerialName("max_dpi") val maxDpi: Int? = null,
    @SerialName("polling_rate_hz") val pollingRateHz: Int? = null,
    @SerialName("switch_type") val switchType: String? = null,
    @SerialName("weight_g") val weightG: Int? = null,
    @SerialName("connectivity") val connectivity: String? = null,
    @SerialName("battery_hours") val batteryHours: Int? = null,
    @SerialName("mcu") val mcu: String? = null,
    @SerialName("click_latency_ms") val clickLatencyMs: Double? = null,
    @SerialName("shape_type") val shapeType: String? = null,
    // Keyboard fields
    @SerialName("rapid_trigger_supported") val rapidTriggerSupported: Boolean? = null,
    @SerialName("min_actuation_mm") val minActuationMm: Double? = null,
    @SerialName("max_actuation_mm") val maxActuationMm: Double? = null,
    @SerialName("layout") val layout: String? = null,
    @SerialName("hot_swap") val hotSwap: Boolean? = null,
    @SerialName("case_material") val caseMaterial: String? = null,
    @SerialName("is_hall_effect") val isHallEffect: Boolean? = null,
    // Headset fields
    @SerialName("driver_type") val driverType: String? = null,
    @SerialName("driver_size_mm") val driverSizeMm: Int? = null,
    @SerialName("impedance_ohm") val impedanceOhm: Int? = null,
    @SerialName("frequency_response") val frequencyResponse: String? = null,
    @SerialName("dac_amp_required") val dacAmpRequired: Boolean? = null,
    @SerialName("mic_type") val micType: String? = null,
    @SerialName("sound_isolation") val soundIsolation: String? = null,
    // Monitor fields
    @SerialName("panel_type") val panelType: String? = null,
    @SerialName("screen_size_inch") val screenSizeInch: Double? = null,
    @SerialName("resolution") val resolution: String? = null,
    @SerialName("refresh_rate_hz") val refreshRateHz: Int? = null,
    @SerialName("response_speed_gtg_ms") val responseSpeedGtGMs: Double? = null,
    @SerialName("response_mprt_ms") val responseMprtMs: Double? = null,
    @SerialName("ppi") val ppi: Int? = null,
    @SerialName("hdr_certification") val hdrCertification: String? = null,
    @SerialName("bit_depth") val bitDepth: String? = null,
    @SerialName("brightness_sdr_nit") val brightnessSdrNit: Int? = null,
    @SerialName("brightness_hdr_nit") val brightnessHdrNit: Int? = null,
    @SerialName("color_gamut") val colorGamut: String? = null,
    @SerialName("vrr_support") val vrrSupport: String? = null,
)
