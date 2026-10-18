package com.panda.myapplication.data.local.converter

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.time.Instant

import com.panda.myapplication.domain.model.PricePoint

/** Enums are persisted by name via Room's built-in enum support; only non-primitive types live here. */
class HardwareTypeConverters {

    @TypeConverter
    fun instantToEpochMillis(value: Instant): Long = value.toEpochMilliseconds()

    @TypeConverter
    fun epochMillisToInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    @TypeConverter
    fun stringListToJson(value: List<String>?): String? = value?.let {
        json.encodeToString(stringListSerializer, it)
    }

    @TypeConverter
    fun jsonToStringList(value: String?): List<String>? = value?.let {
        runCatching { json.decodeFromString(stringListSerializer, it) }.getOrNull()
    }

    @TypeConverter
    fun pricePointListToJson(value: List<PricePoint>?): String? = value?.let {
        json.encodeToString(it)
    }

    @TypeConverter
    fun jsonToPricePointList(value: String?): List<PricePoint>? = value?.let {
        runCatching { json.decodeFromString<List<PricePoint>>(it) }.getOrNull()
    }

    @TypeConverter
    fun storeOfferListToJson(value: List<com.panda.myapplication.domain.model.StoreOffer>?): String? = value?.let {
        json.encodeToString(it)
    }

    @TypeConverter
    fun jsonToStoreOfferList(value: String?): List<com.panda.myapplication.domain.model.StoreOffer>? = value?.let {
        runCatching { json.decodeFromString<List<com.panda.myapplication.domain.model.StoreOffer>>(it) }.getOrNull()
    }

    @TypeConverter
    fun cpuSyntheticToJson(value: com.panda.myapplication.domain.model.CpuSyntheticBenchmark?): String? = value?.let {
        json.encodeToString(it)
    }

    @TypeConverter
    fun jsonToCpuSynthetic(value: String?): com.panda.myapplication.domain.model.CpuSyntheticBenchmark? = value?.let {
        runCatching { json.decodeFromString<com.panda.myapplication.domain.model.CpuSyntheticBenchmark>(it) }.getOrNull()
    }

    @TypeConverter
    fun gpuSyntheticToJson(value: com.panda.myapplication.domain.model.GpuSyntheticBenchmark?): String? = value?.let {
        json.encodeToString(it)
    }

    @TypeConverter
    fun jsonToGpuSynthetic(value: String?): com.panda.myapplication.domain.model.GpuSyntheticBenchmark? = value?.let {
        runCatching { json.decodeFromString<com.panda.myapplication.domain.model.GpuSyntheticBenchmark>(it) }.getOrNull()
    }

    @TypeConverter
    fun gameFpsToJson(value: com.panda.myapplication.domain.model.GameFpsBenchmark?): String? = value?.let {
        json.encodeToString(it)
    }

    @TypeConverter
    fun jsonToGameFps(value: String?): com.panda.myapplication.domain.model.GameFpsBenchmark? = value?.let {
        runCatching { json.decodeFromString<com.panda.myapplication.domain.model.GameFpsBenchmark>(it) }.getOrNull()
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        val stringListSerializer = ListSerializer(String.serializer())
    }
}
