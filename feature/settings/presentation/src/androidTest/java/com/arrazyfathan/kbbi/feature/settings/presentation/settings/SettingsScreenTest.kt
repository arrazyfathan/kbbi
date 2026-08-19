package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun languagePicker_showsBothLanguages() {
        composeTestRule.setContent {
            KBBITheme {
                SettingsScreen(
                    state = SettingsState(isLanguagePickerVisible = true),
                    onNavigateBack = {},
                    onAction = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Choose language").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bahasa Indonesia").assertIsDisplayed()
        composeTestRule.onNodeWithText("English").assertIsDisplayed()
    }
}
