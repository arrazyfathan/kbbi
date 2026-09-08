package com.arrazyfathan.kbbi.core.appupdate.data

import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRequirement
import com.arrazyfathan.kbbi.core.data.remote.network.HttpClientFactory
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.observability.NoOpNetworkPerformanceReporter
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubAppUpdateRepositoryTest {
    @Test
    fun `major update is required without policy asset`() =
        runBlocking {
            val store = FakeAppUpdateStore()
            val repository = repository(store, releaseResponse(version = "6.0", includePolicy = false))

            val result = repository.checkForUpdate(currentVersion = "5.24") as AppResult.Success

            assertEquals(AppUpdateRequirement.REQUIRED, result.data?.requirement)
            assertEquals(result.data, store.requiredUpdate)
            assertEquals(true, store.lastCheckSuccessful)
        }

    @Test
    fun `same major update is required when policy forces it`() =
        runBlocking {
            val store = FakeAppUpdateStore()
            val repository =
                repository(
                    store = store,
                    releaseJson = releaseResponse(version = "5.25"),
                    policyJson = policyResponse(version = "5.25", forceUpdate = true),
                )

            val result = repository.checkForUpdate(currentVersion = "5.24") as AppResult.Success

            assertEquals(AppUpdateRequirement.REQUIRED, result.data?.requirement)
            assertEquals("5.25", store.requiredUpdate?.latestVersion)
        }

    @Test
    fun `mismatched policy defaults same major update to optional and schedules retry`() =
        runBlocking {
            val store = FakeAppUpdateStore()
            val repository =
                repository(
                    store = store,
                    releaseJson = releaseResponse(version = "5.25"),
                    policyJson = policyResponse(version = "5.24", forceUpdate = true),
                )

            val result = repository.checkForUpdate(currentVersion = "5.24") as AppResult.Success

            assertEquals(AppUpdateRequirement.OPTIONAL, result.data?.requirement)
            assertNull(store.requiredUpdate)
            assertEquals(false, store.lastCheckSuccessful)
        }

    @Test
    fun `cached required update is enforced without network check`() =
        runBlocking {
            val cached = requiredUpdate("6.0")
            val store = FakeAppUpdateStore(shouldCheck = false, requiredUpdate = cached)
            var requestCount = 0
            val repository = repository(store, releaseResponse("7.0")) { requestCount += 1 }

            val result = repository.checkForUpdate(currentVersion = "5.24") as AppResult.Success

            assertEquals(cached, result.data)
            assertEquals(0, requestCount)
        }

    @Test
    fun `installed required version clears cached enforcement`() =
        runBlocking {
            val store = FakeAppUpdateStore(shouldCheck = false, requiredUpdate = requiredUpdate("6.0"))
            val repository = repository(store, releaseResponse("6.0", includePolicy = false))

            val result = repository.checkForUpdate(currentVersion = "6.0") as AppResult.Success

            assertNull(result.data)
            assertNull(store.requiredUpdate)
        }

    @Test
    fun `network failure falls back to cached required update`() =
        runBlocking {
            val cached = requiredUpdate("6.0")
            val store = FakeAppUpdateStore(requiredUpdate = cached)
            val repository =
                repository(store, releaseResponse("6.0"), releaseStatus = HttpStatusCode.ServiceUnavailable)

            val result = repository.checkForUpdate(currentVersion = "5.24") as AppResult.Success

            assertEquals(cached, result.data)
            assertEquals(false, store.lastCheckSuccessful)
        }

    @Test
    fun `policy network failure preserves cached required update`() =
        runBlocking {
            val cached = requiredUpdate("5.25")
            val store = FakeAppUpdateStore(requiredUpdate = cached)
            val repository =
                repository(
                    store = store,
                    releaseJson = releaseResponse("5.26"),
                    policyStatus = HttpStatusCode.ServiceUnavailable,
                )

            val result = repository.checkForUpdate(currentVersion = "5.24") as AppResult.Success

            assertEquals(cached, result.data)
            assertEquals(cached, store.requiredUpdate)
            assertEquals(false, store.lastCheckSuccessful)
        }

    @Test
    fun `manual check bypasses automatic throttle`() =
        runBlocking {
            val store = FakeAppUpdateStore(shouldCheck = false)
            var requestCount = 0
            val repository = repository(store, releaseResponse("5.25", includePolicy = false)) { requestCount += 1 }

            val result = repository.checkForUpdate(currentVersion = "5.24", force = true) as AppResult.Success

            assertEquals(AppUpdateRequirement.OPTIONAL, result.data?.requirement)
            assertEquals(1, requestCount)
            assertNull(store.lastCheckSuccessful)
        }

    private fun repository(
        store: FakeAppUpdateStore,
        releaseJson: String,
        policyJson: String = policyResponse("5.25", forceUpdate = false),
        releaseStatus: HttpStatusCode = HttpStatusCode.OK,
        policyStatus: HttpStatusCode = HttpStatusCode.OK,
        onRequest: () -> Unit = {},
    ): GitHubAppUpdateRepository {
        val json = Json { ignoreUnknownKeys = true }
        val engine =
            MockEngine { request ->
                onRequest()
                if (request.url.encodedPath.endsWith(GitHubReleaseAssetSelector.POLICY_ASSET_NAME)) {
                    respond(
                        content = policyJson,
                        status = policyStatus,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString()),
                    )
                } else {
                    respond(
                        content = releaseJson,
                        status = releaseStatus,
                        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                    )
                }
            }
        val client = HttpClientFactory(json, NoOpNetworkPerformanceReporter).build(engine)
        return GitHubAppUpdateRepository(client, store, json)
    }

    private fun releaseResponse(
        version: String,
        includePolicy: Boolean = true,
    ): String {
        val policyAsset =
            if (includePolicy) {
                """,{"name":"kbbi-update-policy.json","browser_download_url":"https://example.com/kbbi-update-policy.json"}"""
            } else {
                ""
            }
        return """
            {
              "tag_name": "v$version",
              "html_url": "https://example.com/releases/v$version",
              "body": "Notes",
              "assets": [
                {"name":"kbbi-v$version-release.apk","browser_download_url":"https://example.com/kbbi.apk"}$policyAsset
              ]
            }
            """.trimIndent()
    }

    private fun policyResponse(
        version: String,
        forceUpdate: Boolean,
    ) = """{"schemaVersion":1,"releaseVersion":"$version","forceUpdate":$forceUpdate}"""

    private fun requiredUpdate(version: String) =
        AppUpdate(
            latestVersion = version,
            releaseUrl = "https://example.com/releases/v$version",
            downloadUrl = "https://example.com/kbbi.apk",
            releaseNotes = null,
            requirement = AppUpdateRequirement.REQUIRED,
        )
}

private class FakeAppUpdateStore(
    var shouldCheck: Boolean = true,
    var requiredUpdate: AppUpdate? = null,
) : AppUpdateStore {
    var lastCheckSuccessful: Boolean? = null

    override fun shouldRunAutomaticCheck(): Boolean = shouldCheck

    override fun markAutomaticCheckFinished(successful: Boolean) {
        lastCheckSuccessful = successful
    }

    override fun readRequiredUpdate(): AppUpdate? = requiredUpdate

    override fun writeRequiredUpdate(update: AppUpdate) {
        requiredUpdate = update
    }

    override fun clearRequiredUpdate() {
        requiredUpdate = null
    }
}
