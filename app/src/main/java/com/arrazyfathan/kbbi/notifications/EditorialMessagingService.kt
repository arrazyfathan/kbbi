package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class EditorialMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    @Deprecated("Firebase Messaging invokes this callback when the device token changes.")
    override fun onNewToken(token: String) {
        reconcile()
    }

    @SuppressLint("MissingPermission") // Permission is checked before posting and SecurityException is handled.
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        if (data["schema_version"] != "1" || data["topic"] !in TOPICS) return
        if (data["campaign_id"].isNullOrBlank()) return
        val id = data["delivery_id"]?.takeIf { it.isNotBlank() } ?: return
        val title = data["title"]?.takeIf { it.isNotBlank() && it.length <= 100 } ?: return
        val body = data["body"]?.takeIf { it.isNotBlank() && it.length <= 500 } ?: return
        val expiryMillis = data["expires_at"]?.let(::parseIso8601Millis) ?: return
        if (expiryMillis < System.currentTimeMillis()) return
        val destination = data["destination"] ?: return
        val kind =
            when {
                destination.startsWith("word/") && destination.removePrefix("word/").isNotBlank() -> "word"
                destination.startsWith("proverb/") && destination.removePrefix("proverb/").isNotBlank() -> "proverb"
                else -> return
            }
        if (!hasPermission()) return
        val prefs = getSharedPreferences("editorial_deliveries", MODE_PRIVATE)
        val now = System.currentTimeMillis()
        prefs.edit {
            prefs.all
                .filterValues { it is Long && it < now - TimeUnit.DAYS.toMillis(7) }
                .keys
                .forEach(this::remove)
        }
        if (prefs.contains(id)) return
        serviceScope.launch {
            val settings =
                GlobalContext
                    .get()
                    .get<NotificationSettingsRepository>()
                    .settings
                    .first()
            if (!settings.campaignNotificationsEnabled ||
                (settings.permissionRequired && !settings.permissionGranted)
            ) {
                if (!settings.campaignNotificationsEnabled) cancelEditorialNotifications(prefs)
                return@launch
            }
            val manager = this@EditorialMessagingService.getSystemService(NotificationManager::class.java)
            val channel = "editorial_notifications"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        channel,
                        getString(R.string.notification_editorial_title),
                        NotificationManager.IMPORTANCE_DEFAULT,
                    ),
                )
            }
            val intent =
                Intent(this@EditorialMessagingService, MainActivity::class.java).apply {
                    action = "com.arrazyfathan.kbbi.EDITORIAL.$id"
                    putExtra(EXTRA_DESTINATION_KIND, kind)
                    putExtra(EXTRA_DESTINATION_VALUE, destination.substringAfter('/'))
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            val pending =
                PendingIntent.getActivity(
                    this@EditorialMessagingService,
                    id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val notification =
                NotificationCompat
                    .Builder(this@EditorialMessagingService, channel)
                    .setSmallIcon(R.drawable.ic_new_icon_foreground)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                    .build()
            if (!hasPermission()) return@launch
            try {
                NotificationManagerCompat.from(this@EditorialMessagingService).notify(id.hashCode(), notification)
            } catch (_: SecurityException) {
                return@launch
            }
            prefs.edit { putLong(id, now) }
        }
    }

    private fun reconcile() {
        val request =
            OneTimeWorkRequestBuilder<EditorialTopicWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
        WorkManager
            .getInstance(this)
            .enqueueUniqueWork("editorial-topic-reconcile", ExistingWorkPolicy.REPLACE, request)
    }

    private fun cancelEditorialNotifications(prefs: android.content.SharedPreferences) {
        prefs.all.keys.forEach { key -> NotificationManagerCompat.from(this).cancel(key.hashCode()) }
    }

    private fun hasPermission() =
        (
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
        ) && NotificationManagerCompat.from(this).areNotificationsEnabled()

    private fun parseIso8601Millis(value: String): Long? {
        val parts = ISO_8601.find(value)?.groupValues ?: return null
        val fraction = parts[2].take(3).padEnd(3, '0')
        val zone = if (parts[3] == "Z") "+00:00" else parts[3]
        val normalized = "${parts[1]}.$fraction$zone"
        val formatter =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("UTC")
            }
        val position = ParsePosition(0)
        return formatter.parse(normalized, position)?.takeIf { position.index == normalized.length }?.time
    }

    companion object {
        private val TOPICS = setOf("word_of_day", "trending_words", "proverbs")
        private val ISO_8601 = Regex("^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})(?:\\.(\\d+))?(Z|[+-]\\d{2}:\\d{2})$")
        const val EXTRA_DESTINATION_KIND = "editorial_destination_kind"
        const val EXTRA_DESTINATION_VALUE = "editorial_destination_value"

        fun enqueueReconciliation(context: android.content.Context) {
            val request =
                OneTimeWorkRequestBuilder<EditorialTopicWorker>()
                    .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "editorial-topic-reconcile",
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
