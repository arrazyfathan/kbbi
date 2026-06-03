package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository

class SaveBookmarkUseCase(
    private val wordRepository: IWordRepository,
) {
    suspend operator fun invoke(
        word: String,
        wordList: List<WordModel>,
    ): Boolean = wordRepository.bookmarkWord(word = word, result = wordList, isSaved = true) != -1L
}
