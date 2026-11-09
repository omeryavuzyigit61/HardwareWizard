package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class VerifiedLabTestEntry(
    val modelName: String,
    val fullModelName: String,
    val wattLabel: String,
    val isAmd: Boolean,
    val avgFps: Double,
    val minFps: Double,
    val modelKeywords: List<String> = emptyList(),
    val maxFps: Double? = null,
    val low1PctFps: Double? = null,
) {
    fun matchesItem(item: HardwareItem): Boolean {
        val target = "${item.brand} ${item.model}".lowercase()
        return modelKeywords.any { kw ->
            val lk = kw.lowercase()
            target.contains(lk)
        } || target.contains(modelName.lowercase())
    }
}

@Immutable
@Serializable
data class VerifiedLabGameTest(
    val id: String,
    val gameTitle: String,
    val resolutionSettings: String,
    val rigSummary: String,
    val rigGpuOrCpu: String,
    val rigRam: String,
    val isCpuTest: Boolean,
    val entries: List<VerifiedLabTestEntry>,
) {
    val cleanSettings: String
        get() = resolutionSettings
            .replace("(", "")
            .replace(")", "")
            .replace("/", "•")
            .trim()
}

object VerifiedLabBenchmarks {

    private var remoteTests: List<VerifiedLabGameTest>? = null

    fun updateRemoteTests(tests: List<VerifiedLabGameTest>) {
        if (tests.isNotEmpty()) {
            remoteTests = tests
        }
    }

    fun getAllTests(): List<VerifiedLabGameTest> {
        return remoteTests ?: emptyList()
    }

    fun getLabTestsForItem(item: HardwareItem): List<VerifiedLabGameTest> {
        val all = getAllTests()
        val isCpu = item.category == HardwareCategory.CPU
        return all.filter { it.isCpuTest == isCpu }
    }

    fun hasVerifiedLabTest(item: HardwareItem): Boolean {
        val tests = getLabTestsForItem(item)
        return tests.any { test -> test.entries.any { it.matchesItem(item) } }
    }
}
