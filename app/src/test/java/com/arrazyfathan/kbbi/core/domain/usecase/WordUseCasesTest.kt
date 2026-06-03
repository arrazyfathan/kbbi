package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.data.FakeWordRepository
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.MeaningModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WordUseCasesTest {
    private lateinit var fakeRepository: FakeWordRepository

    @Before
    fun setUp() {
        fakeRepository = FakeWordRepository()
    }

    @Test
    fun searchWordReturnsListWordModel() =
        runBlocking {
            val word = "belajar"
            val meanings = listOf(MeaningModel(wordClass = "v", description = "berusaha memperoleh kepandaian"))
            val wordModel = WordModel(entry = word, meanings = meanings)
            fakeRepository.setRemoteData(word, AppResult.Success(listOf(wordModel)))

            val result = SearchWordUseCase(fakeRepository)(word)

            assertTrue(result is AppResult.Success)
            val data = (result as AppResult.Success).data
            assertEquals(word, data.word)
            assertEquals(word, data.listWords.first().entry)
            assertEquals(
                "v",
                data.listWords
                    .first()
                    .meanings
                    .first()
                    .wordClass,
            )
        }

    @Test
    fun searchWordRejectsBlankQuery() =
        runBlocking {
            val result = SearchWordUseCase(fakeRepository)(" ")

            assertTrue(result is AppResult.Error)
            assertEquals(DataError.EmptyQuery, (result as AppResult.Error).error)
        }

    @Test
    fun searchWordWithHistoryAddsHistoryOnlyAfterSuccess() =
        runBlocking {
            val word = "buku"
            val wordModel = WordModel(entry = word, meanings = emptyList())
            fakeRepository.setRemoteData(word, AppResult.Success(listOf(wordModel)))

            val searchWord = SearchWordUseCase(fakeRepository)
            val addSearchHistory = AddSearchHistoryUseCase(fakeRepository)
            val result = SearchWordWithHistoryUseCase(searchWord, addSearchHistory)(word)

            assertTrue(result is AppResult.Success)
            assertEquals(
                word,
                fakeRepository
                    .getAllHistories()
                    .first()
                    .single()
                    .word,
            )
        }

    @Test
    fun bookmarkUseCasesSaveObserveAndDeleteWord() =
        runBlocking {
            val word = "pena"
            val wordModel = WordModel(entry = word, meanings = emptyList())

            assertFalse(CheckWordSavedUseCase(fakeRepository)(word).first())

            val saved = SaveBookmarkUseCase(fakeRepository)(word, listOf(wordModel))
            assertTrue(saved)
            assertTrue(CheckWordSavedUseCase(fakeRepository)(word).first())
            assertEquals(word, ObserveBookmarksUseCase(fakeRepository)().first().single().word)

            DeleteBookmarkUseCase(fakeRepository)(word)
            assertFalse(CheckWordSavedUseCase(fakeRepository)(word).first())
        }

    @Test
    fun getWordEntriesReturnsCatalogWords() =
        runBlocking {
            fakeRepository.setCatalogWords(listOf("aku", "kamu"))

            assertEquals(listOf("aku", "kamu"), GetWordEntriesUseCase(fakeRepository)())
        }
}
