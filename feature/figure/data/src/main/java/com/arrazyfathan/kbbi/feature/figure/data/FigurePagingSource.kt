package com.arrazyfathan.kbbi.feature.figure.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.FigureRemoteDataSource
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigurePageModel
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigurePagingException

private const val FIRST_PAGE = 1

class FigurePagingSource(
    private val remoteDataSource: FigureRemoteDataSource,
    private val query: String,
    private val includeDetails: Boolean,
    private val pageSize: Int,
) : PagingSource<Int, FigureModel>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FigureModel> {
        val page = params.key ?: FIRST_PAGE
        return when (
            val result =
                remoteDataSource.getFigures(
                    page = page,
                    limit = pageSize,
                    query = query,
                    includeDetails = includeDetails,
                )
        ) {
            is AppResult.Success -> result.data.toLoadResult()
            is AppResult.Error -> LoadResult.Error(FigurePagingException(result.error))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FigureModel>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    private fun FigurePageModel.toLoadResult(): LoadResult.Page<Int, FigureModel> =
        LoadResult.Page(
            data = items,
            prevKey = if (page == FIRST_PAGE) null else page - 1,
            nextKey = if (hasNextPage) page + 1 else null,
        )
}
