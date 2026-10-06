package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arrazyfathan.kbbi.BuildConfig
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.domain.AppVersionComparator
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import org.koin.core.context.GlobalContext

class AppUpdatePushWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success()
            }
            if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return Result.success()
            val settings =
                GlobalContext
                    .get()
                    .get<NotificationSettingsRepository>()
                    .settings
                    .first()
            if (!settings.updateNotificationsEnabled ||
                (settings.permissionRequired && !settings.permissionGranted)
            ) {
                return Result.success()
            }

            val version =
                inputData.getString(KEY_VERSION_NAME)?.let(AppVersionComparator::normalize)
                    ?: return Result.failure()
            val versionCode =
                inputData.getLong(KEY_VERSION_CODE, 0L).takeIf { it > 0 }
                    ?: return Result.failure()
            val releaseId = inputData.getString(KEY_RELEASE_ID)?.takeIf { it.isNotBlank() } ?: return Result.failure()
            if (!AppVersionComparator.isNewer(version, BuildConfig.VERSION_NAME)) return Result.success()

            val preferences = applicationContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
            if (versionCode <= preferences.getLong(LAST_NOTIFIED_VERSION_CODE, 0L)) return Result.success()
            showNotification(version, releaseId)
            preferences.edit { putLong(LAST_NOTIFIED_VERSION_CODE, versionCode) }
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            Result.retry()
        }

    private fun showNotification(
        version: String,
        releaseId: String,
    ) {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    applicationContext.getString(R.string.notification_app_update_channel),
                    NotificationManager.IMPORTANCE_HIGH,
                ),
            )
        }
        val intent =
            Intent(applicationContext, MainActivity::class.java).apply {
                action = "com.arrazyfathan.kbbi.APP_UPDATE.$releaseId"
                putExtra(EXTRA_APP_UPDATE_NOTIFICATION, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val pendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                NOTIFICATION_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat
                .Builder(applicationContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_update)
                .setContentTitle(applicationContext.getString(R.string.notification_app_update_title))
                .setContentText(applicationContext.getString(R.string.notification_app_update_body, version))
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(
                        applicationContext.getString(R.string.notification_app_update_body, version),
                    ),
                ).setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        post(notification)
    }

    @SuppressLint("MissingPermission") // Permission and notification settings are checked before this call.
    private fun post(notification: Notification) {
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val KEY_VERSION_NAME = "version_name"
        const val KEY_VERSION_CODE = "version_code"
        const val KEY_RELEASE_ID = "release_id"
        const val EXTRA_APP_UPDATE_NOTIFICATION = "app_update_notification"
        const val CHANNEL_ID = "app_update_available"
        private const val NOTIFICATION_ID = 4_301
        private const val PREFERENCES = "app_update_notifications"
        private const val LAST_NOTIFIED_VERSION_CODE = "last_notified_version_code"
    }
}
