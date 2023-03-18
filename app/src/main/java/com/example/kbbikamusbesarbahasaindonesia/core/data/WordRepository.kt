package com.example.kbbikamusbesarbahasaindonesia.core.data

import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.LocalDataSource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.ListWordEntity
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.RemoteDataSource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.network.ApiResponse
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.repository.IWordRepository
import com.example.kbbikamusbesarbahasaindonesia.core.utils.DataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class WordRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : IWordRepository {

    override fun getMeaningOfWord(word: String): Flow<Resource<List<WordModel>>> = flow {
        emit(Resource.Loading())
        when (val response = remoteDataSource.getMeaningOfWord(word).first()) {
            is ApiResponse.Success -> {
                emit(Resource.Success(DataMapper.mapResponseToDomain(response.data)))
            }
            is ApiResponse.Empty -> {
                emit(Resource.Error(message = "Data not found"))
            }
            is ApiResponse.Error -> {
                emit(Resource.Error(response.errorMessage))
            }
        }
    }

    override suspend fun bookmarkWord(word: String, result: List<WordModel>, isSaved: Boolean) =
        withContext(Dispatchers.IO) {
            return@withContext localDataSource.insertWord(
                ListWordEntity(
                    word = word,
                    DataMapper.mapDomainToEntity(result),
                    isSaved = isSaved,
                ),
            )
        }

    override suspend fun addToHistory(historyEntity: HistoryEntity) = withContext(Dispatchers.IO) {
        return@withContext localDataSource.insertHistory(historyEntity)
    }

    override fun getAllHistories(): Flow<List<HistoryEntity>> {
        return localDataSource.getAllHistories()
    }
}
