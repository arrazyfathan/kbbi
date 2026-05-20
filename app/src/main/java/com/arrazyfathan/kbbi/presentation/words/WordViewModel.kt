package com.arrazyfathan.kbbi.presentation.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.Resource
import com.arrazyfathan.kbbi.core.domain.usecase.WordUseCase
import com.arrazyfathan.kbbi.utils.toJson
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
    data class OnWordsLoaded(
        val words: List<String>,
    ) : WordListAction

    data class OnSearchQueryChanged(
        val query: String,
    ) : WordListAction

    data class OnWordClicked(
        val word: String,
    ) : WordListAction
}

sealed interface WordListEvent {
    data class NavigateToDetail(
        val dataJson: String,
    ) : WordListEvent

    data class ShowMessage(
        val message: String,
    ) : WordListEvent
}

class WordViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(WordListState())
    val state = _state.asStateFlow()

    private val _events = Channel<WordListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var searchJob: Job? = null

    fun onAction(action: WordListAction) {
        when (action) {
            is WordListAction.OnWordsLoaded -> {
                _state.update {
                    it.copy(
                        words = action.words,
                        filteredWords = filterWords(action.words, it.searchQuery),
                    )
                }
            }

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

    private fun search(word: String) {
        val wordToSearch = word.trim()
        if (wordToSearch.isBlank()) return

        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                wordUseCase.getMeaningOfWord(word = wordToSearch).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _state.update { it.copy(isLoading = true) }
                        }

                        is Resource.Success -> {
                            _state.update { it.copy(isLoading = false) }
                            val dataJson =
                                ListWordModel(
                                    word = wordToSearch,
                                    listWords = resource.data ?: emptyList(),
                                ).toJson()
                            _events.send(WordListEvent.NavigateToDetail(dataJson))
                        }

                        is Resource.Error -> {
                            _state.update { it.copy(isLoading = false) }
                            _events.send(WordListEvent.ShowMessage(resource.message ?: "Error occurred"))
                        }
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
