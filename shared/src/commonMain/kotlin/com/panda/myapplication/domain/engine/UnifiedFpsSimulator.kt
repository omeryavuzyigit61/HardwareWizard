package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DataSourceType
import com.panda.myapplication.domain.model.GameCategory
import com.panda.myapplication.domain.model.GameFpsBenchmark
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.ResolutionFps
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Hardware Wizard — Unified Physical Frame-Time & ML-Calibrated FPS Simulation Engine (v6.0)
 *
 * Single Source of Truth powering:
 *  1. Product Detail (GPU)   -> simulate(REF_CPU_9800X3D, gpu)
 *  2. Product Detail (CPU)   -> simulate(cpu, REF_GPU_RTX_5090)
 *  3. Product Detail (Laptop)-> simulate(mobileCpu, mobileGpu @ TGP)
 *  4. PC Build Wizard        -> simulate(userCpu, userGpu, userRam, resolution, preset, upscaling)
 *  5. VS Arena               -> side-by-side comparison of identical calibrated outputs
 *
 * Physics:
 *  DX12/Vulkan pipelined frame-time L_p norm (p = 3.2):
 *      T_sys = (T_gpu^p + T_cpu^p)^(1/p)
 *      FPS_sys = (F_gpu^(-p) + F_cpu^(-p))^(-1/p)
 *
 * ML Fallback Weights:
 *  Trained via Leave-One-Out CV Log-Ridge Regression in scripts/calibrate_benchmarks_ml.py:
 *   - GPU Log-Ridge (63 TechPowerUp anchors): LOO-CV MAPE = 9.51%
 *   - CPU Gaming Log-Ridge (116 anchors):     LOO-CV MAPE = 2.67%
 *   - CPU Productivity Log-Ridge (116 anchors): LOO-CV MAPE = 1.41%
 */
object UnifiedFpsSimulator {

    private const val FRAME_TIME_P = 3.2

    // Trained Log-Ridge weights from scripts/calibrate_benchmarks_ml.py
    // GPU features: [1, ln(vramGb), ln(busBits), ln(tdpW), ln(archTier)]
    private val GPU_ML_W = doubleArrayOf(-0.4178, 0.26139, 0.11847, 0.53118, 1.06712)

    // CPU Gaming features: [1, ln(cb23Single), ln(effL3PerCcdMb), isX3d, isDualNonX3d, isArrowLake]
    private val CPU_GAMING_ML_W = doubleArrayOf(-1.96463, 0.75913, 0.14677, 0.03841, -0.03087, -0.07307)

    // CPU Productivity features: [1, ln(cb23Multi), ln(passmark)]
    private val CPU_PROD_ML_W = doubleArrayOf(-6.33826, 0.81809, 0.19905)

    // Reference Test Bench Partners (consistent with AppStrings.gameBenchReferenceRig*)
    val REF_CPU_9800X3D = CpuSimProfile(
        gamingIndex = 100.0,
        productivityIndex = 56.0,
        isX3d = true,
        ccdCount = 1,
        cores = 8,
        l3CacheMb = 96,
        effectiveL3PerCcdMb = 96,
        isArrowLake = false,
        topologySummaryTr = "Tek CCD • 8 Çekirdek Birleşik 96 MB 3D V-Cache",
        topologySummaryEn = "Single CCD • 8C Unified 96 MB 3D V-Cache",
        dataSource = DataSourceType.VERIFIED_BENCHMARK,
    )

    val REF_GPU_RTX_5090 = GpuSimProfile(
        relativeIndex = 133.0, // RTX 4090 = 100.0 (uncapped TechPowerUp baseline)
        vramGb = 32.0,
        dataSource = DataSourceType.VERIFIED_BENCHMARK,
    )

    data class CpuSimProfile(
        val gamingIndex: Double,          // Ryzen 7 9800X3D = 100.0
        val productivityIndex: Double,    // Ryzen 9 9950X = 100.0 (uncapped for Threadripper / 285K)
        val isX3d: Boolean,
        val ccdCount: Int,
        val cores: Int,
        val l3CacheMb: Int,
        val effectiveL3PerCcdMb: Int,
        val isArrowLake: Boolean,
        val topologySummaryTr: String,
        val topologySummaryEn: String,
        val dataSource: DataSourceType,
    ) {
        val isDualCcdNonX3d: Boolean get() = ccdCount >= 2 && !isX3d
        val isSingleCcdX3d: Boolean get() = isX3d && ccdCount <= 1
        val overallScore100: Int
            get() = (0.55 * gamingIndex + 0.45 * min(100.0, productivityIndex)).roundToInt().coerceIn(10, 100)
    }

    data class GpuSimProfile(
        val relativeIndex: Double,        // RTX 4090 = 100.0 (RTX 5090 = 133.0, RTX 5080 = 86.0)
        val vramGb: Double,
        val dataSource: DataSourceType,
    ) {
        val score100: Int get() = ((relativeIndex / 133.0) * 100.0).roundToInt().coerceIn(5, 100)
    }

