package com.arrazyfathan.kbbi.core.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
    ): Boolean

    suspend fun deleteWord(word: String)

    fun checkIfWordIsSaved(word: String): Flow<Boolean>

    fun getBookmarks(): Flow<List<ListWordModel>>
}
