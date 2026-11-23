package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.BenchmarkResult
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.CpuSyntheticBenchmark
import com.panda.myapplication.domain.model.DataSourceType
import com.panda.myapplication.domain.model.GameFpsBenchmark
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.GpuSyntheticBenchmark
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.ResolutionFps
import kotlin.math.round
import kotlin.math.roundToInt

object BenchmarkEngine {

    fun evaluate(
        item: HardwareItem,
        verifiedBenchmarks: Map<String, HardwareBenchmarkInfo> = emptyMap(),
    ): BenchmarkResult {
        val isCpu = item.category == HardwareCategory.CPU
        val isGpu = item.category == HardwareCategory.GPU

        val gpuChipsetKey = if (isGpu) {
            val chipset = (item.spec as? GpuSpec)?.chipset ?: item.model
            normalizeGpuChipsetKey(chipset, item.model)
        } else null

        val verified = verifiedBenchmarks[item.id.value]
            ?: (gpuChipsetKey?.let { verifiedBenchmarks["gpu_$it"] ?: verifiedBenchmarks[it] })
            ?: item.benchmarkInfo?.takeIf { it.isVerified }

        if (isCpu) {
            val profile = UnifiedFpsSimulator.resolveCpuProfile(item)
            val games = verified?.gameFps ?: UnifiedFpsSimulator.simulateForCpuDetail(item)
            if (verified != null) {
                return BenchmarkResult(
                    info = verified.copy(
                        gamingIndex = verified.gamingIndex ?: profile.gamingIndex.roundToInt(),
                        productivityIndex = verified.productivityIndex ?: profile.productivityIndex.roundToInt(),
                        gameFps = games,
                        dataSource = DataSourceType.VERIFIED_BENCHMARK.name,
                    ),
                    dataSource = DataSourceType.VERIFIED_BENCHMARK,
                )
            }
            val baseSyn = simulateCpuSynthetic(item, item.spec as? CpuSpec, profile)
            val info = HardwareBenchmarkInfo(
                score = profile.overallScore100,
                tier = UnifiedFpsSimulator.tierForCpu(profile.gamingIndex, profile.productivityIndex),
                gamingIndex = profile.gamingIndex.roundToInt(),
                productivityIndex = profile.productivityIndex.roundToInt(),
                gamingRelativeIndex = round1(profile.gamingIndex) / 100.0,
                relativePerformanceIndex = round1(profile.gamingIndex),
                cpuSynthetic = baseSyn,
                gameFps = games,
                dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
                testSuite = "ML Log-Ridge Calibrated v6 (Ref: RTX 5090)",
            )
            return BenchmarkResult(info = info, dataSource = DataSourceType.ESTIMATED_SIMULATION)
        }

        if (isGpu) {
            val profile = UnifiedFpsSimulator.resolveGpuProfile(item)
            val games = verified?.gameFps ?: UnifiedFpsSimulator.simulateForGpuDetail(item)
            if (verified != null) {
                return BenchmarkResult(
                    info = verified.copy(
                        gamingIndex = verified.gamingIndex ?: profile.relativeIndex.roundToInt(),
                        gameFps = games,
                        dataSource = DataSourceType.VERIFIED_BENCHMARK.name,
                    ),
                    dataSource = DataSourceType.VERIFIED_BENCHMARK,
                )
            }
            val baseSyn = GpuSyntheticBenchmark(
                timeSpyGraphics = (profile.relativeIndex * 365.0).roundToInt(),
                timeSpyExtreme = (profile.relativeIndex * 185.0).roundToInt(),
                steelNomad = (profile.relativeIndex * 96.0).roundToInt(),
            )
            val info = HardwareBenchmarkInfo(
                score = profile.score100,
                tier = UnifiedFpsSimulator.tierForGpuRelativeIndex(profile.relativeIndex),
                gamingIndex = profile.relativeIndex.roundToInt(),
                relativePerformancePct = round1(profile.relativeIndex) / 100.0,
                relativePerformanceIndex = round1(profile.relativeIndex),
                gpuSynthetic = baseSyn,
                gameFps = games,
                dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
                testSuite = "ML Log-Ridge Calibrated v6 (Ref: Ryzen 7 9800X3D)",
            )
            return BenchmarkResult(info = info, dataSource = DataSourceType.ESTIMATED_SIMULATION)
        }

        if (verified != null) {
            return BenchmarkResult(
                info = verified.copy(dataSource = DataSourceType.VERIFIED_BENCHMARK.name),
                dataSource = DataSourceType.VERIFIED_BENCHMARK,
            )
        }

        val simulated = simulateBenchmark(item)
        return BenchmarkResult(
            info = simulated,
            dataSource = DataSourceType.ESTIMATED_SIMULATION,
        )
    }

