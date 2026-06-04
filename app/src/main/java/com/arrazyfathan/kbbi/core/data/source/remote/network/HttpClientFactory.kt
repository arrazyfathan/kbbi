package com.arrazyfathan.kbbi.core.data.source.remote.network

import com.arrazyfathan.kbbi.BuildConfig
import com.arrazyfathan.kbbi.core.logging.AppLogger
import com.arrazyfathan.kbbi.core.logging.NetworkLogFormatter
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val NETWORK_TIMEOUT_SECONDS = 120L
private const val NETWORK_TIMEOUT_MILLIS = NETWORK_TIMEOUT_SECONDS * 1_000L
private const val API_LOG_TAG = "KBBI-API"

class HttpClientFactory(
    private val json: Json,
) {
    fun build(engine: HttpClientEngine = OkHttp.create()): HttpClient =
        HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            install(HttpTimeout) {
                connectTimeoutMillis = NETWORK_TIMEOUT_MILLIS
                requestTimeoutMillis = NETWORK_TIMEOUT_MILLIS
                socketTimeoutMillis = NETWORK_TIMEOUT_MILLIS
            }
            install(Logging) {
                level = LogLevel.BODY
                logger =
                    object : Logger {
                        override fun log(message: String) {
                            AppLogger.debug(
                                tag = API_LOG_TAG,
                                message = NetworkLogFormatter.format(message),
                            )
                        }
                    }
            }
            defaultRequest {
                url(BuildConfig.BASE_URL)
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                header("Accept-Language", "id")
            }
        }
}
