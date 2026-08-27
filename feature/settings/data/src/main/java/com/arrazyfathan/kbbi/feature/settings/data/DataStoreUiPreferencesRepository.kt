package com.arrazyfathan.kbbi.feature.settings.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.feature.settings.domain.model.UiPreferences
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiPreferencesDataStore by preferencesDataStore(name = "ui_preferences")

class DataStoreUiPreferencesRepository(
    private val context: Context,
) : UiPreferencesRepository {
    override val preferences: Flow<UiPreferences> =
        context.uiPreferencesDataStore.data.map(Preferences::toUiPreferences)

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.uiPreferencesDataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED] = enabled
        }
    }

    override suspend fun setTheme(theme: AppTheme) {
        context.uiPreferencesDataStore.edit { preferences ->
            preferences[THEME] = theme.storageKey
        }
    }
}

private val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
private val THEME = stringPreferencesKey("theme")

internal fun Preferences.toUiPreferences(): UiPreferences =
    UiPreferences(
        hapticsEnabled = this[HAPTICS_ENABLED] ?: true,
        theme = AppTheme.fromStorageKey(this[THEME]),
    )
