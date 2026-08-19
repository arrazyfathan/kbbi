package com.arrazyfathan.kbbi.feature.home.domain

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeWordRepository :
    WordSearchRepository,
    BookmarkRepository,
    SearchHistoryRepository,
    WordCatalogRepository {
    private val bookmarks = MutableStateFlow<Map<String, ListWordModel>>(emptyMap())
    private val histories = MutableStateFlow<List<HistoryModel>>(emptyList())
    private val remoteMeanings = mutableMapOf<String, AppResult<ListWordModel, DataError>>()
    private var catalogWords = emptyList<String>()

    fun setRemoteData(
        word: String,
        result: AppResult<ListWordModel, DataError>,
    ) {
        remoteMeanings[word] = result
    }

    fun setCatalogWords(words: List<String>) {
        catalogWords = words
    }

    override suspend fun getMeaningOfWord(word: String): AppResult<ListWordModel, DataError> =
        remoteMeanings[word] ?: AppResult.Error(DataError.NotFound)

    override suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        visitorCount: Int?,
    ): Boolean {
        bookmarks.value = bookmarks.value + (word to ListWordModel(word, result, visitorCount))
        return true
    }

    override suspend fun deleteWord(word: String) {
        bookmarks.value = bookmarks.value - word
    }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> = bookmarks.map { it.containsKey(word) }

    override fun getBookmarks(): Flow<List<ListWordModel>> = bookmarks.map { it.values.toList() }

    override suspend fun addToHistory(history: HistoryModel) {
        histories.value = histories.value + history
    }

    override fun getAllHistories(): Flow<List<HistoryModel>> = histories

    override suspend fun clearHistory() {
        histories.value = emptyList()
    }

    override suspend fun getWords(): List<String> = catalogWords
}
