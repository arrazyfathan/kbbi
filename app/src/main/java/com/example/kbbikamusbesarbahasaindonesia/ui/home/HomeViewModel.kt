package com.example.kbbikamusbesarbahasaindonesia.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.kbbikamusbesarbahasaindonesia.model.History
import com.example.kbbikamusbesarbahasaindonesia.repository.KataRepository
import com.example.kbbikamusbesarbahasaindonesia.ui.detail.DetailViewModel
import java.lang.IllegalArgumentException

/**
 * Created by Ar Razy Fathan Rabbani on 19/01/23.
 */
class HomeViewModel(private val repository: KataRepository) : ViewModel() {

    val historyList: LiveData<List<History>> = repository.listHistory.asLiveData()
}

class HomeViewModelFactory(private val repository: KataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
