package com.panda.myapplication.data.mapper

import com.panda.myapplication.data.local.entity.AccessPointSpecEntity
import com.panda.myapplication.data.local.entity.CaseFanSpecEntity
import com.panda.myapplication.data.local.entity.CaseSpecEntity
import com.panda.myapplication.data.local.entity.CoolerSpecEntity
import com.panda.myapplication.data.local.entity.CpuSpecEntity
import com.panda.myapplication.data.local.entity.GamingChairSpecEntity
import com.panda.myapplication.data.local.entity.GamingDeskSpecEntity
import com.panda.myapplication.data.local.entity.GamingLightingSpecEntity
import com.panda.myapplication.data.local.entity.GpuSpecEntity
import com.panda.myapplication.data.local.entity.HardwareCatalogSnapshot
import com.panda.myapplication.data.local.entity.HardwareItemEntity
import com.panda.myapplication.data.local.entity.HeadsetSpecEntity
import com.panda.myapplication.data.local.entity.KeyboardSpecEntity
import com.panda.myapplication.data.local.entity.LaptopSpecEntity
import com.panda.myapplication.data.local.entity.MicrophoneSpecEntity
import com.panda.myapplication.data.local.entity.ModemSpecEntity
import com.panda.myapplication.data.local.entity.MonitorSpecEntity
import com.panda.myapplication.data.local.entity.MotherboardSpecEntity
import com.panda.myapplication.data.local.entity.MouseSpecEntity
import com.panda.myapplication.data.local.entity.MousepadSpecEntity
import com.panda.myapplication.data.local.entity.PhoneSpecEntity
import com.panda.myapplication.data.local.entity.PsuSpecEntity
import com.panda.myapplication.data.local.entity.RamSpecEntity
import com.panda.myapplication.data.local.entity.RangeExtenderSpecEntity
import com.panda.myapplication.data.local.entity.RouterSpecEntity
import com.panda.myapplication.data.local.entity.SmartwatchSpecEntity
import com.panda.myapplication.data.local.entity.StorageSpecEntity
import com.panda.myapplication.data.local.entity.TabletSpecEntity
import com.panda.myapplication.data.local.entity.UpsSpecEntity
import com.panda.myapplication.data.local.entity.WebcamSpecEntity
import com.panda.myapplication.data.model.dto.SupabaseHardwareCatalogRowDto
import com.panda.myapplication.domain.model.CoolerType
import com.panda.myapplication.domain.model.DdrGeneration
import com.panda.myapplication.domain.model.DeviceVariant
import com.panda.myapplication.domain.model.HardwareCategory
import com.panda.myapplication.domain.model.MotherboardFormFactor
import com.panda.myapplication.domain.model.PcieGeneration
import com.panda.myapplication.domain.model.PsuEfficiencyRating
import com.panda.myapplication.domain.model.StorageFormFactor
import com.panda.myapplication.domain.model.StorageInterface
import com.panda.myapplication.domain.model.StorageMediaType
import com.panda.myapplication.domain.util.SecurityUtils
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Lossless mapper converting Supabase `hardware_catalog` delta rows across all 28 categories
 * into a Room [HardwareCatalogSnapshot] for incremental `upsertDeltaCatalog(...)`.
 */
