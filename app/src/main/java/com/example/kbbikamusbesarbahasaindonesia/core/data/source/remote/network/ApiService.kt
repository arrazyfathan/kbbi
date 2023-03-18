package com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.network

import com.example.kbbikamusbesarbahasaindonesia.core.data.source.remote.response.ListWordResponse
import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("/cari/{kata}")
    fun getArtiKata(
        @Path("kata") kata: String,
    ): Call<Kata>

    @GET("/cari/{word}")
    suspend fun getMeaningWord(@Path("word") word: String): ListWordResponse
}
