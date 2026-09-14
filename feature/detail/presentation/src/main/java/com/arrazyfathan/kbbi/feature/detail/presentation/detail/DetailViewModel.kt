package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.onFailure
import com.arrazyfathan.kbbi.core.domain.model.onSuccess
import com.arrazyfathan.kbbi.core.observability.AiProviderType
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.AnalyticsScreen
import com.arrazyfathan.kbbi.core.observability.BookmarkAction
import com.arrazyfathan.kbbi.core.observability.EventOutcome
import com.arrazyfathan.kbbi.core.observability.NoOpAnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.TranslationAction
import com.arrazyfathan.kbbi.feature.home.domain.model.AiConfigurationModel
import com.arrazyfathan.kbbi.feature.home.domain.model.AiProviderMode
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordStudyModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.AiConfigurationRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.CheckWordSavedUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.DeleteBookmarkUseCase
import com.arrazyfathan.kbbi.feature.home.domain.usecase.GenerateWordStudyUseCase
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
    val aiConfiguration: AiConfigurationModel = AiConfigurationModel(),
    val isWordStudyLoading: Boolean = false,
    val wordStudy: WordStudyModel? = null,
    @param:StringRes val wordStudyErrorResId: Int? = null,
)

sealed interface DetailAction {
    data class OnStarted(
        val word: ListWordModel,
        val language: String,
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

    data object OnGenerateWordStudy : DetailAction

    data object OnRetryWordStudy : DetailAction

    data object OnConfigureAi : DetailAction
}

sealed interface DetailEvent {
    data class BookmarkChanged(
        val isSaved: Boolean,
        @param:StringRes val messageResId: Int,
    ) : DetailEvent

    data class TranslationChanged(
        val enabled: Boolean,
    ) : DetailEvent

    data class ShowError(
        @param:StringRes val messageResId: Int,
    ) : DetailEvent

