package com.panda.myapplication.data.model.dto

import com.panda.myapplication.domain.engine.BenchmarkEngine
import com.panda.myapplication.domain.model.CpuSyntheticBenchmark
import com.panda.myapplication.domain.model.DataSourceType
import com.panda.myapplication.domain.model.GameFpsBenchmark
import com.panda.myapplication.domain.model.GpuSyntheticBenchmark
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CpuBenchmarkRawDto(
    @SerialName("gaming_relative_index") val gamingRelativeIndex: Double? = null,
    val score: Int? = null,
    val tier: String? = null,
    val synthetic: CpuSyntheticBenchmark? = null,
    val games: GameFpsBenchmark? = null,
)

@Serializable
data class GpuBenchmarkRawDto(
    @SerialName("relative_performance_pct") val relativePerformancePct: Double? = null,
    val score: Int? = null,
    val tier: String? = null,
    val synthetic: GpuSyntheticBenchmark? = null,
    val games: GameFpsBenchmark? = null,
)

@Serializable
data class BenchmarkCatalogRawDto(
    val cpus: Map<String, CpuBenchmarkRawDto> = emptyMap(),
    val gpus: Map<String, GpuBenchmarkRawDto> = emptyMap(),
) {
    fun toDomainMap(): Map<String, HardwareBenchmarkInfo> {
        val result = mutableMapOf<String, HardwareBenchmarkInfo>()

        for ((id, dto) in cpus) {
            val rel = dto.gamingRelativeIndex ?: 0.85
            val score = dto.score ?: (rel * 100.0).toInt().coerceIn(10, 100)
            val tier = dto.tier ?: when {
                score >= 95 -> "S-Tier"
                score >= 85 -> "A-Tier"
                score >= 70 -> "B-Tier"
                else -> "C-Tier"
            }
            val isX3d = id.contains("x3d", ignoreCase = true)
            result[id] = HardwareBenchmarkInfo(
                score = score,
                tier = tier,
                gamingRelativeIndex = rel,
                relativePerformanceIndex = rel * 100.0,
                cpuSynthetic = dto.synthetic,
                gameFps = BenchmarkEngine.buildFull12GameMatrix(
                    ratio = rel,
                    isCpu = true,
                    isX3d = isX3d,
                    existing = dto.games,
                ),
                dataSource = DataSourceType.VERIFIED_BENCHMARK.name,
                testSuite = "CPU-Monkey & Cinebench / Geekbench Çoklu Test Paketi",
            )
        }

        for ((id, dto) in gpus) {
            val rel = dto.relativePerformancePct ?: 0.85
            val score = dto.score ?: (rel * 100.0).toInt().coerceIn(10, 100)
            val tier = dto.tier ?: when {
                score >= 95 -> "S-Tier"
                score >= 85 -> "A-Tier"
                score >= 70 -> "B-Tier"
                else -> "C-Tier"
            }
            result[id] = HardwareBenchmarkInfo(
                score = score,
                tier = tier,
                relativePerformancePct = rel,
                relativePerformanceIndex = rel * 100.0,
                gpuSynthetic = dto.synthetic,
                gameFps = BenchmarkEngine.buildFull12GameMatrix(
                    ratio = rel,
                    isCpu = false,
                    isX3d = false,
                    existing = dto.games,
                ),
                dataSource = DataSourceType.VERIFIED_BENCHMARK.name,
                testSuite = "3DMark Time Spy / Steel Nomad Çoklu Test Paketi",
            )
        }

        return result
    }
}
