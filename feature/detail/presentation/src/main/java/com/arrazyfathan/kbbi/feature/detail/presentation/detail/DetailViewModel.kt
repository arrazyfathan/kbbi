package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailState(
    val isSaved: Boolean = false,
)

sealed interface DetailAction {
    data class OnStarted(
        val word: String,
    ) : DetailAction

    data class OnBookmarkClick(
        val word: String,
        val wordList: List<WordModel>,
        val visitorCount: Int?,
    ) : DetailAction
}

sealed interface DetailEvent {
    data class ShowMessage(
        @param:StringRes val messageResId: Int,
    ) : DetailEvent
}

class DetailViewModel(
    private val checkWordSaved: CheckWordSavedUseCase,
    private val saveBookmark: SaveBookmarkUseCase,
    private val deleteBookmark: DeleteBookmarkUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var savedStateJob: Job? = null

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.OnStarted -> observeSavedState(action.word)
            is DetailAction.OnBookmarkClick -> toggleBookmark(action.word, action.wordList, action.visitorCount)
        }
    }

    private fun observeSavedState(word: String) {
        if (savedStateJob != null) return
        savedStateJob =
            viewModelScope.launch {
                checkWordSaved(word).collect { isSaved ->
                    _state.update { it.copy(isSaved = isSaved) }
                }
            }
    }

    private fun toggleBookmark(
        word: String,
        wordList: List<WordModel>,
        visitorCount: Int?,
    ) {
        viewModelScope.launch {
            if (state.value.isSaved) {
                deleteBookmark(word)
                _events.send(DetailEvent.ShowMessage(R.string.word_deleted_success))
            } else {
                val isSaved = saveBookmark(word, wordList, visitorCount)
                if (isSaved) {
                    _events.send(DetailEvent.ShowMessage(R.string.word_saved_success))
                }
            }
        }
    }
}
