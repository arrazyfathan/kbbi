package com.example.kbbikamusbesarbahasaindonesia.ui.detail

import androidx.lifecycle.*
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import com.example.kbbikamusbesarbahasaindonesia.repository.KataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class DetailViewModel(
    private val repository: KataRepository
) : ViewModel() {



    fun isSaved(id: String): Boolean {
        return repository.kataIsExists(id)
    }

    fun insert(kata: Kata?) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(kata)
    }

    fun delete(kata: Kata) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(kata)
    }


}


class DetailViewModelFactory(private val repository: KataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}