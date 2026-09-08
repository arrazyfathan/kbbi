package com.arrazyfathan.kbbi.core.appupdate.data

import android.content.Context
import androidx.core.content.edit
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRequirement

private const val PREFERENCES_NAME = "app_update_preferences"
private const val KEY_LAST_ATTEMPT_MILLIS = "last_automatic_attempt_millis"
private const val KEY_LAST_SUCCESS_MILLIS = "last_automatic_success_millis"
private const val KEY_REQUIRED_VERSION = "required_version"
private const val KEY_REQUIRED_RELEASE_URL = "required_release_url"
private const val KEY_REQUIRED_DOWNLOAD_URL = "required_download_url"
private const val KEY_REQUIRED_RELEASE_NOTES = "required_release_notes"

internal interface AppUpdateStore {
    fun shouldRunAutomaticCheck(): Boolean

    fun markAutomaticCheckFinished(successful: Boolean)

    fun readRequiredUpdate(): AppUpdate?

    fun writeRequiredUpdate(update: AppUpdate)

    fun clearRequiredUpdate()
}

internal class AppUpdatePreferences(
    context: Context,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : AppUpdateStore {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun shouldRunAutomaticCheck(): Boolean {
        val nowMillis = currentTimeMillis()
        val lastAttemptMillis = preferences.getLong(KEY_LAST_ATTEMPT_MILLIS, 0L)
        val lastSuccessMillis = preferences.getLong(KEY_LAST_SUCCESS_MILLIS, 0L)
        return AppUpdateCheckCadence.shouldRun(nowMillis, lastAttemptMillis, lastSuccessMillis)
    }

    override fun markAutomaticCheckFinished(successful: Boolean) {
        val nowMillis = currentTimeMillis()
        preferences.edit {
            putLong(KEY_LAST_ATTEMPT_MILLIS, nowMillis)
            if (successful) putLong(KEY_LAST_SUCCESS_MILLIS, nowMillis)
        }
    }

    override fun readRequiredUpdate(): AppUpdate? {
        val version = preferences.getString(KEY_REQUIRED_VERSION, null)?.takeIf(String::isNotBlank) ?: return null
        val releaseUrl =
            preferences.getString(KEY_REQUIRED_RELEASE_URL, null)?.takeIf(String::isNotBlank) ?: return null
        return AppUpdate(
            latestVersion = version,
            releaseUrl = releaseUrl,
            downloadUrl = preferences.getString(KEY_REQUIRED_DOWNLOAD_URL, null),
            releaseNotes = preferences.getString(KEY_REQUIRED_RELEASE_NOTES, null),
            requirement = AppUpdateRequirement.REQUIRED,
        )
    }

    override fun writeRequiredUpdate(update: AppUpdate) {
        require(update.requirement == AppUpdateRequirement.REQUIRED)
        preferences.edit {
            putString(KEY_REQUIRED_VERSION, update.latestVersion)
            putString(KEY_REQUIRED_RELEASE_URL, update.releaseUrl)
            putString(KEY_REQUIRED_DOWNLOAD_URL, update.downloadUrl)
            putString(KEY_REQUIRED_RELEASE_NOTES, update.releaseNotes)
        }
    }

    override fun clearRequiredUpdate() {
        preferences.edit {
            remove(KEY_REQUIRED_VERSION)
            remove(KEY_REQUIRED_RELEASE_URL)
            remove(KEY_REQUIRED_DOWNLOAD_URL)
            remove(KEY_REQUIRED_RELEASE_NOTES)
        }
    }
}

internal object AppUpdateCheckCadence {
    private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1_000L
    private const val ONE_HOUR_MILLIS = 60L * 60L * 1_000L

    fun shouldRun(
        nowMillis: Long,
        lastAttemptMillis: Long,
        lastSuccessMillis: Long,
    ): Boolean {
        if (lastAttemptMillis == 0L) return true
        val retryInterval = if (lastSuccessMillis >= lastAttemptMillis) ONE_DAY_MILLIS else ONE_HOUR_MILLIS
        return nowMillis - lastAttemptMillis >= retryInterval
    }
}
