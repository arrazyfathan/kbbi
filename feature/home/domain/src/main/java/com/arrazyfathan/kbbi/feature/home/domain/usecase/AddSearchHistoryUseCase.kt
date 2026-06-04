package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository

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
