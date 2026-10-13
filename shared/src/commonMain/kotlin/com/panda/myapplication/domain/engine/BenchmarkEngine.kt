package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.BenchmarkResult
import com.panda.myapplication.domain.model.CinebenchScores
import com.panda.myapplication.domain.model.DataSourceType
import com.panda.myapplication.domain.model.GameFpsBenchmark
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.Mark3dScores
import com.panda.myapplication.domain.model.ResolutionFps
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GpuSpec
import kotlin.math.round

object BenchmarkEngine {

    fun evaluate(
        item: HardwareItem,
        verifiedBenchmarks: Map<String, HardwareBenchmarkInfo> = emptyMap(),
    ): BenchmarkResult {
        // 1. Doğrulanmış Benchmark Verisi (Verified Benchmark)
        val verified = verifiedBenchmarks[item.id.value] ?: item.benchmarkInfo?.takeIf { it.isVerified }
        if (verified != null) {
            return BenchmarkResult(
                info = verified.copy(dataSource = DataSourceType.VERIFIED_BENCHMARK.name),
                dataSource = DataSourceType.VERIFIED_BENCHMARK,
            )
        }

        // 2. Matematiksel Bağıl Simülasyon (Estimated Simulation)
        val simulated = simulateBenchmark(item)
        return BenchmarkResult(
            info = simulated,
            dataSource = DataSourceType.ESTIMATED_SIMULATION,
        )
    }

    fun simulateBenchmark(item: HardwareItem): HardwareBenchmarkInfo {
        return when (val s = item.spec) {
            is CpuSpec -> simulateCpu(item, s)
            is GpuSpec -> simulateGpu(item, s)
            else -> simulateGeneral(item)
        }
    }

    private fun simulateCpu(item: HardwareItem, s: CpuSpec): HardwareBenchmarkInfo {
        val archMultiplier = when {
            s.architecture?.contains("Zen 5", ignoreCase = true) == true -> 1.25
            s.architecture?.contains("Zen 4", ignoreCase = true) == true -> 1.15
            s.architecture?.contains("Zen 3", ignoreCase = true) == true -> 1.00
            s.architecture?.contains("Raptor", ignoreCase = true) == true -> 1.18
            s.architecture?.contains("Alder", ignoreCase = true) == true -> 1.08
            s.socket.equals("AM5", ignoreCase = true) -> 1.15
            s.socket.equals("LGA1700", ignoreCase = true) -> 1.10
            else -> 0.90
        }

        val boostGhz = (s.boostClock?.mhz ?: 4000) / 1000.0
        val singleCore = (boostGhz * 380.0 * archMultiplier).toInt().coerceAtLeast(1000)
        val multiCore = ((s.coreCount * boostGhz * 280.0 + s.tdp.watts * 30.0) * archMultiplier).toInt().coerceAtLeast(4000)
        val relativeIndex = ((singleCore * 0.4 + multiCore * 0.0025) / 12.0 * 10.0).coerceIn(20.0, 108.0)
        val score = relativeIndex.toInt().coerceIn(10, 100)
        val tier = when {
            score >= 95 -> "S-Tier"
            score >= 85 -> "A-Tier"
            score >= 70 -> "B-Tier"
            else -> "C-Tier"
        }

        val cbRatio = relativeIndex / 100.0
        val gameFps = GameFpsBenchmark(
            cyberpunk2077 = ResolutionFps(
                p1080 = round1(165.0 * cbRatio),
                p1440 = round1(132.0 * cbRatio),
                p4k = round1(88.0 * cbRatio),
            ),
            forzaHorizon5 = ResolutionFps(
                p1080 = round1(240.0 * cbRatio),
                p1440 = round1(195.0 * cbRatio),
                p4k = round1(130.0 * cbRatio),
            ),
            counterStrike2 = ResolutionFps(
                p1080 = round1(580.0 * cbRatio),
                p1440 = round1(460.0 * cbRatio),
                p4k = round1(285.0 * cbRatio),
            ),
        )

        return HardwareBenchmarkInfo(
            score = score,
            tier = tier,
            relativePerformanceIndex = round1(relativeIndex),
            cinebenchR23 = CinebenchScores(singleCore = singleCore, multiCore = multiCore),
            mark3d = Mark3dScores(timeSpy = (multiCore * 0.75).toInt()),
            gameFps = gameFps,
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = "Matematiksel IPC & Saat Hızı Simülasyonu",
            singleCoreScore = singleCore,
            multiCoreScore = multiCore,
            timeSpyScore = (multiCore * 0.75).toInt(),
            gamingIndex = score,
            productivityIndex = (multiCore / 350).coerceIn(10, 100),
        )
    }

