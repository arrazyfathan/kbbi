package com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyDefinitionModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyEntryModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudySourceModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository

class GetAiProvidersUseCase(
    private val repository: WordStudyRepository,
) {
    suspend operator fun invoke(): AppResult<AiBackendProviderCatalogModel, DataError> =
        repository.getBackendProviders()
}

class SaveAiConfigurationUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ): AppResult<String, DataError> {
        val normalizedUrl = normalizeAiBaseUrl(baseUrl) ?: return AppResult.Error(DataError.BadRequest)
        val normalizedModels = models.map(String::trim).filter(String::isNotBlank).distinct()
        val normalizedSelectedModel = selectedModel.trim()
        if (
            normalizedModels.isEmpty() ||
            normalizedModels.size > MAX_CUSTOM_MODELS ||
            normalizedModels.any { it.length > MAX_MODEL_LENGTH } ||
            normalizedSelectedModel !in normalizedModels ||
            apiKey?.isBlank() == true
        ) {
            return AppResult.Error(DataError.BadRequest)
        }
        val resolvedName = name.trim().ifBlank { providerDomainName(normalizedUrl) }
        if (resolvedName.isBlank() || resolvedName.length > MAX_PROVIDER_NAME_LENGTH) {
            return AppResult.Error(DataError.BadRequest)
        }
        return repository.saveCustomProvider(
            providerId = providerId,
            name = resolvedName,
            baseUrl = normalizedUrl,
            models = normalizedModels,
            selectedModel = normalizedSelectedModel,
            apiKey = apiKey?.trim(),
        )
    }
}

class SelectAiProviderModeUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(mode: AiProviderMode): AppResult<Unit, DataError> = repository.selectProviderMode(mode)
}

class SelectBackendAiProviderUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError> = repository.selectBackendProvider(providerId, model)
}

class SelectCustomAiProviderUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError> = repository.selectCustomProvider(providerId, model)
}

class RemoveAiConfigurationUseCase(
    private val repository: AiConfigurationRepository,
) {
    suspend operator fun invoke(providerId: String): AppResult<Unit, DataError> =
        repository.removeCustomProvider(providerId)
}

class TestAiConnectionUseCase(
    private val wordStudyRepository: WordStudyRepository,
    private val configurationRepository: AiConfigurationRepository,
) {
    suspend operator fun invoke(
        providerId: String?,
        name: String,
        baseUrl: String,
        model: String,
        apiKey: String,
    ): AppResult<Unit, DataError> {
        val normalizedUrl = normalizeAiBaseUrl(baseUrl) ?: return AppResult.Error(DataError.BadRequest)
        if (model.isBlank()) return AppResult.Error(DataError.BadRequest)
        val sample =
            WordStudySourceModel(
                word = "bahasa",
                entries =
                    listOf(
                        WordStudyEntryModel(
                            "bahasa",
                            listOf(WordStudyDefinitionModel("n", "sistem lambang bunyi")),
                        ),
                    ),
            )
        val resolvedApiKey =
            apiKey.trim().ifBlank {
                val selectedCredentials = configurationRepository.getCustomCredentials(providerId)
                if (selectedCredentials is AppResult.Success) {
                    selectedCredentials.data.apiKey
                } else {
                    return AppResult.Error(DataError.BadRequest)
                }
            }
        val credentials =
            AiCustomCredentialsModel(
                id = providerId.orEmpty(),
                name = name.trim().ifBlank { providerDomainName(normalizedUrl) },
                baseUrl = normalizedUrl,
                model = model.trim(),
                apiKey = resolvedApiKey,
            )
        val request = sample.toWordStudyRequest("id") ?: return AppResult.Error(DataError.BadRequest)
        return when (val result = wordStudyRepository.generateWithCustomProvider(request, credentials)) {
            is AppResult.Success -> AppResult.Success(Unit)
            is AppResult.Error -> result
        }
    }
}

internal fun normalizeAiBaseUrl(value: String): String? {
    val trimmed = value.trim().trimEnd('/')
    val uri = runCatching { java.net.URI(trimmed) }.getOrNull() ?: return null
    if (
        uri.scheme?.lowercase() != "https" ||
        uri.host.isNullOrBlank() ||
        uri.userInfo != null ||
        uri.query != null ||
        uri.fragment != null ||
        uri.path
            .orEmpty()
            .trimEnd('/')
            .endsWith("/chat/completions", ignoreCase = true)
    ) {
        return null
    }
    return trimmed
}

internal fun providerDomainName(baseUrl: String): String =
    runCatching {
        java.net
            .URI(baseUrl)
            .host
            .orEmpty()
            .removePrefix("www.")
    }.getOrDefault("")

private const val MAX_CUSTOM_MODELS = 20
private const val MAX_MODEL_LENGTH = 200
private const val MAX_PROVIDER_NAME_LENGTH = 80
