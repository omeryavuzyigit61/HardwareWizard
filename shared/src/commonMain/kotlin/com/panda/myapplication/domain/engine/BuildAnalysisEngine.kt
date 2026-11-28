package com.panda.myapplication.domain.engine

import androidx.compose.runtime.Immutable
import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.BuildCompatibilityReport
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.GameCategory
import com.panda.myapplication.domain.model.GamingResolution
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import kotlin.math.abs
import kotlin.math.roundToInt

enum class ResolutionOption(val label: String, val subLabel: String) {
    P1080("1080p", "FHD Ultra"),
    P1440("1440p", "2K QHD"),
    P2160("4K", "UHD 2160p"),
}

enum class QualityPreset(val label: String, val subLabel: String, val fpsMultiplier: Float) {
    MEDIUM("Orta", "Hızlı / Dengeli", 1.25f),
    HIGH("Yüksek", "Standart Grafik", 1.0f),
    ULTRA("Ultra", "Maksimum Kalite", 0.85f),
    RAY_TRACING("Ray Tracing", "Işın İzleme (RTX)", 0.55f),
}

enum class UpscalingMode(val label: String, val subLabel: String, val fpsMultiplier: Float) {
    OFF("Kapalı", "Native Çözünürlük", 1.0f),
    QUALITY("DLSS / FSR Kalite", "+35% FPS", 1.35f),
    BALANCED("Dengeli", "+50% FPS", 1.50f),
    PERFORMANCE("Performans", "+70% FPS", 1.70f),
    FRAME_GEN("Frame Gen (Kare Oluşturma)", "+65% İlave Akıcılık", 1.65f),
}

enum class BottleneckType {
    BALANCED,
    CPU_BOTTLENECK,
    GPU_BOTTLENECK,
}

@Immutable
data class WattageCockpitInfo(
    val componentTdpSum: Int,
    val peakTdpWithTolerance: Int,
    val recommendedPsuWattage: Int,
    val currentPsuWattage: Int?,
    val isSufficient: Boolean,
)

@Immutable
data class PlatformStatusBadge(
    val platformName: String,
    val isCompatible: Boolean,
    val statusText: String,
)

@Immutable
data class BottleneckAnalysis(
    val percentage: Int,
    val type: BottleneckType,
    val message: String,
)

@Immutable
data class ClearanceRadarResult(
    val gpuClearanceMm: Int?,
    val maxGpuLengthMm: Int?,
    val gpuLengthMm: Int?,
    val coolerClearanceMm: Int?,
    val maxCoolerHeightMm: Int?,
    val coolerHeightMm: Int?,
    val radiatorClearanceMm: Int?,
    val maxRadiatorMm: Int?,
    val coolerRadiatorMm: Int?,
    val hasClearanceIssue: Boolean,
    val issueMessage: String?,
    val radiatorSupportSummary: String? = null,
    val ramClearanceStatus: String? = null,
    val fanCapacitySummary: String? = null,
    val isCoolerHeightEstimated: Boolean = false,
)

@Immutable
data class UpgradeRecommendation(
    val priority: Int,
    val title: String,
    val targetCategory: com.panda.myapplication.domain.model.HardwareCategory,
    val expectedGain: String,
    val roiScore: Double,
    val explanation: String,
    val suggestedModels: List<String>,
)

@Immutable
data class BuildDoctorReport(
    val healthScore: Int,
    val statusText: String,
    val weakestLinkComponent: String?,
    val weakestLinkCategory: com.panda.myapplication.domain.model.HardwareCategory?,
    val resolutionBottlenecks: Map<ResolutionOption, BottleneckAnalysis>,
    val upgradeRecommendations: List<UpgradeRecommendation>,
    val doctorDiagnosis: String,
)

@Immutable
data class GameFpsEstimate(
    val gameName: String,
    val fps: Int,
    val low1PctFps: Int = (fps * 0.76).roundToInt(),
    val settings: String,
    val isEsports: Boolean = false,
)

@Immutable
data class SyntheticBenchmarkEstimate(
    val name: String,
    val score: Int,
    val unit: String,
)

@Immutable
data class GamingSimulationResult(
    val resolution: ResolutionOption,
    val preset: QualityPreset = QualityPreset.HIGH,
    val upscaling: UpscalingMode = UpscalingMode.OFF,
    val isReady: Boolean,
    val overallAvgFps: Int = 0,
    val overallLow1Fps: Int = 0,
    val estimatedVramGb: Float = 0f,
    val vramWarning: Boolean = false,
    val smoothnessRating: String = "",
    val games: List<GameFpsEstimate>,
    val synthetics: List<SyntheticBenchmarkEstimate>,
)

