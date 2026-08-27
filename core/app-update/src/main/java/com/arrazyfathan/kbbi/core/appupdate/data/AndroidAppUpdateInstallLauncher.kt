package com.arrazyfathan.kbbi.core.appupdate.data

import android.content.Context
import android.content.Intent
import com.arrazyfathan.kbbi.core.appupdate.domain.AppUpdateInstallLauncher

class AndroidAppUpdateInstallLauncher(
    context: Context,
) : AppUpdateInstallLauncher {
    private val appContext = context.applicationContext

    override fun launch(downloadId: Long) {
        appContext.startActivity(
            AppUpdateInstallActivity
                .intent(appContext, downloadId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
