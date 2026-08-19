package com.arrazyfathan.kbbi.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveNavigationTest {
    @Test
    fun `top level destinations show navigation`() {
        assertTrue(shouldShowNavigation(Screen.Home, Screen.Home))
        assertTrue(shouldShowNavigation(Screen.WordList, Screen.WordList))
        assertTrue(shouldShowNavigation(Screen.Bookmarks, Screen.Bookmarks))
    }

    @Test
    fun `secondary destinations hide navigation`() {
        assertFalse(shouldShowNavigation(Screen.Settings, Screen.Home))
        assertFalse(shouldShowNavigation(Screen.Proverb, Screen.Home))
        assertFalse(shouldShowNavigation(OpenSourceLicensesRoute, Screen.Home))
    }

    @Test
    fun `detail keeps navigation only for persistent list origins`() {
        val detail = DetailNavRoute("{}")

        assertTrue(shouldShowNavigation(detail, Screen.WordList))
        assertTrue(shouldShowNavigation(detail, Screen.Bookmarks))
        assertFalse(shouldShowNavigation(detail, Screen.Home))
    }
}