@Immutable
data class AnalysisState(
    val build: ActiveBuild,
    val cockpitInfo: WattageCockpitInfo,
    val platformStatus: PlatformStatusBadge,
    val bottleneck: BottleneckAnalysis,
    val clearance: ClearanceRadarResult,
    val gamingSim: GamingSimulationResult,
    val buildDoctor: BuildDoctorReport,
    val compatReport: BuildCompatibilityReport,
    val resolution: ResolutionOption,
)

object BuildAnalysisEngine {

    private val sharedPsuCalculator = com.panda.myapplication.domain.usecase.CalculatePsuRequirementUseCase()

    fun calculateWattageCockpit(build: ActiveBuild): WattageCockpitInfo {
        val psuReq = sharedPsuCalculator(build.items)
        val tdpSum = psuReq.estimatedLoad.watts
        val peakTdp = if (tdpSum > 0) (tdpSum * 1.25).roundToInt() else 0
        val recommendedPsu = psuReq.recommendedWattage.watts
        val currentPsu = (build.psu?.spec as? PsuSpec)?.wattage?.watts
        val isSufficient = currentPsu == null || currentPsu >= recommendedPsu

        return WattageCockpitInfo(
            componentTdpSum = tdpSum,
            peakTdpWithTolerance = peakTdp,
            recommendedPsuWattage = recommendedPsu,
            currentPsuWattage = currentPsu,
            isSufficient = isSufficient,
        )
    }

    fun calculatePlatformStatus(build: ActiveBuild): PlatformStatusBadge {
        val cpu = build.cpu?.spec as? CpuSpec
        val mobo = build.motherboard?.spec as? MotherboardSpec
        val ram = build.ram?.spec as? RamSpec

        if (cpu == null && mobo == null) {
            return PlatformStatusBadge(
                platformName = "Platform Seçilmedi",
                isCompatible = true,
                statusText = "CPU veya Anakart ekleyerek başlayın",
            )
        }

        val rawSocket = cpu?.socket ?: mobo?.socket
        val socket = rawSocket?.let {
            com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase.normalizeSocketName(it).ifBlank { it }
        } ?: "Bilinmeyen Soket"
        val ramGen = mobo?.memoryGeneration?.name ?: ram?.generation?.name ?: "DDR"

        val socketMatch = if (cpu != null && mobo != null) {
            com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase.socketsMatch(cpu.socket, mobo.socket)
        } else true

        val ramMatch = if (mobo != null && ram != null) {
            mobo.memoryGeneration == ram.generation
        } else true

        val isFullyCompatible = socketMatch && ramMatch

        val statusText = when {
            !socketMatch -> "Soket Uyuşmazlığı (${com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase.normalizeSocketName(cpu?.socket)} != ${com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase.normalizeSocketName(mobo?.socket)})"
            !ramMatch -> "Bellek Nesli Uyuşmazlığı (${mobo?.memoryGeneration} != ${ram?.generation})"
            cpu != null && mobo != null && ram != null -> "$socket + $ramGen Platformu - %100 Uyumlu"
            cpu != null && mobo != null -> "$socket Mimarisi - Soket Uyumlu"
            else -> "$socket Altyapısı Aktif"
        }

        return PlatformStatusBadge(
            platformName = "$socket $ramGen",
            isCompatible = isFullyCompatible,
            statusText = statusText,
        )
    }

