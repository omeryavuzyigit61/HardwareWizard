package com.panda.myapplication.data.mapper

import com.panda.myapplication.data.local.entity.CaseSpecEntity
import com.panda.myapplication.data.local.entity.CoolerSpecEntity
import com.panda.myapplication.data.local.entity.CpuSpecEntity
import com.panda.myapplication.data.local.entity.GpuSpecEntity
import com.panda.myapplication.data.local.entity.HardwareCatalogSnapshot
import com.panda.myapplication.data.local.entity.HardwareItemEntity
import com.panda.myapplication.data.local.entity.HardwareItemWithSpec
import com.panda.myapplication.data.local.entity.MotherboardSpecEntity
import com.panda.myapplication.data.local.entity.PsuSpecEntity
import com.panda.myapplication.data.local.entity.RamSpecEntity
import com.panda.myapplication.data.local.entity.StorageSpecEntity
import com.panda.myapplication.data.model.dto.CaseRawDto
import com.panda.myapplication.data.model.dto.CoolerRawDto
import com.panda.myapplication.data.model.dto.CpuRawDto
import com.panda.myapplication.data.model.dto.GpuRawDto
import com.panda.myapplication.data.model.dto.MotherboardRawDto
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
import com.panda.myapplication.domain.model.MemorySize
import com.panda.myapplication.domain.model.Money
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.PsuSpec
import com.panda.myapplication.domain.model.RamSpec
import com.panda.myapplication.domain.model.PricePoint
import com.panda.myapplication.domain.model.ProductPriceInfo
import com.panda.myapplication.domain.model.HardwareBenchmarkInfo
import com.panda.myapplication.domain.model.StorageFormFactor
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.model.StorageSpec
import com.panda.myapplication.domain.model.Wattage
import kotlin.time.Instant

object HardwareMapper {

    // region Room -> Domain

