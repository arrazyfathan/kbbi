package com.arrazyfathan.kbbi.feature.home.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
@Entity(tableName = "history_table")
@Serializable
data class HistoryEntity(
    @PrimaryKey(autoGenerate = false)
    var word: String = "",
    val searchedAt: Long = System.currentTimeMillis(),
)
