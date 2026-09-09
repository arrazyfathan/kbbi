package com.arrazyfathan.kbbi.feature.figure.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.figure.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto.FigureListResponseDto
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.dto.FigureResponseDto
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigurePageModel
import io.ktor.client.HttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class FigureRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun getFigures(
        page: Int,
        limit: Int,
        query: String,
        includeDetails: Boolean,
    ): AppResult<FigurePageModel, DataError> {
        val route = if (query.isBlank()) FIGURE_ROUTE else FIGURE_SEARCH_ROUTE
        val parameters =
            buildMap<String, Any?> {
                put("page", page)
                put("limit", limit)
                put("includeDetails", includeDetails)
                if (query.isNotBlank()) {
                    put("q", query)
                }
            }

        return when (
            val result =
                httpClient.get<FigureListResponseDto>(
                    route = route,
                    queryParameters = parameters,
                )
        ) {
            is AppResult.Success -> result.data.toFigurePageResult()
            is AppResult.Error -> result
        }
    }

    suspend fun getFigure(slug: String): AppResult<FigureModel, DataError> {
        val result =
            httpClient.get<FigureResponseDto>(
                route = "$FIGURE_ROUTE/${slug.toPathSegment()}",
            )
        return when (result) {
            is AppResult.Success -> result.data.toFigureResult()
            is AppResult.Error -> result
        }
    }

    private fun FigureListResponseDto.toFigurePageResult(): AppResult<FigurePageModel, DataError> =
        when {
            success && data != null -> AppResult.Success(data.toDomain())
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.EmptyBody)
        }

    private fun FigureResponseDto.toFigureResult(): AppResult<FigureModel, DataError> =
        when {
            success && data != null -> AppResult.Success(data.toDomain())
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.NotFound)
        }

    private fun String.toPathSegment(): String =
        URLEncoder.encode(this, StandardCharsets.UTF_8.toString()).replace("+", "%20")

    private companion object {
        const val FIGURE_ROUTE = "/api/v1/figure"
        const val FIGURE_SEARCH_ROUTE = "/api/v1/figure/search"
    }
}
