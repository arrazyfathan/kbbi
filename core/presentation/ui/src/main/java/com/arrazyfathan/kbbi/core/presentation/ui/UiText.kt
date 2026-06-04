package com.arrazyfathan.kbbi.core.presentation.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed interface UiText {
    data class DynamicString(
        val value: String,
    ) : UiText

    data class StringResource(
        @param:StringRes val id: Int,
        val args: Array<Any> = emptyArray(),
    ) : UiText

    @Composable
    fun asString(): String =
        when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(id = id, formatArgs = args)
        }

    fun asString(context: Context): String =
        when (this) {
            is DynamicString -> value
            is StringResource -> context.getString(id, *args)
        }
}
