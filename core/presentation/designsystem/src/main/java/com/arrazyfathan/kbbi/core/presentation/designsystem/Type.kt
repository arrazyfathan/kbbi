package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.core.R

val InterFontFamily =
    FontFamily(
        Font(R.font.inter_regular, FontWeight.Normal),
        Font(R.font.inter_medium, FontWeight.Medium),
        Font(R.font.inter_semibold, FontWeight.SemiBold),
        Font(R.font.inter_bold, FontWeight.Bold),
        Font(R.font.inter_light, FontWeight.Light),
        Font(R.font.inter_thin, FontWeight.Thin),
        Font(R.font.inter_extrabold, FontWeight.ExtraBold),
    )

val MetropolisFontFamily =
    FontFamily(
        Font(R.font.metropolis_extrabold, FontWeight.ExtraBold),
    )

val SpaceGroteskFontFamily =
    FontFamily(
        Font(R.font.spacegrotesk_regular, FontWeight.Normal),
        Font(R.font.spacegrotesk_medium, FontWeight.Medium),
        Font(R.font.spacegrotesk_semibold, FontWeight.SemiBold),
        Font(R.font.spacegrotesk_bold, FontWeight.Bold),
        Font(R.font.spacegrotesk_light, FontWeight.Light),
    )

val KBBITypography =
    Typography(
        headlineLarge =
            TextStyle(
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = MetropolisFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
        titleLarge =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
            ),
    )
