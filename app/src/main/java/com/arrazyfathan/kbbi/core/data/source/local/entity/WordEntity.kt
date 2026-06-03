package com.arrazyfathan.kbbi.core.data.source.local.entity

import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
@Serializable
data class WordEntity(
    val entry: String,
    val meanings: List<MeaningEntity>,
)
