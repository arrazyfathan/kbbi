package com.example.kbbikamusbesarbahasaindonesia.ui.saved

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import com.example.kbbikamusbesarbahasaindonesia.repository.KataRepository
import java.lang.IllegalArgumentException

class SavedViewModel(
    private val repository: KataRepository
) : ViewModel() {

    val savedKata: LiveData<List<Kata>> = repository.allSavedKata.asLiveData()

}


class SavedViewModelFactory(private val repository: KataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavedViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}