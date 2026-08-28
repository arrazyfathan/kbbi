package com.arrazyfathan.kbbi.feature.settings.domain.service

import com.arrazyfathan.kbbi.feature.settings.domain.model.AppIcon

interface AppIconManager {
    fun currentIcon(): AppIcon

    suspend fun changeIcon(icon: AppIcon): AppIconChangeResult
}

enum class AppIconChangeResult {
    SUCCESS,
    FAILURE,
}
