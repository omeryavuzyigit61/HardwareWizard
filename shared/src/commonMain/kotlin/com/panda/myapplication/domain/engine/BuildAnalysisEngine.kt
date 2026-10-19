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

    fun calculateWattageCockpit(build: ActiveBuild): WattageCockpitInfo {
        val cpuTdp = (build.cpu?.spec as? CpuSpec)?.tdp?.watts ?: 0
        val gpuTdp = (build.gpu?.spec as? GpuSpec)?.tdp?.watts ?: 0
        val baseBoardOverhead = if (build.motherboard != null) 50 else 0
        val ramOverhead = (build.ram?.spec as? RamSpec)?.let { it.moduleCount * 8 } ?: if (build.ram != null) 16 else 0
        val storageOverhead = if (build.storage != null) 10 else 0
        val coolerOverhead = if (build.cooler != null) 15 else 0
        val caseOverhead = if (build.case != null) 10 else 0

        val tdpSum = cpuTdp + gpuTdp + baseBoardOverhead + ramOverhead + storageOverhead + coolerOverhead + caseOverhead
        val peakTdp = if (tdpSum > 0) (tdpSum * 1.20).roundToInt() else 0

        val gpuRec = (build.gpu?.spec as? GpuSpec)?.recommendedPsu?.watts ?: 0
        val calculatedRec = if (peakTdp > 0) ((peakTdp + 49) / 50) * 50 else 0
        val recommendedPsu = maxOf(calculatedRec, gpuRec)

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

        val socket = cpu?.socket ?: mobo?.socket ?: "Bilinmeyen Soket"
        val ramGen = mobo?.memoryGeneration?.name ?: ram?.generation?.name ?: "DDR"

        val socketMatch = if (cpu != null && mobo != null) {
            cpu.socket.equals(mobo.socket, ignoreCase = true)
        } else true

        val ramMatch = if (mobo != null && ram != null) {
            mobo.memoryGeneration == ram.generation
        } else true

        val isFullyCompatible = socketMatch && ramMatch

        val statusText = when {
            !socketMatch -> "Soket Uyuşmazlığı (${cpu?.socket} != ${mobo?.socket})"
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
        val cpu = build.cpu?.spec as? CpuSpec
        val gpu = build.gpu?.spec as? GpuSpec

        if (cpu == null || gpu == null) {
            return BottleneckAnalysis(
                percentage = 0,
                type = BottleneckType.BALANCED,
                message = "CPU ve GPU seçildiğinde darboğaz analizi hesaplanacak",
            )
        }

        // Estimate relative power tier (1 to 100)
        val cpuClock = (cpu.boostClock?.mhz ?: cpu.baseClock.mhz).toDouble()
        val l3Bonus = (cpu.l3CacheMb ?: 16) * 0.4
        val cpuPowerRaw = ((cpu.coreCount * (cpuClock / 1000.0) * 1.5) + l3Bonus).coerceIn(10.0, 100.0)

        val gpuTdp = gpu.tdp.watts.toDouble()
        val gpuVram = gpu.vram.megabytes.toDouble() / 1024.0
        val gpuClock = (gpu.boostClock?.mhz ?: gpu.coreClock.mhz).toDouble()
        val gpuPowerRaw = ((gpuTdp * 0.22) + (gpuVram * 2.8) + (gpuClock / 100.0)).coerceIn(10.0, 100.0)

        // Resolution scaling: 1080p stresses CPU more, 4K is massively GPU bound
        val (cpuPower, gpuPower) = when (resolution) {
            ResolutionOption.P1080 -> (cpuPowerRaw * 0.88) to (gpuPowerRaw * 1.15)
            ResolutionOption.P1440 -> cpuPowerRaw to gpuPowerRaw
            ResolutionOption.P2160 -> (cpuPowerRaw * 1.25) to (gpuPowerRaw * 0.82)
        }

        val delta = ((cpuPower - gpuPower) / maxOf(cpuPower, gpuPower)) * 100.0
        val absDelta = abs(delta).roundToInt().coerceIn(1, 55)

        return when {
            absDelta <= 8 -> BottleneckAnalysis(
                percentage = absDelta,
                type = BottleneckType.BALANCED,
                message = "%$absDelta - Mükemmel Denge (${resolution.label})",
            )
            delta < -8 -> BottleneckAnalysis(
                percentage = absDelta,
                type = BottleneckType.CPU_BOTTLENECK,
                message = "%$absDelta - CPU Darboğazı Riski (${resolution.label})",
            )
            else -> BottleneckAnalysis(
                percentage = absDelta,
                type = BottleneckType.GPU_BOTTLENECK,
                message = "%$absDelta - GPU Limitli / Tam Yükte (${resolution.label})",
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
        val maxCooler = case.maxCpuCoolerHeightMm
        if (cooler != null && cooler.type == CoolerType.AIR && maxCooler != null) {
            estimatedAirHeight = cooler.heightMm ?: 158 // Average tower air cooler height
            coolerMargin = maxCooler - estimatedAirHeight
        }

        var radMargin: Int? = null
        val coolerRad = cooler?.radiatorSizeMm
        val maxRad = case.maxRadiatorSizeMm
        if (cooler != null && cooler.type == CoolerType.LIQUID && coolerRad != null && maxRad != null && coolerRad > 0) {
            radMargin = maxRad - coolerRad
        }

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
        )
    }

    fun calculateBuildDoctor(build: ActiveBuild): BuildDoctorReport {
        val cpu = build.cpu?.spec as? CpuSpec
        val gpu = build.gpu?.spec as? GpuSpec
        val mobo = build.motherboard?.spec as? MotherboardSpec
        val ram = build.ram?.spec as? RamSpec
        val psu = build.psu?.spec as? PsuSpec

        val missingCore = build.cpu == null || build.gpu == null
        val resBottlenecks = ResolutionOption.entries.associateWith { res ->
            calculateBottleneckForResolution(build, res)
        }
        val defaultBottleneck = resBottlenecks[ResolutionOption.P1440] ?: calculateBottleneck(build)
        val cockpit = calculateWattageCockpit(build)
        val clearance = calculateClearanceRadar(build)
        val platform = calculatePlatformStatus(build)

        if (missingCore) {
            return BuildDoctorReport(
                healthScore = if (build.totalCount > 0) 45 else 0,
                statusText = if (build.totalCount > 0) "Eksik Temel Bileşenler (CPU/GPU bekleniyor)" else "Sistem Boş",
                weakestLinkComponent = if (build.cpu == null) "İşlemci (CPU) Eklenmedi" else "Ekran Kartı (GPU) Eklenmedi",
                weakestLinkCategory = if (build.cpu == null) com.panda.myapplication.domain.model.HardwareCategory.CPU else com.panda.myapplication.domain.model.HardwareCategory.GPU,
                resolutionBottlenecks = resBottlenecks,
                upgradeRecommendations = emptyList(),
                doctorDiagnosis = "Sistem sağlığı ve darboğaz analizi için öncelikle CPU ve GPU bileşenlerini seçin.",
            )
        }

        var healthScore = 100
        val recommendations = mutableListOf<UpgradeRecommendation>()
        var weakestLink = "Yok (Bileşenler dengeli)"
        var weakestCategory: com.panda.myapplication.domain.model.HardwareCategory? = null

        // 1. Compatibility check
        if (!platform.isCompatible) {
            healthScore -= 40
            weakestLink = "Platform / Soket Uyuşmazlığı"
            weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.MOTHERBOARD
            recommendations.add(
                UpgradeRecommendation(
                    priority = 1,
                    title = "Anakart / İşlemci Soketini Eşitleyin",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.MOTHERBOARD,
                    expectedGain = "Sistem Çalışabilirlik",
                    roiScore = 10.0,
                    explanation = "Seçilen işlemci (${cpu?.socket}) ile anakart (${mobo?.socket}) soketleri birbiriyle fiziksel olarak uyumlu değil.",
                    suggestedModels = listOf(if (cpu?.socket?.contains("AM5", ignoreCase = true) == true) "B650 / X670 Anakart" else "B760 / Z790 Anakart"),
                )
            )
        }

        // 2. Power check
        if (!cockpit.isSufficient) {
            healthScore -= 25
            val needed = cockpit.recommendedPsuWattage
            weakestLink = "Yetersiz Güç Kaynağı (${psu?.wattage?.watts ?: 0}W < ${needed}W)"
            weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.PSU
            recommendations.add(
                UpgradeRecommendation(
                    priority = 2,
                    title = "Güç Kaynağını (PSU) Yükseltin",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.PSU,
                    expectedGain = "Sistem Kararlılığı & Aşırı Yük Koruması",
                    roiScore = 9.2,
                    explanation = "Sistem yük altında ani tepe güç tüketimine ulaştığında kapanma veya kararsızlık yaşanabilir.",
                    suggestedModels = listOf("${needed}W 80+ Gold veya 850W ATX 3.0 PSU"),
                )
            )
        }

        // 3. Physical clearance
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
                    suggestedModels = listOf("400mm+ GPU destekli Mid-Tower / Full-Tower Kasa"),
                )
            )
        }

        // 4. Bottleneck check
        if (defaultBottleneck.type == BottleneckType.CPU_BOTTLENECK && defaultBottleneck.percentage >= 15) {
            healthScore -= (defaultBottleneck.percentage / 2).coerceAtMost(25)
            weakestLink = "${build.cpu?.displayName ?: "CPU"} (%${defaultBottleneck.percentage} Darboğaz)"
            weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.CPU

            val isAm4 = cpu?.socket?.contains("AM4", ignoreCase = true) == true
            val isAm5 = cpu?.socket?.contains("AM5", ignoreCase = true) == true
            val isIntel = cpu?.socket?.contains("1700", ignoreCase = true) == true

            val suggestedCpu = when {
                isAm4 -> listOf("AMD Ryzen 7 5700X3D", "AMD Ryzen 7 5800X3D")
                isAm5 -> listOf("AMD Ryzen 7 7800X3D", "AMD Ryzen 7 9800X3D")
                isIntel -> listOf("Intel Core i7-14700KF", "Intel Core i5-14600KF")
                else -> listOf("Daha yüksek çekirdek / saat hızlı modern işlemci")
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
            healthScore -= 10
            weakestLink = "${build.gpu?.displayName ?: "GPU"} (%${defaultBottleneck.percentage} GPU Limitli)"
            weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.GPU
            recommendations.add(
                UpgradeRecommendation(
                    priority = 5,
                    title = "Ekran Kartını (GPU) Üst Seviyeye Taşıyın",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.GPU,
                    expectedGain = "+%25-45 Oyun Grafik & FPS Artışı",
                    roiScore = 8.0,
                    explanation = "İşlemciniz çok güçlü, daha yüksek grafik ayarları ve 2K/4K için GPU yükseltmesi yüksek getiri sağlar.",
                    suggestedModels = listOf("NVIDIA GeForce RTX 4070 SUPER", "AMD Radeon RX 7800 XT", "RTX 4070 Ti SUPER"),
                )
            )
        }

        // 5. RAM capacity check
        val ramCapacityGb = (ram?.capacity?.megabytes ?: 0) / 1024
        if (ram != null && ramCapacityGb < 16) {
            healthScore -= 15
            if (weakestCategory == null) {
                weakestLink = "Düşük RAM Kapasitesi (${ramCapacityGb} GB)"
                weakestCategory = com.panda.myapplication.domain.model.HardwareCategory.RAM
            }
            recommendations.add(
                UpgradeRecommendation(
                    priority = 6,
                    title = "RAM Kapasitesini En Az 16GB veya 32GB'a Çıkarın",
                    targetCategory = com.panda.myapplication.domain.model.HardwareCategory.RAM,
                    expectedGain = "Oyun İçi Takılmaların (Stutter) Önlenmesi",
                    roiScore = 9.5,
                    explanation = "Modern oyunlar ve Windows arka plan servisleri için 16GB asgari, 32GB ise ideal kapasitedir.",
                    suggestedModels = listOf("2x16GB 32GB DDR5 6000MHz CL30", "2x8GB 16GB DDR4 3600MHz CL16"),
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

        if (cpu == null || gpu == null || cpuItem == null || gpuItem == null) {
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

        // Base GPU power indexing
        val gpuTdp = gpu.tdp.watts.toDouble().coerceAtLeast(50.0)
        val gpuVramGb = (gpu.vram.megabytes.toDouble() / 1024.0).coerceAtLeast(4.0)
        val gpuClock = (gpu.boostClock?.mhz ?: gpu.coreClock.mhz).toDouble().coerceAtLeast(1200.0)

        val timeSpyScore = gpuItem.benchmarkInfo?.effectiveTimeSpy
            ?: ((gpuTdp * 58.0) + (gpuVramGb * 850.0) + (gpuClock * 2.8)).roundToInt().coerceIn(3500, 39500)

        val cpuClock = (cpu.boostClock?.mhz ?: cpu.baseClock.mhz).toDouble().coerceAtLeast(2000.0)
        val cinebenchScore = cpuItem.benchmarkInfo?.effectiveMultiCore
            ?: (cpu.coreCount * (cpuClock / 1000.0) * 440.0).roundToInt().coerceIn(4000, 42000)

        val gamingRes = when (resolution) {
            ResolutionOption.P1080 -> GamingResolution.P1080
            ResolutionOption.P1440 -> GamingResolution.P1440
            ResolutionOption.P2160 -> GamingResolution.P4K
        }

        // CPU influence factor (X3D cache / high clock boost particularly at 1080p & E-Sports)
        val cpuScore = (cpuItem.benchmarkScore ?: 75).coerceIn(35, 100)
        val x3dBonus = if ((cpu.l3CacheMb ?: 0) >= 64) 1.10 else 1.0
        val cpuEsportsFactor = (((cpuScore / 82.0) * x3dBonus)).coerceIn(0.78, 1.18)
        val cpuAaaFactor = (((cpuScore / 85.0) * (if (x3dBonus > 1.0) 1.04 else 1.0))).coerceIn(0.84, 1.10)

        val gpuMatrix = BenchmarkEngine.resolveGameFpsForItem(gpuItem)
            ?: BenchmarkEngine.buildFull12GameMatrix(ratio = (gpuItem.benchmarkScore ?: 75) / 100.0, isCpu = false)

        val entries = gpuMatrix.toSimulationEntries()

        val totalMultiplier = (preset.fpsMultiplier * upscaling.fpsMultiplier).toDouble()

        val games = entries.map { entry ->
            val factor = if (entry.category == GameCategory.ESPORTS) cpuEsportsFactor else cpuAaaFactor
            val resDampenedFactor = when (resolution) {
                ResolutionOption.P1080 -> factor
                ResolutionOption.P1440 -> 1.0 + (factor - 1.0) * 0.65
                ResolutionOption.P2160 -> 1.0 + (factor - 1.0) * 0.30
            }
            val rawAvg = entry.fps.avgFps(gamingRes) * resDampenedFactor * totalMultiplier
            val avg = rawAvg.roundToInt().coerceAtLeast(18)
            val rawLow1 = entry.fps.low1PctFps(gamingRes, x3dBonus > 1.0) * resDampenedFactor * totalMultiplier
            val low1 = rawLow1.roundToInt().coerceIn(12, avg - 2)
            GameFpsEstimate(
                gameName = entry.title,
                fps = avg,
                low1PctFps = low1,
                settings = "${preset.label} • ${upscaling.label}",
                isEsports = entry.category == GameCategory.ESPORTS,
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
