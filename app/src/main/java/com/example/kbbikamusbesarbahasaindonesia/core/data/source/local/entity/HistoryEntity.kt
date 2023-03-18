package com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = false)
    var word: String = "",
)
