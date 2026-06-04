package com.arrazyfathan.kbbi.feature.home.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
@Serializable
data class MeaningDto(
    @SerialName("wordClass")
    val wordClass: String,
    @SerialName("description")
    val description: String,
)
