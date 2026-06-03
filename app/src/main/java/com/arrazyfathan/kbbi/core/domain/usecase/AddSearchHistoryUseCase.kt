package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.repository.SearchHistoryRepository

class AddSearchHistoryUseCase(
    private val searchHistoryRepository: SearchHistoryRepository,
) {
    suspend operator fun invoke(word: String) {
        val historyWord = word.trim().lowercase()
        if (historyWord.isNotBlank()) {
            searchHistoryRepository.addToHistory(HistoryModel(historyWord))
        }
    }
}
