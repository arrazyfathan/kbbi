package com.arrazyfathan.kbbi.feature.detail.presentation.detail

internal fun translationProviderDisplayName(provider: String): String =
    when (val normalizedProvider = provider.trim()) {
        "" -> ""
        else ->
            when (normalizedProvider.lowercase()) {
                "google" -> "Google"
                "lara" -> "Lara"
                else -> normalizedProvider.replaceFirstChar { it.uppercase() }
            }
    }

internal fun shouldShowTranslationProvider(
    isTranslationEnabled: Boolean,
    provider: String?,
): Boolean = isTranslationEnabled && !provider.isNullOrBlank()
