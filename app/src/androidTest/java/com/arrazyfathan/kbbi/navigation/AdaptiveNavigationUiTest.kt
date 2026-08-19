package com.arrazyfathan.kbbi.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class AdaptiveNavigationUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun compactWidth_placesDestinationsInBottomBar() {
        composeRule.setContent { NavigationFixture(width = 390.dp) }

        val home = composeRule.onNodeWithContentDescription("home").fetchSemanticsNode().boundsInRoot
        val words = composeRule.onNodeWithContentDescription("words").fetchSemanticsNode().boundsInRoot

        assertTrue(abs(home.center.y - words.center.y) < 1f)
        assertTrue(abs(home.center.x - words.center.x) > 1f)
    }

    @Test
    fun mediumWidth_placesDestinationsInNavigationRail() {
        composeRule.setContent { NavigationFixture(width = 700.dp) }

        val home = composeRule.onNodeWithContentDescription("home").fetchSemanticsNode().boundsInRoot
        val words = composeRule.onNodeWithContentDescription("words").fetchSemanticsNode().boundsInRoot

        assertTrue(abs(home.center.x - words.center.x) < 1f)
        assertTrue(abs(home.center.y - words.center.y) > 1f)
    }
}

@Composable
private fun NavigationFixture(width: Dp) {
    BoxWithConstraints(modifier = Modifier.requiredSize(width, 800.dp)) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                listOf("home", "words", "bookmarks").forEachIndexed { index, label ->
                    item(
                        selected = index == 0,
                        onClick = {},
                        icon = {
                            Box(
                                modifier =
                                    Modifier
                                        .size(24.dp)
                                        .semantics { contentDescription = label },
                            )
                        },
                    )
                }
            },
            layoutType = navigationSuiteTypeForWidth(maxWidth),
        ) {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}
