package com.panda.myapplication.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.UriHandler
import com.panda.myapplication.domain.model.AppCurrency
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.util.SecurityUtils
import com.panda.myapplication.presentation.theme.ElectricBlue
import com.panda.myapplication.presentation.theme.GoldAccent
import com.panda.myapplication.presentation.theme.NeonAmber
import com.panda.myapplication.presentation.theme.NeonCyan
import com.panda.myapplication.presentation.theme.NeonEmerald
import com.panda.myapplication.presentation.theme.NeonPurple
import com.panda.myapplication.presentation.theme.SilverAccent
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * Currency conversion parities against Turkish Lira (TRY base).
 */
private const val RATE_USD = 38.50
private const val RATE_EUR = 42.00
private const val RATE_GBP = 48.50

/**
 * Formats a numeric value with dot thousands separator (e.g., `15431.0` -> `"15.431"`).
 */
fun Double.formatPrice(): String {
    val rounded = abs(this).roundToLong()
    val s = rounded.toString()
    val sb = StringBuilder()
    if (this < 0) sb.append('-')
    for (i in s.indices) {
        if (i > 0 && (s.length - i) % 3 == 0) {
            sb.append('.')
        }
        sb.append(s[i])
    }
    return sb.toString()
}

/**
 * Formats a validated price with dynamic currency conversion (TRY, USD, EUR, GBP).
 */
fun Double?.formatPriceWithCurrency(
    currency: AppCurrency = AppCurrency.TRY,
    fallback: String = "Stok / Fiyat Yok",
): String {
    val cleanTry = SecurityUtils.sanitizeTryPrice(this) ?: return fallback
    return when (currency) {
        AppCurrency.TRY -> "${cleanTry.formatPrice()} ₺"
        AppCurrency.USD -> {
            val usd = cleanTry / RATE_USD
            "$ ${usd.formatPrice()}"
        }
        AppCurrency.EUR -> {
            val eur = cleanTry / RATE_EUR
            "€ ${eur.formatPrice()}"
        }
        AppCurrency.GBP -> {
            val gbp = cleanTry / RATE_GBP
            "£ ${gbp.formatPrice()}"
        }
    }
}

fun Double?.formatPriceWithCurrency(fallback: String): String = formatPriceWithCurrency(AppCurrency.TRY, fallback)

/**
 * Formats a validated price or returns `null` if missing or below realistic threshold.
 */
fun Double?.formatTryOrNull(currency: AppCurrency = AppCurrency.TRY): String? {
    val clean = SecurityUtils.sanitizeTryPrice(this) ?: return null
    return clean.formatPriceWithCurrency(currency)
}


/**
 * Opens an external store URL ONLY if it passes [SecurityUtils.validateStoreUrl] (HTTPS + Whitelisted Domain).
 */
fun UriHandler.openSafeStoreUrl(
    rawUrl: String?,
    isTr: Boolean = true,
    onBlocked: ((String) -> Unit)? = null,
): Boolean {
    val safeUrl = SecurityUtils.validateStoreUrl(rawUrl)
    if (safeUrl == null) {
        onBlocked?.invoke(if (isTr) "Güvenlik duvarı: Doğrulanmamış veya güvenli olmayan mağaza bağlantısı engellendi." else "Firewall: Blocked unverified or unsafe store link.")
        return false
    }
    return runCatching {
        this.openUri(safeUrl)
        true
    }.getOrElse {
        onBlocked?.invoke(if (isTr) "Tarayıcı açılamadı." else "Could not open web browser.")
        false
    }
}

/**
 * Centralized category accent color mapping used across Catalog, Detail, Wizard, and VS Arena.
 */
fun HardwareCategory.accentColor(): Color = when (this) {
    HardwareCategory.PREBUILT_PC -> NeonEmerald
    HardwareCategory.LAPTOP -> NeonPurple
    HardwareCategory.CPU -> ElectricBlue
    HardwareCategory.GPU -> NeonPurple
    HardwareCategory.MOTHERBOARD -> NeonAmber
    HardwareCategory.RAM -> NeonCyan
    HardwareCategory.STORAGE -> NeonEmerald
    HardwareCategory.PSU -> GoldAccent
    HardwareCategory.COOLER -> ElectricBlue
    HardwareCategory.CASE -> SilverAccent
    HardwareCategory.MOUSE -> NeonCyan
    HardwareCategory.KEYBOARD -> NeonPurple
    HardwareCategory.HEADSET -> NeonAmber
    HardwareCategory.MONITOR -> NeonEmerald
    HardwareCategory.MICROPHONE -> NeonAmber
    HardwareCategory.MOUSEPAD -> NeonCyan
    HardwareCategory.WEBCAM -> ElectricBlue
    HardwareCategory.GAMING_CHAIR -> NeonPurple
    HardwareCategory.CASE_FAN -> NeonCyan
    HardwareCategory.UPS -> GoldAccent
    HardwareCategory.GAMING_DESK -> NeonEmerald
    HardwareCategory.GAMING_LIGHTING -> NeonAmber
    HardwareCategory.SMARTWATCH -> ElectricBlue
    HardwareCategory.ROUTER -> NeonCyan
    HardwareCategory.MODEM -> NeonPurple
    HardwareCategory.ACCESS_POINT -> ElectricBlue
    HardwareCategory.RANGE_EXTENDER -> NeonAmber
    HardwareCategory.SMARTPHONE -> ElectricBlue
    HardwareCategory.TABLET -> NeonPurple
}

