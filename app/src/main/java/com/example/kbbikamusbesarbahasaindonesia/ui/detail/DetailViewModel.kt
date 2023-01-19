package com.example.kbbikamusbesarbahasaindonesia.ui.detail

import androidx.lifecycle.*
import com.example.kbbikamusbesarbahasaindonesia.model.History
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import com.example.kbbikamusbesarbahasaindonesia.repository.KataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

class DetailViewModel(
    private val repository: KataRepository
) : ViewModel() {

    private val _saveState = MutableLiveData<Boolean>()
    val saveState: LiveData<Boolean> get() = _saveState

    private val _isHistoryExist = MutableLiveData<Boolean>()
    val isHistoryExist: LiveData<Boolean> get() = _isHistoryExist

    fun isHistoryExist(kata: String) {
        val isExist = repository.historyIsExist(kata)
        _isHistoryExist.postValue(isExist)
    }

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

    fun insertHistory(history: History) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertHistory(history)
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
