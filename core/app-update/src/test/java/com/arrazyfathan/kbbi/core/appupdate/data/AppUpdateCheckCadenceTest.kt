package com.arrazyfathan.kbbi.core.appupdate.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppUpdateCheckCadenceTest {
    @Test
    fun `successful check waits twenty four hours`() {
        val checkedAt = 1_000L

        assertFalse(AppUpdateCheckCadence.shouldRun(checkedAt + HOURS_23, checkedAt, checkedAt))
        assertTrue(AppUpdateCheckCadence.shouldRun(checkedAt + HOURS_24, checkedAt, checkedAt))
    }

    @Test
    fun `failed check retries after one hour`() {
        val attemptedAt = 10_000L

        assertFalse(AppUpdateCheckCadence.shouldRun(attemptedAt + MINUTES_59, attemptedAt, 0L))
        assertTrue(AppUpdateCheckCadence.shouldRun(attemptedAt + HOURS_1, attemptedAt, 0L))
    }

    private companion object {
        const val MINUTES_59 = 59L * 60L * 1_000L
        const val HOURS_1 = 60L * 60L * 1_000L
        const val HOURS_23 = 23L * HOURS_1
        const val HOURS_24 = 24L * HOURS_1
    }
}
