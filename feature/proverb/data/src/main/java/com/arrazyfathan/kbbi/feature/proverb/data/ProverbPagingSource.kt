package com.arrazyfathan.kbbi.feature.proverb.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.feature.proverb.data.mapper.toCachedProverbs
import com.arrazyfathan.kbbi.feature.proverb.data.mapper.toProverbPage
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.ProverbLocalDataSource
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.ProverbRemoteDataSource
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbPageModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbPagingException

private const val FIRST_PAGE = 1

class ProverbPagingSource(
    private val remoteDataSource: ProverbRemoteDataSource,
    private val localDataSource: ProverbLocalDataSource,
    private val query: String,
    private val pageSize: Int,
) : PagingSource<Int, ProverbModel>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProverbModel> {
        val page = params.key ?: FIRST_PAGE
        return when (val result = remoteDataSource.getProverbs(page = page, limit = pageSize, query = query)) {
            is AppResult.Success -> {
                val pageData = result.data
                runCatching {
                    localDataSource.replaceProverbPage(
                        query = query,
                        page = page,
                        proverbs = pageData.toCachedProverbs(query),
                    )
                }
                pageData.toLoadResult()
            }

            is AppResult.Error -> {
                val cachedPage =
                    runCatching {
                        localDataSource
                            .getProverbs(query = query, page = page)
                            .toProverbPage(page = page)
                    }.getOrNull()

                if (cachedPage != null && cachedPage.items.isNotEmpty()) {
                    cachedPage.toLoadResult()
                } else {
                    LoadResult.Error(ProverbPagingException(result.error))
                }
            }
        }
    }

    private fun ProverbPageModel.toLoadResult(): LoadResult.Page<Int, ProverbModel> =
        LoadResult.Page(
            data = items,
            prevKey = if (page == FIRST_PAGE) null else page - 1,
            nextKey = if (hasNextPage) page + 1 else null,
        )

    override fun getRefreshKey(state: PagingState<Int, ProverbModel>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
