package com.arrazyfathan.kbbi.feature.home.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.presentation.ui.asUiText
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.ContentType
import com.arrazyfathan.kbbi.core.observability.EventOutcome
import com.arrazyfathan.kbbi.core.observability.EventSource
import com.arrazyfathan.kbbi.core.observability.InputMethod
import com.arrazyfathan.kbbi.core.observability.NoOpAnalyticsReporter
import com.arrazyfathan.kbbi.core.utils.VoiceRecognitionError
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordEntriesUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GetWordSuggestionsUseCase
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
@Immutable
data class HomeState(
    val searchQuery: String = "",
    val histories: List<HistoryModel> = emptyList(),
    val suggestions: List<String> = emptyList(),
    val suggestionMode: HomeSuggestionMode = HomeSuggestionMode.Search,
    val isLoading: Boolean = false,
    val isVoiceListening: Boolean = false,
    val voicePartialText: String = "",
)

enum class HomeSuggestionMode {
    Search,
    DidYouMean,
}

sealed interface HomeAction {
    data object OnStarted : HomeAction

    data class OnSearchQueryChanged(
        val query: String,
    ) : HomeAction

    data class OnSearchSubmitted(
        val word: String,
        val source: EventSource = EventSource.TypedSearch,
    ) : HomeAction

    data class OnSuggestionClick(
        val word: String,
    ) : HomeAction

    data object OnRandomWordRequested : HomeAction

    data object OnVoiceSearchStarted : HomeAction

    data object OnVoiceSearchFinished : HomeAction

    data class OnVoiceSearchResult(
        val recognizedTexts: List<String>,
    ) : HomeAction

    data class OnVoiceSearchPartialResult(
        val recognizedTexts: List<String>,
    ) : HomeAction

    data class OnVoiceSearchError(
        val error: VoiceRecognitionError,
    ) : HomeAction

    data object OnVoiceSearchUnavailable : HomeAction

    data object OnVoiceSearchPermissionDenied : HomeAction

    data object OnVoiceSearchCancelled : HomeAction

    data object OnVoiceSearchEmptyResult : HomeAction
}

sealed interface HomeEvent {
    data class NavigateToDetail(
        val word: ListWordModel,
    ) : HomeEvent

    data class ShowMessage(
        val message: UiText,
        val isError: Boolean = true,
    ) : HomeEvent
}

