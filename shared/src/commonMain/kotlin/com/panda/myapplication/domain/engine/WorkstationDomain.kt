package com.panda.myapplication.domain.engine

/**
 * Profesyonel İş İstasyonu & Yaratıcı Donanım Laboratuvarı (Workstation Lab) Domain Modelleri.
 * pre_prompt_kmp.md standartlarına uygun: Saf Kotlin, Android/Java bağımsız.
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
        descriptionTr = "Standart 8-bit H.264 / Hafif 3D / 32 Kanal DAW / Temel CAD",
        descriptionEn = "Standard 8-bit H.264 / Light 3D / 32-Track DAW / Basic CAD",
    ),
    QHD_2K(
        labelTr = "2K QHD (1440p) / Orta Ölçek",
        labelEn = "2K QHD (1440p) / Mid Project",
        badge = "2K",
        descriptionTr = "Yüksek bit hızlı H.265 / ProRes Proxy / 64 Kanal DAW / Orta Montaj",
        descriptionEn = "High bitrate H.265 / ProRes Proxy / 64-Track DAW / Mid Assemblies",
    ),
    UHD_4K(
        labelTr = "4K UHD (2160p) / Ağır Üretim",
        labelEn = "4K UHD (2160p) / Heavy Pro",
        badge = "4K",
        descriptionTr = "10-bit 4:2:2 All-Intra / Nanite & Işın İzleme / 100+ Kanal Orkestra / FEA",
        descriptionEn = "10-bit 4:2:2 All-Intra / Nanite & RT / 100+ Orchestral Tracks / FEA",
    ),
    RAW_8K(
        labelTr = "8K Cinema RAW / Extreme Stüdyo",
        labelEn = "8K Cinema RAW / Studio Extreme",
        badge = "8K",
        descriptionTr = "8K RED/ARRIRAW / Hollywood VFX & Houdini Sim / 70B LLM & Flux.1",
        descriptionEn = "8K RED/ARRIRAW / Hollywood VFX & Houdini / 70B LLM & Flux.1",
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
        descriptionTr = "Çoklu çekirdekleri, çift NVENC ve QuickSync motorlarını zorlayan nihai çıktı süreci",
        descriptionEn = "Full multithreaded, dual-NVENC and QuickSync accelerated final output pipeline",
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
    val benchmarkConfidenceScore: Int = 98,
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
    val minimumTier: HardwareRecommendation,
    val recommendedTier: HardwareRecommendation,
    val studioTier: HardwareRecommendation,
    val dataCitation: String,
    val keyTechnicalTakeawayTr: String,
    val keyTechnicalTakeawayEn: String,
)
