package com.arrazyfathan.kbbi.feature.home.data.source.local.entity

import kotlinx.serialization.Serializable

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
@Serializable
data class MeaningEntity(
    val wordClass: String,
    val description: String,
)
