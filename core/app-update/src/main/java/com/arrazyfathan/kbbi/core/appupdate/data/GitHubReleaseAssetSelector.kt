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
}
