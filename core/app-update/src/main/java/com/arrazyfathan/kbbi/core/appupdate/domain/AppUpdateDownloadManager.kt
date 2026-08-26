package com.arrazyfathan.kbbi.core.appupdate.domain

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.StateFlow


@Immutable
sealed interface AppUpdateDownloadState {
    data object Idle : AppUpdateDownloadState

    data class Downloading(
        val downloadId: Long,
        val version: String,
        val progressPercent: Int?,
    ) : AppUpdateDownloadState

    data class Ready(
        val downloadId: Long,
        val version: String,
    ) : AppUpdateDownloadState

    data class Failed(
        val version: String,
        val reason: AppUpdateDownloadError,
    ) : AppUpdateDownloadState
}

enum class AppUpdateDownloadError {
    INVALID_URL,
    INVALID_APK,
    DOWNLOAD_FAILED,
    DOWNLOAD_MISSING,
}

interface AppUpdateDownloadManager {
    val state: StateFlow<AppUpdateDownloadState>

    fun startDownload(update: AppUpdate)

    fun refresh()
}
