package com.panda.myapplication.presentation.util

import kotlin.math.roundToLong

/**
 * Extension for formatting Turkish Lira (₺) values with dot thousands separator (e.g. 15.431).
 */
fun Double.formatPrice(): String {
    val rounded = this.roundToLong()
    val s = rounded.toString()
    val sb = StringBuilder()
    for (i in s.indices) {
        if (i > 0 && (s.length - i) % 3 == 0) {
            sb.append('.')
        }
        sb.append(s[i])
    }
    return sb.toString()
}

/**
 * Formats nullable Double price as "₺15.431" or returns fallback text like "Stok / Fiyat Yok".
 */
fun Double?.formatPriceWithCurrency(fallback: String = "Stok / Fiyat Yok"): String {
    if (this == null || this <= 0.0) return fallback
    return "₺${this.formatPrice()}"
}
