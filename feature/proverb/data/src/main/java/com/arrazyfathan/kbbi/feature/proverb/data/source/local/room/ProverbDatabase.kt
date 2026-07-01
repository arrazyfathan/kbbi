package com.arrazyfathan.kbbi.feature.proverb.data.source.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity

@Database(
    entities = [
        CachedProverbEntity::class,
        CachedProverbDetailEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class ProverbDatabase : RoomDatabase() {
    abstract fun proverbDao(): ProverbDao
}
