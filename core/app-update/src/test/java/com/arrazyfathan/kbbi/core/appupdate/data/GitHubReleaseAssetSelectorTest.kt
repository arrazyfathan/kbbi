package com.arrazyfathan.kbbi.core.appupdate.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GitHubReleaseAssetSelectorTest {
    @Test
    fun `matching release apk is preferred`() {
        val assets =
            listOf(
                GitHubReleaseAssetDto("other.apk", "https://example.com/other.apk"),
                GitHubReleaseAssetDto("kbbi-v1.2.0-release.apk", "https://example.com/kbbi.apk"),
            )

        assertEquals(
            "https://example.com/kbbi.apk",
            GitHubReleaseAssetSelector.selectDownloadUrl("1.2.0", assets),
        )
    }

    @Test
    fun `first apk is used as fallback`() {
        val assets =
            listOf(
                GitHubReleaseAssetDto("release-notes.txt", "https://example.com/notes.txt"),
                GitHubReleaseAssetDto("kbbi.apk", "https://example.com/kbbi.apk"),
            )

        assertEquals(
            "https://example.com/kbbi.apk",
            GitHubReleaseAssetSelector.selectDownloadUrl("1.2.0", assets),
        )
    }

    @Test
    fun `null is returned when no apk asset exists`() {
        val assets =
            listOf(
                GitHubReleaseAssetDto("release-notes.txt", "https://example.com/notes.txt"),
            )

        assertNull(GitHubReleaseAssetSelector.selectDownloadUrl("1.2.0", assets))
    }
}
