package com.panda.myapplication

import com.panda.myapplication.domain.engine.BuildAnalysisEngine
import com.panda.myapplication.domain.engine.QualityPreset
import com.panda.myapplication.domain.engine.ResolutionOption
import com.panda.myapplication.domain.engine.UpscalingMode
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.Wattage
import com.panda.myapplication.domain.engine.BenchmarkEngine
import com.panda.myapplication.domain.model.VerifiedLabBenchmarks
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BuildDoctorAndSimulationTest {

    private fun createTestCpu(
        name: String = "Ryzen 7 7800X3D",
        socket: String = "AM5",
        boostMhz: Int = 5050,
        cores: Int = 8,
        l3Mb: Int = 96,
        score: Int = 94,
    ): HardwareItem = HardwareItem(
        id = HardwareId("cpu-1"),
        brand = "AMD",
        model = name,
        price = Money(1650000L, "TRY"),
        benchmarkInfo = HardwareBenchmarkInfo(
            score = score,
            relativePerformancePct = score / 100.0,
            gamingRelativeIndex = score / 100.0,
        ),
        spec = CpuSpec(
            socket = socket,
            coreCount = cores,
            threadCount = cores * 2,
            baseClock = Frequency(3000),
            boostClock = Frequency(boostMhz),
            tdp = Wattage(120),
            hasIntegratedGraphics = true,
            l3CacheMb = l3Mb,
        ),
    )

    private fun createTestGpu(
        name: String = "RTX 4070 SUPER",
        vramMb: Int = 12288,
        tdpWatts: Int = 220,
        score: Int = 90,
    ): HardwareItem = HardwareItem(
        id = HardwareId("gpu-1"),
        brand = "NVIDIA",
        model = name,
        price = Money(2600000L, "TRY"),
        benchmarkInfo = HardwareBenchmarkInfo(
            score = score,
            relativePerformancePct = score / 100.0,
            gamingRelativeIndex = score / 100.0,
        ),
        spec = GpuSpec(
            chipset = "AD104",
            vram = MemorySize(vramMb),
            memoryType = "GDDR6X",
            tdp = Wattage(tdpWatts),
            recommendedPsu = Wattage(650),
            lengthMm = 242,
            coreClock = Frequency(1980),
            boostClock = Frequency(2475),
        ),
    )

    private fun createTestMobo(socket: String = "AM5"): HardwareItem = HardwareItem(
        id = HardwareId("mobo-1"),
        brand = "MSI",
        model = "B650 Tomahawk",
        price = Money(750000L, "TRY"),
        spec = MotherboardSpec(
            socket = socket,
            formFactor = MotherboardFormFactor.ATX,
            chipset = "B650",
            memoryGeneration = DdrGeneration.DDR5,
            ramSlots = 4,
            pcieGeneration = PcieGeneration.GEN4,
            maxMemoryGb = 128,
        ),
    )

    private fun createTestRam(capacityMb: Int = 32768): HardwareItem = HardwareItem(
        id = HardwareId("ram-1"),
        brand = "Corsair",
        model = "Corsair Vengeance 32GB",
        price = Money(420000L, "TRY"),
        spec = RamSpec(
            generation = DdrGeneration.DDR5,
            speed = Frequency(6000),
            casLatency = 30,
            capacity = MemorySize(capacityMb),
            moduleCount = 2,
        ),
    )

    private fun createTestPsu(watts: Int = 750): HardwareItem = HardwareItem(
        id = HardwareId("psu-1"),
        brand = "Corsair",
        model = "Corsair RM750e",
        price = Money(380000L, "TRY"),
        spec = PsuSpec(
            wattage = Wattage(watts),
            efficiency = PsuEfficiencyRating.GOLD,
            isAtx3Compliant = true,
            supportsPcie5Power = true,
            modularity = "Full",
        ),
    )

    @Test
    fun buildDoctorCalculatesHealthScoreAndRecommendations() {
        val cpu = createTestCpu()
        val gpu = createTestGpu()
        val mobo = createTestMobo()
        val ram = createTestRam(32768)
        val psu = createTestPsu(750)

        val fullBuild = ActiveBuild(
            cpu = cpu,
            gpu = gpu,
            motherboard = mobo,
            ram = ram,
            psu = psu,
        )

        val doctorReport = BuildAnalysisEngine.calculateBuildDoctor(fullBuild)
        assertTrue(doctorReport.healthScore >= 85, "Balanced build should achieve >= 85 health score, got ${doctorReport.healthScore}")
        assertNotNull(doctorReport.doctorDiagnosis)
        assertEquals(3, doctorReport.resolutionBottlenecks.size, "Should map all 3 resolutions")
    }

    @Test
    fun buildDoctorDetectsInsufficientPsuAndSuggestsUpgrade() {
        val cpu = createTestCpu()
        val gpu = createTestGpu()
        val mobo = createTestMobo()
        val psuWeak = createTestPsu(watts = 400) // 400W is far below 650W recommended

        val weakPsuBuild = ActiveBuild(
            cpu = cpu,
            gpu = gpu,
            motherboard = mobo,
            psu = psuWeak,
        )

        val report = BuildAnalysisEngine.calculateBuildDoctor(weakPsuBuild)
        assertTrue(report.healthScore < 85, "Weak PSU must penalize health score")
        assertTrue(report.upgradeRecommendations.any { it.targetCategory == HardwareCategory.PSU }, "Must recommend PSU upgrade")
    }

    @Test
    fun resolutionBottlenecksShiftBetween1080pAnd4K() {
        val cpu = createTestCpu(cores = 4, boostMhz = 3600, score = 45) // Modest CPU
        val gpu = createTestGpu(vramMb = 16384, score = 96) // High-end GPU
        val build = ActiveBuild(cpu = cpu, gpu = gpu)

        val b1080 = BuildAnalysisEngine.calculateBottleneckForResolution(build, ResolutionOption.P1080)
        val b4k = BuildAnalysisEngine.calculateBottleneckForResolution(build, ResolutionOption.P2160)

        // At 1080p, CPU stress is higher than at 4K
        assertTrue(b1080.percentage >= b4k.percentage, "1080p CPU bottleneck should be higher than at 4K")
    }

    @Test
    fun gamingSimulationCalculatesInteractiveMultipliersAndVram() {
        val cpu = createTestCpu()
        val gpu = createTestGpu(vramMb = 8192) // 8GB GPU
        val build = ActiveBuild(cpu = cpu, gpu = gpu)

        val highNative = BuildAnalysisEngine.calculateGamingSimulation(
            build = build,
            resolution = ResolutionOption.P1440,
            preset = QualityPreset.HIGH,
            upscaling = UpscalingMode.OFF,
        )

        val ultraRayTracing = BuildAnalysisEngine.calculateGamingSimulation(
            build = build,
            resolution = ResolutionOption.P2160,
            preset = QualityPreset.RAY_TRACING,
            upscaling = UpscalingMode.OFF,
        )

        assertTrue(highNative.isReady)
        assertTrue(highNative.overallAvgFps > 0)
        assertTrue(highNative.overallLow1Fps > 0)
        assertTrue(highNative.smoothnessRating.isNotBlank())

        // 4K Ray Tracing requires heavy VRAM, which should exceed 8GB and trigger vramWarning
        assertTrue(ultraRayTracing.estimatedVramGb > 8.0f)
        assertTrue(ultraRayTracing.vramWarning, "8GB card at 4K Ray Tracing should trigger vramWarning")

        // Upscaling Performance should boost FPS significantly over Native
        val upscaledPerformance = BuildAnalysisEngine.calculateGamingSimulation(
            build = build,
            resolution = ResolutionOption.P1440,
            preset = QualityPreset.HIGH,
            upscaling = UpscalingMode.PERFORMANCE,
        )
        assertTrue(upscaledPerformance.overallAvgFps > highNative.overallAvgFps, "Upscaling Performance must yield higher FPS than Native")
    }

    @Test
    fun x3dCacheAdvantageGivesSuperiorGamingFps() {
        // Ryzen 7 9800X3D (8 cores, 5200MHz, 96MB L3 cache)
        val x3dCpu = createTestCpu(
            name = "Ryzen 7 9800X3D",
            cores = 8,
            boostMhz = 5200,
            l3Mb = 96,
            score = 96
        )

        // Ryzen 9 9950X (16 cores, 5700MHz, 64MB cache - more raw compute & clock, but no 3D V-Cache)
        val standardComputeCpu = createTestCpu(
            name = "Ryzen 9 9950X",
            cores = 16,
            boostMhz = 5700,
            l3Mb = 64,
            score = 99
        )

        val x3dFps = BenchmarkEngine.resolveGameFpsForItem(x3dCpu)
        val standardFps = BenchmarkEngine.resolveGameFpsForItem(standardComputeCpu)

        assertNotNull(x3dFps?.counterStrike2)
        assertNotNull(standardFps?.counterStrike2)

        val x3dCs2Avg = x3dFps.counterStrike2.p1080
        val standardCs2Avg = standardFps.counterStrike2.p1080

        // In CPU-bound competitive games like CS2 at 1080p, 9800X3D must beat the 9950X due to 3D V-Cache
        assertTrue(
            x3dCs2Avg > standardCs2Avg,
            "Ryzen 7 9800X3D with 3D V-Cache ($x3dCs2Avg FPS) must outperform Ryzen 9 9950X ($standardCs2Avg FPS) in CS2 1080p"
        )
        // Calibrated baseline should be ~850+ FPS matching real lab tests
        assertTrue(x3dCs2Avg >= 850.0, "Ryzen 7 9800X3D 1080p CS2 should simulate around real test result (~922 FPS)")
    }

    @Test
    fun verifiedLabBenchmarksReturnsAccurateDataset() {
        val cpu9800X3D = createTestCpu(name = "Ryzen 7 9800X3D")
        val tests = VerifiedLabBenchmarks.getLabTestsForItem(cpu9800X3D)

        assertTrue(tests.isNotEmpty(), "Verified tests for Ryzen 7 9800X3D must not be empty")
        val cs2Test = tests.find { it.gameTitle == "Counter-Strike 2" }
        assertNotNull(cs2Test)
        assertEquals("NVIDIA GeForce RTX 5090 32GB", cs2Test.rigGpuOrCpu)

        val topEntry = cs2Test.entries.first()
        assertTrue(topEntry.fullModelName.contains("9800X3D"), "Top entry in CS2 test must be Ryzen 7 9800X3D")
        assertEquals(926.0, topEntry.avgFps)
        assertEquals(318.0, topEntry.minFps)
        assertTrue(topEntry.isAmd)
    }
}
