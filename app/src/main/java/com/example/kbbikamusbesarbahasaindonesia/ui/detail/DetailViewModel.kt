package com.example.kbbikamusbesarbahasaindonesia.ui.detail

import androidx.lifecycle.* // ktlint-disable no-wildcard-imports
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase.WordUseCase
import kotlinx.coroutines.launch

class DetailViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {

    private var _resultBookmark = MutableLiveData<Long>()
    val resultBookmark: LiveData<Long> get() = _resultBookmark

    private var _resultDelete = MutableLiveData<Boolean>()
    val resultDelete: LiveData<Boolean> get() = _resultDelete

    fun checkIsWordSaved(word: String) = wordUseCase.checkIfWordIsSaved(word).asLiveData()

    fun bookmark(word: String, wordList: List<WordModel>, isSaved: Boolean) {
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
