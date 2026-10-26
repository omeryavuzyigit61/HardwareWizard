package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Represents a specific storage & RAM hardware configuration of a smartphone or tablet,
 * complete with its dedicated pricing, store offers, and historical price ledger.
 */
@Immutable
@Serializable
data class DeviceVariant(
    val variantKey: String,          // e.g. "256gb", "512gb", "1tb", "2tb"
    val storageGb: Int,              // e.g. 256, 512, 1024, 2048
    val ramGb: Int,                  // e.g. 8, 12, 16, 24
    val priceTry: Double,            // e.g. 148999.0
    val stores: List<StoreOffer> = emptyList(),
    val priceHistory: List<PricePoint> = emptyList(),
) {
    val displayStorage: String get() = if (storageGb >= 1024) "${storageGb / 1024} TB" else "$storageGb GB"
    val displayRam: String get() = "$ramGb GB RAM"
    val fullLabel: String get() = "$displayStorage · $displayRam"
}
