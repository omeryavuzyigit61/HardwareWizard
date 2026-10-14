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
        // 1. Doğrulanmış Benchmark Verisi (Tam ID veya GPU Çip Normalizasyonu)
        val gpuChipsetKey = if (item.category == HardwareCategory.GPU) {
            val chipset = (item.spec as? GpuSpec)?.chipset ?: item.model
            normalizeGpuChipsetKey(chipset, item.model)
        } else null

        val verified = verifiedBenchmarks[item.id.value]
            ?: (gpuChipsetKey?.let { verifiedBenchmarks["gpu_$it"] ?: verifiedBenchmarks[it] })
            ?: item.benchmarkInfo?.takeIf { it.isVerified }

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

    fun normalizeGpuChipsetKey(chipset: String, model: String = ""): String? {
        val text = "$chipset $model".lowercase()
        return when {
            text.contains("5090") -> "rtx_5090"
            text.contains("5080") -> "rtx_5080"
            text.contains("5070 ti") || text.contains("5070ti") -> "rtx_5070_ti"
            text.contains("5070") -> "rtx_5070"
            text.contains("5060 ti") || text.contains("5060ti") -> "rtx_5060_ti"
            text.contains("4090") -> "rtx_4090"
            text.contains("4080 super") || text.contains("4080s") -> "rtx_4080_super"
            text.contains("4080") -> "rtx_4080"
            text.contains("4070 ti super") || text.contains("4070tis") -> "rtx_4070_ti_super"
            text.contains("4070 ti") || text.contains("4070ti") -> "rtx_4070_ti"
            text.contains("4070 super") || text.contains("4070s") -> "rtx_4070_super"
            text.contains("4070") -> "rtx_4070"
            text.contains("4060 ti") || text.contains("4060ti") -> "rtx_4060_ti"
            text.contains("4060") -> "rtx_4060"
            text.contains("3090 ti") || text.contains("3090ti") -> "rtx_3090_ti"
            text.contains("3090") -> "rtx_3090"
            text.contains("3080 ti") || text.contains("3080ti") -> "rtx_3080_ti"
            text.contains("3080") -> "rtx_3080"
            text.contains("3070 ti") || text.contains("3070ti") -> "rtx_3070_ti"
            text.contains("3070") -> "rtx_3070"
            text.contains("3060 ti") || text.contains("3060ti") -> "rtx_3060_ti"
            text.contains("3060") -> "rtx_3060"
            text.contains("7900 xtx") || text.contains("7900xtx") -> "rx_7900_xtx"
            text.contains("7900 xt") || text.contains("7900xt") -> "rx_7900_xt"
            text.contains("7900 gre") || text.contains("7900gre") -> "rx_7900_gre"
            text.contains("7800 xt") || text.contains("7800xt") -> "rx_7800_xt"
            text.contains("7700 xt") || text.contains("7700xt") -> "rx_7700_xt"
            text.contains("7600 xt") || text.contains("7600xt") -> "rx_7600_xt"
            text.contains("7600") -> "rx_7600"
            text.contains("6950 xt") || text.contains("6950xt") -> "rx_6950_xt"
            text.contains("6800 xt") || text.contains("6800xt") -> "rx_6800_xt"
            text.contains("6700 xt") || text.contains("6700xt") -> "rx_6700_xt"
            text.contains("6600 xt") || text.contains("6600xt") -> "rx_6600_xt"
            text.contains("6600") -> "rx_6600"
            else -> null
        }
    }

    private fun round1(value: Double): Double = round(value * 10.0) / 10.0
}
