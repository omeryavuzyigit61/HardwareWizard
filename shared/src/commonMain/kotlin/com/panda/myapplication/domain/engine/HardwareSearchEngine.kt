package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageSpec

/**
 * Intelligent token-based and hardware-alias-aware search engine.
 *
 * Implements Google-style multi-token search with domain-specific alias normalization:
 * - Expands abbreviations: r9 -> ryzen 9, i7 -> core i7, u9 -> ultra 9, etc.
 * - Normalizes concatenated tokens: 4070ti -> 4070 ti, 7800x3d -> 7800 x3d, 850w -> 850, 16gb -> 16
 * - Matches all tokens in any order across all product attributes (name, brand, model, chipset, socket, wattage, etc.)
 * - Computes a relevance score so that exact and prioritized matches (e.g. "r9 9900x" -> "AMD Ryzen 9 9900X")
 *   rank at the very top.
 */
object HardwareSearchEngine {

    private val PREFIX_ALIASES = mapOf(
        "r9" to listOf("ryzen", "9"),
        "r7" to listOf("ryzen", "7"),
        "r5" to listOf("ryzen", "5"),
        "r3" to listOf("ryzen", "3"),
        "i9" to listOf("core", "i9"),
        "i7" to listOf("core", "i7"),
        "i5" to listOf("core", "i5"),
        "i3" to listOf("core", "i3"),
        "u9" to listOf("ultra", "9"),
        "u7" to listOf("ultra", "7"),
        "u5" to listOf("ultra", "5"),
    )

    private val MODEL_SUFFIX_REGEX = Regex("""^(\d{4,5})(ti|super|xtx|xt|x3d|x|kf|k|f|g)$""", RegexOption.IGNORE_CASE)
    private val UNIT_SUFFIX_REGEX = Regex("""^(\d+)(w|watt|gb|mb|tb|ghz|mhz|mts)$""", RegexOption.IGNORE_CASE)

    /**
     * Splits and normalizes user query into searchable tokens with hardware aliases expanded.
     */
    fun tokenize(query: String): List<String> {
        val clean = query.trim().lowercase()
        if (clean.isBlank()) return emptyList()

        val rawTokens = clean.split(Regex("""[\s\-+/,]+""")).filter { it.isNotBlank() }
        val result = mutableListOf<String>()

        for (token in rawTokens) {
            val alias = PREFIX_ALIASES[token]
            if (alias != null) {
                result.addAll(alias)
                continue
            }

            val modelMatch = MODEL_SUFFIX_REGEX.matchEntire(token)
            if (modelMatch != null) {
                result.add(token) // keep original e.g. "9900x"
                result.add(modelMatch.groupValues[1]) // "9900"
                result.add(modelMatch.groupValues[2].lowercase()) // "x"
                continue
            }

            val unitMatch = UNIT_SUFFIX_REGEX.matchEntire(token)
            if (unitMatch != null) {
                result.add(token) // "850w"
                result.add(unitMatch.groupValues[1]) // "850"
                continue
            }

            result.add(token)
        }

        return result.distinct()
    }

    /**
     * Builds a comprehensive searchable text blob for [item].
     */
    fun buildSearchableText(item: HardwareItem): String {
        val parts = mutableListOf<String>()
        parts.add(item.displayName)
        parts.add(item.brand)
        parts.add(item.model)
        parts.add(item.category.displayName)

        when (val s = item.spec) {
            is CpuSpec -> {
                parts.add(s.socket)
                s.architecture?.let { parts.add(it) }
                parts.add("${s.coreCount} core")
                parts.add("${s.coreCount} cekirdek")
                parts.add("${s.tdp.watts}w")
                parts.add("${s.tdp.watts}")
                s.l3CacheMb?.let { parts.add("${it}mb") }
            }
            is GpuSpec -> {
                parts.add(s.chipset)
                s.partNumber?.let { parts.add(it) }
                parts.add(s.memoryType)
                val vramGb = s.vram.megabytes / 1024
                parts.add("${vramGb}gb")
                parts.add("$vramGb")
                parts.add("${s.tdp.watts}w")
            }
            is MotherboardSpec -> {
                parts.add(s.socket)
                parts.add(s.chipset)
                parts.add(s.formFactor.displayName)
                parts.add(s.memoryGeneration.displayName)
            }
            is RamSpec -> {
                parts.add(s.generation.displayName)
                val totalGb = s.capacity.megabytes / 1024
                parts.add("${totalGb}gb")
                parts.add("$totalGb")
                parts.add("${s.speed.mhz}mhz")
                parts.add("${s.speed.mhz}")
                parts.add("cl${s.casLatency}")
            }
            is StorageSpec -> {
                parts.add(s.mediaType.displayName)
                parts.add(s.storageInterface.displayName)
                val capGb = s.capacity.megabytes / 1024
                if (capGb >= 1024) {
                    parts.add("${capGb / 1024}tb")
                    parts.add("${capGb / 1024}")
                }
                parts.add("${capGb}gb")
                parts.add("$capGb")
            }
            is PsuSpec -> {
                parts.add("${s.wattage.watts}w")
                parts.add("${s.wattage.watts}")
                parts.add(s.efficiency.displayName)
                s.modularity?.let { parts.add(it) }
                s.tierRating?.let { parts.add(it) }
            }
            is CoolerSpec -> {
                parts.add(s.type.displayName)
                s.radiatorSizeMm?.let { parts.add("${it}mm") }
                parts.addAll(s.supportedSockets)
            }
            else -> Unit
        }

        return parts.joinToString(" ").lowercase()
    }

