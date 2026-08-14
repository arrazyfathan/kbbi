package com.arrazyfathan.kbbi.feature.proverb.data.source.local.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbDetailEntity
import com.arrazyfathan.kbbi.feature.proverb.data.source.local.entity.CachedProverbEntity

@Dao
interface ProverbDao {
    @Query("SELECT * FROM cached_proverb_table ORDER BY slug ASC")
    suspend fun getCachedProverbs(): List<CachedProverbEntity>

    @Query(
        "SELECT * FROM cached_proverb_table WHERE query = :query AND page = :page ORDER BY position ASC",
    )
    suspend fun getProverbs(
        query: String,
        page: Int,
    ): List<CachedProverbEntity>

    @Query("DELETE FROM cached_proverb_table WHERE query = :query")
    suspend fun deleteProverbsForQuery(query: String)

    @Upsert
    suspend fun upsertProverbs(proverbs: List<CachedProverbEntity>)

    @Transaction
    suspend fun replaceProverbPage(
        query: String,
        page: Int,
        proverbs: List<CachedProverbEntity>,
    ) {
        if (page == FIRST_PROVERB_PAGE) {
            deleteProverbsForQuery(query)
        }
        upsertProverbs(proverbs)
    }

    @Query("SELECT * FROM cached_proverb_detail_table WHERE slug = :slug")
    suspend fun getProverbDetail(slug: String): CachedProverbDetailEntity?

    @Upsert
    suspend fun upsertProverbDetail(proverb: CachedProverbDetailEntity)

    private companion object {
        const val FIRST_PROVERB_PAGE = 1
    }
}
