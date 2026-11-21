package com.panda.myapplication

import com.panda.myapplication.data.local.FastSettingsStore
import com.panda.myapplication.data.repository.ActiveBuildRepositoryImpl
import com.panda.myapplication.data.repository.SettingsRepositoryImpl
import com.panda.myapplication.data.repository.UserRepositoryImpl
import com.panda.myapplication.domain.common.DomainResult
import com.panda.myapplication.domain.engine.BenchmarkEngine
import com.panda.myapplication.domain.engine.BottleneckType
import com.panda.myapplication.domain.engine.BuildAnalysisEngine
import com.panda.myapplication.domain.engine.ComparisonWinner
import com.panda.myapplication.domain.engine.ResolutionOption
import com.panda.myapplication.domain.engine.VsArenaEngine
import com.panda.myapplication.domain.model.AccessPointSpec
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CaseFanSpec
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.FpsTarget
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GamingDeskSpec
import com.panda.myapplication.domain.model.GamingLightingSpec
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.Gender
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.LengthUnit
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.ModemSpec
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.RangeExtenderSpec
import com.panda.myapplication.domain.model.ResolutionTarget
import com.panda.myapplication.domain.model.RouterSpec
import com.panda.myapplication.domain.model.SmartwatchSpec
import com.panda.myapplication.domain.model.SpiritHardware
import com.panda.myapplication.domain.model.TemperatureUnit
import com.panda.myapplication.domain.model.UpsSpec
import com.panda.myapplication.domain.model.UserProfile
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.domain.repository.VsArenaMode
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

    @Test
    fun intelNumericSocketsMatchLgaMotherboardsAndCoolers() {
        val compatUseCase = CheckBuildCompatibilityUseCase(CalculatePsuRequirementUseCase())
        val slotUseCase = GetCompatibleHardwareForSlotUseCase()

        val intelCpuBareSocket = HardwareItem(
            id = HardwareId("cpu-14700k"),
            brand = "Intel",
            model = "Core i7-14700K",
            price = null,
            spec = CpuSpec(
                socket = "1700", // Bare numeric socket format from legacy dataset
                coreCount = 20,
                threadCount = 28,
                baseClock = Frequency(3400),
                boostClock = Frequency(5600),
                tdp = Wattage(125),
                hasIntegratedGraphics = true,
            ),
        )
        val lga1700Mobo = HardwareItem(
            id = HardwareId("mobo-z790"),
            brand = "ASUS",
            model = "ROG STRIX Z790-F",
            price = null,
            spec = MotherboardSpec(
                socket = "LGA1700",
                formFactor = MotherboardFormFactor.ATX,
                chipset = "Z790",
                memoryGeneration = DdrGeneration.DDR5,
                ramSlots = 4,
                pcieGeneration = PcieGeneration.GEN5,
            ),
        )
        val cooler = HardwareItem(
            id = HardwareId("cooler-aio"),
            brand = "Arctic",
            model = "Liquid Freezer III 360",
            price = null,
            spec = CoolerSpec(
                type = CoolerType.LIQUID,
                maxCoolingTdp = Wattage(300),
                radiatorSizeMm = 360,
                supportedSockets = listOf("AM5", "LGA1700", "LGA1851"),
            ),
        )

        val build = ActiveBuild(cpu = intelCpuBareSocket, motherboard = lga1700Mobo, cooler = cooler)
        val (_, report) = compatUseCase(build)
        assertTrue(report.isCompatible, "Socket '1700' must match 'LGA1700' without false warnings: ${report.warnings}")

        val platform = BuildAnalysisEngine.calculatePlatformStatus(build)
        assertTrue(platform.isCompatible, "Platform status must recognize 1700 == LGA1700")

        val moboCandidates = slotUseCase(HardwareCategory.MOTHERBOARD, ActiveBuild(cpu = intelCpuBareSocket), listOf(lga1700Mobo))
        assertEquals(1, moboCandidates.items.size, "Motherboard slot picker must return LGA1700 board for '1700' CPU")

        val coolerCandidates = slotUseCase(HardwareCategory.COOLER, ActiveBuild(cpu = intelCpuBareSocket), listOf(cooler))
        assertEquals(1, coolerCandidates.items.size, "Cooler slot picker must return LGA1700 cooler for '1700' CPU")
    }

    @Test
    fun vsArenaGuardsZeroPollingRateAndZeroWeightAndCoversAll9NewCategories() {
        val unknownMouse = HardwareItem(
            id = HardwareId("m-zero"),
            brand = "Generic",
            model = "Unknown Spec Mouse",
            price = null,
            spec = MouseSpec(sensor = "PAW3395", pollingRateHz = 0, weightG = 0, maxDpi = 0),
        )
        val proMouse = HardwareItem(
            id = HardwareId("m-pro"),
            brand = "Logitech",
            model = "G Pro X Superlight 2",
            price = null,
            spec = MouseSpec(sensor = "HERO 2", pollingRateHz = 8000, weightG = 60, maxDpi = 44000),
        )

        val mouseReport = VsArenaEngine.compare(unknownMouse, proMouse, VsArenaMode.COMPONENT_VS_COMPONENT)
        val pollingRow = mouseReport.rows.first { it.label.contains("Polling", ignoreCase = true) }
        assertFalse(pollingRow.leftValue.contains("Infinity"), "0 Hz polling rate must never display Infinity ms: ${pollingRow.leftValue}")
        assertEquals("Belirtilmedi", pollingRow.leftValue)

        val weightRow = mouseReport.rows.first { it.label.contains("Ağırlık", ignoreCase = true) }
        assertEquals(ComparisonWinner.NEUTRAL, weightRow.winner, "0g unknown mouse must never win Ultra-Light against 60g mouse")

        // Verify all 9 newly added VS Arena categories return real rows & non-tied performance scores
        val pairs = listOf(
            HardwareItem(HardwareId("cf1"), "Noctua", "NF-A12x25", null, spec = CaseFanSpec(120, 450, 2000, 60.1, 2.34, 22.6, "SSO2")) to
                HardwareItem(HardwareId("cf2"), "Phanteks", "T30-120", null, spec = CaseFanSpec(120, 500, 3000, 101.0, 7.11, 39.7, "Dual Vapo")),
            HardwareItem(HardwareId("u1"), "APC", "Back-UPS 850VA", null, spec = UpsSpec(850, 480, "Line-Interactive", "Simüle Sinüs", outletCount = 4)) to
                HardwareItem(HardwareId("u2"), "Eaton", "9PX 3000VA", null, spec = UpsSpec(3000, 2700, "Online", "Tam Sinüs", outletCount = 8)),
            HardwareItem(HardwareId("d1"), "IKEA", "Fredde", null, spec = GamingDeskSpec(140, 74, false, maxLoadKg = 60)) to
                HardwareItem(HardwareId("d2"), "Secretlab", "Magnus Pro XL", null, spec = GamingDeskSpec(177, 80, true, 65, 125, 120, hasCableManagement = true)),
            HardwareItem(HardwareId("l1"), "Basic", "LED Strip", null, spec = GamingLightingSpec("Strip", 400, "3000K", 80)) to
                HardwareItem(HardwareId("l2"), "Govee", "AI Sync Box 2", null, spec = GamingLightingSpec("Sync Bar", 1500, "2700K-6500K", 95, hasScreenMirroring = true)),
            HardwareItem(HardwareId("sw1"), "BrandA", "Watch SE", null, spec = SmartwatchSpec(1.6, "AMOLED", 1000, 2, 5, "WatchOS")) to
                HardwareItem(HardwareId("sw2"), "Garmin", "Fenix 8", null, spec = SmartwatchSpec(1.4, "AMOLED", 2000, 16, 10, "GarminOS")),
            HardwareItem(HardwareId("rt1"), "TP-Link", "Archer C6", null, spec = RouterSpec("Wi-Fi 5", "AC1200", "Dual-Band")) to
                HardwareItem(HardwareId("rt2"), "ASUS", "ROG Rapture GT-BE98", null, spec = RouterSpec("Wi-Fi 7", "BE25000", "Quad-Band", hasGamingQos = true, hasSqmBufferbloat = true)),
            HardwareItem(HardwareId("md1"), "Keenetic", "Omni DSL", null, spec = ModemSpec("VDSL2 17a", false, "Wi-Fi 4")) to
                HardwareItem(HardwareId("md2"), "Keenetic", "Hopper DSL", null, spec = ModemSpec("VDSL2 35b", true, "Wi-Fi 6")),
            HardwareItem(HardwareId("ap1"), "Ubiquiti", "U6 Lite", null, spec = AccessPointSpec("Ceiling", "Wi-Fi 6", "PoE 802.3af", 200)) to
                HardwareItem(HardwareId("ap2"), "Ubiquiti", "U7 Pro Max", null, spec = AccessPointSpec("Ceiling", "Wi-Fi 7", "PoE+", 500)),
            HardwareItem(HardwareId("re1"), "TP-Link", "RE200", null, spec = RangeExtenderSpec("Wall Plug", "Wi-Fi 5", coverageAreaM2 = 90)) to
                HardwareItem(HardwareId("re2"), "TP-Link", "RE815XE", null, spec = RangeExtenderSpec("Wall Plug", "Wi-Fi 6E", meshProtocol = "EasyMesh", coverageAreaM2 = 240)),
        )

        pairs.forEach { (left, right) ->
            val rep = VsArenaEngine.compare(left, right, VsArenaMode.COMPONENT_VS_COMPONENT)
            assertTrue(rep.rows.size >= 3, "${left.category} must produce >= 3 comparison rows, got ${rep.rows.size}")
            assertEquals(ComparisonWinner.RIGHT, rep.performanceChampion.winner, "${left.category}: stronger right item must win instead of 75-75 tie")
        }
    }

    @Test
    fun settingsRepositoryHydratesAllNinePreferencesSynchronouslyFromFastStore() {
        val backing = mutableMapOf(
            "setting_theme_mode" to "LIGHT",
            "setting_language" to "en",
            "setting_currency" to "USD",
            "setting_data_saver" to "true",
            "setting_temperature_unit" to "FAHRENHEIT",
            "setting_length_unit" to "IMPERIAL",
            "setting_resolution" to "UHD_4K",
            "setting_fps" to "FPS_240",
            "setting_enthusiast_mode" to "false",
        )
        val fakeStore = object : FastSettingsStore {
            override fun getString(key: String, default: String?): String? = backing[key] ?: default
            override fun setString(key: String, value: String) { backing[key] = value }
        }
        val repo = SettingsRepositoryImpl(
            hardwareDao = null,
            ioDispatcher = Dispatchers.Unconfined,
            fastSettingsStore = fakeStore,
        )
        val s = repo.settings.value
        assertEquals(TemperatureUnit.FAHRENHEIT, s.temperatureUnit)
        assertEquals(LengthUnit.IMPERIAL, s.lengthUnit)
        assertEquals(ResolutionTarget.UHD_4K, s.defaultResolution)
        assertEquals(FpsTarget.FPS_240, s.defaultFps)
        assertTrue(s.dataSaver)
        assertFalse(s.enthusiastMode)
    }

    @Test
    fun userProfileCalendarDateAlgorithmMatchesKnownEpoch() {
        // 2026-11-21T00:00:00Z = 20778 days * 86400000 ms
        val (y, m, d) = UserProfile.currentCalendarDate(20778L * 86_400_000L)
        assertEquals(2026, y)
        assertEquals(11, m)
        assertEquals(21, d)
    }
}
