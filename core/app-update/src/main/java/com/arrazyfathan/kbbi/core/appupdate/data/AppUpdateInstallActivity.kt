package com.arrazyfathan.kbbi.core.appupdate.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.arrazyfathan.kbbi.core.R
import com.arrazyfathan.kbbi.core.appupdate.domain.ApkValidationResult
import org.koin.core.context.GlobalContext
import java.io.File

class AppUpdateInstallActivity : Activity() {
    private val updateManager: AndroidAppUpdateDownloadManager by lazy {
        GlobalContext.get().get()
    }
    private var downloadId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId < 0L) {
            finish()
            return
        }
        continueInstallation()
    }

    @Deprecated("Used for the Android 8+ unknown-source settings result")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UNKNOWN_SOURCE_REQUEST_CODE) {
            if (canInstallPackages()) {
                launchPackageInstaller()
            } else {
                Toast.makeText(this, R.string.update_install_permission_denied, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun continueInstallation() {
        val stored = updateManager.storedDownload(downloadId)
        if (stored == null) {
            showInvalidDownload()
            return
        }
        val validation = ApkArchiveValidator(this).validate(File(stored.filePath))
        if (validation != ApkValidationResult.VALID) {
            updateManager.clearInvalidDownload(downloadId)
            val message =
                when (validation) {
                    ApkValidationResult.WRONG_PACKAGE -> R.string.update_install_wrong_package
                    ApkValidationResult.VERSION_NOT_NEWER -> R.string.update_install_old_version
                    ApkValidationResult.SIGNER_MISMATCH -> R.string.update_install_signer_mismatch
                    ApkValidationResult.VALID -> error("Handled above")
                    null -> R.string.update_install_invalid_apk
                }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !packageManager.canRequestPackageInstalls()) {
            requestUnknownSourcePermission()
        } else {
            launchPackageInstaller()
        }
    }

    private fun canInstallPackages(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || packageManager.canRequestPackageInstalls()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestUnknownSourcePermission() {
        val intent =
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                "package:$packageName".toUri(),
            )
        @Suppress("DEPRECATION")
        startActivityForResult(intent, UNKNOWN_SOURCE_REQUEST_CODE)
    }

    private fun launchPackageInstaller() {
        val stored = updateManager.storedDownload(downloadId)
        if (stored == null) {
            showInvalidDownload()
            return
        }
        val contentUri =
            FileProvider.getUriForFile(
                this,
                "$packageName.app-update-files",
                File(stored.filePath),
            )
        val installIntent =
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(contentUri, AndroidAppUpdateDownloadManager.APK_MIME_TYPE)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        if (installIntent.resolveActivity(packageManager) == null) {
            Toast.makeText(this, R.string.update_install_unavailable, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        AppUpdateNotificationPublisher.cancel(this)
        startActivity(installIntent)
        finish()
    }

    private fun showInvalidDownload() {
        Toast.makeText(this, R.string.update_install_invalid_apk, Toast.LENGTH_LONG).show()
        finish()
    }

    companion object {
        private const val EXTRA_DOWNLOAD_ID = "download_id"
        private const val UNKNOWN_SOURCE_REQUEST_CODE = 7_301

        fun intent(
            context: Context,
            downloadId: Long,
        ): Intent =
            Intent(context, AppUpdateInstallActivity::class.java)
                .putExtra(EXTRA_DOWNLOAD_ID, downloadId)
    }
}
