package com.arrazyfathan.kbbi.feature.settings.presentation.legal

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import org.junit.Rule
import org.junit.Test

class LegalDocumentScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun privacyPolicy_showsLocalizedDocumentContent() {
        composeTestRule.setContent {
            KBBITheme { PrivacyPolicyScreen(onNavigateBack = {}) }
        }

        composeTestRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Effective August 21, 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("10. Contact").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun termsConditions_showsDocumentContent() {
        composeTestRule.setContent {
            KBBITheme { TermsConditionsScreen(onNavigateBack = {}) }
        }

        composeTestRule.onNodeWithText("Terms & Conditions").assertIsDisplayed()
        composeTestRule.onNodeWithText("1. Acceptance").assertIsDisplayed()
        composeTestRule.onNodeWithText("9. Contact").performScrollTo().assertIsDisplayed()
    }
}
