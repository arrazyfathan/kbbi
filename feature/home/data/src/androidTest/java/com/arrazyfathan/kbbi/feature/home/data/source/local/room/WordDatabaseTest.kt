package com.arrazyfathan.kbbi.feature.home.data.source.local.room

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.MeaningEntity
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.WordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class WordDatabaseTest {
    private lateinit var db: WordDatabase
    private lateinit var wordDao: WordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db =
            Room
                .inMemoryDatabaseBuilder(
                    context,
                    WordDatabase::class.java,
                )
                // Allowing main thread queries for testing purposes
                .allowMainThreadQueries()
                .build()
        wordDao = db.wordDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetWord() =
        runBlocking {
            val meaning = MeaningEntity(wordClass = "adj", description = "sangat indah")
            val word = WordEntity(entry = "cantik", meanings = listOf(meaning))
            val listWordEntity = ListWordEntity(word = "cantik", listWords = listOf(word), isSaved = true)

            wordDao.insertWord(listWordEntity)

            val allWords = wordDao.getSavedWords().first()
            assertEquals(1, allWords.size)
            assertEquals("cantik", allWords[0].word)
            assertTrue(allWords[0].isSaved)
            assertEquals(1, allWords[0].listWords.size)
            assertEquals("cantik", allWords[0].listWords[0].entry)
        }

    @Test
    @Throws(Exception::class)
    fun unbookmarkWordKeepsCachedMeaning() =
        runBlocking {
            val meaning = MeaningEntity(wordClass = "n", description = "benda cair")
            val word = WordEntity(entry = "air", meanings = listOf(meaning))
            val listWordEntity = ListWordEntity(word = "air", listWords = listOf(word), isSaved = true)

            wordDao.insertWord(listWordEntity)
            assertTrue(wordDao.checkWordIsExist("air").first())

            wordDao.unbookmarkWord("air")
            assertFalse(wordDao.checkWordIsExist("air").first())

            val allWords = wordDao.getSavedWords().first()
            assertTrue(allWords.isEmpty())

            val cachedWord = wordDao.getWord("air")
            assertEquals("air", cachedWord?.word)
            assertFalse(cachedWord?.isSaved ?: true)
            assertEquals(
                "benda cair",
                cachedWord?.listWords?.firstOrNull()?.meanings?.firstOrNull()?.description,
            )
        }

    @Test
    @Throws(Exception::class)
    fun cachedWordIsHiddenFromBookmarks() =
        runBlocking {
            val meaning = MeaningEntity(wordClass = "n", description = "cahaya dan panas")
            val word = WordEntity(entry = "api", meanings = listOf(meaning))
            val listWordEntity = ListWordEntity(word = "api", listWords = listOf(word), isSaved = false)

            wordDao.insertWord(listWordEntity)

            assertFalse(wordDao.checkWordIsExist("api").first())
            assertTrue(wordDao.getSavedWords().first().isEmpty())
            assertEquals("api", wordDao.getWord("api")?.word)
        }

    @Test
    @Throws(Exception::class)
    fun insertAndGetHistory() =
        runBlocking {
            val history1 = HistoryEntity(word = "pintar")
            val history2 = HistoryEntity(word = "hebat")

            wordDao.insertHistory(history1)
            wordDao.insertHistory(history2)

            val historyList = wordDao.getListHistory().first()
            assertEquals(2, historyList.size)

            // Ordered DESC by word, so "pintar" (starts with p) comes before "hebat" (starts with h)
            assertEquals("pintar", historyList[0].word)
            assertEquals("hebat", historyList[1].word)
        }

    @Test
    @Throws(Exception::class)
    fun clearHistoryRemovesAllHistoryRows() =
        runBlocking {
            wordDao.insertHistory(HistoryEntity(word = "pintar"))
            wordDao.insertHistory(HistoryEntity(word = "hebat"))

            wordDao.clearHistory()

            assertTrue(wordDao.getListHistory().first().isEmpty())
        }
}
