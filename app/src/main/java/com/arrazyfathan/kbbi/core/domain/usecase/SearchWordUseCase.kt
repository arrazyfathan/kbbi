package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.Resource
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class SearchWordUseCase(
    private val wordRepository: IWordRepository,
) {
    operator fun invoke(word: String): Flow<Resource<ListWordModel>> {
        val wordToSearch = word.trim()
        if (wordToSearch.isBlank()) {
            return flowOf(Resource.Error(message = "Word cannot be empty"))
        }

        return wordRepository.getMeaningOfWord(wordToSearch).map { resource ->
            when (resource) {
                is Resource.Loading -> Resource.Loading()
                is Resource.Success ->
                    Resource.Success(
                        ListWordModel(
                            word = wordToSearch,
                            listWords = resource.data.orEmpty(),
                        ),
                    )
                is Resource.Error -> Resource.Error(resource.message ?: "Error occurred")
            }
        }
    }
}
