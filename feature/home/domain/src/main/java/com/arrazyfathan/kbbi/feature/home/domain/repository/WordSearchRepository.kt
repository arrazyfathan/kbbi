package com.arrazyfathan.kbbi.feature.home.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel

interface WordSearchRepository {
    suspend fun getMeaningOfWord(word: String): AppResult<List<WordModel>, DataError>
}
