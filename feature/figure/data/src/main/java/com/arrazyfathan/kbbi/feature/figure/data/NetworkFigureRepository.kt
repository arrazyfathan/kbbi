package com.arrazyfathan.kbbi.feature.figure.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.FigureRemoteDataSource
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.repository.FigureRepository
import kotlinx.coroutines.flow.Flow

private const val FIGURE_PAGE_SIZE = 20
private const val FIGURE_PREFETCH_DISTANCE = 5

class NetworkFigureRepository(
    private val remoteDataSource: FigureRemoteDataSource,
) : FigureRepository {
    override fun getFigures(
        query: String,
        includeDetails: Boolean,
    ): Flow<PagingData<FigureModel>> =
        Pager(
            config =
                PagingConfig(
                    pageSize = FIGURE_PAGE_SIZE,
                    prefetchDistance = FIGURE_PREFETCH_DISTANCE,
                    enablePlaceholders = false,
                ),
            pagingSourceFactory = {
                FigurePagingSource(
                    remoteDataSource = remoteDataSource,
                    query = query,
                    includeDetails = includeDetails,
                    pageSize = FIGURE_PAGE_SIZE,
                )
            },
        ).flow

    override suspend fun getFigure(slug: String): AppResult<FigureModel, DataError> = remoteDataSource.getFigure(slug)
}
