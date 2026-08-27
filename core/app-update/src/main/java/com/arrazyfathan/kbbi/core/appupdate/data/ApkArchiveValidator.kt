package com.arrazyfathan.kbbi.core.appupdate.data

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.pm.PackageInfoCompat
import com.arrazyfathan.kbbi.core.appupdate.domain.ApkIdentity
import com.arrazyfathan.kbbi.core.appupdate.domain.ApkIdentityValidator
import com.arrazyfathan.kbbi.core.appupdate.domain.ApkValidationResult
import java.io.File
import java.security.MessageDigest

internal class ApkArchiveValidator(
    private val context: Context,
) {
    fun validate(file: File): ApkValidationResult? {
        val installed = packageInfo(context.packageName) ?: return null
        val candidate = archivePackageInfo(file) ?: return null
        return ApkIdentityValidator.validate(installed.toIdentity(), candidate.toIdentity())
    }

    private fun packageInfo(packageName: String): PackageInfo? =
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            }
        }.getOrNull()

    private fun archivePackageInfo(file: File): PackageInfo? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageArchiveInfo(
                file.absolutePath,
                PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong()),
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
        }

    private fun PackageInfo.toIdentity(): ApkIdentity =
        ApkIdentity(
            packageName = packageName,
            versionCode = PackageInfoCompat.getLongVersionCode(this),
            signerDigests = signerDigests(),
        )

    private fun PackageInfo.signerDigests(): Set<String> {
        val signatures =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                signingInfo?.apkContentsSigners.orEmpty()
            } else {
                @Suppress("DEPRECATION")
                signatures.orEmpty()
            }
        return signatures.mapTo(mutableSetOf()) { signature ->
            MessageDigest
                .getInstance("SHA-256")
                .digest(signature.toByteArray())
                .joinToString(separator = "") { byte ->
                    (byte.toInt() and 0xff).toString(radix = 16).padStart(length = 2, padChar = '0')
                }
        }
    }
}
