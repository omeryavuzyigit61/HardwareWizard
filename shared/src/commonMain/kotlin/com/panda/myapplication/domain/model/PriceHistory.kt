package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class StoreOffer(
    val storeName: String,
    val priceTry: Double,
    val url: String,
    val inStock: Boolean = true,
)

@Serializable
data class PricePoint(
    val date: String,
    val price: Double,
)

@Serializable
data class ProductPriceInfo(
    val currentPriceTry: Double? = null,
    val lowestPriceTry: Double? = null,
    val highestPriceTry: Double? = null,
    val currency: String = "TRY",
    val offers: List<StoreOffer> = emptyList(),
    val priceHistory: List<PricePoint> = emptyList(),
) {
    val lastChangePercent: Double
        get() {
            val current = currentPriceTry ?: return 0.0
            if (priceHistory.size < 2) return 0.0
            val previousPrice = priceHistory[priceHistory.size - 2].price
            if (previousPrice <= 0) return 0.0
            return ((current - previousPrice) / previousPrice) * 100.0
        }
}
