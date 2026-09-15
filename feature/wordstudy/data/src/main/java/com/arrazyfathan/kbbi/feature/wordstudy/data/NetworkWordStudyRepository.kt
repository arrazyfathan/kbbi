package com.arrazyfathan.kbbi.feature.wordstudy.data

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.BackendWordStudyRemoteDataSource
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.CustomAiWordStudyRemoteDataSource
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository

class NetworkWordStudyRepository(
    private val backendDataSource: BackendWordStudyRemoteDataSource,
    private val customDataSource: CustomAiWordStudyRemoteDataSource,
) : WordStudyRepository {
    override suspend fun getBackendProviders(): AppResult<AiBackendProviderCatalogModel, DataError> =
        backendDataSource.getProviders()

    override suspend fun generateWithBackend(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError> =
        backendDataSource.generate(request)

    override suspend fun generateWithCustomProvider(
        request: WordStudyRequestModel,
        credentials: AiCustomCredentialsModel,
    ): AppResult<WordStudyModel, DataError> = customDataSource.generate(request, credentials)
}
