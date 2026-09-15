package com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomProviderModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyDefinitionModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyEntryModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudySourceModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenerateWordStudyUseCaseTest {
    @Test
    fun `backend is the default and only backend is called`() =
        runBlocking {
            val configuration = FakeConfigurationRepository()
            val repository = FakeStudyRepository()

            val result = GenerateWordStudyUseCase(configuration, repository)(sampleWord(), "id")

            assertTrue(result is AppResult.Success)
            assertEquals(1, repository.backendCalls)
            assertEquals(0, repository.customCalls)
        }

    @Test
    fun `backend selection is included in generation request`() =
        runBlocking {
            val configuration =
                FakeConfigurationRepository(
                    AiConfigurationModel(
                        backendProviderId = "gemini",
                        backendModel = "gemini-2.5-flash",
                    ),
                )
            val repository = FakeStudyRepository()

            GenerateWordStudyUseCase(configuration, repository)(sampleWord(), "id")

            assertEquals("gemini", repository.lastRequest?.provider)
            assertEquals("gemini-2.5-flash", repository.lastRequest?.model)
        }

    @Test
    fun `custom error is returned without backend fallback`() =
        runBlocking {
            val configuration =
                FakeConfigurationRepository(
                    AiConfigurationModel(
                        providerMode = AiProviderMode.CUSTOM,
                        customProviders =
                            listOf(
                                AiCustomProviderModel(
                                    id = "custom",
                                    name = "Example",
                                    baseUrl = "https://example.com/v1",
                                    models = listOf("model", "model-fast"),
                                    selectedModel = "model",
                                    hasApiKey = true,
                                ),
                            ),
                        selectedCustomProviderId = "custom",
                    ),
                )
            val repository = FakeStudyRepository(customResult = AppResult.Error(DataError.Unauthorized))

            val result = GenerateWordStudyUseCase(configuration, repository)(sampleWord(), "en")

            assertEquals(DataError.Unauthorized, (result as AppResult.Error).error)
            assertEquals(0, repository.backendCalls)
            assertEquals(1, repository.customCalls)
        }

    @Test
    fun `request normalizes repetition placeholder and preserves empty word class`() =
        runBlocking {
            val repository = FakeStudyRepository()

            GenerateWordStudyUseCase(FakeConfigurationRepository(), repository)(
                WordStudySourceModel(
                    "batas",
                    listOf(WordStudyEntryModel("batas", listOf(WordStudyDefinitionModel("  ", "tanah --")))),
                ),
                "id",
            )

            val definition =
                repository.lastRequest!!
                    .entries
                    .single()
                    .definitions
                    .single()
            assertEquals("", definition.wordClass)
            assertEquals("tanah batas", definition.description)
        }

    @Test
    fun `invalid language is rejected without calling a source`() =
        runBlocking {
            val repository = FakeStudyRepository()

            val result = GenerateWordStudyUseCase(FakeConfigurationRepository(), repository)(sampleWord(), "fr")

            assertEquals(DataError.BadRequest, (result as AppResult.Error).error)
            assertEquals(0, repository.backendCalls + repository.customCalls)
        }

    @Test
    fun `empty or oversized source is rejected without calling a source`() =
        runBlocking {
            val repository = FakeStudyRepository()
            val emptyResult = GenerateWordStudyUseCase(FakeConfigurationRepository(), repository)(
                WordStudySourceModel("bahasa", emptyList()),
                "id",
            )
            val oversizedResult = GenerateWordStudyUseCase(FakeConfigurationRepository(), repository)(
                WordStudySourceModel(
                    "bahasa",
                    (1..11).map { WordStudyEntryModel("word$it", listOf(WordStudyDefinitionModel("n", "meaning"))) },
                ),
                "id",
            )

            assertEquals(DataError.BadRequest, (emptyResult as AppResult.Error).error)
            assertEquals(DataError.BadRequest, (oversizedResult as AppResult.Error).error)
            assertEquals(0, repository.backendCalls + repository.customCalls)
        }

    private fun sampleWord() =
        WordStudySourceModel(
            "bahasa",
            listOf(WordStudyEntryModel("bahasa", listOf(WordStudyDefinitionModel("n", "sistem lambang bunyi")))),
        )
}

private class FakeConfigurationRepository(
    initial: AiConfigurationModel = AiConfigurationModel(),
) : AiConfigurationRepository {
    override val configuration: Flow<AiConfigurationModel> = MutableStateFlow(initial)

    override suspend fun saveCustomProvider(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ) = AppResult.Success(providerId ?: "custom")

    override suspend fun selectProviderMode(mode: AiProviderMode) = AppResult.Success(Unit)

    override suspend fun selectBackendProvider(
        providerId: String,
        model: String,
    ) = AppResult.Success(Unit)

    override suspend fun selectCustomProvider(
        providerId: String,
        model: String,
    ) = AppResult.Success(Unit)

    override suspend fun removeCustomProvider(providerId: String) = AppResult.Success(Unit)

    override suspend fun getCustomCredentials(providerId: String?) =
        AppResult.Success(
            AiCustomCredentialsModel("custom", "Example", "https://example.com/v1", "model", "secret"),
        )
}

private class FakeStudyRepository(
    private val customResult: AppResult<WordStudyModel, DataError> = AppResult.Success(sampleResult()),
) : WordStudyRepository {
    var backendCalls = 0
    var customCalls = 0
    var lastRequest: WordStudyRequestModel? = null

    override suspend fun getBackendProviders() =
        AppResult.Success(AiBackendProviderCatalogModel(defaultProvider = null, providers = emptyList()))

    override suspend fun generateWithBackend(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError> {
        backendCalls++
        lastRequest = request
        return AppResult.Success(sampleResult())
    }

    override suspend fun generateWithCustomProvider(
        request: WordStudyRequestModel,
        credentials: AiCustomCredentialsModel,
    ): AppResult<WordStudyModel, DataError> {
        customCalls++
        lastRequest = request
        return customResult
    }

    companion object {
        private fun sampleResult() =
            WordStudyModel(
                explanation = "Simple",
                examples = listOf("One", "Two"),
                usageNotes = listOf("One", "Two"),
                relatedWords = listOf("a", "b", "c"),
                providerMode = AiProviderMode.BACKEND,
                provider = "openai",
                model = "model",
            )
    }
}
