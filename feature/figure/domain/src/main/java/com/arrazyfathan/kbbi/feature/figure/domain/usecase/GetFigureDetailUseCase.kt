package com.arrazyfathan.kbbi.feature.figure.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository

class GetFigureDetailUseCase(
    private val repository: FigureRepository,
) {
    suspend operator fun invoke(slug: String): AppResult<FigureModel, DataError> {
        val normalizedSlug = slug.trim().replace(WHITESPACE, "_")
        if (normalizedSlug.isBlank()) {
            return AppResult.Error(DataError.EmptyQuery)
        }

        return repository.getFigure(normalizedSlug)
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
