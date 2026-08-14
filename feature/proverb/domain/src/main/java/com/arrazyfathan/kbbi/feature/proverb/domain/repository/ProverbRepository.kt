package com.arrazyfathan.kbbi.feature.proverb.domain.repository

import androidx.paging.PagingData
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbDetailModel
import com.arrazyfathan.kbbi.feature.proverb.domain.model.ProverbModel
import kotlinx.coroutines.flow.Flow

interface ProverbRepository {
    fun getListProverbs(query: String): Flow<PagingData<ProverbModel>>

    suspend fun getCachedProverbsForReminder(): List<ProverbModel> = emptyList()

    suspend fun getProverbMeaning(slug: String): AppResult<ProverbDetailModel, DataError>
}
