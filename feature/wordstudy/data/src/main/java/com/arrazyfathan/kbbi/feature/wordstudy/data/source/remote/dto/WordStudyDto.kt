package com.arrazyfathan.kbbi.feature.wordstudy.data.source.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WordStudyDefinitionDto(
    @SerialName("wordClass") val wordClass: String,
    @SerialName("description") val description: String,
)

@Serializable
data class WordStudyEntryDto(
    @SerialName("headword") val headword: String,
    @SerialName("definitions") val definitions: List<WordStudyDefinitionDto>,
)

@Serializable
data class BackendWordStudyRequestDto(
    @SerialName("word") val word: String,
    @SerialName("language") val language: String,
    @SerialName("entries") val entries: List<WordStudyEntryDto>,
    @SerialName("provider") val provider: String? = null,
    @SerialName("model") val model: String? = null,
)

@Serializable
data class BackendAiProviderDto(
    @SerialName("id") val id: String,
    @SerialName("defaultModel") val defaultModel: String,
    @SerialName("models") val models: List<String>,
)

@Serializable
data class BackendAiProviderCatalogDto(
    @SerialName("defaultProvider") val defaultProvider: String? = null,
    @SerialName("providers") val providers: List<BackendAiProviderDto>,
)

@Serializable
data class BackendAiProviderCatalogResponseDto(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: BackendAiProviderCatalogDto? = null,
)

@Serializable
data class WordStudyContentDto(
    @SerialName("explanation") val explanation: String,
    @SerialName("examples") val examples: List<String>,
    @SerialName("usageNotes") val usageNotes: List<String>,
    @SerialName("relatedWords") val relatedWords: List<String>,
)

@Serializable
data class BackendWordStudyResultDto(
    @SerialName("explanation") val explanation: String,
    @SerialName("examples") val examples: List<String>,
    @SerialName("usageNotes") val usageNotes: List<String>,
    @SerialName("relatedWords") val relatedWords: List<String>,
    @SerialName("provider") val provider: String,
    @SerialName("model") val model: String,
)

@Serializable
data class BackendWordStudyResponseDto(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String,
    @SerialName("data") val data: BackendWordStudyResultDto? = null,
)

@Serializable
data class ChatCompletionRequestDto(
    @SerialName("model") val model: String,
    @SerialName("messages") val messages: List<ChatMessageDto>,
)

@Serializable
data class ChatMessageDto(
    @SerialName("role") val role: String,
    @SerialName("content") val content: String,
)

@Serializable
data class ChatCompletionResponseDto(
    @SerialName("choices") val choices: List<ChatChoiceDto> = emptyList(),
)

@Serializable
data class ChatChoiceDto(
    @SerialName("message") val message: ChatMessageDto,
)
