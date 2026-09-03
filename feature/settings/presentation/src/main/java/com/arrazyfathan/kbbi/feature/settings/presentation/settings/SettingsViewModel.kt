package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateConfig
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.core.presentation.ui.UiText
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.DefaultReportingPreferencesRepository
import com.arrazyfathan.kbbi.core.observability.NoOpAnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.ReminderKind
import com.arrazyfathan.kbbi.core.observability.ReportingPreferencesRepository
import com.arrazyfathan.kbbi.feature.home.domain.usecase.ClearSearchHistoryUseCase
import com.arrazyfathan.kbbi.feature.settings.domain.model.NotificationSettings
import com.arrazyfathan.kbbi.feature.settings.domain.model.AppIcon
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
import com.arrazyfathan.kbbi.feature.settings.domain.service.ReminderScheduler
import com.arrazyfathan.kbbi.feature.settings.domain.service.AppIconChangeResult
import com.arrazyfathan.kbbi.feature.settings.domain.service.AppIconManager
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
    val hapticsEnabled: Boolean = true,
    val selectedTheme: AppTheme = AppTheme.ROYAL_OCEAN,
    val selectedAppIcon: AppIcon = AppIcon.DEFAULT,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isLanguagePickerVisible: Boolean = false,
    val appVersion: String = "",
    val isCheckingUpdate: Boolean = false,
    val availableUpdate: AppUpdate? = null,
    val isUpdatePromptVisible: Boolean = false,
    val isClearHistoryDialogVisible: Boolean = false,
    val pendingAppIcon: AppIcon? = null,
    val crashReportingEnabled: Boolean = true,
    val analyticsEnabled: Boolean = false,
)

sealed interface SettingsAction {
    data class OnStarted(
        val currentLanguage: AppLanguage,
    ) : SettingsAction

    data object OnLanguageClick : SettingsAction

    data object OnLanguagePickerDismissed : SettingsAction

    data class OnLanguageConfigurationChanged(
        val language: AppLanguage,
    ) : SettingsAction

    data object OnCheckForUpdate : SettingsAction

    data object OnUpdatePromptDismissed : SettingsAction

    data object OnClearHistoryClick : SettingsAction

    data object OnClearHistoryConfirmed : SettingsAction

    data object OnClearHistoryDismissed : SettingsAction

    data class OnLanguageSelected(
        val language: AppLanguage,
    ) : SettingsAction

    data class OnReminderToggled(
        val type: ReminderType,
        val enabled: Boolean,
    ) : SettingsAction

    data class OnHapticsToggled(
        val enabled: Boolean,
    ) : SettingsAction

    data class OnCrashReportingToggled(
        val enabled: Boolean,
    ) : SettingsAction

    data class OnAnalyticsToggled(
        val enabled: Boolean,
    ) : SettingsAction

    data class OnThemeSelected(
        val theme: AppTheme,
    ) : SettingsAction

    data class OnAppIconSelected(
        val icon: AppIcon,
    ) : SettingsAction

    data object OnAppIconChangeConfirmed : SettingsAction

    data object OnAppIconChangeDismissed : SettingsAction

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

    data class ReminderChanged(
        val enabled: Boolean,
    ) : SettingsEvent

    data class HapticsChanged(
        val enabled: Boolean,
    ) : SettingsEvent

    data object SelectionChanged : SettingsEvent

    data object AppIconChanged : SettingsEvent

    data class ShowMessage(
        val message: UiText,
        val isError: Boolean = false,
    ) : SettingsEvent
}

