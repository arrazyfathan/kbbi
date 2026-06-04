package com.arrazyfathan.kbbi.feature.detail.presentation.navigation

import androidx.compose.runtime.Composable
import com.arrazyfathan.kbbi.feature.detail.presentation.detail.DetailScreen
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel

@Composable
fun DetailRoute(listWordModel: ListWordModel) {
    DetailScreen(listWordModel = listWordModel)
}
