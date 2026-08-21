package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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

    @Test
    fun settings_showsAboutAndDataSections() {
        composeTestRule.setContent {
            KBBITheme {
                SettingsScreen(
                    state = SettingsState(appVersion = "1.2.3"),
                    onNavigateBack = {},
                    onAction = {},
                )
            }
        }

        listOf(
            "About",
            "App update",
            "Version",
            "Privacy Policy",
            "Terms & Conditions",
            "Share app",
            "Report a bug",
            "Open source licenses",
            "Data",
            "Clear search history",
        ).forEach { text ->
            composeTestRule.onNodeWithText(text).performScrollTo().assertIsDisplayed()
        }
    }
    @Test
    fun legalRows_invokeNavigationCallbacks() {
        var privacyOpened = false
        var termsOpened = false
        composeTestRule.setContent {
            KBBITheme {
                SettingsScreen(
                    state = SettingsState(),
                    onNavigateBack = {},
                    onAction = {},
                    onOpenPrivacyPolicy = { privacyOpened = true },
                    onOpenTermsConditions = { termsOpened = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Privacy Policy").performScrollTo().performClick()
        composeTestRule.onNodeWithText("Terms & Conditions").performScrollTo().performClick()

        assert(privacyOpened)
        assert(termsOpened)
    }
}
