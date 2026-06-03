package com.arrazyfathan.kbbi.core.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.WordModel

interface WordSearchRepository {
    suspend fun getMeaningOfWord(word: String): AppResult<List<WordModel>, DataError>
}
