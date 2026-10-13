package com.panda.myapplication.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class HardwareId(val value: String) {
    init {
        require(value.isNotBlank()) { "HardwareId must not be blank." }
    }
}

/** Frequency in megahertz. */
@JvmInline
value class Frequency(val mhz: Int) {
    init {
        require(mhz > 0) { "Frequency must be positive: $mhz" }
    }
}

/** Memory capacity in megabytes. */
@JvmInline
value class MemorySize(val megabytes: Int) {
    init {
        require(megabytes > 0) { "MemorySize must be positive: $megabytes" }
    }
}

/** Power draw in watts. */
@JvmInline
value class Wattage(val watts: Int) {
    init {
        require(watts >= 0) { "Wattage must not be negative: $watts" }
    }
}

/** Monetary amount stored in minor units (e.g. kuruş, cents) to avoid floating-point errors. */
data class Money(
    val amountMinor: Long,
    val currencyCode: String,
) {
    init {
        require(amountMinor >= 0) { "Money amount must not be negative: $amountMinor" }
        require(currencyCode.length == 3) { "Currency code must be ISO-4217 (3 letters): $currencyCode" }
    }
}
