package com.arrazyfathan.kbbi.feature.proverb.domain.model

data class ProverbPageModel(
    val items: List<ProverbModel>,
    val page: Int,
    val totalPages: Int,
    val hasNextPage: Boolean,
)
