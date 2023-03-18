package com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.room

import androidx.room.*
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

@Dao
interface WordDao {

    @Query("SELECT * FROM word_table")
    fun getAllWords(): Flow<List<ListWordEntity>>

    @Upsert
    suspend fun insertWord(listWordEntity: ListWordEntity): Long

    @Query("DELETE FROM word_table WHERE word = :word")
    suspend fun deleteWord(word: String)

    @Query("SELECT EXISTS (SELECT * FROM word_table WHERE word = :word)")
    fun checkWordIsExist(word: String): Flow<Boolean>

    @Upsert
    suspend fun insertHistory(historyEntity: HistoryEntity)

    @Query("SELECT * FROM history_table ORDER BY word DESC")
    fun getListHistory(): Flow<List<HistoryEntity>>
}