    /**
     * Resolves a complete 12-game FPS matrix (5 E-Spor + 7 AAA) across 1080p, 1440p, and 4K
     * using the Unified Physical Frame-Time Engine (Single Source of Truth across Detail, Wizard & VS Arena).
     */
    fun resolveGameFpsForItem(item: HardwareItem): GameFpsBenchmark? {
        return when (val s = item.spec) {
            is GpuSpec -> {
                val bench = item.benchmarkInfo
                if (bench?.gameFps != null && bench.testSuite.contains("v6", ignoreCase = true)) {
                    bench.gameFps
                } else {
                    UnifiedFpsSimulator.simulateForGpuDetail(item)
                }
            }
            is CpuSpec -> {
                val bench = item.benchmarkInfo
                if (bench?.gameFps != null && bench.testSuite.contains("v6", ignoreCase = true)) {
                    bench.gameFps
                } else {
                    UnifiedFpsSimulator.simulateForCpuDetail(item)
                }
            }
            is LaptopSpec -> UnifiedFpsSimulator.simulateForLaptopDetail(s)
            else -> null
        }
    }

    /**
     * Generates or enriches the 12-game FPS matrix with calibrated reference coefficients.
     */
    fun buildFull12GameMatrix(
        ratio: Double,
        isCpu: Boolean = false,
        isX3d: Boolean = false,
        existing: GameFpsBenchmark? = null,
    ): GameFpsBenchmark {
        val r = ratio.coerceIn(0.15, 1.40)
        val x3dEsportsBoost = if (isX3d) 1.28 else 1.0
        val x3dAaaBoost = if (isX3d) 1.18 else 1.0
        val lowRatioEsports = if (isCpu) (if (isX3d) 0.74 else 0.68) else (if (isX3d) 0.85 else 0.78)
        val lowRatioAaa = if (isCpu) (if (isX3d) 0.76 else 0.73) else (if (isX3d) 0.84 else 0.79)

        fun makeFps(base1080: Double, base1440: Double, base4k: Double, lowFactor: Double, override: ResolutionFps?): ResolutionFps {
            val minF = if (isX3d) 0.86 else 0.80
            val maxF = if (isCpu) (if (isX3d) 1.34 else 1.30) else 1.25
            if (override != null && override.p1080 > 0.0) {
                val l1080 = override.low1Pct1080 ?: round1(override.p1080 * lowFactor)
                val l1440 = override.low1Pct1440 ?: round1(override.p1440 * (lowFactor + 0.01))
                val l4k = override.low1Pct4k ?: round1(override.p4k * (lowFactor + 0.02))
                return override.copy(
                    low1Pct1080 = l1080,
                    low1Pct1440 = l1440,
                    low1Pct4k = l4k,
                    max1080p = override.max1080p ?: round1(override.p1080 * maxF),
                    max1440p = override.max1440p ?: round1(override.p1440 * (maxF - 0.03)),
                    max4k = override.max4k ?: round1(override.p4k * (maxF - 0.06)),
                    min1080p = override.min1080p ?: round1(l1080 * minF),
                    min1440p = override.min1440p ?: round1(l1440 * minF),
                    min4k = override.min4k ?: round1(l4k * (minF - 0.01)),
                )
            }
            val p1080 = round1((base1080 * r).coerceAtLeast(18.0))
            val p1440 = round1((base1440 * r).coerceAtLeast(14.0))
            val p4k = round1((base4k * r).coerceAtLeast(10.0))
            val l1080 = round1(p1080 * lowFactor)
            val l1440 = round1(p1440 * (lowFactor + 0.01))
            val l4k = round1(p4k * (lowFactor + 0.02))
            return ResolutionFps(
                p1080 = p1080,
                p1440 = p1440,
                p4k = p4k,
                low1Pct1080 = l1080,
                low1Pct1440 = l1440,
                low1Pct4k = l4k,
                max1080p = round1(p1080 * maxF),
                max1440p = round1(p1440 * (maxF - 0.03)),
                max4k = round1(p4k * (maxF - 0.06)),
                min1080p = round1(l1080 * minF),
                min1440p = round1(l1440 * minF),
                min4k = round1(l4k * (minF - 0.01)),
            )
        }

        val cpuScale4k = if (isCpu) 1.12 else 1.0
        val cs2Base1080 = if (isCpu) 720.0 else 540.0
        val valorantBase1080 = if (isCpu) 820.0 else 640.0
        val cyberpunkBase1080 = if (isCpu) 205.0 else 155.0

        return GameFpsBenchmark(
            // 5 E-Spor / Rekabetçi Oyun
            counterStrike2 = makeFps(
                base1080 = cs2Base1080 * x3dEsportsBoost,
                base1440 = (cs2Base1080 * 0.78) * if (isX3d) 1.15 else 1.0,
                base4k = 270.0 * cpuScale4k,
                lowFactor = lowRatioEsports,
                override = existing?.counterStrike2,
            ),
            valorant = makeFps(
                base1080 = valorantBase1080 * x3dEsportsBoost,
                base1440 = (valorantBase1080 * 0.78) * if (isX3d) 1.15 else 1.0,
                base4k = 360.0 * cpuScale4k,
                lowFactor = lowRatioEsports,
                override = existing?.valorant,
            ),
            apexLegends = makeFps(
                base1080 = (if (isCpu) 360.0 else 295.0) * if (isX3d) 1.16 else 1.0,
                base1440 = 245.0,
                base4k = 168.0 * cpuScale4k,
                lowFactor = lowRatioEsports,
                override = existing?.apexLegends,
            ),
            warzone = makeFps(
                base1080 = (if (isCpu) 280.0 else 225.0) * if (isX3d) 1.18 else 1.0,
                base1440 = 182.0,
                base4k = 122.0 * cpuScale4k,
                lowFactor = lowRatioEsports - 0.02,
                override = existing?.warzone,
            ),
            pubg = makeFps(
                base1080 = (if (isCpu) 340.0 else 285.0) * if (isX3d) 1.16 else 1.0,
                base1440 = 220.0,
                base4k = 142.0 * cpuScale4k,
                lowFactor = lowRatioEsports - 0.02,
                override = existing?.pubg,
            ),
            // 7 AAA / Ağır Grafik Oyun
            cyberpunk2077 = makeFps(
                base1080 = cyberpunkBase1080 * x3dAaaBoost,
                base1440 = 118.0,
                base4k = 68.0 * cpuScale4k,
                lowFactor = lowRatioAaa,
                override = existing?.cyberpunk2077,
            ),
            alanWake2 = makeFps(
                base1080 = 128.0,
                base1440 = 96.0,
                base4k = 56.0 * cpuScale4k,
                lowFactor = lowRatioAaa,
                override = existing?.alanWake2,
            ),
            redDeadRedemption2 = makeFps(
                base1080 = 185.0,
                base1440 = 148.0,
                base4k = 98.0 * cpuScale4k,
                lowFactor = lowRatioAaa + 0.02,
                override = existing?.redDeadRedemption2,
            ),
            blackMythWukong = makeFps(
                base1080 = 134.0,
                base1440 = 102.0,
                base4k = 60.0 * cpuScale4k,
                lowFactor = lowRatioAaa - 0.01,
                override = existing?.blackMythWukong,
            ),
            forzaHorizon5 = makeFps(
                base1080 = 225.0,
                base1440 = 175.0,
                base4k = 122.0 * cpuScale4k,
                lowFactor = lowRatioAaa + 0.03,
                override = existing?.forzaHorizon5,
            ),
            hogwartsLegacy = makeFps(
                base1080 = 160.0 * x3dEsportsBoost,
                base1440 = 124.0,
                base4k = 76.0 * cpuScale4k,
                lowFactor = lowRatioAaa - 0.02,
                override = existing?.hogwartsLegacy,
            ),
            baldursGate3 = makeFps(
                base1080 = 190.0 * x3dEsportsBoost,
                base1440 = 154.0 * x3dEsportsBoost,
                base4k = 104.0 * cpuScale4k,
                lowFactor = lowRatioAaa,
                override = existing?.baldursGate3,
            ),
        )
    }

