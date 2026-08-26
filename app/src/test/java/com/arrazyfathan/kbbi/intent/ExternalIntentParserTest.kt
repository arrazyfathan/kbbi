package com.arrazyfathan.kbbi.intent

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExternalIntentParserTest {
    @Test
    fun `search query normalization extracts first word`() {
        assertEquals("makan", "  (Makan!) nasi".toKbbiSearchQuery())
        assertEquals("ber-jalan", "  BER-JALAN. kaki".toKbbiSearchQuery())
    }

    @Test
    fun `word deep link extracts path segment`() {
        assertEquals(
            "makan",
            extractWordDeepLinkQuery(
                scheme = "kbbi",
                host = "word",
                pathSegments = listOf("makan"),
            )?.toKbbiSearchQuery(),
        )
    }

    @Test
    fun `word deep link normalizes decoded query`() {
        assertEquals(
            "terima",
            extractWordDeepLinkQuery(
                scheme = "kbbi",
                host = "word",
                pathSegments = listOf("(Terima kasih!)"),
            )?.toKbbiSearchQuery(),
        )
    }

    @Test
    fun `empty word deep link path is ignored`() {
        assertNull(
            extractWordDeepLinkQuery(
                scheme = "kbbi",
                host = "word",
                pathSegments = emptyList(),
            )?.toKbbiSearchQuery(),
        )
    }

    @Test
    fun `unknown deep link host is ignored`() {
        assertNull(
            extractWordDeepLinkQuery(
                scheme = "kbbi",
                host = "unknown",
                pathSegments = listOf("makan"),
            )?.toKbbiSearchQuery(),
        )
    }

    @Test
    fun `wrong deep link scheme is ignored`() {
        assertNull(
            extractWordDeepLinkQuery(
                scheme = "https",
                host = "word",
                pathSegments = listOf("makan"),
            )?.toKbbiSearchQuery(),
        )
    }

    @Test
    fun `repeated same deep link still extracts same query`() {
        val firstQuery =
            extractWordDeepLinkQuery(
                scheme = "kbbi",
                host = "word",
                pathSegments = listOf("makan"),
            )?.toKbbiSearchQuery()
        val secondQuery =
            extractWordDeepLinkQuery(
                scheme = "kbbi",
                host = "word",
                pathSegments = listOf("makan"),
            )?.toKbbiSearchQuery()

        assertEquals("makan", firstQuery)
        assertEquals("makan", secondQuery)
    }
}
