package com.arrazyfathan.kbbi.feature.home.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.HttpClientFactory
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.observability.NoOpNetworkPerformanceReporter
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
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class TopWordsRemoteDataSourceTest {
    @Test
    fun `requests versioned endpoint and maps ordered items`() =
        runBlocking {
            var requestedPath: String? = null
            var requestedLimit: String? = null
            val dataSource =
                TopWordsRemoteDataSource(
                    httpClientWithMockEngine { request ->
                        requestedPath = request.url.encodedPath
                        requestedLimit = request.url.parameters["limit"]
                        respondJson(TOP_WORDS_RESPONSE)
                    },
                )

            val result = dataSource.getTopWords(2)

            assertEquals("/api/v1/words/top", requestedPath)
            assertEquals("2", requestedLimit)
            assertTrue(result is AppResult.Success)
            val words = (result as AppResult.Success).data
            assertEquals(listOf("demokrasi", "ajar"), words.map { it.word })
            assertEquals(listOf(3_000_000_000L, 8L), words.map { it.visitorCount })
        }

    @Test
    fun `returns success for empty ranking`() =
        runBlocking {
            val dataSource =
                TopWordsRemoteDataSource(
                    httpClientWithMockEngine {
                        respondJson("""{"success":true,"message":"ok","data":{"count":0,"items":[]}}""")
                    },
                )

            val result = dataSource.getTopWords(10)

            assertTrue(result is AppResult.Success)
            assertTrue((result as AppResult.Success).data.isEmpty())
        }

    @Test
    fun `maps unsuccessful envelope to remote error`() =
        runBlocking {
            val dataSource =
                TopWordsRemoteDataSource(
                    httpClientWithMockEngine {
                        respondJson("""{"success":false,"message":"Service unavailable"}""")
                    },
                )

            val result = dataSource.getTopWords(10)

            assertEquals(DataError.Remote("Service unavailable"), (result as AppResult.Error).error)
        }

    @Test
    fun `maps missing successful data to empty body`() =
        runBlocking {
            val dataSource =
                TopWordsRemoteDataSource(
                    httpClientWithMockEngine {
                        respondJson("""{"success":true,"message":"ok"}""")
                    },
                )

            val result = dataSource.getTopWords(10)

            assertEquals(DataError.EmptyBody, (result as AppResult.Error).error)
        }

    @Test
    fun `preserves shared http error mapping`() =
        runBlocking {
            val dataSource =
                TopWordsRemoteDataSource(
                    httpClientWithMockEngine {
                        respond(
                            content = "",
                            status = HttpStatusCode.TooManyRequests,
                        )
                    },
                )

            val result = dataSource.getTopWords(10)

            assertEquals(DataError.TooManyRequests, (result as AppResult.Error).error)
        }

    @Test
    fun `propagates request cancellation`() =
        runBlocking {
            val dataSource =
                TopWordsRemoteDataSource(
                    httpClientWithMockEngine {
                        throw CancellationException("cancelled")
                    },
                )

            try {
                dataSource.getTopWords(10)
                fail("Expected cancellation to propagate")
            } catch (_: CancellationException) {
                // Expected.
            }
        }

    private fun httpClientWithMockEngine(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ) = HttpClientFactory(
        json = Json { ignoreUnknownKeys = true },
        networkPerformanceReporter = NoOpNetworkPerformanceReporter,
    ).build(MockEngine { request -> handler(request) })

    private fun MockRequestHandleScope.respondJson(content: String) =
        respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
}

private const val TOP_WORDS_RESPONSE =
    """
    {
      "success": true,
      "message": "Top visited words fetched successfully",
      "data": {
        "count": 2,
        "items": [
          { "word": "demokrasi", "visitorCount": 3000000000 },
          { "word": "ajar", "visitorCount": 8 }
        ]
      }
    }
    """
