package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslatedMeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslatedWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.TranslateRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
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
    fun `toggling translation on fetches and enables translation`() = runTest(dispatcher) {
        val translateRepository = FakeTranslateRepository(AppResult.Success(sampleTranslation()))
        val viewModel = createViewModel(translateRepository = translateRepository)

        viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))

        assertEquals("belajar", translateRepository.requestedWords.single())
        assertTrue(viewModel.state.value.isTranslationEnabled)
        assertFalse(viewModel.state.value.isTranslationLoading)
        assertEquals("learn", viewModel.state.value.translation?.translation)
    }

    @Test
    fun `toggling translation off disables it but keeps cached result`() = runTest(dispatcher) {
        val translateRepository = FakeTranslateRepository(AppResult.Success(sampleTranslation()))
        val viewModel = createViewModel(translateRepository = translateRepository)

        viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))
        viewModel.onAction(DetailAction.OnTranslateToggled("belajar", false))

        assertFalse(viewModel.state.value.isTranslationEnabled)
        assertEquals("learn", viewModel.state.value.translation?.translation)
        assertEquals(1, translateRepository.requestedWords.size)
    }

    @Test
    fun `toggling translation on again uses cached result without refetching`() = runTest(dispatcher) {
        val translateRepository = FakeTranslateRepository(AppResult.Success(sampleTranslation()))
        val viewModel = createViewModel(translateRepository = translateRepository)

        viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))
        viewModel.onAction(DetailAction.OnTranslateToggled("belajar", false))
        viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))

        assertTrue(viewModel.state.value.isTranslationEnabled)
        assertEquals(1, translateRepository.requestedWords.size)
    }

    @Test
    fun `translation failure keeps toggle off and emits message`() = runTest(dispatcher) {
        val translateRepository = FakeTranslateRepository(AppResult.Error(DataError.NoInternet))
        val viewModel = createViewModel(translateRepository = translateRepository)

        viewModel.onAction(DetailAction.OnTranslateToggled("belajar", true))

        assertFalse(viewModel.state.value.isTranslationEnabled)
        assertFalse(viewModel.state.value.isTranslationLoading)
        assertNull(viewModel.state.value.translation)
        assertMessage(R.string.translate_failed, viewModel.events.first())
    }

    private fun createViewModel(
        bookmarkRepository: FakeBookmarkRepository = FakeBookmarkRepository(),
        translateRepository: FakeTranslateRepository = FakeTranslateRepository(),
    ): DetailViewModel =
        DetailViewModel(
            checkWordSaved = CheckWordSavedUseCase(bookmarkRepository),
            saveBookmark = SaveBookmarkUseCase(bookmarkRepository),
            deleteBookmark = DeleteBookmarkUseCase(bookmarkRepository),
            getWordTranslation = GetWordTranslationUseCase(translateRepository),
        )

    private fun sampleTranslation(): TranslateModel =
        TranslateModel(
            word = "belajar",
            translation = "learn",
            from = "id",
            to = "en",
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
        val message = (event as DetailEvent.ShowMessage).messageResId
        assertEquals(expectedResId, message)
    }
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

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> =
        MutableStateFlow(saved.value.contains(word))

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
