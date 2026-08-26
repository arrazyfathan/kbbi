package com.arrazyfathan.kbbi.widgets

import android.content.Context
import android.content.Intent
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.intent.toKbbiSearchQuery

internal const val ACTION_WIDGET_QUICK_SEARCH = "com.arrazyfathan.kbbi.action.WIDGET_QUICK_SEARCH"
internal const val ACTION_WIDGET_WORD_OF_DAY = "com.arrazyfathan.kbbi.action.WIDGET_WORD_OF_DAY"
internal const val ACTION_WIDGET_SAVED_WORD = "com.arrazyfathan.kbbi.action.WIDGET_SAVED_WORD"
internal const val EXTRA_WIDGET_WORD = "com.arrazyfathan.kbbi.extra.WIDGET_WORD"

internal sealed interface WidgetLaunchRequest {
    data object QuickSearch : WidgetLaunchRequest

    data class WordOfDay(
        val word: String?,
    ) : WidgetLaunchRequest

    data class SavedWord(
        val word: String?,
    ) : WidgetLaunchRequest

    companion object {
        fun parse(
            action: String?,
            word: String?,
        ): WidgetLaunchRequest? =
            when (action) {
                ACTION_WIDGET_QUICK_SEARCH -> QuickSearch
                ACTION_WIDGET_WORD_OF_DAY -> WordOfDay(word?.toKbbiSearchQuery())
                ACTION_WIDGET_SAVED_WORD -> SavedWord(word?.toKbbiSearchQuery())
                else -> null
            }
    }
}

internal fun Intent.extractWidgetLaunchRequest(): WidgetLaunchRequest? =
    WidgetLaunchRequest.parse(action, getStringExtra(EXTRA_WIDGET_WORD))

internal fun widgetLaunchIntent(
    context: Context,
    action: String,
    word: String? = null,
): Intent =
    Intent(context, MainActivity::class.java)
        .setAction(action)
        .apply { word?.let { putExtra(EXTRA_WIDGET_WORD, it) } }
        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
