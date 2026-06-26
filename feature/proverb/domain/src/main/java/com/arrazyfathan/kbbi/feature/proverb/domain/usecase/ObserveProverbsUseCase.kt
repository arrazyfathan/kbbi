package com.arrazyfathan.kbbi.feature.proverb.domain.usecase

import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository

class ObserveProverbsUseCase(
    private val repository: ProverbRepository,
) {
    operator fun invoke(query: String) = repository.observeProverbs(query)
}
