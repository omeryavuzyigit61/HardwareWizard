package com.panda.myapplication.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HardwareBenchmarkInfo(
    val score: Int = 0,
    val singleCoreScore: Int? = null,
    val multiCoreScore: Int? = null,
    val timeSpyScore: Int? = null,
    val gamingIndex: Int? = null,
    val productivityIndex: Int? = null,
    val tier: String = "A-Tier",
    val rank: Int? = null,
    val testSuite: String = "Donanım Benchmark Laboratuvarı",
)
