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
    val isReady: Boolean,
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

    fun calculateBottleneck(build: ActiveBuild): BottleneckAnalysis {
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
        val cpuPower = ((cpu.coreCount * (cpuClock / 1000.0) * 1.5) + l3Bonus).coerceIn(10.0, 100.0)

        val gpuTdp = gpu.tdp.watts.toDouble()
        val gpuVram = gpu.vram.megabytes.toDouble() / 1024.0
        val gpuClock = (gpu.boostClock?.mhz ?: gpu.coreClock.mhz).toDouble()
        val gpuPower = ((gpuTdp * 0.22) + (gpuVram * 2.8) + (gpuClock / 100.0)).coerceIn(10.0, 100.0)

        val delta = ((cpuPower - gpuPower) / maxOf(cpuPower, gpuPower)) * 100.0
        val absDelta = abs(delta).roundToInt().coerceIn(1, 45)

        return when {
            absDelta <= 8 -> BottleneckAnalysis(
                percentage = absDelta,
                type = BottleneckType.BALANCED,
                message = "%$absDelta - Mükemmel Denge",
            )
            delta < -8 -> BottleneckAnalysis(
                percentage = absDelta,
                type = BottleneckType.CPU_BOTTLENECK,
                message = "%$absDelta - CPU Darboğazı Riski (GPU daha güçlü)",
            )
            else -> BottleneckAnalysis(
                percentage = absDelta,
                type = BottleneckType.GPU_BOTTLENECK,
                message = "%$absDelta - GPU Hafif Limitli (CPU çok güçlü)",
            )
        }
    }

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

    fun calculateGamingSimulation(
        build: ActiveBuild,
        resolution: ResolutionOption = ResolutionOption.P1440,
    ): GamingSimulationResult {
        val cpuItem = build.cpu
        val gpuItem = build.gpu
        val cpu = cpuItem?.spec as? CpuSpec
        val gpu = gpuItem?.spec as? GpuSpec

        if (cpu == null || gpu == null || cpuItem == null || gpuItem == null) {
            return GamingSimulationResult(
                resolution = resolution,
                isReady = false,
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
        val games = entries.map { entry ->
            val factor = if (entry.category == GameCategory.ESPORTS) cpuEsportsFactor else cpuAaaFactor
            val resDampenedFactor = when (resolution) {
                ResolutionOption.P1080 -> factor
                ResolutionOption.P1440 -> 1.0 + (factor - 1.0) * 0.65
                ResolutionOption.P2160 -> 1.0 + (factor - 1.0) * 0.30
            }
            val avg = (entry.fps.avgFps(gamingRes) * resDampenedFactor).roundToInt().coerceAtLeast(18)
            val low1 = (entry.fps.low1PctFps(gamingRes, x3dBonus > 1.0) * resDampenedFactor).roundToInt().coerceIn(12, avg - 2)
            GameFpsEstimate(
                gameName = entry.title,
                fps = avg,
                low1PctFps = low1,
                settings = entry.presetSubtitle,
                isEsports = entry.category == GameCategory.ESPORTS,
            )
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
            isReady = true,
            games = games,
            synthetics = synthetics,
        )
    }
}
