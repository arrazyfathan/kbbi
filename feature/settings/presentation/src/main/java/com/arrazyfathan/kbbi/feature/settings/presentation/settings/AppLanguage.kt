package com.arrazyfathan.kbbi.feature.settings.presentation.settings

enum class AppLanguage(
    val languageTag: String,
) {
    INDONESIAN("id"),
    ENGLISH("en"),
    ;

    companion object {
        fun fromLanguageTag(languageTag: String): AppLanguage? =
            when (languageTag.substringBefore('-').lowercase()) {
                "id", "in" -> INDONESIAN
                "en" -> ENGLISH
                else -> null
            }
    }
}

internal fun resolveAppLanguage(
    applicationLanguageTags: List<String>,
    systemLanguageTags: List<String>,
): AppLanguage {
    val languageTags = applicationLanguageTags.ifEmpty { systemLanguageTags }
    return languageTags.firstNotNullOfOrNull(AppLanguage::fromLanguageTag) ?: AppLanguage.ENGLISH
}
