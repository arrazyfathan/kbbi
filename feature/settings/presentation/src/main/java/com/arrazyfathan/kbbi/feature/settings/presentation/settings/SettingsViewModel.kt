package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
)

sealed interface SettingsAction {
    data object OnStarted : SettingsAction

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
    data class RequestNotificationPermission(
        val type: ReminderType,
    ) : SettingsEvent

    data object PermissionDenied : SettingsEvent
}

class SettingsViewModel(
    private val repository: NotificationSettingsRepository,
    private val scheduler: ReminderScheduler,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
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
            SettingsAction.OnStarted -> reconcilePermission()
            is SettingsAction.OnReminderToggled -> toggle(action.type, action.enabled)
            is SettingsAction.OnReminderTimeChanged -> updateTime(action.type, action.time)
            is SettingsAction.OnPermissionResult -> onPermissionResult(action.type, action.granted)
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