class SettingsViewModel(
    private val repository: NotificationSettingsRepository,
    private val uiPreferencesRepository: UiPreferencesRepository,
    private val scheduler: ReminderScheduler,
    private val appUpdateRepository: AppUpdateRepository,
    private val appUpdateConfig: AppUpdateConfig,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    private val appIconManager: AppIconManager,
    private val reportingPreferencesRepository: ReportingPreferencesRepository = DefaultReportingPreferencesRepository,
    private val analyticsReporter: AnalyticsReporter = NoOpAnalyticsReporter,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState(appVersion = appUpdateConfig.currentVersion))
    val state = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        _state.update {
            it.copy(
                selectedAppIcon =
                    runCatching { appIconManager.currentIcon() }
                        .getOrDefault(AppIcon.DEFAULT),
            )
        }
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _state.update { it.copy(notifications = settings) }
            }
        }
        viewModelScope.launch {
            uiPreferencesRepository.preferences.collect { preferences ->
                _state.update {
                    it.copy(
                        hapticsEnabled = preferences.hapticsEnabled,
                        selectedTheme = preferences.theme,
                    )
                }
            }
        }
        viewModelScope.launch {
            reportingPreferencesRepository.preferences.collect { preferences ->
                _state.update {
                    it.copy(
                        crashReportingEnabled = preferences.crashReportingEnabled,
                        analyticsEnabled = preferences.analyticsEnabled,
                    )
                }
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

            is SettingsAction.OnLanguageConfigurationChanged -> {
                _state.update { it.copy(selectedLanguage = action.language) }
            }

            SettingsAction.OnCheckForUpdate -> {
                checkForUpdate(force = true, showResult = true)
            }

            SettingsAction.OnUpdatePromptDismissed -> {
                _state.update { it.copy(isUpdatePromptVisible = false) }
            }

            SettingsAction.OnClearHistoryClick -> {
                _state.update { it.copy(isClearHistoryDialogVisible = true) }
            }

            SettingsAction.OnClearHistoryConfirmed -> {
                clearSearchHistory()
            }

            SettingsAction.OnClearHistoryDismissed -> {
                _state.update { it.copy(isClearHistoryDialogVisible = false) }
            }

            is SettingsAction.OnLanguageSelected -> {
                selectLanguage(action.language)
            }

            is SettingsAction.OnReminderToggled -> {
                toggle(action.type, action.enabled)
            }

            is SettingsAction.OnHapticsToggled -> {
                setHapticsEnabled(action.enabled)
            }

            is SettingsAction.OnCrashReportingToggled -> {
                setCrashReportingEnabled(action.enabled)
            }

            is SettingsAction.OnAnalyticsToggled -> {
                setAnalyticsEnabled(action.enabled)
            }

            is SettingsAction.OnThemeSelected -> {
                selectTheme(action.theme)
            }

            is SettingsAction.OnAppIconSelected -> {
                selectAppIcon(action.icon)
            }

            SettingsAction.OnAppIconChangeConfirmed -> {
                confirmAppIconChange()
            }

            SettingsAction.OnAppIconChangeDismissed -> {
                _state.update { it.copy(pendingAppIcon = null) }
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
                            SettingsEvent.ShowMessage(
                                message = UiText.StringResource(R.string.update_check_failed),
                                isError = true,
                            ),
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

    private fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            uiPreferencesRepository.setHapticsEnabled(enabled)
            _events.send(SettingsEvent.HapticsChanged(enabled))
        }
    }

    private fun setCrashReportingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { reportingPreferencesRepository.setCrashReportingEnabled(enabled) }
                .onFailure { showMessage(R.string.reporting_setting_failed) }
        }
    }

    private fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            runCatching { reportingPreferencesRepository.setAnalyticsEnabled(enabled) }
                .onFailure { showMessage(R.string.reporting_setting_failed) }
        }
    }

    private fun selectTheme(theme: AppTheme) {
        if (theme == state.value.selectedTheme) return

        viewModelScope.launch {
            uiPreferencesRepository.setTheme(theme)
            _events.send(SettingsEvent.SelectionChanged)
        }
    }

    private fun selectAppIcon(icon: AppIcon) {
        if (icon == state.value.selectedAppIcon) return

        _state.update { it.copy(pendingAppIcon = icon) }
    }

    private fun confirmAppIconChange() {
        val icon = state.value.pendingAppIcon ?: return
        _state.update { it.copy(pendingAppIcon = null) }

        viewModelScope.launch {
            val result =
                try {
                    appIconManager.changeIcon(icon)
                } catch (_: Exception) {
                    AppIconChangeResult.FAILURE
                }
            when (result) {
                AppIconChangeResult.SUCCESS -> {
                    _state.update {
                        it.copy(
                            selectedAppIcon = icon,
                        )
                    }
                    _events.send(SettingsEvent.AppIconChanged)
                }

                AppIconChangeResult.FAILURE -> {
                    _events.send(
                        SettingsEvent.ShowMessage(
                            message = UiText.StringResource(R.string.app_icon_change_failed),
                            isError = true,
                        ),
                    )
                }
            }
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
                analyticsReporter.log(AnalyticsEvent.ReminderChanged(type.toAnalyticsKind(), false))
                _events.send(SettingsEvent.ReminderChanged(false))
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
            analyticsReporter.log(AnalyticsEvent.ReminderChanged(type.toAnalyticsKind(), true))
            _events.send(SettingsEvent.ReminderChanged(true))
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
            _events.send(SettingsEvent.SelectionChanged)
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

private fun ReminderType.toAnalyticsKind(): ReminderKind =
    when (this) {
        ReminderType.DAILY_WORD -> ReminderKind.DailyWord
        ReminderType.DAILY_PROVERB -> ReminderKind.DailyProverb
        ReminderType.BOOKMARK_REVIEW -> ReminderKind.BookmarkReview
    }
