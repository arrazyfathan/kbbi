package com.arrazyfathan.kbbi.core.domain.model

interface AppError

sealed interface AppResult<out D, out E : AppError> {
    data class Success<out D>(
        val data: D,
    ) : AppResult<D, Nothing>

    data class Error<out E : AppError>(
        val error: E,
    ) : AppResult<Nothing, E>
}

inline fun <T, E : AppError, R> AppResult<T, E>.map(transform: (T) -> R): AppResult<R, E> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Error -> AppResult.Error(error)
    }
