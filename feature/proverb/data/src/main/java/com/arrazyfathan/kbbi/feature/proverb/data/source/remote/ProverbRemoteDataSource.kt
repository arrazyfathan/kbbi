package com.arrazyfathan.kbbi.feature.proverb.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.proverb.data.mapper.toProverbDetail
import com.arrazyfathan.kbbi.feature.proverb.data.mapper.toProverbPage
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto.ProverbDetailResponseDto
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.dto.ProverbListDto
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbPageModel
import io.ktor.client.HttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class ProverbRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun getProverbs(
        page: Int,
        limit: Int,
        query: String,
    ): AppResult<ProverbPageModel, DataError> {
        val route = if (query.isBlank()) "/proverb" else "/proverb/search"
        val parameters =
            buildMap {
                put("page", page)
                put("limit", limit)
                if (query.isNotBlank()) {
                    put("q", query)
                }
            }

        return when (val result = httpClient.get<ProverbListDto>(route = route, queryParameters = parameters)) {
            is AppResult.Success -> result.data.toProverbPageResult()
            is AppResult.Error -> result
        }
    }

    suspend fun getProverbMeaning(slug: String): AppResult<ProverbDetailModel, DataError> =
        when (val result = httpClient.get<ProverbDetailResponseDto>(route = "/proverb/${slug.toPathSegment()}")) {
            is AppResult.Success -> result.data.toProverbDetailResult()
            is AppResult.Error -> result
        }

    private fun ProverbListDto.toProverbPageResult(): AppResult<ProverbPageModel, DataError> =
        when {
            success && data != null -> AppResult.Success(data.toProverbPage())
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.EmptyBody)
        }

    private fun ProverbDetailResponseDto.toProverbDetailResult(): AppResult<ProverbDetailModel, DataError> =
        when {
            success && data != null -> AppResult.Success(data.toProverbDetail())
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.NotFound)
        }

    private fun String.toPathSegment(): String =
        URLEncoder
            .encode(this, StandardCharsets.UTF_8.toString())
            .replace("+", "%20")
}
