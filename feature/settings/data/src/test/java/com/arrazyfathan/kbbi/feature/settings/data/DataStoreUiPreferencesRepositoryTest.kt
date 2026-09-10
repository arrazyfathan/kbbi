package com.arrazyfathan.kbbi.feature.settings.data

import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.feature.settings.domain.model.BookmarkLayout
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStoreUiPreferencesRepositoryTest {
    private val themeKey = stringPreferencesKey("theme")
    private val bookmarkLayoutKey = stringPreferencesKey("bookmark_layout")

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

    @Test
    fun `missing or invalid bookmark layout defaults to grid`() {
        assertEquals(BookmarkLayout.GRID, preferencesOf().toUiPreferences().bookmarkLayout)
        assertEquals(
            BookmarkLayout.GRID,
            preferencesOf(bookmarkLayoutKey to "invalid").toUiPreferences().bookmarkLayout,
        )
    }

    @Test
    fun `every persisted bookmark layout key is restored`() {
        BookmarkLayout.entries.forEach { layout ->
            assertEquals(
                layout,
                preferencesOf(bookmarkLayoutKey to layout.storageKey).toUiPreferences().bookmarkLayout,
            )
        }
    }
}
