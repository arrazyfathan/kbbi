package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.SearchWordWithHistoryUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Created by Ar Razy Fathan Rabbani on 19/01/23.
 */
data class HomeState(
    val searchQuery: String = "",
    val histories: List<HistoryModel> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface HomeAction {
    data object OnStarted : HomeAction

    data class OnSearchQueryChanged(
        val query: String,
    ) : HomeAction

    data class OnSearchSubmitted(
        val word: String,
    ) : HomeAction

    data class OnSuggestionClick(
        val word: String,
    ) : HomeAction
}

sealed interface HomeEvent {
    data class NavigateToDetail(
        val word: ListWordModel,
    ) : HomeEvent

    data class ShowMessage(
        val message: UiText,
    ) : HomeEvent
}

class HomeViewModel(
    private val searchWordWithHistory: SearchWordWithHistoryUseCase,
    private val observeSearchHistory: ObserveSearchHistoryUseCase,
    private val getWordEntries: GetWordEntriesUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var historiesJob: Job? = null
    private var searchJob: Job? = null
    private var wordEntriesJob: Job? = null
    private var wordEntries: List<String> = emptyList()

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnStarted -> {
                observeHistories()
                loadWordEntries()
            }
            is HomeAction.OnSearchQueryChanged -> updateSearchQuery(action.query)
            is HomeAction.OnSearchSubmitted -> search(action.word)
            is HomeAction.OnSuggestionClick -> search(action.word)
        }
    }

    private fun observeHistories() {
        if (historiesJob != null) return
        historiesJob =
            viewModelScope.launch {
                observeSearchHistory().collect { histories ->
                    _state.update { it.copy(histories = histories) }
                }
            }
    }

    private fun loadWordEntries() {
        if (wordEntriesJob != null) return
        wordEntriesJob =
            viewModelScope.launch {
                wordEntries = getWordEntries()
                _state.update {
                    it.copy(suggestions = getSuggestions(it.searchQuery))
                }
            }
    }

    private fun updateSearchQuery(query: String) {
        val normalizedQuery = query.replace(" ", "")
        _state.update {
            it.copy(
                searchQuery = normalizedQuery,
                suggestions = getSuggestions(normalizedQuery),
            )
        }
    }

    private fun search(word: String) {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        searchQuery = word,
                        suggestions = emptyList(),
                        isLoading = true,
                    )
                }
                val result = searchWordWithHistory(word)
                _state.update { it.copy(isLoading = false) }
                when (result) {
                    is AppResult.Success -> {
                        _state.update { it.copy(searchQuery = "") }
                        _events.send(HomeEvent.NavigateToDetail(result.data))
                    }

                    is AppResult.Error -> {
                        _events.send(HomeEvent.ShowMessage(result.error.asUiText()))
                    }
                }
            }
    }

    private fun getSuggestions(query: String): List<String> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.length < MIN_SUGGESTION_QUERY_LENGTH) return emptyList()

        val prefixMatches =
            wordEntries
                .asSequence()
                .filter { word -> word.startsWith(normalizedQuery) }

        val containsMatches =
            wordEntries
                .asSequence()
                .filter { word ->
                    normalizedQuery in word && !word.startsWith(normalizedQuery)
                }

        return (prefixMatches + containsMatches)
            .distinct()
            .take(MAX_SUGGESTIONS)
            .toList()
    }

    private companion object {
        const val MIN_SUGGESTION_QUERY_LENGTH = 2
        const val MAX_SUGGESTIONS = 8
    }
}
