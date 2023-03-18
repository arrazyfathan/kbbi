package com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
data class WordResponse(
    @SerializedName("lema")
    val entry: String,

    @SerializedName("arti")
    val meanings: List<Meaning>
)