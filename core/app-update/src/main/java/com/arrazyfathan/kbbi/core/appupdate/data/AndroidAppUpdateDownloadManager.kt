package com.arrazyfathan.kbbi.core.appupdate.data

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import androidx.core.net.toUri
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.domain.ApkValidationResult
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadError
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadManager
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AndroidAppUpdateDownloadManager(
    context: Context,
) : AppUpdateDownloadManager {
    private val appContext = context.applicationContext
    private val downloadManager = appContext.getSystemService(DownloadManager::class.java)
    private val store = AppUpdateDownloadStore(appContext)
    private val _state = MutableStateFlow<AppUpdateDownloadState>(AppUpdateDownloadState.Idle)
    override val state = _state.asStateFlow()

    init {
        refresh()
    }

    override fun startDownload(update: AppUpdate) {
        val downloadUrl = update.downloadUrl
        if (downloadUrl == null || downloadUrl.toUri().scheme?.equals("https", ignoreCase = true) != true) {
            _state.value = AppUpdateDownloadState.Failed(update.latestVersion, AppUpdateDownloadError.INVALID_URL)
            return
        }

        val existing = store.read()
        if (existing != null) {
            refresh()
            val currentState = _state.value
            if (
                existing.version == update.latestVersion &&
                (currentState is AppUpdateDownloadState.Downloading || currentState is AppUpdateDownloadState.Ready)
            ) {
                return
            }
            downloadManager.remove(existing.downloadId)
            File(existing.filePath).delete()
            store.clear()
        }

        val downloadsDirectory = appContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        if (downloadsDirectory == null) {
            _state.value = AppUpdateDownloadState.Failed(update.latestVersion, AppUpdateDownloadError.DOWNLOAD_FAILED)
            return
        }
        val updateDirectory = File(downloadsDirectory, UPDATE_DIRECTORY)
        if (!updateDirectory.exists() && !updateDirectory.mkdirs()) {
            _state.value = AppUpdateDownloadState.Failed(update.latestVersion, AppUpdateDownloadError.DOWNLOAD_FAILED)
            return
        }
        val safeVersion = update.latestVersion.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val destination = File(updateDirectory, "kbbi-v$safeVersion-release.apk")
        destination.delete()

        val request =
            DownloadManager
                .Request(downloadUrl.toUri())
                .setTitle(appContext.getString(R.string.update_download_notification_title, update.latestVersion))
                .setDescription(appContext.getString(R.string.update_download_notification_description))
                .setMimeType(APK_MIME_TYPE)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(false)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destination))

        runCatching { downloadManager.enqueue(request) }
            .onSuccess { downloadId ->
                store.write(StoredAppUpdateDownload(downloadId, update.latestVersion, destination.absolutePath))
                _state.value = AppUpdateDownloadState.Downloading(downloadId, update.latestVersion, null)
            }.onFailure {
                _state.value =
                    AppUpdateDownloadState.Failed(update.latestVersion, AppUpdateDownloadError.DOWNLOAD_FAILED)
            }
    }

    override fun refresh() {
        val stored =
            store.read() ?: run {
                _state.value = AppUpdateDownloadState.Idle
                return
            }
        val cursor =
            downloadManager.query(
                DownloadManager.Query().setFilterById(stored.downloadId),
            )
        cursor.use {
            if (!it.moveToFirst()) {
                _state.value = AppUpdateDownloadState.Failed(stored.version, AppUpdateDownloadError.DOWNLOAD_MISSING)
                store.clear()
                return
            }
            _state.value = it.toDownloadState(stored)
            if (_state.value is AppUpdateDownloadState.Failed) {
                File(stored.filePath).delete()
                store.clear()
            }
        }
    }

    internal fun refreshCompletedDownload(downloadId: Long): Boolean {
        if (store.read()?.downloadId != downloadId) return false
        refresh()
        return _state.value is AppUpdateDownloadState.Ready
    }

    internal fun storedDownload(downloadId: Long): StoredAppUpdateDownload? =
        store.read()?.takeIf { it.downloadId == downloadId }

    internal fun clearInvalidDownload(downloadId: Long) {
        val stored = storedDownload(downloadId) ?: return
        File(stored.filePath).delete()
        downloadManager.remove(downloadId)
        store.clear()
        _state.value = AppUpdateDownloadState.Failed(stored.version, AppUpdateDownloadError.DOWNLOAD_FAILED)
    }

    private fun Cursor.toDownloadState(stored: StoredAppUpdateDownload): AppUpdateDownloadState {
        val status = getInt(getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
        return when (status) {
            DownloadManager.STATUS_PENDING,
            DownloadManager.STATUS_PAUSED,
            DownloadManager.STATUS_RUNNING,
            -> {
                val downloaded = getLong(getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = getLong(getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val progress = if (total > 0L) ((downloaded * 100L) / total).toInt().coerceIn(0, 100) else null
                AppUpdateDownloadState.Downloading(stored.downloadId, stored.version, progress)
            }

            DownloadManager.STATUS_SUCCESSFUL -> {
                val downloadedFile = File(stored.filePath)
                when {
                    !downloadedFile.isFile || downloadedFile.length() <= 0L -> {
                        AppUpdateDownloadState.Failed(stored.version, AppUpdateDownloadError.DOWNLOAD_MISSING)
                    }

                    ApkArchiveValidator(appContext).validate(downloadedFile) != ApkValidationResult.VALID -> {
                        AppUpdateDownloadState.Failed(stored.version, AppUpdateDownloadError.INVALID_APK)
                    }

                    else -> {
                        AppUpdateDownloadState.Ready(stored.downloadId, stored.version)
                    }
                }
            }

            else -> {
                AppUpdateDownloadState.Failed(stored.version, AppUpdateDownloadError.DOWNLOAD_FAILED)
            }
        }
    }

    companion object {
        internal const val APK_MIME_TYPE = "application/vnd.android.package-archive"
        private const val UPDATE_DIRECTORY = "updates"
    }
}
