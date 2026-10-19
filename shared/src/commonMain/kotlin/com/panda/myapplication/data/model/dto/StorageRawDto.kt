package com.panda.myapplication.data.model.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

@OptIn(ExperimentalSerializationApi::class)
object LenientStringSerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LenientString", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) encoder.encodeString(value) else encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeString() }.getOrNull()
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        if (element is JsonPrimitive) return element.content
        return element.toString()
    }
}

@OptIn(ExperimentalSerializationApi::class)
object LenientCapacityGbSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LenientCapacityGb", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value != null) encoder.encodeInt(value) else encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeInt() }.getOrNull()
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        if (element is JsonPrimitive) {
            val intVal = element.intOrNull
            if (intVal != null) return intVal
            val doubleVal = element.doubleOrNull
            if (doubleVal != null) return doubleVal.toInt()

            val text = element.content.trim()
            val numPart = text.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: return null
            return if (text.contains("TB", ignoreCase = true)) {
                (numPart * 1000).toInt()
            } else {
                numPart.toInt()
            }
        }
        return null
    }
}

@Serializable
data class StorageRawDto(
    val name: String? = null,
    val price: Double? = null,
    @Serializable(with = LenientCapacityGbSerializer::class)
    val capacity: Int? = null,
    @Serializable(with = LenientStringSerializer::class)
    val type: String? = null,
    @SerialName("form_factor")
    @Serializable(with = LenientStringSerializer::class)
    val formFactor: String? = null,
    @SerialName("interface")
    @Serializable(with = LenientStringSerializer::class)
    val `interface`: String? = null,
    @SerialName("cache") val cacheMb: Int? = null,
    @SerialName("seq_read") val seqRead: Int? = null,
    @SerialName("seq_write") val seqWrite: Int? = null,
    @SerialName("nand_type") val nandType: String? = null,
    @SerialName("endurance_tbw") val enduranceTbw: Int? = null,
    val controller: String? = null,
    @SerialName("dram_cache") val dramCache: String? = null,
    @SerialName("random_iops") val randomIops: String? = null,
    val imageUrl: String? = null,
    val images: List<String> = emptyList(),
)
