package com.arrazyfathan.kbbi.core.appupdate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateConfig
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.observability.AnalyticsEvent
import com.arrazyfathan.kbbi.core.observability.AnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.EventOutcome
import com.arrazyfathan.kbbi.core.observability.NoOpAnalyticsReporter
import com.arrazyfathan.kbbi.core.observability.UpdateAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUpdateState(
    val currentVersion: String = "",
    val availableUpdate: AppUpdate? = null,
)

sealed interface AppUpdateAction {
    data object OnAppStarted : AppUpdateAction

    data object OnPromptDismissed : AppUpdateAction
}

class AppUpdateViewModel(
    private val repository: AppUpdateRepository,
    private val config: AppUpdateConfig,
    private val analyticsReporter: AnalyticsReporter = NoOpAnalyticsReporter,
) : ViewModel() {
    private val _state = MutableStateFlow(AppUpdateState(currentVersion = config.currentVersion))
    val state = _state.asStateFlow()

    fun onAction(action: AppUpdateAction) {
        when (action) {
            AppUpdateAction.OnAppStarted -> checkForUpdate()
            AppUpdateAction.OnPromptDismissed -> {
                analyticsReporter.log(
                    AnalyticsEvent.AppUpdateInteraction(UpdateAction.Prompt, EventOutcome.Dismissed),
                )
                _state.update { it.copy(availableUpdate = null) }
            }
        }
    }

    private fun checkForUpdate() {
        if (!config.isUpdateCheckEnabled) return

        viewModelScope.launch {
            when (val result = repository.checkForUpdate(config.currentVersion)) {
                is AppResult.Success -> {
                    if (result.data != null) {
                        analyticsReporter.log(
                            AnalyticsEvent.AppUpdateInteraction(UpdateAction.Prompt, EventOutcome.Shown),
                        )
                    }
                    _state.update { it.copy(availableUpdate = result.data) }
                }
                is AppResult.Error -> {
                    analyticsReporter.log(
                        AnalyticsEvent.AppUpdateInteraction(UpdateAction.Prompt, EventOutcome.Error),
                    )
                }
            }
        }
    }
}
