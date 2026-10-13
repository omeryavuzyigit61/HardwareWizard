package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PricePoint(
    val date: String,
    val price: Double,
)

@Serializable
data class ProductPriceInfo(
    val currentPriceTry: Double,
    val lowestPriceTry: Double,
    val highestPriceTry: Double,
    val currency: String = "TRY",
    val priceHistory: List<PricePoint> = emptyList(),
) {
    val lastChangePercent: Double
        get() {
            if (priceHistory.size < 2) return 0.0
            val previousPrice = priceHistory[priceHistory.size - 2].price
            if (previousPrice <= 0) return 0.0
            return ((currentPriceTry - previousPrice) / previousPrice) * 100.0
        }
}
