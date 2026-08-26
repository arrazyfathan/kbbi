package com.arrazyfathan.kbbi.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WidgetLaunchRequestTest {
    @Test
    fun `parser maps every widget action`() {
        assertEquals(WidgetLaunchRequest.QuickSearch, WidgetLaunchRequest.parse(ACTION_WIDGET_QUICK_SEARCH, null))
        assertEquals(
            WidgetLaunchRequest.WordOfDay("bahasa"),
            WidgetLaunchRequest.parse(ACTION_WIDGET_WORD_OF_DAY, " Bahasa "),
        )
        assertEquals(
            WidgetLaunchRequest.SavedWord("makna"),
            WidgetLaunchRequest.parse(ACTION_WIDGET_SAVED_WORD, "makna"),
        )
    }

    @Test
    fun `recognized data actions retain missing and blank words as empty state`() {
        assertEquals(WidgetLaunchRequest.WordOfDay(null), WidgetLaunchRequest.parse(ACTION_WIDGET_WORD_OF_DAY, null))
        assertEquals(WidgetLaunchRequest.WordOfDay(null), WidgetLaunchRequest.parse(ACTION_WIDGET_WORD_OF_DAY, "  "))
        assertEquals(WidgetLaunchRequest.SavedWord(null), WidgetLaunchRequest.parse(ACTION_WIDGET_SAVED_WORD, null))
    }

    @Test
    fun `unknown actions are ignored`() {
        assertNull(WidgetLaunchRequest.parse(null, "word"))
        assertNull(WidgetLaunchRequest.parse("unknown", "word"))
    }

    @Test
    fun `repeated identical requests remain parseable`() {
        val first = WidgetLaunchRequest.parse(ACTION_WIDGET_SAVED_WORD, "kata")
        val second = WidgetLaunchRequest.parse(ACTION_WIDGET_SAVED_WORD, "kata")

        assertEquals(first, second)
        assertEquals(WidgetLaunchRequest.SavedWord("kata"), second)
    }
}
