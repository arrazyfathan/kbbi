package com.arrazyfathan.kbbi.feature.proverb.domain.model

data class ProverbModel(
    val text: String,
    val letter: String,
    val slug: String,
    val sourceUrl: String?,
)
