package com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase

import com.example.kbbikamusbesarbahasaindonesia.core.data.Resource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.repository.IWordRepository
import kotlinx.coroutines.flow.Flow

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class WordInteractor(private val wordRepository: IWordRepository) : WordUseCase {

    override fun getMeaningOfWord(word: String): Flow<Resource<List<WordModel>>> {
        return wordRepository.getMeaningOfWord(word)
    }

    override suspend fun bookmarkWord(word: String, wordList: List<WordModel>, isSaved: Boolean): Long {
        return wordRepository.bookmarkWord(word, wordList, isSaved)
    }

    override suspend fun addToHistory(historyEntity: HistoryEntity) {
        wordRepository.addToHistory(historyEntity)
    }

    override fun getAllHistories(): Flow<List<HistoryEntity>> {
        return wordRepository.getAllHistories()
    }

    override suspend fun deleteWord(word: String) {
        wordRepository.deleteWord(word)
    }

    override fun checkIfWordIsSaved(word: String): Flow<Boolean> {
        return wordRepository.checkIfWordIsSaved(word)
    }
}
