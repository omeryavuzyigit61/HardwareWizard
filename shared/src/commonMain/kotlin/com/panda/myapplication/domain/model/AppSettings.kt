package com.panda.myapplication.domain.model

enum class ThemeMode {
    SYSTEM,
    DARK,
    LIGHT,
    WOOD,
    OLED,
    CYBERPUNK;

    companion object {
        fun fromString(value: String?): ThemeMode = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: DARK
    }
}

enum class AppLanguage(val code: String, val displayName: String, val flag: String) {
    TR("tr", "Türkçe", "🇹🇷"),
    EN("en", "English", "🇬🇧"),
    DE("de", "Deutsch", "🇩🇪"),
    ES("es", "Español", "🇪🇸"),
    FR("fr", "Français", "🇫🇷"),
    RU("ru", "Русский", "🇷🇺"),
    JA("ja", "日本語", "🇯🇵");

    companion object {
        fun fromCode(code: String?): AppLanguage = entries.firstOrNull {
            it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true)
        } ?: TR
    }
}

enum class AppCurrency(val symbol: String, val code: String, val displayName: String) {
    TRY("₺", "TRY", "₺ TRY (Türk Lirası)"),
    USD("$", "USD", "$ USD (US Dollar)"),
    EUR("€", "EUR", "€ EUR (Euro)"),
    GBP("£", "GBP", "£ GBP (British Pound)");

    companion object {
        fun fromCode(code: String?): AppCurrency = entries.firstOrNull {
            it.code.equals(code, ignoreCase = true) || it.name.equals(code, ignoreCase = true)
        } ?: TRY
    }
}

enum class TemperatureUnit(val symbol: String, val displayName: String) {
    CELSIUS("°C", "Celsius (°C)"),
    FAHRENHEIT("°F", "Fahrenheit (°F)");

    companion object {
        fun fromString(value: String?): TemperatureUnit = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: CELSIUS
    }
}

enum class LengthUnit(val symbol: String, val displayName: String) {
    METRIC("mm", "Metrik (mm / cm)"),
    IMPERIAL("inç", "İmperyal (inç)");

    companion object {
        fun fromString(value: String?): LengthUnit = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: METRIC
    }
}

enum class ResolutionTarget(val displayName: String, val subtitle: String) {
    FHD_1080P("1080p FHD", "1920x1080 (Rekabetçi & Standart)"),
    QHD_1440P("1440p 2K", "2560x1440 (Tatlı Nokta / Sweet Spot)"),
    UHD_4K("4K UHD", "3840x2160 (Maksimum Görsel Şölen)");

    companion object {
        fun fromString(value: String?): ResolutionTarget = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: QHD_1440P
    }
}

enum class FpsTarget(val displayName: String, val subtitle: String) {
    FPS_60("60 FPS", "Akıcı AAA Hikaye Deneyimi"),
    FPS_144("144+ FPS", "Yüksek Yenileme & E-Spor"),
    FPS_240("240+ FPS", "Ultra Düşük Gecikmeli Rekabet");

    companion object {
        fun fromString(value: String?): FpsTarget = entries.firstOrNull {
            it.name.equals(value, ignoreCase = true)
        } ?: FPS_144
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val language: AppLanguage = AppLanguage.TR,
    val currency: AppCurrency = AppCurrency.TRY,
    val dataSaver: Boolean = false,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val lengthUnit: LengthUnit = LengthUnit.METRIC,
    val defaultResolution: ResolutionTarget = ResolutionTarget.QHD_1440P,
    val defaultFps: FpsTarget = FpsTarget.FPS_144,
    val enthusiastMode: Boolean = true,
)
