package com.arrazyfathan.kbbi.core.appupdate.domain

data class AppUpdate(
    val latestVersion: String,
    val releaseUrl: String,
    val downloadUrl: String?,
    val releaseNotes: String?,
)
