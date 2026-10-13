package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.BenchmarkResult
import com.panda.myapplication.domain.model.CpuSyntheticBenchmark
import com.panda.myapplication.domain.model.DataSourceType
import com.panda.myapplication.domain.model.GameFpsBenchmark
import com.panda.myapplication.domain.model.GpuSyntheticBenchmark
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
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

        val cb2024Single = (singleCore / 16.5).toInt().coerceAtLeast(60)
        val cb2024Multi = (multiCore / 15.2).toInt().coerceAtLeast(800)
        val gb6Single = (singleCore * 1.52).toInt().coerceAtLeast(1500)
        val gb6Multi = (multiCore * 0.62).toInt().coerceAtLeast(6000)
        val passmark = (multiCore * 1.63).toInt().coerceAtLeast(15000)

        val cpuSynthetic = CpuSyntheticBenchmark(
            cinebench2024Single = cb2024Single,
            cinebench2024Multi = cb2024Multi,
            cinebenchR23Single = singleCore,
            cinebenchR23Multi = multiCore,
            geekbench6Single = gb6Single,
            geekbench6Multi = gb6Multi,
            passmarkScore = passmark,
        )

        return HardwareBenchmarkInfo(
            score = score,
            tier = tier,
            gamingRelativeIndex = round1(relativeIndex) / 100.0,
            relativePerformanceIndex = round1(relativeIndex),
            cpuSynthetic = cpuSynthetic,
            gameFps = gameFps,
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = "Matematiksel IPC & Saat Hızı Simülasyonu",
        )
    }

    private fun simulateGpu(item: HardwareItem, s: GpuSpec): HardwareBenchmarkInfo {
        val vramGb = s.vram.megabytes / 1024.0
        val tdp = s.tdp.watts.toDouble()
        val clockMhz = s.boostClock?.mhz ?: s.coreClock.mhz
        val rawPower = (tdp * 1.5 + vramGb * 80.0 + (clockMhz / 100.0) * 15.0)
        val relativeIndex = (rawPower / 9.5).coerceIn(15.0, 100.0)
        val score = relativeIndex.toInt().coerceIn(10, 100)
        val timeSpyGfx = (relativeIndex * 310.0).toInt()
        val timeSpyExt = (timeSpyGfx * 0.49).toInt()
        val steelNomad = (timeSpyGfx * 0.238).toInt()
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

        val gpuSynthetic = GpuSyntheticBenchmark(
            timeSpyGraphics = timeSpyGfx,
            timeSpyExtreme = timeSpyExt,
            steelNomad = steelNomad,
        )

        return HardwareBenchmarkInfo(
            score = score,
            tier = tier,
            relativePerformancePct = round1(relativeIndex) / 100.0,
            relativePerformanceIndex = round1(relativeIndex),
            gpuSynthetic = gpuSynthetic,
            gameFps = gameFps,
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = "Matematiksel TFLOPS & Raster Simülasyonu",
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
