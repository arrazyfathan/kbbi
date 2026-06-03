package com.arrazyfathan.kbbi.core.domain.model

sealed interface DataError : AppError {
    data object EmptyQuery : DataError
    data object NoInternet : DataError
    data object BadRequest : DataError
    data object RequestTimeout : DataError
    data object Unauthorized : DataError
    data object Forbidden : DataError
    data object NotFound : DataError
    data object Conflict : DataError
    data object TooManyRequests : DataError
    data object PayloadTooLarge : DataError
    data object ServerError : DataError
    data object ServiceUnavailable : DataError
    data object EmptyBody : DataError
    data object Serialization : DataError
    data class Remote(
        val message: String,
    ) : DataError
    data object Unknown : DataError
}
