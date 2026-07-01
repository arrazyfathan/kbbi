package com.arrazyfathan.kbbi.feature.proverb.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbRoot

@Composable
fun ProverbRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProverbRoot(
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
