package com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity

import androidx.room.Entity

@Entity(
    tableName = "cached_proverb_table",
    primaryKeys = ["query", "slug"],
)
data class CachedProverbEntity(
    val query: String,
    val page: Int,
    val position: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
    val text: String,
    val letter: String,
    val slug: String,
    val sourceUrl: String?,
)
