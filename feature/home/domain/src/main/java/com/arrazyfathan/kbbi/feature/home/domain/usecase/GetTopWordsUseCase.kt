package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.TopWordsRepository

private const val DEFAULT_TOP_WORDS_LIMIT = 10
private const val MAX_TOP_WORDS_LIMIT = 100

class GetTopWordsUseCase(
    private val repository: TopWordsRepository,
) {
    suspend operator fun invoke(
        limit: Int = DEFAULT_TOP_WORDS_LIMIT,
    ): AppResult<List<TopWordModel>, DataError> =
        repository.getTopWords(
            limit = limit.takeIf { it > 0 }?.coerceAtMost(MAX_TOP_WORDS_LIMIT) ?: DEFAULT_TOP_WORDS_LIMIT,
        )

    suspend fun cached(limit: Int = DEFAULT_TOP_WORDS_LIMIT): List<TopWordModel> =
        repository.getCachedTopWords(
            limit = limit.takeIf { it > 0 }?.coerceAtMost(MAX_TOP_WORDS_LIMIT) ?: DEFAULT_TOP_WORDS_LIMIT,
        )
}
