package com.arrazyfathan.kbbi.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavigationTransitionTest {
    @Test
    fun `top-level navigation follows tab order`() {
        assertMotion(
            initial = Screen.Home,
            target = Screen.WordList,
            expected = AppNavigationMotion.TopLevelForward,
        )
        assertMotion(
            initial = Screen.WordList,
            target = Screen.Bookmarks,
            expected = AppNavigationMotion.TopLevelForward,
        )
        assertMotion(
            initial = Screen.Bookmarks,
            target = Screen.Home,
            expected = AppNavigationMotion.TopLevelBackward,
        )
        assertMotion(
            initial = Screen.WordList,
            target = Screen.Home,
            expected = AppNavigationMotion.TopLevelBackward,
        )
    }

    @Test
    fun `deeper destinations use hierarchical push motion`() {
        assertMotion(
            initial = Screen.Home,
            target = DetailNavRoute(dataJson = "word"),
            expected = AppNavigationMotion.HierarchicalPush,
        )
        assertMotion(
            initial = Screen.Settings,
            target = PrivacyPolicyRoute,
            expected = AppNavigationMotion.HierarchicalPush,
        )
    }

    @Test
    fun `back and predictive back use hierarchical pop motion`() {
        assertMotion(
            initial = DetailNavRoute(dataJson = "word"),
            target = Screen.Home,
            isPop = true,
            expected = AppNavigationMotion.HierarchicalPop,
        )
        assertMotion(
            initial = TermsConditionsRoute,
            target = Screen.Settings,
            isPop = true,
            expected = AppNavigationMotion.HierarchicalPop,
        )
    }

    @Test
    fun `top-level back still follows tab direction`() {
        assertMotion(
            initial = Screen.Bookmarks,
            target = Screen.Home,
            isPop = true,
            expected = AppNavigationMotion.TopLevelBackward,
        )
    }

    @Test
    fun `unknown content keys fall back to hierarchical motion`() {
        assertEquals(
            AppNavigationMotion.HierarchicalPush,
            resolveAppNavigationMotion("future-a", "future-b", isPop = false),
        )
        assertEquals(
            AppNavigationMotion.HierarchicalPop,
            resolveAppNavigationMotion("future-b", "future-a", isPop = true),
        )
    }

    private fun assertMotion(
        initial: NavKey,
        target: NavKey,
        expected: AppNavigationMotion,
        isPop: Boolean = false,
    ) {
        assertEquals(
            expected,
            resolveAppNavigationMotion(
                initialContentKey = initial.toAppNavigationContentKey(),
                targetContentKey = target.toAppNavigationContentKey(),
                isPop = isPop,
            ),
        )
    }
}
