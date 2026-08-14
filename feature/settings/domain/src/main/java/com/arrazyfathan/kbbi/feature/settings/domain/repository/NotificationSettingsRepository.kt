package com.arrazyfathan.kbbi.feature.settings.domain.repository

import com.arrazyfathan.kbbi.feature.settings.domain.model.NotificationSettings
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import kotlinx.coroutines.flow.Flow

interface NotificationSettingsRepository {
    val settings: Flow<NotificationSettings>

    suspend fun setEnabled(type: ReminderType, enabled: Boolean)

    suspend fun setTime(type: ReminderType, time: ReminderTime)
}
