package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable

/** Core domain entity representing a single purchasable hardware component. */
@Immutable
data class HardwareItem(
    val id: HardwareId,
    val brand: String,
    val model: String,
    val price: Money?,
    val spec: HardwareSpec,
    val priceInfo: ProductPriceInfo? = null,
    val benchmarkInfo: HardwareBenchmarkInfo? = null,
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    val customPriceTry: Double? = null,
    val customPriceNote: String? = null,
) {
    val category: HardwareCategory get() = spec.category

    val effectiveImageUrl: String? get() = imageUrl ?: priceInfo?.imageUrl ?: images.firstOrNull()
    val effectiveImages: List<String> get() = if (images.isNotEmpty()) images else listOfNotNull(effectiveImageUrl)
    val storePriceTry: Double? get() = priceInfo?.currentPriceTry
    val currentPriceTry: Double? get() = customPriceTry ?: priceInfo?.currentPriceTry
    val isCustomPrice: Boolean get() = customPriceTry != null
    val lowestPriceTry: Double? get() = priceInfo?.lowestPriceTry
    val highestPriceTry: Double? get() = priceInfo?.highestPriceTry
    val offers: List<StoreOffer> get() = priceInfo?.offers.orEmpty()
    val priceHistory: List<PricePoint> get() = priceInfo?.priceHistory.orEmpty()
    val benchmarkScore: Int? get() = benchmarkInfo?.score
    val benchmarkTier: String? get() = benchmarkInfo?.tier

    val displayName: String get() = if (model.startsWith(brand, ignoreCase = true)) {
        model
    } else {
        "$brand $model"
    }

    init {
        require(brand.isNotBlank()) { "brand must not be blank." }
        require(model.isNotBlank()) { "model must not be blank." }
    }
}