    data object NavigateToAiSettings : DetailEvent
}

class DetailViewModel(
    private val checkWordSaved: CheckWordSavedUseCase,
    private val saveBookmark: SaveBookmarkUseCase,
    private val deleteBookmark: DeleteBookmarkUseCase,
    private val getWordTranslation: GetWordTranslationUseCase,
    private val generateWordStudyUseCase: GenerateWordStudyUseCase,
    private val aiConfigurationRepository: AiConfigurationRepository,
    private val analyticsReporter: AnalyticsReporter = NoOpAnalyticsReporter,
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state = _state.asStateFlow()

    private val _events = Channel<DetailEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var savedStateJob: Job? = null
    private var translationJob: Job? = null
    private var wordStudyJob: Job? = null
    private var currentWord: ListWordModel? = null
    private var currentLanguage: String = "id"
    private var wordStudyMemoryKey: WordStudyMemoryKey? = null

    init {
        viewModelScope.launch {
            aiConfigurationRepository.configuration.collect { configuration ->
                val previous = _state.value.aiConfiguration
                if (previous.providerMode != configuration.providerMode ||
                    previous.revision != configuration.revision
                ) {
                    wordStudyJob?.cancel()
                    wordStudyMemoryKey =
                        currentWord?.let {
                            WordStudyMemoryKey(
                                it.word,
                                currentLanguage,
                                configuration.providerMode,
                                configuration.revision,
                            )
                        }
                    _state.update {
                        it.copy(
                            aiConfiguration = configuration,
                            isWordStudyLoading = false,
                            wordStudy = null,
                            wordStudyErrorResId = null,
                        )
                    }
                } else {
                    _state.update { it.copy(aiConfiguration = configuration) }
                }
            }
        }
    }

    fun onAction(action: DetailAction) {
        when (action) {
            is DetailAction.OnStarted -> {
                val nextKey =
                    WordStudyMemoryKey(
                        word = action.word.word,
                        language = action.language,
                        providerMode = state.value.aiConfiguration.providerMode,
                        configurationRevision = state.value.aiConfiguration.revision,
                    )
                if (wordStudyMemoryKey != null && wordStudyMemoryKey != nextKey) {
                    wordStudyJob?.cancel()
                    _state.update {
                        it.copy(isWordStudyLoading = false, wordStudy = null, wordStudyErrorResId = null)
                    }
                }
                wordStudyMemoryKey = nextKey
                currentWord = action.word
                currentLanguage = action.language
                observeSavedState(action.word.word.lowercase())
            }

            is DetailAction.OnBookmarkClick -> {
                toggleBookmark(action.word, action.wordList, action.visitorCount)
            }

            is DetailAction.OnTranslateToggled -> {
                toggleTranslation(action.word, action.enabled)
            }

            DetailAction.OnGenerateWordStudy,
            DetailAction.OnRetryWordStudy,
            -> {
                generateWordStudy()
            }

            DetailAction.OnConfigureAi -> {
                viewModelScope.launch { _events.send(DetailEvent.NavigateToAiSettings) }
            }
        }
    }

    private fun generateWordStudy() {
        val word = currentWord ?: return
        val configuration = state.value.aiConfiguration
        if (configuration.providerMode == AiProviderMode.CUSTOM && !configuration.isCustomConfigurationComplete) {
            viewModelScope.launch { _events.send(DetailEvent.NavigateToAiSettings) }
            return
        }
        wordStudyJob?.cancel()
        _state.update { it.copy(isWordStudyLoading = true, wordStudy = null, wordStudyErrorResId = null) }
        val providerType =
            if (configuration.providerMode == AiProviderMode.BACKEND) AiProviderType.Backend else AiProviderType.Custom
        wordStudyJob =
            viewModelScope.launch {
                when (val result = generateWordStudyUseCase(word, currentLanguage)) {
                    is com.arrazyfathan.kbbi.core.domain.model.AppResult.Success -> {
                        analyticsReporter.log(AnalyticsEvent.AiWordStudyGenerated(providerType, EventOutcome.Success))
                        _state.update {
                            it.copy(isWordStudyLoading = false, wordStudy = result.data, wordStudyErrorResId = null)
                        }
                    }

                    is com.arrazyfathan.kbbi.core.domain.model.AppResult.Error -> {
                        analyticsReporter.log(AnalyticsEvent.AiWordStudyGenerated(providerType, EventOutcome.Error))
                        _state.update {
                            it.copy(
                                isWordStudyLoading = false,
                                wordStudy = null,
                                wordStudyErrorResId = result.error.toWordStudyErrorResource(),
                            )
                        }
                    }
                }
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
                analyticsReporter.log(
                    AnalyticsEvent.BookmarkChanged(BookmarkAction.Removed, AnalyticsScreen.WordDetail),
                )
                _events.send(
                    DetailEvent.BookmarkChanged(
                        isSaved = false,
                        messageResId = R.string.word_deleted_success,
                    ),
                )
            } else {
                val isSaved = saveBookmark(word, wordList, visitorCount)
                if (isSaved) {
                    analyticsReporter.log(
                        AnalyticsEvent.BookmarkChanged(BookmarkAction.Added, AnalyticsScreen.WordDetail),
                    )
                    _events.send(
                        DetailEvent.BookmarkChanged(
                            isSaved = true,
                            messageResId = R.string.word_saved_success,
                        ),
                    )
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
            analyticsReporter.log(
                AnalyticsEvent.TranslationChanged(
                    action = TranslationAction.Disabled,
                    outcome = EventOutcome.Success,
                    cacheHit = state.value.translation != null,
                ),
            )
            viewModelScope.launch { _events.send(DetailEvent.TranslationChanged(false)) }
        }
    }

    private fun enableTranslation(word: String) {
        val cachedTranslation = state.value.translation
        if (cachedTranslation != null) {
            _state.update { it.copy(isTranslationEnabled = true) }
            viewModelScope.launch { _events.send(DetailEvent.TranslationChanged(true)) }
            analyticsReporter.log(
                AnalyticsEvent.TranslationChanged(
                    action = TranslationAction.Enabled,
                    outcome = EventOutcome.Success,
                    cacheHit = true,
                ),
            )
            return
        }

        if (translationJob?.isActive == true) return
        _state.update { it.copy(isTranslationLoading = true) }
        translationJob =
            viewModelScope.launch {
                getWordTranslation(word)
                    .onSuccess { translation ->
                        analyticsReporter.log(
                            AnalyticsEvent.TranslationChanged(
                                action = TranslationAction.Enabled,
                                outcome = EventOutcome.Success,
                                cacheHit = false,
                            ),
                        )
                        _state.update {
                            it.copy(
                                isTranslationEnabled = true,
                                isTranslationLoading = false,
                                translation = translation,
                            )
                        }
                        _events.send(DetailEvent.TranslationChanged(true))
                    }.onFailure {
                        analyticsReporter.log(
                            AnalyticsEvent.TranslationChanged(
                                action = TranslationAction.Enabled,
                                outcome = EventOutcome.Error,
                                cacheHit = false,
                            ),
                        )
                        _state.update { it.copy(isTranslationLoading = false) }
                        _events.send(DetailEvent.ShowError(R.string.translate_failed))
                    }
            }
    }
}

private data class WordStudyMemoryKey(
    val word: String,
    val language: String,
    val providerMode: AiProviderMode,
    val configurationRevision: Long,
)

@StringRes
private fun DataError.toWordStudyErrorResource(): Int =
    when (this) {
        DataError.TooManyRequests -> R.string.ai_word_study_error_rate_limited

        DataError.NoInternet -> R.string.ai_word_study_error_no_internet

        DataError.RequestTimeout -> R.string.ai_word_study_error_timeout

        DataError.BadRequest -> R.string.ai_word_study_error_invalid_request

        DataError.ServiceUnavailable,
        DataError.ServerError,
        -> R.string.ai_word_study_error_unavailable

        else -> R.string.ai_word_study_error_generic
    }
