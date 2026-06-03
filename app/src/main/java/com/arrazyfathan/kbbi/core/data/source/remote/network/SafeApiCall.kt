package com.arrazyfathan.kbbi.core.data.source.remote.network

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
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

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): AppResult<T, DataError> =
    withContext(Dispatchers.IO) {
        try {
            val response = apiCall()
            val body = response.body()

            when {
                response.isSuccessful && body != null -> AppResult.Success(body)
                response.isSuccessful -> AppResult.Error(DataError.EmptyBody)
                else -> AppResult.Error(response.code().toDataError())
            }
        } catch (e: IOException) {
            AppResult.Error(e.toDataError())
        } catch (e: CancellationException) {
            throw e
        } catch (_: JsonParseException) {
            AppResult.Error(DataError.Serialization)
        } catch (_: IllegalArgumentException) {
            AppResult.Error(DataError.Serialization)
        } catch (_: Exception) {
            AppResult.Error(DataError.Unknown)
        }
    }

private fun IOException.toDataError(): DataError =
    when (this) {
        is ConnectException,
        is UnknownHostException,
        -> DataError.NoInternet
        is SocketTimeoutException -> DataError.RequestTimeout
        else -> DataError.Unknown
    }

private fun Int.toDataError(): DataError =
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
