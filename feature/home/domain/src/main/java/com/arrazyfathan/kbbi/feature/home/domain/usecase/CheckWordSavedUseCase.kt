package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow

class CheckWordSavedUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(word: String): Flow<Boolean> = bookmarkRepository.checkIfWordIsSaved(word)
}
