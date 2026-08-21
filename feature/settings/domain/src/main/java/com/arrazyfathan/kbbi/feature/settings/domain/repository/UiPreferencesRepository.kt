package com.arrazyfathan.kbbi.feature.settings.domain.repository

import com.arrazyfathan.kbbi.feature.settings.domain.model.UiPreferences
import kotlinx.coroutines.flow.Flow

interface UiPreferencesRepository {
    val preferences: Flow<UiPreferences>

    suspend fun setHapticsEnabled(enabled: Boolean)
}