    data class GamePhysicsProfile(
        val id: String,
        val title: String,
        val shortTitle: String,
        val category: GameCategory,
        val engineTag: String,
        val gpu1080: Double,
        val gpu1440: Double,
        val gpu4k: Double,
        val cpuBase: Double,
        val gpuExp: Double,
        val x3dAffinity: Double,
        val vram1080: Double,
        val vram1440: Double,
        val vram4k: Double,
        val baseLow1: Double,
    )

    val GAME_PROFILES: List<GamePhysicsProfile> = listOf(
        // 5 E-Sports Titles (Competitive settings — matches TechPowerUp/HUB lab benches)
        GamePhysicsProfile("counter_strike_2", "Counter-Strike 2 (CS2)", "CS2", GameCategory.ESPORTS, "Source 2", 1150.0, 760.0, 390.0, 920.0, 0.92, 0.14, 4.5, 5.5, 7.0, 0.72),
        GamePhysicsProfile("valorant", "Valorant", "Valorant", GameCategory.ESPORTS, "UE4 Competitive", 1180.0, 860.0, 480.0, 960.0, 0.86, 0.16, 3.0, 3.8, 5.0, 0.74),
        GamePhysicsProfile("apex_legends", "Apex Legends", "Apex", GameCategory.ESPORTS, "Source DX12", 460.0, 340.0, 200.0, 390.0, 0.94, 0.10, 5.5, 6.8, 8.5, 0.76),
        GamePhysicsProfile("warzone", "Call of Duty: Warzone", "Warzone", GameCategory.ESPORTS, "IW 9.0", 330.0, 245.0, 145.0, 295.0, 0.95, 0.14, 7.5, 9.5, 12.0, 0.73),
        GamePhysicsProfile("pubg", "PUBG: Battlegrounds", "PUBG", GameCategory.ESPORTS, "DX11 Enhanced", 410.0, 305.0, 175.0, 350.0, 0.94, 0.13, 6.0, 7.5, 9.5, 0.72),
        // 7 AAA Heavy Titles (Native Ultra / High Raster — TechPowerUp / HUB calibrated)
        GamePhysicsProfile("cyberpunk_2077", "Cyberpunk 2077", "Cyberpunk 2077", GameCategory.AAA, "REDengine 4", 245.0, 158.0, 82.0, 215.0, 1.00, 0.12, 7.2, 8.6, 12.2, 0.77),
        GamePhysicsProfile("alan_wake_2", "Alan Wake 2", "Alan Wake 2", GameCategory.AAA, "Northlight Mesh", 172.0, 114.0, 58.0, 195.0, 1.02, 0.05, 8.5, 10.8, 14.0, 0.78),
        GamePhysicsProfile("red_dead_redemption_2", "Red Dead Redemption 2", "RDR 2", GameCategory.AAA, "RAGE Vulkan", 240.0, 175.0, 105.0, 235.0, 0.98, 0.08, 6.5, 8.0, 10.5, 0.79),
        GamePhysicsProfile("black_myth_wukong", "Black Myth: Wukong", "Wukong", GameCategory.AAA, "Unreal Engine 5", 185.0, 124.0, 64.0, 205.0, 1.01, 0.06, 8.0, 10.2, 13.5, 0.75),
        GamePhysicsProfile("forza_horizon_5", "Forza Horizon 5", "Forza Horizon 5", GameCategory.AAA, "ForzaTech Extreme", 310.0, 235.0, 146.0, 285.0, 0.97, 0.09, 7.5, 9.2, 11.8, 0.81),
        GamePhysicsProfile("hogwarts_legacy", "Hogwarts Legacy", "Hogwarts Legacy", GameCategory.AAA, "UE4 Ultra", 225.0, 154.0, 84.0, 198.0, 0.99, 0.13, 8.8, 11.0, 14.5, 0.74),
        GamePhysicsProfile("baldurs_gate_3", "Baldur's Gate 3", "Baldur's Gate 3", GameCategory.AAA, "Divinity 4.0 Act 3", 265.0, 196.0, 118.0, 188.0, 0.96, 0.16, 7.0, 8.5, 11.0, 0.74),
    )

    // -------------------------------------------------------------------------
    // 1. CPU & GPU PROFILE RESOLUTION (DB Calibrated Anchor -> ML Log-Ridge Fallback)
    // -------------------------------------------------------------------------

