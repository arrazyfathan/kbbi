package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel

class SearchWordWithHistoryUseCase(
    private val searchWord: SearchWordUseCase,
    private val addSearchHistory: AddSearchHistoryUseCase,
) {
    suspend operator fun invoke(word: String): AppResult<ListWordModel, DataError> =
        searchWord(word).also { result ->
            if (result is AppResult.Success) {
                addSearchHistory(result.data.word)
            }
        }
}
