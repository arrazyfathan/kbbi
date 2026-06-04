package com.arrazyfathan.kbbi

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
import com.arrazyfathan.kbbi.navigation.MainApp
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.utils.updateSystemBarStyle
import com.arrazyfathan.kbbi.feature.splash.presentation.navigation.SplashRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        updateSystemBarStyle(
            ContextCompat.getColor(this, R.color.blue_primary),
            ContextCompat.getColor(this, android.R.color.white),
        )

        setContent {
            KBBITheme {
                var isSplashVisible by rememberSaveable { mutableStateOf(true) }

                if (isSplashVisible) {
                    SplashRoute(
                        onTimeout = {
                            isSplashVisible = false
                        },
                    )
                } else {
                    MainApp()
                }
            }
        }
    }
}