    fun resolveCpuProfile(item: HardwareItem): CpuSimProfile {
        val spec = item.spec as? CpuSpec
        val model = item.model
        val modelLower = model.lowercase()
        val brandLower = item.brand.lowercase()
        val archLower = spec?.architecture?.lowercase().orEmpty()
        val socketLower = spec?.socket?.lowercase().orEmpty()

        val cores = (spec?.coreCount ?: 8).coerceAtLeast(2)
        val boostMhz = (spec?.boostClock?.mhz ?: spec?.baseClock?.mhz ?: 4400).coerceAtLeast(2000)
        val tdp = spec?.tdp?.watts ?: 65
        val isAmd = brandLower.contains("amd") || modelLower.contains("ryzen") || modelLower.contains("threadripper")
        val isThreadripper = modelLower.contains("threadripper") || (isAmd && cores >= 24)
        val isX3d = modelLower.contains("x3d") || ((spec?.l3CacheMb ?: 0) >= 96 && isAmd && !isThreadripper)
        val isArrowLake = archLower.contains("arrow lake") || socketLower.contains("1851") ||
            modelLower.contains("ultra 9 2") || modelLower.contains("ultra 7 2") || modelLower.contains("ultra 5 2")

        // Correct known corrupted L3 values dynamically even before a local DB refresh
        val rawL3 = spec?.l3CacheMb ?: if (isX3d) 96 else 32
        val l3Mb = when {
            modelLower.contains("9950x3d2") -> max(rawL3, 192)
            modelLower.contains("9950x3d") || modelLower.contains("9900x3d") ||
                modelLower.contains("7950x3d") || modelLower.contains("7900x3d") -> max(rawL3, 128)
            isX3d && rawL3 < 96 -> 96
            else -> rawL3
        }

        // Physical CCD count detection:
        // AMD consumer Ryzen 9 (12C/16C) = 2 CCDs (6+6 or 8+8); Ryzen 5/7 (6C/8C) = 1 CCD; Intel monolithic/tile = 1 gaming domain
        val ccdCount = when {
            isThreadripper -> max(2, cores / 8)
            isAmd && cores >= 12 -> 2
            else -> 1
        }
        val effL3PerCcd = when {
            isX3d && ccdCount == 1 -> l3Mb                  // 9800X3D / 7800X3D: unified 96 MB on 1 CCD
            isX3d && ccdCount >= 2 -> if (l3Mb >= 192) 96 else 96 // 9950X3D / 9900X3D: 96 MB on V-Cache CCD0 + 32 MB on CCD1
            ccdCount >= 2 -> (l3Mb / ccdCount).coerceAtLeast(16) // 9900X / 9950X: 64 MB split = 32 MB per CCD!
            else -> l3Mb
        }

        val perCcdCores = (cores / ccdCount).coerceAtLeast(1)
        val (topoTr, topoEn) = when {
            isX3d && ccdCount == 1 ->
                "Tek CCD • ${cores}Ç Birleşik $l3Mb MB 3D V-Cache" to
                    "Single CCD • ${cores}C Unified $l3Mb MB 3D V-Cache"
            isX3d && ccdCount >= 2 ->
                "Çift CCD (${perCcdCores}+${perCcdCores}) • 96 MB V-Cache + 32 MB L3" to
                    "Dual CCD (${perCcdCores}+${perCcdCores}) • 96 MB V-Cache + 32 MB L3"
            isThreadripper ->
                "$ccdCount×CCD Workstation • $l3Mb MB L3 (Yüksek Çekirdek)" to
                    "$ccdCount×CCD Workstation • $l3Mb MB L3 (HEDT)"
            ccdCount >= 2 ->
                "Çift CCD (${perCcdCores}+${perCcdCores}) • Bölünmüş ${effL3PerCcd}+${effL3PerCcd} MB L3" to
                    "Dual CCD (${perCcdCores}+${perCcdCores}) • Split ${effL3PerCcd}+${effL3PerCcd} MB L3"
            isArrowLake ->
                "Tile Mimarisi (Arrow Lake) • $l3Mb MB SmartCache" to
                    "Tile Architecture (Arrow Lake) • $l3Mb MB SmartCache"
            !isAmd && cores >= 10 ->
                "Hibrit P+E Çekirdek • $l3Mb MB SmartCache" to
                    "Hybrid P+E Cores • $l3Mb MB SmartCache"
            else ->
                "Tek Blok / Tek CCD • $l3Mb MB L3" to
                    "Single Die / CCD • $l3Mb MB L3"
        }

        val bench = item.benchmarkInfo
        val syn = bench?.cpuSynthetic
        val isCalibratedV6 = bench?.testSuite?.contains("v6", ignoreCase = true) == true

        // Productivity Index (Ryzen 9 9950X = 100.0, CB23 Multi = 43,200)
        val cbMulti = syn?.cinebenchR23Multi?.takeIf { it > 0 }
        val passmark = syn?.passmarkScore?.takeIf { it > 0 }
        val prodIdx = when {
            bench?.productivityIndex != null && bench.productivityIndex > 0 -> bench.productivityIndex.toDouble()
            cbMulti != null && passmark != null -> exp(
                CPU_PROD_ML_W[0] +
                    CPU_PROD_ML_W[1] * ln(cbMulti.toDouble()) +
                    CPU_PROD_ML_W[2] * ln(passmark.toDouble()),
            )
            cbMulti != null -> (cbMulti / 432.0)
            passmark != null -> (passmark / 655.0)
            else -> {
                val ipc = inferCpuArchIpc(archLower, socketLower, modelLower)
                val estCbM = cores * (boostMhz / 1000.0) * (ipc / 2.2) * 0.64
                (estCbM / 432.0)
            }
        }.coerceIn(10.0, 280.0)

        // Gaming Index (Ryzen 7 9800X3D = 100.0)
        val gamingIdx: Double
        val source: DataSourceType
        if (isCalibratedV6 && bench.gamingIndex != null && bench.gamingIndex > 0) {
            gamingIdx = bench.gamingIndex.toDouble().coerceIn(15.0, 108.0)
            source = DataSourceType.VERIFIED_BENCHMARK
        } else if (bench != null && bench.isVerified && (bench.gamingIndex != null || bench.gamingRelativeIndex != null || bench.score > 0)) {
            // Respect explicit caller/legacy verified index while preserving 3D V-Cache vs Dual-CCD physics
            val rawBase = bench.gamingIndex?.toDouble()
                ?: bench.gamingRelativeIndex?.times(100.0)
                ?: bench.relativePerformanceIndex.takeIf { it > 0.0 }
                ?: bench.score.toDouble()
            val archTilt = when {
                isX3d && ccdCount <= 1 -> 1.06
                isX3d -> 1.03
                ccdCount >= 2 -> 0.91
                isArrowLake -> 0.94
                else -> 1.0
            }
            gamingIdx = (rawBase * archTilt).coerceIn(15.0, 105.0)
            source = DataSourceType.VERIFIED_BENCHMARK
        } else {
            // ML Log-Ridge prediction using verified synthetic single-core or architectural IPC proxy
            val ipc = inferCpuArchIpc(archLower, socketLower, modelLower)
            val cbSingle = syn?.cinebenchR23Single?.takeIf { it > 0 }?.toDouble()
                ?: (ipc * (boostMhz / 5000.0).pow(0.55))
            val dualNonX3d = if (ccdCount >= 2 && !isX3d) 1.0 else 0.0
            val arrowFlag = if (isArrowLake) 1.0 else 0.0
            var pred = exp(
                CPU_GAMING_ML_W[0] +
                    CPU_GAMING_ML_W[1] * ln(max(cbSingle, 800.0)) +
                    CPU_GAMING_ML_W[2] * ln(max(effL3PerCcd.toDouble(), 8.0)) +
                    CPU_GAMING_ML_W[3] * (if (isX3d) 1.0 else 0.0) +
                    CPU_GAMING_ML_W[4] * dualNonX3d +
                    CPU_GAMING_ML_W[5] * arrowFlag,
            )
            if (isThreadripper) pred *= 0.86
            if (tdp <= 40) pred *= 0.88
            else if (cores <= 4) pred *= 0.86
            gamingIdx = pred.coerceIn(18.0, 105.0)
            source = if (bench?.isVerified == true && syn?.cinebenchR23Single != null) {
                DataSourceType.VERIFIED_BENCHMARK
            } else {
                DataSourceType.ESTIMATED_SIMULATION
            }
        }

        return CpuSimProfile(
            gamingIndex = gamingIdx,
            productivityIndex = prodIdx,
            isX3d = isX3d,
            ccdCount = ccdCount,
            cores = cores,
            l3CacheMb = l3Mb,
            effectiveL3PerCcdMb = effL3PerCcd,
            isArrowLake = isArrowLake,
            topologySummaryTr = topoTr,
            topologySummaryEn = topoEn,
            dataSource = source,
        )
    }