object SupabaseCatalogMapper {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    fun toCatalogSnapshot(
        rows: List<SupabaseHardwareCatalogRowDto>,
        syncedAt: Instant,
    ): HardwareCatalogSnapshot {
        val items = mutableListOf<HardwareItemEntity>()
        val cpus = mutableListOf<CpuSpecEntity>()
        val gpus = mutableListOf<GpuSpecEntity>()
        val rams = mutableListOf<RamSpecEntity>()
        val motherboards = mutableListOf<MotherboardSpecEntity>()
        val psus = mutableListOf<PsuSpecEntity>()
        val storages = mutableListOf<StorageSpecEntity>()
        val coolers = mutableListOf<CoolerSpecEntity>()
        val cases = mutableListOf<CaseSpecEntity>()
        val laptops = mutableListOf<LaptopSpecEntity>()
        val mice = mutableListOf<MouseSpecEntity>()
        val keyboards = mutableListOf<KeyboardSpecEntity>()
        val headsets = mutableListOf<HeadsetSpecEntity>()
        val monitors = mutableListOf<MonitorSpecEntity>()
        val microphones = mutableListOf<MicrophoneSpecEntity>()
        val mousepads = mutableListOf<MousepadSpecEntity>()
        val webcams = mutableListOf<WebcamSpecEntity>()
        val gamingChairs = mutableListOf<GamingChairSpecEntity>()
        val caseFans = mutableListOf<CaseFanSpecEntity>()
        val upss = mutableListOf<UpsSpecEntity>()
        val gamingDesks = mutableListOf<GamingDeskSpecEntity>()
        val gamingLightings = mutableListOf<GamingLightingSpecEntity>()
        val smartwatches = mutableListOf<SmartwatchSpecEntity>()
        val routers = mutableListOf<RouterSpecEntity>()
        val modems = mutableListOf<ModemSpecEntity>()
        val accessPoints = mutableListOf<AccessPointSpecEntity>()
        val rangeExtenders = mutableListOf<RangeExtenderSpecEntity>()
        val phones = mutableListOf<PhoneSpecEntity>()
        val tablets = mutableListOf<TabletSpecEntity>()

        for (row in rows) {
            runCatching {
                val category = HardwareCategory.entries.firstOrNull { it.name.equals(row.category, ignoreCase = true) }
                    ?: return@runCatching
                val id = row.id.trim().takeIf { it.isNotEmpty() } ?: return@runCatching
                val brand = row.brand.trim().takeIf { it.isNotEmpty() } ?: return@runCatching
                val model = row.model.trim().takeIf { it.isNotEmpty() } ?: return@runCatching
                val p = row.specPayload

                val mappedSpecAdded = when (category) {
                    HardwareCategory.CPU -> cpus.add(
                        CpuSpecEntity(
                            itemId = id,
                            socket = p.str("socket", "AM5"),
                            coreCount = p.int("core_count", 6),
                            threadCount = p.int("thread_count", 12),
                            baseClockMhz = p.int("base_clock_mhz", 3500),
                            boostClockMhz = p.intOrNull("boost_clock_mhz"),
                            tdpWatts = p.int("tdp_watts", 65),
                            hasIntegratedGraphics = p.bool("has_integrated_graphics", false),
                            architecture = p.strOrNull("architecture"),
                            l2CacheMb = p.dblOrNull("l2_cache_mb"),
                            l3CacheMb = p.intOrNull("l3_cache_mb"),
                            processNode = p.strOrNull("process_node"),
                            pcieVersion = p.strOrNull("pcie_version"),
                            maxMemorySpeed = p.strOrNull("max_memory_speed"),
                            unlockedMultiplier = p.boolOrNull("unlocked_multiplier"),
                            coreTopology = p.strOrNull("core_topology"),
                            powerLimits = p.strOrNull("power_limits"),
                            igpuModel = p.strOrNull("igpu_model"),
                            l1Cache = p.strOrNull("l1_cache"),
                            dieSizeMm2 = p.dblOrNull("die_size_mm2"),
                            transistorsBillion = p.dblOrNull("transistors_billion"),
                            memoryChannels = p.intOrNull("memory_channels"),
                            eccSupport = p.boolOrNull("ecc_support"),
                            pcieLanesDetail = p.strOrNull("pcie_lanes_detail"),
                            tjMaxCelsius = p.intOrNull("tj_max_celsius"),
                            instructionSets = p.strOrNull("instruction_sets"),
                            npuTops = p.strOrNull("npu_tops"),
                            releaseDate = p.strOrNull("release_date"),
                        )
                    )
                    HardwareCategory.GPU -> gpus.add(
                        GpuSpecEntity(
                            itemId = id,
                            chipset = p.str("chipset", model),
                            vramMb = p.int("vram_mb", 8192),
                            memoryType = p.str("memory_type", "GDDR6"),
                            coreClockMhz = p.int("core_clock_mhz", 1800),
                            boostClockMhz = p.intOrNull("boost_clock_mhz"),
                            tdpWatts = p.int("tdp_watts", 180),
                            recommendedPsuWatts = p.int("recommended_psu_watts", 650),
                            busWidthBits = p.intOrNull("bus_width_bits"),
                            lengthMm = p.intOrNull("length_mm"),
                            powerConnectors = p.strOrNull("power_connectors"),
                            displayOutputs = p.strOrNull("display_outputs"),
                            slotWidth = p.strOrNull("slot_width"),
                            partNumber = p.strOrNull("part_number"),
                            architecture = p.strOrNull("architecture"),
                            processNode = p.strOrNull("process_node"),
                            cudaCores = p.intOrNull("cuda_cores"),
                            tensorAiTops = p.strOrNull("tensor_ai_tops"),
                            rtCores = p.intOrNull("rt_cores"),
                            memorySpeedGbps = p.dblOrNull("memory_speed_gbps"),
                            bandwidthGbs = p.dblOrNull("bandwidth_gbs"),
                            tmus = p.intOrNull("tmus"),
                            rops = p.intOrNull("rops"),
                            l2CacheMb = p.dblOrNull("l2_cache_mb"),
                            fp32Tflops = p.dblOrNull("fp32_tflops"),
                            pcieInterface = p.strOrNull("pcie_interface"),
                            upscalingTech = p.strOrNull("upscaling_tech"),
                            mediaEngines = p.strOrNull("media_engines"),
                            gpuDie = p.strOrNull("gpu_die"),
                            releaseDate = p.strOrNull("release_date"),
                        )
                    )
                    HardwareCategory.RAM -> rams.add(
                        RamSpecEntity(
                            itemId = id,
                            generation = p.enum("generation", DdrGeneration.DDR5),
                            speedMts = p.int("speed_mts", 6000),
                            casLatency = p.int("cas_latency", 36),
                            capacityMb = p.int("capacity_mb", 32768),
                            moduleCount = p.int("module_count", 2),
                            firstWordLatencyNs = p.dblOrNull("first_word_latency_ns"),
                            voltage = p.dblOrNull("voltage"),
                            heatSpreader = p.boolOrNull("heat_spreader"),
                            rgb = p.boolOrNull("rgb"),
                            dieType = p.strOrNull("die_type"),
                            timings = p.strOrNull("timings"),
                            profile = p.strOrNull("profile"),
                        )
                    )
                    HardwareCategory.MOTHERBOARD -> motherboards.add(
                        MotherboardSpecEntity(
                            itemId = id,
                            socket = p.str("socket", "AM5"),
                            chipset = p.str("chipset", "B650"),
                            formFactor = p.enum("form_factor", MotherboardFormFactor.ATX),
                            memoryGeneration = p.enum("memory_generation", DdrGeneration.DDR5),
                            ramSlots = p.int("ram_slots", 4),
                            pcieGeneration = p.enum("pcie_generation", PcieGeneration.GEN4),
                            maxMemoryGb = p.intOrNull("max_memory_gb"),
                            m2Slots = p.intOrNull("m2_slots"),
                            pcieSlotCount = p.intOrNull("pcie_slot_count"),
                            sataPorts = p.intOrNull("sata_ports"),
                            hasWifi = p.boolOrNull("has_wifi"),
                            vrmPhases = p.strOrNull("vrm_phases"),
                            interfacePorts = p.strOrNull("interface_ports"),
                            pcbLayers = p.strOrNull("pcb_layers"),
                            m2SlotsDetail = p.strOrNull("m2_slots_detail"),
                            memorySpeedOc = p.strOrNull("memory_speed_oc"),
                            network = p.strOrNull("network"),
                            audioCodec = p.strOrNull("audio_codec"),
                        )
                    )
                    HardwareCategory.PSU -> psus.add(
                        PsuSpecEntity(
                            itemId = id,
                            wattage = p.int("wattage", 750),
                            efficiency = p.enum("efficiency", PsuEfficiencyRating.GOLD),
                            isAtx3Compliant = p.bool("is_atx3_compliant", false),
                            supportsPcie5Power = p.bool("supports_pcie5_power", false),
                            modularity = p.strOrNull("modularity"),
                            formFactor = p.strOrNull("form_factor"),
                            outputs = p.strOrNull("outputs"),
                            tierRating = p.strOrNull("tier_rating"),
                            capacitors = p.strOrNull("capacitors"),
                            atxStandard = p.strOrNull("atx_standard"),
                            pcie12Vhpwr = p.strOrNull("pcie_12vhpwr"),
                            epsConnectors = p.strOrNull("eps_connectors"),
                            pcieConnectors = p.strOrNull("pcie_connectors"),
                            sataConnectors = p.strOrNull("sata_connectors"),
                            topology = p.strOrNull("topology"),
                            protections = p.strOrNull("protections"),
                            fanAndSize = p.strOrNull("fan_and_size"),
                            cybeneticsCert = p.strOrNull("cybenetics_cert"),
                            cybeneticsLambda = p.strOrNull("cybenetics_lambda"),
                        )
                    )
                    HardwareCategory.STORAGE -> storages.add(
                        StorageSpecEntity(
                            itemId = id,
                            mediaType = p.enum("media_type", StorageMediaType.SSD),
                            storageInterface = p.enum("storage_interface", StorageInterface.NVME),
                            formFactor = p.enum("form_factor", StorageFormFactor.M2_2280),
                            pcieGeneration = p.enumOrNull("pcie_generation"),
                            capacityMb = p.int("capacity_mb", 1000000),
                            sequentialReadMbps = p.int("seq_read_mbps", 3500),
                            sequentialWriteMbps = p.int("seq_write_mbps", 3000),
                            nandType = p.strOrNull("nand_type"),
                            enduranceTbw = p.intOrNull("endurance_tbw"),
                            cacheMb = p.intOrNull("cache_mb"),
                            controller = p.strOrNull("controller"),
                            dramCache = p.strOrNull("dram_cache"),
                            randomIops = p.strOrNull("random_iops"),
                        )
                    )
                    HardwareCategory.COOLER -> coolers.add(
                        CoolerSpecEntity(
                            itemId = id,
                            type = p.enum("type", CoolerType.AIR),
                            radiatorSizeMm = p.intOrNull("radiator_size_mm"),
                            maxCoolingTdpWatts = p.int("max_cooling_tdp_watts", 220),
                            supportedSockets = p.strList("supported_sockets"),
                            heightMm = p.intOrNull("height_mm"),
                            fanRpm = p.strOrNull("fan_rpm"),
                            noiseDba = p.dblOrNull("noise_dba"),
                            radiatorDimensions = p.strOrNull("radiator_dimensions"),
                            fanAirflow = p.strOrNull("fan_airflow"),
                        )
                    )
                    HardwareCategory.CASE -> cases.add(
                        CaseSpecEntity(
                            itemId = id,
                            type = p.strOrNull("type"),
                            maxGpuLengthMm = p.intOrNull("max_gpu_length_mm"),
                            maxCpuCoolerHeightMm = p.intOrNull("max_cpu_cooler_height_mm"),
                            maxRadiatorSizeMm = p.intOrNull("max_radiator_size_mm"),
                            sidePanel = p.strOrNull("side_panel"),
                            driveBays = p.strOrNull("drive_bays"),
                            radiatorSupport = p.strOrNull("radiator_support"),
                            includedFans = p.strOrNull("included_fans"),
                            frontPanelUsb = p.strOrNull("front_panel_usb"),
                        )
                    )
                    HardwareCategory.LAPTOP -> laptops.add(
                        LaptopSpecEntity(
                            itemId = id,
                            cpuModel = p.str("cpu_model"),
                            gpuModel = p.str("gpu_model"),
                            gpuWattTgp = p.str("gpu_watt_tgp"),
                            muxSwitch = p.str("mux_switch"),
                            ramSpec = p.str("ram_spec"),
                            storageSpec = p.str("storage_spec"),
                            displaySpec = p.str("display_spec"),
                            batteryWh = p.int("battery_wh"),
                            weightKg = p.dbl("weight_kg"),
                            coolingTech = p.str("cooling_tech"),
                            gamingScore = p.int("gaming_score"),
                            fps1080p = p.int("fps_1080p"),
                            fps1440p = p.int("fps_1440p"),
                            cpuCores = p.str("cpu_cores"),
                            cpuClockGhz = p.str("cpu_clock_ghz"),
                            screenResolution = p.str("screen_resolution"),
                            refreshRateHz = p.int("refresh_rate_hz"),
                            panelTech = p.str("panel_tech"),
                            colorGamut = p.str("color_gamut"),
                            screenSizeInch = p.dbl("screen_size_inch"),
                            ramSlots = p.str("ram_slots"),
                            storageSlots = p.str("storage_slots"),
                            chargerWatt = p.int("charger_watt"),
                        )
                    )
                    HardwareCategory.MOUSE -> mice.add(
                        MouseSpecEntity(
                            hardwareId = id,
                            sensor = p.str("sensor"),
                            maxDpi = p.int("max_dpi"),
                            pollingRateHz = p.int("polling_rate_hz"),
                            switchType = p.str("switch_type"),
                            weightG = p.int("weight_g"),
                            connectivity = p.str("connectivity"),
                            batteryHours = p.intOrNull("battery_hours"),
                            mcu = p.str("mcu"),
                            clickLatencyMs = p.dbl("click_latency_ms"),
                            shapeType = p.str("shape_type"),
                        )
                    )
                    HardwareCategory.KEYBOARD -> keyboards.add(
                        KeyboardSpecEntity(
                            hardwareId = id,
                            switchType = p.str("switch_type"),
                            rapidTriggerSupported = p.bool("rapid_trigger_supported"),
                            minActuationMm = p.dbl("min_actuation_mm"),
                            pollingRateHz = p.int("polling_rate_hz"),
                            layout = p.str("layout"),
                            hotSwap = p.bool("hot_swap"),
                            connectivity = p.str("connectivity"),
                            maxActuationMm = p.dbl("max_actuation_mm"),
                            caseMaterial = p.str("case_material"),
                            isHallEffect = p.bool("is_hall_effect"),
                        )
                    )
                    HardwareCategory.HEADSET -> headsets.add(
                        HeadsetSpecEntity(
                            hardwareId = id,
                            driverType = p.str("driver_type"),
                            driverSizeMm = p.int("driver_size_mm"),
                            impedanceOhm = p.int("impedance_ohm"),
                            frequencyResponse = p.str("frequency_response"),
                            dacAmpRequired = p.bool("dac_amp_required"),
                            micType = p.str("mic_type"),
                            weightG = p.int("weight_g"),
                            connectivity = p.str("connectivity"),
                            soundIsolation = p.str("sound_isolation"),
                        )
                    )
                    HardwareCategory.MONITOR -> monitors.add(
                        MonitorSpecEntity(
                            hardwareId = id,
                            panelType = p.str("panel_type"),
                            screenSizeInch = p.dbl("screen_size_inch"),
                            resolution = p.str("resolution"),
                            refreshRateHz = p.int("refresh_rate_hz"),
                            responseSpeedGtGMs = p.dbl("response_speed_gtg_ms"),
                            ppi = p.int("ppi"),
                            hdrCertification = p.str("hdr_certification"),
                            bitDepth = p.str("bit_depth"),
                            responseMprtMs = p.dblOrNull("response_mprt_ms"),
                            brightnessSdrNit = p.int("brightness_sdr_nit"),
                            brightnessHdrNit = p.int("brightness_hdr_nit"),
                            colorGamut = p.str("color_gamut"),
                            vrrSupport = p.str("vrr_support"),
                        )
                    )
                    HardwareCategory.MICROPHONE -> microphones.add(
                        MicrophoneSpecEntity(
                            hardwareId = id,
                            capsuleType = p.str("capsule_type"),
                            polarPattern = p.str("polar_pattern"),
                            sampleRateAndBit = p.str("sample_rate_and_bit"),
                            frequencyResponse = p.str("frequency_response"),
                            connectivity = p.str("connectivity"),
                            maxSplDb = p.int("max_spl_db"),
                            hasBuiltInDsp = p.bool("has_built_in_dsp"),
                            requiresPhantomPower = p.bool("requires_phantom_power"),
                            hasZeroLatencyMonitoring = p.bool("has_zero_latency_monitoring"),
                            hasPopFilter = p.bool("has_pop_filter"),
                        )
                    )
                    HardwareCategory.MOUSEPAD -> mousepads.add(
                        MousepadSpecEntity(
                            hardwareId = id,
                            surfaceType = p.str("surface_type"),
                            speedGlideTier = p.str("speed_glide_tier"),
                            baseMaterial = p.str("base_material"),
                            dimensionsMm = p.str("dimensions_mm"),
                            thicknessMm = p.dbl("thickness_mm"),
                            stitchedEdges = p.bool("stitched_edges"),
                            stoppingPowerScore = p.int("stopping_power_score"),
                            humidityResistance = p.str("humidity_resistance"),
                        )
                    )
                    HardwareCategory.WEBCAM -> webcams.add(
                        WebcamSpecEntity(
                            hardwareId = id,
                            maxResolution = p.str("max_resolution"),
                            maxFrameRateFps = p.int("max_frame_rate_fps"),
                            sensorModel = p.str("sensor_model"),
                            sensorSizeInch = p.str("sensor_size_inch"),
                            aperture = p.str("aperture"),
                            fieldOfViewDeg = p.int("field_of_view_deg"),
                            focusType = p.str("focus_type"),
                            hasAiTracking = p.bool("has_ai_tracking"),
                            hasHardwareHdr = p.bool("has_hardware_hdr"),
                            connectionType = p.str("connection_type"),
                            privacyShutter = p.bool("privacy_shutter"),
                        )
                    )
                    HardwareCategory.GAMING_CHAIR -> gamingChairs.add(
                        GamingChairSpecEntity(
                            hardwareId = id,
                            chairType = p.str("chair_type"),
                            upholsteryMaterial = p.str("upholstery_material"),
                            lumbarSupportType = p.str("lumbar_support_type"),
                            armrestType = p.str("armrest_type"),
                            gasLiftClass = p.str("gas_lift_class"),
                            maxWeightCapacityKg = p.int("max_weight_capacity_kg"),
                            recommendedHeightRange = p.str("recommended_height_range"),
                            reclineAngleDeg = p.int("recline_angle_deg"),
                            tiltMechanism = p.str("tilt_mechanism"),
                        )
                    )
                    HardwareCategory.CASE_FAN -> caseFans.add(
                        CaseFanSpecEntity(
                            hardwareId = id,
                            fanSizeMm = p.int("fan_size_mm"),
                            minRpm = p.int("min_rpm"),
                            maxRpm = p.int("max_rpm"),
                            maxAirflowCfm = p.dbl("max_airflow_cfm"),
                            staticPressureMmH2o = p.dbl("static_pressure_mmh2o"),
                            noiseLevelDba = p.dbl("noise_level_dba"),
                            bearingType = p.str("bearing_type"),
                            pwmPins = p.int("pwm_pins"),
                            isDaisyChain = p.bool("is_daisy_chain"),
                            argbType = p.str("argb_type"),
                        )
                    )
                    HardwareCategory.UPS -> upss.add(
                        UpsSpecEntity(
                            hardwareId = id,
                            vaRating = p.int("va_rating"),
                            wattCapacity = p.int("watt_capacity"),
                            topology = p.str("topology"),
                            sinewaveType = p.str("sinewave_type"),
                            batterySpecs = p.str("battery_specs"),
                            runtimeMinFullLoad = p.int("runtime_min_full_load"),
                            runtimeMinHalfLoad = p.int("runtime_min_half_load"),
                            outletCount = p.int("outlet_count"),
                            hasUsbInterface = p.bool("has_usb_interface"),
                            surgeProtectionJoules = p.int("surge_protection_joules"),
                        )
                    )
                    HardwareCategory.GAMING_DESK -> gamingDesks.add(
                        GamingDeskSpecEntity(
                            hardwareId = id,
                            deskWidthCm = p.int("desk_width_cm"),
                            deskDepthCm = p.int("desk_depth_cm"),
                            isMotorizedHeight = p.bool("is_motorized_height"),
                            minHeightCm = p.int("min_height_cm"),
                            maxHeightCm = p.int("max_height_cm"),
                            maxLoadKg = p.int("max_load_kg"),
                            surfaceFinish = p.str("surface_finish"),
                            hasCableManagement = p.bool("has_cable_management"),
                            integratedMousepad = p.bool("integrated_mousepad"),
                        )
                    )
                    HardwareCategory.GAMING_LIGHTING -> gamingLightings.add(
                        GamingLightingSpecEntity(
                            hardwareId = id,
                            formFactor = p.str("lighting_form_factor"),
                            maxLumens = p.int("max_lumens"),
                            colorTempKelvin = p.str("color_temp_kelvin"),
                            criScore = p.int("cri_score"),
                            hasScreenMirroring = p.bool("has_screen_mirroring"),
                            supportedEcosystems = p.str("supported_ecosystems"),
                            connectionType = p.str("connection_type"),
                            powerSource = p.str("power_source"),
                        )
                    )
                    HardwareCategory.SMARTWATCH -> smartwatches.add(
                        SmartwatchSpecEntity(
                            hardwareId = id,
                            screenSizeInch = p.dbl("screen_size_inch"),
                            screenPanel = p.str("screen_panel"),
                            peakBrightnessNits = p.int("peak_brightness_nits"),
                            batteryLifeDays = p.int("battery_life_days"),
                            waterResistanceAtm = p.int("water_resistance_atm"),
                            osName = p.str("os_name"),
                            sensorsList = p.str("sensors_list"),
                            hasCellularLte = p.bool("has_cellular_lte"),
                            caseMaterial = p.str("case_material"),
                        )
                    )
                    HardwareCategory.ROUTER -> routers.add(
                        RouterSpecEntity(
                            hardwareId = id,
                            wifiStandard = p.str("wifi_standard"),
                            speedClass = p.str("speed_class"),
                            bands = p.str("bands"),
                            ethernetPorts = p.str("ethernet_ports"),
                            hasGamingQos = p.bool("has_gaming_qos"),
                            hasSqmBufferbloat = p.bool("has_sqm_bufferbloat"),
                            processorAndRam = p.str("processor_and_ram"),
                            usbSharingPorts = p.str("usb_sharing_ports"),
                        )
                    )
                    HardwareCategory.MODEM -> modems.add(
                        ModemSpecEntity(
                            hardwareId = id,
                            vdslProfile = p.str("vdsl_profile"),
                            fiberWanSupport = p.bool("fiber_wan_support"),
                            wifiStandard = p.str("wifi_standard"),
                            lanPorts = p.str("lan_ports"),
                            voipPorts = p.str("voip_ports"),
                            chipset = p.str("chipset"),
                        )
                    )
                    HardwareCategory.ACCESS_POINT -> accessPoints.add(
                        AccessPointSpecEntity(
                            hardwareId = id,
                            formFactor = p.str("form_factor"),
                            wifiStandard = p.str("wifi_standard"),
                            poeSupport = p.str("poe_support"),
                            maxClients = p.int("max_clients"),
                            muMimoStreams = p.str("mu_mimo_streams"),
                            uplinkPort = p.str("uplink_port"),
                        )
                    )
                    HardwareCategory.RANGE_EXTENDER -> rangeExtenders.add(
                        RangeExtenderSpecEntity(
                            hardwareId = id,
                            extenderType = p.str("extender_type"),
                            wifiStandard = p.str("wifi_standard"),
                            dedicatedBackhaul = p.str("dedicated_backhaul"),
                            meshProtocol = p.str("mesh_protocol"),
                            ethernetPorts = p.str("ethernet_ports"),
                            coverageAreaM2 = p.int("coverage_area_m2"),
                        )
                    )
                    HardwareCategory.SMARTPHONE -> phones.add(
                        PhoneSpecEntity(
                            itemId = id,
                            chipset = p.str("chipset"),
                            cpuArch = p.str("cpu_arch"),
                            gpuModel = p.str("gpu_model"),
                            processNodeNm = p.int("process_node_nm"),
                            ramGb = p.int("ram_gb"),
                            ramType = p.str("ram_type"),
                            storageGb = p.int("storage_gb"),
                            storageType = p.str("storage_type"),
                            hasMicroSd = p.bool("has_micro_sd"),
                            antutuScore = p.intOrNull("antutu_score"),
                            geekbenchSingle = p.intOrNull("geekbench_single"),
                            geekbenchMulti = p.intOrNull("geekbench_multi"),
                            stabilityPercent = p.intOrNull("stability_percent"),
                            dxomarkCamera = p.intOrNull("dxomark_camera"),
                            dxomarkSelfie = p.intOrNull("dxomark_selfie"),
                            dxomarkDisplay = p.intOrNull("dxomark_display"),
                            dxomarkBattery = p.intOrNull("dxomark_battery"),
                            dxomarkAudio = p.intOrNull("dxomark_audio"),
                            displaySizeInch = p.dbl("display_size_inch"),
                            displayTech = p.str("display_tech"),
                            resolution = p.str("resolution"),
                            ppi = p.int("ppi"),
                            refreshRateHz = p.int("refresh_rate_hz"),
                            peakBrightnessNit = p.int("peak_brightness_nit"),
                            glassProtection = p.str("glass_protection"),
                            hdrSupport = p.str("hdr_support"),
                            pwmDimmingHz = p.intOrNull("pwm_dimming_hz"),
                            mainCameraMp = p.int("main_camera_mp"),
                            mainSensorSize = p.str("main_sensor_size"),
                            mainAperture = p.str("main_aperture"),
                            hasOis = p.bool("has_ois"),
                            oisType = p.str("ois_type"),
                            telephotoMp = p.intOrNull("telephoto_mp"),
                            opticalZoomX = p.dblOrNull("optical_zoom_x"),
                            ultraWideMp = p.intOrNull("ultra_wide_mp"),
                            videoResolution = p.str("video_resolution"),
                            selfieCameraMp = p.int("selfie_camera_mp"),
                            batteryMah = p.int("battery_mah"),
                            wiredChargingWatt = p.int("wired_charging_watt"),
                            wirelessChargingWatt = p.intOrNull("wireless_charging_watt"),
                            hasReverseCharging = p.bool("has_reverse_charging"),
                            chargingTimeToFullMin = p.intOrNull("charging_time_to_full_min"),
                            weightGrams = p.int("weight_grams"),
                            thicknessMm = p.dbl("thickness_mm"),
                            frameMaterial = p.str("frame_material"),
                            waterResistance = p.str("water_resistance"),
                            hasStylus = p.bool("has_stylus"),
                            usbSpeed = p.strOrNull("usb_speed"),
                            wifiBluetooth = p.strOrNull("wifi_bluetooth"),
                            osName = p.str("os_name"),
                            updateYears = p.int("update_years"),
                            pubgFpsSupport = p.str("pubg_fps_support"),
                            genshinStabilityScore = p.str("genshin_stability_score"),
                            variants = p.variants("variants"),
                        )
                    )
                    HardwareCategory.TABLET -> tablets.add(
                        TabletSpecEntity(
                            itemId = id,
                            chipset = p.str("chipset"),
                            cpuArch = p.str("cpu_arch"),
                            gpuModel = p.str("gpu_model"),
                            processNodeNm = p.int("process_node_nm"),
                            ramGb = p.int("ram_gb"),
                            storageGb = p.int("storage_gb"),
                            storageType = p.str("storage_type"),
                            hasMicroSd = p.bool("has_micro_sd"),
                            antutuScore = p.intOrNull("antutu_score"),
                            geekbenchSingle = p.intOrNull("geekbench_single"),
                            geekbenchMulti = p.intOrNull("geekbench_multi"),
                            stabilityPercent = p.intOrNull("stability_percent"),
                            displaySizeInch = p.dbl("display_size_inch"),
                            displayTech = p.str("display_tech"),
                            resolution = p.str("resolution"),
                            ppi = p.int("ppi"),
                            refreshRateHz = p.int("refresh_rate_hz"),
                            peakBrightnessNit = p.int("peak_brightness_nit"),
                            stylusSupport = p.str("stylus_support"),
                            aspectRatio = p.strOrNull("aspect_ratio"),
                            mainCameraMp = p.int("main_camera_mp"),
                            frontCameraMp = p.int("front_camera_mp"),
                            speakerCount = p.int("speaker_count"),
                            hasDolbyAtmos = p.bool("has_dolby_atmos"),
                            batteryMah = p.int("battery_mah"),
                            wiredChargingWatt = p.int("wired_charging_watt"),
                            wirelessChargingWatt = p.intOrNull("wireless_charging_watt"),
                            weightGrams = p.int("weight_grams"),
                            thicknessMm = p.dbl("thickness_mm"),
                            bodyMaterial = p.str("body_material"),
                            osName = p.str("os_name"),
                            updateYears = p.int("update_years"),
                            cellularOption = p.str("cellular_option"),
                            usbSpeed = p.strOrNull("usb_speed"),
                            externalDisplaySupport = p.strOrNull("external_display_support"),
                            variants = p.variants("variants"),
                        )
                    )
                }

                if (mappedSpecAdded) {
                    val sanitizedGallery = (listOfNotNull(row.imageUrl) + row.images)
                        .mapNotNull { SecurityUtils.sanitizeImageUrl(it) }
                        .distinct()
                    items.add(
                        HardwareItemEntity(
                            id = id,
                            brand = brand,
                            model = model,
                            category = category,
                            priceMinor = null,
                            currencyCode = "TRY",
                            cachedAt = syncedAt,
                            benchmarkScore = row.benchmarkScore,
                            benchmarkTier = row.benchmarkTier,
                            benchmarkTestSuite = row.benchmarkTestSuite,
                            benchmarkGamingIndex = row.benchmarkGamingIndex,
                            cpuSynthetic = row.cpuSynthetic,
                            gpuSynthetic = row.gpuSynthetic,
                            gameFps = row.gameFps,
                            imageUrl = sanitizedGallery.firstOrNull(),
                            images = sanitizedGallery.takeIf { it.isNotEmpty() },
                            extraSpecs = row.extraSpecs.takeIf { it.isNotEmpty() },
                            sourceProvenance = row.sourceProvenance.takeIf { it.isNotEmpty() } ?: listOf("HardwareWizard Katalog"),
                            specVerifiedAt = row.specVerifiedAt,
                        )
                    )
                }
            }
        }

        return HardwareCatalogSnapshot(
            items = items,
            cpus = cpus,
            gpus = gpus,
            rams = rams,
            motherboards = motherboards,
            psus = psus,
            storages = storages,
            coolers = coolers,
            cases = cases,
            laptops = laptops,
            mice = mice,
            keyboards = keyboards,
            headsets = headsets,
            monitors = monitors,
            microphones = microphones,
            mousepads = mousepads,
            webcams = webcams,
            gamingChairs = gamingChairs,
            caseFans = caseFans,
            upss = upss,
            gamingDesks = gamingDesks,
            gamingLightings = gamingLightings,
            smartwatches = smartwatches,
            routers = routers,
            modems = modems,
            accessPoints = accessPoints,
            rangeExtenders = rangeExtenders,
            phones = phones,
            tablets = tablets,
        )
    }

