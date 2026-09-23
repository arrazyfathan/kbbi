package com.arrazyfathan.kbbi.feature.figure.data.source.remote

import com.arrazyfathan.kbbi.core.data.remote.network.HttpClientFactory
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.observability.NoOpNetworkPerformanceReporter
import io.ktor.client.HttpClient
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class FigureRemoteDataSourceTest {
    private val clients = mutableListOf<HttpClient>()

    @After
    fun tearDown() {
        clients.forEach(HttpClient::close)
    }

    @Test
    fun `list requests versioned route and maps summary items with photo`() =
        runBlocking {
            var request: HttpRequestData? = null
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine {
                        request = it
                        respondJson(FIGURE_LIST_RESPONSE)
                    },
                )

            val result = dataSource.getFigures(page = 2, limit = 20, query = "", includeDetails = false)

            assertEquals("/api/v1/figure", request?.url?.encodedPath)
            assertEquals("2", request?.url?.parameters?.get("page"))
            assertEquals("20", request?.url?.parameters?.get("limit"))
            assertEquals("false", request?.url?.parameters?.get("includeDetails"))
            assertNull(request?.url?.parameters?.get("q"))
            val page = (result as AppResult.Success).data
            assertEquals(2, page.page)
            assertEquals(3, page.totalPages)
            assertTrue(page.hasNextPage)
            assertEquals("Soekarno", page.items.single().name)
            assertEquals("https://upload.wikimedia.org/soekarno.jpg", page.items.single().photo)
        }

    @Test
    fun `search requests query and maps enriched items`() =
        runBlocking {
            var request: HttpRequestData? = null
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine {
                        request = it
                        respondJson(FIGURE_ENRICHED_LIST_RESPONSE)
                    },
                )

            val result = dataSource.getFigures(page = 1, limit = 20, query = "soekarno", includeDetails = true)

            assertEquals("/api/v1/figure/search", request?.url?.encodedPath)
            assertEquals("soekarno", request?.url?.parameters?.get("q"))
            assertEquals("true", request?.url?.parameters?.get("includeDetails"))
            val figure = (result as AppResult.Success).data.items.single()
            assertEquals("https://upload.wikimedia.org/soekarno.jpg", figure.photo)
            assertEquals(listOf("Jas merah"), figure.quotes)
        }

    @Test
    fun `detail encodes slug and maps nullable fields`() =
        runBlocking {
            var requestedPath: String? = null
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine {
                        requestedPath = it.url.encodedPath
                        respondJson(FIGURE_DETAIL_RESPONSE)
                    },
                )

            val result = dataSource.getFigure("Cut Nyak/Dien")

            assertEquals("/api/v1/figure/Cut%20Nyak%2FDien", requestedPath)
            val figure = (result as AppResult.Success).data
            assertEquals("Cut Nyak Dien", figure.name)
            assertNull(figure.photo)
            assertNull(figure.quotes)
        }

    @Test
    fun `empty list remains a successful page`() =
        runBlocking {
            val dataSource = FigureRemoteDataSource(httpClientWithMockEngine { respondJson(EMPTY_LIST_RESPONSE) })

            val result = dataSource.getFigures(1, 20, "none", false)

            assertTrue((result as AppResult.Success).data.items.isEmpty())
        }

    @Test
    fun `unsuccessful envelope maps to remote error`() =
        runBlocking {
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine {
                        respondJson("""{"success":false,"message":"Upstream unavailable"}""")
                    },
                )

            val result = dataSource.getFigures(1, 20, "", false)

            assertEquals(DataError.Remote("Upstream unavailable"), (result as AppResult.Error).error)
        }

    @Test
    fun `missing successful list data maps to empty body`() =
        runBlocking {
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine { respondJson("""{"success":true,"message":"ok"}""") },
                )

            val result = dataSource.getFigures(1, 20, "", false)

            assertEquals(DataError.EmptyBody, (result as AppResult.Error).error)
        }

    @Test
    fun `missing successful detail data maps to not found`() =
        runBlocking {
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine { respondJson("""{"success":true,"message":"ok"}""") },
                )

            val result = dataSource.getFigure("missing")

            assertEquals(DataError.NotFound, (result as AppResult.Error).error)
        }

    @Test
    fun `unsuccessful detail envelope maps to remote error`() =
        runBlocking {
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine {
                        respondJson("""{"success":false,"message":"Figure unavailable"}""")
                    },
                )

            val result = dataSource.getFigure("missing")

            assertEquals(DataError.Remote("Figure unavailable"), (result as AppResult.Error).error)
        }

    @Test
    fun `detail preserves not found http error`() =
        runBlocking {
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine {
                        respond(content = "", status = HttpStatusCode.NotFound)
                    },
                )

            val result = dataSource.getFigure("missing")

            assertEquals(DataError.NotFound, (result as AppResult.Error).error)
        }

    @Test
    fun `preserves shared http error mapping`() =
        runBlocking {
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine {
                        respond(content = "", status = HttpStatusCode.TooManyRequests)
                    },
                )

            val result = dataSource.getFigures(1, 20, "", false)

            assertEquals(DataError.TooManyRequests, (result as AppResult.Error).error)
        }

    @Test
    fun `propagates request cancellation`() =
        runBlocking {
            val dataSource =
                FigureRemoteDataSource(
                    httpClientWithMockEngine { throw CancellationException("cancelled") },
                )

            try {
                dataSource.getFigures(1, 20, "", false)
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
    ).build(MockEngine { request -> handler(request) }).also(clients::add)

    private fun MockRequestHandleScope.respondJson(content: String) =
        respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
}

