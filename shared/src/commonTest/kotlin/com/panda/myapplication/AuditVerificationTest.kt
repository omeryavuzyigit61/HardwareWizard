package com.panda.myapplication

import com.panda.myapplication.data.local.FastSettingsStore
import com.panda.myapplication.data.repository.ActiveBuildRepositoryImpl
import com.panda.myapplication.data.repository.UserRepositoryImpl
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.engine.BenchmarkEngine
import com.panda.myapplication.domain.engine.BottleneckType
import com.panda.myapplication.domain.engine.BuildAnalysisEngine
import com.panda.myapplication.domain.engine.ResolutionOption
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.FpsTarget
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.ResolutionTarget
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.model.TemperatureUnit
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.domain.usecase.CalculatePsuRequirementUseCase
import com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase
import com.panda.myapplication.domain.usecase.GetCompatibleHardwareForSlotUseCase
import com.panda.myapplication.presentation.util.formatLengthDeltaMm
import com.panda.myapplication.presentation.util.formatLengthMm
import com.panda.myapplication.presentation.util.formatTemperature
import com.panda.myapplication.presentation.util.meetsTarget
import com.panda.myapplication.presentation.util.toGamingResolution
import com.panda.myapplication.presentation.util.toResolutionOption
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuditVerificationTest {

    private val cpu9800X3D = HardwareItem(
        id = HardwareId("cpu-9800x3d"),
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
            l3CacheMb = 96,
        ),
    )

    private val cpuBudget = HardwareItem(
        id = HardwareId("cpu-i3-10100"),
        brand = "Intel",
        model = "Core i3-10100F",
        price = null,
        spec = CpuSpec(
            socket = "LGA1200",
            coreCount = 4,
            threadCount = 8,
            baseClock = Frequency(3600),
            boostClock = Frequency(4300),
            tdp = Wattage(65),
            hasIntegratedGraphics = false,
            l3CacheMb = 6,
        ),
    )

    private val gpu4060Ti16 = HardwareItem(
        id = HardwareId("gpu-4060ti-16"),
        brand = "NVIDIA",
        model = "GeForce RTX 4060 Ti 16GB",
        price = null,
        spec = GpuSpec(
            chipset = "RTX 4060 Ti",
            vram = MemorySize(16384),
            memoryType = "GDDR6",
            tdp = Wattage(165),
            recommendedPsu = Wattage(550),
            lengthMm = 240,
            coreClock = Frequency(2310),
            boostClock = Frequency(2535),
        ),
    )

    private val gpu5090 = HardwareItem(
        id = HardwareId("gpu-5090"),
        brand = "NVIDIA",
        model = "GeForce RTX 5090 32GB",
        price = null,
        spec = GpuSpec(
            chipset = "RTX 5090",
            vram = MemorySize(32768),
            memoryType = "GDDR7",
            tdp = Wattage(575),
            recommendedPsu = Wattage(1000),
            lengthMm = 358,
            coreClock = Frequency(2017),
            boostClock = Frequency(2407),
        ),
    )

    @Test
    fun flagshipCpuAndGpuAreBalancedEvenAt1080p() {
        val build = ActiveBuild(cpu = cpu9800X3D, gpu = gpu5090)
        val b1080 = BuildAnalysisEngine.calculateBottleneckForResolution(build, ResolutionOption.P1080)
        val b1440 = BuildAnalysisEngine.calculateBottleneckForResolution(build, ResolutionOption.P1440)
        val b4k = BuildAnalysisEngine.calculateBottleneckForResolution(build, ResolutionOption.P2160)

        assertEquals(BottleneckType.BALANCED, b1080.type, "9800X3D + RTX 5090 must not trigger false CPU bottleneck at 1080p: $b1080")
        assertEquals(BottleneckType.BALANCED, b1440.type)
        assertEquals(BottleneckType.BALANCED, b4k.type)
    }

    @Test
    fun midRangeGpuDoesNotEqualFlagshipGpuInBottleneckCalculation() {
        val eval4060Ti = BenchmarkEngine.evaluate(gpu4060Ti16)
        val eval5090 = BenchmarkEngine.evaluate(gpu5090)
        assertTrue(
            eval4060Ti.info.score < eval5090.info.score,
            "RTX 4060 Ti 16GB (${eval4060Ti.info.score}) must score strictly lower than RTX 5090 (${eval5090.info.score})",
        )

        val build4060Ti = ActiveBuild(cpu = cpu9800X3D, gpu = gpu4060Ti16)
        val build5090 = ActiveBuild(cpu = cpu9800X3D, gpu = gpu5090)

        val b4060Ti4k = BuildAnalysisEngine.calculateBottleneckForResolution(build4060Ti, ResolutionOption.P2160)
        val b50904k = BuildAnalysisEngine.calculateBottleneckForResolution(build5090, ResolutionOption.P2160)

        assertEquals(BottleneckType.GPU_BOTTLENECK, b4060Ti4k.type, "9800X3D + RTX 4060 Ti at 4K should show GPU bottleneck: $b4060Ti4k")
        assertTrue(b4060Ti4k.percentage > b50904k.percentage, "4060 Ti 4K bottleneck (${b4060Ti4k.percentage}%) must exceed RTX 5090 (${b50904k.percentage}%)")
    }

    @Test
    fun weakCpuWithFlagshipGpuFlagsCpuBottleneckAt1080p() {
        val build = ActiveBuild(cpu = cpuBudget, gpu = gpu5090)
        val b1080 = BuildAnalysisEngine.calculateBottleneckForResolution(build, ResolutionOption.P1080)
        assertEquals(BottleneckType.CPU_BOTTLENECK, b1080.type, "i3-10100F + RTX 5090 must detect CPU bottleneck at 1080p: $b1080")
        assertTrue(b1080.percentage >= 15)
    }

    @Test
    fun wattageCockpitMatchesPsuRequirementUseCaseSingleSourceOfTruth() {
        val psuUseCase = CalculatePsuRequirementUseCase()
        val build = ActiveBuild(cpu = cpu9800X3D, gpu = gpu5090)
        val req = psuUseCase(build.items)
        val cockpit = BuildAnalysisEngine.calculateWattageCockpit(build)

        assertEquals(req.estimatedLoad.watts, cockpit.componentTdpSum)
        assertEquals(req.recommendedWattage.watts, cockpit.recommendedPsuWattage)
    }

    @Test
    fun cpuEsportsLow1PercentFpsIsRealistic() {
        val matrix = BenchmarkEngine.resolveGameFpsForItem(cpu9800X3D)
        val cs2 = assertNotNull(matrix?.counterStrike2)
        val avg = cs2.avgFps(GamingResolution.P1080)
        val low1 = cs2.low1PctFps(GamingResolution.P1080)
        val ratio = low1.toDouble() / avg.toDouble()
        assertTrue(ratio in 0.65..0.85, "CPU CS2 1% Low/Avg ratio should be realistic (0.65..0.85), got $ratio ($low1/$avg)")
    }

    @Test
    fun compatibilityChecksAirCoolerHeightFormFactorRamSlotsAndAtx3() {
        val compatUseCase = CheckBuildCompatibilityUseCase(CalculatePsuRequirementUseCase())
        val slotUseCase = GetCompatibleHardwareForSlotUseCase()

        val eAtxMobo = HardwareItem(
            id = HardwareId("mobo-eatx"),
            brand = "ASUS",
            model = "ROG Crosshair Extreme",
            price = null,
            spec = MotherboardSpec(
                socket = "AM5",
                formFactor = MotherboardFormFactor.E_ATX,
                chipset = "X870E",
                memoryGeneration = DdrGeneration.DDR5,
                ramSlots = 2,
                pcieGeneration = PcieGeneration.GEN5,
                maxMemoryGb = 96,
            ),
        )
        val quadRam = HardwareItem(
            id = HardwareId("ram-4x32"),
            brand = "G.Skill",
            model = "Trident Z5 128GB (4x32GB)",
            price = null,
            spec = RamSpec(
                generation = DdrGeneration.DDR5,
                speed = Frequency(6000),
                casLatency = 30,
                capacity = MemorySize(131072),
                moduleCount = 4,
            ),
        )
        val tallAirCooler = HardwareItem(
            id = HardwareId("cooler-tall"),
            brand = "Noctua",
            model = "NH-D15 G2",
            price = null,
            spec = CoolerSpec(
                type = CoolerType.AIR,
                maxCoolingTdp = Wattage(250),
                radiatorSizeMm = null,
                heightMm = 168,
                supportedSockets = listOf("AM5"),
            ),
        )
        val miniItxCase = HardwareItem(
            id = HardwareId("case-itx"),
            brand = "Cooler Master",
            model = "NR200P",
            price = null,
            spec = CaseSpec(
                type = "Mini-ITX",
                maxGpuLengthMm = 330,
                maxCpuCoolerHeightMm = 155,
                maxRadiatorSizeMm = 280,
            ),
        )
        val oldPsu = HardwareItem(
            id = HardwareId("psu-atx2"),
            brand = "Corsair",
            model = "RM1000x (2021)",
            price = null,
            spec = PsuSpec(
                wattage = Wattage(1000),
                efficiency = PsuEfficiencyRating.GOLD,
                isAtx3Compliant = false,
                supportsPcie5Power = false,
            ),
        )

        val badBuild = ActiveBuild(
            cpu = cpu9800X3D,
            motherboard = eAtxMobo,
            ram = quadRam,
            gpu = gpu5090,
            cooler = tallAirCooler,
            case = miniItxCase,
            psu = oldPsu,
        )

        val (_, report) = compatUseCase(badBuild)
        assertFalse(report.isCompatible)
        assertNotNull(report.ramMismatch, "Should flag 4-module / 128GB RAM exceeding 2-slot / 96GB motherboard")
        assertTrue(report.ramMismatch.contains("Yuvası") && report.ramMismatch.contains("Kapasite"))
        assertNotNull(report.clearanceMismatch, "Should flag tall air cooler, long GPU, and E-ATX in Mini-ITX")
        assertTrue(report.clearanceMismatch.contains("Hava Soğutucu"))
        assertTrue(report.clearanceMismatch.contains("Anakart Kasa Uyumsuzluğu"))
        assertNotNull(report.psuWarning, "Should flag non-ATX 3.0 PSU paired with RTX 5090")
        assertTrue(report.psuWarning.contains("12V-2x6"))

        val filteredMobos = slotUseCase(HardwareCategory.MOTHERBOARD, ActiveBuild(case = miniItxCase), listOf(eAtxMobo))
        assertTrue(filteredMobos.items.isEmpty(), "Mini-ITX case must filter out E-ATX motherboard")

        val filteredCoolers = slotUseCase(HardwareCategory.COOLER, ActiveBuild(case = miniItxCase), listOf(tallAirCooler))
        assertTrue(filteredCoolers.items.isEmpty(), "Case max 155mm must filter out 168mm air cooler")

        val filteredRam = slotUseCase(HardwareCategory.RAM, ActiveBuild(motherboard = eAtxMobo), listOf(quadRam))
        assertTrue(filteredRam.items.isEmpty(), "2-slot motherboard must filter out 4-module RAM kit")
    }

    @Test
    fun unitFormattersConvertTemperatureLengthResolutionAndFpsTarget() {
        assertEquals("75 °C", formatTemperature(75, TemperatureUnit.CELSIUS))
        assertEquals("167 °F", formatTemperature(75, TemperatureUnit.FAHRENHEIT))
        assertEquals("330mm", formatLengthMm(330, LengthUnit.METRIC))
        assertEquals("13.0 in", formatLengthMm(330, LengthUnit.IMPERIAL))
        assertEquals("+1.0 in", formatLengthDeltaMm(25, LengthUnit.IMPERIAL))
        assertEquals("-0.5 in", formatLengthDeltaMm(-13, LengthUnit.IMPERIAL))

        assertEquals(ResolutionOption.P1080, ResolutionTarget.FHD_1080P.toResolutionOption())
        assertEquals(ResolutionOption.P2160, ResolutionTarget.UHD_4K.toResolutionOption())
        assertEquals(GamingResolution.P4K, ResolutionTarget.UHD_4K.toGamingResolution())
        assertTrue(FpsTarget.FPS_144.meetsTarget(160))
        assertFalse(FpsTarget.FPS_240.meetsTarget(160))
    }

    @Test
    fun activeBuildRepositoryPersistsSlotIdsToFastSettingsStore() {
        val backingMap = mutableMapOf<String, String>()
        val fakeStore = object : FastSettingsStore {
            override fun getString(key: String, default: String?): String? = backingMap[key] ?: default
            override fun setString(key: String, value: String) {
                backingMap[key] = value
            }
        }
        val repo = ActiveBuildRepositoryImpl(
            hardwareRepository = null,
            fastSettingsStore = fakeStore,
            ioDispatcher = Dispatchers.Unconfined,
        )
        repo.setComponent(cpu9800X3D)
        repo.setComponent(gpu5090)

        val storedJson = backingMap["active_build_slots_v1"]
        assertNotNull(storedJson)
        assertTrue(storedJson.contains("cpu-9800x3d"))
        assertTrue(storedJson.contains("gpu-5090"))

        repo.clearBuild()
        assertEquals("{}", backingMap["active_build_slots_v1"])
    }

    @Test
    fun userRepositoryGeneratesEntropyIdAndRejectsDuplicateRegistration() = runTest {
        val repo = UserRepositoryImpl(
            httpClient = HttpClient(),
            json = Json { ignoreUnknownKeys = true },
            ioDispatcher = Dispatchers.Unconfined,
            remoteSyncEnabled = false,
        )

        val first = repo.signUp(
            email = "audit_user@hardwarewizard.com",
            password = "StrongPassword99!",
            username = "audit_user",
            birthDateIso = "1999-05-05",
            gender = Gender.MALE,
            spiritHardware = SpiritHardware.OVERCLOCKED_CPU,
        )
        assertTrue(first is DomainResult.Success)
        assertTrue(first.data.id.startsWith("usr-") && first.data.id.length == 20, "ID should be 16-hex entropy format: ${first.data.id}")

        val duplicate = repo.signUp(
            email = "audit_user@hardwarewizard.com",
            password = "AnotherPassword99!",
            username = "audit_user_2",
            birthDateIso = "1999-05-05",
            gender = Gender.MALE,
            spiritHardware = SpiritHardware.FLAGSHIP_GPU,
        )
        assertTrue(duplicate is DomainResult.Failure, "Duplicate email must fail registration instead of silent success")
    }
}