    private fun buildFull12GameMatrixByResolutionRatios(
        r1080: Double,
        r1440: Double,
        r4k: Double,
        isX3d: Boolean,
    ): GameFpsBenchmark {
        val lowFactor = if (isX3d) 0.84 else 0.78
        val minF = if (isX3d) 0.86 else 0.80
        val maxF = if (isX3d) 1.32 else 1.26
        fun make(b1080: Double, b1440: Double, b4k: Double, lf: Double = lowFactor): ResolutionFps {
            val p1080 = round1((b1080 * r1080).coerceAtLeast(18.0))
            val p1440 = round1((b1440 * r1440).coerceAtLeast(14.0))
            val p4k = round1((b4k * r4k).coerceAtLeast(10.0))
            val l1080 = round1(p1080 * lf)
            val l1440 = round1(p1440 * (lf + 0.01))
            val l4k = round1(p4k * (lf + 0.02))
            return ResolutionFps(
                p1080 = p1080,
                p1440 = p1440,
                p4k = p4k,
                low1Pct1080 = l1080,
                low1Pct1440 = l1440,
                low1Pct4k = l4k,
                max1080p = round1(p1080 * maxF),
                max1440p = round1(p1440 * (maxF - 0.03)),
                max4k = round1(p4k * (maxF - 0.06)),
                min1080p = round1(l1080 * minF),
                min1440p = round1(l1440 * minF),
                min4k = round1(l4k * (minF - 0.01)),
            )
        }
        return GameFpsBenchmark(
            counterStrike2 = make(540.0, 430.0, 270.0, lowFactor + 0.01),
            valorant = make(640.0, 520.0, 360.0, lowFactor + 0.02),
            apexLegends = make(295.0, 245.0, 168.0, lowFactor),
            warzone = make(225.0, 182.0, 122.0, lowFactor - 0.02),
            pubg = make(285.0, 220.0, 142.0, lowFactor - 0.01),
            cyberpunk2077 = make(155.0, 118.0, 68.0, lowFactor),
            alanWake2 = make(128.0, 96.0, 56.0, lowFactor),
            redDeadRedemption2 = make(185.0, 148.0, 98.0, lowFactor + 0.02),
            blackMythWukong = make(134.0, 102.0, 60.0, lowFactor - 0.01),
            forzaHorizon5 = make(225.0, 175.0, 122.0, lowFactor + 0.03),
            hogwartsLegacy = make(160.0, 124.0, 76.0, lowFactor - 0.02),
            baldursGate3 = make(190.0, 154.0, 104.0, lowFactor),
        )
    }

