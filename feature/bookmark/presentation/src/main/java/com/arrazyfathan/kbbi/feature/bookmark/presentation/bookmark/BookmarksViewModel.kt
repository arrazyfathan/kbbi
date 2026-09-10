package com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.AnalyticsScreen
import com.arrazyfathan.kbbi.core.observability.BookmarkAction
import com.arrazyfathan.kbbi.core.observability.NoOpAnalyticsReporter
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveBookmarksUseCase
import com.arrazyfathan.kbbi.feature.settings.domain.model.BookmarkLayout
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class BookmarksState(
    val bookmarks: List<ListWordModel> = emptyList(),
    val bookmarkLayout: BookmarkLayout = BookmarkLayout.GRID,
)

sealed interface BookmarksAction {
    data class OnLayoutSelected(
        val layout: BookmarkLayout,
    ) : BookmarksAction

    data class OnDeleteConfirmed(
        val word: String,
    ) : BookmarksAction
}

sealed interface BookmarksEvent {
    data object BookmarkDeleted : BookmarksEvent
}

class BookmarksViewModel(
    observeBookmarks: ObserveBookmarksUseCase,
    private val deleteBookmark: DeleteBookmarkUseCase,
    private val uiPreferencesRepository: UiPreferencesRepository,
    private val analyticsReporter: AnalyticsReporter = NoOpAnalyticsReporter,
) : ViewModel() {
    private val _events = Channel<BookmarksEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val state: StateFlow<BookmarksState> =
        combine(observeBookmarks(), uiPreferencesRepository.preferences) { bookmarks, preferences ->
            BookmarksState(
                bookmarks = bookmarks,
                bookmarkLayout = preferences.bookmarkLayout,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BookmarksState(),
        )

    fun onAction(action: BookmarksAction) {
        when (action) {
            is BookmarksAction.OnLayoutSelected -> {
                viewModelScope.launch {
                    uiPreferencesRepository.setBookmarkLayout(action.layout)
                }
            }
            is BookmarksAction.OnDeleteConfirmed -> {
                viewModelScope.launch {
                    deleteBookmark(action.word)
                    analyticsReporter.log(
                        AnalyticsEvent.BookmarkChanged(BookmarkAction.Removed, AnalyticsScreen.Bookmarks),
                    )
                    _events.send(BookmarksEvent.BookmarkDeleted)
                }
            }
        }
    }
}
