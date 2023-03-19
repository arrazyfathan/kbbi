package com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.network

import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.response.ListWordResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("/cari/{word}")
    suspend fun getMeaningWord(@Path("word") word: String): ListWordResponse
}
