package com.arrazyfathan.kbbi.presentation.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.SearchWordUseCase
import com.arrazyfathan.kbbi.presentation.common.toMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ar Razy Fathan Rabbani on 18/03/23.
 */
data class WordListState(
    val searchQuery: String = "",
    val words: List<String> = emptyList(),
    val filteredWords: List<String> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface WordListAction {
    data object OnStarted : WordListAction

    data class OnSearchQueryChanged(
        val query: String,
    ) : WordListAction

    data class OnWordClicked(
        val word: String,
    ) : WordListAction
}

sealed interface WordListEvent {
    data class NavigateToDetail(
        val word: ListWordModel,
    ) : WordListEvent

    data class ShowMessage(
        val message: String,
    ) : WordListEvent
}

class WordViewModel(
    private val searchWord: SearchWordUseCase,
    private val getWordEntries: GetWordEntriesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(WordListState())
    val state = _state.asStateFlow()

    private val _events = Channel<WordListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var searchJob: Job? = null
    private var wordsJob: Job? = null

    fun onAction(action: WordListAction) {
        when (action) {
            WordListAction.OnStarted -> loadWords()

            is WordListAction.OnSearchQueryChanged -> {
                _state.update {
                    it.copy(
                        searchQuery = action.query,
                        filteredWords = filterWords(it.words, action.query),
                    )
                }
            }

            is WordListAction.OnWordClicked -> search(action.word)
        }
    }

    private fun loadWords() {
        if (wordsJob != null) return
        wordsJob =
            viewModelScope.launch {
                val words = getWordEntries()
                _state.update {
                    it.copy(
                        words = words,
                        filteredWords = filterWords(words, it.searchQuery),
                    )
                }
            }
    }

    private fun search(word: String) {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                val result = searchWord(word)
                _state.update { it.copy(isLoading = false) }
                when (result) {
                    is AppResult.Success -> {
                        _events.send(WordListEvent.NavigateToDetail(result.data))
                    }

                    is AppResult.Error -> {
                        _events.send(WordListEvent.ShowMessage(result.error.toMessage()))
                    }
                }
            }
    }

    private fun filterWords(
        words: List<String>,
        query: String,
    ): List<String> =
        if (query.isEmpty()) {
            words
        } else {
            words.filter { it.contains(query, ignoreCase = true) }
        }
}