    fun resolveGpuProfile(item: HardwareItem): GpuSimProfile {
        val spec = item.spec as? GpuSpec
        val vramGb = ((spec?.vram?.megabytes ?: 8192) / 1024.0).coerceAtLeast(2.0)
        val bench = item.benchmarkInfo
        val isCalibratedV6 = bench?.testSuite?.contains("v6", ignoreCase = true) == true

        if (isCalibratedV6 && bench.gamingIndex != null && bench.gamingIndex > 0) {
            return GpuSimProfile(
                relativeIndex = bench.gamingIndex.toDouble().coerceIn(6.0, 160.0),
                vramGb = vramGb,
                dataSource = DataSourceType.VERIFIED_BENCHMARK,
            )
        }

        // Respect explicit verified GPU index if caller supplied relativePerformancePct / score
        if (bench != null && bench.isVerified && (bench.relativePerformancePct != null || bench.gamingIndex != null || bench.score > 0)) {
            val explicitRel = bench.gamingIndex?.toDouble()
                ?: bench.relativePerformancePct?.times(100.0)
                ?: bench.relativePerformanceIndex.takeIf { it > 0.0 }
                ?: bench.score.toDouble()
            return GpuSimProfile(
                relativeIndex = explicitRel.coerceIn(6.0, 160.0),
                vramGb = vramGb,
                dataSource = DataSourceType.VERIFIED_BENCHMARK,
            )
        }

        // Fallback: Time Spy Graphics anchor (strong linear correlation R²=0.985) or trained Log-Ridge ML
        val ts = bench?.gpuSynthetic?.timeSpyGraphics?.takeIf { it > 1500 }
        if (ts != null) {
            val rel = (ts / 368.0).coerceIn(8.0, 150.0)
            return GpuSimProfile(
                relativeIndex = rel,
                vramGb = vramGb,
                dataSource = DataSourceType.VERIFIED_BENCHMARK,
            )
        }

        val bus = (spec?.busWidthBits ?: 128).coerceAtLeast(64).toDouble()
        val tdp = (spec?.tdp?.watts ?: 150).coerceAtLeast(40).toDouble()
        val archLower = "${spec?.architecture.orEmpty()} ${spec?.chipset.orEmpty()} ${item.model}".lowercase()
        val archTier = when {
            archLower.contains("5090") || archLower.contains("5080") || archLower.contains("5070") ||
                archLower.contains("5060") || archLower.contains("blackwell") -> 1.28
            archLower.contains("4090") || archLower.contains("4080") || archLower.contains("4070") ||
                archLower.contains("4060") || archLower.contains("ada") ||
                archLower.contains("9070") || archLower.contains("9060") || archLower.contains("rdna 4") -> 1.24
            archLower.contains("7900") || archLower.contains("7800") || archLower.contains("7700") ||
                archLower.contains("7600") || archLower.contains("rdna 3") || archLower.contains("b580") ||
                archLower.contains("b570") || archLower.contains("battlemage") -> 1.10
            else -> 1.00
        }

        val pred = exp(
            GPU_ML_W[0] +
                GPU_ML_W[1] * ln(vramGb) +
                GPU_ML_W[2] * ln(bus) +
                GPU_ML_W[3] * ln(tdp) +
                GPU_ML_W[4] * ln(archTier),
        ).coerceIn(8.0, 150.0)

        return GpuSimProfile(
            relativeIndex = pred,
            vramGb = vramGb,
            dataSource = DataSourceType.ESTIMATED_SIMULATION,
        )
    }

