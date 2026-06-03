package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveSearchHistoryUseCase(
    private val searchHistoryRepository: SearchHistoryRepository,
) {
    operator fun invoke(): Flow<List<HistoryModel>> = searchHistoryRepository.getAllHistories()
}
