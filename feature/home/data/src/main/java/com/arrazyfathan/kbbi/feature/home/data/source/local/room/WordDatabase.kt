package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity

@Database(
    entities = [
        ListWordEntity::class, HistoryEntity::class,
    ],
    version = 9,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