class HomeViewModel(
    private val searchWordWithHistory: SearchWordWithHistoryUseCase,
    private val observeSearchHistory: ObserveSearchHistoryUseCase,
    private val getWordEntries: GetWordEntriesUseCase,
    private val getWordSuggestions: GetWordSuggestionsUseCase,
    private val analyticsReporter: AnalyticsReporter = NoOpAnalyticsReporter,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var historiesJob: Job? = null
    private var searchJob: Job? = null
    private var wordEntriesJob: Job? = null
    private var wordEntries: List<String> = emptyList()
    private var pendingInputMethod = InputMethod.Text

    fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnStarted -> {
                observeHistories()
                loadWordEntries()
            }

            is HomeAction.OnSearchQueryChanged -> {
                updateSearchQuery(action.query)
            }

            is HomeAction.OnSearchSubmitted -> {
                search(action.word, action.source, pendingInputMethod)
                pendingInputMethod = InputMethod.Text
            }

            is HomeAction.OnSuggestionClick -> {
                search(action.word, EventSource.Suggestion, InputMethod.Text, suggestionUsed = true)
            }

            HomeAction.OnRandomWordRequested -> {
                searchRandomWord()
            }

            HomeAction.OnVoiceSearchStarted -> {
                _state.update { it.copy(isVoiceListening = true, voicePartialText = "") }
            }

            HomeAction.OnVoiceSearchFinished -> {
                _state.update {
                    it.copy(
                        isVoiceListening = false,
                        voicePartialText = "",
                    )
                }
            }

            is HomeAction.OnVoiceSearchResult -> {
                updateVoiceSearchQuery(action.recognizedTexts)
            }

            is HomeAction.OnVoiceSearchPartialResult -> {
                updateVoicePartialResult(action.recognizedTexts)
            }

            is HomeAction.OnVoiceSearchError -> {
                pendingInputMethod = InputMethod.Text
                showVoiceSearchError(action.error)
            }

            HomeAction.OnVoiceSearchUnavailable -> {
                pendingInputMethod = InputMethod.Text
                showMessage(R.string.voice_search_unavailable)
            }

            HomeAction.OnVoiceSearchPermissionDenied -> {
                pendingInputMethod = InputMethod.Text
                showMessage(R.string.voice_search_permission_denied)
            }

            HomeAction.OnVoiceSearchCancelled -> {
                pendingInputMethod = InputMethod.Text
                showMessage(R.string.voice_search_cancelled, isError = false)
            }

            HomeAction.OnVoiceSearchEmptyResult -> {
                showMessage(R.string.voice_search_empty_result)
            }
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
                    it.copy(suggestions = getWordSuggestions(it.searchQuery, wordEntries))
                }
            }
    }

    private fun updateSearchQuery(query: String) {
        val normalizedQuery = normalizeTypedSearchQuery(query)
        _state.update {
            it.copy(
                searchQuery = normalizedQuery,
                suggestions = getWordSuggestions(normalizedQuery, wordEntries),
                suggestionMode = HomeSuggestionMode.Search,
            )
        }
    }

    private fun updateVoiceSearchQuery(recognizedTexts: List<String>) {
        val normalizedQuery = normalizeVoiceSearchCandidates(recognizedTexts, wordEntries)
        if (normalizedQuery.isBlank()) {
            pendingInputMethod = InputMethod.Text
            showMessage(R.string.voice_search_empty_result)
            return
        }

        pendingInputMethod = InputMethod.Voice
        _state.update {
            it.copy(
                searchQuery = normalizedQuery,
                suggestions = getWordSuggestions(normalizedQuery, wordEntries),
                suggestionMode = HomeSuggestionMode.Search,
            )
        }
    }

    private fun updateVoicePartialResult(recognizedTexts: List<String>) {
        val partialText = recognizedTexts.firstOrNull().orEmpty()
        _state.update { it.copy(voicePartialText = partialText) }
    }

    private fun showVoiceSearchError(error: VoiceRecognitionError) {
        val messageResId =
            when (error) {
                VoiceRecognitionError.NoMatch,
                VoiceRecognitionError.NoSpeech,
                -> R.string.voice_search_empty_result

                VoiceRecognitionError.PermissionDenied -> R.string.voice_search_permission_denied

                VoiceRecognitionError.RecognizerBusy -> R.string.voice_search_busy

                VoiceRecognitionError.Network,
                VoiceRecognitionError.NetworkTimeout,
                VoiceRecognitionError.Server,
                VoiceRecognitionError.TooManyRequests,
                VoiceRecognitionError.Client,
                VoiceRecognitionError.Unknown,
                -> R.string.voice_search_failed
            }
        showMessage(messageResId)
    }

    private fun showMessage(
        messageResId: Int,
        isError: Boolean = true,
    ) {
        viewModelScope.launch {
            _events.send(
                HomeEvent.ShowMessage(
                    message = UiText.StringResource(messageResId),
                    isError = isError,
                ),
            )
        }
    }

    private fun search(
        word: String,
        source: EventSource,
        inputMethod: InputMethod,
        suggestionUsed: Boolean = false,
    ) {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                performSearch(word, source, inputMethod, suggestionUsed)
            }
    }

    private fun searchRandomWord() {
        searchJob?.cancel()
        searchJob =
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                if (wordEntries.isEmpty()) {
                    wordEntries = getWordEntries()
                }

                val randomWord = wordEntries.randomOrNull()
                if (randomWord == null) {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(HomeEvent.ShowMessage(UiText.StringResource(R.string.error_random_word_unavailable)))
                    return@launch
                }

                performSearch(
                    word = randomWord,
                    source = EventSource.RandomWord,
                    inputMethod = InputMethod.System,
                    randomSelection = true,
                )
            }
    }

    private suspend fun performSearch(
        word: String,
        source: EventSource,
        inputMethod: InputMethod,
        suggestionUsed: Boolean = false,
        randomSelection: Boolean = false,
    ) {
        _state.update {
            it.copy(
                searchQuery = word,
                suggestions = emptyList(),
                suggestionMode = HomeSuggestionMode.Search,
                isLoading = true,
            )
        }
        val result = searchWordWithHistory(word)
        _state.update { it.copy(isLoading = false) }
        when (result) {
            is AppResult.Success -> {
                analyticsReporter.log(
                    AnalyticsEvent.SearchCompleted(
                        source = source,
                        inputMethod = inputMethod,
                        outcome = EventOutcome.Success,
                        suggestionUsed = suggestionUsed,
                        randomSelection = randomSelection,
                    ),
                )
                analyticsReporter.log(AnalyticsEvent.ContentOpened(ContentType.Word, source))
                _state.update { it.copy(searchQuery = "") }
                _events.send(HomeEvent.NavigateToDetail(result.data))
            }

            is AppResult.Error -> {
                analyticsReporter.log(
                    AnalyticsEvent.SearchCompleted(
                        source = source,
                        inputMethod = inputMethod,
                        outcome =
                            if (result.error == DataError.NotFound) {
                                EventOutcome.NotFound
                            } else {
                                EventOutcome.Error
                            },
                        suggestionUsed = suggestionUsed,
                        randomSelection = randomSelection,
                    ),
                )
                if (result.error == DataError.NotFound) {
                    _state.update {
                        it.copy(
                            suggestions = getWordSuggestions(word, wordEntries),
                            suggestionMode = HomeSuggestionMode.DidYouMean,
                        )
                    }
                }
                _events.send(HomeEvent.ShowMessage(result.error.asUiText()))
            }
        }
    }
}
