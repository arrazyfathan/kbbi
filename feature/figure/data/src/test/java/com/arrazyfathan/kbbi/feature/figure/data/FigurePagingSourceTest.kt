package com.arrazyfathan.kbbi.feature.figure.data

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arrazyfathan.kbbi.core.data.remote.network.HttpClientFactory
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.observability.NoOpNetworkPerformanceReporter
import com.arrazyfathan.kbbi.feature.figure.data.source.remote.FigureRemoteDataSource
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigureModel
import com.arrazyfathan.kbbi.feature.figure.domain.model.FigurePagingException
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
import org.junit.Test

class FigurePagingSourceTest {
    private val clients = mutableListOf<HttpClient>()

    @After
    fun tearDown() {
        clients.forEach(HttpClient::close)
    }

    @Test
    fun `first page uses configured size and returns next key`() =
        runBlocking {
            var requestedPage: String? = null
            var requestedLimit: String? = null
            val pagingSource =
                pagingSource { request ->
                    requestedPage = request.url.parameters["page"]
                    requestedLimit = request.url.parameters["limit"]
                    respondJson(pageResponse(page = 1, hasNextPage = true))
                }

            val result =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 60, placeholdersEnabled = false),
                )

            assertEquals("1", requestedPage)
            assertEquals("20", requestedLimit)
            val page = result as PagingSource.LoadResult.Page
            assertNull(page.prevKey)
            assertEquals(2, page.nextKey)
            assertEquals("Figure 1", page.data.single().name)
        }

    @Test
    fun `last subsequent page returns previous key without next key`() =
        runBlocking {
            val pagingSource = pagingSource { respondJson(pageResponse(page = 3, hasNextPage = false)) }

            val result =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = 3, loadSize = 20, placeholdersEnabled = false),
                )

            val page = result as PagingSource.LoadResult.Page
            assertEquals(2, page.prevKey)
            assertNull(page.nextKey)
        }

    @Test
    fun `data error is wrapped for paging consumers`() =
        runBlocking {
            val pagingSource =
                pagingSource {
                    respond(content = "", status = HttpStatusCode.ServiceUnavailable)
                }

            val result =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
                )

            val error = (result as PagingSource.LoadResult.Error).throwable as FigurePagingException
            assertTrue(error.dataError === DataError.ServiceUnavailable)
        }

    @Test
    fun `refresh key is derived from the anchored page`() {
        val pagingSource = pagingSource { respondJson(pageResponse(page = 2, hasNextPage = true)) }
        val state =
            PagingState(
                pages =
                    listOf(
                        PagingSource.LoadResult.Page(
                            data = listOf(figure(page = 2)),
                            prevKey = 1,
                            nextKey = 3,
                        ),
                    ),
                anchorPosition = 0,
                config = PagingConfig(pageSize = 20),
                leadingPlaceholderCount = 0,
            )

        assertEquals(2, pagingSource.getRefreshKey(state))
    }

    private fun pagingSource(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): FigurePagingSource {
        val httpClient =
            HttpClientFactory(
                json = Json { ignoreUnknownKeys = true },
                networkPerformanceReporter = NoOpNetworkPerformanceReporter,
            ).build(MockEngine { request -> handler(request) }).also(clients::add)
        return FigurePagingSource(
            remoteDataSource = FigureRemoteDataSource(httpClient),
            query = "",
            includeDetails = false,
            pageSize = 20,
        )
    }

    private fun MockRequestHandleScope.respondJson(content: String) =
        respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
}

private fun figure(page: Int) =
    FigureModel(
        name = "Figure $page",
        slug = "Figure_$page",
        sourceUrl = "https://id.wikiquote.org/wiki/Figure_$page",
    )

private fun pageResponse(
    page: Int,
    hasNextPage: Boolean,
): String =
    """
    {
      "success": true,
      "message": "ok",
      "data": {
        "source": "https://id.wikiquote.org",
        "pagination": {
          "page": $page,
          "limit": 20,
          "total": 41,
          "totalPages": 3,
          "hasNextPage": $hasNextPage,
          "hasPreviousPage": ${page > 1}
        },
        "items": [
          {
            "name": "Figure $page",
            "slug": "Figure_$page",
            "sourceUrl": "https://id.wikiquote.org/wiki/Figure_$page"
          }
        ]
      }
    }
    """
