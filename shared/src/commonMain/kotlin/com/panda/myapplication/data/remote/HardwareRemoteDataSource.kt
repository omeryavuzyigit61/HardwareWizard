package com.panda.myapplication.data.remote

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
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlin.time.Clock
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement

data class RemoteHardwareCatalog(
    val cpus: List<CpuRawDto> = emptyList(),
    val gpus: List<GpuRawDto> = emptyList(),
    val rams: List<RamRawDto> = emptyList(),
    val motherboards: List<MotherboardRawDto> = emptyList(),
    val psus: List<PsuRawDto> = emptyList(),
    val storages: List<StorageRawDto> = emptyList(),
    val coolers: List<CoolerRawDto> = emptyList(),
    val cases: List<CaseRawDto> = emptyList(),
    val prebuilts: List<PrebuiltRawDto> = emptyList(),
    val laptops: List<LaptopRawDto> = emptyList(),
    val peripherals: List<PeripheralRawDto> = emptyList(),
)

class HardwareRemoteDataSource(
    private val httpClient: HttpClient,
    private val ioDispatcher: CoroutineDispatcher,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    },
    private val baseUrl: String = DEFAULT_BASE_URL,
) {

    suspend fun fetchAllHardware(): Result<RemoteHardwareCatalog> = withContext(ioDispatcher) {
        runCatching {
            coroutineScope {
                val cpusDeferred = async { fetchJsonList<CpuRawDto>("$baseUrl/cpu.json") }
                val gpusDeferred = async { fetchJsonList<GpuRawDto>("$baseUrl/video-card.json") }
                val ramsDeferred = async { fetchJsonList<RamRawDto>("$baseUrl/memory.json") }
                val motherboardsDeferred = async { fetchJsonList<MotherboardRawDto>("$baseUrl/motherboard.json") }
                val psusDeferred = async { fetchJsonList<PsuRawDto>("$baseUrl/power-supply.json") }
                val storagesDeferred = async { fetchJsonList<StorageRawDto>("$baseUrl/internal-hard-drive.json") }
                val coolersDeferred = async { fetchJsonList<CoolerRawDto>("$baseUrl/cpu-cooler.json") }
                val casesDeferred = async { fetchJsonList<CaseRawDto>("$baseUrl/case.json") }
                val prebuiltsDeferred = async { fetchJsonList<PrebuiltRawDto>("$baseUrl/prebuilt-pc.json") }
                val laptopsDeferred = async { fetchJsonList<LaptopRawDto>("$baseUrl/laptop.json") }
                val peripheralsDeferred = async { fetchJsonList<PeripheralRawDto>("$baseUrl/peripherals.json") }

                val cpus = cpusDeferred.await()
                val gpus = gpusDeferred.await()
                val rams = ramsDeferred.await()
                val mobos = motherboardsDeferred.await()
                val psus = psusDeferred.await()
                val storages = storagesDeferred.await()
                val coolers = coolersDeferred.await()
                val cases = casesDeferred.await()
                val prebuilts = prebuiltsDeferred.await()
                val laptops = laptopsDeferred.await()
                val peripherals = peripheralsDeferred.await()

                if (cpus.isEmpty() && gpus.isEmpty() && rams.isEmpty() && mobos.isEmpty() && psus.isEmpty() && storages.isEmpty() && coolers.isEmpty() && cases.isEmpty()) {
                    error("GitHub açık veri kaynağından donanım parçaları çekilemedi.")
                }

                RemoteHardwareCatalog(
                    cpus = cpus,
                    gpus = gpus,
                    rams = rams,
                    motherboards = mobos,
                    psus = psus,
                    storages = storages,
                    coolers = coolers,
                    cases = cases,
                    prebuilts = prebuilts,
                    laptops = laptops,
                    peripherals = peripherals,
                )
            }
        }
    }

    private fun bustCacheUrl(url: String): String {
        val sep = if (url.contains("?")) "&" else "?"
        val timestamp = Clock.System.now().toEpochMilliseconds()
        return "$url${sep}nocache=$timestamp"
    }

    suspend fun fetchPricesTr(url: String = DEFAULT_PRICES_TR_URL): Result<Map<String, com.panda.myapplication.domain.model.ProductPriceInfo>> = withContext(ioDispatcher) {
        runCatching {
            val text = com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(
                httpClient.get(bustCacheUrl(url)) {
                    header("Cache-Control", "no-cache")
                    header("Pragma", "no-cache")
                }.bodyAsText(),
                "prices_tr.json",
            )
            json.decodeFromString<Map<String, com.panda.myapplication.domain.model.ProductPriceInfo>>(text)
        }
    }

    suspend fun fetchBenchmarks(url: String = DEFAULT_BENCHMARKS_URL): Result<Map<String, com.panda.myapplication.domain.model.HardwareBenchmarkInfo>> = withContext(ioDispatcher) {
        runCatching {
            val text = com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(
                httpClient.get(bustCacheUrl(url)) {
                    header("Cache-Control", "no-cache")
                    header("Pragma", "no-cache")
                }.bodyAsText(),
                "hardware_benchmarks.json",
            )
            parseBenchmarkJson(text)
        }
    }

    fun parseBenchmarkJson(text: String): Map<String, com.panda.myapplication.domain.model.HardwareBenchmarkInfo> = runCatching {
        val safeText = com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(text, "hardware_benchmarks")
        val element = json.parseToJsonElement(safeText)
        if (element is kotlinx.serialization.json.JsonObject && (element.containsKey("cpus") || element.containsKey("gpus"))) {
            return@runCatching json.decodeFromJsonElement(com.panda.myapplication.data.model.dto.BenchmarkCatalogRawDto.serializer(), element).toDomainMap()
        }
        json.decodeFromString<Map<String, com.panda.myapplication.domain.model.HardwareBenchmarkInfo>>(safeText)
    }.getOrDefault(emptyMap())

    suspend fun fetchVerifiedLabBenchmarks(
        url: String = DEFAULT_VERIFIED_BENCHMARKS_URL,
    ): Result<List<com.panda.myapplication.domain.model.VerifiedLabGameTest>> = withContext(ioDispatcher) {
        runCatching {
            val list = fetchJsonList<com.panda.myapplication.domain.model.VerifiedLabGameTest>(url)
            if (list.isNotEmpty()) {
                com.panda.myapplication.domain.model.VerifiedLabBenchmarks.updateRemoteTests(list)
            }
            list
        }
    }

    suspend fun fetchSmartphonesJson(url: String = DEFAULT_SMARTPHONES_URL): Result<String> = withContext(ioDispatcher) {
        runCatching {
            com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(
                httpClient.get(bustCacheUrl(url)) {
                    header("Cache-Control", "no-cache")
                    header("Pragma", "no-cache")
                }.bodyAsText(),
                "smartphone.json",
            )
        }
    }

    suspend fun fetchTabletsJson(url: String = DEFAULT_TABLETS_URL): Result<String> = withContext(ioDispatcher) {
        runCatching {
            com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(
                httpClient.get(bustCacheUrl(url)) {
                    header("Cache-Control", "no-cache")
                    header("Pragma", "no-cache")
                }.bodyAsText(),
                "tablet.json",
            )
        }
    }

    private suspend inline fun <reified T> fetchJsonList(url: String): List<T> = runCatching {
        val targetUrl = bustCacheUrl(url)
        val text = com.panda.myapplication.domain.util.SecurityUtils.requireSafeJsonPayload(
            httpClient.get(targetUrl) {
                header("Cache-Control", "no-cache")
                header("Pragma", "no-cache")
            }.bodyAsText(),
            url,
        )
        val jsonElement = json.parseToJsonElement(text)
        if (jsonElement is kotlinx.serialization.json.JsonArray) {
            jsonElement.mapNotNull { element ->
                runCatching { json.decodeFromJsonElement<T>(element) }.getOrNull()
            }
        } else {
            emptyList()
        }
    }.getOrDefault(emptyList())

    companion object {
        const val DEFAULT_BASE_URL = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/catalog"
        const val DEFAULT_SMARTPHONES_URL = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/catalog/smartphone.json"
        const val DEFAULT_TABLETS_URL = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/catalog/tablet.json"
        const val DEFAULT_PRICES_TR_URL = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/prices_tr.json"
        const val DEFAULT_BENCHMARKS_URL = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/hardware_benchmarks.json"
        const val DEFAULT_VERIFIED_BENCHMARKS_URL = "https://raw.githubusercontent.com/omeryavuzyigit61/HardwareWizard/main/data/verified_lab_benchmarks.json"
    }
}
