package com.arrazyfathan.kbbi

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.feature.splash.presentation.navigation.SplashRoute
import com.arrazyfathan.kbbi.intent.NotificationLaunchRequest
import com.arrazyfathan.kbbi.intent.extractExternalSearchQuery
import com.arrazyfathan.kbbi.intent.extractNotificationLaunchRequest
import com.arrazyfathan.kbbi.navigation.AppShortcutRequest
import com.arrazyfathan.kbbi.navigation.MainApp
import com.arrazyfathan.kbbi.navigation.MainAppLaunchRequests
import com.arrazyfathan.kbbi.notifications.AppUpdateTopicWorker
import com.arrazyfathan.kbbi.notifications.EditorialMessagingService
import com.arrazyfathan.kbbi.ui.AppUiViewModel
import com.arrazyfathan.kbbi.widgets.WidgetLaunchRequest
import com.arrazyfathan.kbbi.widgets.extractWidgetLaunchRequest
import org.koin.androidx.compose.koinViewModel

class MainActivity : AppCompatActivity() {
    private var externalSearchQuery by mutableStateOf<String?>(null)
    private var externalSearchRequestKey by mutableLongStateOf(0L)
    private var shortcutRequest by mutableStateOf<AppShortcutRequest?>(null)
    private var shortcutRequestKey by mutableLongStateOf(0L)
    private var notificationRequest by mutableStateOf<NotificationLaunchRequest?>(null)
    private var notificationRequestKey by mutableLongStateOf(0L)
    private var widgetRequest by mutableStateOf<WidgetLaunchRequest?>(null)
    private var widgetRequestKey by mutableLongStateOf(0L)
    private var showUpdateNotificationRationale by mutableStateOf(false)

    override fun onResume() {
        super.onResume()
        EditorialMessagingService.enqueueReconciliation(this)
        AppUpdateTopicWorker.enqueue(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        prepareNotificationPermissionPrompt()
        window.disableNavigationBarContrastEnforcement(Build.VERSION.SDK_INT)
        handleLaunchIntent(intent)
        setMainContent()
    }

    private fun prepareNotificationPermissionPrompt() {
        if (!shouldPromptForUpdateNotifications(isProductionFlavor(), Build.VERSION.SDK_INT)) return
        val preferences = getSharedPreferences("update_notification_permission", MODE_PRIVATE)
        val permissionGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
        showUpdateNotificationRationale = !permissionGranted && !preferences.getBoolean("explained", false)
    }

    private fun setMainContent() {
        setContent {
            val appUiViewModel: AppUiViewModel = koinViewModel()
            val appUiState by appUiViewModel.state.collectAsStateWithLifecycle()
            KBBITheme(theme = appUiState.theme) {
                MainActivityContent()
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun MainActivityContent() {
        val notificationPermissionLauncher =
            rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { }
        UpdateNotificationPermissionDialog(notificationPermissionLauncher::launch)
        val primary = MaterialTheme.colorScheme.primary.toArgb()
        val background = MaterialTheme.colorScheme.background.toArgb()
        LaunchedEffect(primary, background) {
            updateSystemBarStyle(primary, background)
        }
        LaunchRequestContent()
    }

    @androidx.compose.runtime.Composable
    private fun UpdateNotificationPermissionDialog(onRequestPermission: (String) -> Unit) {
        if (!showUpdateNotificationRationale) return
        AlertDialog(
            onDismissRequest = ::dismissUpdateNotificationRationale,
            title = { Text(stringResource(R.string.update_notification_permission_title)) },
            text = { Text(stringResource(R.string.update_notification_permission_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        dismissUpdateNotificationRationale()
                        onRequestPermission(Manifest.permission.POST_NOTIFICATIONS)
                    },
                ) {
                    Text(stringResource(R.string.update_notification_permission_allow))
                }
            },
            dismissButton = {
                TextButton(onClick = ::dismissUpdateNotificationRationale) {
                    Text(stringResource(R.string.update_notification_permission_later))
                }
            },
        )
    }

    @androidx.compose.runtime.Composable
    private fun LaunchRequestContent() {
        var isSplashVisible by rememberSaveable {
            mutableStateOf(
                hasNoLaunchRequest(externalSearchQuery, shortcutRequest, notificationRequest, widgetRequest),
            )
        }
        LaunchedEffect(externalSearchQuery, shortcutRequest, notificationRequest, widgetRequest) {
            if (!hasNoLaunchRequest(externalSearchQuery, shortcutRequest, notificationRequest, widgetRequest)) {
                isSplashVisible = false
            }
        }
        if (isSplashVisible) {
            SplashRoute(onTimeout = { isSplashVisible = false })
        } else {
            MainApp(
                launchRequests = currentLaunchRequests(),
                onExternalSearchConsumed = { externalSearchQuery = null },
                onShortcutConsumed = { shortcutRequest = null },
                onNotificationRequestConsumed = { notificationRequest = null },
                onWidgetRequestConsumed = { widgetRequest = null },
            )
        }
    }

    private fun currentLaunchRequests() =
        MainAppLaunchRequests(
            externalSearchQuery = externalSearchQuery,
            externalSearchRequestKey = externalSearchRequestKey,
            shortcutRequest = shortcutRequest,
            shortcutRequestKey = shortcutRequestKey,
            notificationRequest = notificationRequest,
            notificationRequestKey = notificationRequestKey,
            widgetRequest = widgetRequest,
            widgetRequestKey = widgetRequestKey,
        )

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent) {
        val campaignRequest = intent.extractNotificationLaunchRequest()
        val query =
            when (campaignRequest) {
                is NotificationLaunchRequest.Word -> campaignRequest.term
                else -> intent.extractExternalSearchQuery()
            }
        val notification = campaignRequest?.takeUnless { it is NotificationLaunchRequest.Word }
        val shortcut = AppShortcutRequest.fromAction(intent.action)
        val widget = intent.extractWidgetLaunchRequest()
        when {
            query != null -> {
                notificationRequest = null
                shortcutRequest = null
                widgetRequest = null
                externalSearchQuery = query
                externalSearchRequestKey += 1
            }

            notification != null -> {
                externalSearchQuery = null
                shortcutRequest = null
                widgetRequest = null
                notificationRequest = notification
                notificationRequestKey += 1
            }

            shortcut != null -> {
                externalSearchQuery = null
                widgetRequest = null
                shortcutRequest = shortcut
                shortcutRequestKey += 1
            }

            widget != null -> {
                externalSearchQuery = null
                notificationRequest = null
                shortcutRequest = null
                widgetRequest = widget
                widgetRequestKey += 1
            }
        }
    }

    private fun dismissUpdateNotificationRationale() {
        showUpdateNotificationRationale = false
        getSharedPreferences("update_notification_permission", MODE_PRIVATE)
            .edit()
            .putBoolean("explained", true)
            .apply()
    }
}

private fun shouldPromptForUpdateNotifications(
    isProductionFlavor: Boolean,
    sdkInt: Int,
) = isProductionFlavor && sdkInt >= Build.VERSION_CODES.TIRAMISU

@SuppressLint("NewApi")
private fun Window.disableNavigationBarContrastEnforcement(sdkInt: Int) {
    if (sdkInt >= Build.VERSION_CODES.Q) isNavigationBarContrastEnforced = false
}

private fun hasNoLaunchRequest(
    externalSearchQuery: String?,
    shortcutRequest: AppShortcutRequest?,
    notificationRequest: NotificationLaunchRequest?,
    widgetRequest: WidgetLaunchRequest?,
) = externalSearchQuery == null &&
    shortcutRequest == null &&
    notificationRequest == null &&
    widgetRequest == null
