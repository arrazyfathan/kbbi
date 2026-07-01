package com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProverbDetailResponseDto(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: ProverbDetailDto? = null,
)
