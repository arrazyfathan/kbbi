package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Test

class AdaptiveLayoutTest {
    @Test
    fun `width policy uses compact medium and expanded boundaries`() {
        assertEquals(KbbiWindowWidthSizeClass.Compact, kbbiWindowWidthSizeClass(599.dp))
        assertEquals(KbbiWindowWidthSizeClass.Medium, kbbiWindowWidthSizeClass(600.dp))
        assertEquals(KbbiWindowWidthSizeClass.Medium, kbbiWindowWidthSizeClass(839.dp))
        assertEquals(KbbiWindowWidthSizeClass.Expanded, kbbiWindowWidthSizeClass(840.dp))
    }
}
