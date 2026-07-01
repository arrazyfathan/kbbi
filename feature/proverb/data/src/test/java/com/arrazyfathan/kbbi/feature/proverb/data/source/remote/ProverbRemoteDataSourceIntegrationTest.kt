package com.arrazyfathan.kbbi.feature.proverb.data.source.remote

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
import org.junit.Assert.assertTrue
import org.junit.Test

class ProverbRemoteDataSourceIntegrationTest {
    @Test
    fun getProverbsRequestsSearchRouteAndMapsSuccessfulResponse() =
        runBlocking {
            var requestedPath: String? = null
            var requestedPage: String? = null
            var requestedLimit: String? = null
            var requestedQuery: String? = null
            val dataSource =
                ProverbRemoteDataSource(
                    httpClient =
                        httpClientWithMockEngine {
                            requestedPath = it.url.encodedPath
                            requestedPage = it.url.parameters["page"]
                            requestedLimit = it.url.parameters["limit"]
                            requestedQuery = it.url.parameters["q"]
                            respondJson(PROVERB_LIST_RESPONSE)
                        },
                )

            val result = dataSource.getProverbs(page = 2, limit = 20, query = "air")

            assertEquals("/proverb/search", requestedPath)
            assertEquals("2", requestedPage)
            assertEquals("20", requestedLimit)
            assertEquals("air", requestedQuery)
            assertTrue(result is AppResult.Success)
            val page = (result as AppResult.Success).data
            assertEquals(2, page.page)
            assertEquals(3, page.totalPages)
            assertTrue(page.hasNextPage)
            assertEquals("Air beriak tanda tak dalam", page.items.single().text)
            assertEquals("air-beriak-tanda-tak-dalam", page.items.single().slug)
        }

    @Test
    fun getProverbMeaningEncodesSlugAndMapsSuccessfulResponse() =
        runBlocking {
            var requestedPath: String? = null
            val dataSource =
                ProverbRemoteDataSource(
                    httpClient =
                        httpClientWithMockEngine {
                            requestedPath = it.url.encodedPath
                            respondJson(PROVERB_DETAIL_RESPONSE)
                        },
                )

            val result = dataSource.getProverbMeaning("air beriak tanda tak dalam")

            assertEquals("/proverb/air%20beriak%20tanda%20tak%20dalam", requestedPath)
            assertTrue(result is AppResult.Success)
            val detail = (result as AppResult.Success).data
            assertEquals("Air beriak tanda tak dalam", detail.text)
            assertEquals("A", detail.letter)
            assertEquals("air-beriak-tanda-tak-dalam", detail.slug)
            assertEquals(
                "orang yang banyak bicara biasanya tidak berilmu; orang yang terlalu banyak berbicara adalah orang yang tidak terlalu paham masalah pembicaraannya",
                detail.meaning,
            )
        }

    @Test
    fun getProverbMeaningMapsUnsuccessfulApiResponseToRemoteError() =
        runBlocking {
            val dataSource =
                ProverbRemoteDataSource(
                    httpClient =
                        httpClientWithMockEngine {
                            respondJson("""{"success":false,"message":"Proverb not found","data":null}""")
                        },
                )

            val result = dataSource.getProverbMeaning("missing")

            assertTrue(result is AppResult.Error)
            assertEquals(DataError.Remote("Proverb not found"), (result as AppResult.Error).error)
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

private const val PROVERB_LIST_RESPONSE =
    """
    {
      "success": true,
      "message": "Success",
      "data": {
        "source": "KBBI",
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
            "text": "Air beriak tanda tak dalam",
            "letter": "A",
            "slug": "air-beriak-tanda-tak-dalam",
            "sourceUrl": "https://example.com/proverb/air-beriak-tanda-tak-dalam"
          }
        ]
      }
    }
    """

private const val PROVERB_DETAIL_RESPONSE =
    """
    {
      "success": true,
      "message": "Success",
      "data": {
        "text": "Air beriak tanda tak dalam",
        "letter": "A",
        "slug": "air-beriak-tanda-tak-dalam",
        "sourceUrl": "https://example.com/proverb/air-beriak-tanda-tak-dalam",
        "meaning": "orang yang banyak bicara biasanya tidak berilmu; orang yang terlalu banyak berbicara adalah orang yang tidak terlalu paham masalah pembicaraannya"
      }
    }
    """
