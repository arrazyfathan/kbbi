package com.arrazyfathan.kbbi.feature.settings.domain.service

interface NotificationPermissionGateway {
    val isRuntimePermissionRequired: Boolean

    fun isGranted(): Boolean
}
