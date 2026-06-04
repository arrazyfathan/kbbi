package com.arrazyfathan.kbbi.feature.home.domain.repository

interface WordCatalogRepository {
    suspend fun getWords(): List<String>
}
