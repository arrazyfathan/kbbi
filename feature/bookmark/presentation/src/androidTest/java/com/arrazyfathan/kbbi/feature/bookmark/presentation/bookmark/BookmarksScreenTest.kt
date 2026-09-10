package com.arrazyfathan.kbbi.feature.bookmark.presentation.bookmark

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.feature.home.domain.model.ListWordModel
import com.arrazyfathan.kbbi.feature.home.domain.model.WordModel
import com.arrazyfathan.kbbi.feature.settings.domain.model.BookmarkLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class BookmarksScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun layoutToggle_selectsAndRendersTheChosenLayout() {
        var state by mutableStateOf(BookmarksState(bookmarks = listOf(sampleBookmark())))
        composeTestRule.setContent {
            KBBITheme {
                BookmarksScreenContent(
                    state = state,
                    onNavigateToDetail = {},
                    onAction = { action ->
                        if (action is BookmarksAction.OnLayoutSelected) {
                            state = state.copy(bookmarkLayout = action.layout)
                        }
                    },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Grid view").assertIsSelected()
        composeTestRule.onNodeWithTag(BOOKMARK_GRID_TEST_TAG).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription("List view").performClick()

        composeTestRule.onNodeWithContentDescription("List view").assertIsSelected()
        composeTestRule.onNodeWithTag(BOOKMARK_LIST_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun listItem_tapNavigatesAndSwipeRequiresConfirmation() {
        val actions = mutableListOf<BookmarksAction>()
        var navigatedWord: String? = null
        composeTestRule.setContent {
            KBBITheme {
                BookmarksScreenContent(
                    state =
                        BookmarksState(
                            bookmarks = listOf(sampleBookmark()),
                            bookmarkLayout = BookmarkLayout.LIST,
                        ),
                    onNavigateToDetail = { navigatedWord = it.word },
                    onAction = actions::add,
                )
            }
        }

        val row = composeTestRule.onNodeWithTag("${BOOKMARK_LIST_ITEM_TEST_TAG_PREFIX}belajar")
        row.performTouchInput { click() }
        composeTestRule.runOnIdle { assertEquals("belajar", navigatedWord) }

        row.performTouchInput { swipeLeft() }
        composeTestRule.onNodeWithText("Delete word?").assertIsDisplayed()
        composeTestRule.runOnIdle { assertTrue(actions.none { it is BookmarksAction.OnDeleteConfirmed }) }

        composeTestRule.onNodeWithText("Cancel").performClick()
        composeTestRule.runOnIdle { assertTrue(actions.none { it is BookmarksAction.OnDeleteConfirmed }) }

        row.performTouchInput { swipeLeft() }
        composeTestRule.onNodeWithText("Delete").performClick()
        composeTestRule.runOnIdle {
            assertEquals(
                listOf(BookmarksAction.OnDeleteConfirmed("belajar")),
                actions.filterIsInstance<BookmarksAction.OnDeleteConfirmed>(),
            )
        }
    }

    @Test
    fun gridItem_longPressKeepsExistingDeleteConfirmation() {
        val actions = mutableListOf<BookmarksAction>()
        composeTestRule.setContent {
            KBBITheme {
                BookmarksScreenContent(
                    state = BookmarksState(bookmarks = listOf(sampleBookmark())),
                    onNavigateToDetail = {},
                    onAction = actions::add,
                )
            }
        }

        composeTestRule
            .onNodeWithTag("${BOOKMARK_GRID_ITEM_TEST_TAG_PREFIX}belajar")
            .performTouchInput { longClick() }
        composeTestRule.onNodeWithContentDescription("Delete").performClick()
        composeTestRule.onNodeWithText("Delete word?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete").performClick()

        composeTestRule.runOnIdle {
            assertEquals(
                listOf(BookmarksAction.OnDeleteConfirmed("belajar")),
                actions.filterIsInstance<BookmarksAction.OnDeleteConfirmed>(),
            )
        }
    }

    private fun sampleBookmark() =
        ListWordModel(
            word = "belajar",
            listWords = listOf(WordModel(entry = "belajar", meanings = emptyList())),
        )
}
