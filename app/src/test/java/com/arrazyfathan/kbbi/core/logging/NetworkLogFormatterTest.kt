package com.arrazyfathan.kbbi.core.logging

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkLogFormatterTest {
    @Test
    fun formatRequestIncludesCopyableCurlCommand() {
        val formattedLog =
            NetworkLogFormatter.format(
                """
                REQUEST: https://kbbi-api-green.vercel.app/search/belajar
                METHOD: HttpMethod(value=GET)
                -> Accept: application/json
                -> Accept-Language: id
                -> Authorization: Bearer secret-token
                BODY START
                BODY END
                """.trimIndent(),
            )

        assertTrue(formattedLog.contains(" CURL "))
        assertTrue(formattedLog.contains("curl --location --request GET 'https://kbbi-api-green.vercel.app/search/belajar'"))
        assertTrue(formattedLog.contains("--header 'Accept: application/json'"))
        assertTrue(formattedLog.contains("--header 'Authorization: <redacted>'"))
        assertFalse(formattedLog.contains("secret-token"))
    }

    @Test
    fun formatPostRequestIncludesRedactedBodyInCurlCommand() {
        val formattedLog =
            NetworkLogFormatter.format(
                """
                REQUEST: https://kbbi-api-green.vercel.app/login
                METHOD: HttpMethod(value=POST)
                BODY Content-Type: application/json
                BODY START
                {"username":"tester","password":"secret-password"}
                BODY END
                """.trimIndent(),
            )

        assertTrue(formattedLog.contains("--request POST"))
        assertTrue(formattedLog.contains("--header 'Content-Type: application/json'"))
        assertTrue(formattedLog.contains("--data-raw '{\"username\":\"tester\",\"password\":\"<redacted>\"}'"))
        assertFalse(formattedLog.contains("secret-password"))
    }
}