    fun calculateBottleneckForResolution(build: ActiveBuild, resolution: ResolutionOption): BottleneckAnalysis {
        val cpuItem = build.cpu
        val gpuItem = build.gpu
        val cpu = cpuItem?.spec as? CpuSpec
        val gpu = gpuItem?.spec as? GpuSpec

        if (cpuItem == null || gpuItem == null || cpu == null || gpu == null) {
            return BottleneckAnalysis(
                percentage = 0,
                type = BottleneckType.BALANCED,
                message = "CPU ve GPU seçildiğinde darboğaz analizi hesaplanacak",
            )
        }

        // Physical CPU vs GPU frame-time share analysis using UnifiedFpsSimulator
        val cpuProfile = UnifiedFpsSimulator.resolveCpuProfile(cpuItem)
        val gpuProfile = UnifiedFpsSimulator.resolveGpuProfile(gpuItem)
        val ramFactor = UnifiedFpsSimulator.computeRamFactor(build.ram?.spec as? RamSpec)
        val gamingRes = when (resolution) {
            ResolutionOption.P1080 -> GamingResolution.P1080
            ResolutionOption.P1440 -> GamingResolution.P1440
            ResolutionOption.P2160 -> GamingResolution.P4K
        }

        // Compute average CPU pipeline share across representative AAA & E-Sport titles
        val samples = UnifiedFpsSimulator.GAME_PROFILES.map { game ->
            game.category to UnifiedFpsSimulator.simulateSingleGameAtResolution(
                game = game,
                cpu = cpuProfile,
                gpu = gpuProfile,
                resolution = gamingRes,
                ramFactor = ramFactor,
            )
        }
        // Weight AAA titles 75% and E-Sports 25% so flagship rigs aren't falsely flagged by 900 FPS Valorant/CS2
        val aaaShare = samples.filter { it.first == GameCategory.AAA }.map { it.second.cpuShare }.average()
        val espShare = samples.filter { it.first == GameCategory.ESPORTS }.map { it.second.cpuShare }.average()
        val avgCpuShare = aaaShare * 0.75 + espShare * 0.25

        // Resolution-specific balanced ideal CPU share (at 1080p CPU naturally works harder; at 4K GPU dominates)
        val idealCpuShare = when (resolution) {
            ResolutionOption.P1080 -> 0.50
            ResolutionOption.P1440 -> 0.36
            ResolutionOption.P2160 -> 0.22
        }

        // Top-of-market flagship CPU (>=90) + flagship GPU (>=85) is inherently balanced across all resolutions
        val bothFlagship = cpuProfile.gamingIndex >= 90.0 && gpuProfile.relativeIndex >= 85.0
        val delta = avgCpuShare - idealCpuShare
        val rawPct = (abs(delta) * 115.0).roundToInt().coerceIn(0, 60)

        return when {
            bothFlagship || abs(delta) <= 0.12 || rawPct <= 10 -> BottleneckAnalysis(
                percentage = rawPct.coerceIn(1, 9),
                type = BottleneckType.BALANCED,
                message = "%${rawPct.coerceIn(1, 9)} - Mükemmel Denge (${resolution.label})",
            )
            delta > 0.0 -> BottleneckAnalysis(
                percentage = rawPct,
                type = BottleneckType.CPU_BOTTLENECK,
                message = "%$rawPct - CPU Darboğazı Riski (${resolution.label})",
            )
            else -> BottleneckAnalysis(
                percentage = rawPct,
                type = BottleneckType.GPU_BOTTLENECK,
                message = "%$rawPct - GPU Limitli / Tam Yükte (${resolution.label})",
            )
        }
    }

    fun calculateBottleneck(build: ActiveBuild): BottleneckAnalysis =
        calculateBottleneckForResolution(build, ResolutionOption.P1440)

