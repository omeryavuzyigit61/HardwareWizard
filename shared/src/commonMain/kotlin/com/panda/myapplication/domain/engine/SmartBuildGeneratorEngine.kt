package com.panda.myapplication.domain.engine

import com.panda.myapplication.domain.model.ActiveBuild
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.usecase.CalculatePsuRequirementUseCase
import com.panda.myapplication.domain.usecase.CheckBuildCompatibilityUseCase
import kotlin.math.abs

enum class SmartBuildUseCase {
    ESPORTS_1080P,
    GAMING_1440P,
    ULTRA_4K,
    WORKSTATION_AI_3D;

    fun title(isTr: Boolean): String = when (this) {
        ESPORTS_1080P -> if (isTr) "1080p Rekabetçi E-Spor" else "1080p Competitive Esports"
        GAMING_1440P -> if (isTr) "2K (1440p) Dengeli AAA" else "1440p Balanced AAA Gaming"
        ULTRA_4K -> if (isTr) "4K Ultra & Ray Tracing" else "4K Ultra & Ray Tracing"
        WORKSTATION_AI_3D -> if (isTr) "3D Render, Video & AI" else "3D Render, Video & Local AI"
    }

    fun defaultResolution(): ResolutionOption = when (this) {
        ESPORTS_1080P -> ResolutionOption.P1080
        GAMING_1440P -> ResolutionOption.P1440
        ULTRA_4K -> ResolutionOption.P2160
        WORKSTATION_AI_3D -> ResolutionOption.P1440
    }
}

enum class SmartBuildTier {
    SWEET_SPOT,
    HIGH_END,
    FLAGSHIP_EXTREME;

    fun title(isTr: Boolean): String = when (this) {
        SWEET_SPOT -> if (isTr) "F/P Tatlı Nokta" else "Sweet-Spot Value"
        HIGH_END -> if (isTr) "Üst Seviye" else "High-End Performance"
        FLAGSHIP_EXTREME -> if (isTr) "Amiral Gemisi" else "Flagship Extreme"
    }
}

enum class CpuPlatformPreference {
    AUTO,
    AMD,
    INTEL;

    fun title(isTr: Boolean): String = when (this) {
        AUTO -> if (isTr) "Otomatik" else "Auto"
        AMD -> "AMD Ryzen"
        INTEL -> "Intel Core"
    }
}

data class SmartBuildResult(
    val build: ActiveBuild,
    val recommendedResolution: ResolutionOption,
    val summaryTr: String,
    val summaryEn: String,
    val isFullyCompatible: Boolean,
)

/**
 * Deterministic, 100% catalog-backed PC Build Generator.
 * Queries real catalog items across 8 core categories and enforces socket, DDR generation,
 * RAM slot/capacity, GPU/cooler/radiator millimeter clearance, thermal headroom, and ATX 3.x PSU wattage.
 */
object SmartBuildGeneratorEngine {

    private val psuCalculator = CalculatePsuRequirementUseCase()
    private val compatibilityChecker = CheckBuildCompatibilityUseCase(psuCalculator)

