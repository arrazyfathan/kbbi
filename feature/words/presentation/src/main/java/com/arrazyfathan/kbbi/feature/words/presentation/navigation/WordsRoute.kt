package com.arrazyfathan.kbbi.feature.words.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.words.presentation.words.WordListScreen

@Composable
fun WordsRoute(
    onNavigateToDetail: (ListWordModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    WordListScreen(
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
    )
}
