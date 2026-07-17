package com.arrazyfathan.kbbi.feature.home.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.home.data.source.remote.dto.ListWordDto
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import io.ktor.client.HttpClient

private const val VISITOR_ID_HEADER = "x-visitor-id"

class WordRemoteDataSource(
    private val httpClient: HttpClient,
    private val visitorIdProvider: VisitorIdProvider,
) {
    suspend fun getMeaningOfWord(word: String): AppResult<ListWordModel, DataError> =
        when (
            val result =
                httpClient.get<ListWordDto>(
                    route = "/search/$word",
                    headers = mapOf(VISITOR_ID_HEADER to visitorIdProvider.getVisitorId()),
                )
        ) {
            is AppResult.Success -> result.data.toWordResult()
            is AppResult.Error -> result
        }

    private fun ListWordDto.toWordResult(): AppResult<ListWordModel, DataError> {
        val searchData = data
        val words = searchData?.entries.orEmpty()
        return when {
            success && searchData != null && words.isNotEmpty() ->
                AppResult.Success(
                    ListWordModel(
                        word = searchData.word,
                        listWords = words.map { it.toDomain() },
                        visitorCount = searchData.visitorCount,
                    ),
                )
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.NotFound)
        }
    }
}
