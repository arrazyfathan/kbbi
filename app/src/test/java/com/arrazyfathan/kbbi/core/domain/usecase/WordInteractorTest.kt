package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.data.FakeWordRepository
import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.model.MeaningModel
import com.arrazyfathan.kbbi.core.domain.model.Resource
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordInteractorTest {
    private lateinit var fakeRepository: FakeWordRepository
    private lateinit var wordInteractor: WordInteractor

    @Before
    fun setUp() {
        fakeRepository = FakeWordRepository()
        wordInteractor = WordInteractor(fakeRepository)
    }

    @Test
    fun testGetMeaningOfWord_Success() =
        runBlocking {
            val word = "belajar"
            val meanings = listOf(MeaningModel(wordClass = "v", description = "berusaha memperoleh kepandaian"))
            val wordModel = WordModel(entry = word, meanings = meanings)
            val expectedResource = Resource.Success(listOf(wordModel))

            fakeRepository.setRemoteData(word, expectedResource)

            val flowResult = wordInteractor.getMeaningOfWord(word).first()
            assertTrue(flowResult is Resource.Success)
            val data = (flowResult as Resource.Success).data
            assertEquals(1, data?.size)
            assertEquals(word, data?.get(0)?.entry)
            assertEquals(
                "v",
                data
                    ?.get(0)
                    ?.meanings
                    ?.get(0)
                    ?.wordClass,
            )
        }

    @Test
    fun testGetMeaningOfWord_Error() =
        runBlocking {
            val word = "nonexistentword"
            val expectedResource = Resource.Error<List<WordModel>>("Word not found")

            fakeRepository.setRemoteData(word, expectedResource)

            val flowResult = wordInteractor.getMeaningOfWord(word).first()
            assertTrue(flowResult is Resource.Error)
            assertEquals("Word not found", flowResult.message)
        }

    @Test
    fun testBookmarkAndGetBookmarks() =
        runBlocking {
            val word = "buku"
            val meanings = listOf(MeaningModel(wordClass = "n", description = "lembar kertas berjilid"))
            val wordModel = listOf(WordModel(entry = word, meanings = meanings))

            // Assert initially not saved
            var isSaved = wordInteractor.checkIfWordIsSaved(word).first()
            assertFalse(isSaved)

            // Bookmark the word
            wordInteractor.bookmarkWord(word, wordModel, isSaved = true)

            // Assert is saved now
            isSaved = wordInteractor.checkIfWordIsSaved(word).first()
            assertTrue(isSaved)

            // Verify bookmark contents
            val bookmarks = wordInteractor.getBookmarks().first()
            assertEquals(1, bookmarks.size)
            assertEquals(word, bookmarks[0].word)
            assertEquals(1, bookmarks[0].listWords.size)
            assertEquals(word, bookmarks[0].listWords[0].entry)
        }

    @Test
    fun testDeleteWord() =
        runBlocking {
            val word = "pena"
            val meanings = listOf(MeaningModel(wordClass = "n", description = "alat tulis"))
            val wordModel = listOf(WordModel(entry = word, meanings = meanings))

            // Bookmark the word
            wordInteractor.bookmarkWord(word, wordModel, isSaved = true)
            assertTrue(wordInteractor.checkIfWordIsSaved(word).first())

            // Delete the word
            wordInteractor.deleteWord(word)

            // Assert deleted
            assertFalse(wordInteractor.checkIfWordIsSaved(word).first())
            assertTrue(wordInteractor.getBookmarks().first().isEmpty())
        }

    @Test
    fun testHistory() =
        runBlocking {
            val history1 = HistoryModel("belajar")
            val history2 = HistoryModel("membaca")

            assertTrue(wordInteractor.getAllHistories().first().isEmpty())

            wordInteractor.addToHistory(history1)
            wordInteractor.addToHistory(history2)

            val histories = wordInteractor.getAllHistories().first()
            assertEquals(2, histories.size)
            assertEquals("belajar", histories[0].word)
            assertEquals("membaca", histories[1].word)
        }
}
