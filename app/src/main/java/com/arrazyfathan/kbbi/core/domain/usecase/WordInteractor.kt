package com.arrazyfathan.kbbi.core.domain.usecase

import com.arrazyfathan.kbbi.core.domain.model.HistoryModel
import com.arrazyfathan.kbbi.core.domain.model.ListWordModel
import com.arrazyfathan.kbbi.core.domain.model.Resource
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.repository.IWordRepository
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class WordInteractor(
    private val wordRepository: IWordRepository,
) : WordUseCase {
    override fun getMeaningOfWord(word: String): Flow<Resource<List<WordModel>>> = wordRepository.getMeaningOfWord(word)

    override suspend fun bookmarkWord(
        word: String,
        wordList: List<WordModel>,
        isSaved: Boolean,
    ): Long = wordRepository.bookmarkWord(word, wordList, isSaved)

    override suspend fun addToHistory(history: HistoryModel) {
        wordRepository.addToHistory(history)
    }

    override fun getAllHistories(): Flow<List<HistoryModel>> = wordRepository.getAllHistories()

    override suspend fun deleteWord(word: String) {
        wordRepository.deleteWord(word)
    }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> = wordRepository.checkIfWordIsSaved(word)

    override fun getBookmarks(): Flow<List<ListWordModel>> = wordRepository.getBookmarks()
}
