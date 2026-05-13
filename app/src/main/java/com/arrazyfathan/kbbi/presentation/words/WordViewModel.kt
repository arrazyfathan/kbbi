package com.arrazyfathan.kbbi.presentation.words

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.arrazyfathan.kbbi.core.data.Resource
import com.arrazyfathan.kbbi.core.domain.model.WordModel
import com.arrazyfathan.kbbi.core.domain.usecase.WordUseCase

/**
 * Created by Ar Razy Fathan Rabbani on 18/03/23.
 */
class WordViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {
    fun getMeaningOfWord(word: String): LiveData<Resource<List<WordModel>>> =
        wordUseCase.getMeaningOfWord(word = word).asLiveData()
}
