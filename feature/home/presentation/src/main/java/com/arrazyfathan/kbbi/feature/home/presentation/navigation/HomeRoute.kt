package com.arrazyfathan.kbbi.feature.home.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeScreen

@Composable
fun HomeRoute(
    onHaptic: (KBBIHapticType) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    externalSearchQuery: String? = null,
    externalSearchRequestKey: Long = 0L,
    onExternalSearchConsumed: () -> Unit = {},
    focusSearchRequestKey: Long = 0L,
    randomWordRequestKey: Long = 0L,
    onShortcutConsumed: () -> Unit = {},
) {
    HomeScreen(
        onHaptic = onHaptic,
        externalSearchQuery = externalSearchQuery,
        externalSearchRequestKey = externalSearchRequestKey,
        onExternalSearchConsumed = onExternalSearchConsumed,
        focusSearchRequestKey = focusSearchRequestKey,
        randomWordRequestKey = randomWordRequestKey,
        onShortcutConsumed = onShortcutConsumed,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToProverb = onNavigateToProverb,
        onNavigateToSettings = onNavigateToSettings,
        modifier = modifier,
    )
}
