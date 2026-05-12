package com.arrazyfathan.kbbi.core.data.source.remote.network

import com.arrazyfathan.kbbi.core.data.source.remote.response.ListWordResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/search/{word}")
    suspend fun getMeaningWord(@Path("word") word: String): Response<ListWordResponse>
}
