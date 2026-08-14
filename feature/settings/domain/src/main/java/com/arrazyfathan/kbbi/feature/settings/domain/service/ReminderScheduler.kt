package com.arrazyfathan.kbbi.feature.settings.domain.service

import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType

interface ReminderScheduler {
    fun schedule(type: ReminderType, time: ReminderTime)

    fun cancel(type: ReminderType)
}
