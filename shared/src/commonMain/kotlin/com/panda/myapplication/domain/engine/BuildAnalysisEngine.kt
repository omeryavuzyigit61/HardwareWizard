package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.BuildCompatibilityReport
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
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

data class WattageCockpitInfo(
    val componentTdpSum: Int,
    val peakTdpWithTolerance: Int,
    val recommendedPsuWattage: Int,
    val currentPsuWattage: Int?,
    val isSufficient: Boolean,
)

data class PlatformStatusBadge(
    val platformName: String,
    val isCompatible: Boolean,
    val statusText: String,
)

data class BottleneckAnalysis(
    val percentage: Int,
    val type: BottleneckType,
    val message: String,
)

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

data class GameFpsEstimate(
    val gameName: String,
    val fps: Int,
    val settings: String,
)

data class SyntheticBenchmarkEstimate(
    val name: String,
    val score: Int,
    val unit: String,
)

data class GamingSimulationResult(
    val resolution: ResolutionOption,
    val isReady: Boolean,
    val games: List<GameFpsEstimate>,
    val synthetics: List<SyntheticBenchmarkEstimate>,
)

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
        val cpu = build.cpu?.spec as? CpuSpec
        val gpu = build.gpu?.spec as? GpuSpec

        if (cpu == null || gpu == null) {
            return GamingSimulationResult(
                resolution = resolution,
                isReady = false,
                games = emptyList(),
                synthetics = emptyList(),
            )
        }

        // Base GPU power indexing (score normalized to 100)
        val gpuTdp = gpu.tdp.watts.toDouble().coerceAtLeast(50.0)
        val gpuVramGb = (gpu.vram.megabytes.toDouble() / 1024.0).coerceAtLeast(4.0)
        val gpuClock = (gpu.boostClock?.mhz ?: gpu.coreClock.mhz).toDouble().coerceAtLeast(1200.0)

        // Time Spy Graphics index roughly ~4000 to ~36000
        val timeSpyScore = ((gpuTdp * 58.0) + (gpuVramGb * 850.0) + (gpuClock * 2.8)).roundToInt().coerceIn(3500, 39500)

        // Cinebench R23 Multi-Core roughly ~5000 to ~40000
        val cpuClock = (cpu.boostClock?.mhz ?: cpu.baseClock.mhz).toDouble().coerceAtLeast(2000.0)
        val cinebenchScore = (cpu.coreCount * (cpuClock / 1000.0) * 440.0).roundToInt().coerceIn(4000, 42000)

        // Resolution scaling multipliers for FPS
        val (resMultiplierCyberpunk, resMultiplierWarzone, resMultiplierCs2) = when (resolution) {
            ResolutionOption.P1080 -> Triple(1.00, 1.00, 1.00)
            ResolutionOption.P1440 -> Triple(0.72, 0.76, 0.85)
            ResolutionOption.P2160 -> Triple(0.42, 0.48, 0.62)
        }

        // Estimated FPS at 1080p Ultra
        val baseCyberpunkFps = ((timeSpyScore / 185.0) + (cpu.coreCount * 2.2)).coerceIn(28.0, 165.0)
        val baseWarzoneFps = ((timeSpyScore / 145.0) + (cpu.coreCount * 3.5)).coerceIn(45.0, 240.0)
        val baseCs2Fps = ((timeSpyScore / 95.0) + (cpu.coreCount * 12.0) + (cpuClock / 40.0)).coerceIn(120.0, 520.0)

        val games = listOf(
            GameFpsEstimate(
                gameName = "Cyberpunk 2077",
                fps = (baseCyberpunkFps * resMultiplierCyberpunk).roundToInt(),
                settings = "Ultra + Ray Tracing",
            ),
            GameFpsEstimate(
                gameName = "Call of Duty: Warzone",
                fps = (baseWarzoneFps * resMultiplierWarzone).roundToInt(),
                settings = "Competitive Max",
            ),
            GameFpsEstimate(
                gameName = "Counter-Strike 2",
                fps = (baseCs2Fps * resMultiplierCs2).roundToInt(),
                settings = "Very High",
            ),
        )

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
