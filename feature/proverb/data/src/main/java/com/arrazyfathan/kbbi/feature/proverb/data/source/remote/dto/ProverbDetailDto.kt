package com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProverbDetailDto(
    @SerialName("text")
    val text: String,
    @SerialName("letter")
    val letter: String,
    @SerialName("slug")
    val slug: String,
    @SerialName("sourceUrl")
    val sourceUrl: String? = null,
    @SerialName("meaning")
    val meaning: String? = null,
)
