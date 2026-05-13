package com.arrazyfathan.kbbi.core.data.source.remote

import com.arrazyfathan.kbbi.core.data.source.remote.network.ApiResponse
import com.arrazyfathan.kbbi.core.data.source.remote.network.ApiService
import com.arrazyfathan.kbbi.core.data.source.remote.response.WordResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Created by Ar Razy Fathan Rabbani on 17/03/23.
 */
class RemoteDataSource(
    private val apiService: ApiService,
) {
    private companion object {
        const val HTTP_NOT_FOUND = 404
    }

    fun getMeaningOfWord(word: String): Flow<ApiResponse<List<WordResponse>>> =
        flow {
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
                    val message =
                        when (response.code()) {
                            HTTP_NOT_FOUND -> "Data tidak ditemukan"
                            else -> "Something went wrong"
                        }
                    emit(ApiResponse.Error(message))
                }
            } catch (e: IOException) {
                val message =
                    when (e) {
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
