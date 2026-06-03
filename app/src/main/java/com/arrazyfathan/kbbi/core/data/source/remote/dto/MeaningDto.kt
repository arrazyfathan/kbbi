package com.arrazyfathan.kbbi.core.data.source.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
data class MeaningDto(
    @SerializedName("wordClass")
    val wordClass: String,
    @SerializedName("description")
    val description: String,
)
