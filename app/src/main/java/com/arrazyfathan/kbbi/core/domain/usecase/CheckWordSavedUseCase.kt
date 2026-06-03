package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow

class CheckWordSavedUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(word: String): Flow<Boolean> = bookmarkRepository.checkIfWordIsSaved(word)
}
