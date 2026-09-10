package com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark

import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.AnalyticsScreen
import com.arrazyfathan.kbbi.core.observability.BookmarkAction
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveBookmarksUseCase
import com.arrazyfathan.kbbi.feature.settings.domain.model.BookmarkLayout
import com.arrazyfathan.kbbi.feature.settings.domain.model.UiPreferences
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class BookmarksViewModelTest {
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
    fun `state combines bookmarks with persisted layout`() = runTest(dispatcher) {
        val bookmarkRepository = FakeBookmarkRepository()
        val preferencesRepository = FakeUiPreferencesRepository()
        val viewModel = createViewModel(bookmarkRepository, preferencesRepository)

        bookmarkRepository.bookmarks.value = listOf(sampleBookmark("belajar"))
        preferencesRepository.value = UiPreferences(bookmarkLayout = BookmarkLayout.LIST)

        val state = viewModel.state.first { it.bookmarkLayout == BookmarkLayout.LIST }

        assertEquals(listOf("belajar"), state.bookmarks.map(ListWordModel::word))
    }

    @Test
    fun `selecting layout persists it and updates state`() = runTest(dispatcher) {
        val preferencesRepository = FakeUiPreferencesRepository()
        val viewModel = createViewModel(preferencesRepository = preferencesRepository)

        viewModel.onAction(BookmarksAction.OnLayoutSelected(BookmarkLayout.LIST))

        assertEquals(listOf(BookmarkLayout.LIST), preferencesRepository.layoutWrites)
        assertEquals(
            BookmarkLayout.LIST,
            viewModel.state.first { it.bookmarkLayout == BookmarkLayout.LIST }.bookmarkLayout,
        )
    }

    @Test
    fun `confirmed deletion deletes once reports sanitized analytics and emits event`() = runTest(dispatcher) {
        val bookmarkRepository = FakeBookmarkRepository()
        val reporter = FakeAnalyticsReporter()
        val viewModel = createViewModel(bookmarkRepository, analyticsReporter = reporter)

        viewModel.onAction(BookmarksAction.OnDeleteConfirmed("private-word"))

        assertEquals(listOf("private-word"), bookmarkRepository.deletedWords)
        assertEquals(
            AnalyticsEvent.BookmarkChanged(BookmarkAction.Removed, AnalyticsScreen.Bookmarks),
            reporter.events.single(),
        )
        assertFalse(reporter.events.single().parameters.values.contains("private-word"))
        assertEquals(BookmarksEvent.BookmarkDeleted, viewModel.events.first())
    }

    private fun createViewModel(
        bookmarkRepository: FakeBookmarkRepository = FakeBookmarkRepository(),
        preferencesRepository: FakeUiPreferencesRepository = FakeUiPreferencesRepository(),
        analyticsReporter: AnalyticsReporter = FakeAnalyticsReporter(),
    ): BookmarksViewModel =
        BookmarksViewModel(
            observeBookmarks = ObserveBookmarksUseCase(bookmarkRepository),
            deleteBookmark = DeleteBookmarkUseCase(bookmarkRepository),
            uiPreferencesRepository = preferencesRepository,
            analyticsReporter = analyticsReporter,
        )

    private fun sampleBookmark(word: String) = ListWordModel(word = word, listWords = emptyList())
}

private class FakeBookmarkRepository : BookmarkRepository {
    val bookmarks = MutableStateFlow<List<ListWordModel>>(emptyList())
    val deletedWords = mutableListOf<String>()

    override suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        visitorCount: Int?,
    ): Boolean = true

    override suspend fun deleteWord(word: String) {
        deletedWords += word
    }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> = MutableStateFlow(false)

    override fun getBookmarks(): Flow<List<ListWordModel>> = bookmarks
}

private class FakeUiPreferencesRepository : UiPreferencesRepository {
    private val state = MutableStateFlow(UiPreferences())
    override val preferences: Flow<UiPreferences> = state
    val layoutWrites = mutableListOf<BookmarkLayout>()

    var value: UiPreferences
        get() = state.value
        set(value) {
            state.value = value
        }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        value = value.copy(hapticsEnabled = enabled)
    }

    override suspend fun setTheme(theme: AppTheme) {
        value = value.copy(theme = theme)
    }

    override suspend fun setBookmarkLayout(layout: BookmarkLayout) {
        layoutWrites += layout
        value = value.copy(bookmarkLayout = layout)
    }
}

private class FakeAnalyticsReporter : AnalyticsReporter {
    val events = mutableListOf<AnalyticsEvent>()

    override fun log(event: AnalyticsEvent) {
        events += event
    }

    override fun screenViewed(screen: AnalyticsScreen) = Unit
}
