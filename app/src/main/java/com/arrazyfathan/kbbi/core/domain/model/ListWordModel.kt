package com.arrazyfathan.kbbi.core.domain.model

import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
@Serializable
data class ListWordModel(
    val word: String,
    val listWords: List<WordModel>,
)
