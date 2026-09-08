package com.arrazyfathan.kbbi.core.appupdate.data

import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRepository
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRequirement
import com.arrazyfathan.kbbi.core.appupdate.domain.AppVersionComparator
import com.arrazyfathan.kbbi.core.data.remote.network.get
import com.arrazyfathan.kbbi.core.domain.model.AppResult
import com.arrazyfathan.kbbi.core.domain.model.DataError
import io.ktor.client.HttpClient
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val LATEST_RELEASE_URL = "https://api.github.com/repos/arrazyfathan/kbbi/releases/latest"

internal class GitHubAppUpdateRepository(
    private val httpClient: HttpClient,
    private val preferences: AppUpdateStore,
    private val json: Json,
) : AppUpdateRepository {
    override suspend fun checkForUpdate(
        currentVersion: String,
        force: Boolean,
    ): AppResult<AppUpdate?, DataError> {
        val cachedRequiredUpdate = validCachedRequiredUpdate(currentVersion)
        if (!force && !preferences.shouldRunAutomaticCheck()) {
            return AppResult.Success(cachedRequiredUpdate)
        }

        val result = httpClient.get<GitHubReleaseDto>(route = LATEST_RELEASE_URL)
        return when (result) {
            is AppResult.Error -> {
                markAutomaticCheckFinished(force, successful = false)
                cachedRequiredUpdate?.let { AppResult.Success(it) } ?: result
            }

            is AppResult.Success -> {
                result.data.toAppUpdateResult(currentVersion, force)
            }
        }
    }

    private suspend fun GitHubReleaseDto.toAppUpdateResult(
        currentVersion: String,
        force: Boolean,
    ): AppResult<AppUpdate?, DataError> {
        val latestVersion =
            AppVersionComparator.normalize(tagName)
                ?: return noUpdate(force)

        if (!AppVersionComparator.isNewer(latestVersion, currentVersion)) {
            return noUpdate(force)
        }

        val isMajorUpdate = AppVersionComparator.isMajorUpdate(latestVersion, currentVersion)
        val policy = loadPolicy(latestVersion, assets)
        if (!policy.successful && !isMajorUpdate) {
            validCachedRequiredUpdate(currentVersion)?.let { cachedUpdate ->
                markAutomaticCheckFinished(force, successful = false)
                return AppResult.Success(cachedUpdate)
            }
        }
        val requirement =
            if (isMajorUpdate || policy.forceUpdate) {
                AppUpdateRequirement.REQUIRED
            } else {
                AppUpdateRequirement.OPTIONAL
            }
        val update =
            AppUpdate(
                latestVersion = latestVersion,
                releaseUrl = htmlUrl,
                downloadUrl = GitHubReleaseAssetSelector.selectDownloadUrl(latestVersion, assets),
                releaseNotes = body?.takeIf { it.isNotBlank() },
                requirement = requirement,
            )

        if (requirement == AppUpdateRequirement.REQUIRED) {
            preferences.writeRequiredUpdate(update)
        } else {
            preferences.clearRequiredUpdate()
        }
        markAutomaticCheckFinished(force, successful = policy.successful)
        return AppResult.Success(update)
    }

    private suspend fun loadPolicy(
        latestVersion: String,
        assets: List<GitHubReleaseAssetDto>,
    ): LoadedPolicy {
        val policyUrl =
            GitHubReleaseAssetSelector.selectPolicyUrl(assets)
                ?: return LoadedPolicy(forceUpdate = false, successful = true)

        return when (val result = httpClient.get<String>(route = policyUrl)) {
            is AppResult.Error -> {
                LoadedPolicy(forceUpdate = false, successful = false)
            }

            is AppResult.Success -> {
                val policy =
                    try {
                        json.decodeFromString<GitHubUpdatePolicyDto>(result.data)
                    } catch (_: SerializationException) {
                        null
                    } catch (_: IllegalArgumentException) {
                        null
                    }
                val isValid =
                    policy?.schemaVersion == SUPPORTED_POLICY_SCHEMA_VERSION &&
                        AppVersionComparator.isSameVersion(policy.releaseVersion, latestVersion)
                LoadedPolicy(
                    forceUpdate = isValid && policy.forceUpdate,
                    successful = isValid,
                )
            }
        }
    }

    private fun validCachedRequiredUpdate(currentVersion: String): AppUpdate? {
        val cachedUpdate = preferences.readRequiredUpdate() ?: return null
        return if (AppVersionComparator.isNewer(cachedUpdate.latestVersion, currentVersion)) {
            cachedUpdate
        } else {
            preferences.clearRequiredUpdate()
            null
        }
    }

    private fun noUpdate(force: Boolean): AppResult<AppUpdate?, DataError> {
        preferences.clearRequiredUpdate()
        markAutomaticCheckFinished(force, successful = true)
        return AppResult.Success(null)
    }

    private fun markAutomaticCheckFinished(
        force: Boolean,
        successful: Boolean,
    ) {
        if (!force) preferences.markAutomaticCheckFinished(successful)
    }

    private data class LoadedPolicy(
        val forceUpdate: Boolean,
        val successful: Boolean,
    )

    private companion object {
        const val SUPPORTED_POLICY_SCHEMA_VERSION = 1
    }
}
