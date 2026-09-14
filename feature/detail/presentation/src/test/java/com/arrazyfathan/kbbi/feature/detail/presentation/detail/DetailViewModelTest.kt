package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.AnalyticsScreen
import com.arrazyfathan.kbbi.core.observability.BookmarkAction
import com.arrazyfathan.kbbi.feature.home.domain.model.AiBackendProviderCatalogModel
import com.arrazyfathan.kbbi.feature.home.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.home.domain.model.AiCustomCredentialsModel
import com.arrazyfathan.kbbi.feature.home.domain.model.AiCustomProviderModel
import com.arrazyfathan.kbbi.feature.home.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.MeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslatedMeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslatedWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordStudyRequestModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TranslateRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordStudyRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GenerateWordStudyUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordTranslationUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `toggling translation on fetches and enables translation`() =
        runTest(dispatcher) {
            val translateRepository = FakeTranslateRepository(AppResult.Success(sampleTranslation()))
            val viewModel = createViewModel(translateRepository = translateRepository)

            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))

            assertEquals("belajar", translateRepository.requestedWords.single())
            assertTrue(viewModel.state.value.isTranslationEnabled)
            assertFalse(viewModel.state.value.isTranslationLoading)
            assertEquals(
                "learn",
                viewModel.state.value.translation
                    ?.translation,
            )
        }

    @Test
    fun `toggling translation off disables it but keeps cached result`() =
        runTest(dispatcher) {
            val translateRepository = FakeTranslateRepository(AppResult.Success(sampleTranslation()))
            val viewModel = createViewModel(translateRepository = translateRepository)

            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))
            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", false))

            assertFalse(viewModel.state.value.isTranslationEnabled)
            assertEquals(
                "learn",
                viewModel.state.value.translation
                    ?.translation,
            )
            assertEquals(1, translateRepository.requestedWords.size)
        }

    @Test
    fun `toggling translation on again uses cached result without refetching`() =
        runTest(dispatcher) {
            val translateRepository = FakeTranslateRepository(AppResult.Success(sampleTranslation()))
            val viewModel = createViewModel(translateRepository = translateRepository)

            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))
            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", false))
            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))

            assertTrue(viewModel.state.value.isTranslationEnabled)
            assertEquals(1, translateRepository.requestedWords.size)
        }

    @Test
    fun `translation failure keeps toggle off and emits message`() =
        runTest(dispatcher) {
            val translateRepository = FakeTranslateRepository(AppResult.Error(DataError.NoInternet))
            val viewModel = createViewModel(translateRepository = translateRepository)

            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))

            assertFalse(viewModel.state.value.isTranslationEnabled)
            assertFalse(viewModel.state.value.isTranslationLoading)
            assertNull(viewModel.state.value.translation)
            assertMessage(R.string.translate_failed, viewModel.events.first())
        }

    @Test
    fun `retrying translation after a failure refetches and enables`() =
        runTest(dispatcher) {
            val translateRepository = FakeTranslateRepository(AppResult.Error(DataError.NoInternet))
            val viewModel = createViewModel(translateRepository = translateRepository)

            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))
            viewModel.events.first()

            translateRepository.result = AppResult.Success(sampleTranslation())
            viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))

            assertTrue(viewModel.state.value.isTranslationEnabled)
            assertFalse(viewModel.state.value.isTranslationLoading)
            assertEquals(
                "learn",
                viewModel.state.value.translation
                    ?.translation,
            )
            assertEquals(2, translateRepository.requestedWords.size)
        }

    @Test
    fun `bookmark action reports only action and surface`() =
        runTest(dispatcher) {
            val reporter = FakeAnalyticsReporter()
            val viewModel = createViewModel(analyticsReporter = reporter)

            viewModel.onAction(DetailAction.OnBookmarkClick("private-word", emptyList(), null))

            assertEquals(
                AnalyticsEvent.BookmarkChanged(BookmarkAction.Added, AnalyticsScreen.WordDetail),
                reporter.events.single(),
            )
            assertFalse(
                reporter.events
                    .single()
                    .parameters.values
                    .contains("private-word"),
            )
        }

    @Test
    fun `generating study uses backend and stores result`() =
        runTest(dispatcher) {
            val studyRepository = FakeWordStudyRepository()
            val viewModel = createViewModel(wordStudyRepository = studyRepository)
            val word = sampleWord()

            viewModel.onAction(DetailAction.OnStarted(word, "id"))
            viewModel.onAction(DetailAction.OnGenerateWordStudy)

            assertEquals(1, studyRepository.backendCalls)
            assertEquals(0, studyRepository.customCalls)
            assertEquals(
                "Penjelasan",
                viewModel.state.value.wordStudy
                    ?.explanation,
            )
        }

    @Test
    fun `regenerating study always issues a fresh request`() =
        runTest(dispatcher) {
            val studyRepository = FakeWordStudyRepository()
            val viewModel = createViewModel(wordStudyRepository = studyRepository)

            viewModel.onAction(DetailAction.OnStarted(sampleWord(), "id"))
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            viewModel.onAction(DetailAction.OnGenerateWordStudy)

            assertEquals(2, studyRepository.backendCalls)
        }

    @Test
    fun `provider change clears visible study`() =
        runTest(dispatcher) {
            val configurationRepository = FakeAiConfigurationRepository()
            val studyRepository = FakeWordStudyRepository()
            val viewModel =
                createViewModel(
                    configurationRepository = configurationRepository,
                    wordStudyRepository = studyRepository,
                )
            viewModel.onAction(DetailAction.OnStarted(sampleWord(), "id"))
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            assertTrue(viewModel.state.value.wordStudy != null)

            configurationRepository.configurationState.value =
                AiConfigurationModel(
                    providerMode = AiProviderMode.CUSTOM,
                    customProviders =
                        listOf(
                            AiCustomProviderModel(
                                id = "custom",
                                name = "Example",
                                baseUrl = "https://example.com/v1",
                                models = listOf("model"),
                                selectedModel = "model",
                                hasApiKey = true,
                            ),
                        ),
                    selectedCustomProviderId = "custom",
                    revision = 1,
                )

            assertNull(viewModel.state.value.wordStudy)
            viewModel.onAction(DetailAction.OnGenerateWordStudy)
            assertEquals(1, studyRepository.backendCalls)
            assertEquals(1, studyRepository.customCalls)
            assertEquals(
                AiProviderMode.CUSTOM,
                viewModel.state.value.wordStudy
                    ?.providerMode,
            )
        }

    private fun createViewModel(
        bookmarkRepository: FakeBookmarkRepository = FakeBookmarkRepository(),
        translateRepository: FakeTranslateRepository = FakeTranslateRepository(),
        analyticsReporter: AnalyticsReporter = FakeAnalyticsReporter(),
        configurationRepository: FakeAiConfigurationRepository = FakeAiConfigurationRepository(),
        wordStudyRepository: FakeWordStudyRepository = FakeWordStudyRepository(),
    ): DetailViewModel =
        DetailViewModel(
            checkWordSaved = CheckWordSavedUseCase(bookmarkRepository),
            saveBookmark = SaveBookmarkUseCase(bookmarkRepository),
            deleteBookmark = DeleteBookmarkUseCase(bookmarkRepository),
            getWordTranslation = GetWordTranslationUseCase(translateRepository),
            generateWordStudyUseCase = GenerateWordStudyUseCase(configurationRepository, wordStudyRepository),
            aiConfigurationRepository = configurationRepository,
            analyticsReporter = analyticsReporter,
        )

    private fun sampleWord() =
        ListWordModel(
            word = "belajar",
            listWords = listOf(WordModel("belajar", listOf(MeaningModel("v", "mencari ilmu")))),
        )

    private fun sampleTranslation(): TranslateModel =
        TranslateModel(
            word = "belajar",
            translation = "learn",
            from = "id",
            to = "en",
            provider = "google",
            entries =
                listOf(
                    TranslatedWordModel(
                        headword = "belajar",
                        meanings =
                            listOf(
                                TranslatedMeaningModel(
                                    wordClass = "v",
                                    description = "berusaha memperoleh kepandaian atau ilmu",
                                    translation = "attempt to gain knowledge or skill",
                                ),
                            ),
                    ),
                ),
        )

    private fun assertMessage(
        expectedResId: Int,
        event: DetailEvent,
    ) {
        val message = (event as DetailEvent.ShowError).messageResId
        assertEquals(expectedResId, message)
    }
}

