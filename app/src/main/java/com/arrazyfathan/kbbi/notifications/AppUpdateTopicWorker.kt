package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.arrazyfathan.kbbi.isProductionFlavor
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.core.context.GlobalContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.arrazyfathan.kbbi.core.R as CoreR

class AppUpdateTopicWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        try {
            if (!applicationContext.isProductionFlavor()) {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC).awaitResult()
                return Result.success()
            }
            val settings =
                GlobalContext
                    .get()
                    .get<NotificationSettingsRepository>()
                    .settings
                    .first()
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        AppUpdatePushWorker.CHANNEL_ID,
                        applicationContext.getString(CoreR.string.notification_app_update_channel),
                        NotificationManager.IMPORTANCE_HIGH,
                    ),
                )
            }
            val permissionGranted =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
            val channelEnabled =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                    manager.getNotificationChannel(AppUpdatePushWorker.CHANNEL_ID)?.importance !=
                    NotificationManager.IMPORTANCE_NONE
            val enabled =
                settings.updateNotificationsEnabled &&
                    (!settings.permissionRequired || settings.permissionGranted) &&
                    permissionGranted &&
                    channelEnabled &&
                    NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
            val task =
                if (enabled) {
                    FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)
                } else {
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(TOPIC)
                }
            task.awaitResult()
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            Result.retry()
        }

    companion object {
        const val TOPIC = "production_app_updates"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<AppUpdateTopicWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "app-update-topic-reconcile",
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}

private suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
        addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        addOnCanceledListener { continuation.cancel() }
    }