    /**
     * Determines whether [item] matches the given search query.
     * All non-compound query tokens must be present in the item's searchable text.
     */
    fun matches(item: HardwareItem, query: String): Boolean {
        if (query.isBlank()) return true

        val queryTokens = tokenize(query)
        if (queryTokens.isEmpty()) return true

        val searchable = buildSearchableText(item)

        // Group tokens that are sub-parts: e.g. for "9900x", having "9900x" OR ("9900" AND "x") matches
        // Check if all primary query concepts are matched
        val rawWords = query.trim().lowercase().split(Regex("""[\s\-+/,]+""")).filter { it.isNotBlank() }

        for (raw in rawWords) {
            val alias = PREFIX_ALIASES[raw]
            if (alias != null) {
                // Must match all alias tokens (e.g. "ryzen" and "9")
                val allAliasMatch = alias.all { tok -> searchable.contains(tok) }
                if (!allAliasMatch) return false
                continue
            }

            val modelMatch = MODEL_SUFFIX_REGEX.matchEntire(raw)
            if (modelMatch != null) {
                val full = raw
                val num = modelMatch.groupValues[1]
                val sfx = modelMatch.groupValues[2].lowercase()
                val matched = searchable.contains(full) || (searchable.contains(num) && searchable.contains(sfx))
                if (!matched) return false
                continue
            }

            val unitMatch = UNIT_SUFFIX_REGEX.matchEntire(raw)
            if (unitMatch != null) {
                val full = raw
                val num = unitMatch.groupValues[1]
                val matched = searchable.contains(full) || searchable.contains(num)
                if (!matched) return false
                continue
            }

            // Normal token must be contained
            if (!searchable.contains(raw)) {
                return false
            }
        }

        return true
    }

    /**
     * Computes a relevance score. Higher is better.
     * Exact name / model matches receive massive bonuses.
     */
    fun calculateScore(item: HardwareItem, query: String): Int {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isBlank()) return 0

        var score = 0
        val nameLower = item.displayName.lowercase()
        val modelLower = item.model.lowercase()
        val brandLower = item.brand.lowercase()
        val searchable = buildSearchableText(item)

        // Exact matches
        if (nameLower == cleanQuery || modelLower == cleanQuery) {
            score += 1000
        }
        if (nameLower.startsWith(cleanQuery) || modelLower.startsWith(cleanQuery)) {
            score += 500
        }
        if (nameLower.contains(cleanQuery)) {
            score += 300
        }

        val tokens = tokenize(query)
        for (token in tokens) {
            when {
                modelLower.contains(token) -> score += 60
                nameLower.contains(token) -> score += 40
                brandLower.contains(token) -> score += 20
                searchable.contains(token) -> score += 10
            }
        }

        // Shorter display names matching the query rank higher (closer match)
        val lengthDiff = (nameLower.length - cleanQuery.length).coerceAtLeast(0)
        score -= (lengthDiff / 5).coerceAtMost(50)

        return score
    }

    /**
     * Filters items by query and sorts results by relevance score descending.
     */
    fun search(items: List<HardwareItem>, query: String): List<HardwareItem> {
        if (query.isBlank()) return items
        return items
            .filter { matches(it, query) }
            .sortedByDescending { calculateScore(it, query) }
    }
}
