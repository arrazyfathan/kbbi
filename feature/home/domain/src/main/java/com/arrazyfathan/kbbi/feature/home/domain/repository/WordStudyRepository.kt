package com.arrazyfathan.kbbi.feature.home.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.home.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordStudyRequestModel

interface WordStudyRepository {
    suspend fun getBackendProviders(): AppResult<AiBackendProviderCatalogModel, DataError>

    suspend fun generateWithBackend(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError>

    suspend fun generateWithCustomProvider(
        request: WordStudyRequestModel,
        credentials: AiCustomCredentialsModel,
    ): AppResult<WordStudyModel, DataError>
}
