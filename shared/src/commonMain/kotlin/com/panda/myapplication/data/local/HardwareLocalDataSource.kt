package com.panda.myapplication.data.local

import com.panda.myapplication.data.local.dao.HardwareDao
import com.panda.myapplication.data.mapper.HardwareMapper
import com.panda.myapplication.data.model.dto.CaseRawDto
import com.panda.myapplication.data.model.dto.CoolerRawDto
import com.panda.myapplication.data.model.dto.CpuRawDto
import com.panda.myapplication.data.model.dto.GpuRawDto
import com.panda.myapplication.data.model.dto.MotherboardRawDto
import com.panda.myapplication.data.model.dto.PsuRawDto
import com.panda.myapplication.data.model.dto.RamRawDto
import com.panda.myapplication.data.model.dto.StorageRawDto
import hardwarewizard.shared.generated.resources.Res
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlinx.serialization.json.Json
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
    suspend fun seedDatabaseIfEmpty(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val cpus = readJsonAssetList<CpuRawDto>("files/cpu.json")
            val gpus = readJsonAssetList<GpuRawDto>("files/video-card.json")
            val rams = readJsonAssetList<RamRawDto>("files/memory.json")
            val motherboards = readJsonAssetList<MotherboardRawDto>("files/motherboard.json")
            val psus = readJsonAssetList<PsuRawDto>("files/power-supply.json")
            val storages = readJsonAssetList<StorageRawDto>("files/internal-hard-drive.json")
            val coolers = readJsonAssetList<CoolerRawDto>("files/cpu-cooler.json")
            val cases = readJsonAssetList<CaseRawDto>("files/case.json")

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
            val text = bytes.decodeToString()
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
            val text = bytes.decodeToString()
            val element = json.parseToJsonElement(text)
            if (element is kotlinx.serialization.json.JsonArray) {
                element.mapNotNull { itemElement ->
                    runCatching {
                        json.decodeFromString<T>(itemElement.toString())
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
            json.decodeFromString<T>(bytes.decodeToString())
        }.getOrNull()
    }
}
