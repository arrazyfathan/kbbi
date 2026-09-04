package com.arrazyfathan.kbbi.core.observability

import org.junit.Assert.assertEquals
import org.junit.Test

class PerformanceUrlSanitizationTest {
    @Test
    fun `redacts words and strips query parameters`() {
        assertEquals(
            "https://api.example.com/search/_redacted_",
            sanitizePerformanceUrl("https://api.example.com/search/rahasia?q=rahasia"),
        )
        assertEquals(
            "https://api.example.com/translate/_redacted_",
            sanitizePerformanceUrl("https://api.example.com/translate/kata?to=en"),
        )
    }

    @Test
    fun `preserves only safe static routes`() {
        assertEquals("https://api.example.com/proverb/search", sanitizePerformanceUrl("https://api.example.com/proverb/search?q=rahasia"))
        assertEquals("https://api.example.com/proverb/_redacted_", sanitizePerformanceUrl("https://api.example.com/proverb/judul-rahasia"))
        assertEquals("https://api.example.com/unknown", sanitizePerformanceUrl("https://api.example.com/private/rahasia"))
    }
}
