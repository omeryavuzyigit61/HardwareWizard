package com.panda.myapplication.domain.model

/** Core domain entity representing a single purchasable hardware component. */
data class HardwareItem(
    val id: HardwareId,
    val brand: String,
    val model: String,
    val price: Money?,
    val spec: HardwareSpec,
    val priceInfo: ProductPriceInfo? = null,
) {
    val category: HardwareCategory get() = spec.category

    val currentPriceTry: Double? get() = priceInfo?.currentPriceTry ?: price?.let { (it.amountMinor / 100.0) * 38.5 }
    val lowestPriceTry: Double? get() = priceInfo?.lowestPriceTry
    val highestPriceTry: Double? get() = priceInfo?.highestPriceTry
    val priceHistory: List<PricePoint> get() = priceInfo?.priceHistory.orEmpty()

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
