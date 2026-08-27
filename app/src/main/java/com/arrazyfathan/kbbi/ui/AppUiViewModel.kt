package com.arrazyfathan.kbbi.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class AppUiState(
    val hapticsEnabled: Boolean = true,
    val theme: AppTheme = AppTheme.ROYAL_OCEAN,
)

class AppUiViewModel(
    repository: UiPreferencesRepository,
) : ViewModel() {
    val state: StateFlow<AppUiState> =
        repository.preferences
            .map { preferences ->
                AppUiState(
                    hapticsEnabled = preferences.hapticsEnabled,
                    theme = preferences.theme,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AppUiState(),
            )
}
