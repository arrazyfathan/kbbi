package com.example.kbbikamusbesarbahasaindonesia.repository

import androidx.annotation.WorkerThread
import com.example.kbbikamusbesarbahasaindonesia.database.KataDao
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import kotlinx.coroutines.flow.Flow

class KataRepository(private val kataDao: KataDao) {

    val allSavedKata: Flow<List<Kata>> = kataDao.getAllKata()


    @WorkerThread
    suspend fun insert(kata: Kata?) {
        kataDao.insert(kata)
    }

    @WorkerThread
    suspend fun delete(kata: Kata) {
        kataDao.delete(kata)
    }

}