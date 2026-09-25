package com.arrazyfathan.kbbi.feature.figure.presentation.figure

import androidx.paging.PagingData
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository
import com.arrazyfathan.kbbi.feature.figure.domain.usecase.GetFigureDetailUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FigureDetailViewModelTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load fetches detail by slug and exposes optional content`() = runTest(dispatcher) {
        val figure = FigureModel("R.A. Kartini", "RA_Kartini", "https://example.com", quotes = null)
        val repository = FakeFigureRepository(AppResult.Success(figure))
        val viewModel = FigureDetailViewModel(GetFigureDetailUseCase(repository))

        viewModel.onAction(FigureDetailAction.Load("RA_Kartini"))

        assertEquals(listOf("RA_Kartini"), repository.requestedSlugs)
        assertEquals(figure, viewModel.state.value.figure)
        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `failed load can be retried without changing the route`() = runTest(dispatcher) {
        val repository = FakeFigureRepository(AppResult.Error(DataError.NotFound))
        val viewModel = FigureDetailViewModel(GetFigureDetailUseCase(repository))

        viewModel.onAction(FigureDetailAction.Load("missing"))
        assertNotNull(viewModel.state.value.error)
        assertNull(viewModel.state.value.figure)

        val figure = FigureModel("Found", "missing", "https://example.com")
        repository.result = AppResult.Success(figure)
        viewModel.onAction(FigureDetailAction.Retry)

        assertEquals(listOf("missing", "missing"), repository.requestedSlugs)
        assertEquals(figure, viewModel.state.value.figure)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }
}

private class FakeFigureRepository(
    var result: AppResult<FigureModel, DataError>,
) : FigureRepository {
    val requestedSlugs = mutableListOf<String>()

    override fun getFigures(query: String, includeDetails: Boolean): Flow<PagingData<FigureModel>> = emptyFlow()

    override suspend fun getFigure(slug: String): AppResult<FigureModel, DataError> {
        requestedSlugs += slug
        return result
    }
}
