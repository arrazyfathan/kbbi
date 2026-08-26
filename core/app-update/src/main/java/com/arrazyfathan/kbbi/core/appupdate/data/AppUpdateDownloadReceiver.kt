package com.arrazyfathan.kbbi.core.appupdate.data

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.koin.core.context.GlobalContext

class AppUpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId < 0L) return
        val manager =
            runCatching { GlobalContext.get().get<AndroidAppUpdateDownloadManager>() }
                .getOrNull() ?: return
        if (manager.refreshCompletedDownload(downloadId)) {
            AppUpdateNotificationPublisher.publishReady(context, downloadId)
        }
    }
}
