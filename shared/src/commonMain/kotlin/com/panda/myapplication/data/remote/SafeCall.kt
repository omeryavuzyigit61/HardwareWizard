package com.panda.myapplication.data.remote

import com.panda.myapplication.domain.common.AppResult
import com.panda.myapplication.domain.common.DataError
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/**
 * Merkezi Ktor güvenli ağ çağrısı sarmalayıcısı (pre_prompt_kmp.md Kurallar 60-66).
 *
 * 1. CancellationException ASLA yutulmaz, her zaman yeniden fırlatılır.
 * 2. Zaman aşımı istisnaları ve 408 -> TIMEOUT
 * 3. Ağ/IO istisnaları (iOS DarwinHttpRequestException dahil) -> NO_CONNECTION
 * 4. Serileştirme istisnaları -> SERIALIZATION
 * 5. 401 -> UNAUTHORIZED, 429 -> TOO_MANY_REQUESTS, 5xx -> SERVER
 * 6. TLS / sertifika pinning hataları -> TLS_ERROR
 */
suspend inline fun <T> safeCall(
    execute: () -> HttpResponse,
    transform: (HttpResponse) -> T,
): AppResult<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: CancellationException) {
        throw e // CancellationException her zaman yeniden fırlatılır
    } catch (_: HttpRequestTimeoutException) {
        return AppResult.Failure(DataError.Network.Timeout)
    } catch (_: SerializationException) {
        return AppResult.Failure(DataError.Network.Serialization)
    } catch (e: Exception) {
        val name = e::class.simpleName ?: ""
        if (name.contains("DarwinHttpRequestException", ignoreCase = true) ||
            name.contains("ConnectException", ignoreCase = true) ||
            name.contains("UnknownHost", ignoreCase = true) ||
            name.contains("IOException", ignoreCase = true)
        ) {
            return AppResult.Failure(DataError.Network.NoConnection)
        }
        if (name.contains("SSL", ignoreCase = true) ||
            name.contains("Certificate", ignoreCase = true) ||
            name.contains("Tls", ignoreCase = true)
        ) {
            return AppResult.Failure(DataError.Network.TlsError)
        }
        return AppResult.Failure(DataError.Network.Unknown(e.message))
    }

    return when (response.status.value) {
        in 200..299 -> {
            try {
                AppResult.Success(transform(response))
            } catch (e: CancellationException) {
                throw e
            } catch (_: SerializationException) {
                AppResult.Failure(DataError.Network.Serialization)
            } catch (e: Exception) {
                AppResult.Failure(DataError.Network.Unknown(e.message))
            }
        }
        401 -> AppResult.Failure(DataError.Network.Unauthorized)
        408 -> AppResult.Failure(DataError.Network.Timeout)
        429 -> AppResult.Failure(DataError.Network.TooManyRequests)
        in 500..599 -> AppResult.Failure(DataError.Network.ServerError)
        else -> AppResult.Failure(DataError.Network.Unknown("HTTP ${response.status.value}"))
    }
}
