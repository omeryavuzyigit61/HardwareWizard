package com.panda.myapplication.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.panda.myapplication.domain.model.CpuSyntheticBenchmark
import com.panda.myapplication.domain.model.GameFpsBenchmark
import com.panda.myapplication.domain.model.GpuSyntheticBenchmark
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.PricePoint
import kotlin.time.Instant

@Entity(
    tableName = "hardware_items",
    indices = [Index(value = ["category"]), Index(value = ["cached_at"])],
)
data class HardwareItemEntity(
    @PrimaryKey val id: String,
    val brand: String,
    val model: String,
    val category: HardwareCategory,
    @ColumnInfo(name = "price_minor") val priceMinor: Long?,
    @ColumnInfo(name = "currency_code") val currencyCode: String?,
    @ColumnInfo(name = "cached_at") val cachedAt: Instant,
    @ColumnInfo(name = "current_price_try") val currentPriceTry: Double? = null,
    @ColumnInfo(name = "lowest_price_try") val lowestPriceTry: Double? = null,
    @ColumnInfo(name = "highest_price_try") val highestPriceTry: Double? = null,
    @ColumnInfo(name = "price_history") val priceHistory: List<PricePoint>? = null,
    @ColumnInfo(name = "benchmark_score") val benchmarkScore: Int? = null,
    @ColumnInfo(name = "benchmark_tier") val benchmarkTier: String? = null,
    @ColumnInfo(name = "benchmark_test_suite") val benchmarkTestSuite: String? = null,
    @ColumnInfo(name = "benchmark_gaming_index") val benchmarkGamingIndex: Int? = null,
    @ColumnInfo(name = "cpu_synthetic") val cpuSynthetic: CpuSyntheticBenchmark? = null,
    @ColumnInfo(name = "gpu_synthetic") val gpuSynthetic: GpuSyntheticBenchmark? = null,
    @ColumnInfo(name = "game_fps") val gameFps: GameFpsBenchmark? = null,
)
