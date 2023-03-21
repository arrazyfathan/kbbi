package com.example.kbbikamusbesarbahasaindonesia.presentation.bookmark

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase.WordUseCase
import kotlinx.coroutines.launch

class SavedViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {
    fun getBookmarks(): LiveData<List<ListWordModel>> = wordUseCase.getBookmarks().asLiveData()
    fun removeFromBookmark(word: String) {
        viewModelScope.launch {
            wordUseCase.deleteWord(word)
        }
    }
}
