package com.arrazyfathan.kbbi.feature.home.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Locally cached "top words" entry so the home screen can render instantly (offline-first) while a
 * fresh request is in flight. [position] preserves the server ordering for ranked display.
 */
@Entity(tableName = "cached_top_word_table")
data class CachedTopWordEntity(
    @PrimaryKey(autoGenerate = false)
    val word: String,
    val visitorCount: Long,
    val position: Int,
    val cachedAt: Long = System.currentTimeMillis(),
)
