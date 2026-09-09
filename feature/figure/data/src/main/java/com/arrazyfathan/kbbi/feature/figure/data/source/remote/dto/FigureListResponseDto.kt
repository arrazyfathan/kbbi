package com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FigureListResponseDto(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: FigurePageDto? = null,
)

@Serializable
data class FigurePageDto(
    @SerialName("source")
    val source: String,
    @SerialName("pagination")
    val pagination: FigurePaginationDto,
    @SerialName("items")
    val items: List<FigureDto>,
)

@Serializable
data class FigurePaginationDto(
    @SerialName("page")
    val page: Int,
    @SerialName("limit")
    val limit: Int,
    @SerialName("total")
    val total: Int,
    @SerialName("totalPages")
    val totalPages: Int,
    @SerialName("hasNextPage")
    val hasNextPage: Boolean,
    @SerialName("hasPreviousPage")
    val hasPreviousPage: Boolean,
)
