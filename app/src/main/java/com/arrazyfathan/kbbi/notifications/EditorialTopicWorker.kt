package com.arrazyfathan.kbbi.notifications

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
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

class EditorialTopicWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result =
        try {
            val enabled =
                GlobalContext.get().get<NotificationSettingsRepository>().settings.first().let {
                    it.campaignNotificationsEnabled && (!it.permissionRequired || it.permissionGranted)
                }
            if (!enabled) {
                val prefs = applicationContext.getSharedPreferences("editorial_deliveries", Context.MODE_PRIVATE)
                prefs.all.keys.forEach { key ->
                    NotificationManagerCompat.from(applicationContext).cancel(key.hashCode())
                }
            }
            val prefix = if (applicationContext.isProductionFlavor()) "production_" else "development_"
            listOf("word_of_day", "trending_words", "proverbs").forEach { topic ->
                val task =
                    if (enabled) {
                        FirebaseMessaging.getInstance().subscribeToTopic(prefix + topic)
                    } else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(prefix + topic)
                    }
                task.awaitResult()
            }
            Result.success()
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            Result.retry()
        }
}

private suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { if (continuation.isActive) continuation.resume(it) }
        addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
        addOnCanceledListener { continuation.cancel() }
    }
