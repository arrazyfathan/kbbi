package com.arrazyfathan.kbbi.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderTime
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.service.ReminderScheduler
import java.util.Calendar
import java.util.concurrent.TimeUnit

private const val WORK_PERIOD_HOURS = 24L

class WorkManagerReminderScheduler(
    private val context: Context,
) : ReminderScheduler {
    override fun schedule(
        type: ReminderType,
        time: ReminderTime,
    ) {
        val request =
            PeriodicWorkRequestBuilder<DailyReminderWorker>(WORK_PERIOD_HOURS, TimeUnit.HOURS)
                .setInitialDelay(delayUntil(time), TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(DailyReminderWorker.REMINDER_TYPE_KEY to type.name))
                .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            uniqueName(type),
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    override fun cancel(type: ReminderType) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(type))
    }

    private fun uniqueName(type: ReminderType) = "kbbi_daily_reminder_${type.name.lowercase()}"

    private fun delayUntil(time: ReminderTime): Long {
        val now = Calendar.getInstance()
        val next = now.clone() as Calendar
        next.set(Calendar.HOUR_OF_DAY, time.hour)
        next.set(Calendar.MINUTE, time.minute)
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)
        if (!next.after(now)) next.add(Calendar.DAY_OF_YEAR, 1)
        return next.timeInMillis - now.timeInMillis
    }
}
