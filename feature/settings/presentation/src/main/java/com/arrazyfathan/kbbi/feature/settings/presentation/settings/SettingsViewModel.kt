package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateConfig
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ClearSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.settings.domain.model.NotificationSettings
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.arrazyfathan.kbbi.feature.settings.domain.service.ReminderScheduler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SettingsState(
    val notifications: NotificationSettings = NotificationSettings(),
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isLanguagePickerVisible: Boolean = false,
    val appVersion: String = "",
    val isCheckingUpdate: Boolean = false,
    val availableUpdate: AppUpdate? = null,
    val isUpdatePromptVisible: Boolean = false,
    val isClearHistoryDialogVisible: Boolean = false,
)

sealed interface SettingsAction {
    data class OnStarted(
        val currentLanguage: AppLanguage,
    ) : SettingsAction

    data object OnLanguageClick : SettingsAction

    data object OnLanguagePickerDismissed : SettingsAction

    data object OnCheckForUpdate : SettingsAction

    data object OnUpdatePromptDismissed : SettingsAction

    data object OnClearHistoryClick : SettingsAction

    data object OnClearHistoryConfirmed : SettingsAction

    data object OnClearHistoryDismissed : SettingsAction

    data object OnPrivacyPolicyClick : SettingsAction

    data object OnTermsClick : SettingsAction

    data class OnLanguageSelected(
        val language: AppLanguage,
    ) : SettingsAction

    data class OnReminderToggled(
        val type: ReminderType,
        val enabled: Boolean,
    ) : SettingsAction

    data class OnReminderTimeChanged(
        val type: ReminderType,
        val time: ReminderTime,
    ) : SettingsAction

    data class OnPermissionResult(
        val type: ReminderType,
        val granted: Boolean,
    ) : SettingsAction
}

sealed interface SettingsEvent {
    data class ApplyLanguage(
        val language: AppLanguage,
    ) : SettingsEvent

    data class RequestNotificationPermission(
        val type: ReminderType,
    ) : SettingsEvent

    data object PermissionDenied : SettingsEvent

    data class ShowMessage(
        val message: UiText,
    ) : SettingsEvent
}

