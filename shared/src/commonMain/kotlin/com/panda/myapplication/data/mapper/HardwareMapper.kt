package com.panda.myapplication.data.mapper

import com.panda.myapplication.data.local.entity.CaseSpecEntity
import com.panda.myapplication.data.local.entity.CoolerSpecEntity
import com.panda.myapplication.data.local.entity.CpuSpecEntity
import com.panda.myapplication.data.local.entity.GpuSpecEntity
import com.panda.myapplication.data.local.entity.HardwareCatalogSnapshot
import com.panda.myapplication.data.local.entity.HardwareItemEntity
import com.panda.myapplication.data.local.entity.HardwareItemWithSpec
import com.panda.myapplication.data.local.entity.HeadsetSpecEntity
import com.panda.myapplication.data.local.entity.KeyboardSpecEntity
import com.panda.myapplication.data.local.entity.LaptopSpecEntity
import com.panda.myapplication.data.local.entity.MonitorSpecEntity
import com.panda.myapplication.data.local.entity.MotherboardSpecEntity
import com.panda.myapplication.data.local.entity.MouseSpecEntity
import com.panda.myapplication.data.local.entity.PrebuiltSpecEntity
import com.panda.myapplication.data.local.entity.PsuSpecEntity
import com.panda.myapplication.data.local.entity.RamSpecEntity
import com.panda.myapplication.data.local.entity.StorageSpecEntity
import com.panda.myapplication.data.model.dto.CaseRawDto
import com.panda.myapplication.data.model.dto.CoolerRawDto
import com.panda.myapplication.data.model.dto.CpuRawDto
import com.panda.myapplication.data.model.dto.GpuRawDto
import com.panda.myapplication.data.model.dto.LaptopRawDto
import com.panda.myapplication.data.model.dto.MotherboardRawDto
import com.panda.myapplication.data.model.dto.PeripheralRawDto
import com.panda.myapplication.data.model.dto.PrebuiltRawDto
import com.panda.myapplication.data.model.dto.PsuRawDto
import com.panda.myapplication.data.model.dto.RamRawDto
import com.panda.myapplication.data.model.dto.StorageRawDto
import com.panda.myapplication.domain.model.CaseSpec
import com.panda.myapplication.domain.model.CoolerSpec
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.CpuSpec
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.Frequency
import com.panda.myapplication.domain.model.GpuSpec
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.HardwareId
import com.panda.myapplication.domain.model.HardwareItem
import com.panda.myapplication.domain.model.HeadsetSpec
import com.panda.myapplication.domain.model.KeyboardSpec
import com.panda.myapplication.domain.model.LaptopSpec
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PrebuiltSpec
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.PricePoint
import com.panda.myapplication.domain.model.ProductPriceInfo
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.HardwareSpec
import com.panda.myapplication.domain.model.StorageFormFactor
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.StoreOffer
import com.panda.myapplication.domain.model.Wattage
import kotlin.time.Instant

object HardwareMapper {

    // region Room -> Domain

