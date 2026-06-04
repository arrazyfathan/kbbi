package com.arrazyfathan.kbbi.feature.splash.presentation.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.arrazyfathan.kbbi.core.R
import androidx.compose.ui.tooling.preview.Preview
import com.arrazyfathan.kbbi.core.presentation.designsystem.BluePrimary
import com.arrazyfathan.kbbi.core.presentation.designsystem.InterFontFamily
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onTimeout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logoTranslationY = remember { Animatable(0f) }
    val readingAlpha = remember { Animatable(0.8f) }
    val readingTranslationY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoTranslationY.animateTo(
                targetValue = 100f,
                animationSpec = tween(durationMillis = 2000, easing = { it }), // Linear
            )
        }
        launch {
            readingAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 2000, easing = { it }),
            )
        }
        launch {
            readingTranslationY.animateTo(
                targetValue = -80f,
                animationSpec = tween(durationMillis = 2000, easing = { it }),
            )
        }
        delay(3000)
        onTimeout()
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(BluePrimary),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, logoTranslationY.value.toInt()) }
                    .graphicsLayer {
                        translationY = 0.2f * size.height
                    },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_kbbi),
                contentDescription = stringResource(id = R.string.logo_desc),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            val loadingComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading))
            LottieAnimation(
                composition = loadingComposition,
                iterations = LottieConstants.IterateForever,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(100.dp),
            )
        }

        val readingComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.reading))
        LottieAnimation(
            composition = readingComposition,
            isPlaying = true,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, (readingTranslationY.value + 100).toInt()) }
                    .alpha(readingAlpha.value),
        )

        val versionText =
            stringResource(
                id = R.string.version_label,
                stringResource(id = R.string.version_name),
            )
        Text(
            text = versionText,
            color = Color.White,
            fontSize = 12.sp,
            fontFamily = InterFontFamily,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    KBBITheme {
        SplashScreen(onTimeout = {})
    }
}
