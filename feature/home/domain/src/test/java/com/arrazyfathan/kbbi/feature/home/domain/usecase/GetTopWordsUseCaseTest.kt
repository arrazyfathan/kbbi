package com.arrazyfathan.kbbi.feature.home.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.home.domain.model.TopWordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.TopWordsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class GetTopWordsUseCaseTest {
    private val repository = FakeTopWordsRepository()
    private val useCase = GetTopWordsUseCase(repository)

    @Test
    fun `uses default limit`() =
        runBlocking {
            useCase()

            assertEquals(10, repository.requestedLimit)
        }

    @Test
    fun `normalizes invalid and excessive limits`() =
        runBlocking {
            useCase(0)
            assertEquals(10, repository.requestedLimit)

            useCase(-1)
            assertEquals(10, repository.requestedLimit)

            useCase(101)
            assertEquals(100, repository.requestedLimit)
        }

    @Test
    fun `passes valid limit and repository result through`() =
        runBlocking {
            val expected = AppResult.Success(listOf(TopWordModel("demokrasi", 12)))
            repository.result = expected

            val actual = useCase(25)

            assertEquals(25, repository.requestedLimit)
            assertSame(expected, actual)
        }
}

private class FakeTopWordsRepository : TopWordsRepository {
    var requestedLimit: Int? = null
    var result: AppResult<List<TopWordModel>, DataError> = AppResult.Success(emptyList())

    override suspend fun getTopWords(limit: Int): AppResult<List<TopWordModel>, DataError> {
        requestedLimit = limit
        return result
    }
}
