package com.arrazyfathan.kbbi.feature.wordstudy.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import kotlinx.coroutines.flow.Flow

interface AiConfigurationRepository {
    val configuration: Flow<AiConfigurationModel>

    suspend fun saveCustomProvider(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ): AppResult<String, DataError>

    suspend fun selectProviderMode(mode: AiProviderMode): AppResult<Unit, DataError>

    suspend fun selectBackendProvider(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError>

    suspend fun selectCustomProvider(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError>

    suspend fun removeCustomProvider(providerId: String): AppResult<Unit, DataError>

    suspend fun getCustomCredentials(providerId: String? = null): AppResult<AiCustomCredentialsModel, DataError>
}
