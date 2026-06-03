package com.arrazyfathan.kbbi.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.arrazyfathan.kbbi.R

val BluePrimary = Color(0xFF303E9F)
val BlueSecondary = Color(0xFF5763B1)
val BlueBg = Color(0xFFE8F0F1)
val TextPrimary = Color(0xFF090B1E)
val TextH1 = Color(0xFF2E494C)
val TextP = Color(0xFF7C8796)
val Red = Color(0xFFF3254F)
val MoreRed = Color(0xFFA60B2B)
val Grey = Color(0xFFD6D6D6)

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
