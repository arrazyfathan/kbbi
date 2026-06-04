package com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark.BookmarksScreen
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel

@Composable
fun BookmarkRoute(
    onNavigateToDetail: (ListWordModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    BookmarksScreen(
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
    )
}
