package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.repository.BookmarkRepository

class SaveBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(
        word: String,
        wordList: List<WordModel>,
    ): Boolean = bookmarkRepository.bookmarkWord(word = word, result = wordList)
}
