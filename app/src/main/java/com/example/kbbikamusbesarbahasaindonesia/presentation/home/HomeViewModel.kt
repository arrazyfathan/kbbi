package com.example.kbbikamusbesarbahasaindonesia.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kbbikamusbesarbahasaindonesia.core.data.Resource
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.HistoryEntity
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase.WordUseCase
import kotlinx.coroutines.launch

/**
 * Created by Ar Razy Fathan Rabbani on 19/01/23.
 */
class HomeViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {

    fun getAllHistories() = wordUseCase.getAllHistories().asLiveData()

    fun getMeaningOfWord(word: String): LiveData<Resource<List<WordModel>>> {
        return wordUseCase.getMeaningOfWord(word).asLiveData()
    }

    fun bookmarkWord(word: String, wordList: List<WordModel>, isSaved: Boolean) {
        viewModelScope.launch {
            wordUseCase.bookmarkWord(word, wordList, isSaved)
        }
    }

    fun addToHistory(historyEntity: HistoryEntity) {
        viewModelScope.launch {
            wordUseCase.addToHistory(historyEntity)
        }
    }
}
