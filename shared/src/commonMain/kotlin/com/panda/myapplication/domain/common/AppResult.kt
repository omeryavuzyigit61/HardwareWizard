package com.panda.myapplication.domain.common

/**
 * pre_prompt_kmp.md mimari kurallarına uygun tip güvenli sonuç sarmalayıcısı.
 * kotlin.Result / kotlin.Error ile isim çakışması önlenmiştir.
 */
sealed interface AppError

sealed interface DataError : AppError {
    sealed interface Network : DataError {
        data object Timeout : Network
        data object TooManyRequests : Network
        data object NoConnection : Network
        data object Serialization : Network
        data object Unauthorized : Network
        data object ServerError : Network
        data object TlsError : Network
        data class Unknown(val message: String? = null) : Network
    }

    sealed interface Local : DataError {
        data object NotFound : Local
        data class Unknown(val message: String? = null) : Local
    }
}

sealed interface AppResult<out D, out E : AppError> {
    data class Success<out D>(val data: D) : AppResult<D, Nothing>
    data class Failure<out E : AppError>(val error: E) : AppResult<Nothing, E>
}

inline fun <D, E : AppError, R> AppResult<D, E>.map(transform: (D) -> R): AppResult<R, E> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}

inline fun <D, E : AppError> AppResult<D, E>.onSuccess(action: (D) -> Unit): AppResult<D, E> {
    if (this is AppResult.Success) action(data)
    return this
}

inline fun <D, E : AppError> AppResult<D, E>.onFailure(action: (E) -> Unit): AppResult<D, E> {
    if (this is AppResult.Failure) action(error)
    return this
}
