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

    private val _saveState = MutableLiveData<Boolean>()
    val saveState: LiveData<Boolean> get() = _saveState


    fun isSaved(id: String) {
        val state = repository.kataIsExists(id)
        _saveState.postValue(state)
    }

    fun insert(kata: Kata?) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(kata)
        _saveState.postValue(true)
    }

    fun delete(kata: Kata) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(kata)
        _saveState.postValue(false)
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