private const val FIGURE_LIST_RESPONSE =
    """
    {
      "success": true,
      "message": "Indonesian figure list fetched successfully",
      "data": {
        "source": "https://id.wikiquote.org/wiki/Kategori:Tokoh_Indonesia",
        "pagination": {
          "page": 2,
          "limit": 20,
          "total": 41,
          "totalPages": 3,
          "hasNextPage": true,
          "hasPreviousPage": true
        },
        "items": [
          {
            "name": "Soekarno",
            "slug": "Soekarno",
            "sourceUrl": "https://id.wikiquote.org/wiki/Soekarno",
            "photo": "https://upload.wikimedia.org/soekarno.jpg"
          }
        ]
      }
    }
    """

private const val FIGURE_ENRICHED_LIST_RESPONSE =
    """
    {
      "success": true,
      "message": "Indonesian figure search successful",
      "data": {
        "source": "https://id.wikiquote.org/wiki/Kategori:Tokoh_Indonesia",
        "pagination": {
          "page": 1,
          "limit": 20,
          "total": 1,
          "totalPages": 1,
          "hasNextPage": false,
          "hasPreviousPage": false
        },
        "items": [
          {
            "name": "Soekarno",
            "slug": "Soekarno",
            "sourceUrl": "https://id.wikiquote.org/wiki/Soekarno",
            "photo": "https://upload.wikimedia.org/soekarno.jpg",
            "description": "Presiden pertama Republik Indonesia.",
            "quotes": ["Jas merah"]
          }
        ]
      }
    }
    """

private const val FIGURE_DETAIL_RESPONSE =
    """
    {
      "success": true,
      "message": "Indonesian figure detail fetched successfully",
      "data": {
        "name": "Cut Nyak Dien",
        "slug": "Cut_Nyak_Dien",
        "sourceUrl": "https://id.wikiquote.org/wiki/Cut_Nyak_Dien",
        "photo": null,
        "description": "Pahlawan nasional Indonesia.",
        "quotes": null
      }
    }
    """

private const val EMPTY_LIST_RESPONSE =
    """
    {
      "success": true,
      "message": "Indonesian figure search successful",
      "data": {
        "source": "https://id.wikiquote.org/wiki/Kategori:Tokoh_Indonesia",
        "pagination": {
          "page": 1,
          "limit": 20,
          "total": 0,
          "totalPages": 1,
          "hasNextPage": false,
          "hasPreviousPage": false
        },
        "items": []
      }
    }
    """
