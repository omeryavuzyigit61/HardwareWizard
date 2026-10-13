package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

enum class DataSourceType {
    VERIFIED_BENCHMARK,
    ESTIMATED_SIMULATION,
}

@Serializable
data class CinebenchScores(
    val singleCore: Int,
    val multiCore: Int,
)

@Serializable
data class Mark3dScores(
    val timeSpy: Int? = null,
    val fireStrike: Int? = null,
    val portRoyal: Int? = null,
)

@Serializable
data class ResolutionFps(
    val p1080: Double,
    val p1440: Double,
    val p4k: Double,
)

@Serializable
data class GameFpsBenchmark(
    val cyberpunk2077: ResolutionFps? = null,
    val forzaHorizon5: ResolutionFps? = null,
    val counterStrike2: ResolutionFps? = null,
)

@Serializable
data class HardwareBenchmarkInfo(
    val score: Int = 0,
    val tier: String = "A-Tier",
    val relativePerformanceIndex: Double = 50.0,
    val cinebenchR23: CinebenchScores? = null,
    val mark3d: Mark3dScores? = null,
    val gameFps: GameFpsBenchmark? = null,
    val dataSource: String = DataSourceType.VERIFIED_BENCHMARK.name,
    val testSuite: String = "Donanım Benchmark Laboratuvarı",
    val singleCoreScore: Int? = null,
    val multiCoreScore: Int? = null,
    val timeSpyScore: Int? = null,
    val gamingIndex: Int? = null,
    val productivityIndex: Int? = null,
    val rank: Int? = null,
) {
    val isVerified: Boolean
        get() = dataSource == DataSourceType.VERIFIED_BENCHMARK.name

    val effectiveSingleCore: Int?
        get() = cinebenchR23?.singleCore ?: singleCoreScore

    val effectiveMultiCore: Int?
        get() = cinebenchR23?.multiCore ?: multiCoreScore

    val effectiveTimeSpy: Int?
        get() = mark3d?.timeSpy ?: timeSpyScore

    val effectiveGamingIndex: Int
        get() = gamingIndex ?: relativePerformanceIndex.toInt().coerceIn(10, 100)
}

data class BenchmarkResult(
    val info: HardwareBenchmarkInfo,
    val dataSource: DataSourceType,
)
