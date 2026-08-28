package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.arrazyfathan.kbbi.core.domain.model.AppTheme

internal fun colorSchemeFor(theme: AppTheme) =
    with(theme.palette) {
        lightColorScheme(
            primary = primary,
            secondary = secondary,
            background = BlueBg,
            surface = Color.White,
            surfaceTint = Color.Transparent,
            onPrimary = Color.White,
            onSecondary =
                when (theme) {
                    AppTheme.NEON_VIOLET, AppTheme.ROYAL_OCEAN, AppTheme.DEEP_FOREST_ENERGY -> Color.White
                    AppTheme.GOLDEN_SUNSET, AppTheme.GOLDEN_CORAL_ENERGY -> Color.White
                    AppTheme.BLAZE_ORANGE -> Color.White
                },
            onBackground = TextPrimary,
            onSurface = TextPrimary,
            outlineVariant = BlueBg,
        )
    }

@Composable
fun KBBITheme(
    theme: AppTheme = AppTheme.ROYAL_OCEAN,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorSchemeFor(theme),
        typography = KBBITypography,
        content = content,
    )
}
