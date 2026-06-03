package com.arrazyfathan.kbbi.core.data.source.local

import android.content.Context
import com.arrazyfathan.kbbi.core.domain.repository.WordCatalogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class AssetWordCatalogRepository(
    private val context: Context,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WordCatalogRepository {
    override suspend fun getWords(): List<String> =
        withContext(ioDispatcher) {
            val jsonString =
                try {
                    context.assets
                        .open(WORD_ENTRIES_ASSET)
                        .bufferedReader()
                        .use { it.readText() }
                } catch (_: Exception) {
                    return@withContext emptyList()
                }

            json.decodeFromString<List<String>>(jsonString)
        }

    private companion object {
        const val WORD_ENTRIES_ASSET = "entries.json"
    }
}
