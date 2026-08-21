package com.arrazyfathan.kbbi.feature.home.domain.model

import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 21/08/23.
 */
@Serializable
data class TranslateModel(
    val word: String,
    val translation: String,
    val from: String,
    val to: String,
    val entries: List<TranslatedWordModel>,
)

@Serializable
data class TranslatedWordModel(
    val headword: String,
    val meanings: List<TranslatedMeaningModel>,
)

@Serializable
data class TranslatedMeaningModel(
    val wordClass: String,
    val description: String,
    val translation: String,
)
