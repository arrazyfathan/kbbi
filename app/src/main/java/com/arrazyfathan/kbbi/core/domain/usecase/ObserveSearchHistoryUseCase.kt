package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository
import kotlinx.coroutines.flow.Flow

class ObserveSearchHistoryUseCase(
    private val wordRepository: IWordRepository,
) {
    operator fun invoke(): Flow<List<HistoryModel>> = wordRepository.getAllHistories()
}
