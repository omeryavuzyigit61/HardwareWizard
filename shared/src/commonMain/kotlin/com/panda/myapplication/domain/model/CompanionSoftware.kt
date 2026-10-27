package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class CompanionSoftware(
    val id: String,
    val name: String,
    val developer: String,
    val latestVersion: String,
    val officialDownloadUrl: String,
    val webConfiguratorUrl: String? = null,
    val isWebBased: Boolean = false,
    val supportedOs: List<String> = listOf("Windows 10/11", "macOS"),
    val ramFootprintMb: Int? = null,
    val description: String,
    val capabilities: List<String> = emptyList(),
)

/**
 * Universal registry providing instant companion software, drivers, and web-configurator
 * links for hundreds of gaming peripherals and hardware brands.
 */
object CompanionSoftwareRegistry {

    private val SOFTWARE_CATALOG = mapOf(
        "razer" to CompanionSoftware(
            id = "razer-synapse",
            name = "Razer Synapse 4",
            developer = "Razer Inc.",
            latestVersion = "v4.0.86 (2024)",
            officialDownloadUrl = "https://www.razer.com/synapse-4",
            ramFootprintMb = 160,
            description = "Tuş eşleme, 8000Hz HyperPolling, Snap Tap, Chroma RGB aydınlatma ve makro yönetimi.",
            capabilities = listOf("Snap Tap / Rapid Trigger", "8000Hz Polling", "Chroma RGB Sync", "Makro Motoru"),
        ),
        "asus" to CompanionSoftware(
            id = "asus-armoury-crate",
            name = "ASUS Armoury Crate",
            developer = "ASUSTeK Computer Inc.",
            latestVersion = "v5.8.2.0",
            officialDownloadUrl = "https://www.asus.com/armoury-crate/",
            ramFootprintMb = 280,
            description = "Aura Sync RGB senkronizasyonu, OLED ekran animasyonları, fan eğrileri ve cihaz yazılım güncellemeleri.",
            capabilities = listOf("Aura Sync RGB", "AniMe Matrix OLED", "Fan & Sıcaklık Kontrolü", "Sürücü Güncelleyici"),
        ),
        "logitech" to CompanionSoftware(
            id = "logitech-ghub",
            name = "Logitech G HUB",
            developer = "Logitech",
            latestVersion = "v2024.7",
            officialDownloadUrl = "https://www.logitechg.com/innovation/g-hub.html",
            ramFootprintMb = 140,
            description = "HERO sensör DPI hassasiyeti, LIGHTSYNC RGB ve Blue VO!CE yayıncı mikrofon ekolayzer ayarları.",
            capabilities = listOf("HERO Sensör 32K DPI", "LIGHTSYNC RGB", "Blue VO!CE EQ", "Oyun Profilleri"),
        ),
        "corsair" to CompanionSoftware(
            id = "corsair-icue",
            name = "Corsair iCUE 5",
            developer = "Corsair Gaming Inc.",
            latestVersion = "v5.18.106",
            officialDownloadUrl = "https://www.corsair.com/icue",
            ramFootprintMb = 320,
            description = "iCUE Murals dinamik aydınlatma, fan RPM eğrileri, Elgato Stream Deck eklentileri ve ses profilleri.",
            capabilities = listOf("iCUE Murals RGB", "Fan & Pompa Kontrolü", "Slipstream Wireless", "Ses Ekolayzer"),
        ),
        "steelseries" to CompanionSoftware(
            id = "steelseries-gg",
            name = "SteelSeries GG & Sonar",
            developer = "SteelSeries",
            latestVersion = "v68.0.0",
            officialDownloadUrl = "https://steelseries.com/gg",
            ramFootprintMb = 210,
            description = "Sonar parametrik E-Spor ekolayzerı, Rapid Trigger aktivasyon derinliği ve PrismSync RGB.",
            capabilities = listOf("Sonar Parametrik EQ", "Rapid Trigger (0.1mm)", "PrismSync RGB", "OLED Ekran"),
        ),
        "wooting" to CompanionSoftware(
            id = "wooting-wootility",
            name = "Wootility Web & App",
            developer = "Wooting Technologies",
            latestVersion = "v4.6.14",
            officialDownloadUrl = "https://wooting.io/wootility",
            webConfiguratorUrl = "https://wootility.io",
            isWebBased = true,
            ramFootprintMb = 0,
            description = "Web tarayıcısından sürücüsüz çalışan Rapid Trigger, DKS (Dynamic Keystroke) ve analog eğri yapılandırıcısı.",
            capabilities = listOf("Sıfır Kurulum (WebHID)", "Rapid Trigger (0.1 - 4.0mm)", "DKS Analog Giriş", "Tachyon Modu"),
        ),
        "keychron" to CompanionSoftware(
            id = "keychron-launcher",
            name = "Keychron Launcher / VIA",
            developer = "Keychron & QMK Community",
            latestVersion = "Web Cloud v3.1",
            officialDownloadUrl = "https://launcher.keychron.com",
            webConfiguratorUrl = "https://launcher.keychron.com",
            isWebBased = true,
            ramFootprintMb = 0,
            description = "Tarayıcı üzerinden çalışan açık kaynak QMK/VIA tuş haritalama, makro ve kablosuz firmware yönetim aracı.",
            capabilities = listOf("WebUSB Sıfır Bloatware", "QMK/VIA Katmanları", "Kablosuz Firmware", "RGB Efektleri"),
        ),
        "glorious" to CompanionSoftware(
            id = "glorious-core",
            name = "Glorious CORE 2.0",
            developer = "Glorious LLC",
            latestVersion = "v2.0.9",
            officialDownloadUrl = "https://www.gloriousgaming.com/pages/glorious-core",
            ramFootprintMb = 120,
            description = "BAMF sensör kalibrasyonu, kalkış mesafesi (LOD), debounce süresi ve tuş atamaları.",
            capabilities = listOf("BAMF Sensör Kalibrasyonu", "LOD Ayarı", "Debounce Süresi", "RGB Kontrol"),
        ),
        "elgato" to CompanionSoftware(
            id = "elgato-camera-hub",
            name = "Elgato Camera Hub & Wave Link",
            developer = "Elgato / Corsair",
            latestVersion = "v1.10.2",
            officialDownloadUrl = "https://www.elgato.com/downloads",
            ramFootprintMb = 150,
            description = "DSLR/Webcam için manuel ISO, enstantane, FOV kadrajlama ve sanal 9 kanallı Wave Link mikser.",
            capabilities = listOf("Manuel ISO/Enstantane", "AI Arka Plan Bulanıklığı", "VST3 Ses Eklentileri", "Stream Deck Entegrasyonu"),
        ),
        "shure" to CompanionSoftware(
            id = "shure-motiv",
            name = "ShurePlus MOTIV Desktop",
            developer = "Shure Incorporated",
            latestVersion = "v1.5.3",
            officialDownloadUrl = "https://www.shure.com/en-US/products/software/shure_plus_motiv_desktop",
            ramFootprintMb = 75,
            description = "Dahili donanımsal DSP limiter, kompresör, konuşma mesafesi önayarları ve gerçek zamanlı EQ kontrolü.",
            capabilities = listOf("Donanımsal DSP Kompresör", "Peak Limiter", "Ses Tonu (Koyu/Doğal/Parlak)", "Gerçek Zamanlı Monitör"),
        ),
        "lian li" to CompanionSoftware(
            id = "lianli-lconnect",
            name = "Lian Li L-Connect 3",
            developer = "Lian Li Industrial Co.",
            latestVersion = "v2.0.18",
            officialDownloadUrl = "https://lian-li.com/l-connect3/",
            ramFootprintMb = 180,
            description = "UNI FAN kablosuz papatya dizimi fan hızları, LCD ekran animasyonları ve Strimer Plus ARGB efektleri.",
            capabilities = listOf("Manyetik Fan RPM Eşleme", "Kasa İçi LCD Ekran Kontrolü", "Strimer ARGB", "Sıcaklık Grafikleri"),
        ),
        "nzxt" to CompanionSoftware(
            id = "nzxt-cam",
            name = "NZXT CAM",
            developer = "NZXT Inc.",
            latestVersion = "v4.62.2",
            officialDownloadUrl = "https://nzxt.com/software/cam",
            ramFootprintMb = 190,
            description = "Kraken LCD ekran GIF/video oynatma, fan gürültü profilleri ve oyun içi FPS OSD göstergesi.",
            capabilities = listOf("Kraken LCD Ekran", "Akıllı Fan Gürültü İndeksi", "Oyun İçi OSD Overlay", "ARGB Eşleme"),
        ),
        "apc" to CompanionSoftware(
            id = "apc-powerchute",
            name = "Schneider PowerChute Serial Shutdown",
            developer = "Schneider Electric / APC",
            latestVersion = "v1.1.0",
            officialDownloadUrl = "https://www.apc.com/shop/us/en/categories/tools-software/powerchute/",
            ramFootprintMb = 65,
            description = "Elektrik kesintisinde PC'yi otomatik uykuya alma/kapatma, akü sağlığı ve voltaj loglama.",
            capabilities = listOf("Otomatik Güvenli Kapatma", "Voltaj Dalgalanma Kaydı", "Akü Testi", "Enerji Tüketim Raporu"),
        ),
        "cyberpower" to CompanionSoftware(
            id = "cyberpower-powerpanel",
            name = "CyberPower PowerPanel Personal",
            developer = "Cyber Power Systems",
            latestVersion = "v2.3.2",
            officialDownloadUrl = "https://www.cyberpowersystems.com/products/software/powerpanel-personal/",
            ramFootprintMb = 50,
            description = "USB üzerinden UPS akü durumu izleme, kalan kesinti süresi hesabı ve planlı sistem kapatma.",
            capabilities = listOf("Akü Kalan Süre Hesabı", "Sessiz Gece Modu", "Otomatik Kapanma", "Şebeke Voltaj Takibi"),
        ),
        "govee" to CompanionSoftware(
            id = "govee-desktop",
            name = "Govee Desktop",
            developer = "Govee Innovations",
            latestVersion = "v2.1.0",
            officialDownloadUrl = "https://www.govee.com/desktop",
            ramFootprintMb = 95,
            description = "PC ekranındaki pikselleri analiz ederek masaüstü kule ve duvar panellerine gerçek zamanlı senkronize eder.",
            capabilities = listOf("Ekran Senkronizasyonu (Screen Mirror)", "Razer Chroma Connect", "Müzik Reaktif Mod", "DreamView"),
        ),
        "philips" to CompanionSoftware(
            id = "philips-hue-sync",
            name = "Philips Hue Sync",
            developer = "Signify",
            latestVersion = "v1.3.4",
            officialDownloadUrl = "https://www.philips-hue.com/en-us/explore-hue/apps/desktop",
            ramFootprintMb = 110,
            description = "Oyunlardaki aksiyonu ve patlamaları gecikmesiz olarak Hue Play ampul ve ışık şeritlerine yansıtır.",
            capabilities = listOf("Ultra Düşük Gecikmeli Ekran Eşleme", "Oyun Modu", "Çoklu Işık Bölgesi", "Razer Entegrasyonu"),
        ),
        "nanoleaf" to CompanionSoftware(
            id = "nanoleaf-desktop",
            name = "Nanoleaf Desktop App",
            developer = "Nanoleaf",
            latestVersion = "v1.6.2",
            officialDownloadUrl = "https://nanoleaf.me/en-US/products/desktop-app/",
            ramFootprintMb = 100,
            description = "Duvar ışık panelleri ve ışık hatları için ekran yansıtma ve dinamik sahne kontrolü.",
            capabilities = listOf("Screen Mirror (4 Mod)", "Rhythm Müzik Görselleştirici", "Matter Uyumlu", "Razer Synapse"),
        ),
    )

