package com.arrazyfathan.kbbi.core.appupdate.data

object GitHubReleaseAssetSelector {
    fun selectDownloadUrl(
        latestVersion: String,
        assets: List<GitHubReleaseAssetDto>,
    ): String? {
        val expectedName = "kbbi-v$latestVersion-release.apk"

        return assets
            .firstOrNull { asset -> asset.name.equals(expectedName, ignoreCase = true) }
            ?.browserDownloadUrl
            ?: assets
                .firstOrNull { asset -> asset.name.endsWith(".apk", ignoreCase = true) }
                ?.browserDownloadUrl
    }

    fun selectPolicyUrl(assets: List<GitHubReleaseAssetDto>): String? =
        assets
            .firstOrNull { asset -> asset.name.equals(POLICY_ASSET_NAME, ignoreCase = true) }
            ?.browserDownloadUrl

    const val POLICY_ASSET_NAME = "kbbi-update-policy.json"
}
