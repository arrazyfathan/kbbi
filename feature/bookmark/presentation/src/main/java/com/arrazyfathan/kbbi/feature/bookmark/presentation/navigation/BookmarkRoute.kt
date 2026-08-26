package com.arrazyfathan.kbbi.feature.bookmark.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBIHapticType
import com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark.BookmarksScreen
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel

@Composable
fun BookmarkRoute(
    onHaptic: (KBBIHapticType) -> Unit,
    onNavigateToDetail: (ListWordModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    BookmarksScreen(
        onHaptic = onHaptic,
        onNavigateToDetail = onNavigateToDetail,
        modifier = modifier,
    )
}
