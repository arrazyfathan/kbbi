package com.arrazyfathan.kbbi.feature.settings.domain

import com.arrazyfathan.kbbi.feature.settings.domain.model.NotificationSettings
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NotificationSettingsTest {
    @Test
    fun `new settings disable every reminder and use staggered defaults`() {
        val settings = NotificationSettings()

        assertFalse(settings.dailyWord.enabled)
        assertFalse(settings.dailyProverb.enabled)
        assertFalse(settings.bookmarkReview.enabled)
        assertEquals(ReminderTime(9, 0), settings.preference(ReminderType.DAILY_WORD).time)
        assertEquals(ReminderTime(12, 0), settings.preference(ReminderType.DAILY_PROVERB).time)
        assertEquals(ReminderTime(19, 0), settings.preference(ReminderType.BOOKMARK_REVIEW).time)
    }

    @Test
    fun `reminder time exposes total minutes`() {
        assertEquals(1_145, ReminderTime(19, 5).totalMinutes)
    }
}
