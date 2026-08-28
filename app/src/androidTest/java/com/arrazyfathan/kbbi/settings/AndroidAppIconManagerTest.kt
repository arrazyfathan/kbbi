package com.arrazyfathan.kbbi.settings

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import androidx.test.platform.app.InstrumentationRegistry
import com.arrazyfathan.kbbi.MainActivity
import com.arrazyfathan.kbbi.feature.settings.domain.model.AppIcon
import com.arrazyfathan.kbbi.feature.settings.domain.service.AppIconChangeResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidAppIconManagerTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val packageManager = context.packageManager
    private val manager = AndroidAppIconManager(context)

    @Test
    fun switchingIcon_enablesOnlyRequestedAlias_andAllAliasesTargetMainActivity() =
        runBlocking {
            try {
                assertEquals(
                    AppIconChangeResult.SUCCESS,
                    manager.changeIcon(AppIcon.ROYAL_OCEAN),
                )

                AppIcon.entries.forEach { icon ->
                    assertEquals(
                        if (icon == AppIcon.ROYAL_OCEAN) {
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        } else {
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        },
                        packageManager.getComponentEnabledSetting(componentName(icon)),
                    )
                    assertEquals(
                        MainActivity::class.java.name,
                        packageManager.getActivityInfo(componentName(icon), 0).targetActivity,
                    )
                }

                val launcherIntent =
                    Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER)
                        .setPackage(context.packageName)
                val launcherActivities = packageManager.queryIntentActivities(launcherIntent, 0)
                assertEquals(1, launcherActivities.size)
                assertEquals(
                    componentName(AppIcon.ROYAL_OCEAN).className,
                    launcherActivities.single().activityInfo.name,
                )
            } finally {
                assertEquals(
                    AppIconChangeResult.SUCCESS,
                    manager.changeIcon(AppIcon.DEFAULT),
                )
            }
        }

    private fun componentName(icon: AppIcon): ComponentName = appIconComponentName(context.packageName, icon)
}