    fun toDomain(itemWithSpec: HardwareItemWithSpec): HardwareItem? {
        val entity = itemWithSpec.item
        val spec = when {
            itemWithSpec.cpu != null -> {
                val c = itemWithSpec.cpu
                CpuSpec(
                    socket = c.socket,
                    coreCount = c.coreCount,
                    threadCount = c.threadCount,
                    baseClock = Frequency(c.baseClockMhz),
                    boostClock = c.boostClockMhz?.let { Frequency(it) },
                    tdp = Wattage(c.tdpWatts),
                    hasIntegratedGraphics = c.hasIntegratedGraphics,
                    architecture = c.architecture,
                    l2CacheMb = c.l2CacheMb,
                    l3CacheMb = c.l3CacheMb,
                )
            }
            itemWithSpec.gpu != null -> {
                val g = itemWithSpec.gpu
                GpuSpec(
                    chipset = g.chipset,
                    vram = MemorySize(g.vramMb),
                    memoryType = g.memoryType,
                    coreClock = Frequency(g.coreClockMhz),
                    boostClock = g.boostClockMhz?.let { Frequency(it) },
                    tdp = Wattage(g.tdpWatts),
                    recommendedPsu = Wattage(g.recommendedPsuWatts),
                    busWidthBits = g.busWidthBits,
                    lengthMm = g.lengthMm,
                    powerConnectors = g.powerConnectors,
                    displayOutputs = g.displayOutputs,
                    slotWidth = g.slotWidth,
                    partNumber = g.partNumber,
                )
            }
            itemWithSpec.ram != null -> {
                val r = itemWithSpec.ram
                RamSpec(
                    generation = r.generation,
                    speed = Frequency(r.speedMts),
                    casLatency = r.casLatency,
                    capacity = MemorySize(r.capacityMb),
                    moduleCount = r.moduleCount,
                    firstWordLatencyNs = r.firstWordLatencyNs,
                    voltage = r.voltage,
                    heatSpreader = r.heatSpreader,
                    rgb = r.rgb,
                )
            }
            itemWithSpec.motherboard != null -> {
                val m = itemWithSpec.motherboard
                MotherboardSpec(
                    socket = m.socket,
                    chipset = m.chipset,
                    formFactor = m.formFactor,
                    memoryGeneration = m.memoryGeneration,
                    ramSlots = m.ramSlots,
                    pcieGeneration = m.pcieGeneration,
                    maxMemoryGb = m.maxMemoryGb,
                    m2Slots = m.m2Slots,
                    pcieSlotCount = m.pcieSlotCount,
                    sataPorts = m.sataPorts,
                    hasWifi = m.hasWifi,
                    vrmPhases = m.vrmPhases,
                    interfacePorts = m.interfacePorts,
                )
            }
            itemWithSpec.psu != null -> {
                val p = itemWithSpec.psu
                PsuSpec(
                    wattage = Wattage(p.wattage),
                    efficiency = p.efficiency,
                    isAtx3Compliant = p.isAtx3Compliant,
                    supportsPcie5Power = p.supportsPcie5Power,
                    modularity = p.modularity,
                    formFactor = p.formFactor,
                    outputs = p.outputs,
                    tierRating = p.tierRating,
                    capacitors = p.capacitors,
                )
            }
            itemWithSpec.storage != null -> {
                val s = itemWithSpec.storage
                StorageSpec(
                    mediaType = s.mediaType,
                    storageInterface = s.storageInterface,
                    formFactor = s.formFactor,
                    pcieGeneration = s.pcieGeneration,
                    capacity = MemorySize(s.capacityMb),
                    sequentialReadMbps = s.sequentialReadMbps,
                    sequentialWriteMbps = s.sequentialWriteMbps,
                    nandType = s.nandType,
                    enduranceTbw = s.enduranceTbw,
                    cacheMb = s.cacheMb,
                )
            }
            itemWithSpec.cooler != null -> {
                val cl = itemWithSpec.cooler
                CoolerSpec(
                    type = cl.type,
                    radiatorSizeMm = cl.radiatorSizeMm,
                    maxCoolingTdp = Wattage(cl.maxCoolingTdpWatts),
                    supportedSockets = cl.supportedSockets,
                    heightMm = cl.heightMm,
                    fanRpm = cl.fanRpm,
                    noiseDba = cl.noiseDba,
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
                )
            }
            else -> return null
        }

        val price = if (entity.priceMinor != null && entity.currencyCode != null) {
            Money(amountMinor = entity.priceMinor, currencyCode = entity.currencyCode)
        } else null

        val priceInfo = if (entity.currentPriceTry != null || !entity.offers.isNullOrEmpty() || !entity.priceHistory.isNullOrEmpty()) {
            ProductPriceInfo(
                currentPriceTry = entity.currentPriceTry,
                lowestPriceTry = entity.lowestPriceTry ?: entity.currentPriceTry,
                highestPriceTry = entity.highestPriceTry ?: entity.currentPriceTry,
                currency = "TRY",
                offers = entity.offers.orEmpty(),
                priceHistory = entity.priceHistory.orEmpty(),
            )
        } else {
            null
        }

        val intermediateItem = runCatching {
            HardwareItem(
                id = HardwareId(entity.id),
                brand = entity.brand,
                model = entity.model,
                price = price,
                spec = spec,
                priceInfo = priceInfo,
                benchmarkInfo = null,
            )
        }.getOrNull() ?: return null

        val isCpuOrGpu = entity.category == HardwareCategory.CPU || entity.category == HardwareCategory.GPU
        val benchmarkInfo = if (isCpuOrGpu) {
            if (entity.benchmarkScore != null) {
                HardwareBenchmarkInfo(
                    score = entity.benchmarkScore,
                    tier = entity.benchmarkTier ?: "A-Tier",
                    gamingIndex = entity.benchmarkGamingIndex,
                    testSuite = entity.benchmarkTestSuite ?: "Donanım Benchmark Laboratuvarı",
                    relativePerformanceIndex = entity.benchmarkGamingIndex?.toDouble() ?: entity.benchmarkScore.toDouble(),
                    cpuSynthetic = entity.cpuSynthetic,
                    gpuSynthetic = entity.gpuSynthetic,
                    gameFps = entity.gameFps,
                    dataSource = com.panda.myapplication.domain.model.DataSourceType.VERIFIED_BENCHMARK.name,
                )
            } else {
                com.panda.myapplication.domain.engine.BenchmarkEngine.simulateBenchmark(intermediateItem)
            }
        } else {
            null
        }

        return intermediateItem.copy(benchmarkInfo = benchmarkInfo)
    }

    // endregion

    // region DTO -> Room Entities

