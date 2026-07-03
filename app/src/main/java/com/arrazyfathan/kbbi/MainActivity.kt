package com.arrazyfathan.kbbi

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.feature.splash.presentation.navigation.SplashRoute
import com.arrazyfathan.kbbi.navigation.MainApp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var externalSearchQuery by mutableStateOf<String?>(null)
    private var externalSearchRequestKey by mutableStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        handleExternalSearchIntent(intent)
        updateSystemBarStyle(
            ContextCompat.getColor(this, R.color.blue_primary),
            ContextCompat.getColor(this, android.R.color.white),
        )

        setContent {
            KBBITheme {
                var isSplashVisible by rememberSaveable {
                    mutableStateOf(externalSearchQuery == null)
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
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleExternalSearchIntent(intent)
    }

    private fun handleExternalSearchIntent(intent: Intent) {
        val query = intent.extractProcessTextQuery() ?: return
        externalSearchQuery = query
        externalSearchRequestKey += 1
    }
}

private fun Intent.extractProcessTextQuery(): String? {
    if (action != Intent.ACTION_PROCESS_TEXT) return null

    return getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
        ?.toString()
        ?.toKbbiSearchQuery()
}

private fun String.toKbbiSearchQuery(): String? =
    trim()
        .split(Regex("\\s+"))
        .firstOrNull()
        ?.trim { !it.isLetterOrDigit() && it != '-' }
        ?.lowercase(Locale.ROOT)
        ?.takeIf { it.isNotBlank() }
