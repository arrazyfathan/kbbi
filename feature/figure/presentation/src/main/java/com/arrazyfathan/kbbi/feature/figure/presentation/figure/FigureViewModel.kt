package com.arrazyfathan.kbbi.feature.figure.presentation.figure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.arrazyfathan.kbbi.feature.figure.domain.usecase.GetFiguresUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds

data class FigureState(val searchQuery: String = "")

sealed interface FigureAction {
    data class OnSearchQueryChanged(val query: String) : FigureAction
}

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FigureViewModel(
    getFigures: GetFiguresUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(FigureState())
    val state = _state.asStateFlow()

    val figures =
        state
            .map { it.searchQuery.trim() }
            .debounce(300.milliseconds)
            .distinctUntilChanged()
            .flatMapLatest { query -> getFigures(query = query, includeDetails = true) }
            .cachedIn(viewModelScope)

    fun onAction(action: FigureAction) {
        when (action) {
            is FigureAction.OnSearchQueryChanged -> _state.update { it.copy(searchQuery = action.query) }
        }
    }
}
