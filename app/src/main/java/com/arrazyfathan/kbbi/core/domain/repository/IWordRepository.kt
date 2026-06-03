package com.arrazyfathan.kbbi.core.domain.repository

import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
interface IWordRepository {
    fun getMeaningOfWord(word: String): Flow<AppResult<List<WordModel>, DataError>>

    suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        isSaved: Boolean,
    ): Long

    suspend fun addToHistory(history: HistoryModel)

    fun getAllHistories(): Flow<List<HistoryModel>>

    suspend fun deleteWord(word: String)

    fun checkIfWordIsSaved(word: String): Flow<Boolean>

    fun getBookmarks(): Flow<List<ListWordModel>>
}
