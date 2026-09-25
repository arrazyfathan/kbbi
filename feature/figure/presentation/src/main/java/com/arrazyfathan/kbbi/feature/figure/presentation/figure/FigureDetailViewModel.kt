package com.arrazyfathan.kbbi.feature.figure.presentation.figure

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.usecase.GetFigureDetailUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class FigureDetailState(
    val isLoading: Boolean = true,
    val figure: FigureModel? = null,
    val error: UiText? = null,
)

sealed interface FigureDetailAction {
    data class Load(
        val slug: String,
    ) : FigureDetailAction

    data object Retry : FigureDetailAction
}

class FigureDetailViewModel(
    private val getFigureDetail: GetFigureDetailUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(FigureDetailState())
    val state = _state.asStateFlow()

    private var slug: String? = null
    private var loadJob: Job? = null

    fun onAction(action: FigureDetailAction) {
        when (action) {
            is FigureDetailAction.Load -> {
                if (slug != action.slug || (_state.value.figure == null && loadJob == null)) {
                    slug = action.slug
                    load(action.slug)
                }
            }

            FigureDetailAction.Retry -> {
                slug?.let(::load)
            }
        }
    }

    private fun load(slug: String) {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, figure = null, error = null) }
                when (val result = getFigureDetail(slug)) {
                    is AppResult.Success -> _state.update { it.copy(isLoading = false, figure = result.data) }
                    is AppResult.Error -> _state.update { it.copy(isLoading = false, error = result.error.asUiText()) }
                }
            }
    }
}
