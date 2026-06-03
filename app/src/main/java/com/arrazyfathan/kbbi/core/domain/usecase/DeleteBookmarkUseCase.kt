package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository

class DeleteBookmarkUseCase(
    private val wordRepository: IWordRepository,
) {
    suspend operator fun invoke(word: String) {
        wordRepository.deleteWord(word)
    }
}
