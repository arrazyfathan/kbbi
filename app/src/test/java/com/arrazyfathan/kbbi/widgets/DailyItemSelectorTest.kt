package com.arrazyfathan.kbbi.widgets

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class DailyItemSelectorTest {
    @Test
    fun `selection is deterministic for a fixed date`() {
        val items = listOf("alpha", "beta", "gamma")

        assertEquals(
            DailyItemSelector.select(items, 2026, 236),
            DailyItemSelector.select(items, 2026, 236),
        )
    }

    @Test
    fun `selection does not advance sequentially through sorted items`() {
        val items = (1..32).map { "word-$it" }
        val selections = (1..14).map { dayOfYear -> DailyItemSelector.select(items, 2026, dayOfYear) }
        val followsSequentialOrder =
            selections.zipWithNext().all { (current, next) ->
                next == items[(items.indexOf(current) + 1) % items.size]
            }

        assertTrue(selections.distinct().size > 1)
        assertFalse(followsSequentialOrder)
    }

    @Test
    fun `selection adapts to list size and empty input`() {
        val items = listOf("a", "b", "c")

        assertTrue(DailyItemSelector.select(items, 2026, 1) in items)
        assertEquals("only", DailyItemSelector.select(listOf("only"), 2026, 1))
        assertNull(DailyItemSelector.select(emptyList<String>(), 2026, 1))
    }

    @Test
    fun `saved words are filtered sorted and selected deterministically`() {
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
