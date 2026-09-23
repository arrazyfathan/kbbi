package com.arrazyfathan.kbbi.feature.figure.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.feature.figure.presentation.figure.FigureRoot

@Composable
fun FigureRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FigureRoot(onNavigateBack = onNavigateBack, modifier = modifier)
}
