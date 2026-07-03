package com.arrazyfathan.kbbi.feature.home.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.presentation.home.HomeScreen

@Composable
fun HomeRoute(
    onNavigateToDetail: (ListWordModel) -> Unit,
    onNavigateToProverb: () -> Unit,
    modifier: Modifier = Modifier,
    externalSearchQuery: String? = null,
    externalSearchRequestKey: Long = 0L,
    onExternalSearchConsumed: () -> Unit = {},
) {
    HomeScreen(
        externalSearchQuery = externalSearchQuery,
        externalSearchRequestKey = externalSearchRequestKey,
        onExternalSearchConsumed = onExternalSearchConsumed,
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToProverb = onNavigateToProverb,
        modifier = modifier,
    )
}
