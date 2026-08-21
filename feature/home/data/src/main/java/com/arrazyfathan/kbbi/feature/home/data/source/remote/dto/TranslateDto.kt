package com.arrazyfathan.kbbi.feature.home.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 21/08/23.
 */
@Serializable
data class TranslateDto(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: TranslateDataDto? = null,
)

@Serializable
data class TranslateDataDto(
    @SerialName("word")
    val word: String,
    @SerialName("translation")
    val translation: String,
    @SerialName("from")
    val from: String,
    @SerialName("to")
    val to: String,
    @SerialName("entries")
    val entries: List<TranslatedEntryDto> = emptyList(),
)

@Serializable
data class TranslatedEntryDto(
    @SerialName("headword")
    val headword: String,
    @SerialName("definitions")
    val definitions: List<TranslatedDefinitionDto> = emptyList(),
)

@Serializable
data class TranslatedDefinitionDto(
    @SerialName("wordClass")
    val wordClass: String,
    @SerialName("description")
    val description: String,
    @SerialName("translation")
    val translation: String,
)
