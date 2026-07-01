package com.arrazyfathan.kbbi.feature.proverb.domain.usecase

import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository

class GetProverbMeaningUseCase(
    private val repository: ProverbRepository,
) {
    suspend operator fun invoke(slug: String) = repository.getProverbMeaning(slug)
}
