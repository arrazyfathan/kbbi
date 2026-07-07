package com.arrazyfathan.kbbi.feature.home.domain.usecase

import kotlin.math.abs

class GetWordSuggestionsUseCase {
    operator fun invoke(
        query: String,
        words: List<String>,
        maxSuggestions: Int = DEFAULT_MAX_SUGGESTIONS,
    ): List<String> {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.length < MIN_SUGGESTION_QUERY_LENGTH) return emptyList()

        val normalizedWords =
            words
                .asSequence()
                .map { word ->
                    WordCandidate(
                        original = word,
                        normalized = word.trim().lowercase(),
                    )
                }
                .filter { candidate -> candidate.normalized.isNotBlank() }
                .distinctBy { candidate -> candidate.normalized }
                .toList()

        val prefixMatches =
            normalizedWords
                .asSequence()
                .filter { candidate -> candidate.normalized.startsWith(normalizedQuery) }

        val containsMatches =
            normalizedWords
                .asSequence()
                .filter { candidate ->
                    normalizedQuery in candidate.normalized && !candidate.normalized.startsWith(normalizedQuery)
                }

        val exactMatches =
            (prefixMatches + containsMatches)
                .map { candidate -> candidate.original }
                .distinct()
                .take(maxSuggestions)
                .toList()

        if (exactMatches.size >= maxSuggestions || normalizedQuery.length < MIN_FUZZY_QUERY_LENGTH) {
            return exactMatches
        }

        val exactNormalized = exactMatches.map { it.trim().lowercase() }.toSet()
        val maxDistance = maxEditDistance(normalizedQuery.length)
        val fuzzyMatches =
            normalizedWords
                .asSequence()
                .filter { candidate -> candidate.normalized !in exactNormalized }
                .filter { candidate -> isLengthClose(normalizedQuery, candidate.normalized, maxDistance) }
                .mapNotNull { candidate ->
                    val distance = boundedEditDistance(normalizedQuery, candidate.normalized, maxDistance)
                    if (distance == null) {
                        null
                    } else {
                        RankedWordCandidate(
                            original = candidate.original,
                            normalized = candidate.normalized,
                            distance = distance,
                            firstCharacterMatches =
                                candidate.normalized.firstOrNull() == normalizedQuery.firstOrNull(),
                            lengthDelta = abs(candidate.normalized.length - normalizedQuery.length),
                        )
                    }
                }
                .sortedWith(
                    compareBy<RankedWordCandidate> { it.distance }
                        .thenByDescending { it.firstCharacterMatches }
                        .thenBy { it.lengthDelta }
                        .thenBy { it.normalized },
                ).map { candidate -> candidate.original }
                .take(maxSuggestions - exactMatches.size)
                .toList()

        return exactMatches + fuzzyMatches
    }

    private fun maxEditDistance(queryLength: Int): Int =
        when {
            queryLength <= 4 -> 1
            else -> 2
        }

    private fun isLengthClose(
        query: String,
        candidate: String,
        maxDistance: Int,
    ): Boolean = abs(query.length - candidate.length) <= maxDistance

    private fun boundedEditDistance(
        query: String,
        candidate: String,
        maxDistance: Int,
    ): Int? {
        if (query == candidate) return 0
        if (abs(query.length - candidate.length) > maxDistance) return null

        val previousPrevious = IntArray(candidate.length + 1)
        var previous = IntArray(candidate.length + 1) { it }
        var current = IntArray(candidate.length + 1)

        for (i in 1..query.length) {
            current[0] = i
            var rowMinimum = current[0]

            for (j in 1..candidate.length) {
                val substitutionCost = if (query[i - 1] == candidate[j - 1]) 0 else 1
                val deletion = previous[j] + 1
                val insertion = current[j - 1] + 1
                val substitution = previous[j - 1] + substitutionCost
                var best = minOf(deletion, insertion, substitution)

                if (
                    i > 1 &&
                    j > 1 &&
                    query[i - 1] == candidate[j - 2] &&
                    query[i - 2] == candidate[j - 1]
                ) {
                    best = minOf(best, previousPrevious[j - 2] + 1)
                }

                current[j] = best
                rowMinimum = minOf(rowMinimum, best)
            }

            if (rowMinimum > maxDistance) return null

            previous.copyInto(previousPrevious)
            val oldPrevious = previous
            previous = current
            current = oldPrevious
        }

        return previous[candidate.length].takeIf { it <= maxDistance }
    }

    private data class WordCandidate(
        val original: String,
        val normalized: String,
    )

    private data class RankedWordCandidate(
        val original: String,
        val normalized: String,
        val distance: Int,
        val firstCharacterMatches: Boolean,
        val lengthDelta: Int,
    )

    private companion object {
        const val MIN_SUGGESTION_QUERY_LENGTH = 2
        const val MIN_FUZZY_QUERY_LENGTH = 3
        const val DEFAULT_MAX_SUGGESTIONS = 8
    }
}
