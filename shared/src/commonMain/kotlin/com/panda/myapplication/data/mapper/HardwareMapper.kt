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
                    lengthMm = g.lengthMm,
                    powerConnectors = g.powerConnectors,
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
                    vrmPhases = m.vrmPhases,
                    m2Slots = m.m2Slots,
                    hasWifi = m.hasWifi,
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
                    tierRating = p.tierRating,
                    capacitors = p.capacitors,
                    modularity = p.modularity,
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
                )
            }
            itemWithSpec.cooler != null -> {
                val cl = itemWithSpec.cooler
                CoolerSpec(
                    type = cl.type,
                    radiatorSizeMm = cl.radiatorSizeMm,
                    maxCoolingTdp = Wattage(cl.maxCoolingTdpWatts),
                    supportedSockets = cl.supportedSockets,
                )
            }
            itemWithSpec.case != null -> {
                val cs = itemWithSpec.case
                CaseSpec(
                    type = cs.type,
                    maxGpuLengthMm = cs.maxGpuLengthMm,
                    maxCpuCoolerHeightMm = cs.maxCpuCoolerHeightMm,
                    maxRadiatorSizeMm = cs.maxRadiatorSizeMm,
                )
            }
            else -> return null
        }

        val price = if (entity.priceMinor != null && entity.currencyCode != null) {
            Money(amountMinor = entity.priceMinor, currencyCode = entity.currencyCode)
        } else null

        val priceInfo = if (entity.currentPriceTry != null && entity.priceHistory != null && entity.priceHistory.isNotEmpty()) {
            ProductPriceInfo(
                currentPriceTry = entity.currentPriceTry,
                lowestPriceTry = entity.lowestPriceTry ?: entity.currentPriceTry,
                highestPriceTry = entity.highestPriceTry ?: entity.currentPriceTry,
                currency = "TRY",
                priceHistory = entity.priceHistory,
            )
        } else {
            generateFallbackPriceInfo(entity.id, entity.priceMinor ?: 30000L)
        }

        return runCatching {
            HardwareItem(
                id = HardwareId(entity.id),
                brand = entity.brand,
                model = entity.model,
                price = price,
                spec = spec,
                priceInfo = priceInfo,
            )
        }.getOrNull()
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
        val threadCount = (coreCount * 2).coerceAtLeast(coreCount)
        val baseClockMhz = ((dto.coreClockGhz ?: 3.5) * 1000).toInt().coerceAtLeast(500)
        val boostClockMhz = ((dto.boostClockGhz ?: dto.coreClockGhz ?: 4.0) * 1000).toInt().coerceAtLeast(baseClockMhz)
        val tdpWatts = (dto.tdp ?: 65).coerceAtLeast(15)
        val hasIntegratedGraphics = !dto.graphics.isNullOrBlank() && !dto.graphics.equals("None", ignoreCase = true)

        val architecture = dto.microarchitecture?.ifBlank { null }
        val l3CacheMb = when {
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

        val vramMb = ((dto.memory ?: 8) * 1024).coerceAtLeast(1024)
        val memoryType = if (rawName.contains("GDDR6X", ignoreCase = true)) "GDDR6X" else "GDDR6"
        val coreClockMhz = (dto.coreClockMhz ?: 1500).coerceAtLeast(500)
        val boostClockMhz = (dto.boostClockMhz ?: (coreClockMhz + 250)).coerceAtLeast(coreClockMhz)
        val tdpWatts = estimateGpuTdp(chipset, rawName)
        val recommendedPsuWatts = maxOf(tdpWatts + 250, 550)
        val lengthMm = (dto.length?.toInt() ?: 300).coerceAtLeast(150)
        val powerConnectors = if (tdpWatts >= 285 || chipset.contains("4070") || chipset.contains("4080") || chipset.contains("4090")) {
            "1x 16-pin (12VHPWR)"
        } else if (tdpWatts >= 200) {
            "2x 8-pin PCIe"
        } else {
            "1x 8-pin PCIe"
        }
        val slotWidth = if (tdpWatts >= 300) "3.5 Yuva" else if (tdpWatts >= 220) "3.0 Yuva" else "2.5 Yuva"

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
            lengthMm = lengthMm,
            powerConnectors = powerConnectors,
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

        val isDdr5 = dto.speed?.firstOrNull() == 5 || rawName.contains("DDR5", ignoreCase = true)
        val generation = if (isDdr5) DdrGeneration.DDR5 else DdrGeneration.DDR4
        val speedMts = when {
            dto.speed != null && dto.speed.size >= 2 -> dto.speed[1]
            dto.speed != null && dto.speed.size == 1 -> dto.speed[0]
            else -> if (isDdr5) 6000 else 3200
        }.coerceAtLeast(1600)

        val casLatency = (dto.casLatency ?: if (isDdr5) 36 else 16).coerceAtLeast(10)
        val moduleCount = dto.modules?.firstOrNull()?.coerceAtLeast(1) ?: 2
        val perModuleGb = dto.modules?.getOrNull(1)?.coerceAtLeast(4) ?: 16
        val capacityMb = (moduleCount * perModuleGb * 1024).coerceAtLeast(4096)

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
        val chipset = deduceMotherboardChipset(rawName)
        val formFactor = when {
            dto.formFactor?.contains("Micro", ignoreCase = true) == true -> MotherboardFormFactor.MICRO_ATX
            dto.formFactor?.contains("Mini", ignoreCase = true) == true -> MotherboardFormFactor.MINI_ITX
            dto.formFactor?.contains("E-ATX", ignoreCase = true) == true -> MotherboardFormFactor.E_ATX
            else -> MotherboardFormFactor.ATX
        }
        val memoryGeneration = if (socket.equals("AM4", ignoreCase = true)) DdrGeneration.DDR4 else DdrGeneration.DDR5
        val ramSlots = (dto.memorySlots ?: 4).coerceAtLeast(2)
        val pcieGeneration = if (socket.equals("AM5", ignoreCase = true) || chipset.contains("Z790") || chipset.contains("X670")) {
            PcieGeneration.GEN5
        } else {
            PcieGeneration.GEN4
        }

        val vrmPhases = when {
            chipset.contains("Z790") || chipset.contains("X670") -> "18+2+1 Fazlı Güç"
            chipset.contains("B650") || chipset.contains("B760") -> "14+2+1 Fazlı DrMOS"
            else -> "8+2 Faz"
        }
        val m2Slots = if (formFactor == MotherboardFormFactor.MINI_ITX) 2 else if (chipset.contains("Z") || chipset.contains("X")) 4 else 3
        val hasWifi = rawName.contains("WIFI", ignoreCase = true) || rawName.contains("AX", ignoreCase = true)
        val interfacePorts = if (hasWifi) "Wi-Fi 6E, USB 3.2 Gen 2 Type-C, 2.5G LAN" else "USB 3.2 Gen 2 Type-C, Gigabit LAN"

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
            vrmPhases = vrmPhases,
            m2Slots = m2Slots,
            hasWifi = hasWifi,
            interfacePorts = interfacePorts,
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

        val tierRating = when {
            efficiency == PsuEfficiencyRating.TITANIUM || (efficiency == PsuEfficiencyRating.PLATINUM && wattage >= 850) -> "Tier A (En Üst Seviye)"
            efficiency == PsuEfficiencyRating.GOLD && wattage >= 750 -> "Tier A (High-End)"
            efficiency == PsuEfficiencyRating.GOLD || efficiency == PsuEfficiencyRating.SILVER -> "Tier B (Mid-Range)"
            else -> "Tier C (Giriş Seviyesi)"
        }
        val capacitors = if (tierRating.startsWith("Tier A")) "100% Japon 105°C Kapasitörler" else "Japon Üretimi Ana Kapasitör"
        val modularity = if (wattage >= 750) "Tam Modüler (Full-Modular)" else "Yarı Modüler"

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
            tierRating = tierRating,
            capacitors = capacitors,
            modularity = modularity,
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
        val seqRead = if (isNvme) 7000 else 550
        val seqWrite = if (isNvme) 6000 else 500
        val nandType = if (isNvme) "3D TLC NAND Flash" else "3D NAND"
        val enduranceTbw = ((capacityMb / 1024) * 0.6).toInt().coerceAtLeast(300)

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
            nandType = nandType,
            enduranceTbw = enduranceTbw,
        )
        return item to spec
    }

    fun coolerDtoToEntity(dto: CoolerRawDto, cachedAt: Instant): Pair<HardwareItemEntity, CoolerSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.COOLER, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }

        val isLiquid = dto.size != null && dto.size > 0
        val type = if (isLiquid) CoolerType.LIQUID else CoolerType.AIR
        val radiatorSize = if (isLiquid) dto.size else null
        val maxTdp = if (isLiquid) 300 else 220

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
            supportedSockets = listOf("AM5", "AM4", "LGA1700", "LGA1851"),
        )
        return item to spec
    }

    fun caseDtoToEntity(dto: CaseRawDto, cachedAt: Instant): Pair<HardwareItemEntity, CaseSpecEntity>? {
        val rawName = dto.name?.trim() ?: return null
        if (rawName.isBlank()) return null
        val (brand, model) = extractBrandAndModel(rawName)
        val id = generateHardwareId(HardwareCategory.CASE, rawName)
        val priceMinor = dto.price?.let { (it * 100).toLong().coerceAtLeast(0L) }
        val type = dto.type?.trim() ?: "ATX Mid Tower"

        val maxGpu = when {
            type.contains("Full Tower", ignoreCase = true) -> 420
            type.contains("Mid Tower", ignoreCase = true) -> 380
            type.contains("Mini Tower", ignoreCase = true) -> 330
            type.contains("Mini ITX", ignoreCase = true) || type.contains("Desktop", ignoreCase = true) -> 310
            else -> 360
        }
        val maxCooler = when {
            type.contains("Full Tower", ignoreCase = true) -> 185
            type.contains("Mid Tower", ignoreCase = true) -> 165
            type.contains("Mini Tower", ignoreCase = true) -> 155
            type.contains("Mini ITX", ignoreCase = true) || type.contains("Desktop", ignoreCase = true) -> 140
            else -> 160
        }
        val maxRad = when {
            type.contains("Full Tower", ignoreCase = true) -> 420
            type.contains("Mid Tower", ignoreCase = true) -> 360
            type.contains("Mini Tower", ignoreCase = true) -> 240
            type.contains("Mini ITX", ignoreCase = true) -> 240
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
            maxGpuLengthMm = maxGpu,
            maxCpuCoolerHeightMm = maxCooler,
            maxRadiatorSizeMm = maxRad,
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
            val priceData = pricesTr[item.id] ?: generateFallbackPriceInfo(item.id, item.priceMinor ?: 30000L)
            item.copy(
                currentPriceTry = priceData.currentPriceTry,
                lowestPriceTry = priceData.lowestPriceTry,
                highestPriceTry = priceData.highestPriceTry,
                priceHistory = priceData.priceHistory,
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

    fun generateFallbackPriceInfo(id: String, priceMinor: Long): ProductPriceInfo {
        val baseUsd = if (priceMinor > 0) priceMinor / 100.0 else 180.0
        val baseTry = (baseUsd * 38.5).coerceAtLeast(1499.0)
        val seed = kotlin.math.abs(id.hashCode())
        val f1 = ((seed % 17) - 8) / 100.0
        val f2 = (((seed / 17) % 19) - 9) / 100.0
        val f3 = (((seed / 323) % 15) - 7) / 100.0
        val f4 = (((seed / 4845) % 21) - 10) / 100.0
        val f5 = (((seed / 10000) % 13) - 6) / 100.0

        val p1 = kotlin.math.round(baseTry * (1.08 + f1))
        val p2 = kotlin.math.round(baseTry * (1.04 + f2))
        val p3 = kotlin.math.round(baseTry * (1.12 + f3))
        val p4 = kotlin.math.round(baseTry * (0.94 + f4))
        val p5 = kotlin.math.round(baseTry * (0.97 + f5))
        val pCurrent = kotlin.math.round(baseTry)

        val history = listOf(
            PricePoint("2025-10-15", p1),
            PricePoint("2025-12-01", p2),
            PricePoint("2026-02-10", p3),
            PricePoint("2026-04-15", p4),
            PricePoint("2026-07-01", p5),
            PricePoint("2026-10-13", pCurrent),
        )
        val allPrices = history.map { it.price }
        return ProductPriceInfo(
            currentPriceTry = pCurrent,
            lowestPriceTry = allPrices.minOrNull() ?: pCurrent,
            highestPriceTry = allPrices.maxOrNull() ?: pCurrent,
            currency = "TRY",
            priceHistory = history,
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
        name.contains("B650", ignoreCase = true) || name.contains("X670", ignoreCase = true) ||
                name.contains("A620", ignoreCase = true) || name.contains("B850", ignoreCase = true) ||
                name.contains("X870", ignoreCase = true) -> "AM5"
        name.contains("B550", ignoreCase = true) || name.contains("X570", ignoreCase = true) ||
                name.contains("A520", ignoreCase = true) || name.contains("B450", ignoreCase = true) -> "AM4"
        name.contains("Z790", ignoreCase = true) || name.contains("B760", ignoreCase = true) ||
                name.contains("Z690", ignoreCase = true) || name.contains("B660", ignoreCase = true) -> "LGA1700"
        name.contains("Z890", ignoreCase = true) || name.contains("B860", ignoreCase = true) -> "LGA1851"
        else -> "AM5"
    }

    private fun deduceMotherboardChipset(name: String): String {
        val chipsets = listOf(
            "X870E", "X870", "B850", "B840", "X670E", "X670", "B650E", "B650", "A620",
            "X570", "B550", "A520", "B450",
            "Z890", "B860", "Z790", "B760", "H770", "Z690", "B660", "H610"
        )
        return chipsets.firstOrNull { name.contains(it, ignoreCase = true) } ?: "B650"
    }

    private fun estimateGpuTdp(chipset: String, name: String): Int {
        val text = "$chipset $name".uppercase()
        return when {
            text.contains("4090") -> 450
            text.contains("4080") -> 320
            text.contains("4070 TI") -> 285
            text.contains("4070") -> 200
            text.contains("4060 TI") -> 160
            text.contains("4060") -> 115
            text.contains("3090") -> 350
            text.contains("3080") -> 320
            text.contains("3070") -> 220
            text.contains("3060 TI") -> 200
            text.contains("3060") -> 170
            text.contains("7900 XTX") -> 355
            text.contains("7900 XT") -> 315
            text.contains("7800 XT") -> 263
            text.contains("7700 XT") -> 245
            text.contains("7600") -> 165
            else -> 200
        }
    }

    // endregion
}
