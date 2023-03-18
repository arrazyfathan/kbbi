package com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

@Entity(tableName = "word_table")
data class ListWordEntity(
    val word: String = "",
    val listWords: List<WordEntity>,
    var isSaved: Boolean = false,
) {
    @PrimaryKey(autoGenerate = false)
    var id: String = word
}
