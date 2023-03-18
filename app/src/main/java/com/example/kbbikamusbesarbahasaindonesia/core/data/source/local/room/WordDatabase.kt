package com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.ListWordEntity

@Database(
    entities = [
        ListWordEntity::class, HistoryEntity::class,
    ],
    version = 7,
)
@TypeConverters(Converters::class)
abstract class WordDatabase : RoomDatabase() {
    abstract fun wordDao(): WordDao
}
