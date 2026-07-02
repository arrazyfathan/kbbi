package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */

@Dao
interface WordDao {
    @Query("SELECT * FROM word_table WHERE isSaved = 1")
    fun getSavedWords(): Flow<List<ListWordEntity>>

    @Query("SELECT * FROM word_table WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): ListWordEntity?

    @Upsert
    suspend fun insertWord(listWordEntity: ListWordEntity): Long

    @Query("UPDATE word_table SET isSaved = 0 WHERE word = :word")
    suspend fun unbookmarkWord(word: String)

    @Query("SELECT EXISTS (SELECT * FROM word_table WHERE word = :word AND isSaved = 1)")
    fun checkWordIsExist(word: String): Flow<Boolean>

    @Upsert
    suspend fun insertHistory(historyEntity: HistoryEntity)

    @Query(
        "DELETE FROM history_table WHERE word NOT IN (SELECT word FROM history_table ORDER BY searchedAt DESC, word DESC LIMIT :limit)",
    )
    suspend fun trimHistories(limit: Int)

    @Transaction
    suspend fun insertHistoryAndTrim(
        historyEntity: HistoryEntity,
        limit: Int = 5,
    ) {
        insertHistory(historyEntity)
        trimHistories(limit)
    }

    @Query("SELECT * FROM history_table ORDER BY searchedAt DESC, word DESC")
    fun getListHistory(): Flow<List<HistoryEntity>>
}
