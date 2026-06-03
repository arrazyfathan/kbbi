package com.arrazyfathan.kbbi.core.data.source.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
data class ListWordDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: List<WordDto>,
)
