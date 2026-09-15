package com.arrazyfathan.kbbi.feature.wordstudy.presentation.ai

import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiBackendProviderModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiCustomProviderModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.model.WordStudyRequestModel
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.repository.WordStudyRepository
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.GetAiProvidersUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.RemoveAiConfigurationUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SaveAiConfigurationUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectAiProviderModeUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectBackendAiProviderUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.SelectCustomAiProviderUseCase
import com.arrazyfathan.kbbi.feature.wordstudy.domain.usecase.TestAiConnectionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class AiSettingsViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `backend provider catalog selects the server default`() =
        runTest(dispatcher) {
            val viewModel = createViewModel()

            assertEquals(
                listOf("openai", "gemini"),
                viewModel.state.value.backendProviders
                    .map { it.id },
            )
            assertEquals("gemini", viewModel.state.value.selectedBackendProviderId)
            assertEquals("gemini-2.5-flash", viewModel.state.value.selectedBackendModel)
        }

    @Test
    fun `saving custom provider supports a default domain name and multiple models`() =
        runTest(dispatcher) {
            val repository = FakeAiSettingsConfigurationRepository()
            val viewModel = createViewModel(repository)
            viewModel.onAction(AiSettingsAction.OnAddCustomProvider)
            viewModel.onAction(AiSettingsAction.OnBaseUrlChanged("https://provider.example/v1/"))
            viewModel.onAction(AiSettingsAction.OnModelChanged(0, "model-a"))
            viewModel.onAction(AiSettingsAction.OnAddModel)
            viewModel.onAction(AiSettingsAction.OnModelChanged(1, "model-b"))
            viewModel.onAction(AiSettingsAction.OnEditorModelSelected(1))
            viewModel.onAction(AiSettingsAction.OnApiKeyChanged("secret"))

            viewModel.onAction(AiSettingsAction.OnSave)

            val provider =
                repository.state.value.customProviders
                    .single()
            assertEquals(AiProviderMode.BACKEND, repository.state.value.providerMode)
            assertEquals("provider.example", provider.name)
            assertEquals(listOf("model-a", "model-b"), provider.models)
            assertEquals("model-b", provider.selectedModel)
            assertTrue(provider.hasApiKey)
            assertFalse(viewModel.state.value.isEditorVisible)
            assertEquals(
                R.string.ai_settings_saved,
                (viewModel.events.first() as AiSettingsEvent.ShowMessage).messageResId,
            )
        }

    @Test
    fun `selecting custom while incomplete shows error and keeps backend`() =
        runTest(dispatcher) {
            val repository = FakeAiSettingsConfigurationRepository()
            val viewModel = createViewModel(repository)

            viewModel.onAction(AiSettingsAction.OnProviderModeSelected(AiProviderMode.CUSTOM))

            assertEquals(AiProviderMode.BACKEND, repository.state.value.providerMode)
            assertEquals(
                R.string.ai_settings_custom_incomplete,
                (viewModel.events.first() as AiSettingsEvent.ShowMessage).messageResId,
            )
        }

    @Test
    fun `selecting backend provider persists its default model`() =
        runTest(dispatcher) {
            val repository = FakeAiSettingsConfigurationRepository()
            val viewModel = createViewModel(repository)

            viewModel.onAction(AiSettingsAction.OnBackendProviderSelected("openai"))

            assertEquals("openai", repository.state.value.backendProviderId)
            assertEquals("gpt-5-mini", repository.state.value.backendModel)
        }

    private fun createViewModel(
        repository: FakeAiSettingsConfigurationRepository = FakeAiSettingsConfigurationRepository(),
        wordStudyRepository: WordStudyRepository = FakeWordStudyRepository(),
    ): AiSettingsViewModel =
        AiSettingsViewModel(
            configurationRepository = repository,
            getProviders = GetAiProvidersUseCase(wordStudyRepository),
            saveConfiguration = SaveAiConfigurationUseCase(repository),
            selectProviderMode = SelectAiProviderModeUseCase(repository),
            selectBackendProvider = SelectBackendAiProviderUseCase(repository),
            selectCustomProvider = SelectCustomAiProviderUseCase(repository),
            removeConfiguration = RemoveAiConfigurationUseCase(repository),
            testConnection = TestAiConnectionUseCase(wordStudyRepository, repository),
        )
}

