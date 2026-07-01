package com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProverbListDto(
    @SerialName("success")
    val success: Boolean,
    @SerialName("message")
    val message: String,
    @SerialName("data")
    val data: ProverbPageDto? = null,
)

@Serializable
data class ProverbPageDto(
    @SerialName("source")
    val source: String,
    @SerialName("pagination")
    val pagination: ProverbPaginationDto,
    @SerialName("items")
    val items: List<ProverbDto>,
)

@Serializable
data class ProverbPaginationDto(
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
