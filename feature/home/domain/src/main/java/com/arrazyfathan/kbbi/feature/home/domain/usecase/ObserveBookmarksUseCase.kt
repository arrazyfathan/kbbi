package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import kotlinx.coroutines.flow.Flow

class ObserveBookmarksUseCase(
    private val bookmarkRepository: BookmarkRepository,
) {
    operator fun invoke(): Flow<List<ListWordModel>> = bookmarkRepository.getBookmarks()
}
