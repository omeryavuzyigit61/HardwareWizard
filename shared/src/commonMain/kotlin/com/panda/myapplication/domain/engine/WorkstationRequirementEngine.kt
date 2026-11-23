package com.panda.myapplication.domain.engine

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Profesyonel İş İstasyonu & Yaratıcı Donanım Laboratuvarı — Parametrik Hesaplama Motoru (v2).
 *
 * Her uygulama (35 profesyonel yazılım) için:
 *  - Gerçek darboğaz ağırlık vektörü (CPU Tek Çekirdek, CPU Çoklu, GPU/OptiX/CUDA, Bellek/IO)
 *  - Çözünürlük/ölçek (1080p < 2K < 4K < 8K) ve aşama (Önizleme / Render / Simülasyon) çarpanları
 *  - Katalog (rev = 7) ile birebir eşleşen 2025/2026 CPU & GPU havuzu (RTX 5090/5080/5070 Ti,
 *    RTX 6000 Ada 48GB, Core Ultra 285K/265K/245K, Ryzen 9 9950X [64MB L3], 9900X, 9700X, Threadripper)
 *  - Fiziksel formüllere dayalı gerçek hesaplanan çıktı metrikleri:
 *    PugetBench skoru, Blender Cycles OptiX (smp/dk), LLM bellek bant genişliği tok/s + VRAM sığma kontrolü,
 *    SDXL/Flux it/s, CAD tek çekirdek/FEA indeksi, DAW 64-sample ASIO kanal kapasitesi.
 */
object WorkstationRequirementEngine {

    // =========================================================================
    // 1. 2025/2026 DOĞRULANMIŞ İŞ İSTASYONU DONANIM HAVUZU (Catalog rev = 7 ile 1:1)
    // =========================================================================

    private data class CpuCandidate(
        val id: String,
        val displayName: String,
        val searchQuery: String,
        val specsSummary: String,
        val cb23Single: Int,
        val cb23Multi: Int,
        val hasQuickSync: Boolean,
        val hasAvx512: Boolean,
        val isHedtQuadChannel: Boolean,
        val isUltraQuietDaw: Boolean,
        val tierRank: Int, // 1..10
    )

    private data class GpuCandidate(
        val id: String,
        val displayName: String,
        val searchQuery: String,
        val specsSummary: String,
        val vramGb: Int,
        val bandwidthGbs: Int,
        val optixSpm: Int,         // Blender Open Data Cycles OptiX samples/min (Monster/Junkshop/Classroom sum)
        val tensorIndex: Double,   // RTX 4090 = 100.0, RTX 5090 = 152.0
        val hasDualOrTripleNvenc: Boolean,
        val has422HardwareDecode: Boolean, // Blackwell 9th-Gen NVDEC 10-bit 4:2:2
        val isSilentEntry: Boolean,
        val tierRank: Int, // 0..10
    )

    private val CPU_POOL: List<CpuCandidate> = listOf(
        CpuCandidate(
            id = "9600x", displayName = "Ryzen 5 9600X", searchQuery = "9600X",
            specsSummary = "6Ç/12İ Zen 5 • 5.4 GHz • 32 MB L3 • 65W",
            cb23Single = 2180, cb23Multi = 17200,
            hasQuickSync = false, hasAvx512 = true, isHedtQuadChannel = false, isUltraQuietDaw = true, tierRank = 1,
        ),
        CpuCandidate(
            id = "245k", displayName = "Core Ultra 5 245K", searchQuery = "245K",
            specsSummary = "14Ç/14İ (6P+8E) • 5.2 GHz • Intel QuickSync (4:2:2)",
            cb23Single = 2180, cb23Multi = 26000,
            hasQuickSync = true, hasAvx512 = false, isHedtQuadChannel = false, isUltraQuietDaw = true, tierRank = 2,
        ),
        CpuCandidate(
            id = "14600k", displayName = "Core i5-14600K", searchQuery = "14600K",
            specsSummary = "14Ç/20İ (6P+8E) • 5.3 GHz • Intel QuickSync",
            cb23Single = 2060, cb23Multi = 24600,
            hasQuickSync = true, hasAvx512 = false, isHedtQuadChannel = false, isUltraQuietDaw = false, tierRank = 2,
        ),
        CpuCandidate(
            id = "9700x", displayName = "Ryzen 7 9700X", searchQuery = "9700X",
            specsSummary = "8Ç/16İ Tek CCD Zen 5 • 5.5 GHz • 32 MB L3 • 65W (Ultra Sessiz)",
            cb23Single = 2210, cb23Multi = 21800,
            hasQuickSync = false, hasAvx512 = true, isHedtQuadChannel = false, isUltraQuietDaw = true, tierRank = 3,
        ),
        CpuCandidate(
            id = "14700k", displayName = "Core i7-14700K", searchQuery = "14700K",
            specsSummary = "20Ç/28İ (8P+12E) • 5.6 GHz • 33 MB L3 • Intel QuickSync",
            cb23Single = 2160, cb23Multi = 34800,
            hasQuickSync = true, hasAvx512 = false, isHedtQuadChannel = false, isUltraQuietDaw = false, tierRank = 5,
        ),
        CpuCandidate(
            id = "265k", displayName = "Core Ultra 7 265K", searchQuery = "265K",
            specsSummary = "20Ç/20İ (8P+12E) • 5.5 GHz • 30 MB L3 • Intel QuickSync (4:2:2)",
            cb23Single = 2260, cb23Multi = 37800,
            hasQuickSync = true, hasAvx512 = false, isHedtQuadChannel = false, isUltraQuietDaw = true, tierRank = 6,
        ),
        CpuCandidate(
            id = "9900x", displayName = "Ryzen 9 9900X", searchQuery = "9900X",
            specsSummary = "12Ç/24İ Zen 5 • 5.6 GHz • 64 MB L3 (2×32 MB) • AVX-512",
            cb23Single = 2280, cb23Multi = 34200,
            hasQuickSync = false, hasAvx512 = true, isHedtQuadChannel = false, isUltraQuietDaw = true, tierRank = 6,
        ),
        CpuCandidate(
            id = "285k", displayName = "Core Ultra 9 285K", searchQuery = "285K",
            specsSummary = "24Ç/24İ (8P+16E) • 5.7 GHz • 36 MB L3 • Intel QuickSync (4:2:2)",
            cb23Single = 2320, cb23Multi = 45600,
            hasQuickSync = true, hasAvx512 = false, isHedtQuadChannel = false, isUltraQuietDaw = true, tierRank = 8,
        ),
        CpuCandidate(
            id = "9950x", displayName = "Ryzen 9 9950X", searchQuery = "9950X",
            specsSummary = "16Ç/32İ Zen 5 • 5.7 GHz • 64 MB L3 • Tam 512-bit AVX-512",
            cb23Single = 2340, cb23Multi = 43200,
            hasQuickSync = false, hasAvx512 = true, isHedtQuadChannel = false, isUltraQuietDaw = true, tierRank = 8,
        ),
        CpuCandidate(
            id = "tr_9970x", displayName = "Ryzen Threadripper 7970X / 9970X", searchQuery = "Threadripper",
            specsSummary = "32Ç/64İ HEDT • 5.3+ GHz • 128 MB L3 • 4-Kanal DDR5 ECC & 48+ PCIe 5.0",
            cb23Single = 2190, cb23Multi = 69500,
            hasQuickSync = false, hasAvx512 = true, isHedtQuadChannel = true, isUltraQuietDaw = false, tierRank = 10,
        ),
    )

