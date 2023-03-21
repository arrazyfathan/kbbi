package com.example.kbbikamusbesarbahasaindonesia.presentation.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.kbbikamusbesarbahasaindonesia.core.data.Resource
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.WordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase.WordUseCase

/**
 * Created by Ar Razy Fathan Rabbani on 18/03/23.
 */
class WordViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {

    fun getMeaningOfWord(word: String): LiveData<Resource<List<WordModel>>> {
        return wordUseCase.getMeaningOfWord(word = word).asLiveData()
    }
}
