package com.arrazyfathan.kbbi.core.data.remote.network

import com.arrazyfathan.kbbi.core.data.BuildConfig
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.logging.AppLogger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
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

suspend inline fun <reified Response : Any> HttpClient.get(
    route: String,
    queryParameters: Map<String, Any?> = emptyMap(),
    headers: Map<String, String> = emptyMap(),
): AppResult<Response, DataError> =
    safeCall {
        get {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
            headers.forEach { (key, value) ->
                header(key, value)
            }
        }
    }

suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.post(
    route: String,
    body: Request,
): AppResult<Response, DataError> =
    safeCall {
        post {
            url(constructRoute(route))
            setBody(body)
        }
    }

suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.put(
    route: String,
    body: Request,
): AppResult<Response, DataError> =
    safeCall {
        put {
            url(constructRoute(route))
            setBody(body)
        }
    }

suspend inline fun <reified Request : Any, reified Response : Any> HttpClient.patch(
    route: String,
    body: Request,
): AppResult<Response, DataError> =
    safeCall {
        patch {
            url(constructRoute(route))
            setBody(body)
        }
    }

suspend inline fun <reified Response : Any> HttpClient.delete(
    route: String,
    queryParameters: Map<String, Any?> = emptyMap(),
): AppResult<Response, DataError> =
    safeCall {
        delete {
            url(constructRoute(route))
            queryParameters.forEach { (key, value) ->
                parameter(key, value)
            }
        }
    }

suspend inline fun <reified T : Any> safeCall(noinline execute: suspend () -> HttpResponse): AppResult<T, DataError> {
    val response =
        try {
            execute()
        } catch (_: UnresolvedAddressException) {
            return AppResult.Error(DataError.NoInternet)
        } catch (_: HttpRequestTimeoutException) {
            return AppResult.Error(DataError.RequestTimeout)
        } catch (e: IOException) {
            val error = e.toDataError()
            if (error == DataError.Unknown) {
                AppLogger.error(API_ERROR_TAG, e, "Network request failed unexpectedly")
            }
            return AppResult.Error(error)
        } catch (e: CancellationException) {
            throw e
        } catch (e: SerializationException) {
            AppLogger.error(API_ERROR_TAG, e, "Network response serialization failed")
            return AppResult.Error(DataError.Serialization)
        } catch (e: IllegalArgumentException) {
            AppLogger.error(API_ERROR_TAG, e, "Network response was invalid")
            return AppResult.Error(DataError.Serialization)
        } catch (e: Exception) {
            AppLogger.error(API_ERROR_TAG, e, "Network request failed unexpectedly")
            return AppResult.Error(DataError.Unknown)
        }

    return responseToResult(response)
}

suspend inline fun <reified T : Any> responseToResult(response: HttpResponse): AppResult<T, DataError> =
    when (response.status.value) {
        in 200..299 -> readSuccessBody(response)
        else -> AppResult.Error(response.status.value.toDataError())
    }

suspend inline fun <reified T : Any> readSuccessBody(response: HttpResponse): AppResult<T, DataError> =
    try {
        AppResult.Success(response.body<T>())
    } catch (e: CancellationException) {
        throw e
    } catch (e: SerializationException) {
        AppLogger.error(API_ERROR_TAG, e, "Response body serialization failed")
        AppResult.Error(DataError.Serialization)
    } catch (e: IllegalArgumentException) {
        AppLogger.error(API_ERROR_TAG, e, "Response body was invalid")
        AppResult.Error(DataError.Serialization)
    } catch (e: NoSuchElementException) {
        AppLogger.error(API_ERROR_TAG, e, "Successful response body was empty")
        AppResult.Error(DataError.EmptyBody)
    } catch (e: Exception) {
        AppLogger.error(API_ERROR_TAG, e, "Response body processing failed unexpectedly")
        AppResult.Error(DataError.Unknown)
    }

fun constructRoute(route: String): String =
    when {
        route.startsWith("https://") || route.startsWith("http://") -> route
        route.contains(BuildConfig.BASE_URL) -> route
        route.startsWith("/") -> BuildConfig.BASE_URL.trimEnd('/') + route
        else -> BuildConfig.BASE_URL.trimEnd('/') + "/$route"
    }

@PublishedApi
internal const val API_ERROR_TAG = "KBBI-API"

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
