package com.arrazyfathan.kbbi.di

import androidx.paging.PagingData
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository
import com.arrazyfathan.kbbi.feature.figure.presentation.figure.FigureDetailViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class FigureDetailDiTest {
    @Test
    fun `figure detail ViewModel resolves through app registrations`() {
        val application = koinApplication {
            modules(
                useCaseModule,
                viewModelModule,
                module {
                    single<FigureRepository> { FakeFigureRepository() }
                },
            )
        }

        try {
            assertNotNull(application.koin.get<FigureDetailViewModel>())
        } finally {
            application.close()
        }
    }
}

private class FakeFigureRepository : FigureRepository {
    override fun getFigures(query: String, includeDetails: Boolean): Flow<PagingData<FigureModel>> = emptyFlow()

    override suspend fun getFigure(slug: String): AppResult<FigureModel, DataError> =
        AppResult.Error(DataError.NotFound)
}
