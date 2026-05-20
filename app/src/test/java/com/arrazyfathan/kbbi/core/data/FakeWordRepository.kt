package com.arrazyfathan.kbbi.core.data

import com.arrazyfathan.kbbi.core.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class FakeWordRepository : IWordRepository {

    private val bookmarks = MutableStateFlow<Map<String, ListWordModel>>(emptyMap())
    private val histories = MutableStateFlow<List<HistoryEntity>>(emptyList())
    private val remoteMeanings = mutableMapOf<String, Resource<List<WordModel>>>()

    fun setRemoteData(word: String, resource: Resource<List<WordModel>>) {
        remoteMeanings[word] = resource
    }

    override fun getMeaningOfWord(word: String): Flow<Resource<List<WordModel>>> = flow {
        val result = remoteMeanings[word] ?: Resource.Error("Word not found in remote source")
        emit(result)
    }

    override suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        isSaved: Boolean
    ): Long {
        val current = bookmarks.value.toMutableMap()
        if (isSaved) {
            current[word] = ListWordModel(word, result)
        } else {
            current.remove(word)
        }
        bookmarks.value = current
        return 1L
    }

    override suspend fun addToHistory(historyEntity: HistoryEntity) {
        val current = histories.value.toMutableList()
        current.add(historyEntity)
        histories.value = current
    }

    override fun getAllHistories(): Flow<List<HistoryEntity>> {
        return histories
    }

    override suspend fun deleteWord(word: String) {
        val current = bookmarks.value.toMutableMap()
        current.remove(word)
        bookmarks.value = current
    }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> {
        return bookmarks.map { it.containsKey(word) }
    }

    override fun getBookmarks(): Flow<List<ListWordModel>> {
        return bookmarks.map { it.values.toList() }
    }
}
