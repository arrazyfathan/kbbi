package com.arrazyfathan.kbbi.core.data.source.remote.network

import android.util.Log
import com.arrazyfathan.kbbi.BuildConfig
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
import kotlinx.serialization.json.JsonElement

private const val NETWORK_TIMEOUT_SECONDS = 120L
private const val NETWORK_TIMEOUT_MILLIS = NETWORK_TIMEOUT_SECONDS * 1_000L
private const val API_LOG_TAG = "KBBI-API"
private const val ANDROID_LOG_CHUNK_SIZE = 3_500
private const val KTOR_BODY_START = "BODY START"
private const val KTOR_BODY_END = "BODY END"

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
                            logApiMessage(message)
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

private val prettyApiLogJson =
    Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

private fun logApiMessage(message: String) {
    val formattedMessage = message.prettyKtorBodyJsonOrSelf()
    formattedMessage.chunked(ANDROID_LOG_CHUNK_SIZE).forEach { chunk ->
        Log.d(API_LOG_TAG, chunk)
    }
}

private fun String.prettyKtorBodyJsonOrSelf(): String {
    val bodyStartIndex = indexOf(KTOR_BODY_START)
    if (bodyStartIndex == -1) return prettyJsonOrSelf()

    val bodyContentStart = indexOf('\n', startIndex = bodyStartIndex).takeIf { it != -1 }?.plus(1) ?: return this
    val bodyEndIndex = indexOf(KTOR_BODY_END, startIndex = bodyContentStart).takeIf { it != -1 } ?: return this
    val body = substring(bodyContentStart, bodyEndIndex).trim()
    val prettyBody = body.toPrettyJsonOrNull() ?: return this

    return replaceRange(bodyContentStart, bodyEndIndex, "$prettyBody\n")
}

private fun String.prettyJsonOrSelf(): String = toPrettyJsonOrNull() ?: this

private fun String.toPrettyJsonOrNull(): String? {
    val trimmedMessage = trim()
    val isJsonObject = trimmedMessage.startsWith("{") && trimmedMessage.endsWith("}")
    val isJsonArray = trimmedMessage.startsWith("[") && trimmedMessage.endsWith("]")

    if (!isJsonObject && !isJsonArray) return null

    return runCatching {
        val jsonElement = prettyApiLogJson.parseToJsonElement(trimmedMessage)
        prettyApiLogJson.encodeToString(JsonElement.serializer(), jsonElement)
    }.getOrNull()
}
