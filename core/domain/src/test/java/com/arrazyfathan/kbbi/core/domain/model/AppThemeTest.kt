package com.arrazyfathan.kbbi.core.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppThemeTest {
    @Test
    fun `every storage key round trips`() {
        AppTheme.entries.forEach { theme ->
            assertEquals(theme, AppTheme.fromStorageKey(theme.storageKey))
        }
    }

    @Test
    fun `missing or unknown storage keys default to royal ocean`() {
        assertEquals(AppTheme.ROYAL_OCEAN, AppTheme.fromStorageKey(null))
        assertEquals(AppTheme.ROYAL_OCEAN, AppTheme.fromStorageKey("unknown"))
    }
}
