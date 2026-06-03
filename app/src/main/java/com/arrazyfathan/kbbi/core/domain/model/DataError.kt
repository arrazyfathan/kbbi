package com.arrazyfathan.kbbi.core.domain.model

sealed interface DataError : AppError {
    data object EmptyQuery : DataError
    data object NotFound : DataError
    data object NoInternet : DataError
    data class Remote(
        val message: String,
    ) : DataError
    data object Unknown : DataError
}
