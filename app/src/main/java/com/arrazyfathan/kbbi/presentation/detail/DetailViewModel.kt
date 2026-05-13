package com.arrazyfathan.kbbi.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.usecase.WordUseCase
import kotlinx.coroutines.launch

class DetailViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {
    private var _resultBookmark = MutableLiveData<Long>()
    val resultBookmark: LiveData<Long> get() = _resultBookmark

    private var _resultDelete = MutableLiveData<Boolean>()
    val resultDelete: LiveData<Boolean> get() = _resultDelete

    fun checkIsWordSaved(word: String) = wordUseCase.checkIfWordIsSaved(word).asLiveData()

    fun bookmark(
        word: String,
        wordList: List<WordModel>,
        isSaved: Boolean,
    ) {
        viewModelScope.launch {
            val result = wordUseCase.bookmarkWord(word, wordList, isSaved)
            _resultBookmark.postValue(result)
        }
    }

    fun delete(word: String) {
        viewModelScope.launch {
            wordUseCase.deleteWord(word)
            _resultDelete.postValue(true)
        }
    }
}
