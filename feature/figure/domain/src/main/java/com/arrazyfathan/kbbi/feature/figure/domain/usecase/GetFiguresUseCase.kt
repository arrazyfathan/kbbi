package com.arrazyfathan.kbbi.feature.figure.domain.usecase

import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository

class GetFiguresUseCase(
    private val repository: FigureRepository,
) {
    operator fun invoke(
        query: String = "",
        includeDetails: Boolean = false,
    ) = repository.getFigures(
        query = query.trim(),
        includeDetails = includeDetails,
    )
}
