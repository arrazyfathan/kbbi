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

typealias EmptyResult<E> = AppResult<Unit, E>

inline fun <T, E : AppError, R> AppResult<T, E>.map(transform: (T) -> R): AppResult<R, E> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(data))
        is AppResult.Error -> AppResult.Error(error)
    }

inline fun <T, E : AppError> AppResult<T, E>.onSuccess(action: (T) -> Unit): AppResult<T, E> =
    when (this) {
        is AppResult.Success -> {
            action(data)
            this
        }
        is AppResult.Error -> this
    }

inline fun <T, E : AppError> AppResult<T, E>.onFailure(action: (E) -> Unit): AppResult<T, E> =
    when (this) {
        is AppResult.Success -> this
        is AppResult.Error -> {
            action(error)
            this
        }
    }

fun <T, E : AppError> AppResult<T, E>.asEmptyResult(): EmptyResult<E> = map { }
