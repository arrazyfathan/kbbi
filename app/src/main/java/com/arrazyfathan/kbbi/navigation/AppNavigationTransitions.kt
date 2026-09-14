package com.arrazyfathan.kbbi.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey

private const val TOP_LEVEL_TRANSITION_DURATION_MILLIS = 250
private const val TOP_LEVEL_INCOMING_FADE_DURATION_MILLIS = 160
private const val TOP_LEVEL_INCOMING_FADE_DELAY_MILLIS = 90
private const val TOP_LEVEL_OUTGOING_FADE_DURATION_MILLIS = 90
private const val TOP_LEVEL_OFFSET_DIVISOR = 20
private const val HIERARCHICAL_TRANSITION_DURATION_MILLIS = 300
private const val HIERARCHICAL_INCOMING_FADE_DURATION_MILLIS = 210
private const val HIERARCHICAL_INCOMING_FADE_DELAY_MILLIS = 50
private const val HIERARCHICAL_OUTGOING_FADE_DURATION_MILLIS = 100
private const val HIERARCHICAL_INCOMING_OFFSET_DIVISOR = 10
private const val HIERARCHICAL_OUTGOING_OFFSET_DIVISOR = 20

internal enum class AppNavigationMotion {
    TopLevelForward,
    TopLevelBackward,
    HierarchicalPush,
    HierarchicalPop,
}

private enum class AppNavigationDestination(
    val contentKeyPrefix: String,
    val topLevelIndex: Int? = null,
) {
    Home("home", topLevelIndex = 0),
    WordList("word-list", topLevelIndex = 1),
    Bookmarks("bookmarks", topLevelIndex = 2),
    Proverb("proverb"),
    Settings("settings"),
    Detail("detail:"),
    OpenSourceLicenses("open-source-licenses"),
    PrivacyPolicy("privacy-policy"),
    TermsConditions("terms-conditions"),
}

internal fun NavKey.toAppNavigationContentKey(): String =
    when (this) {
        Screen.Home -> AppNavigationDestination.Home.contentKeyPrefix
        Screen.WordList -> AppNavigationDestination.WordList.contentKeyPrefix
        Screen.Bookmarks -> AppNavigationDestination.Bookmarks.contentKeyPrefix
        Screen.Proverb -> AppNavigationDestination.Proverb.contentKeyPrefix
        Screen.Settings -> AppNavigationDestination.Settings.contentKeyPrefix
        is DetailNavRoute -> AppNavigationDestination.Detail.contentKeyPrefix + dataJson
        OpenSourceLicensesRoute -> AppNavigationDestination.OpenSourceLicenses.contentKeyPrefix
        PrivacyPolicyRoute -> AppNavigationDestination.PrivacyPolicy.contentKeyPrefix
        TermsConditionsRoute -> AppNavigationDestination.TermsConditions.contentKeyPrefix
        else -> "unknown:$this"
    }

internal fun resolveAppNavigationMotion(
    initialContentKey: Any?,
    targetContentKey: Any?,
    isPop: Boolean,
): AppNavigationMotion {
    val initialDestination = initialContentKey.toAppNavigationDestination()
    val targetDestination = targetContentKey.toAppNavigationDestination()
    val initialTopLevelIndex = initialDestination?.topLevelIndex
    val targetTopLevelIndex = targetDestination?.topLevelIndex

    if (initialTopLevelIndex != null && targetTopLevelIndex != null) {
        return if (targetTopLevelIndex > initialTopLevelIndex) {
            AppNavigationMotion.TopLevelForward
        } else {
            AppNavigationMotion.TopLevelBackward
        }
    }

    return if (isPop) AppNavigationMotion.HierarchicalPop else AppNavigationMotion.HierarchicalPush
}

private fun Any?.toAppNavigationDestination(): AppNavigationDestination? {
    val contentKey = this as? String ?: return null
    return AppNavigationDestination.entries.firstOrNull { destination ->
        if (destination == AppNavigationDestination.Detail) {
            contentKey.startsWith(destination.contentKeyPrefix)
        } else {
            contentKey == destination.contentKeyPrefix
        }
    }
}

internal fun appNavigationTransition(motion: AppNavigationMotion): ContentTransform =
    when (motion) {
        AppNavigationMotion.TopLevelForward -> topLevelNavigationTransition(direction = 1)
        AppNavigationMotion.TopLevelBackward -> topLevelNavigationTransition(direction = -1)
        AppNavigationMotion.HierarchicalPush -> hierarchicalNavigationTransition(direction = 1)
        AppNavigationMotion.HierarchicalPop -> hierarchicalNavigationTransition(direction = -1)
    }

private fun topLevelNavigationTransition(direction: Int): ContentTransform =
    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = TOP_LEVEL_TRANSITION_DURATION_MILLIS),
            initialOffsetX = { fullWidth -> direction * fullWidth / TOP_LEVEL_OFFSET_DIVISOR },
        ) +
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = TOP_LEVEL_INCOMING_FADE_DURATION_MILLIS,
                        delayMillis = TOP_LEVEL_INCOMING_FADE_DELAY_MILLIS,
                    ),
            )
    ) togetherWith
        (
            slideOutHorizontally(
                animationSpec = tween(durationMillis = TOP_LEVEL_TRANSITION_DURATION_MILLIS),
                targetOffsetX = { fullWidth -> -direction * fullWidth / TOP_LEVEL_OFFSET_DIVISOR },
            ) +
                fadeOut(
                    animationSpec = tween(durationMillis = TOP_LEVEL_OUTGOING_FADE_DURATION_MILLIS),
                )
        )

private fun hierarchicalNavigationTransition(direction: Int): ContentTransform =
    (
        slideInHorizontally(
            animationSpec = tween(durationMillis = HIERARCHICAL_TRANSITION_DURATION_MILLIS),
            initialOffsetX = { fullWidth -> direction * fullWidth / HIERARCHICAL_INCOMING_OFFSET_DIVISOR },
        ) +
            fadeIn(
                animationSpec =
                    tween(
                        durationMillis = HIERARCHICAL_INCOMING_FADE_DURATION_MILLIS,
                        delayMillis = HIERARCHICAL_INCOMING_FADE_DELAY_MILLIS,
                    ),
            )
    ) togetherWith
        (
            slideOutHorizontally(
                animationSpec = tween(durationMillis = HIERARCHICAL_TRANSITION_DURATION_MILLIS),
                targetOffsetX = { fullWidth -> -direction * fullWidth / HIERARCHICAL_OUTGOING_OFFSET_DIVISOR },
            ) +
                fadeOut(
                    animationSpec = tween(durationMillis = HIERARCHICAL_OUTGOING_FADE_DURATION_MILLIS),
                )
        )
