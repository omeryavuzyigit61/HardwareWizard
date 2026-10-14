package com.panda.myapplication

import com.panda.myapplication.domain.engine.HardwareSearchEngine
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.Wattage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HardwareSearchEngineTest {

    private val ryzen9 = HardwareItem(
        id = HardwareId("cpu_amd_ryzen_9_9900x"),
        brand = "AMD",
        model = "Ryzen 9 9900X",
        price = null,
        spec = CpuSpec(
            socket = "AM5",
            coreCount = 12,
            threadCount = 24,
            baseClock = Frequency(4400),
            boostClock = Frequency(5600),
            tdp = Wattage(120),
            hasIntegratedGraphics = true,
            architecture = "Zen 5",
            l2CacheMb = 12.0,
            l3CacheMb = 64,
            processNode = "TSMC 4nm FinFET",
            pcieVersion = "PCIe 5.0 (28 Hattı)",
            maxMemorySpeed = "DDR5-5600 MT/s",
            unlockedMultiplier = true,
        ),
    )

    private val ryzen7 = HardwareItem(
        id = HardwareId("cpu_amd_ryzen_7_9800x3d"),
        brand = "AMD",
        model = "Ryzen 7 9800X3D",
        price = null,
        spec = CpuSpec(
            socket = "AM5",
            coreCount = 8,
            threadCount = 16,
            baseClock = Frequency(4700),
            boostClock = Frequency(5200),
            tdp = Wattage(120),
            hasIntegratedGraphics = true,
            architecture = "Zen 5",
            l2CacheMb = 8.0,
            l3CacheMb = 96,
        ),
    )

    private val rtx4070Ti = HardwareItem(
        id = HardwareId("gpu_msi_geforce_rtx_4070_ti_super"),
        brand = "MSI",
        model = "GeForce RTX 4070 Ti Super 16G Ventus 3X",
        price = null,
        spec = GpuSpec(
            chipset = "GeForce RTX 4070 Ti SUPER",
            vram = MemorySize(16384),
            memoryType = "GDDR6X",
            coreClock = Frequency(2340),
            boostClock = Frequency(2655),
            tdp = Wattage(285),
            recommendedPsu = Wattage(750),
            busWidthBits = 256,
        ),
    )

    private val psuEdge = HardwareItem(
        id = HardwareId("psu_lian_li_edge_1000w"),
        brand = "Lian Li",
        model = "EDGE 1000W",
        price = null,
        spec = PsuSpec(
            wattage = Wattage(1000),
            efficiency = PsuEfficiencyRating.PLATINUM,
            isAtx3Compliant = true,
            supportsPcie5Power = true,
            modularity = "Tam Modüler (Full Modular)",
            tierRating = "Tier A (High-End)",
            atxStandard = "ATX 3.1 & PCIe 5.1",
        ),
    )

    @Test
    fun testR9QueryFindsRyzen9AtTop() {
        val query = "r9 9900x"
        val items = listOf(ryzen7, ryzen9, rtx4070Ti, psuEdge)

        assertTrue(HardwareSearchEngine.matches(ryzen9, query))
        assertFalse(HardwareSearchEngine.matches(ryzen7, query))

        val sorted = HardwareSearchEngine.search(items, query)
        assertEquals(1, sorted.size)
        assertEquals(ryzen9.id, sorted.first().id)
    }

    @Test
    fun testOrderIndependentMatching() {
        val query = "9900x r9"
        assertTrue(HardwareSearchEngine.matches(ryzen9, query))
    }

    @Test
    fun testR7ShortcutFinds9800X3D() {
        val query = "r7 9800"
        assertTrue(HardwareSearchEngine.matches(ryzen7, query))
        assertFalse(HardwareSearchEngine.matches(ryzen9, query))
    }

    @Test
    fun test4070TiConcatenatedMatching() {
        val query = "4070ti"
        assertTrue(HardwareSearchEngine.matches(rtx4070Ti, query))
    }

    @Test
    fun testWattageSearch() {
        val query = "1000w"
        assertTrue(HardwareSearchEngine.matches(psuEdge, query))
        assertFalse(HardwareSearchEngine.matches(ryzen9, query))
    }

    @Test
    fun testRyzen9Has64MbL3Cache() {
        val spec = ryzen9.spec as CpuSpec
        assertEquals(64, spec.l3CacheMb)
        assertEquals(12.0, spec.l2CacheMb)
    }

    @Test
    fun testLianLiPsuSpecAttributes() {
        val spec = psuEdge.spec as PsuSpec
        assertEquals("Tam Modüler (Full Modular)", spec.modularity)
        assertEquals("Tier A (High-End)", spec.tierRating)
        assertEquals("ATX 3.1 & PCIe 5.1", spec.atxStandard)
    }
}
