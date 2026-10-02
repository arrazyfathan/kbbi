package com.arrazyfathan.kbbi.feature.proverb.domain.model

data class ProverbDetailModel(
    val text: String,
    val letter: String,
    val slug: String,
    val sourceUrl: String?,
    val meaning: String?,
    val aiGenerated: Boolean = false,
    val notice: String? = null,
)
