package com.arrazyfathan.kbbi.feature.proverb.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.ProverbRemoteDataSource
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbPagingException

private const val FIRST_PAGE = 1

class ProverbPagingSource(
    private val remoteDataSource: ProverbRemoteDataSource,
    private val query: String,
    private val pageSize: Int,
) : PagingSource<Int, ProverbModel>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProverbModel> {
        val page = params.key ?: FIRST_PAGE
        return when (val result = remoteDataSource.getProverbs(page = page, limit = pageSize, query = query)) {
            is AppResult.Success -> {
                val pageData = result.data
                LoadResult.Page(
                    data = pageData.items,
                    prevKey = if (page == FIRST_PAGE) null else page - 1,
                    nextKey = if (pageData.hasNextPage) page + 1 else null,
                )
            }

            is AppResult.Error -> LoadResult.Error(ProverbPagingException(result.error))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ProverbModel>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
