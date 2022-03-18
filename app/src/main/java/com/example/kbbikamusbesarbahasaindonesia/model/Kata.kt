package com.example.kbbikamusbesarbahasaindonesia.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "table_kata")
@Parcelize
data class Kata(
    val kata: String = "",
    val data: List<Data>,
    val message: String,
    val status: Boolean,
    var isSaved: Boolean = false
) : Parcelable {
    @PrimaryKey(autoGenerate = false)
    var id: String = kata
}