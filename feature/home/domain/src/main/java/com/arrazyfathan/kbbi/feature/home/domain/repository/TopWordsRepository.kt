package com.arrazyfathan.kbbi.feature.home.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel

interface TopWordsRepository {
    suspend fun getTopWords(limit: Int): AppResult<List<TopWordModel>, DataError>
}
