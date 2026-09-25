package com.arrazyfathan.kbbi.feature.wordstudy.domain.model

enum class AiProviderMode {
    BACKEND,
    CUSTOM,
}

data class AiConfigurationModel(
    val providerMode: AiProviderMode = AiProviderMode.BACKEND,
    val backendProviderId: String? = null,
    val backendModel: String? = null,
    val customProviders: List<AiCustomProviderModel> = emptyList(),
    val selectedCustomProviderId: String? = null,
    val revision: Long = 0L,
) {
    val selectedCustomProvider: AiCustomProviderModel?
        get() = customProviders.firstOrNull { it.id == selectedCustomProviderId }

    val isCustomConfigurationComplete: Boolean
        get() = selectedCustomProvider?.isComplete == true
}

data class AiBackendProviderModel(
    val id: String,
    val defaultModel: String,
    val models: List<String>,
)

data class AiBackendProviderCatalogModel(
    val defaultProvider: String?,
    val providers: List<AiBackendProviderModel>,
)

data class AiCustomProviderModel(
    val id: String,
    val name: String,
    val baseUrl: String,
    val models: List<String>,
    val selectedModel: String,
    val hasApiKey: Boolean,
) {
    val isComplete: Boolean
        get() = baseUrl.isNotBlank() && models.isNotEmpty() && selectedModel in models && hasApiKey
}

data class AiCustomCredentialsModel(
    val id: String,
    val name: String,
    val baseUrl: String,
    val model: String,
    val apiKey: String,
)

data class WordStudyDefinitionModel(
    val wordClass: String,
    val description: String,
)

data class WordStudyEntryModel(
    val headword: String,
    val definitions: List<WordStudyDefinitionModel>,
)

data class WordStudySourceModel(
    val word: String,
    val entries: List<WordStudyEntryModel>,
    val aiGenerated: Boolean = false,
)

data class WordStudyRequestModel(
    val word: String,
    val language: String,
    val entries: List<WordStudyEntryModel>,
    val provider: String? = null,
    val model: String? = null,
    val aiGenerated: Boolean = false,
)

data class WordStudyModel(
    val explanation: String,
    val examples: List<String>,
    val usageNotes: List<String>,
    val relatedWords: List<String>,
    val providerMode: AiProviderMode,
    val provider: String,
    val model: String,
)
