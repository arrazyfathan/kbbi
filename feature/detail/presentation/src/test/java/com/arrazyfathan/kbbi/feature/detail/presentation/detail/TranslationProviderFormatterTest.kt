package com.arrazyfathan.kbbi.feature.detail.presentation.detail

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationProviderFormatterTest {
    @Test
    fun `known provider names use branded capitalization`() {
        assertEquals("Google", translationProviderDisplayName("google"))
        assertEquals("Lara", translationProviderDisplayName("LARA"))
    }

    @Test
    fun `unknown provider name is trimmed and capitalized`() {
        assertEquals("FutureProvider", translationProviderDisplayName("  futureProvider  "))
    }

    @Test
    fun `provider is shown only for an enabled translation with a value`() {
        assertTrue(shouldShowTranslationProvider(true, "google"))
        assertFalse(shouldShowTranslationProvider(false, "google"))
        assertFalse(shouldShowTranslationProvider(true, null))
        assertFalse(shouldShowTranslationProvider(true, "  "))
    }
}
