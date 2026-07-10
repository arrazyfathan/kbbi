package com.arrazyfathan.kbbi.feature.home.presentation.home

import java.util.Locale

internal fun normalizeTypedSearchQuery(query: String): String = query.trim().replace(Regex("\\s+"), "")

internal fun normalizeVoiceSearchQuery(
    recognizedText: String,
    wordEntries: List<String>,
): String {
    val cleanedPhrase =
        recognizedText
            .lowercase(Locale.forLanguageTag("id-ID"))
            .replace(Regex("[^\\p{L}\\p{N}'-]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")

    if (cleanedPhrase.isBlank()) return ""

    val entries = wordEntries.toHashSet()
    if (cleanedPhrase in entries) return cleanedPhrase

    val tokens = cleanedPhrase.split(" ")
    if (tokens.size == 1) return tokens.first()

    val candidates =
        listOf(
            tokens.joinToString("-"),
            tokens.joinToString(""),
        )

    return candidates.firstOrNull { it in entries } ?: cleanedPhrase
}

internal fun normalizeVoiceSearchCandidates(
    recognizedTexts: List<String>,
    wordEntries: List<String>,
): String {
    val normalizedCandidates =
        recognizedTexts
            .map { normalizeVoiceSearchQuery(it, wordEntries) }
            .filter { it.isNotBlank() }

    return normalizedCandidates.firstOrNull { it in wordEntries.toHashSet() }
        ?: normalizedCandidates.firstOrNull()
        ?: ""
}
