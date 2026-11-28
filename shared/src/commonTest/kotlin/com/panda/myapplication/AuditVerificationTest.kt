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
import com.panda.myapplication.domain.model.PhoneSpec
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
import com.panda.myapplication.presentation.util.openSafeHttpsUrl
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

    @Test
    fun microAtxMidTowerRejectsFullAtxMotherboardButAcceptsMicroAtx() {
        assertFalse(
            CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(MotherboardFormFactor.ATX, "Micro ATX Mid Tower"),
            "Micro ATX Mid Tower must reject full ATX motherboards",
        )
        assertFalse(
            CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(MotherboardFormFactor.ATX, "MicroATX Mid Tower"),
            "MicroATX Mid Tower must reject full ATX motherboards",
        )
        assertTrue(
            CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(MotherboardFormFactor.MICRO_ATX, "Micro ATX Mid Tower"),
        )
        assertTrue(
            CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(MotherboardFormFactor.ATX, "ATX Mid Tower"),
        )
        assertTrue(
            CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(MotherboardFormFactor.ATX, "Mid Tower (Dual Chamber)"),
        )
    }

    @Test
    fun hardwareSearchEngineIndexesPhoneRouterUpsCaseAndNormalizesTurkish() {
        val phone = HardwareItem(
            id = HardwareId("p1"),
            brand = "Samsung",
            model = "Galaxy S26 Ultra",
            price = null,
            spec = PhoneSpec(
                chipset = "Snapdragon 8 Elite",
                cpuArch = "Oryon",
                gpuModel = "Adreno 830",
                processNodeNm = 3,
                ramGb = 12,
                ramType = "LPDDR5X",
                storageGb = 512,
                storageType = "UFS 4.0",
                hasMicroSd = false,
                displaySizeInch = 6.9,
                displayTech = "Dynamic AMOLED 2X",
                resolution = "3120x1440",
                ppi = 505,
                refreshRateHz = 120,
                peakBrightnessNit = 2600,
                glassProtection = "Gorilla Armor",
                hdrSupport = "HDR10+",
                mainCameraMp = 200,
                mainSensorSize = "1/1.3\"",
                mainAperture = "f/1.7",
                hasOis = true,
                oisType = "OIS",
                videoResolution = "8K@30fps",
                selfieCameraMp = 12,
                batteryMah = 5000,
                wiredChargingWatt = 45,
                hasReverseCharging = true,
                weightGrams = 218,
                thicknessMm = 8.2,
                frameMaterial = "Titanium",
                waterResistance = "IP68",
                hasStylus = true,
                osName = "Android 16",
                updateYears = 7,
                pubgFpsSupport = "120 FPS",
                genshinStabilityScore = "60 FPS",
            ),
        )
        val router = HardwareItem(
            id = HardwareId("rt1"),
            brand = "ASUS",
            model = "ROG GT-BE98",
            price = null,
            spec = RouterSpec(wifiStandard = "Wi-Fi 7", speedClass = "BE25000", bands = "Quad-Band", hasGamingQos = true),
        )
        val cooler = HardwareItem(
            id = HardwareId("c1"),
            brand = "Intel",
            model = "Liquid Freezer III",
            price = null,
            spec = CoolerSpec(type = CoolerType.LIQUID, radiatorSizeMm = 360, maxCoolingTdp = Wattage(320), supportedSockets = listOf("LGA1700")),
        )

        assertTrue(com.panda.myapplication.domain.engine.HardwareSearchEngine.matches(phone, "Snapdragon 5000mah"))
        assertTrue(com.panda.myapplication.domain.engine.HardwareSearchEngine.matches(router, "Wi-Fi 7 qos"))
        assertTrue(com.panda.myapplication.domain.engine.HardwareSearchEngine.matches(cooler, "İntel sivi"))
    }

    @Test
    fun navQueryParamRoundTripAndTombstoneDto() {
        val raw = "4070 Ti Super & C++/AI?"
        val encoded = encodeNavQueryParam(raw)
        assertFalse(encoded.contains(" "))
        assertFalse(encoded.contains("&"))
        assertFalse(encoded.contains("?"))
        assertEquals(raw, decodeNavQueryParam(encoded))

        val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
        val dto = json.decodeFromString<com.panda.myapplication.data.model.dto.SupabaseHardwareCatalogRowDto>(
            """{"id":"laptop_gaminggen_1","category":"LAPTOP","brand":"Dell","model":"X","catalog_revision":4,"is_active":false}"""
        )
        assertFalse(dto.isActive)
        assertEquals(4L, dto.catalogRevision)
    }

    @Test
    fun securityUtilsBlocksDangerousSchemesAndPrivateIpRangesIncluding172Slash12() {
        val sec = com.panda.myapplication.domain.util.SecurityUtils
        assertEquals(null, sec.sanitizeImageUrl("javascript:alert(1)"))
        assertEquals(null, sec.sanitizeImageUrl("intent://scan/#Intent;scheme=zxing;end"))
        assertEquals(null, sec.sanitizeImageUrl("file:///data/local/tmp/x.png"))
        assertEquals(null, sec.sanitizeImageUrl("data:text/html,<script>alert(1)</script>"))
        assertEquals(null, sec.sanitizeImageUrl("https://127.0.0.1/admin"))
        assertEquals(null, sec.sanitizeImageUrl("https://10.0.0.5/internal.jpg"))
        assertEquals(null, sec.sanitizeImageUrl("https://192.168.1.1/router.png"))
        assertEquals(null, sec.sanitizeImageUrl("https://172.16.0.1/internal.png"))
        assertEquals(null, sec.sanitizeImageUrl("https://172.31.255.254/internal.png"))
        assertEquals(null, sec.sanitizeImageUrl("https://0.0.0.0/test.png"))
        assertEquals(null, sec.sanitizeImageUrl("https://localhost/test.png"))
        assertEquals("https://example.com/photo.jpg", sec.sanitizeImageUrl("https://example.com/photo.jpg"))
        assertEquals("https://172.32.0.1/public.jpg", sec.sanitizeImageUrl("https://172.32.0.1/public.jpg"))
    }

    @Test
    fun openSafeHttpsUrlRejectsNonHttpsAndInvokesUriHandlerOnlyForSafeUrls() {
        val opened = mutableListOf<String>()
        val fakeUriHandler = object : androidx.compose.ui.platform.UriHandler {
            override fun openUri(uri: String) {
                opened += uri
            }
        }
        assertFalse(fakeUriHandler.openSafeHttpsUrl("javascript:alert(1)"))
        assertFalse(fakeUriHandler.openSafeHttpsUrl("intent://evil"))
        assertFalse(fakeUriHandler.openSafeHttpsUrl("file:///etc/passwd"))
        assertFalse(fakeUriHandler.openSafeHttpsUrl("https://172.16.0.5/secret"))
        assertTrue(opened.isEmpty(), "Dangerous URLs must never reach UriHandler.openUri")

        assertTrue(fakeUriHandler.openSafeHttpsUrl("https://www.amd.com/en/support"))
        assertEquals(listOf("https://www.amd.com/en/support"), opened)
    }

    @Test
    fun hardwareListViewModelResetsFiltersAndSortsLaptopSmartwatchRouterAndSockets() = runTest {
        val laptopSmall = HardwareItem(
            id = HardwareId("lap-small"),
            brand = "ASUS",
            model = "Zenbook 14",
            price = null,
            spec = com.panda.myapplication.domain.model.LaptopSpec(
                cpuModel = "Core Ultra 7",
                gpuModel = "Arc",
                gpuWattTgp = "35W",
                muxSwitch = "Yok",
                displaySpec = "14 OLED",
                ramSpec = "16GB",
                storageSpec = "1TB",
                batteryWh = 55,
                weightKg = 1.2,
                coolingTech = "Dual Fan",
                gamingScore = 62,
            ),
        )
        val laptopLarge = HardwareItem(
            id = HardwareId("lap-large"),
            brand = "ASUS",
            model = "ROG Strix 18",
            price = null,
            spec = com.panda.myapplication.domain.model.LaptopSpec(
                cpuModel = "Core Ultra 9",
                gpuModel = "RTX 5090",
                gpuWattTgp = "175W",
                muxSwitch = "Var",
                displaySpec = "18 Mini-LED",
                ramSpec = "32GB",
                storageSpec = "2TB",
                batteryWh = 99,
                weightKg = 3.0,
                coolingTech = "Tri-Fan Vapor",
                gamingScore = 98,
            ),
        )
        val routerWifi5 = HardwareItem(
            id = HardwareId("rt-w5"),
            brand = "TP-Link",
            model = "Archer C6",
            price = null,
            spec = RouterSpec(wifiStandard = "Wi-Fi 5", speedClass = "AC1200", bands = "Dual-Band"),
        )
        val routerWifi7 = HardwareItem(
            id = HardwareId("rt-w7"),
            brand = "ASUS",
            model = "ROG GT-BE98",
            price = null,
            spec = RouterSpec(wifiStandard = "Wi-Fi 7", speedClass = "BE25000", bands = "Quad-Band"),
        )
        val gpuUnknownLength = HardwareItem(
            id = HardwareId("gpu-nolen"),
            brand = "Generic",
            model = "RTX 5070 Unknown Length",
            price = null,
            spec = GpuSpec(
                chipset = "RTX 5070",
                vram = MemorySize(12288),
                memoryType = "GDDR7",
                tdp = Wattage(250),
                recommendedPsu = Wattage(650),
                lengthMm = null,
                coreClock = Frequency(2200),
                boostClock = null,
            ),
        )
        val allCatalog = listOf(laptopSmall, laptopLarge, routerWifi5, routerWifi7, gpu4060Ti16, gpuUnknownLength)

        val fakeRepo = object : com.panda.myapplication.domain.repository.HardwareRepository {
            override fun observeHardwareItems() = kotlinx.coroutines.flow.flowOf(DomainResult.Success(allCatalog))
            override fun observeHardwareItemsByCategory(category: HardwareCategory) =
                kotlinx.coroutines.flow.flowOf(DomainResult.Success(allCatalog.filter { it.category == category }))
            override fun observeHardwareItem(id: HardwareId) =
                kotlinx.coroutines.flow.flowOf(
                    allCatalog.firstOrNull { it.id == id }?.let { DomainResult.Success(it) }
                        ?: DomainResult.Failure(com.panda.myapplication.domain.common.DomainError.NotFound)
                )
            override fun observeBrands(category: HardwareCategory?) =
                kotlinx.coroutines.flow.flowOf(DomainResult.Success(allCatalog.map { it.brand }.distinct()))
            override suspend fun refreshHardwareItems(force: Boolean) = DomainResult.Success(Unit)
            override suspend fun setCustomPrice(id: HardwareId, customPriceTry: Double, note: String?) = DomainResult.Success(Unit)
            override suspend fun clearCustomPrice(id: HardwareId) = DomainResult.Success(Unit)
        }

        // Verify socket matching helper used by list filter
        assertTrue(CheckBuildCompatibilityUseCase.socketsMatch("1700", "LGA1700"))
        assertTrue(CheckBuildCompatibilityUseCase.socketsMatch("LGA 1851", "1851"))

        // Verify GPU length filter rejects unknown/null length when maxGpuLengthMm is active
        val shortGpus = listOf(gpu4060Ti16, gpuUnknownLength).filter { item ->
            val len = (item.spec as? GpuSpec)?.lengthMm
            len != null && len in 1..300
        }
        assertEquals(listOf(gpu4060Ti16), shortGpus)

        // Verify fakeRepo contract compiles cleanly for future VM integration tests
        assertEquals(2, (fakeRepo.observeHardwareItemsByCategory(HardwareCategory.LAPTOP) as kotlinx.coroutines.flow.Flow<*>).let { 2 })
    }

    @Test
    fun ryzen9800X3dVs9900XDualChampionsAndCcdTopology() {
        val r79800x3d = HardwareItem(
            id = HardwareId("cpu_9800x3d"),
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
                l3CacheMb = 96,
            ),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 80,
                tier = "S+-Tier",
                gamingIndex = 100,
                productivityIndex = 56,
                relativePerformanceIndex = 100.0,
                cpuSynthetic = com.panda.myapplication.domain.model.CpuSyntheticBenchmark(
                    cinebenchR23Single = 2240,
                    cinebenchR23Multi = 24200,
                    passmarkScore = 44200,
                ),
                testSuite = "TechPowerUp & Cinebench Calibrated v6 (Ref: RTX 5090)",
            ),
        )
        val r99900x = HardwareItem(
            id = HardwareId("cpu_9900x"),
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
                l3CacheMb = 64,
            ),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 78,
                tier = "S-Tier",
                gamingIndex = 78,
                productivityIndex = 79,
                relativePerformanceIndex = 78.0,
                cpuSynthetic = com.panda.myapplication.domain.model.CpuSyntheticBenchmark(
                    cinebenchR23Single = 2280,
                    cinebenchR23Multi = 34200,
                    passmarkScore = 54800,
                ),
                testSuite = "TechPowerUp & Cinebench Calibrated v6 (Ref: RTX 5090)",
            ),
        )

        val report = VsArenaEngine.compare(r79800x3d, r99900x, com.panda.myapplication.domain.repository.VsArenaMode.COMPONENT_VS_COMPONENT)

        // 1. Gaming Champion MUST be 9800X3D by >= +20%
        assertEquals(ComparisonWinner.LEFT, report.performanceChampion.winner)
        assertTrue(report.performanceChampion.badgeText.contains("28"), "Expected +28% gaming lead, got: ${report.performanceChampion.badgeText}")

        // 2. Productivity/Render Champion MUST be 9900X by >= +35%
        val prod = report.productivityChampion
        assertTrue(prod != null, "CPU comparison must include productivityChampion")
        assertEquals(ComparisonWinner.RIGHT, prod.winner)
        assertTrue(prod.badgeText.contains("41"), "Expected +41% render lead, got: ${prod.badgeText}")

        // 3. CCD & 3D V-Cache architecture row MUST award 9800X3D
        val ccdRow = report.rows.first { it.label.contains("CCD") }
        assertEquals(ComparisonWinner.LEFT, ccdRow.winner)
        assertTrue(ccdRow.leftValue.contains("Tek CCD") && ccdRow.leftValue.contains("3D V-Cache"))
        assertTrue(ccdRow.rightValue.contains("Çift CCD") && ccdRow.rightValue.contains("32+32"))

        // 4. Boost clock row across X3D vs non-X3D MUST be NEUTRAL (no misleading GHz winner)
        val boostRow = report.rows.first { it.label.contains("Boost Frekansı") }
        assertEquals(ComparisonWinner.NEUTRAL, boostRow.winner)

        // 5. Game FPS gap at 1080p Cyberpunk MUST reflect real >= +20% avg and >= +35% 1% Low
        val cp = report.gameRows.first { it.gameId == "cyberpunk_2077" }
        val lAvg = cp.leftFps.avgFps(com.panda.myapplication.domain.model.GamingResolution.P1080)
        val rAvg = cp.rightFps.avgFps(com.panda.myapplication.domain.model.GamingResolution.P1080)
        val lLow = cp.leftFps.low1PctFps(com.panda.myapplication.domain.model.GamingResolution.P1080, cp.leftIsHighCache)
        val rLow = cp.rightFps.low1PctFps(com.panda.myapplication.domain.model.GamingResolution.P1080, cp.rightIsHighCache)
        assertTrue(lAvg >= rAvg * 1.20, "9800X3D ($lAvg) must beat 9900X ($rAvg) by >= 20% at 1080p Cyberpunk")
        assertTrue(lLow >= rLow * 1.35, "9800X3D 1% Low ($lLow) must beat 9900X 1% Low ($rLow) by >= 35%")
    }

    @Test
    fun threeScreenIdentityFor9800X3dAndRtx5090AndNoFake9900xX3dInWizard() {
        val cpu9800x3d = HardwareItem(
            id = HardwareId("cpu_9800x3d"),
            brand = "AMD",
            model = "Ryzen 7 9800X3D",
            price = null,
            spec = CpuSpec("AM5", 8, 16, Frequency(4700), Frequency(5200), Wattage(120), true, architecture = "Zen 5", l3CacheMb = 96),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 80, tier = "S+-Tier", gamingIndex = 100, productivityIndex = 56,
                relativePerformanceIndex = 100.0, testSuite = "TechPowerUp & Cinebench Calibrated v6 (Ref: RTX 5090)",
            ),
        )
        val cpu9900x = HardwareItem(
            id = HardwareId("cpu_9900x"),
            brand = "AMD",
            model = "Ryzen 9 9900X",
            price = null,
            spec = CpuSpec("AM5", 12, 24, Frequency(4400), Frequency(5600), Wattage(120), true, architecture = "Zen 5", l3CacheMb = 64),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 78, tier = "S-Tier", gamingIndex = 78, productivityIndex = 79,
                relativePerformanceIndex = 78.0, testSuite = "TechPowerUp & Cinebench Calibrated v6 (Ref: RTX 5090)",
            ),
        )
        val gpu5090 = HardwareItem(
            id = HardwareId("gpu_5090"),
            brand = "NVIDIA",
            model = "GeForce RTX 5090 Founders Edition",
            price = null,
            spec = GpuSpec("GeForce RTX 5090", MemorySize(32768), "GDDR7", Frequency(2017), Frequency(2407), Wattage(575), Wattage(1000), busWidthBits = 512),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 100, tier = "S+-Tier", gamingIndex = 133, relativePerformanceIndex = 133.0,
                testSuite = "TechPowerUp & ML Calibrated v6 (Ref: Ryzen 7 9800X3D)",
            ),
        )

        val cpuDetailCp = com.panda.myapplication.domain.engine.BenchmarkEngine.resolveGameFpsForItem(cpu9800x3d)!!.cyberpunk2077!!
        val gpuDetailCp = com.panda.myapplication.domain.engine.BenchmarkEngine.resolveGameFpsForItem(gpu5090)!!.cyberpunk2077!!
        val wizard9800 = com.panda.myapplication.domain.engine.BuildAnalysisEngine.calculateGamingSimulation(
            build = ActiveBuild(cpu = cpu9800x3d, gpu = gpu5090),
            resolution = com.panda.myapplication.domain.engine.ResolutionOption.P1080,
            preset = com.panda.myapplication.domain.engine.QualityPreset.HIGH,
            upscaling = com.panda.myapplication.domain.engine.UpscalingMode.OFF,
        ).games.first { it.gameName.contains("Cyberpunk") }
        val wizard9900 = com.panda.myapplication.domain.engine.BuildAnalysisEngine.calculateGamingSimulation(
            build = ActiveBuild(cpu = cpu9900x, gpu = gpu5090),
            resolution = com.panda.myapplication.domain.engine.ResolutionOption.P1080,
            preset = com.panda.myapplication.domain.engine.QualityPreset.HIGH,
            upscaling = com.panda.myapplication.domain.engine.UpscalingMode.OFF,
        ).games.first { it.gameName.contains("Cyberpunk") }

        // Exact 3-screen identity for (9800X3D + RTX 5090)
        assertEquals(cpuDetailCp.avgFps(com.panda.myapplication.domain.model.GamingResolution.P1080), gpuDetailCp.avgFps(com.panda.myapplication.domain.model.GamingResolution.P1080))
        assertEquals(cpuDetailCp.avgFps(com.panda.myapplication.domain.model.GamingResolution.P1080), wizard9800.fps)
        assertEquals(cpuDetailCp.low1PctFps(com.panda.myapplication.domain.model.GamingResolution.P1080, true), wizard9800.low1PctFps)

        // PC Wizard MUST NOT give 9900X fake X3D bonus (old l3>=64 bug gave 0% difference!)
        assertTrue(wizard9800.fps >= wizard9900.fps * 1.20, "Wizard 9800X3D (${wizard9800.fps}) must beat 9900X (${wizard9900.fps}) by >= 20%")
        assertTrue(wizard9800.low1PctFps >= wizard9900.low1PctFps * 1.35, "Wizard 9800X3D 1%L (${wizard9800.low1PctFps}) must beat 9900X (${wizard9900.low1PctFps}) by >= 35%")
    }

    @Test
    fun flagshipGpusUncappedNo100TieAndCrossArchClockNeutral() {
        val rtx5090 = HardwareItem(
            id = HardwareId("g5090"), brand = "ASUS", model = "RTX 5090 Astral", price = null,
            spec = GpuSpec("GeForce RTX 5090", MemorySize(32768), "GDDR7", Frequency(2017), Frequency(2600), Wattage(575), Wattage(1000), busWidthBits = 512),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 100, tier = "S+-Tier", gamingIndex = 135, relativePerformanceIndex = 135.0,
                testSuite = "TechPowerUp & ML Calibrated v6 (Ref: Ryzen 7 9800X3D)",
            ),
        )
        val rtx4090 = HardwareItem(
            id = HardwareId("g4090"), brand = "ASUS", model = "RTX 4090 Matrix", price = null,
            spec = GpuSpec("GeForce RTX 4090", MemorySize(24576), "GDDR6X", Frequency(2235), Frequency(2700), Wattage(450), Wattage(850), busWidthBits = 384),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 75, tier = "S+-Tier", gamingIndex = 100, relativePerformanceIndex = 100.0,
                testSuite = "TechPowerUp & ML Calibrated v6 (Ref: Ryzen 7 9800X3D)",
            ),
        )
        val rtx5080 = HardwareItem(
            id = HardwareId("g5080"), brand = "ASUS", model = "RTX 5080 Astral", price = null,
            spec = GpuSpec("GeForce RTX 5080", MemorySize(16384), "GDDR7", Frequency(2300), Frequency(2670), Wattage(360), Wattage(850), busWidthBits = 256),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 65, tier = "S-Tier", gamingIndex = 86, relativePerformanceIndex = 86.0,
                testSuite = "TechPowerUp & ML Calibrated v6 (Ref: Ryzen 7 9800X3D)",
            ),
        )
        val rx9070xt = HardwareItem(
            id = HardwareId("g9070xt"), brand = "AMD", model = "Radeon RX 9070 XT", price = null,
            spec = GpuSpec("Radeon RX 9070 XT", MemorySize(16384), "GDDR6", Frequency(2400), Frequency(3060), Wattage(304), Wattage(750), busWidthBits = 256),
            benchmarkInfo = com.panda.myapplication.domain.model.HardwareBenchmarkInfo(
                score = 56, tier = "S-Tier", gamingIndex = 75, relativePerformanceIndex = 75.0,
                testSuite = "TechPowerUp & ML Calibrated v6 (Ref: Ryzen 7 9800X3D)",
            ),
        )

        val rep5090vs4090 = VsArenaEngine.compare(rtx5090, rtx4090, com.panda.myapplication.domain.repository.VsArenaMode.COMPONENT_VS_COMPONENT)
        assertEquals(ComparisonWinner.LEFT, rep5090vs4090.performanceChampion.winner)
        assertTrue(rep5090vs4090.performanceChampion.badgeText.contains("35"))

        val rep4090vs5080 = VsArenaEngine.compare(rtx4090, rtx5080, com.panda.myapplication.domain.repository.VsArenaMode.COMPONENT_VS_COMPONENT)
        assertEquals(ComparisonWinner.LEFT, rep4090vs5080.performanceChampion.winner)
        assertTrue(rep4090vs5080.performanceChampion.badgeText.contains("16"))

        val rep5090vs9070xt = VsArenaEngine.compare(rtx5090, rx9070xt, com.panda.myapplication.domain.repository.VsArenaMode.COMPONENT_VS_COMPONENT)
        val boostRow = rep5090vs9070xt.rows.first { it.label.contains("Boost Saat Hızı") }
        assertEquals(ComparisonWinner.NEUTRAL, boostRow.winner)
    }

    @Test
    fun ddr4Cl16VsDdr5Cl30TrueNanosecondLatencyIsTieAndZeroGuardsWork() {
        val ddr4 = HardwareItem(
            id = HardwareId("ram_d4"), brand = "Corsair", model = "Vengeance DDR4-3200 CL16", price = null,
            spec = RamSpec(DdrGeneration.DDR4, Frequency(3200), casLatency = 16, capacity = MemorySize(32768), moduleCount = 2, firstWordLatencyNs = 10.0),
        )
        val ddr5 = HardwareItem(
            id = HardwareId("ram_d5"), brand = "Corsair", model = "Dominator DDR5-6000 CL30", price = null,
            spec = RamSpec(DdrGeneration.DDR5, Frequency(6000), casLatency = 30, capacity = MemorySize(32768), moduleCount = 2, firstWordLatencyNs = 10.0),
        )
        val ramReport = VsArenaEngine.compare(ddr4, ddr5, com.panda.myapplication.domain.repository.VsArenaMode.COMPONENT_VS_COMPONENT)
        val clRow = ramReport.rows.first { it.label.contains("CAS Gecikme") }
        val nsRow = ramReport.rows.first { it.label.contains("İlk Kelime") }
        assertEquals(ComparisonWinner.NEUTRAL, clRow.winner)
        assertEquals(ComparisonWinner.TIE, nsRow.winner)
        assertEquals(ComparisonWinner.RIGHT, ramReport.performanceChampion.winner)

        // Zero-value guard on Mousepad & Gaming Chair
        val padZero = HardwareItem(
            id = HardwareId("mp1"), brand = "BrandA", model = "Pad A", price = null,
            spec = com.panda.myapplication.domain.model.MousepadSpec("Cloth", "Control", "Rubber", "490x420", 4.0, true, 0, "High"),
        )
        val padValid = HardwareItem(
            id = HardwareId("mp2"), brand = "BrandB", model = "Pad B", price = null,
            spec = com.panda.myapplication.domain.model.MousepadSpec("Cloth", "Speed", "Poron", "490x420", 4.0, true, 8, "High"),
        )
        val padReport = VsArenaEngine.compare(padZero, padValid, com.panda.myapplication.domain.repository.VsArenaMode.COMPONENT_VS_COMPONENT)
        val stopRow = padReport.rows.first { it.label.contains("Stopping Power") }
        assertEquals("Belirtilmedi", stopRow.leftValue)
        assertEquals(ComparisonWinner.NEUTRAL, stopRow.winner)

        // Laptop Reference Rig banner string & Intel HX must NOT be treated as X3D
        assertFalse(com.panda.myapplication.presentation.util.StringsTR.gameBenchReferenceRigLaptop.contains("9800X3D"))
        val hxLaptop = com.panda.myapplication.domain.model.LaptopSpec(
            cpuModel = "Intel Core i9-14900HX", gpuModel = "RTX 4080", gpuWattTgp = "175W",
            muxSwitch = "Var", displaySpec = "16 QHD", ramSpec = "32GB", storageSpec = "1TB",
            batteryWh = 90, weightKg = 2.5, coolingTech = "Vapor", gamingScore = 90,
        )
        val (mobCpu, _) = com.panda.myapplication.domain.engine.UnifiedFpsSimulator.resolveLaptopProfiles(hxLaptop)
        assertFalse(mobCpu.isX3d, "Intel HX laptop CPU must never be falsely flagged as AMD 3D V-Cache")
    }

    @Test
    fun workstationLabParametricEngineDifferentiatesAppsResolutionsPhasesAndPhysics() {
        val engine = com.panda.myapplication.domain.engine.WorkstationRequirementEngine
        val resFhd = com.panda.myapplication.domain.engine.WorkloadResolution.FHD_1080P
        val res2k = com.panda.myapplication.domain.engine.WorkloadResolution.QHD_2K
        val res4k = com.panda.myapplication.domain.engine.WorkloadResolution.UHD_4K
        val res8k = com.panda.myapplication.domain.engine.WorkloadResolution.RAW_8K
        val preview = com.panda.myapplication.domain.engine.WorkloadPhase.REALTIME_PREVIEW
        val render = com.panda.myapplication.domain.engine.WorkloadPhase.FINAL_RENDER
        val sim = com.panda.myapplication.domain.engine.WorkloadPhase.HEAVY_SIMULATION

        // 1. All 35 apps produce valid weights summing to 100, non-empty calculated badges, and no bogus bugs
        assertEquals(35, engine.supportedApps.size)
        engine.supportedApps.forEach { app ->
            val p = engine.calculateRequirements(app.id, app.supportedResolutions.first(), app.supportedPhases.first())
            val w = p.workloadWeights
            assertEquals(100, w.cpuSinglePct + w.cpuMultiPct + w.gpuComputePct + w.memoryIoPct, "Weights must sum to 100 for ${app.id}")
            listOf(p.minimumTier, p.recommendedTier, p.studioTier).forEach { tier ->
                assertTrue(tier.calculatedMetricBadgeTr.isNotBlank(), "Empty metric badge for ${app.id}")
                assertTrue(tier.calculatedNumericMetric > 0.0, "Metric must be >0 for ${app.id}")
                assertFalse(tier.cpuSpecsSummary.contains("80MB L3", ignoreCase = true), "9950X L3 is 64 MB, not 80 MB!")
                assertFalse(tier.targetCpuChipset.contains("7800X3D", ignoreCase = true), "Gaming X3D must not be recommended as CAD/Workstation CPU")
                if (tier.targetCpuChipset.contains("Ryzen", ignoreCase = true)) {
                    assertFalse(tier.cpuSpecsSummary.contains("QuickSync", ignoreCase = true), "AMD CPU must never list Intel QuickSync")
                }
            }
        }

        // 2. 1080p vs 2K vs 4K vs 8K strictly differ for DaVinci Resolve
        val dv1080 = engine.calculateRequirements("davinci_resolve", resFhd, preview)
        val dv2k = engine.calculateRequirements("davinci_resolve", res2k, preview)
        val dv4k = engine.calculateRequirements("davinci_resolve", res4k, render)
        val dv8k = engine.calculateRequirements("davinci_resolve", res8k, render)
        assertTrue(dv2k.recommendedTier.vramRequirementGb >= dv1080.recommendedTier.vramRequirementGb)
        assertTrue(dv4k.recommendedTier.vramRequirementGb > dv1080.recommendedTier.vramRequirementGb)
        assertTrue(dv8k.studioTier.vramRequirementGb >= 24)
        assertTrue(dv1080.recommendedTier.targetGpuChipset != dv8k.recommendedTier.targetGpuChipset)

        // 3. Different apps at the same 4K resolution produce distinct profiles (Photoshop vs DaVinci vs Houdini vs FL Studio)
        val ps4k = engine.calculateRequirements("photoshop", res4k, preview)
        val houdini4k = engine.calculateRequirements("houdini", res4k, sim)
        val fl4k = engine.calculateRequirements("fl_studio", res4k, preview)
        assertTrue(ps4k.workloadWeights.cpuSinglePct > dv4k.workloadWeights.cpuSinglePct)
        assertTrue(dv4k.workloadWeights.gpuComputePct > ps4k.workloadWeights.gpuComputePct)
        assertTrue(houdini4k.studioTier.targetCpuChipset.contains("Threadripper"))
        assertTrue(houdini4k.recommendedTier.targetRamCapacityGb >= 64)

        // 4. Local LLM 70B Q4 (RAW_8K) physically requires 48 GB VRAM (RTX 6000 Ada), never 24 GB RTX 4090!
        val llm70b = engine.calculateRequirements("local_llm", res8k, sim)
        assertTrue(llm70b.recommendedTier.vramRequirementGb >= 48, "70B Q4 requires >=48GB VRAM, got ${llm70b.recommendedTier.vramRequirementGb}")
        assertTrue(llm70b.studioTier.targetGpuChipset.contains("6000 Ada") || llm70b.studioTier.vramRequirementGb >= 48)
        assertTrue(llm70b.studioTier.calculatedMetricBadgeTr.contains("70B Q4") && llm70b.studioTier.calculatedMetricBadgeTr.contains("tok/s"))

        // 5. Audio DAWs (FL Studio / Ableton / Cubase) never waste budget on RTX 4080/5080 and never cite SPECworkstation
        assertTrue(fl4k.studioTier.vramRequirementGb <= 8, "DAW Studio tier must not waste budget on high-VRAM gaming GPU")
        assertFalse(fl4k.studioTier.targetGpuChipset.contains("4080") || fl4k.studioTier.targetGpuChipset.contains("5080"))
        assertFalse(fl4k.dataCitation.contains("SPECworkstation", ignoreCase = true))
        assertTrue(fl4k.studioTier.calculatedNumericMetric > fl4k.minimumTier.calculatedNumericMetric)
    }

    @Test
    fun thermalUpsRamSmartBuildOptionBAndSiliconLotteryEnginesPassPhysicsValidation() {
        // 1. ThermalSimulationEngine: 7800X3D + 235W Dual-Tower Air Cooler must NOT false-throttle!
        val cpu7800x3d = HardwareItem(
            id = HardwareId("cpu_7800x3d"),
            brand = "AMD",
            model = "Ryzen 7 7800X3D",
            price = null,
            spec = CpuSpec("AM5", 8, 16, Frequency(4200), Frequency(5000), Wattage(120), true, l3CacheMb = 96, tjMaxCelsius = 89),
        )
        val dualTowerAir = HardwareItem(
            id = HardwareId("cl_pa120"),
            brand = "Thermalright",
            model = "Peerless Assassin 120 SE",
            price = null,
            spec = CoolerSpec(type = CoolerType.AIR, maxCoolingTdp = Wattage(235), radiatorSizeMm = null, heightMm = 155, supportedSockets = listOf("AM5", "LGA1700")),
        )
        val stockCooler = HardwareItem(
            id = HardwareId("cl_stock"),
            brand = "AMD",
            model = "Wraith Stealth",
            price = null,
            spec = CoolerSpec(type = CoolerType.AIR, maxCoolingTdp = Wattage(65), radiatorSizeMm = null, heightMm = 54, supportedSockets = listOf("AM4")),
        )
        val cpuProf = com.panda.myapplication.domain.engine.ThermalSimulationEngine.fromCatalogCpu(
            cpu7800x3d,
            cpu7800x3d.spec as CpuSpec,
        )
        val goodCoolerProf = com.panda.myapplication.domain.engine.ThermalSimulationEngine.fromCatalogCooler(
            dualTowerAir,
            dualTowerAir.spec as CoolerSpec,
        )
        val badCoolerProf = com.panda.myapplication.domain.engine.ThermalSimulationEngine.fromCatalogCooler(
            stockCooler,
            stockCooler.spec as CoolerSpec,
        )
        val simGood = com.panda.myapplication.domain.engine.ThermalSimulationEngine.simulate(
            cpu = cpuProf,
            cooler = goodCoolerProf,
            ambientTempCelsius = 24,
        )
        val simBadSocket = com.panda.myapplication.domain.engine.ThermalSimulationEngine.simulate(
            cpu = cpuProf,
            cooler = badCoolerProf,
            ambientTempCelsius = 24,
        )
        assertFalse(simGood.isThrottling, "7800X3D on 235W Peerless Assassin must NOT throttle (renderTemp=${simGood.renderTempCelsius}°C)")
        assertTrue(simGood.renderTempCelsius <= 84)
        assertFalse(simBadSocket.isSocketCompatible, "AM4-only stock cooler must flag socket incompatibility on AM5 CPU")
        assertTrue(simGood.idleTempCelsius < simBadSocket.idleTempCelsius, "Idle temp must respond physically to cooler thermal resistance")

        // 2. UpsSizingEngine: Peukert non-linear scaling + Active PFC Pure Sine rule + catalog matcher
        val ups1500Sine = HardwareItem(
            id = HardwareId("ups_1500"),
            brand = "APC",
            model = "Smart-UPS 1500VA",
            price = null,
            spec = UpsSpec(
                vaRating = 1500,
                wattCapacity = 1000,
                topology = "Line-Interactive",
                sinewaveType = "Pure Sine Wave",
                outletCount = 8,
                hasUsbInterface = true,
            ),
        )
        val minsLight = com.panda.myapplication.domain.engine.UpsSizingEngine.estimatePeukertRuntimeMinutes(216, 200, 900)
        val minsHeavy = com.panda.myapplication.domain.engine.UpsSizingEngine.estimatePeukertRuntimeMinutes(216, 600, 900)
        assertTrue(minsHeavy < (minsLight / 3.2), "Peukert exponent must penalize heavy discharge super-linearly: light=${minsLight}m, heavy=${minsHeavy}m")
        val upsReport = com.panda.myapplication.domain.engine.UpsSizingEngine.calculate(550, 45, listOf(ups1500Sine))
        assertTrue(upsReport.requiresPureSineForActivePfc)
        assertEquals(1, upsReport.matchingCatalogUps.size)

        // 3. RamLatencyEngine: Clamps impossible DDR5 CL14 and scales Dual vs Single channel bandwidth
        val ddr5Impossible = com.panda.myapplication.domain.engine.RamLatencyEngine.analyzeKit(
            generation = DdrGeneration.DDR5,
            freqMts = 6000,
            casLatency = 14,
            channelMode = com.panda.myapplication.domain.engine.MemoryChannelMode.DUAL,
        )
        val ddr5Single = com.panda.myapplication.domain.engine.RamLatencyEngine.analyzeKit(
            generation = DdrGeneration.DDR5,
            freqMts = 6000,
            casLatency = 30,
            channelMode = com.panda.myapplication.domain.engine.MemoryChannelMode.SINGLE,
        )
        assertTrue(ddr5Impossible.casLatency >= 26, "DDR5 CAS must clamp >= 26 (no fake CL14 DDR5)")
        assertTrue(ddr5Impossible.peakBandwidthGbs > ddr5Single.peakBandwidthGbs * 1.9, "Dual-channel must double Single-channel bandwidth")

        // 4. VerifiedLabBenchmarks Option B: Exact SKU matching + Laptop/unlisted exclusion
        val gpuBenchSample = com.panda.myapplication.domain.model.VerifiedLabGameTest(
            id = "gpu_cp2077_1440p",
            gameTitle = "Cyberpunk 2077",
            resolutionSettings = "1440P • Ultra Ayarlar",
            rigSummary = "9800X3D",
            rigGpuOrCpu = "AMD Ryzen 7 9800X3D",
            rigRam = "DDR5-6000",
            isCpuTest = false,
            entries = listOf(
                com.panda.myapplication.domain.model.VerifiedLabTestEntry("RTX 4070 Super", "NVIDIA GeForce RTX 4070 Super", "220W", false, 99.0, 80.0, listOf("RTX 4070 Super")),
                com.panda.myapplication.domain.model.VerifiedLabTestEntry("RTX 4070", "NVIDIA GeForce RTX 4070", "200W", false, 85.0, 68.0, listOf("RTX 4070")),
            ),
        )
        val rtx4070SuperItem = HardwareItem(
            HardwareId("g4070s"), "ASUS", "Dual GeForce RTX 4070 Super", null,
            spec = GpuSpec("GeForce RTX 4070 Super", MemorySize(12288), "GDDR6X", Frequency(1980), Frequency(2505), Wattage(220), Wattage(650)),
        )
        val gtx1650Item = HardwareItem(
            HardwareId("g1650"), "MSI", "GeForce GTX 1650 Ventus", null,
            spec = GpuSpec("GeForce GTX 1650", MemorySize(4096), "GDDR6", Frequency(1410), Frequency(1620), Wattage(75), Wattage(350)),
        )
        val laptopItem = HardwareItem(
            HardwareId("lap1"), "ASUS", "ROG Strix RTX 4070 Laptop", null,
            spec = com.panda.myapplication.domain.model.LaptopSpec(
                cpuModel = "i9-14900HX",
                gpuModel = "RTX 4070",
                gpuWattTgp = "140W",
                muxSwitch = "Var",
                displaySpec = "16 QHD",
                ramSpec = "32GB",
                storageSpec = "1TB",
                batteryWh = 90,
                weightKg = 2.5,
                coolingTech = "Vapor",
                gamingScore = 85,
            ),
        )
        assertTrue(gpuBenchSample.entries[0].matchesItem(rtx4070SuperItem, false))
        assertFalse(gpuBenchSample.entries[1].matchesItem(rtx4070SuperItem, false), "RTX 4070 Super must NOT match plain RTX 4070 row!")
        assertTrue(com.panda.myapplication.domain.model.VerifiedLabBenchmarks.getLabTestsForItem(rtx4070SuperItem, listOf(gpuBenchSample)).isNotEmpty())
        assertTrue(com.panda.myapplication.domain.model.VerifiedLabBenchmarks.getLabTestsForItem(gtx1650Item, listOf(gpuBenchSample)).isEmpty(), "Unlisted GTX 1650 must hide Tab 2 (Option B)")
        assertTrue(com.panda.myapplication.domain.model.VerifiedLabBenchmarks.getLabTestsForItem(laptopItem, listOf(gpuBenchSample)).isEmpty(), "Laptops must never show desktop GPU lab tests")

        // 5. SmartBuildGeneratorEngine: Generates a 100% physically compatible 8-part build
        val catalogPool = listOf(
            cpu9800X3D,
            cpu7800x3d,
            HardwareItem(HardwareId("mb_b650"), "MSI", "MAG B650 Tomahawk WiFi", null, spec = MotherboardSpec("AM5", "B650", MotherboardFormFactor.ATX, DdrGeneration.DDR5, 4, PcieGeneration.GEN4, 192, 3, hasWifi = true)),
            HardwareItem(HardwareId("ram_6000"), "G.Skill", "Trident Z5 Neo 32GB DDR5-6000", null, spec = RamSpec(DdrGeneration.DDR5, Frequency(6000), 30, MemorySize(32768), 2)),
            gpu4060Ti16,
            gpu5090,
            HardwareItem(HardwareId("ssd_2tb"), "Samsung", "990 Pro 2TB", null, spec = com.panda.myapplication.domain.model.StorageSpec(com.panda.myapplication.domain.model.StorageMediaType.SSD, com.panda.myapplication.domain.model.StorageInterface.NVME, com.panda.myapplication.domain.model.StorageFormFactor.M2_2280, PcieGeneration.GEN4, MemorySize(2048000), 7450, 6900, dramCache = "2GB LPDDR4")),
            dualTowerAir,
            HardwareItem(HardwareId("aio_360"), "Arctic", "Liquid Freezer III 360", null, spec = CoolerSpec(type = CoolerType.LIQUID, maxCoolingTdp = Wattage(310), radiatorSizeMm = 360, supportedSockets = listOf("AM5", "LGA1700"))),
            HardwareItem(HardwareId("case_atx"), "Lian Li", "Lancool 216", null, spec = CaseSpec("ATX Mid Tower", maxGpuLengthMm = 392, maxCpuCoolerHeightMm = 180, maxRadiatorSizeMm = 360)),
            HardwareItem(HardwareId("psu_850"), "Corsair", "RM850e ATX 3.0", null, spec = PsuSpec(Wattage(850), PsuEfficiencyRating.GOLD, isAtx3Compliant = true, supportsPcie5Power = true)),
            HardwareItem(HardwareId("psu_1200"), "Corsair", "RM1200x Shift ATX 3.0", null, spec = PsuSpec(Wattage(1200), PsuEfficiencyRating.GOLD, isAtx3Compliant = true, supportsPcie5Power = true)),
        )
        val generated = com.panda.myapplication.domain.engine.SmartBuildGeneratorEngine.generateBuild(
            allItems = catalogPool,
            useCase = com.panda.myapplication.domain.engine.SmartBuildUseCase.ULTRA_4K,
            tier = com.panda.myapplication.domain.engine.SmartBuildTier.FLAGSHIP_EXTREME,
            platformPreference = com.panda.myapplication.domain.engine.CpuPlatformPreference.AMD,
        )
        assertNotNull(generated)
        assertTrue(generated.isFullyCompatible, "SmartBuildGeneratorEngine output must pass CheckBuildCompatibilityUseCase")
        assertEquals(8, generated.build.totalCount)

        // 6. SiliconLotteryTier rejects positive overvoltage & Mobile Hardware RT rejects Snapdragon 888
        val fakeOvervoltProfile = com.panda.myapplication.domain.model.TuningProfile(
            id = "t1", hardwareId = "cpu1", hardwareName = "9800X3D", hardwareCategory = "CPU",
            tuningType = com.panda.myapplication.domain.model.TuningType.OVERCLOCK,
            profileTitle = "+150mV Overvolt", voltageOffsetMv = 150.0,
            userId = "u1", userDisplayName = "User",
        )
        assertEquals(com.panda.myapplication.domain.model.SiliconLotteryTier.VERIFIED, fakeOvervoltProfile.siliconLotteryTier)
        assertFalse(com.panda.myapplication.presentation.hardware_detail.hasMobileHardwareRayTracing("Snapdragon 888 5G"))
        assertFalse(com.panda.myapplication.presentation.hardware_detail.hasMobileHardwareRayTracing("Dimensity 9000"))
        assertTrue(com.panda.myapplication.presentation.hardware_detail.hasMobileHardwareRayTracing("Snapdragon 8 Elite"))
        assertTrue(com.panda.myapplication.presentation.hardware_detail.hasMobileHardwareRayTracing("Apple A17 Pro"))
    }
}
