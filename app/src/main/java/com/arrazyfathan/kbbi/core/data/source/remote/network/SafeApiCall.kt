package com.arrazyfathan.kbbi.core.data.source.remote.network

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

private const val HTTP_BAD_REQUEST = 400
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_CONFLICT = 409
private const val HTTP_PAYLOAD_TOO_LARGE = 413
private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_REQUEST_TIMEOUT = 408
private const val HTTP_SERVER_ERROR_START = 500
private const val HTTP_SERVICE_UNAVAILABLE = 503

suspend inline fun <reified T> safeApiCall(noinline apiCall: suspend () -> HttpResponse): AppResult<T, DataError> =
    withContext(Dispatchers.IO) {
        try {
            val response = apiCall()

            when {
                response.status.isSuccess() -> AppResult.Success(response.body<T>())
                else -> AppResult.Error(response.status.value.toDataError())
            }
        } catch (e: IOException) {
            AppResult.Error(e.toDataError())
        } catch (e: CancellationException) {
            throw e
        } catch (_: SerializationException) {
            AppResult.Error(DataError.Serialization)
        } catch (_: IllegalArgumentException) {
            AppResult.Error(DataError.Serialization)
        } catch (_: Exception) {
            AppResult.Error(DataError.Unknown)
        }
    }

@PublishedApi
internal fun IOException.toDataError(): DataError =
    when (this) {
        is ConnectException,
        is UnknownHostException,
        -> DataError.NoInternet

        is SocketTimeoutException -> DataError.RequestTimeout

        else -> DataError.Unknown
    }

@PublishedApi
internal fun Int.toDataError(): DataError =
    when (this) {
        HTTP_BAD_REQUEST -> DataError.BadRequest
        HTTP_UNAUTHORIZED -> DataError.Unauthorized
        HTTP_FORBIDDEN -> DataError.Forbidden
        HTTP_NOT_FOUND -> DataError.NotFound
        HTTP_CONFLICT -> DataError.Conflict
        HTTP_PAYLOAD_TOO_LARGE -> DataError.PayloadTooLarge
        HTTP_TOO_MANY_REQUESTS -> DataError.TooManyRequests
        HTTP_REQUEST_TIMEOUT -> DataError.RequestTimeout
        HTTP_SERVICE_UNAVAILABLE -> DataError.ServiceUnavailable
        in HTTP_SERVER_ERROR_START..599 -> DataError.ServerError
        else -> DataError.Unknown
    }
