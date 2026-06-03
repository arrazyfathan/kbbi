package com.arrazyfathan.kbbi.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

@Entity(tableName = "word_table")
@Serializable
data class ListWordEntity(
    @PrimaryKey(autoGenerate = false)
    val word: String = "",
    val listWords: List<WordEntity>,
    var isSaved: Boolean = false,
)
