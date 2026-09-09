package com.arrazyfathan.kbbi.feature.figure.domain.usecase

import androidx.paging.PagingData
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class FigureUseCasesTest {
    private val repository = FakeFigureRepository()

    @Test
    fun `get figures trims query and delegates detail preference`() {
        GetFiguresUseCase(repository)(query = "  soekarno  ", includeDetails = true)

        assertEquals("soekarno", repository.requestedQuery)
        assertTrue(repository.requestedIncludeDetails)
    }

    @Test
    fun `get figures preserves blank query as list request`() {
        GetFiguresUseCase(repository)(query = "   ")

        assertEquals("", repository.requestedQuery)
        assertFalse(repository.requestedIncludeDetails)
    }

    @Test
    fun `get figure detail normalizes whitespace and returns repository result`() =
        runBlocking {
            val figure = figure()
            repository.detailResult = AppResult.Success(figure)

            val result = GetFigureDetailUseCase(repository)("  Cut  Nyak Dien  ")

            assertEquals("Cut_Nyak_Dien", repository.requestedSlug)
            assertSame(figure, (result as AppResult.Success).data)
        }

    @Test
    fun `get figure detail rejects blank slug`() =
        runBlocking {
            val result = GetFigureDetailUseCase(repository)("   ")

            assertEquals(DataError.EmptyQuery, (result as AppResult.Error).error)
            assertEquals(null, repository.requestedSlug)
        }
}

private class FakeFigureRepository : FigureRepository {
    var requestedQuery: String? = null
    var requestedIncludeDetails: Boolean = false
    var requestedSlug: String? = null
    var detailResult: AppResult<FigureModel, DataError> = AppResult.Error(DataError.Unknown)

    override fun getFigures(
        query: String,
        includeDetails: Boolean,
    ): Flow<PagingData<FigureModel>> {
        requestedQuery = query
        requestedIncludeDetails = includeDetails
        return flowOf(PagingData.empty())
    }

    override suspend fun getFigure(slug: String): AppResult<FigureModel, DataError> {
        requestedSlug = slug
        return detailResult
    }
}

private fun figure() =
    FigureModel(
        name = "Cut Nyak Dien",
        slug = "Cut_Nyak_Dien",
        sourceUrl = "https://id.wikiquote.org/wiki/Cut_Nyak_Dien",
    )
