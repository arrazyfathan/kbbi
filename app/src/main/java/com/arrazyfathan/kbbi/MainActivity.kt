package com.arrazyfathan.kbbi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
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
import com.arrazyfathan.kbbi.widgets.WidgetLaunchRequest
import com.arrazyfathan.kbbi.widgets.extractWidgetLaunchRequest

class MainActivity : AppCompatActivity() {
    private var externalSearchQuery by mutableStateOf<String?>(null)
    private var externalSearchRequestKey by mutableLongStateOf(0L)
    private var shortcutRequest by mutableStateOf<AppShortcutRequest?>(null)
    private var shortcutRequestKey by mutableLongStateOf(0L)
    private var notificationRequest by mutableStateOf<NotificationLaunchRequest?>(null)
    private var notificationRequestKey by mutableLongStateOf(0L)
    private var widgetRequest by mutableStateOf<WidgetLaunchRequest?>(null)
    private var widgetRequestKey by mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        handleLaunchIntent(intent)
        updateSystemBarStyle(
            ContextCompat.getColor(this, R.color.blue_primary),
            ContextCompat.getColor(this, android.R.color.white),
        )

        setContent {
            KBBITheme {
                var isSplashVisible by rememberSaveable {
                    mutableStateOf(externalSearchQuery == null && shortcutRequest == null && widgetRequest == null)
                }

                LaunchedEffect(externalSearchQuery, shortcutRequest, widgetRequest) {
                    if (externalSearchQuery != null || shortcutRequest != null || widgetRequest != null) {
                        isSplashVisible = false
                    }
                }

                if (isSplashVisible) {
                    SplashRoute(
                        onTimeout = {
                            isSplashVisible = false
                        },
                    )
                } else {
                    MainApp(
                        launchRequests =
                            MainAppLaunchRequests(
                                externalSearchQuery = externalSearchQuery,
                                externalSearchRequestKey = externalSearchRequestKey,
                                shortcutRequest = shortcutRequest,
                                shortcutRequestKey = shortcutRequestKey,
                                notificationRequest = notificationRequest,
                                notificationRequestKey = notificationRequestKey,
                                widgetRequest = widgetRequest,
                                widgetRequestKey = widgetRequestKey,
                            ),
                        onExternalSearchConsumed = {
                            externalSearchQuery = null
                        },
                        onShortcutConsumed = {
                            shortcutRequest = null
                        },
                        onNotificationRequestConsumed = { notificationRequest = null },
                        onWidgetRequestConsumed = { widgetRequest = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLaunchIntent(intent)
    }

    private fun handleLaunchIntent(intent: Intent) {
        val query = intent.extractExternalSearchQuery()
        val notification = intent.extractNotificationLaunchRequest()
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
}
