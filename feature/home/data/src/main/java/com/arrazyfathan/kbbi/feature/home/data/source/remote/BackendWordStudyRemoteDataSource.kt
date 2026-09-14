package com.arrazyfathan.kbbi.feature.home.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.data.remote.network.post
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.data.mapper.toBackendDto
import com.arrazyfathan.kbbi.feature.home.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.BackendAiProviderCatalogResponseDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.BackendWordStudyResponseDto
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.BackendWordStudyResultDto
import com.arrazyfathan.kbbi.feature.home.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordStudyRequestModel
import io.ktor.client.HttpClient

class BackendWordStudyRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun getProviders(): AppResult<AiBackendProviderCatalogModel, DataError> =
        when (
            val result = httpClient.get<BackendAiProviderCatalogResponseDto>(route = "/api/v1/ai/providers")
        ) {
            is AppResult.Error -> {
                result
            }

            is AppResult.Success -> {
                val response = result.data
                when {
                    !response.success -> AppResult.Error(DataError.Remote(response.message))
                    response.data == null -> AppResult.Error(DataError.EmptyBody)
                    else -> AppResult.Success(response.data.toDomain())
                }
            }
        }

    suspend fun generate(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError> =
        when (
            val result =
                httpClient.post<_, BackendWordStudyResponseDto>(
                    route = "/api/v1/ai/word-study",
                    body = request.toBackendDto(),
                )
        ) {
            is AppResult.Error -> {
                result
            }

            is AppResult.Success -> {
                val response = result.data
                when {
                    !response.success -> AppResult.Error(DataError.Remote(response.message))
                    response.data == null -> AppResult.Error(DataError.EmptyBody)
                    !response.data.hasValidContent(request.word) -> AppResult.Error(DataError.Serialization)
                    else -> AppResult.Success(response.data.toDomain())
                }
            }
        }
}

internal fun BackendWordStudyResultDto.hasValidContent(word: String): Boolean =
    explanation.isNotBlank() &&
        examples.size >= 2 && examples.all(String::isNotBlank) &&
        usageNotes.size >= 2 && usageNotes.all(String::isNotBlank) &&
        relatedWords.hasValidRelatedWords(word)

internal fun List<String>.hasValidRelatedWords(word: String): Boolean {
    if (size < 3 || any(String::isBlank)) return false
    val normalized = map { it.trim().lowercase() }
    return word.trim().lowercase() !in normalized && normalized.distinct().size == normalized.size
}