private class FakeAiConfigurationRepository : AiConfigurationRepository {
    val configurationState = MutableStateFlow(AiConfigurationModel())
    override val configuration: Flow<AiConfigurationModel> = configurationState

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

private class FakeWordStudyRepository : WordStudyRepository {
    var backendCalls = 0
    var customCalls = 0
    private val result =
        WordStudyModel(
            explanation = "Penjelasan",
            examples = listOf("Satu", "Dua"),
            usageNotes = listOf("Satu", "Dua"),
            relatedWords = listOf("ajar", "pelajar", "pelajaran"),
            providerMode = AiProviderMode.BACKEND,
            provider = "openai",
            model = "model",
        )

    override suspend fun getBackendProviders() =
        AppResult.Success(AiBackendProviderCatalogModel(defaultProvider = null, providers = emptyList()))

    override suspend fun generateWithBackend(request: WordStudyRequestModel): AppResult<WordStudyModel, DataError> {
        backendCalls++
        return AppResult.Success(result)
    }

    override suspend fun generateWithCustomProvider(
        request: WordStudyRequestModel,
        credentials: AiCustomCredentialsModel,
    ): AppResult<WordStudyModel, DataError> {
        customCalls++
        return AppResult.Success(result.copy(providerMode = AiProviderMode.CUSTOM))
    }
}

private class FakeAnalyticsReporter : AnalyticsReporter {
    val events = mutableListOf<AnalyticsEvent>()

    override fun log(event: AnalyticsEvent) {
        events += event
    }

    override fun screenViewed(screen: AnalyticsScreen) = Unit
}

private class FakeBookmarkRepository : BookmarkRepository {
    private val saved = MutableStateFlow<Set<String>>(emptySet())

    override suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        visitorCount: Int?,
    ): Boolean {
        saved.value = saved.value + word
        return true
    }

    override suspend fun deleteWord(word: String) {
        saved.value = saved.value - word
    }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> = MutableStateFlow(saved.value.contains(word))

    override fun getBookmarks() = MutableStateFlow(emptyList<ListWordModel>())
}

private class FakeTranslateRepository(
    var result: AppResult<TranslateModel, DataError> = AppResult.Error(DataError.NotFound),
) : TranslateRepository {
    val requestedWords = mutableListOf<String>()

    override suspend fun getTranslation(word: String): AppResult<TranslateModel, DataError> {
        requestedWords += word
        return result
    }
}
