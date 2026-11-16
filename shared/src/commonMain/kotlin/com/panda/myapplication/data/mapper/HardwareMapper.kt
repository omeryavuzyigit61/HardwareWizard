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
import com.panda.myapplication.data.local.entity.GamingChairSpecEntity
import com.panda.myapplication.data.local.entity.CaseFanSpecEntity
import com.panda.myapplication.data.local.entity.UpsSpecEntity
import com.panda.myapplication.data.local.entity.GamingDeskSpecEntity
import com.panda.myapplication.data.local.entity.GamingLightingSpecEntity
import com.panda.myapplication.data.local.entity.SmartwatchSpecEntity
import com.panda.myapplication.data.local.entity.RouterSpecEntity
import com.panda.myapplication.data.local.entity.ModemSpecEntity
import com.panda.myapplication.data.local.entity.AccessPointSpecEntity
import com.panda.myapplication.data.local.entity.RangeExtenderSpecEntity
import com.panda.myapplication.data.local.entity.PhoneSpecEntity
import com.panda.myapplication.data.local.entity.TabletSpecEntity
import com.panda.myapplication.data.model.dto.SmartphoneCatalogItemDto
import com.panda.myapplication.data.model.dto.TabletCatalogItemDto
import com.panda.myapplication.domain.model.PhoneSpec
import com.panda.myapplication.domain.model.TabletSpec
import com.panda.myapplication.data.local.entity.MicrophoneSpecEntity
import com.panda.myapplication.data.local.entity.MonitorSpecEntity
import com.panda.myapplication.data.local.entity.MousepadSpecEntity
import com.panda.myapplication.data.local.entity.WebcamSpecEntity
import com.panda.myapplication.data.local.entity.MotherboardSpecEntity
import com.panda.myapplication.data.local.entity.MouseSpecEntity
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
import com.panda.myapplication.domain.model.GamingChairSpec
import com.panda.myapplication.domain.model.CaseFanSpec
import com.panda.myapplication.domain.model.UpsSpec
import com.panda.myapplication.domain.model.GamingDeskSpec
import com.panda.myapplication.domain.model.GamingLightingSpec
import com.panda.myapplication.domain.model.SmartwatchSpec
import com.panda.myapplication.domain.model.RouterSpec
import com.panda.myapplication.domain.model.ModemSpec
import com.panda.myapplication.domain.model.AccessPointSpec
import com.panda.myapplication.domain.model.RangeExtenderSpec
import com.panda.myapplication.domain.model.MicrophoneSpec
import com.panda.myapplication.domain.model.MonitorSpec
import com.panda.myapplication.domain.model.MousepadSpec
import com.panda.myapplication.domain.model.WebcamSpec
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.MotherboardSpec
import com.panda.myapplication.domain.model.MouseSpec
import com.panda.myapplication.domain.model.PcieGeneration
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
                    l1Cache = c.l1Cache,
                    dieSizeMm2 = c.dieSizeMm2,
                    transistorsBillion = c.transistorsBillion,
                    memoryChannels = c.memoryChannels,
                    eccSupport = c.eccSupport,
                    pcieLanesDetail = c.pcieLanesDetail,
                    tjMaxCelsius = c.tjMaxCelsius,
                    instructionSets = c.instructionSets,
                    npuTops = c.npuTops,
                    releaseDate = c.releaseDate,
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
                    tmus = g.tmus,
                    rops = g.rops,
                    l2CacheMb = g.l2CacheMb,
                    fp32Tflops = g.fp32Tflops,
                    pcieInterface = g.pcieInterface,
                    upscalingTech = g.upscalingTech,
                    mediaEngines = g.mediaEngines,
                    gpuDie = g.gpuDie,
                    releaseDate = g.releaseDate,
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
            itemWithSpec.microphone != null -> {
                val mc = itemWithSpec.microphone
                MicrophoneSpec(
                    capsuleType = mc.capsuleType,
                    polarPattern = mc.polarPattern,
                    sampleRateAndBit = mc.sampleRateAndBit,
                    frequencyResponse = mc.frequencyResponse,
                    connectivity = mc.connectivity,
                    maxSplDb = mc.maxSplDb,
                    hasBuiltInDsp = mc.hasBuiltInDsp,
                    requiresPhantomPower = mc.requiresPhantomPower,
                    hasZeroLatencyMonitoring = mc.hasZeroLatencyMonitoring,
                    hasPopFilter = mc.hasPopFilter,
                )
            }
            itemWithSpec.mousepad != null -> {
                val mp = itemWithSpec.mousepad
                MousepadSpec(
                    surfaceType = mp.surfaceType,
                    speedGlideTier = mp.speedGlideTier,
                    baseMaterial = mp.baseMaterial,
                    dimensionsMm = mp.dimensionsMm,
                    thicknessMm = mp.thicknessMm,
                    stitchedEdges = mp.stitchedEdges,
                    stoppingPowerScore = mp.stoppingPowerScore,
                    humidityResistance = mp.humidityResistance,
                )
            }
            itemWithSpec.webcam != null -> {
                val wc = itemWithSpec.webcam
                WebcamSpec(
                    maxResolution = wc.maxResolution,
                    maxFrameRateFps = wc.maxFrameRateFps,
                    sensorModel = wc.sensorModel,
                    sensorSizeInch = wc.sensorSizeInch,
                    aperture = wc.aperture,
                    fieldOfViewDeg = wc.fieldOfViewDeg,
                    focusType = wc.focusType,
                    hasAiTracking = wc.hasAiTracking,
                    hasHardwareHdr = wc.hasHardwareHdr,
                    connectionType = wc.connectionType,
                    privacyShutter = wc.privacyShutter,
                )
            }
            itemWithSpec.gamingChair != null -> {
                val gc = itemWithSpec.gamingChair
                GamingChairSpec(
                    chairType = gc.chairType,
                    upholsteryMaterial = gc.upholsteryMaterial,
                    lumbarSupportType = gc.lumbarSupportType,
                    armrestType = gc.armrestType,
                    gasLiftClass = gc.gasLiftClass,
                    maxWeightCapacityKg = gc.maxWeightCapacityKg,
                    recommendedHeightRange = gc.recommendedHeightRange,
                    reclineAngleDeg = gc.reclineAngleDeg,
                    tiltMechanism = gc.tiltMechanism,
                )
            }
            itemWithSpec.caseFan != null -> {
                val cf = itemWithSpec.caseFan
                CaseFanSpec(
                    fanSizeMm = cf.fanSizeMm,
                    minRpm = cf.minRpm,
                    maxRpm = cf.maxRpm,
                    maxAirflowCfm = cf.maxAirflowCfm,
                    staticPressureMmH2o = cf.staticPressureMmH2o,
                    noiseLevelDba = cf.noiseLevelDba,
                    bearingType = cf.bearingType,
                    pwmPins = cf.pwmPins,
                    isDaisyChain = cf.isDaisyChain,
                    argbType = cf.argbType,
                )
            }
            itemWithSpec.ups != null -> {
                val u = itemWithSpec.ups
                UpsSpec(
                    vaRating = u.vaRating,
                    wattCapacity = u.wattCapacity,
                    topology = u.topology,
                    sinewaveType = u.sinewaveType,
                    batterySpecs = u.batterySpecs,
                    runtimeMinFullLoad = u.runtimeMinFullLoad,
                    runtimeMinHalfLoad = u.runtimeMinHalfLoad,
                    outletCount = u.outletCount,
                    hasUsbInterface = u.hasUsbInterface,
                    surgeProtectionJoules = u.surgeProtectionJoules,
                )
            }
            itemWithSpec.gamingDesk != null -> {
                val gd = itemWithSpec.gamingDesk
                GamingDeskSpec(
                    deskWidthCm = gd.deskWidthCm,
                    deskDepthCm = gd.deskDepthCm,
                    isMotorizedHeight = gd.isMotorizedHeight,
                    minHeightCm = gd.minHeightCm,
                    maxHeightCm = gd.maxHeightCm,
                    maxLoadKg = gd.maxLoadKg,
                    surfaceFinish = gd.surfaceFinish,
                    hasCableManagement = gd.hasCableManagement,
                    integratedMousepad = gd.integratedMousepad,
                )
            }
            itemWithSpec.gamingLighting != null -> {
                val gl = itemWithSpec.gamingLighting
                GamingLightingSpec(
                    formFactor = gl.formFactor,
                    maxLumens = gl.maxLumens,
                    colorTempKelvin = gl.colorTempKelvin,
                    criScore = gl.criScore,
                    hasScreenMirroring = gl.hasScreenMirroring,
                    supportedEcosystems = gl.supportedEcosystems,
                    connectionType = gl.connectionType,
                    powerSource = gl.powerSource,
                )
            }
            itemWithSpec.smartwatch != null -> {
                val sw = itemWithSpec.smartwatch
                SmartwatchSpec(
                    screenSizeInch = sw.screenSizeInch,
                    screenPanel = sw.screenPanel,
                    peakBrightnessNits = sw.peakBrightnessNits,
                    batteryLifeDays = sw.batteryLifeDays,
                    waterResistanceAtm = sw.waterResistanceAtm,
                    osName = sw.osName,
                    sensorsList = sw.sensorsList,
                    hasCellularLte = sw.hasCellularLte,
                    caseMaterial = sw.caseMaterial,
                )
            }
            itemWithSpec.router != null -> {
                val r = itemWithSpec.router
                RouterSpec(
                    wifiStandard = r.wifiStandard,
                    speedClass = r.speedClass,
                    bands = r.bands,
                    ethernetPorts = r.ethernetPorts,
                    hasGamingQos = r.hasGamingQos,
                    hasSqmBufferbloat = r.hasSqmBufferbloat,
                    processorAndRam = r.processorAndRam,
                    usbSharingPorts = r.usbSharingPorts,
                )
            }
            itemWithSpec.modem != null -> {
                val m = itemWithSpec.modem
                ModemSpec(
                    vdslProfile = m.vdslProfile,
                    fiberWanSupport = m.fiberWanSupport,
                    wifiStandard = m.wifiStandard,
                    lanPorts = m.lanPorts,
                    voipPorts = m.voipPorts,
                    chipset = m.chipset,
                )
            }
            itemWithSpec.accessPoint != null -> {
                val ap = itemWithSpec.accessPoint
                AccessPointSpec(
                    formFactor = ap.formFactor,
                    wifiStandard = ap.wifiStandard,
                    poeSupport = ap.poeSupport,
                    maxClients = ap.maxClients,
                    muMimoStreams = ap.muMimoStreams,
                    uplinkPort = ap.uplinkPort,
                )
            }
            itemWithSpec.rangeExtender != null -> {
                val re = itemWithSpec.rangeExtender
                RangeExtenderSpec(
                    extenderType = re.extenderType,
                    wifiStandard = re.wifiStandard,
                    dedicatedBackhaul = re.dedicatedBackhaul,
                    meshProtocol = re.meshProtocol,
                    ethernetPorts = re.ethernetPorts,
                    coverageAreaM2 = re.coverageAreaM2,
                )
            }
            itemWithSpec.phone != null -> {
                val p = itemWithSpec.phone
                PhoneSpec(
                    chipset = p.chipset,
                    cpuArch = p.cpuArch,
                    gpuModel = p.gpuModel,
                    processNodeNm = p.processNodeNm,
                    ramGb = p.ramGb,
                    ramType = p.ramType,
                    storageGb = p.storageGb,
                    storageType = p.storageType,
                    hasMicroSd = p.hasMicroSd,
                    antutuScore = p.antutuScore,
                    geekbenchSingle = p.geekbenchSingle,
                    geekbenchMulti = p.geekbenchMulti,
                    stabilityPercent = p.stabilityPercent,
                    dxomarkCamera = p.dxomarkCamera,
                    dxomarkSelfie = p.dxomarkSelfie,
                    dxomarkDisplay = p.dxomarkDisplay,
                    dxomarkBattery = p.dxomarkBattery,
                    dxomarkAudio = p.dxomarkAudio,
                    displaySizeInch = p.displaySizeInch,
                    displayTech = p.displayTech,
                    resolution = p.resolution,
                    ppi = p.ppi,
                    refreshRateHz = p.refreshRateHz,
                    peakBrightnessNit = p.peakBrightnessNit,
                    glassProtection = p.glassProtection,
                    hdrSupport = p.hdrSupport,
                    pwmDimmingHz = p.pwmDimmingHz,
                    mainCameraMp = p.mainCameraMp,
                    mainSensorSize = p.mainSensorSize,
                    mainAperture = p.mainAperture,
                    hasOis = p.hasOis,
                    oisType = p.oisType,
                    telephotoMp = p.telephotoMp,
                    opticalZoomX = p.opticalZoomX,
                    ultraWideMp = p.ultraWideMp,
                    videoResolution = p.videoResolution,
                    selfieCameraMp = p.selfieCameraMp,
                    batteryMah = p.batteryMah,
                    wiredChargingWatt = p.wiredChargingWatt,
                    wirelessChargingWatt = p.wirelessChargingWatt,
                    hasReverseCharging = p.hasReverseCharging,
                    chargingTimeToFullMin = p.chargingTimeToFullMin,
                    weightGrams = p.weightGrams,
                    thicknessMm = p.thicknessMm,
                    frameMaterial = p.frameMaterial,
                    waterResistance = p.waterResistance,
                    hasStylus = p.hasStylus,
                    usbSpeed = p.usbSpeed,
                    wifiBluetooth = p.wifiBluetooth,
                    osName = p.osName,
                    updateYears = p.updateYears,
                    pubgFpsSupport = p.pubgFpsSupport,
                    genshinStabilityScore = p.genshinStabilityScore,
                    variants = p.variants,
                )
            }
            itemWithSpec.tablet != null -> {
                val t = itemWithSpec.tablet
                TabletSpec(
                    chipset = t.chipset,
                    cpuArch = t.cpuArch,
                    gpuModel = t.gpuModel,
                    processNodeNm = t.processNodeNm,
                    ramGb = t.ramGb,
                    storageGb = t.storageGb,
                    storageType = t.storageType,
                    hasMicroSd = t.hasMicroSd,
                    antutuScore = t.antutuScore,
                    geekbenchSingle = t.geekbenchSingle,
                    geekbenchMulti = t.geekbenchMulti,
                    stabilityPercent = t.stabilityPercent,
                    displaySizeInch = t.displaySizeInch,
                    displayTech = t.displayTech,
                    resolution = t.resolution,
                    ppi = t.ppi,
                    refreshRateHz = t.refreshRateHz,
                    peakBrightnessNit = t.peakBrightnessNit,
                    stylusSupport = t.stylusSupport,
                    aspectRatio = t.aspectRatio,
                    mainCameraMp = t.mainCameraMp,
                    frontCameraMp = t.frontCameraMp,
                    speakerCount = t.speakerCount,
                    hasDolbyAtmos = t.hasDolbyAtmos,
                    batteryMah = t.batteryMah,
                    wiredChargingWatt = t.wiredChargingWatt,
                    wirelessChargingWatt = t.wirelessChargingWatt,
                    weightGrams = t.weightGrams,
                    thicknessMm = t.thicknessMm,
                    bodyMaterial = t.bodyMaterial,
                    osName = t.osName,
                    updateYears = t.updateYears,
                    cellularOption = t.cellularOption,
                    usbSpeed = t.usbSpeed,
                    externalDisplaySupport = t.externalDisplaySupport,
                    variants = t.variants,
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
            extraSpecs = entity.extraSpecs.orEmpty(),
            sourceProvenance = entity.sourceProvenance.orEmpty(),
            specVerifiedAt = entity.specVerifiedAt,
        )

        val isCpuOrGpu = entity.category == HardwareCategory.CPU || entity.category == HardwareCategory.GPU
        val benchmarkInfo = if (isCpuOrGpu) {
            if (entity.benchmarkScore != null) {
                val relPct = (entity.benchmarkGamingIndex ?: entity.benchmarkScore).toDouble()
                val enrichedGames = entity.gameFps ?: com.panda.myapplication.domain.engine.BenchmarkEngine.buildFull12GameMatrix(
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
                null
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
        val l3CacheMb = dto.l3CacheMb
        val l2CacheMb = dto.l2CacheMb
        val processNode = dto.processNode?.ifBlank { null }
        val pcieVersion = dto.pcieVersion?.ifBlank { null }
        val maxMemorySpeed = dto.maxMemorySpeed?.ifBlank { null }
        val unlockedMultiplier = dto.unlockedMultiplier

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.CPU,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl ?: dto.images.firstOrNull(),
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
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
            l1Cache = dto.l1Cache,
            dieSizeMm2 = dto.dieSizeMm2,
            transistorsBillion = dto.transistorsBillion,
            memoryChannels = dto.memoryChannels,
            eccSupport = dto.eccSupport,
            pcieLanesDetail = dto.pcieLanesDetail,
            tjMaxCelsius = dto.tjMaxCelsius,
            instructionSets = dto.instructionSets,
            npuTops = dto.npuTops,
            releaseDate = dto.releaseDate,
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

        // Marketing model name: prioritize chipset if model is SKU, or combine without repeating core GPU family
        val chipCore = rawChipset?.replace(Regex("^(?:GeForce|Radeon|Intel Arc|Intel)\\s+", RegexOption.IGNORE_CASE), "")?.trim().orEmpty()
        val finalModel = when {
            isSku && rawChipset != null -> {
                if (rawChipset.startsWith(brand, ignoreCase = true)) {
                    rawChipset.substring(brand.length).trim().ifBlank { rawChipset }
                } else {
                    rawChipset
                }
            }
            rawChipset != null &&
                !extractedModel.contains(rawChipset, ignoreCase = true) &&
                !rawChipset.contains(extractedModel, ignoreCase = true) &&
                (chipCore.isBlank() || !extractedModel.contains(chipCore, ignoreCase = true)) -> {
                "$rawChipset $extractedModel"
            }
            else -> extractedModel
        }

        val vramMb = dto.memory?.let { it * 1024 } ?: 0
        val memoryType = dto.memoryType ?: ""
        val coreClockMhz = dto.coreClockMhz ?: 0
        val boostClockMhz = dto.boostClockMhz
        val tdpWatts = dto.tdpWatts ?: 0
        val recommendedPsuWatts = dto.recommendedPsuWatts ?: 0
        val lengthMm = dto.length?.toInt()
        val powerConnectors = dto.powerConnectors
        val slotWidth = dto.slotWidth

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = finalModel,
            category = HardwareCategory.GPU,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl ?: dto.images.firstOrNull(),
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
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
            tmus = dto.tmus,
            rops = dto.rops,
            l2CacheMb = dto.l2CacheMb,
            fp32Tflops = dto.fp32Tflops,
            pcieInterface = dto.pcieInterface,
            upscalingTech = dto.upscalingTech,
            mediaEngines = dto.mediaEngines,
            gpuDie = dto.gpuDie,
            releaseDate = dto.releaseDate,
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
            imageUrl = dto.imageUrl ?: dto.images.firstOrNull(),
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
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
            imageUrl = dto.imageUrl ?: dto.images.firstOrNull(),
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
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

        val modularity = dto.modularity?.ifBlank { null }
        val tierRating = dto.tierRating?.ifBlank { null }
        val atxStandard = dto.atxStandard?.ifBlank { null }
        val pcie12Vhpwr = dto.pcie12Vhpwr?.ifBlank { null }
        val epsConnectors = dto.epsConnectors
        val pcieConnectors = dto.pcieConnectors
        val sataConnectors = dto.sataConnectors
        val topology = dto.topology?.ifBlank { null }
        val protections = dto.protections
        val fanAndSize = dto.fanAndSize?.ifBlank { null }
        val cybeneticsCert = dto.cybeneticsCert?.ifBlank { null }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = model,
            category = HardwareCategory.PSU,
            priceMinor = priceMinor,
            currencyCode = if (priceMinor != null) "USD" else null,
            cachedAt = cachedAt,
            imageUrl = dto.imageUrl ?: dto.images.firstOrNull(),
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
        )
        val spec = PsuSpecEntity(
            itemId = id,
            wattage = wattage,
            efficiency = efficiency,
            isAtx3Compliant = isAtx3,
            supportsPcie5Power = isAtx3,
            modularity = modularity,
            formFactor = dto.type?.ifBlank { null },
            outputs = dto.outputs ?: listOfNotNull(pcie12Vhpwr, epsConnectors, pcieConnectors).takeIf { it.isNotEmpty() }?.joinToString(" • "),
            tierRating = tierRating,
            capacitors = dto.capacitors?.ifBlank { null },
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
            imageUrl = dto.imageUrl ?: dto.images.firstOrNull(),
            images = dto.images.takeIf { it.isNotEmpty() } ?: listOfNotNull(dto.imageUrl),
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
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
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
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
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
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

    fun laptopDtoToEntity(dto: LaptopRawDto, cachedAt: Instant): Pair<HardwareItemEntity, LaptopSpecEntity> {
        val id = requireNotNull(dto.id?.takeIf { it.isNotBlank() }) { "Laptop id must not be blank" }
        val name = dto.name ?: "Gaming Laptop"
        val brand = dto.brand ?: "Laptop"
        val lapModel = if (name.length > brand.length + 2 && name.startsWith("$brand ", ignoreCase = true)) {
            name.substring(brand.length).trim().ifBlank { name }
        } else {
            name
        }
        val priceMinor = dto.price?.let { (it * 100).toLong() }

        val item = HardwareItemEntity(
            id = id,
            brand = brand,
            model = lapModel,
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
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
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

    fun smartphoneDtoToEntity(dto: SmartphoneCatalogItemDto, syncedAt: Instant): Pair<HardwareItemEntity, PhoneSpecEntity>? {
        val s = dto.spec ?: return null
        val effImg = dto.imageUrl.ifBlank { dto.images.firstOrNull().orEmpty() }
        val effImgs = dto.images.ifEmpty { listOfNotNull(effImg.takeIf { it.isNotBlank() }) }
        val item = HardwareItemEntity(
            id = dto.id,
            brand = dto.brand.ifBlank { "Akıllı Telefon" },
            model = dto.model.ifBlank { dto.name },
            category = HardwareCategory.SMARTPHONE,
            priceMinor = (dto.priceTry * 100).toLong(),
            currencyCode = "TRY",
            cachedAt = syncedAt,
            currentPriceTry = dto.priceTry,
            lowestPriceTry = dto.priceTry,
            highestPriceTry = dto.priceTry,
            offers = dto.stores.map { StoreOffer(storeName = it.storeName, priceTry = it.priceTry, url = it.url) },
            priceHistory = dto.priceHistory.map { PricePoint(date = it.date, priceTry = it.priceTry) },
            benchmarkScore = dto.benchmarkScore ?: s.antutuScore,
            benchmarkTier = dto.tier,
            benchmarkGamingIndex = dto.gamingIndex ?: 80,
            imageUrl = effImg.takeIf { it.isNotBlank() },
            images = effImgs,
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
        )
        val spec = PhoneSpecEntity(
            itemId = dto.id,
            chipset = s.chipset,
            cpuArch = s.cpuArch,
            gpuModel = s.gpuModel,
            processNodeNm = s.processNodeNm,
            ramGb = s.ramGb,
            ramType = s.ramType,
            storageGb = s.storageGb,
            storageType = s.storageType,
            hasMicroSd = s.hasMicroSd,
            antutuScore = s.antutuScore,
            geekbenchSingle = s.geekbenchSingle,
            geekbenchMulti = s.geekbenchMulti,
            stabilityPercent = s.stabilityPercent,
            dxomarkCamera = s.dxomarkCamera,
            dxomarkSelfie = s.dxomarkSelfie,
            dxomarkDisplay = s.dxomarkDisplay,
            dxomarkBattery = s.dxomarkBattery,
            dxomarkAudio = s.dxomarkAudio,
            displaySizeInch = s.displaySizeInch,
            displayTech = s.displayTech,
            resolution = s.resolution,
            ppi = s.ppi,
            refreshRateHz = s.refreshRateHz,
            peakBrightnessNit = s.peakBrightnessNit,
            glassProtection = s.glassProtection,
            hdrSupport = s.hdrSupport,
            pwmDimmingHz = s.pwmDimmingHz,
            mainCameraMp = s.mainCameraMp,
            mainSensorSize = s.mainSensorSize,
            mainAperture = s.mainAperture,
            hasOis = s.hasOis,
            oisType = s.oisType,
            telephotoMp = s.telephotoMp,
            opticalZoomX = s.opticalZoomX,
            ultraWideMp = s.ultraWideMp,
            videoResolution = s.videoResolution,
            selfieCameraMp = s.selfieCameraMp,
            batteryMah = s.batteryMah,
            wiredChargingWatt = s.wiredChargingWatt,
            wirelessChargingWatt = s.wirelessChargingWatt,
            hasReverseCharging = s.hasReverseCharging,
            chargingTimeToFullMin = s.chargingTimeToFullMin,
            weightGrams = s.weightGrams,
            thicknessMm = s.thicknessMm,
            frameMaterial = s.frameMaterial,
            waterResistance = s.waterResistance,
            hasStylus = s.hasStylus,
            usbSpeed = s.usbSpeed,
            wifiBluetooth = s.wifiBluetooth,
            osName = s.osName,
            updateYears = s.updateYears,
            pubgFpsSupport = s.pubgFpsSupport,
            genshinStabilityScore = s.genshinStabilityScore,
            variants = s.variants,
        )
        return item to spec
    }

    fun tabletDtoToEntity(dto: TabletCatalogItemDto, syncedAt: Instant): Pair<HardwareItemEntity, TabletSpecEntity>? {
        val s = dto.spec ?: return null
        val effImg = dto.imageUrl.ifBlank { dto.images.firstOrNull().orEmpty() }
        val effImgs = dto.images.ifEmpty { listOfNotNull(effImg.takeIf { it.isNotBlank() }) }
        val item = HardwareItemEntity(
            id = dto.id,
            brand = dto.brand.ifBlank { "Tablet" },
            model = dto.model.ifBlank { dto.name },
            category = HardwareCategory.TABLET,
            priceMinor = (dto.priceTry * 100).toLong(),
            currencyCode = "TRY",
            cachedAt = syncedAt,
            currentPriceTry = dto.priceTry,
            lowestPriceTry = dto.priceTry,
            highestPriceTry = dto.priceTry,
            offers = dto.stores.map { StoreOffer(storeName = it.storeName, priceTry = it.priceTry, url = it.url) },
            priceHistory = dto.priceHistory.map { PricePoint(date = it.date, priceTry = it.priceTry) },
            benchmarkScore = dto.benchmarkScore ?: s.antutuScore,
            benchmarkTier = dto.tier,
            benchmarkGamingIndex = dto.gamingIndex ?: 80,
            imageUrl = effImg.takeIf { it.isNotBlank() },
            images = effImgs,
            extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
            sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
            specVerifiedAt = dto.specVerifiedAt,
        )
        val spec = TabletSpecEntity(
            itemId = dto.id,
            chipset = s.chipset,
            cpuArch = s.cpuArch,
            gpuModel = s.gpuModel,
            processNodeNm = s.processNodeNm,
            ramGb = s.ramGb,
            storageGb = s.storageGb,
            storageType = s.storageType,
            hasMicroSd = s.hasMicroSd,
            antutuScore = s.antutuScore,
            geekbenchSingle = s.geekbenchSingle,
            geekbenchMulti = s.geekbenchMulti,
            stabilityPercent = s.stabilityPercent,
            displaySizeInch = s.displaySizeInch,
            displayTech = s.displayTech,
            resolution = s.resolution,
            ppi = s.ppi,
            refreshRateHz = s.refreshRateHz,
            peakBrightnessNit = s.peakBrightnessNit,
            stylusSupport = s.stylusSupport,
            aspectRatio = s.aspectRatio,
            mainCameraMp = s.mainCameraMp,
            frontCameraMp = s.frontCameraMp,
            speakerCount = s.speakerCount,
            hasDolbyAtmos = s.hasDolbyAtmos,
            batteryMah = s.batteryMah,
            wiredChargingWatt = s.wiredChargingWatt,
            wirelessChargingWatt = s.wirelessChargingWatt,
            weightGrams = s.weightGrams,
            thicknessMm = s.thicknessMm,
            bodyMaterial = s.bodyMaterial,
            osName = s.osName,
            updateYears = s.updateYears,
            cellularOption = s.cellularOption,
            usbSpeed = s.usbSpeed,
            externalDisplaySupport = s.externalDisplaySupport,
            variants = s.variants,
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
        laptops: List<LaptopRawDto> = emptyList(),
        peripherals: List<PeripheralRawDto> = emptyList(),
        smartphones: List<SmartphoneCatalogItemDto> = emptyList(),
        tablets: List<TabletCatalogItemDto> = emptyList(),
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
        val laptopPairs = laptops.mapNotNull { runCatching { laptopDtoToEntity(it, syncedAt) }.getOrNull() }
        val phonePairs = smartphones.mapNotNull { runCatching { smartphoneDtoToEntity(it, syncedAt) }.getOrNull() }
        val tabletPairs = tablets.mapNotNull { runCatching { tabletDtoToEntity(it, syncedAt) }.getOrNull() }

        val mousePairs = mutableListOf<Pair<HardwareItemEntity, MouseSpecEntity>>()
        val keyboardPairs = mutableListOf<Pair<HardwareItemEntity, KeyboardSpecEntity>>()
        val headsetPairs = mutableListOf<Pair<HardwareItemEntity, HeadsetSpecEntity>>()
        val monitorPairs = mutableListOf<Pair<HardwareItemEntity, MonitorSpecEntity>>()
        val microphonePairs = mutableListOf<Pair<HardwareItemEntity, MicrophoneSpecEntity>>()
        val mousepadPairs = mutableListOf<Pair<HardwareItemEntity, MousepadSpecEntity>>()
        val webcamPairs = mutableListOf<Pair<HardwareItemEntity, WebcamSpecEntity>>()
        val gamingChairPairs = mutableListOf<Pair<HardwareItemEntity, GamingChairSpecEntity>>()
        val caseFanPairs = mutableListOf<Pair<HardwareItemEntity, CaseFanSpecEntity>>()
        val upsPairs = mutableListOf<Pair<HardwareItemEntity, UpsSpecEntity>>()
        val gamingDeskPairs = mutableListOf<Pair<HardwareItemEntity, GamingDeskSpecEntity>>()
        val gamingLightingPairs = mutableListOf<Pair<HardwareItemEntity, GamingLightingSpecEntity>>()
        val smartwatchPairs = mutableListOf<Pair<HardwareItemEntity, SmartwatchSpecEntity>>()
        val routerPairs = mutableListOf<Pair<HardwareItemEntity, RouterSpecEntity>>()
        val modemPairs = mutableListOf<Pair<HardwareItemEntity, ModemSpecEntity>>()
        val accessPointPairs = mutableListOf<Pair<HardwareItemEntity, AccessPointSpecEntity>>()
        val rangeExtenderPairs = mutableListOf<Pair<HardwareItemEntity, RangeExtenderSpecEntity>>()

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
                    extraSpecs = dto.extraSpecs.takeIf { it.isNotEmpty() },
                    sourceProvenance = dto.sourceProvenance.takeIf { it.isNotEmpty() },
                    specVerifiedAt = dto.specVerifiedAt,
                )
                when (category) {
                    HardwareCategory.MOUSE -> {
                        mousePairs += itemEntity to MouseSpecEntity(
                            hardwareId = id,
                            sensor = dto.sensor ?: "",
                            maxDpi = dto.maxDpi ?: 0,
                            pollingRateHz = dto.pollingRateHz ?: 0,
                            switchType = dto.switchType ?: "",
                            weightG = dto.weightG ?: 0,
                            connectivity = dto.connectivity ?: "",
                            batteryHours = dto.batteryHours,
                            mcu = dto.mcu ?: "",
                            clickLatencyMs = dto.clickLatencyMs ?: 0.0,
                            shapeType = dto.shapeType ?: "",
                        )
                    }
                    HardwareCategory.KEYBOARD -> {
                        keyboardPairs += itemEntity to KeyboardSpecEntity(
                            hardwareId = id,
                            switchType = dto.switchType ?: "",
                            rapidTriggerSupported = dto.rapidTriggerSupported ?: false,
                            minActuationMm = dto.minActuationMm ?: 0.0,
                            pollingRateHz = dto.pollingRateHz ?: 0,
                            layout = dto.layout ?: "",
                            hotSwap = dto.hotSwap ?: false,
                            connectivity = dto.connectivity ?: "",
                            maxActuationMm = dto.maxActuationMm ?: 0.0,
                            caseMaterial = dto.caseMaterial ?: "",
                            isHallEffect = dto.isHallEffect ?: (dto.switchType?.contains("Hall Effect", ignoreCase = true) == true),
                        )
                    }
                    HardwareCategory.HEADSET -> {
                        headsetPairs += itemEntity to HeadsetSpecEntity(
                            hardwareId = id,
                            driverType = dto.driverType ?: "",
                            driverSizeMm = dto.driverSizeMm ?: 0,
                            impedanceOhm = dto.impedanceOhm ?: 0,
                            frequencyResponse = dto.frequencyResponse ?: "",
                            dacAmpRequired = dto.dacAmpRequired ?: false,
                            micType = dto.micType ?: "",
                            weightG = dto.weightG ?: 0,
                            connectivity = dto.connectivity ?: "",
                            soundIsolation = dto.soundIsolation ?: "",
                        )
                    }
                    HardwareCategory.MONITOR -> {
                        monitorPairs += itemEntity to MonitorSpecEntity(
                            hardwareId = id,
                            panelType = dto.panelType ?: "",
                            screenSizeInch = dto.screenSizeInch ?: 0.0,
                            resolution = dto.resolution ?: "",
                            refreshRateHz = dto.refreshRateHz ?: 0,
                            responseSpeedGtGMs = dto.responseSpeedGtGMs ?: 0.0,
                            ppi = dto.ppi ?: 0,
                            hdrCertification = dto.hdrCertification ?: "",
                            bitDepth = dto.bitDepth ?: "",
                            responseMprtMs = dto.responseMprtMs ?: 0.0,
                            brightnessSdrNit = dto.brightnessSdrNit ?: 0,
                            brightnessHdrNit = dto.brightnessHdrNit ?: 0,
                            colorGamut = dto.colorGamut ?: "",
                            vrrSupport = dto.vrrSupport ?: "",
                        )
                    }
                    HardwareCategory.MICROPHONE -> {
                        microphonePairs += itemEntity to MicrophoneSpecEntity(
                            hardwareId = id,
                            capsuleType = dto.capsuleType ?: "",
                            polarPattern = dto.polarPattern ?: "",
                            sampleRateAndBit = dto.sampleRateAndBit ?: "",
                            frequencyResponse = dto.frequencyResponse ?: "",
                            connectivity = dto.connectivity ?: "",
                            maxSplDb = dto.maxSplDb ?: 0,
                            hasBuiltInDsp = dto.hasBuiltInDsp ?: false,
                            requiresPhantomPower = dto.requiresPhantomPower ?: false,
                            hasZeroLatencyMonitoring = dto.hasZeroLatencyMonitoring ?: false,
                            hasPopFilter = dto.hasPopFilter ?: false,
                        )
                    }
                    HardwareCategory.MOUSEPAD -> {
                        mousepadPairs += itemEntity to MousepadSpecEntity(
                            hardwareId = id,
                            surfaceType = dto.surfaceType ?: "",
                            speedGlideTier = dto.speedGlideTier ?: "",
                            baseMaterial = dto.baseMaterial ?: "",
                            dimensionsMm = dto.dimensionsMm ?: "",
                            thicknessMm = dto.thicknessMm ?: 0.0,
                            stitchedEdges = dto.stitchedEdges ?: false,
                            stoppingPowerScore = dto.stoppingPowerScore ?: 0,
                            humidityResistance = dto.humidityResistance ?: "",
                        )
                    }
                    HardwareCategory.WEBCAM -> {
                        webcamPairs += itemEntity to WebcamSpecEntity(
                            hardwareId = id,
                            maxResolution = dto.maxResolution ?: "",
                            maxFrameRateFps = dto.maxFrameRateFps ?: 0,
                            sensorModel = dto.sensorModel ?: "",
                            sensorSizeInch = dto.sensorSizeInch ?: "",
                            aperture = dto.aperture ?: "",
                            fieldOfViewDeg = dto.fieldOfViewDeg ?: 0,
                            focusType = dto.focusType ?: "",
                            hasAiTracking = dto.hasAiTracking ?: false,
                            hasHardwareHdr = dto.hasHardwareHdr ?: false,
                            connectionType = dto.connectionType ?: "",
                            privacyShutter = dto.privacyShutter ?: false,
                        )
                    }
                    HardwareCategory.GAMING_CHAIR -> {
                        gamingChairPairs += itemEntity to GamingChairSpecEntity(
                            hardwareId = id,
                            chairType = dto.chairType ?: "",
                            upholsteryMaterial = dto.upholsteryMaterial ?: "",
                            lumbarSupportType = dto.lumbarSupportType ?: "",
                            armrestType = dto.armrestType ?: "",
                            gasLiftClass = dto.gasLiftClass ?: "",
                            maxWeightCapacityKg = dto.maxWeightCapacityKg ?: 0,
                            recommendedHeightRange = dto.recommendedHeightRange ?: "",
                            reclineAngleDeg = dto.reclineAngleDeg ?: 0,
                            tiltMechanism = dto.tiltMechanism ?: "",
                        )
                    }
                    HardwareCategory.CASE_FAN -> {
                        caseFanPairs += itemEntity to CaseFanSpecEntity(
                            hardwareId = id,
                            fanSizeMm = dto.fanSizeMm ?: 0,
                            minRpm = dto.minRpm ?: 0,
                            maxRpm = dto.maxRpm ?: 0,
                            maxAirflowCfm = dto.maxAirflowCfm ?: 0.0,
                            staticPressureMmH2o = dto.staticPressureMmH2o ?: 0.0,
                            noiseLevelDba = dto.noiseLevelDba ?: 0.0,
                            bearingType = dto.bearingType ?: "",
                            pwmPins = dto.pwmPins ?: 0,
                            isDaisyChain = dto.isDaisyChain ?: false,
                            argbType = dto.argbType ?: "",
                        )
                    }
                    HardwareCategory.UPS -> {
                        upsPairs += itemEntity to UpsSpecEntity(
                            hardwareId = id,
                            vaRating = dto.vaRating ?: 0,
                            wattCapacity = dto.wattCapacity ?: 0,
                            topology = dto.topology ?: "",
                            sinewaveType = dto.sinewaveType ?: "",
                            batterySpecs = dto.batterySpecs ?: "",
                            runtimeMinFullLoad = dto.runtimeMinFullLoad ?: 0,
                            runtimeMinHalfLoad = dto.runtimeMinHalfLoad ?: 0,
                            outletCount = dto.outletCount ?: 0,
                            hasUsbInterface = dto.hasUsbInterface ?: false,
                            surgeProtectionJoules = dto.surgeProtectionJoules ?: 0,
                        )
                    }
                    HardwareCategory.GAMING_DESK -> {
                        gamingDeskPairs += itemEntity to GamingDeskSpecEntity(
                            hardwareId = id,
                            deskWidthCm = dto.deskWidthCm ?: 0,
                            deskDepthCm = dto.deskDepthCm ?: 0,
                            isMotorizedHeight = dto.isMotorizedHeight ?: false,
                            minHeightCm = dto.minHeightCm ?: 0,
                            maxHeightCm = dto.maxHeightCm ?: 0,
                            maxLoadKg = dto.maxLoadKg ?: 0,
                            surfaceFinish = dto.surfaceFinish ?: "",
                            hasCableManagement = dto.hasCableManagement ?: false,
                            integratedMousepad = dto.integratedMousepad ?: false,
                        )
                    }
                    HardwareCategory.GAMING_LIGHTING -> {
                        gamingLightingPairs += itemEntity to GamingLightingSpecEntity(
                            hardwareId = id,
                            formFactor = dto.lightingFormFactor ?: "",
                            maxLumens = dto.maxLumens ?: 0,
                            colorTempKelvin = dto.colorTempKelvin ?: "",
                            criScore = dto.criScore ?: 0,
                            hasScreenMirroring = dto.hasScreenMirroring ?: false,
                            supportedEcosystems = dto.supportedEcosystems ?: "",
                            connectionType = dto.connectionType ?: "",
                            powerSource = dto.powerSource ?: "",
                        )
                    }
                    HardwareCategory.SMARTWATCH -> {
                        smartwatchPairs += itemEntity to SmartwatchSpecEntity(
                            hardwareId = id,
                            screenSizeInch = dto.screenSizeInch ?: 0.0,
                            screenPanel = dto.screenPanel ?: "",
                            peakBrightnessNits = dto.peakBrightnessNits ?: 0,
                            batteryLifeDays = dto.batteryLifeDays ?: 0,
                            waterResistanceAtm = dto.waterResistanceAtm ?: 0,
                            osName = dto.osName ?: "",
                            sensorsList = dto.sensorsList ?: "",
                            hasCellularLte = dto.hasCellularLte ?: false,
                            caseMaterial = dto.caseMaterial ?: "",
                        )
                    }
                    HardwareCategory.ROUTER -> {
                        routerPairs += itemEntity to RouterSpecEntity(
                            hardwareId = id,
                            wifiStandard = dto.wifiStandard ?: "",
                            speedClass = dto.speedClass ?: "",
                            bands = dto.bands ?: "",
                            ethernetPorts = dto.ethernetPorts ?: "",
                            hasGamingQos = dto.hasGamingQos ?: false,
                            hasSqmBufferbloat = dto.hasSqmBufferbloat ?: false,
                            processorAndRam = dto.processorAndRam ?: "",
                            usbSharingPorts = dto.usbSharingPorts ?: "",
                        )
                    }
                    HardwareCategory.MODEM -> {
                        modemPairs += itemEntity to ModemSpecEntity(
                            hardwareId = id,
                            vdslProfile = dto.vdslProfile ?: "",
                            fiberWanSupport = dto.fiberWanSupport ?: false,
                            wifiStandard = dto.wifiStandard ?: "",
                            lanPorts = dto.lanPorts ?: "",
                            voipPorts = dto.voipPorts ?: "",
                            chipset = dto.chipset ?: "",
                        )
                    }
                    HardwareCategory.ACCESS_POINT -> {
                        accessPointPairs += itemEntity to AccessPointSpecEntity(
                            hardwareId = id,
                            formFactor = dto.formFactor ?: "",
                            wifiStandard = dto.wifiStandard ?: "",
                            poeSupport = dto.poeSupport ?: "",
                            maxClients = dto.maxClients ?: 0,
                            muMimoStreams = dto.muMimoStreams ?: "",
                            uplinkPort = dto.uplinkPort ?: "",
                        )
                    }
                    HardwareCategory.RANGE_EXTENDER -> {
                        rangeExtenderPairs += itemEntity to RangeExtenderSpecEntity(
                            hardwareId = id,
                            extenderType = dto.extenderType ?: "",
                            wifiStandard = dto.wifiStandard ?: "",
                            dedicatedBackhaul = dto.dedicatedBackhaul ?: "",
                            meshProtocol = dto.meshProtocol ?: "",
                            ethernetPorts = dto.ethernetPorts ?: "",
                            coverageAreaM2 = dto.coverageAreaM2 ?: 0,
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
                laptopPairs.map { it.first } +
                mousePairs.map { it.first } +
                keyboardPairs.map { it.first } +
                headsetPairs.map { it.first } +
                monitorPairs.map { it.first } +
                microphonePairs.map { it.first } +
                mousepadPairs.map { it.first } +
                webcamPairs.map { it.first } +
                gamingChairPairs.map { it.first } +
                caseFanPairs.map { it.first } +
                upsPairs.map { it.first } +
                gamingDeskPairs.map { it.first } +
                gamingLightingPairs.map { it.first } +
                smartwatchPairs.map { it.first } +
                routerPairs.map { it.first } +
                modemPairs.map { it.first } +
                accessPointPairs.map { it.first } +
                rangeExtenderPairs.map { it.first } +
                phonePairs.map { it.first } +
                tabletPairs.map { it.first })
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
            laptops = laptopPairs.map { it.second }.distinctBy { it.itemId },
            mice = mousePairs.map { it.second }.distinctBy { it.hardwareId },
            keyboards = keyboardPairs.map { it.second }.distinctBy { it.hardwareId },
            headsets = headsetPairs.map { it.second }.distinctBy { it.hardwareId },
            monitors = monitorPairs.map { it.second }.distinctBy { it.hardwareId },
            microphones = microphonePairs.map { it.second }.distinctBy { it.hardwareId },
            mousepads = mousepadPairs.map { it.second }.distinctBy { it.hardwareId },
            webcams = webcamPairs.map { it.second }.distinctBy { it.hardwareId },
            gamingChairs = gamingChairPairs.map { it.second }.distinctBy { it.hardwareId },
            caseFans = caseFanPairs.map { it.second }.distinctBy { it.hardwareId },
            upss = upsPairs.map { it.second }.distinctBy { it.hardwareId },
            gamingDesks = gamingDeskPairs.map { it.second }.distinctBy { it.hardwareId },
            gamingLightings = gamingLightingPairs.map { it.second }.distinctBy { it.hardwareId },
            smartwatches = smartwatchPairs.map { it.second }.distinctBy { it.hardwareId },
            routers = routerPairs.map { it.second }.distinctBy { it.hardwareId },
            modems = modemPairs.map { it.second }.distinctBy { it.hardwareId },
            accessPoints = accessPointPairs.map { it.second }.distinctBy { it.hardwareId },
            rangeExtenders = rangeExtenderPairs.map { it.second }.distinctBy { it.hardwareId },
            phones = phonePairs.map { it.second }.distinctBy { it.itemId },
            tablets = tabletPairs.map { it.second }.distinctBy { it.itemId },
        )
    }

    fun sanitizeImageUrl(url: String?): String? =
        com.panda.myapplication.domain.util.SecurityUtils.sanitizeImageUrl(url)

    fun resolveDefaultHardwareImage(category: HardwareCategory, model: String, brand: String): String? {
        if (category != HardwareCategory.CPU) return null
        val upper = "${brand.uppercase()} ${model.uppercase()}"
        return when {
            upper.contains("9800X3D") ->
                "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/amd_ryzen_7_9800x3d.webp"
            upper.contains("7800X3D") ->
                "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/amd_ryzen_7_7800x3d.webp"
            upper.contains("5800X3D") ->
                "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/amd_ryzen_7_5800x3d.webp"
            upper.contains("285K") ->
                "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/intel_core_ultra_9_285k.webp"
            upper.contains("14700K") && !upper.contains("KF") ->
                "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/intel_core_i7_14700k.webp"
            upper.contains("14400") && !upper.contains("14400T") ->
                "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/images/cpu/intel_core_i5_14400.webp"
            else -> null
        }
    }

    // region DTO -> Domain Helpers

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

    // region Anti-Antique Hardware Modernity Guards

    fun isModernCpu(name: String, socket: String): Boolean {
        val u = name.uppercase()
        val s = socket.uppercase()

        // 1. Explicit antique & industrial embedded blacklists
        if (u.contains("A4-") || u.contains("A6-") || u.contains("A8-") || u.contains("A10-") || u.contains("A12-") ||
            u.contains("ATHLON II") || u.contains("PHENOM") || u.contains("SEMPRON") || u.contains("OPTERON") ||
            u.contains("FX-") || u.contains("PENTIUM") || u.contains("CELERON") || u.contains("CORE 2") ||
            u.contains("XEON") || u.contains("CORE DUO") || u.contains("ATOM") || u.contains("EMBEDDED") ||
            s.contains("AM1") || s.contains("AM2") || s.contains("AM3") || s.contains("FM1") || s.contains("FM2") ||
            s.contains("775") || s.contains("1156") || s.contains("1155") || s.contains("1150") || s.contains("1151")) {
            return false
        }

        // 2. Modern AMD Whitelist: Ryzen (1000-9000, Threadripper) or Athlon 3000G/200GE on AM4/AM5/TR4/sTRX4/sWRX8/sTR5
        val isModernAmd = (u.contains("RYZEN") || u.contains("THREADRIPPER") || u.contains("3000G") || u.contains("200GE")) &&
                (s.contains("AM4") || s.contains("AM5") || s.contains("TR4") || s.contains("TRX4") || s.contains("STR5") || s.contains("WRX8") || s == "BELIRTILMEDI")

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
                        u.contains("9060") || u.contains("9070") || u.contains("9080") || u.contains("9000"))
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

}
