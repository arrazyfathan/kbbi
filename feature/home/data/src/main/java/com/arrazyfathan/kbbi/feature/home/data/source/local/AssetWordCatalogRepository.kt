package com.arrazyfathan.kbbi.feature.home.data.source.local

import android.content.Context
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class AssetWordCatalogRepository(
    private val context: Context,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WordCatalogRepository {
    private val cacheMutex = Mutex()
    private var cachedWords: List<String>? = null

    override suspend fun getWords(): List<String> =
        withContext(ioDispatcher) {
            cacheMutex.withLock {
                cachedWords ?: loadWords().also { cachedWords = it }
            }
        }

    private fun loadWords(): List<String> =
        try {
            val jsonString =
                context.assets
                    .open(WORD_ENTRIES_ASSET)
                    .bufferedReader()
                    .use { it.readText() }
            json.decodeFromString<List<String>>(jsonString)
        } catch (_: Exception) {
            emptyList()
        }

    private companion object {
        const val WORD_ENTRIES_ASSET = "entries.json"
    }
}
