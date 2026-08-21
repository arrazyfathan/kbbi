package com.arrazyfathan.kbbi.feature.detail.presentation.navigation

import androidx.compose.runtime.Composable
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailScreen
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel

@Composable
fun DetailRoute(
    listWordModel: ListWordModel,
    onHaptic: (KBBIHapticType) -> Unit,
) {
    DetailScreen(
        listWordModel = listWordModel,
        onHaptic = onHaptic,
    )
}