private class FakeAiSettingsConfigurationRepository : AiConfigurationRepository {
    val state = MutableStateFlow(AiConfigurationModel())
    override val configuration: Flow<AiConfigurationModel> = state
    private val apiKeys = mutableMapOf<String, String>()
    private var nextId = 1

    override suspend fun saveCustomProvider(
        providerId: String?,
        name: String,
        baseUrl: String,
        models: List<String>,
        selectedModel: String,
        apiKey: String?,
    ): AppResult<String, DataError> {
        val id = providerId ?: "custom-${nextId++}"
        apiKey?.let { apiKeys[id] = it }
        val provider =
            AiCustomProviderModel(
                id = id,
                name = name,
                baseUrl = baseUrl,
                models = models,
                selectedModel = selectedModel,
                hasApiKey = apiKeys[id] != null,
            )
        state.value =
            state.value.copy(
                customProviders = state.value.customProviders.filterNot { it.id == id } + provider,
                selectedCustomProviderId = state.value.selectedCustomProviderId ?: id,
                revision = state.value.revision + 1,
            )
        return AppResult.Success(id)
    }

    override suspend fun selectProviderMode(mode: AiProviderMode): AppResult<Unit, DataError> {
        if (mode == AiProviderMode.CUSTOM && !state.value.isCustomConfigurationComplete) {
            return AppResult.Error(DataError.BadRequest)
        }
        state.value = state.value.copy(providerMode = mode)
        return AppResult.Success(Unit)
    }

    override suspend fun selectBackendProvider(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError> {
        state.value = state.value.copy(backendProviderId = providerId, backendModel = model)
        return AppResult.Success(Unit)
    }

    override suspend fun selectCustomProvider(
        providerId: String,
        model: String,
    ): AppResult<Unit, DataError> {
        state.value =
            state.value.copy(
                customProviders =
                    state.value.customProviders.map {
                        if (it.id == providerId) it.copy(selectedModel = model) else it
                    },
                selectedCustomProviderId = providerId,
            )
        return AppResult.Success(Unit)
    }

    override suspend fun removeCustomProvider(providerId: String): AppResult<Unit, DataError> {
        apiKeys.remove(providerId)
        val providers = state.value.customProviders.filterNot { it.id == providerId }
        state.value =
            state.value.copy(
                providerMode = if (providers.isEmpty()) AiProviderMode.BACKEND else state.value.providerMode,
                customProviders = providers,
                selectedCustomProviderId = providers.firstOrNull()?.id,
                revision = state.value.revision + 1,
            )
        return AppResult.Success(Unit)
    }

    override suspend fun getCustomCredentials(providerId: String?): AppResult<AiCustomCredentialsModel, DataError> {
        val id = providerId ?: state.value.selectedCustomProviderId ?: return AppResult.Error(DataError.BadRequest)
        val provider =
            state.value.customProviders.firstOrNull { it.id == id }
                ?: return AppResult.Error(DataError.BadRequest)
        val apiKey = apiKeys[id] ?: return AppResult.Error(DataError.BadRequest)
        return AppResult.Success(
            AiCustomCredentialsModel(id, provider.name, provider.baseUrl, provider.selectedModel, apiKey),
        )
    }
}

private class FakeWordStudyRepository : WordStudyRepository {
    override suspend fun getBackendProviders() =
        AppResult.Success(
            AiBackendProviderCatalogModel(
                defaultProvider = "gemini",
                providers =
                    listOf(
                        AiBackendProviderModel("openai", "gpt-5-mini", listOf("gpt-5-mini", "gpt-5")),
                        AiBackendProviderModel("gemini", "gemini-2.5-flash", listOf("gemini-2.5-flash")),
                    ),
            ),
        )

    override suspend fun generateWithBackend(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError> =
        AppResult.Error(DataError.Unknown)

    override suspend fun generateWithCustomProvider(
        request: WordStudyRequestModel,
        credentials: AiCustomCredentialsModel,
    ): AppResult<WordStudyModel, DataError> = AppResult.Error(DataError.Unknown)
}
