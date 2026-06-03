package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository

class AddSearchHistoryUseCase(
    private val wordRepository: IWordRepository,
) {
    suspend operator fun invoke(word: String) {
        val historyWord = word.trim().lowercase()
        if (historyWord.isNotBlank()) {
            wordRepository.addToHistory(HistoryModel(historyWord))
        }
    }
}
