package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository

class GetWordEntriesUseCase(
    private val wordCatalogRepository: WordCatalogRepository,
) {
    suspend operator fun invoke(): List<String> = wordCatalogRepository.getWords()
}
