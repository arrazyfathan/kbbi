package com.arrazyfathan.kbbi.feature.proverb.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbRoot

@Composable
fun ProverbRoute(
    onHaptic: (KBBIHapticType) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProverbRoot(
        onHaptic = onHaptic,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
