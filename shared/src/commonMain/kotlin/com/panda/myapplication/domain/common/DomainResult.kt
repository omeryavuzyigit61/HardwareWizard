package com.panda.myapplication.domain.common

/** Standardized outcome wrapper for domain operations. */
sealed interface DomainResult<out T> {
    data class Success<out T>(val data: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

/** Typed, framework-agnostic domain failures. */
sealed interface DomainError {
    data object Network : DomainError
    data object NotFound : DomainError
    data object Storage : DomainError
    data class Unknown(val cause: Throwable) : DomainError
}

inline fun <T, R> DomainResult<T>.map(transform: (T) -> R): DomainResult<R> = when (this) {
    is DomainResult.Success -> DomainResult.Success(transform(data))
    is DomainResult.Failure -> this
}