class SettingsViewModel(
    private val repository: NotificationSettingsRepository,
    private val scheduler: ReminderScheduler,
    private val appUpdateRepository: AppUpdateRepository,
    private val appUpdateConfig: AppUpdateConfig,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState(appVersion = appUpdateConfig.currentVersion))
    val state = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _state.update { it.copy(notifications = settings) }
            }
        }
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnStarted -> {
                _state.update { it.copy(selectedLanguage = action.currentLanguage) }
                reconcilePermission()
                checkForUpdate(force = false, showResult = false)
            }

            SettingsAction.OnLanguageClick -> {
                _state.update { it.copy(isLanguagePickerVisible = true) }
            }

            SettingsAction.OnLanguagePickerDismissed -> {
                _state.update { it.copy(isLanguagePickerVisible = false) }
            }

            SettingsAction.OnCheckForUpdate -> checkForUpdate(force = true, showResult = true)

            SettingsAction.OnUpdatePromptDismissed -> {
                _state.update { it.copy(isUpdatePromptVisible = false) }
            }

            SettingsAction.OnClearHistoryClick -> {
                _state.update { it.copy(isClearHistoryDialogVisible = true) }
            }

            SettingsAction.OnClearHistoryConfirmed -> clearSearchHistory()

            SettingsAction.OnClearHistoryDismissed -> {
                _state.update { it.copy(isClearHistoryDialogVisible = false) }
            }

            SettingsAction.OnPrivacyPolicyClick,
            SettingsAction.OnTermsClick,
            -> showMessage(R.string.coming_soon)

            is SettingsAction.OnLanguageSelected -> {
                selectLanguage(action.language)
            }

            is SettingsAction.OnReminderToggled -> {
                toggle(action.type, action.enabled)
            }

            is SettingsAction.OnReminderTimeChanged -> {
                updateTime(action.type, action.time)
            }

            is SettingsAction.OnPermissionResult -> {
                onPermissionResult(action.type, action.granted)
            }
        }
    }

    private fun checkForUpdate(
        force: Boolean,
        showResult: Boolean,
    ) {
        if (!force && !appUpdateConfig.isUpdateCheckEnabled) return

        viewModelScope.launch {
            _state.update { it.copy(isCheckingUpdate = true) }
            when (val result = appUpdateRepository.checkForUpdate(appUpdateConfig.currentVersion, force)) {
                is AppResult.Success -> {
                    _state.update {
                        it.copy(
                            isCheckingUpdate = false,
                            availableUpdate = result.data,
                            isUpdatePromptVisible = showResult && result.data != null,
                        )
                    }
                    if (showResult && result.data == null) {
                        _events.send(
                            SettingsEvent.ShowMessage(UiText.StringResource(R.string.app_update_up_to_date)),
                        )
                    }
                }

                is AppResult.Error -> {
                    _state.update { it.copy(isCheckingUpdate = false) }
                    if (showResult) {
                        _events.send(
                            SettingsEvent.ShowMessage(UiText.StringResource(R.string.update_check_failed)),
                        )
                    }
                }
            }
        }
    }

    private fun clearSearchHistory() {
        viewModelScope.launch {
            clearSearchHistoryUseCase()
            _state.update { it.copy(isClearHistoryDialogVisible = false) }
            _events.send(
                SettingsEvent.ShowMessage(UiText.StringResource(R.string.clear_history_confirmed)),
            )
        }
    }

    private fun showMessage(messageResId: Int) {
        viewModelScope.launch {
            _events.send(SettingsEvent.ShowMessage(UiText.StringResource(messageResId)))
        }
    }

    private fun selectLanguage(language: AppLanguage) {
        val languageChanged = language != state.value.selectedLanguage
        _state.update {
            it.copy(
                selectedLanguage = language,
                isLanguagePickerVisible = false,
            )
        }
        if (languageChanged) {
            viewModelScope.launch { _events.send(SettingsEvent.ApplyLanguage(language)) }
        }
    }

    private fun toggle(
        type: ReminderType,
        enabled: Boolean,
    ) {
        if (!enabled) {
            viewModelScope.launch {
                repository.setEnabled(type, false)
                scheduler.cancel(type)
            }
            return
        }

        val settings = state.value.notifications
        if (settings.permissionRequired && !settings.permissionGranted) {
            viewModelScope.launch { _events.send(SettingsEvent.RequestNotificationPermission(type)) }
        } else {
            enable(type)
        }
    }

    private fun onPermissionResult(
        type: ReminderType,
        granted: Boolean,
    ) {
        if (granted) {
            enable(type)
        } else {
            viewModelScope.launch {
                repository.setEnabled(type, false)
                scheduler.cancel(type)
                _events.send(SettingsEvent.PermissionDenied)
            }
        }
    }

    private fun enable(type: ReminderType) {
        viewModelScope.launch {
            repository.setEnabled(type, true)
            scheduler.schedule(
                type,
                state.value.notifications
                    .preference(type)
                    .time,
            )
        }
    }

    private fun updateTime(
        type: ReminderType,
        time: ReminderTime,
    ) {
        viewModelScope.launch {
            repository.setTime(type, time)
            if (state.value.notifications
                    .preference(type)
                    .enabled
            ) {
                scheduler.schedule(type, time)
            }
        }
    }

    private fun reconcilePermission() {
        viewModelScope.launch {
            val settings = repository.settings.first()
            if (settings.permissionRequired && !settings.permissionGranted) {
                ReminderType.entries
                    .filter { settings.preference(it).enabled }
                    .forEach { type ->
                        repository.setEnabled(type, false)
                        scheduler.cancel(type)
                    }
            }
        }
    }
}
