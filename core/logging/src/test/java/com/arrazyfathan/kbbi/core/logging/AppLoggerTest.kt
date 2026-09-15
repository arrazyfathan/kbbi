package com.arrazyfathan.kbbi.core.logging

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

class AppLoggerTest {
    private val entries = mutableListOf<LogEntry>()

    @Before
    fun setUp() {
        AppLogger.installRemoteSink { level, tag, message, throwable ->
            entries += LogEntry(level, tag, message, throwable)
        }
    }

    @After
    fun tearDown() {
        AppLogger.installRemoteSink(null)
    }

    @Test
    fun errorThrowableMessageWritesOnce() {
        val throwable = IllegalStateException("request failed")

        AppLogger.error("Test", throwable, "Request failed")

        assertEquals(1, entries.size)
        assertEquals(AppLogger.LogLevel.Error, entries.single().level)
        assertEquals("Test", entries.single().tag)
        assertEquals("Request failed", entries.single().message)
        assertSame(throwable, entries.single().throwable)
    }

    @Test
    fun throwableWritesOnce() {
        val throwable = IllegalStateException("request failed")

        AppLogger.throwable("Test", throwable, "Request failed")

        assertEquals(1, entries.size)
        assertSame(throwable, entries.single().throwable)
    }

    private data class LogEntry(
        val level: AppLogger.LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
    )
}
