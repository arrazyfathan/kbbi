package com.arrazyfathan.kbbi.core.di

import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arrazyfathan.kbbi.BuildConfig
import com.arrazyfathan.kbbi.core.data.WordRepository
import com.arrazyfathan.kbbi.core.data.source.local.AssetWordCatalogRepository
import com.arrazyfathan.kbbi.core.data.source.local.WordLocalDataSource
import com.arrazyfathan.kbbi.core.data.source.local.room.WordDatabase
import com.arrazyfathan.kbbi.core.data.source.remote.WordRemoteDataSource
import com.arrazyfathan.kbbi.core.data.source.remote.network.ApiService
import com.arrazyfathan.kbbi.core.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.core.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.core.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.core.domain.repository.WordSearchRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.accept
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import io.ktor.serialization.kotlinx.json.json as ktorJson

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

private const val NETWORK_TIMEOUT_SECONDS = 120L
private const val NETWORK_TIMEOUT_MILLIS = NETWORK_TIMEOUT_SECONDS * 1_000L
private const val API_LOG_TAG = "KBBI-API"
private const val ANDROID_LOG_CHUNK_SIZE = 3_500
private const val KTOR_BODY_START = "BODY START"
private const val KTOR_BODY_END = "BODY END"

private val prettyApiLogJson =
    Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

private val MIGRATION_7_8 =
    object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE history_table ADD COLUMN searchedAt INTEGER NOT NULL DEFAULT 0")
        }
    }

val databaseModule =
    module {
        factory { get<WordDatabase>().wordDao() }
        single {
            Room
                .databaseBuilder(
                    androidContext(),
                    WordDatabase::class.java,
                    "kbbi_db",
                ).addMigrations(MIGRATION_7_8)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }

val networkModule =
    module {
        single {
            Json {
                ignoreUnknownKeys = true
            }
        }

        single {
            HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    ktorJson(get())
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
                install(DefaultRequest) {
                    url(BuildConfig.BASE_URL)
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                    header("Accept-Language", "id")
                }
            }
        }

        singleOf(::ApiService)
    }

val repositoryModule =
    module {
        singleOf(::WordRemoteDataSource)
        singleOf(::WordLocalDataSource)
        single<WordCatalogRepository> { AssetWordCatalogRepository(androidContext(), get()) }
        singleOf(::WordRepository) {
            bind<WordSearchRepository>()
            bind<BookmarkRepository>()
            bind<SearchHistoryRepository>()
        }
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
