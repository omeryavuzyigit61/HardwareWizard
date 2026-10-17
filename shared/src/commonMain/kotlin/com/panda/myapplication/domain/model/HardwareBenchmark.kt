package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

enum class DataSourceType {
    VERIFIED_BENCHMARK,
    ESTIMATED_SIMULATION,
}

enum class GameCategory(val displayName: String) {
    ESPORTS("Rekabetçi / E-Spor"),
    AAA("AAA / Ağır Grafik"),
}

enum class GamingResolution(val label: String, val subtitle: String) {
    P1080("1080p", "FHD • Ultra"),
    P1440("1440p", "2K QHD • Ultra"),
    P4K("4K", "UHD • Ultra");

    val subLabel: String get() = subtitle
}

@Immutable
@Serializable
data class CpuSyntheticBenchmark(
    @SerialName("cinebench_2024_single")
    val cinebench2024Single: Int? = null,

    @SerialName("cinebench_2024_multi")
    val cinebench2024Multi: Int? = null,

    @SerialName("cinebench_r23_single")
    val cinebenchR23Single: Int? = null,

    @SerialName("cinebench_r23_multi")
    val cinebenchR23Multi: Int? = null,

    @SerialName("geekbench_6_single")
    val geekbench6Single: Int? = null,

    @SerialName("geekbench_6_multi")
    val geekbench6Multi: Int? = null,

    @SerialName("passmark_cpu_mark")
    val passmarkScore: Int? = null,
)

@Immutable
@Serializable
data class GpuSyntheticBenchmark(
    @SerialName("time_spy_graphics")
    val timeSpyGraphics: Int? = null,

    @SerialName("time_spy_extreme")
    val timeSpyExtreme: Int? = null,

    @SerialName("steel_nomad")
    val steelNomad: Int? = null,
)

@Immutable
@Serializable
data class ResolutionFps(
    val p1080: Double,
    val p1440: Double,
    val p4k: Double,
    @SerialName("low1_1080p")
    val low1Pct1080: Double? = null,
    @SerialName("low1_1440p")
    val low1Pct1440: Double? = null,
    @SerialName("low1_4k")
    val low1Pct4k: Double? = null,
) {
    fun avgFps(resolution: GamingResolution): Int = when (resolution) {
        GamingResolution.P1080 -> p1080.roundToInt().coerceAtLeast(1)
        GamingResolution.P1440 -> p1440.roundToInt().coerceAtLeast(1)
        GamingResolution.P4K -> p4k.roundToInt().coerceAtLeast(1)
    }

    fun low1PctFps(resolution: GamingResolution, isHighCache: Boolean = false): Int {
        val ratio = if (isHighCache) 0.84 else 0.78
        return when (resolution) {
            GamingResolution.P1080 -> (low1Pct1080 ?: (p1080 * ratio)).roundToInt().coerceAtLeast(1)
            GamingResolution.P1440 -> (low1Pct1440 ?: (p1440 * (ratio + 0.01))).roundToInt().coerceAtLeast(1)
            GamingResolution.P4K -> (low1Pct4k ?: (p4k * (ratio + 0.02))).roundToInt().coerceAtLeast(1)
        }
    }
}

@Immutable
data class GameSimulationEntry(
    val id: String,
    val title: String,
    val shortTitle: String,
    val category: GameCategory,
    val engineTag: String,
    val fps: ResolutionFps,
) {
    val presetSubtitle: String get() = engineTag
    val isEsports: Boolean get() = category == GameCategory.ESPORTS
    val avg1080p: Int get() = fps.avgFps(GamingResolution.P1080)
    val avg1440p: Int get() = fps.avgFps(GamingResolution.P1440)
    val avg4k: Int get() = fps.avgFps(GamingResolution.P4K)
    val low1Fps1080p: Int get() = fps.low1PctFps(GamingResolution.P1080)
    val low1Fps1440p: Int get() = fps.low1PctFps(GamingResolution.P1440)
    val low1Fps4k: Int get() = fps.low1PctFps(GamingResolution.P4K)
}

