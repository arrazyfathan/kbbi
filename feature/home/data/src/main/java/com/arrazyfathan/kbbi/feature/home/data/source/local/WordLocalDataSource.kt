package com.arrazyfathan.kbbi.feature.home.data.source.local

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDao

class WordLocalDataSource(
    private val wordDao: WordDao,
) {
    fun getAllWords() = wordDao.getAllWords()

    suspend fun insertWord(listWordEntity: ListWordEntity) = wordDao.insertWord(listWordEntity)

    suspend fun deleteWord(word: String) = wordDao.deleteWord(word)

    fun checkWordIsExist(word: String) = wordDao.checkWordIsExist(word)

    suspend fun insertHistory(historyEntity: HistoryEntity) = wordDao.insertHistoryAndTrim(historyEntity)

    fun getAllHistories() = wordDao.getListHistory()
}
