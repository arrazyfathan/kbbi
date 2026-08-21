package com.arrazyfathan.kbbi.feature.home.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel

interface TranslateRepository {
    suspend fun getTranslation(word: String): AppResult<TranslateModel, DataError>
}
