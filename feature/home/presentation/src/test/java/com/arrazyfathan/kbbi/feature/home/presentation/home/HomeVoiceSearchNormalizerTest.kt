package com.arrazyfathan.kbbi.feature.home.presentation.home

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeVoiceSearchNormalizerTest {
    @Test
    fun typedSearchRemovesAllWhitespace() {
        assertEquals("rumahsakit", normalizeTypedSearchQuery(" rumah   sakit "))
    }

    @Test
    fun voiceSearchTrimsPunctuationAndExtraSpaces() {
        assertEquals("belajar", normalizeVoiceSearchQuery("  Belajar!!!  ", emptyList()))
    }

    @Test
    fun voiceSearchUsesHyphenatedCatalogMatchForMultiWordSpeech() {
        assertEquals(
            "kupu-kupu",
            normalizeVoiceSearchQuery("Kupu kupu", listOf("kupu-kupu")),
        )
    }

    @Test
    fun voiceSearchDoesNotSilentlyMergeUnknownMultiWordSpeech() {
        assertEquals(
            "rumah sakit",
            normalizeVoiceSearchQuery("rumah sakit", emptyList()),
        )
    }

    @Test
    fun voiceSearchCandidatesPreferCatalogMatch() {
        assertEquals(
            "belajar",
            normalizeVoiceSearchCandidates(
                recognizedTexts = listOf("pelajar", "belajar"),
                wordEntries = listOf("belajar"),
            ),
        )
    }
}
