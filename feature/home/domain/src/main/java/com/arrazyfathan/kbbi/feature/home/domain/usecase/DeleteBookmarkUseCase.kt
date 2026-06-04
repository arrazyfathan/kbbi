package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository

class DeleteBookmarkUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    suspend operator fun invoke(word: String) {
        bookmarkRepository.deleteWord(word)
    }
}
