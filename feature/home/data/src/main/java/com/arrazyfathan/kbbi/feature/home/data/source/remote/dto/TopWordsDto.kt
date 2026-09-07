package com.arrazyfathan.kbbi.feature.home.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopWordsDto(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: TopWordsDataDto? = null,
)

@Serializable
data class TopWordsDataDto(
    @SerialName("count")
    val count: Int,
    @SerialName("items")
    val items: List<TopWordDto>,
)

@Serializable
data class TopWordDto(
    @SerialName("word")
    val word: String,
    @SerialName("visitorCount")
    val visitorCount: Long,
)