    fun toDomain(itemWithSpec: HardwareItemWithSpec): HardwareItem? = runCatching {
        val entity = itemWithSpec.item
        if (entity.brand.isBlank() || entity.model.isBlank()) return@runCatching null

        val spec: HardwareSpec = when {
            itemWithSpec.cpu != null -> {
                val c = itemWithSpec.cpu
                val base = c.baseClockMhz.coerceAtLeast(500)
                val boost = c.boostClockMhz?.coerceAtLeast(base)
                CpuSpec(
                    socket = c.socket.ifBlank { "Bilinmeyen Soket" },
                    coreCount = c.coreCount.coerceAtLeast(1),
                    threadCount = c.threadCount.coerceAtLeast(1),
                    baseClock = Frequency(base),
                    boostClock = boost?.let { Frequency(it) },
                    tdp = Wattage(c.tdpWatts.coerceAtLeast(0)),
                    hasIntegratedGraphics = c.hasIntegratedGraphics,
                    architecture = c.architecture,
                    l2CacheMb = c.l2CacheMb,
                    l3CacheMb = c.l3CacheMb,
                    processNode = c.processNode,
                    pcieVersion = c.pcieVersion,
                    maxMemorySpeed = c.maxMemorySpeed,
                    unlockedMultiplier = c.unlockedMultiplier,
                    coreTopology = c.coreTopology,
                    powerLimits = c.powerLimits,
                    igpuModel = c.igpuModel,
                )
            }
            itemWithSpec.gpu != null -> {
                val g = itemWithSpec.gpu
                val core = g.coreClockMhz.coerceAtLeast(300)
                val boost = g.boostClockMhz?.coerceAtLeast(core)
                GpuSpec(
                    chipset = g.chipset.ifBlank { entity.model },
                    vram = MemorySize(g.vramMb.coerceAtLeast(1024)),
                    memoryType = g.memoryType.ifBlank { "GDDR6" },
                    coreClock = Frequency(core),
                    boostClock = boost?.let { Frequency(it) },
                    tdp = Wattage(g.tdpWatts.coerceAtLeast(0)),
                    recommendedPsu = Wattage(g.recommendedPsuWatts.coerceAtLeast(0)),
                    busWidthBits = g.busWidthBits,
                    lengthMm = g.lengthMm,
                    powerConnectors = g.powerConnectors,
                    displayOutputs = g.displayOutputs,
                    slotWidth = g.slotWidth,
                    partNumber = g.partNumber,
                    architecture = g.architecture,
                    processNode = g.processNode,
                    cudaCores = g.cudaCores,
                    tensorAiTops = g.tensorAiTops,
                    rtCores = g.rtCores,
                    memorySpeedGbps = g.memorySpeedGbps,
                    bandwidthGbs = g.bandwidthGbs,
                )
            }
            itemWithSpec.ram != null -> {
                val r = itemWithSpec.ram
                RamSpec(
                    generation = r.generation,
                    speed = Frequency(r.speedMts.coerceAtLeast(800)),
                    casLatency = r.casLatency.coerceAtLeast(1),
                    capacity = MemorySize(r.capacityMb.coerceAtLeast(1024)),
                    moduleCount = r.moduleCount.coerceAtLeast(1),
                    firstWordLatencyNs = r.firstWordLatencyNs,
                    voltage = r.voltage,
                    heatSpreader = r.heatSpreader,
                    rgb = r.rgb,
                    dieType = r.dieType,
                    timings = r.timings,
                    profile = r.profile,
                )
            }
            itemWithSpec.motherboard != null -> {
                val m = itemWithSpec.motherboard
                MotherboardSpec(
                    socket = m.socket.ifBlank { "Bilinmeyen Soket" },
                    chipset = m.chipset.ifBlank { "Bilinmeyen Chipset" },
                    formFactor = m.formFactor,
                    memoryGeneration = m.memoryGeneration,
                    ramSlots = m.ramSlots.coerceAtLeast(1),
                    pcieGeneration = m.pcieGeneration,
                    maxMemoryGb = m.maxMemoryGb,
                    m2Slots = m.m2Slots,
                    pcieSlotCount = m.pcieSlotCount,
                    sataPorts = m.sataPorts,
                    hasWifi = m.hasWifi,
                    vrmPhases = m.vrmPhases,
                    interfacePorts = m.interfacePorts,
                    pcbLayers = m.pcbLayers,
                    m2SlotsDetail = m.m2SlotsDetail,
                    memorySpeedOc = m.memorySpeedOc,
                    network = m.network,
                    audioCodec = m.audioCodec,
                )
            }
            itemWithSpec.psu != null -> {
                val p = itemWithSpec.psu
                PsuSpec(
                    wattage = Wattage(p.wattage.coerceAtLeast(100)),
                    efficiency = p.efficiency,
                    isAtx3Compliant = p.isAtx3Compliant,
                    supportsPcie5Power = p.supportsPcie5Power,
                    modularity = p.modularity,
                    formFactor = p.formFactor,
                    outputs = p.outputs,
                    tierRating = p.tierRating,
                    capacitors = p.capacitors,
                    atxStandard = p.atxStandard,
                    pcie12Vhpwr = p.pcie12Vhpwr,
                    epsConnectors = p.epsConnectors,
                    pcieConnectors = p.pcieConnectors,
                    sataConnectors = p.sataConnectors,
                    topology = p.topology,
                    protections = p.protections,
                    fanAndSize = p.fanAndSize,
                    cybeneticsCert = p.cybeneticsCert,
                    cybeneticsLambda = p.cybeneticsLambda,
                )
            }
            itemWithSpec.storage != null -> {
                val s = itemWithSpec.storage
                StorageSpec(
                    mediaType = s.mediaType,
                    storageInterface = s.storageInterface,
                    formFactor = s.formFactor,
                    pcieGeneration = s.pcieGeneration,
                    capacity = MemorySize(s.capacityMb.coerceAtLeast(1024)),
                    sequentialReadMbps = s.sequentialReadMbps.coerceAtLeast(0),
                    sequentialWriteMbps = s.sequentialWriteMbps.coerceAtLeast(0),
                    nandType = s.nandType,
                    enduranceTbw = s.enduranceTbw,
                    cacheMb = s.cacheMb,
                    controller = s.controller,
                    dramCache = s.dramCache,
                    randomIops = s.randomIops,
                )
            }
            itemWithSpec.cooler != null -> {
                val cl = itemWithSpec.cooler
                val radSize = cl.radiatorSizeMm?.takeIf { it > 0 }
                CoolerSpec(
                    type = cl.type,
                    radiatorSizeMm = radSize,
                    maxCoolingTdp = Wattage(cl.maxCoolingTdpWatts.coerceAtLeast(0)),
                    supportedSockets = cl.supportedSockets,
                    heightMm = cl.heightMm,
                    fanRpm = cl.fanRpm,
                    noiseDba = cl.noiseDba,
                    radiatorDimensions = cl.radiatorDimensions,
                    fanAirflow = cl.fanAirflow,
                )
            }
            itemWithSpec.case != null -> {
                val cs = itemWithSpec.case
                CaseSpec(
                    type = cs.type,
                    maxGpuLengthMm = cs.maxGpuLengthMm,
                    maxCpuCoolerHeightMm = cs.maxCpuCoolerHeightMm,
                    maxRadiatorSizeMm = cs.maxRadiatorSizeMm,
                    sidePanel = cs.sidePanel,
                    driveBays = cs.driveBays,
                    radiatorSupport = cs.radiatorSupport,
                    includedFans = cs.includedFans,
                    frontPanelUsb = cs.frontPanelUsb,
                )
            }
            itemWithSpec.prebuilt != null -> {
                val pb = itemWithSpec.prebuilt
                PrebuiltSpec(
                    storeName = pb.storeName,
                    cpuModel = pb.cpuModel,
                    gpuModel = pb.gpuModel,
                    motherboardModel = pb.motherboardModel,
                    ramSpec = pb.ramSpec,
                    storageSpec = pb.storageSpec,
                    psuCaseSpec = pb.psuCaseSpec,
                    estimatedSeparatePriceTry = pb.estimatedSeparatePriceTry,
                    priceAdvantageTry = pb.priceAdvantageTry,
                    fps1080p = pb.fps1080p,
                    fps1440p = pb.fps1440p,
                    fps4k = pb.fps4k,
                    gamingScore = pb.gamingScore,
                    cpuId = pb.cpuId,
                    gpuId = pb.gpuId,
                )
            }
            itemWithSpec.laptop != null -> {
                val lp = itemWithSpec.laptop
                LaptopSpec(
                    brand = entity.brand,
                    cpuModel = lp.cpuModel,
                    gpuModel = lp.gpuModel,
                    gpuWattTgp = lp.gpuWattTgp,
                    muxSwitch = lp.muxSwitch,
                    ramSpec = lp.ramSpec,
                    storageSpec = lp.storageSpec,
                    displaySpec = lp.displaySpec,
                    batteryWh = lp.batteryWh,
                    weightKg = lp.weightKg,
                    coolingTech = lp.coolingTech,
                    gamingScore = lp.gamingScore,
                    fps1080p = lp.fps1080p,
                    fps1440p = lp.fps1440p,
                    cpuCores = lp.cpuCores,
                    cpuClockGhz = lp.cpuClockGhz,
                    screenResolution = lp.screenResolution,
                    refreshRateHz = lp.refreshRateHz,
                    panelTech = lp.panelTech,
                    colorGamut = lp.colorGamut,
                    screenSizeInch = lp.screenSizeInch,
                    ramSlots = lp.ramSlots,
                    storageSlots = lp.storageSlots,
                    chargerWatt = lp.chargerWatt,
                )
            }
            itemWithSpec.mouse != null -> {
                val ms = itemWithSpec.mouse
                MouseSpec(
                    sensor = ms.sensor,
                    maxDpi = ms.maxDpi,
                    pollingRateHz = ms.pollingRateHz,
                    switchType = ms.switchType,
                    weightG = ms.weightG,
                    connectivity = ms.connectivity,
                    batteryHours = ms.batteryHours,
                    mcu = ms.mcu,
                    clickLatencyMs = ms.clickLatencyMs,
                    shapeType = ms.shapeType,
                )
            }
            itemWithSpec.keyboard != null -> {
                val kb = itemWithSpec.keyboard
                KeyboardSpec(
                    switchType = kb.switchType,
                    rapidTriggerSupported = kb.rapidTriggerSupported,
                    minActuationMm = kb.minActuationMm,
                    pollingRateHz = kb.pollingRateHz,
                    layout = kb.layout,
                    hotSwap = kb.hotSwap,
                    connectivity = kb.connectivity,
                    maxActuationMm = kb.maxActuationMm,
                    caseMaterial = kb.caseMaterial,
                    isHallEffect = kb.isHallEffect,
                )
            }
            itemWithSpec.headset != null -> {
                val hs = itemWithSpec.headset
                HeadsetSpec(
                    driverType = hs.driverType,
                    driverSizeMm = hs.driverSizeMm,
                    impedanceOhm = hs.impedanceOhm,
                    frequencyResponse = hs.frequencyResponse,
                    dacAmpRequired = hs.dacAmpRequired,
                    micType = hs.micType,
                    weightG = hs.weightG,
                    connectivity = hs.connectivity,
                    soundIsolation = hs.soundIsolation,
                )
            }
            itemWithSpec.monitor != null -> {
                val mn = itemWithSpec.monitor
                MonitorSpec(
                    panelType = mn.panelType,
                    screenSizeInch = mn.screenSizeInch,
                    resolution = mn.resolution,
                    refreshRateHz = mn.refreshRateHz,
                    responseSpeedGtGMs = mn.responseSpeedGtGMs,
                    ppi = mn.ppi,
                    hdrCertification = mn.hdrCertification,
                    bitDepth = mn.bitDepth,
                    responseMprtMs = mn.responseMprtMs,
                    brightnessSdrNit = mn.brightnessSdrNit,
                    brightnessHdrNit = mn.brightnessHdrNit,
                    colorGamut = mn.colorGamut,
                    vrrSupport = mn.vrrSupport,
                )
            }
            else -> return@runCatching null
        }

        val price = if (entity.priceMinor != null && entity.currencyCode != null && entity.currencyCode.length == 3 && entity.priceMinor >= 0) {
            Money(amountMinor = entity.priceMinor, currencyCode = entity.currencyCode)
        } else null

        val cleanCurrentPrice = com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(entity.currentPriceTry)
        val cleanLowestPrice = com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(entity.lowestPriceTry) ?: cleanCurrentPrice
        val cleanHighestPrice = com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(entity.highestPriceTry) ?: cleanCurrentPrice
        val cleanOffers = entity.offers.orEmpty().mapNotNull { offer ->
            val validPrice = com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(offer.priceTry) ?: return@mapNotNull null
            val safeUrl = com.panda.myapplication.domain.util.SecurityUtils.validateStoreUrl(offer.url) ?: ""
            offer.copy(priceTry = validPrice, url = safeUrl)
        }.sortedBy { it.priceTry }
        val cleanHistory = entity.priceHistory.orEmpty()
            .filter { com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(it.effectivePrice) != null }
            .distinctBy { it.date.take(10) }
            .sortedBy { it.date }
        val cleanImage = com.panda.myapplication.domain.util.SecurityUtils.sanitizeImageUrl(entity.imageUrl)

        val priceInfo = if (cleanCurrentPrice != null || cleanOffers.isNotEmpty() || cleanHistory.isNotEmpty() || cleanImage != null) {
            ProductPriceInfo(
                currentPriceTry = cleanCurrentPrice ?: cleanOffers.firstOrNull()?.priceTry,
                lowestPriceTry = cleanLowestPrice,
                highestPriceTry = cleanHighestPrice,
                currency = "TRY",
                imageUrl = cleanImage,
                offers = cleanOffers,
                priceHistory = cleanHistory,
            )
        } else {
            null
        }

        val intermediateItem = HardwareItem(
            id = HardwareId(entity.id),
            brand = entity.brand,
            model = entity.model,
            price = price,
            spec = spec,
            priceInfo = priceInfo,
            benchmarkInfo = null,
            imageUrl = cleanImage,
            images = entity.images.orEmpty().mapNotNull { com.panda.myapplication.domain.util.SecurityUtils.sanitizeImageUrl(it) },
        )

        val isCpuOrGpu = entity.category == HardwareCategory.CPU || entity.category == HardwareCategory.GPU
        val benchmarkInfo = if (isCpuOrGpu) {
            if (entity.benchmarkScore != null) {
                val relPct = (entity.benchmarkGamingIndex ?: entity.benchmarkScore).toDouble()
                val enrichedGames = com.panda.myapplication.domain.engine.BenchmarkEngine.buildFull12GameMatrix(
                    ratio = relPct / 100.0,
                    isCpu = entity.category == HardwareCategory.CPU,
                    isX3d = entity.model.contains("X3D", ignoreCase = true),
                    existing = entity.gameFps,
                )
                HardwareBenchmarkInfo(
                    score = entity.benchmarkScore,
                    tier = entity.benchmarkTier ?: "A-Tier",
                    gamingIndex = entity.benchmarkGamingIndex,
                    testSuite = entity.benchmarkTestSuite ?: "Donanım Benchmark Laboratuvarı",
                    relativePerformanceIndex = relPct,
                    cpuSynthetic = entity.cpuSynthetic,
                    gpuSynthetic = entity.gpuSynthetic,
                    gameFps = enrichedGames,
                    dataSource = com.panda.myapplication.domain.model.DataSourceType.VERIFIED_BENCHMARK.name,
                )
            } else {
                com.panda.myapplication.domain.engine.BenchmarkEngine.simulateBenchmark(intermediateItem)
            }
        } else if (entity.benchmarkScore != null) {
            HardwareBenchmarkInfo(
                score = entity.benchmarkScore,
                tier = entity.benchmarkTier ?: "S-Tier",
                gamingIndex = entity.benchmarkGamingIndex ?: entity.benchmarkScore,
                testSuite = entity.benchmarkTestSuite ?: "Çevre Birimleri Mühendislik Laboratuvarı",
                relativePerformanceIndex = entity.benchmarkScore.toDouble(),
                dataSource = com.panda.myapplication.domain.model.DataSourceType.VERIFIED_BENCHMARK.name,
            )
        } else {
            null
        }

        intermediateItem.copy(benchmarkInfo = benchmarkInfo)
    }.getOrNull()

    // endregion

    // region DTO -> Room Entities

    fun cpuDtoToEntity(dto: CpuRawDto, cachedAt: Instant): Pair<HardwareItemEntity, CpuSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.CPU, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val socket = deduceCpuSocket(dto, rawName)
        if (!isModernCpu(rawName, socket)) return null
        val coreCount = (dto.coreCount ?: 6).coerceAtLeast(1)
        val threadCount = (dto.threads ?: (coreCount * 2)).coerceAtLeast(coreCount)
        val baseClockMhz = ((dto.coreClockGhz ?: 3.5) * 1000).toInt().coerceAtLeast(500)
        val boostClockMhz = ((dto.boostClockGhz ?: dto.coreClockGhz ?: 4.0) * 1000).toInt().coerceAtLeast(baseClockMhz)
        val tdpWatts = (dto.tdp ?: 65).coerceAtLeast(15)
        val hasIntegratedGraphics = !dto.graphics.isNullOrBlank() && !dto.graphics.equals("None", ignoreCase = true)

        val architecture = dto.microarchitecture?.ifBlank { null }
        val upper = rawName.uppercase()
        val l3CacheMb = dto.l3CacheMb ?: when {
            // Dual CCD X3D
            upper.contains("9950X3D") || upper.contains("7950X3D") || upper.contains("7900X3D") -> 128
            // Single CCD X3D
            upper.contains("9800X3D") || upper.contains("7800X3D") || upper.contains("5800X3D") || upper.contains("5700X3D") || upper.contains("7600X3D") -> 96
            // Dual CCD Ryzen 9 (Zen 3, Zen 4, Zen 5, Zen 2)
            upper.contains("9950X") || upper.contains("9900X") || upper.contains("7950X") || upper.contains("7900X") || upper.contains("7900") || upper.contains("5950X") || upper.contains("5900X") || upper.contains("3950X") || upper.contains("3900X") -> 64
            // Intel Raptor / Arrow Lake
            upper.contains("14900") || upper.contains("13900") || upper.contains("285K") -> 36
            upper.contains("14700") || upper.contains("13700") -> 33
            upper.contains("265K") -> 30
            upper.contains("14600") || upper.contains("13600") || upper.contains("245K") -> 24
            upper.contains("12900") -> 30
            upper.contains("12700") -> 25
            upper.contains("12600") -> 20
            upper.contains("12400") || upper.contains("13400") || upper.contains("14400") -> 18
            coreCount >= 12 -> 64
            coreCount >= 8 -> 32
            else -> 16
        }

