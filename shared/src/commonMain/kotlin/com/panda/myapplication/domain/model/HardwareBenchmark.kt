package com.panda.myapplication.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class DataSourceType {
    VERIFIED_BENCHMARK,
    ESTIMATED_SIMULATION,
}

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

@Serializable
data class GpuSyntheticBenchmark(
    @SerialName("time_spy_graphics")
    val timeSpyGraphics: Int? = null,

    @SerialName("time_spy_extreme")
    val timeSpyExtreme: Int? = null,

    @SerialName("steel_nomad")
    val steelNomad: Int? = null,
)

@Serializable
data class ResolutionFps(
    val p1080: Double,
    val p1440: Double,
    val p4k: Double,
)

@Serializable
data class GameFpsBenchmark(
    @SerialName("cyberpunk_2077")
    val cyberpunk2077: ResolutionFps? = null,

    @SerialName("forza_horizon_5")
    val forzaHorizon5: ResolutionFps? = null,

    @SerialName("counter_strike_2")
    val counterStrike2: ResolutionFps? = null,
)

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

data class BenchmarkResult(
    val info: HardwareBenchmarkInfo,
    val dataSource: DataSourceType,
)