    fun cpuDtoToEntity(dto: CpuRawDto, cachedAt: Instant): Pair<HardwareItemEntity, CpuSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.CPU, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val socket = deduceCpuSocket(dto, rawName)
        val coreCount = (dto.coreCount ?: 6).coerceAtLeast(1)
        val threadCount = (dto.threads ?: (coreCount * 2)).coerceAtLeast(coreCount)
        val baseClockMhz = ((dto.coreClockGhz ?: 3.5) * 1000).toInt().coerceAtLeast(500)
        val boostClockMhz = ((dto.boostClockGhz ?: dto.coreClockGhz ?: 4.0) * 1000).toInt().coerceAtLeast(baseClockMhz)
        val tdpWatts = (dto.tdp ?: 65).coerceAtLeast(15)
        val hasIntegratedGraphics = !dto.graphics.isNullOrBlank() && !dto.graphics.equals("None", ignoreCase = true)

        val architecture = dto.microarchitecture?.ifBlank { null }
        val l3CacheMb = dto.l3CacheMb ?: when {
            coreCount >= 16 -> 64
            coreCount >= 8 -> 32
            else -> 16
        }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.CPU,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
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
            l2CacheMb = dto.l2CacheMb,
            l3CacheMb = l3CacheMb,
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
        val efficiency = when (dto.efficiency?.lowercase()?.trim()) {
            "titanium" -> PsuEfficiencyRating.TITANIUM
            "platinum" -> PsuEfficiencyRating.PLATINUM
            "gold" -> PsuEfficiencyRating.GOLD
            "silver" -> PsuEfficiencyRating.SILVER
            "bronze" -> PsuEfficiencyRating.BRONZE
            else -> PsuEfficiencyRating.GOLD
        }
        val isAtx3 = rawName.contains("PCIE5", ignoreCase = true) || rawName.contains("ATX 3", ignoreCase = true) || rawName.contains("ATX3", ignoreCase = true)

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.PSU,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
        )
        val spec = PsuSpecEntity(
            itemId = id,
            wattage = wattage,
            efficiency = efficiency,
            isAtx3Compliant = isAtx3,
            supportsPcie5Power = isAtx3,
            modularity = dto.modularity,
            formFactor = dto.type,
            outputs = dto.outputs,
            tierRating = dto.tierRating,
            capacitors = dto.capacitors,
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

        val mediaType = if (isHdd) StorageMediaType.HDD else StorageMediaType.SSD
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
        )
        return item to spec
    }

    fun caseDtoToEntity(dto: CaseRawDto, cachedAt: Instant): Pair<HardwareItemEntity, CaseSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.CASE, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }
        val type = dto.type?.trim()

        val maxGpu = when {
            type?.contains("Full Tower", ignoreCase = true) == true -> 420
            type?.contains("Mid Tower", ignoreCase = true) == true -> 380
            type?.contains("Mini Tower", ignoreCase = true) == true -> 330
            type?.contains("Mini ITX", ignoreCase = true) == true || type?.contains("Desktop", ignoreCase = true) == true -> 310
            else -> 360
        }
        val maxCooler = when {
            type?.contains("Full Tower", ignoreCase = true) == true -> 185
            type?.contains("Mid Tower", ignoreCase = true) == true -> 165
            type?.contains("Mini Tower", ignoreCase = true) == true -> 155
            type?.contains("Mini ITX", ignoreCase = true) == true || type?.contains("Desktop", ignoreCase = true) == true -> 140
            else -> 160
        }
        val maxRad = when {
            type?.contains("Full Tower", ignoreCase = true) == true -> 420
            type?.contains("Mid Tower", ignoreCase = true) == true -> 360
            type?.contains("Mini Tower", ignoreCase = true) == true -> 240
            type?.contains("Mini ITX", ignoreCase = true) == true -> 240
            else -> 280
        }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.CASE,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
        )
        val spec = CaseSpecEntity(
            itemId = id,
            type = type,
            maxGpuLengthMm = dto.maxGpuLengthMm ?: maxGpu,
            maxCpuCoolerHeightMm = dto.maxCpuCoolerHeightMm ?: maxCooler,
            maxRadiatorSizeMm = dto.maxRadiatorSizeMm ?: maxRad,
            sidePanel = dto.sidePanel,
            driveBays = dto.driveBays,
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
        pricesTr: Map<String, ProductPriceInfo> = emptyMap(),
        benchmarks: Map<String, HardwareBenchmarkInfo> = emptyMap(),
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

        val rawItems = (cpuPairs.map { it.first } +
                gpuPairs.map { it.first } +
                ramPairs.map { it.first } +
                moboPairs.map { it.first } +
                psuPairs.map { it.first } +
                storagePairs.map { it.first } +
                coolerPairs.map { it.first } +
                casePairs.map { it.first })
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

            item.copy(
                currentPriceTry = priceData?.currentPriceTry,
                lowestPriceTry = priceData?.lowestPriceTry,
                highestPriceTry = priceData?.highestPriceTry,
                offers = priceData?.offers,
                priceHistory = priceData?.priceHistory,
                benchmarkScore = benchData?.score,
                benchmarkTier = benchData?.tier,
                benchmarkTestSuite = benchData?.testSuite,
                benchmarkGamingIndex = benchData?.gamingIndex,
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
        val testSuite = when (category) {
            HardwareCategory.CPU -> "Cinebench R23 & 3DMark TimeSpy"
            HardwareCategory.GPU -> "3DMark TimeSpy Extreme & Port Royal"
            else -> "Benchmark Laboratuvarı"
        }
        return HardwareBenchmarkInfo(
            score = baseScore,
            gamingIndex = (baseScore + (seed % 5)).coerceAtMost(100),
            productivityIndex = (baseScore + ((seed / 3) % 5)).coerceAtMost(100),
            tier = tier,
            testSuite = testSuite,
        )
    }


    // endregion

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
        return when {
            arch.contains("Zen 5", ignoreCase = true) || arch.contains("Zen 4", ignoreCase = true) ||
                    rawName.contains("9800X", ignoreCase = true) || rawName.contains("7800X", ignoreCase = true) ||
                    rawName.contains("7600", ignoreCase = true) || rawName.contains("7700", ignoreCase = true) ||
                    rawName.contains("7900", ignoreCase = true) || rawName.contains("7950", ignoreCase = true) ||
                    rawName.contains("9600", ignoreCase = true) || rawName.contains("9700", ignoreCase = true) ||
                    rawName.contains("9900", ignoreCase = true) || rawName.contains("9950", ignoreCase = true) -> "AM5"

            arch.contains("Zen", ignoreCase = true) || rawName.contains("5600", ignoreCase = true) ||
                    rawName.contains("5700", ignoreCase = true) || rawName.contains("5800", ignoreCase = true) ||
                    rawName.contains("3600", ignoreCase = true) -> "AM4"

            rawName.contains("12900", ignoreCase = true) || rawName.contains("13600", ignoreCase = true) ||
                    rawName.contains("13700", ignoreCase = true) || rawName.contains("13900", ignoreCase = true) ||
                    rawName.contains("14600", ignoreCase = true) || rawName.contains("14700", ignoreCase = true) ||
                    rawName.contains("14900", ignoreCase = true) -> "LGA1700"

            rawName.contains("Core Ultra", ignoreCase = true) || rawName.contains("265K", ignoreCase = true) ||
                    rawName.contains("285K", ignoreCase = true) -> "LGA1851"

            else -> if (rawName.contains("AMD", ignoreCase = true) || rawName.contains("Ryzen", ignoreCase = true)) "AM5" else "LGA1700"
        }
    }

    private fun deduceMotherboardSocket(name: String): String = when {
        // Modern AMD
        name.contains("B650", ignoreCase = true) || name.contains("X670", ignoreCase = true) ||
                name.contains("A620", ignoreCase = true) || name.contains("B850", ignoreCase = true) ||
                name.contains("X870", ignoreCase = true) || name.contains("AM5", ignoreCase = true) -> "AM5"
        // AM4
        name.contains("B550", ignoreCase = true) || name.contains("X570", ignoreCase = true) ||
                name.contains("A520", ignoreCase = true) || name.contains("B450", ignoreCase = true) ||
                name.contains("X470", ignoreCase = true) || name.contains("A320", ignoreCase = true) ||
                name.contains("B350", ignoreCase = true) || name.contains("X370", ignoreCase = true) ||
                name.contains("AM4", ignoreCase = true) -> "AM4"
        // Legacy AMD
        name.contains("760G", ignoreCase = true) || name.contains("760GM", ignoreCase = true) ||
                name.contains("970", ignoreCase = true) || name.contains("990FX", ignoreCase = true) ||
                name.contains("990X", ignoreCase = true) || name.contains("880G", ignoreCase = true) ||
                name.contains("890FX", ignoreCase = true) || name.contains("890GX", ignoreCase = true) ||
                name.contains("785G", ignoreCase = true) || name.contains("780G", ignoreCase = true) ||
                name.contains("740G", ignoreCase = true) || name.contains("AM3+", ignoreCase = true) ||
                name.contains("AM3", ignoreCase = true) -> "AM3"
        name.contains("FM2+", ignoreCase = true) || name.contains("FM2", ignoreCase = true) ||
                name.contains("A88X", ignoreCase = true) || name.contains("A78", ignoreCase = true) ||
                name.contains("A68H", ignoreCase = true) || name.contains("A58", ignoreCase = true) -> "FM2+"
        name.contains("AM2+", ignoreCase = true) || name.contains("AM2", ignoreCase = true) -> "AM2"
        // Modern Intel
        name.contains("Z890", ignoreCase = true) || name.contains("B860", ignoreCase = true) ||
                name.contains("H810", ignoreCase = true) || name.contains("LGA1851", ignoreCase = true) -> "LGA1851"
        name.contains("Z790", ignoreCase = true) || name.contains("B760", ignoreCase = true) ||
                name.contains("H770", ignoreCase = true) || name.contains("Z690", ignoreCase = true) ||
                name.contains("B660", ignoreCase = true) || name.contains("H610", ignoreCase = true) ||
                name.contains("LGA1700", ignoreCase = true) -> "LGA1700"
        name.contains("Z590", ignoreCase = true) || name.contains("B560", ignoreCase = true) ||
                name.contains("H570", ignoreCase = true) || name.contains("H510", ignoreCase = true) ||
                name.contains("Z490", ignoreCase = true) || name.contains("B460", ignoreCase = true) ||
                name.contains("H470", ignoreCase = true) || name.contains("H410", ignoreCase = true) ||
                name.contains("LGA1200", ignoreCase = true) -> "LGA1200"
        name.contains("Z390", ignoreCase = true) || name.contains("Z370", ignoreCase = true) ||
                name.contains("B365", ignoreCase = true) || name.contains("B360", ignoreCase = true) ||
                name.contains("H370", ignoreCase = true) || name.contains("H310", ignoreCase = true) ||
                name.contains("Z270", ignoreCase = true) || name.contains("B250", ignoreCase = true) ||
                name.contains("H270", ignoreCase = true) || name.contains("Z170", ignoreCase = true) ||
                name.contains("B150", ignoreCase = true) || name.contains("H110", ignoreCase = true) ||
                name.contains("LGA1151", ignoreCase = true) -> "LGA1151"
        name.contains("Z97", ignoreCase = true) || name.contains("H97", ignoreCase = true) ||
                name.contains("Z87", ignoreCase = true) || name.contains("H87", ignoreCase = true) ||
                name.contains("B85", ignoreCase = true) || name.contains("H81", ignoreCase = true) ||
                name.contains("LGA1150", ignoreCase = true) -> "LGA1150"
        name.contains("Z77", ignoreCase = true) || name.contains("Z75", ignoreCase = true) ||
                name.contains("H77", ignoreCase = true) || name.contains("B75", ignoreCase = true) ||
                name.contains("Z68", ignoreCase = true) || name.contains("P67", ignoreCase = true) ||
                name.contains("H67", ignoreCase = true) || name.contains("H61", ignoreCase = true) ||
                name.contains("LGA1155", ignoreCase = true) -> "LGA1155"
        name.contains("G41", ignoreCase = true) || name.contains("P45", ignoreCase = true) ||
                name.contains("P43", ignoreCase = true) || name.contains("P35", ignoreCase = true) ||
                name.contains("945", ignoreCase = true) || name.contains("LGA775", ignoreCase = true) -> "LGA775"
        else -> "Bilinmeyen Soket"
    }

    private fun deduceMotherboardChipset(name: String): String {
        val chipsets = listOf(
            // AMD
            "X870E", "X870", "B850", "B840", "X670E", "X670", "B650E", "B650", "A620",
            "X570", "B550", "A520", "X470", "B450", "A320", "X370", "B350",
            "990FX", "990X", "970", "890FX", "890GX", "880G", "870", "790FX", "790GX",
            "785G", "780G", "760GM", "760G", "740G",
            "A88X", "A85X", "A78", "A75", "A68H", "A58", "A55", "TRX40", "WRX80", "TRX50",
            // Intel
            "Z890", "B860", "H810",
            "Z790", "H770", "B760", "Z690", "H670", "B660", "H610",
            "Z590", "H570", "B560", "H510",
            "Z490", "H470", "B460", "H410",
            "Z390", "Z370", "H370", "B365", "B360", "H310",
            "Z270", "H270", "B250", "Z170", "H170", "B150", "H110",
            "Z97", "H97", "Z87", "H87", "B85", "H81",
            "Z77", "Z75", "H77", "B75", "Z68", "P67", "H67", "H61",
            "X79", "X99", "X299", "G41", "P45", "P43", "P35", "945P"
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

    // endregion
}
