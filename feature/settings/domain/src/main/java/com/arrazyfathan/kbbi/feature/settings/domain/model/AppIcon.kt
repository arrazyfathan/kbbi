package com.arrazyfathan.kbbi.feature.settings.domain.model

enum class AppIcon(
    val identifier: String,
) {
    DEFAULT("default"),
    ROYAL_OCEAN("royal_ocean"),
    GOLDEN_SUNSET("golden_sunset"),
    GOLDEN_CORAL_ENERGY("golden_coral_energy"),
    DEEP_FOREST_ENERGY("deep_forest_energy"),
    NEON_VIOLET("neon_violet"),
    BLAZE_ORANGE("blaze_orange"),
    ;

    companion object {
        fun fromIdentifier(identifier: String?): AppIcon =
            entries.firstOrNull { it.identifier == identifier } ?: DEFAULT
    }
}
