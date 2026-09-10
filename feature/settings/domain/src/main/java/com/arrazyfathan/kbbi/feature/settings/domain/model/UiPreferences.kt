package com.arrazyfathan.kbbi.feature.settings.domain.model

import com.arrazyfathan.kbbi.core.domain.model.AppTheme

data class UiPreferences(
    val hapticsEnabled: Boolean = true,
    val theme: AppTheme = AppTheme.ROYAL_OCEAN,
    val bookmarkLayout: BookmarkLayout = BookmarkLayout.GRID,
)

enum class BookmarkLayout(
    val storageKey: String,
) {
    GRID("grid"),
    LIST("list"),
    ;

    companion object {
        fun fromStorageKey(key: String?): BookmarkLayout = entries.firstOrNull { it.storageKey == key } ?: GRID
    }
}
