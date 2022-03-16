package com.example.kbbikamusbesarbahasaindonesia.services

import com.example.kbbikamusbesarbahasaindonesia.model.Kata
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("/cari/{kata}")
    fun getArtiKata(
        @Path("kata") kata: String
    ): Call<Kata>

}