package com.arrazyfathan.kbbi.core.data.source.remote

import com.arrazyfathan.kbbi.core.data.source.remote.network.ApiService
import com.arrazyfathan.kbbi.core.data.source.remote.response.WordResponse
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
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

    fun getMeaningOfWord(word: String): Flow<AppResult<List<WordResponse>, DataError>> =
        flow {
            try {
                val response = apiService.getMeaningWord(word)
                if (response.isSuccessful) {
                    val body = response.body()
                    val data = body?.data.orEmpty()
                    if (body?.success == true && data.isNotEmpty()) {
                        emit(AppResult.Success(data))
                    } else if (body?.success == false) {
                        emit(AppResult.Error(DataError.Remote(body.message)))
                    } else {
                        emit(AppResult.Error(DataError.NotFound))
                    }
                } else {
                    val error =
                        when (response.code()) {
                            HTTP_NOT_FOUND -> DataError.NotFound
                            else -> DataError.Unknown
                        }
                    emit(AppResult.Error(error))
                }
            } catch (e: IOException) {
                val error =
                    when (e) {
                        is ConnectException -> {
                            DataError.NoInternet
                        }
                        is UnknownHostException -> {
                            DataError.NoInternet
                        }
                        else -> DataError.Unknown
                    }
                emit(AppResult.Error(error))
            }
        }.flowOn(Dispatchers.IO)
}
