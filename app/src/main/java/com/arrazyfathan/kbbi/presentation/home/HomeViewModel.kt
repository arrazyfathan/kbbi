package com.arrazyfathan.kbbi.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.usecase.ObserveSearchHistoryUseCase
import com.arrazyfathan.kbbi.core.domain.usecase.SearchWordWithHistoryUseCase
import com.arrazyfathan.kbbi.presentation.common.toMessage
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
        val word: ListWordModel,
    ) : HomeEvent

    data class ShowMessage(
        val message: String,
    ) : HomeEvent
}

class HomeViewModel(
    private val searchWordWithHistory: SearchWordWithHistoryUseCase,
    private val observeSearchHistory: ObserveSearchHistoryUseCase,
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
                observeSearchHistory().collect { histories ->
                    _state.update { it.copy(histories = histories) }
                }
            }
    }

    private fun search(word: String) {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                val result = searchWordWithHistory(word)
                _state.update { it.copy(isLoading = false) }
                when (result) {
                    is AppResult.Success -> {
                        _events.send(HomeEvent.NavigateToDetail(result.data))
                    }

                    is AppResult.Error -> {
                        _events.send(HomeEvent.ShowMessage(result.error.toMessage()))
                    }
                }
            }
    }
}
