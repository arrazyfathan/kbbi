package com.arrazyfathan.kbbi.feature.proverb.data.source.local

import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.room.ProverbDao

class ProverbLocalDataSource(
    private val proverbDao: ProverbDao,
) {
    suspend fun getCachedProverbs(): List<CachedProverbEntity> = proverbDao.getCachedProverbs()

    suspend fun getProverbs(
        query: String,
        page: Int,
    ): List<CachedProverbEntity> = proverbDao.getProverbs(query = query, page = page)

    suspend fun replaceProverbPage(
        query: String,
        page: Int,
        proverbs: List<CachedProverbEntity>,
    ) = proverbDao.replaceProverbPage(query = query, page = page, proverbs = proverbs)

    suspend fun getProverbDetail(slug: String): CachedProverbDetailEntity? = proverbDao.getProverbDetail(slug)

    suspend fun upsertProverbDetail(proverb: CachedProverbDetailEntity) = proverbDao.upsertProverbDetail(proverb)
}
