package com.arrazyfathan.kbbi.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.data.Resource
import com.arrazyfathan.kbbi.core.data.source.local.entity.HistoryEntity
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.usecase.WordUseCase
import kotlinx.coroutines.launch

/**
 * Created by Ar Razy Fathan Rabbani on 19/01/23.
 */
class HomeViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {
    fun getAllHistories() = wordUseCase.getAllHistories().asLiveData()

    fun getMeaningOfWord(word: String): LiveData<Resource<List<WordModel>>> =
        wordUseCase.getMeaningOfWord(word).asLiveData()

    fun addToHistory(historyEntity: HistoryEntity) {
        viewModelScope.launch {
            wordUseCase.addToHistory(historyEntity)
        }
    }
}
