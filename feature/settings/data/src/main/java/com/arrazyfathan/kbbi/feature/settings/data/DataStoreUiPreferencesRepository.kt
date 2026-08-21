package com.arrazyfathan.kbbi.feature.settings.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.arrazyfathan.kbbi.feature.settings.domain.model.UiPreferences
import com.arrazyfathan.kbbi.feature.settings.domain.repository.UiPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.uiPreferencesDataStore by preferencesDataStore(name = "ui_preferences")

class DataStoreUiPreferencesRepository(
    private val context: Context,
) : UiPreferencesRepository {
    override val preferences: Flow<UiPreferences> =
        context.uiPreferencesDataStore.data.map { preferences ->
            UiPreferences(
                hapticsEnabled = preferences[HAPTICS_ENABLED] ?: true,
            )
        }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        context.uiPreferencesDataStore.edit { preferences ->
            preferences[HAPTICS_ENABLED] = enabled
        }
    }

    private companion object {
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
    }
}
