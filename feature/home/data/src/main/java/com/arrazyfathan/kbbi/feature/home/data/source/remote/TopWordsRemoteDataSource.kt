package com.arrazyfathan.kbbi.feature.home.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.TopWordsDto
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel
import io.ktor.client.HttpClient

class TopWordsRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun getTopWords(limit: Int): AppResult<List<TopWordModel>, DataError> =
        when (
            val result =
                httpClient.get<TopWordsDto>(
                    route = "/api/v1/words/top",
                    queryParameters = mapOf("limit" to limit),
                )
        ) {
            is AppResult.Success -> result.data.toTopWordsResult()
            is AppResult.Error -> result
        }

    private fun TopWordsDto.toTopWordsResult(): AppResult<List<TopWordModel>, DataError> =
        when {
            success && data != null -> AppResult.Success(data.items.map { it.toDomain() })
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.EmptyBody)
        }
}
