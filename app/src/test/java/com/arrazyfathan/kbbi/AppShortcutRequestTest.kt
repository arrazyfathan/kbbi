package com.arrazyfathan.kbbi

import com.arrazyfathan.kbbi.navigation.AppShortcutRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppShortcutRequestTest {
    @Test
    fun `fromAction maps known shortcut actions`() {
        assertEquals(
            AppShortcutRequest.Search,
            AppShortcutRequest.fromAction("com.arrazyfathan.kbbi.action.SHORTCUT_SEARCH"),
        )
        assertEquals(
            AppShortcutRequest.Bookmarks,
            AppShortcutRequest.fromAction("com.arrazyfathan.kbbi.action.SHORTCUT_BOOKMARKS"),
        )
        assertEquals(
            AppShortcutRequest.Proverbs,
            AppShortcutRequest.fromAction("com.arrazyfathan.kbbi.action.SHORTCUT_PROVERBS"),
        )
        assertEquals(
            AppShortcutRequest.RandomWord,
            AppShortcutRequest.fromAction("com.arrazyfathan.kbbi.action.SHORTCUT_RANDOM_WORD"),
        )
    }

    @Test
    fun `fromAction ignores unknown actions`() {
        assertNull(AppShortcutRequest.fromAction(null))
        assertNull(AppShortcutRequest.fromAction("android.intent.action.MAIN"))
        assertNull(AppShortcutRequest.fromAction("unknown"))
    }
}
