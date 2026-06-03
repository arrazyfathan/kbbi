package com.arrazyfathan.kbbi.core.data.source.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
data class WordDto(
    @SerializedName("headword")
    val entry: String,
    @SerializedName("definitions")
    val meanings: List<MeaningDto>,
)
