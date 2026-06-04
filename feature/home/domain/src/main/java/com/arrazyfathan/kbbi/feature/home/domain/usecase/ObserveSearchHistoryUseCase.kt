package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveSearchHistoryUseCase(
    private val searchHistoryRepository: SearchHistoryRepository,
) {
    operator fun invoke(): Flow<List<HistoryModel>> = searchHistoryRepository.getAllHistories()
}