    fun simulateBenchmark(item: HardwareItem): HardwareBenchmarkInfo {
        return when (val s = item.spec) {
            is CpuSpec -> simulateCpu(item, s)
            is GpuSpec -> simulateGpu(item, s)
            else -> simulateGeneral()
        }
    }

    private fun simulateCpuSynthetic(
        item: HardwareItem,
        s: CpuSpec?,
        profile: UnifiedFpsSimulator.CpuSimProfile,
    ): CpuSyntheticBenchmark {
        val cbMulti = (profile.productivityIndex * 432.0).roundToInt().coerceAtLeast(3000)
        val cores = (s?.coreCount ?: profile.cores).coerceAtLeast(2)
        val cbSingle = (profile.gamingIndex * 22.0).roundToInt().coerceIn(850, 2500)
        val cb2024Single = (cbSingle / 16.5).roundToInt().coerceAtLeast(55)
        val cb2024Multi = (cbMulti / 18.0).roundToInt().coerceAtLeast(350)
        val gb6Single = (cbSingle * 1.48).roundToInt().coerceAtLeast(1200)
        val gb6Multi = (cbMulti * 0.55).roundToInt().coerceAtLeast(4500)
        val passmark = (cbMulti * 1.54).roundToInt().coerceAtLeast(cores * 1200)
        return CpuSyntheticBenchmark(
            cinebench2024Single = cb2024Single,
            cinebench2024Multi = cb2024Multi,
            cinebenchR23Single = cbSingle,
            cinebenchR23Multi = cbMulti,
            geekbench6Single = gb6Single,
            geekbench6Multi = gb6Multi,
            passmarkScore = passmark,
        )
    }

