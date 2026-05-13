package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.data.Resource
import com.arrazyfathan.kbbi.core.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
interface WordUseCase {
    fun getMeaningOfWord(word: String): Flow<Resource<List<WordModel>>>

    suspend fun bookmarkWord(
        word: String,
        wordList: List<WordModel>,
        isSaved: Boolean,
    ): Long

    suspend fun addToHistory(historyEntity: HistoryEntity)

    fun getAllHistories(): Flow<List<HistoryEntity>>

    suspend fun deleteWord(word: String)

    fun checkIfWordIsSaved(word: String): Flow<Boolean>

    fun getBookmarks(): Flow<List<ListWordModel>>
}
