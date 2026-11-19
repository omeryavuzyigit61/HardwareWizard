package com.panda.myapplication.data.model.dto

import com.panda.myapplication.domain.model.CpuSyntheticBenchmark
import com.panda.myapplication.domain.model.GameFpsBenchmark
import com.panda.myapplication.domain.model.GpuSyntheticBenchmark
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Lightweight (~120 bytes) version header returned by Supabase `catalog_meta`.
 * Checked before fetching any delta rows so unchanged clients consume ~0 egress.
 */
@Serializable
data class SupabaseCatalogMetaDto(
    @SerialName("latest_revision") val latestRevision: Long = 0L,
    @SerialName("total_items") val totalItems: Int = 0,
    @SerialName("last_delta_at") val lastDeltaAt: String? = null,
)

/**
 * Authoritative single-row representation from Supabase `public.hardware_catalog`.
 * `spec_payload` mirrors the exact Room `*_specs` entity columns for all 28 categories.
 */
@Serializable
data class SupabaseHardwareCatalogRowDto(
    val id: String,
    val category: String,
    val brand: String,
    val model: String,
    @SerialName("canonical_key") val canonicalKey: String = "",
    @SerialName("spec_table") val specTable: String = "",
    @SerialName("spec_payload") val specPayload: JsonObject = JsonObject(emptyMap()),
    @SerialName("benchmark_score") val benchmarkScore: Int? = null,
    @SerialName("benchmark_tier") val benchmarkTier: String? = null,
    @SerialName("benchmark_test_suite") val benchmarkTestSuite: String? = null,
    @SerialName("benchmark_gaming_index") val benchmarkGamingIndex: Int? = null,
    @SerialName("cpu_synthetic") val cpuSynthetic: CpuSyntheticBenchmark? = null,
    @SerialName("gpu_synthetic") val gpuSynthetic: GpuSyntheticBenchmark? = null,
    @SerialName("game_fps") val gameFps: GameFpsBenchmark? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val images: List<String> = emptyList(),
    @SerialName("extra_specs") val extraSpecs: Map<String, String> = emptyMap(),
    @SerialName("source_provenance") val sourceProvenance: List<String> = emptyList(),
    @SerialName("spec_verified_at") val specVerifiedAt: String? = null,
    @SerialName("catalog_revision") val catalogRevision: Long = 1L,
)
