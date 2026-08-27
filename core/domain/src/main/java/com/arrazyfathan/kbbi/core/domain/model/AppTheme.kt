package com.arrazyfathan.kbbi.core.domain.model

enum class AppTheme(
    val storageKey: String,
) {
    ROYAL_OCEAN("royal_ocean"),
    GOLDEN_SUNSET("golden_sunset"),
    GOLDEN_CORAL_ENERGY("golden_coral_energy"),
    DEEP_FOREST_ENERGY("deep_forest_energy"),
    ;

    companion object {
        fun fromStorageKey(key: String?): AppTheme =
            entries.firstOrNull { it.storageKey == key } ?: ROYAL_OCEAN
    }
}
