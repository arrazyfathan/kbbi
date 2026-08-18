package com.arrazyfathan.kbbi.core.presentation.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.presentation.designsystem.KBBITheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KBBITimePickerTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val picker = KBBITimePickerRobot(composeTestRule)

    @Test
    fun initialTime_isCenteredAndSelected() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        picker
            .setWheel(hour = 8, minute = 30)
            .assertSelected(
                hourDescription = context.getString(R.string.time_picker_hour_option, 8),
                minuteDescription = context.getString(R.string.time_picker_minute_option, 30),
            )
    }

    @Test
    fun done_confirmsScrolledBoundaryTime() {
        var confirmedTime: Pair<Int, Int>? = null
        picker
            .setSheet(onConfirm = { hour, minute -> confirmedTime = hour to minute })
            .selectTime(hour = 23, minute = 59)
            .pressDone()

        assertEquals(23 to 59, confirmedTime)
    }

    @Test
    fun cancel_dismissesWithoutConfirmation() {
        var confirmedTime: Pair<Int, Int>? = null
        var dismissCount = 0
        picker
            .setSheet(
                onConfirm = { hour, minute -> confirmedTime = hour to minute },
                onDismiss = { dismissCount++ },
            ).selectTime(hour = 12, minute = 45)
            .pressCancel()

        assertNull(confirmedTime)
        assertEquals(1, dismissCount)
    }

    @Test
    fun systemBack_dismissesWithoutConfirmation() {
        var confirmedTime: Pair<Int, Int>? = null
        var dismissCount = 0
        picker.setSheet(
            onConfirm = { hour, minute -> confirmedTime = hour to minute },
            onDismiss = { dismissCount++ },
        )

        Espresso.pressBack()
        composeTestRule.waitForIdle()

        assertNull(confirmedTime)
        assertEquals(1, dismissCount)
    }
}

private class KBBITimePickerRobot(
    private val composeTestRule: ComposeContentTestRule,
) {
    fun setWheel(
        hour: Int,
        minute: Int,
    ) = apply {
        composeTestRule.setContent {
            KBBITheme {
                KBBIWheelTimePicker(
                    hour = hour,
                    minute = minute,
                    onTimeChange = { _, _ -> },
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    fun setSheet(
        onConfirm: (Int, Int) -> Unit,
        onDismiss: () -> Unit = {},
    ) = apply {
        composeTestRule.setContent {
            KBBITheme {
                KBBITimePickerBottomSheet(
                    initialHour = 0,
                    initialMinute = 0,
                    onConfirm = onConfirm,
                    onDismissRequest = onDismiss,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    fun assertSelected(
        hourDescription: String,
        minuteDescription: String,
    ) = apply {
        composeTestRule
            .onNode(hasContentDescription(hourDescription) and isSelected())
            .assertIsDisplayed()
        composeTestRule
            .onNode(hasContentDescription(minuteDescription) and isSelected())
            .assertIsDisplayed()
    }

    fun selectTime(
        hour: Int,
        minute: Int,
    ) = apply {
        composeTestRule.onNodeWithTag(HOUR_WHEEL_TEST_TAG).performScrollToIndex(hour)
        composeTestRule.onNodeWithTag(MINUTE_WHEEL_TEST_TAG).performScrollToIndex(minute)
        composeTestRule.waitForIdle()
    }

    fun pressDone() = apply {
        composeTestRule.onNodeWithTag(DONE_TIME_PICKER_TEST_TAG).performClick()
        composeTestRule.waitForIdle()
    }

    fun pressCancel() = apply {
        composeTestRule.onNodeWithTag(CANCEL_TIME_PICKER_TEST_TAG).performClick()
        composeTestRule.waitForIdle()
    }
}