/**
 * Centralized short badge text for hardware categories across all 7 supported languages.
 */
fun HardwareCategory.shortBadgeText(strings: AppStrings): String = when (this) {
    HardwareCategory.PREBUILT_PC -> strings.catPrebuilt.uppercase()
    HardwareCategory.LAPTOP -> strings.catLaptop.uppercase()
    HardwareCategory.CPU -> "CPU"
    HardwareCategory.GPU -> "GPU"
    HardwareCategory.RAM -> "RAM"
    HardwareCategory.MOTHERBOARD -> strings.catMotherboard.uppercase()
    HardwareCategory.PSU -> "PSU"
    HardwareCategory.STORAGE -> "SSD"
    HardwareCategory.COOLER -> strings.catCooler.uppercase()
    HardwareCategory.CASE -> strings.catCase.uppercase()
    HardwareCategory.MOUSE -> strings.catMouse.uppercase()
    HardwareCategory.KEYBOARD -> strings.catKeyboard.uppercase()
    HardwareCategory.HEADSET -> strings.catHeadset.uppercase()
    HardwareCategory.MONITOR -> strings.catMonitor.uppercase()
    HardwareCategory.MICROPHONE -> strings.catMicrophone.uppercase()
    HardwareCategory.MOUSEPAD -> strings.catMousepad.uppercase()
    HardwareCategory.WEBCAM -> strings.catWebcam.uppercase()
    HardwareCategory.GAMING_CHAIR -> strings.catGamingChair.uppercase()
    HardwareCategory.CASE_FAN -> strings.catCaseFan.uppercase()
    HardwareCategory.UPS -> strings.catUps.uppercase()
    HardwareCategory.GAMING_DESK -> strings.catGamingDesk.uppercase()
    HardwareCategory.GAMING_LIGHTING -> strings.catGamingLighting.uppercase()
    HardwareCategory.SMARTWATCH -> strings.catSmartwatch.uppercase()
    HardwareCategory.ROUTER -> strings.catRouter.uppercase()
    HardwareCategory.MODEM -> strings.catModem.uppercase()
    HardwareCategory.ACCESS_POINT -> strings.catAccessPoint.uppercase()
    HardwareCategory.RANGE_EXTENDER -> strings.catRangeExtender.uppercase()
    HardwareCategory.SMARTPHONE -> strings.catPhone.uppercase()
    HardwareCategory.TABLET -> strings.catTablet.uppercase()
}

fun HardwareCategory.shortBadgeText(isTr: Boolean = true): String =
    shortBadgeText(if (isTr) StringsTR else StringsEN)

/**
 * Centralized category emoji icon.
 */
fun HardwareCategory.iconEmoji(): String = when (this) {
    HardwareCategory.PREBUILT_PC -> "🖥️"
    HardwareCategory.LAPTOP -> "💻"
    HardwareCategory.CPU -> "🧠"
    HardwareCategory.GPU -> "🎮"
    HardwareCategory.MOTHERBOARD -> "🎛️"
    HardwareCategory.RAM -> "⚡"
    HardwareCategory.STORAGE -> "💿"
    HardwareCategory.PSU -> "🔋"
    HardwareCategory.COOLER -> "❄️"
    HardwareCategory.CASE -> "🧊"
    HardwareCategory.MOUSE -> "🖱️"
    HardwareCategory.KEYBOARD -> "⌨️"
    HardwareCategory.HEADSET -> "🎧"
    HardwareCategory.MONITOR -> "🖥️"
    HardwareCategory.MICROPHONE -> "🎙️"
    HardwareCategory.MOUSEPAD -> "🎯"
    HardwareCategory.WEBCAM -> "📷"
    HardwareCategory.GAMING_CHAIR -> "🪑"
    HardwareCategory.CASE_FAN -> "💨"
    HardwareCategory.UPS -> "⚡"
    HardwareCategory.GAMING_DESK -> "🪵"
    HardwareCategory.GAMING_LIGHTING -> "💡"
    HardwareCategory.SMARTWATCH -> "⌚"
    HardwareCategory.ROUTER -> "📡"
    HardwareCategory.MODEM -> "📶"
    HardwareCategory.ACCESS_POINT -> "🗼"
    HardwareCategory.RANGE_EXTENDER -> "🛜"
    HardwareCategory.SMARTPHONE -> "📱"
    HardwareCategory.TABLET -> "📱"
}
