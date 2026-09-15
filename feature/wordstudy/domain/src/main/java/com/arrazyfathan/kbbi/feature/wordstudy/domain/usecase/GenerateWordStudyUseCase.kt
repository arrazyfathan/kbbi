package com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyDefinitionModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyEntryModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudySourceModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository
import kotlinx.coroutines.flow.first

class GenerateWordStudyUseCase(
    private val configurationRepository: AiConfigurationRepository,
    private val wordStudyRepository: WordStudyRepository,
) {
    suspend operator fun invoke(
        word: WordStudySourceModel,
        language: String,
    ): AppResult<WordStudyModel, DataError> {
        val request = word.toWordStudyRequest(language) ?: return AppResult.Error(DataError.BadRequest)
        val configuration = configurationRepository.configuration.first()
        return when (configuration.providerMode) {
            AiProviderMode.BACKEND -> {
                wordStudyRepository.generateWithBackend(
                    request.copy(
                        provider = configuration.backendProviderId,
                        model = configuration.backendModel,
                    ),
                )
            }

            AiProviderMode.CUSTOM -> {
                when (val credentials = configurationRepository.getCustomCredentials()) {
                    is AppResult.Success -> wordStudyRepository.generateWithCustomProvider(request, credentials.data)
                    is AppResult.Error -> credentials
                }
            }
        }
    }
}

internal fun WordStudySourceModel.toWordStudyRequest(language: String): WordStudyRequestModel? {
    val normalizedWord = word.trim()
    val normalizedLanguage = language.lowercase().takeIf { it == "id" || it == "en" } ?: return null
    if (normalizedWord.isBlank() || normalizedWord.length > 100 || entries.isEmpty() ||
        entries.size > 10
    ) {
        return null
    }

    var combinedDescriptionLength = 0
    val entries =
        entries.map { entry ->
            val headword = entry.headword.trim()
            if (
                headword.isBlank() || headword.length > 100 || entry.definitions.isEmpty() ||
                entry.definitions.size > 20
            ) {
                return null
            }
            val definitions =
                entry.definitions.map { definition ->
                    val wordClass = definition.wordClass.trim()
                    val description = definition.description.replace("--", headword).trim()
                    combinedDescriptionLength += description.length
                    if (wordClass.length > 50 || description.isBlank() || description.length > 1_000) return null
                    WordStudyDefinitionModel(wordClass, description)
                }
            WordStudyEntryModel(headword, definitions)
        }
    if (combinedDescriptionLength > 12_000) return null
    return WordStudyRequestModel(normalizedWord, normalizedLanguage, entries)
}
