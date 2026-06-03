package com.arrazyfathan.kbbi.core.data.source.remote

import com.arrazyfathan.kbbi.core.data.source.remote.dto.ListWordDto
import com.arrazyfathan.kbbi.core.data.source.remote.dto.WordDto
import com.arrazyfathan.kbbi.core.data.source.remote.network.ApiService
import com.arrazyfathan.kbbi.core.data.source.remote.network.safeApiCall
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class RemoteDataSource(
    private val apiService: ApiService,
) {
    suspend fun getMeaningOfWord(word: String): AppResult<List<WordDto>, DataError> =
        when (val result = safeApiCall { apiService.getMeaningWord(word) }) {
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
