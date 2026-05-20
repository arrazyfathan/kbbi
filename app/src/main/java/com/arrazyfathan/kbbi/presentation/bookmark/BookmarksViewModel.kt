package com.arrazyfathan.kbbi.presentation.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.usecase.WordUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BookmarksState(
    val bookmarks: List<ListWordModel> = emptyList(),
)

sealed interface BookmarksAction {
    data class OnDeleteConfirmed(
        val word: String,
    ) : BookmarksAction
}

class BookmarksViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {
    val state: StateFlow<BookmarksState> =
        wordUseCase
            .getBookmarks()
            .map { bookmarks -> BookmarksState(bookmarks = bookmarks) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = BookmarksState(),
            )

    fun onAction(action: BookmarksAction) {
        when (action) {
            is BookmarksAction.OnDeleteConfirmed -> {
                viewModelScope.launch {
                    wordUseCase.deleteWord(action.word)
                }
            }
        }
    }
}
