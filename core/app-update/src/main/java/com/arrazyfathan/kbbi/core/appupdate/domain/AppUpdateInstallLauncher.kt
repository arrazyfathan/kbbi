package com.arrazyfathan.kbbi.core.appupdate.domain

fun interface AppUpdateInstallLauncher {
    fun launch(downloadId: Long)
}
