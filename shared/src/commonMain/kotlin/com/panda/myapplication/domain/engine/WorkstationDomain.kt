package com.panda.myapplication.domain.engine

/**
 * Profesyonel İş İstasyonu & Yaratıcı Donanım Laboratuvarı (Workstation Lab v2) Domain Modelleri.
 * Saf Kotlin (KMP commonMain), Android/Java bağımsız.
 */

enum class WorkstationCategory(
    val titleTr: String,
    val titleEn: String,
    val emoji: String,
) {
    VIDEO_EDITING("Video & Post-Prodüksiyon", "Video & Post-Production", "🎬"),
    VFX_3D("3D Modelleme, CGI & VFX", "3D Modeling, CGI & VFX", "🧊"),
    GAME_DEV_ARCHVIZ("Oyun Motorları & ArchViz", "Game Engines & ArchViz", "🎮"),
    CAD_ENGINEERING("Mühendislik, CAD & Simülasyon", "Engineering, CAD & Sim", "📐"),
    LOCAL_AI("Yapay Zeka & Veri Bilimi", "AI, LLM & Data Science", "🧠"),
    GRAPHIC_DESIGN("Grafik & Fotoğraf Düzenleme", "Graphic & Photo Editing", "🎨"),
    AUDIO_PRODUCTION("Müzik Prodüksiyonu & Ses", "Music Production & Audio", "🎧"),
}

enum class WorkloadResolution(
    val labelTr: String,
    val labelEn: String,
    val badge: String,
    val descriptionTr: String,
    val descriptionEn: String,
) {
    FHD_1080P(
        labelTr = "1080p FHD / Standart Proje",
        labelEn = "1080p FHD / Standard Project",
        badge = "FHD",
        descriptionTr = "8-bit H.264 • Hafif 3D • 8B Q4 LLM • 32 Kanal DAW • Temel CAD",
        descriptionEn = "8-bit H.264 • Light 3D • 8B Q4 LLM • 32-Track DAW • Basic CAD",
    ),
    QHD_2K(
        labelTr = "2K QHD (1440p) / Orta Ölçek",
        labelEn = "2K QHD (1440p) / Mid Project",
        badge = "2K",
        descriptionTr = "Yüksek Bitrate H.265 / ProRes • 14B Q4 LLM • 64 Kanal DAW • Orta Montaj",
        descriptionEn = "High-Bitrate H.265 / ProRes • 14B Q4 LLM • 64-Track DAW • Mid Assemblies",
    ),
    UHD_4K(
        labelTr = "4K UHD (2160p) / Ağır Üretim",
        labelEn = "4K UHD (2160p) / Heavy Pro",
        badge = "4K",
        descriptionTr = "10-bit 4:2:2 All-Intra • 32B Q4 LLM • Nanite/RT • 128 Kanal Orkestra • FEA",
        descriptionEn = "10-bit 4:2:2 All-Intra • 32B Q4 LLM • Nanite/RT • 128-Track Orchestra • FEA",
    ),
    RAW_8K(
        labelTr = "8K Cinema RAW / Extreme Stüdyo",
        labelEn = "8K Cinema RAW / Studio Extreme",
        badge = "8K",
        descriptionTr = "8K RED/ARRIRAW • 70B Q4 LLM (48GB VRAM) • Houdini FX • 250+ Senfonik Kanal",
        descriptionEn = "8K RED/ARRIRAW • 70B Q4 LLM (48GB VRAM) • Houdini FX • 250+ Symphonic Tracks",
    ),
}

