package com.arrazyfathan.kbbi.feature.settings.data

import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStoreUiPreferencesRepositoryTest {
    private val themeKey = stringPreferencesKey("theme")

    @Test
    fun `missing or invalid theme defaults to royal ocean`() {
        assertEquals(AppTheme.ROYAL_OCEAN, preferencesOf().toUiPreferences().theme)
        assertEquals(
            AppTheme.ROYAL_OCEAN,
            preferencesOf(themeKey to "invalid").toUiPreferences().theme,
        )
    }

    @Test
    fun `every persisted theme key is restored`() {
        AppTheme.entries.forEach { theme ->
            assertEquals(
                theme,
                preferencesOf(themeKey to theme.storageKey).toUiPreferences().theme,
            )
        }
    }
}
