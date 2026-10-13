package com.panda.myapplication

import com.panda.myapplication.data.model.dto.GpuRawDto
import com.panda.myapplication.data.model.dto.RamRawDto
import com.panda.myapplication.data.model.dto.StorageRawDto
import com.panda.myapplication.domain.engine.HardwareEnrichmentEngine
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageFormFactor
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.domain.usecase.CalculatePsuRequirementUseCase
import com.panda.myapplication.presentation.hardware_list.HardwareSortOption
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HardwareEnrichmentAndStorageTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun storageRawDto_parsesHeterogeneousTypesWithoutCrashing() {
        // Sample from docyx/pc-part-dataset/internal-hard-drive.json with numeric form_factor and int type
        val rawJson = """
            [
                {
                    "name": "Seagate Barracuda Compute 2 TB",
                    "price": 49.99,
                    "capacity": 2000,
                    "type": 7200,
                    "cache": 256,
                    "form_factor": 3.5,
                    "interface": "SATA 6.0 Gb/s"
                },
                {
                    "name": "Samsung 990 Pro 2 TB",
                    "price": 179.99,
                    "capacity": "2 TB",
                    "type": "SSD",
                    "cache": 2048,
                    "form_factor": "M.2-2280",
                    "interface": "M.2 PCIe 4.0 X4"
                },
                {
                    "name": "Kingston NV2 500 GB",
                    "price": 39.99,
                    "capacity": "500 GB",
                    "type": "SSD",
                    "cache": null,
                    "form_factor": "M.2-2280",
                    "interface": "PCIe 4.0 X4"
                }
            ]
        """.trimIndent()

        val parsed = json.decodeFromString<List<StorageRawDto>>(rawJson)
        assertEquals(3, parsed.size)

        assertEquals("Seagate Barracuda Compute 2 TB", parsed[0].name)
        assertEquals(2000, parsed[0].capacity)
        assertEquals("7200", parsed[0].type)
        assertEquals("3.5", parsed[0].formFactor)

        assertEquals("Samsung 990 Pro 2 TB", parsed[1].name)
        assertEquals(2000, parsed[1].capacity)
        assertEquals("SSD", parsed[1].type)
        assertEquals("M.2-2280", parsed[1].formFactor)

        assertEquals("Kingston NV2 500 GB", parsed[2].name)
        assertEquals(500, parsed[2].capacity)
    }

    @Test
    fun calculatePsuRequirement_handlesEmptyBuildSafely() {
        val useCase = CalculatePsuRequirementUseCase()
        val emptyResult = useCase(emptyList())

        assertEquals(0, emptyResult.estimatedLoad.watts)
        assertEquals(0, emptyResult.recommendedWattage.watts)
    }

    @Test
    fun hardwareEnrichmentEngine_generatesDeepEngineeringBadgesForAllCategories() {
        val psu = HardwareItem(
            id = HardwareId("psu-1"),
            brand = "Corsair",
            model = "RM850x Shift ATX 3.0",
            price = Money(14999L, "USD"),
            spec = PsuSpec(
                wattage = Wattage(850),
                efficiency = PsuEfficiencyRating.GOLD,
                isAtx3Compliant = true,
                supportsPcie5Power = true,
                modularity = "Full Modüler",
            ),
        )

        val psuBadges = HardwareEnrichmentEngine.getEngineeringBadges(psu)
        assertTrue(psuBadges.any { it.label == "Kapasitör Markası" && it.value.contains("Nippon Chemi-Con") })
        assertTrue(psuBadges.any { it.label == "Güç Standardı" && it.value.contains("ATX 3.1 & PCIe 5.1") })
        assertTrue(psuBadges.any { it.label == "Koruma Devreleri" })

        val cpu = HardwareItem(
            id = HardwareId("cpu-1"),
            brand = "AMD",
            model = "Ryzen 7 7800X3D",
            price = Money(44900L, "USD"),
            spec = CpuSpec(
                socket = "AM5",
                coreCount = 8,
                threadCount = 16,
                baseClock = Frequency(4200),
                boostClock = Frequency(5000),
                tdp = Wattage(120),
                hasIntegratedGraphics = true,
                l3CacheMb = 96,
                architecture = "Zen 4",
            ),
        )

        val cpuBadges = HardwareEnrichmentEngine.getEngineeringBadges(cpu)
        assertTrue(cpuBadges.any { it.label == "L3 Önbellek Yapısı" && it.value.contains("3D V-Cache") })
        assertTrue(cpuBadges.any { it.label.contains("IMC Sweet Spot") })

        val ram = HardwareItem(
            id = HardwareId("ram-1"),
            brand = "G.Skill",
            model = "Trident Z5 Neo RGB 32GB DDR5-6000 CL30",
            price = Money(12000L, "USD"),
            spec = RamSpec(
                generation = DdrGeneration.DDR5,
                speed = Frequency(6000),
                casLatency = 30,
                capacity = MemorySize(32768),
                moduleCount = 2,
            ),
        )

        val ramBadges = HardwareEnrichmentEngine.getEngineeringBadges(ram)
        assertTrue(ramBadges.any { it.label == "DRAM Yonga (Die) Tipi" && it.value.contains("SK Hynix A-die") })

        val ssd = HardwareItem(
            id = HardwareId("ssd-1"),
            brand = "Samsung",
            model = "990 Pro 2TB NVMe M.2",
            price = Money(17999L, "USD"),
            spec = StorageSpec(
                mediaType = StorageMediaType.SSD,
                storageInterface = StorageInterface.NVME,
                formFactor = StorageFormFactor.M2_2280,
                pcieGeneration = PcieGeneration.GEN4,
                capacity = MemorySize(2000000),
                sequentialReadMbps = 7450,
                sequentialWriteMbps = 6900,
            ),
        )

        val ssdBadges = HardwareEnrichmentEngine.getEngineeringBadges(ssd)
        assertTrue(ssdBadges.any { it.label == "NAND Kontrolcü Yongası" && it.value.contains("Samsung") })
        assertTrue(ssdBadges.any { it.label == "DRAM Önbellek Durumu" && it.value.contains("LPDDR4") })
    }

    @Test
    fun ramRawDto_parsesHeterogeneousSpeedAndModulesWithoutCrashing() {
        val rawJson = """
            [
                {
                    "name": "Corsair Vengeance RGB 32 GB",
                    "price": 94.99,
                    "speed": [5, 6000],
                    "modules": [2, 16],
                    "cas_latency": 36
                },
                {
                    "name": "G.Skill Flare X5 32 GB",
                    "price": 89.99,
                    "speed": 6000,
                    "modules": [2, 16],
                    "cas_latency": 30
                },
                {
                    "name": "Kingston FURY Beast 16 GB",
                    "price": 49.99,
                    "speed": "DDR5-5600",
                    "modules": "2 x 8GB",
                    "cas_latency": "36"
                },
                {
                    "name": "Crucial Pro 32 GB",
                    "price": 79.99,
                    "speed": "3200",
                    "modules": 2,
                    "cas_latency": 16
                }
            ]
        """.trimIndent()

        val parsed = json.decodeFromString<List<RamRawDto>>(rawJson)
        assertEquals(4, parsed.size)

        assertEquals(listOf(5, 6000), parsed[0].speed)
        assertEquals(listOf(2, 16), parsed[0].modules)

        assertEquals(listOf(5, 6000), parsed[1].speed)

        assertEquals(listOf(5, 5600), parsed[2].speed)
        assertEquals(listOf(2, 8), parsed[2].modules)
        assertEquals(36, parsed[2].casLatency)

        assertEquals(listOf(4, 3200), parsed[3].speed)
        assertEquals(listOf(1, 2), parsed[3].modules)
        assertEquals(16, parsed[3].casLatency)
    }

    @Test
    fun gpuRawDto_parsesHeterogeneousTypesWithoutCrashing() {
        val rawJson = """
            [
                {
                    "name": "MSI GeForce RTX 4070 Ventus",
                    "price": 549.99,
                    "chipset": "GeForce RTX 4070",
                    "memory": 12,
                    "core_clock": 1920,
                    "boost_clock": 2475,
                    "length": 242
                },
                {
                    "name": "Asus TUF Gaming RTX 4080",
                    "price": 1199.99,
                    "chipset": "GeForce RTX 4080",
                    "memory": "16 GB",
                    "core_clock": "2205",
                    "boost_clock": "2505",
                    "length": "348 mm"
                }
            ]
        """.trimIndent()

        val parsed = json.decodeFromString<List<GpuRawDto>>(rawJson)
        assertEquals(2, parsed.size)
        assertEquals(12, parsed[0].memory)
        assertEquals(242.0, parsed[0].length)

        assertEquals(16, parsed[1].memory)
        assertEquals(2205, parsed[1].coreClockMhz)
        assertEquals(348.0, parsed[1].length)
    }

    @Test
    fun resilientJsonDecoding_skipsCorruptedItemsWithoutDroppingList() {
        val mixedJson = """
            [
                {
                    "name": "Valid GPU 1",
                    "chipset": "RTX 4070",
                    "memory": 12
                },
                {
                    "name": 12345,
                    "corrupted_structure": true
                },
                {
                    "name": "Valid GPU 2",
                    "chipset": "RX 7800 XT",
                    "memory": 16
                }
            ]
        """.trimIndent()

        val jsonArray = json.parseToJsonElement(mixedJson) as JsonArray
        val validItems = jsonArray.mapNotNull { element ->
            runCatching { json.decodeFromJsonElement<GpuRawDto>(element) }.getOrNull()
        }

        // Even with 1 corrupted record, both valid GPUs are preserved!
        assertTrue(validItems.size >= 2)
        assertTrue(validItems.any { it.name == "Valid GPU 1" })
        assertTrue(validItems.any { it.name == "Valid GPU 2" })
    }

    @Test
    fun dynamicSorting_returnsOnlyCategoryRelevantOptions() {
        val cpuSorts = HardwareSortOption.optionsForCategory(HardwareCategory.CPU)
        assertTrue(cpuSorts.contains(HardwareSortOption.CPU_CORES_DESC))
        assertTrue(cpuSorts.contains(HardwareSortOption.CPU_TDP_DESC))
        assertFalse(cpuSorts.contains(HardwareSortOption.RAM_SPEED_DESC))
        assertFalse(cpuSorts.contains(HardwareSortOption.STORAGE_READ_SPEED_DESC))

        val ramSorts = HardwareSortOption.optionsForCategory(HardwareCategory.RAM)
        assertTrue(ramSorts.contains(HardwareSortOption.RAM_SPEED_DESC))
        assertTrue(ramSorts.contains(HardwareSortOption.RAM_CL_ASC))
        // RAM must NOT have TDP sort options!
        assertFalse(ramSorts.contains(HardwareSortOption.CPU_TDP_ASC))
        assertFalse(ramSorts.contains(HardwareSortOption.CPU_TDP_DESC))

        val moboSorts = HardwareSortOption.optionsForCategory(HardwareCategory.MOTHERBOARD)
        assertTrue(moboSorts.contains(HardwareSortOption.MOTHERBOARD_M2_SLOTS_DESC))
        assertTrue(moboSorts.contains(HardwareSortOption.MOTHERBOARD_RAM_SLOTS_DESC))
        // Motherboard must NOT have TDP sort options!
        assertFalse(moboSorts.contains(HardwareSortOption.CPU_TDP_ASC))

        val psuSorts = HardwareSortOption.optionsForCategory(HardwareCategory.PSU)
        assertTrue(psuSorts.contains(HardwareSortOption.PSU_WATTAGE_DESC))
    }

    @Test
    fun gpuDtoToEntity_promotesChipsetWhenModelIsFactoryPartNumber() {
        val dto = GpuRawDto(
            name = "AMD 100-435415",
            chipset = "Radeon RX 6950 XT",
            memory = 16,
            price = 899.99,
        )
        val pair = com.panda.myapplication.data.mapper.HardwareMapper.gpuDtoToEntity(dto, kotlin.time.Instant.DISTANT_PAST)
        kotlin.test.assertNotNull(pair)
        val (itemEntity, gpuSpecEntity) = pair

        assertEquals("AMD", itemEntity.brand)
        assertEquals("Radeon RX 6950 XT", itemEntity.model)
        assertEquals("100-435415", gpuSpecEntity.partNumber)

        val domainItem = com.panda.myapplication.data.mapper.HardwareMapper.toDomain(
            com.panda.myapplication.data.local.entity.HardwareItemWithSpec(
                item = itemEntity,
                gpu = gpuSpecEntity,
            )
        )
        kotlin.test.assertNotNull(domainItem)
        assertEquals("AMD Radeon RX 6950 XT", domainItem.displayName)
        assertEquals("100-435415", (domainItem.spec as com.panda.myapplication.domain.model.GpuSpec).partNumber)
    }

    @Test
    fun gpuDtoToEntity_preservesMarketingBrandAndChipset() {
        val dto = GpuRawDto(
            name = "MSI GeForce RTX 4070 Ti SUPER Ventus 3X",
            chipset = "GeForce RTX 4070 Ti SUPER",
            memory = 16,
            price = 799.99,
        )
        val pair = com.panda.myapplication.data.mapper.HardwareMapper.gpuDtoToEntity(dto, kotlin.time.Instant.DISTANT_PAST)
        kotlin.test.assertNotNull(pair)
        val (itemEntity, gpuSpecEntity) = pair

        assertEquals("MSI", itemEntity.brand)
        assertEquals("GeForce RTX 4070 Ti SUPER Ventus 3X", itemEntity.model)
        kotlin.test.assertNull(gpuSpecEntity.partNumber)
    }

    @Test
    fun benchmarkInfo_parsesAndEnrichesCorrectly() {
        val sampleJson = """
            {
              "cpu_amd_ryzen_7_7800x3d": {
                "score": 98,
                "singleCoreScore": 1820,
                "multiCoreScore": 18500,
                "gamingIndex": 99,
                "tier": "S-Tier",
                "testSuite": "Cinebench R23"
              }
            }
        """.trimIndent()

        val benchmarks = json.decodeFromString<Map<String, com.panda.myapplication.domain.model.HardwareBenchmarkInfo>>(sampleJson)
        assertEquals(1, benchmarks.size)
        val b = benchmarks["cpu_amd_ryzen_7_7800x3d"]
        kotlin.test.assertNotNull(b)
        assertEquals(98, b.score)
        assertEquals("S-Tier", b.tier)
    }

    @Test
    fun benchmarkEngine_usesVerifiedWhenPresentAndSimulatesWhenAbsent() {
        val cpuVerified = HardwareItem(
            id = HardwareId("cpu_amd_ryzen_7_7800x3d"),
            brand = "AMD",
            model = "Ryzen 7 7800X3D",
            price = Money(44900L, "USD"),
            spec = CpuSpec(
                socket = "AM5",
                coreCount = 8,
                threadCount = 16,
                baseClock = Frequency(4200),
                boostClock = Frequency(5000),
                tdp = Wattage(120),
                hasIntegratedGraphics = true,
                l3CacheMb = 96,
                architecture = "Zen 4",
            ),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 98,
                tier = "S-Tier",
                relativePerformanceIndex = 100.0,
                dataSource = com.panda.myapplication.domain.model.DataSourceType.VERIFIED_BENCHMARK.name,
            ),
        )

        val verifiedResult = com.panda.myapplication.domain.engine.BenchmarkEngine.evaluate(cpuVerified)
        assertEquals(com.panda.myapplication.domain.model.DataSourceType.VERIFIED_BENCHMARK, verifiedResult.dataSource)
        assertEquals(98, verifiedResult.info.score)
        assertEquals(100.0, verifiedResult.info.relativePerformanceIndex)

        val cpuUnknown = HardwareItem(
            id = HardwareId("cpu_custom_prototype"),
            brand = "Custom",
            model = "Experimental 16-Core",
            price = Money(50000L, "USD"),
            spec = CpuSpec(
                socket = "AM5",
                coreCount = 16,
                threadCount = 32,
                baseClock = Frequency(4000),
                boostClock = Frequency(5400),
                tdp = Wattage(170),
                hasIntegratedGraphics = false,
                l3CacheMb = 64,
                architecture = "Zen 5",
            ),
            benchmarkInfo = null,
        )

        val estimatedResult = com.panda.myapplication.domain.engine.BenchmarkEngine.evaluate(cpuUnknown)
        assertEquals(com.panda.myapplication.domain.model.DataSourceType.ESTIMATED_SIMULATION, estimatedResult.dataSource)
        assertTrue(estimatedResult.info.score >= 80)
        kotlin.test.assertNotNull(estimatedResult.info.cpuSynthetic?.cinebenchR23Multi)
        kotlin.test.assertNotNull(estimatedResult.info.gameFps?.cyberpunk2077)
    }
}
