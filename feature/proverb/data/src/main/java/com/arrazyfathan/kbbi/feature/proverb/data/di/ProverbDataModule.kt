package com.arrazyfathan.kbbi.feature.proverb.data.di

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.arrazyfathan.kbbi.feature.proverb.data.NetworkProverbRepository
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.ProverbLocalDataSource
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.ProverbDatabase
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.ProverbRemoteDataSource
import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cached_proverb_detail_table ADD COLUMN aiGenerated INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE cached_proverb_detail_table ADD COLUMN notice TEXT")
    }
}

val proverbDataModule =
    module {
        single {
            Room
                .databaseBuilder(
                    androidContext(),
                    ProverbDatabase::class.java,
                    "proverb_db",
                ).addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
        factory { get<ProverbDatabase>().proverbDao() }
        singleOf(::ProverbLocalDataSource)
        singleOf(::ProverbRemoteDataSource)
        singleOf(::NetworkProverbRepository) {
            bind<ProverbRepository>()
        }
    }
