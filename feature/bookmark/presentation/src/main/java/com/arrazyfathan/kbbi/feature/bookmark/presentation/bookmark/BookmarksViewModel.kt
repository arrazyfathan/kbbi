package com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveBookmarksUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class BookmarksState(
    val bookmarks: List<ListWordModel> = emptyList(),
)

sealed interface BookmarksAction {
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
) : ViewModel() {
    private val _events = Channel<BookmarksEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val state: StateFlow<BookmarksState> =
        observeBookmarks().map { bookmarks -> BookmarksState(bookmarks = bookmarks) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BookmarksState(),
        )

    fun onAction(action: BookmarksAction) {
        when (action) {
            is BookmarksAction.OnDeleteConfirmed -> {
                viewModelScope.launch {
                    deleteBookmark(action.word)
                    _events.send(BookmarksEvent.BookmarkDeleted)
                }
            }
        }
    }
}
