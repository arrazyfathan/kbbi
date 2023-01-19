package com.example.kbbikamusbesarbahasaindonesia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by Ar Razy Fathan Rabbani on 18/01/23.
 */

@Entity(tableName = "table_history")
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var kata: String = ""
)