    fun resolveLaptopProfiles(spec: LaptopSpec): Pair<CpuSimProfile, GpuSimProfile> {
        val cpuLow = spec.cpuModel.lowercase()
        val gpuLow = spec.gpuModel.lowercase()
        val tgp = Regex("""(\d{2,3})\s*w""", RegexOption.IGNORE_CASE)
            .find(spec.gpuWattTgp)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 115

        // Mobile CPU profile (strictly NO false X3D on Intel HX!)
        val isX3d = cpuLow.contains("x3d")
        val ccdCount = if (cpuLow.contains("7945") || cpuLow.contains("9955") || cpuLow.contains("7845")) 2 else 1
        val cpuGaming = when {
            cpuLow.contains("9955hx3d") -> 94.0
            cpuLow.contains("7945hx3d") -> 88.0
            cpuLow.contains("275hx") || cpuLow.contains("285hx") || cpuLow.contains("14900hx") -> 78.0
            cpuLow.contains("13980hx") || cpuLow.contains("13950hx") || cpuLow.contains("13900hx") ||
                cpuLow.contains("9955hx") || cpuLow.contains("7945hx") -> 75.0
            cpuLow.contains("14700hx") || cpuLow.contains("13700hx") || cpuLow.contains("ultra 9") -> 72.0
            cpuLow.contains("13650hx") || cpuLow.contains("12900h") || cpuLow.contains("ai 9") ||
                cpuLow.contains("8945") || cpuLow.contains("7840") -> 67.0
            cpuLow.contains("13700h") || cpuLow.contains("13620h") || cpuLow.contains("7735") ||
                cpuLow.contains("ultra 7") -> 63.0
            cpuLow.contains("12700h") || cpuLow.contains("12650h") || cpuLow.contains("7640") -> 60.0
            cpuLow.contains("12450h") || cpuLow.contains("6800h") || cpuLow.contains("5800h") -> 54.0
            else -> (spec.gamingScore.coerceIn(35, 95) * 0.72)
        }
        val cpuProfile = CpuSimProfile(
            gamingIndex = cpuGaming,
            productivityIndex = (cpuGaming * 0.85).coerceIn(25.0, 95.0),
            isX3d = isX3d,
            ccdCount = ccdCount,
            cores = Regex("""(\d+)""").find(spec.cpuCores)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 8,
            l3CacheMb = if (isX3d) 128 else 24,
            effectiveL3PerCcdMb = if (isX3d) 96 else 24,
            isArrowLake = cpuLow.contains("ultra") && cpuLow.contains("2"),
            topologySummaryTr = "Mobil İşlemci (${spec.cpuModel})",
            topologySummaryEn = "Mobile CPU (${spec.cpuModel})",
            dataSource = DataSourceType.ESTIMATED_SIMULATION,
        )

        // Mobile GPU relative index (RTX 4090 Desktop = 100.0), scaled by TGP wattage
        val (baseIdx, baseVram, refTgp) = when {
            gpuLow.contains("5090") -> Triple(74.0, 24.0, 175)
            gpuLow.contains("5080") -> Triple(61.0, 16.0, 175)
            gpuLow.contains("4090") -> Triple(58.0, 16.0, 175)
            gpuLow.contains("5070 ti") -> Triple(50.0, 12.0, 140)
            gpuLow.contains("4080") -> Triple(49.0, 12.0, 175)
            gpuLow.contains("5070") -> Triple(40.0, 8.0, 115)
            gpuLow.contains("4070") -> Triple(34.0, 8.0, 115)
            gpuLow.contains("5060") -> Triple(33.0, 8.0, 115)
            gpuLow.contains("4060") -> Triple(29.0, 8.0, 115)
            gpuLow.contains("3080 ti") -> Triple(37.0, 16.0, 175)
            gpuLow.contains("3080") -> Triple(33.0, 8.0, 150)
            gpuLow.contains("3070 ti") -> Triple(31.0, 8.0, 150)
            gpuLow.contains("3070") -> Triple(28.0, 8.0, 140)
            gpuLow.contains("4050") -> Triple(22.0, 6.0, 105)
            gpuLow.contains("3060") -> Triple(23.0, 6.0, 130)
            gpuLow.contains("3050") -> Triple(15.0, 4.0, 75)
            gpuLow.contains("7700s") -> Triple(27.0, 8.0, 100)
            gpuLow.contains("7600s") -> Triple(23.0, 8.0, 95)
            else -> Triple((spec.gamingScore.coerceIn(20, 95) * 0.45), 8.0, 115)
        }
        val tgpFactor = (tgp.toDouble() / refTgp.toDouble()).pow(0.32).coerceIn(0.72, 1.06)
        val gpuProfile = GpuSimProfile(
            relativeIndex = (baseIdx * tgpFactor).coerceIn(8.0, 90.0),
            vramGb = baseVram,
            dataSource = DataSourceType.ESTIMATED_SIMULATION,
        )
        return cpuProfile to gpuProfile
    }

