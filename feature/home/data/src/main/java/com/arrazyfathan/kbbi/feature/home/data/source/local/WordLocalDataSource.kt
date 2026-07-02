package com.arrazyfathan.kbbi.feature.home.data.source.local

import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.WordDao

class WordLocalDataSource(
    private val wordDao: WordDao,
) {
    fun getSavedWords() = wordDao.getSavedWords()

    suspend fun getWord(word: String) = wordDao.getWord(word)

    suspend fun insertWord(listWordEntity: ListWordEntity) = wordDao.insertWord(listWordEntity)

    suspend fun unbookmarkWord(word: String) = wordDao.unbookmarkWord(word)

    fun checkWordIsExist(word: String) = wordDao.checkWordIsExist(word)

    suspend fun insertHistory(historyEntity: HistoryEntity) = wordDao.insertHistoryAndTrim(historyEntity)

    fun getAllHistories() = wordDao.getListHistory()
}
