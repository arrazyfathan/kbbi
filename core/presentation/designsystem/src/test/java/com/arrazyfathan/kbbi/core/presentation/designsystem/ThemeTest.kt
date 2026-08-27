package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.ui.graphics.Color
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeTest {
    @Test
    fun `themes map to their defined palette colors`() {
        AppTheme.entries.forEach { theme ->
            val scheme = colorSchemeFor(theme)
            assertEquals(theme.palette.primary, scheme.primary)
            assertEquals(theme.palette.secondary, scheme.secondary)
        }
    }

    @Test
    fun `lighter palette colors use dark foregrounds`() {
        assertEquals(TextPrimary, colorSchemeFor(AppTheme.GOLDEN_SUNSET).onPrimary)
        assertEquals(TextPrimary, colorSchemeFor(AppTheme.GOLDEN_SUNSET).onSecondary)
        assertEquals(TextPrimary, colorSchemeFor(AppTheme.GOLDEN_CORAL_ENERGY).onSecondary)
        assertEquals(Color.White, colorSchemeFor(AppTheme.GOLDEN_CORAL_ENERGY).onPrimary)
    }
}
