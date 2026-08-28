package com.arrazyfathan.kbbi.settings

import android.content.pm.PackageManager
import com.arrazyfathan.kbbi.feature.settings.domain.model.AppIcon
import org.junit.Assert.assertEquals
import org.junit.Test

class AppIconStateResolverTest {
    @Test
    fun `component names use application id and permanent alias class for every flavor`() {
        val production = appIconComponentSpec("com.arrazyfathan.kbbi", AppIcon.ROYAL_OCEAN)
        val development = appIconComponentSpec("com.arrazyfathan.kbbi.dev", AppIcon.ROYAL_OCEAN)

        assertEquals("com.arrazyfathan.kbbi", production.applicationId)
        assertEquals("com.arrazyfathan.kbbi.dev", development.applicationId)
        assertEquals("com.arrazyfathan.kbbi.launcher.RoyalOceanIconAlias", production.className)
        assertEquals(production.className, development.className)
    }

    @Test
    fun `default alias enabled resolves default icon`() {
        val states = disabledStates().toMutableMap()
        states[AppIcon.DEFAULT] = PackageManager.COMPONENT_ENABLED_STATE_DEFAULT

        assertEquals(AppIcon.DEFAULT, resolveCurrentAppIcon(states))
    }

    @Test
    fun `alternate alias enabled resolves alternate icon`() {
        val states = disabledStates().toMutableMap()
        states[AppIcon.ROYAL_OCEAN] = PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        assertEquals(AppIcon.ROYAL_OCEAN, resolveCurrentAppIcon(states))
    }

    @Test
    fun `no known alias enabled falls back to default icon`() {
        assertEquals(AppIcon.DEFAULT, resolveCurrentAppIcon(disabledStates()))
    }

    @Test
    fun `multiple aliases enabled falls back to default icon`() {
        val states = disabledStates().toMutableMap()
        states[AppIcon.DEFAULT] = PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        states[AppIcon.NEON_VIOLET] = PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        assertEquals(AppIcon.DEFAULT, resolveCurrentAppIcon(states))
    }

    private fun disabledStates(): Map<AppIcon, Int> =
        AppIcon.entries.associateWith { PackageManager.COMPONENT_ENABLED_STATE_DISABLED }
}
