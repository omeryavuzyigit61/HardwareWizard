package com.panda.myapplication.data.local

import com.panda.myapplication.data.local.dao.HardwareDao
import com.panda.myapplication.data.mapper.HardwareMapper
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
import hardwarewizard.shared.generated.resources.Res
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.jetbrains.compose.resources.ExperimentalResourceApi

class HardwareLocalDataSource(
    private val hardwareDao: HardwareDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    },
) {

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalCpus(): List<CpuRawDto> = readJsonAssetList("files/cpu.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalGpus(): List<GpuRawDto> = readJsonAssetList("files/video-card.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalRams(): List<RamRawDto> = readJsonAssetList("files/memory.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalMotherboards(): List<MotherboardRawDto> = readJsonAssetList("files/motherboard.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalPsus(): List<PsuRawDto> = readJsonAssetList("files/power-supply.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalStorages(): List<StorageRawDto> = readJsonAssetList("files/internal-hard-drive.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalCoolers(): List<CoolerRawDto> = readJsonAssetList("files/cpu-cooler.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalCases(): List<CaseRawDto> = readJsonAssetList("files/case.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalPrebuilts(): List<PrebuiltRawDto> = readJsonAssetList("files/prebuilt-pc.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalLaptops(): List<LaptopRawDto> = readJsonAssetList("files/laptop.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalPeripherals(): List<PeripheralRawDto> = readJsonAssetList("files/peripherals.json")

    @OptIn(ExperimentalResourceApi::class)
    suspend fun seedDatabaseIfEmpty(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val cpus = loadLocalCpus()
            val gpus = loadLocalGpus()
            val rams = loadLocalRams()
            val motherboards = loadLocalMotherboards()
            val psus = loadLocalPsus()
            val storages = loadLocalStorages()
            val coolers = loadLocalCoolers()
            val cases = loadLocalCases()
            val prebuilts = loadLocalPrebuilts()
            val laptops = loadLocalLaptops()
            val peripherals = loadLocalPeripherals()

            val pricesTr = loadLocalPricesTr()
            val benchmarks = loadLocalBenchmarks()

            val syncedAt = Clock.System.now()
            val snapshot = HardwareMapper.buildCatalogSnapshot(
                cpus = cpus,
                gpus = gpus,
                rams = rams,
                motherboards = motherboards,
                psus = psus,
                storages = storages,
                coolers = coolers,
                cases = cases,
                prebuilts = prebuilts,
                laptops = laptops,
                peripherals = peripherals,
                pricesTr = pricesTr,
                benchmarks = benchmarks,
                syncedAt = syncedAt,
            )

            if (snapshot.items.isNotEmpty()) {
                hardwareDao.replaceCatalog(snapshot, syncedAt = syncedAt)
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalPricesTr(): Map<String, com.panda.myapplication.domain.model.ProductPriceInfo> = withContext(ioDispatcher) {
        readJsonAsset<Map<String, com.panda.myapplication.domain.model.ProductPriceInfo>>("files/prices_tr.json").orEmpty()
    }

    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadLocalBenchmarks(): Map<String, com.panda.myapplication.domain.model.HardwareBenchmarkInfo> = withContext(ioDispatcher) {
        runCatching {
            val bytes = Res.readBytes("files/hardware_benchmarks.json")
            val text = com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(
                bytes.decodeToString(),
                "files/hardware_benchmarks.json",
            )
            val element = json.parseToJsonElement(text)
            if (element is kotlinx.serialization.json.JsonObject && (element.containsKey("cpus") || element.containsKey("gpus"))) {
                json.decodeFromJsonElement(com.panda.myapplication.data.model.dto.BenchmarkCatalogRawDto.serializer(), element).toDomainMap()
            } else {
                json.decodeFromString<Map<String, com.panda.myapplication.domain.model.HardwareBenchmarkInfo>>(text)
            }
        }.getOrDefault(emptyMap())
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend inline fun <reified T> readJsonAssetList(path: String): List<T> = withContext(ioDispatcher) {
        runCatching {
            val bytes = Res.readBytes(path)
            val text = com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(
                bytes.decodeToString(),
                path,
            )
            val element = json.parseToJsonElement(text)
            if (element is kotlinx.serialization.json.JsonArray) {
                element.mapNotNull { itemElement ->
                    runCatching {
                        json.decodeFromJsonElement<T>(itemElement)
                    }.getOrNull()
                }
            } else {
                emptyList()
            }
        }.getOrDefault(emptyList())
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend inline fun <reified T> readJsonAsset(path: String): T? {
        return runCatching {
            val bytes = Res.readBytes(path)
            val text = com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(
                bytes.decodeToString(),
                path,
            )
            json.decodeFromString<T>(text)
        }.getOrNull()
    }
}
