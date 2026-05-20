package com.arrazyfathan.kbbi.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
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
 * Created by Ar Razy Fathan Rabbani on 19/01/23.
 */
data class HomeState(
    val histories: List<HistoryModel> = emptyList(),
    val isLoading: Boolean = false,
)

sealed interface HomeAction {
    data object OnStarted : HomeAction

    data class OnSearchSubmitted(
        val word: String,
    ) : HomeAction
}

sealed interface HomeEvent {
    data class NavigateToDetail(
        val dataJson: String,
    ) : HomeEvent

    data class ShowMessage(
        val message: String,
    ) : HomeEvent
}

class HomeViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var historiesJob: Job? = null
    private var searchJob: Job? = null

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnStarted -> observeHistories()
            is HomeAction.OnSearchSubmitted -> search(action.word)
        }
    }

    private fun observeHistories() {
        if (historiesJob != null) return
        historiesJob =
            viewModelScope.launch {
                wordUseCase.getAllHistories().collect { histories ->
                    _state.update { it.copy(histories = histories) }
                }
            }
    }

    private fun search(word: String) {
        val wordToSearch = word.trim()
        if (wordToSearch.isBlank()) return

        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                wordUseCase.getMeaningOfWord(wordToSearch).collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            _state.update { it.copy(isLoading = true) }
                        }

                        is Resource.Success -> {
                            _state.update { it.copy(isLoading = false) }
                            wordUseCase.addToHistory(HistoryModel(wordToSearch.lowercase()))

                            val dataJson =
                                ListWordModel(
                                    word = wordToSearch,
                                    listWords = resource.data ?: emptyList(),
                                ).toJson()
                            _events.send(HomeEvent.NavigateToDetail(dataJson))
                        }

                        is Resource.Error -> {
                            _state.update { it.copy(isLoading = false) }
                            _events.send(HomeEvent.ShowMessage(resource.message ?: "Error occurred"))
                        }
                    }
                }
            }
    }
}
