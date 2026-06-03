package com.arrazyfathan.kbbi.core.data.source.local

import android.content.Context
import com.arrazyfathan.kbbi.core.domain.repository.WordCatalogRepository
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetWordCatalogRepository(
    private val context: Context,
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

            val wordListType = object : TypeToken<List<String>>() {}.type
            GsonBuilder().create().fromJson<List<String>>(jsonString, wordListType)
        }

    private companion object {
        const val WORD_ENTRIES_ASSET = "entries.json"
    }
}
