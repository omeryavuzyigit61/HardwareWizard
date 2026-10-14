package com.panda.myapplication

import com.panda.myapplication.domain.model.ProductPriceInfo
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

class PriceLoadingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testPricesTrDeserialization() = runTest {
        val sampleJson = """
        {
          "cpu_amd_ryzen_7_7800x3d": {
            "productId": "cpu_amd_ryzen_7_7800x3d",
            "currentPriceTry": 20799.0,
            "lastUpdated": "2026-10-14T16:45:17.718408",
            "priceHistory": [
              { "date": "2026-08-10", "price": 16499.0 },
              { "date": "2026-10-14", "priceTry": 20799.0 }
            ],
            "offers": [
              {
                "storeName": "Gaming.Gen.TR",
                "priceTry": 20799.0,
                "url": "https://www.gaming.gen.tr/urun/360524/",
                "inStock": true
              }
            ]
          },
          "legacy_product_without_price": {
            "productId": "legacy_product_without_price",
            "currentPriceTry": null,
            "offers": []
          }
        }
        """.trimIndent()

        val map = json.decodeFromString<Map<String, ProductPriceInfo>>(sampleJson)
        assertTrue(map.isNotEmpty(), "Map should not be empty")
        val p = map["cpu_amd_ryzen_7_7800x3d"]
        assertTrue(p?.currentPriceTry == 20799.0)
        assertTrue(p?.offers?.size == 1)
        assertTrue(p?.priceHistory?.size == 2)
        // Verify effectivePrice handles both price and priceTry
        assertTrue(p?.priceHistory?.get(1)?.effectivePrice == 20799.0)
    }
}
