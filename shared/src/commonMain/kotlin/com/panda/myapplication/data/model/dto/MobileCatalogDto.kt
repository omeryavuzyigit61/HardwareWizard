package com.panda.myapplication.data.model.dto

import com.panda.myapplication.domain.model.CpuSyntheticBenchmark
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.PricePoint
import com.panda.myapplication.domain.model.ProductPriceInfo
import com.panda.myapplication.domain.model.StoreOffer
import com.panda.myapplication.domain.model.TabletSpec
import kotlinx.serialization.Serializable

@Serializable
data class MobileStoreDto(
    val storeName: String,
    val priceTry: Double,
    val url: String = "https://www.google.com",
)

@Serializable
data class MobilePricePointDto(
    val date: String,
    val priceTry: Double,
)

@Serializable
data class SmartphoneCatalogItemDto(
    val id: String,
    val name: String = "",
    val brand: String,
    val model: String,
    val priceTry: Double = 0.0,
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val tier: String = "A",
    val benchmarkScore: Int? = null,
    val gamingIndex: Int? = null,
    val productivityIndex: Int? = null,
    val geekbench6Single: Int? = null,
    val geekbench6Multi: Int? = null,
    val relativePerformanceIndex: Double? = null,
    val stores: List<MobileStoreDto> = emptyList(),
    val priceHistory: List<MobilePricePointDto> = emptyList(),
    val spec: PhoneSpec? = null,
) {
    fun toDomain(): HardwareItem? {
        val phoneSpec = spec ?: return null
        val effectiveImg = imageUrl.ifBlank { images.firstOrNull().orEmpty() }
        val effectiveImgs = if (images.isNotEmpty()) images else listOfNotNull(effectiveImg.takeIf { it.isNotBlank() })
        return HardwareItem(
            id = HardwareId(id),
            brand = brand,
            model = model,
            price = Money((priceTry * 100).toLong(), "TRY"),
            imageUrl = effectiveImg,
            images = effectiveImgs,
            benchmarkInfo = HardwareBenchmarkInfo(
                score = benchmarkScore ?: phoneSpec.antutuScore ?: 0,
                tier = tier,
                gamingIndex = gamingIndex ?: 75,
                productivityIndex = productivityIndex ?: 75,
                cpuSynthetic = CpuSyntheticBenchmark(
                    geekbench6Single = geekbench6Single ?: phoneSpec.geekbenchSingle,
                    geekbench6Multi = geekbench6Multi ?: phoneSpec.geekbenchMulti,
                ),
                relativePerformanceIndex = relativePerformanceIndex ?: 70.0,
            ),
            priceInfo = ProductPriceInfo(
                currentPriceTry = priceTry,
                lowestPriceTry = stores.minOfOrNull { it.priceTry } ?: priceTry,
                highestPriceTry = stores.maxOfOrNull { it.priceTry } ?: priceTry,
                currency = "TRY",
                offers = stores.map { StoreOffer(storeName = it.storeName, priceTry = it.priceTry, url = it.url) },
                priceHistory = priceHistory.map { PricePoint(date = it.date, priceTry = it.priceTry) },
            ),
            spec = phoneSpec,
        )
    }
}

@Serializable
data class TabletCatalogItemDto(
    val id: String,
    val name: String = "",
    val brand: String,
    val model: String,
    val priceTry: Double = 0.0,
    val imageUrl: String = "",
    val images: List<String> = emptyList(),
    val tier: String = "A",
    val benchmarkScore: Int? = null,
    val gamingIndex: Int? = null,
    val productivityIndex: Int? = null,
    val geekbench6Single: Int? = null,
    val geekbench6Multi: Int? = null,
    val relativePerformanceIndex: Double? = null,
    val stores: List<MobileStoreDto> = emptyList(),
    val priceHistory: List<MobilePricePointDto> = emptyList(),
    val spec: TabletSpec? = null,
) {
    fun toDomain(): HardwareItem? {
        val tabletSpec = spec ?: return null
        val effectiveImg = imageUrl.ifBlank { images.firstOrNull().orEmpty() }
        val effectiveImgs = if (images.isNotEmpty()) images else listOfNotNull(effectiveImg.takeIf { it.isNotBlank() })
        return HardwareItem(
            id = HardwareId(id),
            brand = brand,
            model = model,
            price = Money((priceTry * 100).toLong(), "TRY"),
            imageUrl = effectiveImg,
            images = effectiveImgs,
            benchmarkInfo = HardwareBenchmarkInfo(
                score = benchmarkScore ?: tabletSpec.antutuScore ?: 0,
                tier = tier,
                gamingIndex = gamingIndex ?: 75,
                productivityIndex = productivityIndex ?: 75,
                cpuSynthetic = CpuSyntheticBenchmark(
                    geekbench6Single = geekbench6Single ?: tabletSpec.geekbenchSingle,
                    geekbench6Multi = geekbench6Multi ?: tabletSpec.geekbenchMulti,
                ),
                relativePerformanceIndex = relativePerformanceIndex ?: 70.0,
            ),
            priceInfo = ProductPriceInfo(
                currentPriceTry = priceTry,
                lowestPriceTry = stores.minOfOrNull { it.priceTry } ?: priceTry,
                highestPriceTry = stores.maxOfOrNull { it.priceTry } ?: priceTry,
                currency = "TRY",
                offers = stores.map { StoreOffer(storeName = it.storeName, priceTry = it.priceTry, url = it.url) },
                priceHistory = priceHistory.map { PricePoint(date = it.date, priceTry = it.priceTry) },
            ),
            spec = tabletSpec,
        )
    }
}
