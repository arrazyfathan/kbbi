package com.arrazyfathan.kbbi.feature.home.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetWordSuggestionsUseCaseTest {
    private val getWordSuggestions = GetWordSuggestionsUseCase()

    @Test
    fun prefixAndContainsMatchesRankBeforeFuzzyMatches() {
        val words = listOf("belajar", "membela", "bola")

        val suggestions = getWordSuggestions(query = "bela", words = words)

        assertEquals(listOf("belajar", "membela", "bola"), suggestions.take(3))
    }

    @Test
    fun oneEditTypoReturnsCloseCandidate() {
        val words = listOf("buku", "buka", "baki")

        val suggestions = getWordSuggestions(query = "bku", words = words)

        assertEquals("buku", suggestions.first())
    }

    @Test
    fun transposedCharactersReturnCloseCandidate() {
        val words = listOf("kamus", "kamis", "kuman")

        val suggestions = getWordSuggestions(query = "kaums", words = words)

        assertEquals("kamus", suggestions.first())
    }

    @Test
    fun matchingIsCaseInsensitiveAndTrimsInput() {
        val words = listOf("Belajar", "Bekerja")

        val suggestions = getWordSuggestions(query = "  belajar  ", words = words)

        assertEquals(listOf("Belajar"), suggestions.take(1))
    }

    @Test
    fun veryShortQueryDoesNotReturnSuggestions() {
        val words = listOf("aku", "akan")

        val suggestions = getWordSuggestions(query = "a", words = words)

        assertTrue(suggestions.isEmpty())
    }

    @Test
    fun noCloseMatchesReturnsEmptyList() {
        val words = listOf("buku", "kamus", "pohon")

        val suggestions = getWordSuggestions(query = "xyz", words = words)

        assertTrue(suggestions.isEmpty())
    }
}
