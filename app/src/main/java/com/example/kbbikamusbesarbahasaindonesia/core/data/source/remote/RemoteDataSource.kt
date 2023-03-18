package com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote

import android.util.Log
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.network.ApiResponse
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.network.ApiService
import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.response.WordResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class RemoteDataSource(private val apiService: ApiService) {

    suspend fun getMeaningOfWord(word: String): Flow<ApiResponse<List<WordResponse>>> {
        return flow {
            try {
                val response = apiService.getMeaningWord(word)
                val data = response.data
                if (data.isNotEmpty()) emit(ApiResponse.Success(data)) else emit(ApiResponse.Empty)
            } catch (e: Exception) {
                emit(ApiResponse.Error(e.toString()))
                Log.e("RemoteDataSource: ", e.toString())
            }
        }.flowOn(Dispatchers.IO)
    }
}
