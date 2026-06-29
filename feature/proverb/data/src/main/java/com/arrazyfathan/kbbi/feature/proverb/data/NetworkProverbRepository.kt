package com.arrazyfathan.kbbi.feature.proverb.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.proverb.data.source.remote.ProverbRemoteDataSource
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository
import kotlinx.coroutines.flow.Flow

private const val PROVERB_PAGE_SIZE = 20
private const val PROVERB_PREFETCH_DISTANCE = 5

class NetworkProverbRepository(
    private val remoteDataSource: ProverbRemoteDataSource,
) : ProverbRepository {
    override fun getListProverbs(query: String): Flow<PagingData<ProverbModel>> =
        Pager(
            config =
                PagingConfig(
                    pageSize = PROVERB_PAGE_SIZE,
                    prefetchDistance = PROVERB_PREFETCH_DISTANCE,
                    enablePlaceholders = false,
                ),
            pagingSourceFactory = {
                ProverbPagingSource(
                    remoteDataSource = remoteDataSource,
                    query = query.trim(),
                    pageSize = PROVERB_PAGE_SIZE,
                )
            },
        ).flow

    override suspend fun getProverbMeaning(slug: String): AppResult<ProverbDetailModel, DataError> =
        remoteDataSource.getProverbMeaning(slug)
}
