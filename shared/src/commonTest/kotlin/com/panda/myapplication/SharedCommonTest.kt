package com.panda.myapplication

import com.panda.myapplication.domain.engine.BenchmarkEngine
import com.panda.myapplication.domain.engine.ComparisonWinner
import com.panda.myapplication.domain.engine.VsArenaEngine
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.ProductPriceInfo
import com.panda.myapplication.domain.model.StoreOffer
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.domain.repository.VsArenaMode
import com.panda.myapplication.domain.util.SecurityUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SharedCommonTest {

    @Test
    fun securityUtilsEnforcesHttpsAndDomainWhitelist() {
        // Valid trusted Turkish store HTTPS URLs
        assertNotNull(SecurityUtils.validateStoreUrl("https://www.gaming.gen.tr/urun/rtx-4070-super/"))
        assertNotNull(SecurityUtils.validateStoreUrl("https://www.itopya.com/amd-ryzen-7-7800x3d"))
        assertNotNull(SecurityUtils.validateStoreUrl("https://www.hepsiburada.com/asus-rog-strix-pm-HBC001"))

        // Rejects dangerous schemes and cleartext HTTP
        assertNull(SecurityUtils.validateStoreUrl("http://www.itopya.com/amd-ryzen-7-7800x3d"))
        assertNull(SecurityUtils.validateStoreUrl("javascript:alert(1)"))
        assertNull(SecurityUtils.validateStoreUrl("file:///etc/passwd"))
        assertNull(SecurityUtils.validateStoreUrl("data:text/html,<script>alert(1)</script>"))
        assertNull(SecurityUtils.validateStoreUrl("https://evil-phishing-site.xyz/steal"))
    }

    @Test
    fun securityUtilsPurgesLeakedUsdPrices() {
        assertNull(SecurityUtils.sanitizeTryPrice(70.0))
        assertNull(SecurityUtils.sanitizeTryPrice(105.0))
        assertNull(SecurityUtils.sanitizeTryPrice(249.99))
        assertEquals(25999.0, SecurityUtils.sanitizeTryPrice(25999.0))
    }

    @Test
    fun benchmarkEngineGenerates12GameMatrixWithLow1Fps() {
        val matrix = BenchmarkEngine.buildFull12GameMatrix(
            ratio = 0.92,
            isCpu = false,
            isX3d = false,
        )

        val entries = matrix.toSimulationEntries()
        assertEquals(12, entries.size, "Should contain all 12 E-Sports + AAA games")
        val esportsCount = entries.count { it.isEsports }
        val aaaCount = entries.count { !it.isEsports }
        assertEquals(5, esportsCount, "Should contain 5 E-Sports games")
        assertEquals(7, aaaCount, "Should contain 7 AAA games")

        entries.forEach { game ->
            assertTrue(game.avg1080p > game.low1Fps1080p, "${game.title} 1080p Avg FPS must exceed 1% Low FPS")
            assertTrue(game.avg1440p > game.low1Fps1440p, "${game.title} 1440p Avg FPS must exceed 1% Low FPS")
            assertTrue(game.avg4k > game.low1Fps4k, "${game.title} 4K Avg FPS must exceed 1% Low FPS")
            assertTrue(game.low1Fps1080p > 0)
        }
    }

    @Test
    fun vsArenaEngineCalculatesComponentWinnerAndFpChampion() {
        val rtx4070Super = HardwareItem(
            id = HardwareId("gpu_rtx_4070_super"),
            brand = "ASUS",
            model = "GeForce RTX 4070 SUPER 12GB",
            price = null,
            priceInfo = ProductPriceInfo(
                currentPriceTry = 25999.0,
                lowestPriceTry = 25999.0,
                highestPriceTry = 25999.0,
                offers = listOf(
                    StoreOffer("Gaming.Gen.TR", 25999.0, "https://www.gaming.gen.tr/urun/rtx4070s", true)
                ),
            ),
            spec = GpuSpec(
                chipset = "GeForce RTX 4070 SUPER",
                vram = MemorySize(12 * 1024),
                memoryType = "GDDR6X",
                coreClock = Frequency(1980),
                boostClock = Frequency(2550),
                tdp = Wattage(220),
                recommendedPsu = Wattage(700),
                busWidthBits = 192,
                cudaCores = 7168,
            ),
        )

        val rtx4080Super = HardwareItem(
            id = HardwareId("gpu_rtx_4080_super"),
            brand = "MSI",
            model = "GeForce RTX 4080 SUPER 16GB",
            price = null,
            priceInfo = ProductPriceInfo(
                currentPriceTry = 46999.0,
                lowestPriceTry = 46999.0,
                highestPriceTry = 46999.0,
                offers = listOf(
                    StoreOffer("Sinerji", 46999.0, "https://www.sinerji.gen.tr/rtx4080s", true)
                ),
            ),
            spec = GpuSpec(
                chipset = "GeForce RTX 4080 SUPER",
                vram = MemorySize(16 * 1024),
                memoryType = "GDDR6X",
                coreClock = Frequency(2295),
                boostClock = Frequency(2610),
                tdp = Wattage(320),
                recommendedPsu = Wattage(750),
                busWidthBits = 256,
                cudaCores = 10240,
            ),
        )

        val report = VsArenaEngine.compare(
            left = rtx4070Super,
            right = rtx4080Super,
            mode = VsArenaMode.COMPONENT_VS_COMPONENT,
        )

        assertEquals(ComparisonWinner.RIGHT, report.performanceChampion.winner, "RTX 4080 SUPER must win raw performance")
        assertEquals(ComparisonWinner.LEFT, report.valueChampion.winner, "RTX 4070 SUPER must win Price/Performance")
        assertEquals(12, report.gameRows.size, "VS Arena must compare all 12 games head-to-head")
    }

    @Test
    fun vsArenaEngineComparesPrebuiltSystemsAndLaptops() {
        val prebuiltA = HardwareItem(
            id = HardwareId("prebuilt_a"),
            brand = "Gaming.Gen.TR",
            model = "BLADE-7500F RTX 4060 Ti",
            price = null,
            priceInfo = ProductPriceInfo(currentPriceTry = 29999.0),
            spec = PrebuiltSpec(
                storeName = "Gaming.Gen.TR",
                cpuModel = "AMD Ryzen 5 7500F",
                gpuModel = "ASUS DUAL RTX 4060 Ti 8GB",
                motherboardModel = "ASUS PRIME B650M-R",
                ramSpec = "16GB DDR5",
                storageSpec = "1TB NVMe",
                psuCaseSpec = "650W 80+ Bronze",
                estimatedSeparatePriceTry = 32500.0,
                priceAdvantageTry = 2501.0,
                fps1080p = 145,
                fps1440p = 98,
                fps4k = 54,
                gamingScore = 82,
            ),
        )

        val prebuiltB = HardwareItem(
            id = HardwareId("prebuilt_b"),
            brand = "Sinerji",
            model = "Sinerji Kraken RTX 4080 SUPER",
            price = null,
            priceInfo = ProductPriceInfo(currentPriceTry = 89999.0),
            spec = PrebuiltSpec(
                storeName = "Sinerji",
                cpuModel = "AMD Ryzen 7 9800X3D",
                gpuModel = "MSI RTX 4080 SUPER 16GB",
                motherboardModel = "MSI X670E TOMAHAWK",
                ramSpec = "32GB DDR5",
                storageSpec = "2TB KC3000",
                psuCaseSpec = "850W Gold ATX 3.0",
                estimatedSeparatePriceTry = 93500.0,
                priceAdvantageTry = 3501.0,
                fps1080p = 250,
                fps1440p = 190,
                fps4k = 115,
                gamingScore = 98,
            ),
        )

        val prebuiltReport = VsArenaEngine.compare(
            left = prebuiltA,
            right = prebuiltB,
            mode = VsArenaMode.PREBUILT_VS_PREBUILT,
        )
        assertEquals(ComparisonWinner.RIGHT, prebuiltReport.performanceChampion.winner)
        assertTrue(prebuiltReport.rows.any { it.label.contains("Tek Tek") || it.label.contains("Avantaj") })

        val laptopA = HardwareItem(
            id = HardwareId("laptop_a"),
            brand = "ASUS",
            model = "ROG Strix G16 RTX 4080",
            price = null,
            priceInfo = ProductPriceInfo(currentPriceTry = 78999.0),
            spec = LaptopSpec(
                brand = "ASUS",
                cpuModel = "Intel Core i9-14900HX",
                gpuModel = "RTX 4080 Laptop 12GB",
                gpuWattTgp = "175W Full Power",
                muxSwitch = "MUX Switch + Advanced Optimus",
                ramSpec = "32GB DDR5",
                storageSpec = "1TB Gen4 SSD",
                displaySpec = "16 inç 240Hz QHD+ Nebula",
                batteryWh = 90,
                weightKg = 2.50,
                coolingTech = "Tri-Fan + Sıvı Metal",
                gamingScore = 95,
                fps1080p = 175,
                fps1440p = 135,
            ),
        )

        val laptopB = HardwareItem(
            id = HardwareId("laptop_b"),
            brand = "Lenovo",
            model = "LOQ 15IRX9 RTX 4060",
            price = null,
            priceInfo = ProductPriceInfo(currentPriceTry = 32999.0),
            spec = LaptopSpec(
                brand = "Lenovo",
                cpuModel = "Intel Core i5-13450HX",
                gpuModel = "RTX 4060 Laptop 8GB",
                gpuWattTgp = "115W Dynamic Boost",
                muxSwitch = "MUX Switch",
                ramSpec = "16GB DDR5",
                storageSpec = "512GB Gen4 SSD",
                displaySpec = "15.6 inç 144Hz FHD IPS",
                batteryWh = 60,
                weightKg = 2.38,
                coolingTech = "Hyperchamber",
                gamingScore = 81,
                fps1080p = 105,
                fps1440p = 68,
            ),
        )

        val laptopReport = VsArenaEngine.compare(
            left = laptopA,
            right = laptopB,
            mode = VsArenaMode.LAPTOP_VS_LAPTOP,
        )
        assertEquals(ComparisonWinner.LEFT, laptopReport.performanceChampion.winner)
        assertTrue(laptopReport.rows.any { it.label.contains("TGP") })
    }
}