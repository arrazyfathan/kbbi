package com.arrazyfathan.kbbi.core.observability

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsEventTest {
    @Test
    fun `search event contains only low-cardinality context`() {
        val event =
            AnalyticsEvent.SearchCompleted(
                source = EventSource.VoiceSearch,
                inputMethod = InputMethod.Voice,
                outcome = EventOutcome.Success,
                suggestionUsed = false,
            )

        assertEquals("search_completed", event.name)
        assertEquals("voice_search", event.parameters["source"])
        assertEquals("voice", event.parameters["input_method"])
        assertFalse(event.parameters.keys.any { it.contains("term") || it.contains("query") })
    }

    @Test
    fun `all event names and parameter names satisfy Firebase naming rules`() {
        val events =
            listOf(
                AnalyticsEvent.SearchCompleted(EventSource.TypedSearch, InputMethod.Text, EventOutcome.Success),
                AnalyticsEvent.ContentOpened(ContentType.Word, EventSource.WordList),
                AnalyticsEvent.BookmarkChanged(BookmarkAction.Added, AnalyticsScreen.WordDetail),
                AnalyticsEvent.TranslationChanged(TranslationAction.Enabled, EventOutcome.Success, false),
                AnalyticsEvent.ProverbOpened(EventOutcome.Success),
                AnalyticsEvent.ReminderChanged(ReminderKind.DailyWord, true),
                AnalyticsEvent.NotificationOpened(ReminderKind.BookmarkReview),
                AnalyticsEvent.WidgetOpened(WidgetKind.QuickSearch),
                AnalyticsEvent.AppUpdateInteraction(UpdateAction.Download, EventOutcome.Started),
                AnalyticsEvent.AppShared,
            )

        val firebaseName = Regex("[A-Za-z][A-Za-z0-9_]{0,39}")
        events.forEach { event ->
            assertTrue(event.name, firebaseName.matches(event.name))
            event.parameters.keys.forEach { key -> assertTrue(key, firebaseName.matches(key)) }
        }
    }
}
