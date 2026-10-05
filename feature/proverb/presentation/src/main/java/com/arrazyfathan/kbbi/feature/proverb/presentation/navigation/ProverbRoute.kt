package com.arrazyfathan.kbbi.feature.proverb.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.feature.proverb.presentation.proverb.ProverbRoot

@Composable
fun ProverbRoute(
    modifier: Modifier = Modifier,
    onHaptic: (KBBIHapticType) -> Unit,
    onNavigateBack: () -> Unit,
    campaignSlug: String? = null,
    onCampaignRequestConsumed: () -> Unit = {},
) {
    ProverbRoot(
        onHaptic = onHaptic,
        onNavigateBack = onNavigateBack,
        campaignSlug = campaignSlug,
        onCampaignRequestConsumed = onCampaignRequestConsumed,
        modifier = modifier,
    )
}
