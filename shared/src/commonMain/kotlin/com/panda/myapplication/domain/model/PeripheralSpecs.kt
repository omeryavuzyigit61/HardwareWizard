package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

/**
 * Domain models for Gaming Peripherals & Setup Ecosystem:
 * - [MouseSpec]: Oyuncu Faresi
 * - [KeyboardSpec]: Mekanik & Manyetik (Hall Effect) Klavye
 * - [HeadsetSpec]: Oyuncu & Stüdyo Kulaklığı
 * - [MonitorSpec]: Oyuncu Monitörü
 * - [MicrophoneSpec]: Yayıncı & Stüdyo Mikrofonu
 * - [MousepadSpec]: Oyuncu Mousepad & Deskmat
 * - [WebcamSpec]: Yayıncı & İçerik Üreticisi Kamerası
 * - [GamingChairSpec]: Oyuncu & Ergonomi Koltuğu
 */

@Serializable
data class MouseSpec(
    val sensor: String,
    val maxDpi: Int,
    val pollingRateHz: Int,
    val switchType: String,
    val weightG: Int,
    val connectivity: String,
    val batteryHours: Int? = null,
    val mcu: String = "Nordic 52840",
    val clickLatencyMs: Double = 0.8,
    val shapeType: String = "Simetrik",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MOUSE
}

@Serializable
data class KeyboardSpec(
    val switchType: String,
    val rapidTriggerSupported: Boolean,
    val minActuationMm: Double,
    val pollingRateHz: Int,
    val layout: String,
    val hotSwap: Boolean,
    val connectivity: String,
    val maxActuationMm: Double = 4.0,
    val caseMaterial: String = "Alüminyum CNC / ABS",
    val isHallEffect: Boolean = false,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.KEYBOARD
}

@Serializable
data class HeadsetSpec(
    val driverType: String,
    val driverSizeMm: Int,
    val impedanceOhm: Int,
    val frequencyResponse: String,
    val dacAmpRequired: Boolean,
    val micType: String,
    val weightG: Int,
    val connectivity: String = "2.4GHz / Bluetooth / 3.5mm",
    val soundIsolation: String = "Pasif / ANC",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.HEADSET
}

@Serializable
data class MonitorSpec(
    val panelType: String,
    val screenSizeInch: Double,
    val resolution: String,
    val refreshRateHz: Int,
    val responseSpeedGtGMs: Double,
    val ppi: Int,
    val hdrCertification: String,
    val bitDepth: String = "8-bit + FRC",
    val responseMprtMs: Double? = 0.5,
    val brightnessSdrNit: Int = 350,
    val brightnessHdrNit: Int = 400,
    val colorGamut: String = "%100 sRGB, %98 DCI-P3",
    val vrrSupport: String = "G-Sync Compatible / FreeSync Premium Pro",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MONITOR
}

@Serializable
data class MicrophoneSpec(
    val capsuleType: String,           // Dinamik (Dynamic) veya Kondenser (Condenser)
    val polarPattern: String,          // Kardioid, Çift Yönlü, Çok Yönlü
    val sampleRateAndBit: String,      // 24-bit / 96kHz
    val frequencyResponse: String,     // 20Hz - 20kHz
    val connectivity: String,          // USB-C, XLR, Hibrit (USB-C + XLR)
    val maxSplDb: Int = 130,           // Maksimum Ses Basınç Seviyesi (dB)
    val hasBuiltInDsp: Boolean = true, // Dahili DSP (Gürültü Kapısı / Kompresör / EQ)
    val requiresPhantomPower: Boolean = false,
    val hasZeroLatencyMonitoring: Boolean = true,
    val hasPopFilter: Boolean = true,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MICROPHONE
}

@Serializable
data class MousepadSpec(
    val surfaceType: String,           // Kumaş (Cloth), Poron Hibrit, Temperli Cam (Glass), Cordura
    val speedGlideTier: String,        // Control, Balanced, Speed, Glass Ultra-Fast
    val baseMaterial: String,          // Japon Poron, Doğal Kauçuk, Silikon Taban
    val dimensionsMm: String,          // 490 x 420 mm, 900 x 400 mm
    val thicknessMm: Double = 4.0,     // 2.0mm, 3.5mm, 4.0mm
    val stitchedEdges: Boolean = true, // Ultra düşük profilli dikişli kenar
    val stoppingPowerScore: Int = 8,   // 1-10 arası durdurma gücü
    val humidityResistance: String = "Yüksek (%95+)",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MOUSEPAD
}

