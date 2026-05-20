package com.arrazyfathan.kbbi.presentation.detail

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.R
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.usecase.WordUseCase
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
    ) : DetailAction
}

sealed interface DetailEvent {
    data class ShowMessage(
        @param:StringRes val messageResId: Int,
    ) : DetailEvent
}

class DetailViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var savedStateJob: Job? = null

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.OnStarted -> observeSavedState(action.word)
            is DetailAction.OnBookmarkClick -> toggleBookmark(action.word, action.wordList)
        }
    }

    private fun observeSavedState(word: String) {
        if (savedStateJob != null) return
        savedStateJob =
            viewModelScope.launch {
                wordUseCase.checkIfWordIsSaved(word).collect { isSaved ->
                    _state.update { it.copy(isSaved = isSaved) }
                }
            }
    }

    private fun toggleBookmark(
        word: String,
        wordList: List<WordModel>,
    ) {
        viewModelScope.launch {
            if (state.value.isSaved) {
                wordUseCase.deleteWord(word)
                _events.send(DetailEvent.ShowMessage(R.string.word_deleted_success))
            } else {
                val result = wordUseCase.bookmarkWord(word, wordList, true)
                if (result != -1L) {
                    _events.send(DetailEvent.ShowMessage(R.string.word_saved_success))
                }
            }
        }
    }
}
