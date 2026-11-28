package com.panda.myapplication.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    fun matchesItem(item: HardwareItem, isCpuTest: Boolean? = null): Boolean {
        if (item.category != HardwareCategory.CPU && item.category != HardwareCategory.GPU) {
            return false
        }
        if (isCpuTest != null) {
            val itemIsCpu = item.category == HardwareCategory.CPU
            if (itemIsCpu != isCpuTest) return false
        }
        return if (item.category == HardwareCategory.CPU) {
            val itemKey = VerifiedLabBenchmarks.canonicalCpuKey("${item.brand} ${item.model}")
            if (itemKey.isEmpty()) return false
            val entryKeys = (listOf(modelName, fullModelName) + modelKeywords)
                .map { VerifiedLabBenchmarks.canonicalCpuKey(it) }
                .filter { it.isNotEmpty() }
            itemKey in entryKeys
        } else {
            val gpuChip = (item.spec as? GpuSpec)?.chipset.orEmpty()
            val itemKey = VerifiedLabBenchmarks.canonicalGpuKey("${item.brand} ${item.model} $gpuChip")
            if (itemKey.isEmpty()) return false
            val entryKeys = (listOf(modelName, fullModelName) + modelKeywords)
                .map { VerifiedLabBenchmarks.canonicalGpuKey(it) }
                .filter { it.isNotEmpty() }
            itemKey in entryKeys
        }
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

    fun cleanSettingsLocalized(isTr: Boolean): String {
        val base = cleanSettings
        if (isTr) return base
        return base
            .replace("Düşük Detay", "Low Preset")
            .replace("Rekabetçi", "Competitive")
            .replace("Ultra Ayarlar", "Ultra Preset")
            .replace("Çok Yüksek", "Very High")
            .replace("Yüksek Ayarlar", "High Preset")
            .replace("Sinematik", "Cinematic")
            .replace("Ekstrem", "Extreme")
            .replace("RT Kapalı", "RT Off")
    }
}

object VerifiedLabBenchmarks {

    private val _testsFlow = MutableStateFlow<List<VerifiedLabGameTest>>(emptyList())
    val testsFlow: StateFlow<List<VerifiedLabGameTest>> = _testsFlow.asStateFlow()

    fun updateRemoteTests(tests: List<VerifiedLabGameTest>) {
        if (tests.isNotEmpty()) {
            _testsFlow.value = tests
        }
    }

    fun getAllTests(): List<VerifiedLabGameTest> = _testsFlow.value

    /**
     * Option B: Returns verified lab tests ONLY for desktop CPUs and desktop GPUs that actually
     * appear in the verified benchmark chart (never returns desktop GPU charts on Laptops or unlisted SKUs).
     */
    fun getLabTestsForItem(
        item: HardwareItem,
        sourceTests: List<VerifiedLabGameTest> = _testsFlow.value,
    ): List<VerifiedLabGameTest> {
        if (item.category != HardwareCategory.CPU && item.category != HardwareCategory.GPU) {
            return emptyList()
        }
        val isCpu = item.category == HardwareCategory.CPU
        return sourceTests.filter { test ->
            test.isCpuTest == isCpu && test.entries.any { entry -> entry.matchesItem(item, test.isCpuTest) }
        }
    }

    fun hasVerifiedLabTest(
        item: HardwareItem,
        sourceTests: List<VerifiedLabGameTest> = _testsFlow.value,
    ): Boolean = getLabTestsForItem(item, sourceTests).isNotEmpty()

    private val CPU_TOKEN_REGEX = Regex(
        """\b(\d{4,5}X3D2|\d{4,5}X3D|\d{4,5}XT|\d{4,5}KF|\d{4,5}KS|\d{4,5}K|\d{4,5}X|\d{4,5}F|\d{4,5}G|\d{4,5}|285K|265KF|265K|265F|245KF|245K|225F)\b""",
        RegexOption.IGNORE_CASE,
    )

