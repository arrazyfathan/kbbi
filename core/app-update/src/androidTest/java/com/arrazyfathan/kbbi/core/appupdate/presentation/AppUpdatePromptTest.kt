package com.arrazyfathan.kbbi.core.appupdate.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateDownloadState
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateRequirement
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppUpdatePromptTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun requiredUpdate_removesLaterAndOffersExit() {
        composeTestRule.setContent {
            KBBITheme {
                AppUpdatePromptContent(
                    update = requiredUpdate(),
                    currentVersion = "5.24",
                    downloadState = AppUpdateDownloadState.Idle,
                    isRequired = true,
                    onDownload = {},
                    onOpenRelease = {},
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Update required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Exit app").assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithText("Later").fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun requiredUpdate_exitInvokesCallback() {
        var exited = false
        composeTestRule.setContent {
            KBBITheme {
                AppUpdatePromptContent(
                    update = requiredUpdate(),
                    currentVersion = "5.24",
                    downloadState = AppUpdateDownloadState.Idle,
                    isRequired = true,
                    onDownload = {},
                    onOpenRelease = {},
                    onDismiss = { exited = true },
                )
            }
        }

        composeTestRule.onNodeWithText("Exit app").performClick()

        assertTrue(exited)
    }

    private fun requiredUpdate() =
        AppUpdate(
            latestVersion = "6.0",
            releaseUrl = "https://example.com/release",
            downloadUrl = "https://example.com/kbbi.apk",
            releaseNotes = "Important compatibility update",
            requirement = AppUpdateRequirement.REQUIRED,
        )
}