    fun calculateClearanceRadar(build: ActiveBuild): ClearanceRadarResult {
        val case = build.case?.spec as? CaseSpec
        val gpu = build.gpu?.spec as? GpuSpec
        val cooler = build.cooler?.spec as? CoolerSpec

        if (case == null) {
            return ClearanceRadarResult(
                gpuClearanceMm = null,
                maxGpuLengthMm = null,
                gpuLengthMm = gpu?.lengthMm,
                coolerClearanceMm = null,
                maxCoolerHeightMm = null,
                coolerHeightMm = null,
                radiatorClearanceMm = null,
                maxRadiatorMm = null,
                coolerRadiatorMm = cooler?.radiatorSizeMm,
                hasClearanceIssue = false,
                issueMessage = null,
            )
        }

        var gpuMargin: Int? = null
        val maxGpu = case.maxGpuLengthMm
        val gpuLen = gpu?.lengthMm
        if (gpuLen != null && maxGpu != null && gpuLen > 0) {
            gpuMargin = maxGpu - gpuLen
        }

        var coolerMargin: Int? = null
        var estimatedAirHeight: Int? = null
        var isHeightEstimated = false
        val maxCooler = case.maxCpuCoolerHeightMm
        if (cooler != null && cooler.type == CoolerType.AIR && maxCooler != null) {
            isHeightEstimated = cooler.heightMm == null
            estimatedAirHeight = cooler.heightMm ?: 158
            coolerMargin = maxCooler - estimatedAirHeight
        }

        var radMargin: Int? = null
        val coolerRad = cooler?.radiatorSizeMm
        val maxRad = case.maxRadiatorSizeMm
        if (cooler != null && cooler.type == CoolerType.LIQUID && coolerRad != null && maxRad != null && coolerRad > 0) {
            radMargin = maxRad - coolerRad
        }

        val mobo = build.motherboard?.spec as? MotherboardSpec
        val issues = mutableListOf<String>()
        if (gpuMargin != null && gpuMargin < 0) {
            issues.add("GPU ${abs(gpuMargin)}mm uzun (Kasaya sığmıyor!)")
        }
        if (coolerMargin != null && coolerMargin < 0) {
            issues.add("Soğutucu ${abs(coolerMargin)}mm yüksek")
        }
        if (radMargin != null && radMargin < 0) {
            issues.add("Radyatör ${cooler?.radiatorSizeMm}mm > Kasa ${case.maxRadiatorSizeMm}mm")
        }
        if (mobo != null && !com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(mobo.formFactor, case.type)) {
            issues.add("${mobo.formFactor.displayName} anakart, ${case.type ?: "seçili"} kasaya sığmıyor!")
        }

        val radSupportSummary = case.radiatorSupport?.takeIf { it.isNotBlank() }
            ?: (if (case.maxRadiatorSizeMm != null) "Maksimum ${case.maxRadiatorSizeMm}mm radyatör desteği" else null)

        val ramClearance = when {
            cooler == null -> null
            cooler.type == CoolerType.LIQUID -> "Sıvı Soğutma (AIO): RAM yuvaları tamamen açık (Sıfır çakışma)"
            (estimatedAirHeight ?: 0) >= 155 -> "Büyük Kule Soğutucu: Yüksek RGB RAM'lerde fan konumu gerekebilir"
            else -> "Kompakt Hava Soğutucu: RAM çakışması riski yok"
        }

        val fanSummary = case.includedFans?.takeIf { it.isNotBlank() }
            ?: (if (case.type != null) "${case.type} Kasa" else null)

        return ClearanceRadarResult(
            gpuClearanceMm = gpuMargin,
            maxGpuLengthMm = case.maxGpuLengthMm,
            gpuLengthMm = gpu?.lengthMm,
            coolerClearanceMm = coolerMargin,
            maxCoolerHeightMm = case.maxCpuCoolerHeightMm,
            coolerHeightMm = estimatedAirHeight,
            radiatorClearanceMm = radMargin,
            maxRadiatorMm = case.maxRadiatorSizeMm,
            coolerRadiatorMm = cooler?.radiatorSizeMm,
            hasClearanceIssue = issues.isNotEmpty(),
            issueMessage = if (issues.isNotEmpty()) issues.joinToString(" | ") else null,
            radiatorSupportSummary = radSupportSummary,
            ramClearanceStatus = ramClearance,
            fanCapacitySummary = fanSummary,
            isCoolerHeightEstimated = isHeightEstimated,
        )
    }