enum class WorkloadPhase(
    val labelTr: String,
    val labelEn: String,
    val descriptionTr: String,
    val descriptionEn: String,
) {
    REALTIME_PREVIEW(
        labelTr = "Canlı Önizleme & Viewport (Playback)",
        labelEn = "Real-Time Timeline Scrubbing & Viewport",
        descriptionTr = "Zaman çizelgesinde veya 3D/DAW sahnesinde anlık kare atlamasız akıcı etkileşim",
        descriptionEn = "Fluid timeline scrubbing and low-latency live viewport navigation",
    ),
    FINAL_RENDER(
        labelTr = "Final Dışa Aktarma & Render (Export / Mixdown)",
        labelEn = "Final Export & Baking / Mixdown",
        descriptionTr = "Çoklu çekirdekleri, çift/üçlü NVENC ve QuickSync motorlarını zorlayan nihai çıktı süreci",
        descriptionEn = "Full multithreaded, multi-NVENC and QuickSync accelerated final output pipeline",
    ),
    HEAVY_SIMULATION(
        labelTr = "Ağır Simülasyon, Işın İzleme & AI Çıkarım",
        labelEn = "Heavy Simulation, Ray Tracing & AI Inference",
        descriptionTr = "Akışkan/partikül fiziği, OptiX gürültü giderme, tensör matrisleri ve FEA çözücüleri",
        descriptionEn = "Fluid/particle physics, OptiX denoising, tensor matrix and FEA solvers",
    ),
}

enum class HardwareTier(val titleTr: String, val titleEn: String, val badgeColorHex: Long) {
    MINIMUM(
        titleTr = "Temel Giriş Düzeyi",
        titleEn = "Entry Minimum",
        badgeColorHex = 0xFFFFA000, // Amber
    ),
    RECOMMENDED_PRO(
        titleTr = "Önerilen Profesyonel",
        titleEn = "Recommended Pro",
        badgeColorHex = 0xFF00E676, // Neon Emerald
    ),
    STUDIO_EXTREME(
        titleTr = "Ağır Stüdyo & İş İstasyonu",
        titleEn = "Studio Extreme Workstation",
        badgeColorHex = 0xFF00E5FF, // Cyan
    ),
}

enum class WorkstationMetricKind {
    PUGET_SCORE,
    BLENDER_OPTIX_SPM,
    LLM_TOKENS_PER_SEC,
    AI_IT_PER_SEC,
    CAD_IPC_INDEX,
    ENGINE_VIEWPORT_BUILD,
    DAW_TRACK_HEADROOM,
}

/**
 * Uygulama bazlı gerçek darboğaz ağırlık vektörü (toplamı normalize edilerek UI'da da gösterilir).
 */
data class WorkloadWeights(
    val cpuSinglePct: Int,
    val cpuMultiPct: Int,
    val gpuComputePct: Int,
    val memoryIoPct: Int,
)

data class HardwareRecommendation(
    val tier: HardwareTier,
    val targetCpuChipset: String,
    val targetCpuSearchQuery: String,
    val cpuSpecsSummary: String,
    val targetGpuChipset: String,
    val targetGpuSearchQuery: String,
    val gpuSpecsSummary: String,
    val targetRamCapacityGb: Int,
    val targetRamTypeAndSpeed: String,
    val targetStorageType: String,
    val vramRequirementGb: Int,
    val estimatedExperienceNoteTr: String,
    val estimatedExperienceNoteEn: String,
    /** Gerçek formülle hesaplanmış çıktı metriği (örn. "PugetBench ~11.850", "~71 tok/s (32B Q4)", "~15.800 smp/dk"). */
    val calculatedMetricBadgeTr: String,
    val calculatedMetricBadgeEn: String,
    /** Sayısal karşılaştırma ve regresyon testleri için ham hesaplanan skor/değer. */
    val calculatedNumericMetric: Double,
    /** Eski uyumluluk alanı: artık sahte 98-100 yerine hesaplanan kademe yeterlilik skoru (0..100). */
    val benchmarkConfidenceScore: Int,
)

data class WorkstationApp(
    val id: String,
    val name: String,
    val shortName: String,
    val category: WorkstationCategory,
    val iconName: String,
    val primaryBottleneckTr: String,
    val primaryBottleneckEn: String,
    val officialBenchmarkSource: String,
    val supportedResolutions: List<WorkloadResolution>,
    val supportedPhases: List<WorkloadPhase>,
)

data class WorkstationRequirementProfile(
    val app: WorkstationApp,
    val resolution: WorkloadResolution,
    val phase: WorkloadPhase,
    val workloadWeights: WorkloadWeights,
    val minimumTier: HardwareRecommendation,
    val recommendedTier: HardwareRecommendation,
    val studioTier: HardwareRecommendation,
    val dataCitation: String,
    val keyTechnicalTakeawayTr: String,
    val keyTechnicalTakeawayEn: String,
)
