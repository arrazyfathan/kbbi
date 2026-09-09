package com.arrazyfathan.kbbi.feature.figure.domain.model

data class FigurePageModel(
    val items: List<FigureModel>,
    val page: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
)
