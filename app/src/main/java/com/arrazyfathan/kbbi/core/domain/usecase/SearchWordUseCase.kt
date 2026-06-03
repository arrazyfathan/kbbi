package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.map
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SearchWordUseCase(
    private val wordRepository: IWordRepository,
) {
    operator fun invoke(word: String): Flow<AppResult<ListWordModel, DataError>> {
        val wordToSearch = word.trim()
        if (wordToSearch.isBlank()) {
            return flowOf(AppResult.Error(DataError.EmptyQuery))
        }

        return wordRepository.getMeaningOfWord(wordToSearch).map { result ->
            result.map { words ->
                ListWordModel(
                    word = wordToSearch,
                    listWords = words,
                )
            }
        }
    }
}
