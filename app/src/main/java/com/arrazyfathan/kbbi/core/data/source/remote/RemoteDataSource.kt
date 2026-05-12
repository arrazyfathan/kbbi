package com.arrazyfathan.kbbi.core.data.source.remote

import com.arrazyfathan.kbbi.core.data.source.remote.network.ApiResponse
import com.arrazyfathan.kbbi.core.data.source.remote.network.ApiService
import com.arrazyfathan.kbbi.core.data.source.remote.response.WordResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class RemoteDataSource(private val apiService: ApiService) {

    suspend fun getMeaningOfWord(word: String): Flow<ApiResponse<List<WordResponse>>> {
        return flow {
            try {
                val response = apiService.getMeaningWord(word)
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data.orEmpty()
                    if (body?.success == true && data.isNotEmpty()) {
                        emit(ApiResponse.Success(data))
                    } else if (body?.success == false) {
                        emit(ApiResponse.Error(body.message))
                    } else {
                        emit(ApiResponse.Empty)
                    }
                } else {
                    val message = when (response.code()) {
                        404 -> "Data tidak ditemukan"
                        else -> "Something went wrong"
                    }
                    emit(ApiResponse.Error(message))
                }
            } catch (e: Exception) {
                val message = when (e) {
                    is ConnectException -> {
                        "Tidak ada koneksi internet"
                    }
                    is UnknownHostException -> {
                        "Tidak ada koneksi internet"
                    }
                    else -> "Something went wrong"
                }
                emit(ApiResponse.Error(message))
            }
        }.flowOn(Dispatchers.IO)
    }
}
