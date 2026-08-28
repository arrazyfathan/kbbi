package com.arrazyfathan.kbbi.feature.settings.presentation.settings

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import com.arrazyfathan.kbbi.core.domain.model.AppTheme
import com.arrazyfathan.kbbi.feature.settings.domain.model.AppIcon
import org.junit.Rule
import org.junit.Test
import java.util.Locale

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
    fun appearancePicker_showsThemesAndDispatchesSelection() {
        var selectedTheme: AppTheme? = null
        composeTestRule.setContent {
            KBBITheme {
                SettingsScreen(
                    state = SettingsState(selectedTheme = AppTheme.ROYAL_OCEAN),
                    onNavigateBack = {},
                    onAction = { action ->
                        if (action is SettingsAction.OnThemeSelected) selectedTheme = action.theme
                    },
                )
            }
        }

        listOf(
            "Royal Ocean",
            "Golden Sunset",
            "Golden Coral Energy",
            "Deep Forest Energy",
        ).forEach { name ->
            composeTestRule.onNodeWithText(name).performScrollTo().assertIsDisplayed()
        }
        composeTestRule.onNodeWithText("Royal Ocean").assertIsSelected()
        composeTestRule.onNodeWithText("Deep Forest Energy").performClick()
        composeTestRule.runOnIdle {
            assert(selectedTheme == AppTheme.DEEP_FOREST_ENERGY)
        }
    }

    @Test
    fun appIconPicker_showsAllOptionsAndCurrentSelection() {
        composeTestRule.setContent {
            KBBITheme {
                SettingsScreen(
                    state = SettingsState(selectedAppIcon = AppIcon.NEON_VIOLET),
                    onNavigateBack = {},
                    onAction = {},
                )
            }
        }

        AppIcon.entries.forEach { icon ->
            composeTestRule
                .onNodeWithTag("$APP_ICON_OPTION_TEST_TAG_PREFIX${icon.identifier}")
                .performScrollTo()
                .assertIsDisplayed()
        }
        composeTestRule
            .onNodeWithTag("${APP_ICON_OPTION_TEST_TAG_PREFIX}neon_violet")
            .performScrollTo()
            .assertIsSelected()
        composeTestRule
            .onNodeWithTag(
                testTag = "${APP_ICON_SELECTED_INDICATOR_TEST_TAG_PREFIX}neon_violet",
                useUnmergedTree = true,
            )
            .assertIsDisplayed()
    }

    @Test
    fun appIconPicker_dispatchesSelectionAndDescribesPreview() {
        var selectedIcon: AppIcon? = null
        composeTestRule.setContent {
            KBBITheme {
                SettingsScreen(
                    state = SettingsState(selectedAppIcon = AppIcon.DEFAULT),
                    onNavigateBack = {},
                    onAction = { action ->
                        if (action is SettingsAction.OnAppIconSelected) selectedIcon = action.icon
                    },
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Golden Sunset app icon preview")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("${APP_ICON_OPTION_TEST_TAG_PREFIX}golden_sunset")
            .performClick()
        composeTestRule.runOnIdle {
            assert(selectedIcon == AppIcon.GOLDEN_SUNSET)
        }
    }

    @Test
    fun appIconPicker_remainsUsableInIndonesianWithMultipleRows() {
        composeTestRule.setContent {
            val baseContext = LocalContext.current
            val baseConfiguration = LocalConfiguration.current
            val configuration =
                Configuration(baseConfiguration).apply {
                    setLocale(Locale.forLanguageTag("id-ID"))
                }
            val localizedContext = baseContext.createConfigurationContext(configuration)
            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides configuration,
            ) {
                KBBITheme {
                    SettingsScreen(
                        state = SettingsState(),
                        onNavigateBack = {},
                        onAction = {},
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Ikon aplikasi").performScrollTo().assertIsDisplayed()
        composeTestRule
            .onNodeWithContentDescription("Pratinjau ikon aplikasi Bawaan")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("${APP_ICON_OPTION_TEST_TAG_PREFIX}blaze_orange")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun appIconChangeDialog_informsUserAndDispatchesConfirmation() {
        var confirmed = false
        composeTestRule.setContent {
            KBBITheme {
                SettingsScreen(
                    state = SettingsState(pendingAppIcon = AppIcon.GOLDEN_SUNSET),
                    onNavigateBack = {},
                    onAction = { action ->
                        if (action == SettingsAction.OnAppIconChangeConfirmed) {
                            confirmed = true
                        }
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Change app icon?").assertIsDisplayed()
        composeTestRule
            .onNodeWithText(
                "The app may need to be closed and reopened to apply the new launcher icon. " +
                    "Do you want to continue?",
            ).assertIsDisplayed()
        composeTestRule.onNodeWithText("Yes, change icon").performClick()
        composeTestRule.runOnIdle { assert(confirmed) }
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
