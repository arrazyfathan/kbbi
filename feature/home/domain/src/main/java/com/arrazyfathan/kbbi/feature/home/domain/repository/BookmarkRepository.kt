package com.arrazyfathan.kbbi.feature.home.domain.repository

import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {
    suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        visitorCount: Int? = null,
    ): Boolean

    suspend fun deleteWord(word: String)

    fun checkIfWordIsSaved(word: String): Flow<Boolean>

    fun getBookmarks(): Flow<List<ListWordModel>>
}
