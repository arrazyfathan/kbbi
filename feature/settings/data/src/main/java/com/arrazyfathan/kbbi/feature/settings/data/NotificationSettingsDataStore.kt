package com.arrazyfathan.kbbi.feature.settings.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.arrazyfathan.kbbi.feature.settings.domain.model.NotificationSettings
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderPreference
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.arrazyfathan.kbbi.feature.settings.domain.service.NotificationPermissionGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationSettingsDataStore by preferencesDataStore(name = "notification_settings")

class DataStoreNotificationSettingsRepository(
    private val context: Context,
    private val permissionGateway: NotificationPermissionGateway,
) : NotificationSettingsRepository {
    override val settings: Flow<NotificationSettings> =
        context.notificationSettingsDataStore.data.map { preferences ->
            NotificationSettings(
                dailyWord = preference(preferences, ReminderType.DAILY_WORD, ReminderTime.DailyWord),
                dailyProverb = preference(preferences, ReminderType.DAILY_PROVERB, ReminderTime.DailyProverb),
                bookmarkReview = preference(preferences, ReminderType.BOOKMARK_REVIEW, ReminderTime.BookmarkReview),
                permissionGranted = permissionGateway.isGranted(),
                permissionRequired = permissionGateway.isRuntimePermissionRequired,
            )
        }

    override suspend fun setEnabled(type: ReminderType, enabled: Boolean) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[enabledKey(type)] = enabled
        }
    }

    override suspend fun setTime(type: ReminderType, time: ReminderTime) {
        context.notificationSettingsDataStore.edit { preferences ->
            preferences[hourKey(type)] = time.hour
            preferences[minuteKey(type)] = time.minute
        }
    }

    private fun preference(
        preferences: androidx.datastore.preferences.core.Preferences,
        type: ReminderType,
        defaultTime: ReminderTime,
    ): ReminderPreference =
        ReminderPreference(
            enabled = preferences[enabledKey(type)] ?: false,
            time =
                ReminderTime(
                    hour = preferences[hourKey(type)] ?: defaultTime.hour,
                    minute = preferences[minuteKey(type)] ?: defaultTime.minute,
                ),
        )

    private fun enabledKey(type: ReminderType) = booleanPreferencesKey("${type.name.lowercase()}_enabled")

    private fun hourKey(type: ReminderType) = intPreferencesKey("${type.name.lowercase()}_hour")

    private fun minuteKey(type: ReminderType) = intPreferencesKey("${type.name.lowercase()}_minute")
}