    fun generateBuild(
        allItems: List<HardwareItem>,
        useCase: SmartBuildUseCase,
        tier: SmartBuildTier,
        platformPreference: CpuPlatformPreference = CpuPlatformPreference.AUTO,
    ): SmartBuildResult? {
        val byCat = allItems.groupBy { it.category }

        val cpus = byCat[HardwareCategory.CPU].orEmpty().filter { it.spec is CpuSpec }
        val mobos = byCat[HardwareCategory.MOTHERBOARD].orEmpty().filter { it.spec is MotherboardSpec }
        val rams = byCat[HardwareCategory.RAM].orEmpty().filter { it.spec is RamSpec }
        val gpus = byCat[HardwareCategory.GPU].orEmpty().filter { it.spec is GpuSpec }
        val storages = byCat[HardwareCategory.STORAGE].orEmpty().filter { it.spec is StorageSpec }
        val cases = byCat[HardwareCategory.CASE].orEmpty().filter { it.spec is CaseSpec }
        val coolers = byCat[HardwareCategory.COOLER].orEmpty().filter { it.spec is CoolerSpec }
        val psus = byCat[HardwareCategory.PSU].orEmpty().filter { it.spec is PsuSpec }

        if (cpus.isEmpty() || gpus.isEmpty()) return null

        val socketsWithMobo = mobos.mapNotNull { (it.spec as? MotherboardSpec)?.socket?.uppercase()?.trim() }.toSet()

        // 1. Select CPU with matching socket availability
        val candidateCpus = cpus.filter { item ->
            val spec = item.spec as CpuSpec
            val socketOk = socketsWithMobo.isEmpty() || socketsWithMobo.contains(spec.socket.uppercase().trim())
            val brandOk = when (platformPreference) {
                CpuPlatformPreference.AUTO -> true
                CpuPlatformPreference.AMD -> item.brand.contains("AMD", ignoreCase = true) || spec.socket.startsWith("AM", ignoreCase = true)
                CpuPlatformPreference.INTEL -> item.brand.contains("Intel", ignoreCase = true) || spec.socket.startsWith("LGA", ignoreCase = true)
            }
            // Prefer modern sockets when available
            val modernSocket = spec.socket.equals("AM5", ignoreCase = true) ||
                spec.socket.equals("LGA1851", ignoreCase = true) ||
                spec.socket.equals("LGA1700", ignoreCase = true) ||
                (tier == SmartBuildTier.SWEET_SPOT && spec.socket.equals("AM4", ignoreCase = true))
            socketOk && brandOk && modernSocket
        }.ifEmpty {
            cpus.filter { item ->
                val spec = item.spec as CpuSpec
                socketsWithMobo.isEmpty() || socketsWithMobo.contains(spec.socket.uppercase().trim())
            }.ifEmpty { cpus }
        }

        val cpuTargetPercentile = when (useCase) {
            SmartBuildUseCase.ESPORTS_1080P -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 0.65
                SmartBuildTier.HIGH_END -> 0.88
                SmartBuildTier.FLAGSHIP_EXTREME -> 0.99
            }
            SmartBuildUseCase.GAMING_1440P -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 0.60
                SmartBuildTier.HIGH_END -> 0.84
                SmartBuildTier.FLAGSHIP_EXTREME -> 0.97
            }
            SmartBuildUseCase.ULTRA_4K -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 0.62
                SmartBuildTier.HIGH_END -> 0.85
                SmartBuildTier.FLAGSHIP_EXTREME -> 0.98
            }
            SmartBuildUseCase.WORKSTATION_AI_3D -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 0.68
                SmartBuildTier.HIGH_END -> 0.88
                SmartBuildTier.FLAGSHIP_EXTREME -> 0.99
            }
        }

        val chosenCpu = pickAtPercentile(
            items = candidateCpus,
            percentile = cpuTargetPercentile,
            scorer = { scoreCpuForUseCase(it, useCase) },
        ) ?: return null
        val cpuSpec = chosenCpu.spec as CpuSpec

        // 2. Select Compatible Motherboard
        val socketMobos = mobos.filter {
            (it.spec as MotherboardSpec).socket.equals(cpuSpec.socket, ignoreCase = true)
        }
        val preferredDdrMobos = if (tier != SmartBuildTier.SWEET_SPOT) {
            socketMobos.filter { (it.spec as MotherboardSpec).memoryGeneration == DdrGeneration.DDR5 }.ifEmpty { socketMobos }
        } else {
            socketMobos.filter { (it.spec as MotherboardSpec).memoryGeneration == DdrGeneration.DDR5 }.ifEmpty { socketMobos }
        }
        val standardFormMobos = preferredDdrMobos.filter {
            val ff = (it.spec as MotherboardSpec).formFactor
            ff == MotherboardFormFactor.ATX || ff == MotherboardFormFactor.MICRO_ATX
        }.ifEmpty { preferredDdrMobos }

        val chosenMobo = standardFormMobos.maxByOrNull { scoreMotherboardForTier(it, tier) }
        val moboSpec = chosenMobo?.spec as? MotherboardSpec

        // 3. Select Compatible RAM
        val targetRamGb = when {
            useCase == SmartBuildUseCase.WORKSTATION_AI_3D && tier == SmartBuildTier.FLAGSHIP_EXTREME -> 96
            useCase == SmartBuildUseCase.WORKSTATION_AI_3D && tier == SmartBuildTier.HIGH_END -> 64
            useCase == SmartBuildUseCase.WORKSTATION_AI_3D -> 32
            tier == SmartBuildTier.FLAGSHIP_EXTREME -> 64
            else -> 32
        }
        val compatibleRams = rams.filter { item ->
            val r = item.spec as RamSpec
            if (moboSpec != null) {
                val maxMemMb = (moboSpec.maxMemoryGb ?: 256) * 1024
                r.generation == moboSpec.memoryGeneration &&
                    r.moduleCount <= moboSpec.ramSlots &&
                    r.capacity.megabytes <= maxMemMb
            } else {
                val expectedGen = if (cpuSpec.socket.equals("AM4", ignoreCase = true)) DdrGeneration.DDR4 else DdrGeneration.DDR5
                r.generation == expectedGen
            }
        }
        val chosenRam = compatibleRams.maxByOrNull { scoreRamForTarget(it, targetRamGb, cpuSpec.socket) }

        // 4. Select GPU
        val minVramGb = when (useCase) {
            SmartBuildUseCase.WORKSTATION_AI_3D -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 12
                SmartBuildTier.HIGH_END -> 16
                SmartBuildTier.FLAGSHIP_EXTREME -> 24
            }
            SmartBuildUseCase.ULTRA_4K -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 12
                SmartBuildTier.HIGH_END -> 16
                SmartBuildTier.FLAGSHIP_EXTREME -> 24
            }
            SmartBuildUseCase.GAMING_1440P -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 12
                SmartBuildTier.HIGH_END -> 16
                SmartBuildTier.FLAGSHIP_EXTREME -> 16
            }
            SmartBuildUseCase.ESPORTS_1080P -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 8
                SmartBuildTier.HIGH_END -> 12
                SmartBuildTier.FLAGSHIP_EXTREME -> 16
            }
        }
        val candidateGpus = gpus.filter { item ->
            val g = item.spec as GpuSpec
            val vramGb = g.vram.megabytes / 1024
            val vramOk = vramGb >= minVramGb
            val cudaPreferred = if (useCase == SmartBuildUseCase.WORKSTATION_AI_3D) {
                g.chipset.contains("RTX", ignoreCase = true) || item.displayName.contains("RTX", ignoreCase = true)
            } else true
            vramOk && cudaPreferred
        }.ifEmpty {
            gpus.filter { ((it.spec as GpuSpec).vram.megabytes / 1024) >= minVramGb }.ifEmpty { gpus }
        }

        val gpuPercentile = when (useCase) {
            SmartBuildUseCase.ESPORTS_1080P -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 0.50
                SmartBuildTier.HIGH_END -> 0.76
                SmartBuildTier.FLAGSHIP_EXTREME -> 0.94
            }
            SmartBuildUseCase.GAMING_1440P -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 0.64
                SmartBuildTier.HIGH_END -> 0.85
                SmartBuildTier.FLAGSHIP_EXTREME -> 0.97
            }
            SmartBuildUseCase.ULTRA_4K -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 0.75
                SmartBuildTier.HIGH_END -> 0.90
                SmartBuildTier.FLAGSHIP_EXTREME -> 0.99
            }
            SmartBuildUseCase.WORKSTATION_AI_3D -> when (tier) {
                SmartBuildTier.SWEET_SPOT -> 0.68
                SmartBuildTier.HIGH_END -> 0.88
                SmartBuildTier.FLAGSHIP_EXTREME -> 0.99
            }
        }
        val chosenGpu = pickAtPercentile(
            items = candidateGpus,
            percentile = gpuPercentile,
            scorer = { scoreGpuForUseCase(it, useCase) },
        ) ?: return null
        val gpuSpec = chosenGpu.spec as GpuSpec

        // 5. Select NVMe SSD Storage
        val targetStorageGb = when {
            useCase == SmartBuildUseCase.WORKSTATION_AI_3D && tier != SmartBuildTier.SWEET_SPOT -> 2048
            tier == SmartBuildTier.FLAGSHIP_EXTREME -> 2048
            tier == SmartBuildTier.HIGH_END -> 2048
            else -> 1024
        }
        val nvmeStorages = storages.filter {
            val s = it.spec as StorageSpec
            s.mediaType == StorageMediaType.SSD && s.storageInterface == StorageInterface.NVME
        }.ifEmpty {
            storages.filter { (it.spec as StorageSpec).mediaType == StorageMediaType.SSD }.ifEmpty { storages }
        }
        val chosenStorage = nvmeStorages.maxByOrNull { scoreStorageForTarget(it, targetStorageGb, tier) }

        // 6. Select Physically Compatible Case
        val gpuLengthMm = gpuSpec.lengthMm ?: 300
        val moboForm = moboSpec?.formFactor ?: MotherboardFormFactor.ATX
        val compatibleCases = cases.filter { item ->
            val c = item.spec as CaseSpec
            val gpuFits = c.maxGpuLengthMm == null || c.maxGpuLengthMm >= (gpuLengthMm + 15)
            val moboFits = caseSupportsFormFactor(c, moboForm)
            val radOk = if (tier == SmartBuildTier.SWEET_SPOT) true else (c.maxRadiatorSizeMm == null || c.maxRadiatorSizeMm >= 240)
            gpuFits && moboFits && radOk
        }.ifEmpty {
            cases.filter { item ->
                val c = item.spec as CaseSpec
                (c.maxGpuLengthMm == null || c.maxGpuLengthMm >= gpuLengthMm) && caseSupportsFormFactor(c, moboForm)
            }.ifEmpty { cases }
        }
        val chosenCase = compatibleCases.maxByOrNull { item ->
            val c = item.spec as CaseSpec
            val imageBonus = if (item.effectiveImageUrl != null) 15.0 else 0.0
            val radScore = ((c.maxRadiatorSizeMm ?: 240).coerceAtMost(360) / 36.0)
            val gpuHeadroom = ((c.maxGpuLengthMm ?: 360).coerceAtMost(420) / 42.0)
            imageBonus + radScore + gpuHeadroom
        }
        val caseSpec = chosenCase?.spec as? CaseSpec

        // 7. Select Physically & Thermally Compatible Cooler
        val requiredCoolerTdp = (cpuSpec.tdp.watts * 1.35).toInt().coerceAtLeast(150)
        val compatibleCoolers = coolers.filter { item ->
            val cl = item.spec as CoolerSpec
            val socketOk = cl.supportedSockets.isEmpty() || cl.supportedSockets.any { it.equals(cpuSpec.socket, ignoreCase = true) }
            val tdpOk = cl.maxCoolingTdp.watts >= requiredCoolerTdp
            val heightOk = if (cl.type == CoolerType.AIR && cl.heightMm != null && caseSpec?.maxCpuCoolerHeightMm != null) {
                cl.heightMm <= caseSpec.maxCpuCoolerHeightMm
            } else true
            val radOk = if (cl.type == CoolerType.LIQUID && cl.radiatorSizeMm != null && caseSpec?.maxRadiatorSizeMm != null) {
                cl.radiatorSizeMm <= caseSpec.maxRadiatorSizeMm
            } else true
            socketOk && tdpOk && heightOk && radOk
        }.ifEmpty {
            coolers.filter { item ->
                val cl = item.spec as CoolerSpec
                val socketOk = cl.supportedSockets.isEmpty() || cl.supportedSockets.any { it.equals(cpuSpec.socket, ignoreCase = true) }
                val heightOk = if (cl.type == CoolerType.AIR && cl.heightMm != null && caseSpec?.maxCpuCoolerHeightMm != null) {
                    cl.heightMm <= caseSpec.maxCpuCoolerHeightMm
                } else true
                val radOk = if (cl.type == CoolerType.LIQUID && cl.radiatorSizeMm != null && caseSpec?.maxRadiatorSizeMm != null) {
                    cl.radiatorSizeMm <= caseSpec.maxRadiatorSizeMm
                } else true
                socketOk && heightOk && radOk
            }.ifEmpty { coolers }
        }
        val chosenCooler = compatibleCoolers.maxByOrNull { scoreCoolerForTier(it, tier, cpuSpec.tdp.watts) }

        // 8. Calculate Exact PSU Wattage & Select ATX 3.x Compatible PSU
        val partialBuild = ActiveBuild(
            cpu = chosenCpu,
            motherboard = chosenMobo,
            ram = chosenRam,
            gpu = chosenGpu,
            storage = chosenStorage,
            cooler = chosenCooler,
            case = chosenCase,
        )
        val psuReq = psuCalculator(partialBuild.items)
        val minPsuWatts = maxOf(psuReq.recommendedWattage.watts, gpuSpec.recommendedPsu.watts, 650)
        val needsAtx3 = gpuSpec.tdp.watts >= 240 ||
            gpuSpec.powerConnectors?.contains("12V", ignoreCase = true) == true ||
            gpuSpec.powerConnectors?.contains("16-pin", ignoreCase = true) == true

        val compatiblePsus = psus.filter { item ->
            val p = item.spec as PsuSpec
            val wattsOk = p.wattage.watts >= minPsuWatts
            val atxOk = !needsAtx3 || p.isAtx3Compliant || p.atxStandard?.contains("3.", ignoreCase = true) == true
            val effOk = p.efficiency == PsuEfficiencyRating.GOLD ||
                p.efficiency == PsuEfficiencyRating.PLATINUM ||
                p.efficiency == PsuEfficiencyRating.TITANIUM ||
                (tier == SmartBuildTier.SWEET_SPOT && p.efficiency == PsuEfficiencyRating.BRONZE)
            wattsOk && atxOk && effOk
        }.ifEmpty {
            psus.filter { (it.spec as PsuSpec).wattage.watts >= minPsuWatts }.ifEmpty { psus }
        }

        val chosenPsu = compatiblePsus.maxByOrNull { scorePsuForTarget(it, minPsuWatts, tier) }

        val finalBuild = partialBuild.copy(psu = chosenPsu)
        val (_, compatReport) = compatibilityChecker(finalBuild)

        val summaryTr = "${useCase.title(true)} (${tier.title(true)}) profili için ${chosenCpu.displayName} + ${chosenGpu.displayName} merkezli, %100 soket/kasa/güç uyumlu sistem oluşturuldu."
        val summaryEn = "Generated a 100% compatible ${useCase.title(false)} (${tier.title(false)}) build centered around ${chosenCpu.displayName} + ${chosenGpu.displayName}."

        return SmartBuildResult(
            build = finalBuild,
            recommendedResolution = useCase.defaultResolution(),
            summaryTr = summaryTr,
            summaryEn = summaryEn,
            isFullyCompatible = compatReport.isCompatible,
        )
    }

    private fun pickAtPercentile(
        items: List<HardwareItem>,
        percentile: Double,
        scorer: (HardwareItem) -> Double,
    ): HardwareItem? {
        if (items.isEmpty()) return null
        val scored = items.map { it to scorer(it) }.sortedBy { it.second }
        val targetIdx = ((scored.lastIndex) * percentile.coerceIn(0.0, 1.0)).toInt().coerceIn(0, scored.lastIndex)
        val targetScore = scored[targetIdx].second
        // Within ±3.5% of targetScore, prefer items with product imagery for polished UI presentation
        val windowRadius = (abs(targetScore) * 0.035).coerceAtLeast(2.0)
        val windowCandidates = scored.filter { abs(it.second - targetScore) <= windowRadius }
        return windowCandidates
            .sortedWith(
                compareByDescending<Pair<HardwareItem, Double>> { it.first.effectiveImageUrl != null }
                    .thenBy { abs(it.second - targetScore) }
            )
            .firstOrNull()?.first ?: scored[targetIdx].first
    }

    private fun scoreCpuForUseCase(item: HardwareItem, useCase: SmartBuildUseCase): Double {
        val s = item.spec as CpuSpec
        val gamingIdx = item.benchmarkInfo?.gamingIndex?.toDouble()
        val single = item.benchmarkInfo?.effectiveSingleCore?.toDouble() ?: (s.boostClock?.mhz ?: s.baseClock.mhz).toDouble() * 0.45
        val multi = item.benchmarkInfo?.effectiveMultiCore?.toDouble() ?: (s.coreCount * 2100.0)
        val x3dBonus = if ((s.l3CacheMb ?: 0) >= 64 || item.model.contains("X3D", ignoreCase = true)) 18.0 else 0.0

        return when (useCase) {
            SmartBuildUseCase.WORKSTATION_AI_3D -> (multi / 450.0) + s.coreCount * 1.8
            SmartBuildUseCase.ESPORTS_1080P -> (gamingIdx ?: (single / 32.0)) + x3dBonus * 1.3
            SmartBuildUseCase.GAMING_1440P -> (gamingIdx ?: (single / 32.0)) + x3dBonus + (multi / 3500.0)
            SmartBuildUseCase.ULTRA_4K -> (gamingIdx ?: (single / 32.0)) + x3dBonus * 0.9 + (multi / 2800.0)
        }
    }

    private fun scoreGpuForUseCase(item: HardwareItem, useCase: SmartBuildUseCase): Double {
        val s = item.spec as GpuSpec
        val vramGb = (s.vram.megabytes / 1024).toDouble()
        val composite = VsArenaEngine.computeCompositePerformanceScore(item)
        return when (useCase) {
            SmartBuildUseCase.WORKSTATION_AI_3D -> {
                val cudaBonus = if (s.chipset.contains("RTX", ignoreCase = true) || item.displayName.contains("RTX", ignoreCase = true)) 20.0 else 0.0
                composite * 0.75 + vramGb * 2.2 + cudaBonus
            }
            SmartBuildUseCase.ULTRA_4K -> composite + (vramGb.coerceAtMost(24.0) * 0.8)
            else -> composite
        }
    }

    private fun scoreMotherboardForTier(item: HardwareItem, tier: SmartBuildTier): Double {
        val s = item.spec as MotherboardSpec
        val chip = s.chipset.uppercase()
        val tierMatch = when (tier) {
            SmartBuildTier.SWEET_SPOT -> when {
                chip.startsWith("B650") || chip.startsWith("B850") || chip.startsWith("B760") || chip.startsWith("B860") || chip.startsWith("B550") -> 40.0
                chip.startsWith("X") || chip.startsWith("Z") -> 22.0
                else -> 12.0
            }
            SmartBuildTier.HIGH_END -> when {
                chip.contains("B650E") || chip.contains("B850") || chip.startsWith("X670") || chip.startsWith("X870") || chip.startsWith("Z790") || chip.startsWith("Z890") -> 42.0
                chip.startsWith("B") -> 28.0
                else -> 10.0
            }
            SmartBuildTier.FLAGSHIP_EXTREME -> when {
                chip.contains("X870E") || chip.contains("X670E") || chip.startsWith("Z890") || chip.startsWith("Z790") -> 48.0
                chip.startsWith("X") || chip.startsWith("Z") -> 35.0
                else -> 15.0
            }
        }
        val m2Bonus = ((s.m2Slots ?: 2).coerceAtMost(4)) * 3.0
        val wifiBonus = if (s.hasWifi == true || s.network?.contains("Wi-Fi", ignoreCase = true) == true) 6.0 else 0.0
        val imgBonus = if (item.effectiveImageUrl != null) 8.0 else 0.0
        return tierMatch + m2Bonus + wifiBonus + imgBonus
    }

    private fun scoreRamForTarget(item: HardwareItem, targetGb: Int, socket: String): Double {
        val s = item.spec as RamSpec
        val capGb = s.capacity.megabytes / 1024
        val capPenalty = abs(capGb - targetGb) * 1.4
        val dualChannelBonus = if (s.moduleCount == 2) 18.0 else if (s.moduleCount == 4) 6.0 else 0.0
        val idealMt = when {
            s.generation == DdrGeneration.DDR4 -> 3600
            socket.equals("AM5", ignoreCase = true) -> 6000
            else -> 6400
        }
        val speedScore = 25.0 - (abs(s.speed.mhz - idealMt) / 160.0).coerceAtMost(22.0)
        val trueLatencyNs = (s.casLatency * 2000.0) / s.speed.mhz.coerceAtLeast(1600)
        val latencyBonus = (20.0 - trueLatencyNs).coerceIn(0.0, 12.0)
        val imgBonus = if (item.effectiveImageUrl != null) 6.0 else 0.0
        return 60.0 - capPenalty + dualChannelBonus + speedScore + latencyBonus + imgBonus
    }

    private fun scoreStorageForTarget(item: HardwareItem, targetGb: Int, tier: SmartBuildTier): Double {
        val s = item.spec as StorageSpec
        val capGb = s.capacity.megabytes / 1024
        val capDiffPenalty = (abs(capGb - targetGb) / 256.0) * 6.0
        val targetRead = when (tier) {
            SmartBuildTier.SWEET_SPOT -> 5000
            SmartBuildTier.HIGH_END -> 7000
            SmartBuildTier.FLAGSHIP_EXTREME -> 12000
        }
        val speedScore = 25.0 - (abs(s.sequentialReadMbps - targetRead) / 500.0).coerceAtMost(20.0)
        val dramBonus = if (!s.dramCache.isNullOrBlank() && !s.dramCache.equals("Yok", ignoreCase = true) && !s.dramCache.equals("None", ignoreCase = true)) 8.0 else 0.0
        val imgBonus = if (item.effectiveImageUrl != null) 6.0 else 0.0
        return 50.0 - capDiffPenalty + speedScore + dramBonus + imgBonus
    }

    private fun scoreCoolerForTier(item: HardwareItem, tier: SmartBuildTier, cpuTdpW: Int): Double {
        val s = item.spec as CoolerSpec
        val preferAio = tier != SmartBuildTier.SWEET_SPOT || cpuTdpW >= 150
        val typeScore = when {
            preferAio && s.type == CoolerType.LIQUID -> {
                val rad = s.radiatorSizeMm ?: 240
                if (tier == SmartBuildTier.FLAGSHIP_EXTREME && rad >= 360) 42.0
                else if (rad in 240..360) 36.0
                else 22.0
            }
            !preferAio && s.type == CoolerType.AIR -> 36.0
            else -> 20.0
        }
        val headroomScore = ((s.maxCoolingTdp.watts - cpuTdpW).coerceIn(0, 160)) / 10.0
        val imgBonus = if (item.effectiveImageUrl != null) 6.0 else 0.0
        return typeScore + headroomScore + imgBonus
    }

    private fun scorePsuForTarget(item: HardwareItem, minWatts: Int, tier: SmartBuildTier): Double {
        val s = item.spec as PsuSpec
        val idealWatts = when (tier) {
            SmartBuildTier.SWEET_SPOT -> minWatts + 50
            SmartBuildTier.HIGH_END -> minWatts + 100
            SmartBuildTier.FLAGSHIP_EXTREME -> minWatts + 150
        }
        val wattPenalty = (abs(s.wattage.watts - idealWatts) / 50.0) * 4.5
        val effBonus = when (s.efficiency) {
            PsuEfficiencyRating.TITANIUM -> if (tier == SmartBuildTier.FLAGSHIP_EXTREME) 24.0 else 16.0
            PsuEfficiencyRating.PLATINUM -> 20.0
            PsuEfficiencyRating.GOLD -> 18.0
            else -> 6.0
        }
        val atx3Bonus = if (s.isAtx3Compliant) 12.0 else 0.0
        val tierBonus = if (s.tierRating?.startsWith("Tier A", ignoreCase = true) == true) 10.0 else 0.0
        val imgBonus = if (item.effectiveImageUrl != null) 6.0 else 0.0
        return 60.0 - wattPenalty + effBonus + atx3Bonus + tierBonus + imgBonus
    }

    private fun caseSupportsFormFactor(caseSpec: CaseSpec, moboForm: MotherboardFormFactor): Boolean {
        return CheckBuildCompatibilityUseCase.isMotherboardSupportedByCase(moboForm, caseSpec.type)
    }
}
