package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
    lightColorScheme(
        primary = BluePrimary,
        secondary = BlueSecondary,
        background = BlueBg,
        surface = Color.White,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = TextPrimary,
        onSurface = TextPrimary,
    )

@Composable
fun KBBITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = KBBITypography,
        content = content,
    )
}
