package com.example.kbbikamusbesarbahasaindonesia.database

import androidx.room.*
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

}