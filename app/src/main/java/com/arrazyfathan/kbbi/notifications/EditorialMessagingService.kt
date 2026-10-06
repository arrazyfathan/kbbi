package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.feature.settings.domain.repository.NotificationSettingsRepository
import com.arrazyfathan.kbbi.isProductionFlavor
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

private data class EditorialDelivery(
    val id: String,
    val title: String,
    val body: String,
    val destinationKind: String,
    val destinationValue: String,
)

private data class AppUpdateMessage(
    val version: String,
    val versionCode: Long,
    val releaseId: String,
)

private val ISO_8601 =
    Regex(
        """^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d+))?(Z|[+-]\d{2}:\d{2})$""",
    )

private fun Context.cancelEditorialNotifications(prefs: android.content.SharedPreferences) {
    prefs.all.keys.forEach { key -> NotificationManagerCompat.from(this).cancel(key.hashCode()) }
}

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
        when (data["type"]) {
            "app_update" -> data.toAppUpdateMessage()?.let(::enqueueAppUpdate)
            else -> data.toEditorialDelivery()?.takeIf { hasPermission() }?.let(::enqueueEditorialDelivery)
        }
    }

    private fun enqueueEditorialDelivery(delivery: EditorialDelivery) {
        val prefs = getSharedPreferences("editorial_deliveries", MODE_PRIVATE)
        val now = System.currentTimeMillis()
        prefs.edit {
            prefs.all
                .filterValues { it is Long && it < now - TimeUnit.DAYS.toMillis(7) }
                .keys
                .forEach(this::remove)
        }
        if (!prefs.contains(delivery.id)) serviceScope.launch { deliverEditorial(delivery, prefs, now) }
    }

    private suspend fun deliverEditorial(
        delivery: EditorialDelivery,
        prefs: android.content.SharedPreferences,
        now: Long,
    ) {
        val settings =
            GlobalContext
                .get()
                .get<NotificationSettingsRepository>()
                .settings
                .first()
        val enabled =
            settings.campaignNotificationsEnabled &&
                (!settings.permissionRequired || settings.permissionGranted) &&
                hasPermission()
        if (enabled) {
            showEditorialNotification(delivery, prefs, now)
        } else if (!settings.campaignNotificationsEnabled) {
            cancelEditorialNotifications(prefs)
        }
    }

    @SuppressLint("MissingPermission") // Permission is checked immediately before posting.
    private fun showEditorialNotification(
        delivery: EditorialDelivery,
        prefs: android.content.SharedPreferences,
        now: Long,
    ) {
        val manager = getSystemService(NotificationManager::class.java)
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
            Intent(this, MainActivity::class.java).apply {
                action = "com.arrazyfathan.kbbi.EDITORIAL.${delivery.id}"
                putExtra(EXTRA_DESTINATION_KIND, delivery.destinationKind)
                putExtra(EXTRA_DESTINATION_VALUE, delivery.destinationValue)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val pending =
            PendingIntent.getActivity(
                this,
                delivery.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val notification =
            NotificationCompat
                .Builder(this, channel)
                .setSmallIcon(R.drawable.ic_new_icon_foreground)
                .setContentTitle(delivery.title)
                .setContentText(delivery.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(delivery.body))
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()
        try {
            NotificationManagerCompat.from(this).notify(delivery.id.hashCode(), notification)
            prefs.edit { putLong(delivery.id, now) }
        } catch (_: SecurityException) {
            // Notification permission may change after validation.
        }
    }

    private fun enqueueAppUpdate(message: AppUpdateMessage) {
        if (!isProductionFlavor()) return
        val request =
            OneTimeWorkRequestBuilder<AppUpdatePushWorker>()
                .setInputData(
                    workDataOf(
                        AppUpdatePushWorker.KEY_VERSION_NAME to message.version,
                        AppUpdatePushWorker.KEY_VERSION_CODE to message.versionCode,
                        AppUpdatePushWorker.KEY_RELEASE_ID to message.releaseId,
                    ),
                ).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
        WorkManager.getInstance(this).enqueueUniqueWork(
            "app-update-push-${message.versionCode}",
            ExistingWorkPolicy.KEEP,
            request,
        )
    }

    private fun Map<String, String>.toAppUpdateMessage(): AppUpdateMessage? {
        val version = this["version_name"]?.takeIf { it.matches(Regex("v?\\d+(\\.\\d+){1,3}")) }
        val versionCode = this["version_code"]?.toLongOrNull()?.takeIf { it > 0 }
        val releaseId = this["release_id"]?.takeIf { it.isNotBlank() && it.length <= 100 }
        val expiryMillis = this["expires_at"]?.let(::parseIso8601Millis)
        val metadataValid =
            this["schema_version"] == "1" &&
                this["flavor"] == "production" &&
                expiryMillis != null &&
                expiryMillis > System.currentTimeMillis()
        return version?.let { parsedVersion ->
            versionCode?.let { parsedVersionCode ->
                releaseId?.let { parsedReleaseId ->
                    AppUpdateMessage(parsedVersion.removePrefix("v"), parsedVersionCode, parsedReleaseId)
                        .takeIf { metadataValid }
                }
            }
        }
    }

    private fun Map<String, String>.toEditorialDelivery(): EditorialDelivery? {
        val id = this["delivery_id"]?.takeIf { it.isNotBlank() }
        val title = this["title"]?.takeIf { it.isNotBlank() && it.length <= 100 }
        val body = this["body"]?.takeIf { it.isNotBlank() && it.length <= 500 }
        val destination = this["destination"]
        val destinationValue = destination?.substringAfter('/')?.takeIf { it.isNotBlank() }
        val kind =
            when (destination?.substringBefore('/')) {
                "word" -> "word"
                "proverb" -> "proverb"
                else -> null
            }
        val expiryMillis = this["expires_at"]?.let(::parseIso8601Millis)
        val metadataValid =
            this["schema_version"] == "1" &&
                this["topic"] in TOPICS &&
                !this["campaign_id"].isNullOrBlank() &&
                expiryMillis != null &&
                expiryMillis >= System.currentTimeMillis()
        val fieldsPresent = id != null && title != null && body != null && kind != null && destinationValue != null
        return if (metadataValid) {
            if (fieldsPresent) {
                EditorialDelivery(
                    requireNotNull(id),
                    requireNotNull(title),
                    requireNotNull(body),
                    requireNotNull(kind),
                    requireNotNull(destinationValue),
                )
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun reconcile() {
        AppUpdateTopicWorker.enqueue(this)
        val request =
            OneTimeWorkRequestBuilder<EditorialTopicWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
        WorkManager
            .getInstance(this)
            .enqueueUniqueWork("editorial-topic-reconcile", ExistingWorkPolicy.REPLACE, request)
    }

    private fun hasPermission() =
        (
            Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
        ) &&
            NotificationManagerCompat.from(this).areNotificationsEnabled()

    companion object {
        private val TOPICS = setOf("word_of_day", "trending_words", "proverbs")
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