    // -------------------------------------------------------------------------
    // 2. CORE PHYSICAL FRAME-TIME PIPELINE (Identical to Python calibration)
    // -------------------------------------------------------------------------

    data class SystemFpsSample(
        val avgFps: Int,
        val low1PctFps: Int,
        val cpuShare: Double, // 0.0 (100% GPU-bound) .. 1.0 (100% CPU-bound)
        val vramDeficitGb: Double,
    )

    fun simulateSingleGameAtResolution(
        game: GamePhysicsProfile,
        cpu: CpuSimProfile,
        gpu: GpuSimProfile,
        resolution: GamingResolution,
        preset: QualityPreset = QualityPreset.HIGH,
        upscaling: UpscalingMode = UpscalingMode.OFF,
        ramFactor: Double = 1.0,
    ): SystemFpsSample {
        val gpuRatio = max(gpu.relativeIndex / 100.0, 0.08)
        val cpuRatio = max(cpu.gamingIndex / 100.0, 0.18) * ramFactor

        // Per-game 3D V-Cache sensitivity centered at mean affinity 0.11
        val affinityDelta = game.x3dAffinity - 0.11
        val cpuGameMult = (if (cpu.isX3d) 1.0 + affinityDelta * 0.65 else 1.0 - affinityDelta * 0.65)
            .coerceIn(0.88, 1.12)

        // Architecture-aware 1% Low stability modifier
        var cpuLow1Mod = 0.0
        when {
            cpu.isSingleCcdX3d -> cpuLow1Mod += 0.07
            cpu.isX3d && cpu.ccdCount >= 2 -> cpuLow1Mod += 0.04
            cpu.isDualCcdNonX3d -> cpuLow1Mod -= 0.04
        }
        if (cpu.cores <= 4) cpuLow1Mod -= 0.06
        else if (cpu.cores <= 6 && !cpu.isX3d) cpuLow1Mod -= 0.02

        val (gpuBase, vramBase, cpuResDrag) = when (resolution) {
            GamingResolution.P1080 -> Triple(game.gpu1080, game.vram1080, 1.00)
            GamingResolution.P1440 -> Triple(game.gpu1440, game.vram1440, 0.97)
            GamingResolution.P4K -> Triple(game.gpu4k, game.vram4k, 0.94)
        }

        // Physically separate preset & upscaling effects onto GPU vs CPU!
        val presetGpuMult = when (preset) {
            QualityPreset.MEDIUM -> 1.28
            QualityPreset.HIGH -> 1.00
            QualityPreset.ULTRA -> 0.84
            QualityPreset.RAY_TRACING -> 0.54
        }
        val presetCpuMult = when (preset) {
            QualityPreset.MEDIUM -> 1.05
            QualityPreset.HIGH -> 1.00
            QualityPreset.ULTRA -> 0.96
            QualityPreset.RAY_TRACING -> 0.86 // RT BVH traversal adds real CPU overhead
        }
        val presetVramMult = when (preset) {
            QualityPreset.MEDIUM -> 0.82
            QualityPreset.HIGH -> 1.00
            QualityPreset.ULTRA -> 1.14
            QualityPreset.RAY_TRACING -> 1.32
        }

        // Spatial/temporal upscaling reduces GPU render pixels (does NOT remove CPU limit!)
        val upscalingGpuMult = when (upscaling) {
            UpscalingMode.OFF -> 1.00
            UpscalingMode.QUALITY -> 1.35
            UpscalingMode.BALANCED -> 1.54
            UpscalingMode.PERFORMANCE -> 1.78
            UpscalingMode.FRAME_GEN -> 1.00 // Applied post-pipeline to presented frames
        }
        val upscalingVramMult = when (upscaling) {
            UpscalingMode.OFF -> 1.00
            UpscalingMode.QUALITY -> 0.90
            UpscalingMode.BALANCED -> 0.84
            UpscalingMode.PERFORMANCE -> 0.78
            UpscalingMode.FRAME_GEN -> 1.14 // Optical flow / frame buffers cost extra VRAM
        }

        val effectiveVramDemand = vramBase * presetVramMult * upscalingVramMult
        val vramDeficit = max(0.0, effectiveVramDemand - max(gpu.vramGb, 4.0))
        val vramAvgPen = 1.0 / (1.0 + 0.060 * vramDeficit.pow(1.30))
        val vramLow1Pen = min(0.24, 0.042 * vramDeficit.pow(1.25))

        val fGpu = max(8.0, gpuBase * gpuRatio.pow(game.gpuExp) * presetGpuMult * upscalingGpuMult * vramAvgPen)
        val fCpu = max(18.0, game.cpuBase * cpuRatio * cpuGameMult * cpuResDrag * presetCpuMult)

        // Pipelined DX12/Vulkan L_p frame-time norm
        val gTerm = fGpu.pow(-FRAME_TIME_P)
        val cTerm = fCpu.pow(-FRAME_TIME_P)
        val fSysNative = (gTerm + cTerm).pow(-1.0 / FRAME_TIME_P)
        val cpuShare = cTerm / (gTerm + cTerm)

        val fgMult = if (upscaling == UpscalingMode.FRAME_GEN) 1.58 else 1.0
        val avgFps = (fSysNative * fgMult).roundToInt().coerceAtLeast(10)

        val effLow1Ratio = (game.baseLow1 + cpuLow1Mod * (0.45 + 0.55 * cpuShare) - vramLow1Pen)
            .coerceIn(0.45, 0.89)
        // Frame Gen increases displayed avg more than real input-responsive 1% low
        val fgLow1Mult = if (upscaling == UpscalingMode.FRAME_GEN) 1.32 else 1.0
        val low1Fps = (fSysNative * effLow1Ratio * fgLow1Mult).roundToInt().coerceIn(6, (avgFps - 2).coerceAtLeast(6))

        return SystemFpsSample(
            avgFps = avgFps,
            low1PctFps = low1Fps,
            cpuShare = cpuShare,
            vramDeficitGb = vramDeficit,
        )
    }

