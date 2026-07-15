package com.arrazyfathan.kbbi.core.appupdate.domain

data class AppUpdateConfig(
    val currentVersion: String,
    val isUpdateCheckEnabled: Boolean,
)
