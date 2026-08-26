package com.arrazyfathan.kbbi.core.appupdate.data

import android.content.Context
import androidx.core.content.edit

private const val DOWNLOAD_PREFERENCES_NAME = "app_update_download"
private const val KEY_DOWNLOAD_ID = "download_id"
private const val KEY_VERSION = "version"
private const val KEY_FILE_PATH = "file_path"

internal data class StoredAppUpdateDownload(
    val downloadId: Long,
    val version: String,
    val filePath: String,
)

internal class AppUpdateDownloadStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences(DOWNLOAD_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun read(): StoredAppUpdateDownload? {
        val downloadId = preferences.getLong(KEY_DOWNLOAD_ID, -1L)
        val version = preferences.getString(KEY_VERSION, null)
        val filePath = preferences.getString(KEY_FILE_PATH, null)
        return if (downloadId >= 0L && version != null && filePath != null) {
            StoredAppUpdateDownload(downloadId, version, filePath)
        } else {
            null
        }
    }

    fun write(download: StoredAppUpdateDownload) {
        preferences.edit(commit = true) {
            putLong(KEY_DOWNLOAD_ID, download.downloadId)
            putString(KEY_VERSION, download.version)
            putString(KEY_FILE_PATH, download.filePath)
        }
    }

    fun clear() {
        preferences.edit(commit = true) { clear() }
    }
}