@Immutable
@Serializable
data class GameFpsBenchmark(
    // Rekabetçi / E-Spor (5 Oyun)
    @SerialName("counter_strike_2")
    val counterStrike2: ResolutionFps? = null,

    @SerialName("valorant")
    val valorant: ResolutionFps? = null,

    @SerialName("apex_legends")
    val apexLegends: ResolutionFps? = null,

    @SerialName("warzone")
    val warzone: ResolutionFps? = null,

    @SerialName("pubg")
    val pubg: ResolutionFps? = null,

    // AAA / Ağır Grafik (7 Oyun)
    @SerialName("cyberpunk_2077")
    val cyberpunk2077: ResolutionFps? = null,

    @SerialName("alan_wake_2")
    val alanWake2: ResolutionFps? = null,

    @SerialName("red_dead_redemption_2")
    val redDeadRedemption2: ResolutionFps? = null,

    @SerialName("black_myth_wukong")
    val blackMythWukong: ResolutionFps? = null,

    @SerialName("forza_horizon_5")
    val forzaHorizon5: ResolutionFps? = null,

    @SerialName("hogwarts_legacy")
    val hogwartsLegacy: ResolutionFps? = null,

    @SerialName("baldurs_gate_3")
    val baldursGate3: ResolutionFps? = null,
) {
    fun toSimulationEntries(): List<GameSimulationEntry> = buildList {
        counterStrike2?.let {
            add(GameSimulationEntry("counter_strike_2", "Counter-Strike 2 (CS2)", "CS2", GameCategory.ESPORTS, "Source 2", it))
        }
        valorant?.let {
            add(GameSimulationEntry("valorant", "Valorant", "Valorant", GameCategory.ESPORTS, "UE4 Competitive", it))
        }
        apexLegends?.let {
            add(GameSimulationEntry("apex_legends", "Apex Legends", "Apex", GameCategory.ESPORTS, "Source DX12", it))
        }
        warzone?.let {
            add(GameSimulationEntry("warzone", "Call of Duty: Warzone", "Warzone", GameCategory.ESPORTS, "IW 9.0", it))
        }
        pubg?.let {
            add(GameSimulationEntry("pubg", "PUBG: Battlegrounds", "PUBG", GameCategory.ESPORTS, "DX11 Enhanced", it))
        }
        cyberpunk2077?.let {
            add(GameSimulationEntry("cyberpunk_2077", "Cyberpunk 2077", "Cyberpunk 2077", GameCategory.AAA, "REDengine 4 RT", it))
        }
        alanWake2?.let {
            add(GameSimulationEntry("alan_wake_2", "Alan Wake 2", "Alan Wake 2", GameCategory.AAA, "Northlight Mesh", it))
        }
        redDeadRedemption2?.let {
            add(GameSimulationEntry("red_dead_redemption_2", "Red Dead Redemption 2", "RDR 2", GameCategory.AAA, "RAGE Vulkan", it))
        }
        blackMythWukong?.let {
            add(GameSimulationEntry("black_myth_wukong", "Black Myth: Wukong", "Wukong", GameCategory.AAA, "Unreal Engine 5", it))
        }
        forzaHorizon5?.let {
            add(GameSimulationEntry("forza_horizon_5", "Forza Horizon 5", "Forza Horizon 5", GameCategory.AAA, "ForzaTech Extreme", it))
        }
        hogwartsLegacy?.let {
            add(GameSimulationEntry("hogwarts_legacy", "Hogwarts Legacy", "Hogwarts Legacy", GameCategory.AAA, "UE4 Ultra", it))
        }
        baldursGate3?.let {
            add(GameSimulationEntry("baldurs_gate_3", "Baldur's Gate 3", "Baldur's Gate 3", GameCategory.AAA, "Divinity 4.0 Act 3", it))
        }
    }
}

@Immutable
@Serializable
data class HardwareBenchmarkInfo(
    val score: Int = 0,
    val tier: String = "A-Tier",
    val gamingIndex: Int? = null,
    val productivityIndex: Int? = null,
    @SerialName("relative_performance_pct")
    val relativePerformancePct: Double? = null,
    @SerialName("gaming_relative_index")
    val gamingRelativeIndex: Double? = null,
    val relativePerformanceIndex: Double = (relativePerformancePct?.let { it * 100.0 }
        ?: gamingRelativeIndex?.let { it * 100.0 }
        ?: gamingIndex?.toDouble()
        ?: 50.0),
    val cpuSynthetic: CpuSyntheticBenchmark? = null,
    val gpuSynthetic: GpuSyntheticBenchmark? = null,
    val gameFps: GameFpsBenchmark? = null,
    val dataSource: String = DataSourceType.VERIFIED_BENCHMARK.name,
    val testSuite: String = "Donanım Benchmark Laboratuvarı",
) {
    val isVerified: Boolean
        get() = dataSource == DataSourceType.VERIFIED_BENCHMARK.name

    val effectiveSingleCore: Int?
        get() = cpuSynthetic?.cinebenchR23Single ?: cpuSynthetic?.cinebench2024Single ?: cpuSynthetic?.geekbench6Single

    val effectiveMultiCore: Int?
        get() = cpuSynthetic?.cinebenchR23Multi ?: cpuSynthetic?.cinebench2024Multi ?: cpuSynthetic?.geekbench6Multi ?: cpuSynthetic?.passmarkScore

    val effectiveTimeSpy: Int?
        get() = gpuSynthetic?.timeSpyGraphics ?: gpuSynthetic?.timeSpyExtreme

    val effectiveGamingIndex: Int
        get() = (gamingRelativeIndex?.let { (it * 100.0).toInt() }
            ?: relativePerformancePct?.let { (it * 100.0).toInt() }
            ?: relativePerformanceIndex.toInt()).coerceIn(10, 100)
}

@Immutable
data class BenchmarkResult(
    val info: HardwareBenchmarkInfo,
    val dataSource: DataSourceType,
)
