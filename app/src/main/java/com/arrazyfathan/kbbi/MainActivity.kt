package com.arrazyfathan.kbbi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.feature.splash.presentation.navigation.SplashRoute
import com.arrazyfathan.kbbi.navigation.AppShortcutRequest
import com.arrazyfathan.kbbi.navigation.MainApp

class MainActivity : ComponentActivity() {
    private var externalSearchQuery by mutableStateOf<String?>(null)
    private var externalSearchRequestKey by mutableStateOf(0L)
    private var shortcutRequest by mutableStateOf<AppShortcutRequest?>(null)
    private var shortcutRequestKey by mutableStateOf(0L)

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
                    mutableStateOf(externalSearchQuery == null && shortcutRequest == null)
                }

                LaunchedEffect(externalSearchQuery, shortcutRequest) {
                    if (externalSearchQuery != null || shortcutRequest != null) {
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
                        externalSearchQuery = externalSearchQuery,
                        externalSearchRequestKey = externalSearchRequestKey,
                        onExternalSearchConsumed = {
                            externalSearchQuery = null
                        },
                        shortcutRequest = shortcutRequest,
                        shortcutRequestKey = shortcutRequestKey,
                        onShortcutConsumed = {
                            shortcutRequest = null
                        },
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
        if (query != null) {
            shortcutRequest = null
            externalSearchQuery = query
            externalSearchRequestKey += 1
            return
        }

        val shortcut = AppShortcutRequest.fromAction(intent.action) ?: return
        externalSearchQuery = null
        shortcutRequest = shortcut
        shortcutRequestKey += 1
    }
}