    fun calculateBuildDoctor(build: ActiveBuild): BuildDoctorReport {
        val cpu = build.cpu?.spec as? CpuSpec
        val gpu = build.gpu?.spec as? GpuSpec
        val mobo = build.motherboard?.spec as? MotherboardSpec
        val ram = build.ram?.spec as? RamSpec
        val psu = build.psu?.spec as? PsuSpec
        val cooler = build.cooler?.spec as? CoolerSpec

        val (_, compatReport) = com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase(sharedPsuCalculator)(build)
        val missingCore = build.cpu == null || build.gpu == null
        val resBottlenecks = ResolutionOption.entries.associateWith { res ->
            calculateBottleneckForResolution(build, res)
        }
        val defaultBottleneck = resBottlenecks[ResolutionOption.P1440] ?: calculateBottleneck(build)
        val cockpit = calculateWattageCockpit(build)
        val clearance = calculateClearanceRadar(build)
        val platform = calculatePlatformStatus(build)

        if (missingCore) {
            val earlyPenalty = (compatReport.warnings.size * 15).coerceAtMost(35)
            val basePartialScore = if (build.totalCount > 0) (45 - earlyPenalty).coerceAtLeast(15) else 0
            return BuildDoctorReport(
                healthScore = basePartialScore,
                statusText = if (compatReport.warnings.isNotEmpty()) {
                    "Uyumsuzluk Uyarısı Mevcut (Temel Bileşenler Eksik)"
                } else if (build.totalCount > 0) {
                    "Eksik Temel Bileşenler (CPU/GPU bekleniyor)"
                } else {
                    "Sistem Boş"
                },
                weakestLinkComponent = compatReport.warnings.firstOrNull()
                    ?: if (build.cpu == null) "İşlemci (CPU) Eklenmedi" else "Ekran Kartı (GPU) Eklenmedi",
                weakestLinkCategory = if (build.cpu == null) com.panda.myapplication.domain.model.HardwareCategory.CPU else com.panda.myapplication.domain.model.HardwareCategory.GPU,
                resolutionBottlenecks = resBottlenecks,
                upgradeRecommendations = emptyList(),
                doctorDiagnosis = if (compatReport.warnings.isNotEmpty()) {
                    compatReport.warnings.first()
                } else {
                    "Sistem sağlığı ve darboğaz analizi için öncelikle CPU ve GPU bileşenlerini seçin."
                },
            )
        }

        var healthScore = 100
        val recommendations = mutableListOf<UpgradeRecommendation>()
        var weakestLink = "Yok (Bileşenler dengeli)"
        var weakestCategory: com.panda.myapplication.domain.model.HardwareCategory? = null

        val normSocket = com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase.normalizeSocketName(cpu?.socket)

        // 1. Socket & RAM Platform Compatibility check
        if (!platform.isCompatible || compatReport.ramMismatch != null) {
            healthScore -= 40
            weakestLink = compatReport.socketMismatch ?: compatReport.ramMismatch ?: "Platform / Soket Uyuşmazlığı"
            if (compatReport.socketMismatch != null) {
                weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.MOTHERBOARD
                val suggestedBoard = when {
                    normSocket.contains("AM5") -> "B650 / B850 / X870 (AM5) Anakart"
                    normSocket.contains("AM4") -> "B550 / X570 (AM4) Anakart"
                    normSocket.contains("1851") -> "B860 / Z890 (LGA1851) Anakart"
                    normSocket.contains("1700") -> "B760 / Z790 (LGA1700) Anakart"
                    else -> "$normSocket Uyumlu Anakart"
                }
                recommendations.add(
                    UpgradeRecommendation(
                        priority = 1,
                        title = "Anakart / İşlemci Soketini Eşitleyin",
                        targetCategory = com.panda.myapplication.domain.model.HardwareCategory.MOTHERBOARD,
                        expectedGain = "Sistem Çalışabilirlik",
                        roiScore = 10.0,
                        explanation = "Seçilen işlemci (${cpu?.socket}) ile anakart (${mobo?.socket}) soketleri birbiriyle fiziksel olarak uyumlu değil.",
                        suggestedModels = listOf(suggestedBoard),
                    )
                )
            } else if (compatReport.ramMismatch != null) {
                weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.RAM
                val targetDdr = mobo?.memoryGeneration?.name ?: "DDR5"
                val suggestedRamByBoard = if (targetDdr == "DDR4") {
                    "2x16GB (32GB) DDR4-3600 CL16 (${mobo?.ramSlots ?: 2} slota uygun)"
                } else {
                    "2x16GB (32GB) DDR5-6000 CL30 (${mobo?.ramSlots ?: 2} slota uygun)"
                }
                recommendations.add(
                    UpgradeRecommendation(
                        priority = 1,
                        title = "Uyumlu Bellek (RAM) Kiti Seçin",
                        targetCategory = com.panda.myapplication.domain.model.HardwareCategory.RAM,
                        expectedGain = " Anakart Bellek Uyumluluğu",
                        roiScore = 10.0,
                        explanation = compatReport.ramMismatch.replace("\n", " "),
                        suggestedModels = listOf(suggestedRamByBoard),
                    )
                )
            }
        }

        // 1b. Cooler Socket Mounting check
        if (compatReport.coolerMismatch != null) {
            healthScore -= 15
            if (weakestCategory == null) {
                weakestLink = "Soğutucu Montaj Soketi Uyumsuz ($normSocket)"
                weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.COOLER
            }
            recommendations.add(
                UpgradeRecommendation(
                    priority = 2,
                    title = "$normSocket Uyumlu İşlemci Soğutucu Seçin",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.COOLER,
                    expectedGain = "Doğru Termal Temas & Montaj Uyumu",
                    roiScore = 9.4,
                    explanation = compatReport.coolerMismatch,
                    suggestedModels = listOf(
                        if ((cpu?.tdp?.watts ?: 65) >= 125) "360mm AIO Sıvı Soğutucu ($normSocket)"
                        else "Çift Kule Hava Soğutucu ($normSocket)"
                    ),
                )
            )
        }

        // 2. Power & ATX 3.0 / 12V-2x6 check
        if (!cockpit.isSufficient) {
            healthScore -= 25
            val needed = cockpit.recommendedPsuWattage
            weakestLink = "Yetersiz Güç Kaynağı (${psu?.wattage?.watts ?: 0}W < ${needed}W)"
            weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.PSU
            val targetWatt = maxOf(needed, 750)
            recommendations.add(
                UpgradeRecommendation(
                    priority = 2,
                    title = "Güç Kaynağını (PSU) Yükseltin",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.PSU,
                    expectedGain = "Sistem Kararlılığı & Ani Yük Koruması",
                    roiScore = 9.2,
                    explanation = "Sistem yük altında ani tepe güç tüketimine (${cockpit.peakTdpWithTolerance}W) ulaştığında kapanma veya kararsızlık yaşanabilir.",
                    suggestedModels = listOf("${targetWatt}W+ 80+ Gold ATX 3.1 / PCIe 5.1 PSU"),
                )
            )
        } else if (compatReport.psuWarning != null && cooler != null) {
            healthScore -= 8
            recommendations.add(
                UpgradeRecommendation(
                    priority = 3,
                    title = "ATX 3.0 / 12V-2x6 Destekli PSU Tercih Edin",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.PSU,
                    expectedGain = "Doğrudan Tek Kablo 12V-2x6 Güvenliği",
                    roiScore = 8.4,
                    explanation = compatReport.psuWarning,
                    suggestedModels = listOf("${maxOf(cockpit.recommendedPsuWattage, 850)}W ATX 3.1 80+ Gold PSU"),
                )
            )
        }

        // 3. Physical clearance & Motherboard/Case form factor
        if (clearance.hasClearanceIssue) {
            healthScore -= 20
            weakestLink = "Fiziksel Boyut / Kasa Sığmama Sorunu"
            weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.CASE
            recommendations.add(
                UpgradeRecommendation(
                    priority = 3,
                    title = "Kasa veya Soğutucu Boyutunu Optimize Edin",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.CASE,
                    expectedGain = "Fiziksel Montaj Uyumluluğu",
                    roiScore = 8.8,
                    explanation = clearance.issueMessage ?: "Kasa içi boyut kısıtlamaları aşıldı.",
                    suggestedModels = listOf("ATX / E-ATX Uyumlu, 400mm+ GPU & 360mm Radyatör Destekli Kasa"),
                )
            )
        }

        // 4. Bottleneck check
        if (defaultBottleneck.type == BottleneckType.CPU_BOTTLENECK && defaultBottleneck.percentage >= 15) {
            healthScore -= (defaultBottleneck.percentage / 2).coerceAtMost(25)
            weakestLink = "${build.cpu.displayName} (%${defaultBottleneck.percentage} Darboğaz)"
            weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.CPU

            val suggestedCpu = when {
                normSocket.contains("AM4") -> listOf("AMD Ryzen 7 5700X3D", "AMD Ryzen 7 5800X3D")
                normSocket.contains("AM5") -> listOf("AMD Ryzen 7 9800X3D", "AMD Ryzen 7 7800X3D")
                normSocket.contains("1851") -> listOf("Intel Core Ultra 7 265K", "Intel Core Ultra 9 285K")
                normSocket.contains("1700") -> listOf("Intel Core i7-14700K", "Intel Core i5-14600K")
                else -> listOf("Daha yüksek IPC / L3 önbellekli modern işlemci")
            }

            recommendations.add(
                UpgradeRecommendation(
                    priority = 4,
                    title = "İşlemciyi (CPU) Güçlendirin",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.CPU,
                    expectedGain = "+%${defaultBottleneck.percentage + 8} Minimum %1 Low FPS",
                    roiScore = 8.5,
                    explanation = "Ekran kartınız çok güçlü ancak işlemci özellikle 1080p ve 1440p'de kartı tam besleyemiyor.",
                    suggestedModels = suggestedCpu,
                )
            )
        } else if (defaultBottleneck.type == BottleneckType.GPU_BOTTLENECK && defaultBottleneck.percentage >= 20) {
            val gpuRelIndex = UnifiedFpsSimulator.resolveGpuProfile(build.gpu).relativeIndex
            // Only suggest an upgrade if the GPU isn't already a top-tier flagship (>= 85 index = RTX 5080 / 4090 / 5090)
            if (gpuRelIndex < 85.0) {
                healthScore -= 10
                weakestLink = "${build.gpu.displayName} (%${defaultBottleneck.percentage} GPU Limitli)"
                weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.GPU
                val suggestedGpus = when {
                    gpuRelIndex < 45.0 -> listOf("NVIDIA GeForce RTX 5070", "AMD Radeon RX 9070", "NVIDIA GeForce RTX 4070 SUPER")
                    gpuRelIndex < 68.0 -> listOf("NVIDIA GeForce RTX 5070 Ti", "AMD Radeon RX 9070 XT", "NVIDIA GeForce RTX 4080 SUPER")
                    else -> listOf("NVIDIA GeForce RTX 5080", "NVIDIA GeForce RTX 4090", "NVIDIA GeForce RTX 5090")
                }
                recommendations.add(
                    UpgradeRecommendation(
                        priority = 5,
                        title = "Ekran Kartını (GPU) Üst Seviyeye Taşıyın",
                        targetCategory = com.panda.myapplication.domain.model.HardwareCategory.GPU,
                        expectedGain = "+%25-45 Oyun Grafik & FPS Artışı",
                        roiScore = 8.0,
                        explanation = "İşlemciniz çok güçlü; daha yüksek grafik ayarları ve 2K/4K için üst segment GPU yüksek getiri sağlar.",
                        suggestedModels = suggestedGpus,
                    )
                )
            }
        }

        // 5. RAM capacity check (strictly DDR-generation aware)
        val ramCapacityGb = (ram?.capacity?.megabytes ?: 0) / 1024
        if (ram != null && ramCapacityGb < 16) {
            healthScore -= 15
            if (weakestCategory == null) {
                weakestLink = "Düşük RAM Kapasitesi (${ramCapacityGb} GB)"
                weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.RAM
            }
            val activeDdr = mobo?.memoryGeneration ?: ram.generation
            val ramRec = if (activeDdr == com.panda.myapplication.domain.model.DdrGeneration.DDR4) {
                listOf("2x16GB (32GB) DDR4-3600MHz CL16", "2x8GB (16GB) DDR4-3600MHz CL16")
            } else {
                listOf("2x16GB (32GB) DDR5-6000MHz CL30", "2x24GB (48GB) DDR5-6000MHz CL30")
            }
            recommendations.add(
                UpgradeRecommendation(
                    priority = 6,
                    title = "RAM Kapasitesini En Az 32GB'a Çıkarın",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.RAM,
                    expectedGain = "Oyun İçi Takılmaların (Stutter) Önlenmesi",
                    roiScore = 9.5,
                    explanation = "Modern AAA oyunlar ve arka plan görevleri için 16GB alt sınır, 32GB (${activeDdr.name}) ise ideal kapasitedir.",
                    suggestedModels = ramRec,
                )
            )
        }

        healthScore = healthScore.coerceIn(15, 100)

        val statusText = when {
            healthScore >= 90 -> "Mükemmel Denge & Yüksek Verim"
            healthScore >= 75 -> "İyi Durumda - Küçük İyileştirmeler Yapılabilir"
            healthScore >= 55 -> "Optimize Edilmeli - Belirgin Dengesizlik Var"
            else -> "Kritik Sorunlar Mevcut - Revizyon Gerekli"
        }

        val doctorDiagnosis = when {
            healthScore >= 90 -> "Bileşenleriniz birbiriyle son derece uyumlu. Güç, soğutma ve darboğaz marjları optimum seviyede."
            recommendations.isNotEmpty() -> "Sistemden tam performans almak için öncelikli olarak ${recommendations.first().title.lowercase()} adımını değerlendirin."
            else -> "Sistem dengesi genel standartları karşılıyor."
        }

        return BuildDoctorReport(
            healthScore = healthScore,
            statusText = statusText,
            weakestLinkComponent = weakestLink,
            weakestLinkCategory = weakestCategory,
            resolutionBottlenecks = resBottlenecks,
            upgradeRecommendations = recommendations.sortedBy { it.priority },
            doctorDiagnosis = doctorDiagnosis,
        )
    }

