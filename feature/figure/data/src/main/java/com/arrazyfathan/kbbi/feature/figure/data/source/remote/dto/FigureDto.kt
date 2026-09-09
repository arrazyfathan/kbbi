package com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FigureDto(
    @SerialName("name")
    val name: String?,
    @SerialName("slug")
    val slug: String,
    @SerialName("sourceUrl")
    val sourceUrl: String,
    @SerialName("photo")
    val photo: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("quotes")
    val quotes: List<String>? = null,
)