    /**
     * Resolves the official companion software for a given [HardwareItem].
     */
    fun resolveFor(item: HardwareItem): CompanionSoftware? {
        val brandNorm = item.brand.trim().lowercase()
        val nameNorm = item.model.lowercase()

        // 1. Direct Brand Match
        for ((key, sw) in SOFTWARE_CATALOG) {
            if (brandNorm.contains(key) || nameNorm.contains(key)) {
                return sw
            }
        }

        // 2. Specialized Peripheral Category Defaults
        return when (item.category) {
            HardwareCategory.KEYBOARD -> {
                if (brandNorm.contains("wooting")) SOFTWARE_CATALOG["wooting"]
                else if (brandNorm.contains("keychron") || brandNorm.contains("monsgeek") || brandNorm.contains("epomaker") || brandNorm.contains("akko")) SOFTWARE_CATALOG["keychron"]
                else if (brandNorm.contains("glorious")) SOFTWARE_CATALOG["glorious"]
                else null
            }
            HardwareCategory.WEBCAM -> {
                if (brandNorm.contains("elgato")) SOFTWARE_CATALOG["elgato"]
                else if (brandNorm.contains("logitech")) SOFTWARE_CATALOG["logitech"]
                else if (brandNorm.contains("razer")) SOFTWARE_CATALOG["razer"]
                else null
            }
            HardwareCategory.MICROPHONE -> {
                if (brandNorm.contains("shure")) SOFTWARE_CATALOG["shure"]
                else if (brandNorm.contains("elgato")) SOFTWARE_CATALOG["elgato"]
                else if (brandNorm.contains("hyperx")) CompanionSoftware(
                    id = "hyperx-ngenuity",
                    name = "HyperX NGENUITY",
                    developer = "HP Inc. / HyperX",
                    latestVersion = "v2.25",
                    officialDownloadUrl = "https://hyperx.com/pages/ngenuity",
                    ramFootprintMb = 90,
                    description = "Dahili kazanç, polar patern ve QuadCast LED aydınlatma kontrolü.",
                    capabilities = listOf("RGB Aydınlatma", "Kazanç Ayarı", "Mikrofon İzleme"),
                )
                else null
            }
            HardwareCategory.UPS -> {
                if (brandNorm.contains("apc") || brandNorm.contains("schneider")) SOFTWARE_CATALOG["apc"]
                else if (brandNorm.contains("cyberpower")) SOFTWARE_CATALOG["cyberpower"]
                else null
            }
            HardwareCategory.GAMING_LIGHTING -> {
                if (brandNorm.contains("govee")) SOFTWARE_CATALOG["govee"]
                else if (brandNorm.contains("hue") || brandNorm.contains("philips")) SOFTWARE_CATALOG["philips"]
                else if (brandNorm.contains("nanoleaf")) SOFTWARE_CATALOG["nanoleaf"]
                else null
            }
            HardwareCategory.CASE_FAN -> {
                if (brandNorm.contains("lian")) SOFTWARE_CATALOG["lian li"]
                else if (brandNorm.contains("corsair")) SOFTWARE_CATALOG["corsair"]
                else if (brandNorm.contains("nzxt")) SOFTWARE_CATALOG["nzxt"]
                else null
            }
            else -> null
        }
    }
}
