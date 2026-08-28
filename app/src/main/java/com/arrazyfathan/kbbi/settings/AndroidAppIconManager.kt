package com.arrazyfathan.kbbi.settings

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.arrazyfathan.kbbi.feature.settings.domain.model.AppIcon
import com.arrazyfathan.kbbi.feature.settings.domain.service.AppIconChangeResult
import com.arrazyfathan.kbbi.feature.settings.domain.service.AppIconManager

class AndroidAppIconManager(
    context: Context,
) : AppIconManager {
    private val packageManager = context.packageManager
    private val packageName = context.packageName

    override fun currentIcon(): AppIcon {
        val states = componentStates()
        val current = resolveCurrentAppIcon(states)
        if (!hasExactlyOneEnabledIcon(states)) {
            runCatching { applyIcon(AppIcon.DEFAULT) }
        }
        return current
    }

    override suspend fun changeIcon(icon: AppIcon): AppIconChangeResult {
        val previousStates = componentStates()
        if (resolveEnabledIcons(previousStates) == listOf(icon)) {
            return AppIconChangeResult.SUCCESS
        }

        return try {
            applyIcon(icon)
            check(resolveEnabledIcons(componentStates()) == listOf(icon))
            AppIconChangeResult.SUCCESS
        } catch (_: Exception) {
            runCatching { restoreStates(previousStates) }
            AppIconChangeResult.FAILURE
        }
    }

    private fun applyIcon(icon: AppIcon) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.setComponentEnabledSettings(
                AppIcon.entries.map { candidate ->
                    PackageManager.ComponentEnabledSetting(
                        componentName(candidate),
                        if (candidate == icon) {
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        } else {
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        },
                        PackageManager.DONT_KILL_APP,
                    )
                },
            )
        } else {
            setState(icon, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
            AppIcon.entries
                .filterNot { it == icon }
                .forEach { setState(it, PackageManager.COMPONENT_ENABLED_STATE_DISABLED) }
        }
    }

    private fun restoreStates(states: Map<AppIcon, Int>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.setComponentEnabledSettings(
                states.map { (icon, state) ->
                    PackageManager.ComponentEnabledSetting(
                        componentName(icon),
                        state,
                        PackageManager.DONT_KILL_APP,
                    )
                },
            )
        } else {
            resolveEnabledIcons(states).forEach { icon ->
                setState(icon, states.getValue(icon))
            }
            states
                .filterKeys { it !in resolveEnabledIcons(states) }
                .forEach { (icon, state) -> setState(icon, state) }
        }
    }

    private fun componentStates(): Map<AppIcon, Int> =
        AppIcon.entries.associateWith { icon ->
            packageManager.getComponentEnabledSetting(componentName(icon))
        }

    private fun setState(
        icon: AppIcon,
        state: Int,
    ) {
        packageManager.setComponentEnabledSetting(
            componentName(icon),
            state,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun componentName(icon: AppIcon): ComponentName = appIconComponentName(packageName, icon)
}

internal fun appIconComponentName(
    applicationId: String,
    icon: AppIcon,
): ComponentName =
    appIconComponentSpec(applicationId, icon).let { spec ->
        ComponentName(spec.applicationId, spec.className)
    }

internal fun appIconComponentSpec(
    applicationId: String,
    icon: AppIcon,
): AppIconComponentSpec = AppIconComponentSpec(applicationId, icon.aliasClassName)

internal data class AppIconComponentSpec(
    val applicationId: String,
    val className: String,
)

internal fun resolveCurrentAppIcon(states: Map<AppIcon, Int>): AppIcon =
    resolveEnabledIcons(states).singleOrNull() ?: AppIcon.DEFAULT

internal fun hasExactlyOneEnabledIcon(states: Map<AppIcon, Int>): Boolean = resolveEnabledIcons(states).size == 1

internal fun resolveEnabledIcons(states: Map<AppIcon, Int>): List<AppIcon> =
    AppIcon.entries.filter { icon ->
        when (states[icon] ?: PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> icon == AppIcon.DEFAULT
            else -> false
        }
    }

internal val AppIcon.aliasClassName: String
    get() =
        when (this) {
            AppIcon.DEFAULT -> "com.arrazyfathan.kbbi.launcher.DefaultIconAlias"
            AppIcon.ROYAL_OCEAN -> "com.arrazyfathan.kbbi.launcher.RoyalOceanIconAlias"
            AppIcon.GOLDEN_SUNSET -> "com.arrazyfathan.kbbi.launcher.GoldenSunsetIconAlias"
            AppIcon.GOLDEN_CORAL_ENERGY -> "com.arrazyfathan.kbbi.launcher.GoldenCoralEnergyIconAlias"
            AppIcon.DEEP_FOREST_ENERGY -> "com.arrazyfathan.kbbi.launcher.DeepForestEnergyIconAlias"
            AppIcon.NEON_VIOLET -> "com.arrazyfathan.kbbi.launcher.NeonVioletIconAlias"
            AppIcon.BLAZE_ORANGE -> "com.arrazyfathan.kbbi.launcher.BlazeOrangeIconAlias"
        }
