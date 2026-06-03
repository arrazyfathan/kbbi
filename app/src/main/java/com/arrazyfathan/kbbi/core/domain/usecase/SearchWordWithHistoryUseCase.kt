package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SearchWordWithHistoryUseCase(
    private val searchWord: SearchWordUseCase,
    private val addSearchHistory: AddSearchHistoryUseCase,
) {
    operator fun invoke(word: String): Flow<Resource<ListWordModel>> =
        searchWord(word).map { resource ->
            if (resource is Resource.Success) {
                resource.data?.word?.let { addSearchHistory(it) }
            }
            resource
        }
}
