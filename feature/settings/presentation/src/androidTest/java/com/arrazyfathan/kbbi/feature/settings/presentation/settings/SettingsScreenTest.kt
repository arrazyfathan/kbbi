package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
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
    fun languagePicker_dispatchesOnceAfterSheetIsHidden() {
        composeTestRule.mainClock.autoAdvance = false
        var state by mutableStateOf(
            SettingsState(
                selectedLanguage = AppLanguage.ENGLISH,
                isLanguagePickerVisible = true,
            ),
        )
        val selections = mutableListOf<AppLanguage>()
        composeTestRule.setContent {
            KBBITheme {
                SettingsScreen(
                    state = state,
                    onNavigateBack = {},
                    onAction = { action ->
                        if (action is SettingsAction.OnLanguageSelected) {
                            selections += action.language
                            state = state.copy(isLanguagePickerVisible = false)
                        }
                    },
                )
            }
        }
        composeTestRule.mainClock.advanceTimeBy(1_000)

        composeTestRule.onNodeWithText("Bahasa Indonesia").performClick()

        composeTestRule.runOnIdle { assert(selections.isEmpty()) }
        composeTestRule.onNodeWithText("Bahasa Indonesia").assertIsNotEnabled()
        composeTestRule.mainClock.advanceTimeBy(1_000)
        composeTestRule.runOnIdle {
            assert(selections == listOf(AppLanguage.INDONESIAN))
        }
    }

    @Test
    fun languageTransitionOverlay_blocksInputUntilRemoved() {
        var isOverlayVisible by mutableStateOf(true)
        var clickCount = 0
        composeTestRule.setContent {
            KBBITheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Underlying action",
                        modifier = Modifier.clickable { clickCount++ },
                    )
                    if (isOverlayVisible) {
                        LanguageTransitionOverlay(
                            alpha = { 1f },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        composeTestRule.onNodeWithTag(LANGUAGE_TRANSITION_OVERLAY_TEST_TAG).assertIsDisplayed()
        composeTestRule.onNodeWithText("Underlying action").performTouchInput { click() }
        composeTestRule.runOnIdle { assert(clickCount == 0) }

        composeTestRule.runOnIdle { isOverlayVisible = false }
        composeTestRule.onNodeWithText("Underlying action").performTouchInput { click() }
        composeTestRule.runOnIdle { assert(clickCount == 1) }
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
