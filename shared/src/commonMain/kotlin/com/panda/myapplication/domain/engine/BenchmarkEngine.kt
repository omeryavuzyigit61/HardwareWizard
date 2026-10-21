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
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.ResolutionFps
import kotlin.math.round

object BenchmarkEngine {

    fun evaluate(
        item: HardwareItem,
        verifiedBenchmarks: Map<String, HardwareBenchmarkInfo> = emptyMap(),
    ): BenchmarkResult {
        val isCpu = item.category == HardwareCategory.CPU
        val isX3d = item.model.contains("X3D", ignoreCase = true)

        val gpuChipsetKey = if (item.category == HardwareCategory.GPU) {
            val chipset = (item.spec as? GpuSpec)?.chipset ?: item.model
            normalizeGpuChipsetKey(chipset, item.model)
        } else null

        val verified = verifiedBenchmarks[item.id.value]
            ?: (gpuChipsetKey?.let { verifiedBenchmarks["gpu_$it"] ?: verifiedBenchmarks[it] })
            ?: item.benchmarkInfo?.takeIf { it.isVerified }

        if (verified != null) {
            val ratio = (verified.relativePerformanceIndex / 100.0).coerceIn(0.15, 1.35)
            val enrichedGames = buildFull12GameMatrix(
                ratio = ratio,
                isCpu = isCpu,
                isX3d = isX3d,
                existing = verified.gameFps,
            )
            return BenchmarkResult(
                info = verified.copy(
                    gameFps = enrichedGames,
                    dataSource = DataSourceType.VERIFIED_BENCHMARK.name,
                ),
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
     * with Average FPS and 1% Low FPS for GPU, CPU, Prebuilt PC, and Gaming Laptop items.
     */
    fun resolveGameFpsForItem(item: HardwareItem): GameFpsBenchmark? {
        return when (val s = item.spec) {
            is GpuSpec -> {
                val existing = item.benchmarkInfo?.gameFps
                val ratio = ((item.benchmarkInfo?.relativePerformanceIndex ?: simulateGpu(item, s).relativePerformanceIndex) / 100.0)
                    .coerceIn(0.18, 1.35)
                buildFull12GameMatrix(ratio = ratio, isCpu = false, isX3d = false, existing = existing)
            }
            is CpuSpec -> {
                val existing = item.benchmarkInfo?.gameFps
                val isX3d = item.model.contains("X3D", ignoreCase = true) || (s.l3CacheMb ?: 0) >= 96
                val ratio = ((item.benchmarkInfo?.relativePerformanceIndex ?: simulateCpu(item, s).relativePerformanceIndex) / 100.0)
                    .coerceIn(0.25, 1.25)
                buildFull12GameMatrix(ratio = ratio, isCpu = true, isX3d = isX3d, existing = existing)
            }
            is PrebuiltSpec -> {
                val isX3d = s.cpuModel.contains("X3D", ignoreCase = true)
                val base1080 = s.fps1080p.takeIf { it > 0 }?.toDouble()
                val base1440 = s.fps1440p.takeIf { it > 0 }?.toDouble()
                val base4k = s.fps4k.takeIf { it > 0 }?.toDouble()
                val scoreRatio = if (s.gamingScore > 0) (s.gamingScore / 100.0).coerceIn(0.25, 1.25) else 0.72
                val ratio1080 = base1080?.let { (it / 165.0).coerceIn(0.25, 1.30) } ?: scoreRatio
                val ratio1440 = base1440?.let { (it / 120.0).coerceIn(0.22, 1.30) } ?: (ratio1080 * 0.96)
                val ratio4k = base4k?.let { (it / 70.0).coerceIn(0.18, 1.30) } ?: (ratio1440 * 0.90)
                buildFull12GameMatrixByResolutionRatios(
                    r1080 = ratio1080,
                    r1440 = ratio1440,
                    r4k = ratio4k,
                    isX3d = isX3d,
                )
            }
            is LaptopSpec -> {
                val base1080 = s.fps1080p.takeIf { it > 0 }?.toDouble()
                val base1440 = s.fps1440p.takeIf { it > 0 }?.toDouble()
                val hasMux = s.muxSwitch.contains("Var", ignoreCase = true) || s.muxSwitch.contains("Advanced Optimus", ignoreCase = true)
                val muxBonus = if (hasMux) 1.04 else 0.97
                val scoreRatio = if (s.gamingScore > 0) ((s.gamingScore / 100.0) * muxBonus).coerceIn(0.22, 1.15) else 0.65
                val ratio1080 = base1080?.let { (it / 155.0).coerceIn(0.22, 1.20) } ?: scoreRatio
                val ratio1440 = base1440?.let { (it / 115.0).coerceIn(0.20, 1.18) } ?: (ratio1080 * 0.92)
                val ratio4k = (ratio1440 * 0.82).coerceIn(0.15, 1.10)
                buildFull12GameMatrixByResolutionRatios(
                    r1080 = ratio1080,
                    r1440 = ratio1440,
                    r4k = ratio4k,
                    isX3d = s.cpuModel.contains("X3D", ignoreCase = true) || s.cpuModel.contains("HX", ignoreCase = true),
                )
            }
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
        val lowRatioEsports = if (isCpu) (if (isX3d) 0.345 else 0.310) else (if (isX3d) 0.85 else 0.78)
        val lowRatioAaa = if (isCpu) (if (isX3d) 0.76 else 0.73) else (if (isX3d) 0.84 else 0.79)

        fun makeFps(base1080: Double, base1440: Double, base4k: Double, lowFactor: Double, override: ResolutionFps?): ResolutionFps {
            if (override != null && override.p1080 > 0.0) {
                return override.copy(
                    low1Pct1080 = override.low1Pct1080 ?: round1(override.p1080 * lowFactor),
                    low1Pct1440 = override.low1Pct1440 ?: round1(override.p1440 * (lowFactor + 0.01)),
                    low1Pct4k = override.low1Pct4k ?: round1(override.p4k * (lowFactor + 0.02)),
                )
            }
            val p1080 = round1((base1080 * r).coerceAtLeast(18.0))
            val p1440 = round1((base1440 * r).coerceAtLeast(14.0))
            val p4k = round1((base4k * r).coerceAtLeast(10.0))
            return ResolutionFps(
                p1080 = p1080,
                p1440 = p1440,
                p4k = p4k,
                low1Pct1080 = round1(p1080 * lowFactor),
                low1Pct1440 = round1(p1440 * (lowFactor + 0.01)),
                low1Pct4k = round1(p4k * (lowFactor + 0.02)),
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
        fun make(b1080: Double, b1440: Double, b4k: Double, lf: Double = lowFactor): ResolutionFps {
            val p1080 = round1((b1080 * r1080).coerceAtLeast(18.0))
            val p1440 = round1((b1440 * r1440).coerceAtLeast(14.0))
            val p4k = round1((b4k * r4k).coerceAtLeast(10.0))
            return ResolutionFps(
                p1080 = p1080,
                p1440 = p1440,
                p4k = p4k,
                low1Pct1080 = round1(p1080 * lf),
                low1Pct1440 = round1(p1440 * (lf + 0.01)),
                low1Pct4k = round1(p4k * (lf + 0.02)),
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

        val isX3d = item.model.contains("X3D", ignoreCase = true) || (s.l3CacheMb ?: 0) >= 96
        val isArrowLake = s.architecture?.contains("Arrow", ignoreCase = true) == true ||
                item.model.contains("285K", ignoreCase = true) ||
                item.model.contains("265", ignoreCase = true) ||
                item.model.contains("245", ignoreCase = true) ||
                item.model.contains("225", ignoreCase = true) ||
                s.socket.contains("1851", ignoreCase = true)

        val boostGhz = (s.boostClock?.mhz ?: 4000) / 1000.0
        val singleCore = (boostGhz * 380.0 * archMultiplier).toInt().coerceAtLeast(1000)
        val multiCore = ((s.coreCount * boostGhz * 280.0 + s.tdp.watts * 30.0) * archMultiplier).toInt().coerceAtLeast(4000)

        // Raw productivity index (rendering/compute)
        val rawProductivityIndex = (((singleCore * 0.4 + multiCore * 0.0025) / 12.0 * 10.0)).coerceIn(20.0, 115.0)

        // Calibrated Gaming Index: 3D V-Cache massive boost vs Arrow Lake tile memory latency penalty
        val gamingCacheMultiplier = when {
            isX3d -> 1.30
            isArrowLake -> 0.94
            else -> 1.0
        }
        val gamingRelativeIndex = (rawProductivityIndex * gamingCacheMultiplier).coerceIn(20.0, 135.0)
        val score = (if (isX3d) (rawProductivityIndex * 1.15) else rawProductivityIndex).toInt().coerceIn(10, 100)
        val tier = when {
            score >= 95 -> "S-Tier"
            score >= 85 -> "A-Tier"
            score >= 70 -> "B-Tier"
            else -> "C-Tier"
        }

        val gameFps = buildFull12GameMatrix(ratio = gamingRelativeIndex / 100.0, isCpu = true, isX3d = isX3d)

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
            gamingRelativeIndex = round1(gamingRelativeIndex) / 100.0,
            relativePerformanceIndex = round1(rawProductivityIndex),
            cpuSynthetic = cpuSynthetic,
            gameFps = gameFps,
            dataSource = DataSourceType.ESTIMATED_SIMULATION.name,
            testSuite = "Kalibre Edilmiş IPC & 3D V-Cache Simülasyonu",
        )
    }

    private fun simulateGpu(item: HardwareItem, s: GpuSpec): HardwareBenchmarkInfo {
        val vramGb = s.vram.megabytes / 1024.0
        val tdp = s.tdp.watts.toDouble()
        val clockMhz = s.boostClock?.mhz ?: s.coreClock.mhz
        val cores = (s.cudaCores ?: 0).toDouble()
        val busBits = (s.busWidthBits ?: 128).toDouble()

        val chipsetBase = when (normalizeGpuChipsetKey(s.chipset, item.model)) {
            "rtx_5090" -> 115.0
            "rtx_4090", "rtx_5080" -> 100.0
            "rx_7900_xtx", "rtx_5070_ti" -> 93.0
            "rtx_4080_super" -> 91.0
            "rtx_4080", "rx_7900_xt" -> 88.0
            "rtx_4070_ti_super", "rx_9070_xt", "rtx_5070" -> 83.0
            "rtx_4070_ti", "rx_7900_gre", "rx_9070" -> 79.0
            "rtx_4070_super" -> 76.0
            "rx_7800_xt", "rtx_4070" -> 70.0
            "rx_7700_xt", "rtx_4060_ti", "rtx_5060_ti" -> 61.0
            "rx_6800_xt", "rtx_3080" -> 68.0
            "rx_6700_xt", "rtx_3070" -> 56.0
            "rtx_4060", "rx_7600_xt", "arc_b580" -> 52.0
            "rx_7600", "rtx_3060" -> 45.0
            else -> null
        }

        val formulaIndex = (
            tdp * 0.14 +
                vramGb * 2.1 +
                (clockMhz / 1000.0) * 8.5 +
                (cores / 1000.0) * 1.8 +
                (busBits / 64.0) * 2.2
            ).coerceIn(18.0, 115.0)

        val relativeIndex = if (chipsetBase != null) {
            (chipsetBase * 0.85 + formulaIndex * 0.15).coerceIn(18.0, 115.0)
        } else {
            formulaIndex
        }
        val score = relativeIndex.toInt().coerceIn(15, 100)
        val timeSpyGfx = (relativeIndex * 310.0).toInt()
        val timeSpyExt = (timeSpyGfx * 0.49).toInt()
        val steelNomad = (timeSpyGfx * 0.238).toInt()
        val tier = when {
            score >= 90 -> "S-Tier"
            score >= 75 -> "A-Tier"
            score >= 55 -> "B-Tier"
            else -> "C-Tier"
        }

        val gpuRatio = relativeIndex / 100.0
        val gameFps = buildFull12GameMatrix(ratio = gpuRatio, isCpu = false, isX3d = false)

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
