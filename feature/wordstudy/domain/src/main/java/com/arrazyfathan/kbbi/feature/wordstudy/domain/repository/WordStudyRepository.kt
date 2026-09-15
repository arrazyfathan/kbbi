package com.arrazyfathan.kbbi.feature.wordstudy.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel

interface WordStudyRepository {
    suspend fun getBackendProviders(): AppResult<AiBackendProviderCatalogModel, DataError>

    suspend fun generateWithBackend(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError>

    suspend fun generateWithCustomProvider(
        request: WordStudyRequestModel,
        credentials: AiCustomCredentialsModel,
    ): AppResult<WordStudyModel, DataError>
}
