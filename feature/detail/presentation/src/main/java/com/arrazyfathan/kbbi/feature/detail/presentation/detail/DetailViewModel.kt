package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.domain.model.onFailure
import com.arrazyfathan.kbbi.core.domain.model.onSuccess
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordTranslationUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SaveBookmarkUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class DetailState(
    val isSaved: Boolean = false,
    val isTranslationEnabled: Boolean = false,
    val isTranslationLoading: Boolean = false,
    val translation: TranslateModel? = null,
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

    data class OnTranslateToggled(
        val word: String,
        val enabled: Boolean,
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
    private val getWordTranslation: GetWordTranslationUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var savedStateJob: Job? = null
    private var translationJob: Job? = null

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.OnStarted -> observeSavedState(action.word)
            is DetailAction.OnBookmarkClick -> toggleBookmark(action.word, action.wordList, action.visitorCount)
            is DetailAction.OnTranslateToggled -> toggleTranslation(action.word, action.enabled)
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

    private fun toggleTranslation(
        word: String,
        enabled: Boolean,
    ) {
        if (enabled) {
            enableTranslation(word)
        } else {
            _state.update { it.copy(isTranslationEnabled = false) }
        }
    }

    private fun enableTranslation(word: String) {
        val cachedTranslation = state.value.translation
        if (cachedTranslation != null) {
            _state.update { it.copy(isTranslationEnabled = true) }
            return
        }

        if (translationJob != null) return
        _state.update { it.copy(isTranslationLoading = true) }
        translationJob =
            viewModelScope.launch {
                getWordTranslation(word)
                    .onSuccess { translation ->
                        _state.update {
                            it.copy(
                                isTranslationEnabled = true,
                                isTranslationLoading = false,
                                translation = translation,
                            )
                        }
                    }.onFailure {
                        _state.update { it.copy(isTranslationLoading = false) }
                        _events.send(DetailEvent.ShowMessage(R.string.translate_failed))
                    }
            }
    }
}
