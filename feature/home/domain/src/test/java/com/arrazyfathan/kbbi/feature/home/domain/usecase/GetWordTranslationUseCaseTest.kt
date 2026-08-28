package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslateModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslatedMeaningModel
import com.arrazyfathan.kbbi.feature.home.domain.model.TranslatedWordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.TranslateRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetWordTranslationUseCaseTest {
    private val fakeRepository = FakeTranslateRepository()

    @Test
    fun `returns translated model on success`() =
        runBlocking {
            val translated = sampleTranslation()
            fakeRepository.result = AppResult.Success(translated)

            val result = GetWordTranslationUseCase(fakeRepository)("belajar")

            assertTrue(result is AppResult.Success)
            val data = (result as AppResult.Success).data
            assertEquals("learn", data.translation)
            assertEquals("en", data.to)
            assertEquals(
                "attempt to gain knowledge or skill",
                data.entries.first().meanings.first().translation,
            )
        }

    @Test
    fun `rejects blank query`() =
        runBlocking {
            val result = GetWordTranslationUseCase(fakeRepository)(" ")

            assertTrue(result is AppResult.Error)
            assertEquals(DataError.EmptyQuery, (result as AppResult.Error).error)
        }

    @Test
    fun `forwards repository error`() =
        runBlocking {
            fakeRepository.result = AppResult.Error(DataError.NoInternet)

            val result = GetWordTranslationUseCase(fakeRepository)("belajar")

            assertTrue(result is AppResult.Error)
            assertEquals(DataError.NoInternet, (result as AppResult.Error).error)
        }

    private fun sampleTranslation(): TranslateModel =
        TranslateModel(
            word = "belajar",
            translation = "learn",
            from = "id",
            to = "en",
            provider = "google",
            entries =
                listOf(
                    TranslatedWordModel(
                        headword = "belajar",
                        meanings =
                            listOf(
                                TranslatedMeaningModel(
                                    wordClass = "v",
                                    description = "berusaha memperoleh kepandaian atau ilmu",
                                    translation = "attempt to gain knowledge or skill",
                                ),
                            ),
                    ),
                ),
        )
}

private class FakeTranslateRepository : TranslateRepository {
    var result: AppResult<TranslateModel, DataError> = AppResult.Error(DataError.NotFound)

    override suspend fun getTranslation(word: String): AppResult<TranslateModel, DataError> = result
}
