package com.example.kbbikamusbesarbahasaindonesia.repository

import androidx.annotation.WorkerThread
import com.example.kbbikamusbesarbahasaindonesia.database.KataDao
import com.example.kbbikamusbesarbahasaindonesia.model.History
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import kotlinx.coroutines.flow.Flow

class KataRepository(private val kataDao: KataDao) {

    val allSavedKata: Flow<List<Kata>> = kataDao.getAllKata()
    val listHistory: Flow<List<History>> = kataDao.getListHistory()

    @WorkerThread
    fun kataIsExists(kata: String) = kataDao.isExists(kata)

    @WorkerThread
    fun historyIsExist(kata: String) = kataDao.isHistoryExist(kata)

    @WorkerThread
    suspend fun insert(kata: Kata?) {
        kataDao.insert(kata)
    }

    @WorkerThread
    suspend fun insertHistory(history: History) {
        kataDao.insertHistory(history)
    }

    @WorkerThread
    suspend fun delete(kata: Kata) {
        kataDao.delete(kata)
    }
}