@Serializable
data class WebcamSpec(
    val maxResolution: String,         // 4K UHD (3840x2160), 1080p FHD
    val maxFrameRateFps: Int,          // 60 FPS, 30 FPS
    val sensorModel: String,           // Sony STARVIS 2, Sony IMX, CMOS
    val sensorSizeInch: String = "1/2.8\"", // 1/1.2", 1/2.8"
    val aperture: String = "f/2.0",    // f/1.7, f/2.0
    val fieldOfViewDeg: Int = 82,      // 78°, 82°, 90°
    val focusType: String = "PDAF Otomatik Odak",
    val hasAiTracking: Boolean = false,
    val hasHardwareHdr: Boolean = true,
    val connectionType: String = "USB 3.0 Type-C",
    val privacyShutter: Boolean = true,
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.WEBCAM
}

@Serializable
data class GamingChairSpec(
    val chairType: String,             // Ergonomik File (Mesh), Yarışçı (Racing), Hibrit
    val upholsteryMaterial: String,    // Nefes Alan Kumaş (SoftWeave), PU Deri, Gerçek Deri, Elastomer File
    val lumbarSupportType: String,     // 4 Yönlü Mekanik L4-L5 Bel Desteği, Manyetik Hafızalı Yastık
    val armrestType: String = "4D Metal Mekanizma",
    val gasLiftClass: String = "Class 4 Ağır Hizmet (BIFMA)",
    val maxWeightCapacityKg: Int = 150,
    val recommendedHeightRange: String = "165 - 195 cm",
    val reclineAngleDeg: Int = 165,
    val tiltMechanism: String = "Multi-Tilt (Kurbağa Mekanizma)",
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.GAMING_CHAIR
}

@Serializable
data class CaseFanSpec(
    val fanSizeMm: Int = 120,                  // 120, 140, 200 mm
    val minRpm: Int = 600,                     // Min devir
    val maxRpm: Int = 2000,                    // Max devir
    val maxAirflowCfm: Double = 65.0,          // CFM hava akışı
    val staticPressureMmH2o: Double = 2.5,     // Statik basınç (mmH2O)
    val noiseLevelDba: Double = 26.5,          // dBA gürültü seviyesi
    val bearingType: String = "FDB (Fluid Dynamic)", // FDB, SSO2, Dual Ball
    val pwmPins: Int = 4,                      // 4-pin PWM kontrol
    val isDaisyChain: Boolean = true,          // Manyetik / Geçmeli kablosuz daisy-chain
    val argbType: String = "Adreslenebilir 5V ARGB", // ARGB, iCUE Link, Standart
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.CASE_FAN
}

@Serializable
data class UpsSpec(
    val vaRating: Int = 1500,                  // Volt-Amper (VA)
    val wattCapacity: Int = 900,               // Gerçek Watt Kapasitesi (W)
    val topology: String = "Line-Interactive", // Line-Interactive, Online Double Conversion
    val sinewaveType: String = "Tam Sinüs (Pure Sine Wave)", // Pure Sine Wave, Simüle Sinüs
    val batterySpecs: String = "12V 9Ah x 2",  // Akü tipi ve adedi
    val runtimeMinFullLoad: Int = 4,           // %100 yükte dakika
    val runtimeMinHalfLoad: Int = 14,          // %50 yükte dakika
    val outletCount: Int = 6,                  // Priz çıkış adedi
    val hasUsbInterface: Boolean = true,       // PC bağlantısı (Otomatik Kapanma)
    val surgeProtectionJoules: Int = 450,      // Parafudr darbe koruma joule değeri
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.UPS
}

@Serializable
data class GamingDeskSpec(
    val deskWidthCm: Int = 150,                // Masa genişliği (cm)
    val deskDepthCm: Int = 75,                 // Masa derinliği (cm)
    val isMotorizedHeight: Boolean = true,     // Motorlu ayarlanabilir standing desk
    val minHeightCm: Int = 65,                 // Min yükseklik
    val maxHeightCm: Int = 125,                // Max yükseklik
    val maxLoadKg: Int = 120,                  // Max taşıma yükü (kg)
    val surfaceFinish: String = "Manyetik Çelik / Karbon MDF", // Tabla yüzeyi
    val hasCableManagement: Boolean = true,    // Entegre kablo tepsisi ve kanalı
    val integratedMousepad: Boolean = true,    // Tam yüzey özel kesim deskmat
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.GAMING_DESK
}

