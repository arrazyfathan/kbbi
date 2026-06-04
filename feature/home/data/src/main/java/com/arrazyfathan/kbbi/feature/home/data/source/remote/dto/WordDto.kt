package com.arrazyfathan.kbbi.feature.home.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
@Serializable
data class WordDto(
    @SerialName("headword")
    val entry: String,
    @SerialName("definitions")
    val meanings: List<MeaningDto>,
)
