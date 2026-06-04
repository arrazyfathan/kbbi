package com.arrazyfathan.kbbi.core.data.source.remote

import com.arrazyfathan.kbbi.core.data.source.remote.dto.ListWordDto
import com.arrazyfathan.kbbi.core.data.source.remote.dto.WordDto
import com.arrazyfathan.kbbi.core.data.source.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import io.ktor.client.HttpClient

class WordRemoteDataSource(
    private val httpClient: HttpClient,
) {
    suspend fun getMeaningOfWord(word: String): AppResult<List<WordDto>, DataError> =
        when (val result = httpClient.get<ListWordDto>(route = "/search/$word")) {
            is AppResult.Success -> result.data.toWordResult()
            is AppResult.Error -> result
        }

    private fun ListWordDto.toWordResult(): AppResult<List<WordDto>, DataError> {
        val words = data
        return when {
            success && words.isNotEmpty() -> AppResult.Success(words)
            !success -> AppResult.Error(DataError.Remote(message))
            else -> AppResult.Error(DataError.NotFound)
        }
    }
}
