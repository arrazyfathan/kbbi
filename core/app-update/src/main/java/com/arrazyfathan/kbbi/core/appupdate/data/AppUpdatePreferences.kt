package com.arrazyfathan.kbbi.core.appupdate.data

import android.content.Context
import androidx.core.content.edit

private const val PREFERENCES_NAME = "app_update_preferences"
private const val KEY_LAST_AUTOMATIC_CHECK_MILLIS = "last_automatic_check_millis"
private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1_000L

class AppUpdatePreferences(
    context: Context,
) {
    private val preferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun shouldRunAutomaticCheck(nowMillis: Long = System.currentTimeMillis()): Boolean {
        val lastCheckMillis = preferences.getLong(KEY_LAST_AUTOMATIC_CHECK_MILLIS, 0L)
        return nowMillis - lastCheckMillis >= ONE_DAY_MILLIS
    }

    fun markAutomaticCheckFinished(nowMillis: Long = System.currentTimeMillis()) {
        preferences
            .edit {
                putLong(KEY_LAST_AUTOMATIC_CHECK_MILLIS, nowMillis)
            }
    }
}