    fun calculateGamingSimulation(
        build: ActiveBuild,
        resolution: ResolutionOption = ResolutionOption.P1440,
        preset: QualityPreset = QualityPreset.HIGH,
        upscaling: UpscalingMode = UpscalingMode.OFF,
    ): GamingSimulationResult {
        val cpuItem = build.cpu
        val gpuItem = build.gpu
        val cpu = cpuItem?.spec as? CpuSpec
        val gpu = gpuItem?.spec as? GpuSpec

        if (cpu == null || gpu == null) {
            return GamingSimulationResult(
                resolution = resolution,
                preset = preset,
                upscaling = upscaling,
                isReady = false,
                overallAvgFps = 0,
                overallLow1Fps = 0,
                estimatedVramGb = 0f,
                vramWarning = false,
                smoothnessRating = "",
                games = emptyList(),
                synthetics = emptyList(),
            )
        }

        val cpuProfile = UnifiedFpsSimulator.resolveCpuProfile(cpuItem)
        val gpuProfile = UnifiedFpsSimulator.resolveGpuProfile(gpuItem)
        val ramFactor = UnifiedFpsSimulator.computeRamFactor(build.ram?.spec as? RamSpec)

        val timeSpyScore = gpuItem.benchmarkInfo?.effectiveTimeSpy
            ?: (gpuProfile.relativeIndex * 365.0).roundToInt().coerceIn(3000, 55000)

        val cinebenchScore = cpuItem.benchmarkInfo?.effectiveMultiCore
            ?: (cpuProfile.productivityIndex * 432.0).roundToInt().coerceIn(3000, 120000)

        val gamingRes = when (resolution) {
            ResolutionOption.P1080 -> GamingResolution.P1080
            ResolutionOption.P1440 -> GamingResolution.P1440
            ResolutionOption.P2160 -> GamingResolution.P4K
        }

        // Single Source of Truth: exact same L_p physical frame-time pipeline as CPU/GPU Detail & VS Arena!
        val games = UnifiedFpsSimulator.GAME_PROFILES.map { gameProfile ->
            val sample = UnifiedFpsSimulator.simulateSingleGameAtResolution(
                game = gameProfile,
                cpu = cpuProfile,
                gpu = gpuProfile,
                resolution = gamingRes,
                preset = preset,
                upscaling = upscaling,
                ramFactor = ramFactor,
            )
            GameFpsEstimate(
                gameName = gameProfile.title,
                fps = sample.avgFps,
                low1PctFps = sample.low1PctFps,
                settings = "${preset.label} • ${upscaling.label}",
                isEsports = gameProfile.category == GameCategory.ESPORTS,
            )
        }

        val overallAvg = if (games.isNotEmpty()) games.map { it.fps }.average().roundToInt() else 0
        val overallLow1 = if (games.isNotEmpty()) games.map { it.low1PctFps }.average().roundToInt() else 0

        // VRAM calculation
        val baseVram = when (resolution) {
            ResolutionOption.P1080 -> 5.2f
            ResolutionOption.P1440 -> 7.8f
            ResolutionOption.P2160 -> 12.0f
        }
        val presetVramFactor = when (preset) {
            QualityPreset.MEDIUM -> 0.82f
            QualityPreset.HIGH -> 1.0f
            QualityPreset.ULTRA -> 1.22f
            QualityPreset.RAY_TRACING -> 1.45f
        }
        val upscalingVramFactor = when (upscaling) {
            UpscalingMode.OFF -> 1.0f
            UpscalingMode.QUALITY -> 0.88f
            UpscalingMode.BALANCED -> 0.80f
            UpscalingMode.PERFORMANCE -> 0.72f
            UpscalingMode.FRAME_GEN -> 1.15f
        }
        val estVram = ((baseVram * presetVramFactor * upscalingVramFactor) * 10f).roundToInt() / 10f
        val actualVram = (gpu.vram.megabytes.toFloat() / 1024f)
        val vramWarning = estVram > actualVram

        val smoothness = when {
            overallAvg >= 120 -> "🌟 Rekabetçi / Ultra Akıcı (120+ FPS)"
            overallAvg >= 75 -> "✨ Çok Akıcı / Yüksek Hız (75-120 FPS)"
            overallAvg >= 50 -> "⚡ Akıcı & Kararlı (50-75 FPS)"
            overallAvg >= 30 -> "🎮 Oynanabilir Standart (30-50 FPS)"
            else -> "⚠️ Düşük Kare Hızı (<30 FPS)"
        }

        val synthetics = listOf(
            SyntheticBenchmarkEstimate(
                name = "3DMark Time Spy",
                score = timeSpyScore,
                unit = "Graphics Puanı",
            ),
            SyntheticBenchmarkEstimate(
                name = "Cinebench R23",
                score = cinebenchScore,
                unit = "Multi-Core",
            ),
        )

        return GamingSimulationResult(
            resolution = resolution,
            preset = preset,
            upscaling = upscaling,
            isReady = true,
            overallAvgFps = overallAvg,
            overallLow1Fps = overallLow1,
            estimatedVramGb = estVram,
            vramWarning = vramWarning,
            smoothnessRating = smoothness,
            games = games,
            synthetics = synthetics,
        )
    }
}
