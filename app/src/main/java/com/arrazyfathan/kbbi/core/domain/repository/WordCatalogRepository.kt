package com.arrazyfathan.kbbi.core.domain.repository

interface WordCatalogRepository {
    suspend fun getWords(): List<String>
}
