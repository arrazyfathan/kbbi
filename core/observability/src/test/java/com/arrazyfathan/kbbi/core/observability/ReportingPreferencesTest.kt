package com.arrazyfathan.kbbi.core.observability

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportingPreferencesTest {
    @Test
    fun `missing preferences use privacy defaults`() {
        val preferences = preferencesOf().toReportingPreferences()

        assertTrue(preferences.crashReportingEnabled)
        assertFalse(preferences.analyticsEnabled)
    }

    @Test
    fun `stored preferences are restored independently`() {
        val preferences =
            preferencesOf(
                booleanPreferencesKey("crash_reporting_enabled") to false,
                booleanPreferencesKey("analytics_enabled") to true,
            ).toReportingPreferences()

        assertFalse(preferences.crashReportingEnabled)
        assertTrue(preferences.analyticsEnabled)
    }
}
