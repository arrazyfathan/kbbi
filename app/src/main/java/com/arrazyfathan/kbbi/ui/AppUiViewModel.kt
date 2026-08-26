package com.arrazyfathan.kbbi.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class AppUiState(
    val hapticsEnabled: Boolean = true,
)

class AppUiViewModel(
    repository: UiPreferencesRepository,
) : ViewModel() {
    val state: StateFlow<AppUiState> =
        repository.preferences
            .map { preferences -> AppUiState(hapticsEnabled = preferences.hapticsEnabled) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AppUiState(),
            )
}
