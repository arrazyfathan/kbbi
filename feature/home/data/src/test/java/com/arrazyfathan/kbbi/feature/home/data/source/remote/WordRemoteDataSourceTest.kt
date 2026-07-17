package com.arrazyfathan.kbbi.feature.home.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.HttpClientFactory
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WordRemoteDataSourceTest {
    @Test
    fun searchSendsVisitorIdAndMapsSuccessfulResponse() =
        runBlocking {
            var requestedPath: String? = null
            var visitorId: String? = null
            val dataSource =
                WordRemoteDataSource(
                    httpClient =
                        httpClientWithMockEngine {
                            requestedPath = it.url.encodedPath
                            visitorId = it.headers["x-visitor-id"]
                            respondJson(WORD_SEARCH_RESPONSE)
                        },
                    visitorIdProvider = FakeVisitorIdProvider("mobile-visitor-1"),
                )

            val result = dataSource.getMeaningOfWord("demokrasi")

            assertEquals("/search/demokrasi", requestedPath)
            assertEquals("mobile-visitor-1", visitorId)
            assertTrue(result is AppResult.Success)
            val word = (result as AppResult.Success).data
            assertEquals("demokrasi", word.word)
            assertEquals(12, word.visitorCount)
            assertEquals("demokrasi", word.listWords.single().entry)
            assertEquals("n[Nomina]", word.listWords.single().meanings.single().wordClass)
            assertEquals("pemerintahan rakyat", word.listWords.single().meanings.single().description)
        }

    @Test
    fun searchAllowsNullVisitorCount() =
        runBlocking {
            val dataSource =
                WordRemoteDataSource(
                    httpClient =
                        httpClientWithMockEngine {
                            respondJson(WORD_SEARCH_NULL_COUNT_RESPONSE)
                        },
                    visitorIdProvider = FakeVisitorIdProvider("mobile-visitor-1"),
                )

            val result = dataSource.getMeaningOfWord("ajar")

            assertTrue(result is AppResult.Success)
            assertNull((result as AppResult.Success).data.visitorCount)
        }

    @Test
    fun searchMapsUnsuccessfulApiResponseToRemoteError() =
        runBlocking {
            val dataSource =
                WordRemoteDataSource(
                    httpClient =
                        httpClientWithMockEngine {
                            respondJson("""{"success":false,"message":"Word not found"}""")
                        },
                    visitorIdProvider = FakeVisitorIdProvider("mobile-visitor-1"),
                )

            val result = dataSource.getMeaningOfWord("missing")

            assertTrue(result is AppResult.Error)
            assertEquals(DataError.Remote("Word not found"), (result as AppResult.Error).error)
        }

    @Test
    fun searchMapsEmptyEntriesToNotFound() =
        runBlocking {
            val dataSource =
                WordRemoteDataSource(
                    httpClient =
                        httpClientWithMockEngine {
                            respondJson(WORD_SEARCH_EMPTY_ENTRIES_RESPONSE)
                        },
                    visitorIdProvider = FakeVisitorIdProvider("mobile-visitor-1"),
                )

            val result = dataSource.getMeaningOfWord("kosong")

            assertTrue(result is AppResult.Error)
            assertEquals(DataError.NotFound, (result as AppResult.Error).error)
        }

    private fun httpClientWithMockEngine(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) =
        HttpClientFactory(Json { ignoreUnknownKeys = true })
            .build(MockEngine { request -> handler(request) })

    private fun MockRequestHandleScope.respondJson(content: String) =
        respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
}

private class FakeVisitorIdProvider(
    private val visitorId: String,
) : VisitorIdProvider {
    override fun getVisitorId(): String = visitorId
}

private const val WORD_SEARCH_RESPONSE =
    """
    {
      "success": true,
      "message": "Search successful",
      "data": {
        "word": "demokrasi",
        "visitorCount": 12,
        "entries": [
          {
            "headword": "demokrasi",
            "definitions": [
              {
                "wordClass": "n[Nomina]",
                "description": "pemerintahan rakyat"
              }
            ]
          }
        ]
      }
    }
    """

private const val WORD_SEARCH_NULL_COUNT_RESPONSE =
    """
    {
      "success": true,
      "message": "Search successful",
      "data": {
        "word": "ajar",
        "visitorCount": null,
        "entries": [
          {
            "headword": "ajar",
            "definitions": [
              {
                "wordClass": "v[verba]",
                "description": "petunjuk"
              }
            ]
          }
        ]
      }
    }
    """

private const val WORD_SEARCH_EMPTY_ENTRIES_RESPONSE =
    """
    {
      "success": true,
      "message": "Search successful",
      "data": {
        "word": "kosong",
        "visitorCount": 0,
        "entries": []
      }
    }
    """
