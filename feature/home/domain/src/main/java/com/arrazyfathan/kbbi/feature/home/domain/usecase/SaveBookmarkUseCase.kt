package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository

class SaveBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(
        word: String,
        wordList: List<WordModel>,
        visitorCount: Int? = null,
    ): Boolean =
        bookmarkRepository.bookmarkWord(
            word = word,
            result = wordList,
            visitorCount = visitorCount,
        )
}
