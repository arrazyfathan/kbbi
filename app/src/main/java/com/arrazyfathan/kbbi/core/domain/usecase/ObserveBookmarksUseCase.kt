package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow

class ObserveBookmarksUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(): Flow<List<ListWordModel>> = bookmarkRepository.getBookmarks()
}
