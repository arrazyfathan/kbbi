package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.repository.WordCatalogRepository

class GetWordEntriesUseCase(
    private val wordCatalogRepository: WordCatalogRepository,
) {
    suspend operator fun invoke(): List<String> = wordCatalogRepository.getWords()
}