    private fun simulateCpu(item: HardwareItem, s: CpuSpec): HardwareBenchmarkInfo {
        val profile = UnifiedFpsSimulator.resolveCpuProfile(item)
        val gameFps = UnifiedFpsSimulator.simulateForCpuDetail(item)
        val cpuSynthetic = simulateCpuSynthetic(item, s, profile)

        return HardwareBenchmarkInfo(
            score = profile.overallScore100,
            tier = UnifiedFpsSimulator.tierForCpu(profile.gamingIndex, profile.productivityIndex),
            gamingIndex = profile.gamingIndex.roundToInt(),
            productivityIndex = profile.productivityIndex.roundToInt(),
            gamingRelativeIndex = round1(profile.gamingIndex) / 100.0,
            relativePerformanceIndex = round1(profile.gamingIndex),
            cpuSynthetic = cpuSynthetic,
            gameFps = gameFps,
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = "ML Log-Ridge Kalibreli IPC & 3D V-Cache Simülasyonu (v6)",
        )
    }

    private fun simulateGpu(item: HardwareItem, s: GpuSpec): HardwareBenchmarkInfo {
        val profile = UnifiedFpsSimulator.resolveGpuProfile(item)
        val timeSpyGfx = (profile.relativeIndex * 365.0).roundToInt()
        val timeSpyExt = (timeSpyGfx * 0.51).roundToInt()
        val steelNomad = (profile.relativeIndex * 96.0).roundToInt()
        val gameFps = UnifiedFpsSimulator.simulateForGpuDetail(item)

        val gpuSynthetic = GpuSyntheticBenchmark(
            timeSpyGraphics = timeSpyGfx,
            timeSpyExtreme = timeSpyExt,
            steelNomad = steelNomad,
        )

        return HardwareBenchmarkInfo(
            score = profile.score100,
            tier = UnifiedFpsSimulator.tierForGpuRelativeIndex(profile.relativeIndex),
            gamingIndex = profile.relativeIndex.roundToInt(),
            relativePerformancePct = round1(profile.relativeIndex) / 100.0,
            relativePerformanceIndex = round1(profile.relativeIndex),
            gpuSynthetic = gpuSynthetic,
            gameFps = gameFps,
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = "ML Log-Ridge Kalibreli Raster & VRAM Simülasyonu (v6)",
        )
    }

    private fun simulateGeneral(): HardwareBenchmarkInfo {
        return HardwareBenchmarkInfo(
            score = 0,
            tier = "",
            relativePerformanceIndex = 0.0,
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = "",
            gamingIndex = 0,
            productivityIndex = 0,
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
            text.contains("3050") -> "rtx_3050"
            text.contains("arc b580") || text.contains("b580") -> "arc_b580"
            text.contains("arc a770") || text.contains("a770") -> "arc_a770"
            text.contains("arc a750") || text.contains("a750") -> "arc_a750"
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
            text.contains("9070 xt") || text.contains("9070xt") -> "rx_9070_xt"
            text.contains("9070") -> "rx_9070"
            else -> null
        }
    }

    private fun round1(value: Double): Double = round(value * 10.0) / 10.0
}