    fun simulateFull12GameMatrix(
        cpu: CpuSimProfile,
        gpu: GpuSimProfile,
        ramFactor: Double = 1.0,
    ): GameFpsBenchmark {
        fun buildRes(game: GamePhysicsProfile): ResolutionFps {
            val s1080 = simulateSingleGameAtResolution(game, cpu, gpu, GamingResolution.P1080, ramFactor = ramFactor)
            val s1440 = simulateSingleGameAtResolution(game, cpu, gpu, GamingResolution.P1440, ramFactor = ramFactor)
            val s4k = simulateSingleGameAtResolution(game, cpu, gpu, GamingResolution.P4K, ramFactor = ramFactor)
            return ResolutionFps(
                p1080 = s1080.avgFps.toDouble(),
                p1440 = s1440.avgFps.toDouble(),
                p4k = s4k.avgFps.toDouble(),
                low1Pct1080 = s1080.low1PctFps.toDouble(),
                low1Pct1440 = s1440.low1PctFps.toDouble(),
                low1Pct4k = s4k.low1PctFps.toDouble(),
            )
        }
        val byId = GAME_PROFILES.associate { it.id to buildRes(it) }
        return GameFpsBenchmark(
            counterStrike2 = byId["counter_strike_2"],
            valorant = byId["valorant"],
            apexLegends = byId["apex_legends"],
            warzone = byId["warzone"],
            pubg = byId["pubg"],
            cyberpunk2077 = byId["cyberpunk_2077"],
            alanWake2 = byId["alan_wake_2"],
            redDeadRedemption2 = byId["red_dead_redemption_2"],
            blackMythWukong = byId["black_myth_wukong"],
            forzaHorizon5 = byId["forza_horizon_5"],
            hogwartsLegacy = byId["hogwarts_legacy"],
            baldursGate3 = byId["baldurs_gate_3"],
        )
    }