        val l2CacheMb = dto.l2CacheMb ?: when {
            upper.contains("285K") -> 40.0
            upper.contains("265K") -> 36.0
            upper.contains("14900") || upper.contains("13900") -> 32.0
            upper.contains("14700") -> 28.0
            upper.contains("245K") -> 26.0
            upper.contains("14600") || upper.contains("13600") -> 20.0
            upper.contains("9950") || upper.contains("7950") -> 16.0
            upper.contains("9900") || upper.contains("7900") -> 12.0
            upper.contains("9800") || upper.contains("7800") || upper.contains("9700") || upper.contains("7700") -> 8.0
            upper.contains("9600") || upper.contains("7600") || upper.contains("7500") -> 6.0
            else -> (coreCount * 0.5)
        }

        val processNode = dto.processNode ?: when {
            upper.contains("285K") || upper.contains("265K") || upper.contains("245K") -> "TSMC 3nm N3B FinFET"
            upper.contains("9950") || upper.contains("9900") || upper.contains("9800") || upper.contains("9700") || upper.contains("9600") -> "TSMC 4nm FinFET"
            upper.contains("7950") || upper.contains("7900") || upper.contains("7800") || upper.contains("7700") || upper.contains("7600") || upper.contains("7500") -> "TSMC 5nm FinFET"
            upper.contains("14900") || upper.contains("14700") || upper.contains("14600") || upper.contains("14400") -> "Intel 7 (10nm Enhanced SuperFin)"
            upper.contains("13900") || upper.contains("13700") || upper.contains("13600") || upper.contains("13400") -> "Intel 7 (10nm)"
            upper.contains("12900") || upper.contains("12700") || upper.contains("12600") || upper.contains("12400") || upper.contains("12100") -> "Intel 7 (10nm)"
            upper.contains("5950") || upper.contains("5900") || upper.contains("5800") || upper.contains("5700") || upper.contains("5600") || upper.contains("5500") -> "TSMC 7nm FinFET"
            else -> null
        }

        val pcieVersion = dto.pcieVersion ?: when {
            socket.contains("AM5", ignoreCase = true) -> "PCIe 5.0 (28 Hattı / Lanes)"
            socket.contains("1851", ignoreCase = true) -> "PCIe 5.0 (24 Hattı / Lanes)"
            socket.contains("1700", ignoreCase = true) -> "PCIe 5.0 (x16) + PCIe 4.0 (x4)"
            socket.contains("AM4", ignoreCase = true) -> if (upper.contains("5500") || upper.contains("G")) "PCIe 3.0" else "PCIe 4.0 (24 Hattı)"
            else -> null
        }

        val maxMemorySpeed = dto.maxMemorySpeed ?: when {
            socket.contains("AM5", ignoreCase = true) -> if (upper.contains("9000") || upper.contains("9900") || upper.contains("9800") || upper.contains("9700") || upper.contains("9600")) "DDR5-5600 MT/s (Native) • EXPO 6000+" else "DDR5-5200 MT/s (Native) • EXPO 6000+"
            socket.contains("1851", ignoreCase = true) -> "DDR5-6400 MT/s • XMP 3.0"
            socket.contains("1700", ignoreCase = true) -> "DDR5-5600 / DDR4-3200 MT/s"
            socket.contains("AM4", ignoreCase = true) -> "DDR4-3200 MT/s"
            else -> null
        }

