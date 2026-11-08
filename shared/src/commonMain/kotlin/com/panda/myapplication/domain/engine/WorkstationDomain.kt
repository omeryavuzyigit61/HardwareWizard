package com.panda.myapplication.domain.engine

/**
 * Profesyonel İş İstasyonu & Yaratıcı Donanım Laboratuvarı (Workstation Lab) Domain Modelleri.
 * pre_prompt_kmp.md standartlarına uygun: Saf Kotlin, Android/Java bağımsız.
 */

enum class WorkstationCategory(val titleTr: String, val titleEn: String) {
    VIDEO_EDITING("Video & Post-Prodüksiyon", "Video & Post-Production"),
    VFX_3D("3D Modelleme, CGI & VFX", "3D Modeling, CGI & VFX"),
    CAD_ENGINEERING("Mühendislik & CAD / BIM", "Engineering & CAD / BIM"),
    LOCAL_AI("Yapay Zeka & Yerel LLM", "Local AI & Machine Learning"),
    GRAPHIC_DESIGN("Grafik & Fotoğraf Düzenleme", "Graphic & Photo Editing"),
}

enum class WorkloadResolution(
    val labelTr: String,
    val labelEn: String,
    val badge: String,
    val descriptionTr: String,
    val descriptionEn: String,
) {
    FHD_1080P(
        labelTr = "1080p FHD",
        labelEn = "1080p FHD",
        badge = "FHD",
        descriptionTr = "Standart 8-bit H.264 / Hafif 3D sahneler",
        descriptionEn = "Standard 8-bit H.264 / Light 3D assets",
    ),
    QHD_2K(
        labelTr = "2K QHD (1440p)",
        labelEn = "2K QHD (1440p)",
        badge = "2K",
        descriptionTr = "Yüksek bit hızlı H.265 / ProRes Proxy",
        descriptionEn = "High bitrate H.265 / ProRes Proxy",
    ),
    UHD_4K(
        labelTr = "4K UHD (2160p)",
        labelEn = "4K UHD (2160p)",
        badge = "4K",
        descriptionTr = "10-bit 4:2:2 All-Intra / Karmaşık 3D ve Nanite",
        descriptionEn = "10-bit 4:2:2 All-Intra / Dense 3D & Nanite",
    ),
    RAW_8K(
        labelTr = "8K Cinema RAW",
        labelEn = "8K Cinema RAW",
        badge = "8K",
        descriptionTr = "8K RED/ARRIRAW, Sinematik VFX, Ağır Model Eğitimi",
        descriptionEn = "8K RED/ARRIRAW, Cinematic VFX, Heavy AI Model",
    ),
}

enum class WorkloadPhase(
    val labelTr: String,
    val labelEn: String,
    val descriptionTr: String,
    val descriptionEn: String,
) {
    REALTIME_PREVIEW(
        labelTr = "Canlı Zaman Çizelgesi Önizleme (Playback)",
        labelEn = "Real-Time Timeline Scrubbing",
        descriptionTr = "Zaman çizelgesinde kare atlamadan (zero dropped frames) akıcı gezinme ve anlık efekt önizleme",
        descriptionEn = "Fluid timeline playback with zero dropped frames and live FX preview",
    ),
    FINAL_RENDER(
        labelTr = "Final Dışa Aktarma & Render (Export)",
        labelEn = "Final Export & Baking",
        descriptionTr = "Tüm çekirdekleri ve çift donanımsal kodlayıcıları (NVENC) zorlayan nihai çıktı süreci",
        descriptionEn = "Full multithreaded and hardware dual-encoder export pipeline",
    ),
    HEAVY_SIMULATION(
        labelTr = "Ağır Simülasyon, Işın İzleme & AI Çıkarım",
        labelEn = "Heavy Simulation, Ray Tracing & Inference",
        descriptionTr = "Fizik motoru, sıvı/partikül dinamikleri, OptiX gürültü giderme ve tensör matris hesaplaması",
        descriptionEn = "Fluid/cloth dynamics, OptiX denoising and matrix tensor operations",
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
