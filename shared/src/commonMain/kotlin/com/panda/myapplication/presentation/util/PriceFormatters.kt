package com.panda.myapplication.presentation.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.UriHandler
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
 * Centralized CoreUtils & DesignSystem formatters (DRY Principle).
 *
 * Single source of truth for:
 * - Turkish Lira (₺) price formatting with dot thousands separator
 * - Price delta / F/P formatting
 * - Category accent colors, short badges, and emoji icons
 * - Strict HTTPS & Whitelist validated store link launching
 */

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
 * Formats a validated TRY price as `"15.431 ₺"` or returns `null` if missing or below realistic TRY threshold.
 */
fun Double?.formatTryOrNull(): String? {
    val clean = SecurityUtils.sanitizeTryPrice(this) ?: return null
    return "${clean.formatPrice()} ₺"
}

/**
 * Formats a nullable Double price as `"15.431 ₺"` or returns honest fallback text `"Stok / Fiyat Yok"`.
 */
fun Double?.formatPriceWithCurrency(fallback: String = "Stok / Fiyat Yok"): String {
    val clean = SecurityUtils.sanitizeTryPrice(this) ?: return fallback
    return "${clean.formatPrice()} ₺"
}

/**
 * Compact TRY formatter for chart axes (e.g., `"34.5B ₺"` or `"15.430 ₺"`).
 */
fun formatTryCompact(price: Double): String {
    val rounded = price.roundToLong()
    return if (rounded >= 10_000L) {
        val k = (rounded / 100.0).roundToLong() / 10.0
        "${k}B ₺"
    } else {
        "${price.formatPrice()} ₺"
    }
}

/**
 * Opens an external store URL ONLY if it passes [SecurityUtils.validateStoreUrl] (HTTPS + Whitelisted Domain).
 * Wraps the platform intent launch in `runCatching` so malformed or unsupported intents never crash the app.
 */
fun UriHandler.openSafeStoreUrl(
    rawUrl: String?,
    onBlocked: ((String) -> Unit)? = null,
): Boolean {
    val safeUrl = SecurityUtils.validateStoreUrl(rawUrl)
    if (safeUrl == null) {
        onBlocked?.invoke("Güvenlik duvarı: Doğrulanmamış veya güvenli olmayan mağaza bağlantısı engellendi.")
        return false
    }
    return runCatching {
        this.openUri(safeUrl)
        true
    }.getOrElse {
        onBlocked?.invoke("Tarayıcı açılamadı.")
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
}

/**
 * Centralized short badge text for hardware categories.
 */
fun HardwareCategory.shortBadgeText(): String = when (this) {
    HardwareCategory.PREBUILT_PC -> "OEM SİSTEM"
    HardwareCategory.LAPTOP -> "LAPTOP"
    HardwareCategory.CPU -> "CPU"
    HardwareCategory.GPU -> "GPU"
    HardwareCategory.RAM -> "RAM"
    HardwareCategory.MOTHERBOARD -> "ANAKART"
    HardwareCategory.PSU -> "PSU"
    HardwareCategory.STORAGE -> "SSD"
    HardwareCategory.COOLER -> "SOĞUTUCU"
    HardwareCategory.CASE -> "KASA"
    HardwareCategory.MOUSE -> "MOUSE"
    HardwareCategory.KEYBOARD -> "KLAVYE"
    HardwareCategory.HEADSET -> "KULAKLIK"
    HardwareCategory.MONITOR -> "MONİTÖR"
}

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
}
