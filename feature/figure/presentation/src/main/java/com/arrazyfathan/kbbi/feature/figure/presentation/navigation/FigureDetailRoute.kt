package com.arrazyfathan.kbbi.feature.figure.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.feature.figure.presentation.figure.FigureDetailRoot

@Composable
fun FigureDetailRoute(
    slug: String,
    onNavigateBack: () -> Unit,
    onOpenSourceUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FigureDetailRoot(
        slug = slug,
        onNavigateBack = onNavigateBack,
        onOpenSourceUrl = onOpenSourceUrl,
        modifier = modifier,
    )
}
