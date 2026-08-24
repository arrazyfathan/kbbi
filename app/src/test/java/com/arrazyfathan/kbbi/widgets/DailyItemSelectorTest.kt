package com.arrazyfathan.kbbi.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class DailyItemSelectorTest {
    @Test
    fun `selection is deterministic for a fixed date`() {
        assertEquals("gamma", DailyItemSelector.select(listOf("alpha", "beta", "gamma"), 2026, 236))
        assertEquals("gamma", DailyItemSelector.select(listOf("alpha", "beta", "gamma"), 2026, 236))
    }

    @Test
    fun `selection advances at date rollover`() {
        val items = listOf("one", "two", "three", "four")
        val first = DailyItemSelector.select(items, 2026, 236)
        val next = DailyItemSelector.select(items, 2026, 237)

        assertEquals(items[(items.indexOf(first) + 1) % items.size], next)
    }

    @Test
    fun `selection adapts to list size and empty input`() {
        assertEquals("b", DailyItemSelector.select(listOf("a", "b", "c"), 2026, 1))
        assertEquals("b", DailyItemSelector.select(listOf("a", "b"), 2026, 1))
        assertNull(DailyItemSelector.select(emptyList<String>(), 2026, 1))
    }

    @Test
    fun `saved words are filtered sorted and rotate deterministically`() {
        val date: Calendar = GregorianCalendar(2026, Calendar.AUGUST, 24)
        val words = listOf("zebra", "", "apel", "  ", "makan")

        assertEquals(
            DailyItemSelector.select(listOf("apel", "makan", "zebra"), date),
            DailyItemSelector.selectSortedWords(words, date),
        )
    }

    @Test
    fun `notification and widget selection share the same selector`() {
        val date: Calendar = GregorianCalendar(2026, Calendar.AUGUST, 24)
        val words = listOf("bahasa", "kamus", "makna")

        val notificationWord = DailyItemSelector.select(words, date)
        val widgetWord = DailyItemSelector.select(words, date)

        assertEquals(notificationWord, widgetWord)
    }
}
