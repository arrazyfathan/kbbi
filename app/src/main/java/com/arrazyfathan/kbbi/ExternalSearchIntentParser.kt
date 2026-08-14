package com.arrazyfathan.kbbi

import android.content.Intent
import java.util.Locale

private const val KBBI_DEEP_LINK_SCHEME = "kbbi"
private const val WORD_DEEP_LINK_HOST = "word"
private const val PROVERB_DEEP_LINK_HOST = "proverb"
private const val BOOKMARKS_DEEP_LINK_HOST = "bookmarks"

sealed interface NotificationLaunchRequest {
    data class Proverb(val slug: String?) : NotificationLaunchRequest
    data object Bookmarks : NotificationLaunchRequest
}

internal fun Intent.extractExternalSearchQuery(): String? {
    val rawText =
        when (action) {
            Intent.ACTION_PROCESS_TEXT -> getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
            Intent.ACTION_SEND -> {
                if (type?.startsWith("text/plain") == true) {
                    getStringExtra(Intent.EXTRA_TEXT)
                } else {
                    null
                }
            }
            Intent.ACTION_VIEW ->
                data?.let { uri ->
                    extractWordDeepLinkQuery(
                        scheme = uri.scheme,
                        host = uri.host,
                        pathSegments = uri.pathSegments,
                    )
                }
            else -> null
        }

    return rawText?.toKbbiSearchQuery()
}

internal fun Intent.extractNotificationLaunchRequest(): NotificationLaunchRequest? {
    if (action != Intent.ACTION_VIEW || data?.scheme != KBBI_DEEP_LINK_SCHEME) return null
    return when (data?.host) {
        PROVERB_DEEP_LINK_HOST -> NotificationLaunchRequest.Proverb(data?.pathSegments?.firstOrNull())
        BOOKMARKS_DEEP_LINK_HOST -> NotificationLaunchRequest.Bookmarks
        else -> null
    }
}

internal fun extractWordDeepLinkQuery(
    scheme: String?,
    host: String?,
    pathSegments: List<String>,
): String? {
    if (scheme != KBBI_DEEP_LINK_SCHEME || host != WORD_DEEP_LINK_HOST) {
        return null
    }

    return pathSegments.firstOrNull()
}

internal fun String.toKbbiSearchQuery(): String? =
    trim()
        .split(Regex("\\s+"))
        .firstOrNull()
        ?.trim { !it.isLetterOrDigit() && it != '-' }
        ?.lowercase(Locale.ROOT)
        ?.takeIf { it.isNotBlank() }
