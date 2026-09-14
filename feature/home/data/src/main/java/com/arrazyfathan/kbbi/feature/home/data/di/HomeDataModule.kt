package com.arrazyfathan.kbbi.feature.home.data.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arrazyfathan.kbbi.feature.home.data.AiConfigurationDataStore
import com.arrazyfathan.kbbi.feature.home.data.NetworkWordStudyRepository
import com.arrazyfathan.kbbi.feature.home.data.WordRepository
import com.arrazyfathan.kbbi.feature.home.data.source.local.AssetWordCatalogRepository
import com.arrazyfathan.kbbi.feature.home.data.source.local.WordLocalDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDatabase
import com.arrazyfathan.kbbi.feature.home.data.source.remote.BackendWordStudyRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.remote.CustomAiWordStudyRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.remote.SharedPreferencesVisitorIdProvider
import com.arrazyfathan.kbbi.feature.home.data.source.remote.TopWordsRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.remote.VisitorIdProvider
import com.arrazyfathan.kbbi.feature.home.data.source.remote.WordRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TopWordsRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TranslateRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordStudyRepository
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

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

private val MIGRATION_7_8 =
    object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE history_table ADD COLUMN searchedAt INTEGER NOT NULL DEFAULT 0")
        }
    }

private val MIGRATION_8_9 =
    object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE word_table ADD COLUMN visitorCount INTEGER")
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
                ).addMigrations(MIGRATION_7_8, MIGRATION_8_9)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }

val repositoryModule =
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
        single<VisitorIdProvider> { SharedPreferencesVisitorIdProvider(androidContext()) }
        singleOf(::WordRemoteDataSource)
        singleOf(::TopWordsRemoteDataSource)
        singleOf(::BackendWordStudyRemoteDataSource)
        single { CustomAiWordStudyRemoteDataSource(get(named(CUSTOM_AI_HTTP_CLIENT)), get()) }
        singleOf(::NetworkWordStudyRepository) { bind<WordStudyRepository>() }
        single<AiConfigurationRepository> { AiConfigurationDataStore(androidContext(), get()) }
        singleOf(::WordLocalDataSource)
        single<WordCatalogRepository> { AssetWordCatalogRepository(androidContext(), get()) }
        singleOf(::WordRepository) {
            bind<WordSearchRepository>()
            bind<BookmarkRepository>()
            bind<SearchHistoryRepository>()
            bind<TranslateRepository>()
            bind<TopWordsRepository>()
        }
    }

private const val CUSTOM_AI_HTTP_CLIENT = "customAiHttpClient"
private const val CUSTOM_AI_TIMEOUT_MILLIS = 120_000L
