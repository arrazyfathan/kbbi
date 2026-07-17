package com.arrazyfathan.kbbi.feature.home.data.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arrazyfathan.kbbi.feature.home.data.WordRepository
import com.arrazyfathan.kbbi.feature.home.data.source.local.AssetWordCatalogRepository
import com.arrazyfathan.kbbi.feature.home.data.source.local.WordLocalDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDatabase
import com.arrazyfathan.kbbi.feature.home.data.source.remote.SharedPreferencesVisitorIdProvider
import com.arrazyfathan.kbbi.feature.home.data.source.remote.VisitorIdProvider
import com.arrazyfathan.kbbi.feature.home.data.source.remote.WordRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
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
        single<VisitorIdProvider> { SharedPreferencesVisitorIdProvider(androidContext()) }
        singleOf(::WordRemoteDataSource)
        singleOf(::WordLocalDataSource)
        single<WordCatalogRepository> { AssetWordCatalogRepository(androidContext(), get()) }
        singleOf(::WordRepository) {
            bind<WordSearchRepository>()
            bind<BookmarkRepository>()
            bind<SearchHistoryRepository>()
        }
    }
