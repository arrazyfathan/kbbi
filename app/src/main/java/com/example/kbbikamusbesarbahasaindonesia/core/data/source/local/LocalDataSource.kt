package com.example.kbbikamusbesarbahasaindonesia.core.data.source.local

import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.ListWordEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.room.WordDao

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class LocalDataSource(private val wordDao: WordDao) {
    fun getAllWords() = wordDao.getAllWords()
    suspend fun insertWord(listWordEntity: ListWordEntity) = wordDao.insertWord(listWordEntity)
    suspend fun deleteWord(word: String) = wordDao.deleteWord(word)
    fun checkWordIsExist(word: String) = wordDao.checkWordIsExist(word)
    suspend fun insertHistory(historyEntity: HistoryEntity) = wordDao.insertHistory(historyEntity)
    fun getAllHistories() = wordDao.getListHistory()
}
