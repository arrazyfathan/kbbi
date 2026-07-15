package com.arrazyfathan.kbbi.core.appupdate.data

import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.core.appupdate.domain.AppVersionComparator
import io.ktor.client.HttpClient

private const val LATEST_RELEASE_URL = "https://api.github.com/repos/arrazyfathan/kbbi/releases/latest"

class GitHubAppUpdateRepository(
    private val httpClient: HttpClient,
    private val preferences: AppUpdatePreferences,
) : AppUpdateRepository {
    override suspend fun checkForUpdate(
        currentVersion: String,
        force: Boolean,
    ): AppResult<AppUpdate?, DataError> {
        if (!force && !preferences.shouldRunAutomaticCheck()) {
            return AppResult.Success(null)
        }

        val result = httpClient.get<GitHubReleaseDto>(route = LATEST_RELEASE_URL)
        if (!force) {
            preferences.markAutomaticCheckFinished()
        }

        return when (result) {
            is AppResult.Error -> result
            is AppResult.Success -> result.data.toAppUpdateResult(currentVersion)
        }
    }

    private fun GitHubReleaseDto.toAppUpdateResult(currentVersion: String): AppResult<AppUpdate?, DataError> {
        val latestVersion =
            AppVersionComparator.normalize(tagName)
                ?: return AppResult.Success(null)

        return if (AppVersionComparator.isNewer(latestVersion, currentVersion)) {
            AppResult.Success(
                AppUpdate(
                    latestVersion = latestVersion,
                    releaseUrl = htmlUrl,
                    downloadUrl = GitHubReleaseAssetSelector.selectDownloadUrl(latestVersion, assets),
                    releaseNotes = body?.takeIf { it.isNotBlank() },
                ),
            )
        } else {
            AppResult.Success(null)
        }
    }
}
