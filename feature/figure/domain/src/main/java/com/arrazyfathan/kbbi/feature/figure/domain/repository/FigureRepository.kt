package com.arrazyfathan.kbbi.feature.figure.domain.repository

import androidx.paging.PagingData
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import kotlinx.coroutines.flow.Flow

interface FigureRepository {
    fun getFigures(
        query: String,
        includeDetails: Boolean,
    ): Flow<PagingData<FigureModel>>

    suspend fun getFigure(slug: String): AppResult<FigureModel, DataError>
}
