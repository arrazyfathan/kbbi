package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.TranslateRepository

class GetWordTranslationUseCase(
    private val translateRepository: TranslateRepository,
) {
    suspend operator fun invoke(word: String): AppResult<TranslateModel, DataError> {
        val wordToTranslate = word.trim()
        if (wordToTranslate.isBlank()) {
            return AppResult.Error(DataError.EmptyQuery)
        }

        return translateRepository.getTranslation(wordToTranslate)
    }
}
