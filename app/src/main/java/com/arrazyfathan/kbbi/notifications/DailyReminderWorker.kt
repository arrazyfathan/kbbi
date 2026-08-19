package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.feature.home.domain.repository.BookmarkRepository
import com.arrazyfathan.kbbi.feature.home.domain.repository.WordCatalogRepository
import com.arrazyfathan.kbbi.feature.proverb.domain.repository.ProverbRepository
import com.arrazyfathan.kbbi.feature.settings.domain.model.ReminderType
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext
import java.util.Calendar

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val type =
            inputData.getString(REMINDER_TYPE_KEY)?.let { runCatching { ReminderType.valueOf(it) }.getOrNull() }
                ?: return Result.failure()
        val settings =
            GlobalContext
                .get()
                .get<NotificationSettingsRepository>()
                .settings
                .first()
        val preference = settings.preference(type)
        if (!preference.enabled || (settings.permissionRequired && !settings.permissionGranted)) return Result.success()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val localizedContext = languageContext()
        val content =
            when (type) {
                ReminderType.DAILY_WORD -> wordContent(GlobalContext.get().get(), localizedContext)
                ReminderType.DAILY_PROVERB -> proverbContent(GlobalContext.get().get(), localizedContext)
                ReminderType.BOOKMARK_REVIEW -> bookmarkContent(GlobalContext.get().get(), localizedContext)
            } ?: return Result.success()

        createChannel(type, localizedContext)
        val notification =
            NotificationCompat
                .Builder(applicationContext, channelId(type))
                .setSmallIcon(com.arrazyfathan.kbbi.core.R.drawable.ic_new_icon_foreground)
                .setContentTitle(content.title)
                .setContentText(content.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(content.body))
                .setContentIntent(content.pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        NotificationManagerCompat.from(applicationContext).notify(notificationId(type), notification)
        return Result.success()
    }

    private suspend fun wordContent(
        repository: WordCatalogRepository,
        localizedContext: Context,
    ): NotificationContent? {
        val words = repository.getWords().filter { it.isNotBlank() }
        if (words.isEmpty()) return null
        val word = words.getOrNull(dailyIndex(words.size)) ?: return null
        return NotificationContent(
            title = localizedContext.getString(R.string.notification_daily_word_title),
            body = localizedContext.getString(R.string.notification_daily_word_body, word),
            pendingIntent = pendingIntent("kbbi://word/${Uri.encode(word)}"),
        )
    }

    private suspend fun proverbContent(
        repository: ProverbRepository,
        localizedContext: Context,
    ): NotificationContent? {
        val proverbs = repository.getCachedProverbsForReminder().filter { it.text.isNotBlank() && it.slug.isNotBlank() }
        if (proverbs.isEmpty()) return null
        val proverb = proverbs.getOrNull(dailyIndex(proverbs.size)) ?: return null
        return NotificationContent(
            title = localizedContext.getString(R.string.notification_daily_proverb_title),
            body = proverb.text,
            pendingIntent = pendingIntent("kbbi://proverb/${Uri.encode(proverb.slug)}"),
        )
    }

    private suspend fun bookmarkContent(
        repository: BookmarkRepository,
        localizedContext: Context,
    ): NotificationContent? {
        val count = repository.getBookmarks().first().size
        if (count == 0) return null
        return NotificationContent(
            title = localizedContext.getString(R.string.notification_bookmark_title),
            body = localizedContext.resources.getQuantityString(R.plurals.notification_bookmark_body, count, count),
            pendingIntent = pendingIntent("kbbi://bookmarks"),
        )
    }

    private fun dailyIndex(size: Int): Int {
        val calendar = Calendar.getInstance()
        return (calendar.get(Calendar.YEAR) * 366 + calendar.get(Calendar.DAY_OF_YEAR)).mod(size)
    }

    private fun pendingIntent(uri: String): PendingIntent =
        PendingIntent.getActivity(
            applicationContext,
            uri.hashCode(),
            Intent(Intent.ACTION_VIEW, Uri.parse(uri), applicationContext, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private fun createChannel(
        type: ReminderType,
        localizedContext: Context,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                channelId(type),
                localizedContext.getString(channelName(type)),
                NotificationManager.IMPORTANCE_DEFAULT,
            ),
        )
    }

    private fun languageContext(): Context = ContextCompat.getContextForLanguage(applicationContext)

    private data class NotificationContent(
        val title: String,
        val body: String,
        val pendingIntent: PendingIntent,
    )

    companion object {
        const val REMINDER_TYPE_KEY = "reminder_type"

        fun channelId(type: ReminderType) = "kbbi_reminder_${type.name.lowercase()}"

        fun notificationId(type: ReminderType) = 4_100 + type.ordinal

        private fun channelName(type: ReminderType): Int =
            when (type) {
                ReminderType.DAILY_WORD -> R.string.notification_daily_word_channel
                ReminderType.DAILY_PROVERB -> R.string.notification_daily_proverb_channel
                ReminderType.BOOKMARK_REVIEW -> R.string.notification_bookmark_channel
            }
    }
}
