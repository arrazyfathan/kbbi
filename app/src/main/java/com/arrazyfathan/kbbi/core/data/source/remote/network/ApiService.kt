package com.arrazyfathan.kbbi.core.data.source.remote.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendPathSegments

class ApiService(
    private val httpClient: HttpClient,
) {
    suspend fun getMeaningWord(word: String): HttpResponse =
        httpClient.get {
            url {
                appendPathSegments("search", word)
            }
        }
}
