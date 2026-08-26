package com.arrazyfathan.kbbi.core.appupdate.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadManager
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class AppUpdateDownloadUiState(
    val downloadState: AppUpdateDownloadState = AppUpdateDownloadState.Idle,
)

sealed interface AppUpdateDownloadAction {
    data class OnPromptShown(
        val update: AppUpdate,
    ) : AppUpdateDownloadAction

    data object OnDownloadClick : AppUpdateDownloadAction

    data object OnHostResumed : AppUpdateDownloadAction

    data object OnHostPaused : AppUpdateDownloadAction
}

sealed interface AppUpdateDownloadEvent {
    data class LaunchInstaller(
        val downloadId: Long,
    ) : AppUpdateDownloadEvent
}

class AppUpdateDownloadViewModel(
    private val downloadManager: AppUpdateDownloadManager,
) : ViewModel() {
    private val _state = MutableStateFlow(AppUpdateDownloadUiState(downloadManager.state.value))
    val state = _state.asStateFlow()

    private val _events = Channel<AppUpdateDownloadEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentUpdate: AppUpdate? = null
    private var isHostResumed = false
    private var lastInstallerDownloadId: Long? = null
    private var refreshJob: Job? = null

    init {
        viewModelScope.launch {
            downloadManager.state.collect { downloadState ->
                _state.update { it.copy(downloadState = downloadState) }
                updateRefreshPolling(downloadState)
                launchInstallerWhenReady(downloadState)
            }
        }
    }

    fun onAction(action: AppUpdateDownloadAction) {
        when (action) {
            is AppUpdateDownloadAction.OnPromptShown -> {
                currentUpdate = action.update
                downloadManager.refresh()
                launchInstallerWhenReady(downloadManager.state.value)
            }

            AppUpdateDownloadAction.OnDownloadClick -> {
                onDownloadClick()
            }

            AppUpdateDownloadAction.OnHostResumed -> {
                isHostResumed = true
                downloadManager.refresh()
                launchInstallerWhenReady(downloadManager.state.value)
            }

            AppUpdateDownloadAction.OnHostPaused -> {
                isHostResumed = false
            }
        }
    }

    private fun onDownloadClick() {
        val update = currentUpdate ?: return
        when (val downloadState = downloadManager.state.value) {
            is AppUpdateDownloadState.Ready -> {
                if (downloadState.version == update.latestVersion) {
                    emitInstallerEvent(downloadState.downloadId)
                } else {
                    downloadManager.startDownload(update)
                }
            }

            is AppUpdateDownloadState.Downloading -> {
                if (downloadState.version != update.latestVersion) downloadManager.startDownload(update)
            }

            AppUpdateDownloadState.Idle,
            is AppUpdateDownloadState.Failed,
            -> {
                downloadManager.startDownload(update)
            }
        }
    }

    private fun updateRefreshPolling(downloadState: AppUpdateDownloadState) {
        if (downloadState !is AppUpdateDownloadState.Downloading) {
            refreshJob?.cancel()
            refreshJob = null
            return
        }
        if (refreshJob?.isActive == true) return
        refreshJob =
            viewModelScope.launch {
                while (isActive && downloadManager.state.value is AppUpdateDownloadState.Downloading) {
                    delay(DOWNLOAD_REFRESH_INTERVAL_MILLIS.milliseconds)
                    downloadManager.refresh()
                }
            }
    }

    private fun launchInstallerWhenReady(downloadState: AppUpdateDownloadState) {
        val update = currentUpdate ?: return
        if (
            isHostResumed &&
            downloadState is AppUpdateDownloadState.Ready &&
            downloadState.version == update.latestVersion &&
            lastInstallerDownloadId != downloadState.downloadId
        ) {
            emitInstallerEvent(downloadState.downloadId)
        }
    }

    private fun emitInstallerEvent(downloadId: Long) {
        lastInstallerDownloadId = downloadId
        viewModelScope.launch { _events.send(AppUpdateDownloadEvent.LaunchInstaller(downloadId)) }
    }

    private companion object {
        const val DOWNLOAD_REFRESH_INTERVAL_MILLIS = 750L
    }
}
