package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository
import kotlinx.coroutines.flow.Flow

class CheckWordSavedUseCase(
    private val wordRepository: IWordRepository,
) {
    operator fun invoke(word: String): Flow<Boolean> = wordRepository.checkIfWordIsSaved(word)
}