    private val GPU_PATTERNS: List<Pair<Regex, String>> = listOf(
        Regex("""\bRTX\s*5090\s*D\b""", RegexOption.IGNORE_CASE) to "RTX_5090_D",
        Regex("""\bRTX\s*5090\b""", RegexOption.IGNORE_CASE) to "RTX_5090",
        Regex("""\bRTX\s*5080\b""", RegexOption.IGNORE_CASE) to "RTX_5080",
        Regex("""\bRTX\s*5070\s*TI\b""", RegexOption.IGNORE_CASE) to "RTX_5070_TI",
        Regex("""\bRTX\s*5070\b""", RegexOption.IGNORE_CASE) to "RTX_5070",
        Regex("""\bRTX\s*5060\s*TI\b""", RegexOption.IGNORE_CASE) to "RTX_5060_TI",
        Regex("""\bRTX\s*5060\b""", RegexOption.IGNORE_CASE) to "RTX_5060",
        Regex("""\bRTX\s*4090\s*D\b""", RegexOption.IGNORE_CASE) to "RTX_4090_D",
        Regex("""\bRTX\s*4090\b""", RegexOption.IGNORE_CASE) to "RTX_4090",
        Regex("""\bRTX\s*4080\s*SUPER\b""", RegexOption.IGNORE_CASE) to "RTX_4080_SUPER",
        Regex("""\bRTX\s*4080\b""", RegexOption.IGNORE_CASE) to "RTX_4080",
        Regex("""\bRTX\s*4070\s*TI\s*SUPER\b""", RegexOption.IGNORE_CASE) to "RTX_4070_TI_SUPER",
        Regex("""\bRTX\s*4070\s*SUPER\b""", RegexOption.IGNORE_CASE) to "RTX_4070_SUPER",
        Regex("""\bRTX\s*4070\s*TI\b""", RegexOption.IGNORE_CASE) to "RTX_4070_TI",
        Regex("""\bRTX\s*4070\b""", RegexOption.IGNORE_CASE) to "RTX_4070",
        Regex("""\bRTX\s*4060\s*TI\b""", RegexOption.IGNORE_CASE) to "RTX_4060_TI",
        Regex("""\bRTX\s*4060\b""", RegexOption.IGNORE_CASE) to "RTX_4060",
        Regex("""\bRX\s*9070\s*XT\b""", RegexOption.IGNORE_CASE) to "RX_9070_XT",
        Regex("""\bRX\s*9070\b""", RegexOption.IGNORE_CASE) to "RX_9070",
        Regex("""\bRX\s*7900\s*XTX\b""", RegexOption.IGNORE_CASE) to "RX_7900_XTX",
        Regex("""\bRX\s*7900\s*XT\b""", RegexOption.IGNORE_CASE) to "RX_7900_XT",
        Regex("""\bRX\s*7900\s*GRE\b""", RegexOption.IGNORE_CASE) to "RX_7900_GRE",
        Regex("""\bRX\s*7800\s*XT\b""", RegexOption.IGNORE_CASE) to "RX_7800_XT",
        Regex("""\bRX\s*7700\s*XT\b""", RegexOption.IGNORE_CASE) to "RX_7700_XT",
        Regex("""\bRX\s*7600\s*XT\b""", RegexOption.IGNORE_CASE) to "RX_7600_XT",
        Regex("""\bRX\s*7600\b""", RegexOption.IGNORE_CASE) to "RX_7600",
        Regex("""\bARC\s*B580\b""", RegexOption.IGNORE_CASE) to "ARC_B580",
        Regex("""\bARC\s*B570\b""", RegexOption.IGNORE_CASE) to "ARC_B570",
    )

    internal fun canonicalCpuKey(raw: String): String {
        val u = raw.uppercase()
        if (u.contains("LAPTOP") || u.contains("MOBILE") || Regex("""\b\d{4,5}(HX|HS|H|U)\b""").containsMatchIn(u)) {
            return ""
        }
        val match = CPU_TOKEN_REGEX.findAll(u).lastOrNull()?.value ?: return ""
        // Normalize KF -> K and non-iGPU F variants on Intel unlocked/locked pairs only when exact
        return when (match) {
            "14900KF" -> "14900K"
            "14700KF" -> "14700K"
            "14600KF" -> "14600K"
            "13600KF" -> "13600K"
            "265KF" -> "265K"
            "245KF" -> "245K"
            "12400" -> "12400F"
            else -> match
        }
    }

    internal fun canonicalGpuKey(raw: String): String {
        val u = raw.uppercase()
        if (u.contains("LAPTOP") || u.contains("MOBILE") || u.contains("MAX-Q")) return ""
        for ((regex, key) in GPU_PATTERNS) {
            if (regex.containsMatchIn(u)) return key
        }
        return ""
    }
}
