package com.arrazyfathan.kbbi.navigation

enum class AppShortcutRequest(
    val action: String,
) {
    Search("com.arrazyfathan.kbbi.action.SHORTCUT_SEARCH"),
    Bookmarks("com.arrazyfathan.kbbi.action.SHORTCUT_BOOKMARKS"),
    Proverbs("com.arrazyfathan.kbbi.action.SHORTCUT_PROVERBS"),
    RandomWord("com.arrazyfathan.kbbi.action.SHORTCUT_RANDOM_WORD"),
    ;

    companion object {
        fun fromAction(action: String?): AppShortcutRequest? =
            entries.firstOrNull { shortcut -> shortcut.action == action }
    }
}
