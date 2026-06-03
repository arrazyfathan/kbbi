package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchWordWithHistoryUseCase(
    private val searchWord: SearchWordUseCase,
    private val addSearchHistory: AddSearchHistoryUseCase,
) {
    operator fun invoke(word: String): Flow<AppResult<ListWordModel, DataError>> =
        searchWord(word).map { result ->
            if (result is AppResult.Success) {
                addSearchHistory(result.data.word)
            }
            result
        }
}
