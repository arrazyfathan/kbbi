package com.arrazyfathan.kbbi.feature.proverb.presentation.proverb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.usecase.GetProverbMeaningUseCase
import com.arrazyfathan.kbbi.feature.proverb.domain.usecase.ObserveProverbsUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProverbState(
    val searchQuery: String = "",
    val selectedProverb: ProverbDetailModel? = null,
    val isMeaningLoading: Boolean = false,
)

sealed interface ProverbAction {
    data class OnSearchQueryChanged(
        val query: String,
    ) : ProverbAction

    data class OnProverbClicked(
        val proverb: ProverbModel,
    ) : ProverbAction

    data object OnMeaningDismissed : ProverbAction
}

sealed interface ProverbEvent {
    data class ShowMessage(
        val message: UiText,
    ) : ProverbEvent
}

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ProverbViewModel(
    observeProverbs: ObserveProverbsUseCase,
    private val getProverbMeaning: GetProverbMeaningUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(ProverbState())
    val state = _state.asStateFlow()

    val proverbs: Flow<PagingData<ProverbModel>> =
        state
            .map { it.searchQuery.trim() }
            .debounce(300)
            .distinctUntilChanged()
            .flatMapLatest { query -> observeProverbs(query) }
            .cachedIn(viewModelScope)

    private val _events = Channel<ProverbEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var meaningJob: Job? = null

    fun onAction(action: ProverbAction) {
        when (action) {
            is ProverbAction.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = action.query) }
            }

            is ProverbAction.OnProverbClicked -> loadMeaning(action.proverb)

            ProverbAction.OnMeaningDismissed -> {
                meaningJob?.cancel()
                _state.update {
                    it.copy(
                        selectedProverb = null,
                        isMeaningLoading = false,
                    )
                }
            }
        }
    }

    private fun loadMeaning(proverb: ProverbModel) {
        meaningJob?.cancel()
        meaningJob =
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        selectedProverb =
                            ProverbDetailModel(
                                text = proverb.text,
                                letter = proverb.letter,
                                slug = proverb.slug,
                                sourceUrl = proverb.sourceUrl,
                                meaning = null,
                            ),
                        isMeaningLoading = true,
                    )
                }

                when (val result = getProverbMeaning(proverb.slug)) {
                    is AppResult.Success -> {
                        _state.update {
                            it.copy(
                                selectedProverb = result.data,
                                isMeaningLoading = false,
                            )
                        }
                    }

                    is AppResult.Error -> {
                        _state.update { it.copy(isMeaningLoading = false) }
                        _events.send(ProverbEvent.ShowMessage(result.error.asUiText()))
                    }
                }
            }
    }
}
