package com.arrazyfathan.kbbi.core.di

import com.arrazyfathan.kbbi.core.data.remote.network.HttpClientFactory
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val networkModule =
    module {
        single {
            Json {
                ignoreUnknownKeys = true
            }
        }

        single {
            HttpClientFactory(get()).build()
        }
    }
