package com.arrazyfathan.kbbi.core.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import kotlinx.coroutines.flow.Flow

interface SearchHistoryRepository {
    suspend fun addToHistory(history: HistoryModel)

    fun getAllHistories(): Flow<List<HistoryModel>>
}
