package com.arrazyfathan.kbbi.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.arrazyfathan.kbbi.feature.settings.domain.service.NotificationPermissionGateway

class AndroidNotificationPermissionGateway(
    private val context: Context,
) : NotificationPermissionGateway {
    override val isRuntimePermissionRequired: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    override fun isGranted(): Boolean =
        !isRuntimePermissionRequired ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}
