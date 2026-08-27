package com.arrazyfathan.kbbi.feature.settings.domain.model

import com.arrazyfathan.kbbi.core.domain.model.AppTheme

data class UiPreferences(
    val hapticsEnabled: Boolean = true,
    val theme: AppTheme = AppTheme.ROYAL_OCEAN,
)
