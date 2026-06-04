package com.arrazyfathan.kbbi.feature.splash.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.feature.splash.presentation.splash.SplashScreen

@Composable
fun SplashRoute(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SplashScreen(
        onTimeout = onTimeout,
        modifier = modifier,
    )
}
