package com.example.kbbikamusbesarbahasaindonesia.ui.saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.local.entity.ListWordEntity
import com.example.kbbikamusbesarbahasaindonesia.core.domain.model.ListWordModel
import com.example.kbbikamusbesarbahasaindonesia.core.domain.usecase.WordUseCase

class SavedViewModel(
    private val wordUseCase: WordUseCase,
) : ViewModel() {

    fun getBookmarks(): LiveData<List<ListWordModel>> = wordUseCase.getBookmarks().asLiveData()
}
