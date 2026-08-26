package com.arrazyfathan.kbbi.core.presentation.designsystem

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

enum class KBBIHapticType {
    Confirm,
    Reject,
    ToggleOn,
    ToggleOff,
    LongPress,
    Selection,
    ContextClick,
    GestureThreshold,
}

fun HapticFeedback.perform(type: KBBIHapticType) {
    val platformType =
        when (type) {
            KBBIHapticType.Confirm -> HapticFeedbackType.Confirm
            KBBIHapticType.Reject -> HapticFeedbackType.Reject
            KBBIHapticType.ToggleOn -> HapticFeedbackType.ToggleOn
            KBBIHapticType.ToggleOff -> HapticFeedbackType.ToggleOff
            KBBIHapticType.LongPress -> HapticFeedbackType.LongPress
            KBBIHapticType.Selection -> HapticFeedbackType.SegmentTick
            KBBIHapticType.ContextClick -> HapticFeedbackType.ContextClick
            KBBIHapticType.GestureThreshold -> HapticFeedbackType.GestureThresholdActivate
        }
    performHapticFeedback(platformType)
}
