package com.panda.myapplication.data.model.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

@OptIn(ExperimentalSerializationApi::class)
object LenientIntSerializer : KSerializer<Int?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LenientInt", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value != null) encoder.encodeInt(value) else encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeInt() }.getOrNull()
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        if (element is JsonPrimitive) {
            element.intOrNull?.let { return it }
            element.doubleOrNull?.let { return it.toInt() }
            val digits = element.content.filter { it.isDigit() }
            return digits.toIntOrNull()
        }
        return null
    }
}

@OptIn(ExperimentalSerializationApi::class)
object LenientDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LenientDouble", PrimitiveKind.DOUBLE)

    override fun serialize(encoder: Encoder, value: Double?) {
        if (value != null) encoder.encodeDouble(value) else encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): Double? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeDouble() }.getOrNull()
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        if (element is JsonPrimitive) {
            element.doubleOrNull?.let { return it }
            element.intOrNull?.let { return it.toDouble() }
            val cleaned = element.content.filter { it.isDigit() || it == '.' }
            return cleaned.toDoubleOrNull()
        }
        return null
    }
}

@OptIn(ExperimentalSerializationApi::class)
object FlexibleSpeedListSerializer : KSerializer<List<Int>?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleSpeedList", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<Int>?) {
        if (value != null) {
            val jsonEncoder = encoder as? JsonEncoder
            if (jsonEncoder != null) {
                jsonEncoder.encodeJsonElement(JsonArray(value.map { JsonPrimitive(it) }))
            } else {
                encoder.encodeString(value.joinToString(","))
            }
        } else {
            encoder.encodeNull()
        }
    }

    override fun deserialize(decoder: Decoder): List<Int>? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching {
            val str = decoder.decodeString()
            parseSpeedFromString(str)
        }.getOrNull()

        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        if (element is JsonArray) {
            val list = element.mapNotNull {
                val prim = it as? JsonPrimitive
                prim?.intOrNull ?: prim?.content?.filter { c -> c.isDigit() }?.toIntOrNull()
            }
            return if (list.isNotEmpty()) list else null
        }
        if (element is JsonPrimitive) {
            val intVal = element.intOrNull
            if (intVal != null) {
                val gen = when {
                    intVal >= 4800 -> 5
                    intVal >= 2133 -> 4
                    else -> 3
                }
                return listOf(gen, intVal)
            }
            return parseSpeedFromString(element.content)
        }
        return null
    }

    private fun parseSpeedFromString(str: String): List<Int>? {
        if (str.isBlank()) return null
        val gen = when {
            str.contains("DDR5", ignoreCase = true) -> 5
            str.contains("DDR4", ignoreCase = true) -> 4
            str.contains("DDR3", ignoreCase = true) -> 3
            else -> null
        }
        val numbers = Regex("\\d+").findAll(str).mapNotNull { it.value.toIntOrNull() }.toList()
        val mhz = numbers.firstOrNull { it >= 800 } ?: numbers.lastOrNull() ?: return null
        val deducedGen = gen ?: when {
            mhz >= 4800 -> 5
            mhz >= 2133 -> 4
            else -> 3
        }
        return listOf(deducedGen, mhz)
    }
}

@OptIn(ExperimentalSerializationApi::class)
object FlexibleModulesListSerializer : KSerializer<List<Int>?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleModulesList", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: List<Int>?) {
        if (value != null) {
            val jsonEncoder = encoder as? JsonEncoder
            if (jsonEncoder != null) {
                jsonEncoder.encodeJsonElement(JsonArray(value.map { JsonPrimitive(it) }))
            } else {
                encoder.encodeString(value.joinToString(","))
            }
        } else {
            encoder.encodeNull()
        }
    }

    override fun deserialize(decoder: Decoder): List<Int>? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        if (element is JsonArray) {
            val list = element.mapNotNull {
                val prim = it as? JsonPrimitive
                prim?.intOrNull ?: prim?.content?.filter { c -> c.isDigit() }?.toIntOrNull()
            }
            return if (list.isNotEmpty()) list else null
        }
        if (element is JsonPrimitive) {
            val intVal = element.intOrNull
            if (intVal != null) {
                return listOf(1, intVal)
            }
            val numbers = Regex("\\d+").findAll(element.content).mapNotNull { it.value.toIntOrNull() }.toList()
            return if (numbers.size >= 2) {
                listOf(numbers[0], numbers[1])
            } else if (numbers.size == 1) {
                listOf(1, numbers[0])
            } else null
        }
        return null
    }
}

@OptIn(ExperimentalSerializationApi::class)
object LenientModularitySerializer : KSerializer<String?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LenientModularity", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: String?) {
        if (value != null) encoder.encodeString(value) else encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): String? {
        val jsonDecoder = decoder as? JsonDecoder ?: return runCatching { decoder.decodeString() }.getOrNull()
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonNull) return null
        if (element is JsonPrimitive) {
            val content = element.content.trim()
            if (content.equals("true", ignoreCase = true) || content.equals("full", ignoreCase = true)) {
                return "Tam Modüler (Full Modular)"
            }
            if (content.equals("semi", ignoreCase = true)) {
                return "Yarı Modüler"
            }
            if (content.equals("false", ignoreCase = true) || content.equals("none", ignoreCase = true)) {
                return "Modüler Değil"
            }
            if (content.isNotBlank()) {
                return content
            }
        }
        return null
    }
}

