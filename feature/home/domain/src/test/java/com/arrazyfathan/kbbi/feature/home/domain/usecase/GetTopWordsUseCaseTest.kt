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

    @Test
    fun `cached uses default limit and returns repository cache`() =
        runBlocking {
            val expected = listOf(TopWordModel("kamus", 30))
            repository.cached = expected

            val actual = useCase.cached()

            assertEquals(10, repository.requestedCachedLimit)
            assertEquals(expected, actual)
        }

    @Test
    fun `cached normalizes invalid and excessive limits`() =
        runBlocking {
            useCase.cached(0)
            assertEquals(10, repository.requestedCachedLimit)

            useCase.cached(101)
            assertEquals(100, repository.requestedCachedLimit)
        }
}

private class FakeTopWordsRepository : TopWordsRepository {
    var requestedLimit: Int? = null
    var requestedCachedLimit: Int? = null
    var cached: List<TopWordModel> = emptyList()
    var result: AppResult<List<TopWordModel>, DataError> = AppResult.Success(emptyList())

    override suspend fun getTopWords(limit: Int): AppResult<List<TopWordModel>, DataError> {
        requestedLimit = limit
        return result
    }

    override suspend fun getCachedTopWords(limit: Int): List<TopWordModel> {
        requestedCachedLimit = limit
        return cached
    }
}
