package com.arrazyfathan.kbbi.core.data

import com.arrazyfathan.kbbi.core.data.source.local.LocalDataSource
import com.arrazyfathan.kbbi.core.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.core.data.source.remote.RemoteDataSource
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.model.map
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository
import com.arrazyfathan.kbbi.core.utils.DataMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class WordRepository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
) : IWordRepository {
    override fun getMeaningOfWord(word: String): Flow<AppResult<List<WordModel>, DataError>> =
        remoteDataSource.getMeaningOfWord(word).map { result ->
            result.map(DataMapper::mapResponseToDomain)
        }

    override suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
        isSaved: Boolean,
    ) = withContext(Dispatchers.IO) {
        return@withContext localDataSource.insertWord(
            ListWordEntity(
                word = word,
                DataMapper.mapDomainToEntity(result),
                isSaved = isSaved,
            ),
        )
    }

    override suspend fun addToHistory(history: HistoryModel) =
        withContext(Dispatchers.IO) {
            return@withContext localDataSource.insertHistory(DataMapper.mapHistoryDomainToEntity(history))
        }

    override fun getAllHistories(): Flow<List<HistoryModel>> =
        localDataSource.getAllHistories().map {
            DataMapper.mapHistoryEntitiesToDomain(it)
        }

    override suspend fun deleteWord(word: String) =
        withContext(Dispatchers.IO) {
            return@withContext localDataSource.deleteWord(word)
        }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> = localDataSource.checkWordIsExist(word)

    override fun getBookmarks(): Flow<List<ListWordModel>> =
        localDataSource.getAllWords().map {
            DataMapper.mapListWordEntityToDomain(it)
        }
}
