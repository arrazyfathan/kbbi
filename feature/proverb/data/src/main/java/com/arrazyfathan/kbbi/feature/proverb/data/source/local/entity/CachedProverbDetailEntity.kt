package com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_proverb_detail_table")
data class CachedProverbDetailEntity(
    @PrimaryKey(autoGenerate = false)
    val slug: String,
    val text: String,
    val letter: String,
    val sourceUrl: String?,
    val meaning: String?,
)
