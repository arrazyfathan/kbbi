package com.arrazyfathan.kbbi.feature.wordstudy.data.mapper

import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.BackendAiProviderCatalogDto
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.BackendWordStudyRequestDto
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.BackendWordStudyResultDto
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.WordStudyContentDto
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.WordStudyDefinitionDto
import com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto.WordStudyEntryDto
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel

fun WordStudyRequestModel.toBackendDto() =
    BackendWordStudyRequestDto(
        word = word,
        language = language,
        entries =
            entries.map { entry ->
                WordStudyEntryDto(
                    headword = entry.headword,
                    definitions =
                        entry.definitions.map { definition ->
                            WordStudyDefinitionDto(definition.wordClass, definition.description)
                        },
                )
            },
        provider = provider,
        model = model,
    )

fun BackendAiProviderCatalogDto.toDomain() =
    AiBackendProviderCatalogModel(
        defaultProvider = defaultProvider,
        providers =
            providers.map { provider ->
                AiBackendProviderModel(
                    id = provider.id,
                    defaultModel = provider.defaultModel,
                    models = provider.models,
                )
            },
    )

fun BackendWordStudyResultDto.toDomain() =
    WordStudyModel(
        explanation = explanation,
        examples = examples,
        usageNotes = usageNotes,
        relatedWords = relatedWords,
        providerMode = AiProviderMode.BACKEND,
        provider = provider,
        model = model,
    )

fun WordStudyContentDto.toCustomDomain(
    provider: String,
    model: String,
) = WordStudyModel(
    explanation = explanation,
    examples = examples,
    usageNotes = usageNotes,
    relatedWords = relatedWords,
    providerMode = AiProviderMode.CUSTOM,
    provider = provider,
    model = model,
)
