package com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase

import com.example.kbbikamusbesarbahasaindonesia.core.data.Resource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
interface WordUseCase {
    fun getMeaningOfWord(word: String): Flow<Resource<List<WordModel>>>
    suspend fun bookmarkWord(word: String, wordList: List<WordModel>, isSaved: Boolean): Long
    suspend fun addToHistory(historyEntity: HistoryEntity)
    fun getAllHistories(): Flow<List<HistoryEntity>>
    suspend fun deleteWord(word: String)
    fun checkIfWordIsSaved(word: String): Flow<Boolean>
}