    private fun JsonObject.primitive(key: String): JsonPrimitive? {
        val el: JsonElement = this[key] ?: return null
        if (el is JsonNull) return null
        return el as? JsonPrimitive
    }

    private fun JsonObject.str(key: String, default: String = ""): String =
        primitive(key)?.content?.takeIf { it.isNotBlank() } ?: default

    private fun JsonObject.strOrNull(key: String): String? =
        primitive(key)?.content?.takeIf { it.isNotBlank() }

    private fun JsonObject.int(key: String, default: Int = 0): Int =
        primitive(key)?.let { it.intOrNull ?: it.doubleOrNull?.toInt() } ?: default

    private fun JsonObject.intOrNull(key: String): Int? =
        primitive(key)?.let { it.intOrNull ?: it.doubleOrNull?.toInt() }

    private fun JsonObject.dbl(key: String, default: Double = 0.0): Double =
        primitive(key)?.doubleOrNull ?: default

    private fun JsonObject.dblOrNull(key: String): Double? =
        primitive(key)?.doubleOrNull

    private fun JsonObject.bool(key: String, default: Boolean = false): Boolean =
        boolOrNull(key) ?: default

    private fun JsonObject.boolOrNull(key: String): Boolean? {
        val p = primitive(key) ?: return null
        p.booleanOrNull?.let { return it }
        p.intOrNull?.let { return it != 0 }
        return when (p.content.trim().lowercase()) {
            "true", "1", "yes" -> true
            "false", "0", "no" -> false
            else -> null
        }
    }

    private fun JsonObject.strList(key: String): List<String> {
        val arr = this[key] as? JsonArray ?: return emptyList()
        return arr.mapNotNull { (it as? JsonPrimitive)?.content?.takeIf { s -> s.isNotBlank() } }
    }

    private fun JsonObject.variants(key: String): List<DeviceVariant> {
        val arr = this[key] as? JsonArray ?: return emptyList()
        return arr.mapNotNull { el -> runCatching { json.decodeFromJsonElement<DeviceVariant>(el) }.getOrNull() }
    }

    private inline fun <reified E : Enum<E>> JsonObject.enum(key: String, default: E): E =
        enumOrNull<E>(key) ?: default

    private inline fun <reified E : Enum<E>> JsonObject.enumOrNull(key: String): E? {
        val raw = strOrNull(key) ?: return null
        return enumValues<E>().firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) }
    }
}
