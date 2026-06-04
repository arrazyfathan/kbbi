package com.arrazyfathan.kbbi.feature.home.data

import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.domain.model.map
import com.arrazyfathan.kbbi.feature.home.data.mapper.toDomain
import com.arrazyfathan.kbbi.feature.home.data.mapper.toEntity
import com.arrazyfathan.kbbi.feature.home.data.mapper.toHistoryModels
import com.arrazyfathan.kbbi.feature.home.data.mapper.toWordEntities
import com.arrazyfathan.kbbi.feature.home.data.mapper.toWordModels
import com.arrazyfathan.kbbi.feature.home.data.source.local.WordLocalDataSource
import com.arrazyfathan.kbbi.feature.home.data.source.local.entity.ListWordEntity
import com.arrazyfathan.kbbi.feature.home.data.source.remote.WordRemoteDataSource
import com.arrazyfathan.kbbi.feature.home.domain.model.HistoryModel
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.SearchHistoryRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordSearchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class WordRepository(
    private val remoteDataSource: WordRemoteDataSource,
    private val localDataSource: WordLocalDataSource,
) : WordSearchRepository,
    BookmarkRepository,
    SearchHistoryRepository {
    override suspend fun getMeaningOfWord(word: String): AppResult<List<WordModel>, DataError> =
        remoteDataSource.getMeaningOfWord(word).map { it.toWordModels() }

    override suspend fun bookmarkWord(
        word: String,
        result: List<WordModel>,
    ): Boolean =
        withContext(Dispatchers.IO) {
            localDataSource.insertWord(
                ListWordEntity(
                    word = word,
                    listWords = result.toWordEntities(),
                    isSaved = true,
                ),
            ) != -1L
        }

    override suspend fun addToHistory(history: HistoryModel) =
        withContext(Dispatchers.IO) {
            return@withContext localDataSource.insertHistory(history.toEntity())
        }

    override fun getAllHistories(): Flow<List<HistoryModel>> =
        localDataSource.getAllHistories().map {
            it.toHistoryModels()
        }

    override suspend fun deleteWord(word: String) =
        withContext(Dispatchers.IO) {
            return@withContext localDataSource.deleteWord(word)
        }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> = localDataSource.checkWordIsExist(word)

    override fun getBookmarks(): Flow<List<ListWordModel>> =
        localDataSource.getAllWords().map {
            it.map { entity -> entity.toDomain() }
        }
}