@Serializable
data class GamingLightingSpec(
    val formFactor: String = "Monitör ScreenBar", // ScreenBar, Işık Kulesi, Duvar Paneli, LED Şerit
    val maxLumens: Int = 500,                  // Lümen parlaklık
    val colorTempKelvin: String = "2700K - 6500K + RGB", // Renk sıcaklığı aralığı
    val criScore: Int = 95,                    // Renk doğruluğu indeksi (Ra)
    val hasScreenMirroring: Boolean = true,    // Reaktif ekran renk senkronizasyonu
    val supportedEcosystems: String = "Razer Chroma / Matter / Alexa", // Ekosistem desteği
    val connectionType: String = "Wi-Fi + Bluetooth", // Bağlantı
    val powerSource: String = "USB-C 5V / Adaptör",   // Güç beslemesi
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.GAMING_LIGHTING
}

@Serializable
data class SmartwatchSpec(
    val screenSizeInch: Double = 1.43,         // Ekran boyutu (inç)
    val screenPanel: String = "AMOLED Always-On", // Panel türü
    val peakBrightnessNits: Int = 2000,        // Zirve parlaklık
    val batteryLifeDays: Int = 5,              // Tipik kullanım pil ömrü (gün)
    val waterResistanceAtm: Int = 5,           // Su geçirmezlik (5 ATM / 50m)
    val osName: String = "Wear OS / WatchOS",  // İşletim sistemi
    val sensorsList: String = "EKG, SpO2, Kalp Atışı, Çift Frekans GPS, Sıcaklık", // Sensörler
    val hasCellularLte: Boolean = false,       // eSIM / LTE hücresel bağlantı
    val caseMaterial: String = "Havacılık Sınıfı Alüminyum / Titanyum", // Kasa
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.SMARTWATCH
}

@Serializable
data class RouterSpec(
    val wifiStandard: String = "Wi-Fi 7 (802.11be)", // Wi-Fi 7, Wi-Fi 6E, Wi-Fi 6
    val speedClass: String = "BE19000 Tri-Band",     // BE19000, AX6000, AX3000
    val bands: String = "2.4GHz + 5GHz + 6GHz",      // Frekans bantları
    val ethernetPorts: String = "2x 10GbE + 4x 2.5GbE LAN", // Ethernet port konfigürasyonu
    val hasGamingQos: Boolean = true,                // Donanımsal E-Spor önceliği
    val hasSqmBufferbloat: Boolean = true,           // SQM / Cake bufferbloat önleme
    val processorAndRam: String = "Dört Çekirdek 2.6GHz + 2GB RAM", // Donanım
    val usbSharingPorts: String = "1x USB 3.2 Gen 1", // USB depolama/hücresel yedekleme
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.ROUTER
}

@Serializable
data class ModemSpec(
    val vdslProfile: String = "VDSL2 35b (Supervectoring 300M)", // 35b, 17a, FTTH Fiber WAN
    val fiberWanSupport: Boolean = true,             // Gigabit WAN fiber sonlandırma
    val wifiStandard: String = "Wi-Fi 6 (AX3000)",   // Dahili Wi-Fi
    val lanPorts: String = "1x 2.5GbE + 3x 1GbE LAN", // LAN portları
    val voipPorts: String = "2x FXS Telefon Hattı",  // VoIP desteği
    val chipset: String = "Broadcom / MediaTek",     // Yonga seti
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.MODEM
}

@Serializable
data class AccessPointSpec(
    val formFactor: String = "Tavan / Duvar Tipi (Ceiling Mount)", // Tavan, Masaüstü
    val wifiStandard: String = "Wi-Fi 7 / Wi-Fi 6E", // Wi-Fi standardı
    val poeSupport: String = "802.3at PoE+ (30W)",   // PoE sınıfı
    val maxClients: Int = 300,                       // Eşzamanlı istemci kapasitesi
    val muMimoStreams: String = "4x4 MU-MIMO",       // Çoklu anten akışı
    val uplinkPort: String = "2.5GbE PoE Uplink",    // Uplink port hızı
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.ACCESS_POINT
}

@Serializable
data class RangeExtenderSpec(
    val extenderType: String = "Mesh Düğümü (Mesh Node)", // Mesh, Priz Tipi Menzil Genişletici
    val wifiStandard: String = "Wi-Fi 6E (AX5400)",  // Wi-Fi standardı
    val dedicatedBackhaul: String = "Ayrılmış 5GHz / 6GHz Kablosuz Omurga + GbE LAN", // Backhaul
    val meshProtocol: String = "EasyMesh / AiMesh / OneMesh", // Mesh ekosistemi
    val ethernetPorts: String = "2x Gigabit LAN",    // LAN port adedi
    val coverageAreaM2: Int = 220,                   // Kapsama alanı (m²)
) : HardwareSpec {
    override val category: HardwareCategory = HardwareCategory.RANGE_EXTENDER
}
