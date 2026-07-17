package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository

class SearchWordUseCase(
    private val wordSearchRepository: WordSearchRepository,
) {
    suspend operator fun invoke(word: String): AppResult<ListWordModel, DataError> {
        val wordToSearch = word.trim()
        if (wordToSearch.isBlank()) {
            return AppResult.Error(DataError.EmptyQuery)
        }

        return wordSearchRepository
            .getMeaningOfWord(wordToSearch)
    }
}