        val unlockedMultiplier = dto.unlockedMultiplier ?: when {
            upper.contains("AMD") || upper.contains("RYZEN") -> !upper.contains("PRO ")
            upper.contains("K") || upper.contains("KF") || upper.contains("KS") || upper.contains("EXTREME") -> true
            else -> false
        }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.CPU,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl,
        )
        val spec = CpuSpecEntity(
            itemId = id,
            socket = socket,
            coreCount = coreCount,
            threadCount = threadCount,
            baseClockMhz = baseClockMhz,
            boostClockMhz = boostClockMhz,
            tdpWatts = tdpWatts,
            hasIntegratedGraphics = hasIntegratedGraphics,
            architecture = architecture,
            l2CacheMb = l2CacheMb,
            l3CacheMb = l3CacheMb,
            processNode = processNode,
            pcieVersion = pcieVersion,
            maxMemorySpeed = maxMemorySpeed,
            unlockedMultiplier = unlockedMultiplier,
            coreTopology = dto.coreTopology,
            powerLimits = dto.powerLimits,
            igpuModel = dto.igpuModel,
        )
        return item to spec
    }

    fun gpuDtoToEntity(dto: GpuRawDto, cachedAt: Instant): Pair<HardwareItemEntity, GpuSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        var (brand, extractedModel) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.GPU, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val rawChipset = dto.chipset?.trim()?.ifBlank { null }
        val chipset = rawChipset ?: rawName
        if (!isModernGpu(rawName, chipset)) return null

        // If brand was not recognized or "Generic", deduce brand from chipset
        if (brand.equals("Generic", ignoreCase = true)) {
            brand = when {
                chipset.contains("Radeon", ignoreCase = true) -> "AMD"
                chipset.contains("GeForce", ignoreCase = true) || chipset.contains("RTX", ignoreCase = true) || chipset.contains("GTX", ignoreCase = true) -> "NVIDIA"
                chipset.contains("Arc", ignoreCase = true) -> "Intel"
                else -> brand
            }
        }

        // Check if the extracted model is an OPN/SKU/stock code (e.g. "100-435415")
        val isSku = isStockCode(extractedModel)
        val partNumber = if (isSku) extractedModel else null

        // Marketing model name: prioritize chipset if model is SKU, or combine properly
        val finalModel = when {
            isSku && rawChipset != null -> {
                if (rawChipset.startsWith(brand, ignoreCase = true)) {
                    rawChipset.substring(brand.length).trim().ifBlank { rawChipset }
                } else {
                    rawChipset
                }
            }
            rawChipset != null && !extractedModel.contains(rawChipset, ignoreCase = true) && !rawChipset.contains(extractedModel, ignoreCase = true) -> {
                "$rawChipset $extractedModel"
            }
            else -> extractedModel
        }

        val factorySpecs = resolveGpuFactorySpecs(chipset, rawName, dto.memory)
        val vramMb = (dto.memory?.let { it * 1024 }) ?: factorySpecs.vramMb
        val memoryType = dto.memoryType ?: factorySpecs.memoryType
        val coreClockMhz = (dto.coreClockMhz ?: 1500).coerceAtLeast(500)
        val boostClockMhz = (dto.boostClockMhz ?: (coreClockMhz + 250)).coerceAtLeast(coreClockMhz)
        val tdpWatts = dto.tdpWatts ?: factorySpecs.tdpWatts
        val recommendedPsuWatts = dto.recommendedPsuWatts ?: factorySpecs.recommendedPsuWatts
        val lengthMm = dto.length?.toInt()
        val powerConnectors = dto.powerConnectors ?: factorySpecs.powerConnectors
        val slotWidth = dto.slotWidth ?: factorySpecs.slotWidth

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = finalModel,
            category = HardwareCategory.GPU,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl,
        )
        val spec = GpuSpecEntity(
            itemId = id,
            chipset = chipset,
            vramMb = vramMb,
            memoryType = memoryType,
            coreClockMhz = coreClockMhz,
            boostClockMhz = boostClockMhz,
            tdpWatts = tdpWatts,
            recommendedPsuWatts = recommendedPsuWatts,
            busWidthBits = dto.busWidthBits,
            lengthMm = lengthMm,
            powerConnectors = powerConnectors,
            displayOutputs = dto.displayOutputs,
            slotWidth = slotWidth,
            partNumber = partNumber,
            architecture = dto.architecture,
            processNode = dto.processNode,
            cudaCores = dto.cudaCores,
            tensorAiTops = dto.tensorAiTops,
            rtCores = dto.rtCores,
            memorySpeedGbps = dto.memorySpeedGbps,
            bandwidthGbs = dto.bandwidthGbs,
        )
        return item to spec
    }

    fun ramDtoToEntity(dto: RamRawDto, cachedAt: Instant): Pair<HardwareItemEntity, RamSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.RAM, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val isDdr3 = dto.speed?.firstOrNull() == 3 || rawName.contains("DDR3", ignoreCase = true)
        val isDdr5 = dto.speed?.firstOrNull() == 5 || rawName.contains("DDR5", ignoreCase = true)
        val generation = when {
            isDdr3 -> DdrGeneration.DDR3
            isDdr5 -> DdrGeneration.DDR5
            else -> DdrGeneration.DDR4
        }
        val speedMts = when {
            dto.speed != null && dto.speed.size >= 2 -> dto.speed[1]
            dto.speed != null && dto.speed.size == 1 -> dto.speed[0]
            isDdr3 -> 1600
            isDdr5 -> 6000
            else -> 3200
        }.coerceAtLeast(800)

        val casLatency = (dto.casLatency ?: if (isDdr3) 11 else if (isDdr5) 36 else 16).coerceAtLeast(7)
        val moduleCount = dto.modules?.firstOrNull()?.coerceAtLeast(1) ?: 2
        val perModuleGb = dto.modules?.getOrNull(1)?.coerceAtLeast(2) ?: 16
        val capacityMb = (moduleCount * perModuleGb * 1024).coerceAtLeast(2048)

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.RAM,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl,
        )
        val spec = RamSpecEntity(
            itemId = id,
            generation = generation,
            speedMts = speedMts,
            casLatency = casLatency,
            capacityMb = capacityMb,
            moduleCount = moduleCount,
            firstWordLatencyNs = dto.firstWordLatency,
            voltage = dto.voltage,
            heatSpreader = dto.heatSpreader,
            rgb = dto.rgb,
            dieType = dto.dieType,
            timings = dto.timings,
            profile = dto.profile,
        )
        return item to spec
    }

    fun motherboardDtoToEntity(dto: MotherboardRawDto, cachedAt: Instant): Pair<HardwareItemEntity, MotherboardSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.MOTHERBOARD, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val socket = dto.socket?.ifBlank { null } ?: deduceMotherboardSocket(rawName)
        if (!isModernMotherboard(rawName, socket)) return null
        val chipset = dto.chipset?.ifBlank { null } ?: deduceMotherboardChipset(rawName)
        val formFactor = when {
            dto.formFactor?.contains("Micro", ignoreCase = true) == true -> MotherboardFormFactor.MICRO_ATX
            dto.formFactor?.contains("Mini", ignoreCase = true) == true -> MotherboardFormFactor.MINI_ITX
            dto.formFactor?.contains("E-ATX", ignoreCase = true) == true -> MotherboardFormFactor.E_ATX
            else -> MotherboardFormFactor.ATX
        }
        val memoryGeneration = when {
            dto.memoryType?.contains("DDR3", ignoreCase = true) == true -> DdrGeneration.DDR3
            dto.memoryType?.contains("DDR4", ignoreCase = true) == true -> DdrGeneration.DDR4
            dto.memoryType?.contains("DDR5", ignoreCase = true) == true -> DdrGeneration.DDR5
            socket in listOf("AM3", "AM3+", "AM2", "AM2+", "FM1", "FM2", "FM2+", "LGA775", "LGA1156", "LGA1155", "LGA1150", "LGA2011") -> DdrGeneration.DDR3
            socket in listOf("AM4", "LGA1200", "LGA1151", "LGA2011-v3", "LGA2066") -> DdrGeneration.DDR4
            socket in listOf("AM5", "LGA1851") -> DdrGeneration.DDR5
            socket == "LGA1700" -> {
                if (rawName.contains("D4", ignoreCase = true) || rawName.contains("DDR4", ignoreCase = true)) {
                    DdrGeneration.DDR4
                } else {
                    DdrGeneration.DDR5
                }
            }
            rawName.contains("DDR3", ignoreCase = true) -> DdrGeneration.DDR3
            rawName.contains("DDR4", ignoreCase = true) -> DdrGeneration.DDR4
            rawName.contains("DDR5", ignoreCase = true) -> DdrGeneration.DDR5
            else -> DdrGeneration.DDR4
        }
        val ramSlots = (dto.memorySlots ?: if (formFactor == MotherboardFormFactor.MINI_ITX) 2 else 4).coerceAtLeast(2)
        val pcieGeneration = when {
            dto.pcieGeneration?.contains("5.0", ignoreCase = true) == true -> PcieGeneration.GEN5
            dto.pcieGeneration?.contains("4.0", ignoreCase = true) == true -> PcieGeneration.GEN4
            dto.pcieGeneration?.contains("3.0", ignoreCase = true) == true -> PcieGeneration.GEN3
            dto.pcieGeneration?.contains("2.0", ignoreCase = true) == true -> PcieGeneration.GEN2
            socket in listOf("AM5", "LGA1851") || chipset.contains("Z790") || chipset.contains("X670") || chipset.contains("X870") || chipset.contains("Z890") -> PcieGeneration.GEN5
            socket in listOf("AM4", "LGA1700", "LGA1200") -> PcieGeneration.GEN4
            socket in listOf("AM3", "AM3+", "AM2", "FM2", "LGA775", "LGA1155", "LGA1150") -> PcieGeneration.GEN2
            else -> PcieGeneration.GEN3
        }

        val hasWifi = dto.hasWifi ?: (rawName.contains("WIFI", ignoreCase = true) || rawName.contains("AX", ignoreCase = true))

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.MOTHERBOARD,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl,
        )
        val spec = MotherboardSpecEntity(
            itemId = id,
            socket = socket,
            chipset = chipset,
            formFactor = formFactor,
            memoryGeneration = memoryGeneration,
            ramSlots = ramSlots,
            pcieGeneration = pcieGeneration,
            maxMemoryGb = dto.maxMemoryGb,
            m2Slots = dto.m2Slots,
            pcieSlotCount = dto.pcieSlots,
            sataPorts = dto.sataPorts,
            hasWifi = hasWifi,
            vrmPhases = dto.vrmPhases,
            interfacePorts = dto.interfacePorts,
            pcbLayers = dto.pcbLayers,
            m2SlotsDetail = dto.m2SlotsDetail,
            memorySpeedOc = dto.memorySpeedOc,
            network = dto.network,
            audioCodec = dto.audioCodec,
        )
        return item to spec
    }

    fun psuDtoToEntity(dto: PsuRawDto, cachedAt: Instant): Pair<HardwareItemEntity, PsuSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.PSU, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val wattage = (dto.wattage ?: 750).coerceAtLeast(300)
        val upper = rawName.uppercase()
        val isEdge = upper.contains("EDGE")
        val isAtx31 = upper.contains("ATX 3.1") || upper.contains("ATX3.1") || upper.contains("PCIE 5.1") || isEdge || upper.contains("VITA GM")
        val isAtx3 = isAtx31 || upper.contains("PCIE5") || upper.contains("ATX 3") || upper.contains("ATX3") || upper.contains("GL") || upper.contains("AURA")

        val efficiency = when {
            dto.efficiency?.lowercase()?.trim() == "titanium" || upper.contains("TITANIUM") -> PsuEfficiencyRating.TITANIUM
            dto.efficiency?.lowercase()?.trim() == "platinum" || isEdge || upper.contains("PLATINUM") || upper.contains("THOR") -> PsuEfficiencyRating.PLATINUM
            dto.efficiency?.lowercase()?.trim() == "bronze" || upper.contains("BRONZE") || upper.contains("BN") -> PsuEfficiencyRating.BRONZE
            dto.efficiency?.lowercase()?.trim() == "silver" || upper.contains("SILVER") -> PsuEfficiencyRating.SILVER
            else -> PsuEfficiencyRating.GOLD
        }

        val modularity = dto.modularity ?: when {
            isEdge || upper.contains("RM") || upper.contains("HYDRO G") || upper.contains("VITA") || upper.contains("THOR") || upper.contains("AURA") || upper.contains("C750") || upper.contains("C850") || upper.contains("C1000") || upper.contains("A750GL") || upper.contains("A850GL") || upper.contains("A1000G") -> "Tam Modüler (Full Modular)"
            upper.contains("SEMI") -> "Yarı Modüler"
            upper.contains("BN") -> "Modüler Değil"
            else -> null
        }

        val tierRating = dto.tierRating ?: when {
            isEdge || upper.contains("RM") || upper.contains("HYDRO G") || upper.contains("A1000G") || upper.contains("A850GL") || upper.contains("A750GL") || upper.contains("THOR") || upper.contains("STRIX") || upper.contains("VITA GM") || upper.contains("NZXT C") -> "Tier A (High-End)"
            upper.contains("BN") || upper.contains("BRONZE") -> "Tier C (Entry-Level)"
            else -> null
        }

        val atxStandard = dto.atxStandard ?: when {
            isAtx31 -> "ATX 3.1 & PCIe 5.1"
            isAtx3 -> "ATX 3.0 & PCIe 5.0"
            else -> null
        }

        val pcie12Vhpwr = dto.pcie12Vhpwr ?: when {
            isAtx31 -> "1x 16-Pin 12V-2x6 (600W Native)"
            isAtx3 -> "1x 16-Pin 12VHPWR (600W Native)"
            else -> null
        }

        val epsConnectors = dto.epsConnectors
        val pcieConnectors = dto.pcieConnectors
        val sataConnectors = dto.sataConnectors
        val topology = dto.topology ?: when {
            isEdge -> "LLC Resonant + DC-DC / %100 Japon 105°C Kapasitörler"
            upper.contains("HYDRO G") -> "LLC Half-Bridge + DC-DC / 105°C Japon Kapasitör (Conformal Coating)"
            upper.contains("THOR") || upper.contains("STRIX") -> "ROG Soğutuculu LLC Resonant + DC-DC / %100 Japon Kapasitör"
            else -> null
        }
        val protections = dto.protections
        val fanAndSize = dto.fanAndSize ?: when {
            isEdge -> "120mm FDB Fan (Sıfır RPM) / 182mm (L-Shape Çift Bölmeli)"
            upper.contains("RM1000") || upper.contains("RM850X") -> "135mm Manyetik Kaldırma (ML) Fan / 160mm"
            upper.contains("A1000G") || upper.contains("NZXT C") || upper.contains("AURA") -> "135mm FDB Fan (Sıfır RPM) / 150mm"
            upper.contains("GL") || upper.contains("VITA") || upper.contains("RME") -> "120mm FDB Fan / 140mm Kompakt"
            else -> null
        }
        val cybeneticsCert = dto.cybeneticsCert ?: when {
            isEdge -> "Cybenetics Platinum (Verimlilik) & Standard+ (Ses)"
            upper.contains("RM") -> "Cybenetics Gold"
            upper.contains("THOR") -> "Cybenetics Platinum / Lambda A++"
            else -> null
        }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.PSU,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl,
        )
        val spec = PsuSpecEntity(
            itemId = id,
            wattage = wattage,
            efficiency = efficiency,
            isAtx3Compliant = isAtx3,
            supportsPcie5Power = isAtx3,
            modularity = modularity,
            formFactor = dto.type ?: "ATX",
            outputs = dto.outputs ?: listOfNotNull(pcie12Vhpwr, epsConnectors, pcieConnectors).takeIf { it.isNotEmpty() }?.joinToString(" • "),
            tierRating = tierRating,
            capacitors = dto.capacitors ?: topology,
            atxStandard = atxStandard,
            pcie12Vhpwr = pcie12Vhpwr,
            epsConnectors = epsConnectors,
            pcieConnectors = pcieConnectors,
            sataConnectors = sataConnectors,
            topology = topology,
            protections = protections,
            fanAndSize = fanAndSize,
            cybeneticsCert = cybeneticsCert,
            cybeneticsLambda = dto.cybeneticsLambda,
        )
        return item to spec
    }

    fun storageDtoToEntity(dto: StorageRawDto, cachedAt: Instant): Pair<HardwareItemEntity, StorageSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.STORAGE, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val isHdd = dto.type?.contains("HDD", ignoreCase = true) == true ||
                dto.type?.contains("RPM", ignoreCase = true) == true ||
                dto.type?.contains("7200") == true ||
                dto.type?.contains("5400") == true ||
                dto.formFactor?.contains("3.5") == true ||
                rawName.contains("Barracuda", ignoreCase = true) ||
                rawName.contains("IronWolf", ignoreCase = true)
        if (isHdd || (dto.capacity != null && dto.capacity < 120)) return null

        val mediaType = StorageMediaType.SSD
        val isNvme = !isHdd && (dto.formFactor?.contains("M.2", ignoreCase = true) == true || dto.`interface`?.contains("PCIe", ignoreCase = true) == true)
        val storageInterface = if (isNvme) StorageInterface.NVME else StorageInterface.SATA
        val pcieGen = if (storageInterface == StorageInterface.NVME) {
            if (dto.`interface`?.contains("5.0") == true) PcieGeneration.GEN5 else PcieGeneration.GEN4
        } else null

        val formFactor = when {
            dto.formFactor?.contains("2280") == true -> StorageFormFactor.M2_2280
            dto.formFactor?.contains("2242") == true -> StorageFormFactor.M2_2242
            dto.formFactor?.contains("2230") == true -> StorageFormFactor.M2_2230
            dto.formFactor?.contains("2.5") == true -> StorageFormFactor.INCH_2_5
            else -> StorageFormFactor.M2_2280
        }
        val capacityMb = ((dto.capacity ?: 1000) * 1024).coerceAtLeast(1024)
        val seqRead = (dto.seqRead ?: if (isNvme) 3500 else 500).coerceAtLeast(100)
        val seqWrite = (dto.seqWrite ?: if (isNvme) 3000 else 450).coerceAtLeast(100)

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.STORAGE,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl,
        )
        val spec = StorageSpecEntity(
            itemId = id,
            mediaType = mediaType,
            storageInterface = storageInterface,
            formFactor = formFactor,
            pcieGeneration = pcieGen,
            capacityMb = capacityMb,
            sequentialReadMbps = seqRead,
            sequentialWriteMbps = seqWrite,
            nandType = dto.nandType,
            enduranceTbw = dto.enduranceTbw,
            cacheMb = dto.cacheMb,
            controller = dto.controller,
            dramCache = dto.dramCache,
            randomIops = dto.randomIops,
        )
        return item to spec
    }

    fun coolerDtoToEntity(dto: CoolerRawDto, cachedAt: Instant): Pair<HardwareItemEntity, CoolerSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.COOLER, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val isLiquid = (dto.size != null && dto.size > 0) || dto.type?.contains("Liquid", ignoreCase = true) == true
        val type = if (isLiquid) CoolerType.LIQUID else CoolerType.AIR
        val radiatorSize = if (isLiquid) dto.size else null
        val maxTdp = dto.maxTdp ?: if (isLiquid) 300 else 220

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.COOLER,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl ?: dto.images.firstOrNull(),
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
        )
        val spec = CoolerSpecEntity(
            itemId = id,
            type = type,
            radiatorSizeMm = radiatorSize,
            maxCoolingTdpWatts = maxTdp,
            supportedSockets = dto.supportedSockets ?: listOf("AM5", "AM4", "LGA1700", "LGA1851"),
            heightMm = dto.heightMm,
            fanRpm = dto.fanRpm,
            noiseDba = dto.noiseLevel,
            radiatorDimensions = dto.radiatorDimensions,
            fanAirflow = dto.fanAirflow,
        )
        return item to spec
    }

    fun caseDtoToEntity(dto: CaseRawDto, cachedAt: Instant): Pair<HardwareItemEntity, CaseSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.CASE, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }
        val type = dto.type?.trim()?.ifBlank { null }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.CASE,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl ?: dto.images.firstOrNull(),
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
        )
        val spec = CaseSpecEntity(
            itemId = id,
            type = type,
            maxGpuLengthMm = dto.maxGpuLengthMm,
            maxCpuCoolerHeightMm = dto.maxCpuCoolerHeightMm,
            maxRadiatorSizeMm = dto.maxRadiatorSizeMm,
            sidePanel = dto.sidePanel,
            driveBays = dto.driveBays,
            radiatorSupport = dto.radiatorSupport,
            includedFans = dto.includedFans,
            frontPanelUsb = dto.frontPanelUsb,
        )
        return item to spec
    }

    fun prebuiltDtoToEntity(dto: PrebuiltRawDto, cachedAt: Instant): Pair<HardwareItemEntity, PrebuiltSpecEntity> {
        val id = requireNotNull(dto.id?.takeIf { it.isNotBlank() }) { "Prebuilt id must not be blank" }
        val name = dto.name ?: "OEM Hazır Sistem"
        val brand = dto.brand ?: dto.storeName ?: "OEM"
        val model = name
        val priceMinor = dto.price?.let { (it * 100).toLong() }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.PREBUILT_PC,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "TRY" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl,
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
            currentPriceTry = dto.priceTry,
            lowestPriceTry = dto.priceTry,
            highestPriceTry = dto.priceTry,
            benchmarkGamingIndex = dto.gamingScore,
        )
        val spec = PrebuiltSpecEntity(
            itemId = id,
            storeName = dto.storeName ?: "Mağaza",
            cpuModel = dto.cpuModel ?: "",
            gpuModel = dto.gpuModel ?: "",
            motherboardModel = dto.motherboardModel ?: "",
            ramSpec = dto.ramSpec ?: "",
            storageSpec = dto.storageSpec ?: "",
            psuCaseSpec = dto.psuCaseSpec ?: "",
            estimatedSeparatePriceTry = dto.estimatedSeparatePriceTry,
            priceAdvantageTry = dto.priceAdvantageTry,
            fps1080p = dto.fps1080p ?: 0,
            fps1440p = dto.fps1440p ?: 0,
            fps4k = dto.fps4k ?: 0,
            gamingScore = dto.gamingScore ?: 0,
            cpuId = dto.cpuId,
            gpuId = dto.gpuId,
        )
        return item to spec
    }

    fun laptopDtoToEntity(dto: LaptopRawDto, cachedAt: Instant): Pair<HardwareItemEntity, LaptopSpecEntity> {
        val id = requireNotNull(dto.id?.takeIf { it.isNotBlank() }) { "Laptop id must not be blank" }
        val name = dto.name ?: "Gaming Laptop"
        val brand = dto.brand ?: "Laptop"
        val model = name
        val priceMinor = dto.price?.let { (it * 100).toLong() }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.LAPTOP,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "TRY" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl,
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
            currentPriceTry = dto.priceTry,
            lowestPriceTry = dto.priceTry,
            highestPriceTry = dto.priceTry,
            benchmarkGamingIndex = dto.gamingScore,
        )
        val spec = LaptopSpecEntity(
            itemId = id,
            cpuModel = dto.cpuModel ?: "",
            gpuModel = dto.gpuModel ?: "",
            gpuWattTgp = dto.gpuWattTgp ?: "",
            muxSwitch = dto.muxSwitch ?: "",
            ramSpec = dto.ramSpec ?: "",
            storageSpec = dto.storageSpec ?: "",
            displaySpec = dto.displaySpec ?: "",
            batteryWh = dto.batteryWh ?: 0,
            weightKg = dto.weightKg ?: 0.0,
            coolingTech = dto.coolingTech ?: "",
            gamingScore = dto.gamingScore ?: 0,
            fps1080p = dto.fps1080p ?: 0,
            fps1440p = dto.fps1440p ?: 0,
            cpuCores = dto.cpuCores ?: "",
            cpuClockGhz = dto.cpuClockGhz ?: "",
            screenResolution = dto.screenResolution ?: "",
            refreshRateHz = dto.refreshRateHz ?: 0,
            panelTech = dto.panelTech ?: "",
            colorGamut = dto.colorGamut ?: "",
            screenSizeInch = dto.screenSizeInch ?: 0.0,
            ramSlots = dto.ramSlots ?: "",
            storageSlots = dto.storageSlots ?: "",
            chargerWatt = dto.chargerWatt ?: 0,
        )
        return item to spec
    }

    fun buildCatalogSnapshot(
        cpus: List<CpuRawDto> = emptyList(),
        gpus: List<GpuRawDto> = emptyList(),
        rams: List<RamRawDto> = emptyList(),
        motherboards: List<MotherboardRawDto> = emptyList(),
        psus: List<PsuRawDto> = emptyList(),
        storages: List<StorageRawDto> = emptyList(),
        coolers: List<CoolerRawDto> = emptyList(),
        cases: List<CaseRawDto> = emptyList(),
        prebuilts: List<PrebuiltRawDto> = emptyList(),
        laptops: List<LaptopRawDto> = emptyList(),
        peripherals: List<PeripheralRawDto> = emptyList(),
        pricesTr: Map<String, ProductPriceInfo> = emptyMap(),
        benchmarks: Map<String, HardwareBenchmarkInfo> = emptyMap(),
        existingImages: Map<String, String?> = emptyMap(),
        syncedAt: Instant,
    ): HardwareCatalogSnapshot {
        val cpuPairs = cpus.mapNotNull { runCatching { cpuDtoToEntity(it, syncedAt) }.getOrNull() }
        val gpuPairs = gpus.mapNotNull { runCatching { gpuDtoToEntity(it, syncedAt) }.getOrNull() }
        val ramPairs = rams.mapNotNull { runCatching { ramDtoToEntity(it, syncedAt) }.getOrNull() }
        val moboPairs = motherboards.mapNotNull { runCatching { motherboardDtoToEntity(it, syncedAt) }.getOrNull() }
        val psuPairs = psus.mapNotNull { runCatching { psuDtoToEntity(it, syncedAt) }.getOrNull() }
        val storagePairs = storages.mapNotNull { runCatching { storageDtoToEntity(it, syncedAt) }.getOrNull() }
        val coolerPairs = coolers.mapNotNull { runCatching { coolerDtoToEntity(it, syncedAt) }.getOrNull() }
        val casePairs = cases.mapNotNull { runCatching { caseDtoToEntity(it, syncedAt) }.getOrNull() }
        val prebuiltPairs = prebuilts.mapNotNull { runCatching { prebuiltDtoToEntity(it, syncedAt) }.getOrNull() }
        val laptopPairs = laptops.mapNotNull { runCatching { laptopDtoToEntity(it, syncedAt) }.getOrNull() }

        val mousePairs = mutableListOf<Pair<HardwareItemEntity, MouseSpecEntity>>()
        val keyboardPairs = mutableListOf<Pair<HardwareItemEntity, KeyboardSpecEntity>>()
        val headsetPairs = mutableListOf<Pair<HardwareItemEntity, HeadsetSpecEntity>>()
        val monitorPairs = mutableListOf<Pair<HardwareItemEntity, MonitorSpecEntity>>()

        peripherals.forEach { dto ->
            runCatching {
                val category = HardwareCategory.valueOf(dto.category.uppercase())
                val id = dto.id.trim().ifBlank { generateHardwareId(category, dto.name) }
                val offerList = if (dto.currentPriceTry != null && !dto.storeName.isNullOrBlank()) {
                    listOf(
                        StoreOffer(
                            storeName = dto.storeName,
                            priceTry = dto.currentPriceTry,
                            url = dto.storeUrl ?: "",
                        ),
                    )
                } else null
                val itemEntity = HardwareItemEntity(
                    id = id,
                    brand = dto.brand.trim().ifBlank { "Çevre Birimi" },
                    model = dto.model.trim().ifBlank { dto.name.trim() },
                    category = category,
                    priceMinor = dto.currentPriceTry?.let { (it * 100).toLong() },
                    currencyCode = if (dto.currentPriceTry != null) "TRY" else null,
                    cachedAt = syncedAt,
                    imageUrl = dto.imageUrl,
                    images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
                    currentPriceTry = dto.currentPriceTry,
                    lowestPriceTry = dto.lowestPriceTry ?: dto.currentPriceTry,
                    highestPriceTry = dto.highestPriceTry ?: dto.currentPriceTry,
                    offers = offerList,
                    benchmarkScore = dto.benchmarkScore,
                    benchmarkTier = dto.benchmarkTier,
                    benchmarkGamingIndex = dto.benchmarkScore,
                )
                when (category) {
                    HardwareCategory.MOUSE -> {
                        mousePairs += itemEntity to MouseSpecEntity(
                            hardwareId = id,
                            sensor = dto.sensor ?: "PixArt PAW3395",
                            maxDpi = dto.maxDpi ?: 26000,
                            pollingRateHz = dto.pollingRateHz ?: 1000,
                            switchType = dto.switchType ?: "Optik Switch",
                            weightG = dto.weightG ?: 60,
                            connectivity = dto.connectivity ?: "2.4GHz / USB-C",
                            batteryHours = dto.batteryHours,
                            mcu = dto.mcu ?: "Nordic 52840",
                            clickLatencyMs = dto.clickLatencyMs ?: 0.8,
                            shapeType = dto.shapeType ?: "Simetrik",
                        )
                    }
                    HardwareCategory.KEYBOARD -> {
                        keyboardPairs += itemEntity to KeyboardSpecEntity(
                            hardwareId = id,
                            switchType = dto.switchType ?: "Mekanik Switch",
                            rapidTriggerSupported = dto.rapidTriggerSupported ?: false,
                            minActuationMm = dto.minActuationMm ?: 1.8,
                            pollingRateHz = dto.pollingRateHz ?: 1000,
                            layout = dto.layout ?: "TKL",
                            hotSwap = dto.hotSwap ?: true,
                            connectivity = dto.connectivity ?: "Kablolu USB-C",
                            maxActuationMm = dto.maxActuationMm ?: 4.0,
                            caseMaterial = dto.caseMaterial ?: "Alüminyum CNC / ABS",
                            isHallEffect = dto.isHallEffect ?: (dto.switchType?.contains("Hall Effect", ignoreCase = true) == true),
                        )
                    }
                    HardwareCategory.HEADSET -> {
                        headsetPairs += itemEntity to HeadsetSpecEntity(
                            hardwareId = id,
                            driverType = dto.driverType ?: "Dinamik Neodimyum",
                            driverSizeMm = dto.driverSizeMm ?: 50,
                            impedanceOhm = dto.impedanceOhm ?: 32,
                            frequencyResponse = dto.frequencyResponse ?: "20Hz - 20kHz",
                            dacAmpRequired = dto.dacAmpRequired ?: false,
                            micType = dto.micType ?: "Çıkarılabilir Kardioid",
                            weightG = dto.weightG ?: 300,
                            connectivity = dto.connectivity ?: "2.4GHz / Bluetooth / 3.5mm",
                            soundIsolation = dto.soundIsolation ?: "Pasif / ANC",
                        )
                    }
                    HardwareCategory.MONITOR -> {
                        monitorPairs += itemEntity to MonitorSpecEntity(
                            hardwareId = id,
                            panelType = dto.panelType ?: "Fast IPS",
                            screenSizeInch = dto.screenSizeInch ?: 27.0,
                            resolution = dto.resolution ?: "1440p",
                            refreshRateHz = dto.refreshRateHz ?: 240,
                            responseSpeedGtGMs = dto.responseSpeedGtGMs ?: 1.0,
                            ppi = dto.ppi ?: 109,
                            hdrCertification = dto.hdrCertification ?: "HDR400",
                            bitDepth = dto.bitDepth ?: "8-bit + FRC",
                            responseMprtMs = dto.responseMprtMs ?: 0.5,
                            brightnessSdrNit = dto.brightnessSdrNit ?: 350,
                            brightnessHdrNit = dto.brightnessHdrNit ?: 400,
                            colorGamut = dto.colorGamut ?: "%100 sRGB, %98 DCI-P3",
                            vrrSupport = dto.vrrSupport ?: "G-Sync Compatible / FreeSync Premium Pro",
                        )
                    }
                    else -> Unit
                }
            }
        }

        val rawItems = (cpuPairs.map { it.first } +
                gpuPairs.map { it.first } +
                ramPairs.map { it.first } +
                moboPairs.map { it.first } +
                psuPairs.map { it.first } +
                storagePairs.map { it.first } +
                coolerPairs.map { it.first } +
                casePairs.map { it.first } +
                prebuiltPairs.map { it.first } +
                laptopPairs.map { it.first } +
                mousePairs.map { it.first } +
                keyboardPairs.map { it.first } +
                headsetPairs.map { it.first } +
                monitorPairs.map { it.first })
            .distinctBy { it.id }

        val enrichedItems = rawItems.map { item ->
            val priceData = pricesTr[item.id]
            val isCpuOrGpu = item.category == HardwareCategory.CPU || item.category == HardwareCategory.GPU
            val gpuKey = if (item.category == HardwareCategory.GPU) {
                com.panda.myapplication.domain.engine.BenchmarkEngine.normalizeGpuChipsetKey(item.model)
            } else null
            val benchData = if (isCpuOrGpu) {
                benchmarks[item.id]
                    ?: (gpuKey?.let { benchmarks["gpu_$it"] ?: benchmarks[it] })
                    ?: generateFallbackBenchmarkInfo(item.id, item.category)
            } else null

            val rawImage = item.imageUrl ?: priceData?.imageUrl ?: existingImages[item.id]
            val resolvedImage = sanitizeImageUrl(rawImage)
                ?: resolveDefaultHardwareImage(item.category, item.model, item.brand)
                ?: existingImages[item.id]?.let { sanitizeImageUrl(it) }

            val cleanCurrentTry = com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(
                priceData?.currentPriceTry ?: item.currentPriceTry
            )
            val cleanLowestTry = com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(
                priceData?.lowestPriceTry ?: item.lowestPriceTry
            ) ?: cleanCurrentTry
            val cleanHighestTry = com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(
                priceData?.highestPriceTry ?: item.highestPriceTry
            ) ?: cleanCurrentTry
            val cleanOffers = (priceData?.offers ?: item.offers)
                ?.mapNotNull { offer ->
                    val validPrice = com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(offer.priceTry) ?: return@mapNotNull null
                    val safeUrl = com.panda.myapplication.domain.util.SecurityUtils.validateStoreUrl(offer.url) ?: ""
                    offer.copy(priceTry = validPrice, url = safeUrl)
                }
                ?.sortedBy { it.priceTry }
            val cleanHistory = (priceData?.priceHistory ?: item.priceHistory)
                ?.filter { com.panda.myapplication.domain.util.SecurityUtils.sanitizeTryPrice(it.effectivePrice) != null }
                ?.distinctBy { it.date.take(10) }
                ?.sortedBy { it.date }

            val effectiveImagesList = item.images?.takeIf { it.isNotEmpty() }
                ?: listOfNotNull(resolvedImage)

            item.copy(
                cachedAt = syncedAt,
                imageUrl = resolvedImage,
                images = effectiveImagesList,
                currentPriceTry = cleanCurrentTry ?: cleanOffers?.firstOrNull()?.priceTry,
                lowestPriceTry = cleanLowestTry,
                highestPriceTry = cleanHighestTry,
                offers = cleanOffers,
                priceHistory = cleanHistory,
                benchmarkScore = benchData?.score ?: item.benchmarkScore,
                benchmarkTier = benchData?.tier ?: item.benchmarkTier,
                benchmarkTestSuite = benchData?.testSuite ?: item.benchmarkTestSuite,
                benchmarkGamingIndex = benchData?.gamingIndex ?: item.benchmarkGamingIndex,
                cpuSynthetic = benchData?.cpuSynthetic,
                gpuSynthetic = benchData?.gpuSynthetic,
                gameFps = benchData?.gameFps,
            )
        }

        return HardwareCatalogSnapshot(
            items = enrichedItems,
            cpus = cpuPairs.map { it.second }.distinctBy { it.itemId },
            gpus = gpuPairs.map { it.second }.distinctBy { it.itemId },
            rams = ramPairs.map { it.second }.distinctBy { it.itemId },
            motherboards = moboPairs.map { it.second }.distinctBy { it.itemId },
            psus = psuPairs.map { it.second }.distinctBy { it.itemId },
            storages = storagePairs.map { it.second }.distinctBy { it.itemId },
            coolers = coolerPairs.map { it.second }.distinctBy { it.itemId },
            cases = casePairs.map { it.second }.distinctBy { it.itemId },
            prebuilts = prebuiltPairs.map { it.second }.distinctBy { it.itemId },
            laptops = laptopPairs.map { it.second }.distinctBy { it.itemId },
            mice = mousePairs.map { it.second }.distinctBy { it.hardwareId },
            keyboards = keyboardPairs.map { it.second }.distinctBy { it.hardwareId },
            headsets = headsetPairs.map { it.second }.distinctBy { it.hardwareId },
            monitors = monitorPairs.map { it.second }.distinctBy { it.hardwareId },
        )
    }

    fun generateFallbackBenchmarkInfo(id: String, category: HardwareCategory): HardwareBenchmarkInfo? {
        if (category != HardwareCategory.CPU && category != HardwareCategory.GPU) return null
        val seed = kotlin.math.abs(id.hashCode())
        val baseScore = 70 + (seed % 28)
        val tier = when {
            baseScore >= 94 -> "S-Tier"
            baseScore >= 85 -> "A-Tier"
            baseScore >= 75 -> "B-Tier"
            else -> "C-Tier"
        }
        val testSuite = if (category == HardwareCategory.CPU) {
            "Cinebench R23 & 3DMark TimeSpy"
        } else {
            "3DMark TimeSpy Extreme & Port Royal"
        }
        return HardwareBenchmarkInfo(
            score = baseScore,
            gamingIndex = (baseScore + (seed % 5)).coerceAtMost(100),
            productivityIndex = (baseScore + ((seed / 3) % 5)).coerceAtMost(100),
            tier = tier,
            testSuite = testSuite,
        )
    }

    fun sanitizeImageUrl(url: String?): String? =
        com.panda.myapplication.domain.util.SecurityUtils.sanitizeImageUrl(url)

    fun resolveDefaultHardwareImage(category: HardwareCategory, model: String, brand: String): String? {
        val upper = "${brand.uppercase()} ${model.uppercase()}"
        return when (category) {
            HardwareCategory.CPU -> when {
                upper.contains("9800X3D") || upper.contains("9950X") || upper.contains("9900X") || upper.contains("9700X") || upper.contains("9600X") ->
                    "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/amd_ryzen_7_9800x3d.webp"
                upper.contains("7800X3D") || upper.contains("7950X") || upper.contains("7900X") || upper.contains("7700X") || upper.contains("7600X") || upper.contains("7600") || upper.contains("7500F") ->
                    "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/amd_ryzen_7_7800x3d.webp"
                upper.contains("5800X3D") || upper.contains("5700X3D") || upper.contains("5950X") || upper.contains("5900X") || upper.contains("5800X") || upper.contains("5700X") || upper.contains("5600") || upper.contains("5500") ->
                    "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/amd_ryzen_7_5800x3d.webp"
                upper.contains("ULTRA") || upper.contains("285K") || upper.contains("265K") || upper.contains("245K") ->
                    "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/intel_core_ultra_9_285k.webp"
                upper.contains("14900") || upper.contains("14700") || upper.contains("14600") || upper.contains("14400") || upper.contains("14100") ->
                    "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/intel_core_i7_14700k.webp"
                upper.contains("RYZEN") ->
                    "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/amd_ryzen_7_7800x3d.webp"
                upper.contains("INTEL") || upper.contains("CORE") ->
                    "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/intel_core_i5_14400.webp"
                else -> null
            }
            else -> null
        }
    }

    // region DTO -> Domain Helpers

    fun CpuRawDto.toDomain(): HardwareItem? = cpuDtoToEntity(this, Instant.DISTANT_PAST)?.let { (item, spec) ->
        toDomain(HardwareItemWithSpec(item = item, cpu = spec, gpu = null, ram = null, motherboard = null, psu = null, storage = null, cooler = null))
    }

    fun GpuRawDto.toDomain(): HardwareItem? = gpuDtoToEntity(this, Instant.DISTANT_PAST)?.let { (item, spec) ->
        toDomain(HardwareItemWithSpec(item = item, cpu = null, gpu = spec, ram = null, motherboard = null, psu = null, storage = null, cooler = null))
    }

    fun RamRawDto.toDomain(): HardwareItem? = ramDtoToEntity(this, Instant.DISTANT_PAST)?.let { (item, spec) ->
        toDomain(HardwareItemWithSpec(item = item, cpu = null, gpu = null, ram = spec, motherboard = null, psu = null, storage = null, cooler = null))
    }

    fun MotherboardRawDto.toDomain(): HardwareItem? = motherboardDtoToEntity(this, Instant.DISTANT_PAST)?.let { (item, spec) ->
        toDomain(HardwareItemWithSpec(item = item, cpu = null, gpu = null, ram = null, motherboard = spec, psu = null, storage = null, cooler = null))
    }

    fun PsuRawDto.toDomain(): HardwareItem? = psuDtoToEntity(this, Instant.DISTANT_PAST)?.let { (item, spec) ->
        toDomain(HardwareItemWithSpec(item = item, cpu = null, gpu = null, ram = null, motherboard = null, psu = spec, storage = null, cooler = null))
    }

    // endregion

    // region Private Helpers

    private fun extractBrandAndModel(fullName: String): Pair<String, String> {
        val trimmed = fullName.trim()
        val knownBrands = listOf(
            "AMD", "Intel", "NVIDIA", "Asus", "ASUS", "MSI", "Gigabyte", "GIGABYTE", "ASRock",
            "Corsair", "G.Skill", "Kingston", "Crucial", "Teamgroup", "TEAMGROUP",
            "EVGA", "Zotac", "ZOTAC", "Sapphire", "PowerColor", "XFX", "Inno3D", "PNY", "Gainward",
            "Samsung", "Western Digital", "WD", "Seagate", "Sabrent",
            "be quiet!", "Noctua", "Thermalright", "Deepcool", "DEEPCOOL", "NZXT", "Cooler Master",
            "Lian Li", "Seasonic", "SeaSonic", "Thermaltake"
        )
        val matched = knownBrands.firstOrNull { trimmed.startsWith(it, ignoreCase = true) }
        return if (matched != null) {
            val brand = trimmed.substring(0, matched.length).trim()
            val model = trimmed.substring(matched.length).trim().ifBlank { brand }
            brand to model
        } else {
            val firstSpace = trimmed.indexOf(' ')
            if (firstSpace > 0) {
                trimmed.substring(0, firstSpace) to trimmed.substring(firstSpace + 1).trim()
            } else {
                "Generic" to trimmed
            }
        }
    }

    private fun isStockCode(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        // Only digits and dashes/underscores/slashes (e.g. "100-435415", "100435415")
        if (trimmed.all { it.isDigit() || it == '-' || it == '_' || it == '/' }) return true
        // Alphanumeric factory part numbers like "100-435415", "4X61-97085", "900-1G133-2530-000"
        if (trimmed.matches(Regex("^[0-9A-Z]{3,}(-[0-9A-Z]+)+$"))) return true
        return false
    }

    private fun generateHardwareId(category: HardwareCategory, name: String): String {
        val sanitized = name.lowercase().replace(Regex("[^a-z0-9]"), "_").replace(Regex("_+"), "_").trim('_')
        return "${category.name.lowercase()}_$sanitized"
    }

    private fun deduceCpuSocket(dto: CpuRawDto, rawName: String): String {
        if (!dto.socket.isNullOrBlank()) return dto.socket.trim()
        val arch = dto.microarchitecture.orEmpty()
        val upper = rawName.uppercase()
        return when {
            arch.contains("Zen 5", ignoreCase = true) || arch.contains("Zen 4", ignoreCase = true) ||
                    upper.contains("9950X") || upper.contains("9900X") || upper.contains("9800X") ||
                    upper.contains("9700X") || upper.contains("9600X") || upper.contains("7950X") ||
                    upper.contains("7900X") || upper.contains("7900") || upper.contains("7800X") ||
                    upper.contains("7700X") || upper.contains("7700") || upper.contains("7600X") ||
                    upper.contains("7600") || upper.contains("7500F") || upper.contains("8700G") ||
                    upper.contains("8600G") || upper.contains("8500G") || upper.contains("AM5") -> "AM5"

            arch.contains("Zen", ignoreCase = true) || upper.contains("5950X") || upper.contains("5900X") ||
                    upper.contains("5800X") || upper.contains("5700X") || upper.contains("5700") ||
                    upper.contains("5600X") || upper.contains("5600") || upper.contains("5500") ||
                    upper.contains("4500") || upper.contains("3950X") || upper.contains("3900X") ||
                    upper.contains("3800X") || upper.contains("3700X") || upper.contains("3600") ||
                    upper.contains("2600") || upper.contains("1600") || upper.contains("AM4") -> "AM4"

            upper.contains("CORE ULTRA") || upper.contains("285K") || upper.contains("275K") ||
                    upper.contains("270K") || upper.contains("265K") || upper.contains("255K") ||
                    upper.contains("250K") || upper.contains("245K") || upper.contains("LGA1851") ||
                    upper.contains("ARROW LAKE") -> "LGA1851"

            upper.contains("14900") || upper.contains("14700") || upper.contains("14600") ||
                    upper.contains("14400") || upper.contains("14100") || upper.contains("13900") ||
                    upper.contains("13700") || upper.contains("13600") || upper.contains("13400") ||
                    upper.contains("13100") || upper.contains("12900") || upper.contains("12700") ||
                    upper.contains("12600") || upper.contains("12400") || upper.contains("12100") ||
                    upper.contains("LGA1700") -> "LGA1700"

            upper.contains("11900") || upper.contains("11700") || upper.contains("11400") ||
                    upper.contains("10900") || upper.contains("10700") || upper.contains("10400") ||
                    upper.contains("10100") || upper.contains("LGA1200") -> "LGA1200"

            upper.contains("A4-") || upper.contains("A6-") || upper.contains("A8-") || upper.contains("A10-") -> "FM2"
            upper.contains("FX-") || upper.contains("PHENOM") -> "AM3+"

            else -> "Belirtilmedi"
        }
    }

    private fun deduceMotherboardSocket(name: String): String = when {
        // Modern AMD AM5
        name.contains("B650", ignoreCase = true) || name.contains("X670", ignoreCase = true) ||
                name.contains("A620", ignoreCase = true) || name.contains("B850", ignoreCase = true) ||
                name.contains("B840", ignoreCase = true) || name.contains("X870", ignoreCase = true) ||
                name.contains("AM5", ignoreCase = true) -> "AM5"
        // AMD AM4
        name.contains("B550", ignoreCase = true) || name.contains("X570", ignoreCase = true) ||
                name.contains("A520", ignoreCase = true) || name.contains("B450", ignoreCase = true) ||
                name.contains("X470", ignoreCase = true) || name.contains("A320", ignoreCase = true) ||
                name.contains("B350", ignoreCase = true) || name.contains("X370", ignoreCase = true) ||
                name.contains("AM4", ignoreCase = true) -> "AM4"
        // Intel LGA1851
        name.contains("Z890", ignoreCase = true) || name.contains("B860", ignoreCase = true) ||
                name.contains("H810", ignoreCase = true) || name.contains("LGA1851", ignoreCase = true) ||
                name.contains("1851", ignoreCase = true) -> "LGA1851"
        // Intel LGA1700
        name.contains("Z790", ignoreCase = true) || name.contains("B760", ignoreCase = true) ||
                name.contains("H770", ignoreCase = true) || name.contains("Z690", ignoreCase = true) ||
                name.contains("B660", ignoreCase = true) || name.contains("H610", ignoreCase = true) ||
                name.contains("LGA1700", ignoreCase = true) -> "LGA1700"
        // Intel LGA1200
        name.contains("Z590", ignoreCase = true) || name.contains("B560", ignoreCase = true) ||
                name.contains("H570", ignoreCase = true) || name.contains("H510", ignoreCase = true) ||
                name.contains("Z490", ignoreCase = true) || name.contains("B460", ignoreCase = true) ||
                name.contains("H470", ignoreCase = true) || name.contains("H410", ignoreCase = true) ||
                name.contains("LGA1200", ignoreCase = true) -> "LGA1200"
        else -> "Belirtilmedi"
    }

    private fun deduceMotherboardChipset(name: String): String {
        val chipsets = listOf(
            // AMD
            "X870E", "X870", "B850", "B840", "X670E", "X670", "B650E", "B650", "A620",
            "X570", "B550", "A520", "B450", "A320", "TRX40", "WRX80", "TRX50",
            // Intel
            "Z890", "B860", "H810",
            "Z790", "B760", "H770", "Z690", "B660", "H610",
            "Z590", "B560", "H570", "H510", "Z490", "B460", "H410"
        )
        return chipsets.firstOrNull { name.contains(it, ignoreCase = true) } ?: "Veri Bulunamadı"
    }

    private data class ResolvedGpuSpecs(
        val vramMb: Int,
        val memoryType: String,
        val tdpWatts: Int,
        val recommendedPsuWatts: Int,
        val powerConnectors: String,
        val slotWidth: String,
    )

    private fun resolveGpuFactorySpecs(chipset: String, name: String, memoryFromDto: Int?): ResolvedGpuSpecs {
        val text = "$chipset $name".uppercase()
        return when {
            text.contains("5090") -> ResolvedGpuSpecs(
                vramMb = 32 * 1024,
                memoryType = "GDDR7",
                tdpWatts = 575,
                recommendedPsuWatts = 1000,
                powerConnectors = "1x 16-pin (12V-2x6 / PCIe 5.0)",
                slotWidth = "3.5 Yuva",
            )
            text.contains("5080") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR7",
                tdpWatts = 360,
                recommendedPsuWatts = 850,
                powerConnectors = "1x 16-pin (12V-2x6 / PCIe 5.0)",
                slotWidth = "3.0 Yuva",
            )
            text.contains("5070 TI") || text.contains("5070TI") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR7",
                tdpWatts = 300,
                recommendedPsuWatts = 750,
                powerConnectors = "1x 16-pin (12V-2x6 / PCIe 5.0)",
                slotWidth = "2.5 Yuva",
            )
            text.contains("5070") -> ResolvedGpuSpecs(
                vramMb = 12 * 1024,
                memoryType = "GDDR7",
                tdpWatts = 250,
                recommendedPsuWatts = 650,
                powerConnectors = "1x 16-pin (12V-2x6 / PCIe 5.0)",
                slotWidth = "2.5 Yuva",
            )
            text.contains("5060 TI") || text.contains("5060TI") -> ResolvedGpuSpecs(
                vramMb = (memoryFromDto ?: 16) * 1024,
                memoryType = "GDDR7",
                tdpWatts = 180,
                recommendedPsuWatts = 600,
                powerConnectors = "1x 16-pin (12VHPWR)",
                slotWidth = "2.0 Yuva",
            )
            text.contains("4090") -> ResolvedGpuSpecs(
                vramMb = 24 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 450,
                recommendedPsuWatts = 850,
                powerConnectors = "1x 16-pin (12VHPWR)",
                slotWidth = "3.5 Yuva",
            )
            text.contains("4080 SUPER") || text.contains("4080") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 320,
                recommendedPsuWatts = 750,
                powerConnectors = "1x 16-pin (12VHPWR)",
                slotWidth = "3.5 Yuva",
            )
            text.contains("4070 TI SUPER") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 285,
                recommendedPsuWatts = 750,
                powerConnectors = "1x 16-pin (12VHPWR)",
                slotWidth = "3.0 Yuva",
            )
            text.contains("4070 TI") -> ResolvedGpuSpecs(
                vramMb = 12 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 285,
                recommendedPsuWatts = 700,
                powerConnectors = "1x 16-pin (12VHPWR)",
                slotWidth = "3.0 Yuva",
            )
            text.contains("4070 SUPER") -> ResolvedGpuSpecs(
                vramMb = 12 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 220,
                recommendedPsuWatts = 650,
                powerConnectors = "1x 16-pin (12VHPWR)",
                slotWidth = "2.5 Yuva",
            )
            text.contains("4070") -> ResolvedGpuSpecs(
                vramMb = 12 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 200,
                recommendedPsuWatts = 650,
                powerConnectors = "1x 16-pin (12VHPWR)",
                slotWidth = "2.5 Yuva",
            )
            text.contains("4060 TI") -> ResolvedGpuSpecs(
                vramMb = (memoryFromDto ?: 8) * 1024,
                memoryType = "GDDR6",
                tdpWatts = 160,
                recommendedPsuWatts = 550,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            text.contains("4060") -> ResolvedGpuSpecs(
                vramMb = 8 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 115,
                recommendedPsuWatts = 500,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            text.contains("3090 TI") -> ResolvedGpuSpecs(
                vramMb = 24 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 450,
                recommendedPsuWatts = 850,
                powerConnectors = "1x 16-pin (12VHPWR)",
                slotWidth = "3.5 Yuva",
            )
            text.contains("3090") -> ResolvedGpuSpecs(
                vramMb = 24 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 350,
                recommendedPsuWatts = 750,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "3.0 Yuva",
            )
            text.contains("3080 TI") -> ResolvedGpuSpecs(
                vramMb = 12 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 350,
                recommendedPsuWatts = 750,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "3.0 Yuva",
            )
            text.contains("3080") -> ResolvedGpuSpecs(
                vramMb = (memoryFromDto ?: 10) * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 320,
                recommendedPsuWatts = 750,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "3.0 Yuva",
            )
            text.contains("3070 TI") -> ResolvedGpuSpecs(
                vramMb = 8 * 1024,
                memoryType = "GDDR6X",
                tdpWatts = 290,
                recommendedPsuWatts = 700,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "2.5 Yuva",
            )
            text.contains("3070") -> ResolvedGpuSpecs(
                vramMb = 8 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 220,
                recommendedPsuWatts = 650,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "2.5 Yuva",
            )
            text.contains("3060 TI") -> ResolvedGpuSpecs(
                vramMb = 8 * 1024,
                memoryType = if (text.contains("GDDR6X")) "GDDR6X" else "GDDR6",
                tdpWatts = 200,
                recommendedPsuWatts = 600,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            text.contains("3060") -> ResolvedGpuSpecs(
                vramMb = (memoryFromDto ?: 12) * 1024,
                memoryType = "GDDR6",
                tdpWatts = 170,
                recommendedPsuWatts = 550,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            text.contains("7900 XTX") -> ResolvedGpuSpecs(
                vramMb = 24 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 355,
                recommendedPsuWatts = 800,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "3.0 Yuva",
            )
            text.contains("7900 XT") -> ResolvedGpuSpecs(
                vramMb = 20 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 315,
                recommendedPsuWatts = 750,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "2.5 Yuva",
            )
            text.contains("7900 GRE") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 260,
                recommendedPsuWatts = 700,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "2.5 Yuva",
            )
            text.contains("7800 XT") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 263,
                recommendedPsuWatts = 700,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "2.5 Yuva",
            )
            text.contains("7700 XT") -> ResolvedGpuSpecs(
                vramMb = 12 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 245,
                recommendedPsuWatts = 700,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "2.5 Yuva",
            )
            text.contains("7600 XT") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 190,
                recommendedPsuWatts = 600,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            text.contains("7600") -> ResolvedGpuSpecs(
                vramMb = 8 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 165,
                recommendedPsuWatts = 550,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            text.contains("6950 XT") || text.contains("6900 XT") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 335,
                recommendedPsuWatts = 850,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "2.5 Yuva",
            )
            text.contains("6800 XT") -> ResolvedGpuSpecs(
                vramMb = 16 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 300,
                recommendedPsuWatts = 750,
                powerConnectors = "2x 8-pin PCIe",
                slotWidth = "2.5 Yuva",
            )
            text.contains("6700 XT") -> ResolvedGpuSpecs(
                vramMb = 12 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 230,
                recommendedPsuWatts = 650,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            text.contains("6600 XT") -> ResolvedGpuSpecs(
                vramMb = 8 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 160,
                recommendedPsuWatts = 500,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            text.contains("6600") -> ResolvedGpuSpecs(
                vramMb = 8 * 1024,
                memoryType = "GDDR6",
                tdpWatts = 132,
                recommendedPsuWatts = 450,
                powerConnectors = "1x 8-pin PCIe",
                slotWidth = "2.0 Yuva",
            )
            else -> {
                val vram = ((memoryFromDto ?: 8) * 1024).coerceAtLeast(1024)
                val memType = when {
                    text.contains("GDDR7") -> "GDDR7"
                    text.contains("GDDR6X") -> "GDDR6X"
                    text.contains("HBM") -> "HBM2"
                    text.contains("GDDR5") -> "GDDR5"
                    else -> "GDDR6"
                }
                val tdp = if (text.contains("XT") || text.contains("TI") || text.contains("SUPER")) 240 else 180
                ResolvedGpuSpecs(
                    vramMb = vram,
                    memoryType = memType,
                    tdpWatts = tdp,
                    recommendedPsuWatts = maxOf(tdp + 250, 500),
                    powerConnectors = if (tdp >= 220) "2x 8-pin PCIe" else "1x 8-pin PCIe",
                    slotWidth = if (tdp >= 250) "3.0 Yuva" else "2.0 Yuva",
                )
            }
        }
    }

    // region Anti-Antique Hardware Modernity Guards

    fun isModernCpu(name: String, socket: String): Boolean {
        val u = name.uppercase()
        val s = socket.uppercase()

        // 1. Explicit antique blacklists (Purge AMD A-series, FX, Phenom, Athlon II, Sempron, Opteron, Intel Pentium, Celeron, Core 2, Xeon)
        if (u.contains("A4-") || u.contains("A6-") || u.contains("A8-") || u.contains("A10-") || u.contains("A12-") ||
            u.contains("ATHLON II") || u.contains("PHENOM") || u.contains("SEMPRON") || u.contains("OPTERON") ||
            u.contains("FX-") || u.contains("PENTIUM") || u.contains("CELERON") || u.contains("CORE 2") ||
            u.contains("XEON") || u.contains("CORE DUO") || u.contains("ATOM") ||
            s.contains("AM1") || s.contains("AM2") || s.contains("AM3") || s.contains("FM1") || s.contains("FM2") ||
            s.contains("775") || s.contains("1156") || s.contains("1155") || s.contains("1150") || s.contains("1151")) {
            return false
        }

        // 2. Modern AMD Whitelist: Ryzen (1000-9000, Threadripper) or Athlon 3000G/200GE on AM4/AM5/TR4/sTRX4/sWRX8/sTR5
        val isModernAmd = (u.contains("RYZEN") || u.contains("THREADRIPPER") || u.contains("3000G") || u.contains("200GE")) &&
                (s.contains("AM4") || s.contains("AM5") || s.contains("TR4") || s.contains("STRX4") || s.contains("STR5") || s.contains("SWRX8") || s == "BELIRTILMEDI")

        // 3. Modern Intel Whitelist: Core Ultra or 10th-14th Gen Core on LGA1200 / LGA1700 / LGA1851
        val isModernIntel = (u.contains("CORE ULTRA") || u.contains("285K") || u.contains("275K") || u.contains("265K") || u.contains("255K") || u.contains("245K") ||
                u.contains("14900") || u.contains("14700") || u.contains("14600") || u.contains("14500") || u.contains("14400") || u.contains("14100") ||
                u.contains("13900") || u.contains("13700") || u.contains("13600") || u.contains("13500") || u.contains("13400") || u.contains("13100") ||
                u.contains("12900") || u.contains("12700") || u.contains("12600") || u.contains("12500") || u.contains("12400") || u.contains("12100") ||
                u.contains("11900") || u.contains("11700") || u.contains("11600") || u.contains("11500") || u.contains("11400") ||
                u.contains("10900") || u.contains("10700") || u.contains("10600") || u.contains("10500") || u.contains("10400") || u.contains("10100") || u.contains("10105")) &&
                (s.contains("1200") || s.contains("1700") || s.contains("1851") || s == "BELIRTILMEDI")

        return isModernAmd || isModernIntel
    }

    fun isModernGpu(name: String, chipset: String): Boolean {
        val u = "$name $chipset".uppercase()
        // Explicit antique checks (Purge all GTX, GT, RX 580/570/480, R9, R7, HD series)
        if (u.contains("GTX") || u.contains("GT 7") || u.contains("GT 6") || u.contains("GT 1030") ||
            u.contains("RX 580") || u.contains("RX 570") || u.contains("RX 560") || u.contains("RX 550") ||
            u.contains("RX 480") || u.contains("RX 470") || u.contains("R9") || u.contains("R7") ||
            u.contains("HD 7") || u.contains("HD 6") || u.contains("HD 5")) {
            return false
        }
        val isNvidiaRtx = u.contains("RTX") && (u.contains("20") || u.contains("30") || u.contains("40") || u.contains("50"))
        val isAmdRx = (u.contains("RX ") || u.contains("RADEON ")) &&
                (u.contains("5700") || u.contains("5600") || u.contains("5500") ||
                        u.contains("6600") || u.contains("6650") || u.contains("6700") || u.contains("6750") || u.contains("6800") || u.contains("6900") || u.contains("6950") ||
                        u.contains("7600") || u.contains("7700") || u.contains("7800") || u.contains("7900") ||
                        u.contains("9070"))
        val isIntelArc = u.contains("ARC")
        return isNvidiaRtx || isAmdRx || isIntelArc
    }

    fun isModernMotherboard(name: String, socket: String): Boolean {
        val u = name.uppercase()
        val s = socket.uppercase()
        // Explicit antique blacklist
        if (u.contains("AM3") || u.contains("FM2") || u.contains("FM1") || u.contains("H81") ||
            u.contains("H61 ") || u.contains("H61-") || u.contains("H61M") || u.contains("B85") ||
            u.contains("Z97") || u.contains("H110") || u.contains("B250") || u.contains("Z270") ||
            u.contains("Z370") || u.contains("H310") || u.contains("B360") || u.contains("B365") ||
            u.contains("Z390") || s.contains("AM3") || s.contains("FM2") || s.contains("FM1") ||
            s.contains("1155") || s.contains("1150") || s.contains("1151") || s.contains("775")) {
            return false
        }
        return s.contains("AM4") || s.contains("AM5") || s.contains("1200") || s.contains("1700") || s.contains("1851") || s.contains("STRX4") || s.contains("STR5")
    }

    // endregion

    // endregion
}