    private val GPU_POOL: List<GpuCandidate> = listOf(
        GpuCandidate(
            id = "silent_audio", displayName = "Dahili GPU / Sessiz RTX 4060 (0 dB)", searchQuery = "RTX 4060",
            specsSummary = "8 GB GDDR6 • 0 dB Pasif Fan (Ses Kaydında Sıfır Gürültü, 4K Ekran)",
            vramGb = 8, bandwidthGbs = 272, optixSpm = 2850, tensorIndex = 28.0,
            hasDualOrTripleNvenc = false, has422HardwareDecode = false, isSilentEntry = true, tierRank = 0,
        ),
        GpuCandidate(
            id = "rtx_4060", displayName = "GeForce RTX 4060 8GB", searchQuery = "RTX 4060",
            specsSummary = "8 GB GDDR6 • 3072 CUDA • 272 GB/s • AV1 NVENC",
            vramGb = 8, bandwidthGbs = 272, optixSpm = 2850, tensorIndex = 28.0,
            hasDualOrTripleNvenc = false, has422HardwareDecode = false, isSilentEntry = true, tierRank = 1,
        ),
        GpuCandidate(
            id = "rtx_5060_ti_16", displayName = "GeForce RTX 5060 Ti 16GB", searchQuery = "RTX 5060 Ti",
            specsSummary = "16 GB GDDR7 • 4608 CUDA • 448 GB/s • 10-bit 4:2:2 NVDEC",
            vramGb = 16, bandwidthGbs = 448, optixSpm = 4450, tensorIndex = 46.0,
            hasDualOrTripleNvenc = false, has422HardwareDecode = true, isSilentEntry = false, tierRank = 3,
        ),
        GpuCandidate(
            id = "rtx_5070", displayName = "GeForce RTX 5070 12GB", searchQuery = "RTX 5070",
            specsSummary = "12 GB GDDR7 • 6144 CUDA • 672 GB/s • Blackwell RT & 4:2:2 NVDEC",
            vramGb = 12, bandwidthGbs = 672, optixSpm = 6100, tensorIndex = 62.0,
            hasDualOrTripleNvenc = false, has422HardwareDecode = true, isSilentEntry = false, tierRank = 4,
        ),
        GpuCandidate(
            id = "rtx_5070_ti", displayName = "GeForce RTX 5070 Ti 16GB", searchQuery = "RTX 5070 Ti",
            specsSummary = "16 GB GDDR7 • 8960 CUDA • 896 GB/s • Çift NVENC & 4:2:2 NVDEC",
            vramGb = 16, bandwidthGbs = 896, optixSpm = 7950, tensorIndex = 79.0,
            hasDualOrTripleNvenc = true, has422HardwareDecode = true, isSilentEntry = false, tierRank = 6,
        ),
        GpuCandidate(
            id = "rtx_5080", displayName = "GeForce RTX 5080 16GB", searchQuery = "RTX 5080",
            specsSummary = "16 GB GDDR7 • 10752 CUDA • 960 GB/s • Çift NVENC & 4:2:2 NVDEC",
            vramGb = 16, bandwidthGbs = 960, optixSpm = 9250, tensorIndex = 92.0,
            hasDualOrTripleNvenc = true, has422HardwareDecode = true, isSilentEntry = false, tierRank = 7,
        ),
        GpuCandidate(
            id = "rtx_4090", displayName = "GeForce RTX 4090 24GB", searchQuery = "RTX 4090",
            specsSummary = "24 GB GDDR6X • 16384 CUDA • 1008 GB/s • Çift NVENC",
            vramGb = 24, bandwidthGbs = 1008, optixSpm = 11400, tensorIndex = 100.0,
            hasDualOrTripleNvenc = true, has422HardwareDecode = false, isSilentEntry = false, tierRank = 8,
        ),
        GpuCandidate(
            id = "rtx_5090", displayName = "GeForce RTX 5090 32GB", searchQuery = "RTX 5090",
            specsSummary = "32 GB GDDR7 • 21760 CUDA • 1792 GB/s • Üçlü NVENC & 4:2:2",
            vramGb = 32, bandwidthGbs = 1792, optixSpm = 15800, tensorIndex = 152.0,
            hasDualOrTripleNvenc = true, has422HardwareDecode = true, isSilentEntry = false, tierRank = 9,
        ),
        GpuCandidate(
            id = "rtx_6000_ada", displayName = "NVIDIA RTX 6000 Ada 48GB (ECC)", searchQuery = "RTX 6000 Ada",
            specsSummary = "48 GB GDDR6 ECC • 18176 CUDA • 960 GB/s • Tam 70B Q4 LLM & Stüdyo",
            vramGb = 48, bandwidthGbs = 960, optixSpm = 11800, tensorIndex = 108.0,
            hasDualOrTripleNvenc = true, has422HardwareDecode = false, isSilentEntry = false, tierRank = 10,
        ),
    )

    // =========================================================================
    // 2. HER UYGULAMAYA ÖZEL GERÇEK MATEMATİKSEL İŞ YÜKÜ PROFİLİ (35 Uygulama)
    // =========================================================================

    private data class AppMathProfile(
        val weights: WorkloadWeights, // cpuSingle + cpuMulti + gpuCompute + memoryIo == 100
        val baseVramGb: Double,       // 1080p / Standard baseline VRAM (GB)
        val baseRamGb: Double,        // 1080p / Standard baseline RAM (GB)
        val vramScale: Double,        // Resolution growth exponent for VRAM
        val ramScale: Double,         // Resolution growth exponent for RAM
        val prefersQuickSync: Boolean = false,
        val requiresCudaOptix: Boolean = false,
        val prefersAvx512: Boolean = false,
        val prefersHedtAtStudio: Boolean = false,
        val isAudioMinimalGpu: Boolean = false,
        val metricKind: WorkstationMetricKind,
        val pugetBaseMultiplier: Double = 1.0,
    )

    private val APP_MATH_PROFILES: Map<String, AppMathProfile> = mapOf(
        // 1. Video & Post-Production
        "premiere_pro"     to AppMathProfile(WorkloadWeights(25, 30, 30, 15), 6.0, 24.0, 1.35, 1.35, prefersQuickSync = true, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 1.08),
        "davinci_resolve"  to AppMathProfile(WorkloadWeights(15, 20, 50, 15), 8.0, 24.0, 1.48, 1.32, requiresCudaOptix = true, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 1.02),
        "after_effects"    to AppMathProfile(WorkloadWeights(30, 35, 10, 25), 6.0, 36.0, 1.25, 1.55, prefersQuickSync = false, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 0.96),
        "foundry_nuke"     to AppMathProfile(WorkloadWeights(20, 40, 15, 25), 8.0, 48.0, 1.30, 1.50, prefersHedtAtStudio = true, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 0.94),
        "fusion_studio"    to AppMathProfile(WorkloadWeights(20, 25, 40, 15), 8.0, 32.0, 1.40, 1.35, requiresCudaOptix = true, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 0.98),
        "avid_composer"    to AppMathProfile(WorkloadWeights(30, 35, 15, 20), 6.0, 32.0, 1.25, 1.35, prefersQuickSync = true, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 1.00),

        // 2. 3D Modeling, CGI & VFX
        "blender"          to AppMathProfile(WorkloadWeights(15, 15, 60, 10), 8.0, 24.0, 1.45, 1.32, requiresCudaOptix = true, metricKind = WorkstationMetricKind.BLENDER_OPTIX_SPM),
        "cinema4d"         to AppMathProfile(WorkloadWeights(25, 15, 50, 10), 8.0, 24.0, 1.42, 1.30, requiresCudaOptix = true, metricKind = WorkstationMetricKind.BLENDER_OPTIX_SPM),
        "maya"             to AppMathProfile(WorkloadWeights(35, 20, 35, 10), 8.0, 32.0, 1.38, 1.35, requiresCudaOptix = true, metricKind = WorkstationMetricKind.BLENDER_OPTIX_SPM),
        "houdini"          to AppMathProfile(WorkloadWeights(15, 50, 15, 20), 8.0, 48.0, 1.30, 1.60, prefersAvx512 = true, prefersHedtAtStudio = true, metricKind = WorkstationMetricKind.CAD_IPC_INDEX),
        "zbrush"           to AppMathProfile(WorkloadWeights(50, 30,  5, 15), 4.0, 24.0, 1.10, 1.40, metricKind = WorkstationMetricKind.CAD_IPC_INDEX),
        "substance_painter" to AppMathProfile(WorkloadWeights(20, 15, 45, 20), 8.0, 24.0, 1.45, 1.35, requiresCudaOptix = true, metricKind = WorkstationMetricKind.BLENDER_OPTIX_SPM),

        // 3. Game Engines & ArchViz
        "unreal_engine"    to AppMathProfile(WorkloadWeights(15, 35, 35, 15), 8.0, 32.0, 1.45, 1.45, requiresCudaOptix = true, prefersHedtAtStudio = true, metricKind = WorkstationMetricKind.ENGINE_VIEWPORT_BUILD),
        "unity_engine"     to AppMathProfile(WorkloadWeights(25, 35, 25, 15), 6.0, 24.0, 1.30, 1.35, metricKind = WorkstationMetricKind.ENGINE_VIEWPORT_BUILD),
        "d5_render"        to AppMathProfile(WorkloadWeights(10, 10, 68, 12), 10.0, 24.0, 1.46, 1.30, requiresCudaOptix = true, metricKind = WorkstationMetricKind.BLENDER_OPTIX_SPM),
        "lumion"           to AppMathProfile(WorkloadWeights(15, 10, 63, 12), 8.0, 24.0, 1.45, 1.30, requiresCudaOptix = true, metricKind = WorkstationMetricKind.BLENDER_OPTIX_SPM),
        "chaos_vantage"    to AppMathProfile(WorkloadWeights(10, 10, 70, 10), 10.0, 24.0, 1.48, 1.28, requiresCudaOptix = true, metricKind = WorkstationMetricKind.BLENDER_OPTIX_SPM),
        "godot_engine"     to AppMathProfile(WorkloadWeights(30, 30, 25, 15), 4.0, 16.0, 1.20, 1.25, metricKind = WorkstationMetricKind.ENGINE_VIEWPORT_BUILD),

        // 4. CAD, Engineering & Simulation
        "solidworks"       to AppMathProfile(WorkloadWeights(50, 20, 15, 15), 6.0, 32.0, 1.25, 1.35, metricKind = WorkstationMetricKind.CAD_IPC_INDEX),
        "autocad"          to AppMathProfile(WorkloadWeights(60, 10, 15, 15), 4.0, 16.0, 1.20, 1.30, metricKind = WorkstationMetricKind.CAD_IPC_INDEX),
        "revit"            to AppMathProfile(WorkloadWeights(45, 20, 15, 20), 6.0, 32.0, 1.22, 1.45, metricKind = WorkstationMetricKind.CAD_IPC_INDEX),
        "fusion360"        to AppMathProfile(WorkloadWeights(45, 25, 15, 15), 6.0, 16.0, 1.20, 1.30, metricKind = WorkstationMetricKind.CAD_IPC_INDEX),
        "ansys"            to AppMathProfile(WorkloadWeights(15, 55, 10, 20), 6.0, 48.0, 1.20, 1.60, prefersAvx512 = true, prefersHedtAtStudio = true, metricKind = WorkstationMetricKind.CAD_IPC_INDEX),
        "matlab"           to AppMathProfile(WorkloadWeights(25, 40, 20, 15), 8.0, 32.0, 1.25, 1.45, prefersAvx512 = true, requiresCudaOptix = true, metricKind = WorkstationMetricKind.CAD_IPC_INDEX),

        // 5. Local AI & Data Science
        "stable_diffusion" to AppMathProfile(WorkloadWeights(10, 10, 65, 15), 10.0, 24.0, 1.48, 1.35, requiresCudaOptix = true, metricKind = WorkstationMetricKind.AI_IT_PER_SEC),
        "local_llm"        to AppMathProfile(WorkloadWeights(10, 15, 60, 15), 6.0, 32.0, 1.95, 1.45, requiresCudaOptix = true, metricKind = WorkstationMetricKind.LLM_TOKENS_PER_SEC),
        "pytorch_cuda"     to AppMathProfile(WorkloadWeights(10, 20, 58, 12), 14.0, 32.0, 1.55, 1.50, requiresCudaOptix = true, prefersHedtAtStudio = true, metricKind = WorkstationMetricKind.AI_IT_PER_SEC),
        "topaz_ai"         to AppMathProfile(WorkloadWeights(15, 15, 58, 12), 8.0, 24.0, 1.45, 1.30, requiresCudaOptix = true, metricKind = WorkstationMetricKind.AI_IT_PER_SEC),

        // 6. Graphic & Photo Editing
        "photoshop"        to AppMathProfile(WorkloadWeights(50, 20, 10, 20), 4.0, 24.0, 1.22, 1.45, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 1.05),
        "lightroom"        to AppMathProfile(WorkloadWeights(25, 35, 25, 15), 6.0, 24.0, 1.30, 1.38, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 1.00),
        "illustrator"      to AppMathProfile(WorkloadWeights(55, 15, 15, 15), 4.0, 16.0, 1.20, 1.30, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 0.98),
        "capture_one"      to AppMathProfile(WorkloadWeights(25, 30, 30, 15), 6.0, 32.0, 1.30, 1.35, metricKind = WorkstationMetricKind.PUGET_SCORE, pugetBaseMultiplier = 1.01),

        // 7. Audio Production & DAW (GPU strictly minimal / 0dB silent!)
        "fl_studio"        to AppMathProfile(WorkloadWeights(50, 30,  5, 15), 4.0, 24.0, 1.00, 1.35, isAudioMinimalGpu = true, metricKind = WorkstationMetricKind.DAW_TRACK_HEADROOM),
        "ableton_live"     to AppMathProfile(WorkloadWeights(45, 35,  5, 15), 4.0, 24.0, 1.00, 1.40, isAudioMinimalGpu = true, metricKind = WorkstationMetricKind.DAW_TRACK_HEADROOM),
        "cubase_pro_tools" to AppMathProfile(WorkloadWeights(30, 45,  5, 20), 4.0, 40.0, 1.00, 1.55, isAudioMinimalGpu = true, metricKind = WorkstationMetricKind.DAW_TRACK_HEADROOM),
    )

