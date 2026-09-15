package com.arrazyfathan.kbbi.feature.wordstudy.data.di

import com.arrazyfathan.kbbi.feature.wordstudy.data.AiConfigurationDataStore
import com.arrazyfathan.kbbi.feature.wordstudy.data.NetworkWordStudyRepository
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.BackendWordStudyRemoteDataSource
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.CustomAiWordStudyRemoteDataSource
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

val wordStudyDataModule =
    module {
        single(named(CUSTOM_AI_HTTP_CLIENT)) {
            HttpClient(OkHttp) {
                install(ContentNegotiation) { json(get<Json>()) }
                install(HttpTimeout) {
                    connectTimeoutMillis = CUSTOM_AI_TIMEOUT_MILLIS
                    requestTimeoutMillis = CUSTOM_AI_TIMEOUT_MILLIS
                    socketTimeoutMillis = CUSTOM_AI_TIMEOUT_MILLIS
                }
                defaultRequest {
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }
            }
        }
        singleOf(::BackendWordStudyRemoteDataSource)
        single { CustomAiWordStudyRemoteDataSource(get(named(CUSTOM_AI_HTTP_CLIENT)), get()) }
        singleOf(::NetworkWordStudyRepository) { bind<WordStudyRepository>() }
        single<AiConfigurationRepository> { AiConfigurationDataStore(androidContext(), get()) }
    }

private const val CUSTOM_AI_HTTP_CLIENT = "customAiHttpClient"
private const val CUSTOM_AI_TIMEOUT_MILLIS = 120_000L
