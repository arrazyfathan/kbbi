package com.example.kbbikamusbesarbahasaindonesia.database

import androidx.room.*
import com.example.kbbikamusbesarbahasaindonesia.model.History
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import kotlinx.coroutines.flow.Flow

@Dao
interface KataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kata: Kata?)

    @Query("SELECT * FROM table_kata")
    fun getAllKata(): Flow<List<Kata>>

    @Delete
    suspend fun delete(kata: Kata)

    @Query("SELECT EXISTS (SELECT * FROM table_kata WHERE id = :id)")
    fun isExists(id: String): Boolean

    @Query("SELECT EXISTS (SELECT * FROM table_history WHERE kata = :kata)")
    fun isHistoryExist(kata: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: History)

    @Query("SELECT * FROM table_history ORDER BY id DESC")
    fun getListHistory(): Flow<List<History>>
}