    // =========================================================================
    // 3. DESTEKLENEN 35 PROFESYONEL UYGULAMA LİSTESİ (Kategorik & Doğru Kaynaklı)
    // =========================================================================

    val supportedApps: List<WorkstationApp> = listOf(
        // 1. VIDEO & POST-PRODÜKSİYON (6)
        WorkstationApp(
            id = "premiere_pro", name = "Adobe Premiere Pro 2026", shortName = "Premiere Pro",
            category = WorkstationCategory.VIDEO_EDITING, iconName = "video_camera",
            primaryBottleneckTr = "Intel QuickSync / Blackwell 10-bit 4:2:2 Donanımsal Kod Çözme & VRAM",
            primaryBottleneckEn = "Intel QuickSync / Blackwell 10-bit 4:2:2 Hardware Decode & VRAM",
            officialBenchmarkSource = "PugetBench for Premiere Pro (Puget Systems 2025/2026)",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "davinci_resolve", name = "DaVinci Resolve Studio 19/20", shortName = "DaVinci Resolve",
            category = WorkstationCategory.VIDEO_EDITING, iconName = "palette",
            primaryBottleneckTr = "GPU VRAM Kapasitesi, Bellek Bant Genişliği & Çift/Üçlü NVENC",
            primaryBottleneckEn = "GPU VRAM Capacity, Memory Bandwidth & Multi-NVENC",
            officialBenchmarkSource = "PugetBench for DaVinci Resolve Studio & Blackmagic Design Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "after_effects", name = "Adobe After Effects 2026", shortName = "After Effects",
            category = WorkstationCategory.VIDEO_EDITING, iconName = "auto_awesome_motion",
            primaryBottleneckTr = "RAM Kapasitesi (MFR: Çekirdek Başına 4–8 GB) & Tek/Çoklu Çekirdek CPU",
            primaryBottleneckEn = "RAM Capacity (MFR: 4–8 GB per Core) & CPU Single/Multi-Thread",
            officialBenchmarkSource = "PugetBench for After Effects (Multi-Frame Rendering Suite)",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "foundry_nuke", name = "Foundry Nuke 15", shortName = "Foundry Nuke",
            category = WorkstationCategory.VIDEO_EDITING, iconName = "layers",
            primaryBottleneckTr = "Çok Kanallı EXR İçin PCIe 4.0/5.0 NVMe I/O & 64–128 GB RAM",
            primaryBottleneckEn = "PCIe Gen4/Gen5 NVMe I/O (Multi-Channel EXR) & 64–128 GB RAM",
            officialBenchmarkSource = "The Foundry Nuke Workstation Matrix & SPECworkstation 4.0",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "fusion_studio", name = "Blackmagic Fusion Studio 19", shortName = "Fusion Studio",
            category = WorkstationCategory.VIDEO_EDITING, iconName = "scatter_plot",
            primaryBottleneckTr = "3D Nod Kompozitleme İçin GPU Bellek Bant Genişliği & VRAM",
            primaryBottleneckEn = "GPU Memory Bandwidth & VRAM for 3D Node Compositing",
            officialBenchmarkSource = "Blackmagic Design Official Fusion Studio Performance Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "avid_composer", name = "Avid Media Composer 2026", shortName = "Avid Composer",
            category = WorkstationCategory.VIDEO_EDITING, iconName = "movie_creation",
            primaryBottleneckTr = "Broadcast DNxHR Codec İşleme & Intel QuickSync / DDR5 Bellek",
            primaryBottleneckEn = "Broadcast DNxHR Codec Processing & QuickSync / DDR5 Memory",
            officialBenchmarkSource = "Avid Media Composer Certified Workstation Guidelines",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),

        // 2. 3D MODELLEME, CGI & VFX (6)
        WorkstationApp(
            id = "blender", name = "Blender 4.x (Cycles / Eevee Next)", shortName = "Blender",
            category = WorkstationCategory.VFX_3D, iconName = "view_in_ar",
            primaryBottleneckTr = "NVIDIA OptiX Işın İzleme Çekirdekleri & Sahne VRAM Havuzu",
            primaryBottleneckEn = "NVIDIA OptiX Ray Tracing Cores & Scene VRAM Pool",
            officialBenchmarkSource = "Blender Open Data (opendata.blender.org) Cycles v4.x",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "cinema4d", name = "Cinema 4D (Redshift Engine)", shortName = "Cinema 4D",
            category = WorkstationCategory.VFX_3D, iconName = "3d_rotation",
            primaryBottleneckTr = "Redshift GPU OptiX/CUDA Gücü & Viewport Tek Çekirdek IPC",
            primaryBottleneckEn = "Redshift GPU OptiX/CUDA Compute & Single-Thread Viewport IPC",
            officialBenchmarkSource = "Maxon Cinebench & Redshift GPU Benchmark Suite",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "maya", name = "Autodesk Maya & 3ds Max (Arnold)", shortName = "Autodesk Maya / Max",
            category = WorkstationCategory.VFX_3D, iconName = "animation",
            primaryBottleneckTr = "Karakter Rigleme (Tek Çekirdek IPC) & Arnold GPU Hibrit Render",
            primaryBottleneckEn = "Character Rigging (Single-Core IPC) & Arnold GPU Hybrid Render",
            officialBenchmarkSource = "Autodesk Maya Hardware Certification & SPECviewperf",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "houdini", name = "SideFX Houdini 20 (FX Sim)", shortName = "SideFX Houdini",
            category = WorkstationCategory.VFX_3D, iconName = "tsunami",
            primaryBottleneckTr = "Akışkan/Pyro Simülasyonu İçin Yüksek CPU Çekirdeği, AVX-512 & 128 GB+ RAM",
            primaryBottleneckEn = "Massive Multi-Core CPU, AVX-512 & 128 GB+ RAM for Fluid/Pyro Dynamics",
            officialBenchmarkSource = "SideFX Official Hardware & VEX/FLIP Simulation Architecture Guide",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "zbrush", name = "Maxon ZBrush 2026", shortName = "Maxon ZBrush",
            category = WorkstationCategory.VFX_3D, iconName = "brush",
            primaryBottleneckTr = "100M+ Poligon Sculpting İçin CPU Tek Çekirdek IPC & Hızlı DDR5 RAM",
            primaryBottleneckEn = "CPU Single-Core IPC & Low-Latency DDR5 RAM for 100M+ Polygons",
            officialBenchmarkSource = "Maxon ZBrush Official Hardware Performance Guidelines",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "substance_painter", name = "Adobe Substance 3D Painter", shortName = "Substance Painter",
            category = WorkstationCategory.VFX_3D, iconName = "format_paint",
            primaryBottleneckTr = "4K/8K UDIM Doku Katmanları & DirectX 12 GPU VRAM Kapasitesi",
            primaryBottleneckEn = "4K/8K UDIM Texture Layering & DirectX 12 GPU VRAM Capacity",
            officialBenchmarkSource = "Adobe Substance 3D Official Workstation Guidelines",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),

        // 3. OYUN MOTORLARI & ARCHVIZ (6)
        WorkstationApp(
            id = "unreal_engine", name = "Unreal Engine 5.5", shortName = "Unreal Engine 5",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ, iconName = "sports_esports",
            primaryBottleneckTr = "Shader/C++ Derleme (Çoklu Çekirdek CPU) & Lumen/Nanite Donanım RT VRAM",
            primaryBottleneckEn = "Shader/C++ Compilation (Multi-Core CPU) & Lumen/Nanite RT VRAM",
            officialBenchmarkSource = "PugetBench for Unreal Engine & Epic Games Production Guidelines",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "unity_engine", name = "Unity 6 (URP / HDRP)", shortName = "Unity 6",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ, iconName = "developer_board",
            primaryBottleneckTr = "C# Burst / IL2CPP Derleme Hızı, Lightmap Baking & 32–64 GB RAM",
            primaryBottleneckEn = "C# Burst / IL2CPP Compilation Speed, Lightmap Baking & 32–64 GB RAM",
            officialBenchmarkSource = "Unity Technologies Official Development Workstation Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "d5_render", name = "D5 Render 2026 (RTX Engine)", shortName = "D5 Render",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ, iconName = "wb_sunny",
            primaryBottleneckTr = "Gerçek Zamanlı Donanımsal Işın İzleme (DLSS RR) & 16–32 GB VRAM",
            primaryBottleneckEn = "Real-Time Hardware Ray Tracing (DLSS RR) & 16–32 GB VRAM",
            officialBenchmarkSource = "D5 Render Official Hardware Benchmark Database",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "lumion", name = "Lumion 2024 / 2025", shortName = "Lumion",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ, iconName = "villa",
            primaryBottleneckTr = "GPU Raster/RT Hızı & Büyük Peyzaj/Mimari Sahneler İçin VRAM",
            primaryBottleneckEn = "GPU Raster/RT Throughput & VRAM for Large Architectural Scenes",
            officialBenchmarkSource = "Act-3D Lumion Official System Hardware Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "chaos_vantage", name = "Chaos Vantage / Enscape", shortName = "Chaos Vantage",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ, iconName = "apartment",
            primaryBottleneckTr = "Canlı CAD/BIM Senkronizasyonu & Saf RTX Donanım Işın İzleme",
            primaryBottleneckEn = "Live CAD/BIM Sync & Pure RTX Hardware Ray Tracing",
            officialBenchmarkSource = "Chaos Group Vantage Official Hardware Whitepaper",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "godot_engine", name = "Godot Engine 4.3+", shortName = "Godot 4",
            category = WorkstationCategory.GAME_DEV_ARCHVIZ, iconName = "smart_toy",
            primaryBottleneckTr = "Vulkan Forward+ Viewport & Hızlı C++/GDScript Derleme",
            primaryBottleneckEn = "Vulkan Forward+ Viewport & Fast C++/GDScript Builds",
            officialBenchmarkSource = "Godot Engine Vulkan Forward+ Hardware Guidelines",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),

        // 4. MÜHENDİSLİK, CAD & SİMÜLASYON (6)
        WorkstationApp(
            id = "solidworks", name = "Dassault Systèmes SolidWorks 2026", shortName = "SolidWorks",
            category = WorkstationCategory.CAD_ENGINEERING, iconName = "precision_manufacturing",
            primaryBottleneckTr = "Büyük Montaj Yeniden Oluşturmada CPU Tek Çekirdek IPC & RealView GPU",
            primaryBottleneckEn = "CPU Single-Core IPC for Assembly Rebuilds & RealView GPU",
            officialBenchmarkSource = "SPECviewperf SolidWorks Viewset & Puget Systems CAD Suite",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "autocad", name = "Autodesk AutoCAD 2026", shortName = "AutoCAD",
            category = WorkstationCategory.CAD_ENGINEERING, iconName = "architecture",
            primaryBottleneckTr = "Tek Çekirdek IPC (CB23 Single 2200+) & 32 GB Düşük Gecikmeli RAM",
            primaryBottleneckEn = "Single-Core IPC (CB23 Single 2200+) & 32 GB Low-Latency RAM",
            officialBenchmarkSource = "Autodesk AutoCAD Official Requirements & Puget Systems CAD",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "revit", name = "Autodesk Revit 2026 (BIM)", shortName = "Autodesk Revit",
            category = WorkstationCategory.CAD_ENGINEERING, iconName = "domain",
            primaryBottleneckTr = "BIM Model Boyutu (32–64 GB RAM) & Tek Çekirdek IPC",
            primaryBottleneckEn = "BIM Model Footprint (32–64 GB RAM) & Single-Core IPC",
            officialBenchmarkSource = "Autodesk Revit Model Performance Technical Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "fusion360", name = "Autodesk Fusion 360", shortName = "Fusion 360",
            category = WorkstationCategory.CAD_ENGINEERING, iconName = "handyman",
            primaryBottleneckTr = "Yerel CAM Takım Yolu Hesaplama (Tek/Çoklu CPU) & Viewport GPU",
            primaryBottleneckEn = "Local CAM Toolpath Calculation & Viewport GPU",
            officialBenchmarkSource = "Autodesk Fusion 360 Official Hardware Performance Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "ansys", name = "ANSYS Fluent & Mechanical (FEA/CFD)", shortName = "ANSYS Fluent",
            category = WorkstationCategory.CAD_ENGINEERING, iconName = "air",
            primaryBottleneckTr = "Çoklu CPU Çekirdeği, Tam AVX-512 & 4-Kanal Bellek Bant Genişliği",
            primaryBottleneckEn = "Multi-Core Scaling, Full AVX-512 & Quad-Channel Memory Bandwidth",
            officialBenchmarkSource = "ANSYS Official HPC Hardware & SPECworkstation 4.0 WPC",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "matlab", name = "MathWorks MATLAB & Simulink", shortName = "MATLAB",
            category = WorkstationCategory.CAD_ENGINEERING, iconName = "functions",
            primaryBottleneckTr = "BLAS/LAPACK Matris Çözücüleri (AVX-512) & CUDA Paralel Havuz",
            primaryBottleneckEn = "BLAS/LAPACK Matrix Solvers (AVX-512) & CUDA Parallel Pool",
            officialBenchmarkSource = "MathWorks MATLAB Bench Suite & Parallel Computing Guide",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),

        // 5. YAPAY ZEKA & VERİ BİLİMİ (4)
        WorkstationApp(
            id = "stable_diffusion", name = "Stable Diffusion & ComfyUI (Flux.1 / SDXL)", shortName = "ComfyUI / SD",
            category = WorkstationCategory.LOCAL_AI, iconName = "auto_awesome",
            primaryBottleneckTr = "GPU VRAM (Flux.1 FP8/BF16 İçin 16–32 GB) & TensorRT FP16/FP4",
            primaryBottleneckEn = "GPU VRAM (16–32 GB for Flux.1 FP8/BF16) & TensorRT FP16/FP4",
            officialBenchmarkSource = "Puget Systems Generative AI & ComfyUI TensorRT Benchmark",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "local_llm", name = "Ollama & LM Studio (Llama 3.3 / DeepSeek R1)", shortName = "Ollama / LLM",
            category = WorkstationCategory.LOCAL_AI, iconName = "psychology",
            primaryBottleneckTr = "GPU Bellek Bant Genişliği (GB/s) & VRAM (32B Q4: 24–32 GB, 70B Q4: 48 GB)",
            primaryBottleneckEn = "GPU Memory Bandwidth (GB/s) & VRAM (32B Q4: 24–32 GB, 70B Q4: 48 GB)",
            officialBenchmarkSource = "llama.cpp / Ollama Memory-Bandwidth Token Physics Model",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "pytorch_cuda", name = "PyTorch & TensorFlow (Model Eğitimi)", shortName = "PyTorch CUDA",
            category = WorkstationCategory.LOCAL_AI, iconName = "hub",
            primaryBottleneckTr = "FP16/BF16 Tensör Gücü, 24–48 GB VRAM & PCIe Gen5 x16",
            primaryBottleneckEn = "FP16/BF16 Tensor Compute, 24–48 GB VRAM & PCIe Gen5 x16",
            officialBenchmarkSource = "MLPerf Inference/Training & Lambda Labs PyTorch Benchmarks",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.HEAVY_SIMULATION),
        ),
        WorkstationApp(
            id = "topaz_ai", name = "Topaz Video AI & Photo AI", shortName = "Topaz Video AI",
            category = WorkstationCategory.LOCAL_AI, iconName = "high_quality",
            primaryBottleneckTr = "4K/8K AI Upscale (Proteus/Iris) İçin TensorRT FP16 & Çift NVENC",
            primaryBottleneckEn = "TensorRT FP16 Throughput & Multi-NVENC for 4K/8K AI Upscaling",
            officialBenchmarkSource = "Topaz Labs Community Hardware Benchmark Database",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.FINAL_RENDER, WorkloadPhase.HEAVY_SIMULATION),
        ),

        // 6. GRAFİK & FOTOĞRAF DÜZENLEME (4)
        WorkstationApp(
            id = "photoshop", name = "Adobe Photoshop 2026", shortName = "Photoshop",
            category = WorkstationCategory.GRAPHIC_DESIGN, iconName = "brush",
            primaryBottleneckTr = "CPU Tek Çekirdek IPC Gücü, 32–64 GB RAM & NVMe Scratch Disk",
            primaryBottleneckEn = "CPU Single-Core IPC, 32–64 GB RAM & Dedicated NVMe Scratch Disk",
            officialBenchmarkSource = "PugetBench for Photoshop (Puget Systems 2025/2026)",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "lightroom", name = "Adobe Lightroom Classic 2026", shortName = "Lightroom Classic",
            category = WorkstationCategory.GRAPHIC_DESIGN, iconName = "camera_roll",
            primaryBottleneckTr = "Toplu RAW Export (Çoklu Çekirdek CPU) & AI Denoise/Maskeleme (GPU)",
            primaryBottleneckEn = "Batch RAW Export (Multi-Core CPU) & AI Denoise/Masking (GPU)",
            officialBenchmarkSource = "PugetBench for Lightroom Classic (Puget Systems)",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "illustrator", name = "Adobe Illustrator 2026", shortName = "Illustrator",
            category = WorkstationCategory.GRAPHIC_DESIGN, iconName = "draw",
            primaryBottleneckTr = "Yüksek Tek Çekirdek IPC & GPU Hızlandırmalı Vektör Kanvas",
            primaryBottleneckEn = "High Single-Core IPC & GPU-Accelerated Vector Canvas",
            officialBenchmarkSource = "Adobe Illustrator Official Performance Guidelines",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "capture_one", name = "Capture One Pro 24", shortName = "Capture One",
            category = WorkstationCategory.GRAPHIC_DESIGN, iconName = "photo_camera",
            primaryBottleneckTr = "100MP Canlı Tethered Çekim, OpenCL Renk Motoru & Gen4 NVMe",
            primaryBottleneckEn = "100MP Live Tethering, OpenCL Debayer Engine & PCIe Gen4 NVMe",
            officialBenchmarkSource = "Capture One Official Studio Hardware Benchmark",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),

        // 7. MÜZİK PRODÜKSİYONU & SES (3)
        WorkstationApp(
            id = "fl_studio", name = "FL Studio 2026 / 24 (Image-Line)", shortName = "FL Studio",
            category = WorkstationCategory.AUDIO_PRODUCTION, iconName = "graphic_eq",
            primaryBottleneckTr = "Düşük DPC/ASIO Gecikmesi & Tek Çekirdek IPC (GPU Önemsizdir)",
            primaryBottleneckEn = "Low DPC/ASIO Latency & Single-Core IPC (GPU Is Minimal)",
            officialBenchmarkSource = "Image-Line FL Studio CPU Architecture & DAWBench DSP",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "ableton_live", name = "Ableton Live 12 Suite", shortName = "Ableton Live",
            category = WorkstationCategory.AUDIO_PRODUCTION, iconName = "music_note",
            primaryBottleneckTr = "Ultra Düşük ASIO Buffer (64 smp) İçin CPU IPC, 32–64 GB RAM & Sessiz Sistem",
            primaryBottleneckEn = "CPU IPC for 64-Sample ASIO Buffer, 32–64 GB RAM & Silent Cooling",
            officialBenchmarkSource = "Ableton Live Audio Engine & DAWBench VI/DSP Standards",
            supportedResolutions = listOf(WorkloadResolution.FHD_1080P, WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
        WorkstationApp(
            id = "cubase_pro_tools", name = "Steinberg Cubase 14 / Pro Tools", shortName = "Cubase / Pro Tools",
            category = WorkstationCategory.AUDIO_PRODUCTION, iconName = "piano",
            primaryBottleneckTr = "100+ Orkestral Kontakt/VST Kanalı İçin 64–128 GB RAM, Çoklu CPU & NVMe",
            primaryBottleneckEn = "64–128 GB RAM, Multi-Core CPU & Gen4 Sample NVMe for 100+ VSTs",
            officialBenchmarkSource = "Steinberg Audio Engine & DAWBench Multi-Track Standards",
            supportedResolutions = listOf(WorkloadResolution.QHD_2K, WorkloadResolution.UHD_4K, WorkloadResolution.RAW_8K),
            supportedPhases = listOf(WorkloadPhase.REALTIME_PREVIEW, WorkloadPhase.FINAL_RENDER),
        ),
    )

    // =========================================================================
    // 4. PARAMETRİK HESAPLAMA & GERÇEK BENCHMARK ÇIKTI MOTORU
    // =========================================================================

    fun calculateRequirements(
        appId: String,
        resolution: WorkloadResolution,
        phase: WorkloadPhase,
    ): WorkstationRequirementProfile {
        val app = supportedApps.find { it.id == appId } ?: supportedApps.first()
        val math = APP_MATH_PROFILES[app.id] ?: APP_MATH_PROFILES.getValue("premiere_pro")

        val resIdx = when (resolution) {
            WorkloadResolution.FHD_1080P -> 0.72
            WorkloadResolution.QHD_2K -> 1.00
            WorkloadResolution.UHD_4K -> 1.42
            WorkloadResolution.RAW_8K -> 2.05
        }
        val phaseCpuMultiMult = when (phase) {
            WorkloadPhase.REALTIME_PREVIEW -> 0.92
            WorkloadPhase.FINAL_RENDER -> 1.24
            WorkloadPhase.HEAVY_SIMULATION -> 1.38
        }
        val phaseGpuMult = when (phase) {
            WorkloadPhase.REALTIME_PREVIEW -> 0.92
            WorkloadPhase.FINAL_RENDER -> 1.22
            WorkloadPhase.HEAVY_SIMULATION -> 1.35
        }
        val phaseMemMult = when (phase) {
            WorkloadPhase.REALTIME_PREVIEW -> 0.95
            WorkloadPhase.FINAL_RENDER -> 1.12
            WorkloadPhase.HEAVY_SIMULATION -> 1.32
        }

        val minTier = buildTierRecommendation(
            app = app, math = math, tier = HardwareTier.MINIMUM,
            resolution = resolution, phase = phase,
            resIdx = resIdx, phaseCpuMultiMult = phaseCpuMultiMult,
            phaseGpuMult = phaseGpuMult, phaseMemMult = phaseMemMult,
            tierScale = 0.72,
        )
        val proTier = buildTierRecommendation(
            app = app, math = math, tier = HardwareTier.RECOMMENDED_PRO,
            resolution = resolution, phase = phase,
            resIdx = resIdx, phaseCpuMultiMult = phaseCpuMultiMult,
            phaseGpuMult = phaseGpuMult, phaseMemMult = phaseMemMult,
            tierScale = 1.00,
        )
        val studioTier = buildTierRecommendation(
            app = app, math = math, tier = HardwareTier.STUDIO_EXTREME,
            resolution = resolution, phase = phase,
            resIdx = resIdx, phaseCpuMultiMult = phaseCpuMultiMult,
            phaseGpuMult = phaseGpuMult, phaseMemMult = phaseMemMult,
            tierScale = 1.45,
        )

        // Honest, app-specific source citation (NEVER falsely appends SPECworkstation to DAW/Godot/ComfyUI!)
        val citation = "Referans Kaynak: ${app.officialBenchmarkSource} · Katalog Kalibrasyon Motoru v2."

        return WorkstationRequirementProfile(
            app = app,
            resolution = resolution,
            phase = phase,
            workloadWeights = math.weights,
            minimumTier = minTier,
            recommendedTier = proTier,
            studioTier = studioTier,
            dataCitation = citation,
            keyTechnicalTakeawayTr = takeawayTr(app.id),
            keyTechnicalTakeawayEn = takeawayEn(app.id),
        )
    }

    private fun buildTierRecommendation(
        app: WorkstationApp,
        math: AppMathProfile,
        tier: HardwareTier,
        resolution: WorkloadResolution,
        phase: WorkloadPhase,
        resIdx: Double,
        phaseCpuMultiMult: Double,
        phaseGpuMult: Double,
        phaseMemMult: Double,
        tierScale: Double,
    ): HardwareRecommendation {
        // 1. Physically compute VRAM requirement (special exact model-weight physics for Local LLM!)
        val rawVramGb = if (app.id == "local_llm") {
            val llmWeightsGb = llmModelWeightsGb(resolution)
            when (tier) {
                HardwareTier.MINIMUM -> if (resolution == WorkloadResolution.RAW_8K) 32.0 else (llmWeightsGb * 1.15)
                HardwareTier.RECOMMENDED_PRO -> (llmWeightsGb * 1.20)
                HardwareTier.STUDIO_EXTREME -> max(llmWeightsGb * 1.35, if (resolution == WorkloadResolution.RAW_8K) 48.0 else 32.0)
            }
        } else if (math.isAudioMinimalGpu) {
            // Audio DAWs never waste budget on RTX 4080/5080 — quiet 4-8GB display GPU is plenty!
            if (tier == HardwareTier.STUDIO_EXTREME) 8.0 else 4.0
        } else {
            math.baseVramGb * resIdx.pow(math.vramScale) * phaseGpuMult * (0.78 + 0.25 * tierScale)
        }
        val reqVramGb = snapVramTier(rawVramGb, isAudio = math.isAudioMinimalGpu)

        // 2. Physically compute RAM requirement (snapped to real kit sizes: 16 / 32 / 48 / 64 / 96 / 128 / 256 GB)
        val rawRamGb = math.baseRamGb * resIdx.pow(math.ramScale) * phaseMemMult * tierScale
        val reqRamGb = snapRamKit(rawRamGb, minKit = if (tier == HardwareTier.STUDIO_EXTREME) 64 else 16)

        // 3. Select optimal CPU from calibrated 2025/2026 pool based on app weights + tier
        val cpu = selectOptimalCpu(app, math, tier, resIdx, phaseCpuMultiMult)

        // 4. Select optimal GPU from calibrated 2025/2026 pool based on VRAM floor + GPU compute demand
        val gpu = selectOptimalGpu(app, math, tier, reqVramGb, resIdx, phaseGpuMult)

        // 5. Compute REAL industry benchmark output metric from selected CPU + GPU specs!
        val (metricTr, metricEn, numericVal, adequacyPct) = computeBenchmarkOutput(
            app = app, math = math, cpu = cpu, gpu = gpu,
            resolution = resolution, phase = phase, reqRamGb = reqRamGb,
        )

        val ramSpec = when {
            cpu.isHedtQuadChannel -> "DDR5 6000 MT/s ECC RDIMM (4-Kanal)"
            reqRamGb >= 128 -> "DDR5 5600–6000 MT/s (2×64 GB veya 4×32 GB Kit)"
            reqRamGb >= 64 -> "DDR5 6000 MT/s CL30 (2×32 GB Dual Channel)"
            reqRamGb >= 48 -> "DDR5 6000 MT/s CL30 (2×24 GB Dual Channel)"
            reqRamGb >= 32 -> "DDR5 6000 MT/s CL30 (2×16 GB Dual Channel)"
            else -> "DDR5 5600–6000 MT/s (2×8 GB veya 2×16 GB)"
        }

        val storageSpec = when {
            tier == HardwareTier.STUDIO_EXTREME && (math.weights.memoryIoPct >= 20 || resIdx >= 1.42) ->
                "PCIe 5.0 NVMe (12.000+ MB/s) + Ayrı 4 TB Gen4 Scratch/Cache SSD"
            math.isAudioMinimalGpu && tier != HardwareTier.MINIMUM ->
                "2× PCIe 4.0 NVMe (OS/DAW + Ayrı 2–4 TB Kontakt/VST Sample Diski)"
            resIdx >= 1.42 || tier != HardwareTier.MINIMUM ->
                "PCIe 4.0 NVMe SSD (7.000+ MB/s Okuma/Yazma)"
            else ->
                "PCIe 4.0 M.2 NVMe SSD (5.000 MB/s)"
        }

        val (noteTr, noteEn) = buildExperienceNotes(
            app = app, math = math, tier = tier, resolution = resolution, phase = phase,
            cpu = cpu, gpu = gpu, reqRamGb = reqRamGb, metricTr = metricTr, metricEn = metricEn,
        )

        return HardwareRecommendation(
            tier = tier,
            targetCpuChipset = cpu.displayName,
            targetCpuSearchQuery = cpu.searchQuery,
            cpuSpecsSummary = "${cpu.specsSummary} · CB23: ${cpu.cb23Single} / ${cpu.cb23Multi}",
            targetGpuChipset = gpu.displayName,
            targetGpuSearchQuery = gpu.searchQuery,
            gpuSpecsSummary = gpu.specsSummary,
            targetRamCapacityGb = reqRamGb,
            targetRamTypeAndSpeed = ramSpec,
            targetStorageType = storageSpec,
            vramRequirementGb = max(reqVramGb, gpu.vramGb.coerceAtMost(reqVramGb)),
            estimatedExperienceNoteTr = noteTr,
            estimatedExperienceNoteEn = noteEn,
            calculatedMetricBadgeTr = metricTr,
            calculatedMetricBadgeEn = metricEn,
            calculatedNumericMetric = numericVal,
            benchmarkConfidenceScore = adequacyPct,
        )
    }

    private fun selectOptimalCpu(
        app: WorkstationApp,
        math: AppMathProfile,
        tier: HardwareTier,
        resIdx: Double,
        phaseCpuMultiMult: Double,
    ): CpuCandidate {
        // HEDT Threadripper for extreme multi-core studio workloads (Houdini, ANSYS, Nuke, UE5, PyTorch at 4K/8K Studio)
        if (tier == HardwareTier.STUDIO_EXTREME && math.prefersHedtAtStudio &&
            (resIdx >= 1.42 || phaseCpuMultiMult >= 1.30)
        ) {
            return CPU_POOL.first { it.id == "tr_9970x" }
        }

        val cpuIntensity = ((math.weights.cpuSinglePct * 0.9 + math.weights.cpuMultiPct * 1.15) / 100.0) *
            (0.65 + 0.35 * resIdx) * (0.75 + 0.25 * phaseCpuMultiMult)

        val targetRank = when (tier) {
            HardwareTier.MINIMUM -> when {
                cpuIntensity >= 1.05 -> 5
                cpuIntensity >= 0.72 -> 3
                cpuIntensity >= 0.50 -> 2
                else -> 1
            }
            HardwareTier.RECOMMENDED_PRO -> when {
                cpuIntensity >= 1.05 -> 8
                cpuIntensity >= 0.65 -> 6
                else -> 5
            }
            HardwareTier.STUDIO_EXTREME -> 8
        }

        val candidates = CPU_POOL.filter { !it.isHedtQuadChannel }
        return candidates.maxBy { c ->
            val rankFit = -kotlin.math.abs(c.tierRank - targetRank) * 2.4
            val qsBonus = if (math.prefersQuickSync && c.hasQuickSync) 3.2 else 0.0
            val avxBonus = if (math.prefersAvx512 && c.hasAvx512) 2.6 else 0.0
            val dawBonus = if (math.isAudioMinimalGpu && c.isUltraQuietDaw) 1.5 else 0.0
            val singleBonus = (c.cb23Single / 2340.0) * (math.weights.cpuSinglePct / 25.0)
            val multiBonus = (c.cb23Multi / 45600.0) * (math.weights.cpuMultiPct / 25.0)
            rankFit + qsBonus + avxBonus + dawBonus + singleBonus + multiBonus
        }
    }

    private fun selectOptimalGpu(
        app: WorkstationApp,
        math: AppMathProfile,
        tier: HardwareTier,
        reqVramGb: Int,
        resIdx: Double,
        phaseGpuMult: Double,
    ): GpuCandidate {
        // Audio production NEVER needs an RTX 4080/5080 — always keep GPU quiet and lean!
        if (math.isAudioMinimalGpu) {
            return GPU_POOL.first { it.id == "silent_audio" }
        }

        // Exact 70B Q4 LLM VRAM physics: requires 48 GB RTX 6000 Ada for full in-VRAM inference!
        if (app.id == "local_llm" && reqVramGb >= 40) {
            return GPU_POOL.first { it.id == "rtx_6000_ada" }
        }
        if (app.id == "pytorch_cuda" && tier == HardwareTier.STUDIO_EXTREME && resIdx >= 1.9) {
            return GPU_POOL.first { it.id == "rtx_6000_ada" }
        }

        val gpuDemand = (math.weights.gpuComputePct / 50.0) * resIdx * phaseGpuMult
        val targetRank = when (tier) {
            HardwareTier.MINIMUM -> when {
                gpuDemand >= 1.9 -> 6
                gpuDemand >= 1.2 -> 4
                gpuDemand >= 0.7 -> 3
                else -> 1
            }
            HardwareTier.RECOMMENDED_PRO -> when {
                gpuDemand >= 2.2 -> 8
                gpuDemand >= 1.5 -> 7
                gpuDemand >= 0.9 -> 6
                gpuDemand >= 0.45 -> 4
                else -> 3
            }
            HardwareTier.STUDIO_EXTREME -> when {
                gpuDemand >= 1.1 -> 9
                gpuDemand >= 0.5 -> 7
                else -> 6
            }
        }

        // Filter candidates meeting VRAM requirement (excluding workstation 48GB card unless VRAM > 32GB)
        val eligible = GPU_POOL.filter { g ->
            !g.isSilentEntry &&
                g.vramGb >= reqVramGb &&
                (reqVramGb > 32 || g.id != "rtx_6000_ada")
        }.ifEmpty {
            listOf(GPU_POOL.maxBy { it.vramGb })
        }

        return eligible.maxBy { g ->
            val rankFit = -kotlin.math.abs(g.tierRank - targetRank) * 2.2
            val videoBonus = if (app.category == WorkstationCategory.VIDEO_EDITING && g.has422HardwareDecode) 1.8 else 0.0
            val nvencBonus = if (app.category == WorkstationCategory.VIDEO_EDITING && g.hasDualOrTripleNvenc) 1.0 else 0.0
            rankFit + videoBonus + nvencBonus
        }
    }

    private data class MetricResult(
        val badgeTr: String,
        val badgeEn: String,
        val numericValue: Double,
        val adequacyPct: Int,
    )

    private fun computeBenchmarkOutput(
        app: WorkstationApp,
        math: AppMathProfile,
        cpu: CpuCandidate,
        gpu: GpuCandidate,
        resolution: WorkloadResolution,
        phase: WorkloadPhase,
        reqRamGb: Int,
    ): MetricResult {
        val singleNorm = cpu.cb23Single / 2340.0
        val multiNorm = cpu.cb23Multi / 45600.0
        val gpuNorm = gpu.tensorIndex / 100.0 // RTX 4090 = 1.0, RTX 5090 = 1.52
        val resLoad = when (resolution) {
            WorkloadResolution.FHD_1080P -> 0.75
            WorkloadResolution.QHD_2K -> 1.00
            WorkloadResolution.UHD_4K -> 1.35
            WorkloadResolution.RAW_8K -> 1.80
        }

        return when (math.metricKind) {
            WorkstationMetricKind.PUGET_SCORE -> {
                val qsBoost = if (math.prefersQuickSync && (cpu.hasQuickSync || gpu.has422HardwareDecode)) 1.08 else 1.0
                val composite = (
                    singleNorm * math.weights.cpuSinglePct +
                        multiNorm * math.weights.cpuMultiPct +
                        min(gpuNorm, 1.45) * math.weights.gpuComputePct +
                        (min(reqRamGb, 128) / 64.0).coerceAtMost(1.25) * math.weights.memoryIoPct
                    ) / 100.0
                val score = (composite * 12400.0 * math.pugetBaseMultiplier * qsBoost / (0.85 + 0.15 * resLoad))
                    .roundToInt().coerceIn(3200, 18500)
                val adequacy = ((score / 13500.0) * 100.0).roundToInt().coerceIn(68, 99)
                MetricResult(
                    badgeTr = "PugetBench ~$score pts",
                    badgeEn = "PugetBench ~$score pts",
                    numericValue = score.toDouble(),
                    adequacyPct = adequacy,
                )
            }
            WorkstationMetricKind.BLENDER_OPTIX_SPM -> {
                val resMult = when (resolution) {
                    WorkloadResolution.FHD_1080P -> 1.08
                    WorkloadResolution.QHD_2K -> 1.00
                    WorkloadResolution.UHD_4K -> 0.94
                    WorkloadResolution.RAW_8K -> 0.86
                }
                val spm = (gpu.optixSpm * resMult).roundToInt()
                val adequacy = ((gpu.optixSpm / 14500.0) * 100.0).roundToInt().coerceIn(66, 99)
                MetricResult(
                    badgeTr = "OptiX ~$spm smp/dk",
                    badgeEn = "OptiX ~$spm smp/min",
                    numericValue = spm.toDouble(),
                    adequacyPct = adequacy,
                )
            }
            WorkstationMetricKind.LLM_TOKENS_PER_SEC -> {
                val weightsGb = llmModelWeightsGb(resolution)
                val modelLabel = llmModelLabel(resolution)
                // Memory-bandwidth bound autoregressive decoding physics: tok/s ≈ (BW * 0.78) / activeWeightsGb
                val fitsFullyInVram = gpu.vramGb >= (weightsGb * 1.08)
                val tokPerSec = if (fitsFullyInVram) {
                    ((gpu.bandwidthGbs * 0.78) / weightsGb)
                } else {
                    // Partial RAM offload drops speed sharply to PCIe/DDR5 memory bandwidth (~45-65 GB/s)
                    val vramFraction = (gpu.vramGb / (weightsGb * 1.12)).coerceIn(0.35, 0.85)
                    val effBw = gpu.bandwidthGbs * vramFraction * 0.25 + 65.0 * (1.0 - vramFraction)
                    (effBw / weightsGb).coerceAtLeast(3.5)
                }
                val rounded = (tokPerSec * 10.0).roundToInt() / 10.0
                val modeTag = if (fitsFullyInVram) "Tam VRAM" else "Hibrit RAM"
                val modeTagEn = if (fitsFullyInVram) "Full VRAM" else "Hybrid RAM"
                val adequacy = if (fitsFullyInVram) (78 + min(21, (rounded / 5.0).roundToInt())) else 62
                MetricResult(
                    badgeTr = "$modelLabel: ~$rounded tok/s ($modeTag)",
                    badgeEn = "$modelLabel: ~$rounded tok/s ($modeTagEn)",
                    numericValue = rounded,
                    adequacyPct = adequacy.coerceIn(60, 99),
                )
            }
            WorkstationMetricKind.AI_IT_PER_SEC -> {
                val baseSpeed = if (app.id == "topaz_ai") {
                    (gpu.tensorIndex * 0.22 / resLoad).coerceAtLeast(1.8)
                } else {
                    (gpu.tensorIndex * 0.085 / (0.65 + 0.35 * resLoad)).coerceAtLeast(0.6)
                }
                val rounded = (baseSpeed * 10.0).roundToInt() / 10.0
                val unit = if (app.id == "topaz_ai") "fps AI" else "it/s FP16"
                val adequacy = ((gpu.tensorIndex / 135.0) * 100.0).roundToInt().coerceIn(68, 99)
                MetricResult(
                    badgeTr = "TensorRT ~$rounded $unit",
                    badgeEn = "TensorRT ~$rounded $unit",
                    numericValue = rounded,
                    adequacyPct = adequacy,
                )
            }
            WorkstationMetricKind.CAD_IPC_INDEX -> {
                val isMultiHeavy = app.id == "ansys" || app.id == "houdini" || phase == WorkloadPhase.HEAVY_SIMULATION
                val avxBoost = if (math.prefersAvx512 && cpu.hasAvx512) 1.12 else 1.0
                if (isMultiHeavy) {
                    val solverPts = (cpu.cb23Multi * avxBoost).roundToInt()
                    val adequacy = ((solverPts / 52000.0) * 100.0).roundToInt().coerceIn(70, 99)
                    MetricResult(
                        badgeTr = "Çözücü Gücü ~$solverPts pts",
                        badgeEn = "Solver Index ~$solverPts pts",
                        numericValue = solverPts.toDouble(),
                        adequacyPct = adequacy,
                    )
                } else {
                    val ipcPts = cpu.cb23Single
                    val adequacy = ((ipcPts / 2350.0) * 100.0).roundToInt().coerceIn(74, 99)
                    MetricResult(
                        badgeTr = "CAD Tek Çekirdek $ipcPts CB23",
                        badgeEn = "CAD Single-Core $ipcPts CB23",
                        numericValue = ipcPts.toDouble(),
                        adequacyPct = adequacy,
                    )
                }
            }
            WorkstationMetricKind.ENGINE_VIEWPORT_BUILD -> {
                val buildIdx = ((multiNorm * 0.65 + singleNorm * 0.35) * 100.0).roundToInt().coerceIn(45, 165)
                val vpFps = ((gpu.tensorIndex * 0.95) / resLoad).roundToInt().coerceIn(32, 160)
                val adequacy = ((buildIdx * 0.5 + vpFps * 0.5)).roundToInt().coerceIn(70, 99)
                MetricResult(
                    badgeTr = "Viewport ~$vpFps FPS · Derleme %$buildIdx",
                    badgeEn = "Viewport ~$vpFps FPS · Build $buildIdx%",
                    numericValue = vpFps.toDouble(),
                    adequacyPct = adequacy,
                )
            }
            WorkstationMetricKind.DAW_TRACK_HEADROOM -> {
                // Real DAW DSP capacity at 64-sample ASIO buffer depends strictly on CPU IPC + Multi + RAM (NOT GPU!)
                val dspPower = singleNorm * 0.45 + multiNorm * 0.55
                val ramFactor = (min(reqRamGb, 128) / 64.0).coerceIn(0.65, 1.35)
                val activeTracks = (dspPower * 195.0 * (0.7 + 0.3 * ramFactor)).roundToInt().coerceIn(40, 320)
                val adequacy = ((activeTracks / 210.0) * 100.0).roundToInt().coerceIn(74, 99)
                MetricResult(
                    badgeTr = "~$activeTracks Kanal VST @ 64 smp",
                    badgeEn = "~$activeTracks VST Tracks @ 64 smp",
                    numericValue = activeTracks.toDouble(),
                    adequacyPct = adequacy,
                )
            }
        }
    }

    private fun llmModelWeightsGb(resolution: WorkloadResolution): Double = when (resolution) {
        WorkloadResolution.FHD_1080P -> 5.2   // 8B Q4_K_M
        WorkloadResolution.QHD_2K -> 9.0      // 14B Q4_K_M
        WorkloadResolution.UHD_4K -> 19.5     // 32B Q4_K_M
        WorkloadResolution.RAW_8K -> 42.0     // 70B Q4_K_M (physically requires 48 GB VRAM!)
    }

    private fun llmModelLabel(resolution: WorkloadResolution): String = when (resolution) {
        WorkloadResolution.FHD_1080P -> "8B Q4"
        WorkloadResolution.QHD_2K -> "14B Q4"
        WorkloadResolution.UHD_4K -> "32B Q4"
        WorkloadResolution.RAW_8K -> "70B Q4"
    }

    private fun snapVramTier(rawGb: Double, isAudio: Boolean): Int {
        if (isAudio) return if (rawGb >= 6.0) 8 else 4
        return when {
            rawGb > 34.0 -> 48
            rawGb > 24.5 -> 32
            rawGb > 17.5 -> 24
            rawGb > 12.5 -> 16
            rawGb > 8.5 -> 12
            else -> 8
        }
    }

    private fun snapRamKit(rawGb: Double, minKit: Int): Int {
        val kits = listOf(16, 32, 48, 64, 96, 128, 256)
        val chosen = kits.firstOrNull { it >= rawGb } ?: 256
        return max(chosen, minKit)
    }

    private fun buildExperienceNotes(
        app: WorkstationApp,
        math: AppMathProfile,
        tier: HardwareTier,
        resolution: WorkloadResolution,
        phase: WorkloadPhase,
        cpu: CpuCandidate,
        gpu: GpuCandidate,
        reqRamGb: Int,
        metricTr: String,
        metricEn: String,
    ): Pair<String, String> {
        if (app.id == "local_llm") {
            val model = llmModelLabel(resolution)
            val weights = llmModelWeightsGb(resolution)
            val fits = gpu.vramGb >= weights * 1.08
            val tr = if (fits) {
                "$model (~${weights.roundToInt()} GB) ağırlıkları ${gpu.vramGb} GB VRAM içine tamamen sığar; ${gpu.bandwidthGbs} GB/s bant genişliğiyle $metricTr."
            } else {
                "$model (~${weights.roundToInt()} GB) tek ${gpu.vramGb} GB VRAM'i aştığı için kısmi RAM offload veya Q3 kuantizasyonu gerekir ($metricTr)."
            }
            val en = if (fits) {
                "$model (~${weights.roundToInt()} GB) fits fully inside ${gpu.vramGb} GB VRAM at ${gpu.bandwidthGbs} GB/s ($metricEn)."
            } else {
                "$model (~${weights.roundToInt()} GB) exceeds ${gpu.vramGb} GB VRAM, requiring Q3 quantization or partial RAM offload ($metricEn)."
            }
            return tr to en
        }

        if (math.isAudioMinimalGpu) {
            val tr = "${cpu.displayName} + $reqRamGb GB RAM ile ultra düşük ASIO gecikmesi (<2.5 ms) ve $metricTr. Sessiz 0 dB GPU sayesinde mikrofon kaydında fan gürültüsü oluşmaz."
            val en = "${cpu.displayName} + $reqRamGb GB RAM delivers ultra-low ASIO latency (<2.5 ms) and $metricEn. Zero-dB GPU prevents fan noise during studio tracking."
            return tr to en
        }

        val phaseTr = when (phase) {
            WorkloadPhase.REALTIME_PREVIEW -> "canlı önizleme/viewport akıcılığında"
            WorkloadPhase.FINAL_RENDER -> "final dışa aktarma/render süresinde"
            WorkloadPhase.HEAVY_SIMULATION -> "ağır simülasyon ve ışın izleme yükünde"
        }
        val phaseEn = when (phase) {
            WorkloadPhase.REALTIME_PREVIEW -> "live timeline/viewport interactivity"
            WorkloadPhase.FINAL_RENDER -> "final export/render throughput"
            WorkloadPhase.HEAVY_SIMULATION -> "heavy simulation & ray tracing"
        }
        val tierAdjTr = when (tier) {
            HardwareTier.MINIMUM -> "dengeli giriş verimliliği"
            HardwareTier.RECOMMENDED_PRO -> "darboğazsız profesyonel üretim temposu"
            HardwareTier.STUDIO_EXTREME -> "stüdyo sınıfı kesintisiz maksimum çıktı"
        }
        val tierAdjEn = when (tier) {
            HardwareTier.MINIMUM -> "balanced entry baseline"
            HardwareTier.RECOMMENDED_PRO -> "bottleneck-free pro production pace"
            HardwareTier.STUDIO_EXTREME -> "studio-grade uncompromised throughput"
        }

        val tr = "${resolution.badge} ${app.shortName} $phaseTr $tierAdjTr sağlar ($metricTr; ${gpu.vramGb} GB VRAM + $reqRamGb GB RAM)."
        val en = "Delivers $tierAdjEn for ${resolution.badge} ${app.shortName} $phaseEn ($metricEn; ${gpu.vramGb} GB VRAM + $reqRamGb GB RAM)."
        return tr to en
    }

    private fun takeawayTr(appId: String): String = when (appId) {
        "premiere_pro" -> "Premiere Pro'da 10-bit 4:2:2 H.264/H.265 kod çözme için Intel QuickSync (Core Ultra 265K/285K) veya RTX 50 serisi 9. nesil NVDEC büyük avantaj sağlar; çift/üçlü NVENC dışa aktarma süresini belirgin biçimde kısaltır."
        "davinci_resolve" -> "DaVinci Resolve Studio yoğun biçimde GPU bellek bant genişliği ve VRAM'e bağlıdır. Temporal Noise Reduction ve Fusion nodları 4K'da 16 GB, 8K RAW'da 24–32 GB VRAM (RTX 4090 / RTX 5090) gerektirir."
        "after_effects" -> "After Effects MFR (Multi-Frame Rendering) çekirdek başına 4–8 GB bellek ayırır. 12–24 çekirdekli bir işlemciyi beslemek için 64–128 GB DDR5 RAM, yüksek GPU'dan daha etkilidir."
        "foundry_nuke" -> "Foundry Nuke çok kanallı OpenEXR dizilerini anlık RAM önbelleğine okur. PCIe 4.0/5.0 NVMe I/O hızı ve 64–128 GB RAM darboğazı çözen ana unsurlardır."
        "blender" -> "Blender Cycles OptiX motoru RTX ışın izleme çekirdekleriyle saf CUDA'dan ~2 kat hızlıdır. RTX 5070 Ti (~7.950 smp/dk) ve RTX 5090 (~15.800 smp/dk) zaman/maliyet dengesinde zirvededir."
        "unreal_engine" -> "Unreal Engine 5'te C++ ve Shader derleme CPU çekirdek sayısına (9950X / Threadripper), Lumen/Nanite canlı sahne akıcılığı ise 16–32 GB GPU VRAM ve Gen4/Gen5 SSD'ye bağlıdır."
        "unity_engine" -> "Unity 6'da Burst / IL2CPP derleme ve lightmap baking çoklu çekirdekli CPU ve 32–64 GB RAM ile hızlanır."
        "d5_render", "lumion", "chaos_vantage" -> "Gerçek zamanlı mimari render motorları doğrudan RTX donanım ışın izleme çekirdeklerine ve yüksek VRAM kapasitesine yüklenir; büyük peyzaj/BIM projelerinde 16–32 GB VRAM kritiktir."
        "houdini" -> "SideFX Houdini FLIP/Pyro simülasyonları çoklu CPU çekirdeği, tam 512-bit AVX-512 (Ryzen 9 9950X / Threadripper) ve 64–256 GB RAM ister."
        "solidworks" -> "SolidWorks büyük montaj yeniden oluşturmalarında tek çekirdek IPC (Core Ultra 285K / Ryzen 9 9950X — CB23 Single 2320+) ana belirleyicidir; oyun amaçlı X3D işlemciler burada düşük saat hızı nedeniyle geri kalır."
        "autocad", "revit" -> "AutoCAD ve Revit viewport çizimleri tek iş parçacığına bağlıdır (CB23 Single 2200+). Çok katlı BIM modellerinde 64 GB RAM kilitlenmeleri önler."
        "ansys", "matlab" -> "FEA/CFD matris çözücülerinde tam 512-bit AVX-512 (Zen 5 / Threadripper) ve çok kanallı bellek bant genişliği çözüm süresini dramatik biçimde azaltır."
        "stable_diffusion" -> "ComfyUI / Flux.1 modellerinde VRAM sınırını aşmak hızı 10–20 kat düşürür. 16 GB VRAM (RTX 5060 Ti 16GB / 5070 Ti) minimum güvenli eşik, 32 GB (RTX 5090) BF16/LoRA için zirvedir."
        "local_llm" -> "Yerel LLM token hızı doğrudan GPU bellek bant genişliğine (GB/s) bağlıdır. 32B Q4 (~20 GB) için RTX 4090/5090 uygundur; 70B Q4 (~42 GB ağırlık + KV Cache) ise fiziksel olarak tek GPU'da 48 GB VRAM (RTX 6000 Ada) gerektirir."
        "pytorch_cuda" -> "PyTorch FP16/BF16 eğitiminde batch boyutu ve gradyan bellek yükü VRAM tavanına takılır; ağır eğitimde 32–48 GB VRAM (RTX 5090 / RTX 6000 Ada) belirleyicidir."
        "fl_studio", "ableton_live", "cubase_pro_tools" -> "Müzik prodüksiyonunda ekran kartı gücü önemsizdir; bütçe RTX 4080/5080 yerine yüksek IPC'li sessiz CPU (9700X / 9900X / 9950X / 265K), 64–128 GB RAM ve ayrı NVMe sample diskine yönlendirilmelidir."
        "photoshop", "lightroom", "illustrator", "capture_one" -> "Fotoğraf/grafik iş akışlarında yüksek tek çekirdek IPC (285K / 9950X / 9700X), 32–64 GB RAM ve ayrılmış NVMe Scratch Disk anlık tepki sağlar; Lightroom AI Denoise'da orta-üst RTX GPU belirgin hız kazandırır."
        else -> "İş istasyonu performansında yazılımın gerçek darboğaz bileşenine (Tek Çekirdek, Çoklu Çekirdek, VRAM veya Bellek Bant Genişliği) yatırım yapmak genel sistem verimliliğini en üst düzeye çıkarır."
    }

    private fun takeawayEn(appId: String): String = when (appId) {
        "premiere_pro" -> "Intel QuickSync (Core Ultra 265K/285K) and RTX 50-series 9th-Gen NVDEC accelerate 10-bit 4:2:2 timelines, while dual/triple NVENC speeds up exports."
        "davinci_resolve" -> "DaVinci Resolve Studio is heavily GPU memory-bandwidth and VRAM bound: 16 GB for 4K grading/noise reduction and 24–32 GB (RTX 4090 / 5090) for 8K RAW."
        "after_effects" -> "After Effects MFR allocates 4–8 GB RAM per CPU core. Prioritizing 64–128 GB DDR5 RAM and fast IPC matters far more than an oversized GPU."
        "blender" -> "Blender Cycles OptiX delivers ~2x faster rendering than pure CUDA. RTX 5070 Ti (~7,950 smp/min) and RTX 5090 (~15,800 smp/min) lead in render throughput."
        "unreal_engine" -> "UE5 C++ and shader compilation scales strongly with multi-core CPUs (9950X / Threadripper), while Lumen/Nanite viewports demand 16–32 GB VRAM and fast Gen4/Gen5 NVMe."
        "solidworks", "autocad", "revit" -> "CAD assembly rebuilds and viewports are single-thread IPC bound (Core Ultra 285K / Ryzen 9 9950X, CB23 Single 2300+), where lower-clocked gaming X3D CPUs fall behind."
        "ansys", "houdini" -> "Heavy FEA/CFD and FLIP simulations thrive on full 512-bit AVX-512 (Zen 5 / Threadripper) and high-capacity multi-channel DDR5 memory."
        "local_llm" -> "Local LLM tok/s scales directly with GPU memory bandwidth (GB/s). 32B Q4 (~20 GB) runs fast on 24–32 GB GPUs, while 70B Q4 (~42 GB weights + KV cache) physically requires 48 GB VRAM (RTX 6000 Ada) for full single-GPU speed."
        "stable_diffusion", "pytorch_cuda" -> "Exceeding VRAM causes a 10–20x slowdown via system RAM offload. 16 GB is the safe Flux.1 FP8 floor; 32–48 GB unlocks BF16 and local training."
        "fl_studio", "ableton_live", "cubase_pro_tools" -> "GPU horsepower is irrelevant for DAWs; invest in a quiet high-IPC CPU (9700X / 9900X / 9950X / 265K), 64–128 GB RAM, and a dedicated NVMe sample library drive."
        else -> "Matching hardware investment to each application's real bottleneck (IPC, multi-core, VRAM, or bandwidth) maximizes studio productivity."
    }
}
