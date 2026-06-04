package com.arrazyfathan.kbbi.core.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arrazyfathan.kbbi.core.data.WordRepository
import com.arrazyfathan.kbbi.core.data.source.local.AssetWordCatalogRepository
import com.arrazyfathan.kbbi.core.data.source.local.WordLocalDataSource
import com.arrazyfathan.kbbi.core.data.source.local.room.WordDatabase
import com.arrazyfathan.kbbi.core.data.source.remote.WordRemoteDataSource
import com.arrazyfathan.kbbi.core.data.source.remote.network.HttpClientFactory
import com.arrazyfathan.kbbi.core.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.core.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.core.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.core.domain.repository.WordSearchRepository
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
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
            HttpClientFactory(get()).build()
        }
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