    private fun simulateGpu(item: HardwareItem, s: GpuSpec): HardwareBenchmarkInfo {
        val vramGb = s.vram.megabytes / 1024.0
        val tdp = s.tdp.watts.toDouble()
        val clockMhz = s.boostClock?.mhz ?: s.coreClock.mhz
        val rawPower = (tdp * 1.5 + vramGb * 80.0 + (clockMhz / 100.0) * 15.0)
        val relativeIndex = (rawPower / 9.5).coerceIn(15.0, 100.0)
        val score = relativeIndex.toInt().coerceIn(10, 100)
        val timeSpyGraphics = (relativeIndex * 310.0).toInt()
        val fireStrike = (relativeIndex * 700.0).toInt()
        val portRoyal = (relativeIndex * 210.0).toInt()
        val tier = when {
            score >= 93 -> "S-Tier"
            score >= 80 -> "A-Tier"
            score >= 60 -> "B-Tier"
            else -> "C-Tier"
        }

        val gpuRatio = relativeIndex / 100.0
        val gameFps = GameFpsBenchmark(
            cyberpunk2077 = ResolutionFps(
                p1080 = round1(155.0 * gpuRatio),
                p1440 = round1(118.0 * gpuRatio),
                p4k = round1(68.0 * gpuRatio),
            ),
            forzaHorizon5 = ResolutionFps(
                p1080 = round1(225.0 * gpuRatio),
                p1440 = round1(175.0 * gpuRatio),
                p4k = round1(122.0 * gpuRatio),
            ),
            counterStrike2 = ResolutionFps(
                p1080 = round1(540.0 * gpuRatio),
                p1440 = round1(430.0 * gpuRatio),
                p4k = round1(270.0 * gpuRatio),
            ),
        )

        return HardwareBenchmarkInfo(
            score = score,
            tier = tier,
            relativePerformanceIndex = round1(relativeIndex),
            mark3d = Mark3dScores(timeSpy = timeSpyGraphics, fireStrike = fireStrike, portRoyal = portRoyal),
            gameFps = gameFps,
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = "Matematiksel TFLOPS & Raster Simülasyonu",
            timeSpyScore = timeSpyGraphics,
            gamingIndex = score,
            productivityIndex = (relativeIndex * 0.95).toInt().coerceIn(10, 100),
        )
    }

    private fun simulateGeneral(item: HardwareItem): HardwareBenchmarkInfo {
        val seed = kotlin.math.abs(item.id.value.hashCode())
        val baseScore = 70 + (seed % 28)
        val tier = when {
            baseScore >= 94 -> "S-Tier"
            baseScore >= 85 -> "A-Tier"
            baseScore >= 75 -> "B-Tier"
            else -> "C-Tier"
        }
        val testSuite = when (item.category) {
            HardwareCategory.RAM -> "AIDA64 Memory Latency & Bandwidth"
            HardwareCategory.MOTHERBOARD -> "VRM Thermal Load & Signal Stability"
            HardwareCategory.STORAGE -> "CrystalDiskMark 8 & PCMark 10"
            HardwareCategory.COOLER -> "Thermal Dissipation 30 Dk Stres Testi"
            HardwareCategory.PSU -> "Cybenetics Load & Transient Response"
            HardwareCategory.CASE -> "Wind Tunnel Smoke Flow & Thermal"
            else -> "Donanım Standartları Bağıl Analizi"
        }
        return HardwareBenchmarkInfo(
            score = baseScore,
            tier = tier,
            relativePerformanceIndex = baseScore.toDouble(),
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = testSuite,
            gamingIndex = baseScore,
            productivityIndex = baseScore,
        )
    }

    private fun round1(value: Double): Double = round(value * 10.0) / 10.0
}