    // -------------------------------------------------------------------------
    // 3. SCREEN-SPECIFIC ENTRY POINTS (All Guaranteed Consistent!)
    // -------------------------------------------------------------------------

    fun simulateForGpuDetail(gpuItem: HardwareItem): GameFpsBenchmark =
        simulateFull12GameMatrix(cpu = REF_CPU_9800X3D, gpu = resolveGpuProfile(gpuItem))

    fun simulateForCpuDetail(cpuItem: HardwareItem): GameFpsBenchmark =
        simulateFull12GameMatrix(cpu = resolveCpuProfile(cpuItem), gpu = REF_GPU_RTX_5090)

    fun simulateForLaptopDetail(laptopSpec: LaptopSpec): GameFpsBenchmark {
        val (cpu, gpu) = resolveLaptopProfiles(laptopSpec)
        return simulateFull12GameMatrix(cpu = cpu, gpu = gpu)
    }

    fun computeRamFactor(ramSpec: RamSpec?): Double {
        if (ramSpec == null) return 1.0
        var factor = 1.0
        // Single-channel RAM causes measurable CPU gaming memory bandwidth bottleneck
        if (ramSpec.moduleCount == 1) factor *= 0.91
        // DDR5 6000+ low-latency modest uplift vs slow DDR4-2666
        val speed = ramSpec.speed.mhz
        val isDdr5 = ramSpec.generation.name.equals("DDR5", ignoreCase = true)
        factor *= when {
            isDdr5 && speed >= 6000 -> 1.02
            isDdr5 -> 1.00
            speed >= 3600 -> 1.00
            speed >= 3200 -> 0.98
            else -> 0.94
        }
        return factor.coerceIn(0.84, 1.04)
    }

    fun tierForGpuRelativeIndex(relIdx: Double): String = when {
        relIdx >= 95.0 -> "S+-Tier"
        relIdx >= 72.0 -> "S-Tier"
        relIdx >= 54.0 -> "A+-Tier"
        relIdx >= 38.0 -> "A-Tier"
        relIdx >= 26.0 -> "B-Tier"
        else -> "C-Tier"
    }

    fun tierForCpu(gamingIdx: Double, prodIdx: Double): String {
        val peak = max(gamingIdx, min(prodIdx, 105.0))
        return when {
            peak >= 92.0 -> "S+-Tier"
            peak >= 80.0 -> "S-Tier"
            peak >= 70.0 -> "A+-Tier"
            peak >= 58.0 -> "A-Tier"
            peak >= 44.0 -> "B-Tier"
            else -> "C-Tier"
        }
    }

    private fun inferCpuArchIpc(archLower: String, socketLower: String, modelLower: String): Double = when {
        archLower.contains("zen 5") || archLower.contains("granite ridge") || archLower.contains("shimada") ||
            modelLower.contains("9950") || modelLower.contains("9900") || modelLower.contains("9800") ||
            modelLower.contains("9700") || modelLower.contains("9600") -> 2220.0
        archLower.contains("arrow lake") || socketLower.contains("1851") ||
            modelLower.contains("285k") || modelLower.contains("265k") || modelLower.contains("245k") -> 2200.0
        archLower.contains("raptor lake") ||
            modelLower.contains("14900") || modelLower.contains("14700") || modelLower.contains("14600") ||
            modelLower.contains("13900") || modelLower.contains("13700") || modelLower.contains("13600") -> 2020.0
        archLower.contains("zen 4") || archLower.contains("raphael") || archLower.contains("storm peak") ||
            archLower.contains("phoenix") || archLower.contains("hawk point") ||
            modelLower.contains("7950") || modelLower.contains("7900") || modelLower.contains("7800") ||
            modelLower.contains("7700") || modelLower.contains("7600") || socketLower.contains("am5") -> 1960.0
        archLower.contains("alder lake") ||
            modelLower.contains("12900") || modelLower.contains("12700") || modelLower.contains("12600") ||
            modelLower.contains("12400") || socketLower.contains("1700") -> 1800.0
        archLower.contains("zen 3") || archLower.contains("vermeer") || archLower.contains("cezanne") ||
            archLower.contains("chagall") || modelLower.contains("5800") || modelLower.contains("5700") ||
            modelLower.contains("5600") || modelLower.contains("5900") || modelLower.contains("5950") -> 1520.0
        archLower.contains("rocket lake") || modelLower.contains("11900") || modelLower.contains("11700") ||
            modelLower.contains("11600") || modelLower.contains("11400") -> 1540.0
        archLower.contains("comet lake") || modelLower.contains("10900") || modelLower.contains("10700") ||
            modelLower.contains("10600") || modelLower.contains("10400") -> 1320.0
        archLower.contains("zen 2") || archLower.contains("matisse") || archLower.contains("renoir") ||
            archLower.contains("castle peak") -> 1260.0
        archLower.contains("cascade lake") -> 1220.0
        archLower.contains("zen+") -> 1040.0
        archLower.contains("zen") -> 940.0
        else -> 1350.0
    }
}
